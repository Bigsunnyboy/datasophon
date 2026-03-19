/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datasophon.api.strategy;

import com.datasophon.common.enums.DetectionStatus;
import com.datasophon.common.model.DetectionContext;
import com.datasophon.common.model.DetectionResult;
import com.datasophon.common.model.DiscoveredComponent;
import com.datasophon.common.model.ExistingComponentConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ApiQueryDetectionStrategy extends AbstractDetectionStrategy {
    
    @Override
    protected DetectionResult doDetect(DetectionContext context) throws Exception {
        DetectionResult result = new DetectionResult();
        result.setStartTime(new Date());
        List<DiscoveredComponent> discoveredComponents = new ArrayList<>();
        Map<String, Object> stats = new HashMap<>();
        
        ExistingComponentConfig config = context.getExistingComponentConfig();
        List<String> targetHosts = context.getTargetHosts();
        
        if (config == null) {
            throw new IllegalArgumentException("ExistingComponentConfig is required for API query detection");
        }
        
        List<ExistingComponentConfig.PortMapping> apiEndpoints = getApiEndpointsFromConfig(config);
        if (apiEndpoints.isEmpty()) {
            logger.warn("No API endpoints configured for service: {}", context.getServiceName());
            result.setStatus(DetectionStatus.COMPLETED);
            result.setDiscoveredComponents(discoveredComponents);
            result.setStats(stats);
            return result;
        }
        
        int totalQueries = 0;
        int successfulQueries = 0;
        int foundComponents = 0;
        
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(targetHosts.size() * apiEndpoints.size(), 20));
        List<Future<ApiCheckResult>> futures = new ArrayList<>();
        
        for (String host : targetHosts) {
            for (ExistingComponentConfig.PortMapping endpoint : apiEndpoints) {
                if (endpoint.getPort() == null) {
                    continue;
                }
                futures.add(executor.submit(new ApiCheckTask(host, endpoint.getPort(),
                        endpoint.getProtocol(), endpoint.getServiceRole(), context.getServiceName())));
                totalQueries++;
            }
        }
        
        executor.shutdown();
        
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
            
            for (Future<ApiCheckResult> future : futures) {
                try {
                    ApiCheckResult apiResult = future.get();
                    if (apiResult != null && apiResult.isAlive()) {
                        successfulQueries++;
                        
                        DiscoveredComponent component = createDiscoveredComponent(
                                apiResult.getHost(), apiResult.getPort(), apiResult.getServiceRole(),
                                apiResult.getServiceName(), "API_QUERY");
                        component.setHealthStatus("HEALTHY");
                        component.setConfidence(98);
                        component.setApiEndpoint(apiResult.getEndpoint());
                        component.setApiResponse(apiResult.getResponse());
                        discoveredComponents.add(component);
                        foundComponents++;
                        
                        logger.info("Found component via API query: host={}, port={}, role={}",
                                apiResult.getHost(), apiResult.getPort(), apiResult.getServiceRole());
                    }
                } catch (Exception e) {
                    logger.debug("API check failed: {}", e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("API query detection interrupted");
        }
        
        stats.put("totalQueries", totalQueries);
        stats.put("successfulQueries", successfulQueries);
        stats.put("foundComponents", foundComponents);
        stats.put("apiEndpoints", apiEndpoints.size());
        
        result.setStatus(DetectionStatus.COMPLETED);
        result.setDiscoveredComponents(discoveredComponents);
        result.setStats(stats);
        result.setEndTime(new Date());
        
        return result;
    }
    
    private List<ExistingComponentConfig.PortMapping> getApiEndpointsFromConfig(ExistingComponentConfig config) {
        List<ExistingComponentConfig.PortMapping> endpoints = new ArrayList<>();
        if (config != null && config.getExistingComponentSupport() != null
                && config.getExistingComponentSupport().getPortMappings() != null) {
            for (ExistingComponentConfig.PortMapping mapping : config.getExistingComponentSupport().getPortMappings()) {
                if (mapping.getPort() != null && isHttpProtocol(mapping.getProtocol())) {
                    endpoints.add(mapping);
                }
            }
        }
        return endpoints;
    }
    
    private boolean isHttpProtocol(String protocol) {
        return protocol != null && (protocol.equalsIgnoreCase("HTTP") ||
                protocol.equalsIgnoreCase("HTTPS") || protocol.equalsIgnoreCase("REST"));
    }
    
    private boolean checkApiEndpoint(String host, int port, String protocol) {
        String urlStr = String.format("%s://%s:%d",
                protocol != null && protocol.equalsIgnoreCase("HTTPS") ? "https" : "http",
                host, port);
        
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);
            
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 400) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String responseBody = response.toString();
                return responseBody.length() > 0;
            }
        } catch (Exception e) {
            logger.debug("API endpoint check failed for {}: {}", urlStr, e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public boolean supports(String detectionMethod) {
        return "API_QUERY".equalsIgnoreCase(detectionMethod);
    }
    
    @Override
    public String getStrategyName() {
        return "ApiQueryDetectionStrategy";
    }
    
    @Override
    public String[] getSupportedDetectionMethods() {
        return new String[]{"API_QUERY"};
    }
    
    @Override
    public String[] getSupportedServices() {
        return new String[]{"HDFS", "YARN", "SPARK", "HIVE", "HBASE", "ZOOKEEPER", "KAFKA", "FLINK", "TEZ", "KYUUBI"};
    }
    
    private static class ApiCheckTask implements Callable<ApiCheckResult> {
        private final String host;
        private final Integer port;
        private final String protocol;
        private final String serviceRole;
        private final String serviceName;
        
        ApiCheckTask(String host, Integer port, String protocol, String serviceRole, String serviceName) {
            this.host = host;
            this.port = port;
            this.protocol = protocol != null ? protocol : "HTTP";
            this.serviceRole = serviceRole != null ? serviceRole : "API_SERVICE";
            this.serviceName = serviceName;
        }
        
        @Override
        public ApiCheckResult call() {
            ApiQueryDetectionStrategy strategy = new ApiQueryDetectionStrategy();
            boolean alive = strategy.checkApiEndpoint(host, port, protocol);
            
            ApiCheckResult result = new ApiCheckResult();
            result.setHost(host);
            result.setPort(port);
            result.setProtocol(protocol);
            result.setServiceRole(serviceRole);
            result.setServiceName(serviceName);
            result.setAlive(alive);
            result.setEndpoint(String.format("%s://%s:%d", protocol.toLowerCase(), host, port));
            
            return result;
        }
    }
    
    private static class ApiCheckResult {
        private String host;
        private Integer port;
        private String protocol;
        private String serviceRole;
        private String serviceName;
        private boolean alive;
        private String endpoint;
        private String response;
        
        public String getHost() {
            return host;
        }
        public void setHost(String host) {
            this.host = host;
        }
        
        public Integer getPort() {
            return port;
        }
        public void setPort(Integer port) {
            this.port = port;
        }
        
        public String getProtocol() {
            return protocol;
        }
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
        
        public String getServiceRole() {
            return serviceRole;
        }
        public void setServiceRole(String serviceRole) {
            this.serviceRole = serviceRole;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        
        public boolean isAlive() {
            return alive;
        }
        public void setAlive(boolean alive) {
            this.alive = alive;
        }
        
        public String getEndpoint() {
            return endpoint;
        }
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public String getResponse() {
            return response;
        }
        public void setResponse(String response) {
            this.response = response;
        }
    }
    
}
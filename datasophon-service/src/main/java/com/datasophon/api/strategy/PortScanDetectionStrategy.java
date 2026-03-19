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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortScanDetectionStrategy extends AbstractDetectionStrategy {
    
    @Override
    protected DetectionResult doDetect(DetectionContext context) throws Exception {
        DetectionResult result = new DetectionResult();
        result.setStartTime(new Date());
        List<DiscoveredComponent> discoveredComponents = new ArrayList<>();
        Map<String, Object> stats = new HashMap<>();
        
        ExistingComponentConfig config = context.getExistingComponentConfig();
        List<String> targetHosts = context.getTargetHosts();
        Integer sshPort = context.getSshPort();
        String sshUser = context.getSshUser();
        
        if (config == null) {
            throw new IllegalArgumentException("ExistingComponentConfig is required for port scan detection");
        }
        
        List<Integer> portsToScan = getServicePortsFromConfig(config);
        if (portsToScan.isEmpty()) {
            logger.warn("No ports configured for service: {}", context.getServiceName());
            result.setStatus(DetectionStatus.COMPLETED);
            result.setDiscoveredComponents(discoveredComponents);
            result.setStats(stats);
            return result;
        }
        
        int totalScanned = 0;
        int foundComponents = 0;
        
        for (String host : targetHosts) {
            logger.info("Scanning ports on host: {}, ports: {}", host, portsToScan);
            
            Map<Integer, Boolean> portResults = scanPorts(host, portsToScan, 5000);
            totalScanned += portsToScan.size();
            
            for (Map.Entry<Integer, Boolean> entry : portResults.entrySet()) {
                Integer port = entry.getKey();
                Boolean open = entry.getValue();
                
                if (open) {
                    // Determine service role from port mapping
                    String serviceRole = getServiceRoleForPort(config, port);
                    DiscoveredComponent component = createDiscoveredComponent(
                            host, port, serviceRole, context.getServiceName(), "PORT_SCAN");
                    component.setHealthStatus("HEALTHY");
                    component.setConfidence(90);
                    discoveredComponents.add(component);
                    foundComponents++;
                    
                    logger.info("Found component: host={}, port={}, role={}", host, port, serviceRole);
                }
            }
        }
        
        stats.put("totalScanned", totalScanned);
        stats.put("foundComponents", foundComponents);
        stats.put("scanHosts", targetHosts.size());
        stats.put("scanPorts", portsToScan.size());
        
        result.setStatus(DetectionStatus.COMPLETED);
        result.setDiscoveredComponents(discoveredComponents);
        result.setStats(stats);
        result.setEndTime(new Date());
        
        return result;
    }
    
    @Override
    public boolean supports(String detectionMethod) {
        return "PORT_SCAN".equalsIgnoreCase(detectionMethod);
    }
    
    @Override
    public String getStrategyName() {
        return "PortScanDetectionStrategy";
    }
    
    @Override
    public String[] getSupportedDetectionMethods() {
        return new String[]{"PORT_SCAN"};
    }
    
    @Override
    public String[] getSupportedServices() {
        return new String[]{"HDFS", "YARN", "SPARK", "HIVE", "HBASE", "ZOOKEEPER", "KAFKA", "FLINK"};
    }
    
}
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

public class CustomScriptDetectionStrategy extends AbstractDetectionStrategy {
    
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
            throw new IllegalArgumentException("ExistingComponentConfig is required for custom script detection");
        }
        
        String detectionScript = getDetectionScriptFromConfig(config);
        if (detectionScript == null || detectionScript.trim().isEmpty()) {
            logger.warn("No detection script configured for service: {}", context.getServiceName());
            result.setStatus(DetectionStatus.COMPLETED);
            result.setDiscoveredComponents(discoveredComponents);
            result.setStats(stats);
            return result;
        }
        
        int totalExecuted = 0;
        int successfulExecutions = 0;
        int foundComponents = 0;
        
        for (String host : targetHosts) {
            logger.info("Executing custom detection script on host: {}", host);
            totalExecuted++;
            
            try {
                String scriptOutput = executeRemoteCommand(host, detectionScript, sshPort, sshUser);
                
                if (scriptOutput != null && !scriptOutput.trim().isEmpty()) {
                    successfulExecutions++;
                    
                    boolean componentDetected = parseScriptOutput(scriptOutput);
                    if (componentDetected) {
                        String serviceRole = extractServiceRoleFromScriptOutput(scriptOutput, config);
                        DiscoveredComponent component = createDiscoveredComponent(
                                host, null, serviceRole, context.getServiceName(), "CUSTOM_SCRIPT");
                        component.setHealthStatus("HEALTHY");
                        component.setConfidence(90);
                        component.setScriptOutput(scriptOutput.substring(0, Math.min(scriptOutput.length(), 1000)));
                        discoveredComponents.add(component);
                        foundComponents++;
                        
                        logger.info("Found component via custom script: host={}, role={}", host, serviceRole);
                    }
                }
            } catch (Exception e) {
                logger.warn("Custom script execution failed on host {}: {}", host, e.getMessage());
            }
        }
        
        stats.put("totalExecuted", totalExecuted);
        stats.put("successfulExecutions", successfulExecutions);
        stats.put("foundComponents", foundComponents);
        
        result.setStatus(DetectionStatus.COMPLETED);
        result.setDiscoveredComponents(discoveredComponents);
        result.setStats(stats);
        result.setEndTime(new Date());
        
        return result;
    }
    
    private String getDetectionScriptFromConfig(ExistingComponentConfig config) {
        if (config != null && config.getExistingComponentSupport() != null
                && config.getExistingComponentSupport().getServiceSpecificDetection() != null) {
            return config.getExistingComponentSupport().getServiceSpecificDetection().getDetectionScript();
        }
        return null;
    }
    
    private boolean parseScriptOutput(String scriptOutput) {
        if (scriptOutput == null || scriptOutput.trim().isEmpty()) {
            return false;
        }
        
        String normalizedOutput = scriptOutput.toLowerCase().trim();
        
        if (normalizedOutput.contains("true") || normalizedOutput.contains("success") ||
                normalizedOutput.contains("found") || normalizedOutput.contains("running") ||
                normalizedOutput.contains("active") || normalizedOutput.contains("ok")) {
            return true;
        }
        
        if (normalizedOutput.contains("false") || normalizedOutput.contains("fail") ||
                normalizedOutput.contains("not found") || normalizedOutput.contains("stopped") ||
                normalizedOutput.contains("inactive") || normalizedOutput.contains("error")) {
            return false;
        }
        
        return !normalizedOutput.contains("0");
    }
    
    private String extractServiceRoleFromScriptOutput(String scriptOutput, ExistingComponentConfig config) {
        if (config != null && config.getExistingComponentSupport() != null
                && config.getExistingComponentSupport().getPortMappings() != null) {
            for (ExistingComponentConfig.PortMapping mapping : config.getExistingComponentSupport().getPortMappings()) {
                if (mapping.getServiceRole() != null && scriptOutput.contains(mapping.getServiceRole())) {
                    return mapping.getServiceRole();
                }
            }
        }
        
        if (scriptOutput.contains("namenode") || scriptOutput.contains("hdfs")) {
            return "HDFS_NODE";
        } else if (scriptOutput.contains("resourcemanager") || scriptOutput.contains("yarn")) {
            return "YARN_NODE";
        } else if (scriptOutput.contains("hive") || scriptOutput.contains("metastore")) {
            return "HIVE_NODE";
        } else if (scriptOutput.contains("zookeeper") || scriptOutput.contains("zk")) {
            return "ZOOKEEPER_SERVER";
        } else if (scriptOutput.contains("kafka") || scriptOutput.contains("broker")) {
            return "KAFKA_BROKER";
        } else if (scriptOutput.contains("hbase")) {
            return "HBASE_NODE";
        } else if (scriptOutput.contains("spark")) {
            return "SPARK_NODE";
        } else if (scriptOutput.contains("flink")) {
            return "FLINK_NODE";
        }
        
        return "CUSTOM_SCRIPT_DETECTED";
    }
    
    @Override
    public boolean supports(String detectionMethod) {
        return "CUSTOM_SCRIPT".equalsIgnoreCase(detectionMethod);
    }
    
    @Override
    public String getStrategyName() {
        return "CustomScriptDetectionStrategy";
    }
    
    @Override
    public String[] getSupportedDetectionMethods() {
        return new String[]{"CUSTOM_SCRIPT"};
    }
    
    @Override
    public String[] getSupportedServices() {
        return new String[]{"HDFS", "YARN", "SPARK", "HIVE", "HBASE", "ZOOKEEPER", "KAFKA", "FLINK", "TEZ", "KYUUBI"};
    }
    
}
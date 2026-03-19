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

public class ProcessDetectionStrategy extends AbstractDetectionStrategy {
    
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
            throw new IllegalArgumentException("ExistingComponentConfig is required for process detection");
        }
        
        List<String> processPatterns = getProcessPatternsFromConfig(config);
        if (processPatterns.isEmpty()) {
            logger.warn("No process patterns configured for service: {}", context.getServiceName());
            result.setStatus(DetectionStatus.COMPLETED);
            result.setDiscoveredComponents(discoveredComponents);
            result.setStats(stats);
            return result;
        }
        
        int totalScanned = 0;
        int foundComponents = 0;
        
        for (String host : targetHosts) {
            logger.info("Detecting processes on host: {}, patterns: {}", host, processPatterns);
            
            String processList = executeRemoteCommand(host, "ps aux", sshPort, sshUser);
            if (processList == null || processList.trim().isEmpty()) {
                logger.warn("Failed to get process list from host: {}", host);
                continue;
            }
            
            String javaProcessList = "";
            try {
                javaProcessList = executeRemoteCommand(host, "jps -l", sshPort, sshUser);
            } catch (Exception e) {
                logger.debug("jps command not available on host: {}", host);
            }
            
            totalScanned++;
            
            for (String pattern : processPatterns) {
                boolean found = false;
                String serviceRole = pattern.replaceAll(".*", "").toUpperCase();
                
                if (containsProcess(processList, pattern)) {
                    found = true;
                }
                
                if (!found && !javaProcessList.isEmpty() && containsProcess(javaProcessList, pattern)) {
                    found = true;
                }
                
                if (found) {
                    DiscoveredComponent component = createDiscoveredComponent(
                            host, null, serviceRole, context.getServiceName(), "PROCESS_DETECTION");
                    component.setHealthStatus("HEALTHY");
                    component.setConfidence(85);
                    component.setProcessPattern(pattern);
                    discoveredComponents.add(component);
                    foundComponents++;
                    
                    logger.info("Found component via process detection: host={}, role={}, pattern={}",
                            host, serviceRole, pattern);
                }
            }
        }
        
        stats.put("totalHostsScanned", totalScanned);
        stats.put("foundComponents", foundComponents);
        stats.put("processPatterns", processPatterns.size());
        
        result.setStatus(DetectionStatus.COMPLETED);
        result.setDiscoveredComponents(discoveredComponents);
        result.setStats(stats);
        result.setEndTime(new Date());
        
        return result;
    }
    
    private boolean containsProcess(String processList, String pattern) {
        if (processList == null || pattern == null) {
            return false;
        }
        return processList.toLowerCase().contains(pattern.toLowerCase());
    }
    
    @Override
    public boolean supports(String detectionMethod) {
        return "PROCESS_DETECTION".equalsIgnoreCase(detectionMethod);
    }
    
    @Override
    public String getStrategyName() {
        return "ProcessDetectionStrategy";
    }
    
    @Override
    public String[] getSupportedDetectionMethods() {
        return new String[]{"PROCESS_DETECTION"};
    }
    
    @Override
    public String[] getSupportedServices() {
        
        return new String[]{"HDFS", "YARN", "SPARK", "HIVE", "HBASE", "ZOOKEEPER", "KAFKA", "FLINK", "TEZ", "KYUUBI"};
    }
    
}
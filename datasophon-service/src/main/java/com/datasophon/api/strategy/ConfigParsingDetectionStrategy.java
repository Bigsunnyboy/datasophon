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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParsingDetectionStrategy extends AbstractDetectionStrategy {
    
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
            throw new IllegalArgumentException("ExistingComponentConfig is required for config parsing detection");
        }
        
        List<ExistingComponentConfig.ConfigFilePath> configFilePaths = getConfigPathsFromConfig(config);
        if (configFilePaths.isEmpty()) {
            logger.warn("No config file paths configured for service: {}", context.getServiceName());
            result.setStatus(DetectionStatus.COMPLETED);
            result.setDiscoveredComponents(discoveredComponents);
            result.setStats(stats);
            return result;
        }
        
        int totalScanned = 0;
        int foundComponents = 0;
        int filesFound = 0;
        int filesParsed = 0;
        
        for (String host : targetHosts) {
            logger.info("Parsing config files on host: {}, config files: {}", host, configFilePaths);
            
            for (ExistingComponentConfig.ConfigFilePath configFilePath : configFilePaths) {
                String remotePath = configFilePath.getPath();
                totalScanned++;
                
                try {
                    String fileContent = executeRemoteCommand(host,
                            String.format("cat \"%s\" 2>/dev/null || echo 'FILE_NOT_FOUND'", remotePath),
                            sshPort, sshUser);
                    
                    if (fileContent != null && !fileContent.contains("FILE_NOT_FOUND") && !fileContent.trim().isEmpty()) {
                        filesFound++;
                        
                        boolean configValid = validateConfigFile(fileContent, configFilePath.getConfigType(),
                                config.getExistingComponentSupport().getServiceSpecificDetection());
                        
                        if (configValid) {
                            filesParsed++;
                            
                            String serviceRole = determineServiceRoleFromConfig(remotePath, fileContent, config);
                            DiscoveredComponent component = createDiscoveredComponent(
                                    host, null, serviceRole, context.getServiceName(), "CONFIG_PARSING");
                            component.setHealthStatus("HEALTHY");
                            component.setConfidence(95);
                            component.setConfigFilePath(remotePath);
                            component.setConfigType(configFilePath.getConfigType());
                            discoveredComponents.add(component);
                            foundComponents++;
                            
                            logger.info("Found component via config parsing: host={}, role={}, file={}",
                                    host, serviceRole, remotePath);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Failed to read config file {} on host {}: {}", remotePath, host, e.getMessage());
                }
            }
        }
        
        stats.put("totalScanned", totalScanned);
        stats.put("foundComponents", foundComponents);
        stats.put("filesFound", filesFound);
        stats.put("filesParsed", filesParsed);
        stats.put("configFilePaths", configFilePaths.size());
        
        result.setStatus(DetectionStatus.COMPLETED);
        result.setDiscoveredComponents(discoveredComponents);
        result.setStats(stats);
        result.setEndTime(new Date());
        
        return result;
    }
    
    private List<ExistingComponentConfig.ConfigFilePath> getConfigPathsFromConfig(ExistingComponentConfig config) {
        List<ExistingComponentConfig.ConfigFilePath> paths = new ArrayList<>();
        if (config != null && config.getExistingComponentSupport() != null
                && config.getExistingComponentSupport().getConfigFilePaths() != null) {
            paths.addAll(config.getExistingComponentSupport().getConfigFilePaths());
        }
        return paths;
    }
    
    private boolean validateConfigFile(String content, String configType,
                                       ExistingComponentConfig.ServiceSpecificDetection detectionConfig) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        if (detectionConfig != null && detectionConfig.getExpectedConfigPatterns() != null) {
            for (String pattern : detectionConfig.getExpectedConfigPatterns()) {
                Pattern regex = Pattern.compile(pattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher matcher = regex.matcher(content);
                if (!matcher.find()) {
                    return false;
                }
            }
            return true;
        }
        
        return !content.contains("FILE_NOT_FOUND");
    }
    
    private String determineServiceRoleFromConfig(String filePath, String content, ExistingComponentConfig config) {
        String filename = filePath.substring(filePath.lastIndexOf('/') + 1);
        
        if (config != null && config.getExistingComponentSupport() != null
                && config.getExistingComponentSupport().getPortMappings() != null) {
            for (ExistingComponentConfig.PortMapping mapping : config.getExistingComponentSupport().getPortMappings()) {
                if (mapping.getServiceRole() != null && content.contains(mapping.getServiceRole())) {
                    return mapping.getServiceRole();
                }
            }
        }
        
        if (filename.contains("hdfs-site.xml") || content.contains("namenode") || content.contains("datanode")) {
            return "HDFS_NODE";
        } else if (filename.contains("yarn-site.xml") || content.contains("resourcemanager") || content.contains("nodemanager")) {
            return "YARN_NODE";
        } else if (filename.contains("core-site.xml") || content.contains("fs.defaultFS")) {
            return "HDFS_CORE";
        } else if (filename.contains("hive-site.xml") || content.contains("hive.metastore") || content.contains("hiveserver2")) {
            return "HIVE_NODE";
        } else if (filename.contains("zoo.cfg") || content.contains("zookeeper")) {
            return "ZOOKEEPER_SERVER";
        } else if (filename.contains("kafka") || content.contains("broker.id")) {
            return "KAFKA_BROKER";
        } else if (filename.contains("hbase-site.xml") || content.contains("hbase.master") || content.contains("hbase.regionserver")) {
            return "HBASE_NODE";
        }
        
        return "CONFIG_BASED";
    }
    
    @Override
    public boolean supports(String detectionMethod) {
        return "CONFIG_PARSING".equalsIgnoreCase(detectionMethod);
    }
    
    @Override
    public String getStrategyName() {
        return "ConfigParsingDetectionStrategy";
    }
    
    @Override
    public String[] getSupportedDetectionMethods() {
        return new String[]{"CONFIG_PARSING"};
    }
    
    @Override
    public String[] getSupportedServices() {
        return new String[]{"HDFS", "YARN", "SPARK", "HIVE", "HBASE", "ZOOKEEPER", "KAFKA", "FLINK", "TEZ", "KYUUBI"};
    }
    
}
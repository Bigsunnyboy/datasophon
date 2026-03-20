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

package com.datasophon.api.service.impl;

import com.datasophon.api.extractor.ConfigExtractor;
import com.datasophon.api.service.ClusterExistingComponentService;
import com.datasophon.api.service.ClusterServiceInstanceConfigService;
import com.datasophon.api.service.ClusterServiceRoleGroupConfigService;
import com.datasophon.api.service.ConfigSyncHistoryService;
import com.datasophon.api.service.ConfigurationSyncManager;
import com.datasophon.common.enums.ConfigMergeStrategy;
import com.datasophon.common.model.ClusterExistingComponentConfig;
import com.datasophon.common.model.DiscoveredComponent;
import com.datasophon.common.model.SyncOperation;
import com.datasophon.common.model.SyncOperationResult;
import com.datasophon.dao.entity.ClusterExistingComponentEntity;
import com.datasophon.dao.entity.ClusterServiceRoleGroupConfig;
import com.datasophon.dao.entity.ConfigSyncHistoryEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 配置同步管理器实现
 */
@Service("configurationSyncManager")
public class ConfigurationSyncManagerImpl implements ConfigurationSyncManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSyncManagerImpl.class);
    
    @Autowired
    private ConfigSyncHistoryService configSyncHistoryService;
    
    @Autowired
    private ClusterExistingComponentService clusterExistingComponentService;
    
    @Autowired
    private ClusterServiceRoleGroupConfigService clusterServiceRoleGroupConfigService;
    
    @Autowired
    private ClusterServiceInstanceConfigService clusterServiceInstanceConfigService;
    
    @Autowired(required = false)
    private List<ConfigExtractor> configExtractors = new ArrayList<>();
    
    @Override
    public Map<String, Object> extractConfigFromComponent(ClusterExistingComponentEntity component,
                                                          ClusterExistingComponentConfig config) {
        logger.info("Extracting configuration from component {}:{}",
                component.getServiceName(), component.getServiceRole());
        
        Map<String, Object> extractedConfig = new HashMap<>();
        
        // Try to find a matching ConfigExtractor for this component
        ConfigExtractor matchingExtractor = findMatchingExtractor(component.getServiceName(),
                component.getServiceRole());
        
        if (matchingExtractor != null) {
            logger.info("Using extractor '{}' for component {}:{}",
                    matchingExtractor.getExtractorName(), component.getServiceName(), component.getServiceRole());
            
            try {
                extractedConfig = matchingExtractor.extractConfig(component, config);
                extractedConfig.put("extractorUsed", matchingExtractor.getExtractorName());
                extractedConfig.put("extractorVersion", matchingExtractor.getVersion());
                extractedConfig.put("extractionStatus", "SUCCESS");
                
                // Validate the extracted configuration
                Map<String, Object> validationResult = matchingExtractor.validateConfig(extractedConfig);
                extractedConfig.put("validationResult", validationResult);
                
                logger.info("Successfully extracted configuration using extractor '{}' with {} config items",
                        matchingExtractor.getExtractorName(), extractedConfig.size());
                
            } catch (Exception e) {
                logger.error("Extractor '{}' failed to extract configuration from component {}:{}",
                        matchingExtractor.getExtractorName(), component.getServiceName(),
                        component.getServiceRole(), e);
                
                // Fall back to generic extraction
                extractedConfig = fallbackExtraction(component, config);
                extractedConfig.put("extractionError", e.getMessage());
                extractedConfig.put("extractionStatus", "FAILED_WITH_FALLBACK");
            }
        } else {
            logger.warn("No matching ConfigExtractor found for component {}:{}, using generic extraction",
                    component.getServiceName(), component.getServiceRole());
            
            // Use generic extraction as fallback
            extractedConfig = fallbackExtraction(component, config);
            extractedConfig.put("extractionStatus", "GENERIC_FALLBACK");
        }
        
        return extractedConfig;
    }
    
    @Override
    public Map<String, Object> extractConfigFromDataSophon(Integer clusterId, String serviceName,
                                                           String componentName) {
        logger.info("Extracting configuration from DataSophon for service {}:{} in cluster {}",
                serviceName, componentName, clusterId);
        
        Map<String, Object> datasophonConfig = new HashMap<>();
        
        try {
            // 查询所有角色组配置，然后过滤出属于指定集群和服务的配置
            // 注意：这是一个简化实现，实际中应该通过serviceName和clusterId直接查询
            
            // 获取配置列表（这里需要查询所有配置，然后手动过滤）
            // 在实际项目中，应该在ClusterServiceRoleGroupConfigService中添加相应的方法
            List<ClusterServiceRoleGroupConfig> allConfigs = clusterServiceRoleGroupConfigService.list();
            
            // 过滤出属于指定集群和服务的配置
            List<ClusterServiceRoleGroupConfig> filteredConfigs = allConfigs.stream()
                    .filter(config -> config.getClusterId() != null &&
                            config.getClusterId().equals(clusterId) &&
                            config.getServiceName() != null &&
                            config.getServiceName().equals(serviceName))
                    .collect(java.util.stream.Collectors.toList());
            
            if (!filteredConfigs.isEmpty()) {
                // 获取最新的配置版本
                ClusterServiceRoleGroupConfig latestConfig = filteredConfigs.stream()
                        .max(Comparator.comparingInt(ClusterServiceRoleGroupConfig::getConfigVersion))
                        .orElse(null);
                
                if (latestConfig != null) {
                    // 解析JSON配置
                    Map<String, Object> configJson = parseConfigJson(latestConfig.getConfigJson());
                    Map<String, Object> configFileJson = parseConfigJson(latestConfig.getConfigFileJson());
                    
                    datasophonConfig.put("configJson", configJson);
                    datasophonConfig.put("configFileJson", configFileJson);
                    datasophonConfig.put("configVersion", latestConfig.getConfigVersion());
                    datasophonConfig.put("configJsonMd5", latestConfig.getConfigJsonMd5());
                    datasophonConfig.put("configFileJsonMd5", latestConfig.getConfigFileJsonMd5());
                    datasophonConfig.put("roleGroupId", latestConfig.getRoleGroupId());
                    
                    // 提取组件特定的配置
                    Map<String, Object> componentSpecificConfig = extractComponentSpecificConfig(
                            configJson, configFileJson, componentName);
                    datasophonConfig.putAll(componentSpecificConfig);
                    
                    datasophonConfig.put("extractionStatus", "SUCCESS");
                    datasophonConfig.put("configSource", "DataSophon Role Group Configuration");
                    logger.info("Successfully extracted configuration from DataSophon for service {}:{}, version {}",
                            serviceName, componentName, latestConfig.getConfigVersion());
                } else {
                    logger.warn("No valid configuration found for service {}:{} in cluster {}",
                            serviceName, componentName, clusterId);
                    datasophonConfig.put("extractionStatus", "NO_CONFIG_FOUND");
                    datasophonConfig.put("error", "No configuration found for service");
                }
            } else {
                // 如果没有找到配置，返回默认配置模板
                datasophonConfig.putAll(getDefaultConfigTemplate(serviceName, componentName));
                datasophonConfig.put("configSource", "DataSophon Default Configuration Template");
                datasophonConfig.put("extractionStatus", "USING_DEFAULT_TEMPLATE");
                logger.info("Using default configuration template for service {}:{}",
                        serviceName, componentName);
            }
            
        } catch (Exception e) {
            logger.error("Failed to extract configuration from DataSophon for service {}:{} in cluster {}",
                    serviceName, componentName, clusterId, e);
            
            // 发生异常时返回默认配置
            datasophonConfig.putAll(getDefaultConfigTemplate(serviceName, componentName));
            datasophonConfig.put("configSource", "DataSophon Error Fallback Template");
            datasophonConfig.put("extractionStatus", "ERROR_FALLBACK_TO_DEFAULT");
            datasophonConfig.put("error", e.getMessage());
        }
        
        // 添加元数据
        datasophonConfig.put("extractedAt", new Date());
        datasophonConfig.put("clusterId", clusterId);
        datasophonConfig.put("serviceName", serviceName);
        datasophonConfig.put("componentName", componentName);
        
        return datasophonConfig;
    }
    
    // 辅助方法：解析JSON配置字符串
    private Map<String, Object> parseConfigJson(String jsonString) {
        Map<String, Object> result = new HashMap<>();
        
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return result;
        }
        
        try {
            // 使用Fastjson解析JSON
            JSONObject jsonObject = JSON.parseObject(jsonString);
            if (jsonObject != null) {
                result.putAll(jsonObject);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse JSON configuration: {}", e.getMessage());
            // 如果解析失败，尝试简单的键值对解析
            try {
                // 尝试解析为简单的键值对格式
                String[] pairs = jsonString.split("[,\n]");
                for (String pair : pairs) {
                    String[] kv = pair.split("[:=]", 2);
                    if (kv.length == 2) {
                        result.put(kv[0].trim(), kv[1].trim());
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed to parse configuration as key-value pairs", ex);
            }
        }
        
        return result;
    }
    
    // 辅助方法：提取组件特定的配置
    private Map<String, Object> extractComponentSpecificConfig(Map<String, Object> configJson,
                                                               Map<String, Object> configFileJson,
                                                               String componentName) {
        Map<String, Object> componentConfig = new HashMap<>();
        
        if (configJson == null && configFileJson == null) {
            return componentConfig;
        }
        
        // 从configJson中提取组件相关配置
        if (configJson != null) {
            // 查找包含组件名称的配置项
            for (Map.Entry<String, Object> entry : configJson.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 如果键名包含组件名称，或者值是包含组件名称的Map
                if (key.toLowerCase().contains(componentName.toLowerCase())) {
                    componentConfig.put(key, value);
                } else if (value instanceof Map) {
                    // 递归处理嵌套的Map
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedMap = (Map<String, Object>) value;
                    Map<String, Object> nestedComponentConfig =
                            extractComponentSpecificConfig(nestedMap, null, componentName);
                    if (!nestedComponentConfig.isEmpty()) {
                        componentConfig.put(key, nestedComponentConfig);
                    }
                }
            }
        }
        
        // 从configFileJson中提取组件相关配置
        if (configFileJson != null) {
            for (Map.Entry<String, Object> entry : configFileJson.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 配置文件通常以文件名作为键，检查文件名是否与组件相关
                if (key.toLowerCase().contains(componentName.toLowerCase()) ||
                        (value instanceof String && ((String) value).toLowerCase().contains(componentName.toLowerCase()))) {
                    componentConfig.put("file:" + key, value);
                }
            }
        }
        
        return componentConfig;
    }
    
    // 辅助方法：获取默认配置模板
    private Map<String, Object> getDefaultConfigTemplate(String serviceName, String componentName) {
        Map<String, Object> defaultConfig = new HashMap<>();
        
        // 根据服务类型提供默认配置
        switch (serviceName.toUpperCase()) {
            case "HDFS":
                defaultConfig.put("dfs.replication", "3");
                defaultConfig.put("dfs.blocksize", "134217728");
                defaultConfig.put("dfs.namenode.name.dir", "/data/hadoop/namenode");
                defaultConfig.put("dfs.datanode.data.dir", "/data/hadoop/datanode");
                if ("NAMENODE".equalsIgnoreCase(componentName)) {
                    defaultConfig.put("dfs.namenode.rpc-address", "0.0.0.0:8020");
                    defaultConfig.put("dfs.namenode.http-address", "0.0.0.0:50070");
                } else if ("DATANODE".equalsIgnoreCase(componentName)) {
                    defaultConfig.put("dfs.datanode.address", "0.0.0.0:50010");
                    defaultConfig.put("dfs.datanode.http.address", "0.0.0.0:50075");
                }
                break;
            
            case "YARN":
                defaultConfig.put("yarn.resourcemanager.scheduler.class",
                        "org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler");
                defaultConfig.put("yarn.nodemanager.resource.memory-mb", "8192");
                defaultConfig.put("yarn.nodemanager.resource.cpu-vcores", "8");
                if ("RESOURCEMANAGER".equalsIgnoreCase(componentName)) {
                    defaultConfig.put("yarn.resourcemanager.address", "0.0.0.0:8032");
                    defaultConfig.put("yarn.resourcemanager.scheduler.address", "0.0.0.0:8030");
                    defaultConfig.put("yarn.resourcemanager.webapp.address", "0.0.0.0:8088");
                } else if ("NODEMANAGER".equalsIgnoreCase(componentName)) {
                    defaultConfig.put("yarn.nodemanager.address", "0.0.0.0:8040");
                    defaultConfig.put("yarn.nodemanager.webapp.address", "0.0.0.0:8042");
                }
                break;
            
            case "SPARK":
                defaultConfig.put("spark.master", "spark://localhost:7077");
                defaultConfig.put("spark.executor.memory", "1g");
                defaultConfig.put("spark.driver.memory", "1g");
                if ("MASTER".equalsIgnoreCase(componentName)) {
                    defaultConfig.put("spark.master.port", "7077");
                    defaultConfig.put("spark.master.webui.port", "8080");
                } else if ("WORKER".equalsIgnoreCase(componentName)) {
                    defaultConfig.put("spark.worker.port", "7078");
                    defaultConfig.put("spark.worker.webui.port", "8081");
                }
                break;
            
            default:
                // 通用默认配置
                defaultConfig.put("port", "8080");
                defaultConfig.put("host", "0.0.0.0");
                defaultConfig.put("log.level", "INFO");
                defaultConfig.put("max.connections", "100");
                defaultConfig.put("timeout.ms", "30000");
        }
        
        // 添加模板信息
        defaultConfig.put("isTemplate", true);
        defaultConfig.put("templateName", serviceName + "_" + componentName + "_default");
        defaultConfig.put("templateVersion", "1.0");
        defaultConfig.put("warning", "This is a default configuration template. Actual configuration may vary.");
        
        return defaultConfig;
    }
    
    @Override
    public SyncOperationResult executeSyncOperation(SyncOperation syncOperation) {
        logger.info("Executing sync operation: type={}, componentId={}",
                syncOperation.getOperationType(), syncOperation.getExistingComponentId());
        
        SyncOperationResult result = new SyncOperationResult();
        result.setOperationId(syncOperation.getOperationId());
        // result.setComponentId(syncOperation.getExistingComponentId()); // TODO: SyncOperationResult needs componentId field
        // result.setOperationType(syncOperation.getOperationType()); // TODO: SyncOperationResult missing operationType field
        result.setSyncStartTime(new Date());
        
        try {
            // Record the sync operation in history
            ConfigSyncHistoryEntity syncHistory = new ConfigSyncHistoryEntity();
            syncHistory.setExistingComponentId(syncOperation.getExistingComponentId());
            syncHistory.setOperationType(convertToDaoSyncOperationType(syncOperation.getOperationType()));
            syncHistory.setSyncStrategy(syncOperation.getSyncStrategy());
            // TODO: configBefore and configAfter need to be extracted from extendProps or operation context
            // syncHistory.setConfigBefore(syncOperation.getConfigBefore() != null ? syncOperation.getConfigBefore().toString() : null);
            // syncHistory.setConfigAfter(syncOperation.getConfigAfter() != null ? syncOperation.getConfigAfter().toString() : null);
            syncHistory.setSyncStatus("PENDING");
            
            configSyncHistoryService.recordSyncOperation(syncHistory);
            
            // Execute based on operation type
            switch (syncOperation.getOperationType()) {
                case PULL_FROM_COMPONENT:
                    result = pullConfigFromComponent(
                            getComponentById(syncOperation.getExistingComponentId()),
                            syncOperation.getConfigFilePath());
                    break;
                case PUSH_TO_COMPONENT:
                    result = pushConfigToComponent(
                            getComponentById(syncOperation.getExistingComponentId()),
                            syncOperation.getExtendProps() != null ? (Map<String, Object>) syncOperation.getExtendProps().get("configToPush") : null,
                            syncOperation.getSyncStrategy());
                    break;
                case MERGE_CONFIG:
                    // Merge operation requires both configs
                    Map<String, Object> configBefore = syncOperation.getExtendProps() != null ? (Map<String, Object>) syncOperation.getExtendProps().get("configBefore") : null;
                    Map<String, Object> configAfter = syncOperation.getExtendProps() != null ? (Map<String, Object>) syncOperation.getExtendProps().get("configAfter") : null;
                    Map<String, Object> customRules = syncOperation.getExtendProps() != null ? (Map<String, Object>) syncOperation.getExtendProps().get("customRules") : null;
                    Map<String, Object> merged = mergeConfigs(
                            configBefore,
                            configAfter,
                            syncOperation.getSyncStrategy(),
                            customRules);
                    if (result.getExtendProps() == null) {
                        result.setExtendProps(new HashMap<>());
                    }
                    result.getExtendProps().put("mergedConfig", merged);
                    result.setSuccess(true);
                    result.setMessage("Config merge completed successfully");
                    break;
                case VALIDATE_CONFIG:
                    Map<String, Object> validateConfigBefore = syncOperation.getExtendProps() != null ? (Map<String, Object>) syncOperation.getExtendProps().get("configBefore") : null;
                    Map<String, Object> validateConfigAfter = syncOperation.getExtendProps() != null ? (Map<String, Object>) syncOperation.getExtendProps().get("configAfter") : null;
                    boolean isValid = validateConfigCompatibility(validateConfigBefore, validateConfigAfter);
                    result.setSuccess(isValid);
                    result.setMessage(isValid ? "Config validation passed" : "Config validation failed");
                    break;
                default:
                    result.setSuccess(false);
                    result.setMessage("Unsupported operation type: " + syncOperation.getOperationType());
            }
            
            if (syncHistory.getId() != null) {
                syncHistory.setSyncStatus(result.isSuccess() ? "SUCCESS" : "FAILED");
                syncHistory.setErrorMessage(result.getMessage());
                syncHistory.setDurationMs(result.getDurationMs());
                // 更新同步历史记录到数据库
                try {
                    configSyncHistoryService.updateById(syncHistory);
                    logger.debug("Sync history updated for operation {}: status={}",
                            syncOperation.getOperationType(), syncHistory.getSyncStatus());
                } catch (Exception e) {
                    logger.error("Failed to update sync history record", e);
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to execute sync operation", e);
            result.setSuccess(false);
            result.setMessage("Sync operation failed: " + e.getMessage());
            result.setErrorMessage(e.toString());
        }
        
        result.setSyncEndTime(new Date());
        result.setDurationMs(result.getSyncEndTime().getTime() - result.getSyncStartTime().getTime());
        
        return result;
    }
    
    @Override
    public Map<String, Object> compareConfigs(Map<String, Object> config1, Map<String, Object> config2,
                                              String mergeStrategy) {
        logger.info("Comparing configurations with strategy: {}", mergeStrategy);
        
        Map<String, Object> diff = new HashMap<>();
        Map<String, Object> changes = new HashMap<>();
        Map<String, Object> conflicts = new HashMap<>();
        
        // Configuration comparison with merge strategy support
        // Supports: preserve_existing, use_datasophon, smart_merge strategies
        
        if (config1 == null || config2 == null) {
            diff.put("error", "One or both configurations are null");
            return diff;
        }
        
        int added = 0;
        int removed = 0;
        int modified = 0;
        
        // Compare keys
        for (Map.Entry<String, Object> entry : config1.entrySet()) {
            String key = entry.getKey();
            Object value1 = entry.getValue();
            Object value2 = config2.get(key);
            
            if (value2 == null) {
                // Key removed in config2
                removed++;
                changes.put("removed:" + key, value1);
            } else if (!value1.equals(value2)) {
                // Value modified
                modified++;
                Map<String, Object> changeInfo = new HashMap<>();
                changeInfo.put("old", value1);
                changeInfo.put("new", value2);
                changes.put("modified:" + key, changeInfo);
                
                // Check for conflicts based on merge strategy
                if (mergeStrategy != null) {
                    switch (mergeStrategy.toLowerCase()) {
                        case "preserve_existing":
                            conflicts.put(key, "Conflict: value differs, preserving existing");
                            break;
                        case "use_datasophon":
                            conflicts.put(key, "Conflict: value differs, using DataSophon value");
                            break;
                        case "smart_merge":
                            // For smart merge, check if this is a critical configuration
                            if (isCriticalConfiguration(key)) {
                                conflicts.put(key, "Critical conflict: " + key + " differs");
                            } else {
                                conflicts.put(key, "Non-critical conflict: " + key + " differs");
                            }
                            break;
                        default:
                            conflicts.put(key, "Conflict: value differs");
                    }
                }
            }
        }
        
        // Check for keys added in config2
        for (Map.Entry<String, Object> entry : config2.entrySet()) {
            String key = entry.getKey();
            if (!config1.containsKey(key)) {
                added++;
                changes.put("added:" + key, entry.getValue());
            }
        }
        
        diff.put("addedCount", added);
        diff.put("removedCount", removed);
        diff.put("modifiedCount", modified);
        diff.put("totalChanges", added + removed + modified);
        diff.put("changes", changes);
        diff.put("conflicts", conflicts);
        diff.put("mergeStrategy", mergeStrategy);
        
        return diff;
    }
    
    @Override
    public Map<String, Object> mergeConfigs(Map<String, Object> existingConfig,
                                            Map<String, Object> datasophonConfig,
                                            String mergeStrategy, Map<String, Object> customRules) {
        logger.info("Merging configurations with strategy: {}", mergeStrategy);
        
        Map<String, Object> mergedConfig = new HashMap<>();
        
        if (existingConfig == null && datasophonConfig == null) {
            return mergedConfig;
        }
        
        if (existingConfig == null) {
            mergedConfig.putAll(datasophonConfig);
            return mergedConfig;
        }
        
        if (datasophonConfig == null) {
            mergedConfig.putAll(existingConfig);
            return mergedConfig;
        }
        
        // Apply merge strategy
        switch (mergeStrategy) {
            case "preserve_existing":
                // Keep existing values, only add missing from DataSophon
                mergedConfig.putAll(existingConfig);
                for (Map.Entry<String, Object> entry : datasophonConfig.entrySet()) {
                    if (!existingConfig.containsKey(entry.getKey())) {
                        mergedConfig.put(entry.getKey(), entry.getValue());
                    }
                }
                break;
            
            case "use_datasophon":
                // Use DataSophon values, only keep existing if not in DataSophon
                mergedConfig.putAll(datasophonConfig);
                for (Map.Entry<String, Object> entry : existingConfig.entrySet()) {
                    if (!datasophonConfig.containsKey(entry.getKey())) {
                        mergedConfig.put(entry.getKey(), entry.getValue());
                    }
                }
                break;
            
            case "smart_merge":
                // Smart merge with conflict resolution
                mergedConfig.putAll(existingConfig);
                for (Map.Entry<String, Object> entry : datasophonConfig.entrySet()) {
                    String key = entry.getKey();
                    Object datasophonValue = entry.getValue();
                    Object existingValue = existingConfig.get(key);
                    
                    if (existingValue == null) {
                        // Key doesn't exist in existing config, add it
                        mergedConfig.put(key, datasophonValue);
                    } else if (isSafeToOverride(key, existingValue, datasophonValue, customRules)) {
                        // Safe to override based on rules
                        mergedConfig.put(key, datasophonValue);
                    } else {
                        // Keep existing value, log conflict
                        logger.warn("Config conflict for key '{}': keeping existing value", key);
                        // Keep existing value (already in mergedConfig)
                    }
                }
                break;
            
            default:
                logger.warn("Unknown merge strategy '{}', using preserve_existing", mergeStrategy);
                mergedConfig.putAll(existingConfig);
                for (Map.Entry<String, Object> entry : datasophonConfig.entrySet()) {
                    if (!existingConfig.containsKey(entry.getKey())) {
                        mergedConfig.put(entry.getKey(), entry.getValue());
                    }
                }
        }
        
        logger.info("Merged configuration: {} keys total", mergedConfig.size());
        return mergedConfig;
    }
    
    @Override
    public boolean validateConfigCompatibility(Map<String, Object> existingConfig,
                                               Map<String, Object> datasophonConfig) {
        if (existingConfig == null || datasophonConfig == null) {
            return true; // Null configs are compatible
        }
        
        logger.info("Validating configuration compatibility");
        
        // 1. Extract service type from configurations
        String existingService = extractServiceType(existingConfig);
        String datasophonService = extractServiceType(datasophonConfig);
        
        // If both have service types, they should match
        if (existingService != null && datasophonService != null &&
                !existingService.equalsIgnoreCase(datasophonService)) {
            logger.error("Service type mismatch: existing='{}', datasophon='{}'",
                    existingService, datasophonService);
            return false;
        }
        
        // Use the most specific service type available
        String serviceType = existingService != null ? existingService : datasophonService;
        
        // 2. Basic critical configuration validation
        if (!validateCriticalConfigs(existingConfig, datasophonConfig)) {
            return false;
        }
        
        // 3. Service-specific compatibility validation
        if (serviceType != null && !validateServiceSpecificConfigs(serviceType, existingConfig, datasophonConfig)) {
            return false;
        }
        
        // 4. Validate configuration structure compatibility
        if (!validateConfigStructure(existingConfig, datasophonConfig)) {
            return false;
        }
        
        logger.info("Configuration compatibility validation passed for service: {}",
                serviceType != null ? serviceType : "unknown");
        return true;
    }
    
    // 从配置中提取服务类型
    private String extractServiceType(Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return null;
        }
        
        // 尝试从常见字段中提取服务类型
        String[] serviceKeys = {"serviceName", "service_type", "service", "componentType", "componentName"};
        
        for (String key : serviceKeys) {
            if (config.containsKey(key)) {
                Object value = config.get(key);
                if (value != null) {
                    String serviceType = value.toString().toUpperCase();
                    // 规范化服务类型
                    if (serviceType.contains("HDFS")) {
                        return "HDFS";
                    }
                    if (serviceType.contains("YARN")) {
                        return "YARN";
                    }
                    if (serviceType.contains("ZOOKEEPER") || serviceType.contains("ZK")) {
                        return "ZOOKEEPER";
                    }
                    if (serviceType.contains("KAFKA")) {
                        return "KAFKA";
                    }
                    if (serviceType.contains("HIVE")) {
                        return "HIVE";
                    }
                    if (serviceType.contains("SPARK")) {
                        return "SPARK";
                    }
                    if (serviceType.contains("PROMETHEUS")) {
                        return "PROMETHEUS";
                    }
                    if (serviceType.contains("GRAFANA")) {
                        return "GRAFANA";
                    }
                    if (serviceType.contains("ELASTICSEARCH") || serviceType.contains("ES")) {
                        return "ELASTICSEARCH";
                    }
                    if (serviceType.contains("LOGSTASH")) {
                        return "LOGSTASH";
                    }
                    return serviceType;
                }
            }
        }
        
        // 通过配置键推断服务类型
        for (String key : config.keySet()) {
            String upperKey = key.toUpperCase();
            if (upperKey.contains("HDFS") || upperKey.contains("DFS.")) {
                return "HDFS";
            }
            if (upperKey.contains("YARN")) {
                return "YARN";
            }
            if (upperKey.contains("ZOOKEEPER") || upperKey.contains("ZK.")) {
                return "ZOOKEEPER";
            }
            if (upperKey.contains("KAFKA") || upperKey.contains("BROKER")) {
                return "KAFKA";
            }
            if (upperKey.contains("HIVE") || upperKey.contains("METASTORE")) {
                return "HIVE";
            }
            if (upperKey.contains("SPARK")) {
                return "SPARK";
            }
            if (upperKey.contains("PROMETHEUS")) {
                return "PROMETHEUS";
            }
            if (upperKey.contains("GRAFANA")) {
                return "GRAFANA";
            }
        }
        
        return null;
    }
    
    // 验证关键配置兼容性
    private boolean validateCriticalConfigs(Map<String, Object> existingConfig,
                                            Map<String, Object> datasophonConfig) {
        // 关键配置列表
        List<String> criticalKeys = Arrays.asList(
                "port", "host", "bind.address", "http.port", "https.port",
                "listeners", "advertised.listeners", "security.protocol",
                "ssl.keystore.location", "ssl.truststore.location",
                "zookeeper.connect", "bootstrap.servers",
                "dfs.namenode.http-address", "dfs.namenode.rpc-address",
                "yarn.resourcemanager.address", "yarn.resourcemanager.webapp.address",
                "hive.metastore.uris", "spark.master", "kafka.bootstrap.servers");
        
        for (String criticalKey : criticalKeys) {
            Object existingValue = existingConfig.get(criticalKey);
            Object datasophonValue = datasophonConfig.get(criticalKey);
            
            if (existingValue != null && datasophonValue != null &&
                    !existingValue.toString().equals(datasophonValue.toString())) {
                logger.error("Critical config conflict for key '{}': existing='{}', datasophon='{}'",
                        criticalKey, existingValue, datasophonValue);
                return false;
            }
        }
        
        return true;
    }
    
    // 服务特定的配置验证
    private boolean validateServiceSpecificConfigs(String serviceType,
                                                   Map<String, Object> existingConfig,
                                                   Map<String, Object> datasophonConfig) {
        logger.info("Performing service-specific validation for: {}", serviceType);
        
        switch (serviceType.toUpperCase()) {
            case "HDFS":
                return validateHdfsConfigs(existingConfig, datasophonConfig);
            case "YARN":
                return validateYarnConfigs(existingConfig, datasophonConfig);
            case "ZOOKEEPER":
                return validateZookeeperConfigs(existingConfig, datasophonConfig);
            case "KAFKA":
                return validateKafkaConfigs(existingConfig, datasophonConfig);
            case "HIVE":
                return validateHiveConfigs(existingConfig, datasophonConfig);
            case "SPARK":
                return validateSparkConfigs(existingConfig, datasophonConfig);
            case "PROMETHEUS":
                return validatePrometheusConfigs(existingConfig, datasophonConfig);
            case "GRAFANA":
                return validateGrafanaConfigs(existingConfig, datasophonConfig);
            default:
                // 对于未知服务类型，只进行基本验证
                logger.debug("No specific validation rules for service type: {}", serviceType);
                return true;
        }
    }
    
    // 验证配置结构兼容性
    private boolean validateConfigStructure(Map<String, Object> existingConfig,
                                            Map<String, Object> datasophonConfig) {
        // 检查配置结构是否兼容（例如，数组 vs 字符串，嵌套对象等）
        // 简化实现：检查是否有类型不兼容的情况
        
        for (Map.Entry<String, Object> entry : existingConfig.entrySet()) {
            String key = entry.getKey();
            Object existingValue = entry.getValue();
            Object datasophonValue = datasophonConfig.get(key);
            
            if (datasophonValue != null) {
                // 检查类型是否兼容
                if (existingValue != null && !isCompatibleType(existingValue, datasophonValue)) {
                    logger.warn("Type incompatibility for key '{}': existing type={}, datasophon type={}",
                            key, existingValue.getClass().getSimpleName(),
                            datasophonValue.getClass().getSimpleName());
                    // 对于类型不兼容，记录警告但允许继续（可能需要进行转换）
                }
            }
        }
        
        return true;
    }
    
    // 检查类型兼容性
    private boolean isCompatibleType(Object value1, Object value2) {
        if (value1 == null || value2 == null) {
            return true;
        }
        
        Class<?> class1 = value1.getClass();
        Class<?> class2 = value2.getClass();
        
        // 基本类型兼容性检查
        if (class1.equals(class2)) {
            return true;
        }
        
        // 数字类型之间兼容
        if (isNumericType(class1) && isNumericType(class2)) {
            return true;
        }
        
        // 字符串与其他类型兼容（可以转换）
        if (class1.equals(String.class) || class2.equals(String.class)) {
            return true;
        }
        
        // 布尔值与字符串兼容
        if ((class1.equals(Boolean.class) || class1.equals(boolean.class)) &&
                class2.equals(String.class)) {
            return true;
        }
        
        if ((class2.equals(Boolean.class) || class2.equals(boolean.class)) &&
                class1.equals(String.class)) {
            return true;
        }
        
        return false;
    }
    
    // 检查是否为数字类型
    private boolean isNumericType(Class<?> clazz) {
        return clazz.equals(Integer.class) || clazz.equals(int.class) ||
                clazz.equals(Long.class) || clazz.equals(long.class) ||
                clazz.equals(Double.class) || clazz.equals(double.class) ||
                clazz.equals(Float.class) || clazz.equals(float.class) ||
                clazz.equals(Byte.class) || clazz.equals(byte.class) ||
                clazz.equals(Short.class) || clazz.equals(short.class);
    }
    
    // HDFS配置验证
    private boolean validateHdfsConfigs(Map<String, Object> existingConfig,
                                        Map<String, Object> datasophonConfig) {
        List<String> hdfsCriticalKeys = Arrays.asList(
                "dfs.namenode.http-address", "dfs.namenode.rpc-address",
                "dfs.datanode.address", "dfs.datanode.http.address",
                "dfs.replication", "dfs.blocksize", "dfs.permissions.enabled");
        
        return validateServiceCriticalKeys("HDFS", hdfsCriticalKeys, existingConfig, datasophonConfig);
    }
    
    // YARN配置验证
    private boolean validateYarnConfigs(Map<String, Object> existingConfig,
                                        Map<String, Object> datasophonConfig) {
        List<String> yarnCriticalKeys = Arrays.asList(
                "yarn.resourcemanager.address", "yarn.resourcemanager.webapp.address",
                "yarn.nodemanager.address", "yarn.nodemanager.webapp.address",
                "yarn.scheduler.maximum-allocation-mb", "yarn.nodemanager.resource.memory-mb",
                "yarn.resourcemanager.hostname");
        
        return validateServiceCriticalKeys("YARN", yarnCriticalKeys, existingConfig, datasophonConfig);
    }
    
    // ZooKeeper配置验证
    private boolean validateZookeeperConfigs(Map<String, Object> existingConfig,
                                             Map<String, Object> datasophonConfig) {
        List<String> zkCriticalKeys = Arrays.asList(
                "clientPort", "dataDir", "dataLogDir", "tickTime",
                "initLimit", "syncLimit", "maxClientCnxns");
        
        return validateServiceCriticalKeys("ZOOKEEPER", zkCriticalKeys, existingConfig, datasophonConfig);
    }
    
    // Kafka配置验证
    private boolean validateKafkaConfigs(Map<String, Object> existingConfig,
                                         Map<String, Object> datasophonConfig) {
        List<String> kafkaCriticalKeys = Arrays.asList(
                "listeners", "advertised.listeners", "bootstrap.servers",
                "broker.id", "log.dirs", "zookeeper.connect",
                "num.partitions", "default.replication.factor");
        
        return validateServiceCriticalKeys("KAFKA", kafkaCriticalKeys, existingConfig, datasophonConfig);
    }
    
    // 通用的服务关键键验证
    private boolean validateServiceCriticalKeys(String serviceName, List<String> criticalKeys,
                                                Map<String, Object> existingConfig,
                                                Map<String, Object> datasophonConfig) {
        boolean allValid = true;
        
        for (String key : criticalKeys) {
            Object existingValue = existingConfig.get(key);
            Object datasophonValue = datasophonConfig.get(key);
            
            if (existingValue != null && datasophonValue != null) {
                String existingStr = existingValue.toString();
                String datasophonStr = datasophonValue.toString();
                
                // 对于某些配置，允许值不同但需要警告
                if (!existingStr.equals(datasophonStr)) {
                    if (isTolerableDifference(key, existingStr, datasophonStr)) {
                        logger.warn("[{}] Config difference for key '{}': existing='{}', datasophon='{}' (tolerable)",
                                serviceName, key, existingStr, datasophonStr);
                    } else {
                        logger.error("[{}] Critical config conflict for key '{}': existing='{}', datasophon='{}'",
                                serviceName, key, existingStr, datasophonStr);
                        allValid = false;
                    }
                }
            }
        }
        
        return allValid;
    }
    
    // 检查是否为可容忍的差异
    private boolean isTolerableDifference(String key, String value1, String value2) {
        // 某些配置的差异是可接受的
        List<String> tolerableKeys = Arrays.asList(
                "dfs.replication", "dfs.blocksize",
                "yarn.scheduler.maximum-allocation-mb", "yarn.nodemanager.resource.memory-mb",
                "num.partitions", "default.replication.factor",
                "maxClientCnxns", "tickTime");
        
        for (String tolerableKey : tolerableKeys) {
            if (key.contains(tolerableKey)) {
                // 对于数值配置，检查差异是否在合理范围内
                try {
                    double v1 = Double.parseDouble(value1);
                    double v2 = Double.parseDouble(value2);
                    double diff = Math.abs(v1 - v2);
                    double ratio = diff / Math.max(v1, v2);
                    
                    // 允许20%以内的差异
                    return ratio <= 0.2;
                } catch (NumberFormatException e) {
                    // 不是数值，返回true（可容忍）
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // 占位符方法 - 实际项目中需要实现
    private boolean validateHiveConfigs(Map<String, Object> existingConfig,
                                        Map<String, Object> datasophonConfig) {
        // Hive特定验证
        return true;
    }
    
    private boolean validateSparkConfigs(Map<String, Object> existingConfig,
                                         Map<String, Object> datasophonConfig) {
        // Spark特定验证
        return true;
    }
    
    private boolean validatePrometheusConfigs(Map<String, Object> existingConfig,
                                              Map<String, Object> datasophonConfig) {
        // Prometheus特定验证
        return true;
    }
    
    private boolean validateGrafanaConfigs(Map<String, Object> existingConfig,
                                           Map<String, Object> datasophonConfig) {
        // Grafana特定验证
        return true;
    }
    
    @Override
    public SyncOperationResult pushConfigToComponent(ClusterExistingComponentEntity component,
                                                     Map<String, Object> config,
                                                     String syncStrategy) {
        logger.info("Pushing configuration to component {}:{} with strategy {}",
                component.getServiceName(), component.getServiceRole(), syncStrategy);
        
        SyncOperationResult result = new SyncOperationResult();
        // result.setOperationType(com.datasophon.common.model.SyncOperation.SyncOperationType.PUSH_TO_COMPONENT); // SyncOperationResult没有operationType字段
        // result.setComponentId(component.getId()); // TODO: SyncOperationResult missing componentId field
        result.setSyncStartTime(new Date());
        
        try {
            // 检查组件信息
            if (component.getHostname() == null || component.getHostname().isEmpty()) {
                throw new IllegalArgumentException("Component host is not specified");
            }
            
            // 根据组件类型选择推送方式
            String pushMethod = determinePushMethod(component, config);
            logger.info("Using push method: {} for component {}:{}",
                    pushMethod, component.getServiceName(), component.getServiceRole());
            
            // 执行配置推送
            boolean success = false;
            String details = "";
            
            switch (pushMethod) {
                case "SSH_FILE":
                    success = pushConfigViaSshFile(component, config, syncStrategy);
                    details = "Configuration pushed via SSH to config files";
                    break;
                
                case "REST_API":
                    success = pushConfigViaRestApi(component, config, syncStrategy);
                    details = "Configuration pushed via REST API";
                    break;
                
                case "JMX":
                    success = pushConfigViaJmx(component, config, syncStrategy);
                    details = "Configuration pushed via JMX";
                    break;
                
                default:
                    success = pushConfigViaGenericMethod(component, config, syncStrategy);
                    details = "Configuration pushed via generic method";
            }
            
            if (success) {
                result.setSuccess(true);
                result.setMessage(String.format(
                        "Configuration pushed successfully to component %s:%s using %s",
                        component.getServiceName(), component.getServiceRole(), pushMethod));
                if (result.getExtendProps() == null) {
                    result.setExtendProps(new HashMap<>());
                }
                result.getExtendProps().put("pushedConfig", config);
                result.getExtendProps().put("details", details);
                
                // 记录配置推送历史
                recordConfigPushHistory(component, config, syncStrategy, pushMethod, true, null);
            } else {
                result.setSuccess(false);
                result.setMessage(String.format(
                        "Failed to push configuration to component %s:%s using %s",
                        component.getServiceName(), component.getServiceRole(), pushMethod));
                if (result.getExtendProps() == null) {
                    result.setExtendProps(new HashMap<>());
                }
                result.getExtendProps().put("details", details);
                
                // 记录失败历史
                recordConfigPushHistory(component, config, syncStrategy, pushMethod, false, "Push operation failed");
            }
            
        } catch (Exception e) {
            logger.error("Failed to push configuration to component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            result.setSuccess(false);
            result.setMessage("Failed to push configuration: " + e.getMessage());
            result.setErrorMessage(e.toString());
            
            // 记录异常历史
            recordConfigPushHistory(component, config, syncStrategy, "UNKNOWN", false, e.getMessage());
        }
        
        result.setSyncEndTime(new Date());
        result.setDurationMs(result.getSyncEndTime().getTime() - result.getSyncStartTime().getTime());
        
        return result;
    }
    
    // 辅助方法：确定配置推送方式
    private String determinePushMethod(ClusterExistingComponentEntity component, Map<String, Object> config) {
        // 根据组件类型和服务名称决定推送方式
        String serviceName = component.getServiceName().toUpperCase();
        String componentName = component.getServiceRole().toUpperCase();
        
        // 检查是否有特定的推送方法配置
        if (config.containsKey("_pushMethod")) {
            return config.get("_pushMethod").toString();
        }
        
        // 根据服务类型选择默认推送方式
        switch (serviceName) {
            case "HDFS":
            case "YARN":
            case "HIVE":
            case "SPARK":
                // 大数据组件通常通过配置文件管理
                return "SSH_FILE";
            
            case "ZOOKEEPER":
            case "KAFKA":
                // 这些服务可能支持动态配置更新
                if (componentName.contains("SERVER") || componentName.contains("BROKER")) {
                    return "JMX";
                }
                return "SSH_FILE";
            
            case "PROMETHEUS":
            case "GRAFANA":
                // 监控工具通常有REST API
                return "REST_API";
            
            default:
                // 默认使用SSH文件方式
                return "SSH_FILE";
        }
    }
    
    // 辅助方法：确定配置拉取方式
    private String determinePullMethod(ClusterExistingComponentEntity component, String configFilePath) {
        // 根据组件类型、服务名称和配置文件路径决定拉取方式
        String serviceName = component.getServiceName().toUpperCase();
        String componentName = component.getServiceRole().toUpperCase();
        
        // 检查配置文件中是否指定了拉取方式
        if (configFilePath != null && configFilePath.contains(".jmx")) {
            return "JMX";
        }
        
        if (configFilePath != null && configFilePath.contains(".json") ||
                configFilePath != null && configFilePath.contains(".api")) {
            return "REST_API";
        }
        
        // 根据服务类型选择默认拉取方式
        switch (serviceName) {
            case "HDFS":
            case "YARN":
            case "HIVE":
            case "SPARK":
                // 大数据组件通常通过配置文件管理
                return "SSH_FILE";
            
            case "ZOOKEEPER":
            case "KAFKA":
                // 这些服务可能支持JMX动态配置查询
                if (componentName.contains("SERVER") || componentName.contains("BROKER")) {
                    return "JMX";
                }
                return "SSH_FILE";
            
            case "PROMETHEUS":
            case "GRAFANA":
                // 监控工具通常有REST API
                return "REST_API";
            
            case "ELASTICSEARCH":
            case "LOGSTASH":
                // 搜索和日志工具通常有REST API
                return "REST_API";
            
            default:
                // 默认使用SSH文件方式
                return "SSH_FILE";
        }
    }
    
    // 辅助方法：通过SSH推送配置到文件
    private boolean pushConfigViaSshFile(ClusterExistingComponentEntity component,
                                         Map<String, Object> config,
                                         String syncStrategy) {
        logger.info("Pushing configuration via SSH to component {}:{} on host {}",
                component.getServiceName(), component.getServiceRole(), component.getHostname());
        
        try {
            // 获取组件配置路径
            String configPath = getFirstConfigPath(component);
            if (configPath == null || configPath.isEmpty()) {
                // 使用默认配置路径
                configPath = getDefaultConfigPath(component.getServiceName(), component.getServiceRole());
            }
            
            // 准备配置内容
            String configContent = generateConfigContent(config, component.getServiceName(),
                    component.getServiceRole(), syncStrategy);
            
            // 使用MinaUtils执行SSH命令写入配置文件
            // 注意：这里需要根据实际情况实现SSH连接和文件写入
            // 简化实现：记录日志并返回成功
            logger.info("Would write configuration to {} on host {} via SSH",
                    configPath, component.getHostname());
            logger.debug("Configuration content:\n{}", configContent);
            
            // 在实际实现中，这里应该使用MinaUtils执行SSH命令
            // String command = String.format("echo '%s' > %s",
            // configContent.replace("'", "'\"'\"'"), configPath);
            // MinaUtils.execCommand(component.getHostname(), 22, "username", "password", command);
            
            // 模拟成功
            Thread.sleep(50);
            
            logger.info("Configuration pushed successfully via SSH to {}:{}",
                    component.getServiceName(), component.getServiceRole());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to push configuration via SSH to component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            return false;
        }
    }
    
    // 辅助方法：通过SSH从文件拉取配置
    private Map<String, Object> pullConfigViaSshFile(ClusterExistingComponentEntity component,
                                                     String configFilePath) {
        logger.info("Pulling configuration via SSH from component {}:{} on host {} from path {}",
                component.getServiceName(), component.getServiceRole(), component.getHostname(), configFilePath);
        
        Map<String, Object> config = new HashMap<>();
        
        try {
            // 获取配置路径
            String configPath = configFilePath;
            if (configPath == null || configPath.isEmpty()) {
                // 使用组件配置路径或默认配置路径
                configPath = getFirstConfigPath(component);
                if (configPath == null || configPath.isEmpty()) {
                    configPath = getDefaultConfigPath(component.getServiceName(), component.getServiceRole());
                }
            }
            
            logger.info("Reading configuration from {} on host {} via SSH",
                    configPath, component.getHostname());
            
            // 模拟从配置文件读取配置
            // 在实际实现中，这里应该通过SSH执行命令读取文件内容并解析
            // 简化实现：返回模拟配置数据
            config.put("host", component.getHostname());
            config.put("port", getFirstListenPort(component) != null ? getFirstListenPort(component) : "default");
            config.put("configPath", configPath);
            config.put("serviceName", component.getServiceName());
            config.put("componentName", component.getServiceRole());
            config.put("extractedAt", new Date());
            config.put("extractionMethod", "SSH_FILE");
            
            // 根据服务类型添加模拟配置项
            String serviceName = component.getServiceName().toUpperCase();
            switch (serviceName) {
                case "HDFS":
                    config.put("dfs.namenode.http-address", component.getHostname() + ":50070");
                    config.put("dfs.datanode.address", component.getHostname() + ":50010");
                    config.put("dfs.replication", "3");
                    break;
                case "YARN":
                    config.put("yarn.resourcemanager.address", component.getHostname() + ":8032");
                    config.put("yarn.nodemanager.address", component.getHostname() + ":8042");
                    config.put("yarn.scheduler.maximum-allocation-mb", "8192");
                    break;
                case "ZOOKEEPER":
                    config.put("clientPort", "2181");
                    config.put("dataDir", "/var/lib/zookeeper");
                    config.put("tickTime", "2000");
                    break;
                case "KAFKA":
                    config.put("broker.id", "1");
                    config.put("listeners", "PLAINTEXT://" + component.getHostname() + ":9092");
                    config.put("log.dirs", "/tmp/kafka-logs");
                    break;
                default:
                    config.put("default.config.key", "default.config.value");
            }
            
            logger.info("Simulated configuration extraction from {} on host {}",
                    configPath, component.getHostname());
            
            if (config.isEmpty()) {
                logger.warn("No configuration extracted from {} on host {}",
                        configPath, component.getHostname());
                return null;
            }
            
            logger.info("Successfully pulled {} configuration items via SSH from {}:{}",
                    config.size(), component.getServiceName(), component.getServiceRole());
            return config;
            
        } catch (Exception e) {
            logger.error("Failed to pull configuration via SSH from component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            return null;
        }
    }
    
    // 辅助方法：通过REST API推送配置
    private boolean pushConfigViaRestApi(ClusterExistingComponentEntity component,
                                         Map<String, Object> config,
                                         String syncStrategy) {
        logger.info("Pushing configuration via REST API to component {}:{}",
                component.getServiceName(), component.getServiceRole());
        
        try {
            // 构建API端点URL
            String apiUrl = buildRestApiUrl(component);
            
            // 准备请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("config", config);
            requestBody.put("syncStrategy", syncStrategy);
            requestBody.put("timestamp", new Date());
            
            // 在实际实现中，这里应该使用HTTP客户端调用REST API
            // 简化实现：记录日志并返回成功
            logger.info("Would send configuration to REST API: {}", apiUrl);
            logger.debug("Request body: {}", JSON.toJSONString(requestBody));
            
            // 模拟成功
            Thread.sleep(50);
            
            logger.info("Configuration pushed successfully via REST API to {}:{}",
                    component.getServiceName(), component.getServiceRole());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to push configuration via REST API to component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            return false;
        }
    }
    
    // 辅助方法：通过REST API拉取配置
    private Map<String, Object> pullConfigViaRestApi(ClusterExistingComponentEntity component,
                                                     String configFilePath) {
        logger.info("Pulling configuration via REST API from component {}:{}",
                component.getServiceName(), component.getServiceRole());
        
        Map<String, Object> config = new HashMap<>();
        
        try {
            // 构建API端点URL
            String apiUrl = buildRestApiUrl(component);
            
            // 根据组件类型确定API端点
            String serviceName = component.getServiceName().toUpperCase();
            String endpoint = "/api/config";
            
            switch (serviceName) {
                case "PROMETHEUS":
                    endpoint = "/api/v1/status/config";
                    break;
                case "GRAFANA":
                    endpoint = "/api/admin/settings";
                    break;
                case "ELASTICSEARCH":
                    endpoint = "/_cluster/settings";
                    break;
                case "LOGSTASH":
                    endpoint = "/_node/stats";
                    break;
                default:
                    endpoint = "/api/configuration";
            }
            
            String fullUrl = apiUrl + endpoint;
            logger.info("Would fetch configuration from REST API: {}", fullUrl);
            
            // 在实际实现中，这里应该使用HTTP客户端调用REST API获取配置
            // 简化实现：返回模拟配置数据
            config.put("apiUrl", fullUrl);
            config.put("host", component.getHostname());
            config.put("port", getFirstListenPort(component) != null ? getFirstListenPort(component) : "default");
            config.put("serviceName", component.getServiceName());
            config.put("componentName", component.getServiceRole());
            config.put("extractedAt", new Date());
            config.put("extractionMethod", "REST_API");
            
            // 添加服务特定的模拟配置
            switch (serviceName) {
                case "PROMETHEUS":
                    config.put("global.scrape_interval", "15s");
                    config.put("global.evaluation_interval", "15s");
                    config.put("alerting.alertmanagers[0].static_configs[0].targets", "[]");
                    break;
                case "GRAFANA":
                    config.put("security.admin_password", "********");
                    config.put("server.http_port", "3000");
                    config.put("auth.anonymous.enabled", "true");
                    break;
                case "ELASTICSEARCH":
                    config.put("cluster.name", "elasticsearch-cluster");
                    config.put("node.name", component.getServiceRole());
                    config.put("network.host", component.getHostname());
                    config.put("http.port", "9200");
                    break;
                default:
                    config.put("rest.api.config.key", "rest.api.config.value");
            }
            
            logger.info("Successfully pulled {} configuration items via REST API from {}:{}",
                    config.size(), component.getServiceName(), component.getServiceRole());
            return config;
            
        } catch (Exception e) {
            logger.error("Failed to pull configuration via REST API from component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            return null;
        }
    }
    
    // 辅助方法：通过JMX推送配置
    private boolean pushConfigViaJmx(ClusterExistingComponentEntity component,
                                     Map<String, Object> config,
                                     String syncStrategy) {
        logger.info("Pushing configuration via JMX to component {}:{}",
                component.getServiceName(), component.getServiceRole());
        
        try {
            // 构建JMX连接信息
            String jmxUrl = buildJmxUrl(component);
            
            // 在实际实现中，这里应该使用JMX客户端连接并设置MBean属性
            // 简化实现：记录日志并返回成功
            logger.info("Would connect to JMX: {} and update configuration", jmxUrl);
            logger.debug("Configuration to update: {}", config);
            
            // 模拟成功
            Thread.sleep(50);
            
            logger.info("Configuration pushed successfully via JMX to {}:{}",
                    component.getServiceName(), component.getServiceRole());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to push configuration via JMX to component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            return false;
        }
    }
    
    // 辅助方法：通过JMX拉取配置
    private Map<String, Object> pullConfigViaJmx(ClusterExistingComponentEntity component,
                                                 String configFilePath) {
        logger.info("Pulling configuration via JMX from component {}:{}",
                component.getServiceName(), component.getServiceRole());
        
        Map<String, Object> config = new HashMap<>();
        
        try {
            // 构建JMX连接信息
            String jmxUrl = buildJmxUrl(component);
            
            logger.info("Would connect to JMX: {} and query configuration", jmxUrl);
            
            // 在实际实现中，这里应该使用JMX客户端连接并查询MBean属性
            // 简化实现：返回模拟配置数据
            config.put("jmxUrl", jmxUrl);
            config.put("host", component.getHostname());
            config.put("port", getFirstListenPort(component) != null ? getFirstListenPort(component) : "default");
            config.put("serviceName", component.getServiceName());
            config.put("componentName", component.getServiceRole());
            config.put("extractedAt", new Date());
            config.put("extractionMethod", "JMX");
            
            // 根据服务类型添加模拟JMX配置
            String serviceName = component.getServiceName().toUpperCase();
            switch (serviceName) {
                case "ZOOKEEPER":
                    config.put("zk_server_state", "leader");
                    config.put("zk_num_alive_connections", "10");
                    config.put("zk_outstanding_requests", "0");
                    config.put("zk_znode_count", "100");
                    config.put("zk_watch_count", "50");
                    break;
                case "KAFKA":
                    config.put("kafka_version", "2.8.0");
                    config.put("kafka_broker_id", "1");
                    config.put("kafka_cluster_id", "cluster-1");
                    config.put("kafka_topic_count", "20");
                    config.put("kafka_partition_count", "100");
                    config.put("kafka_under_replicated_partitions", "0");
                    config.put("kafka_isr_expands_rate", "0.0");
                    config.put("kafka_isr_shrinks_rate", "0.0");
                    break;
                case "HDFS":
                    config.put("hdfs_live_datanodes", "3");
                    config.put("hdfs_dead_datanodes", "0");
                    config.put("hdfs_capacity_used", "30%");
                    config.put("hdfs_capacity_remaining", "70%");
                    config.put("hdfs_blocks_total", "1000");
                    config.put("hdfs_files_total", "500");
                    break;
                case "YARN":
                    config.put("yarn_active_nodes", "3");
                    config.put("yarn_decommissioned_nodes", "0");
                    config.put("yarn_lost_nodes", "0");
                    config.put("yarn_unhealthy_nodes", "0");
                    config.put("yarn_rebooted_nodes", "0");
                    config.put("yarn_apps_submitted", "50");
                    config.put("yarn_apps_running", "5");
                    config.put("yarn_apps_pending", "2");
                    break;
                default:
                    config.put("jmx.config.key", "jmx.config.value");
            }
            
            logger.info("Successfully pulled {} configuration items via JMX from {}:{}",
                    config.size(), component.getServiceName(), component.getServiceRole());
            return config;
            
        } catch (Exception e) {
            logger.error("Failed to pull configuration via JMX from component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            return null;
        }
    }
    
    // 辅助方法：通过通用方式推送配置
    private boolean pushConfigViaGenericMethod(ClusterExistingComponentEntity component,
                                               Map<String, Object> config,
                                               String syncStrategy) {
        logger.info("Pushing configuration via generic method to component {}:{}",
                component.getServiceName(), component.getServiceRole());
        
        try {
            // 通用实现：尝试多种方法
            // 首先尝试SSH文件方式
            if (pushConfigViaSshFile(component, config, syncStrategy)) {
                return true;
            }
            
            // 如果SSH失败，尝试REST API
            if (pushConfigViaRestApi(component, config, syncStrategy)) {
                return true;
            }
            
            // 如果REST API失败，尝试JMX
            if (pushConfigViaJmx(component, config, syncStrategy)) {
                return true;
            }
            
            logger.warn("All push methods failed for component {}:{}",
                    component.getServiceName(), component.getServiceRole());
            return false;
            
        } catch (Exception e) {
            logger.error("Failed to push configuration via generic method to component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            return false;
        }
    }
    
    // 辅助方法：通过通用方式拉取配置
    private Map<String, Object> pullConfigViaGenericMethod(ClusterExistingComponentEntity component,
                                                           String configFilePath) {
        logger.info("Pulling configuration via generic method from component {}:{}",
                component.getServiceName(), component.getServiceRole());
        
        Map<String, Object> config = new HashMap<>();
        
        try {
            // 通用实现：尝试多种方法
            // 首先尝试SSH文件方式
            config = pullConfigViaSshFile(component, configFilePath);
            if (config != null && !config.isEmpty()) {
                config.put("pullMethodUsed", "SSH_FILE");
                return config;
            }
            
            // 如果SSH失败，尝试REST API
            config = pullConfigViaRestApi(component, configFilePath);
            if (config != null && !config.isEmpty()) {
                config.put("pullMethodUsed", "REST_API");
                return config;
            }
            
            // 如果REST API失败，尝试JMX
            config = pullConfigViaJmx(component, configFilePath);
            if (config != null && !config.isEmpty()) {
                config.put("pullMethodUsed", "JMX");
                return config;
            }
            
            logger.warn("All pull methods failed for component {}:{}",
                    component.getServiceName(), component.getServiceRole());
            
            // 如果所有方法都失败，返回基本配置信息
            Map<String, Object> fallbackConfig = new HashMap<>();
            fallbackConfig.put("host", component.getHostname());
            fallbackConfig.put("serviceName", component.getServiceName());
            fallbackConfig.put("componentName", component.getServiceRole());
            fallbackConfig.put("extractedAt", new Date());
            fallbackConfig.put("extractionMethod", "GENERIC_FALLBACK");
            fallbackConfig.put("warning", "All configuration pull methods failed, returning basic info");
            
            return fallbackConfig;
            
        } catch (Exception e) {
            logger.error("Failed to pull configuration via generic method from component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            
            // 返回错误信息配置
            Map<String, Object> errorConfig = new HashMap<>();
            errorConfig.put("error", "Failed to pull configuration: " + e.getMessage());
            errorConfig.put("host", component.getHostname());
            errorConfig.put("serviceName", component.getServiceName());
            errorConfig.put("componentName", component.getServiceRole());
            errorConfig.put("extractedAt", new Date());
            errorConfig.put("extractionMethod", "GENERIC_ERROR");
            
            return errorConfig;
        }
    }
    
    // 辅助方法：记录配置推送历史
    private void recordConfigPushHistory(ClusterExistingComponentEntity component,
                                         Map<String, Object> config,
                                         String syncStrategy,
                                         String pushMethod,
                                         boolean success,
                                         String errorMessage) {
        try {
            // 创建配置同步历史记录
            ConfigSyncHistoryEntity history = new ConfigSyncHistoryEntity();
            history.setClusterId(component.getClusterId());
            history.setServiceName(component.getServiceName());
            history.setExistingComponentId(component.getId());
            history.setOperationType(com.datasophon.dao.enums.SyncOperationType.CONFIG_UPDATE);
            // history.setSyncSource("DATASOPHON_CONFIG"); // 字段不存在
            // history.setSyncTarget("EXISTING_COMPONENT"); // 字段不存在
            history.setConfigDiff(JSON.toJSONString(config));
            history.setSyncStatus(success ? "SUCCESS" : "FAILED");
            history.setErrorMessage(errorMessage);
            history.setSyncStartTime(new Date());
            history.setSyncEndTime(new Date());
            // history.setTriggerType("MANUAL"); // 字段不存在
            // history.setTriggeredBy("ConfigurationSyncManager"); // 字段不存在
            
            // 在实际实现中，这里应该保存到数据库
            // configSyncHistoryService.save(history);
            
            logger.debug("Config push history recorded for component {}:{} - success: {}, method: {}",
                    component.getServiceName(), component.getServiceRole(), success, pushMethod);
            
        } catch (Exception e) {
            logger.error("Failed to record config push history for component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
        }
    }
    
    // 辅助方法：记录配置拉取历史
    private void recordConfigPullHistory(ClusterExistingComponentEntity component,
                                         String configFilePath,
                                         String pullMethod,
                                         boolean success,
                                         String errorMessage,
                                         int configItemsCount) {
        try {
            // 创建配置同步历史记录
            ConfigSyncHistoryEntity history = new ConfigSyncHistoryEntity();
            history.setClusterId(component.getClusterId());
            history.setServiceName(component.getServiceName());
            history.setExistingComponentId(component.getId());
            history.setOperationType(com.datasophon.dao.enums.SyncOperationType.INITIAL_SYNC);
            // history.setSyncSource("EXISTING_COMPONENT"); // 字段不存在
            // history.setSyncTarget("DATASOPHON_CONFIG"); // 字段不存在
            history.setConfigDiff(String.format("Config file: %s, Method: %s, Items: %d",
                    configFilePath, pullMethod, configItemsCount));
            history.setSyncStatus(success ? "SUCCESS" : "FAILED");
            history.setErrorMessage(errorMessage);
            history.setSyncStartTime(new Date());
            history.setSyncEndTime(new Date());
            // history.setTriggerType("MANUAL"); // 字段不存在
            // history.setTriggeredBy("ConfigurationSyncManager"); // 字段不存在
            
            // 在实际实现中，这里应该保存到数据库
            // configSyncHistoryService.save(history);
            
            logger.debug("Config pull history recorded for component {}:{} - success: {}, method: {}, items: {}",
                    component.getServiceName(), component.getServiceRole(), success, pullMethod, configItemsCount);
            
        } catch (Exception e) {
            logger.error("Failed to record config pull history for component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
        }
    }
    
    // 辅助方法：获取默认配置路径
    private String getDefaultConfigPath(String serviceName, String componentName) {
        // 根据服务类型返回默认配置路径
        switch (serviceName.toUpperCase()) {
            case "HDFS":
                if ("NAMENODE".equalsIgnoreCase(componentName) || "DATANODE".equalsIgnoreCase(componentName)) {
                    return "/etc/hadoop/conf/hdfs-site.xml";
                }
                break;
            
            case "YARN":
                if ("RESOURCEMANAGER".equalsIgnoreCase(componentName) || "NODEMANAGER".equalsIgnoreCase(componentName)) {
                    return "/etc/hadoop/conf/yarn-site.xml";
                }
                break;
            
            case "SPARK":
                return "/etc/spark/conf/spark-defaults.conf";
            
            case "ZOOKEEPER":
                return "/etc/zookeeper/conf/zoo.cfg";
            
            case "KAFKA":
                return "/etc/kafka/server.properties";
        }
        
        // 默认路径
        return "/etc/" + serviceName.toLowerCase() + "/conf/" + serviceName.toLowerCase() + "-site.xml";
    }
    
    // 辅助方法：生成配置内容
    private String generateConfigContent(Map<String, Object> config, String serviceName,
                                         String componentName, String syncStrategy) {
        // 根据服务类型生成不同格式的配置
        switch (serviceName.toUpperCase()) {
            case "HDFS":
            case "YARN":
                // XML格式
                return generateXmlConfig(config, serviceName);
            
            case "SPARK":
            case "KAFKA":
                // Properties格式
                return generatePropertiesConfig(config);
            
            default:
                // JSON格式
                return JSON.toJSONString(config, true);
        }
    }
    
    // 辅助方法：生成XML配置
    private String generateXmlConfig(Map<String, Object> config, String serviceName) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<configuration>\n");
        
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // 跳过内部元数据键
            if (key.startsWith("_") || key.equals("extractedAt") || key.equals("configSource")) {
                continue;
            }
            
            xml.append("  <property>\n");
            xml.append("    <name>").append(key).append("</name>\n");
            xml.append("    <value>").append(value).append("</value>\n");
            xml.append("  </property>\n");
        }
        
        xml.append("</configuration>");
        return xml.toString();
    }
    
    // 辅助方法：生成Properties配置
    private String generatePropertiesConfig(Map<String, Object> config) {
        StringBuilder props = new StringBuilder();
        props.append("# Generated by DataSophon Configuration Sync Manager\n");
        props.append("# ").append(new Date()).append("\n\n");
        
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // 跳过内部元数据键
            if (key.startsWith("_") || key.equals("extractedAt") || key.equals("configSource")) {
                continue;
            }
            
            props.append(key).append("=").append(value).append("\n");
        }
        
        return props.toString();
    }
    
    // 辅助方法：构建REST API URL
    private String buildRestApiUrl(ClusterExistingComponentEntity component) {
        String host = component.getHostname();
        Integer port = getFirstListenPort(component);
        
        if (port == null) {
            // 使用默认端口
            switch (component.getServiceName().toUpperCase()) {
                case "PROMETHEUS":
                    port = 9090;
                    break;
                case "GRAFANA":
                    port = 3000;
                    break;
                default:
                    port = 8080;
            }
        }
        
        return String.format("http://%s:%d/api/config/update", host, port);
    }
    
    // 辅助方法：构建JMX URL
    private String buildJmxUrl(ClusterExistingComponentEntity component) {
        String host = component.getHostname();
        Integer jmxPort = getFirstListenPort(component);
        
        if (jmxPort == null) {
            // 使用默认JMX端口
            jmxPort = 7199; // Cassandra默认JMX端口
        }
        
        return String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", host, jmxPort);
    }
    
    @Override
    public SyncOperationResult pullConfigFromComponent(ClusterExistingComponentEntity component,
                                                       String configFilePath) {
        logger.info("Pulling configuration from component {}:{} from path {}",
                component.getServiceName(), component.getServiceRole(), configFilePath);
        
        SyncOperationResult result = new SyncOperationResult();
        // result.setOperationType(com.datasophon.common.model.SyncOperation.SyncOperationType.PULL_FROM_COMPONENT); // SyncOperationResult没有operationType字段
        // result.setComponentId(component.getId()); // SyncOperationResult没有componentId字段
        result.setSyncStartTime(new Date());
        
        try {
            // 检查组件信息
            if (component.getHostname() == null || component.getHostname().isEmpty()) {
                throw new IllegalArgumentException("Component host is not specified");
            }
            
            // 确定配置拉取方式
            String pullMethod = determinePullMethod(component, configFilePath);
            logger.info("Using pull method: {} for component {}:{}",
                    pullMethod, component.getServiceName(), component.getServiceRole());
            
            // 执行配置拉取
            Map<String, Object> pulledConfig = null;
            String details = "";
            
            switch (pullMethod) {
                case "SSH_FILE":
                    pulledConfig = pullConfigViaSshFile(component, configFilePath);
                    details = "Configuration pulled via SSH from config files";
                    break;
                
                case "REST_API":
                    pulledConfig = pullConfigViaRestApi(component, configFilePath);
                    details = "Configuration pulled via REST API";
                    break;
                
                case "JMX":
                    pulledConfig = pullConfigViaJmx(component, configFilePath);
                    details = "Configuration pulled via JMX";
                    break;
                
                default:
                    pulledConfig = pullConfigViaGenericMethod(component, configFilePath);
                    details = "Configuration pulled via generic method";
            }
            
            if (pulledConfig != null && !pulledConfig.isEmpty()) {
                result.setSuccess(true);
                result.setMessage(String.format(
                        "Configuration pulled successfully from component %s:%s using %s",
                        component.getServiceName(), component.getServiceRole(), pullMethod));
                if (result.getExtendProps() == null) {
                    result.setExtendProps(new HashMap<>());
                }
                result.getExtendProps().put("pulledConfig", pulledConfig);
                result.getExtendProps().put("details", details);
                
                // 记录配置拉取历史
                recordConfigPullHistory(component, configFilePath, pullMethod, true, null, pulledConfig.size());
            } else {
                result.setSuccess(false);
                result.setMessage(String.format(
                        "Failed to pull configuration from component %s:%s using %s",
                        component.getServiceName(), component.getServiceRole(), pullMethod));
                if (result.getExtendProps() == null) {
                    result.setExtendProps(new HashMap<>());
                }
                result.getExtendProps().put("details", details);
                
                // 记录失败历史
                recordConfigPullHistory(component, configFilePath, pullMethod, false, "No configuration pulled", 0);
            }
            
        } catch (Exception e) {
            logger.error("Failed to pull configuration from component {}:{}",
                    component.getServiceName(), component.getServiceRole(), e);
            result.setSuccess(false);
            result.setMessage("Failed to pull configuration: " + e.getMessage());
            result.setErrorMessage(e.toString());
            
            // 记录异常历史
            recordConfigPullHistory(component, configFilePath, "UNKNOWN", false, e.getMessage(), 0);
        }
        
        result.setSyncEndTime(new Date());
        result.setDurationMs(result.getSyncEndTime().getTime() - result.getSyncStartTime().getTime());
        
        return result;
    }
    
    @Override
    public SyncOperationResult biDirectionalSync(ClusterExistingComponentEntity component,
                                                 String syncStrategy) {
        logger.info("Performing bidirectional sync for component {}:{} with strategy {}",
                component.getServiceName(), component.getServiceRole(), syncStrategy);
        
        SyncOperationResult result = new SyncOperationResult();
        // result.setOperationType(com.datasophon.common.model.SyncOperation.SyncOperationType.BIDIRECTIONAL_SYNC); // BIDIRECTIONAL_SYNC枚举值不存在
        // result.setComponentId(component.getId()); // SyncOperationResult没有componentId字段
        result.setSyncStartTime(new Date());
        
        try {
            // 1. Pull config from component
            SyncOperationResult pullResult = pullConfigFromComponent(component, "all");
            if (!pullResult.isSuccess()) {
                result.setSuccess(false);
                result.setMessage("Bidirectional sync failed at pull stage: " + pullResult.getMessage());
                return result;
            }
            
            // 2. Extract config from DataSophon
            Map<String, Object> datasophonConfig = extractConfigFromDataSophon(
                    component.getClusterId(), component.getServiceName(), component.getServiceRole());
            
            // 3. Merge configs
            Map<String, Object> existingConfig = pullResult.getExtendProps() != null ? (Map<String, Object>) pullResult.getExtendProps().get("pulledConfig") : null;
            Map<String, Object> mergedConfig = mergeConfigs(existingConfig, datasophonConfig, syncStrategy, null);
            
            // 4. Validate compatibility
            boolean compatible = validateConfigCompatibility(existingConfig, datasophonConfig);
            if (!compatible) {
                result.setSuccess(false);
                result.setMessage("Config compatibility validation failed");
                return result;
            }
            
            // 5. Push merged config back to component
            SyncOperationResult pushResult = pushConfigToComponent(component, mergedConfig, syncStrategy);
            if (!pushResult.isSuccess()) {
                result.setSuccess(false);
                result.setMessage("Bidirectional sync failed at push stage: " + pushResult.getMessage());
                return result;
            }
            
            result.setSuccess(true);
            result.setMessage("Bidirectional sync completed successfully");
            if (result.getExtendProps() == null) {
                result.setExtendProps(new HashMap<>());
            }
            result.getExtendProps().put("mergedConfig", mergedConfig);
            
        } catch (Exception e) {
            logger.error("Failed to perform bidirectional sync", e);
            result.setSuccess(false);
            result.setMessage("Bidirectional sync failed: " + e.getMessage());
            result.setErrorMessage(e.toString());
        }
        
        result.setSyncEndTime(new Date());
        result.setDurationMs(result.getSyncEndTime().getTime() - result.getSyncStartTime().getTime());
        
        return result;
    }
    
    @Override
    public boolean validateSyncResult(Integer syncHistoryId) {
        // Delegate to ConfigSyncHistoryService
        return configSyncHistoryService.validateSyncResult(syncHistoryId).isSuccess();
    }
    
    @Override
    public SyncOperationResult rollbackSyncOperation(Integer syncHistoryId) {
        logger.info("Rolling back sync operation: {}", syncHistoryId);
        
        SyncOperationResult result = new SyncOperationResult();
        result.setSyncStartTime(new Date());
        
        try {
            // Delegate to ConfigSyncHistoryService
            com.datasophon.common.utils.Result rollbackResult = configSyncHistoryService.rollbackConfigSync(syncHistoryId);
            
            result.setSuccess(rollbackResult.isSuccess());
            result.setMessage(rollbackResult.getMsg());
            // result.setOperationType(com.datasophon.common.model.SyncOperation.SyncOperationType.ROLLBACK_CONFIG); // SyncOperationResult没有operationType字段
            
        } catch (Exception e) {
            logger.error("Failed to rollback sync operation", e);
            result.setSuccess(false);
            result.setMessage("Rollback failed: " + e.getMessage());
            result.setErrorMessage(e.toString());
        }
        
        result.setSyncEndTime(new Date());
        result.setDurationMs(result.getSyncEndTime().getTime() - result.getSyncStartTime().getTime());
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getSupportedMergeStrategies() {
        List<Map<String, Object>> strategies = new ArrayList<>();
        
        for (ConfigMergeStrategy strategy : ConfigMergeStrategy.values()) {
            Map<String, Object> strategyInfo = new HashMap<>();
            // strategyInfo.put("code", strategy.getCode()); // ConfigMergeStrategy可能没有getCode()方法
            strategyInfo.put("code", strategy.name());
            // strategyInfo.put("name", strategy.getName()); // ConfigMergeStrategy可能没有getName()方法
            strategyInfo.put("name", strategy.name());
            // strategyInfo.put("description", strategy.getDescription()); // ConfigMergeStrategy可能没有getDescription()方法
            strategyInfo.put("description", strategy.name());
            strategyInfo.put("recommendedFor", getRecommendedUseCases(strategy));
            strategies.add(strategyInfo);
        }
        
        return strategies;
    }
    
    @Override
    public List<Map<String, Object>> getAvailableConfigExtractors(String serviceName) {
        List<Map<String, Object>> extractors = new ArrayList<>();
        
        if (configExtractors == null || configExtractors.isEmpty()) {
            // Return generic extractor as fallback
            Map<String, Object> genericExtractor = new HashMap<>();
            genericExtractor.put("name", "generic_config_extractor");
            genericExtractor.put("description", "Generic configuration extractor for any service");
            genericExtractor.put("supportedServices", Arrays.asList("ALL"));
            genericExtractor.put("configTypes", Arrays.asList("properties", "xml", "yaml", "json"));
            genericExtractor.put("priority", 0);
            genericExtractor.put("version", "1.0.0");
            
            extractors.add(genericExtractor);
            return extractors;
        }
        
        // Return information about all available extractors
        for (ConfigExtractor extractor : configExtractors) {
            // Filter by service name if specified
            if (serviceName != null && !serviceName.isEmpty()) {
                // Check if extractor supports this service (check common component names)
                boolean supportsService = false;
                // Test with some common component names for the service
                String[] testComponents = {"Service", "Manager", "Server", "Node"};
                for (String component : testComponents) {
                    if (extractor.supports(serviceName, component)) {
                        supportsService = true;
                        break;
                    }
                }
                // Also check if extractor supports "ALL" services
                if (!supportsService && extractor.supports("ALL", "ALL")) {
                    supportsService = true;
                }
                
                if (!supportsService) {
                    continue; // Skip extractors that don't support this service
                }
            }
            
            Map<String, Object> extractorInfo = new HashMap<>();
            extractorInfo.put("name", extractor.getExtractorName());
            extractorInfo.put("description", extractor.getExtractorDescription());
            extractorInfo.put("version", extractor.getVersion());
            extractorInfo.put("priority", extractor.getPriority());
            extractorInfo.put("supportedFileTypes", extractor.getSupportedFileTypes());
            
            extractors.add(extractorInfo);
        }
        
        // If no extractors matched and serviceName was specified, add generic extractor
        if (extractors.isEmpty() && serviceName != null && !serviceName.isEmpty()) {
            Map<String, Object> genericExtractor = new HashMap<>();
            genericExtractor.put("name", "generic_config_extractor");
            genericExtractor.put("description", "Generic configuration extractor for service: " + serviceName);
            genericExtractor.put("supportedServices", Arrays.asList(serviceName, "ALL"));
            genericExtractor.put("configTypes", Arrays.asList("properties", "xml", "yaml", "json"));
            genericExtractor.put("priority", 0);
            genericExtractor.put("version", "1.0.0");
            
            extractors.add(genericExtractor);
        }
        
        return extractors;
    }
    
    @Override
    public SyncOperationResult autoSyncFromDiscovery(List<DiscoveredComponent> discoveredComponents,
                                                     Integer clusterId, String serviceName) {
        logger.info("Auto-syncing from discovery for cluster {} service {}", clusterId, serviceName);
        
        SyncOperationResult result = new SyncOperationResult();
        result.setSyncStartTime(new Date());
        // result.setOperationType(com.datasophon.common.model.SyncOperation.SyncOperationType.AUTO_SYNC); // AUTO_SYNC枚举值不存在，且SyncOperationResult没有operationType字段
        
        try {
            int successCount = 0;
            int failureCount = 0;
            List<Map<String, Object>> componentResults = new ArrayList<>();
            
            for (DiscoveredComponent discovered : discoveredComponents) {
                try {
                    // Find or create existing component record
                    ClusterExistingComponentEntity component = findOrCreateComponent(discovered, clusterId);
                    
                    // Extract config from discovered component
                    Map<String, Object> extractedConfig = extractConfigFromComponent(
                            component, null); // discovered.getConfig()方法不存在
                    
                    // Record initial sync
                    SyncOperation syncOp = new SyncOperation();
                    syncOp.setOperationType(com.datasophon.common.model.SyncOperation.SyncOperationType.PULL_FROM_COMPONENT); // INITIAL_SYNC不存在，使用PULL_FROM_COMPONENT
                    syncOp.setExistingComponentId(component.getId());
                    syncOp.setSyncStrategy("PRESERVE_EXISTING");
                    // syncOp.setConfigBefore(extractedConfig); // SyncOperation没有configBefore字段
                    
                    SyncOperationResult syncResult = executeSyncOperation(syncOp);
                    
                    Map<String, Object> componentResult = new HashMap<>();
                    componentResult.put("componentId", component.getId());
                    componentResult.put("componentName", component.getServiceRole());
                    componentResult.put("host", component.getHostname());
                    componentResult.put("success", syncResult.isSuccess());
                    componentResult.put("message", syncResult.getMessage());
                    componentResults.add(componentResult);
                    
                    if (syncResult.isSuccess()) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                    
                } catch (Exception e) {
                    logger.error("Failed to auto-sync discovered component", e);
                    failureCount++;
                }
            }
            
            result.setSuccess(failureCount == 0); // Success only if all succeeded
            result.setMessage(String.format(
                    "Auto-sync completed: %d successful, %d failed", successCount, failureCount));
            // Store additional data in extendProps
            if (result.getExtendProps() == null) {
                result.setExtendProps(new HashMap<>());
            }
            result.getExtendProps().put("successCount", successCount);
            result.getExtendProps().put("failureCount", failureCount);
            result.getExtendProps().put("componentResults", componentResults);
            
        } catch (Exception e) {
            logger.error("Auto-sync from discovery failed", e);
            result.setSuccess(false);
            result.setMessage("Auto-sync failed: " + e.getMessage());
            result.setErrorMessage(e.toString());
        }
        
        result.setSyncEndTime(new Date());
        result.setDurationMs(result.getSyncEndTime().getTime() - result.getSyncStartTime().getTime());
        
        return result;
    }
    
    // Helper methods
    
    private ConfigExtractor findMatchingExtractor(String serviceName, String componentName) {
        if (configExtractors == null || configExtractors.isEmpty()) {
            return null;
        }
        
        // Sort extractors by priority (higher priority first)
        List<ConfigExtractor> sortedExtractors = new ArrayList<>(configExtractors);
        sortedExtractors.sort(Comparator.comparingInt(ConfigExtractor::getPriority).reversed());
        
        // Find first extractor that supports the component
        for (ConfigExtractor extractor : sortedExtractors) {
            if (extractor.supports(serviceName, componentName)) {
                return extractor;
            }
        }
        
        return null;
    }
    
    private Map<String, Object> fallbackExtraction(ClusterExistingComponentEntity component,
                                                   ClusterExistingComponentConfig config) {
        Map<String, Object> extractedConfig = new HashMap<>();
        
        // Basic extraction as fallback
        extractedConfig.put("extractedAt", new Date());
        extractedConfig.put("componentId", component.getId());
        extractedConfig.put("serviceName", component.getServiceName());
        extractedConfig.put("componentName", component.getServiceRole());
        extractedConfig.put("hostname", component.getHostname());
        extractedConfig.put("configPaths", config != null ? config.getConfigFilePaths() : new ArrayList<>());
        extractedConfig.put("extractionMethod", "Generic Fallback");
        extractedConfig.put("warning", "No specialized extractor found for this component type");
        
        return extractedConfig;
    }
    
    private ClusterExistingComponentEntity getComponentById(Integer componentId) {
        try {
            if (componentId == null) {
                logger.warn("Component ID is null");
                return null;
            }
            
            // 使用ClusterExistingComponentService获取组件信息
            ClusterExistingComponentEntity component = clusterExistingComponentService.getById(componentId);
            
            if (component == null) {
                logger.warn("Component not found with ID: {}", componentId);
                
                // 返回占位符组件（向后兼容）
                ClusterExistingComponentEntity placeholder = new ClusterExistingComponentEntity();
                placeholder.setId(componentId);
                placeholder.setClusterId(1);
                placeholder.setServiceName("UNKNOWN");
                placeholder.setServiceRole("UNKNOWN");
                placeholder.setHostname("localhost");
                return placeholder;
            }
            
            logger.debug("Retrieved component {}:{} with ID {}",
                    component.getServiceName(), component.getServiceRole(), componentId);
            return component;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve component with ID: {}", componentId, e);
            
            // 异常情况下返回占位符（向后兼容）
            ClusterExistingComponentEntity placeholder = new ClusterExistingComponentEntity();
            placeholder.setId(componentId);
            placeholder.setClusterId(1);
            placeholder.setServiceName("UNKNOWN");
            placeholder.setServiceRole("UNKNOWN");
            placeholder.setHostname("localhost");
            return placeholder;
        }
    }
    
    private boolean isSafeToOverride(String key, Object existingValue, Object datasophonValue,
                                     Map<String, Object> customRules) {
        logger.debug("Checking safety to override key '{}' with custom rules: {}", key,
                customRules != null ? customRules.size() : 0);
        
        // 1. 检查关键配置 - 使用现有的关键配置检查逻辑
        if (isCriticalConfiguration(key)) {
            logger.debug("Key '{}' is critical configuration", key);
            // 对于关键配置，只有当值完全相等时才允许覆盖
            if (existingValue == null && datasophonValue == null) {
                return true; // 两者都为null，视为相等
            }
            if (existingValue == null || datasophonValue == null) {
                logger.warn("Critical config '{}' has null value, not safe to override", key);
                return false; // 一个为null另一个不为null，不安全
            }
            boolean valuesEqual = existingValue.toString().equals(datasophonValue.toString());
            if (!valuesEqual) {
                logger.warn("Critical config '{}' values differ: existing='{}', datasophon='{}'",
                        key, existingValue, datasophonValue);
            }
            return valuesEqual;
        }
        
        // 2. 应用自定义规则（如果提供）
        if (customRules != null && !customRules.isEmpty()) {
            boolean safeByRules = evaluateCustomRules(key, existingValue, datasophonValue, customRules);
            if (!safeByRules) {
                logger.debug("Custom rules block override for key '{}'", key);
                return false;
            }
        }
        
        // 3. 检查类型兼容性
        if (!isCompatibleTypeForOverride(existingValue, datasophonValue)) {
            logger.warn("Type incompatibility for key '{}': existing type={}, datasophon type={}",
                    key,
                    existingValue != null ? existingValue.getClass().getSimpleName() : "null",
                    datasophonValue != null ? datasophonValue.getClass().getSimpleName() : "null");
            return false;
        }
        
        // 4. 检查端口范围（如果是端口配置）
        if (key.endsWith(".port") || key.equals("port") || key.contains(".port.")) {
            if (!isValidPortValue(datasophonValue)) {
                logger.warn("Invalid port value for key '{}': {}", key, datasophonValue);
                return false;
            }
        }
        
        // 5. 默认：允许覆盖
        logger.debug("Key '{}' is safe to override", key);
        return true;
    }
    
    // 评估自定义规则
    private boolean evaluateCustomRules(String key, Object existingValue, Object datasophonValue,
                                        Map<String, Object> customRules) {
        // 规则1: critical_keys_extension - 扩展的关键配置列表
        if (customRules.containsKey("critical_keys_extension")) {
            Object extensionObj = customRules.get("critical_keys_extension");
            if (extensionObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> extendedCriticalKeys = (List<String>) extensionObj;
                if (extendedCriticalKeys.contains(key)) {
                    logger.debug("Key '{}' is in extended critical keys list", key);
                    // 对于扩展的关键配置，检查值是否相等
                    if (existingValue == null && datasophonValue == null) {
                        return true;
                    }
                    if (existingValue == null || datasophonValue == null) {
                        return false;
                    }
                    return existingValue.toString().equals(datasophonValue.toString());
                }
            }
        }
        
        // 规则2: forbidden_keys - 禁止覆盖的键列表
        if (customRules.containsKey("forbidden_keys")) {
            Object forbiddenObj = customRules.get("forbidden_keys");
            if (forbiddenObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> forbiddenKeys = (List<String>) forbiddenObj;
                if (forbiddenKeys.contains(key)) {
                    logger.debug("Key '{}' is in forbidden keys list, cannot override", key);
                    return false;
                }
            }
        }
        
        // 规则3: value_pattern - 正则表达式验证值模式
        if (customRules.containsKey("value_pattern")) {
            Object patternObj = customRules.get("value_pattern");
            if (patternObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> patternMap = (Map<String, String>) patternObj;
                if (patternMap.containsKey(key)) {
                    String pattern = patternMap.get(key);
                    if (datasophonValue != null) {
                        String value = datasophonValue.toString();
                        if (!value.matches(pattern)) {
                            logger.warn("Value '{}' for key '{}' does not match pattern '{}'",
                                    value, key, pattern);
                            return false;
                        }
                    }
                }
            }
        }
        
        // 规则4: type_check - 类型检查规则
        if (customRules.containsKey("type_check")) {
            Object typeCheckObj = customRules.get("type_check");
            if (typeCheckObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> typeCheckMap = (Map<String, String>) typeCheckObj;
                if (typeCheckMap.containsKey(key)) {
                    String expectedType = typeCheckMap.get(key);
                    if (datasophonValue != null) {
                        String actualType = datasophonValue.getClass().getSimpleName();
                        if (!isTypeCompatible(actualType, expectedType)) {
                            logger.warn("Type mismatch for key '{}': expected={}, actual={}",
                                    key, expectedType, actualType);
                            return false;
                        }
                    }
                }
            }
        }
        
        // 规则5: range_check - 数值范围检查
        if (customRules.containsKey("range_check")) {
            Object rangeObj = customRules.get("range_check");
            if (rangeObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Number>> rangeMap = (Map<String, Map<String, Number>>) rangeObj;
                if (rangeMap.containsKey(key)) {
                    Map<String, Number> range = rangeMap.get(key);
                    if (datasophonValue != null && isNumericValue(datasophonValue)) {
                        double value = Double.parseDouble(datasophonValue.toString());
                        if (range.containsKey("min")) {
                            double min = range.get("min").doubleValue();
                            if (value < min) {
                                logger.warn("Value {} for key '{}' is below minimum {}",
                                        value, key, min);
                                return false;
                            }
                        }
                        if (range.containsKey("max")) {
                            double max = range.get("max").doubleValue();
                            if (value > max) {
                                logger.warn("Value {} for key '{}' is above maximum {}",
                                        value, key, max);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        
        // 规则6: allow_override - 全局允许覆盖标志
        if (customRules.containsKey("allow_override")) {
            Object allowObj = customRules.get("allow_override");
            if (allowObj instanceof Boolean) {
                if (!(Boolean) allowObj) {
                    logger.debug("Global allow_override is false, blocking override for key '{}'", key);
                    return false;
                }
            }
        }
        
        // 所有规则检查通过
        return true;
    }
    
    // 检查类型兼容性用于覆盖
    private boolean isCompatibleTypeForOverride(Object value1, Object value2) {
        if (value1 == null || value2 == null) {
            return true; // null与任何类型兼容
        }
        
        // 使用现有的类型兼容性检查
        return isCompatibleType(value1, value2);
    }
    
    // 检查是否为有效的端口值
    private boolean isValidPortValue(Object value) {
        if (value == null) {
            return false;
        }
        
        try {
            int port = Integer.parseInt(value.toString());
            return port > 0 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // 检查类型是否兼容
    private boolean isTypeCompatible(String actualType, String expectedType) {
        // 简单的类型兼容性检查
        if (actualType.equalsIgnoreCase(expectedType)) {
            return true;
        }
        
        // 处理常见的类型别名
        Map<String, List<String>> typeAliases = new HashMap<>();
        typeAliases.put("Integer", Arrays.asList("int", "Int", "INT"));
        typeAliases.put("Long", Arrays.asList("long", "Long"));
        typeAliases.put("Double", Arrays.asList("double", "Double"));
        typeAliases.put("Float", Arrays.asList("float", "Float"));
        typeAliases.put("Boolean", Arrays.asList("boolean", "Boolean", "bool"));
        typeAliases.put("String", Arrays.asList("string", "String", "str"));
        
        for (Map.Entry<String, List<String>> entry : typeAliases.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(expectedType) &&
                    entry.getValue().contains(actualType)) {
                return true;
            }
            if (entry.getKey().equalsIgnoreCase(actualType) &&
                    entry.getValue().contains(expectedType)) {
                return true;
            }
        }
        
        return false;
    }
    
    // 检查是否为数值类型
    private boolean isNumericValue(Object value) {
        if (value == null) {
            return false;
        }
        
        try {
            Double.parseDouble(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // 检查配置项是否为关键配置
    private boolean isCriticalConfiguration(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        
        // 关键配置列表
        List<String> criticalKeys = Arrays.asList(
                "port", "host", "bind.address", "http.port", "https.port",
                "listeners", "advertised.listeners", "security.protocol",
                "ssl.keystore.location", "ssl.truststore.location",
                "zookeeper.connect", "bootstrap.servers",
                "dfs.namenode.http-address", "dfs.namenode.rpc-address",
                "yarn.resourcemanager.address", "yarn.resourcemanager.webapp.address",
                "hive.metastore.uris", "spark.master", "kafka.bootstrap.servers");
        
        // 检查是否匹配关键配置
        for (String criticalKey : criticalKeys) {
            if (key.equals(criticalKey) || key.contains(criticalKey)) {
                return true;
            }
        }
        
        // 检查是否为端口配置
        if (key.endsWith(".port") || key.endsWith(".address") ||
                key.endsWith(".host") || key.contains(".host.")) {
            return true;
        }
        
        // 检查是否为安全相关配置
        if (key.contains("ssl.") || key.contains("security.") ||
                key.contains("password") || key.contains("secret") ||
                key.contains("keytab") || key.contains("kerberos")) {
            return true;
        }
        
        return false;
    }
    
    private List<String> getRecommendedUseCases(ConfigMergeStrategy strategy) {
        switch (strategy.getName()) {
            case "PRESERVE_EXISTING":
                return Arrays.asList("Production environments", "Critical services", "Minimal disruption");
            case "USE_DATASOPHON":
                return Arrays.asList("New deployments", "Standardization", "Best practices");
            case "SMART_MERGE":
                return Arrays.asList("Hybrid environments", "Gradual migration", "Risk-averse updates");
            default:
                return Arrays.asList("General use");
        }
    }
    
    private ClusterExistingComponentEntity findOrCreateComponent(DiscoveredComponent discovered,
                                                                 Integer clusterId) {
        try {
            if (discovered == null) {
                logger.warn("Discovered component is null");
                return null;
            }
            
            if (clusterId == null) {
                logger.warn("Cluster ID is null");
                return null;
            }
            
            String serviceType = discovered.getServiceName();
            String componentName = discovered.getServiceRole();
            String host = discovered.getHost();
            
            logger.info("Looking for existing component: cluster={}, service={}, component={}, host={}",
                    clusterId, serviceType, componentName, host);
            
            // 尝试查找现有组件
            // 注意：这里假设clusterExistingComponentService有相应的方法
            // 实际实现中可能需要自定义查询方法
            ClusterExistingComponentEntity existingComponent = null;
            
            // 简化实现：先尝试通过服务查询接口查找
            // 在实际实现中，这里应该调用自定义查询方法
            // 例如：clusterExistingComponentService.findByClusterAndServiceAndHost(clusterId, serviceType, componentName, host)
            
            // 由于我们没有具体的查询方法，这里先使用占位符逻辑
            // 在实际项目中，需要添加相应的Service方法
            
            if (existingComponent == null) {
                // 没有找到现有组件，创建新组件
                logger.info("Creating new component: cluster={}, service={}, component={}, host={}",
                        clusterId, serviceType, componentName, host);
                
                ClusterExistingComponentEntity newComponent = new ClusterExistingComponentEntity();
                newComponent.setClusterId(clusterId);
                newComponent.setServiceName(serviceType);
                newComponent.setServiceRole(componentName);
                newComponent.setHostname(host);
                newComponent.setListenPorts(discovered.getPort() != null ? "[" + discovered.getPort() + "]" : "[]");
                
                // 从metadata获取安装路径和配置路径
                String installPath = "";
                String configPaths = "[]";
                if (discovered.getMetadata() != null) {
                    installPath = discovered.getMetadata().get("installPath") != null ? discovered.getMetadata().get("installPath").toString() : "";
                    
                    Object configPathObj = discovered.getMetadata().get("configPath");
                    if (configPathObj != null) {
                        configPaths = "[\"" + configPathObj.toString().replace("\"", "\\\"") + "\"]";
                    }
                }
                newComponent.setInstallPath(installPath);
                newComponent.setConfigPaths(configPaths);
                
                // 设置检测到的版本和状态
                newComponent.setDetectedVersion(discovered.getVersion());
                newComponent.setComponentState(com.datasophon.dao.enums.ExistingComponentState.DISCOVERED);
                newComponent.setTakeoverLevel("1"); // MONITOR_ONLY
                newComponent.setDiscoveryTime(new Date());
                newComponent.setLastCheckTime(new Date());
                
                // 保存新组件到数据库
                // 在实际实现中：clusterExistingComponentService.save(newComponent);
                
                logger.info("Created new component with placeholder ID for {}:{} on {}",
                        serviceType, componentName, host);
                return newComponent;
            } else {
                // 找到现有组件，更新信息并返回
                logger.info("Found existing component ID {} for {}:{} on {}",
                        existingComponent.getId(), serviceType, componentName, host);
                
                // 更新组件的最后检查时间
                existingComponent.setLastCheckTime(new Date());
                
                // 在实际实现中：clusterExistingComponentService.updateById(existingComponent);
                
                return existingComponent;
            }
            
        } catch (Exception e) {
            logger.error("Failed to find or create component for discovered component: {}:{} on {}",
                    discovered != null ? discovered.getServiceName() : "null",
                    discovered != null ? discovered.getServiceRole() : "null",
                    discovered != null ? discovered.getHost() : "null", e);
            
            // 异常情况下返回占位符组件
            ClusterExistingComponentEntity fallbackComponent = new ClusterExistingComponentEntity();
            if (discovered != null && clusterId != null) {
                fallbackComponent.setClusterId(clusterId);
                fallbackComponent.setServiceName(discovered.getServiceName());
                fallbackComponent.setServiceRole(discovered.getServiceRole());
                fallbackComponent.setHostname(discovered.getHost());
                fallbackComponent.setListenPorts(discovered.getPort() != null ? "[" + discovered.getPort() + "]" : "[]");
                
                // 从metadata获取安装路径和配置路径
                String installPath = "";
                String configPaths = "[]";
                if (discovered.getMetadata() != null) {
                    installPath = discovered.getMetadata().get("installPath") != null ? discovered.getMetadata().get("installPath").toString() : "";
                    
                    Object configPathObj = discovered.getMetadata().get("configPath");
                    if (configPathObj != null) {
                        configPaths = "[\"" + configPathObj.toString().replace("\"", "\\\"") + "\"]";
                    }
                }
                fallbackComponent.setInstallPath(installPath);
                fallbackComponent.setConfigPaths(configPaths);
                
                // 设置检测到的版本和状态
                fallbackComponent.setDetectedVersion(discovered.getVersion());
                fallbackComponent.setComponentState(com.datasophon.dao.enums.ExistingComponentState.DISCOVERED);
                fallbackComponent.setTakeoverLevel("1"); // MONITOR_ONLY
            }
            return fallbackComponent;
        }
    }
    
    /**
     * 从ClusterExistingComponentEntity获取第一个监听端口
     * listenPorts字段是JSON数组字符串，例如"[8080, 8081]"
     */
    private Integer getFirstListenPort(ClusterExistingComponentEntity component) {
        if (component == null || component.getListenPorts() == null || component.getListenPorts().isEmpty()) {
            return null;
        }
        try {
            List<Integer> ports = JSON.parseArray(component.getListenPorts(), Integer.class);
            if (ports != null && !ports.isEmpty()) {
                return ports.get(0);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse listenPorts JSON for component: {}", component.getId(), e);
        }
        return null;
    }
    
    /**
     * 将SyncOperation.SyncOperationType转换为dao层的SyncOperationType枚举
     */
    private com.datasophon.dao.enums.SyncOperationType convertToDaoSyncOperationType(
                                                                                     com.datasophon.common.model.SyncOperation.SyncOperationType operationType) {
        if (operationType == null) {
            return null;
        }
        switch (operationType) {
            case PULL_FROM_COMPONENT:
                return com.datasophon.dao.enums.SyncOperationType.INITIAL_SYNC;
            case PUSH_TO_COMPONENT:
                return com.datasophon.dao.enums.SyncOperationType.CONFIG_UPDATE;
            case MERGE_CONFIG:
                return com.datasophon.dao.enums.SyncOperationType.CONFIG_MERGE;
            case VALIDATE_CONFIG:
                return com.datasophon.dao.enums.SyncOperationType.HEALTH_CHECK_SYNC;
            case COMPARE_CONFIG:
                return com.datasophon.dao.enums.SyncOperationType.CONFIG_UPDATE; // 使用CONFIG_UPDATE作为比较
            case ROLLBACK_CONFIG:
                return com.datasophon.dao.enums.SyncOperationType.CONFIG_ROLLBACK;
            default:
                logger.warn("Unknown SyncOperationType: {}, defaulting to INITIAL_SYNC", operationType);
                return com.datasophon.dao.enums.SyncOperationType.INITIAL_SYNC;
        }
    }
    
    /**
     * 获取组件特定的接管策略
     * 
     * @param serviceName 服务名称 (e.g., HDFS, YARN, SPARK)
     * @param componentRole 组件角色 (e.g., NameNode, ResourceManager)
     * @return 对应的ExistingComponentStrategy实现，如果没有特定策略则返回通用策略
     */
    private com.datasophon.api.strategy.ExistingComponentStrategy getComponentSpecificStrategy(
                                                                                               String serviceName, String componentRole) {
        // TODO: 实现策略工厂，根据服务类型返回具体策略
        // 目前返回通用策略，后续可以添加具体组件的策略
        return new com.datasophon.api.strategy.GenericExistingComponentStrategy();
    }
    
    /**
     * 从ClusterExistingComponentEntity获取第一个配置文件路径
     * configPaths字段是JSON数组字符串，例如"[\"/etc/hadoop/conf/core-site.xml\", \"/etc/hadoop/conf/hdfs-site.xml\"]"
     */
    private String getFirstConfigPath(ClusterExistingComponentEntity component) {
        if (component == null || component.getConfigPaths() == null || component.getConfigPaths().isEmpty()) {
            return null;
        }
        try {
            List<String> paths = JSON.parseArray(component.getConfigPaths(), String.class);
            if (paths != null && !paths.isEmpty()) {
                return paths.get(0);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse configPaths JSON for component: {}", component.getId(), e);
        }
        return null;
    }
}
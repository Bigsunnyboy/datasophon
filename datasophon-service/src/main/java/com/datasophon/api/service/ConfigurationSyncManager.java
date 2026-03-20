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

package com.datasophon.api.service;

import com.datasophon.common.model.ClusterExistingComponentConfig;
import com.datasophon.common.model.DiscoveredComponent;
import com.datasophon.common.model.SyncOperation;
import com.datasophon.common.model.SyncOperationResult;
import com.datasophon.dao.entity.ClusterExistingComponentEntity;

import java.util.List;
import java.util.Map;

/**
 * 配置同步管理器
 * 负责管理配置的提取、合并、同步和验证
 */
public interface ConfigurationSyncManager {
    
    /**
     * 从现有组件提取配置
     */
    Map<String, Object> extractConfigFromComponent(ClusterExistingComponentEntity component,
                                                   ClusterExistingComponentConfig config);
    
    /**
     * 从DataSophon提取配置
     */
    Map<String, Object> extractConfigFromDataSophon(Integer clusterId, String serviceName,
                                                    String componentName);
    
    /**
     * 执行配置同步操作
     */
    SyncOperationResult executeSyncOperation(SyncOperation syncOperation);
    
    /**
     * 比较两个配置之间的差异
     */
    Map<String, Object> compareConfigs(Map<String, Object> config1, Map<String, Object> config2,
                                       String mergeStrategy);
    
    /**
     * 合并配置
     */
    Map<String, Object> mergeConfigs(Map<String, Object> existingConfig,
                                     Map<String, Object> datasophonConfig,
                                     String mergeStrategy, Map<String, Object> customRules);
    
    /**
     * 验证配置兼容性
     */
    boolean validateConfigCompatibility(Map<String, Object> existingConfig,
                                        Map<String, Object> datasophonConfig);
    
    /**
     * 将配置推送到现有组件
     */
    SyncOperationResult pushConfigToComponent(ClusterExistingComponentEntity component,
                                              Map<String, Object> config,
                                              String syncStrategy);
    
    /**
     * 从现有组件拉取配置
     */
    SyncOperationResult pullConfigFromComponent(ClusterExistingComponentEntity component,
                                                String configFilePath);
    
    /**
     * 执行双向配置同步
     */
    SyncOperationResult biDirectionalSync(ClusterExistingComponentEntity component,
                                          String syncStrategy);
    
    /**
     * 验证配置同步结果
     */
    boolean validateSyncResult(Integer syncHistoryId);
    
    /**
     * 回滚配置同步操作
     */
    SyncOperationResult rollbackSyncOperation(Integer syncHistoryId);
    
    /**
     * 获取支持的配置合并策略列表
     */
    List<Map<String, Object>> getSupportedMergeStrategies();
    
    /**
     * 获取配置提取器列表
     */
    List<Map<String, Object>> getAvailableConfigExtractors(String serviceName);
    
    /**
     * 执行自动配置同步（基于发现结果）
     */
    SyncOperationResult autoSyncFromDiscovery(List<DiscoveredComponent> discoveredComponents,
                                              Integer clusterId, String serviceName);
}
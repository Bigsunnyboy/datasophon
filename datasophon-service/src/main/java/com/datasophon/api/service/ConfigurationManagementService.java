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

import com.datasophon.common.utils.Result;

import java.util.List;

/**
 * 配置管理服务接口
 * 提供对已接管组件配置的同步、对比、历史管理等功能
 */
public interface ConfigurationManagementService {
    
    /**
     * 获取配置列表
     */
    Result listConfigurations(Integer page, Integer pageSize, String keyword, String clusterId, String configStatus);
    
    /**
     * 获取配置详情
     */
    Result getConfigurationDetail(String configId);
    
    /**
     * 获取配置对比信息
     */
    Result getConfigurationCompare(String configId);
    
    /**
     * 同步配置
     */
    Result syncConfiguration(String configId, String syncDirection);
    
    /**
     * 批量同步配置
     */
    Result batchSyncConfigurations(List<String> configIds, String syncDirection);
    
    /**
     * 获取同步状态
     */
    Result getSyncStatus(String syncTaskId);
    
    /**
     * 取消同步任务
     */
    Result cancelSyncTask(String syncTaskId);
    
    /**
     * 获取配置历史
     */
    Result getConfigHistory(String configId, Integer page, Integer pageSize);
    
    /**
     * 获取配置版本详情
     */
    Result getConfigVersionDetail(String versionId);
    
    /**
     * 回滚到指定版本
     */
    Result rollbackToVersion(String configId, String versionId);
    
    /**
     * 验证配置
     */
    Result validateConfiguration(String configId);
    
    /**
     * 批量验证配置
     */
    Result batchValidateConfigurations(List<String> configIds);
    
    /**
     * 导出配置
     */
    Result exportConfiguration(String configId, String format);
    
    /**
     * 导入配置
     */
    Result importConfiguration(String configId, String configData, String format);
    
    /**
     * 分析配置差异
     */
    Result analyzeConfigDiff(String configId);
    
    /**
     * 获取差异摘要
     */
    Result getDiffSummary(String configId);
    
    /**
     * 获取配置统计信息
     */
    Result getConfigStats();
    
    /**
     * 获取同步统计信息
     */
    Result getSyncStats();
    
    /**
     * 获取验证统计信息
     */
    Result getValidationStats();
}
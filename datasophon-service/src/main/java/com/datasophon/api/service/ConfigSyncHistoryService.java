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
import com.datasophon.dao.entity.ConfigSyncHistoryEntity;
import com.datasophon.dao.enums.SyncOperationType;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 配置同步历史表
 *
 * @author 
 * @email 
 * @date 
 */
public interface ConfigSyncHistoryService extends IService<ConfigSyncHistoryEntity> {
    
    /**
     * 记录配置同步操作
     */
    Result recordSyncOperation(ConfigSyncHistoryEntity syncHistory);
    
    /**
     * 根据现有组件ID查询同步历史
     */
    List<ConfigSyncHistoryEntity> listByExistingComponentId(Integer existingComponentId);
    
    /**
     * 根据集群ID和服务名称查询同步历史
     */
    List<ConfigSyncHistoryEntity> listByClusterAndService(Integer clusterId, String serviceName);
    
    /**
     * 查询指定时间范围内的同步历史
     */
    List<ConfigSyncHistoryEntity> listByTimeRange(Integer clusterId, Date startTime, Date endTime);
    
    /**
     * 根据操作类型查询同步历史
     */
    List<ConfigSyncHistoryEntity> listByOperationType(Integer clusterId, SyncOperationType operationType);
    
    /**
     * 获取最近一次成功的同步记录
     */
    ConfigSyncHistoryEntity getLastSuccessfulSync(Integer existingComponentId, String configFileName);
    
    /**
     * 执行配置同步操作
     */
    Result executeConfigSync(Integer componentId, String configFileName,
                             SyncOperationType operationType, String syncStrategy);
    
    /**
     * 回滚配置同步操作
     */
    Result rollbackConfigSync(Integer syncHistoryId);
    
    /**
     * 标记为已回滚
     */
    Result markAsRollbacked(Integer id, Integer rollbackOperationId);
    
    /**
     * 获取同步操作统计
     */
    Map<String, Object> getSyncStats(Integer clusterId, Date startTime, Date endTime);
    
    /**
     * 批量删除过期的同步历史记录
     */
    Result deleteExpiredSyncHistory(Integer daysToKeep);
    
    /**
     * 获取配置同步差异分析
     */
    Result analyzeConfigDiff(Integer syncHistoryId);
    
    /**
     * 验证配置同步结果
     */
    Result validateSyncResult(Integer syncHistoryId);
}
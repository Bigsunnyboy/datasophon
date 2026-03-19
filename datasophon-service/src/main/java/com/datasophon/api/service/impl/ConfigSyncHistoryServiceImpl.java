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

import com.datasophon.api.enums.Status;
import com.datasophon.api.service.ConfigSyncHistoryService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ConfigSyncHistoryEntity;
import com.datasophon.dao.enums.SyncOperationType;
import com.datasophon.dao.mapper.ConfigSyncHistoryMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 配置同步历史表
 *
 * @author 
 * @email 
 * @date 
 */
@Slf4j
@Service("configSyncHistoryService")
@Transactional
public class ConfigSyncHistoryServiceImpl extends ServiceImpl<ConfigSyncHistoryMapper, ConfigSyncHistoryEntity>
        implements
            ConfigSyncHistoryService {
    
    @Autowired
    private ConfigSyncHistoryMapper configSyncHistoryMapper;
    
    @Override
    public Result recordSyncOperation(ConfigSyncHistoryEntity syncHistory) {
        try {
            syncHistory.setCreateTime(new Date());
            if (syncHistory.getSyncStartTime() == null) {
                syncHistory.setSyncStartTime(new Date());
            }
            
            this.save(syncHistory);
            
            log.info("Recorded config sync operation: id={}, componentId={}, operationType={}",
                    syncHistory.getId(), syncHistory.getExistingComponentId(),
                    syncHistory.getOperationType());
            
            return Result.success().put(Constants.DATA, syncHistory.getId());
        } catch (Exception e) {
            log.error("Failed to record sync operation", e);
            return Result.error(Status.SYNC_CONFIG_FAILED.getCode(),
                    Status.SYNC_CONFIG_FAILED.getMsg());
        }
    }
    
    @Override
    public List<ConfigSyncHistoryEntity> listByExistingComponentId(Integer existingComponentId) {
        return configSyncHistoryMapper.listByExistingComponentId(existingComponentId);
    }
    
    @Override
    public List<ConfigSyncHistoryEntity> listByClusterAndService(Integer clusterId, String serviceName) {
        return configSyncHistoryMapper.listByClusterAndService(clusterId, serviceName);
    }
    
    @Override
    public List<ConfigSyncHistoryEntity> listByTimeRange(Integer clusterId, Date startTime, Date endTime) {
        return configSyncHistoryMapper.listByTimeRange(clusterId, startTime, endTime);
    }
    
    @Override
    public List<ConfigSyncHistoryEntity> listByOperationType(Integer clusterId, SyncOperationType operationType) {
        return configSyncHistoryMapper.listByOperationType(clusterId, operationType);
    }
    
    @Override
    public ConfigSyncHistoryEntity getLastSuccessfulSync(Integer existingComponentId, String configFileName) {
        return configSyncHistoryMapper.getLastSuccessfulSync(existingComponentId, configFileName);
    }
    
    @Override
    public Result executeConfigSync(Integer componentId, String configFileName,
                                    SyncOperationType operationType, String syncStrategy) {
        try {
            ConfigSyncHistoryEntity syncHistory = new ConfigSyncHistoryEntity();
            syncHistory.setExistingComponentId(componentId);
            syncHistory.setConfigFileName(configFileName);
            syncHistory.setOperationType(operationType);
            syncHistory.setSyncStrategy(syncStrategy);
            syncHistory.setSyncStatus("PENDING");
            syncHistory.setSyncStartTime(new Date());
            
            this.save(syncHistory);
            
            log.info("Started config sync: id={}, componentId={}, configFile={}, operation={}",
                    syncHistory.getId(), componentId, configFileName, operationType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("syncHistoryId", syncHistory.getId());
            result.put("status", "STARTED");
            result.put("message", "Config sync operation has been started");
            
            return Result.success().put(Constants.DATA, result);
        } catch (Exception e) {
            log.error("Failed to execute config sync: componentId={}, configFile={}",
                    componentId, configFileName, e);
            return Result.error(Status.SYNC_CONFIG_FAILED.getCode(),
                    Status.SYNC_CONFIG_FAILED.getMsg());
        }
    }
    
    @Override
    public Result rollbackConfigSync(Integer syncHistoryId) {
        try {
            ConfigSyncHistoryEntity syncHistory = this.getById(syncHistoryId);
            if (syncHistory == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            if (syncHistory.getRollbackFlag() != null && syncHistory.getRollbackFlag() == 1) {
                return Result.error(Status.CONFIG_ALREADY_ROLLBACKED.getCode(), Status.CONFIG_ALREADY_ROLLBACKED.getMsg());
            }
            
            ConfigSyncHistoryEntity rollbackRecord = new ConfigSyncHistoryEntity();
            rollbackRecord.setExistingComponentId(syncHistory.getExistingComponentId());
            rollbackRecord.setConfigFileName(syncHistory.getConfigFileName());
            rollbackRecord.setOperationType(SyncOperationType.CONFIG_ROLLBACK);
            rollbackRecord.setSyncStrategy("ROLLBACK");
            rollbackRecord.setSyncStatus("PENDING");
            rollbackRecord.setSyncStartTime(new Date());
            rollbackRecord.setRollbackFlag(0);
            
            this.save(rollbackRecord);
            
            int updated = configSyncHistoryMapper.markAsRollbacked(syncHistoryId,
                    rollbackRecord.getId(), new Date());
            
            if (updated > 0) {
                log.info("Rollback config sync: originalId={}, rollbackId={}",
                        syncHistoryId, rollbackRecord.getId());
                
                Map<String, Object> result = new HashMap<>();
                result.put("originalSyncId", syncHistoryId);
                result.put("rollbackSyncId", rollbackRecord.getId());
                result.put("status", "ROLLBACK_INITIATED");
                
                return Result.success().put(Constants.DATA, result);
            } else {
                return Result.error(Status.ROLLBACK_FAILED.getCode(), Status.ROLLBACK_FAILED.getMsg());
            }
        } catch (Exception e) {
            log.error("Failed to rollback config sync: syncHistoryId={}", syncHistoryId, e);
            return Result.error(Status.ROLLBACK_FAILED.getCode(), Status.ROLLBACK_FAILED.getMsg() + ": " + e.getMessage());
        }
    }
    
    @Override
    public Result markAsRollbacked(Integer id, Integer rollbackOperationId) {
        try {
            int updated = configSyncHistoryMapper.markAsRollbacked(id, rollbackOperationId, new Date());
            if (updated > 0) {
                log.info("Marked as rollbacked: id={}, rollbackOpId={}", id, rollbackOperationId);
                return Result.success();
            } else {
                return Result.error(Status.MARK_ROLLBACK_FAILED.getCode(), Status.MARK_ROLLBACK_FAILED.getMsg());
            }
        } catch (Exception e) {
            log.error("Failed to mark as rollbacked: id={}", id, e);
            return Result.error(Status.MARK_ROLLBACK_FAILED.getCode(), Status.MARK_ROLLBACK_FAILED.getMsg() + ": " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getSyncStats(Integer clusterId, Date startTime, Date endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        List<ConfigSyncHistoryEntity> syncHistoryList = this.listByTimeRange(clusterId, startTime, endTime);
        
        long totalSyncs = syncHistoryList.size();
        long successfulSyncs = syncHistoryList.stream()
                .filter(h -> "SUCCESS".equals(h.getSyncStatus()))
                .count();
        long failedSyncs = syncHistoryList.stream()
                .filter(h -> "FAILED".equals(h.getSyncStatus()))
                .count();
        long pendingSyncs = syncHistoryList.stream()
                .filter(h -> "PENDING".equals(h.getSyncStatus()))
                .count();
        
        Map<String, Long> operationTypeStats = new HashMap<>();
        for (SyncOperationType type : SyncOperationType.values()) {
            long count = syncHistoryList.stream()
                    .filter(h -> type.equals(h.getOperationType()))
                    .count();
            operationTypeStats.put(type.getDesc(), count);
        }
        
        stats.put("totalSyncs", totalSyncs);
        stats.put("successfulSyncs", successfulSyncs);
        stats.put("failedSyncs", failedSyncs);
        stats.put("pendingSyncs", pendingSyncs);
        stats.put("successRate", totalSyncs > 0 ? (successfulSyncs * 100.0 / totalSyncs) : 0);
        stats.put("operationTypeStats", operationTypeStats);
        
        return stats;
    }
    
    @Override
    public Result deleteExpiredSyncHistory(Integer daysToKeep) {
        try {
            Date cutoffDate = new Date(System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000));
            
            QueryWrapper<ConfigSyncHistoryEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lt("create_time", cutoffDate)
                    .eq("rollback_flag", 0);
            
            int deletedCount = this.baseMapper.delete(queryWrapper);
            
            log.info("Deleted expired sync history: count={}, daysToKeep={}", deletedCount, daysToKeep);
            
            return Result.success().put(Constants.DATA, deletedCount);
        } catch (Exception e) {
            log.error("Failed to delete expired sync history", e);
            return Result.error(Status.DELETE_EXPIRED_HISTORY_FAILED.getCode(), Status.DELETE_EXPIRED_HISTORY_FAILED.getMsg());
        }
    }
    
    @Override
    public Result analyzeConfigDiff(Integer syncHistoryId) {
        try {
            ConfigSyncHistoryEntity syncHistory = this.getById(syncHistoryId);
            if (syncHistory == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            String configBefore = syncHistory.getConfigBefore();
            String configAfter = syncHistory.getConfigAfter();
            
            Map<String, Object> diffAnalysis = new HashMap<>();
            diffAnalysis.put("syncHistoryId", syncHistoryId);
            diffAnalysis.put("hasConfigBefore", configBefore != null && !configBefore.isEmpty());
            diffAnalysis.put("hasConfigAfter", configAfter != null && !configAfter.isEmpty());
            diffAnalysis.put("hasConfigDiff", syncHistory.getConfigDiff() != null && !syncHistory.getConfigDiff().isEmpty());
            diffAnalysis.put("operationType", syncHistory.getOperationType());
            diffAnalysis.put("syncStrategy", syncHistory.getSyncStrategy());
            
            return Result.success().put(Constants.DATA, diffAnalysis);
        } catch (Exception e) {
            log.error("Failed to analyze config diff: syncHistoryId={}", syncHistoryId, e);
            return Result.error(Status.ANALYZE_CONFIG_DIFF_FAILED.getCode(), Status.ANALYZE_CONFIG_DIFF_FAILED.getMsg());
        }
    }
    
    @Override
    public Result validateSyncResult(Integer syncHistoryId) {
        try {
            ConfigSyncHistoryEntity syncHistory = this.getById(syncHistoryId);
            if (syncHistory == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("syncHistoryId", syncHistoryId);
            validationResult.put("syncStatus", syncHistory.getSyncStatus());
            validationResult.put("hasError", syncHistory.getErrorMessage() != null && !syncHistory.getErrorMessage().isEmpty());
            validationResult.put("durationMs", syncHistory.getDurationMs());
            validationResult.put("operationType", syncHistory.getOperationType());
            
            if ("SUCCESS".equals(syncHistory.getSyncStatus())) {
                validationResult.put("validationStatus", "VALID");
                validationResult.put("message", "配置同步成功");
            } else if ("FAILED".equals(syncHistory.getSyncStatus())) {
                validationResult.put("validationStatus", "INVALID");
                validationResult.put("message", "配置同步失败: " + syncHistory.getErrorMessage());
            } else {
                validationResult.put("validationStatus", "PENDING");
                validationResult.put("message", "配置同步进行中");
            }
            
            return Result.success().put(Constants.DATA, validationResult);
        } catch (Exception e) {
            log.error("Failed to validate sync result: syncHistoryId={}", syncHistoryId, e);
            return Result.error(Status.VALIDATE_SYNC_RESULT_FAILED.getCode(), Status.VALIDATE_SYNC_RESULT_FAILED.getMsg());
        }
    }
}
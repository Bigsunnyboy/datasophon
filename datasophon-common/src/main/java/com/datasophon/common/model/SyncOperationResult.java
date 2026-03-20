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

package com.datasophon.common.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 配置同步操作结果模型
 */
@Data
public class SyncOperationResult {
    
    /**
     * 同步操作ID
     */
    private String operationId;
    
    /**
     * 同步历史记录ID
     */
    private Integer syncHistoryId;
    
    /**
     * 同步状态
     */
    private SyncStatus syncStatus;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 同步消息
     */
    private String message;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 配置差异
     */
    private Map<String, Object> configDiff;
    
    /**
     * 变更项数量
     */
    private Integer changedItems;
    
    /**
     * 新增配置项
     */
    private List<ConfigChange> addedItems;
    
    /**
     * 删除配置项
     */
    private List<ConfigChange> removedItems;
    
    /**
     * 修改配置项
     */
    private List<ConfigChange> modifiedItems;
    
    /**
     * 警告信息列表
     */
    private List<String> warnings;
    
    /**
     * 备份文件路径
     */
    private String backupPath;
    
    /**
     * 同步开始时间
     */
    private Date syncStartTime;
    
    /**
     * 同步结束时间
     */
    private Date syncEndTime;
    
    /**
     * 同步耗时（毫秒）
     */
    private Long durationMs;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> extendProps;
    
    /**
     * 配置变更详情
     */
    @Data
    public static class ConfigChange {
        
        /**
         * 配置键
         */
        private String key;
        
        /**
         * 旧值
         */
        private Object oldValue;
        
        /**
         * 新值
         */
        private Object newValue;
        
        /**
         * 变更类型
         */
        private ChangeType changeType;
        
        /**
         * 变更描述
         */
        private String description;
        
        /**
         * 风险级别
         */
        private RiskLevel riskLevel;
        
        /**
         * 配置文件路径
         */
        private String configFilePath;
        
        /**
         * 是否已应用
         */
        private boolean applied;
        
        /**
         * 应用时间
         */
        private Date appliedTime;
    }
    
    /**
     * 同步状态枚举
     */
    public enum SyncStatus {
        PENDING,
        RUNNING,
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED,
        ROLLBACKED,
        CANCELLED
    }
    
    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        ADDED,
        MODIFIED,
        REMOVED,
        RENAMED,
        MOVED
    }
    
    /**
     * 风险级别枚举
     */
    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
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
import java.util.Map;

import lombok.Data;

/**
 * 配置同步操作模型
 */
@Data
public class SyncOperation {
    
    /**
     * 同步操作ID
     */
    private String operationId;
    
    /**
     * 现有组件ID
     */
    private Integer existingComponentId;
    
    /**
     * 集群ID
     */
    private Integer clusterId;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 组件名称
     */
    private String componentName;
    
    /**
     * 配置文件名称
     */
    private String configFileName;
    
    /**
     * 配置文件路径
     */
    private String configFilePath;
    
    /**
     * 同步操作类型
     */
    private SyncOperationType operationType;
    
    /**
     * 同步方向
     */
    private SyncDirection syncDirection;
    
    /**
     * 同步策略
     */
    private String syncStrategy;
    
    /**
     * 合并策略
     */
    private String mergeStrategy;
    
    /**
     * 自定义合并规则
     */
    private Map<String, Object> customMergeRules;
    
    /**
     * 是否自动执行
     */
    private boolean autoExecute;
    
    /**
     * 是否验证配置
     */
    private boolean validateConfig;
    
    /**
     * 是否创建备份
     */
    private boolean createBackup;
    
    /**
     * 备份路径
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
     * 操作人
     */
    private String operator;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> extendProps;
    
    /**
     * 同步操作类型枚举
     */
    public enum SyncOperationType {
        PULL_FROM_COMPONENT,
        PUSH_TO_COMPONENT,
        MERGE_CONFIG,
        VALIDATE_CONFIG,
        COMPARE_CONFIG,
        ROLLBACK_CONFIG
    }
    
    /**
     * 同步方向枚举
     */
    public enum SyncDirection {
        TO_DATASOPHON,
        TO_COMPONENT,
        BIDIRECTIONAL
    }
}
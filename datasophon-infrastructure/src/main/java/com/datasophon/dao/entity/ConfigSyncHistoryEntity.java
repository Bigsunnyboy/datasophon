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

package com.datasophon.dao.entity;

import com.datasophon.dao.enums.SyncOperationType;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 配置同步历史表
 * 记录平台配置与现有组件配置之间的同步操作历史
 */
@TableName("t_ddh_config_sync_history")
@Data
public class ConfigSyncHistoryEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId
    private Integer id;
    
    /**
     * 集群ID
     */
    private Integer clusterId;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 服务角色
     */
    private String serviceRole;
    
    /**
     * 主机名
     */
    private String hostname;
    
    /**
     * 现有组件ID (关联ClusterExistingComponentEntity)
     */
    private Integer existingComponentId;
    
    /**
     * 配置文件名称 (如: core-site.xml, hdfs-site.xml)
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
     * 同步前配置 (JSON格式存储原始配置)
     */
    private String configBefore;
    
    /**
     * 同步后配置 (JSON格式存储新配置)
     */
    private String configAfter;
    
    /**
     * 配置差异 (JSON格式存储差异详情)
     */
    private String configDiff;
    
    /**
     * 同步策略 (PRESERVE_EXISTING, OVERRIDE_WITH_PLATFORM, SMART_MERGE)
     */
    private String syncStrategy;
    
    /**
     * 同步状态 (SUCCESS, FAILED, PARTIAL)
     */
    private String syncStatus;
    
    /**
     * 错误信息 (同步失败时记录)
     */
    private String errorMessage;
    
    /**
     * 同步开始时间
     */
    private Date syncStartTime;
    
    /**
     * 同步结束时间
     */
    private Date syncEndTime;
    
    /**
     * 同步耗时(毫秒)
     */
    private Long durationMs;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 回滚标识 (0:正常同步, 1:已回滚)
     */
    private Integer rollbackFlag;
    
    /**
     * 回滚时间
     */
    private Date rollbackTime;
    
    /**
     * 回滚操作ID (关联到自身的回滚操作)
     */
    private Integer rollbackOperationId;
    
    /**
     * 备注信息
     */
    private String remark;
    
    @TableField(exist = false)
    private Integer operationTypeCode;
    
    @TableField(exist = false)
    private String clusterName;
    
    @TableField(exist = false)
    private String serviceLabel;
}
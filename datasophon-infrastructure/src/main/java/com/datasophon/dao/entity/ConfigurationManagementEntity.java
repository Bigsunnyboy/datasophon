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

import com.datasophon.dao.enums.ConfigStatus;
import com.datasophon.dao.enums.ConfigSyncStatus;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 配置管理实体
 * 存储已接管组件的配置项信息，包括配置的元数据、状态、同步信息等
 */
@TableName("t_ddh_configuration_management")
@Data
public class ConfigurationManagementEntity implements Serializable {
    
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
     * 服务名称 (如: HDFS, YARN, SPARK)
     */
    private String serviceName;
    
    /**
     * 服务角色 (如: NameNode, DataNode, ResourceManager)
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
     * 配置项名称 (如: dfs.replication, yarn.scheduler.minimum-allocation-mb)
     */
    private String configName;
    
    /**
     * 配置文件名称 (如: core-site.xml, hdfs-site.xml)
     */
    private String configFileName;
    
    /**
     * 配置文件路径
     */
    private String configFilePath;
    
    /**
     * 配置项在文件中的路径 (如: /configuration/property[@name='dfs.replication'])
     */
    private String configItemPath;
    
    /**
     * 配置项数据类型 (STRING, INTEGER, BOOLEAN, LIST, MAP)
     */
    private String dataType;
    
    /**
     * 配置项默认值
     */
    private String defaultValue;
    
    /**
     * 配置项当前值 (来自现有组件)
     */
    private String currentValue;
    
    /**
     * 配置项平台值 (来自DataSophon平台)
     */
    private String platformValue;
    
    /**
     * 配置项推荐值 (由平台分析得出)
     */
    private String recommendedValue;
    
    /**
     * 配置项描述
     */
    private String description;
    
    /**
     * 配置项重要性 (CRITICAL, IMPORTANT, NORMAL, LOW)
     */
    private String importanceLevel;
    
    /**
     * 配置项状态
     */
    private ConfigStatus configStatus;
    
    /**
     * 配置同步状态
     */
    private ConfigSyncStatus syncStatus;
    
    /**
     * 最后同步时间
     */
    private Date lastSyncTime;
    
    /**
     * 最后同步方向 (TO_PLATFORM, TO_COMPONENT)
     */
    private String lastSyncDirection;
    
    /**
     * 最后同步任务ID (关联ConfigSyncHistoryEntity)
     */
    private Integer lastSyncTaskId;
    
    /**
     * 配置验证状态 (VALID, INVALID, WARNING)
     */
    private String validationStatus;
    
    /**
     * 最后验证时间
     */
    private Date lastValidationTime;
    
    /**
     * 验证结果 (JSON格式存储详细验证信息)
     */
    private String validationResult;
    
    /**
     * 配置项是否可修改 (0:只读, 1:可修改)
     */
    private Integer modifiable;
    
    /**
     * 配置项是否已加密 (0:未加密, 1:已加密)
     */
    private Integer encrypted;
    
    /**
     * 配置项敏感级别 (0:非敏感, 1:敏感, 2:高度敏感)
     */
    private Integer sensitivityLevel;
    
    /**
     * 配置项版本号 (每次修改递增)
     */
    private Integer version;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 备注信息
     */
    private String remark;
    
    // 非数据库字段
    @TableField(exist = false)
    private String clusterName;
    
    @TableField(exist = false)
    private String serviceLabel;
    
    @TableField(exist = false)
    private ConfigStatus configStatusCode;
    
    @TableField(exist = false)
    private ConfigSyncStatus syncStatusCode;
}
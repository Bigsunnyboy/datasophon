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

import com.datasophon.dao.enums.OperationStatus;
import com.datasophon.dao.enums.OperationType;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 运维管理实体
 * 记录对已接管组件的运维操作，包括服务启停、健康检查、日志收集、配置同步等操作
 */
@TableName("t_ddh_operations_management")
@Data
public class OperationsManagementEntity implements Serializable {
    
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
     * 操作类型
     */
    private OperationType operationType;
    
    /**
     * 操作子类型 (如: START_SERVICE, STOP_SERVICE, RESTART_SERVICE, CHECK_HEALTH, COLLECT_LOGS, SYNC_CONFIG)
     */
    private String operationSubType;
    
    /**
     * 操作参数 (JSON格式存储操作参数)
     */
    private String operationParams;
    
    /**
     * 操作状态
     */
    private OperationStatus operationStatus;
    
    /**
     * 操作开始时间
     */
    private Date startTime;
    
    /**
     * 操作结束时间
     */
    private Date endTime;
    
    /**
     * 操作耗时 (毫秒)
     */
    private Long durationMs;
    
    /**
     * 操作结果 (JSON格式存储详细结果)
     */
    private String operationResult;
    
    /**
     * 错误信息 (操作失败时记录)
     */
    private String errorMessage;
    
    /**
     * 错误详情 (堆栈信息等)
     */
    private String errorDetails;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 操作IP地址
     */
    private String operatorIp;
    
    /**
     * 操作会话ID (用于关联同一批操作)
     */
    private String sessionId;
    
    /**
     * 父操作ID (用于关联子操作)
     */
    private Integer parentOperationId;
    
    /**
     * 操作优先级 (1-10, 1为最高)
     */
    private Integer priority;
    
    /**
     * 操作重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
    
    /**
     * 操作超时时间 (毫秒)
     */
    private Long timeoutMs;
    
    /**
     * 是否异步操作 (0:同步, 1:异步)
     */
    private Integer asyncOperation;
    
    /**
     * 异步任务ID (用于查询异步操作结果)
     */
    private String asyncTaskId;
    
    /**
     * 操作备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String clusterName;
    
    @TableField(exist = false)
    private String serviceLabel;
    
    @TableField(exist = false)
    private OperationType operationTypeCode;
    
    @TableField(exist = false)
    private OperationStatus operationStatusCode;
    
    @TableField(exist = false)
    private String durationFormatted;
}
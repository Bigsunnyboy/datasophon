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

import com.datasophon.dao.enums.DiscoveryStatus;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 组件发现结果表
 * 记录组件发现任务的执行结果和详情
 */
@TableName("t_ddh_component_discovery_result")
@Data
public class ComponentDiscoveryResultEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId
    private Integer id;
    
    /**
     * 发现任务ID
     */
    private String discoveryTaskId;
    
    /**
     * 集群ID
     */
    private Integer clusterId;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 发现目标 (IP范围、主机名列表、或自动发现)
     */
    private String discoveryTarget;
    
    /**
     * 发现方法 (PORT_SCAN, PROCESS_DETECTION, CONFIG_PARSING, API_QUERY, CUSTOM_SCRIPT)
     */
    private String discoveryMethod;
    
    /**
     * 发现状态
     */
    private DiscoveryStatus discoveryStatus;
    
    /**
     * 发现开始时间
     */
    private Date discoveryStartTime;
    
    /**
     * 发现结束时间
     */
    private Date discoveryEndTime;
    
    /**
     * 发现耗时(毫秒)
     */
    private Long durationMs;
    
    /**
     * 发现结果统计 (JSON格式: {"totalHosts": 10, "foundComponents": 5, "failedHosts": 2})
     */
    private String discoveryStats;
    
    /**
     * 发现详情 (JSON格式存储详细发现结果)
     */
    private String discoveryDetails;
    
    /**
     * 错误信息 (发现失败时记录)
     */
    private String errorMessage;
    
    /**
     * 触发方式 (MANUAL:手动触发, SCHEDULED:定时任务, API_CALL:API调用)
     */
    private String triggerType;
    
    /**
     * 触发人 (手动触发时记录)
     */
    private String triggeredBy;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 是否自动注册 (0:不自动注册, 1:自动注册已验证的组件)
     */
    private Integer autoRegisterFlag;
    
    /**
     * 注册组件数量 (自动注册的组件数量)
     */
    private Integer registeredCount;
    
    /**
     * 备注信息
     */
    private String remark;
    
    @TableField(exist = false)
    private Integer discoveryStatusCode;
    
    @TableField(exist = false)
    private String clusterName;
    
    @TableField(exist = false)
    private String serviceLabel;
}
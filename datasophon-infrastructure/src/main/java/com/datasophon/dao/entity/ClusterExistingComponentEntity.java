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

import com.datasophon.dao.enums.ExistingComponentState;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 集群现有组件注册表
 * 存储通过发现引擎找到的现有大数据组件实例信息
 */
@TableName("t_ddh_cluster_existing_component")
@Data
public class ClusterExistingComponentEntity implements Serializable {
    
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
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 组件状态
     */
    private ExistingComponentState componentState;
    
    /**
     * 检测到的版本
     */
    private String detectedVersion;
    
    /**
     * 安装路径
     */
    private String installPath;
    
    /**
     * 配置文件路径 (JSON格式存储多个路径)
     */
    private String configPaths;
    
    /**
     * 监听的端口 (JSON格式存储多个端口)
     */
    private String listenPorts;
    
    /**
     * 进程ID
     */
    private String processId;
    
    /**
     * 进程启动时间
     */
    private Date processStartTime;
    
    /**
     * 健康状态 (JSON格式存储健康检查结果)
     */
    private String healthStatus;
    
    /**
     * 验证结果 (JSON格式存储验证详情)
     */
    private String validationResult;
    
    /**
     * 接管级别 (MONITOR_ONLY, CONFIGURATION, CONTROL, FULL)
     */
    private String takeoverLevel;
    
    /**
     * 配置合并策略 (PRESERVE_EXISTING, OVERRIDE_WITH_PLATFORM, SMART_MERGE)
     */
    private String configMergeStrategy;
    
    /**
     * 发现时间
     */
    private Date discoveryTime;
    
    /**
     * 注册时间
     */
    private Date registerTime;
    
    /**
     * 最后检查时间
     */
    private Date lastCheckTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 扩展属性 (JSON格式)
     */
    private String extendProps;
    
    /**
     * 服务实例ID (关联到ClusterServiceInstanceEntity)
     */
    private Integer serviceInstanceId;
    
    /**
     * 服务角色实例ID (关联到ClusterServiceRoleInstanceEntity)
     */
    private Integer serviceRoleInstanceId;
    
    @TableField(exist = false)
    private Integer componentStateCode;
    
    @TableField(exist = false)
    private String clusterName;
    
    @TableField(exist = false)
    private String serviceLabel;
}
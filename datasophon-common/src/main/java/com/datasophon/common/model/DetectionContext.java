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

import com.datasophon.common.enums.DetectionMethod;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DetectionContext {
    
    /**
     * 集群ID
     */
    private Integer clusterId;
    
    /**
     * 服务名称，如 HDFS, YARN
     */
    private String serviceName;
    
    /**
     * 检测方法
     */
    private DetectionMethod detectionMethod;
    
    /**
     * 目标主机列表
     */
    private List<String> targetHosts;
    
    /**
     * SSH用户名
     */
    private String sshUser;
    
    /**
     * SSH端口
     */
    private Integer sshPort;
    
    /**
     * 现有组件配置（从service_ddl.json读取）
     */
    private ExistingComponentConfig existingComponentConfig;
    
    /**
     * 自定义参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 发现任务ID
     */
    private String discoveryTaskId;
    
    /**
     * 是否自动注册发现的组件
     */
    private Boolean autoRegister;
    
    /**
     * 超时时间（毫秒）
     */
    private Long timeoutMs;
}
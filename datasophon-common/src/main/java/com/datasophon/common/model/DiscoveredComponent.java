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

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class DiscoveredComponent {
    
    /**
     * 组件ID（自动生成）
     */
    private String componentId;
    
    /**
     * 主机名
     */
    private String host;
    
    /**
     * 端口
     */
    private Integer port;
    
    /**
     * 服务角色名称，如 NameNode, ResourceManager
     */
    private String serviceRole;
    
    /**
     * 服务名称，如 HDFS, YARN
     */
    private String serviceName;
    
    /**
     * 组件版本
     */
    private String version;
    
    /**
     * 健康状态
     */
    private String healthStatus;
    
    /**
     * 检测方法
     */
    private String detectionMethod;
    
    /**
     * 检测到的配置信息
     */
    private Map<String, String> configs = new HashMap<>();
    
    /**
     * 额外元数据
     */
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * 置信度（0-100）
     */
    private Integer confidence;
    
    /**
     * 是否已验证
     */
    private Boolean validated = false;
    
    /**
     * 验证消息
     */
    private String validationMessage;
    
    /**
     * API端点（API_QUERY检测方法使用）
     */
    private String apiEndpoint;
    
    /**
     * API响应（API_QUERY检测方法使用）
     */
    private String apiResponse;
    
    /**
     * 脚本输出（CUSTOM_SCRIPT检测方法使用）
     */
    private String scriptOutput;
    
    /**
     * 配置文件路径（CONFIG_PARSING检测方法使用）
     */
    private String configFilePath;
    
    /**
     * 配置类型（CONFIG_PARSING检测方法使用）
     */
    private String configType;
    
    /**
     * 进程匹配模式（PROCESS_DETECTION检测方法使用）
     */
    private String processPattern;
}
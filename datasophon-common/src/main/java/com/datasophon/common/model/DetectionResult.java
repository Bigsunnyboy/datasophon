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

import com.datasophon.common.enums.DetectionStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DetectionResult {
    
    /**
     * 检测状态
     */
    private DetectionStatus status;
    
    /**
     * 发现的组件列表
     */
    private List<DiscoveredComponent> discoveredComponents = new ArrayList<>();
    
    /**
     * 检测统计信息
     */
    private Map<String, Object> stats = new HashMap<>();
    
    /**
     * 错误消息（如果有）
     */
    private String errorMessage;
    
    /**
     * 检测开始时间
     */
    private Date startTime;
    
    /**
     * 检测结束时间
     */
    private Date endTime;
    
    /**
     * 检测耗时（毫秒）
     */
    private Long durationMs;
    
    /**
     * 原始检测输出（用于调试）
     */
    private String rawOutput;
    
    /**
     * 检测到的配置信息
     */
    private Map<String, String> detectedConfigs = new HashMap<>();
    
    /**
     * 发现的组件数量
     */
    public int getFoundComponentsCount() {
        return discoveredComponents.size();
    }
}
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

package com.datasophon.api.strategy;

import com.datasophon.common.model.DetectionContext;
import com.datasophon.common.model.DetectionResult;

public interface ComponentDetectionStrategy {
    
    /**
     * 执行组件检测
     *
     * @param context 检测上下文
     * @return 检测结果
     */
    DetectionResult detect(DetectionContext context);
    
    /**
     * 是否支持指定的检测方法
     *
     * @param detectionMethod 检测方法
     * @return 是否支持
     */
    boolean supports(String detectionMethod);
    
    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getStrategyName();
    
    /**
     * 获取支持的检测方法列表
     *
     * @return 检测方法数组
     */
    String[] getSupportedDetectionMethods();
    
    /**
     * 获取支持的服务列表
     *
     * @return 服务名称数组
     */
    String[] getSupportedServices();
}
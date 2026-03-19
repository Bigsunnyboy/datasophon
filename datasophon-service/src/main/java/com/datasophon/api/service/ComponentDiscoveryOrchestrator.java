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

package com.datasophon.api.service;

import com.datasophon.common.model.DetectionContext;
import com.datasophon.common.model.DetectionResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ComponentDiscoveryOrchestrator {
    
    /**
     * Execute component discovery using specified detection methods
     *
     * @param context Detection context
     * @return Future of detection results
     */
    CompletableFuture<DetectionResult> discoverComponents(DetectionContext context);
    
    /**
     * Execute component discovery synchronously
     *
     * @param context Detection context
     * @return Detection result
     */
    DetectionResult discoverComponentsSync(DetectionContext context);
    
    /**
     * Execute multiple detection strategies in parallel and merge results
     *
     * @param context Detection context
     * @param detectionMethods List of detection methods to use (empty means all applicable)
     * @return Merged detection result
     */
    DetectionResult discoverComponentsWithMethods(DetectionContext context, List<String> detectionMethods);
    
    /**
     * Get supported detection methods for a service
     *
     * @param serviceName Service name
     * @return List of supported detection methods
     */
    List<String> getSupportedDetectionMethods(String serviceName);
    
    /**
     * Validate detection context before execution
     *
     * @param context Detection context
     * @return Validation error message, null if valid
     */
    String validateDetectionContext(DetectionContext context);
    
    /**
     * Get default detection methods for a service based on its configuration
     *
     * @param serviceName Service name
     * @return List of default detection methods
     */
    List<String> getDefaultDetectionMethods(String serviceName);
}
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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentDetectionStrategyContext {
    
    private static final Map<String, ComponentDetectionStrategy> strategyMap = new ConcurrentHashMap<>();
    private static final Map<String, List<ComponentDetectionStrategy>> detectionMethodMap = new ConcurrentHashMap<>();
    private static final Map<String, List<ComponentDetectionStrategy>> serviceMap = new ConcurrentHashMap<>();
    
    static {
        registerStrategy(new PortScanDetectionStrategy());
        registerStrategy(new ProcessDetectionStrategy());
        registerStrategy(new ConfigParsingDetectionStrategy());
        registerStrategy(new ApiQueryDetectionStrategy());
        registerStrategy(new CustomScriptDetectionStrategy());
    }
    
    private static void registerStrategy(ComponentDetectionStrategy strategy) {
        if (strategy == null) {
            return;
        }
        
        strategyMap.put(strategy.getStrategyName(), strategy);
        
        for (String detectionMethod : strategy.getSupportedDetectionMethods()) {
            detectionMethodMap.computeIfAbsent(detectionMethod, k -> new ArrayList<>())
                    .add(strategy);
        }
        
        for (String serviceName : strategy.getSupportedServices()) {
            serviceMap.computeIfAbsent(serviceName, k -> new ArrayList<>())
                    .add(strategy);
        }
        
        for (String serviceName : strategy.getSupportedServices()) {
            String upperService = serviceName.toUpperCase();
            if (!upperService.equals(serviceName)) {
                serviceMap.computeIfAbsent(upperService, k -> new ArrayList<>())
                        .add(strategy);
            }
        }
    }
    
    /**
     * Get strategy by strategy name
     */
    public static ComponentDetectionStrategy getStrategy(String strategyName) {
        if (StringUtils.isBlank(strategyName)) {
            return null;
        }
        return strategyMap.get(strategyName);
    }
    
    /**
     * Get all strategies supporting a specific detection method
     */
    public static List<ComponentDetectionStrategy> getStrategiesByDetectionMethod(String detectionMethod) {
        if (StringUtils.isBlank(detectionMethod)) {
            return new ArrayList<>();
        }
        List<ComponentDetectionStrategy> strategies = detectionMethodMap.get(detectionMethod);
        return strategies != null ? new ArrayList<>(strategies) : new ArrayList<>();
    }
    
    /**
     * Get all strategies supporting a specific service
     */
    public static List<ComponentDetectionStrategy> getStrategiesByService(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return new ArrayList<>();
        }
        List<ComponentDetectionStrategy> strategies = serviceMap.get(serviceName.toUpperCase());
        return strategies != null ? new ArrayList<>(strategies) : new ArrayList<>();
    }
    
    /**
     * Get all registered strategies
     */
    public static List<ComponentDetectionStrategy> getAllStrategies() {
        return new ArrayList<>(strategyMap.values());
    }
    
    /**
     * Check if a detection method is supported by any strategy
     */
    public static boolean isDetectionMethodSupported(String detectionMethod) {
        return !getStrategiesByDetectionMethod(detectionMethod).isEmpty();
    }
    
    /**
     * Check if a service has any supported detection strategies
     */
    public static boolean isServiceSupported(String serviceName) {
        return !getStrategiesByService(serviceName).isEmpty();
    }
    
    /**
     * Get all supported detection methods across all strategies
     */
    public static List<String> getAllSupportedDetectionMethods() {
        List<String> methods = new ArrayList<>();
        for (ComponentDetectionStrategy strategy : strategyMap.values()) {
            for (String method : strategy.getSupportedDetectionMethods()) {
                if (!methods.contains(method)) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }
    
    /**
     * Get all supported services across all strategies
     */
    public static List<String> getAllSupportedServices() {
        List<String> services = new ArrayList<>();
        for (ComponentDetectionStrategy strategy : strategyMap.values()) {
            for (String service : strategy.getSupportedServices()) {
                if (!services.contains(service)) {
                    services.add(service);
                }
            }
        }
        return services;
    }
    
    /**
     * Get strategies that support both the detection method and service
     */
    public static List<ComponentDetectionStrategy> getStrategies(String detectionMethod, String serviceName) {
        List<ComponentDetectionStrategy> result = new ArrayList<>();
        List<ComponentDetectionStrategy> byMethod = getStrategiesByDetectionMethod(detectionMethod);
        List<ComponentDetectionStrategy> byService = getStrategiesByService(serviceName);
        
        for (ComponentDetectionStrategy strategy : byMethod) {
            if (byService.contains(strategy)) {
                result.add(strategy);
            }
        }
        return result;
    }
}
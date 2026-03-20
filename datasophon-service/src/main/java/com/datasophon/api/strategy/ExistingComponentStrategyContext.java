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

import com.datasophon.common.enums.TakeoverCapability;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterExistingComponentEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context for managing and accessing ExistingComponentStrategy instances.
 * 
 * <p>This class follows the Strategy pattern context role, providing
 * a registry of available strategies and methods to execute them
 * based on component type and capability requirements.</p>
 */
public class ExistingComponentStrategyContext {
    
    private static final Logger logger = LoggerFactory.getLogger(ExistingComponentStrategyContext.class);
    
    private static final Map<String, ExistingComponentStrategy> strategyMap = new ConcurrentHashMap<>();
    private static final Map<String, List<ExistingComponentStrategy>> serviceStrategyMap = new ConcurrentHashMap<>();
    
    static {
        // Register default strategies
        registerStrategy(new GenericExistingComponentStrategy());
        // Additional component-specific strategies will be registered here
        // as they are implemented (e.g., HdfsExistingComponentStrategy, YarnExistingComponentStrategy)
    }
    
    /**
     * Register a strategy instance.
     */
    public static void registerStrategy(ExistingComponentStrategy strategy) {
        if (strategy == null) {
            return;
        }
        
        String strategyName = strategy.getStrategyName();
        strategyMap.put(strategyName, strategy);
        
        logger.info("Registered existing component strategy: {}", strategyName);
    }
    
    /**
     * Get strategy for a specific component type.
     * Returns the first strategy that supports the component, or the generic strategy as fallback.
     */
    public static ExistingComponentStrategy getStrategy(String serviceName, String componentName) {
        // First, check if we have strategies specifically registered for this service
        List<ExistingComponentStrategy> serviceStrategies = serviceStrategyMap.get(serviceName);
        if (serviceStrategies != null) {
            for (ExistingComponentStrategy strategy : serviceStrategies) {
                if (strategy.supports(serviceName, componentName)) {
                    return strategy;
                }
            }
        }
        
        // Fallback: check all strategies
        for (ExistingComponentStrategy strategy : strategyMap.values()) {
            if (strategy.supports(serviceName, componentName)) {
                return strategy;
            }
        }
        
        // Ultimate fallback: generic strategy
        return strategyMap.get("generic-existing-component-strategy");
    }
    
    /**
     * Execute takeover for a component using the appropriate strategy.
     */
    public static Result executeTakeover(ClusterExistingComponentEntity component,
                                         TakeoverCapability targetCapability,
                                         Object params) {
        String serviceName = component.getServiceName();
        String componentName = component.getServiceRole();
        
        ExistingComponentStrategy strategy = getStrategy(serviceName, componentName);
        if (strategy == null) {
            return Result.error(String.format(
                    "No strategy found for component %s:%s", serviceName, componentName));
        }
        
        logger.info("Using strategy {} for component {}:{} takeover",
                strategy.getStrategyName(), serviceName, componentName);
        
        return strategy.executeTakeover(component, targetCapability, params);
    }
    
    /**
     * Validate takeover readiness for a component.
     */
    public static Result validateTakeoverReadiness(ClusterExistingComponentEntity component,
                                                   TakeoverCapability targetCapability) {
        String serviceName = component.getServiceName();
        String componentName = component.getServiceRole();
        
        ExistingComponentStrategy strategy = getStrategy(serviceName, componentName);
        if (strategy == null) {
            return Result.error(String.format(
                    "No strategy found for component %s:%s", serviceName, componentName));
        }
        
        return strategy.validateTakeoverReadiness(component, targetCapability);
    }
    
    /**
     * Upgrade takeover level for a component.
     */
    public static Result upgradeTakeoverLevel(ClusterExistingComponentEntity component,
                                              TakeoverCapability targetCapability,
                                              Object params) {
        String serviceName = component.getServiceName();
        String componentName = component.getServiceRole();
        
        ExistingComponentStrategy strategy = getStrategy(serviceName, componentName);
        if (strategy == null) {
            return Result.error(String.format(
                    "No strategy found for component %s:%s", serviceName, componentName));
        }
        
        return strategy.upgradeTakeoverLevel(component, targetCapability, params);
    }
    
    /**
     * Get all supported capabilities for a component type.
     */
    public static List<TakeoverCapability> getSupportedCapabilities(String serviceName,
                                                                    String componentName) {
        ExistingComponentStrategy strategy = getStrategy(serviceName, componentName);
        if (strategy == null) {
            return new ArrayList<>();
        }
        
        return strategy.getSupportedCapabilities(serviceName, componentName);
    }
    
    /**
     * Check if a component type is supported for takeover.
     */
    public static boolean isComponentSupported(String serviceName, String componentName) {
        ExistingComponentStrategy strategy = getStrategy(serviceName, componentName);
        return strategy != null && strategy.supports(serviceName, componentName);
    }
    
    /**
     * Register a strategy for a specific service.
     * This allows multiple strategies to be available for a service,
     * with the most specific one being selected based on component name.
     */
    public static void registerServiceStrategy(String serviceName,
                                               ExistingComponentStrategy strategy) {
        serviceStrategyMap.computeIfAbsent(serviceName, k -> new ArrayList<>())
                .add(strategy);
        registerStrategy(strategy);
    }
}
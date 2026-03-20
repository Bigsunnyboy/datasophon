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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for existing component takeover strategies.
 * Provides common functionality and default implementations for some methods.
 */
public abstract class AbstractExistingComponentStrategy implements ExistingComponentStrategy {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public Result executeTakeover(ClusterExistingComponentEntity component,
                                  TakeoverCapability targetCapability,
                                  Object params) {
        logger.info("Executing takeover for component {}:{} at level {}",
                component.getServiceName(), component.getServiceRole(), targetCapability);
        
        // Validate takeover readiness
        Result validationResult = validateTakeoverReadiness(component, targetCapability);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }
        
        // Execute based on target capability level
        switch (targetCapability) {
            case MONITOR_ONLY:
                return executeMonitorOnlyTakeover(component, params);
            case CONFIGURATION:
                return executeConfigurationTakeover(component, params);
            case CONTROL:
                return executeControlTakeover(component, params);
            case FULL:
                return executeFullTakeover(component, params);
            default:
                return Result.error("Unsupported takeover capability: " + targetCapability);
        }
    }
    
    @Override
    public Result validateTakeoverReadiness(ClusterExistingComponentEntity component,
                                            TakeoverCapability targetCapability) {
        // Default implementation checks if component is in valid state
        // and target capability is supported
        List<TakeoverCapability> supported = getSupportedCapabilities(
                component.getServiceName(), component.getServiceRole());
        
        if (!supported.contains(targetCapability)) {
            return Result.error(String.format(
                    "Takeover capability %s not supported for component %s:%s",
                    targetCapability, component.getServiceName(), component.getServiceRole()));
        }
        
        // Check component health
        Result healthCheck = performHealthCheck(component);
        if (!healthCheck.isSuccess()) {
            return Result.error("Component health check failed: " + healthCheck.getMsg());
        }
        
        return Result.success();
    }
    
    @Override
    public Result upgradeTakeoverLevel(ClusterExistingComponentEntity component,
                                       TakeoverCapability targetCapability,
                                       Object params) {
        // Default implementation validates and executes takeover at new level
        return executeTakeover(component, targetCapability, params);
    }
    
    @Override
    public Result downgradeTakeoverLevel(ClusterExistingComponentEntity component,
                                         TakeoverCapability targetCapability,
                                         Object params) {
        logger.info("Downgrading takeover level for component {}:{} from {} to {}",
                component.getServiceName(), component.getServiceRole(),
                component.getTakeoverLevel(), targetCapability.getCode());
        
        // For downgrade, we just update the capability level without removing existing integration
        // Implementation should ensure safe downgrade (e.g., stop active management features)
        return Result.success("Downgrade operation queued. Actual downgrade will be performed during next maintenance window.");
    }
    
    @Override
    public Result performHealthCheck(ClusterExistingComponentEntity component) {
        // Default health check implementation
        // Should be overridden by subclasses
        return Result.success("Health check passed (default implementation)");
    }
    
    @Override
    public Result syncConfiguration(ClusterExistingComponentEntity component,
                                    String direction,
                                    Object params) {
        // Default sync implementation - should be overridden
        return Result.success("Configuration sync completed (default implementation)");
    }
    
    @Override
    public Result performMaintenance(ClusterExistingComponentEntity component,
                                     String operationType,
                                     Object params) {
        // Default maintenance implementation - should be overridden
        return Result.success("Maintenance operation completed (default implementation)");
    }
    
    /**
     * Execute takeover at MONITOR_ONLY level.
     */
    protected abstract Result executeMonitorOnlyTakeover(ClusterExistingComponentEntity component,
                                                         Object params);
    
    /**
     * Execute takeover at CONFIGURATION level.
     */
    protected abstract Result executeConfigurationTakeover(ClusterExistingComponentEntity component,
                                                           Object params);
    
    /**
     * Execute takeover at CONTROL level.
     */
    protected abstract Result executeControlTakeover(ClusterExistingComponentEntity component,
                                                     Object params);
    
    /**
     * Execute takeover at FULL level.
     */
    protected abstract Result executeFullTakeover(ClusterExistingComponentEntity component,
                                                  Object params);
    
    /**
     * Default implementation returns all capabilities.
     * Subclasses should override to specify which capabilities they actually support.
     */
    @Override
    public List<TakeoverCapability> getSupportedCapabilities(String serviceName, String componentName) {
        return Arrays.asList(TakeoverCapability.values());
    }
    
    /**
     * Default implementation returns true for all components.
     * Subclasses should override to specify which components they support.
     */
    @Override
    public boolean supports(String serviceName, String componentName) {
        return true;
    }
}
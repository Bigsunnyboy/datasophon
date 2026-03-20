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

import java.util.List;

/**
 * Strategy interface for progressive takeover of existing components.
 * 
 * <p>This strategy defines how DataSophon can progressively take over management
 * of existing big data components, from monitoring-only to full lifecycle management.</p>
 * 
 * <p>Different component types (HDFS, YARN, Spark, etc.) may have different
 * implementations of this strategy to handle their specific requirements.</p>
 */
public interface ExistingComponentStrategy {
    
    /**
     * Execute takeover of an existing component.
     * 
     * @param component The existing component entity
     * @param targetCapability The target takeover capability level
     * @param params Additional parameters for the takeover operation
     * @return Result of the takeover operation
     */
    Result executeTakeover(ClusterExistingComponentEntity component,
                           TakeoverCapability targetCapability,
                           Object params);
    
    /**
     * Validate if a component can be taken over at the specified capability level.
     * 
     * @param component The existing component entity
     * @param targetCapability The target takeover capability level
     * @return Validation result with details
     */
    Result validateTakeoverReadiness(ClusterExistingComponentEntity component,
                                     TakeoverCapability targetCapability);
    
    /**
     * Upgrade the takeover level of an existing component.
     * 
     * @param component The existing component entity
     * @param targetCapability The target takeover capability level (must be higher than current)
     * @param params Upgrade parameters including validation requirements and maintenance window
     * @return Result of the upgrade operation
     */
    Result upgradeTakeoverLevel(ClusterExistingComponentEntity component,
                                TakeoverCapability targetCapability,
                                Object params);
    
    /**
     * Downgrade the takeover level of an existing component.
     * 
     * @param component The existing component entity
     * @param targetCapability The target takeover capability level (must be lower than current)
     * @param params Downgrade parameters
     * @return Result of the downgrade operation
     */
    Result downgradeTakeoverLevel(ClusterExistingComponentEntity component,
                                  TakeoverCapability targetCapability,
                                  Object params);
    
    /**
     * Get the list of takeover capabilities supported by this strategy
     * for the specified component type.
     * 
     * @param serviceName The service name (e.g., HDFS, YARN, SPARK)
     * @param componentName The component/role name (e.g., NameNode, ResourceManager)
     * @return List of supported takeover capabilities
     */
    List<TakeoverCapability> getSupportedCapabilities(String serviceName, String componentName);
    
    /**
     * Check if this strategy supports the specified component type.
     * 
     * @param serviceName The service name
     * @param componentName The component/role name
     * @return true if supported, false otherwise
     */
    boolean supports(String serviceName, String componentName);
    
    /**
     * Get the name of this strategy.
     * 
     * @return Strategy name
     */
    String getStrategyName();
    
    /**
     * Get a description of this strategy.
     * 
     * @return Strategy description
     */
    String getStrategyDescription();
    
    /**
     * Perform health check on a taken-over component.
     * 
     * @param component The existing component entity
     * @return Health check result
     */
    Result performHealthCheck(ClusterExistingComponentEntity component);
    
    /**
     * Synchronize configuration between DataSophon and the existing component.
     * 
     * @param component The existing component entity
     * @param direction Sync direction (TO_DATASOPHON, TO_COMPONENT, BIDIRECTIONAL)
     * @param params Sync parameters
     * @return Sync operation result
     */
    Result syncConfiguration(ClusterExistingComponentEntity component,
                             String direction,
                             Object params);
    
    /**
     * Perform maintenance operation on a taken-over component.
     * 
     * @param component The existing component entity
     * @param operationType Type of operation (RESTART, RELOAD_CONFIG, etc.)
     * @param params Operation parameters
     * @return Operation result
     */
    Result performMaintenance(ClusterExistingComponentEntity component,
                              String operationType,
                              Object params);
}
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

import com.datasophon.common.enums.TakeoverCapability;

import java.util.Date;
import java.util.Map;

import lombok.Data;

/**
 * Result model for resource strategy operations.
 * 
 * <p>This model provides detailed information about the outcome of
 * a resource strategy execution, including status, messages, and
 * any relevant data or metadata.</p>
 */
@Data
public class ResourceStrategyResult {
    
    /**
     * Operation success status
     */
    private boolean success;
    
    /**
     * Operation status message
     */
    private String message;
    
    /**
     * Detailed description or error message
     */
    private String details;
    
    /**
     * Strategy that was executed
     */
    private String strategyName;
    
    /**
     * Component involved in the operation
     */
    private String componentId;
    
    /**
     * Service name
     */
    private String serviceName;
    
    /**
     * Component/role name
     */
    private String componentName;
    
    /**
     * Target takeover capability level
     */
    private TakeoverCapability targetCapability;
    
    /**
     * Previous takeover capability level (for upgrade/downgrade operations)
     */
    private TakeoverCapability previousCapability;
    
    /**
     * Operation start time
     */
    private Date startTime;
    
    /**
     * Operation end time
     */
    private Date endTime;
    
    /**
     * Operation duration in milliseconds
     */
    private Long durationMs;
    
    /**
     * Additional operation data or metadata
     */
    private Map<String, Object> data;
    
    /**
     * Error code if operation failed
     */
    private String errorCode;
    
    /**
     * Stack trace or technical details for errors
     */
    private String errorDetails;
    
    /**
     * Whether the operation can be retried
     */
    private boolean retryable;
    
    /**
     * Recommended action if operation failed
     */
    private String recommendedAction;
    
    /**
     * Create a successful result.
     */
    public static ResourceStrategyResult success(String message) {
        ResourceStrategyResult result = new ResourceStrategyResult();
        result.setSuccess(true);
        result.setMessage(message);
        result.setStartTime(new Date());
        result.setEndTime(new Date());
        return result;
    }
    
    /**
     * Create a successful result with data.
     */
    public static ResourceStrategyResult success(String message, Map<String, Object> data) {
        ResourceStrategyResult result = success(message);
        result.setData(data);
        return result;
    }
    
    /**
     * Create an error result.
     */
    public static ResourceStrategyResult error(String message) {
        ResourceStrategyResult result = new ResourceStrategyResult();
        result.setSuccess(false);
        result.setMessage(message);
        result.setStartTime(new Date());
        result.setEndTime(new Date());
        return result;
    }
    
    /**
     * Create an error result with error details.
     */
    public static ResourceStrategyResult error(String message, String errorDetails) {
        ResourceStrategyResult result = error(message);
        result.setErrorDetails(errorDetails);
        return result;
    }
    
    /**
     * Create an error result with error code and details.
     */
    public static ResourceStrategyResult error(String message, String errorCode, String errorDetails) {
        ResourceStrategyResult result = error(message, errorDetails);
        result.setErrorCode(errorCode);
        return result;
    }
}
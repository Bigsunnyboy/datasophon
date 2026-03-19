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

import com.datasophon.common.enums.ConfigMergeStrategy;
import com.datasophon.common.enums.DetectionMethod;
import com.datasophon.common.enums.TakeoverCapability;
import com.datasophon.common.enums.ValidationRule;

import java.util.List;

import lombok.Data;

/**
 * Configuration for existing component support in DataSophon.
 * Defines how the platform can discover, validate, and manage
 * pre-existing big data service components.
 */
@Data
public class ExistingComponentConfig {
    
    /**
     * Installation type: NEW, EXISTING, or MIXED
     * NEW: Platform will deploy fresh installation
     * EXISTING: Platform will discover and manage existing components
     * MIXED: Platform can do both (some nodes new, some existing)
     */
    private String installationType;
    
    /**
     * Configuration for existing component support
     */
    private ExistingComponentSupport existingComponentSupport;
    
    @Data
    public static class ExistingComponentSupport {
        
        /**
         * Methods to detect existing component instances
         */
        private List<DetectionMethod> detectionMethods;
        
        /**
         * Rules to validate discovered components
         */
        private List<ValidationRule> validationRules;
        
        /**
         * Level of control the platform can exert over existing components
         */
        private TakeoverCapability takeoverCapability;
        
        /**
         * Strategy for merging platform config with existing config
         */
        private ConfigMergeStrategy configMergeStrategy;
        
        /**
         * Supported versions of the component
         */
        private List<String> supportedVersions;
        
        /**
         * Port mappings for service discovery
         */
        private List<PortMapping> portMappings;
        
        /**
         * Configuration file paths to inspect
         */
        private List<ConfigFilePath> configFilePaths;
        
        /**
         * Service-specific detection configuration
         */
        private ServiceSpecificDetection serviceSpecificDetection;
    }
    
    @Data
    public static class PortMapping {
        private String serviceRole;
        private Integer port;
        private String protocol; // TCP, HTTP, HTTPS, RPC
        private String description;
        private Boolean required;
    }
    
    @Data
    public static class ConfigFilePath {
        private String path;
        private String description;
        private Boolean required;
        private String configType; // XML, PROPERTIES, YAML, JSON, etc.
    }
    
    @Data
    public static class ServiceSpecificDetection {
        private String detectionScript;
        private List<String> expectedProcessNames;
        private List<String> expectedConfigPatterns;
        private List<HealthCheck> healthChecks;
    }
    
    @Data
    public static class HealthCheck {
        private String type; // PORT_CHECK, HTTP_CHECK, COMMAND_CHECK
        private String target;
        private Integer timeoutSeconds;
        private String expectedResponse;
    }
}
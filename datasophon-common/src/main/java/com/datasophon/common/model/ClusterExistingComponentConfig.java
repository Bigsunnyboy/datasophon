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
 * Configuration for an existing component that needs to be managed by DataSophon.
 * This class provides configuration details needed for discovering, extracting,
 * and managing configuration from existing big data components.
 */
@Data
public class ClusterExistingComponentConfig {
    
    /**
     * Component ID
     */
    private Integer componentId;
    
    /**
     * Service name (e.g., HDFS, YARN, SPARK)
     */
    private String serviceName;
    
    /**
     * Component/role name (e.g., NameNode, ResourceManager)
     */
    private String componentName;
    
    /**
     * Methods to detect this component
     */
    private List<DetectionMethod> detectionMethods;
    
    /**
     * Rules to validate this component
     */
    private List<ValidationRule> validationRules;
    
    /**
     * Level of control the platform can exert over this component
     */
    private TakeoverCapability takeoverCapability;
    
    /**
     * Strategy for merging platform config with existing config
     */
    private ConfigMergeStrategy configMergeStrategy;
    
    /**
     * Configuration file paths to inspect and extract
     */
    private List<ConfigFilePath> configFilePaths;
    
    /**
     * Supported versions of the component
     */
    private List<String> supportedVersions;
    
    /**
     * Custom configuration rules (JSON format)
     */
    private String customConfigRules;
    
    /**
     * Whether to backup original configuration before sync
     */
    private boolean backupBeforeSync = true;
    
    /**
     * Backup directory path
     */
    private String backupDirectory;
    
    /**
     * Whether to validate configuration after extraction
     */
    private boolean validateAfterExtraction = true;
    
    /**
     * Configuration extraction timeout in seconds
     */
    private Integer extractionTimeout = 30;
    
    /**
     * Maximum configuration file size to parse (in MB)
     */
    private Integer maxConfigFileSize = 10;
    
    @Data
    public static class ConfigFilePath {
        private String path;
        private String description;
        private Boolean required;
        private String configType; // XML, PROPERTIES, YAML, JSON, etc.
        private String encoding = "UTF-8";
    }
}
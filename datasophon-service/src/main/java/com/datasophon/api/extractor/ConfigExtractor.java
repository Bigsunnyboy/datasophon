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

package com.datasophon.api.extractor;

import com.datasophon.common.model.ClusterExistingComponentConfig;
import com.datasophon.dao.entity.ClusterExistingComponentEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for extracting configuration from existing big data components.
 * 
 * <p>Different component types (HDFS, YARN, Spark, etc.) should have their own
 * implementations of this interface to handle component-specific configuration
 * extraction logic.</p>
 * 
 * <p>The extracted configuration is returned as a map structure that can be
 * processed by the ConfigurationSyncManager for synchronization operations.</p>
 */
public interface ConfigExtractor {
    
    /**
     * Extract configuration from an existing component.
     * 
     * @param component The existing component entity containing component metadata
     * @param config Configuration details for the extraction process
     * @return Extracted configuration as a map structure
     */
    Map<String, Object> extractConfig(ClusterExistingComponentEntity component,
                                      ClusterExistingComponentConfig config);
    
    /**
     * Validate if the extractor supports the specified component type.
     * 
     * @param serviceName The service name (e.g., HDFS, YARN, SPARK)
     * @param componentName The component/role name (e.g., NameNode, ResourceManager)
     * @return true if this extractor supports the component type, false otherwise
     */
    boolean supports(String serviceName, String componentName);
    
    /**
     * Get the name of this extractor.
     * 
     * @return Extractor name
     */
    String getExtractorName();
    
    /**
     * Get a description of this extractor.
     * 
     * @return Extractor description
     */
    String getExtractorDescription();
    
    /**
     * Get the priority of this extractor (higher priority extractors are tried first).
     * 
     * @return Priority value (default: 0)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Validate the extracted configuration.
     * 
     * @param extractedConfig The configuration extracted by this extractor
     * @return Validation result with details
     */
    default Map<String, Object> validateConfig(Map<String, Object> extractedConfig) {
        // Default implementation returns empty validation result
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("message", "Configuration validated successfully");
        return result;
    }
    
    /**
     * Get supported configuration file types for this extractor.
     * 
     * @return Array of supported file extensions (e.g., [".xml", ".properties", ".yaml"])
     */
    String[] getSupportedFileTypes();
    
    /**
     * Get the version of this extractor.
     * 
     * @return Extractor version string
     */
    String getVersion();
}
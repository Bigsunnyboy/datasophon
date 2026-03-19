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

package com.datasophon.api.load;

import com.alibaba.fastjson.JSONObject;
import com.datasophon.common.model.ServiceInfo;
import com.datasophon.common.model.ExistingComponentConfig;
import com.datasophon.common.enums.DetectionMethod;
import com.datasophon.common.enums.ValidationRule;
import com.datasophon.common.enums.TakeoverCapability;
import com.datasophon.common.enums.ConfigMergeStrategy;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for parsing existing component support configuration
 */
public class ExistingComponentParsingTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ExistingComponentParsingTest.class);
    
    @Test
    public void testParseHdfsServiceDdl() throws Exception {
        // Read the HDFS service_ddl.json file
        String filePath = "datasophon-api/src/main/resources/meta/DDP-1.2.2/HDFS/service_ddl.json";
        File file = new File(filePath);
        assertTrue(file.exists(), "HDFS service_ddl.json should exist");
        
        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        logger.info("Parsing HDFS service definition...");
        
        // Parse using fastjson (same as LoadServiceMeta)
        ServiceInfo serviceInfo = JSONObject.parseObject(jsonContent, ServiceInfo.class);
        
        assertNotNull(serviceInfo);
        assertEquals("HDFS", serviceInfo.getName());
        assertEquals("3.3.6", serviceInfo.getVersion());
        
        // Test new fields
        assertNotNull(serviceInfo.getInstallationType(), "installationType should not be null");
        assertEquals("MIXED", serviceInfo.getInstallationType());
        
        ExistingComponentConfig.ExistingComponentSupport support = serviceInfo.getExistingComponentSupport();
        assertNotNull(support, "existingComponentSupport should not be null");
        
        // Verify detection methods
        assertNotNull(support.getDetectionMethods());
        assertEquals(3, support.getDetectionMethods().size());
        assertTrue(support.getDetectionMethods().contains(DetectionMethod.PORT_SCAN));
        assertTrue(support.getDetectionMethods().contains(DetectionMethod.PROCESS_DETECTION));
        assertTrue(support.getDetectionMethods().contains(DetectionMethod.CONFIG_PARSING));
        
        // Verify validation rules
        assertNotNull(support.getValidationRules());
        assertEquals(3, support.getValidationRules().size());
        assertTrue(support.getValidationRules().contains(ValidationRule.CONNECTIVITY));
        assertTrue(support.getValidationRules().contains(ValidationRule.FUNCTIONAL));
        assertTrue(support.getValidationRules().contains(ValidationRule.CONFIGURATION_VALIDITY));
        
        // Verify takeover capability
        assertEquals(TakeoverCapability.FULL, support.getTakeoverCapability());
        
        // Verify config merge strategy
        assertEquals(ConfigMergeStrategy.SMART_MERGE, support.getConfigMergeStrategy());
        
        // Verify supported versions
        assertNotNull(support.getSupportedVersions());
        assertTrue(support.getSupportedVersions().contains("3.3.6"));
        assertTrue(support.getSupportedVersions().contains("3.2.4"));
        
        // Verify port mappings
        assertNotNull(support.getPortMappings());
        assertEquals(6, support.getPortMappings().size());
        
        // Verify config file paths
        assertNotNull(support.getConfigFilePaths());
        assertEquals(3, support.getConfigFilePaths().size());
        
        // Verify service-specific detection
        assertNotNull(support.getServiceSpecificDetection());
        assertNotNull(support.getServiceSpecificDetection().getExpectedProcessNames());
        assertTrue(support.getServiceSpecificDetection().getExpectedProcessNames().contains("NameNode"));
        
        logger.info("Successfully parsed HDFS service definition with existing component support");
    }
    
    @Test
    public void testParseServiceDdlWithoutNewFields() throws Exception {
        // Create a minimal service definition without new fields
        String minimalJson = "{" +
                "\"name\": \"TEST\"," +
                "\"label\": \"Test Service\"," +
                "\"version\": \"1.0\"," +
                "\"description\": \"Test\"," +
                "\"sortNum\": 1," +
                "\"dependencies\": []," +
                "\"packageName\": \"test.tar.gz\"," +
                "\"decompressPackageName\": \"test\"," +
                "\"roles\": []" +
                "}";
        
        ServiceInfo serviceInfo = JSONObject.parseObject(minimalJson, ServiceInfo.class);
        
        assertNotNull(serviceInfo);
        assertEquals("TEST", serviceInfo.getName());
        
        // New fields should be null (optional)
        assertNull(serviceInfo.getInstallationType(), "installationType should be null when not specified");
        assertNull(serviceInfo.getExistingComponentSupport(), "existingComponentSupport should be null when not specified");
        
        logger.info("Successfully parsed service definition without new fields (backward compatibility)");
    }
}
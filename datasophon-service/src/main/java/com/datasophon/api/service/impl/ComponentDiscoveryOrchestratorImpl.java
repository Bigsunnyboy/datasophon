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

package com.datasophon.api.service.impl;

import com.datasophon.api.service.ComponentDiscoveryOrchestrator;
import com.datasophon.api.strategy.ComponentDetectionStrategy;
import com.datasophon.api.strategy.ComponentDetectionStrategyContext;
import com.datasophon.common.enums.DetectionStatus;
import com.datasophon.common.model.DetectionContext;
import com.datasophon.common.model.DetectionResult;
import com.datasophon.common.model.DiscoveredComponent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ComponentDiscoveryOrchestratorImpl implements ComponentDiscoveryOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(ComponentDiscoveryOrchestratorImpl.class);
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Override
    public CompletableFuture<DetectionResult> discoverComponents(DetectionContext context) {
        return CompletableFuture.supplyAsync(() -> discoverComponentsSync(context), executorService);
    }
    
    @Override
    public DetectionResult discoverComponentsSync(DetectionContext context) {
        return discoverComponentsWithMethods(context, null);
    }
    
    @Override
    public DetectionResult discoverComponentsWithMethods(DetectionContext context, List<String> detectionMethods) {
        DetectionResult overallResult = new DetectionResult();
        overallResult.setStartTime(new Date());
        overallResult.setStatus(DetectionStatus.COMPLETED);
        
        List<DiscoveredComponent> allDiscoveredComponents = new ArrayList<>();
        Map<String, Object> overallStats = new ConcurrentHashMap<>();
        List<DetectionResult> strategyResults = new ArrayList<>();
        
        // Determine which detection methods to use
        List<String> methodsToUse = detectionMethods != null && !detectionMethods.isEmpty()
                ? detectionMethods
                : getDefaultDetectionMethods(context.getServiceName());
        
        if (methodsToUse.isEmpty()) {
            logger.warn("No detection methods available for service: {}", context.getServiceName());
            overallResult.setStatus(DetectionStatus.COMPLETED);
            overallResult.setDiscoveredComponents(allDiscoveredComponents);
            overallResult.setStats(overallStats);
            overallResult.setEndTime(new Date());
            return overallResult;
        }
        
        // Get strategies for each detection method
        List<ComponentDetectionStrategy> strategies = new ArrayList<>();
        for (String method : methodsToUse) {
            List<ComponentDetectionStrategy> strategiesForMethod =
                    ComponentDetectionStrategyContext.getStrategies(method, context.getServiceName());
            if (strategiesForMethod.isEmpty()) {
                logger.warn("No strategy found for detection method {} and service {}", method, context.getServiceName());
            } else {
                strategies.addAll(strategiesForMethod);
            }
        }
        
        if (strategies.isEmpty()) {
            logger.warn("No strategies available for service {} with methods {}", context.getServiceName(), methodsToUse);
            overallResult.setStatus(DetectionStatus.COMPLETED);
            overallResult.setDiscoveredComponents(allDiscoveredComponents);
            overallResult.setStats(overallStats);
            overallResult.setEndTime(new Date());
            return overallResult;
        }
        
        // Execute strategies in parallel
        List<CompletableFuture<DetectionResult>> futures = new ArrayList<>();
        for (ComponentDetectionStrategy strategy : strategies) {
            CompletableFuture<DetectionResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    logger.info("Executing detection strategy: {} for service {}",
                            strategy.getStrategyName(), context.getServiceName());
                    return strategy.detect(context);
                } catch (Exception e) {
                    logger.error("Detection strategy {} failed: {}", strategy.getStrategyName(), e.getMessage(), e);
                    DetectionResult errorResult = new DetectionResult();
                    errorResult.setStatus(DetectionStatus.FAILED);
                    errorResult.setErrorMessage(e.getMessage());
                    errorResult.setStartTime(new Date());
                    errorResult.setEndTime(new Date());
                    return errorResult;
                }
            }, executorService);
            futures.add(future);
        }
        
        // Wait for all futures to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        
        try {
            allFutures.get(30, TimeUnit.MINUTES); // reasonable timeout
        } catch (Exception e) {
            logger.error("Timeout or error waiting for detection strategies", e);
            overallResult.setStatus(DetectionStatus.FAILED);
            overallResult.setErrorMessage("Detection timeout: " + e.getMessage());
            overallResult.setEndTime(new Date());
            return overallResult;
        }
        
        // Collect results
        for (CompletableFuture<DetectionResult> future : futures) {
            try {
                DetectionResult strategyResult = future.get();
                strategyResults.add(strategyResult);
                
                // Merge discovered components
                if (strategyResult.getDiscoveredComponents() != null) {
                    allDiscoveredComponents.addAll(strategyResult.getDiscoveredComponents());
                }
                
                // Merge stats
                if (strategyResult.getStats() != null) {
                    strategyResult.getStats().forEach((key, value) -> {
                        overallStats.merge(key, value, (oldVal, newVal) -> {
                            if (oldVal instanceof Number && newVal instanceof Number) {
                                return ((Number) oldVal).doubleValue() + ((Number) newVal).doubleValue();
                            }
                            return newVal;
                        });
                    });
                }
                
                if (strategyResult.getStatus() == DetectionStatus.FAILED) {
                    overallResult.setStatus(DetectionStatus.PARTIAL);
                    overallResult.setErrorMessage("Some detection strategies failed");
                }
            } catch (Exception e) {
                logger.error("Failed to get strategy result", e);
            }
        }
        
        // Deduplicate components by componentId
        Map<String, DiscoveredComponent> uniqueComponents = new HashMap<>();
        for (DiscoveredComponent component : allDiscoveredComponents) {
            uniqueComponents.putIfAbsent(component.getComponentId(), component);
        }
        
        overallResult.setDiscoveredComponents(new ArrayList<>(uniqueComponents.values()));
        overallResult.setStats(overallStats);
        overallResult.setEndTime(new Date());
        
        // Calculate duration
        if (overallResult.getStartTime() != null && overallResult.getEndTime() != null) {
            overallResult.setDurationMs(overallResult.getEndTime().getTime() - overallResult.getStartTime().getTime());
        }
        
        logger.info("Discovery completed for service {}: found {} components using {} strategies",
                context.getServiceName(), uniqueComponents.size(), strategies.size());
        
        return overallResult;
    }
    
    @Override
    public List<String> getSupportedDetectionMethods(String serviceName) {
        return ComponentDetectionStrategyContext.getAllSupportedDetectionMethods().stream()
                .filter(method -> ComponentDetectionStrategyContext.isServiceSupported(serviceName))
                .collect(Collectors.toList());
    }
    
    @Override
    public String validateDetectionContext(DetectionContext context) {
        if (context == null) {
            return "Detection context cannot be null";
        }
        
        if (context.getServiceName() == null || context.getServiceName().trim().isEmpty()) {
            return "Service name is required";
        }
        
        if (context.getTargetHosts() == null || context.getTargetHosts().isEmpty()) {
            return "Target hosts are required";
        }
        
        if (context.getExistingComponentConfig() == null) {
            return "Existing component configuration is required";
        }
        
        // Validate SSH credentials if required
        if (context.getSshPort() == null || context.getSshPort() <= 0) {
            return "Valid SSH port is required";
        }
        
        if (context.getSshUser() == null || context.getSshUser().trim().isEmpty()) {
            return "SSH user is required";
        }
        
        return null;
    }
    
    @Override
    public List<String> getDefaultDetectionMethods(String serviceName) {
        // Priority order: PORT_SCAN, PROCESS_DETECTION, CONFIG_PARSING, API_QUERY, CUSTOM_SCRIPT
        List<String> allMethods = ComponentDetectionStrategyContext.getAllSupportedDetectionMethods();
        List<String> serviceMethods = new ArrayList<>();
        
        for (String method : allMethods) {
            if (ComponentDetectionStrategyContext.isDetectionMethodSupported(method) &&
                    ComponentDetectionStrategyContext.isServiceSupported(serviceName)) {
                serviceMethods.add(method);
            }
        }
        
        // Return in priority order
        List<String> orderedMethods = new ArrayList<>();
        String[] priorityOrder = {"PORT_SCAN", "PROCESS_DETECTION", "CONFIG_PARSING", "API_QUERY", "CUSTOM_SCRIPT"};
        for (String method : priorityOrder) {
            if (serviceMethods.contains(method)) {
                orderedMethods.add(method);
            }
        }
        
        return orderedMethods;
    }
}
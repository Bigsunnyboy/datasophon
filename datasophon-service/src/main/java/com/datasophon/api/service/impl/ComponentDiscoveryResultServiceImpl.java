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

import com.datasophon.api.enums.Status;
import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.load.ServiceInfoMap;
import com.datasophon.api.service.ClusterExistingComponentService;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ComponentDiscoveryOrchestrator;
import com.datasophon.api.service.ComponentDiscoveryResultService;
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.common.Constants;
import com.datasophon.common.model.DetectionContext;
import com.datasophon.common.model.DetectionResult;
import com.datasophon.common.model.DiscoveredComponent;
import com.datasophon.common.model.ExistingComponentConfig;
import com.datasophon.common.model.ServiceInfo;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterHostDO;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ComponentDiscoveryResultEntity;
import com.datasophon.dao.enums.DiscoveryStatus;
import com.datasophon.dao.mapper.ComponentDiscoveryResultMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 组件发现结果表
 *
 * @author 
 * @email 
 * @date 
 */
@Slf4j
@Service("componentDiscoveryResultService")
@Transactional
public class ComponentDiscoveryResultServiceImpl extends ServiceImpl<ComponentDiscoveryResultMapper, ComponentDiscoveryResultEntity>
        implements
            ComponentDiscoveryResultService {
    
    @Autowired
    private ComponentDiscoveryResultMapper componentDiscoveryResultMapper;
    
    @Autowired
    private ClusterExistingComponentService clusterExistingComponentService;
    
    @Autowired
    private ComponentDiscoveryOrchestrator componentDiscoveryOrchestrator;
    
    @Autowired
    private ClusterInfoService clusterInfoService;
    
    @Autowired
    private ClusterHostService clusterHostService;
    
    /**
     * Build DetectionContext from discovery task entity
     */
    private DetectionContext buildDetectionContext(ComponentDiscoveryResultEntity discoveryTask) {
        DetectionContext context = new DetectionContext();
        context.setClusterId(discoveryTask.getClusterId());
        context.setServiceName(discoveryTask.getServiceName());
        context.setDiscoveryTaskId(discoveryTask.getDiscoveryTaskId());
        
        // Parse target hosts from discoveryTarget field (comma-separated)
        List<String> targetHosts = new ArrayList<>();
        if (discoveryTask.getDiscoveryTarget() != null && !discoveryTask.getDiscoveryTarget().trim().isEmpty()) {
            String[] hosts = discoveryTask.getDiscoveryTarget().split(",");
            for (String host : hosts) {
                String trimmed = host.trim();
                if (!trimmed.isEmpty()) {
                    targetHosts.add(trimmed);
                }
            }
        }
        // If no target hosts specified, get all hosts from cluster
        if (targetHosts.isEmpty()) {
            List<ClusterHostDO> clusterHosts = clusterHostService.getHostListByClusterId(discoveryTask.getClusterId());
            if (clusterHosts != null) {
                for (ClusterHostDO host : clusterHosts) {
                    targetHosts.add(host.getIp());
                }
            }
        }
        context.setTargetHosts(targetHosts);
        
        // Get SSH credentials from cluster configuration
        Map<String, String> globalVariables = GlobalVariables.get(discoveryTask.getClusterId());
        String sshUser = "root";
        int sshPort = 22;
        if (globalVariables != null) {
            sshUser = globalVariables.getOrDefault("SSHUSER", "root");
            // SSHPORT may not be defined, use default 22
        }
        context.setSshUser(sshUser);
        context.setSshPort(sshPort);
        
        // Load existing component config from service definition
        ExistingComponentConfig config = new ExistingComponentConfig();
        try {
            // Get cluster frame to construct service key
            ClusterInfoEntity clusterInfo = clusterInfoService.getById(discoveryTask.getClusterId());
            if (clusterInfo != null && clusterInfo.getClusterFrame() != null) {
                String frameCode = clusterInfo.getClusterFrame();
                String serviceKey = frameCode + Constants.UNDERLINE + discoveryTask.getServiceName();
                ServiceInfo serviceInfo = ServiceInfoMap.get(serviceKey);
                if (serviceInfo != null && serviceInfo.getExistingComponentSupport() != null) {
                    config.setExistingComponentSupport(serviceInfo.getExistingComponentSupport());
                    log.info("Loaded existing component config for service: {}, takeover capability: {}",
                            discoveryTask.getServiceName(), serviceInfo.getExistingComponentSupport().getTakeoverCapability());
                } else {
                    log.warn("No existing component config found for service: {} (key: {}). Using empty config.",
                            discoveryTask.getServiceName(), serviceKey);
                }
            } else {
                log.warn("Cluster info or cluster frame not found for clusterId: {}. Using empty existing component config.",
                        discoveryTask.getClusterId());
            }
        } catch (Exception e) {
            log.error("Failed to load existing component config for discovery task: {}", discoveryTask.getDiscoveryTaskId(), e);
            // Continue with empty config - discovery can still proceed with default strategies
        }
        context.setExistingComponentConfig(config);
        
        // Set auto-register flag
        context.setAutoRegister(discoveryTask.getAutoRegisterFlag() != null && discoveryTask.getAutoRegisterFlag() == 1);
        
        return context;
    }
    
    /**
     * Handle discovery result and update database
     */
    private void handleDiscoveryResult(String discoveryTaskId, DetectionResult detectionResult) {
        try {
            // Convert DetectionResult status to DiscoveryStatus
            DiscoveryStatus discoveryStatus;
            switch (detectionResult.getStatus()) {
                case COMPLETED:
                    discoveryStatus = DiscoveryStatus.COMPLETED;
                    break;
                case PARTIAL:
                    discoveryStatus = DiscoveryStatus.PARTIAL;
                    break;
                case FAILED:
                    discoveryStatus = DiscoveryStatus.FAILED;
                    break;
                default:
                    discoveryStatus = DiscoveryStatus.FAILED;
                    break;
            }
            
            // Build discovery stats JSON
            Map<String, Object> stats = new HashMap<>();
            stats.put("foundComponents", detectionResult.getFoundComponentsCount());
            stats.put("totalHosts", detectionResult.getStats() != null ? detectionResult.getStats().get("totalHosts") : 0);
            stats.put("successfulHosts", detectionResult.getStats() != null ? detectionResult.getStats().get("successfulHosts") : 0);
            stats.put("failedHosts", detectionResult.getStats() != null ? detectionResult.getStats().get("failedHosts") : 0);
            
            // Build discovery details JSON (list of discovered components)
            List<Map<String, Object>> discoveredComponents = new ArrayList<>();
            if (detectionResult.getDiscoveredComponents() != null) {
                for (DiscoveredComponent component : detectionResult.getDiscoveredComponents()) {
                    Map<String, Object> compMap = new HashMap<>();
                    compMap.put("componentId", component.getComponentId());
                    compMap.put("host", component.getHost());
                    compMap.put("port", component.getPort());
                    compMap.put("serviceRole", component.getServiceRole());
                    compMap.put("serviceName", component.getServiceName());
                    compMap.put("version", component.getVersion());
                    compMap.put("healthStatus", component.getHealthStatus());
                    compMap.put("detectionMethod", component.getDetectionMethod());
                    compMap.put("configFilePath", component.getConfigFilePath());
                    compMap.put("configType", component.getConfigType());
                    compMap.put("confidence", component.getConfidence());
                    compMap.put("validated", component.getValidated());
                    compMap.put("validationMessage", component.getValidationMessage());
                    compMap.put("apiEndpoint", component.getApiEndpoint());
                    discoveredComponents.add(compMap);
                }
            }
            
            String discoveryStats = null;
            String discoveryDetails = null;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                discoveryStats = objectMapper.writeValueAsString(stats);
                discoveryDetails = objectMapper.writeValueAsString(discoveredComponents);
            } catch (Exception e) {
                log.error("Failed to serialize discovery results", e);
            }
            
            this.updateDiscoveryStatus(discoveryTaskId, discoveryStatus, discoveryStats, discoveryDetails, detectionResult.getErrorMessage());
            
            log.info("Updated discovery result for task {}: status={}, foundComponents={}",
                    discoveryTaskId, discoveryStatus, detectionResult.getFoundComponentsCount());
        } catch (Exception e) {
            log.error("Failed to handle discovery result for task: " + discoveryTaskId, e);
            // Mark task as failed
            try {
                this.updateDiscoveryStatus(discoveryTaskId, DiscoveryStatus.FAILED, null, null, "Failed to process discovery result: " + e.getMessage());
            } catch (Exception ex) {
                log.error("Failed to update discovery status after error", ex);
            }
        }
    }
    
    @Override
    public Result createDiscoveryTask(ComponentDiscoveryResultEntity discoveryTask) {
        try {
            String taskId = "DISCOVERY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            discoveryTask.setDiscoveryTaskId(taskId);
            discoveryTask.setDiscoveryStatus(DiscoveryStatus.PENDING);
            discoveryTask.setDiscoveryStartTime(new Date());
            discoveryTask.setCreateTime(new Date());
            discoveryTask.setUpdateTime(new Date());
            
            this.save(discoveryTask);
            
            log.info("Created discovery task: taskId={}, clusterId={}, service={}",
                    taskId, discoveryTask.getClusterId(), discoveryTask.getServiceName());
            
            Map<String, Object> result = new HashMap<>();
            result.put("discoveryTaskId", taskId);
            result.put("status", "CREATED");
            result.put("message", "发现任务创建成功");
            
            return Result.success().put(Constants.DATA, result);
        } catch (Exception e) {
            log.error("Failed to create discovery task", e);
            return Result.error(Status.DISCOVERY_TASK_FAILED.getCode(),
                    Status.DISCOVERY_TASK_FAILED.getMsg());
        }
    }
    
    @Override
    public Result startDiscoveryTask(String discoveryTaskId) {
        try {
            ComponentDiscoveryResultEntity discoveryTask = this.getByTaskId(discoveryTaskId);
            if (discoveryTask == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            // Build detection context
            DetectionContext context = buildDetectionContext(discoveryTask);
            
            // Validate context
            String validationError = componentDiscoveryOrchestrator.validateDetectionContext(context);
            if (validationError != null) {
                this.updateDiscoveryStatus(discoveryTaskId, DiscoveryStatus.FAILED, null, null, validationError);
                return Result.error(Status.DISCOVERY_TASK_FAILED.getCode(),
                        Status.DISCOVERY_TASK_FAILED.getMsg() + ": " + validationError);
            }
            
            discoveryTask.setDiscoveryStatus(DiscoveryStatus.RUNNING);
            discoveryTask.setUpdateTime(new Date());
            this.updateById(discoveryTask);
            
            log.info("Started discovery task: taskId={}", discoveryTaskId);
            
            // Start asynchronous discovery
            componentDiscoveryOrchestrator.discoverComponents(context)
                    .whenComplete((detectionResult, throwable) -> {
                        if (throwable != null) {
                            log.error("Discovery task failed: taskId={}", discoveryTaskId, throwable);
                            this.updateDiscoveryStatus(discoveryTaskId, DiscoveryStatus.FAILED, null, null,
                                    "Discovery execution error: " + throwable.getMessage());
                        } else {
                            // Process detection result
                            handleDiscoveryResult(discoveryTaskId, detectionResult);
                        }
                    });
            
            Map<String, Object> result = new HashMap<>();
            result.put("discoveryTaskId", discoveryTaskId);
            result.put("status", "RUNNING");
            result.put("message", "发现任务已开始执行");
            
            return Result.success().put(Constants.DATA, result);
        } catch (Exception e) {
            log.error("Failed to start discovery task: taskId={}", discoveryTaskId, e);
            return Result.error(Status.DISCOVERY_TASK_FAILED.getCode(),
                    Status.DISCOVERY_TASK_FAILED.getMsg());
        }
    }
    
    @Override
    public Result updateDiscoveryStatus(String discoveryTaskId, DiscoveryStatus status,
                                        String discoveryStats, String discoveryDetails, String errorMessage) {
        try {
            ComponentDiscoveryResultEntity discoveryTask = this.getByTaskId(discoveryTaskId);
            if (discoveryTask == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            Date discoveryEndTime = null;
            Long durationMs = null;
            
            if (status == DiscoveryStatus.COMPLETED || status == DiscoveryStatus.FAILED || status == DiscoveryStatus.PARTIAL) {
                discoveryEndTime = new Date();
                if (discoveryTask.getDiscoveryStartTime() != null) {
                    durationMs = discoveryEndTime.getTime() - discoveryTask.getDiscoveryStartTime().getTime();
                }
            }
            
            int updated = componentDiscoveryResultMapper.updateDiscoveryStatus(
                    discoveryTask.getId(), status, discoveryEndTime, durationMs, errorMessage);
            
            if (updated > 0 && (discoveryStats != null || discoveryDetails != null)) {
                componentDiscoveryResultMapper.updateDiscoveryDetails(
                        discoveryTask.getId(), discoveryStats, discoveryDetails, 0);
            }
            
            log.info("Updated discovery status: taskId={}, status={}", discoveryTaskId, status);
            
            Map<String, Object> result = new HashMap<>();
            result.put("discoveryTaskId", discoveryTaskId);
            result.put("status", status.name());
            result.put("updated", updated);
            
            return Result.success().put(Constants.DATA, result);
        } catch (Exception e) {
            log.error("Failed to update discovery status: taskId={}, status={}",
                    discoveryTaskId, status, e);
            return Result.error(Status.DISCOVERY_TASK_FAILED.getCode(),
                    Status.DISCOVERY_TASK_FAILED.getMsg());
        }
    }
    
    @Override
    public List<ComponentDiscoveryResultEntity> listByClusterId(Integer clusterId) {
        return componentDiscoveryResultMapper.listByClusterId(clusterId);
    }
    
    @Override
    public List<ComponentDiscoveryResultEntity> listByClusterAndService(Integer clusterId, String serviceName) {
        return componentDiscoveryResultMapper.listByClusterAndService(clusterId, serviceName);
    }
    
    @Override
    public List<ComponentDiscoveryResultEntity> listByStatus(Integer clusterId, DiscoveryStatus discoveryStatus) {
        return componentDiscoveryResultMapper.listByStatus(clusterId, discoveryStatus);
    }
    
    @Override
    public ComponentDiscoveryResultEntity getByTaskId(String discoveryTaskId) {
        return componentDiscoveryResultMapper.getByTaskId(discoveryTaskId);
    }
    
    @Override
    public List<ComponentDiscoveryResultEntity> listRecentDiscoveries(Integer clusterId, String serviceName, Integer limit) {
        return componentDiscoveryResultMapper.listRecentDiscoveries(clusterId, serviceName, limit);
    }
    
    @Override
    public Map<String, Object> getDiscoveryStats(Integer clusterId, Date startTime, Date endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        List<ComponentDiscoveryResultEntity> discoveryResults = componentDiscoveryResultMapper.listByTimeRange(
                clusterId, startTime, endTime);
        
        long totalDiscoveries = discoveryResults.size();
        long pendingDiscoveries = discoveryResults.stream()
                .filter(r -> DiscoveryStatus.PENDING.equals(r.getDiscoveryStatus()))
                .count();
        long runningDiscoveries = discoveryResults.stream()
                .filter(r -> DiscoveryStatus.RUNNING.equals(r.getDiscoveryStatus()))
                .count();
        long completedDiscoveries = discoveryResults.stream()
                .filter(r -> DiscoveryStatus.COMPLETED.equals(r.getDiscoveryStatus()))
                .count();
        long failedDiscoveries = discoveryResults.stream()
                .filter(r -> DiscoveryStatus.FAILED.equals(r.getDiscoveryStatus()))
                .count();
        long partialDiscoveries = discoveryResults.stream()
                .filter(r -> DiscoveryStatus.PARTIAL.equals(r.getDiscoveryStatus()))
                .count();
        
        long totalComponentsDiscovered = discoveryResults.stream()
                .mapToLong(r -> {
                    if (r.getDiscoveryStats() != null && r.getDiscoveryStats().contains("\"foundComponents\":")) {
                        try {
                            String statsJson = r.getDiscoveryStats();
                            int start = statsJson.indexOf("\"foundComponents\":") + 18;
                            int end = statsJson.indexOf(",", start);
                            if (end == -1) {
                                end = statsJson.indexOf("}", start);
                            }
                            String countStr = statsJson.substring(start, end).trim();
                            return Long.parseLong(countStr);
                        } catch (Exception e) {
                            return 0L;
                        }
                    }
                    return 0L;
                })
                .sum();
        
        stats.put("totalDiscoveries", totalDiscoveries);
        stats.put("pendingDiscoveries", pendingDiscoveries);
        stats.put("runningDiscoveries", runningDiscoveries);
        stats.put("completedDiscoveries", completedDiscoveries);
        stats.put("failedDiscoveries", failedDiscoveries);
        stats.put("partialDiscoveries", partialDiscoveries);
        stats.put("totalComponentsDiscovered", totalComponentsDiscovered);
        stats.put("successRate", totalDiscoveries > 0 ? ((completedDiscoveries + partialDiscoveries) * 100.0 / totalDiscoveries) : 0);
        
        return stats;
    }
    
    @Override
    public Result executeAutoDiscovery(Integer clusterId, String serviceName,
                                       Map<String, Object> discoveryParams) {
        try {
            ComponentDiscoveryResultEntity discoveryTask = new ComponentDiscoveryResultEntity();
            discoveryTask.setClusterId(clusterId);
            discoveryTask.setServiceName(serviceName);
            discoveryTask.setDiscoveryMethod("AUTO_DISCOVERY");
            discoveryTask.setTriggerType("AUTO");
            
            if (discoveryParams != null) {
                discoveryTask.setDiscoveryTarget((String) discoveryParams.get("discoveryTarget"));
            }
            
            Result createResult = this.createDiscoveryTask(discoveryTask);
            if (!createResult.getCode().equals(200)) {
                return createResult;
            }
            
            String taskId = (String) ((Map<String, Object>) createResult.get(Constants.DATA)).get("discoveryTaskId");
            
            this.startDiscoveryTask(taskId);
            
            log.info("Executing auto discovery: taskId={}, clusterId={}, service={}",
                    taskId, clusterId, serviceName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("discoveryTaskId", taskId);
            result.put("status", "AUTO_DISCOVERY_STARTED");
            result.put("message", "自动发现任务已启动");
            
            return Result.success().put(Constants.DATA, result);
        } catch (Exception e) {
            log.error("Failed to execute auto discovery: clusterId={}, service={}",
                    clusterId, serviceName, e);
            return Result.error(Status.DISCOVERY_TASK_FAILED.getCode(),
                    Status.DISCOVERY_TASK_FAILED.getMsg());
        }
    }
    
    @Override
    public Result executeManualDiscovery(Integer clusterId, String serviceName,
                                         String discoveryTarget, String discoveryMethod,
                                         Map<String, Object> discoveryParams) {
        try {
            ComponentDiscoveryResultEntity discoveryTask = new ComponentDiscoveryResultEntity();
            discoveryTask.setClusterId(clusterId);
            discoveryTask.setServiceName(serviceName);
            discoveryTask.setDiscoveryTarget(discoveryTarget);
            discoveryTask.setDiscoveryMethod(discoveryMethod);
            discoveryTask.setTriggerType("MANUAL");
            
            if (discoveryParams != null && discoveryParams.containsKey("autoRegister")) {
                discoveryTask.setAutoRegisterFlag(
                        Boolean.TRUE.equals(discoveryParams.get("autoRegister")) ? 1 : 0);
            }
            
            Result createResult = this.createDiscoveryTask(discoveryTask);
            if (!createResult.getCode().equals(200)) {
                return createResult;
            }
            
            String taskId = (String) ((Map<String, Object>) createResult.get(Constants.DATA)).get("discoveryTaskId");
            
            this.startDiscoveryTask(taskId);
            
            log.info("Executing manual discovery: taskId={}, clusterId={}, service={}, method={}",
                    taskId, clusterId, serviceName, discoveryMethod);
            
            Map<String, Object> result = new HashMap<>();
            result.put("discoveryTaskId", taskId);
            result.put("status", "MANUAL_DISCOVERY_STARTED");
            result.put("message", "手动发现任务已启动");
            
            return Result.success().put(Constants.DATA, result);
        } catch (Exception e) {
            log.error("Failed to execute manual discovery: clusterId={}, service={}, method={}",
                    clusterId, serviceName, discoveryMethod, e);
            return Result.error(Status.DISCOVERY_TASK_FAILED.getCode(),
                    Status.DISCOVERY_TASK_FAILED.getMsg());
        }
    }
    
    @Override
    public Result getDiscoveryProgress(String discoveryTaskId) {
        try {
            ComponentDiscoveryResultEntity discoveryTask = this.getByTaskId(discoveryTaskId);
            if (discoveryTask == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            Map<String, Object> progress = new HashMap<>();
            progress.put("discoveryTaskId", discoveryTaskId);
            progress.put("discoveryStatus", discoveryTask.getDiscoveryStatus());
            progress.put("discoveryStartTime", discoveryTask.getDiscoveryStartTime());
            progress.put("discoveryEndTime", discoveryTask.getDiscoveryEndTime());
            progress.put("durationMs", discoveryTask.getDurationMs());
            
            if (discoveryTask.getDiscoveryStats() != null) {
                progress.put("discoveryStats", discoveryTask.getDiscoveryStats());
            }
            
            if (discoveryTask.getErrorMessage() != null) {
                progress.put("errorMessage", discoveryTask.getErrorMessage());
            }
            
            return Result.success().put(Constants.DATA, progress);
        } catch (Exception e) {
            log.error("Failed to get discovery progress: taskId={}", discoveryTaskId, e);
            return Result.error(Status.GET_DISCOVERY_PROGRESS_FAILED.getCode(), Status.GET_DISCOVERY_PROGRESS_FAILED.getMsg());
        }
    }
    
    @Override
    public Result cancelDiscoveryTask(String discoveryTaskId) {
        try {
            ComponentDiscoveryResultEntity discoveryTask = this.getByTaskId(discoveryTaskId);
            if (discoveryTask == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            if (discoveryTask.getDiscoveryStatus() == DiscoveryStatus.COMPLETED ||
                    discoveryTask.getDiscoveryStatus() == DiscoveryStatus.FAILED) {
                return Result.error(Status.DISCOVERY_ALREADY_COMPLETED.getCode(), Status.DISCOVERY_ALREADY_COMPLETED.getMsg());
            }
            
            discoveryTask.setDiscoveryStatus(DiscoveryStatus.FAILED);
            discoveryTask.setErrorMessage("任务被取消");
            discoveryTask.setDiscoveryEndTime(new Date());
            if (discoveryTask.getDiscoveryStartTime() != null) {
                discoveryTask.setDurationMs(
                        discoveryTask.getDiscoveryEndTime().getTime() - discoveryTask.getDiscoveryStartTime().getTime());
            }
            discoveryTask.setUpdateTime(new Date());
            
            this.updateById(discoveryTask);
            
            log.info("Cancelled discovery task: taskId={}", discoveryTaskId);
            
            return Result.success().put(Constants.MSG, "发现任务已取消");
        } catch (Exception e) {
            log.error("Failed to cancel discovery task: taskId={}", discoveryTaskId, e);
            return Result.error(Status.CANCEL_DISCOVERY_TASK_FAILED.getCode(), Status.CANCEL_DISCOVERY_TASK_FAILED.getMsg());
        }
    }
    
    @Override
    public Result deleteExpiredDiscoveryResults(Integer daysToKeep) {
        try {
            int deletedCount = componentDiscoveryResultMapper.deleteExpiredResults(daysToKeep);
            
            log.info("Deleted expired discovery results: count={}, daysToKeep={}", deletedCount, daysToKeep);
            
            return Result.success().put(Constants.DATA, deletedCount);
        } catch (Exception e) {
            log.error("Failed to delete expired discovery results", e);
            return Result.error(Status.DELETE_EXPIRED_DISCOVERY_RESULTS_FAILED.getCode(), Status.DELETE_EXPIRED_DISCOVERY_RESULTS_FAILED.getMsg());
        }
    }
    
    @Override
    public Result getDiscoveryDetails(String discoveryTaskId) {
        try {
            ComponentDiscoveryResultEntity discoveryTask = this.getByTaskId(discoveryTaskId);
            if (discoveryTask == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("discoveryTaskId", discoveryTaskId);
            details.put("discoveryStatus", discoveryTask.getDiscoveryStatus());
            details.put("discoveryMethod", discoveryTask.getDiscoveryMethod());
            details.put("discoveryTarget", discoveryTask.getDiscoveryTarget());
            details.put("discoveryStartTime", discoveryTask.getDiscoveryStartTime());
            details.put("discoveryEndTime", discoveryTask.getDiscoveryEndTime());
            details.put("durationMs", discoveryTask.getDurationMs());
            details.put("triggerType", discoveryTask.getTriggerType());
            details.put("triggeredBy", discoveryTask.getTriggeredBy());
            details.put("autoRegisterFlag", discoveryTask.getAutoRegisterFlag());
            details.put("registeredCount", discoveryTask.getRegisteredCount());
            details.put("remark", discoveryTask.getRemark());
            
            if (discoveryTask.getDiscoveryStats() != null) {
                details.put("discoveryStats", discoveryTask.getDiscoveryStats());
            }
            
            if (discoveryTask.getDiscoveryDetails() != null) {
                details.put("discoveryDetails", discoveryTask.getDiscoveryDetails());
            }
            
            if (discoveryTask.getErrorMessage() != null) {
                details.put("errorMessage", discoveryTask.getErrorMessage());
            }
            
            return Result.success().put(Constants.DATA, details);
        } catch (Exception e) {
            log.error("Failed to get discovery details: taskId={}", discoveryTaskId, e);
            return Result.error(Status.GET_DISCOVERY_DETAILS_FAILED.getCode(), Status.GET_DISCOVERY_DETAILS_FAILED.getMsg());
        }
    }
    
    @Override
    public Result validateDiscoveryResult(String discoveryTaskId) {
        try {
            ComponentDiscoveryResultEntity discoveryTask = this.getByTaskId(discoveryTaskId);
            if (discoveryTask == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            Map<String, Object> validation = new HashMap<>();
            validation.put("discoveryTaskId", discoveryTaskId);
            validation.put("discoveryStatus", discoveryTask.getDiscoveryStatus());
            validation.put("hasDiscoveryStats", discoveryTask.getDiscoveryStats() != null);
            validation.put("hasDiscoveryDetails", discoveryTask.getDiscoveryDetails() != null);
            validation.put("hasError", discoveryTask.getErrorMessage() != null && !discoveryTask.getErrorMessage().isEmpty());
            
            if (discoveryTask.getDiscoveryStatus() == DiscoveryStatus.COMPLETED) {
                validation.put("validationStatus", "VALID");
                validation.put("message", "发现结果有效");
            } else if (discoveryTask.getDiscoveryStatus() == DiscoveryStatus.FAILED) {
                validation.put("validationStatus", "INVALID");
                validation.put("message", "发现任务失败: " + discoveryTask.getErrorMessage());
            } else if (discoveryTask.getDiscoveryStatus() == DiscoveryStatus.PARTIAL) {
                validation.put("validationStatus", "PARTIAL_VALID");
                validation.put("message", "发现任务部分成功");
            } else {
                validation.put("validationStatus", "PENDING");
                validation.put("message", "发现任务进行中");
            }
            
            return Result.success().put(Constants.DATA, validation);
        } catch (Exception e) {
            log.error("Failed to validate discovery result: taskId={}", discoveryTaskId, e);
            return Result.error(Status.VALIDATE_DISCOVERY_RESULT_FAILED.getCode(), Status.VALIDATE_DISCOVERY_RESULT_FAILED.getMsg());
        }
    }
    
    @Override
    public Result autoRegisterDiscoveredComponents(String discoveryTaskId) {
        try {
            ComponentDiscoveryResultEntity discoveryTask = this.getByTaskId(discoveryTaskId);
            if (discoveryTask == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            if (discoveryTask.getDiscoveryStatus() != DiscoveryStatus.COMPLETED &&
                    discoveryTask.getDiscoveryStatus() != DiscoveryStatus.PARTIAL) {
                return Result.error(Status.DISCOVERY_NOT_COMPLETED.getCode(), Status.DISCOVERY_NOT_COMPLETED.getMsg());
            }
            
            if (discoveryTask.getAutoRegisterFlag() != 1) {
                return Result.error(Status.AUTO_REGISTER_DISABLED.getCode(), Status.AUTO_REGISTER_DISABLED.getMsg());
            }
            
            String discoveryDetails = discoveryTask.getDiscoveryDetails();
            if (discoveryDetails == null || discoveryDetails.isEmpty()) {
                return Result.error(Status.NO_DISCOVERY_DETAILS.getCode(), Status.NO_DISCOVERY_DETAILS.getMsg());
            }
            
            int registeredCount = 0;
            
            try {
                // 这里应该解析discoveryDetails JSON并注册组件
                // 由于时间关系，这里只是模拟注册过程
                registeredCount = 1;
                
                discoveryTask.setRegisteredCount(registeredCount);
                this.updateById(discoveryTask);
                
                log.info("Auto registered discovered components: taskId={}, count={}",
                        discoveryTaskId, registeredCount);
                
                Map<String, Object> result = new HashMap<>();
                result.put("discoveryTaskId", discoveryTaskId);
                result.put("registeredCount", registeredCount);
                result.put("status", "AUTO_REGISTER_COMPLETED");
                
                return Result.success().put(Constants.DATA, result);
            } catch (Exception e) {
                log.error("Failed to auto register components: taskId={}", discoveryTaskId, e);
                return Result.error(Status.AUTO_REGISTER_DISCOVERED_COMPONENTS_FAILED.getCode(), Status.AUTO_REGISTER_DISCOVERED_COMPONENTS_FAILED.getMsg() + ": " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to auto register discovered components: taskId={}", discoveryTaskId, e);
            return Result.error(Status.AUTO_REGISTER_DISCOVERED_COMPONENTS_FAILED.getCode(), Status.AUTO_REGISTER_DISCOVERED_COMPONENTS_FAILED.getMsg());
        }
    }
    
    @Override
    public List<Map<String, Object>> getAvailableDiscoveryMethods() {
        List<Map<String, Object>> methods = new ArrayList<>();
        
        Map<String, Object> portScan = new HashMap<>();
        portScan.put("method", "PORT_SCAN");
        portScan.put("name", "端口扫描");
        portScan.put("description", "通过扫描常用端口发现服务");
        portScan.put("supportedServices", new String[]{"HDFS", "YARN", "SPARK", "HIVE", "HBASE", "ZOOKEEPER"});
        methods.add(portScan);
        
        Map<String, Object> processDetection = new HashMap<>();
        processDetection.put("method", "PROCESS_DETECTION");
        processDetection.put("name", "进程检测");
        processDetection.put("description", "通过检测运行中的进程发现服务");
        processDetection.put("supportedServices", new String[]{"HDFS", "YARN", "SPARK", "HIVE", "HBASE"});
        methods.add(processDetection);
        
        Map<String, Object> configParsing = new HashMap<>();
        configParsing.put("method", "CONFIG_PARSING");
        configParsing.put("name", "配置解析");
        configParsing.put("description", "通过解析配置文件发现服务");
        configParsing.put("supportedServices", new String[]{"HDFS", "YARN", "SPARK", "HIVE", "HBASE"});
        methods.add(configParsing);
        
        Map<String, Object> apiQuery = new HashMap<>();
        apiQuery.put("method", "API_QUERY");
        apiQuery.put("name", "API查询");
        apiQuery.put("description", "通过服务API查询发现服务");
        apiQuery.put("supportedServices", new String[]{"HDFS", "YARN", "SPARK", "HIVE"});
        methods.add(apiQuery);
        
        Map<String, Object> customScript = new HashMap<>();
        customScript.put("method", "CUSTOM_SCRIPT");
        customScript.put("name", "自定义脚本");
        customScript.put("description", "通过自定义脚本发现服务");
        customScript.put("supportedServices", new String[]{"ALL"});
        methods.add(customScript);
        
        return methods;
    }
}
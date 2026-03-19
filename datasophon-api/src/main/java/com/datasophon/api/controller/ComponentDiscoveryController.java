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

package com.datasophon.api.controller;

import com.datasophon.api.security.UserPermission;
import com.datasophon.api.service.ComponentDiscoveryResultService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ComponentDiscoveryResultEntity;
import com.datasophon.dao.enums.DiscoveryStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组件发现控制器
 * 提供组件发现任务的创建、执行、查询和管理功能
 */
@RestController
@RequestMapping("api/component/discovery")
public class ComponentDiscoveryController {
    
    @Autowired
    private ComponentDiscoveryResultService componentDiscoveryResultService;
    
    /**
     * 创建发现任务
     */
    @RequestMapping("/create")
    @UserPermission
    public Result create(@RequestBody ComponentDiscoveryResultEntity discoveryTask) {
        return componentDiscoveryResultService.createDiscoveryTask(discoveryTask);
    }
    
    /**
     * 启动发现任务
     */
    @RequestMapping("/start/{discoveryTaskId}")
    @UserPermission
    public Result start(@PathVariable("discoveryTaskId") String discoveryTaskId) {
        return componentDiscoveryResultService.startDiscoveryTask(discoveryTaskId);
    }
    
    /**
     * 获取发现任务进度
     */
    @RequestMapping("/progress/{discoveryTaskId}")
    public Result progress(@PathVariable("discoveryTaskId") String discoveryTaskId) {
        return componentDiscoveryResultService.getDiscoveryProgress(discoveryTaskId);
    }
    
    /**
     * 获取发现任务详情
     */
    @RequestMapping("/details/{discoveryTaskId}")
    public Result details(@PathVariable("discoveryTaskId") String discoveryTaskId) {
        return componentDiscoveryResultService.getDiscoveryDetails(discoveryTaskId);
    }
    
    /**
     * 取消发现任务
     */
    @RequestMapping("/cancel/{discoveryTaskId}")
    @UserPermission
    public Result cancel(@PathVariable("discoveryTaskId") String discoveryTaskId) {
        return componentDiscoveryResultService.cancelDiscoveryTask(discoveryTaskId);
    }
    
    /**
     * 验证发现结果
     */
    @RequestMapping("/validate/{discoveryTaskId}")
    @UserPermission
    public Result validate(@PathVariable("discoveryTaskId") String discoveryTaskId) {
        return componentDiscoveryResultService.validateDiscoveryResult(discoveryTaskId);
    }
    
    /**
     * 自动注册发现的组件
     */
    @RequestMapping("/auto-register/{discoveryTaskId}")
    @UserPermission
    public Result autoRegister(@PathVariable("discoveryTaskId") String discoveryTaskId) {
        return componentDiscoveryResultService.autoRegisterDiscoveredComponents(discoveryTaskId);
    }
    
    /**
     * 执行自动发现
     */
    @RequestMapping("/execute-auto")
    @UserPermission
    public Result executeAutoDiscovery(
                                       @RequestParam Integer clusterId,
                                       @RequestParam String serviceName,
                                       @RequestBody Map<String, Object> discoveryParams) {
        return componentDiscoveryResultService.executeAutoDiscovery(clusterId, serviceName, discoveryParams);
    }
    
    /**
     * 执行手动发现
     */
    @RequestMapping("/execute-manual")
    @UserPermission
    public Result executeManualDiscovery(
                                         @RequestParam Integer clusterId,
                                         @RequestParam String serviceName,
                                         @RequestParam(required = false) String discoveryTarget,
                                         @RequestParam String discoveryMethod,
                                         @RequestBody Map<String, Object> discoveryParams) {
        return componentDiscoveryResultService.executeManualDiscovery(
                clusterId, serviceName, discoveryTarget, discoveryMethod, discoveryParams);
    }
    
    /**
     * 获取可用的发现方法
     */
    @RequestMapping("/available-methods")
    public Result getAvailableDiscoveryMethods() {
        return Result.success().put(Constants.DATA, componentDiscoveryResultService.getAvailableDiscoveryMethods());
    }
    
    /**
     * 按集群ID列出发现任务
     */
    @RequestMapping("/list-by-cluster/{clusterId}")
    public Result listByCluster(@PathVariable("clusterId") Integer clusterId) {
        return Result.success().put(Constants.DATA, componentDiscoveryResultService.listByClusterId(clusterId));
    }
    
    /**
     * 按集群和服务列出发现任务
     */
    @RequestMapping("/list-by-cluster-service")
    public Result listByClusterAndService(
                                          @RequestParam Integer clusterId,
                                          @RequestParam String serviceName) {
        return Result.success().put(Constants.DATA,
                componentDiscoveryResultService.listByClusterAndService(clusterId, serviceName));
    }
    
    /**
     * 按状态列出发现任务
     */
    @RequestMapping("/list-by-status")
    public Result listByStatus(
                               @RequestParam Integer clusterId,
                               @RequestParam DiscoveryStatus discoveryStatus) {
        return Result.success().put(Constants.DATA,
                componentDiscoveryResultService.listByStatus(clusterId, discoveryStatus));
    }
    
    /**
     * 获取最近的发现任务
     */
    @RequestMapping("/recent")
    public Result listRecentDiscoveries(
                                        @RequestParam Integer clusterId,
                                        @RequestParam(required = false) String serviceName,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success().put(Constants.DATA,
                componentDiscoveryResultService.listRecentDiscoveries(clusterId, serviceName, limit));
    }
    
    /**
     * 获取发现统计信息
     */
    @RequestMapping("/stats")
    public Result getDiscoveryStats(
                                    @RequestParam Integer clusterId,
                                    @RequestParam(required = false) Date startTime,
                                    @RequestParam(required = false) Date endTime) {
        return Result.success().put(Constants.DATA,
                componentDiscoveryResultService.getDiscoveryStats(clusterId, startTime, endTime));
    }
    
    /**
     * 删除过期的发现结果
     */
    @RequestMapping("/delete-expired")
    @UserPermission
    public Result deleteExpiredDiscoveryResults(@RequestParam Integer daysToKeep) {
        return componentDiscoveryResultService.deleteExpiredDiscoveryResults(daysToKeep);
    }
    
    /**
     * 获取发现任务信息
     */
    @RequestMapping("/info/{discoveryTaskId}")
    public Result info(@PathVariable("discoveryTaskId") String discoveryTaskId) {
        ComponentDiscoveryResultEntity discoveryTask = componentDiscoveryResultService.getByTaskId(discoveryTaskId);
        if (discoveryTask == null) {
            return Result.error("发现任务不存在");
        }
        return Result.success().put(Constants.DATA, discoveryTask);
    }
    
    /**
     * 更新发现任务状态（内部使用，通常由系统调用）
     */
    @RequestMapping("/update-status")
    public Result updateStatus(
                               @RequestParam String discoveryTaskId,
                               @RequestParam DiscoveryStatus status,
                               @RequestParam(required = false) String discoveryStats,
                               @RequestParam(required = false) String discoveryDetails,
                               @RequestParam(required = false) String errorMessage) {
        return componentDiscoveryResultService.updateDiscoveryStatus(
                discoveryTaskId, status, discoveryStats, discoveryDetails, errorMessage);
    }
    
    /**
     * 获取发现任务列表（分页）
     */
    @RequestMapping("/list-tasks")
    public Result listTasks(@RequestBody Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String taskName = (String) params.get("taskName");
        String status = (String) params.get("status");
        String componentType = (String) params.get("componentType");
        
        return componentDiscoveryResultService.listTasks(page, pageSize, taskName, status, componentType);
    }
    
    /**
     * 启动发现任务
     */
    @RequestMapping("/start-task")
    @UserPermission
    public Result startTask(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        Integer clusterId = (Integer) params.get("clusterId");
        String discoveryStrategy = (String) params.get("discoveryStrategy");
        List<String> targetHosts = (List<String>) params.get("targetHostIds");
        Integer timeoutSeconds = (Integer) params.get("timeoutSeconds");
        Integer concurrentThreads = (Integer) params.get("concurrentThreads");
        
        return componentDiscoveryResultService.startTask(
                taskName, clusterId, discoveryStrategy, targetHosts, timeoutSeconds, concurrentThreads);
    }
    
    /**
     * 停止发现任务
     */
    @RequestMapping("/stop-task")
    @UserPermission
    public Result stopTask(@RequestBody Map<String, Object> params) {
        String taskId = (String) params.get("taskId");
        return componentDiscoveryResultService.stopTask(taskId);
    }
    
    /**
     * 重试发现任务
     */
    @RequestMapping("/retry-task")
    @UserPermission
    public Result retryTask(@RequestBody Map<String, Object> params) {
        String taskId = (String) params.get("taskId");
        return componentDiscoveryResultService.retryTask(taskId);
    }
    
    /**
     * 删除发现任务
     */
    @RequestMapping("/delete-task")
    @UserPermission
    public Result deleteTask(@RequestBody Map<String, Object> params) {
        String taskId = (String) params.get("taskId");
        return componentDiscoveryResultService.deleteTask(taskId);
    }
}
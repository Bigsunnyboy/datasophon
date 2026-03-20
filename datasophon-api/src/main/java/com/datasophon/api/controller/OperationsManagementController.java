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
import com.datasophon.api.service.OperationsManagementService;
import com.datasophon.common.utils.Result;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运维管理控制器
 * 提供对已接管组件的运维操作、健康检查、日志查看等功能
 */
@RestController
@RequestMapping("api/operations")
public class OperationsManagementController {
    
    @Autowired
    private OperationsManagementService operationsManagementService;
    
    /**
     * 获取服务列表
     */
    @RequestMapping("/services/list")
    @UserPermission
    public Result listServices(@RequestBody Map<String, Object> params) {
        Integer page = (Integer) params.get("page");
        Integer pageSize = (Integer) params.get("pageSize");
        String keyword = (String) params.get("keyword");
        String clusterId = (String) params.get("clusterId");
        String status = (String) params.get("status");
        return operationsManagementService.listServices(page, pageSize, keyword, clusterId, status);
    }
    
    /**
     * 获取服务详情
     */
    @RequestMapping("/services/detail/{serviceId}")
    @UserPermission
    public Result getServiceDetail(@PathVariable("serviceId") String serviceId) {
        return operationsManagementService.getServiceDetail(serviceId);
    }
    
    /**
     * 获取服务状态
     */
    @RequestMapping("/services/status/{serviceId}")
    @UserPermission
    public Result getServiceStatus(@PathVariable("serviceId") String serviceId) {
        return operationsManagementService.getServiceStatus(serviceId);
    }
    
    /**
     * 获取服务统计信息
     */
    @RequestMapping("/services/stats")
    @UserPermission
    public Result getStats() {
        return operationsManagementService.getStats();
    }
    
    /**
     * 启动服务
     */
    @RequestMapping("/actions/start")
    @UserPermission
    public Result startService(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        return operationsManagementService.startService(serviceId);
    }
    
    /**
     * 停止服务
     */
    @RequestMapping("/actions/stop")
    @UserPermission
    public Result stopService(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        return operationsManagementService.stopService(serviceId);
    }
    
    /**
     * 重启服务
     */
    @RequestMapping("/actions/restart")
    @UserPermission
    public Result restartService(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        return operationsManagementService.restartService(serviceId);
    }
    
    /**
     * 重载配置
     */
    @RequestMapping("/actions/reload-config")
    @UserPermission
    public Result reloadConfig(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        return operationsManagementService.reloadConfig(serviceId);
    }
    
    /**
     * 变更接管级别
     */
    @RequestMapping("/actions/change-takeover-level")
    @UserPermission
    public Result changeTakeoverLevel(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String takeoverLevel = (String) params.get("takeoverLevel");
        return operationsManagementService.changeTakeoverLevel(serviceId, takeoverLevel);
    }
    
    /**
     * 批量启动服务
     */
    @RequestMapping("/actions/batch-start")
    @UserPermission
    public Result batchStartServices(@RequestBody Map<String, Object> params) {
        List<String> serviceIds = (List<String>) params.get("serviceIds");
        return operationsManagementService.batchStartServices(serviceIds);
    }
    
    /**
     * 批量停止服务
     */
    @RequestMapping("/actions/batch-stop")
    @UserPermission
    public Result batchStopServices(@RequestBody Map<String, Object> params) {
        List<String> serviceIds = (List<String>) params.get("serviceIds");
        return operationsManagementService.batchStopServices(serviceIds);
    }
    
    /**
     * 批量重启服务
     */
    @RequestMapping("/actions/batch-restart")
    @UserPermission
    public Result batchRestartServices(@RequestBody Map<String, Object> params) {
        List<String> serviceIds = (List<String>) params.get("serviceIds");
        return operationsManagementService.batchRestartServices(serviceIds);
    }
    
    /**
     * 批量重载配置
     */
    @RequestMapping("/actions/batch-reload-configs")
    @UserPermission
    public Result batchReloadConfigs(@RequestBody Map<String, Object> params) {
        List<String> serviceIds = (List<String>) params.get("serviceIds");
        return operationsManagementService.batchReloadConfigs(serviceIds);
    }
    
    /**
     * 健康检查
     */
    @RequestMapping("/health/check")
    @UserPermission
    public Result checkHealth(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String checkType = (String) params.get("checkType");
        return operationsManagementService.checkHealth(serviceId, checkType);
    }
    
    /**
     * 批量健康检查
     */
    @RequestMapping("/health/batch-check")
    @UserPermission
    public Result batchHealthCheck(@RequestBody Map<String, Object> params) {
        List<String> serviceIds = (List<String>) params.get("serviceIds");
        String checkType = (String) params.get("checkType");
        return operationsManagementService.batchHealthCheck(serviceIds, checkType);
    }
    
    /**
     * 运行全局健康检查
     */
    @RequestMapping("/health/run-all")
    @UserPermission
    public Result runHealthCheckAll() {
        return operationsManagementService.runHealthCheckAll();
    }
    
    /**
     * 获取健康检查结果
     */
    @RequestMapping("/health/result/{checkId}")
    @UserPermission
    public Result getHealthCheckResult(@PathVariable("checkId") String checkId) {
        return operationsManagementService.getHealthCheckResult(checkId);
    }
    
    /**
     * 获取日志
     */
    @RequestMapping("/logs/get")
    @UserPermission
    public Result getLogs(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String logFile = (String) params.get("logFile");
        Integer lines = (Integer) params.get("lines");
        return operationsManagementService.getLogs(serviceId, logFile, lines);
    }
    
    /**
     * 下载日志
     */
    @RequestMapping("/logs/download")
    @UserPermission
    public Result downloadLogs(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String logFile = (String) params.get("logFile");
        return operationsManagementService.downloadLogs(serviceId, logFile);
    }
    
    /**
     * 搜索日志
     */
    @RequestMapping("/logs/search")
    @UserPermission
    public Result searchLogs(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String logFile = (String) params.get("logFile");
        String keyword = (String) params.get("keyword");
        return operationsManagementService.searchLogs(serviceId, logFile, keyword);
    }
    
    /**
     * 清空日志
     */
    @RequestMapping("/logs/clear")
    @UserPermission
    public Result clearLogs(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String logFile = (String) params.get("logFile");
        return operationsManagementService.clearLogs(serviceId, logFile);
    }
    
    /**
     * 获取监控指标
     */
    @RequestMapping("/metrics/get")
    @UserPermission
    public Result getMetrics(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String metricType = (String) params.get("metricType");
        String timeRange = (String) params.get("timeRange");
        return operationsManagementService.getMetrics(serviceId, metricType, timeRange);
    }
    
    /**
     * 获取实时指标
     */
    @RequestMapping("/metrics/realtime")
    @UserPermission
    public Result getRealtimeMetrics(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String metricType = (String) params.get("metricType");
        return operationsManagementService.getRealtimeMetrics(serviceId, metricType);
    }
    
    /**
     * 获取指标历史
     */
    @RequestMapping("/metrics/history")
    @UserPermission
    public Result getMetricHistory(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String metricType = (String) params.get("metricType");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");
        return operationsManagementService.getMetricHistory(serviceId, metricType, startTime, endTime);
    }
    
    /**
     * 创建运维任务
     */
    @RequestMapping("/tasks/create")
    @UserPermission
    public Result createOperationTask(@RequestBody Map<String, Object> params) {
        String taskType = (String) params.get("taskType");
        List<String> targetServices = (List<String>) params.get("targetServices");
        Map<String, Object> parameters = (Map<String, Object>) params.get("parameters");
        return operationsManagementService.createOperationTask(taskType, targetServices, parameters);
    }
    
    /**
     * 获取任务状态
     */
    @RequestMapping("/tasks/status/{taskId}")
    @UserPermission
    public Result getTaskStatus(@PathVariable("taskId") String taskId) {
        return operationsManagementService.getTaskStatus(taskId);
    }
    
    /**
     * 取消运维任务
     */
    @RequestMapping("/tasks/cancel/{taskId}")
    @UserPermission
    public Result cancelOperationTask(@PathVariable("taskId") String taskId) {
        return operationsManagementService.cancelOperationTask(taskId);
    }
    
    /**
     * 获取任务历史
     */
    @RequestMapping("/tasks/history")
    @UserPermission
    public Result getTaskHistory(@RequestBody Map<String, Object> params) {
        Integer page = (Integer) params.get("page");
        Integer pageSize = (Integer) params.get("pageSize");
        return operationsManagementService.getTaskHistory(page, pageSize);
    }
    
    /**
     * 获取服务告警
     */
    @RequestMapping("/alerts/get")
    @UserPermission
    public Result getServiceAlerts(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String severity = (String) params.get("severity");
        String status = (String) params.get("status");
        return operationsManagementService.getServiceAlerts(serviceId, severity, status);
    }
    
    /**
     * 确认告警
     */
    @RequestMapping("/alerts/acknowledge")
    @UserPermission
    public Result acknowledgeAlert(@RequestBody Map<String, Object> params) {
        String alertId = (String) params.get("alertId");
        String comment = (String) params.get("comment");
        return operationsManagementService.acknowledgeAlert(alertId, comment);
    }
    
    /**
     * 清除告警
     */
    @RequestMapping("/alerts/clear")
    @UserPermission
    public Result clearAlert(@RequestBody Map<String, Object> params) {
        String alertId = (String) params.get("alertId");
        return operationsManagementService.clearAlert(alertId);
    }
    
    /**
     * 进入维护模式
     */
    @RequestMapping("/maintenance/enter")
    @UserPermission
    public Result enterMaintenanceMode(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        String reason = (String) params.get("reason");
        Integer duration = (Integer) params.get("duration");
        return operationsManagementService.enterMaintenanceMode(serviceId, reason, duration);
    }
    
    /**
     * 退出维护模式
     */
    @RequestMapping("/maintenance/exit")
    @UserPermission
    public Result exitMaintenanceMode(@RequestBody Map<String, Object> params) {
        String serviceId = (String) params.get("serviceId");
        return operationsManagementService.exitMaintenanceMode(serviceId);
    }
    
    /**
     * 获取维护状态
     */
    @RequestMapping("/maintenance/status/{serviceId}")
    @UserPermission
    public Result getMaintenanceStatus(@PathVariable("serviceId") String serviceId) {
        return operationsManagementService.getMaintenanceStatus(serviceId);
    }
}
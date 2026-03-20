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

package com.datasophon.api.service;

import com.datasophon.common.utils.Result;

import java.util.List;
import java.util.Map;

/**
 * 运维管理服务接口
 * 提供对已接管组件的运维操作、健康检查、日志查看等功能
 */
public interface OperationsManagementService {
    
    /**
     * 获取服务列表
     */
    Result listServices(Integer page, Integer pageSize, String keyword, String clusterId, String status);
    
    /**
     * 获取服务详情
     */
    Result getServiceDetail(String serviceId);
    
    /**
     * 获取服务状态
     */
    Result getServiceStatus(String serviceId);
    
    /**
     * 获取服务统计信息
     */
    Result getStats();
    
    /**
     * 启动服务
     */
    Result startService(String serviceId);
    
    /**
     * 停止服务
     */
    Result stopService(String serviceId);
    
    /**
     * 重启服务
     */
    Result restartService(String serviceId);
    
    /**
     * 重载配置
     */
    Result reloadConfig(String serviceId);
    
    /**
     * 变更接管级别
     */
    Result changeTakeoverLevel(String serviceId, String takeoverLevel);
    
    /**
     * 批量启动服务
     */
    Result batchStartServices(List<String> serviceIds);
    
    /**
     * 批量停止服务
     */
    Result batchStopServices(List<String> serviceIds);
    
    /**
     * 批量重启服务
     */
    Result batchRestartServices(List<String> serviceIds);
    
    /**
     * 批量重载配置
     */
    Result batchReloadConfigs(List<String> serviceIds);
    
    /**
     * 健康检查
     */
    Result checkHealth(String serviceId, String checkType);
    
    /**
     * 批量健康检查
     */
    Result batchHealthCheck(List<String> serviceIds, String checkType);
    
    /**
     * 运行全局健康检查
     */
    Result runHealthCheckAll();
    
    /**
     * 获取健康检查结果
     */
    Result getHealthCheckResult(String checkId);
    
    /**
     * 获取日志
     */
    Result getLogs(String serviceId, String logFile, Integer lines);
    
    /**
     * 下载日志
     */
    Result downloadLogs(String serviceId, String logFile);
    
    /**
     * 搜索日志
     */
    Result searchLogs(String serviceId, String logFile, String keyword);
    
    /**
     * 清空日志
     */
    Result clearLogs(String serviceId, String logFile);
    
    /**
     * 获取监控指标
     */
    Result getMetrics(String serviceId, String metricType, String timeRange);
    
    /**
     * 获取实时指标
     */
    Result getRealtimeMetrics(String serviceId, String metricType);
    
    /**
     * 获取指标历史
     */
    Result getMetricHistory(String serviceId, String metricType, String startTime, String endTime);
    
    /**
     * 创建运维任务
     */
    Result createOperationTask(String taskType, List<String> targetServices, Map<String, Object> parameters);
    
    /**
     * 获取任务状态
     */
    Result getTaskStatus(String taskId);
    
    /**
     * 取消运维任务
     */
    Result cancelOperationTask(String taskId);
    
    /**
     * 获取任务历史
     */
    Result getTaskHistory(Integer page, Integer pageSize);
    
    /**
     * 获取服务告警
     */
    Result getServiceAlerts(String serviceId, String severity, String status);
    
    /**
     * 确认告警
     */
    Result acknowledgeAlert(String alertId, String comment);
    
    /**
     * 清除告警
     */
    Result clearAlert(String alertId);
    
    /**
     * 进入维护模式
     */
    Result enterMaintenanceMode(String serviceId, String reason, Integer duration);
    
    /**
     * 退出维护模式
     */
    Result exitMaintenanceMode(String serviceId);
    
    /**
     * 获取维护状态
     */
    Result getMaintenanceStatus(String serviceId);
}
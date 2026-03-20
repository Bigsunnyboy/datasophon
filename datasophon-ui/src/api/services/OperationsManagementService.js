/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import BaseService from './BaseService';

/**
 * Operations Management Service for managing service operations and health checks
 */
class OperationsManagementService extends BaseService {
  constructor() {
    super(global.API.operationsManagement);
  }

  // 服务列表和状态

  /**
   * 获取服务列表
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async listServices(params) {
    return this.post(this.apiModule.listServices, params);
  }

  /**
   * 获取服务详情
   * @param {string} serviceId - 服务ID
   * @returns {Promise<any>}
   */
  async getServiceDetail(serviceId) {
    const endpoint = this.apiModule.getServiceDetail?.replace('{serviceId}', serviceId) ||
                    `${this.apiModule.getServiceDetail}/${serviceId}`;
    return this.get(endpoint);
  }

  /**
   * 获取服务状态
   * @param {string} serviceId - 服务ID
   * @returns {Promise<any>}
   */
  async getServiceStatus(serviceId) {
    const endpoint = this.apiModule.getServiceStatus?.replace('{serviceId}', serviceId) ||
                    `${this.apiModule.getServiceStatus}/${serviceId}`;
    return this.get(endpoint);
  }

  /**
   * 获取服务统计信息
   * @returns {Promise<any>}
   */
  async getStats() {
    return this.get(this.apiModule.getStats);
  }

  // 服务操作

  /**
   * 启动服务
   * @param {Object} params - 启动参数 { serviceId }
   * @returns {Promise<any>}
   */
  async startService(params) {
    return this.post(this.apiModule.startService, params);
  }

  /**
   * 停止服务
   * @param {Object} params - 停止参数 { serviceId }
   * @returns {Promise<any>}
   */
  async stopService(params) {
    return this.post(this.apiModule.stopService, params);
  }

  /**
   * 重启服务
   * @param {Object} params - 重启参数 { serviceId }
   * @returns {Promise<any>}
   */
  async restartService(params) {
    return this.post(this.apiModule.restartService, params);
  }

  /**
   * 重载配置
   * @param {Object} params - 重载参数 { serviceId }
   * @returns {Promise<any>}
   */
  async reloadConfig(params) {
    return this.post(this.apiModule.reloadConfig, params);
  }

  /**
   * 变更接管级别
   * @param {Object} params - 变更参数 { serviceId, takeoverLevel }
   * @returns {Promise<any>}
   */
  async changeTakeoverLevel(params) {
    return this.post(this.apiModule.changeTakeoverLevel, params);
  }

  // 批量操作

  /**
   * 批量启动服务
   * @param {Object} params - 批量启动参数 { serviceIds }
   * @returns {Promise<any>}
   */
  async batchStartServices(params) {
    return this.post(this.apiModule.batchStartServices, params);
  }

  /**
   * 批量停止服务
   * @param {Object} params - 批量停止参数 { serviceIds }
   * @returns {Promise<any>}
   */
  async batchStopServices(params) {
    return this.post(this.apiModule.batchStopServices, params);
  }

  /**
   * 批量重启服务
   * @param {Object} params - 批量重启参数 { serviceIds }
   * @returns {Promise<any>}
   */
  async batchRestartServices(params) {
    return this.post(this.apiModule.batchRestartServices, params);
  }

  /**
   * 批量重载配置
   * @param {Object} params - 批量重载参数 { serviceIds }
   * @returns {Promise<any>}
   */
  async batchReloadConfigs(params) {
    return this.post(this.apiModule.batchReloadConfigs, params);
  }

  // 健康检查

  /**
   * 健康检查
   * @param {Object} params - 检查参数 { serviceId, checkType }
   * @returns {Promise<any>}
   */
  async checkHealth(params) {
    return this.post(this.apiModule.checkHealth, params);
  }

  /**
   * 批量健康检查
   * @param {Object} params - 批量检查参数 { serviceIds, checkType }
   * @returns {Promise<any>}
   */
  async batchHealthCheck(params) {
    return this.post(this.apiModule.batchHealthCheck, params);
  }

  /**
   * 运行全局健康检查
   * @returns {Promise<any>}
   */
  async runHealthCheckAll() {
    return this.post(this.apiModule.runHealthCheckAll);
  }

  /**
   * 获取健康检查结果
   * @param {string} checkId - 检查ID
   * @returns {Promise<any>}
   */
  async getHealthCheckResult(checkId) {
    const endpoint = this.apiModule.getHealthCheckResult?.replace('{checkId}', checkId) ||
                    `${this.apiModule.getHealthCheckResult}/${checkId}`;
    return this.get(endpoint);
  }

  // 日志管理

  /**
   * 获取日志
   * @param {Object} params - 日志参数 { serviceId, logFile, lines }
   * @returns {Promise<any>}
   */
  async getLogs(params) {
    return this.post(this.apiModule.getLogs, params);
  }

  /**
   * 下载日志
   * @param {Object} params - 下载参数 { serviceId, logFile }
   * @returns {Promise<any>}
   */
  async downloadLogs(params) {
    return this.post(this.apiModule.downloadLogs, params);
  }

  /**
   * 搜索日志
   * @param {Object} params - 搜索参数 { serviceId, logFile, keyword }
   * @returns {Promise<any>}
   */
  async searchLogs(params) {
    return this.post(this.apiModule.searchLogs, params);
  }

  /**
   * 清空日志
   * @param {Object} params - 清空参数 { serviceId, logFile }
   * @returns {Promise<any>}
   */
  async clearLogs(params) {
    return this.post(this.apiModule.clearLogs, params);
  }

  // 监控指标

  /**
   * 获取监控指标
   * @param {Object} params - 指标参数 { serviceId, metricType, timeRange }
   * @returns {Promise<any>}
   */
  async getMetrics(params) {
    return this.post(this.apiModule.getMetrics, params);
  }

  /**
   * 获取实时指标
   * @param {Object} params - 实时参数 { serviceId, metricType }
   * @returns {Promise<any>}
   */
  async getRealtimeMetrics(params) {
    return this.post(this.apiModule.getRealtimeMetrics, params);
  }

  /**
   * 获取指标历史
   * @param {Object} params - 历史参数 { serviceId, metricType, startTime, endTime }
   * @returns {Promise<any>}
   */
  async getMetricHistory(params) {
    return this.post(this.apiModule.getMetricHistory, params);
  }

  // 运维任务

  /**
   * 创建运维任务
   * @param {Object} params - 任务参数 { taskType, targetServices, parameters }
   * @returns {Promise<any>}
   */
  async createOperationTask(params) {
    return this.post(this.apiModule.createOperationTask, params);
  }

  /**
   * 获取任务状态
   * @param {string} taskId - 任务ID
   * @returns {Promise<any>}
   */
  async getTaskStatus(taskId) {
    const endpoint = this.apiModule.getTaskStatus?.replace('{taskId}', taskId) ||
                    `${this.apiModule.getTaskStatus}/${taskId}`;
    return this.get(endpoint);
  }

  /**
   * 取消运维任务
   * @param {string} taskId - 任务ID
   * @returns {Promise<any>}
   */
  async cancelOperationTask(taskId) {
    const endpoint = this.apiModule.cancelOperationTask?.replace('{taskId}', taskId) ||
                    `${this.apiModule.cancelOperationTask}/${taskId}`;
    return this.post(endpoint);
  }

  /**
   * 获取任务历史
   * @param {Object} params - 历史参数 { page, pageSize }
   * @returns {Promise<any>}
   */
  async getTaskHistory(params) {
    return this.post(this.apiModule.getTaskHistory, params);
  }

  // 告警管理

  /**
   * 获取服务告警
   * @param {Object} params - 告警参数 { serviceId, severity, status }
   * @returns {Promise<any>}
   */
  async getServiceAlerts(params) {
    return this.post(this.apiModule.getServiceAlerts, params);
  }

  /**
   * 确认告警
   * @param {Object} params - 确认参数 { alertId, comment }
   * @returns {Promise<any>}
   */
  async acknowledgeAlert(params) {
    return this.post(this.apiModule.acknowledgeAlert, params);
  }

  /**
   * 清除告警
   * @param {Object} params - 清除参数 { alertId }
   * @returns {Promise<any>}
   */
  async clearAlert(params) {
    return this.post(this.apiModule.clearAlert, params);
  }

  // 维护模式

  /**
   * 进入维护模式
   * @param {Object} params - 维护参数 { serviceId, reason, duration }
   * @returns {Promise<any>}
   */
  async enterMaintenanceMode(params) {
    return this.post(this.apiModule.enterMaintenanceMode, params);
  }

  /**
   * 退出维护模式
   * @param {Object} params - 退出参数 { serviceId }
   * @returns {Promise<any>}
   */
  async exitMaintenanceMode(params) {
    return this.post(this.apiModule.exitMaintenanceMode, params);
  }

  /**
   * 获取维护状态
   * @param {string} serviceId - 服务ID
   * @returns {Promise<any>}
   */
  async getMaintenanceStatus(serviceId) {
    const endpoint = this.apiModule.getMaintenanceStatus?.replace('{serviceId}', serviceId) ||
                    `${this.apiModule.getMaintenanceStatus}/${serviceId}`;
    return this.get(endpoint);
  }
}

export default OperationsManagementService;
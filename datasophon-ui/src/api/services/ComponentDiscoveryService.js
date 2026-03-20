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
 * Component Discovery Service for managing component discovery tasks and results
 */
class ComponentDiscoveryService extends BaseService {
  constructor() {
    super(global.API.componentDiscovery);
  }

  // 组件发现任务管理
  
  /**
   * 创建发现任务
   * @param {Object} params - 任务参数
   * @returns {Promise<any>}
   */
  async createDiscoveryTask(params) {
    return this.post(this.apiModule.createDiscoveryTask, params);
  }

  /**
   * 启动发现任务
   * @param {string} discoveryTaskId - 任务ID
   * @returns {Promise<any>}
   */
  async startDiscoveryTask(discoveryTaskId) {
    const endpoint = this.apiModule.startDiscoveryTask.replace('{discoveryTaskId}', discoveryTaskId);
    return this.post(endpoint);
  }

  /**
   * 获取发现任务进度
   * @param {string} discoveryTaskId - 任务ID
   * @returns {Promise<any>}
   */
  async getDiscoveryProgress(discoveryTaskId) {
    const endpoint = this.apiModule.getDiscoveryProgress.replace('{discoveryTaskId}', discoveryTaskId);
    return this.get(endpoint);
  }

  /**
   * 获取发现任务详情
   * @param {string} discoveryTaskId - 任务ID
   * @returns {Promise<any>}
   */
  async getDiscoveryDetails(discoveryTaskId) {
    const endpoint = this.apiModule.getDiscoveryDetails.replace('{discoveryTaskId}', discoveryTaskId);
    return this.get(endpoint);
  }

  /**
   * 取消发现任务
   * @param {string} discoveryTaskId - 任务ID
   * @returns {Promise<any>}
   */
  async cancelDiscoveryTask(discoveryTaskId) {
    const endpoint = this.apiModule.cancelDiscoveryTask.replace('{discoveryTaskId}', discoveryTaskId);
    return this.post(endpoint);
  }

  /**
   * 验证发现结果
   * @param {string} discoveryTaskId - 任务ID
   * @returns {Promise<any>}
   */
  async validateDiscoveryResult(discoveryTaskId) {
    const endpoint = this.apiModule.validateDiscoveryResult.replace('{discoveryTaskId}', discoveryTaskId);
    return this.post(endpoint);
  }

  /**
   * 自动注册发现的组件
   * @param {string} discoveryTaskId - 任务ID
   * @returns {Promise<any>}
   */
  async autoRegisterDiscoveredComponents(discoveryTaskId) {
    const endpoint = this.apiModule.autoRegisterDiscoveredComponents.replace('{discoveryTaskId}', discoveryTaskId);
    return this.post(endpoint);
  }

  // 发现执行
  
  /**
   * 执行自动发现
   * @param {Object} params - 发现参数
   * @returns {Promise<any>}
   */
  async executeAutoDiscovery(params) {
    return this.post(this.apiModule.executeAutoDiscovery, params);
  }

  /**
   * 执行手动发现
   * @param {Object} params - 发现参数
   * @returns {Promise<any>}
   */
  async executeManualDiscovery(params) {
    return this.post(this.apiModule.executeManualDiscovery, params);
  }

  /**
   * 获取可用的发现方法
   * @returns {Promise<any>}
   */
  async getAvailableDiscoveryMethods() {
    return this.get(this.apiModule.getAvailableDiscoveryMethods);
  }

  // 发现任务查询
  
  /**
   * 按集群ID列出发现任务
   * @param {string} clusterId - 集群ID
   * @returns {Promise<any>}
   */
  async listDiscoveryByCluster(clusterId) {
    const endpoint = this.apiModule.listDiscoveryByCluster.replace('{clusterId}', clusterId);
    return this.get(endpoint);
  }

  /**
   * 按集群和服务列出发现任务
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async listDiscoveryByClusterAndService(params) {
    return this.post(this.apiModule.listDiscoveryByClusterAndService, params);
  }

  /**
   * 按状态列出发现任务
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async listDiscoveryByStatus(params) {
    return this.post(this.apiModule.listDiscoveryByStatus, params);
  }

  /**
   * 获取最近的发现任务
   * @returns {Promise<any>}
   */
  async listRecentDiscoveries() {
    return this.get(this.apiModule.listRecentDiscoveries);
  }

  /**
   * 获取发现统计信息
   * @returns {Promise<any>}
   */
  async getDiscoveryStats() {
    return this.get(this.apiModule.getDiscoveryStats);
  }

  /**
   * 获取发现任务信息
   * @param {string} discoveryTaskId - 任务ID
   * @returns {Promise<any>}
   */
  async getDiscoveryInfo(discoveryTaskId) {
    const endpoint = this.apiModule.getDiscoveryInfo.replace('{discoveryTaskId}', discoveryTaskId);
    return this.get(endpoint);
  }

  // 系统管理
  
  /**
   * 删除过期的发现结果
   * @returns {Promise<any>}
   */
  async deleteExpiredDiscoveryResults() {
    return this.post(this.apiModule.deleteExpiredDiscoveryResults);
  }

  // 组件发现任务管理 (新增API)
  
  /**
   * 获取发现任务列表
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async listTasks(params) {
    return this.post(this.apiModule.listTasks, params);
  }

  /**
   * 启动发现任务
   * @param {Object} params - 任务参数
   * @returns {Promise<any>}
   */
  async startTask(params) {
    return this.post(this.apiModule.startTask, params);
  }

  /**
   * 停止发现任务
   * @param {string} taskId - 任务ID
   * @returns {Promise<any>}
   */
  async stopTask(taskId) {
    return this.post(this.apiModule.stopTask, { taskId });
  }

  /**
   * 重试发现任务
   * @param {string} taskId - 任务ID
   * @returns {Promise<any>}
   */
  async retryTask(taskId) {
    return this.post(this.apiModule.retryTask, { taskId });
  }

  /**
   * 删除发现任务
   * @param {string} taskId - 任务ID
   * @returns {Promise<any>}
   */
  async deleteTask(taskId) {
    return this.post(this.apiModule.deleteTask, { taskId });
  }

  /**
   * 获取任务详情
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async getTaskDetail(params) {
    return this.post(this.apiModule.getTaskDetail, params);
  }

  /**
   * 获取任务结果
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async getTaskResults(params) {
    return this.post(this.apiModule.getTaskResults, params);
  }

  /**
   * 获取任务统计
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async getTaskStats(params) {
    return this.post(this.apiModule.getTaskStats, params);
  }

  /**
   * 获取任务主机列表
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async getTaskHosts(params) {
    return this.post(this.apiModule.getTaskHosts, params);
  }

  /**
   * 获取任务进度
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async getTaskProgress(params) {
    return this.post(this.apiModule.getTaskProgress, params);
  }

  /**
   * 获取任务日志
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async getTaskLogs(params) {
    return this.post(this.apiModule.getTaskLogs, params);
  }

  /**
   * 获取预览结果
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async getPreviewResults(params) {
    return this.post(this.apiModule.getPreviewResults, params);
  }

  /**
   * 清除任务日志
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async clearTaskLogs(params) {
    return this.post(this.apiModule.clearTaskLogs, params);
  }

  /**
   * 导出任务日志
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async exportTaskLogs(params) {
    return this.post(this.apiModule.exportTaskLogs, params);
  }

  /**
   * 导出结果
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async exportResults(params) {
    return this.post(this.apiModule.exportResults, params);
  }

  /**
   * 验证组件
   * @param {Object} params - 验证参数
   * @returns {Promise<any>}
   */
  async validateComponent(params) {
    return this.post(this.apiModule.validateComponent, params);
  }

  /**
   * 导入组件
   * @param {Object} params - 导入参数
   * @returns {Promise<any>}
   */
  async importComponent(params) {
    return this.post(this.apiModule.importComponent, params);
  }

  /**
   * 删除结果
   * @param {Object} params - 删除参数
   * @returns {Promise<any>}
   */
  async deleteResult(params) {
    return this.post(this.apiModule.deleteResult, params);
  }

  /**
   * 批量验证
   * @param {Object} params - 批量验证参数
   * @returns {Promise<any>}
   */
  async batchValidate(params) {
    return this.post(this.apiModule.batchValidate, params);
  }

  /**
   * 批量导入
   * @param {Object} params - 批量导入参数
   * @returns {Promise<any>}
   */
  async batchImport(params) {
    return this.post(this.apiModule.batchImport, params);
  }

  /**
   * 批量删除
   * @param {Object} params - 批量删除参数
   * @returns {Promise<any>}
   */
  async batchDelete(params) {
    return this.post(this.apiModule.batchDelete, params);
  }

  // 集群相关API（从其他模块整合）
  
  /**
   * 获取集群列表
   * @returns {Promise<any>}
   */
  async getClusterList() {
    return this.post(global.API.cluster.list, {});
  }

  /**
   * 获取集群信息
   * @param {string} clusterId - 集群ID
   * @returns {Promise<any>}
   */
  async getClusterInfo(clusterId) {
    const endpoint = global.API.cluster.info?.replace('{id}', clusterId) || 
                    `${global.API.cluster.info}/${clusterId}`;
    return this.get(endpoint);
  }

  /**
   * 获取服务列表
   * @returns {Promise<any>}
   */
  async getServiceList() {
    return this.post(global.API.frame.service.list, {});
  }

  /**
   * 获取集群所有主机
   * @returns {Promise<any>}
   */
  async getAllHosts() {
    return this.post(global.API.cluster.host.all, {});
  }

  /**
   * 按集群ID获取主机列表
   * @param {string} clusterId - 集群ID
   * @returns {Promise<any>}
   */
  async getHostListByClusterId(clusterId) {
    return this.post(global.API.host.getHostListByClusterId, { clusterId });
  }
}

export default ComponentDiscoveryService;
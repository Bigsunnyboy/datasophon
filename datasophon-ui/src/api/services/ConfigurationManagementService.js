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
 * Configuration Management Service for managing configuration sync and comparison
 */
class ConfigurationManagementService extends BaseService {
  constructor() {
    super(global.API.configurationManagement);
  }

  // 配置列表查询

  /**
   * 获取配置列表
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  async listConfigurations(params) {
    return this.post(this.apiModule.listConfigurations, params);
  }

  /**
   * 获取配置详情
   * @param {string} configId - 配置ID
   * @returns {Promise<any>}
   */
  async getConfigurationDetail(configId) {
    const endpoint = this.apiModule.getConfigurationDetail?.replace('{configId}', configId) ||
                    `${this.apiModule.getConfigurationDetail}/${configId}`;
    return this.get(endpoint);
  }

  /**
   * 获取配置对比信息
   * @param {string} configId - 配置ID
   * @returns {Promise<any>}
   */
  async getConfigurationCompare(configId) {
    const endpoint = this.apiModule.getConfigurationCompare?.replace('{configId}', configId) ||
                    `${this.apiModule.getConfigurationCompare}/${configId}`;
    return this.get(endpoint);
  }

  // 配置同步操作

  /**
   * 同步配置
   * @param {Object} params - 同步参数 { configId, syncDirection }
   * @returns {Promise<any>}
   */
  async syncConfiguration(params) {
    return this.post(this.apiModule.syncConfiguration, params);
  }

  /**
   * 批量同步配置
   * @param {Object} params - 批量同步参数 { configIds, syncDirection }
   * @returns {Promise<any>}
   */
  async batchSyncConfigurations(params) {
    return this.post(this.apiModule.batchSyncConfigurations, params);
  }

  /**
   * 获取同步状态
   * @param {string} syncTaskId - 同步任务ID
   * @returns {Promise<any>}
   */
  async getSyncStatus(syncTaskId) {
    const endpoint = this.apiModule.getSyncStatus?.replace('{syncTaskId}', syncTaskId) ||
                    `${this.apiModule.getSyncStatus}/${syncTaskId}`;
    return this.get(endpoint);
  }

  /**
   * 取消同步任务
   * @param {string} syncTaskId - 同步任务ID
   * @returns {Promise<any>}
   */
  async cancelSyncTask(syncTaskId) {
    const endpoint = this.apiModule.cancelSyncTask?.replace('{syncTaskId}', syncTaskId) ||
                    `${this.apiModule.cancelSyncTask}/${syncTaskId}`;
    return this.post(endpoint);
  }

  // 配置历史管理

  /**
   * 获取配置历史
   * @param {Object} params - 查询参数 { configId, page, pageSize }
   * @returns {Promise<any>}
   */
  async getConfigHistory(params) {
    return this.post(this.apiModule.getConfigHistory, params);
  }

  /**
   * 获取配置版本详情
   * @param {string} versionId - 版本ID
   * @returns {Promise<any>}
   */
  async getConfigVersionDetail(versionId) {
    const endpoint = this.apiModule.getConfigVersionDetail?.replace('{versionId}', versionId) ||
                    `${this.apiModule.getConfigVersionDetail}/${versionId}`;
    return this.get(endpoint);
  }

  /**
   * 回滚到指定版本
   * @param {Object} params - 回滚参数 { configId, versionId }
   * @returns {Promise<any>}
   */
  async rollbackToVersion(params) {
    return this.post(this.apiModule.rollbackToVersion, params);
  }

  // 配置验证

  /**
   * 验证配置
   * @param {string} configId - 配置ID
   * @returns {Promise<any>}
   */
  async validateConfiguration(configId) {
    const endpoint = this.apiModule.validateConfiguration?.replace('{configId}', configId) ||
                    `${this.apiModule.validateConfiguration}/${configId}`;
    return this.post(endpoint);
  }

  /**
   * 批量验证配置
   * @param {Object} params - 批量验证参数 { configIds }
   * @returns {Promise<any>}
   */
  async batchValidateConfigurations(params) {
    return this.post(this.apiModule.batchValidateConfigurations, params);
  }

  // 配置导入导出

  /**
   * 导出配置
   * @param {Object} params - 导出参数 { configId, format }
   * @returns {Promise<any>}
   */
  async exportConfiguration(params) {
    return this.post(this.apiModule.exportConfiguration, params);
  }

  /**
   * 导入配置
   * @param {Object} params - 导入参数 { configId, configData, format }
   * @returns {Promise<any>}
   */
  async importConfiguration(params) {
    return this.post(this.apiModule.importConfiguration, params);
  }

  // 配置差异分析

  /**
   * 分析配置差异
   * @param {string} configId - 配置ID
   * @returns {Promise<any>}
   */
  async analyzeConfigDiff(configId) {
    const endpoint = this.apiModule.analyzeConfigDiff?.replace('{configId}', configId) ||
                    `${this.apiModule.analyzeConfigDiff}/${configId}`;
    return this.get(endpoint);
  }

  /**
   * 获取差异摘要
   * @param {string} configId - 配置ID
   * @returns {Promise<any>}
   */
  async getDiffSummary(configId) {
    const endpoint = this.apiModule.getDiffSummary?.replace('{configId}', configId) ||
                    `${this.apiModule.getDiffSummary}/${configId}`;
    return this.get(endpoint);
  }

  // 统计信息

  /**
   * 获取配置统计信息
   * @returns {Promise<any>}
   */
  async getConfigStats() {
    return this.get(this.apiModule.getConfigStats);
  }

  /**
   * 获取同步统计信息
   * @returns {Promise<any>}
   */
  async getSyncStats() {
    return this.get(this.apiModule.getSyncStats);
  }

  /**
   * 获取验证统计信息
   * @returns {Promise<any>}
   */
  async getValidationStats() {
    return this.get(this.apiModule.getValidationStats);
  }
}

export default ConfigurationManagementService;
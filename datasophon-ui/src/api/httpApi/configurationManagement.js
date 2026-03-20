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

import paths from '@/api/baseUrl'

let path = paths.path() + '/ddh'

export default {
  // 配置列表查询
  listConfigurations: path + '/api/configurations/list', // 获取配置列表
  getConfigurationDetail: path + '/api/configurations/detail/{configId}', // 获取配置详情
  getConfigurationCompare: path + '/api/configurations/compare/{configId}', // 获取配置对比信息
  
  // 配置同步操作
  syncConfiguration: path + '/api/configurations/sync', // 同步配置
  batchSyncConfigurations: path + '/api/configurations/batch-sync', // 批量同步配置
  getSyncStatus: path + '/api/configurations/sync-status/{syncTaskId}', // 获取同步状态
  cancelSyncTask: path + '/api/configurations/cancel-sync/{syncTaskId}', // 取消同步任务
  
  // 配置历史管理
  getConfigHistory: path + '/api/configurations/history', // 获取配置历史
  getConfigVersionDetail: path + '/api/configurations/version-detail/{versionId}', // 获取配置版本详情
  rollbackToVersion: path + '/api/configurations/rollback', // 回滚到指定版本
  
  // 配置验证
  validateConfiguration: path + '/api/configurations/validate/{configId}', // 验证配置
  batchValidateConfigurations: path + '/api/configurations/batch-validate', // 批量验证配置
  
  // 配置导入导出
  exportConfiguration: path + '/api/configurations/export', // 导出配置
  importConfiguration: path + '/api/configurations/import', // 导入配置
  
  // 配置差异分析
  analyzeConfigDiff: path + '/api/configurations/analyze-diff/{configId}', // 分析配置差异
  getDiffSummary: path + '/api/configurations/diff-summary/{configId}', // 获取差异摘要
  
  // 统计信息
  getConfigStats: path + '/api/configurations/stats', // 获取配置统计信息
  getSyncStats: path + '/api/configurations/sync-stats', // 获取同步统计信息
  getValidationStats: path + '/api/configurations/validation-stats', // 获取验证统计信息
}
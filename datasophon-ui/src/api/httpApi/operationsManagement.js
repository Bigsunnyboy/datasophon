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
  // 服务列表和状态
  listServices: path + '/api/operations/services/list', // 获取服务列表
  getServiceDetail: path + '/api/operations/services/detail/{serviceId}', // 获取服务详情
  getServiceStatus: path + '/api/operations/services/status/{serviceId}', // 获取服务状态
  getStats: path + '/api/operations/services/stats', // 获取服务统计信息
  
  // 服务操作
  startService: path + '/api/operations/actions/start', // 启动服务
  stopService: path + '/api/operations/actions/stop', // 停止服务
  restartService: path + '/api/operations/actions/restart', // 重启服务
  reloadConfig: path + '/api/operations/actions/reload-config', // 重载配置
  changeTakeoverLevel: path + '/api/operations/actions/change-takeover-level', // 变更接管级别
  
  // 批量操作
  batchStartServices: path + '/api/operations/actions/batch-start', // 批量启动服务
  batchStopServices: path + '/api/operations/actions/batch-stop', // 批量停止服务
  batchRestartServices: path + '/api/operations/actions/batch-restart', // 批量重启服务
  batchReloadConfigs: path + '/api/operations/actions/batch-reload-configs', // 批量重载配置
  
  // 健康检查
  checkHealth: path + '/api/operations/health/check', // 健康检查
  batchHealthCheck: path + '/api/operations/health/batch-check', // 批量健康检查
  runHealthCheckAll: path + '/api/operations/health/run-all', // 运行全局健康检查
  getHealthCheckResult: path + '/api/operations/health/result/{checkId}', // 获取健康检查结果
  
  // 日志管理
  getLogs: path + '/api/operations/logs/get', // 获取日志
  downloadLogs: path + '/api/operations/logs/download', // 下载日志
  searchLogs: path + '/api/operations/logs/search', // 搜索日志
  clearLogs: path + '/api/operations/logs/clear', // 清空日志
  
  // 监控指标
  getMetrics: path + '/api/operations/metrics/get', // 获取监控指标
  getRealtimeMetrics: path + '/api/operations/metrics/realtime', // 获取实时指标
  getMetricHistory: path + '/api/operations/metrics/history', // 获取指标历史
  
  // 运维任务
  createOperationTask: path + '/api/operations/tasks/create', // 创建运维任务
  getTaskStatus: path + '/api/operations/tasks/status/{taskId}', // 获取任务状态
  cancelOperationTask: path + '/api/operations/tasks/cancel/{taskId}', // 取消运维任务
  getTaskHistory: path + '/api/operations/tasks/history', // 获取任务历史
  
  // 告警管理
  getServiceAlerts: path + '/api/operations/alerts/get', // 获取服务告警
  acknowledgeAlert: path + '/api/operations/alerts/acknowledge', // 确认告警
  clearAlert: path + '/api/operations/alerts/clear', // 清除告警
  
  // 维护模式
  enterMaintenanceMode: path + '/api/operations/maintenance/enter', // 进入维护模式
  exitMaintenanceMode: path + '/api/operations/maintenance/exit', // 退出维护模式
  getMaintenanceStatus: path + '/api/operations/maintenance/status/{serviceId}', // 获取维护状态
}
/*
/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
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
 *
 */



import paths from '@/api/baseUrl'

let path = paths.path() + '/ddh'

export default {
  // 组件发现任务管理
  createDiscoveryTask: path + '/api/component/discovery/create', // 创建发现任务
  startDiscoveryTask: path + '/api/component/discovery/start/{discoveryTaskId}', // 启动发现任务
  getDiscoveryProgress: path + '/api/component/discovery/progress/{discoveryTaskId}', // 获取发现任务进度
  getDiscoveryDetails: path + '/api/component/discovery/details/{discoveryTaskId}', // 获取发现任务详情
  cancelDiscoveryTask: path + '/api/component/discovery/cancel/{discoveryTaskId}', // 取消发现任务
  validateDiscoveryResult: path + '/api/component/discovery/validate/{discoveryTaskId}', // 验证发现结果
  autoRegisterDiscoveredComponents: path + '/api/component/discovery/auto-register/{discoveryTaskId}', // 自动注册发现的组件
  
  // 发现执行
  executeAutoDiscovery: path + '/api/component/discovery/execute-auto', // 执行自动发现
  executeManualDiscovery: path + '/api/component/discovery/execute-manual', // 执行手动发现
  getAvailableDiscoveryMethods: path + '/api/component/discovery/available-methods', // 获取可用的发现方法
  
  // 发现任务查询
  listDiscoveryByCluster: path + '/api/component/discovery/list-by-cluster/{clusterId}', // 按集群ID列出发现任务
  listDiscoveryByClusterAndService: path + '/api/component/discovery/list-by-cluster-service', // 按集群和服务列出发现任务
  listDiscoveryByStatus: path + '/api/component/discovery/list-by-status', // 按状态列出发现任务
  listRecentDiscoveries: path + '/api/component/discovery/recent', // 获取最近的发现任务
  getDiscoveryStats: path + '/api/component/discovery/stats', // 获取发现统计信息
  getDiscoveryInfo: path + '/api/component/discovery/info/{discoveryTaskId}', // 获取发现任务信息
  
  // 系统管理
  deleteExpiredDiscoveryResults: path + '/api/component/discovery/delete-expired', // 删除过期的发现结果
  updateDiscoveryStatus: path + '/api/component/discovery/update-status', // 更新发现任务状态（内部使用）
  
  // 集群相关
  getClusterList: path + '/api/cluster/list', // 获取集群列表
  getClusterInfo: path + '/api/cluster/info/{id}', // 获取集群信息
  getServiceList: path + '/api/frame/service/list', // 获取服务列表
  getAllHosts: path + '/api/cluster/host/all', // 获取集群所有主机
}
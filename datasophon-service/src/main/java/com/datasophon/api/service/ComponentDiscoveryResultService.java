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
import com.datasophon.dao.entity.ComponentDiscoveryResultEntity;
import com.datasophon.dao.enums.DiscoveryStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 组件发现结果表
 *
 * @author 
 * @email 
 * @date 
 */
public interface ComponentDiscoveryResultService extends IService<ComponentDiscoveryResultEntity> {
    
    /**
     * 创建组件发现任务
     */
    Result createDiscoveryTask(ComponentDiscoveryResultEntity discoveryTask);
    
    /**
     * 开始执行发现任务
     */
    Result startDiscoveryTask(String discoveryTaskId);
    
    /**
     * 更新发现任务状态
     */
    Result updateDiscoveryStatus(String discoveryTaskId, DiscoveryStatus status,
                                 String discoveryStats, String discoveryDetails, String errorMessage);
    
    /**
     * 根据集群ID查询发现结果
     */
    List<ComponentDiscoveryResultEntity> listByClusterId(Integer clusterId);
    
    /**
     * 根据集群ID和服务名称查询发现结果
     */
    List<ComponentDiscoveryResultEntity> listByClusterAndService(Integer clusterId, String serviceName);
    
    /**
     * 根据发现状态查询
     */
    List<ComponentDiscoveryResultEntity> listByStatus(Integer clusterId, DiscoveryStatus discoveryStatus);
    
    /**
     * 根据任务ID查询发现结果
     */
    ComponentDiscoveryResultEntity getByTaskId(String discoveryTaskId);
    
    /**
     * 查询最近N次的发现结果
     */
    List<ComponentDiscoveryResultEntity> listRecentDiscoveries(Integer clusterId, String serviceName, Integer limit);
    
    /**
     * 统计发现结果
     */
    Map<String, Object> getDiscoveryStats(Integer clusterId, Date startTime, Date endTime);
    
    /**
     * 执行自动组件发现
     */
    Result executeAutoDiscovery(Integer clusterId, String serviceName,
                                Map<String, Object> discoveryParams);
    
    /**
     * 执行手动组件发现
     */
    Result executeManualDiscovery(Integer clusterId, String serviceName,
                                  String discoveryTarget, String discoveryMethod,
                                  Map<String, Object> discoveryParams);
    
    /**
     * 获取发现任务进度
     */
    Result getDiscoveryProgress(String discoveryTaskId);
    
    /**
     * 取消发现任务
     */
    Result cancelDiscoveryTask(String discoveryTaskId);
    
    /**
     * 删除过期的发现结果
     */
    Result deleteExpiredDiscoveryResults(Integer daysToKeep);
    
    /**
     * 获取发现结果详情
     */
    Result getDiscoveryDetails(String discoveryTaskId);
    
    /**
     * 验证发现结果
     */
    Result validateDiscoveryResult(String discoveryTaskId);
    
    /**
     * 自动注册发现的组件
     */
    Result autoRegisterDiscoveredComponents(String discoveryTaskId);
    
    /**
     * 获取发现方法列表
     */
    List<Map<String, Object>> getAvailableDiscoveryMethods();
    
    /**
     * 获取发现任务列表（分页）
     */
    Result listTasks(Integer page, Integer pageSize, String taskName, String status, String componentType);
    
    /**
     * 启动发现任务（新接口）
     */
    Result startTask(String taskName, Integer clusterId, String discoveryStrategy,
                     List<String> targetHosts, Integer timeoutSeconds, Integer concurrentThreads);
    
    /**
     * 停止发现任务（新接口）
     */
    Result stopTask(String taskId);
    
    /**
     * 重试发现任务（新接口）
     */
    Result retryTask(String taskId);
    
    /**
     * 删除发现任务（新接口）
     */
    Result deleteTask(String taskId);
}
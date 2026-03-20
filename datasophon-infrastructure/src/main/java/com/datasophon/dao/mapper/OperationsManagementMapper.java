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

package com.datasophon.dao.mapper;

import com.datasophon.dao.entity.OperationsManagementEntity;
import com.datasophon.dao.enums.OperationStatus;
import com.datasophon.dao.enums.OperationType;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 运维管理Mapper接口
 * 
 * @author 
 * @email 
 * @date 
 */
@Mapper
public interface OperationsManagementMapper extends BaseMapper<OperationsManagementEntity> {
    
    /**
     * 分页查询运维操作记录
     */
    IPage<OperationsManagementEntity> listOperations(
                                                     IPage<OperationsManagementEntity> page,
                                                     @Param("keyword") String keyword,
                                                     @Param("clusterId") String clusterId,
                                                     @Param("operationType") String operationType,
                                                     @Param("operationStatus") String operationStatus,
                                                     @Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);
    
    /**
     * 根据集群ID查询运维操作
     */
    List<OperationsManagementEntity> listByClusterId(
                                                     @Param("clusterId") Integer clusterId);
    
    /**
     * 根据现有组件ID查询运维操作
     */
    List<OperationsManagementEntity> listByExistingComponentId(
                                                               @Param("existingComponentId") Integer existingComponentId);
    
    /**
     * 根据操作类型查询
     */
    List<OperationsManagementEntity> listByOperationType(
                                                         @Param("clusterId") Integer clusterId,
                                                         @Param("operationType") OperationType operationType);
    
    /**
     * 根据操作状态查询
     */
    List<OperationsManagementEntity> listByOperationStatus(
                                                           @Param("clusterId") Integer clusterId,
                                                           @Param("operationStatus") OperationStatus operationStatus);
    
    /**
     * 查询待执行的运维操作
     */
    List<OperationsManagementEntity> listPendingOperations(
                                                           @Param("clusterId") Integer clusterId);
    
    /**
     * 查询执行中的运维操作
     */
    List<OperationsManagementEntity> listRunningOperations(
                                                           @Param("clusterId") Integer clusterId);
    
    /**
     * 查询失败的运维操作
     */
    List<OperationsManagementEntity> listFailedOperations(
                                                          @Param("clusterId") Integer clusterId,
                                                          @Param("days") Integer days);
    
    /**
     * 更新操作状态
     */
    int updateOperationStatus(
                              @Param("id") Integer id,
                              @Param("operationStatus") OperationStatus operationStatus,
                              @Param("operationResult") String operationResult,
                              @Param("errorMessage") String errorMessage);
    
    /**
     * 更新操作结束时间和耗时
     */
    int updateOperationCompletion(
                                  @Param("id") Integer id,
                                  @Param("operationStatus") OperationStatus operationStatus,
                                  @Param("endTime") Date endTime,
                                  @Param("durationMs") Long durationMs);
    
    /**
     * 批量更新操作状态
     */
    int batchUpdateOperationStatus(
                                   @Param("ids") List<Integer> ids,
                                   @Param("operationStatus") OperationStatus operationStatus);
    
    /**
     * 获取运维操作统计信息
     */
    List<java.util.Map<String, Object>> getOperationStatsByCluster(
                                                                   @Param("clusterId") Integer clusterId);
    
    /**
     * 获取操作类型统计信息
     */
    List<java.util.Map<String, Object>> getOperationTypeStats(
                                                              @Param("clusterId") Integer clusterId,
                                                              @Param("days") Integer days);
    
    /**
     * 获取操作成功率统计
     */
    List<java.util.Map<String, Object>> getOperationSuccessRate(
                                                                @Param("clusterId") Integer clusterId,
                                                                @Param("startDate") Date startDate,
                                                                @Param("endDate") Date endDate);
    
    /**
     * 获取平均操作耗时统计
     */
    List<java.util.Map<String, Object>> getAvgOperationDuration(
                                                                @Param("clusterId") Integer clusterId,
                                                                @Param("operationType") String operationType);
}
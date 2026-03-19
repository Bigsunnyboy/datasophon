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

import com.datasophon.dao.entity.ComponentDiscoveryResultEntity;
import com.datasophon.dao.enums.DiscoveryStatus;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 组件发现结果表
 * 
 * @author 
 * @email 
 * @date 
 */
@Mapper
public interface ComponentDiscoveryResultMapper extends BaseMapper<ComponentDiscoveryResultEntity> {
    
    /**
     * 根据集群ID查询发现结果
     */
    List<ComponentDiscoveryResultEntity> listByClusterId(
                                                         @Param("clusterId") Integer clusterId);
    
    /**
     * 根据集群ID和服务名称查询发现结果
     */
    List<ComponentDiscoveryResultEntity> listByClusterAndService(
                                                                 @Param("clusterId") Integer clusterId,
                                                                 @Param("serviceName") String serviceName);
    
    /**
     * 根据发现状态查询
     */
    List<ComponentDiscoveryResultEntity> listByStatus(
                                                      @Param("clusterId") Integer clusterId,
                                                      @Param("discoveryStatus") DiscoveryStatus discoveryStatus);
    
    /**
     * 根据任务ID查询发现结果
     */
    ComponentDiscoveryResultEntity getByTaskId(
                                               @Param("discoveryTaskId") String discoveryTaskId);
    
    /**
     * 查询最近N次的发现结果
     */
    List<ComponentDiscoveryResultEntity> listRecentDiscoveries(
                                                               @Param("clusterId") Integer clusterId,
                                                               @Param("serviceName") String serviceName,
                                                               @Param("limit") Integer limit);
    
    /**
     * 统计发现结果
     */
    int countByClusterAndStatus(
                                @Param("clusterId") Integer clusterId,
                                @Param("discoveryStatus") DiscoveryStatus discoveryStatus);
    
    /**
     * 更新发现状态
     */
    int updateDiscoveryStatus(
                              @Param("id") Integer id,
                              @Param("discoveryStatus") DiscoveryStatus discoveryStatus,
                              @Param("discoveryEndTime") Date discoveryEndTime,
                              @Param("durationMs") Long durationMs,
                              @Param("errorMessage") String errorMessage);
    
    /**
     * 更新发现结果详情
     */
    int updateDiscoveryDetails(
                               @Param("id") Integer id,
                               @Param("discoveryStats") String discoveryStats,
                               @Param("discoveryDetails") String discoveryDetails,
                               @Param("registeredCount") Integer registeredCount);
    
    /**
     * 删除过期的发现结果（保留最近30天）
     */
    int deleteExpiredResults(@Param("daysToKeep") Integer daysToKeep);
    
    /**
     * 根据时间段查询发现结果
     */
    List<ComponentDiscoveryResultEntity> listByTimeRange(
                                                         @Param("clusterId") Integer clusterId,
                                                         @Param("startTime") Date startTime,
                                                         @Param("endTime") Date endTime);
}
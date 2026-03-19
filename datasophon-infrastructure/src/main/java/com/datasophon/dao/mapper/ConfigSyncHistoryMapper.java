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

import com.datasophon.dao.entity.ConfigSyncHistoryEntity;
import com.datasophon.dao.enums.SyncOperationType;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 配置同步历史表
 * 
 * @author 
 * @email 
 * @date 
 */
@Mapper
public interface ConfigSyncHistoryMapper extends BaseMapper<ConfigSyncHistoryEntity> {
    
    /**
     * 根据现有组件ID查询同步历史
     */
    List<ConfigSyncHistoryEntity> listByExistingComponentId(
                                                            @Param("existingComponentId") Integer existingComponentId);
    
    /**
     * 根据集群ID和服务名称查询同步历史
     */
    List<ConfigSyncHistoryEntity> listByClusterAndService(
                                                          @Param("clusterId") Integer clusterId,
                                                          @Param("serviceName") String serviceName);
    
    /**
     * 查询指定时间范围内的同步历史
     */
    List<ConfigSyncHistoryEntity> listByTimeRange(
                                                  @Param("clusterId") Integer clusterId,
                                                  @Param("startTime") Date startTime,
                                                  @Param("endTime") Date endTime);
    
    /**
     * 根据操作类型查询
     */
    List<ConfigSyncHistoryEntity> listByOperationType(
                                                      @Param("clusterId") Integer clusterId,
                                                      @Param("operationType") SyncOperationType operationType);
    
    /**
     * 获取最近一次成功的同步记录
     */
    ConfigSyncHistoryEntity getLastSuccessfulSync(
                                                  @Param("existingComponentId") Integer existingComponentId,
                                                  @Param("configFileName") String configFileName);
    
    /**
     * 标记为已回滚
     */
    int markAsRollbacked(
                         @Param("id") Integer id,
                         @Param("rollbackOperationId") Integer rollbackOperationId,
                         @Param("rollbackTime") Date rollbackTime);
}
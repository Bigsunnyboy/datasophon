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

import com.datasophon.dao.entity.ConfigurationManagementEntity;
import com.datasophon.dao.enums.ConfigStatus;
import com.datasophon.dao.enums.ConfigSyncStatus;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 配置管理Mapper接口
 * 
 * @author 
 * @email 
 * @date 
 */
@Mapper
public interface ConfigurationManagementMapper extends BaseMapper<ConfigurationManagementEntity> {
    
    /**
     * 分页查询配置列表
     */
    IPage<ConfigurationManagementEntity> listConfigurations(
                                                            IPage<ConfigurationManagementEntity> page,
                                                            @Param("keyword") String keyword,
                                                            @Param("clusterId") String clusterId,
                                                            @Param("configStatus") String configStatus);
    
    /**
     * 根据集群ID和服务名称查询配置
     */
    List<ConfigurationManagementEntity> listByClusterAndService(
                                                                @Param("clusterId") Integer clusterId,
                                                                @Param("serviceName") String serviceName);
    
    /**
     * 根据集群ID、服务名称和配置文件名查询配置
     */
    List<ConfigurationManagementEntity> listByClusterServiceAndFile(
                                                                    @Param("clusterId") Integer clusterId,
                                                                    @Param("serviceName") String serviceName,
                                                                    @Param("configFileName") String configFileName);
    
    /**
     * 根据现有组件ID查询配置
     */
    List<ConfigurationManagementEntity> listByExistingComponentId(
                                                                  @Param("existingComponentId") Integer existingComponentId);
    
    /**
     * 根据配置状态查询
     */
    List<ConfigurationManagementEntity> listByConfigStatus(
                                                           @Param("clusterId") Integer clusterId,
                                                           @Param("configStatus") ConfigStatus configStatus);
    
    /**
     * 根据同步状态查询
     */
    List<ConfigurationManagementEntity> listBySyncStatus(
                                                         @Param("clusterId") Integer clusterId,
                                                         @Param("syncStatus") ConfigSyncStatus syncStatus);
    
    /**
     * 查询需要同步的配置
     */
    List<ConfigurationManagementEntity> listConfigsNeedSync(
                                                            @Param("clusterId") Integer clusterId);
    
    /**
     * 查询配置冲突的配置项
     */
    List<ConfigurationManagementEntity> listConflictingConfigs(
                                                               @Param("clusterId") Integer clusterId);
    
    /**
     * 更新配置状态
     */
    int updateConfigStatus(
                           @Param("id") Integer id,
                           @Param("configStatus") ConfigStatus configStatus);
    
    /**
     * 更新同步状态
     */
    int updateSyncStatus(
                         @Param("id") Integer id,
                         @Param("syncStatus") ConfigSyncStatus syncStatus);
    
    /**
     * 批量更新配置状态
     */
    int batchUpdateConfigStatus(
                                @Param("ids") List<Integer> ids,
                                @Param("configStatus") ConfigStatus configStatus);
    
    /**
     * 批量更新同步状态
     */
    int batchUpdateSyncStatus(
                              @Param("ids") List<Integer> ids,
                              @Param("syncStatus") ConfigSyncStatus syncStatus);
    
    /**
     * 更新配置值
     */
    int updateConfigValue(
                          @Param("id") Integer id,
                          @Param("currentValue") String currentValue,
                          @Param("platformValue") String platformValue,
                          @Param("version") Integer version);
    
    /**
     * 获取配置统计信息
     */
    List<java.util.Map<String, Object>> getConfigStatsByCluster(
                                                                @Param("clusterId") Integer clusterId);
    
    /**
     * 获取同步统计信息
     */
    List<java.util.Map<String, Object>> getSyncStatsByCluster(
                                                              @Param("clusterId") Integer clusterId);
    
    /**
     * 获取验证统计信息
     */
    List<java.util.Map<String, Object>> getValidationStatsByCluster(
                                                                    @Param("clusterId") Integer clusterId);
}
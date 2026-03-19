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

import com.datasophon.dao.entity.ClusterExistingComponentEntity;
import com.datasophon.dao.enums.ExistingComponentState;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 集群现有组件注册表
 * 
 * @author 
 * @email 
 * @date 
 */
@Mapper
public interface ClusterExistingComponentMapper extends BaseMapper<ClusterExistingComponentEntity> {
    
    /**
     * 根据集群ID和服务名称查询现有组件
     */
    List<ClusterExistingComponentEntity> listByClusterAndService(
                                                                 @Param("clusterId") Integer clusterId,
                                                                 @Param("serviceName") String serviceName);
    
    /**
     * 根据集群ID、服务名称和主机名查询组件
     */
    ClusterExistingComponentEntity getByClusterServiceAndHost(
                                                              @Param("clusterId") Integer clusterId,
                                                              @Param("serviceName") String serviceName,
                                                              @Param("hostname") String hostname);
    
    /**
     * 根据组件状态查询
     */
    List<ClusterExistingComponentEntity> listByState(
                                                     @Param("clusterId") Integer clusterId,
                                                     @Param("componentState") ExistingComponentState componentState);
    
    /**
     * 更新组件状态
     */
    int updateComponentState(
                             @Param("id") Integer id,
                             @Param("componentState") ExistingComponentState componentState);
    
    /**
     * 批量更新组件状态
     */
    int batchUpdateComponentState(
                                  @Param("ids") List<Integer> ids,
                                  @Param("componentState") ExistingComponentState componentState);
}
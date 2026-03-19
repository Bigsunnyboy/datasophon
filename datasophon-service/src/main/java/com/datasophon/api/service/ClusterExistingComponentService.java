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
import com.datasophon.dao.entity.ClusterExistingComponentEntity;
import com.datasophon.dao.enums.ExistingComponentState;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 集群现有组件注册表
 *
 * @author 
 * @email 
 * @date 
 */
public interface ClusterExistingComponentService extends IService<ClusterExistingComponentEntity> {
    
    /**
     * 注册现有组件
     */
    Result registerExistingComponent(ClusterExistingComponentEntity component);
    
    /**
     * 验证现有组件
     */
    Result validateExistingComponent(Integer componentId);
    
    /**
     * 根据集群ID和服务名称查询现有组件
     */
    List<ClusterExistingComponentEntity> listByClusterAndService(Integer clusterId, String serviceName);
    
    /**
     * 根据集群ID、服务名称和主机名查询组件
     */
    ClusterExistingComponentEntity getByClusterServiceAndHost(Integer clusterId, String serviceName, String hostname);
    
    /**
     * 根据组件状态查询
     */
    List<ClusterExistingComponentEntity> listByState(Integer clusterId, ExistingComponentState componentState);
    
    /**
     * 更新组件状态
     */
    Result updateComponentState(Integer componentId, ExistingComponentState componentState);
    
    /**
     * 批量更新组件状态
     */
    Result batchUpdateComponentState(List<Integer> componentIds, ExistingComponentState componentState);
    
    /**
     * 获取组件健康状态
     */
    Result checkComponentHealth(Integer componentId);
    
    /**
     * 同步组件配置
     */
    Result syncComponentConfig(Integer componentId, String syncDirection);
    
    /**
     * 发现集群中的现有组件
     */
    Result discoverExistingComponents(Integer clusterId, String serviceName, Map<String, Object> discoveryParams);
    
    /**
     * 获取可接管的组件列表
     */
    List<ClusterExistingComponentEntity> listTakeoverCandidates(Integer clusterId);
    
    /**
     * 统计组件信息
     */
    Map<String, Object> getComponentStats(Integer clusterId);
}
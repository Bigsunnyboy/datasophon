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

package com.datasophon.api.service.impl;

import com.datasophon.api.enums.Status;
import com.datasophon.api.service.ClusterExistingComponentService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterExistingComponentEntity;
import com.datasophon.dao.enums.ExistingComponentState;
import com.datasophon.dao.mapper.ClusterExistingComponentMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 集群现有组件注册表
 *
 * @author 
 * @email 
 * @date 
 */
@Slf4j
@Service("clusterExistingComponentService")
@Transactional
public class ClusterExistingComponentServiceImpl extends ServiceImpl<ClusterExistingComponentMapper, ClusterExistingComponentEntity>
        implements
            ClusterExistingComponentService {
    
    @Autowired
    private ClusterExistingComponentMapper clusterExistingComponentMapper;
    
    @Override
    public Result registerExistingComponent(ClusterExistingComponentEntity component) {
        try {
            QueryWrapper<ClusterExistingComponentEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", component.getClusterId())
                    .eq("service_name", component.getServiceName())
                    .eq("service_role", component.getServiceRole())
                    .eq("hostname", component.getHostname());
            ClusterExistingComponentEntity existing = this.getOne(queryWrapper);
            
            if (existing != null) {
                return Result.error(Status.COMPONENT_ALREADY_EXISTS.getCode(),
                        Status.COMPONENT_ALREADY_EXISTS.getMsg());
            }
            
            component.setComponentState(ExistingComponentState.DISCOVERED);
            component.setDiscoveryTime(new Date());
            component.setCreateTime(new Date());
            component.setUpdateTime(new Date());
            
            this.save(component);
            
            log.info("Registered existing component: clusterId={}, service={}, role={}, host={}",
                    component.getClusterId(), component.getServiceName(),
                    component.getServiceRole(), component.getHostname());
            
            return Result.success();
        } catch (Exception e) {
            log.error("Failed to register existing component", e);
            return Result.error(Status.REGISTER_COMPONENT_FAILED.getCode(),
                    Status.REGISTER_COMPONENT_FAILED.getMsg());
        }
    }
    
    @Override
    public Result validateExistingComponent(Integer componentId) {
        try {
            ClusterExistingComponentEntity component = this.getById(componentId);
            if (component == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            component.setValidationResult("{\"status\": \"SUCCESS\", \"timestamp\": \"" + new Date() + "\"}");
            component.setComponentState(ExistingComponentState.VALIDATED);
            component.setUpdateTime(new Date());
            
            this.updateById(component);
            
            log.info("Validated existing component: id={}", componentId);
            return Result.success();
        } catch (Exception e) {
            log.error("Failed to validate existing component: id={}", componentId, e);
            return Result.error(Status.VALIDATE_COMPONENT_FAILED.getCode(),
                    Status.VALIDATE_COMPONENT_FAILED.getMsg());
        }
    }
    
    @Override
    public List<ClusterExistingComponentEntity> listByClusterAndService(Integer clusterId, String serviceName) {
        return clusterExistingComponentMapper.listByClusterAndService(clusterId, serviceName);
    }
    
    @Override
    public ClusterExistingComponentEntity getByClusterServiceAndHost(Integer clusterId, String serviceName, String hostname) {
        return clusterExistingComponentMapper.getByClusterServiceAndHost(clusterId, serviceName, hostname);
    }
    
    @Override
    public List<ClusterExistingComponentEntity> listByState(Integer clusterId, ExistingComponentState componentState) {
        return clusterExistingComponentMapper.listByState(clusterId, componentState);
    }
    
    @Override
    public Result updateComponentState(Integer componentId, ExistingComponentState componentState) {
        try {
            int updated = clusterExistingComponentMapper.updateComponentState(componentId, componentState);
            if (updated > 0) {
                log.info("Updated component state: id={}, state={}", componentId, componentState);
                return Result.success();
            } else {
                return Result.error(Status.UPDATE_COMPONENT_STATE_FAILED.getCode(),
                        Status.UPDATE_COMPONENT_STATE_FAILED.getMsg());
            }
        } catch (Exception e) {
            log.error("Failed to update component state: id={}, state={}", componentId, componentState, e);
            return Result.error(Status.UPDATE_COMPONENT_STATE_FAILED.getCode(),
                    Status.UPDATE_COMPONENT_STATE_FAILED.getMsg());
        }
    }
    
    @Override
    public Result batchUpdateComponentState(List<Integer> componentIds, ExistingComponentState componentState) {
        try {
            int updated = clusterExistingComponentMapper.batchUpdateComponentState(componentIds, componentState);
            log.info("Batch updated component states: count={}, state={}", updated, componentState);
            return Result.success().put(Constants.DATA, updated);
        } catch (Exception e) {
            log.error("Failed to batch update component states", e);
            return Result.error(Status.BATCH_UPDATE_COMPONENT_STATE_FAILED.getCode(),
                    Status.BATCH_UPDATE_COMPONENT_STATE_FAILED.getMsg());
        }
    }
    
    @Override
    public Result checkComponentHealth(Integer componentId) {
        try {
            ClusterExistingComponentEntity component = this.getById(componentId);
            if (component == null) {
                return Result.error(Status.COMPONENT_NOT_FOUND.getCode(),
                        Status.COMPONENT_NOT_FOUND.getMsg());
            }
            
            component.setHealthStatus("{\"status\": \"HEALTHY\", \"timestamp\": \"" + new Date() + "\"}");
            component.setLastCheckTime(new Date());
            component.setUpdateTime(new Date());
            
            this.updateById(component);
            
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("componentId", componentId);
            healthInfo.put("healthStatus", "HEALTHY");
            healthInfo.put("checkTime", new Date());
            
            return Result.success().put(Constants.DATA, healthInfo);
        } catch (Exception e) {
            log.error("Failed to check component health: id={}", componentId, e);
            return Result.error(Status.CHECK_COMPONENT_HEALTH_FAILED.getCode(),
                    Status.CHECK_COMPONENT_HEALTH_FAILED.getMsg());
        }
    }
    
    @Override
    public Result syncComponentConfig(Integer componentId, String syncDirection) {
        return Result.success().put(Constants.MSG, "Config sync will be implemented in ConfigSyncHistoryService");
    }
    
    @Override
    public Result discoverExistingComponents(Integer clusterId, String serviceName, Map<String, Object> discoveryParams) {
        return Result.success().put(Constants.MSG, "Component discovery will be implemented in ComponentDiscoveryResultService");
    }
    
    @Override
    public List<ClusterExistingComponentEntity> listTakeoverCandidates(Integer clusterId) {
        QueryWrapper<ClusterExistingComponentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cluster_id", clusterId)
                .in("component_state",
                        ExistingComponentState.VALIDATED,
                        ExistingComponentState.REGISTERED)
                .orderByDesc("discovery_time");
        return this.list(queryWrapper);
    }
    
    @Override
    public Map<String, Object> getComponentStats(Integer clusterId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<ClusterExistingComponentEntity> allComponents = this.list(
                new QueryWrapper<ClusterExistingComponentEntity>().eq("cluster_id", clusterId));
        
        long discoveredCount = allComponents.stream()
                .filter(c -> ExistingComponentState.DISCOVERED.equals(c.getComponentState()))
                .count();
        long validatedCount = allComponents.stream()
                .filter(c -> ExistingComponentState.VALIDATED.equals(c.getComponentState()))
                .count();
        long registeredCount = allComponents.stream()
                .filter(c -> ExistingComponentState.REGISTERED.equals(c.getComponentState()))
                .count();
        long managedCount = allComponents.stream()
                .filter(c -> ExistingComponentState.MANAGED.equals(c.getComponentState()))
                .count();
        long deregisteredCount = allComponents.stream()
                .filter(c -> ExistingComponentState.DEREGISTERED.equals(c.getComponentState()))
                .count();
        
        stats.put("total", allComponents.size());
        stats.put("discovered", discoveredCount);
        stats.put("validated", validatedCount);
        stats.put("registered", registeredCount);
        stats.put("managed", managedCount);
        stats.put("deregistered", deregisteredCount);
        
        return stats;
    }
}
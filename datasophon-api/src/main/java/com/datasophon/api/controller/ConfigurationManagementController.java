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

package com.datasophon.api.controller;

import com.datasophon.api.security.UserPermission;
import com.datasophon.api.service.ConfigurationManagementService;
import com.datasophon.common.utils.Result;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置管理控制器
 * 提供对已接管组件配置的同步、对比、历史管理等功能
 */
@RestController
@RequestMapping("api/configurations")
public class ConfigurationManagementController {
    
    @Autowired
    private ConfigurationManagementService configurationManagementService;
    
    /**
     * 获取配置列表
     */
    @RequestMapping("/list")
    @UserPermission
    public Result listConfigurations(@RequestBody Map<String, Object> params) {
        Integer page = (Integer) params.get("page");
        Integer pageSize = (Integer) params.get("pageSize");
        String keyword = (String) params.get("keyword");
        String clusterId = (String) params.get("clusterId");
        String configStatus = (String) params.get("configStatus");
        return configurationManagementService.listConfigurations(page, pageSize, keyword, clusterId, configStatus);
    }
    
    /**
     * 获取配置详情
     */
    @RequestMapping("/detail/{configId}")
    @UserPermission
    public Result getConfigurationDetail(@PathVariable("configId") String configId) {
        return configurationManagementService.getConfigurationDetail(configId);
    }
    
    /**
     * 获取配置对比信息
     */
    @RequestMapping("/compare/{configId}")
    @UserPermission
    public Result getConfigurationCompare(@PathVariable("configId") String configId) {
        return configurationManagementService.getConfigurationCompare(configId);
    }
    
    /**
     * 同步配置
     */
    @RequestMapping("/sync")
    @UserPermission
    public Result syncConfiguration(@RequestBody Map<String, Object> params) {
        String configId = (String) params.get("configId");
        String syncDirection = (String) params.get("syncDirection");
        return configurationManagementService.syncConfiguration(configId, syncDirection);
    }
    
    /**
     * 批量同步配置
     */
    @RequestMapping("/batch-sync")
    @UserPermission
    public Result batchSyncConfigurations(@RequestBody Map<String, Object> params) {
        List<String> configIds = (List<String>) params.get("configIds");
        String syncDirection = (String) params.get("syncDirection");
        return configurationManagementService.batchSyncConfigurations(configIds, syncDirection);
    }
    
    /**
     * 获取同步状态
     */
    @RequestMapping("/sync-status/{syncTaskId}")
    @UserPermission
    public Result getSyncStatus(@PathVariable("syncTaskId") String syncTaskId) {
        return configurationManagementService.getSyncStatus(syncTaskId);
    }
    
    /**
     * 取消同步任务
     */
    @RequestMapping("/cancel-sync/{syncTaskId}")
    @UserPermission
    public Result cancelSyncTask(@PathVariable("syncTaskId") String syncTaskId) {
        return configurationManagementService.cancelSyncTask(syncTaskId);
    }
    
    /**
     * 获取配置历史
     */
    @RequestMapping("/history")
    @UserPermission
    public Result getConfigHistory(@RequestBody Map<String, Object> params) {
        String configId = (String) params.get("configId");
        Integer page = (Integer) params.get("page");
        Integer pageSize = (Integer) params.get("pageSize");
        return configurationManagementService.getConfigHistory(configId, page, pageSize);
    }
    
    /**
     * 获取配置版本详情
     */
    @RequestMapping("/version-detail/{versionId}")
    @UserPermission
    public Result getConfigVersionDetail(@PathVariable("versionId") String versionId) {
        return configurationManagementService.getConfigVersionDetail(versionId);
    }
    
    /**
     * 回滚到指定版本
     */
    @RequestMapping("/rollback")
    @UserPermission
    public Result rollbackToVersion(@RequestBody Map<String, Object> params) {
        String configId = (String) params.get("configId");
        String versionId = (String) params.get("versionId");
        return configurationManagementService.rollbackToVersion(configId, versionId);
    }
    
    /**
     * 验证配置
     */
    @RequestMapping("/validate/{configId}")
    @UserPermission
    public Result validateConfiguration(@PathVariable("configId") String configId) {
        return configurationManagementService.validateConfiguration(configId);
    }
    
    /**
     * 批量验证配置
     */
    @RequestMapping("/batch-validate")
    @UserPermission
    public Result batchValidateConfigurations(@RequestBody Map<String, Object> params) {
        List<String> configIds = (List<String>) params.get("configIds");
        return configurationManagementService.batchValidateConfigurations(configIds);
    }
    
    /**
     * 导出配置
     */
    @RequestMapping("/export")
    @UserPermission
    public Result exportConfiguration(@RequestBody Map<String, Object> params) {
        String configId = (String) params.get("configId");
        String format = (String) params.get("format");
        return configurationManagementService.exportConfiguration(configId, format);
    }
    
    /**
     * 导入配置
     */
    @RequestMapping("/import")
    @UserPermission
    public Result importConfiguration(@RequestBody Map<String, Object> params) {
        String configId = (String) params.get("configId");
        String configData = (String) params.get("configData");
        String format = (String) params.get("format");
        return configurationManagementService.importConfiguration(configId, configData, format);
    }
    
    /**
     * 分析配置差异
     */
    @RequestMapping("/analyze-diff/{configId}")
    @UserPermission
    public Result analyzeConfigDiff(@PathVariable("configId") String configId) {
        return configurationManagementService.analyzeConfigDiff(configId);
    }
    
    /**
     * 获取差异摘要
     */
    @RequestMapping("/diff-summary/{configId}")
    @UserPermission
    public Result getDiffSummary(@PathVariable("configId") String configId) {
        return configurationManagementService.getDiffSummary(configId);
    }
    
    /**
     * 获取配置统计信息
     */
    @RequestMapping("/stats")
    @UserPermission
    public Result getConfigStats() {
        return configurationManagementService.getConfigStats();
    }
    
    /**
     * 获取同步统计信息
     */
    @RequestMapping("/sync-stats")
    @UserPermission
    public Result getSyncStats() {
        return configurationManagementService.getSyncStats();
    }
    
    /**
     * 获取验证统计信息
     */
    @RequestMapping("/validation-stats")
    @UserPermission
    public Result getValidationStats() {
        return configurationManagementService.getValidationStats();
    }
}
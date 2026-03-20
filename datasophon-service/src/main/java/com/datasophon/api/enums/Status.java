/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.datasophon.api.enums;

import com.alibaba.fastjson.JSONObject;

/**
 * status enum
 */
public enum Status {
    
    SUCCESS(200, "success", "成功"),
    
    INTERNAL_SERVER_ERROR_ARGS(10000, "Internal Server Error: {0}", "服务端异常: {0}"),
    
    USER_NAME_EXIST(10003, "user name already exists", "用户名已存在"),
    USER_NAME_NULL(10004, "user name is null", "用户名不能为空"),
    USER_NOT_EXIST(10010, "user {0} not exists", "用户[{0}]不存在"),
    USER_NAME_PASSWD_ERROR(10013, "user name or password error", "用户名或密码错误"),
    LOGIN_SESSION_FAILED(10014, "create session failed!", "创建session失败"),
    REQUEST_PARAMS_NOT_VALID_ERROR(10101, "request parameter {0} is not valid", "请求参数[{0}]无效"),
    CREATE_USER_ERROR(10090, "create user error", "创建用户错误"),
    QUERY_USER_LIST_PAGING_ERROR(10091, "query user list paging error", "分页查询用户列表错误"),
    UPDATE_USER_ERROR(10092, "update user error", "更新用户错误"),
    LOGIN_SUCCESS(10042, "login success", "登录成功"),
    IP_IS_EMPTY(10125, "ip is empty", "IP地址不能为空"),
    DELETE_USER_BY_ID_ERROR(10093, "delete user by id error", "删除用户错误"),
    
    START_CHECK_HOST(10000, "start check host", "开始主机校验"),
    CHECK_HOST_SUCCESS(10001, "check host success", "主机校验成功"),
    NEED_JAVA_ENVIRONMENT(10002, "need java environment", "缺少Java环境"),
    CONNECTION_FAILED(10003, "connection failed", "主机连接失败"),
    NEED_HOSTNAME(10004, "need hostname", "无法获取主机名"),
    CAN_NOT_GET_IP(10005, "can not get ip", "无法获取ip地址"),
    INSTALL_SERVICE(10006, "Install Service ", "安装服务"),
    
    CLUSTER_CODE_EXISTS(10007, "cluster code exists", "集群编码已存在"),
    ALERT_GROUP_TIPS_ONE(10008,
            "an alarm group has been bound to an alarm indicator, delete the bound alarm indicator first",
            "当前告警组已绑定告警指标，请先删除绑定的告警指标"),
    GROUP_NAME_DUPLICATION(10009, "group name duplication, delete the bound alarm indicator first", "重复组名"),
    USER_GROUP_TIPS_ONE(10011, "the current user group has users,delete the users first", "当前用户组存在用户，请先删除用户"),
    HOST_EXIT_ONE_RUNNING_ROLE(10012, "at least one role is running on the host:", "主机存在正在运行的角色:"),
    REPEAT_NODE_LABEL(10015, "repeat node label", "重复节点标签"),
    ADD_YARN_NODE_LABEL_FAILED(10016, "add yarn node label failed", "添加yarn节点标签失败"),
    NODE_LABEL_IS_USING(10017, "node label is using", "节点标签正在使用"),
    CONFIG_CAPACITY_SCHEDULER_FAILED(10018, "config capacity-scheduler.xml failed", "配置capacity-scheduler.xml失败"),
    FAILED_REFRESH_THE_QUEUE_TO_YARN(10019, "description Failed to refresh the queue to Yarn", "刷新队列到Yarn失败"),
    RACK_IS_USING(10020, "rack is using", "机架正在使用"),
    NO_SERVICE_EXECUTE(10021, "there is no service to execute", "没有要执行的服务"),
    EXIT_RUNNING_ROLE_INSTANCE(10022, "It has running role instance,stop it first", "它有正在运行的角色实例，请先停止它"),
    REPEAT_ROLE_GROUP_NAME(10023, "repeat role group name", "重复角色组名称"),
    THE_CURRENT_ROLE_GROUP_BE_USING(10024, "the current role group is in use,do not delete it", "当前角色组正在使用，请勿删除"),
    EXIT_RUNNING_INSTANCES(10025, "there are running instances and ignore it when delete", "存在正在运行的实例，删除时忽略它"),
    ROLE_GROUP_HAS_NO_OUTDATED_SERVICE(10026, "this role group has no outdated service", "该角色组没有过时服务"),
    DUPLICATE_USER_NAME(10027, "duplicate user name", "用户名重复"),
    QUEUE_NAME_ALREADY_EXISTS(10028, "the queue name already exists", "队列名已存在"),
    THREE_JOURNALNODE_DEPLOYMENTS_REQUIRED(10030, "three JournalNode deployments are required", "JournalNode需要部署三台"),
    TWO_NAMENODES_NEED_TO_BE_DEPLOYED(10031, "two Namenodes need to be deployed", "NameNode需要部署两台"),
    TWO_ZKFC_DEVICES_ARE_REQUIRED(10032, "two ZKFC devices are required", "ZKFC需要部署两台"),
    TWO_RESOURCEMANAGER_ARE_DEPLOYED(10033, "two ResourceManager are deployed", "ResourceManager需要部署两台"),
    SELECT_LEAST_ONE_HOST(10034, "select at least one host", "至少选择一台主机"),
    BASIC_SERVICE_SELECT_MOST_ONE_HOST(10035, "AlertManager/Grafana/Prometheus must at the same host",
            "AlertManager/Grafana/Prometheus必须在同一台主机"),
    ODD_NUMBER_ARE_REQUIRED_FOR_ZKSERVER(10036, "The Number of ZkServer must be an odd number.", "ZkServer个数必须是奇数"),
    REMOVE_YARN_NODE_LABEL_FAILED(10037, "remove yarn node label failed.", "删除yarn节点标签失败"),
    ASSIGN_YARN_NODE_LABEL_FAILED(10037, "assign yarn node label failed.", "分配yarn节点标签失败"),
    USER_NO_OPERATION_PERM(30001, "user has no operation privilege", "当前用户没有操作权限"),
    THE_CURRENT_ROLE_GROUP_IS_DEFAULT(10038, "the current role group is default role group ,please do not delete it",
            "当前角色组是默认角色组，请勿删除"),
    NEED_SAME_ROLE_GROUP(10039,
            "All instances of the same service on the same machine need to be within the same role group",
            "同一个服务在同一台机器上的所有实例需要在同一个角色组内"),
    ODD_NUMBER_ARE_REQUIRED_FOR_DORISFE(10040, "The Number of DorisFE must be an odd number.", "DorisFE个数必须是奇数"),
    NO_SERVICE_ROLE_SELECTED(10041, "No service role selected", "未选择需要安装的服务实例"),
    TWO_KYUUBISERVERS_NEED_TO_BE_DEPLOYED(10042, "two kyuubiServer deployments are required", "KyuubiServer需要两个节点"),
    HOST_EXIT_ONE_INSTALLED_ROLE(10043, "at least one role is installed on the host:", "主机上存在未删除的角色:"),
    
    // Existing component management error codes
    COMPONENT_ALREADY_EXISTS(20001, "Component already exists", "组件已存在"),
    COMPONENT_NOT_FOUND(20002, "Component not found", "组件未找到"),
    REGISTER_COMPONENT_FAILED(20003, "Failed to register component", "注册组件失败"),
    VALIDATE_COMPONENT_FAILED(20004, "Failed to validate component", "验证组件失败"),
    UPDATE_COMPONENT_STATE_FAILED(20005, "Failed to update component state", "更新组件状态失败"),
    BATCH_UPDATE_COMPONENT_STATE_FAILED(20006, "Failed to batch update component state", "批量更新组件状态失败"),
    CHECK_COMPONENT_HEALTH_FAILED(20007, "Failed to check component health", "检查组件健康状态失败"),
    DISCOVERY_TASK_FAILED(20008, "Failed to execute discovery task", "执行发现任务失败"),
    SYNC_CONFIG_FAILED(20009, "Failed to sync configuration", "同步配置失败"),
    CONFIG_ALREADY_ROLLBACKED(20010, "Config already rollbacked", "配置已回滚，无法再次回滚"),
    ROLLBACK_FAILED(20011, "Rollback failed", "回滚操作失败"),
    MARK_ROLLBACK_FAILED(20012, "Mark rollback failed", "标记回滚失败"),
    DELETE_EXPIRED_HISTORY_FAILED(20013, "Delete expired history failed", "删除过期历史记录失败"),
    ANALYZE_CONFIG_DIFF_FAILED(20014, "Analyze config diff failed", "配置差异分析失败"),
    VALIDATE_SYNC_RESULT_FAILED(20015, "Validate sync result failed", "同步结果验证失败"),
    GET_DISCOVERY_PROGRESS_FAILED(20016, "Get discovery progress failed", "获取发现任务进度失败"),
    DISCOVERY_ALREADY_COMPLETED(20017, "Discovery already completed", "发现任务已完成或已失败，无法取消"),
    CANCEL_DISCOVERY_TASK_FAILED(20018, "Cancel discovery task failed", "取消发现任务失败"),
    DELETE_EXPIRED_DISCOVERY_RESULTS_FAILED(20019, "Delete expired discovery results failed", "删除过期发现结果失败"),
    GET_DISCOVERY_DETAILS_FAILED(20020, "Get discovery details failed", "获取发现结果详情失败"),
    VALIDATE_DISCOVERY_RESULT_FAILED(20021, "Validate discovery result failed", "验证发现结果失败"),
    DISCOVERY_NOT_COMPLETED(20022, "Discovery not completed", "发现任务未完成，无法自动注册"),
    AUTO_REGISTER_DISABLED(20023, "Auto register disabled", "自动注册未启用"),
    NO_DISCOVERY_DETAILS(20024, "No discovery details", "没有发现详情可供注册"),
    AUTO_REGISTER_DISCOVERED_COMPONENTS_FAILED(20025, "Auto register discovered components failed", "自动注册发现的组件失败"),
    
    // Component discovery task management errors
    LIST_DISCOVERY_TASKS_FAILED(20026, "List discovery tasks failed", "获取发现任务列表失败"),
    START_DISCOVERY_TASK_FAILED(20027, "Start discovery task failed", "启动发现任务失败"),
    STOP_DISCOVERY_TASK_FAILED(20028, "Stop discovery task failed", "停止发现任务失败"),
    RETRY_DISCOVERY_TASK_FAILED(20029, "Retry discovery task failed", "重试发现任务失败"),
    DELETE_DISCOVERY_TASK_FAILED(20030, "Delete discovery task failed", "删除发现任务失败"),
    DISCOVERY_TASK_NOT_FOUND(20031, "Discovery task not found", "发现任务不存在"),
    DISCOVERY_TASK_NOT_RUNNING(20032, "Discovery task not running", "发现任务未在运行"),
    DISCOVERY_TASK_NOT_FAILED(20033, "Discovery task not failed", "发现任务未失败"),
    
    // Operations management errors
    LIST_SERVICES_FAILED(20034, "List services failed", "获取服务列表失败"),
    GET_SERVICE_DETAIL_FAILED(20035, "Get service detail failed", "获取服务详情失败"),
    SERVICE_NOT_FOUND(20036, "Service not found", "服务不存在"),
    SERVICE_OPERATION_FAILED(20037, "Service operation failed", "服务操作失败"),
    START_SERVICE_FAILED(20038, "Start service failed", "启动服务失败"),
    STOP_SERVICE_FAILED(20039, "Stop service failed", "停止服务失败"),
    RESTART_SERVICE_FAILED(20040, "Restart service failed", "重启服务失败"),
    RELOAD_CONFIG_FAILED(20041, "Reload config failed", "重载配置失败"),
    HEALTH_CHECK_FAILED(20042, "Health check failed", "健康检查失败"),
    GET_LOGS_FAILED(20043, "Get logs failed", "获取日志失败"),
    DOWNLOAD_LOGS_FAILED(20044, "Download logs failed", "下载日志失败"),
    GET_METRICS_FAILED(20045, "Get metrics failed", "获取指标失败"),
    CREATE_TASK_FAILED(20046, "Create task failed", "创建任务失败"),
    GET_TASK_STATUS_FAILED(20047, "Get task status failed", "获取任务状态失败"),
    CANCEL_TASK_FAILED(20048, "Cancel task failed", "取消任务失败"),
    GET_ALERTS_FAILED(20049, "Get alerts failed", "获取告警失败"),
    ACKNOWLEDGE_ALERT_FAILED(20050, "Acknowledge alert failed", "确认告警失败"),
    CLEAR_ALERT_FAILED(20051, "Clear alert failed", "清除告警失败"),
    ENTER_MAINTENANCE_FAILED(20052, "Enter maintenance failed", "进入维护模式失败"),
    EXIT_MAINTENANCE_FAILED(20053, "Exit maintenance failed", "退出维护模式失败"),
    CHANGE_TAKEOVER_LEVEL_FAILED(20054, "Change takeover level failed", "变更接管级别失败"),
    
    // Configuration management errors
    LIST_CONFIGURATIONS_FAILED(20055, "List configurations failed", "获取配置列表失败"),
    GET_CONFIGURATION_DETAIL_FAILED(20056, "Get configuration detail failed", "获取配置详情失败"),
    GET_CONFIG_COMPARE_FAILED(20057, "Get config compare failed", "获取配置对比失败"),
    SYNC_CONFIGURATION_FAILED(20058, "Sync configuration failed", "同步配置失败"),
    BATCH_SYNC_CONFIGURATIONS_FAILED(20059, "Batch sync configurations failed", "批量同步配置失败"),
    GET_CONFIG_HISTORY_FAILED(20060, "Get config history failed", "获取配置历史失败"),
    GET_CONFIG_VERSION_FAILED(20061, "Get config version failed", "获取配置版本失败"),
    ROLLBACK_TO_VERSION_FAILED(20062, "Rollback to version failed", "回滚到版本失败"),
    VALIDATE_CONFIGURATION_FAILED(20063, "Validate configuration failed", "验证配置失败"),
    EXPORT_CONFIGURATION_FAILED(20064, "Export configuration failed", "导出配置失败"),
    IMPORT_CONFIGURATION_FAILED(20065, "Import configuration failed", "导入配置失败"),
    GET_DIFF_SUMMARY_FAILED(20066, "Get diff summary failed", "获取差异摘要失败"),
    GET_CONFIG_STATS_FAILED(20067, "Get config stats failed", "获取配置统计失败"),
    GET_SYNC_STATS_FAILED(20068, "Get sync stats failed", "获取同步统计失败"),
    GET_VALIDATION_STATS_FAILED(20069, "Get validation stats failed", "获取验证统计失败"),
    ;
    
    private final int code;
    private final String enMsg;
    private final String zhMsg;
    
    Status(int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }
    
    public int getCode() {
        return this.code;
    }
    
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("code", this.code);
        json.put("msg", getMsg());
        return json;
    }
    
    public String getMsg() {
        return this.zhMsg;
    }
}

-- ============================================================
-- DataSophon 1.3 DDL - Unified Existing Component Management System
-- 合并版本：包含1.3.0, 1.3.1, 1.3.2, 1.3.3所有功能
-- ============================================================

-- ------------------------------------------------------------
-- 表: t_ddh_cluster_existing_component
-- 集群现有组件注册表
-- 记录已发现的现有大数据组件信息
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_ddh_cluster_existing_component` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cluster_id` int(11) NOT NULL COMMENT '集群ID',
  `service_name` varchar(128) NOT NULL COMMENT '服务名称 (如: HDFS, YARN, SPARK)',
  `component_name` varchar(128) NOT NULL COMMENT '组件名称 (如: NameNode, ResourceManager)',
  `hostname` varchar(256) NOT NULL COMMENT '主机名',
  `ip_address` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `component_state` varchar(32) NOT NULL COMMENT '组件状态 (DISCOVERED, VALIDATED, REGISTERED, MANAGED, SYNCED, ERROR)',
  `detection_method` varchar(64) DEFAULT NULL COMMENT '发现方法 (PORT_SCAN, PROCESS_DETECTION, CONFIG_PARSING, API_QUERY, CUSTOM_SCRIPT)',
  `detection_time` datetime DEFAULT NULL COMMENT '发现时间',
  `validation_status` varchar(32) DEFAULT NULL COMMENT '验证状态 (PENDING, SUCCESS, FAILED)',
  `validation_time` datetime DEFAULT NULL COMMENT '验证时间',
  `validation_details` text COMMENT '验证详情 (JSON格式)',
  `service_port` int(11) DEFAULT NULL COMMENT '服务端口',
  `web_port` int(11) DEFAULT NULL COMMENT 'Web UI端口',
  `component_version` varchar(64) DEFAULT NULL COMMENT '组件版本',
  `config_path` varchar(512) DEFAULT NULL COMMENT '配置文件路径',
  `config_snapshot` longtext COMMENT '配置快照 (JSON格式)',
  `health_status` varchar(32) DEFAULT NULL COMMENT '健康状态 (HEALTHY, UNHEALTHY, UNKNOWN)',
  `last_health_check` datetime DEFAULT NULL COMMENT '最后健康检查时间',
  `last_config_sync` datetime DEFAULT NULL COMMENT '最后配置同步时间',
  `sync_status` varchar(32) DEFAULT NULL COMMENT '同步状态 (PENDING, SYNCED, FAILED)',
  `takeover_capability` varchar(32) DEFAULT NULL COMMENT '接管能力 (FULL, MONITOR_ONLY, CONFIG_ONLY, READ_ONLY)',
  `config_merge_strategy` varchar(32) DEFAULT NULL COMMENT '配置合并策略 (PRESERVE_EXISTING, MERGE, REPLACE, CUSTOM)',
  `custom_config_rules` text COMMENT '自定义配置规则 (JSON格式)',
  `notes` text COMMENT '备注信息',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cluster_service_host_component` (`cluster_id`, `service_name`, `hostname`, `component_name`),
  KEY `idx_cluster_service` (`cluster_id`, `service_name`),
  KEY `idx_component_state` (`component_state`),
  KEY `idx_health_status` (`health_status`),
  KEY `idx_sync_status` (`sync_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集群现有组件注册表';

-- ------------------------------------------------------------
-- 表: t_ddh_config_sync_history
-- 配置同步历史表
-- 记录配置同步操作的历史
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_ddh_config_sync_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cluster_id` int(11) NOT NULL COMMENT '集群ID',
  `service_name` varchar(128) NOT NULL COMMENT '服务名称',
  `component_id` int(11) DEFAULT NULL COMMENT '组件ID (关联t_ddh_cluster_existing_component.id)',
  `sync_operation_type` varchar(32) NOT NULL COMMENT '同步操作类型 (PULL_FROM_COMPONENT, PUSH_TO_COMPONENT, MERGE_CONFIG, VALIDATE_CONFIG)',
  `sync_source` varchar(32) NOT NULL COMMENT '同步来源 (EXISTING_COMPONENT, DATASOPHON_CONFIG)',
  `sync_target` varchar(32) NOT NULL COMMENT '同步目标 (EXISTING_COMPONENT, DATASOPHON_CONFIG)',
  `config_changes` longtext COMMENT '配置变更详情 (JSON格式)',
  `config_snapshot_before` longtext COMMENT '同步前配置快照 (JSON格式)',
  `config_snapshot_after` longtext COMMENT '同步后配置快照 (JSON格式)',
  `sync_status` varchar(32) NOT NULL COMMENT '同步状态 (SUCCESS, PARTIAL_SUCCESS, FAILED)',
  `error_message` text COMMENT '错误信息',
  `sync_duration_ms` bigint(20) DEFAULT NULL COMMENT '同步耗时(毫秒)',
  `sync_start_time` datetime NOT NULL COMMENT '同步开始时间',
  `sync_end_time` datetime DEFAULT NULL COMMENT '同步结束时间',
  `trigger_type` varchar(32) NOT NULL COMMENT '触发类型 (MANUAL, SCHEDULED, API_CALL, AUTO_DETECTION)',
  `triggered_by` varchar(64) DEFAULT NULL COMMENT '触发人',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_cluster_service` (`cluster_id`, `service_name`),
  KEY `idx_component_id` (`component_id`),
  KEY `idx_sync_operation_type` (`sync_operation_type`),
  KEY `idx_sync_status` (`sync_status`),
  KEY `idx_sync_time` (`sync_start_time`),
  KEY `idx_trigger_type` (`trigger_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置同步历史表';

-- ------------------------------------------------------------
-- 表: t_ddh_component_discovery_result
-- 组件发现结果表
-- 记录组件发现任务的执行结果和详情
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_ddh_component_discovery_result` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `discovery_task_id` varchar(64) NOT NULL COMMENT '发现任务ID',
  `cluster_id` int(11) NOT NULL COMMENT '集群ID',
  `service_name` varchar(128) NOT NULL COMMENT '服务名称',
  `discovery_target` text COMMENT '发现目标 (IP范围、主机名列表、或自动发现)',
  `discovery_method` varchar(64) DEFAULT NULL COMMENT '发现方法 (PORT_SCAN, PROCESS_DETECTION, CONFIG_PARSING, API_QUERY, CUSTOM_SCRIPT)',
  `discovery_status` varchar(32) NOT NULL COMMENT '发现状态',
  `discovery_start_time` datetime NOT NULL COMMENT '发现开始时间',
  `discovery_end_time` datetime DEFAULT NULL COMMENT '发现结束时间',
  `duration_ms` bigint(20) DEFAULT NULL COMMENT '发现耗时(毫秒)',
  `discovery_stats` text COMMENT '发现结果统计 (JSON格式: {"totalHosts": 10, "foundComponents": 5, "failedHosts": 2})',
  `discovery_details` longtext COMMENT '发现详情 (JSON格式存储详细发现结果)',
  `error_message` text COMMENT '错误信息 (发现失败时记录)',
  `trigger_type` varchar(32) NOT NULL COMMENT '触发方式 (MANUAL:手动触发, SCHEDULED:定时任务, API_CALL:API调用)',
  `triggered_by` varchar(64) DEFAULT NULL COMMENT '触发人 (手动触发时记录)',
  `auto_register_flag` tinyint(1) DEFAULT '0' COMMENT '是否自动注册 (0:不自动注册, 1:自动注册已验证的组件)',
  `registered_count` int(11) DEFAULT '0' COMMENT '注册组件数量 (自动注册的组件数量)',
  `remark` text COMMENT '备注信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_discovery_task_id` (`discovery_task_id`),
  KEY `idx_cluster_service` (`cluster_id`, `service_name`),
  KEY `idx_discovery_status` (`discovery_status`),
  KEY `idx_discovery_time` (`discovery_start_time`),
  KEY `idx_trigger_type` (`trigger_type`),
  KEY `idx_auto_register` (`auto_register_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组件发现结果表';

-- ------------------------------------------------------------
-- 表: t_ddh_takeover_audit_log
-- 接管审计日志表
-- 记录所有接管操作的审计日志
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_ddh_takeover_audit_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `component_id` int(11) NOT NULL COMMENT '组件ID (关联t_ddh_cluster_existing_component.id)',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型 (DISCOVERY, VALIDATION, REGISTRATION, CONFIG_SYNC, START_STOP, UPGRADE, ROLLBACK)',
  `operation_detail` varchar(500) DEFAULT NULL COMMENT '操作详情',
  `previous_state` text COMMENT '操作前状态 (JSON格式)',
  `new_state` text COMMENT '操作后状态 (JSON格式)',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人',
  `operation_result` varchar(20) NOT NULL COMMENT '操作结果 (SUCCESS, PARTIAL_SUCCESS, FAILED)',
  `error_message` text COMMENT '错误信息',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_component_id` (`component_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operation_result` (`operation_result`),
  KEY `idx_created_time` (`created_time`),
  CONSTRAINT `fk_takeover_audit_component` FOREIGN KEY (`component_id`) 
    REFERENCES `t_ddh_cluster_existing_component` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接管审计日志表';

-- ------------------------------------------------------------
-- 表: t_ddh_configuration_management
-- 配置管理表
-- 存储已接管组件的配置项信息，包括配置的元数据、状态、同步信息等
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_ddh_configuration_management` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cluster_id` int(11) NOT NULL COMMENT '集群ID',
  `service_name` varchar(64) NOT NULL COMMENT '服务名称 (如: HDFS, YARN, SPARK)',
  `service_role` varchar(64) DEFAULT NULL COMMENT '服务角色 (如: NameNode, DataNode, ResourceManager)',
  `hostname` varchar(128) DEFAULT NULL COMMENT '主机名',
  `existing_component_id` int(11) DEFAULT NULL COMMENT '现有组件ID (关联t_ddh_cluster_existing_component.id)',
  `config_name` varchar(256) NOT NULL COMMENT '配置项名称 (如: dfs.replication, yarn.scheduler.minimum-allocation-mb)',
  `config_file_name` varchar(128) DEFAULT NULL COMMENT '配置文件名称 (如: core-site.xml, hdfs-site.xml)',
  `config_file_path` varchar(500) DEFAULT NULL COMMENT '配置文件路径',
  `config_item_path` varchar(500) DEFAULT NULL COMMENT '配置项在文件中的路径 (如: /configuration/property[@name="dfs.replication"])',
  `data_type` varchar(32) DEFAULT 'STRING' COMMENT '配置项数据类型 (STRING, INTEGER, BOOLEAN, LIST, MAP)',
  `default_value` text COMMENT '配置项默认值',
  `current_value` text COMMENT '配置项当前值 (来自现有组件)',
  `platform_value` text COMMENT '配置项平台值 (来自DataSophon平台)',
  `recommended_value` text COMMENT '配置项推荐值 (由平台分析得出)',
  `description` text COMMENT '配置项描述',
  `importance_level` varchar(32) DEFAULT 'NORMAL' COMMENT '配置项重要性 (CRITICAL, IMPORTANT, NORMAL, LOW)',
  `config_status` int(11) DEFAULT 1 COMMENT '配置项状态 (1: 未同步, 2: 同步中, 3: 已同步, 4: 同步失败, 5: 不同步, 6: 配置冲突, 7: 已删除)',
  `sync_status` int(11) DEFAULT 1 COMMENT '配置同步状态 (1: 待同步, 2: 同步中, 3: 同步成功, 4: 同步失败, 5: 部分成功, 6: 已取消, 7: 同步超时)',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最后同步时间',
  `last_sync_direction` varchar(32) DEFAULT NULL COMMENT '最后同步方向 (TO_PLATFORM, TO_COMPONENT)',
  `last_sync_task_id` int(11) DEFAULT NULL COMMENT '最后同步任务ID (关联t_ddh_config_sync_history.id)',
  `validation_status` varchar(32) DEFAULT NULL COMMENT '配置验证状态 (VALID, INVALID, WARNING)',
  `last_validation_time` datetime DEFAULT NULL COMMENT '最后验证时间',
  `validation_result` text COMMENT '验证结果 (JSON格式存储详细验证信息)',
  `modifiable` tinyint(1) DEFAULT 1 COMMENT '配置项是否可修改 (0:只读, 1:可修改)',
  `encrypted` tinyint(1) DEFAULT 0 COMMENT '配置项是否已加密 (0:未加密, 1:已加密)',
  `sensitivity_level` tinyint(1) DEFAULT 0 COMMENT '配置项敏感级别 (0:非敏感, 1:敏感, 2:高度敏感)',
  `version` int(11) DEFAULT 1 COMMENT '配置项版本号 (每次修改递增)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人',
  `remark` text COMMENT '备注信息',
  PRIMARY KEY (`id`),
  KEY `idx_cluster_id` (`cluster_id`),
  KEY `idx_service_name` (`service_name`),
  KEY `idx_existing_component_id` (`existing_component_id`),
  KEY `idx_config_status` (`config_status`),
  KEY `idx_sync_status` (`sync_status`),
  KEY `idx_config_name` (`config_name`(191)),
  KEY `idx_config_file_name` (`config_file_name`),
  KEY `idx_importance_level` (`importance_level`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_config_mgmt_existing_component` FOREIGN KEY (`existing_component_id`) 
    REFERENCES `t_ddh_cluster_existing_component` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_config_mgmt_cluster` FOREIGN KEY (`cluster_id`) 
    REFERENCES `t_ddh_cluster_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置管理表';

-- ------------------------------------------------------------
-- 表: t_ddh_operations_management
-- 运维管理表
-- 记录对已接管组件的运维操作，包括服务启停、健康检查、日志收集、配置同步等操作
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_ddh_operations_management` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cluster_id` int(11) NOT NULL COMMENT '集群ID',
  `service_name` varchar(64) NOT NULL COMMENT '服务名称 (如: HDFS, YARN, SPARK)',
  `service_role` varchar(64) DEFAULT NULL COMMENT '服务角色 (如: NameNode, DataNode, ResourceManager)',
  `hostname` varchar(128) DEFAULT NULL COMMENT '主机名',
  `existing_component_id` int(11) DEFAULT NULL COMMENT '现有组件ID (关联t_ddh_cluster_existing_component.id)',
  `operation_type` int(11) DEFAULT 1 COMMENT '操作类型 (1: 服务控制, 2: 健康检查, 3: 日志管理, 4: 配置同步, 5: 性能监控, 6: 备份恢复, 7: 升级回滚, 8: 安全管理, 9: 自定义操作)',
  `operation_sub_type` varchar(64) DEFAULT NULL COMMENT '操作子类型 (如: START_SERVICE, STOP_SERVICE, RESTART_SERVICE, CHECK_HEALTH, COLLECT_LOGS, SYNC_CONFIG)',
  `operation_params` text COMMENT '操作参数 (JSON格式存储操作参数)',
  `operation_status` int(11) DEFAULT 1 COMMENT '操作状态 (1: 待执行, 2: 执行中, 3: 执行成功, 4: 执行失败, 5: 部分成功, 6: 已取消, 7: 执行超时, 8: 重试中, 9: 已调度)',
  `start_time` datetime DEFAULT NULL COMMENT '操作开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '操作结束时间',
  `duration_ms` bigint(20) DEFAULT NULL COMMENT '操作耗时 (毫秒)',
  `operation_result` text COMMENT '操作结果 (JSON格式存储详细结果)',
  `error_message` text COMMENT '错误信息 (操作失败时记录)',
  `error_details` text COMMENT '错误详情 (堆栈信息等)',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人',
  `operator_ip` varchar(45) DEFAULT NULL COMMENT '操作IP地址',
  `session_id` varchar(128) DEFAULT NULL COMMENT '操作会话ID (用于关联同一批操作)',
  `parent_operation_id` int(11) DEFAULT NULL COMMENT '父操作ID (用于关联子操作)',
  `priority` int(11) DEFAULT 5 COMMENT '操作优先级 (1-10, 1为最高)',
  `retry_count` int(11) DEFAULT 0 COMMENT '操作重试次数',
  `max_retry_count` int(11) DEFAULT 3 COMMENT '最大重试次数',
  `timeout_ms` bigint(20) DEFAULT 300000 COMMENT '操作超时时间 (毫秒，默认5分钟)',
  `async_operation` tinyint(1) DEFAULT 0 COMMENT '是否异步操作 (0:同步, 1:异步)',
  `async_task_id` varchar(128) DEFAULT NULL COMMENT '异步任务ID (用于查询异步操作结果)',
  `remark` text COMMENT '操作备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_cluster_id` (`cluster_id`),
  KEY `idx_service_name` (`service_name`),
  KEY `idx_existing_component_id` (`existing_component_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operation_status` (`operation_status`),
  KEY `idx_operation_sub_type` (`operation_sub_type`),
  KEY `idx_operator` (`operator`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_parent_operation_id` (`parent_operation_id`),
  KEY `idx_priority` (`priority`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_operations_existing_component` FOREIGN KEY (`existing_component_id`) 
    REFERENCES `t_ddh_cluster_existing_component` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_operations_cluster` FOREIGN KEY (`cluster_id`) 
    REFERENCES `t_ddh_cluster_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_operations_parent` FOREIGN KEY (`parent_operation_id`) 
    REFERENCES `t_ddh_operations_management` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维管理表';

-- ------------------------------------------------------------
-- 扩展现有表结构 - 支持现有组件管理
-- 使用动态SQL避免重复列错误
-- ------------------------------------------------------------
SET @dbname = DATABASE();

-- 扩展 t_ddh_cluster_service_instance 表
-- 检查并添加 managed_by 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND COLUMN_NAME = 'managed_by');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_instance` ADD COLUMN `managed_by` varchar(20) DEFAULT ''DATASOPHON'' COMMENT ''管理方 (DATASOPHON: DataSophon管理, EXTERNAL: 外部管理, MIXED: 混合管理)''',
    'SELECT ''Column managed_by already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 takeover_level 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND COLUMN_NAME = 'takeover_level');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_instance` ADD COLUMN `takeover_level` int(11) DEFAULT 4 COMMENT ''接管级别 (1: 监控, 2: 配置, 3: 运维, 4: 完全管理)''',
    'SELECT ''Column takeover_level already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 external_config_path 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND COLUMN_NAME = 'external_config_path');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_instance` ADD COLUMN `external_config_path` varchar(500) DEFAULT NULL COMMENT ''外部配置路径 (当managed_by为EXTERNAL或MIXED时使用)''',
    'SELECT ''Column external_config_path already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 last_config_sync_time 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND COLUMN_NAME = 'last_config_sync_time');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_instance` ADD COLUMN `last_config_sync_time` datetime DEFAULT NULL COMMENT ''最后配置同步时间''',
    'SELECT ''Column last_config_sync_time already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 existing_component_id 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND COLUMN_NAME = 'existing_component_id');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_instance` ADD COLUMN `existing_component_id` int(11) DEFAULT NULL COMMENT ''现有组件ID (关联t_ddh_cluster_existing_component.id)''',
    'SELECT ''Column existing_component_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 discovery_status 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND COLUMN_NAME = 'discovery_status');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_instance` ADD COLUMN `discovery_status` varchar(20) DEFAULT NULL COMMENT ''发现状态 (NOT_DISCOVERED, DISCOVERED, VALIDATED, REGISTERED)''',
    'SELECT ''Column discovery_status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 扩展 t_ddh_cluster_service_role_instance 表
-- 检查并添加 installation_type 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND COLUMN_NAME = 'installation_type');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_role_instance` ADD COLUMN `installation_type` varchar(20) DEFAULT ''NEW'' COMMENT ''安装类型 (NEW: 全新安装, EXISTING: 现有组件, MIGRATED: 迁移组件)''',
    'SELECT ''Column installation_type already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 discovery_method 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND COLUMN_NAME = 'discovery_method');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_role_instance` ADD COLUMN `discovery_method` varchar(50) DEFAULT NULL COMMENT ''发现方法 (PORT_SCAN, PROCESS_DETECTION, CONFIG_PARSING, API_QUERY, CUSTOM_SCRIPT)''',
    'SELECT ''Column discovery_method already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 validation_status 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND COLUMN_NAME = 'validation_status');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_role_instance` ADD COLUMN `validation_status` varchar(20) DEFAULT ''PENDING'' COMMENT ''验证状态 (PENDING, SUCCESS, FAILED, WARNING)''',
    'SELECT ''Column validation_status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 existing_component_id 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND COLUMN_NAME = 'existing_component_id');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_role_instance` ADD COLUMN `existing_component_id` int(11) DEFAULT NULL COMMENT ''现有组件ID (关联t_ddh_cluster_existing_component.id)''',
    'SELECT ''Column existing_component_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 takeover_capability 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND COLUMN_NAME = 'takeover_capability');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_role_instance` ADD COLUMN `takeover_capability` varchar(32) DEFAULT NULL COMMENT ''接管能力 (FULL, MONITOR_ONLY, CONFIG_ONLY, READ_ONLY)''',
    'SELECT ''Column takeover_capability already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 config_merge_strategy 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND COLUMN_NAME = 'config_merge_strategy');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_role_instance` ADD COLUMN `config_merge_strategy` varchar(32) DEFAULT NULL COMMENT ''配置合并策略 (PRESERVE_EXISTING, MERGE, REPLACE, CUSTOM)''',
    'SELECT ''Column config_merge_strategy already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 扩展 t_ddh_cluster_variable 表
-- 检查并添加 variable_scope 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_variable' AND COLUMN_NAME = 'variable_scope');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_variable` ADD COLUMN `variable_scope` varchar(50) DEFAULT ''CLUSTER'' COMMENT ''作用域 (CLUSTER: 集群级别, EXISTING_SERVICE: 现有服务级别, BOTH: 两者都适用)''',
    'SELECT ''Column variable_scope already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 source_type 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_variable' AND COLUMN_NAME = 'source_type');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_variable` ADD COLUMN `source_type` varchar(20) DEFAULT ''DATASOPHON'' COMMENT ''来源类型 (DATASOPHON: DataSophon配置, EXTERNAL: 外部配置, MERGED: 合并配置)''',
    'SELECT ''Column source_type already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 config_source_id 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_variable' AND COLUMN_NAME = 'config_source_id');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_variable` ADD COLUMN `config_source_id` int(11) DEFAULT NULL COMMENT ''配置来源ID (关联t_ddh_cluster_existing_component.id或t_ddh_config_sync_history.id)''',
    'SELECT ''Column config_source_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 is_overridable 列
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_variable' AND COLUMN_NAME = 'is_overridable');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_variable` ADD COLUMN `is_overridable` tinyint(1) DEFAULT 1 COMMENT ''是否可覆盖 (0: 不可覆盖, 1: 可覆盖)''',
    'SELECT ''Column is_overridable already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 索引优化
-- ------------------------------------------------------------

-- 为 t_ddh_cluster_service_instance 表添加索引
-- 检查并添加索引: idx_managed_by
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND INDEX_NAME = 'idx_managed_by');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_managed_by` ON `t_ddh_cluster_service_instance` (`managed_by`)',
    'SELECT ''Index idx_managed_by already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_takeover_level
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND INDEX_NAME = 'idx_takeover_level');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_takeover_level` ON `t_ddh_cluster_service_instance` (`takeover_level`)',
    'SELECT ''Index idx_takeover_level already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_discovery_status
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND INDEX_NAME = 'idx_discovery_status');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_discovery_status` ON `t_ddh_cluster_service_instance` (`discovery_status`)',
    'SELECT ''Index idx_discovery_status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_existing_component_id
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND INDEX_NAME = 'idx_existing_component_id');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_existing_component_id` ON `t_ddh_cluster_service_instance` (`existing_component_id`)',
    'SELECT ''Index idx_existing_component_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为 t_ddh_cluster_service_role_instance 表添加索引
-- 检查并添加索引: idx_installation_type
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND INDEX_NAME = 'idx_installation_type');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_installation_type` ON `t_ddh_cluster_service_role_instance` (`installation_type`)',
    'SELECT ''Index idx_installation_type already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_validation_status
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND INDEX_NAME = 'idx_validation_status');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_validation_status` ON `t_ddh_cluster_service_role_instance` (`validation_status`)',
    'SELECT ''Index idx_validation_status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_takeover_capability
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND INDEX_NAME = 'idx_takeover_capability');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_takeover_capability` ON `t_ddh_cluster_service_role_instance` (`takeover_capability`)',
    'SELECT ''Index idx_takeover_capability already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_existing_component_id_role
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND INDEX_NAME = 'idx_existing_component_id_role');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_existing_component_id_role` ON `t_ddh_cluster_service_role_instance` (`existing_component_id`)',
    'SELECT ''Index idx_existing_component_id_role already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为 t_ddh_cluster_variable 表添加索引
-- 检查并添加索引: idx_variable_scope
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_variable' AND INDEX_NAME = 'idx_variable_scope');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_variable_scope` ON `t_ddh_cluster_variable` (`variable_scope`)',
    'SELECT ''Index idx_variable_scope already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_source_type
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_variable' AND INDEX_NAME = 'idx_source_type');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_source_type` ON `t_ddh_cluster_variable` (`source_type`)',
    'SELECT ''Index idx_source_type already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 复合索引用于常用查询 - t_ddh_configuration_management 表
-- 检查并添加索引: idx_cluster_service_status
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_configuration_management' AND INDEX_NAME = 'idx_cluster_service_status');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_cluster_service_status` ON `t_ddh_configuration_management` (`cluster_id`, `service_name`, `config_status`)',
    'SELECT ''Index idx_cluster_service_status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_cluster_service_sync_status
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_configuration_management' AND INDEX_NAME = 'idx_cluster_service_sync_status');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_cluster_service_sync_status` ON `t_ddh_configuration_management` (`cluster_id`, `service_name`, `sync_status`)',
    'SELECT ''Index idx_cluster_service_sync_status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_config_file_path
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_configuration_management' AND INDEX_NAME = 'idx_config_file_path');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_config_file_path` ON `t_ddh_configuration_management` (`config_file_path`(191))',
    'SELECT ''Index idx_config_file_path already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_validation_status
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_configuration_management' AND INDEX_NAME = 'idx_validation_status');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_validation_status` ON `t_ddh_configuration_management` (`validation_status`)',
    'SELECT ''Index idx_validation_status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 复合索引用于常用查询 - t_ddh_operations_management 表
-- 检查并添加索引: idx_cluster_service_operation
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_operations_management' AND INDEX_NAME = 'idx_cluster_service_operation');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_cluster_service_operation` ON `t_ddh_operations_management` (`cluster_id`, `service_name`, `operation_type`)',
    'SELECT ''Index idx_cluster_service_operation already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_cluster_status_time
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_operations_management' AND INDEX_NAME = 'idx_cluster_status_time');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_cluster_status_time` ON `t_ddh_operations_management` (`cluster_id`, `operation_status`, `create_time`)',
    'SELECT ''Index idx_cluster_status_time already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_async_task_id
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_operations_management' AND INDEX_NAME = 'idx_async_task_id');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_async_task_id` ON `t_ddh_operations_management` (`async_task_id`)',
    'SELECT ''Index idx_async_task_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引: idx_retry_count_status
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_operations_management' AND INDEX_NAME = 'idx_retry_count_status');
SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX `idx_retry_count_status` ON `t_ddh_operations_management` (`retry_count`, `operation_status`)',
    'SELECT ''Index idx_retry_count_status already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 外键约束
-- ------------------------------------------------------------

-- 检查并添加外键约束: fk_existing_component_cluster
SET @constraint_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_existing_component' AND CONSTRAINT_NAME = 'fk_existing_component_cluster');
SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_existing_component` ADD CONSTRAINT `fk_existing_component_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `t_ddh_cluster_info` (`id`) ON DELETE CASCADE',
    'SELECT ''Constraint fk_existing_component_cluster already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加外键约束: fk_sync_history_cluster
SET @constraint_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_config_sync_history' AND CONSTRAINT_NAME = 'fk_sync_history_cluster');
SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE `t_ddh_config_sync_history` ADD CONSTRAINT `fk_sync_history_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `t_ddh_cluster_info` (`id`) ON DELETE CASCADE',
    'SELECT ''Constraint fk_sync_history_cluster already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加外键约束: fk_sync_history_component
SET @constraint_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_config_sync_history' AND CONSTRAINT_NAME = 'fk_sync_history_component');
SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE `t_ddh_config_sync_history` ADD CONSTRAINT `fk_sync_history_component` FOREIGN KEY (`component_id`) REFERENCES `t_ddh_cluster_existing_component` (`id`) ON DELETE SET NULL',
    'SELECT ''Constraint fk_sync_history_component already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加外键约束: fk_discovery_result_cluster
SET @constraint_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_component_discovery_result' AND CONSTRAINT_NAME = 'fk_discovery_result_cluster');
SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE `t_ddh_component_discovery_result` ADD CONSTRAINT `fk_discovery_result_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `t_ddh_cluster_info` (`id`) ON DELETE CASCADE',
    'SELECT ''Constraint fk_discovery_result_cluster already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为 t_ddh_cluster_service_instance 表添加外键约束
-- 检查并添加外键约束: fk_service_instance_existing_component
SET @constraint_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_instance' AND CONSTRAINT_NAME = 'fk_service_instance_existing_component');
SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_instance` ADD CONSTRAINT `fk_service_instance_existing_component` FOREIGN KEY (`existing_component_id`) REFERENCES `t_ddh_cluster_existing_component` (`id`) ON DELETE SET NULL',
    'SELECT ''Constraint fk_service_instance_existing_component already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为 t_ddh_cluster_service_role_instance 表添加外键约束
-- 检查并添加外键约束: fk_role_instance_existing_component
SET @constraint_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_service_role_instance' AND CONSTRAINT_NAME = 'fk_role_instance_existing_component');
SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_service_role_instance` ADD CONSTRAINT `fk_role_instance_existing_component` FOREIGN KEY (`existing_component_id`) REFERENCES `t_ddh_cluster_existing_component` (`id`) ON DELETE SET NULL',
    'SELECT ''Constraint fk_role_instance_existing_component already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为 t_ddh_cluster_variable 表添加外键约束
-- 检查并添加外键约束: fk_variable_existing_component
SET @constraint_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = @dbname AND TABLE_NAME = 't_ddh_cluster_variable' AND CONSTRAINT_NAME = 'fk_variable_existing_component');
SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE `t_ddh_cluster_variable` ADD CONSTRAINT `fk_variable_existing_component` FOREIGN KEY (`config_source_id`) REFERENCES `t_ddh_cluster_existing_component` (`id`) ON DELETE SET NULL',
    'SELECT ''Constraint fk_variable_existing_component already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 视图
-- ------------------------------------------------------------

-- 视图: v_configuration_management_summary
-- 配置管理摘要视图，提供配置状态的聚合信息
CREATE OR REPLACE VIEW `v_configuration_management_summary` AS
SELECT 
  cm.cluster_id,
  ci.cluster_name,
  cm.service_name,
  COUNT(*) AS total_configs,
  SUM(CASE WHEN cm.config_status = 1 THEN 1 ELSE 0 END) AS not_synced,
  SUM(CASE WHEN cm.config_status = 3 THEN 1 ELSE 0 END) AS synced,
  SUM(CASE WHEN cm.config_status = 4 THEN 1 ELSE 0 END) AS sync_failed,
  SUM(CASE WHEN cm.config_status = 6 THEN 1 ELSE 0 END) AS conflicting,
  SUM(CASE WHEN cm.importance_level = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_configs,
  SUM(CASE WHEN cm.importance_level = 'IMPORTANT' THEN 1 ELSE 0 END) AS important_configs,
  SUM(CASE WHEN cm.sync_status = 1 THEN 1 ELSE 0 END) AS pending_sync,
  SUM(CASE WHEN cm.sync_status = 3 THEN 1 ELSE 0 END) AS sync_success,
  MAX(cm.update_time) AS last_updated
FROM `t_ddh_configuration_management` cm
LEFT JOIN `t_ddh_cluster_info` ci ON cm.cluster_id = ci.id
GROUP BY cm.cluster_id, cm.service_name, ci.cluster_name;

-- 视图: v_operations_management_summary
-- 运维管理摘要视图，提供运维操作的聚合信息
CREATE OR REPLACE VIEW `v_operations_management_summary` AS
SELECT 
  om.cluster_id,
  ci.cluster_name,
  om.service_name,
  COUNT(*) AS total_operations,
  SUM(CASE WHEN om.operation_status = 1 THEN 1 ELSE 0 END) AS pending_operations,
  SUM(CASE WHEN om.operation_status = 2 THEN 1 ELSE 0 END) AS running_operations,
  SUM(CASE WHEN om.operation_status = 3 THEN 1 ELSE 0 END) AS successful_operations,
  SUM(CASE WHEN om.operation_status = 4 THEN 1 ELSE 0 END) AS failed_operations,
  SUM(CASE WHEN om.operation_type = 1 THEN 1 ELSE 0 END) AS service_control_ops,
  SUM(CASE WHEN om.operation_type = 2 THEN 1 ELSE 0 END) AS health_check_ops,
  SUM(CASE WHEN om.operation_type = 3 THEN 1 ELSE 0 END) AS log_management_ops,
  SUM(CASE WHEN om.operation_type = 4 THEN 1 ELSE 0 END) AS config_sync_ops,
  AVG(om.duration_ms) AS avg_duration_ms,
  MAX(om.duration_ms) AS max_duration_ms,
  MAX(om.create_time) AS last_operation_time
FROM `t_ddh_operations_management` om
LEFT JOIN `t_ddh_cluster_info` ci ON om.cluster_id = ci.id
GROUP BY om.cluster_id, om.service_name, ci.cluster_name;

-- 视图: v_operations_success_rate_daily
-- 每日操作成功率视图
CREATE OR REPLACE VIEW `v_operations_success_rate_daily` AS
SELECT 
  DATE(om.create_time) AS operation_date,
  om.cluster_id,
  ci.cluster_name,
  om.operation_type,
  COUNT(*) AS total_operations,
  COUNT(CASE WHEN om.operation_status = 3 THEN 1 END) AS successful_operations,
  COUNT(CASE WHEN om.operation_status = 4 THEN 1 END) AS failed_operations,
  ROUND(COUNT(CASE WHEN om.operation_status = 3 THEN 1 END) * 100.0 / COUNT(*), 2) AS success_rate,
  AVG(om.duration_ms) AS avg_duration_ms
FROM `t_ddh_operations_management` om
LEFT JOIN `t_ddh_cluster_info` ci ON om.cluster_id = ci.id
WHERE om.operation_status IN (3, 4) -- 只统计已完成的操作
GROUP BY DATE(om.create_time), om.cluster_id, om.operation_type, ci.cluster_name;

-- ------------------------------------------------------------
-- 注释更新
-- ------------------------------------------------------------
ALTER TABLE `t_ddh_configuration_management` 
  COMMENT = '配置管理表 - 存储已接管组件的配置项信息，包括配置的元数据、状态、同步信息等';

ALTER TABLE `t_ddh_operations_management` 
  COMMENT = '运维管理表 - 记录对已接管组件的运维操作，包括服务启停、健康检查、日志收集、配置同步等操作';
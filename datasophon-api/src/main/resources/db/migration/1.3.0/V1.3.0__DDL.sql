-- ============================================================
-- DataSophon 1.3.0 DDL - Existing Component Management System
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
-- 索引优化和约束添加
-- ------------------------------------------------------------
ALTER TABLE `t_ddh_cluster_existing_component` 
  ADD CONSTRAINT `fk_existing_component_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `t_ddh_cluster_info` (`id`) ON DELETE CASCADE;

ALTER TABLE `t_ddh_config_sync_history` 
  ADD CONSTRAINT `fk_sync_history_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `t_ddh_cluster_info` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_sync_history_component` FOREIGN KEY (`component_id`) REFERENCES `t_ddh_cluster_existing_component` (`id`) ON DELETE SET NULL;

ALTER TABLE `t_ddh_component_discovery_result` 
  ADD CONSTRAINT `fk_discovery_result_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `t_ddh_cluster_info` (`id`) ON DELETE CASCADE;
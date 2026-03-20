-- ============================================================
-- DataSophon 1.3.1 DDL - Existing Component Management System Extensions
-- ============================================================

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
-- 扩展现有表结构 - 支持现有组件管理
-- ------------------------------------------------------------

-- 扩展 t_ddh_cluster_service_instance 表
ALTER TABLE `t_ddh_cluster_service_instance` 
  ADD COLUMN IF NOT EXISTS `managed_by` varchar(20) DEFAULT 'DATASOPHON' COMMENT '管理方 (DATASOPHON: DataSophon管理, EXTERNAL: 外部管理, MIXED: 混合管理)',
  ADD COLUMN IF NOT EXISTS `takeover_level` int(11) DEFAULT 4 COMMENT '接管级别 (1: 监控, 2: 配置, 3: 运维, 4: 完全管理)',
  ADD COLUMN IF NOT EXISTS `external_config_path` varchar(500) DEFAULT NULL COMMENT '外部配置路径 (当managed_by为EXTERNAL或MIXED时使用)',
  ADD COLUMN IF NOT EXISTS `last_config_sync_time` datetime DEFAULT NULL COMMENT '最后配置同步时间',
  ADD COLUMN IF NOT EXISTS `existing_component_id` int(11) DEFAULT NULL COMMENT '现有组件ID (关联t_ddh_cluster_existing_component.id)',
  ADD COLUMN IF NOT EXISTS `discovery_status` varchar(20) DEFAULT NULL COMMENT '发现状态 (NOT_DISCOVERED, DISCOVERED, VALIDATED, REGISTERED)';

-- 扩展 t_ddh_cluster_service_role_instance 表
ALTER TABLE `t_ddh_cluster_service_role_instance` 
  ADD COLUMN IF NOT EXISTS `installation_type` varchar(20) DEFAULT 'NEW' COMMENT '安装类型 (NEW: 全新安装, EXISTING: 现有组件, MIGRATED: 迁移组件)',
  ADD COLUMN IF NOT EXISTS `discovery_method` varchar(50) DEFAULT NULL COMMENT '发现方法 (PORT_SCAN, PROCESS_DETECTION, CONFIG_PARSING, API_QUERY, CUSTOM_SCRIPT)',
  ADD COLUMN IF NOT EXISTS `validation_status` varchar(20) DEFAULT 'PENDING' COMMENT '验证状态 (PENDING, SUCCESS, FAILED, WARNING)',
  ADD COLUMN IF NOT EXISTS `existing_component_id` int(11) DEFAULT NULL COMMENT '现有组件ID (关联t_ddh_cluster_existing_component.id)',
  ADD COLUMN IF NOT EXISTS `takeover_capability` varchar(32) DEFAULT NULL COMMENT '接管能力 (FULL, MONITOR_ONLY, CONFIG_ONLY, READ_ONLY)',
  ADD COLUMN IF NOT EXISTS `config_merge_strategy` varchar(32) DEFAULT NULL COMMENT '配置合并策略 (PRESERVE_EXISTING, MERGE, REPLACE, CUSTOM)';

-- 扩展 t_ddh_cluster_variable 表
ALTER TABLE `t_ddh_cluster_variable` 
  ADD COLUMN IF NOT EXISTS `variable_scope` varchar(50) DEFAULT 'CLUSTER' COMMENT '作用域 (CLUSTER: 集群级别, EXISTING_SERVICE: 现有服务级别, BOTH: 两者都适用)',
  ADD COLUMN IF NOT EXISTS `source_type` varchar(20) DEFAULT 'DATASOPHON' COMMENT '来源类型 (DATASOPHON: DataSophon配置, EXTERNAL: 外部配置, MERGED: 合并配置)',
  ADD COLUMN IF NOT EXISTS `config_source_id` int(11) DEFAULT NULL COMMENT '配置来源ID (关联t_ddh_cluster_existing_component.id或t_ddh_config_sync_history.id)',
  ADD COLUMN IF NOT EXISTS `is_overridable` tinyint(1) DEFAULT 1 COMMENT '是否可覆盖 (0: 不可覆盖, 1: 可覆盖)';

-- ------------------------------------------------------------
-- 索引优化
-- ------------------------------------------------------------

-- 为 t_ddh_cluster_service_instance 表添加索引
CREATE INDEX IF NOT EXISTS `idx_managed_by` ON `t_ddh_cluster_service_instance` (`managed_by`);
CREATE INDEX IF NOT EXISTS `idx_takeover_level` ON `t_ddh_cluster_service_instance` (`takeover_level`);
CREATE INDEX IF NOT EXISTS `idx_discovery_status` ON `t_ddh_cluster_service_instance` (`discovery_status`);
CREATE INDEX IF NOT EXISTS `idx_existing_component_id` ON `t_ddh_cluster_service_instance` (`existing_component_id`);

-- 为 t_ddh_cluster_service_role_instance 表添加索引
CREATE INDEX IF NOT EXISTS `idx_installation_type` ON `t_ddh_cluster_service_role_instance` (`installation_type`);
CREATE INDEX IF NOT EXISTS `idx_validation_status` ON `t_ddh_cluster_service_role_instance` (`validation_status`);
CREATE INDEX IF NOT EXISTS `idx_takeover_capability` ON `t_ddh_cluster_service_role_instance` (`takeover_capability`);
CREATE INDEX IF NOT EXISTS `idx_existing_component_id_role` ON `t_ddh_cluster_service_role_instance` (`existing_component_id`);

-- 为 t_ddh_cluster_variable 表添加索引
CREATE INDEX IF NOT EXISTS `idx_variable_scope` ON `t_ddh_cluster_variable` (`variable_scope`);
CREATE INDEX IF NOT EXISTS `idx_source_type` ON `t_ddh_cluster_variable` (`source_type`);

-- ------------------------------------------------------------
-- 外键约束
-- ------------------------------------------------------------

-- 为 t_ddh_cluster_service_instance 表添加外键约束
ALTER TABLE `t_ddh_cluster_service_instance` 
  ADD CONSTRAINT IF NOT EXISTS `fk_service_instance_existing_component` 
  FOREIGN KEY (`existing_component_id`) 
  REFERENCES `t_ddh_cluster_existing_component` (`id`) 
  ON DELETE SET NULL;

-- 为 t_ddh_cluster_service_role_instance 表添加外键约束
ALTER TABLE `t_ddh_cluster_service_role_instance` 
  ADD CONSTRAINT IF NOT EXISTS `fk_role_instance_existing_component` 
  FOREIGN KEY (`existing_component_id`) 
  REFERENCES `t_ddh_cluster_existing_component` (`id`) 
  ON DELETE SET NULL;

-- 为 t_ddh_cluster_variable 表添加外键约束
ALTER TABLE `t_ddh_cluster_variable` 
  ADD CONSTRAINT IF NOT EXISTS `fk_variable_existing_component` 
  FOREIGN KEY (`config_source_id`) 
  REFERENCES `t_ddh_cluster_existing_component` (`id`) 
  ON DELETE SET NULL;
-- ============================================================
-- DataSophon 1.3 Rollback Script
-- 回滚统一版本：撤销1.3.0, 1.3.1, 1.3.2, 1.3.3所有变更
-- ============================================================

-- ------------------------------------------------------------
-- 第一步：删除视图
-- ------------------------------------------------------------
DROP VIEW IF EXISTS `v_operations_success_rate_daily`;
DROP VIEW IF EXISTS `v_operations_management_summary`;
DROP VIEW IF EXISTS `v_configuration_management_summary`;

-- ------------------------------------------------------------
-- 第二步：删除外键约束（按照依赖顺序）
-- ------------------------------------------------------------
-- 先删除子表的外键约束
ALTER TABLE `t_ddh_configuration_management` DROP FOREIGN KEY IF EXISTS `fk_config_mgmt_cluster`;
ALTER TABLE `t_ddh_configuration_management` DROP FOREIGN KEY IF EXISTS `fk_config_mgmt_existing_component`;
ALTER TABLE `t_ddh_operations_management` DROP FOREIGN KEY IF EXISTS `fk_operations_cluster`;
ALTER TABLE `t_ddh_operations_management` DROP FOREIGN KEY IF EXISTS `fk_operations_existing_component`;
ALTER TABLE `t_ddh_operations_management` DROP FOREIGN KEY IF EXISTS `fk_operations_parent`;

-- 删除现有表扩展的外键约束
ALTER TABLE `t_ddh_cluster_variable` DROP FOREIGN KEY IF EXISTS `fk_variable_existing_component`;
ALTER TABLE `t_ddh_cluster_service_role_instance` DROP FOREIGN KEY IF EXISTS `fk_role_instance_existing_component`;
ALTER TABLE `t_ddh_cluster_service_instance` DROP FOREIGN KEY IF EXISTS `fk_service_instance_existing_component`;
ALTER TABLE `t_ddh_takeover_audit_log` DROP FOREIGN KEY IF EXISTS `fk_takeover_audit_component`;

-- 删除新表之间的外键约束
ALTER TABLE `t_ddh_config_sync_history` DROP FOREIGN KEY IF EXISTS `fk_sync_history_component`;
ALTER TABLE `t_ddh_config_sync_history` DROP FOREIGN KEY IF EXISTS `fk_sync_history_cluster`;
ALTER TABLE `t_ddh_component_discovery_result` DROP FOREIGN KEY IF EXISTS `fk_discovery_result_cluster`;
ALTER TABLE `t_ddh_cluster_existing_component` DROP FOREIGN KEY IF EXISTS `fk_existing_component_cluster`;

-- ------------------------------------------------------------
-- 第三步：删除索引
-- ------------------------------------------------------------
-- 删除 t_ddh_operations_management 表的索引
DROP INDEX IF EXISTS `idx_retry_count_status` ON `t_ddh_operations_management`;
DROP INDEX IF EXISTS `idx_async_task_id` ON `t_ddh_operations_management`;
DROP INDEX IF EXISTS `idx_cluster_status_time` ON `t_ddh_operations_management`;
DROP INDEX IF EXISTS `idx_cluster_service_operation` ON `t_ddh_operations_management`;

-- 删除 t_ddh_configuration_management 表的索引
DROP INDEX IF EXISTS `idx_validation_status` ON `t_ddh_configuration_management`;
DROP INDEX IF EXISTS `idx_config_file_path` ON `t_ddh_configuration_management`;
DROP INDEX IF EXISTS `idx_cluster_service_sync_status` ON `t_ddh_configuration_management`;
DROP INDEX IF EXISTS `idx_cluster_service_status` ON `t_ddh_configuration_management`;

-- 删除 t_ddh_cluster_variable 表的索引
DROP INDEX IF EXISTS `idx_source_type` ON `t_ddh_cluster_variable`;
DROP INDEX IF EXISTS `idx_variable_scope` ON `t_ddh_cluster_variable`;

-- 删除 t_ddh_cluster_service_role_instance 表的索引
DROP INDEX IF EXISTS `idx_existing_component_id_role` ON `t_ddh_cluster_service_role_instance`;
DROP INDEX IF EXISTS `idx_takeover_capability` ON `t_ddh_cluster_service_role_instance`;
DROP INDEX IF EXISTS `idx_validation_status` ON `t_ddh_cluster_service_role_instance`;
DROP INDEX IF EXISTS `idx_installation_type` ON `t_ddh_cluster_service_role_instance`;

-- 删除 t_ddh_cluster_service_instance 表的索引
DROP INDEX IF EXISTS `idx_existing_component_id` ON `t_ddh_cluster_service_instance`;
DROP INDEX IF EXISTS `idx_discovery_status` ON `t_ddh_cluster_service_instance`;
DROP INDEX IF EXISTS `idx_takeover_level` ON `t_ddh_cluster_service_instance`;
DROP INDEX IF EXISTS `idx_managed_by` ON `t_ddh_cluster_service_instance`;

-- ------------------------------------------------------------
-- 第四步：删除扩展字段
-- ------------------------------------------------------------
-- 删除 t_ddh_cluster_variable 表的扩展字段
ALTER TABLE `t_ddh_cluster_variable` 
  DROP COLUMN `is_overridable`,
  DROP COLUMN `config_source_id`,
  DROP COLUMN `source_type`,
  DROP COLUMN `variable_scope`;

-- 删除 t_ddh_cluster_service_role_instance 表的扩展字段
ALTER TABLE `t_ddh_cluster_service_role_instance` 
  DROP COLUMN `config_merge_strategy`,
  DROP COLUMN `takeover_capability`,
  DROP COLUMN `existing_component_id`,
  DROP COLUMN `validation_status`,
  DROP COLUMN `discovery_method`,
  DROP COLUMN `installation_type`;

-- 删除 t_ddh_cluster_service_instance 表的扩展字段
ALTER TABLE `t_ddh_cluster_service_instance` 
  DROP COLUMN `discovery_status`,
  DROP COLUMN `existing_component_id`,
  DROP COLUMN `last_config_sync_time`,
  DROP COLUMN `external_config_path`,
  DROP COLUMN `takeover_level`,
  DROP COLUMN `managed_by`;

-- ------------------------------------------------------------
-- 第五步：删除新创建的表（按照依赖顺序）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_ddh_operations_management`;
DROP TABLE IF EXISTS `t_ddh_configuration_management`;
DROP TABLE IF EXISTS `t_ddh_takeover_audit_log`;
DROP TABLE IF EXISTS `t_ddh_config_sync_history`;
DROP TABLE IF EXISTS `t_ddh_component_discovery_result`;
DROP TABLE IF EXISTS `t_ddh_cluster_existing_component`;

-- ------------------------------------------------------------
-- 第六步：恢复表注释（可选）
-- ------------------------------------------------------------
ALTER TABLE `t_ddh_configuration_management` 
  COMMENT = '配置管理表';
ALTER TABLE `t_ddh_operations_management` 
  COMMENT = '运维管理表';
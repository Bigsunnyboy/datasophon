-- ============================================================
-- DataSophon 1.3.1 Rollback Script
-- Removes extensions for existing component management system
-- ============================================================

-- 删除外键约束
ALTER TABLE `t_ddh_cluster_variable` DROP FOREIGN KEY IF EXISTS `fk_variable_existing_component`;
ALTER TABLE `t_ddh_cluster_service_role_instance` DROP FOREIGN KEY IF EXISTS `fk_role_instance_existing_component`;
ALTER TABLE `t_ddh_cluster_service_instance` DROP FOREIGN KEY IF EXISTS `fk_service_instance_existing_component`;
ALTER TABLE `t_ddh_takeover_audit_log` DROP FOREIGN KEY IF EXISTS `fk_takeover_audit_component`;

-- 删除索引
DROP INDEX IF EXISTS `idx_variable_scope` ON `t_ddh_cluster_variable`;
DROP INDEX IF EXISTS `idx_source_type` ON `t_ddh_cluster_variable`;
DROP INDEX IF EXISTS `idx_existing_component_id_role` ON `t_ddh_cluster_service_role_instance`;
DROP INDEX IF EXISTS `idx_takeover_capability` ON `t_ddh_cluster_service_role_instance`;
DROP INDEX IF EXISTS `idx_validation_status` ON `t_ddh_cluster_service_role_instance`;
DROP INDEX IF EXISTS `idx_installation_type` ON `t_ddh_cluster_service_role_instance`;
DROP INDEX IF EXISTS `idx_existing_component_id` ON `t_ddh_cluster_service_instance`;
DROP INDEX IF EXISTS `idx_discovery_status` ON `t_ddh_cluster_service_instance`;
DROP INDEX IF EXISTS `idx_takeover_level` ON `t_ddh_cluster_service_instance`;
DROP INDEX IF EXISTS `idx_managed_by` ON `t_ddh_cluster_service_instance`;

-- 删除扩展字段
ALTER TABLE `t_ddh_cluster_variable` 
  DROP COLUMN IF EXISTS `is_overridable`,
  DROP COLUMN IF EXISTS `config_source_id`,
  DROP COLUMN IF EXISTS `source_type`,
  DROP COLUMN IF EXISTS `variable_scope`;

ALTER TABLE `t_ddh_cluster_service_role_instance` 
  DROP COLUMN IF EXISTS `config_merge_strategy`,
  DROP COLUMN IF EXISTS `takeover_capability`,
  DROP COLUMN IF EXISTS `existing_component_id`,
  DROP COLUMN IF EXISTS `validation_status`,
  DROP COLUMN IF EXISTS `discovery_method`,
  DROP COLUMN IF EXISTS `installation_type`;

ALTER TABLE `t_ddh_cluster_service_instance` 
  DROP COLUMN IF EXISTS `discovery_status`,
  DROP COLUMN IF EXISTS `existing_component_id`,
  DROP COLUMN IF EXISTS `last_config_sync_time`,
  DROP COLUMN IF EXISTS `external_config_path`,
  DROP COLUMN IF EXISTS `takeover_level`,
  DROP COLUMN IF EXISTS `managed_by`;

-- 删除接管审计日志表
DROP TABLE IF EXISTS `t_ddh_takeover_audit_log`;
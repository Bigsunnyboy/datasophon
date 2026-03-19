-- ============================================================
-- DataSophon 1.3.0 Rollback Script
-- Drops tables created for existing component management system
-- ============================================================

ALTER TABLE `t_ddh_config_sync_history` DROP FOREIGN KEY IF EXISTS `fk_sync_history_component`;
ALTER TABLE `t_ddh_config_sync_history` DROP FOREIGN KEY IF EXISTS `fk_sync_history_cluster`;
ALTER TABLE `t_ddh_component_discovery_result` DROP FOREIGN KEY IF EXISTS `fk_discovery_result_cluster`;
ALTER TABLE `t_ddh_cluster_existing_component` DROP FOREIGN KEY IF EXISTS `fk_existing_component_cluster`;

DROP TABLE IF EXISTS `t_ddh_config_sync_history`;
DROP TABLE IF EXISTS `t_ddh_component_discovery_result`;
DROP TABLE IF EXISTS `t_ddh_cluster_existing_component`;
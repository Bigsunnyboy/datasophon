-- ============================================================
-- DataSophon 1.3.2 DDL - Configuration Management System
-- ============================================================

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
-- 索引优化和性能增强
-- ------------------------------------------------------------

-- 复合索引用于常用查询
CREATE INDEX IF NOT EXISTS `idx_cluster_service_status` 
  ON `t_ddh_configuration_management` (`cluster_id`, `service_name`, `config_status`);

CREATE INDEX IF NOT EXISTS `idx_cluster_service_sync_status` 
  ON `t_ddh_configuration_management` (`cluster_id`, `service_name`, `sync_status`);

CREATE INDEX IF NOT EXISTS `idx_config_file_path` 
  ON `t_ddh_configuration_management` (`config_file_path`(191));

CREATE INDEX IF NOT EXISTS `idx_validation_status` 
  ON `t_ddh_configuration_management` (`validation_status`);

-- ------------------------------------------------------------
-- 视图: v_configuration_management_summary
-- 配置管理摘要视图，提供配置状态的聚合信息
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- 注释更新
-- ------------------------------------------------------------
ALTER TABLE `t_ddh_configuration_management` 
  COMMENT = '配置管理表 - 存储已接管组件的配置项信息，包括配置的元数据、状态、同步信息等';
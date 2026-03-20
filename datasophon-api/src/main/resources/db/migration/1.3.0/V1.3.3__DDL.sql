-- ============================================================
-- DataSophon 1.3.3 DDL - Operations Management System
-- ============================================================

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
-- 索引优化和性能增强
-- ------------------------------------------------------------

-- 复合索引用于常用查询
CREATE INDEX IF NOT EXISTS `idx_cluster_service_operation` 
  ON `t_ddh_operations_management` (`cluster_id`, `service_name`, `operation_type`);

CREATE INDEX IF NOT EXISTS `idx_cluster_status_time` 
  ON `t_ddh_operations_management` (`cluster_id`, `operation_status`, `create_time`);

CREATE INDEX IF NOT EXISTS `idx_async_task_id` 
  ON `t_ddh_operations_management` (`async_task_id`);

CREATE INDEX IF NOT EXISTS `idx_retry_count_status` 
  ON `t_ddh_operations_management` (`retry_count`, `operation_status`);

-- ------------------------------------------------------------
-- 视图: v_operations_management_summary
-- 运维管理摘要视图，提供运维操作的聚合信息
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- 视图: v_operations_success_rate_daily
-- 每日操作成功率视图
-- ------------------------------------------------------------
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
ALTER TABLE `t_ddh_operations_management` 
  COMMENT = '运维管理表 - 记录对已接管组件的运维操作，包括服务启停、健康检查、日志收集、配置同步等操作';
-- 补表：用户备份历史（已存在则跳过）
CREATE TABLE IF NOT EXISTS `user_backup` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `file_path` varchar(512) NOT NULL DEFAULT '' COMMENT '相对存储根路径',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '字节',
  `trigger_type` varchar(16) NOT NULL COMMENT 'MANUAL / AUTO',
  `status` varchar(16) NOT NULL COMMENT 'OK / FAILED',
  `error_message` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_created` (`user_id`,`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户备份历史';

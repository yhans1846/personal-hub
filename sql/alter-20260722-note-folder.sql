-- 笔记文件夹树：存量库增量
-- 2026-07-22

CREATE TABLE IF NOT EXISTS `note_folder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `parent_id` bigint DEFAULT NULL COMMENT '父文件夹（空=根）',
  `name` varchar(100) NOT NULL COMMENT '文件夹名称',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '同级排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_parent_sort` (`user_id`,`parent_id`,`sort_order`),
  UNIQUE KEY `uk_user_parent_name` (`user_id`,`parent_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='笔记文件夹';

ALTER TABLE `note_note`
  ADD COLUMN `folder_id` bigint DEFAULT NULL COMMENT '所属文件夹（空=未分类）' AFTER `excerpt`,
  ADD KEY `idx_user_folder` (`user_id`,`folder_id`);

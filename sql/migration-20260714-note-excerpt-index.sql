-- 笔记列表摘要 + 热点复合索引
-- 升级已有库时执行；新库以 sql/init.sql 为准

ALTER TABLE `note_note`
    ADD COLUMN `excerpt` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列表摘要' AFTER `md_path`;

ALTER TABLE `note_note`
    ADD INDEX `idx_user_deleted_updated` (`user_id` ASC, `is_deleted` ASC, `updated_at` ASC);

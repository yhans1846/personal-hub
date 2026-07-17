-- 2026-07-18: diary_entry 支持多图，image_file_id → image_file_ids（JSON 数组）

ALTER TABLE `diary_entry`
    CHANGE COLUMN `image_file_id` `image_file_ids` text
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
    NULL COMMENT '配图文件ID列表(JSON数组)';

-- 迁移已有数据：旧值转为单元素 JSON 数组
UPDATE `diary_entry`
SET `image_file_ids` = CONCAT('[', `image_file_ids`, ']')
WHERE `image_file_ids` IS NOT NULL
  AND `image_file_ids` NOT LIKE '[%';

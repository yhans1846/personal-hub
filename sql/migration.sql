-- 日记表增加地点字段
ALTER TABLE diary_entry ADD COLUMN `location` VARCHAR(200) DEFAULT NULL COMMENT '地点' AFTER `weather`;
-- 日记表增加图片文件ID
ALTER TABLE diary_entry ADD COLUMN `image_file_id` BIGINT DEFAULT NULL COMMENT '配图文件ID' AFTER `location`;

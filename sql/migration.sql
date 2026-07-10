-- 日记表增加地点字段
ALTER TABLE diary_entry ADD COLUMN `location` VARCHAR(200) DEFAULT NULL COMMENT '地点' AFTER `weather`;
-- 日记表增加图片文件ID
ALTER TABLE diary_entry ADD COLUMN `image_file_id` BIGINT DEFAULT NULL COMMENT '配图文件ID' AFTER `location`;
-- 阅读记录表增加评分和阅读时长
ALTER TABLE reading_record ADD COLUMN `rating` TINYINT DEFAULT NULL COMMENT '评分 1-5' AFTER `progress`;
ALTER TABLE reading_record ADD COLUMN `total_duration` INT DEFAULT NULL COMMENT '总阅读时长（分钟）' AFTER `rating`;

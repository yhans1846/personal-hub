-- 日记表增加地点字段
ALTER TABLE diary_entry ADD COLUMN `location` VARCHAR(200) DEFAULT NULL COMMENT '地点' AFTER `weather`;

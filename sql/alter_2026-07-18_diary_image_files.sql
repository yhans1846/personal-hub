-- 日记配图改为文件名列表（不进 file_resource）；不做内容迁移
ALTER TABLE `diary_entry`
  CHANGE COLUMN `image_file_ids` `image_files` text NULL COMMENT '配图文件名列表(JSON字符串数组)';

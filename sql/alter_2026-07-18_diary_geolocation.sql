-- 日记定位：仅存经纬度，不接地图反查
ALTER TABLE `diary_entry`
  ADD COLUMN `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度' AFTER `location`,
  ADD COLUMN `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度' AFTER `latitude`;

-- 通知软清除：清空后不再展示，也不再自动生成同类型同关联通知
ALTER TABLE `sys_notification`
  ADD COLUMN `is_dismissed` tinyint NOT NULL DEFAULT 0 COMMENT '0-正常 1-已清空/忽略' AFTER `is_read`;

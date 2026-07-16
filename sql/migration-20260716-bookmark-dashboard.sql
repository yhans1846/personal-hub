-- 收藏夹：首页外部快捷展示开关
ALTER TABLE `bookmark_url`
  ADD COLUMN `show_on_dashboard` tinyint NOT NULL DEFAULT 0 COMMENT '展示到首页外部快捷' AFTER `category_id`;

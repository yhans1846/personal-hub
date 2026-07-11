-- Personal Hub 分类系统合并迁移脚本
-- 将 note_category、bookmark_category、file_category 三表合并为统一的 category 表
-- 注意：在运行此脚本前请先备份数据库

START TRANSACTION;

-- 1. 创建统一分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `type` varchar(20) NOT NULL COMMENT '分类类型: note, bookmark, file',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_type` (`type`),
  INDEX `idx_user_type` (`user_id`, `type`),
  UNIQUE INDEX `uk_user_type_name` (`user_id`, `type`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一分类表';

-- 2. 迁移笔记分类（保留原 ID，因为 note_category_rel 引用了它们）
INSERT INTO `category` (id, user_id, name, type, sort_order, created_at, updated_at)
SELECT id, user_id, name, 'note', sort_order, created_at, updated_at FROM note_category;

-- 3. 迁移书签分类（用新 ID）
INSERT INTO `category` (user_id, name, type, sort_order, created_at, updated_at)
SELECT user_id, name, 'bookmark', sort_order, created_at, updated_at FROM bookmark_category;

-- 4. 迁移文件分类（用新 ID）
INSERT INTO `category` (user_id, name, type, sort_order, created_at, updated_at)
SELECT user_id, name, 'file', sort_order, created_at, updated_at FROM file_category;

-- 5. 更新 bookmark_url 的外键引用
UPDATE bookmark_url u
INNER JOIN bookmark_category bc ON u.category_id = bc.id
INNER JOIN category c ON c.user_id = bc.user_id AND c.name = bc.name AND c.type = 'bookmark'
SET u.category_id = c.id
WHERE u.category_id IS NOT NULL;

-- 6. 更新 file_resource 的外键引用
UPDATE file_resource f
INNER JOIN file_category fc ON f.category_id = fc.id
INNER JOIN category c ON c.user_id = fc.user_id AND c.name = fc.name AND c.type = 'file'
SET f.category_id = c.id
WHERE f.category_id IS NOT NULL;

-- 7. 删除旧表（分类）
DROP TABLE IF EXISTS `note_category`;
DROP TABLE IF EXISTS `bookmark_category`;
DROP TABLE IF EXISTS `file_category`;

-- 8. 删除废弃表（旧标签系统，已由统一 tag + tag_rel 替代）
DROP TABLE IF EXISTS `note_tag_rel`;
DROP TABLE IF EXISTS `note_tag`;

COMMIT;

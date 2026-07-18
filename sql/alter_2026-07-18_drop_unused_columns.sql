-- 删除未使用字段（与 init.sql 对齐）
-- bookmark_url.tags：遗留逗号分隔标签，已由 tag_rel 替代
-- bookmark_url.favicon：从未写入，列表图标由前端按域名拉取
-- file_resource.source / ref_id / stored_name：从未参与业务读写（stored_name 与 path 末段冗余）
-- study_record.plan_id：前后端均未使用
-- audit_log.business_id：保留（写入；供后续活动深链）

ALTER TABLE `bookmark_url` DROP COLUMN `tags`;
ALTER TABLE `bookmark_url` DROP COLUMN `favicon`;
ALTER TABLE `file_resource` DROP COLUMN `source`, DROP COLUMN `ref_id`, DROP COLUMN `stored_name`;
ALTER TABLE `study_record` DROP COLUMN `plan_id`;

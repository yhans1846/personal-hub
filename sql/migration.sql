-- Personal Hub 数据库变更脚本
-- 所有已执行的变更已合并到 init.sql，此文件暂空

-- ========================================
-- 2026-07-11: 文件存储重构 - 新增字段
-- ========================================
ALTER TABLE file_resource
    ADD COLUMN source VARCHAR(20) DEFAULT NULL COMMENT '上传来源: upload/diary/avatar',
    ADD COLUMN ref_id BIGINT DEFAULT NULL COMMENT '关联实体ID(diary_id等)';

ALTER TABLE note_note
    ADD COLUMN md_path VARCHAR(500) DEFAULT NULL COMMENT '笔记MD文件路径(notes/{id}/note.md)';

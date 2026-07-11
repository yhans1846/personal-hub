-- ============================================================
-- Migration: 笔记回收站与审计日志改造
-- Date: 2026-07-12
-- ============================================================

-- 1. note_note 新增删除时间和删除原因字段
ALTER TABLE note_note
    ADD COLUMN deleted_at DATETIME NULL COMMENT '删除时间' AFTER is_deleted,
    ADD COLUMN delete_reason VARCHAR(50) NULL COMMENT '删除原因（USER_DELETE/AUTO_ARCHIVE）' AFTER deleted_at;

-- 2. 创建统一审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    module      VARCHAR(50)  NOT NULL COMMENT '业务模块（NOTE/TODO/FILE/STUDY/READING/DIARY/BOOKMARK/TAG/USER）',
    business_id BIGINT       NULL COMMENT '业务ID',
    action      VARCHAR(50)  NOT NULL COMMENT '操作类型（DELETE/RESTORE/CREATE/UPDATE/LOGIN/EXPORT...）',
    content     VARCHAR(500) NOT NULL COMMENT '操作描述',
    operator_id BIGINT       NOT NULL COMMENT '操作用户ID',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_module   (module),
    INDEX idx_business (module, business_id),
    INDEX idx_operator (operator_id),
    INDEX idx_created  (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一审计日志';

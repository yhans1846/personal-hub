-- ==========================================
-- Dashboard Customization: User Layout Config
-- ==========================================
CREATE TABLE IF NOT EXISTS `user_layout` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '所属用户',
    `layout_type` VARCHAR(30) NOT NULL COMMENT '类型: menu / dashboard',
    `layout_json` TEXT NOT NULL COMMENT '布局配置 JSON',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY `uk_user_layout` (`user_id`, `layout_type`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户布局配置';

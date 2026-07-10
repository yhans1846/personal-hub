-- Personal Hub 数据库初始化脚本
-- 运行前请确保数据库 personal_hub 已创建

-- ========================================
-- 1. 用户表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`     VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `email`      VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 2. 笔记表
-- ========================================
CREATE TABLE IF NOT EXISTS `note_note` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '所属用户',
    `title`       VARCHAR(200) NOT NULL COMMENT '笔记标题',
    `content`     LONGTEXT     DEFAULT NULL COMMENT 'Markdown内容',
    `is_favorite` TINYINT      NOT NULL DEFAULT 0 COMMENT '是否收藏',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';

-- ========================================
-- 3. 笔记分类表
-- ========================================
CREATE TABLE IF NOT EXISTS `note_category` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT      NOT NULL COMMENT '所属用户',
    `name`       VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort_order` INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记分类表';

-- ========================================
-- 4. 笔记标签表
-- ========================================
CREATE TABLE IF NOT EXISTS `note_tag` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT      NOT NULL COMMENT '所属用户',
    `name`       VARCHAR(50) NOT NULL COMMENT '标签名称',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_id_name` (`user_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记标签表';

-- ========================================
-- 5. 笔记-分类关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `note_category_rel` (
    `note_id`     BIGINT NOT NULL COMMENT '笔记ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    PRIMARY KEY (`note_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记-分类关联表';

-- ========================================
-- 6. 笔记-标签关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `note_tag_rel` (
    `note_id` BIGINT NOT NULL COMMENT '笔记ID',
    `tag_id`  BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`note_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记-标签关联表';

-- ========================================
-- 7. 学习记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `study_record` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户',
    `subject`    VARCHAR(200) NOT NULL COMMENT '学习主题',
    `date`       DATE         NOT NULL COMMENT '学习日期',
    `duration`   INT          NOT NULL COMMENT '学习时长（分钟）',
    `content`    TEXT         DEFAULT NULL COMMENT '学习内容',
    `reflection` TEXT         DEFAULT NULL COMMENT '学习心得',
    `is_deleted` TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id_date` (`user_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- ========================================
-- 8. 待办表
-- ========================================
CREATE TABLE IF NOT EXISTS `todo_task` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户',
    `title`      VARCHAR(200) NOT NULL COMMENT '任务标题',
    `content`    TEXT         DEFAULT NULL COMMENT '任务内容',
    `is_done`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否完成',
    `priority`   TINYINT      NOT NULL DEFAULT 2 COMMENT '优先级 1-高 2-中 3-低',
    `due_date`   DATE         DEFAULT NULL COMMENT '截止日期',
    `is_deleted` TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办表';

-- ========================================
-- 9. 文件表
-- ========================================
CREATE TABLE IF NOT EXISTS `file_resource` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '所属用户',
    `name`        VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_name` VARCHAR(255) NOT NULL COMMENT '存储文件名（UUID）',
    `path`        VARCHAR(500) NOT NULL COMMENT '存储路径',
    `size`        BIGINT       NOT NULL COMMENT '文件大小（字节）',
    `type`        VARCHAR(50)  NOT NULL COMMENT '文件扩展名',
    `mime_type`   VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `category_id` BIGINT       DEFAULT NULL COMMENT '文件分类ID',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- ========================================
-- 10. 文件分类表
-- ========================================
CREATE TABLE IF NOT EXISTS `file_category` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT      NOT NULL COMMENT '所属用户',
    `name`       VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort_order` INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分类表';

-- ========================================
-- 11. 日记表
-- ========================================
CREATE TABLE IF NOT EXISTS `diary_entry` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户',
    `date`       DATE         NOT NULL COMMENT '日记日期',
    `title`      VARCHAR(200) DEFAULT NULL COMMENT '日记标题',
    `content`    TEXT         DEFAULT NULL COMMENT '日记内容（Markdown）',
    `mood`       TINYINT      DEFAULT NULL COMMENT '心情 1-很好 2-好 3-一般 4-不好 5-很差',
    `weather`    VARCHAR(50)  DEFAULT NULL COMMENT '天气',
    `is_deleted` TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id_date` (`user_id`, `date`),
    INDEX `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日记表';

-- ========================================
-- 12. 收藏夹分类表
-- ========================================
CREATE TABLE IF NOT EXISTS `bookmark_category` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT      NOT NULL COMMENT '所属用户',
    `name`       VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort_order` INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹分类表';

-- ========================================
-- 13. 收藏夹表
-- ========================================
CREATE TABLE IF NOT EXISTS `bookmark_url` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '所属用户',
    `title`       VARCHAR(255) NOT NULL COMMENT '标题',
    `url`         VARCHAR(2048) NOT NULL COMMENT '网址',
    `description` TEXT         DEFAULT NULL COMMENT '描述',
    `favicon`     VARCHAR(500) DEFAULT NULL COMMENT '图标URL',
    `category_id` BIGINT       DEFAULT NULL COMMENT '分类ID',
    `tags`        VARCHAR(500) DEFAULT NULL COMMENT '标签（逗号分隔）',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹表';

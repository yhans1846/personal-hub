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
    `md_path`     VARCHAR(500) DEFAULT NULL COMMENT '笔记MD文件路径(notes/{id}/note.md)',
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
    `plan_id`    BIGINT       DEFAULT NULL COMMENT '关联学习计划ID',
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
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT       NOT NULL COMMENT '所属用户',
    `date`          DATE         NOT NULL COMMENT '日记日期',
    `title`         VARCHAR(200) DEFAULT NULL COMMENT '日记标题',
    `content`       TEXT         DEFAULT NULL COMMENT '日记内容（Markdown）',
    `mood`          TINYINT      DEFAULT NULL COMMENT '心情 1-很好 2-好 3-一般 4-不好 5-很差',
    `weather`       VARCHAR(50)  DEFAULT NULL COMMENT '天气',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `location`      VARCHAR(255) DEFAULT NULL COMMENT '地点',
    `image_file_id` BIGINT       DEFAULT NULL COMMENT '配图文件ID',
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

-- ========================================
-- 14. 学习计划表
-- ========================================
CREATE TABLE IF NOT EXISTS `study_plan` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户',
    `name`       VARCHAR(200) NOT NULL COMMENT '计划名称',
    `goal`       TEXT         DEFAULT NULL COMMENT '学习目标',
    `progress`   INT          NOT NULL DEFAULT 0 COMMENT '进度百分比 0-100',
    `start_date` DATE         DEFAULT NULL COMMENT '开始日期',
    `end_date`   DATE         DEFAULT NULL COMMENT '结束日期',
    `status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0-未开始 1-进行中 2-已完成 3-已放弃',
    `is_deleted` TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习计划表';

-- ========================================
-- 15. 阅读记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `reading_record` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`         BIGINT       NOT NULL COMMENT '所属用户',
    `book_title`      VARCHAR(255) NOT NULL COMMENT '书名',
    `author`          VARCHAR(200) DEFAULT NULL COMMENT '作者',
    `cover_url`       VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `total_chapters`  INT          NOT NULL DEFAULT 0 COMMENT '总章节数',
    `current_chapter` INT          NOT NULL DEFAULT 0 COMMENT '当前章节',
    `progress`        INT          NOT NULL DEFAULT 0 COMMENT '阅读进度 0-100',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0-未读 1-在读 2-读完',
    `notes`           TEXT         DEFAULT NULL COMMENT '阅读笔记',
    `start_date`      DATE         DEFAULT NULL COMMENT '开始阅读日期',
    `end_date`        DATE         DEFAULT NULL COMMENT '读完日期',
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `total_duration`  INT          DEFAULT 0 COMMENT '总阅读时长（分钟）',
    `rating`          INT          DEFAULT NULL COMMENT '评分 1-5',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读记录表';

-- ========================================
-- 16. 统一标签表
-- ========================================
CREATE TABLE IF NOT EXISTS `tag` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT      NOT NULL COMMENT '所属用户',
    `name`       VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color`      VARCHAR(7)  NOT NULL DEFAULT '#409eff' COMMENT '标签颜色',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_tag_name` (`user_id`, `name`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一标签表';

-- ========================================
-- 17. 标签关联表（多态关联）
-- ========================================
CREATE TABLE IF NOT EXISTS `tag_rel` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tag_id`      BIGINT      NOT NULL COMMENT '标签ID',
    `entity_type` VARCHAR(50) NOT NULL COMMENT '关联实体类型(note/bookmark/diary/study/todo/file/reading/study_plan)',
    `entity_id`   BIGINT      NOT NULL COMMENT '关联实体ID',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_entity` (`tag_id`, `entity_type`, `entity_id`),
    INDEX `idx_entity` (`entity_type`, `entity_id`),
    INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签关联表';

-- ========================================
-- 18. 系统通知表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_notification` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT       NOT NULL COMMENT '所属用户',
    `type`         VARCHAR(50)  NOT NULL COMMENT '通知类型(TODO_OVERDUE/PLAN_DEADLINE/PLAN_COMPLETED)',
    `title`        VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content`      TEXT         DEFAULT NULL COMMENT '通知内容',
    `is_read`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    `related_id`   BIGINT       DEFAULT NULL COMMENT '关联实体ID',
    `related_type` VARCHAR(50)  DEFAULT NULL COMMENT '关联实体类型',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_read` (`user_id`, `is_read`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- ========================================
-- 迁移：笔记标签 → 统一标签系统
-- ========================================
INSERT IGNORE INTO `tag` (`id`, `user_id`, `name`, `color`)
SELECT `id`, `user_id`, `name`, '#409eff' FROM `note_tag`;

INSERT IGNORE INTO `tag_rel` (`tag_id`, `entity_type`, `entity_id`)
SELECT `tag_id`, 'note', `note_id` FROM `note_tag_rel`;

-- ========================================
-- 19. 用户布局配置表
-- ========================================
CREATE TABLE IF NOT EXISTS `user_layout` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT      NOT NULL COMMENT '所属用户',
    `layout_type` VARCHAR(30) NOT NULL COMMENT '类型: menu / dashboard',
    `layout_json` TEXT        NOT NULL COMMENT '布局配置 JSON',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_layout` (`user_id`, `layout_type`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户布局配置表';

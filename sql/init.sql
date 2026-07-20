-- Personal Hub 数据库初始化脚本
-- 表结构以 live personal_hub 库为准同步
CREATE DATABASE IF NOT EXISTS personal_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bookmark_url
-- ----------------------------
DROP TABLE IF EXISTS `bookmark_url`;
CREATE TABLE `bookmark_url` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `url` varchar(2048) NOT NULL COMMENT '网址',
  `description` text COMMENT '描述',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `show_on_dashboard` tinyint NOT NULL DEFAULT '0' COMMENT '展示到首页外部快捷',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_category_id` (`category_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='收藏夹表';

-- ----------------------------
-- Records of bookmark_url
-- ----------------------------

-- ----------------------------
-- Table structure for diary_entry
-- ----------------------------
DROP TABLE IF EXISTS `diary_entry`;
CREATE TABLE `diary_entry` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `date` date NOT NULL COMMENT '日记日期',
  `title` varchar(200) DEFAULT NULL COMMENT '日记标题',
  `content` text COMMENT '日记内容（Markdown）',
  `mood` tinyint DEFAULT NULL COMMENT '心情 1-很好 2-好 3-一般 4-不好 5-很差',
  `weather` varchar(50) DEFAULT NULL COMMENT '天气',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `location` varchar(255) DEFAULT NULL COMMENT '地点',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `image_files` text COMMENT '配图文件名列表(JSON字符串数组)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id_date` (`user_id`,`date`) USING BTREE,
  KEY `idx_date` (`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='日记表';

-- ----------------------------
-- Records of diary_entry
-- ----------------------------

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `type` varchar(20) NOT NULL COMMENT '分类类型: note, bookmark, file',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_type_name` (`user_id`,`type`,`name`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_user_type` (`user_id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='统一分类表';

-- ----------------------------
-- Records of category
-- ----------------------------

-- ----------------------------
-- Table structure for file_resource
-- ----------------------------
DROP TABLE IF EXISTS `file_resource`;
CREATE TABLE `file_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `name` varchar(255) NOT NULL COMMENT '原始文件名',
  `path` varchar(500) NOT NULL COMMENT '存储路径',
  `size` bigint NOT NULL COMMENT '文件大小（字节）',
  `type` varchar(50) NOT NULL COMMENT '文件扩展名',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `category_id` bigint DEFAULT NULL COMMENT '文件分类ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='文件表';

-- ----------------------------
-- Records of file_resource
-- ----------------------------

-- ----------------------------
-- Table structure for note_category_rel
-- ----------------------------
DROP TABLE IF EXISTS `note_category_rel`;
CREATE TABLE `note_category_rel` (
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  PRIMARY KEY (`note_id`,`category_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='笔记-分类关联表';

-- ----------------------------
-- Records of note_category_rel
-- ----------------------------

-- ----------------------------
-- Table structure for note_note
-- ----------------------------
DROP TABLE IF EXISTS `note_note`;
CREATE TABLE `note_note` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `title` varchar(200) NOT NULL COMMENT '笔记标题',
  `md_path` varchar(500) DEFAULT NULL COMMENT '笔记MD文件路径(notes/{id}/note.md)',
  `excerpt` varchar(500) DEFAULT NULL COMMENT '列表摘要',
  `is_favorite` tinyint NOT NULL DEFAULT '0' COMMENT '是否收藏',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
  `delete_reason` varchar(50) COMMENT '删除原因（USER_DELETE/AUTO_ARCHIVE）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_updated_at` (`updated_at`) USING BTREE,
  KEY `idx_user_deleted_updated` (`user_id`,`is_deleted`,`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='笔记表';

-- ----------------------------
-- Records of note_note
-- ----------------------------

-- ----------------------------
-- Table structure for reading_record
-- ----------------------------
DROP TABLE IF EXISTS `reading_record`;
CREATE TABLE `reading_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `book_title` varchar(255) NOT NULL COMMENT '书名',
  `author` varchar(200) DEFAULT NULL COMMENT '作者',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图',
  `total_chapters` int NOT NULL DEFAULT '0' COMMENT '总章节数',
  `current_chapter` int NOT NULL DEFAULT '0' COMMENT '当前章节',
  `progress` int NOT NULL DEFAULT '0' COMMENT '阅读进度 0-100',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-未读 1-在读 2-读完',
  `notes` text COMMENT '阅读笔记',
  `start_date` date DEFAULT NULL COMMENT '开始阅读日期',
  `end_date` date DEFAULT NULL COMMENT '读完日期',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `total_duration` int DEFAULT '0' COMMENT '总阅读时长(分钟)',
  `rating` int DEFAULT NULL COMMENT '评分',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='阅读记录表';

-- ----------------------------
-- Records of reading_record
-- ----------------------------

-- ----------------------------
-- Table structure for study_plan
-- ----------------------------
DROP TABLE IF EXISTS `study_plan`;
CREATE TABLE `study_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `name` varchar(200) NOT NULL COMMENT '计划名称',
  `source` varchar(100) DEFAULT NULL COMMENT 'source',
  `author` varchar(100) DEFAULT NULL COMMENT 'author',
  `url` varchar(500) DEFAULT NULL COMMENT 'url',
  `remark` text COMMENT 'remark',
  `progress` int NOT NULL DEFAULT '0' COMMENT 'progress 0-100',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-未开始 1-进行中 2-已完成 3-已放弃',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='学习计划表';

-- ----------------------------
-- Records of study_plan
-- ----------------------------

-- ----------------------------
-- Table structure for study_record
-- ----------------------------
DROP TABLE IF EXISTS `study_record`;
CREATE TABLE `study_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `subject` varchar(200) NOT NULL COMMENT '学习主题',
  `date` date NOT NULL COMMENT '学习日期',
  `duration` int NOT NULL COMMENT '学习时长（分钟）',
  `content` text COMMENT '学习内容',
  `reflection` text COMMENT '学习心得',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id_date` (`user_id`,`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='学习记录表';

-- ----------------------------
-- Records of study_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_notification
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `type` varchar(50) NOT NULL COMMENT '通知类型',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '0-未读 1-已读',
  `is_dismissed` tinyint NOT NULL DEFAULT '0' COMMENT '0-正常 1-已清空/忽略',
  `related_id` bigint DEFAULT NULL COMMENT '关联实体ID',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联实体类型',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_read` (`user_id`,`is_read`) USING BTREE,
  KEY `idx_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='系统通知表';

-- ----------------------------
-- Records of sys_notification
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0-保密 1-男 2-女',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `country` varchar(50) DEFAULT NULL COMMENT '国家',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区/县',
  `website` varchar(200) DEFAULT NULL COMMENT '个人网站',
  `github` varchar(200) DEFAULT NULL COMMENT 'GitHub 地址',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-正常 1-删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$IG8Fmoph4sKKEmPmjDat6.1x1wT9s2ghnoorQoADulBP1PxCPbCxC', '管理员', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-09 22:32:47', '2026-07-09 22:41:33', 0);

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `color` varchar(7) NOT NULL DEFAULT '#409eff' COMMENT '标签颜色',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_user_tag_name` (`user_id`,`name`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='统一标签表';

-- ----------------------------
-- Records of tag
-- ----------------------------

-- ----------------------------
-- Table structure for tag_rel
-- ----------------------------
DROP TABLE IF EXISTS `tag_rel`;
CREATE TABLE `tag_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `entity_type` varchar(50) NOT NULL COMMENT '关联实体类型(note/bookmark/diary/study/todo/file/reading/study_plan)',
  `entity_id` bigint NOT NULL COMMENT '关联实体ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tag_entity` (`tag_id`,`entity_type`,`entity_id`) USING BTREE,
  KEY `idx_entity` (`entity_type`,`entity_id`) USING BTREE,
  KEY `idx_tag_id` (`tag_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='标签关联表';

-- ----------------------------
-- Records of tag_rel
-- ----------------------------

-- ----------------------------
-- Table structure for todo_task
-- ----------------------------
DROP TABLE IF EXISTS `todo_task`;
CREATE TABLE `todo_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `title` varchar(200) NOT NULL COMMENT '任务标题',
  `content` text COMMENT '任务内容',
  `is_done` tinyint NOT NULL DEFAULT '0' COMMENT '是否完成',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '优先级 1-高 2-中 3-低',
  `due_date` date DEFAULT NULL COMMENT '截止日期',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_due_date` (`due_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='待办表';

-- ----------------------------
-- Records of todo_task
-- ----------------------------

-- ----------------------------
-- Table structure for user_layout
-- ----------------------------
DROP TABLE IF EXISTS `user_layout`;
CREATE TABLE `user_layout` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `layout_type` varchar(30) NOT NULL COMMENT '类型: menu/dashboard/stats | reading | appearance | data | advanced',
  `layout_json` text NOT NULL COMMENT '布局配置 JSON',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_layout` (`user_id`,`layout_type`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户布局配置';

INSERT INTO `user_layout` (`id`, `user_id`, `layout_type`, `layout_json`, `created_at`, `updated_at`, `is_deleted`) VALUES (1, 1, 'dashboard', '{\"items\":[{\"code\":\"today_plan\",\"visible\":true,\"order\":1},{\"code\":\"recent_studies\",\"visible\":true,\"order\":2},{\"code\":\"recent_notes\",\"visible\":false,\"order\":3},{\"code\":\"recent_reading\",\"visible\":true,\"order\":4},{\"code\":\"external_links\",\"visible\":true,\"order\":5},{\"code\":\"quick_actions\",\"visible\":true,\"order\":6}]}', NOW(), NOW(), 0);
INSERT INTO `user_layout` (`id`, `user_id`, `layout_type`, `layout_json`, `created_at`, `updated_at`, `is_deleted`) VALUES (2, 1, 'menu', '{\"items\":[{\"code\":\"dashboard\",\"visible\":true,\"order\":1},{\"code\":\"notes\",\"visible\":true,\"order\":2},{\"code\":\"diaries\",\"visible\":true,\"order\":3},{\"code\":\"readings\",\"visible\":true,\"order\":4},{\"code\":\"study-records\",\"visible\":true,\"order\":5},{\"code\":\"todos\",\"visible\":true,\"order\":6},{\"code\":\"study-plans\",\"visible\":true,\"order\":7},{\"code\":\"bookmarks\",\"visible\":true,\"order\":8},{\"code\":\"files\",\"visible\":true,\"order\":9},{\"code\":\"tags\",\"visible\":true,\"order\":10},{\"code\":\"categories\",\"visible\":true,\"order\":11},{\"code\":\"settings\",\"visible\":true,\"order\":12},{\"code\":\"recycle\",\"visible\":true,\"order\":13},{\"code\":\"stats\",\"visible\":true,\"order\":14}]}', NOW(), NOW(), 0);
INSERT INTO `user_layout` (`id`, `user_id`, `layout_type`, `layout_json`, `created_at`, `updated_at`, `is_deleted`) VALUES (3, 1, 'reading', '{\"fontSize\":14,\"readingWidth\":1280,\"lineHeight\":2,\"theme\":\"sepia\",\"imageMaxWidth\":100,\"paragraphGap\":1,\"codeFontSize\":\"14px\",\"codeFontFamily\":\"monospace\"}', NOW(), NOW(), 0);
INSERT INTO `user_layout` (`id`, `user_id`, `layout_type`, `layout_json`, `created_at`, `updated_at`, `is_deleted`) VALUES (6, 1, 'appearance', '{\"theme\":\"light\",\"accent\":\"purple\",\"borderRadius\":\"xl\",\"animationSpeed\":\"slow\",\"density\":\"compact\",\"contentWidth\":95}', NOW(), NOW(), 0);
INSERT INTO `user_layout` (`id`, `user_id`, `layout_type`, `layout_json`, `created_at`, `updated_at`, `is_deleted`) VALUES (7, 1, 'advanced', '{\"desktopEnabled\":true,\"enabledTypes\":[\"note_reminder\",\"todo_due\",\"study_progress\",\"system\"],\"soundEnabled\":true,\"soundName\":\"default\",\"doNotDisturb\":false,\"dndStart\":\"22:00\",\"dndEnd\":\"08:00\"}', NOW(), NOW(), 0);
INSERT INTO `user_layout` (`id`, `user_id`, `layout_type`, `layout_json`, `created_at`, `updated_at`, `is_deleted`) VALUES (8, 1, 'data', '{\"frequency\":\"daily\"}', NOW(), NOW(), 0);


-- ----------------------------
-- Records of user_layout
-- ----------------------------

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `module` varchar(50) NOT NULL COMMENT '业务模块（NOTE/TODO/FILE/STUDY/READING/DIARY/BOOKMARK/TAG/USER）',
  `business_id` bigint DEFAULT NULL COMMENT '业务ID',
  `action` varchar(50) NOT NULL COMMENT '操作类型（DELETE/RESTORE/CREATE/UPDATE/LOGIN/EXPORT...）',
  `content` varchar(500) NOT NULL COMMENT '操作描述',
  `operator_id` bigint NOT NULL COMMENT '操作用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_module` (`module`),
  KEY `idx_business` (`module`,`business_id`),
  KEY `idx_operator` (`operator_id`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='统一审计日志';

-- ----------------------------
-- Records of audit_log
-- ----------------------------

-- ----------------------------
-- Table structure for user_backup
-- ----------------------------
DROP TABLE IF EXISTS `user_backup`;
CREATE TABLE `user_backup` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `file_path` varchar(512) NOT NULL DEFAULT '' COMMENT '相对存储根路径',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '字节',
  `trigger_type` varchar(16) NOT NULL COMMENT 'MANUAL / AUTO',
  `status` varchar(16) NOT NULL COMMENT 'OK / FAILED',
  `error_message` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_created` (`user_id`,`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户备份历史';

SET FOREIGN_KEY_CHECKS = 1;

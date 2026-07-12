-- Personal Hub 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS personal_hub DEFAULT CHARACTER SET utf8mb4;




SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bookmark_url
-- ----------------------------
DROP TABLE IF EXISTS `bookmark_url`;
CREATE TABLE `bookmark_url`  (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `user_id` bigint NOT NULL COMMENT '所属用户',
                                 `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
                                 `url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '网址',
                                 `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
                                 `favicon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标URL',
                                 `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
                                 `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签（逗号分隔）',
                                 `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                 `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                 INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏夹表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bookmark_url
-- ----------------------------

-- ----------------------------
-- Table structure for diary_entry
-- ----------------------------
DROP TABLE IF EXISTS `diary_entry`;
CREATE TABLE `diary_entry`  (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `user_id` bigint NOT NULL COMMENT '所属用户',
                                `date` date NOT NULL COMMENT '日记日期',
                                `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日记标题',
                                `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '日记内容（Markdown）',
                                `mood` tinyint NULL DEFAULT NULL COMMENT '心情 1-很好 2-好 3-一般 4-不好 5-很差',
                                `weather` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '天气',
                                `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地点',
                                `image_file_id` bigint NULL DEFAULT NULL COMMENT '配图文件ID',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_user_id_date`(`user_id` ASC, `date` ASC) USING BTREE,
                                INDEX `idx_date`(`date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '日记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of diary_entry
-- ----------------------------

-- ----------------------------
-- Table structure for category（统一分类）
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `user_id` bigint NOT NULL COMMENT '所属用户',
                             `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
                             `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类类型: note, bookmark, file',
                             `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
                             `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                             INDEX `idx_type`(`type` ASC) USING BTREE,
                             INDEX `idx_user_type`(`user_id` ASC, `type` ASC) USING BTREE,
                             UNIQUE INDEX `uk_user_type_name`(`user_id` ASC, `type` ASC, `name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, 1, '技术笔记', 'note', 0, '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `category` VALUES (2, 1, '生活记录', 'note', 1, '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `category` VALUES (3, 1, '读书笔记', 'note', 2, '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `category` VALUES (4, 1, '开发工具', 'bookmark', 0, '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `category` VALUES (5, 1, '学习资源', 'bookmark', 1, '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `category` VALUES (6, 1, '常用网站', 'bookmark', 2, '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `category` VALUES (7, 1, '文档', 'file', 0, '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `category` VALUES (8, 1, '图片', 'file', 1, '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `category` VALUES (9, 1, '其他', 'file', 2, '2026-07-11 14:34:19', '2026-07-11 14:34:19');

-- ----------------------------
-- Table structure for file_resource
-- ----------------------------
DROP TABLE IF EXISTS `file_resource`;
CREATE TABLE `file_resource`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `user_id` bigint NOT NULL COMMENT '所属用户',
                                  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
                                  `stored_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储文件名（UUID）',
                                  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储路径',
                                  `size` bigint NOT NULL COMMENT '文件大小（字节）',
                                  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件扩展名',
                                  `mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MIME类型',
                                  `category_id` bigint NULL DEFAULT NULL COMMENT '文件分类ID',
                                  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传来源: upload/diary/avatar',
                                  `ref_id` bigint NULL DEFAULT NULL COMMENT '关联实体ID(diary_id等)',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_resource
-- ----------------------------

-- ----------------------------
-- Table structure for note_category_rel
-- ----------------------------
DROP TABLE IF EXISTS `note_category_rel`;
CREATE TABLE `note_category_rel`  (
                                      `note_id` bigint NOT NULL COMMENT '笔记ID',
                                      `category_id` bigint NOT NULL COMMENT '分类ID',
                                      PRIMARY KEY (`note_id`, `category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '笔记-分类关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of note_category_rel
-- ----------------------------

-- ----------------------------
-- Table structure for note_note
-- ----------------------------
DROP TABLE IF EXISTS `note_note`;
CREATE TABLE `note_note`  (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                              `user_id` bigint NOT NULL COMMENT '所属用户',
                              `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '笔记标题',
                              `md_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '笔记MD文件路径(notes/{id}/note.md)',
                              `is_favorite` tinyint NOT NULL DEFAULT 0 COMMENT '是否收藏',
                              `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                              `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
                              `delete_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '删除原因（USER_DELETE/AUTO_ARCHIVE）',
                              `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                              INDEX `idx_updated_at`(`updated_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '笔记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of note_note
-- ----------------------------

-- ----------------------------
-- Table structure for reading_record
-- ----------------------------
DROP TABLE IF EXISTS `reading_record`;
CREATE TABLE `reading_record`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `user_id` bigint NOT NULL COMMENT '所属用户',
                                   `book_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '书名',
                                   `author` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者',
                                   `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图',
                                   `total_chapters` int NOT NULL DEFAULT 0 COMMENT '总章节数',
                                   `current_chapter` int NOT NULL DEFAULT 0 COMMENT '当前章节',
                                   `progress` int NOT NULL DEFAULT 0 COMMENT '阅读进度 0-100',
                                   `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0-未读 1-在读 2-读完',
                                   `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '阅读笔记',
                                   `start_date` date NULL DEFAULT NULL COMMENT '开始阅读日期',
                                   `end_date` date NULL DEFAULT NULL COMMENT '读完日期',
                                   `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                   `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `total_duration` int NULL DEFAULT 0 COMMENT '总阅读时长(分钟)',
                                   `rating` int NULL DEFAULT NULL COMMENT '评分',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '阅读记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reading_record
-- ----------------------------

-- ----------------------------
-- Table structure for study_plan
-- ----------------------------
DROP TABLE IF EXISTS `study_plan`;
CREATE TABLE `study_plan`  (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `user_id` bigint NOT NULL COMMENT '所属用户',
                               `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计划名称',
                               `goal` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '学习目标',
                               `progress` int NOT NULL DEFAULT 0 COMMENT '进度百分比 0-100',
                               `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
                               `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
                               `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0-未开始 1-进行中 2-已完成 3-已放弃',
                               `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                               `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of study_plan
-- ----------------------------

-- ----------------------------
-- Table structure for study_record
-- ----------------------------
DROP TABLE IF EXISTS `study_record`;
CREATE TABLE `study_record`  (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `user_id` bigint NOT NULL COMMENT '所属用户',
                                 `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学习主题',
                                 `date` date NOT NULL COMMENT '学习日期',
                                 `duration` int NOT NULL COMMENT '学习时长（分钟）',
                                 `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '学习内容',
                                 `reflection` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '学习心得',
                                 `plan_id` bigint NULL DEFAULT NULL COMMENT '关联学习计划ID',
                                 `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                 `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 INDEX `idx_user_id_date`(`user_id` ASC, `date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of study_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_notification
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `user_id` bigint NOT NULL COMMENT '所属用户',
                                     `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知类型',
                                     `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
                                     `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '通知内容',
                                     `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '0-未读 1-已读',
                                     `related_id` bigint NULL DEFAULT NULL COMMENT '关联实体ID',
                                     `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联实体类型',
                                     `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
                                     INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notification
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
                             `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（BCrypt加密）',
                             `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
                             `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
                             `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
                             `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$IG8Fmoph4sKKEmPmjDat6.1x1wT9s2ghnoorQoADulBP1PxCPbCxC', '管理员', NULL, NULL, '2026-07-09 22:32:47', '2026-07-09 22:41:33', 0);

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                        `user_id` bigint NOT NULL COMMENT '所属用户',
                        `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
                        `color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '#409eff' COMMENT '标签颜色',
                        `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`) USING BTREE,
                        UNIQUE INDEX `idx_user_tag_name`(`user_id` ASC, `name` ASC) USING BTREE,
                        INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES (1, 1, 'Java', '#409eff', '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `tag` VALUES (2, 1, 'Spring', '#67c23a', '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `tag` VALUES (3, 1, '前端', '#e6a23c', '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `tag` VALUES (4, 1, '数据库', '#f56c6c', '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `tag` VALUES (5, 1, '部署', '#909399', '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `tag` VALUES (6, 1, '读书', '#8b5cf6', '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `tag` VALUES (7, 1, '设计', '#14b8a6', '2026-07-11 14:34:19', '2026-07-11 14:34:19');
INSERT INTO `tag` VALUES (8, 1, '日记', '#f97316', '2026-07-11 14:34:19', '2026-07-11 14:34:19');

-- ----------------------------
-- Table structure for tag_rel
-- ----------------------------
DROP TABLE IF EXISTS `tag_rel`;
CREATE TABLE `tag_rel`  (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `tag_id` bigint NOT NULL COMMENT '标签ID',
                            `entity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联实体类型(note/bookmark/diary/study/todo/file/reading/study_plan)',
                            `entity_id` bigint NOT NULL COMMENT '关联实体ID',
                            `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (`id`) USING BTREE,
                            UNIQUE INDEX `uk_tag_entity`(`tag_id` ASC, `entity_type` ASC, `entity_id` ASC) USING BTREE,
                            INDEX `idx_entity`(`entity_type` ASC, `entity_id` ASC) USING BTREE,
                            INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag_rel
-- ----------------------------

-- ----------------------------
-- Table structure for todo_task
-- ----------------------------
DROP TABLE IF EXISTS `todo_task`;
CREATE TABLE `todo_task`  (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                              `user_id` bigint NOT NULL COMMENT '所属用户',
                              `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务标题',
                              `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务内容',
                              `is_done` tinyint NOT NULL DEFAULT 0 COMMENT '是否完成',
                              `priority` tinyint NOT NULL DEFAULT 2 COMMENT '优先级 1-高 2-中 3-低',
                              `due_date` date NULL DEFAULT NULL COMMENT '截止日期',
                              `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                              `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                              INDEX `idx_due_date`(`due_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '待办表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of todo_task
-- ----------------------------

-- ----------------------------
-- Table structure for user_layout
-- ----------------------------
DROP TABLE IF EXISTS `user_layout`;
CREATE TABLE `user_layout`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL COMMENT '所属用户',
                                `layout_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型: menu / dashboard',
                                `layout_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '布局配置 JSON',
                                `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                `is_deleted` tinyint NOT NULL DEFAULT 0,
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_user_layout`(`user_id` ASC, `layout_type` ASC) USING BTREE,
                                INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户布局配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_layout
-- ----------------------------
INSERT INTO `user_layout` VALUES (1, 1, 'dashboard', '{\"items\":[{\"code\":\"today_plan\",\"visible\":true,\"order\":1},{\"code\":\"pending_todos\",\"visible\":true,\"order\":2},{\"code\":\"recent_notes\",\"visible\":true,\"order\":3},{\"code\":\"recent_studies\",\"visible\":true,\"order\":4},{\"code\":\"recent_bookmarks\",\"visible\":false,\"order\":5},{\"code\":\"recent_reading\",\"visible\":false,\"order\":6}]}', '2026-07-11 12:43:52', '2026-07-11 12:43:52', 0);
INSERT INTO `user_layout` VALUES (2, 1, 'menu', '{\"items\":[{\"code\":\"dashboard\",\"visible\":true,\"order\":1},{\"code\":\"todos\",\"visible\":true,\"order\":2},{\"code\":\"notes\",\"visible\":true,\"order\":3},{\"code\":\"diaries\",\"visible\":true,\"order\":4},{\"code\":\"readings\",\"visible\":true,\"order\":5},{\"code\":\"study-records\",\"visible\":true,\"order\":6},{\"code\":\"study-plans\",\"visible\":true,\"order\":7},{\"code\":\"bookmarks\",\"visible\":true,\"order\":8},{\"code\":\"files\",\"visible\":true,\"order\":9},{\"code\":\"categories\",\"visible\":true,\"order\":10},{\"code\":\"tags\",\"visible\":true,\"order\":11},{\"code\":\"settings\",\"visible\":true,\"order\":12},{\"code\":\"recycle\",\"visible\":true,\"order\":15},{\"code\":\"stats\",\"visible\":true,\"order\":16}]}', '2026-07-11 12:44:02', '2026-07-11 12:44:02', 0);

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`  (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                              `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务模块（NOTE/TODO/FILE/STUDY/READING/DIARY/BOOKMARK/TAG/USER）',
                              `business_id` bigint NULL DEFAULT NULL COMMENT '业务ID',
                              `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型（DELETE/RESTORE/CREATE/UPDATE/LOGIN/EXPORT...）',
                              `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作描述',
                              `operator_id` bigint NOT NULL COMMENT '操作用户ID',
                              `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `idx_module`(`module` ASC) USING BTREE,
                              INDEX `idx_business`(`module` ASC, `business_id` ASC) USING BTREE,
                              INDEX `idx_operator`(`operator_id` ASC) USING BTREE,
                              INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一审计日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of audit_log
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

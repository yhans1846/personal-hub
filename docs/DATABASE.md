# Personal Hub - 数据库设计

## 设计原则
- InnoDB，utf8mb4，BIGINT 自增主键，DATETIME 时间字段，后端映射 LocalDateTime
- 逻辑删除 `is_deleted TINYINT DEFAULT 0`
- 创建/更新时间由 MyBatis-Plus 自动填充
- 表名 snake_case

## 字段约定
| 字段 | 定义 |
|------|------|
| 主键 | `id BIGINT AUTO_INCREMENT PK` |
| 创建时间 | `created_at DATETIME DEFAULT CURRENT_TIMESTAMP` |
| 更新时间 | `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` |
| 逻辑删除 | `is_deleted TINYINT DEFAULT 0` |

---

## 全部表结构

### 1. `sys_user` 用户表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| username | VARCHAR(50) | 用户名 | NOT NULL, UNIQUE |
| password | VARCHAR(255) | 密码（BCrypt 加密） | NOT NULL |
| nickname | VARCHAR(50) | 昵称 | |
| avatar | VARCHAR(255) | 头像 URL | |
| email | VARCHAR(100) | 邮箱 | |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |

### 2. `note_note` 笔记表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| title | VARCHAR(200) | 笔记标题 | NOT NULL |
| md_path | VARCHAR(500) | MD 文件路径 `notes/{id}/note.md` | |
| is_favorite | TINYINT | 是否收藏 | DEFAULT 0 |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| deleted_at | DATETIME | 删除时间 | NULL |
| delete_reason | VARCHAR(50) | 删除原因（USER_DELETE/AUTO_ARCHIVE） | NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

> 笔记正文不存 DB，只存 `md_path`，正文写文件系统。多对多关联分类（`note_category_rel`）和标签（`tag_rel`）。`deleted_at` 用于回收站排序和展示，`delete_reason` 为后续自动归档预留。

### 3. `category` 统一分类表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(50) | 分类名称 | NOT NULL |
| type | VARCHAR(20) | 分类类型: note / bookmark / file | NOT NULL, INDEX |
| sort_order | INT | 排序 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

> 合并了原来的 `note_category`、`bookmark_category`、`file_category` 三张表。UNIQUE `uk_user_type_name` (`user_id`, `type`, `name`)。

### 4. `note_category_rel` 笔记-分类关联表
| 字段 | 类型 | 说明 |
|------|------|------|
| note_id | BIGINT | 笔记 ID |
| category_id | BIGINT | 分类 ID（关联 `category` 表，type='note'） |

PK: (`note_id`, `category_id`)。笔记与分类多对多。

### 5. `study_record` 学习记录表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| subject | VARCHAR(200) | 学习主题 | NOT NULL |
| date | DATE | 学习日期 | NOT NULL |
| duration | INT | 学习时长（分钟） | NOT NULL |
| content | TEXT | 学习内容 | |
| reflection | TEXT | 学习心得 | |
| plan_id | BIGINT | 关联学习计划 ID | |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 6. `todo_task` 待办表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| title | VARCHAR(200) | 任务标题 | NOT NULL |
| content | TEXT | 任务内容 | |
| is_done | TINYINT | 是否完成 | DEFAULT 0 |
| priority | TINYINT | 优先级 1-高 2-中 3-低 | DEFAULT 2 |
| due_date | DATE | 截止日期 | INDEX |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 7. `file_resource` 文件表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(255) | 原始文件名 | NOT NULL |
| stored_name | VARCHAR(255) | 存储文件名（UUID） | NOT NULL |
| path | VARCHAR(500) | 存储路径 | NOT NULL |
| size | BIGINT | 文件大小（字节） | NOT NULL |
| type | VARCHAR(50) | 文件扩展名 | NOT NULL |
| mime_type | VARCHAR(100) | MIME 类型 | |
| category_id | BIGINT | 分类 ID（关联 `category` 表，type='file'） | |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| source | VARCHAR(20) | 上传来源: upload / diary / avatar | |
| ref_id | BIGINT | 关联实体 ID | |

### 8. `diary_entry` 日记表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| date | DATE | 日记日期 | NOT NULL, INDEX |
| title | VARCHAR(200) | 日记标题 | |
| content | TEXT | 日记内容（Markdown） | |
| mood | TINYINT | 心情 1-很好 2-好 3-一般 4-不好 5-很差 | |
| weather | VARCHAR(50) | 天气 | |
| location | VARCHAR(255) | 地点 | |
| image_file_id | BIGINT | 配图文件 ID | |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 9. `bookmark_url` 收藏夹表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| title | VARCHAR(255) | 标题 | NOT NULL |
| url | VARCHAR(2048) | 网址 | NOT NULL |
| description | TEXT | 描述 | |
| favicon | VARCHAR(500) | 图标 URL | |
| category_id | BIGINT | 分类 ID（关联 `category` 表，type='bookmark'） | INDEX |
| tags | VARCHAR(500) | 标签（逗号分隔，遗留字段，已迁移到 tag_rel） | |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 10. `study_plan` 学习计划表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(200) | 计划名称 | NOT NULL |
| goal | TEXT | 学习目标 | |
| progress | INT | 进度百分比 0-100 | DEFAULT 0 |
| start_date | DATE | 开始日期 | |
| end_date | DATE | 结束日期 | |
| status | TINYINT | 状态 0-未开始 1-进行中 2-已完成 3-已放弃 | DEFAULT 0 |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 11. `reading_record` 阅读记录表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| book_title | VARCHAR(255) | 书名 | NOT NULL |
| author | VARCHAR(200) | 作者 | |
| cover_url | VARCHAR(500) | 封面图 | |
| total_chapters | INT | 总章节数 | DEFAULT 0 |
| current_chapter | INT | 当前章节 | DEFAULT 0 |
| progress | INT | 阅读进度 0-100 | DEFAULT 0 |
| status | TINYINT | 状态 0-未读 1-在读 2-读完 | DEFAULT 0 |
| rating | INT | 评分 1-5 | |
| total_duration | INT | 总阅读时长（分钟） | DEFAULT 0 |
| notes | TEXT | 阅读笔记 | |
| start_date | DATE | 开始阅读日期 | |
| end_date | DATE | 读完日期 | |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 12. `tag` 统一标签表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(50) | 标签名称 | NOT NULL |
| color | VARCHAR(7) | 标签颜色（如 #409eff） | NOT NULL, DEFAULT '#409eff' |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

> UNIQUE `idx_user_tag_name` (`user_id`, `name`)

### 13. `tag_rel` 标签关联表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| tag_id | BIGINT | 标签 ID | NOT NULL, INDEX |
| entity_type | VARCHAR(50) | 关联实体类型 | NOT NULL |
| entity_id | BIGINT | 关联实体 ID | NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |

> **entity_type 取值**: note / bookmark / diary / study / todo / file / reading / study_plan<br>
> UNIQUE `uk_tag_entity` (`tag_id`, `entity_type`, `entity_id`)

### 14. `sys_notification` 通知表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| type | VARCHAR(50) | 通知类型 | NOT NULL |
| title | VARCHAR(200) | 通知标题 | NOT NULL |
| content | TEXT | 通知内容 | |
| is_read | TINYINT | 0-未读 1-已读 | DEFAULT 0 |
| related_id | BIGINT | 关联实体 ID | |
| related_type | VARCHAR(50) | 关联实体类型 | |
| created_at | DATETIME | 创建时间 | NOT NULL |

> 无逻辑删除。通知自动生成，只读不可修改，通过清空批量删除。

### 15. `user_layout` 用户布局配置表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| layout_type | VARCHAR(30) | 类型: menu / dashboard | NOT NULL |
| layout_json | TEXT | 布局配置 JSON | NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |

> UNIQUE `uk_user_layout` (`user_id`, `layout_type`)

### 16. `audit_log` 统一审计日志表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| module | VARCHAR(50) | 业务模块（NOTE/TODO/FILE...） | NOT NULL, INDEX |
| business_id | BIGINT | 业务 ID | NULL, INDEX |
| action | VARCHAR(50) | 操作类型（DELETE/RESTORE/CREATE/UPDATE...） | NOT NULL |
| content | VARCHAR(500) | 操作描述 | NOT NULL |
| operator_id | BIGINT | 操作用户 ID | NOT NULL, INDEX |
| created_at | DATETIME | 操作时间 | NOT NULL, INDEX |

> 通用审计日志，所有业务模块共用。通过 `module` + `action` 区分业务类型，`business_id` 对应模块内业务记录。不设逻辑删除，仅追加写入。

---

## ER 关系

```
sys_user ── note_note ── note_category_rel ── category（type=note）
                  └── tag_rel ── tag
     ├── study_record ── tag_rel ── tag
     ├── todo_task ── tag_rel ── tag
     ├── file_resource ── tag_rel ── tag
     │                     └── category（type=file）
     ├── diary_entry ── tag_rel ── tag
     ├── bookmark_url ── tag_rel ── tag
     │                     └── category（type=bookmark）
     ├── study_plan ── tag_rel ── tag
     ├── reading_record ── tag_rel ── tag
     ├── sys_notification（独立，定时任务自动生成）
     └── user_layout（独立，布局配置持久化）
```

## 索引策略

| 表 | 索引 | 类型 | 说明 |
|----|------|------|------|
| sys_user | uk_username | UNIQUE | 用户名唯一 |
| note_note | idx_user_id / idx_updated_at | NORMAL | 用户查询 / 排序 |
| category | idx_user_id | NORMAL | 用户查询 |
| category | idx_type | NORMAL | 按类型查询 |
| category | idx_user_type | NORMAL | 用户+类型查询 |
| category | uk_user_type_name | UNIQUE | 用户+类型+名称唯一 |
| study_record | idx_user_id_date | NORMAL | 用户日期查询 |
| todo_task | idx_user_id / idx_due_date | NORMAL | 用户查询 / 排序 |
| file_resource | idx_user_id | NORMAL | 用户查询 |
| diary_entry | idx_user_id_date / idx_date | NORMAL | 用户+日期查询 |
| bookmark_url | idx_user_id / idx_category_id | NORMAL | 用户/分类查询 |
| study_plan | idx_user_id | NORMAL | 用户查询 |
| reading_record | idx_user_id | NORMAL | 用户查询 |
| sys_notification | idx_user_read / idx_created_at | NORMAL | 用户未读查询 / 时间排序 |
| tag | idx_user_tag_name | UNIQUE | 用户标签去重 |
| tag | idx_user_id | NORMAL | 用户查询 |
| tag_rel | uk_tag_entity | UNIQUE | 标签-实体唯一 |
| tag_rel | idx_entity | NORMAL | 按实体查询标签 |
| tag_rel | idx_tag_id | NORMAL | 按标签查询实体 |
| user_layout | uk_user_layout | UNIQUE | 用户+类型唯一 |
| user_layout | idx_user_id | NORMAL | 用户查询 |
| audit_log | idx_module | NORMAL | 按模块查询 |
| audit_log | idx_business | NORMAL | 按模块+业务 ID 查询 |
| audit_log | idx_operator | NORMAL | 按操作人查询 |
| audit_log | idx_created | NORMAL | 按时间排序 |

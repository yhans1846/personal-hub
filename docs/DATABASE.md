# Personal Hub - 数据库设计

## 设计原则
- InnoDB，utf8mb4，BIGINT 自增主键，DATETIME 时间字段，后端映射 LocalDateTime
- 逻辑删除 `is_deleted TINYINT DEFAULT 0`
- 创建/更新时间由 MyBatis-Plus 自动填充
- 表名 snake_case，模块前缀分组

## 字段约定
| 字段 | 定义 |
|------|------|
| 主键 | `id BIGINT AUTO_INCREMENT PK` |
| 创建时间 | `created_at DATETIME DEFAULT CURRENT_TIMESTAMP` |
| 更新时间 | `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` |
| 逻辑删除 | `is_deleted TINYINT DEFAULT 0` |

---

## 第一阶段 — 表结构

### 1. `sys_user` 用户表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| username | VARCHAR(50) | 用户名 | NOT NULL, UNIQUE |
| password | VARCHAR(255) | 加密密码 | NOT NULL |
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
| content | LONGTEXT | Markdown 内容 | |
| is_favorite | TINYINT | 是否收藏 | DEFAULT 0 |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 3. `note_category` 分类表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(50) | 分类名称 | NOT NULL |
| sort_order | INT | 排序 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 4. `note_tag` 标签表（已废弃，由 `tag` 表替代）
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(50) | 标签名称 | NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |

### 5. `note_category_rel` 笔记-分类关联表
| 字段 | 类型 | 说明 |
|------|------|------|
| note_id | BIGINT | 笔记ID (FK) |
| category_id | BIGINT | 分类ID (FK) |

### 6. `note_tag_rel` 笔记-标签关联表（已废弃，由 `tag_rel` 表替代）
| 字段 | 类型 | 说明 |
|------|------|------|
| note_id | BIGINT | 笔记ID (FK) |
| tag_id | BIGINT | 标签ID (FK) |

### 7. `study_record` 学习记录表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| subject | VARCHAR(200) | 学习主题 | NOT NULL |
| date | DATE | 学习日期 | NOT NULL |
| duration | INT | 时长（分钟）| NOT NULL |
| content | TEXT | 学习内容 | |
| reflection | TEXT | 学习心得 | |
| plan_id | BIGINT | 关联学习计划ID | INDEX |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 8. `todo_task` 待办表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| title | VARCHAR(200) | 任务标题 | NOT NULL |
| content | TEXT | 任务内容 | |
| is_done | TINYINT | 是否完成 | DEFAULT 0 |
| priority | TINYINT | 优先级（1高 2中 3低）| DEFAULT 2 |
| due_date | DATE | 截止日期 | |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 9. `file_resource` 文件表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(255) | 原始文件名 | NOT NULL |
| stored_name | VARCHAR(255) | 存储文件名（UUID）| NOT NULL |
| path | VARCHAR(500) | 存储路径 | NOT NULL |
| size | BIGINT | 大小（字节）| NOT NULL |
| type | VARCHAR(50) | 扩展名 | NOT NULL |
| mime_type | VARCHAR(100) | MIME 类型 | |
| category_id | BIGINT | 文件分类 ID | |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 10. `file_category` 文件分类表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(50) | 分类名称 | NOT NULL |
| sort_order | INT | 排序 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

---

## 第二阶段 — 已扩展表

### 11. `diary_entry` 日记表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| date | DATE | 日记日期 | NOT NULL |
| title | VARCHAR(200) | 日记标题 | |
| content | TEXT | Markdown 内容 | |
| mood | TINYINT | 心情（1很好 2好 3一般 4不好 5很差）| |
| weather | VARCHAR(50) | 天气 | |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 12. `bookmark_url` 收藏夹表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| title | VARCHAR(255) | 标题 | NOT NULL |
| url | VARCHAR(2048) | 网址 | NOT NULL |
| description | TEXT | 描述 | |
| favicon | VARCHAR(500) | 图标URL | |
| category_id | BIGINT | 分类ID | INDEX |
| tags | VARCHAR(500) | 标签（逗号分隔，遗留字段，数据已迁移到 tag_rel）| |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 13. `bookmark_category` 收藏夹分类表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(50) | 分类名称 | NOT NULL |
| sort_order | INT | 排序 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 14. `study_plan` 学习计划表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(200) | 计划名称 | NOT NULL |
| goal | TEXT | 学习目标 | |
| progress | INT | 进度百分比 0-100 | DEFAULT 0 |
| start_date | DATE | 开始日期 | |
| end_date | DATE | 结束日期 | |
| status | TINYINT | 状态（0未开始 1进行中 2已完成 3已放弃）| DEFAULT 0 |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 15. `reading_record` 阅读记录表
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
| status | TINYINT | 状态（0未读 1在读 2读完）| DEFAULT 0 |
| notes | TEXT | 阅读笔记 | |
| start_date | DATE | 开始阅读日期 | |
| end_date | DATE | 读完日期 | |
| is_deleted | TINYINT | 逻辑删除 | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 16. `tag` 统一标签表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| name | VARCHAR(50) | 标签名称 | NOT NULL |
| color | VARCHAR(7) | 标签颜色（#409eff）| NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | NOT NULL |

### 17. `tag_rel` 标签关联表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| tag_id | BIGINT | 标签ID | NOT NULL, INDEX |
| entity_type | VARCHAR(50) | 关联实体类型 | NOT NULL |
| entity_id | BIGINT | 关联实体ID | NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |

**entity_type 取值**: note / bookmark / diary / study / todo / file / reading / study_plan

---

## 第四阶段 — 新增表

### 18. `sys_notification` 系统通知表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 所属用户 | NOT NULL, INDEX |
| type | VARCHAR(50) | 通知类型（TODO_OVERDUE/PLAN_DEADLINE/PLAN_COMPLETED）| NOT NULL |
| title | VARCHAR(200) | 通知标题 | NOT NULL |
| content | TEXT | 通知内容 | |
| is_read | TINYINT | 是否已读 | DEFAULT 0 |
| related_id | BIGINT | 关联实体ID | |
| related_type | VARCHAR(50) | 关联实体类型（todo/study_plan）| |
| created_at | DATETIME | 创建时间 | NOT NULL |

无逻辑删除（通知自动生成，只读不可修改，通过「清空」批量删除）。

---

## ER 关系

```
sys_user ── note_note ── note_category_rel ── note_category
                  └── tag_rel ── tag
     ├── study_record ── tag_rel ── tag
     ├── todo_task ── tag_rel ── tag
     ├── file_resource ── file_category ── tag_rel ── tag
     ├── diary_entry ── tag_rel ── tag
     ├── bookmark_url ── bookmark_category ── tag_rel ── tag
     ├── study_plan ── tag_rel ── tag
     ├── reading_record ── tag_rel ── tag
     └── sys_notification（独立，定时任务自动生成）
```

## 索引策略

| 表 | 索引 | 类型 | 说明 |
|----|------|------|------|
| sys_user | uk_username | UNIQUE | 用户名唯一 |
| note_note | idx_user_id / idx_updated_at | NORMAL | 用户查询 / 排序 |
| note_category | idx_user_id | NORMAL | 用户查询 |
| note_tag | idx_user_id_name | UNIQUE | 用户标签去重 |
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


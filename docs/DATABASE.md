# Personal Hub - 数据库设计

## 设计原则
InnoDB · utf8mb4 · BIGINT 自增 PK · DATETIME（Java `LocalDateTime`）· 逻辑删除 `is_deleted` · MyBatis-Plus 自动填充时间 · snake_case

## 字段约定
| 字段 | 定义 |
|------|------|
| 主键 | `id BIGINT AUTO_INCREMENT PK` |
| 时间 | `created_at` / `updated_at` DATETIME |
| 逻辑删除 | `is_deleted TINYINT DEFAULT 0` |

---

## 全部表结构

### 1. `sys_user` 用户表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| username | VARCHAR(50) | 用户名 | NOT NULL, UNIQUE |
| password | VARCHAR(255) | BCrypt | NOT NULL |
| nickname | VARCHAR(50) | 昵称 | |
| avatar | VARCHAR(255) | 头像 URL | |
| email | VARCHAR(100) | 邮箱 | |
| gender | TINYINT | 0保密/1男/2女 | DEFAULT 0 |
| birthday | DATE | 生日 | |
| phone | VARCHAR(20) | 手机 | |
| country/province/city/district | VARCHAR(50) | 地区 | |
| website/github | VARCHAR(200) | 链接 | |
| bio | VARCHAR(500) | 简介 | |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |

### 2. `note_note` 笔记表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| title | VARCHAR(200) | 标题 | NOT NULL |
| md_path | VARCHAR(500) | `notes/{id}/note.md` | |
| excerpt | VARCHAR(500) | 列表摘要 | NULL |
| is_favorite | TINYINT | 收藏 | DEFAULT 0 |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |
| deleted_at | DATETIME | 回收站排序 | NULL |
| delete_reason | VARCHAR(50) | USER_DELETE/AUTO_ARCHIVE | NULL |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

> 正文在文件系统；分类 `note_category_rel`；标签 `tag_rel`。

### 3. `category` 统一分类表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| name | VARCHAR(50) | 名称 | NOT NULL |
| type | VARCHAR(20) | note/bookmark/file | NOT NULL, INDEX |
| sort_order | INT | 排序 | DEFAULT 0 |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

> UNIQUE `uk_user_type_name`(user_id,type,name)。合并原 note/bookmark/file_category。

### 4. `note_category_rel` 笔记-分类关联表
| 字段 | 类型 | 说明 |
|------|------|------|
| note_id | BIGINT | 笔记 |
| category_id | BIGINT | 分类（type=note） |

PK `(note_id, category_id)`。

### 5. `study_record` 学习记录表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| subject | VARCHAR(200) | 主题 | NOT NULL |
| date | DATE | 日期 | NOT NULL |
| duration | INT | 分钟 | NOT NULL |
| content / reflection | TEXT | 内容/心得 | |
| plan_id | BIGINT | 关联计划 | |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

### 6. `todo_task` 待办表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| title | VARCHAR(200) | 标题 | NOT NULL |
| content | TEXT | 内容 | |
| is_done | TINYINT | 完成 | DEFAULT 0 |
| priority | TINYINT | 1高/2中/3低 | DEFAULT 2 |
| due_date | DATE | 截止 | INDEX |
| completed_at | DATETIME | 完成时间 | |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

### 7. `file_resource` 文件表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| name | VARCHAR(255) | 原名 | NOT NULL |
| stored_name | VARCHAR(255) | UUID 名 | NOT NULL |
| path | VARCHAR(500) | 路径 | NOT NULL |
| size | BIGINT | 字节 | NOT NULL |
| type | VARCHAR(50) | 扩展名 | NOT NULL |
| mime_type | VARCHAR(100) | MIME | |
| category_id | BIGINT | 分类 type=file | |
| source | VARCHAR(20) | upload/diary/avatar | |
| ref_id | BIGINT | 关联实体 | |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

### 8. `diary_entry` 日记表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| date | DATE | 日期 | NOT NULL, INDEX |
| title | VARCHAR(200) | 标题 | |
| content | TEXT | Markdown | |
| mood | TINYINT | 1很好…5很差 | |
| weather / location | VARCHAR | 天气/地点 | |
| image_file_id | BIGINT | 配图 | |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

### 9. `bookmark_url` 收藏夹表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| title | VARCHAR(255) | 标题 | NOT NULL |
| url | VARCHAR(2048) | 网址 | NOT NULL |
| description | TEXT | 描述 | |
| favicon | VARCHAR(500) | 图标 | |
| category_id | BIGINT | 分类 type=bookmark | INDEX |
| show_on_dashboard | TINYINT | 首页快捷 | NOT NULL DEFAULT 0 |
| tags | VARCHAR(500) | 遗留逗号标签（已迁 tag_rel）| |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

### 10. `study_plan` 学习计划表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| name | VARCHAR(200) | 名称 | NOT NULL |
| source / author | VARCHAR(100) | 来源/作者 | |
| url | VARCHAR(500) | 地址 | |
| remark | TEXT | 备注 | |
| progress | INT | 0–100 | DEFAULT 0 |
| start_date / end_date | DATE | 起止 | |
| status | TINYINT | 0未开始/1学习中/2已完成/3已暂停 | DEFAULT 0 |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

> 分类：`tag_rel` + `entity_type=study_plan`。

### 11. `reading_record` 阅读记录表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| book_title | VARCHAR(255) | 书名 | NOT NULL |
| author | VARCHAR(200) | 作者 | |
| cover_url | VARCHAR(500) | 封面 | |
| total_chapters / current_chapter | INT | 章节 | DEFAULT 0 |
| progress | INT | 0–100 | DEFAULT 0 |
| status | TINYINT | 0未读/1在读/2读完 | DEFAULT 0 |
| rating | INT | 1–5 | |
| total_duration | INT | 分钟 | DEFAULT 0 |
| notes | TEXT | 笔记 | |
| start_date / end_date | DATE | 起止 | |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

### 12. `tag` 统一标签表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| name | VARCHAR(50) | 名称 | NOT NULL |
| color | VARCHAR(7) | 如 #409eff | NOT NULL DEFAULT '#409eff' |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |

> UNIQUE `idx_user_tag_name`(user_id,name)

### 13. `tag_rel` 标签关联表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| tag_id | BIGINT | 标签 | NOT NULL, INDEX |
| entity_type | VARCHAR(50) | 实体类型 | NOT NULL |
| entity_id | BIGINT | 实体 ID | NOT NULL |
| created_at | DATETIME | 时间 | NOT NULL |

> entity_type：note/bookmark/diary/study/todo/file/reading/study_plan  
> UNIQUE `uk_tag_entity`(tag_id,entity_type,entity_id)

### 14. `sys_notification` 通知表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| type | VARCHAR(50) | 类型 | NOT NULL |
| title | VARCHAR(200) | 标题 | NOT NULL |
| content | TEXT | 内容 | |
| is_read | TINYINT | 已读 | DEFAULT 0 |
| is_dismissed | TINYINT | 已清空 | DEFAULT 0 |
| related_id / related_type | | 关联实体 | |
| created_at | DATETIME | 时间 | NOT NULL |

> 清空=`is_dismissed=1`；同 type+related_id（含已读/已清空）不重复生成。

### 15. `user_layout` 用户布局配置表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| user_id | BIGINT | 用户 | NOT NULL, INDEX |
| layout_type | VARCHAR(30) | menu/dashboard/preview | NOT NULL |
| layout_json | TEXT | JSON | NOT NULL |
| created_at / updated_at | DATETIME | 时间 | NOT NULL |
| is_deleted | TINYINT | 删除 | DEFAULT 0 |

> UNIQUE `uk_user_layout`(user_id,layout_type)

### 16. `audit_log` 统一审计日志表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PK |
| module | VARCHAR(50) | NOTE/TODO/… | NOT NULL, INDEX |
| business_id | BIGINT | 业务 ID | INDEX |
| action | VARCHAR(50) | DELETE/RESTORE/… | NOT NULL |
| content | VARCHAR(500) | 描述 | NOT NULL |
| operator_id | BIGINT | 操作人 | NOT NULL, INDEX |
| created_at | DATETIME | 时间 | NOT NULL, INDEX |

> 仅追加，无逻辑删除。

---

## ER 关系

```
sys_user ── note_note ── note_category_rel ── category(type=note)
                  └── tag_rel ── tag
     ├── study_record / todo_task / file_resource / diary_entry
     ├── bookmark_url / study_plan / reading_record  ── tag_rel ── tag
     ├── category(type=file|bookmark) ── 文件/收藏
     ├── sys_notification / user_layout（独立）
     └── audit_log（独立）
```

## 索引策略

| 表 | 索引 | 说明 |
|----|------|------|
| sys_user | uk_username | UNIQUE |
| note_note | idx_user_id / idx_updated_at / idx_user_deleted_updated | 列表 |
| category | idx_user_id / idx_type / uk_user_type_name | UNIQUE 名 |
| study_record | idx_user_id_date | |
| todo_task | idx_user_id / idx_due_date | |
| file_resource | idx_user_id | |
| diary_entry | idx_user_id_date / idx_date | |
| bookmark_url | idx_user_id / idx_category_id | |
| study_plan / reading_record | idx_user_id | |
| sys_notification | idx_user_read / idx_created_at | |
| tag | idx_user_tag_name / idx_user_id | UNIQUE 名 |
| tag_rel | uk_tag_entity / idx_entity / idx_tag_id | |
| user_layout | uk_user_layout / idx_user_id | |
| audit_log | idx_module / idx_business / idx_operator / idx_created | |

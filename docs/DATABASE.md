# Personal Hub - 数据库设计

## 设计原则

- 所有表统一使用 `InnoDB` 引擎，字符集 `utf8mb4`
- 主键统一使用 `BIGINT` 自增
- 时间字段统一使用 `DATETIME` 类型，后端映射为 `LocalDateTime`
- 逻辑删除统一使用 `is_deleted` 字段（`TINYINT`，默认 0）
- 创建时间 `created_at`、更新时间 `updated_at` 由 MyBatis-Plus 自动填充
- 表名使用 `snake_case` 命名，模块前缀分组

## 字段约定

| 约定     | 说明                              |
| -------- | --------------------------------- |
| 主键     | `id BIGINT AUTO_INCREMENT PRIMARY KEY` |
| 创建时间 | `created_at DATETIME DEFAULT CURRENT_TIMESTAMP` |
| 更新时间 | `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` |
| 逻辑删除 | `is_deleted TINYINT DEFAULT 0`    |

---

## 第一阶段 — 表结构设计

### 1. 用户表 `sys_user`

| 字段名        | 类型          | 说明         | 约束               |
| ------------- | ------------- | ------------ | ------------------ |
| id            | BIGINT        | 主键         | PK, AUTO_INCREMENT |
| username      | VARCHAR(50)   | 用户名       | NOT NULL, UNIQUE   |
| password      | VARCHAR(255)  | 密码（加密） | NOT NULL           |
| nickname      | VARCHAR(50)   | 昵称         |                    |
| avatar        | VARCHAR(255)  | 头像 URL     |                    |
| email         | VARCHAR(100)  | 邮箱         |                    |
| created_at    | DATETIME      | 创建时间     | NOT NULL           |
| updated_at    | DATETIME      | 更新时间     | NOT NULL           |
| is_deleted    | TINYINT       | 逻辑删除     | DEFAULT 0          |

### 2. 笔记表 `note_note`

| 字段名      | 类型          | 说明       | 约束               |
| ----------- | ------------- | ---------- | ------------------ |
| id          | BIGINT        | 主键       | PK, AUTO_INCREMENT |
| user_id     | BIGINT        | 所属用户   | NOT NULL, INDEX    |
| title       | VARCHAR(200)  | 笔记标题   | NOT NULL           |
| content     | LONGTEXT      | Markdown 内容 |                |
| is_favorite | TINYINT       | 是否收藏   | DEFAULT 0          |
| is_deleted  | TINYINT       | 逻辑删除   | DEFAULT 0          |
| created_at  | DATETIME      | 创建时间   | NOT NULL           |
| updated_at  | DATETIME      | 更新时间   | NOT NULL           |

### 3. 分类表 `note_category`

| 字段名     | 类型         | 说明     | 约束               |
| ---------- | ------------ | -------- | ------------------ |
| id         | BIGINT       | 主键     | PK, AUTO_INCREMENT |
| user_id    | BIGINT       | 所属用户 | NOT NULL, INDEX    |
| name       | VARCHAR(50)  | 分类名称 | NOT NULL           |
| sort_order | INT          | 排序     | DEFAULT 0          |
| created_at | DATETIME     | 创建时间 | NOT NULL           |
| updated_at | DATETIME     | 更新时间 | NOT NULL           |

### 4. 标签表 `note_tag`

| 字段名     | 类型         | 说明     | 约束               |
| ---------- | ------------ | -------- | ------------------ |
| id         | BIGINT       | 主键     | PK, AUTO_INCREMENT |
| user_id    | BIGINT       | 所属用户 | NOT NULL, INDEX    |
| name       | VARCHAR(50)  | 标签名称 | NOT NULL           |
| created_at | DATETIME     | 创建时间 | NOT NULL           |

### 5. 笔记-分类关联表 `note_category_rel`

| 字段名      | 类型     | 说明   | 约束 |
| ----------- | -------- | ------ | ---- |
| note_id     | BIGINT   | 笔记ID | FK   |
| category_id | BIGINT   | 分类ID | FK   |

### 6. 笔记-标签关联表 `note_tag_rel`

| 字段名  | 类型   | 说明   | 约束 |
| ------- | ------ | ------ | ---- |
| note_id | BIGINT | 笔记ID | FK   |
| tag_id  | BIGINT | 标签ID | FK   |

### 7. 学习记录表 `study_record`

| 字段名     | 类型          | 说明                          | 约束               |
| ---------- | ------------- | ----------------------------- | ------------------ |
| id         | BIGINT        | 主键                          | PK, AUTO_INCREMENT |
| user_id    | BIGINT        | 所属用户                      | NOT NULL, INDEX    |
| subject    | VARCHAR(200)  | 学习主题                      | NOT NULL           |
| date       | DATE          | 学习日期                      | NOT NULL           |
| duration   | INT           | 学习时长（分钟）              | NOT NULL           |
| content    | TEXT          | 学习内容                      |                    |
| reflection | TEXT          | 学习心得                      |                    |
| is_deleted | TINYINT       | 逻辑删除                      | DEFAULT 0          |
| created_at | DATETIME      | 创建时间                      | NOT NULL           |
| updated_at | DATETIME      | 更新时间                      | NOT NULL           |

### 8. 待办表 `todo_task`

| 字段名     | 类型         | 说明                          | 约束               |
| ---------- | ------------ | ----------------------------- | ------------------ |
| id         | BIGINT       | 主键                          | PK, AUTO_INCREMENT |
| user_id    | BIGINT       | 所属用户                      | NOT NULL, INDEX    |
| title      | VARCHAR(200) | 任务标题                      | NOT NULL           |
| content    | TEXT         | 任务内容                      |                    |
| is_done    | TINYINT      | 是否完成                      | DEFAULT 0          |
| priority   | TINYINT      | 优先级（1高 2中 3低）         | DEFAULT 2          |
| due_date   | DATE         | 截止日期                      |                    |
| is_deleted | TINYINT      | 逻辑删除                      | DEFAULT 0          |
| created_at | DATETIME     | 创建时间                      | NOT NULL           |
| updated_at | DATETIME     | 更新时间                      | NOT NULL           |

### 9. 文件表 `file_resource`

| 字段名       | 类型          | 说明                         | 约束               |
| ------------ | ------------- | ---------------------------- | ------------------ |
| id           | BIGINT        | 主键                         | PK, AUTO_INCREMENT |
| user_id      | BIGINT        | 所属用户                     | NOT NULL, INDEX    |
| name         | VARCHAR(255)  | 原始文件名                   | NOT NULL           |
| stored_name  | VARCHAR(255)  | 存储文件名（UUID）           | NOT NULL           |
| path         | VARCHAR(500)  | 存储路径                     | NOT NULL           |
| size         | BIGINT        | 文件大小（字节）             | NOT NULL           |
| type         | VARCHAR(50)   | 文件类型（扩展名）           | NOT NULL           |
| mime_type    | VARCHAR(100)  | MIME 类型                    |                    |
| category_id  | BIGINT        | 分类ID                       |                    |
| is_deleted   | TINYINT       | 逻辑删除                     | DEFAULT 0          |
| created_at   | DATETIME      | 创建时间                     | NOT NULL           |
| updated_at   | DATETIME      | 更新时间                     | NOT NULL           |

### 10. 文件分类表 `file_category`

| 字段名     | 类型         | 说明     | 约束               |
| ---------- | ------------ | -------- | ------------------ |
| id         | BIGINT       | 主键     | PK, AUTO_INCREMENT |
| user_id    | BIGINT       | 所属用户 | NOT NULL, INDEX    |
| name       | VARCHAR(50)  | 分类名称 | NOT NULL           |
| sort_order | INT          | 排序     | DEFAULT 0          |
| created_at | DATETIME     | 创建时间 | NOT NULL           |
| updated_at | DATETIME     | 更新时间 | NOT NULL           |

---

## ER 关系图

```
sys_user (1) ──── (N) note_note
    │                    │
    │                    ├── (N) note_category_rel (N) ─── note_category
    │                    └── (N) note_tag_rel (N) ──────── note_tag
    │
    ├── (1) ──── (N) study_record
    ├── (1) ──── (N) todo_task
    └── (1) ──── (N) file_resource ─── (N) file_category_rel (N) ─── file_category
```

## 索引策略

| 表名              | 索引                    | 类型     | 说明               |
| ----------------- | ----------------------- | -------- | ------------------ |
| sys_user          | `uk_username`           | UNIQUE   | 用户名唯一         |
| note_note         | `idx_user_id`           | NORMAL   | 用户查询           |
| note_note         | `idx_updated_at`        | NORMAL   | 最近编辑排序       |
| note_category     | `idx_user_id`           | NORMAL   | 用户查询           |
| note_tag          | `idx_user_id_name`      | UNIQUE   | 用户标签去重       |
| study_record      | `idx_user_id_date`      | NORMAL   | 用户日期查询       |
| todo_task         | `idx_user_id`           | NORMAL   | 用户查询           |
| todo_task         | `idx_due_date`          | NORMAL   | 截止日期排序       |
| file_resource     | `idx_user_id`           | NORMAL   | 用户查询           |

---

## 第二阶段 — 待扩展表

> 以下表结构将在第二阶段开发时细化。

### 日记表规划 `journal_entry`

核心字段：`user_id`, `title`, `content`(Markdown), `entry_date`, `mood`(心情)

### 收藏夹表规划 `bookmark`

核心字段：`user_id`, `title`, `url`, `icon`, `category_id`, `tags`

### 学习计划表规划 `study_plan`

核心字段：`user_id`, `name`, `goal`, `progress`(0-100), `start_date`, `end_date`

### 阅读记录表规划 `reading_record`

核心字段：`user_id`, `book_title`, `author`, `current_chapter`, `progress`(0-100), `notes`

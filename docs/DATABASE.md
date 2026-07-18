# Personal Hub - 数据库设计

> 职责：表结构与索引。DDL 源：`sql/init.sql`（与库 `personal_hub` 对齐）。接口 → `API.md`。

**约定：** InnoDB · utf8mb4 · BIGINT 自增 PK · DATETIME · 逻辑删除 `is_deleted` · snake_case。Captcha 仅 Redis，无表。

共 **16** 表。

---

## 表结构

### `sys_user`
username UK · password · nickname · avatar · email · gender(0/1/2) · birthday · phone · country/province/city/district · website/github · bio · created_at/updated_at · is_deleted

### `note_note`
user_id · title · md_path · excerpt · is_favorite · is_deleted · deleted_at · delete_reason · 时间  
正文在文件系统；分类 `note_category_rel`；标签 `tag_rel`。索引：user_id · updated_at · (user_id,is_deleted,updated_at)

### `category`
user_id · name · type(note|bookmark|file) · sort_order · 时间  
UK `(user_id,type,name)`；索引 user_id / type

### `note_category_rel`
PK `(note_id, category_id)`

### `study_record`
user_id · subject · date · duration · content/reflection · is_deleted · 时间  
索引 `(user_id,date)`

### `todo_task`
user_id · title · content · is_done · priority(1高2中3低) · due_date · is_deleted · 时间 · completed_at  
索引 user_id / due_date

### `file_resource`
user_id · name · path · size · type · mime_type · category_id · is_deleted · 时间  
**仅**文件管理页 `/api/files` 上传；笔记/日记配图不进本表（见存储目录约定）

### `diary_entry`
user_id · date · title · content · mood(1–5) · weather/location · latitude/longitude（浏览器定位，可空）· image_files（文件名 JSON）· is_deleted · 时间  
索引 `(user_id,date)` / date。配图目录：`diaries/{id}/images/`（对齐笔记 `notes/{id}/images/`）

### `bookmark_url`
user_id · title · url · description · category_id · show_on_dashboard · is_deleted · 时间  
索引 user_id / category_id；标签走 `tag_rel`；图标由前端按域名拉取

### `study_plan`
user_id · name · source/author · url · remark · progress · start/end_date · status(0–3) · is_deleted · 时间  
分类：`tag_rel` entity_type=`study_plan`

### `reading_record`
user_id · book_title · author · cover_url · total/current_chapter · progress · status(0–2) · rating · total_duration · notes · start/end_date · is_deleted · 时间

### `tag` / `tag_rel`
tag：user_id · name · color · 时间；UK `(user_id,name)`  
tag_rel：tag_id · entity_type · entity_id · created_at；UK `(tag_id,entity_type,entity_id)`  
entity_type：note/bookmark/diary/study/todo/file/reading/study_plan

### `sys_notification`
user_id · type · title · content · is_read · is_dismissed · related_id/type · created_at  
索引 (user_id,is_read) / created_at。同 type+related_id 不重复生成。

### `user_layout`
user_id · layout_type(menu|dashboard|preview) · layout_json · 时间 · is_deleted  
UK `(user_id,layout_type)`

### `audit_log`
module（含 `AUTH` 等）· business_id · action（含 `LOGIN`/`DELETE`/`RESTORE` 等）· content · operator_id · created_at（仅追加）  
索引 module / (module,business_id) / operator_id / created_at
当前写入点：登录成功；笔记删除 / 恢复

---

## ER

```
sys_user ── note_note ── note_category_rel ── category(type=note)
                └── tag_rel ── tag
     ├── study_record / todo_task / file_resource / diary_entry
     ├── bookmark_url / study_plan / reading_record ── tag_rel
     ├── category(file|bookmark)
     └── sys_notification / user_layout / audit_log
```

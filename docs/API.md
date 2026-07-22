# Personal Hub - API

> 职责：HTTP 契约。表结构 → `DATABASE.md`；业务枚举文案 → 代码 `enums`。

## 约定

- Base：`/api` · JSON · `Authorization: Bearer {token}`（静态资源可用 `?token=`）
- 方法：查询 GET · 新建 POST · 全量改 PUT · 部分改 PATCH · 删 DELETE
- `Result<T>`：`{code,message,data}` · 分页 `data:{records,total,page,size}`
- 错误码：200 / 400 / 401 / 404 / 500（`ResultCode`）
- 分页：继承 `PageParam`，`page` 默认 1，`size` 默认 10

---

## 认证 `/api/auth`

| 方法 | 路径 | 认证 |
|------|------|------|
| GET | /captcha | 否 |
| POST | /captcha/check | 否 |
| POST | /login | 否 |
| POST | /logout | 是 |

captcha → `{captchaId,slotCount,shelfBooks,dragBook}`（空位由 `shelfBooks` 空串推导，不下发 emptyIndex）  
check / login 带 `captchaId`+`sliderX`（槽位下标）；TTL 120s，一次性。  
login → `{token,user:{id,username,nickname,avatar}}`

## 用户 `/api/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/PUT | /profile | 资料；PUT 不含 id/username/avatar |
| PUT | /password | `{oldPassword,newPassword}`（新密码 6–50） |
| POST | /avatar | multipart `file` → `{url}` |
| GET | /api/files/avatar/{filename} | 公开读（7 天缓存）|

字段：nickname/avatar/email/gender/birthday/phone/地区/website/github/bio

## 笔记 `/api/notes`

列表/详情/CRUD · favorite · archive · recent · recycle · recycle-bin 清空 · preview · **backlinks** · restore · permanent · import · import-content · export ZIP（单篇 / 批量）· images/attachments 上传与读取。

`GET /{id}/backlinks`：扫描当前用户未删除笔记中 `[[本篇标题]]` / `[[标题|别名]]`，返回 `[{id,title}]`（最多扫 500 篇）。  
`POST /{id}/archive`：归档进回收站（`delete_reason=AUTO_ARCHIVE`）；`DELETE /{id}` 为用户删除（`USER_DELETE`）。  
`POST /export`：body `{ids}`（1–50），打包 ZIP（每篇目录含 `note.md` + images/attachments）。  
`DELETE /recycle-bin`：清空回收站，返回 `{deleted}`。

列表参数：page,size,keyword,categoryId,tagId,isFavorite,isDeleted,`folderId`（缺省/`all`=全部；`none`/`uncategorized`=未分类；数字=该夹**直属**笔记）  
新建：`{title,content,categoryIds,tagIds,folderId?}`（`folderId` 可空=未分类）  
`PATCH /{id}/folder`：body `{folderId: number|null}`，移动归属

## 笔记文件夹 `/api/note-folders`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tree` | `{ folders, totalCount, uncategorizedCount, uncategorizedNotes }`；节点含 `noteCount`、`notes[]`（直属未删除笔记摘要：`id/title/folderId/updatedAt`） |
| POST | `/` | `{name,parentId?}` 创建（深度≤5） |
| PUT | `/{id}` | `{name}` 重命名 |
| PATCH | `/{id}/move` | `{parentId,sortOrder}`；防成环、超深 |
| DELETE | `/{id}` | 删子树；笔记 `folder_id` 置空（不进回收站） |

## 分类 `/api/categories`

CRUD + `PUT /sort`。`type`=note|bookmark|file。新建 `{name,type,sortOrder}`

## 学习记录 `/api/study-records`

CRUD。POST：`{subject,date,duration,content,reflection}`

## 待办 `/api/todos`

CRUD + `PATCH /{id}/done`。  
列表：keyword,priority,`dueScope`=`all|overdue|today|week|later|done`  
POST：`{title,content,priority,dueDate}`

## 文件 `/api/files`

分页 · 详情 · upload（可带 `categoryId`）· `PATCH /{id}/category` · download/preview · 删除 · **清空**（`DELETE /api/files` → `{deleted}`）。  
列表参数：keyword，`type`（扩展名或分组 `image|pdf|doc|archive`），categoryId  
**边界：** 只管理本页上传写入的 `file_resource`；笔记/日记配图走各自资源包 API，不会出现在本列表。  
`DELETE /api/files`：清空当前用户全部现行文件（磁盘+库，不可恢复），不受列表筛选影响。

## 日记 `/api/diaries`

CRUD + `GET /month?month=YYYY-MM`。列表：keyword,startDate,endDate,mood,month  
创建/更新可含 `location`、`latitude`、`longitude`、`imageFiles`（文件名字符串数组）  
配图：`POST|GET|DELETE /{id}/images[/{filename}]` → `diaries/{id}/images/`（须先有 diaryId；不进 `file_resource`）

## 收藏 `/api/bookmarks`

CRUD + `GET /dashboard?limit=`（默认 8，`show_on_dashboard=1`）。  
POST 可含 `tagIds`、`showOnDashboard`

## 学习计划 `/api/study-plans`

分页 · `GET /stats` · `GET /export?scope=filtered|all` · CRUD  
列表：keyword,status,tagId,sortBy,sortDir。状态 0–3；分类靠 `tagIds`

## 阅读 `/api/readings`

分页 · `GET /export` · CRUD。列表：keyword,status,sortBy,sortDir

## Dashboard `/api/dashboard`

| 路径 | 说明 |
|------|------|
| /stats | 首页计数 |
| /trends?days= | 趋势 |
| /search?keyword= | 跨模块搜索（8 组，每组最多 20；标题命中优先） |
| /detail?days= | 统计页综合（见 `StatsVO`）|

## 标签 `/api/tags`

CRUD（含 usageCount）· bind/unbind · 实体批量设标签 · 实体标签列表。  
`entityType` 取值见 `DATABASE.md` · `tag_rel`

## 通知 `/api/notifications`

分页 · unread-count · read / read-all · 清空（`is_dismissed`）· `POST /check`  
类型：TODO_OVERDUE / PLAN_DEADLINE / PLAN_COMPLETED

## 布局 `/api/layout`

GET 全部或 `/{type}` · PUT 保存 · POST import · DELETE 恢复默认  
`layoutType`：menu | dashboard | stats | reading | appearance | data | advanced（兼容旧值 preview / notification / backup）

## 备份 `/api/backup`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/now` | 写入服务端历史（MANUAL）并下载 ZIP |
| GET | `/list` | 当前用户备份历史 |
| GET | `/{id}/download` | 下载历史包（OK） |
| POST | `/{id}/restore` | 先打当前快照再全量恢复该包 |
| DELETE | `/{id}` | 删除历史记录与文件 |
| GET/PUT | `/settings` | `{ frequency: off\|daily\|weekly }`，默认 daily |
| POST | `/import` | 上传 `.zip` 全量覆盖（username/password 不变） |

定时：每日 02:00 按频率自动备份；成功保留 7、失败保留 3。磁盘：`backups/{userId}/{id}.zip`（不打入 ZIP 包内）。
不含：密码、用户名、通知、审计。含：回收站笔记、`note_folders.json`、`user_layout`、`profile.json`、头像。包格式见 `2026-07-19-data-backup-restore-design`；历史见 `2026-07-20-auto-backup-history-design`。

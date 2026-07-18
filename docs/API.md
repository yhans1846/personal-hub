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
| POST | /avatar | multipart `file` → `{url}` |
| GET | /api/files/avatar/{filename} | 公开读（7 天缓存）|

字段：nickname/avatar/email/gender/birthday/phone/地区/website/github/bio

## 笔记 `/api/notes`

列表/详情/CRUD · favorite · recent · recycle · preview · restore · permanent · import · import-content · export ZIP · images/attachments 上传与读取。

列表参数：page,size,keyword,categoryId,tagId,isFavorite,isDeleted  
新建：`{title,content,categoryIds,tagIds}`

## 分类 `/api/categories`

CRUD + `PUT /sort`。`type`=note|bookmark|file。新建 `{name,type,sortOrder}`

## 学习记录 `/api/study-records`

CRUD。POST：`{subject,date,duration,content,reflection}`

## 待办 `/api/todos`

CRUD + `PATCH /{id}/done`。  
列表：keyword,priority,`dueScope`=`all|overdue|today|week|later|done`  
POST：`{title,content,priority,dueDate}`

## 文件 `/api/files`

分页 · 详情 · upload（可带 `categoryId`）· `PATCH /{id}/category` · download/preview · 删除。  
列表参数：keyword，`type`（扩展名或分组 `image|pdf|doc|archive`），categoryId  
前端：列表内嵌预览；上传可选分类；卡片可改分类；筛选按类型/分类生效

## 日记 `/api/diaries`

CRUD + `GET /month?month=YYYY-MM`。列表：keyword,startDate,endDate,mood,month  
创建/更新可含 `location`、`latitude`、`longitude`（定位仅存坐标，不反查地名）

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
| /search?keyword= | 跨模块搜索 |
| /detail?days= | 统计页综合（见 `StatsVO`）|

## 标签 `/api/tags`

CRUD（含 usageCount）· bind/unbind · 实体批量设标签 · 实体标签列表。  
`entityType` 取值见 `DATABASE.md` · `tag_rel`

## 通知 `/api/notifications`

分页 · unread-count · read / read-all · 清空（`is_dismissed`）· `POST /check`  
类型：TODO_OVERDUE / PLAN_DEADLINE / PLAN_COMPLETED

## 布局 `/api/layout`

GET 全部或 `/{type}` · PUT 保存 · POST import · DELETE 恢复默认  
`layoutType`：menu | dashboard | preview

## 导出 / 备份（占位，建设中）

| 前缀 | 说明 |
|------|------|
| `/api/export` | POST 导出 · GET history · GET download/{id}（占位响应） |
| `/api/backup` | POST now · GET list · GET download/{id} · POST import（占位响应） |

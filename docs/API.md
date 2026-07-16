# Personal Hub - API 接口设计规范

## 统一规范

- Base：`http://localhost:8080/api`｜JSON｜`Authorization: Bearer {token}`
- 静态资源可用 `?token=`（`<img>` 等无法带 Header）

| 动作 | 方法 |
|------|------|
| 查询 / 新增 / 改 / 部分改 / 删 | GET / POST / PUT / PATCH / DELETE |

### 统一返回 `Result<T>`
`{"code":200,"message":"success","data":{}}`

### 分页返回 `PageResult<T>`
`data: { records, total, page, size }`

### 错误码（`ResultCode`）
| code | 含义 |
|------|------|
| 200 | SUCCESS |
| 400 / 401 / 403 / 404 / 422 / 500 | BAD_REQUEST / UNAUTHORIZED / FORBIDDEN / NOT_FOUND / VALIDATION_FAILED / SERVER_ERROR |

### 分页查询参数
继承 `PageParam`：`page` 默认 1，`size` 默认 10。

---

## 第一阶段 — 接口设计

### 一、用户认证 `/api/auth`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/auth/login | 登录 | 否 |
| POST | /api/auth/logout | 退出 | 是 |

**login** Req`username`,`password`} → `{token,user:{id,username,nickname,avatar}}`

### 二、用户信息 `/api/user`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET/PUT | /api/user/profile | 资料读写 | 是 |
| POST | /api/user/avatar | 头像 multipart `file` | 是 |
| GET | /api/files/avatar/{filename} | 头像公开读（7 天缓存）| 否 |

**profile** 含 nickname/avatar/email/gender/birthday/phone/地区/website/github/bio；PUT 不含 id/username/avatar。头像返回 `{url}`。

### 三、Markdown 笔记 `/api/notes`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/notes | 列表分页 | 是 |
| GET | /api/notes/{id} | 详情 | 是 |
| POST | /api/notes | 新建 | 是 |
| PUT | /api/notes/{id} | 编辑 | 是 |
| DELETE | /api/notes/{id} | 进回收站 | 是 |
| PUT | /api/notes/{id}/favorite | 收藏切换 | 是 |
| GET | /api/notes/recent | 最近编辑 | 是 |
| PATCH | /api/notes/{id}/restore | 恢复 | 是 |
| GET | /api/notes/recycle | 回收站 | 是 |
| GET | /api/notes/{id}/preview | 只读预览（含已删）| 是 |
| DELETE | /api/notes/{id}/permanent | 永久删除 | 是 |
| POST | /api/notes/import | 导入 MD 文件 | 是 |
| POST | /api/notes/import-content | 粘贴导入 | 是 |
| GET | /api/notes/{id}/export | 导出 ZIP | 是 |
| POST | /api/notes/{noteId}/images\|attachments | 上传配图/附件 | 是 |
| GET | /api/notes/{noteId}/images\|attachments/{filename} | 取资源 | 是 |

**列表参数：** page, size, keyword, categoryId, tagId, isFavorite, isDeleted  
**recycle：** page, size, keyword；按 `deleted_at DESC`  
**preview：** 不校验 is_deleted，只读  
**新建：** `{title,content,categoryIds,tagIds}`  
**import：** multipart file + 可选 title/categoryIds/tagIds/baseDir → `ImportReport{total,success,failed,skipped,resources,noteId}`  
**import-content：** `{title,content,categoryIds,tagIds}`，可含 warning / rewrittenContent  
**配图上传：** multipart file → `{url,name}`

### 四、统一分类 `/api/categories`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/categories?type= | 按 type 列表 | 是 |
| POST/PUT/DELETE | /api/categories[/{id}] | CRUD | 是 |
| PUT | /api/categories/sort | 批量排序 `[{id,sortOrder}]` | 是 |

**新建：** `{name,type,sortOrder}`；type=note|bookmark|file

### 五、学习记录 `/api/study-records`
CRUD：`GET/POST/PUT/DELETE` + `GET/{id}`。  
**POST：** `{subject,date,duration,content,reflection,planId?}`

### 七、Todo `/api/todos`
CRUD + `PATCH /{id}/done`。  
**POST：** `{title,content,priority,dueDate}`

### 八、文件管理 `/api/files`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/files | 分页：page,size,keyword,type,categoryId |
| GET | /api/files/{id} | 详情 |
| POST | /api/files/upload | multipart file + categoryId? |
| GET | /api/files/{id}/download\|preview | 下载 / inline 预览 |
| DELETE | /api/files/{id} | 删除 |

---

## 第二阶段 — 接口设计

### 十、日记 `/api/diaries`
CRUD + `GET /month?month=YYYY-MM`。  
列表参数：page,size,keyword,startDate,endDate,mood,month  
**POST：** `{date,title,content,mood,weather}`

### 十一、收藏夹 `/api/bookmarks`
CRUD + `GET /dashboard?limit=`（默认 8，最大 20；`show_on_dashboard=1`）。  
列表：page,size,keyword,categoryId,tagId  
**POST：** `{title,url,description,categoryId,tagIds,showOnDashboard}`

### 十二、学习计划 `/api/study-plans`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/study-plans | 分页 |
| GET | /api/study-plans/stats | `{total,pending,learning,done,paused}` |
| GET | /api/study-plans/export | XLSX 导出（内存流，不落盘） |
| GET/POST/PUT/DELETE | /api/study-plans[/{id}] | CRUD |

**列表：** keyword(name/source/author/remark), status, tagId, sortBy(updatedAt 默认|createdAt|startDate|endDate|name), sortDir  
**export：** `scope=filtered|all`（默认 filtered）；filtered 时带与列表相同的筛选/排序参数（不分页）；返回 `.xlsx` 附件；列：名称,分类,状态,进度,开始,结束,来源,作者,URL,备注,更新时间  
**POST/PUT：** `{name,source,author,url,remark,progress,startDate,endDate,status,tagIds}`  
状态：0 未开始 / 1 学习中 / 2 已完成 / 3 已暂停；分类靠 tagIds（`entity_type=study_plan`）  
学习记录可用 `planId` 关联。

### 十四、阅读记录 `/api/readings`
CRUD。列表：page,size,keyword,status  
**POST：** `{bookTitle,author,totalChapters,currentChapter,progress,status,notes}`

---

## 第三阶段 — 接口设计

### 十五、Dashboard `/api/dashboard`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /stats | 首页计数汇总 |
| GET | /trends?days= | 学习/笔记/Todo/阅读趋势 |
| GET | /search?keyword= | 跨 8 模块搜索 |
| GET | /detail?days= | 统计页 8 模块综合数据 |

**stats：** note/study/todo/file/diary/bookmark/reading/studyPlan 等计数与时长  
**search 字段：** 笔记/日记/待办(标题+内容)、学习(主题+内容)、收藏(标题+网址+描述)、阅读(书名+作者+笔记)、文件(名称)、计划(名称+来源+作者+备注)  
**detail：** KPI、趋势、热力、分类/标签、活动、insights 等（见实现 `StatsVO`）

### 十六、统一标签 `/api/tags`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | /api/tags[/{id}] | CRUD（列表含 usageCount） |
| POST/DELETE | /{id}/bind\|unbind | 绑定/解绑 |
| PUT | /entities/{entityType}/{entityId} | 批量设标签（先清空） |
| GET | /entities | 实体标签列表 |

**POST：** `{name,color}`｜bind 参数 entityType + entityId

### 十七、系统通知 `/api/notifications`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 分页，未读优先 |
| GET | /unread-count | 未读数 |
| PUT | /read \| /read-all | 批量/全部已读 |
| DELETE | / | 清空（软清除 is_dismissed） |
| POST | /check | 触发检测生成 |

类型：TODO_OVERDUE / PLAN_DEADLINE / PLAN_COMPLETED。读：`[id…]`

### 十八、布局配置 `/api/layout`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / \| /{type} | 全部 / 指定类型 |
| PUT | / | 保存 `{layoutType,layoutJson}` |
| POST | /import | 导入 |
| DELETE | /{type} | 恢复默认 |

**layout_type：** menu / dashboard / preview

---

### 标签关联实体类型
note · bookmark · diary · study · todo · file · reading · study_plan

### 变更说明
- 统一标签替换 `note_tag`；收藏标签改 `tag_rel`
- 旧数据 `@PostConstruct` 迁移

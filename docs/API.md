# Personal Hub - API 接口设计规范

## 统一规范

- **基础路径**: `http://localhost:8080/api`
- **Content-Type**: `application/json` | **Authorization**: `Bearer {token}`

| 动作 | 方法 |
|------|------|
| 查询 | GET |
| 新增 | POST |
| 修改 | PUT |
| 部分修改 | PATCH |
| 删除 | DELETE |

### 统一返回 `Result<T>`
```json
{"code":200,"message":"success","data":{}}
```

### 分页返回 `PageResult<T>`
```json
{"code":200,"message":"success","data":{"records":[],"total":100,"page":1,"size":10}}
```

### 错误码（定义在 `ResultCode` 枚举中）
| 状态码 | 含义 | 枚举常量 |
|--------|------|----------|
| 200 | 成功 | `SUCCESS` |
| 400 | 请求参数错误 | `BAD_REQUEST` |
| 401 | 未登录或登录已过期 | `UNAUTHORIZED` |
| 403 | 无权限访问 | `FORBIDDEN` |
| 404 | 资源不存在 | `NOT_FOUND` |
| 422 | 参数校验失败 | `VALIDATION_FAILED` |
| 500 | 服务器内部错误 | `SERVER_ERROR` |

### 分页查询参数
所有分页接口统一使用 `PageParam` 作查询基类：
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 1 | 页码 |
| size | int | 10 | 每页条数 |

各模块查询 DTO 继承 `PageParam`，在接口文档中查看具体扩展字段。

---

## 第一阶段 — 接口设计

### 一、用户认证 `/api/auth`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/auth/login | 登录 | 否 |
| POST | /api/auth/logout | 退出 | 是 |

**POST /api/auth/login**
```json
// Request: {"username":"admin","password":"123456"}
// Response: {"code":200,"message":"登录成功","data":{"token":"eyJ...","user":{"id":1,"username":"admin","nickname":"管理员","avatar":null}}}
```

### 二、用户信息 `/api/user`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/user/profile | 获取个人信息 | 是 |
| PUT | /api/user/profile | 修改个人信息 | 是 |
| PUT | /api/user/password | 修改密码 | 是 |

**PUT /api/user/password** — `{"oldPassword":"123456","newPassword":"654321"}`

### 三、Markdown 笔记 `/api/notes`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/notes | 笔记列表（分页）| 是 |
| GET | /api/notes/{id} | 笔记详情 | 是 |
| POST | /api/notes | 新建笔记 | 是 |
| PUT | /api/notes/{id} | 编辑笔记 | 是 |
| DELETE | /api/notes/{id} | 删除（回收站）| 是 |
| PUT | /api/notes/{id}/favorite | 切换收藏 | 是 |
| GET | /api/notes/recent | 最近编辑 | 是 |
| PATCH | /api/notes/{id}/restore | 恢复笔记 | 是 |
| DELETE | /api/notes/{id}/permanent | 永久删除 | 是 |

**GET /api/notes** 参数: page(int), size(int), keyword, categoryId(long), tagId(long), isFavorite(bool), isDeleted(bool)

**POST /api/notes** — `{"title":"...","content":"# MD...","categoryIds":[1,2],"tagIds":[1,3]}`

### 四、笔记分类 `/api/note-categories`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/note-categories | 列表 | 是 |
| POST | /api/note-categories | 新建 | 是 |
| PUT | /api/note-categories/{id} | 修改 | 是 |
| DELETE | /api/note-categories/{id} | 删除 | 是 |

### 五、笔记标签 `/api/note-tags`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/note-tags | 列表 | 是 |
| POST | /api/note-tags | 新建 | 是 |
| PUT | /api/note-tags/{id} | 修改 | 是 |
| DELETE | /api/note-tags/{id} | 删除 | 是 |

### 六、学习记录 `/api/study-records`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/study-records | 记录列表 | 是 |
| GET | /api/study-records/{id} | 详情 | 是 |
| POST | /api/study-records | 新建 | 是 |
| PUT | /api/study-records/{id} | 修改 | 是 |
| DELETE | /api/study-records/{id} | 删除 | 是 |

**POST /api/study-records** — `{"subject":"...","date":"2026-07-09","duration":120,"content":"...","reflection":"..."}`

### 七、Todo `/api/todos`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/todos | 任务列表 | 是 |
| GET | /api/todos/{id} | 详情 | 是 |
| POST | /api/todos | 新建 | 是 |
| PUT | /api/todos/{id} | 修改 | 是 |
| DELETE | /api/todos/{id} | 删除 | 是 |
| PATCH | /api/todos/{id}/done | 切换完成 | 是 |

**POST /api/todos** — `{"title":"...","content":"...","priority":1,"dueDate":"2026-07-10"}`

### 八、文件管理 `/api/files`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/files | 文件列表（分页）| 是 |
| GET | /api/files/{id} | 详情 | 是 |
| POST | /api/files/upload | 上传（multipart）| 是 |
| GET | /api/files/{id}/download | 下载 | 是 |
| DELETE | /api/files/{id} | 删除 | 是 |

**POST /api/files/upload** — multipart/form-data: file(File必填), categoryId(Long可选)

**GET /api/files** 参数: page, size, keyword, type(扩展名), categoryId

### 九、文件分类 `/api/file-categories`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/file-categories | 列表 | 是 |
| POST | /api/file-categories | 新建 | 是 |
| PUT | /api/file-categories/{id} | 修改 | 是 |
| DELETE | /api/file-categories/{id} | 删除 | 是 |

---

## 第二阶段 — 接口设计

### 十、日记 `/api/diaries`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/diaries | 日记列表（分页）| 是 |
| GET | /api/diaries/month | 月视图 | 是 |
| GET | /api/diaries/{id} | 详情 | 是 |
| POST | /api/diaries | 新建 | 是 |
| PUT | /api/diaries/{id} | 编辑 | 是 |
| DELETE | /api/diaries/{id} | 删除 | 是 |

**GET /api/diaries** 参数: page, size, keyword, startDate, endDate, mood, month(YYYY-MM)

**GET /api/diaries/month** 参数: month(YYYY-MM, 必填)

**POST /api/diaries**
```json
{"date":"2026-07-10","title":"忙碌的一天","content":"# 日记内容（Markdown）","mood":2,"weather":"晴"}
```

**PUT /api/diaries/{id}** — 同 POST 请求体，所有字段可选

### 十一、收藏夹 `/api/bookmarks`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/bookmarks | 收藏列表（分页）| 是 |
| GET | /api/bookmarks/{id} | 详情 | 是 |
| POST | /api/bookmarks | 新建 | 是 |
| PUT | /api/bookmarks/{id} | 编辑 | 是 |
| DELETE | /api/bookmarks/{id} | 删除 | 是 |

**GET /api/bookmarks** 参数: page, size, keyword, categoryId, tagId

**POST /api/bookmarks**
```json
{"title":"GitHub","url":"https://github.com","description":"","categoryId":1,"tagIds":[1,2]}
```

### 十二、收藏夹分类 `/api/bookmark-categories`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/bookmark-categories | 列表 | 是 |
| POST | /api/bookmark-categories | 新建 | 是 |
| PUT | /api/bookmark-categories/{id} | 编辑 | 是 |
| DELETE | /api/bookmark-categories/{id} | 删除 | 是 |

### 十三、学习计划 `/api/study-plans`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/study-plans | 计划列表（分页）| 是 |
| GET | /api/study-plans/{id} | 详情 | 是 |
| POST | /api/study-plans | 新建 | 是 |
| PUT | /api/study-plans/{id} | 编辑 | 是 |
| DELETE | /api/study-plans/{id} | 删除 | 是 |

**GET /api/study-plans** 参数: page, size, keyword, status

**POST /api/study-plans**
```json
{"name":"Spring Boot 深入学习","goal":"完成官方文档阅读","progress":0,"startDate":"2026-07-01","endDate":"2026-08-31","status":0}
```

**PUT /api/study-plans/{id}** — 同 POST 请求体

### 学习记录关联
`study_record` 表新增 `plan_id` 字段，学习记录支持关联学习计划。
**POST /api/study-records** 新增可选字段: `planId`

### 十四、阅读记录 `/api/readings`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/readings | 记录列表（分页）| 是 |
| GET | /api/readings/{id} | 详情 | 是 |
| POST | /api/readings | 新建 | 是 |
| PUT | /api/readings/{id} | 编辑 | 是 |
| DELETE | /api/readings/{id} | 删除 | 是 |

**GET /api/readings** 参数: page, size, keyword, status

**POST /api/readings**
```json
{"bookTitle":"深入理解Java虚拟机","author":"周志明","totalChapters":20,"currentChapter":5,"progress":25,"status":1,"notes":"经典好书"}
```

**PUT /api/readings/{id}** — 同 POST 请求体

---

## 第三阶段 — 接口设计

### 十五、Dashboard 数据统计 `/api/dashboard`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/dashboard/stats | Dashboard 统计数据 | 是 |

**GET /api/dashboard/trends** 参数: days(int, 默认30)

**GET /api/dashboard/stats**
```json
{
  "noteCount": 42,           "studyCount": 15,
  "studyDurationTotal": 1200,"studyDurationThisWeek": 300,
  "todoTotal": 20,           "todoDone": 12,
  "todoPending": 8,          "todoOverdue": 2,
  "fileCount": 30,           "diaryCount": 10,
  "diaryCountThisMonth": 3,  "bookmarkCount": 25,
  "readingCount": 8,         "studyPlanCount": 5
}
```

**GET /api/dashboard/search?keyword=xxx** — 全局搜索（跨 8 模块）
- 搜索字段：笔记(标题+内容)、日记(标题+内容)、待办(标题+内容)、学习记录(主题+内容)、收藏夹(标题+网址+描述)、阅读记录(书名+作者+笔记)、文件(名称)、学习计划(名称+目标)
- 返回按模块分组的结果，每项含标题、摘要、日期、跳转链接

**GET /api/dashboard/trends?days=30** -- 返回 4 项趋势数据（每日聚合）
```json
{
  "studyTrend": [{"date":"2026-07-01","value":120}],
  "noteTrend": [{"date":"2026-07-01","value":2}],
  "todoTrend": [{"date":"2026-07-01","value":3}],
  "readingTrend": [{"date":"2026-07-01","value":1}]
}
```

### 十六、统一标签 `/api/tags`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/tags | 获取所有标签（含使用次数）| 是 |
| POST | /api/tags | 创建标签 | 是 |
| PUT | /api/tags/{id} | 更新标签 | 是 |
| DELETE | /api/tags/{id} | 删除标签 | 是 |
| POST | /api/tags/{id}/bind | 绑定标签到实体 | 是 |
| DELETE | /api/tags/{id}/unbind | 解绑标签 | 是 |
| PUT | /api/tags/entities/{entityType}/{entityId} | 批量设置实体标签 | 是 |
| GET | /api/tags/entities | 获取实体的标签列表 | 是 |

**GET /api/tags** — 返回列表，每项含 `id`, `name`, `color`, `usageCount`

**POST /api/tags**
```json
{"name":"前端","color":"#409eff"}
```

**POST /api/tags/{id}/bind** 参数: entityType(String), entityId(Long)

**PUT /api/tags/entities/{entityType}/{entityId}** — 批量设置（先清空再绑定）
```json
[1, 2, 3]
```

### 标签关联实体类型
| entityType | 说明 |
|-----------|------|
| note | 笔记 |
| bookmark | 收藏夹 |
| diary | 日记 |
| study | 学习记录 |
| todo | 待办 |
| file | 文件 |
| reading | 阅读记录 |
| study_plan | 学习计划 |

### 变更说明
- 统一标签系统替换了原有的 `note_tag` + `note_tag_rel` 笔记标签系统
- 收藏夹标签从逗号分隔改为关联系统（`tag_rel` 多态关联）
- 旧版数据通过 `@PostConstruct` 自动迁移

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

### 错误码
200 成功 | 400 参数错误 | 401 未认证 | 403 无权限 | 404 不存在 | 500 服务器错误

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

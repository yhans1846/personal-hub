# Personal Hub - API 接口设计规范

## 统一规范

### 基础路径

```
开发环境：http://localhost:8080
生产环境：按实际部署配置
```

API 统一前缀：`/api`

### 请求方式

| 动作   | 方法    |
| ------ | ------- |
| 查询   | GET     |
| 新增   | POST    |
| 修改   | PUT     |
| 部分修改 | PATCH |
| 删除   | DELETE  |

### 统一返回结构 `Result<T>`

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段    | 类型   | 说明                     |
| ------- | ------ | ------------------------ |
| code    | int    | 状态码，200 表示成功     |
| message | string | 提示信息                 |
| data    | T      | 返回数据，无数据时为 null |

### 分页返回结构 `PageResult<T>`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 错误码约定

| 状态码 | 说明               |
| ------ | ------------------ |
| 200    | 成功               |
| 400    | 请求参数错误       |
| 401    | 未认证 / Token 过期 |
| 403    | 无权限             |
| 404    | 资源不存在         |
| 500    | 服务器内部错误     |

### 请求头

| Header        | 值              | 说明             |
| ------------- | --------------- | ---------------- |
| Content-Type  | application/json | 请求体格式       |
| Authorization | Bearer {token}  | JWT 认证 Token   |

---

## 第一阶段 — 接口设计

### 一、用户认证 `/api/auth`

| 方法 | 路径             | 说明       | 认证 |
| ---- | ---------------- | ---------- | ---- |
| POST | /api/auth/login  | 用户登录   | 否   |
| POST | /api/auth/logout | 退出登录   | 是   |

#### POST `/api/auth/login` — 用户登录

**请求体：**

```json
{
  "username": "admin",
  "password": "123456"
}
```

**返回：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJI...",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "avatar": null
    }
  }
}
```

---

### 二、用户信息 `/api/user`

| 方法   | 路径              | 说明           | 认证 |
| ------ | ----------------- | -------------- | ---- |
| GET    | /api/user/profile | 获取个人信息   | 是   |
| PUT    | /api/user/profile | 修改个人信息   | 是   |
| PUT    | /api/user/password | 修改密码      | 是   |

#### GET `/api/user/profile` — 获取个人信息

**返回：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "avatar": "/uploads/avatar/xxx.png",
    "email": "admin@example.com",
    "createdAt": "2026-07-01T12:00:00"
  }
}
```

#### PUT `/api/user/password` — 修改密码

**请求体：**

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

---

### 三、Markdown 笔记 `/api/notes`

| 方法   | 路径                           | 说明         | 认证 |
| ------ | ------------------------------ | ------------ | ---- |
| GET    | /api/notes                     | 笔记列表     | 是   |
| GET    | /api/notes/{id}                | 笔记详情     | 是   |
| POST   | /api/notes                     | 新建笔记     | 是   |
| PUT    | /api/notes/{id}                | 编辑笔记     | 是   |
| DELETE | /api/notes/{id}                | 删除（回收站）| 是   |
| PUT    | /api/notes/{id}/favorite       | 切换收藏     | 是   |
| GET    | /api/notes/recent              | 最近编辑     | 是   |
| PATCH  | /api/notes/{id}/restore        | 恢复笔记     | 是   |
| DELETE | /api/notes/{id}/permanent      | 永久删除     | 是   |

#### GET `/api/notes` — 笔记列表（分页）

**请求参数：**

| 参数       | 类型    | 必填 | 说明              |
| ---------- | ------- | ---- | ----------------- |
| page       | int     | 否   | 页码，默认 1      |
| size       | int     | 否   | 每页条数，默认 10 |
| keyword    | string  | 否   | 搜索关键词        |
| categoryId | long    | 否   | 分类筛选          |
| tagId      | long    | 否   | 标签筛选          |
| isFavorite | boolean | 否   | 收藏筛选          |
| isDeleted  | boolean | 否   | 回收站筛选        |

#### POST `/api/notes` — 新建笔记

**请求体：**

```json
{
  "title": "Spring Boot 学习笔记",
  "content": "# Spring Boot\n...",
  "categoryIds": [1, 2],
  "tagIds": [1, 3]
}
```

---

### 四、笔记分类 `/api/note-categories`

| 方法   | 路径                        | 说明         | 认证 |
| ------ | --------------------------- | ------------ | ---- |
| GET    | /api/note-categories        | 分类列表     | 是   |
| POST   | /api/note-categories        | 新建分类     | 是   |
| PUT    | /api/note-categories/{id}   | 修改分类     | 是   |
| DELETE | /api/note-categories/{id}   | 删除分类     | 是   |

---

### 五、笔记标签 `/api/note-tags`

| 方法   | 路径                    | 说明     | 认证 |
| ------ | ----------------------- | -------- | ---- |
| GET    | /api/note-tags          | 标签列表 | 是   |
| POST   | /api/note-tags          | 新建标签 | 是   |
| PUT    | /api/note-tags/{id}     | 修改标签 | 是   |
| DELETE | /api/note-tags/{id}     | 删除标签 | 是   |

---

### 六、学习记录 `/api/study-records`

| 方法   | 路径                       | 说明         | 认证 |
| ------ | -------------------------- | ------------ | ---- |
| GET    | /api/study-records         | 记录列表     | 是   |
| GET    | /api/study-records/{id}    | 记录详情     | 是   |
| POST   | /api/study-records         | 新建记录     | 是   |
| PUT    | /api/study-records/{id}    | 修改记录     | 是   |
| DELETE | /api/study-records/{id}    | 删除记录     | 是   |

#### POST `/api/study-records` — 新建学习记录

**请求体：**

```json
{
  "subject": "Spring Boot 基础",
  "date": "2026-07-09",
  "duration": 120,
  "content": "学习了自动配置原理和 Starter 机制",
  "reflection": "对 Spring Boot 的运行原理有了更深入的理解"
}
```

---

### 七、Todo `/api/todos`

| 方法   | 路径                       | 说明       | 认证 |
| ------ | -------------------------- | ---------- | ---- |
| GET    | /api/todos                 | 任务列表   | 是   |
| GET    | /api/todos/{id}            | 任务详情   | 是   |
| POST   | /api/todos                 | 新建任务   | 是   |
| PUT    | /api/todos/{id}            | 修改任务   | 是   |
| DELETE | /api/todos/{id}            | 删除任务   | 是   |
| PATCH  | /api/todos/{id}/done       | 切换完成   | 是   |

#### POST `/api/todos` — 新建任务

**请求体：**

```json
{
  "title": "完成 Spring Security 配置",
  "content": "实现 JWT 登录认证流程",
  "priority": 1,
  "dueDate": "2026-07-10"
}
```

---

### 八、文件管理 `/api/files`

| 方法   | 路径                     | 说明       | 认证 |
| ------ | ------------------------ | ---------- | ---- |
| GET    | /api/files               | 文件列表   | 是   |
| GET    | /api/files/{id}          | 文件详情   | 是   |
| POST   | /api/files/upload        | 上传文件   | 是   |
| GET    | /api/files/{id}/download | 下载文件   | 是   |
| DELETE | /api/files/{id}          | 删除文件   | 是   |

#### POST `/api/files/upload` — 上传文件

**请求方式：** `multipart/form-data`

| 参数       | 类型 | 必填 | 说明     |
| ---------- | ---- | ---- | -------- |
| file       | File | 是   | 文件     |
| categoryId | Long | 否   | 分类 ID  |

#### GET `/api/files` — 文件列表（分页）

| 参数       | 类型   | 必填 | 说明         |
| ---------- | ------ | ---- | ------------ |
| page       | int    | 否   | 页码         |
| size       | int    | 否   | 每页条数     |
| keyword    | string | 否   | 搜索关键词   |
| type       | string | 否   | 文件类型筛选 |
| categoryId | long   | 否   | 分类筛选     |

---

### 九、文件分类 `/api/file-categories`

| 方法   | 路径                       | 说明     | 认证 |
| ------ | -------------------------- | -------- | ---- |
| GET    | /api/file-categories       | 分类列表 | 是   |
| POST   | /api/file-categories       | 新建分类 | 是   |
| PUT    | /api/file-categories/{id}  | 修改分类 | 是   |
| DELETE | /api/file-categories/{id}  | 删除分类 | 是   |

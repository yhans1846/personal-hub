# Personal Hub - 分步开发计划

## 进度总览

| Step | 内容 | 状态 |
|------|------|------|
| 1 | 后端脚手架 | ✅ |
| 2 | 前端脚手架 | ✅ |
| 3 | 数据库初始化 + 用户认证 | ✅ |
| 4 | Swagger 集成 + 注解规范 | ✅ |
| 5 | Markdown 笔记模块 | ✅ |
| 6 | 学习记录模块 | ✅ |
| 7 | Todo 模块 | 🔲 |
| 8 | 文件管理模块 | 🔲 |
| - | 后续：日记/收藏夹/学习计划/阅读记录 | 🔲 |

## 各步文件清单

### Step 1 — 后端脚手架
`pom.xml` / `application.yml` / `PersonalHubApplication.java` / `common/result/{Result,PageResult}.java` / `common/exception/{Global,Business,Unauthorized,NotFound}Exception.java` / `common/config/{MyBatisPlus,Jackson,Redis}Config.java`

### Step 2 — 前端脚手架
Vite 模板 + vue-router + pinia + axios + element-plus / `request.ts` / 路由 / authStore / `AppLayout.vue` / Login/Dashboard 占位

### Step 3 — 用户认证
`sql/init.sql`(10表) / auth模块(login/logout) + user模块(profile/password) / JWT过滤器 / Security配置 / 前端登录页

### Step 4 — Swagger
SpringDoc OpenAPI 2.6.0 / 所有 Controller/DTO/VO 注解 / OpenApiConfig / Security放行

### Step 5 — Markdown 笔记
note模块(CRUD+分类+标签+收藏+回收站) / 前端 5 页面(List+Editor+CategoryManage+TagManage+RecycleBin)

### Step 6 — 学习记录
study模块(CRUD+日期筛选+关键词搜索) / 前端 2 页面(List+Form)

### Step 7 — Todo（待开发）
todo模块(CRUD+状态+优先级+截止日期) / TodoList.vue

### Step 8 — 文件管理（待开发）
file模块(上传+下载+分类) / 前端 FileList.vue + FileCategoryManage.vue

### 后续（第二阶段）
日记 / 收藏夹 / 学习计划 / 阅读记录 / Dashboard & 数据统计 / 全局搜索 / 标签系统

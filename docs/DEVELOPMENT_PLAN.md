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
| 7 | **Maven 多模块拆分** — 父 POM + common/module-auth/module-note/module-study/web | ✅ |
| 8 | **代码质量统一** — PageParam 分页基类 + ResultCode 枚举 + 全局 @Slf4j 日志 | ✅ |
| 9 | Todo 模块 | ✅ |
| 10 | 文件管理模块 | ✅ |
| 11 | 日记模块 | ✅ |
| 12 | 收藏夹模块 — ph-bookmark 模块 + 前端 BookmarkList/BookmarkForm/BookmarkCategoryManage | ✅ |
| 13 | 学习计划模块 — ph-studyplan 模块 + study_record 追加 plan_id | ✅ |
| - | 后续：阅读记录 | 🔲 |

## 各步文件清单

### Step 1 — 后端脚手架
`pom.xml`(父POM) / `ph-boot`模块启动类 / `ph-common`模块(result/exception/config/util/filter) / `ph-boot`模块 resources

### Step 2 — 前端脚手架
Vite 模板 + vue-router + pinia + axios + element-plus / `request.ts` / 路由 / authStore / `AppLayout.vue` / Login/Dashboard 占位

### Step 3 — 用户认证
`sql/init.sql`(10表) / `ph-auth`模块(auth+user) / JWT过滤器 / Security配置 / 前端登录页

### Step 4 — Swagger
SpringDoc OpenAPI 2.6.0 / 所有 Controller/DTO/VO 注解 / OpenApiConfig(common) / Security放行

### Step 5 — Markdown 笔记
`ph-note`模块(CRUD+分类+标签+收藏+回收站) / 前端 5 页面(List+Editor+CategoryManage+TagManage+RecycleBin)

### Step 6 — 学习记录
`ph-study`模块(CRUD+日期筛选+关键词搜索) / 前端 2 页面(List+Form)

### Step 7 — Maven 多模块拆分
父 POM + 5 子模块(`ph-common/ph-auth/ph-note/ph-study/ph-boot`) / 统一 `ph-` 前缀 / 文档更新

### Step 8 — 代码质量统一
`PageParam` 分页基类 + `ResultCode` 状态码枚举 + 全局 `@Slf4j` 日志 / 编码规范补充公共组件思想

### Step 9 — Todo 模块
`ph-todo`模块(CRUD+状态+优先级+截止日期+超期提醒) / 前端 TodoList.vue + TodoForm.vue

### Step 10 — 文件管理模块
`ph-file`模块(上传+下载+分类+搜索) / 前端 FileList.vue + FileCategoryManage.vue

### Step 11 — 日记模块
`ph-diary`模块 + `sql/init.sql`(新增diary_entry表) / 前端 DiaryList.vue + DiaryForm.vue / 路由+导航

### Step 12 — 收藏夹模块
`ph-bookmark`模块(BookmarkUrl+BookmarkCategory) + `sql/init.sql`(新增bookmark_url+bookmark_category表) / 前端 BookmarkList(卡片网格)+BookmarkForm+BookmarkCategoryManage / 路由+导航

### Step 13 — 学习计划模块
`ph-studyplan`模块 + `sql/init.sql`(新增study_plan表+study_record追加plan_id) / 前端 StudyPlanList(进度条)+StudyPlanForm / 路由+导航

### 后续（第二阶段）
阅读记录 / Dashboard & 数据统计 / 全局搜索 / 标签系统

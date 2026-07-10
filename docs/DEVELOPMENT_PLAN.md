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
| 14 | 阅读记录模块 — ph-reading 模块 | ✅ |
| - | 第二阶段全部完成！🎉 | |
| 15 | 统一标签系统 — ph-tag 模块 + tag/tag_rel 表 + 统一管理页面 + 笔记/收藏夹迁移 | ✅ |
| 16 | Dashboard 统计 — ph-dashboard 模块 + 统计卡片概览页 | ✅ |
| 17 | 数据统计趋势图 — ECharts 趋势图（学习/笔记/Todo/阅读）+ 日期范围切换 | ✅ |
| 18 | 全局搜索 — 跨 8 模块统一搜索 + 关键词高亮 + 结果分组 | ✅ |
| - | 第三阶段全部完成！🎉 | |

## 各步文件清单

### Step 1 — 后端脚手架
父 POM + ph-boot/ph-common 模块脚手架

### Step 2 — 前端脚手架
Vite + vue-router + pinia + axios + element-plus + 登录/Dashboard 占位

### Step 3 — 用户认证
sql/init.sql(10表) + ph-auth 模块 + JWT + Security + 前端登录页

### Step 4 — Swagger
SpringDoc OpenAPI + 所有 Controller/DTO/VO 注解

### Step 5 — Markdown 笔记
ph-note 模块(CRUD+分类+标签+收藏+回收站) + 前端 5 页面

### Step 6 — 学习记录
ph-study 模块 + 前端 2 页面

### Step 7 — Maven 多模块拆分
父 POM + 5 子模块(ph-common/ph-auth/ph-note/ph-study/ph-boot)

### Step 8 — 代码质量统一
PageParam + ResultCode + @Slf4j 全局日志

### Step 9 — Todo 模块
ph-todo 模块 + 前端 TodoList/TodoForm

### Step 10 — 文件管理模块
ph-file 模块 + 前端 FileList/FileCategoryManage

### Step 11 — 日记模块
ph-diary 模块 + sql 新表 + 前端 DiaryList/DiaryForm

### Step 12 — 收藏夹模块
ph-bookmark 模块 + sql 新表 + 前端 BookmarkList/BookmarkForm/BookmarkCategoryManage

### Step 13 — 学习计划模块
ph-studyplan 模块 + sql 新表 + 前端 StudyPlanList/StudyPlanForm

### Step 14 — 阅读记录模块
ph-reading 模块 + sql 新表 + 前端 ReadingList/ReadingForm

### 后续（第三阶段）
Dashboard & 数据统计 / 全局搜索 / 标签系统

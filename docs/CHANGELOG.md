# Personal Hub - 版本记录

## [Unreleased]

### 2026-07-09（已完成）
- **代码质量统一提升**：抽取 PageParam 分页基类 + ResultCode 状态码枚举 + 全局 @Slf4j 日志
- **Maven 多模块拆分**：父 POM + 5 子模块（ph-common/ph-auth/ph-note/ph-study/ph-boot）
- 更新开发计划/编码规范/技术栈文档

### 2026-07-10（已完成）
- **Todo 待办任务模块**：完整 CRUD + 完成状态切换 + 优先级(高/中/低) + 截止日期 + 超期提醒
- 后端 ph-todo 模块（Entity/DTO/VO/Mapper/Service/Controller）+ 前端 TodoList/TodoForm 页面
- **文件管理模块**：文件上传/下载/列表/搜索/删除 + 文件分类管理
- 后端 ph-file 模块 + 前端 FileList/FileCategoryManage 页面
- **日记模块**（第二阶段）：日记 CRUD + 月视图聚合 + 关键词搜索 + 心情/天气记录
- 后端 ph-diary 模块 + 前端 DiaryList/DiaryForm 页面 + 侧边栏导航
- **收藏夹模块**（第二阶段）：网址 CRUD + 分类管理 + 标签管理 + 关键词搜索
- 后端 ph-bookmark 模块（BookmarkUrl + BookmarkCategory）+ 前端 BookmarkList/BookmarkForm/BookmarkCategoryManage
- **学习计划模块**（第二阶段）：计划 CRUD + 进度跟踪 + 时间管理 + 关联学习记录
- 后端 ph-studyplan 模块 + study_record 表追加 plan_id 字段
- 前端 StudyPlanList（进度条/状态标签/记录数）/ StudyPlanForm
- **阅读记录模块**（第二阶段）：记录 CRUD + 章节进度 + 阅读笔记
- 后端 ph-reading 模块 + 前端 ReadingList/ReadingForm
- **第二阶段全部完成 🎉**
- 更新路线图/数据库/API/版本记录

### 第一阶段 MVP 全部完成 ✅
- **项目初始化**：Spring Boot 3 后端脚手架 + Vue 3 前端脚手架
- **数据库初始化**：10 张第一阶段表
- **用户认证模块**：JWT 登录/退出 + Spring Security 配置 + 个人信息/修改密码
- **Swagger 集成**：SpringDoc OpenAPI，所有 Controller/DTO/VO 添加注解
- **Markdown 笔记模块**：CRUD + 分类管理 + 标签管理 + 收藏 + 搜索 + 回收站
- **学习记录模块**：CRUD + 日期范围筛选 + 关键词搜索
- 文档压缩精简 + 编码规范完善

---

## [0.1.0] — 2026-07-09
- 初始化项目仓库，完成项目规划文档
- docs 体系搭建：PROJECT / TECH_STACK / ROADMAP / DATABASE / API / STYLE_GUIDE / CHANGELOG
- 安装 Superpowers-ZH 技能框架（20 skills）

# Personal Hub - 版本记录

## [Unreleased]

### 2026-07-12 代码清理与配置优化
- 配置文件分层：application.yml（公共）+ application-dev.yml + application-prod.yml
- 补充 springdoc-openapi 依赖，修复 Swagger 403（/swagger-ui.html 白名单）
- Swagger 仅 dev 环境开启，prod 默认关闭
- 删除废弃文件：test_data.sql、孤立 POM（ph-resource/ph-resource）、note/TagManage.vue
- 删除死类型：BookmarkCategoryVO/DTO、FileCategory
- 删除 5 个未使用 API 函数 + 冗余路由 /notes/tags
- 修复 Editor.vue getCategories 缺少 type 参数

### 2026-07-11 第八阶段：CRUD UI 优化 + 分类系统合并
- 统一 UI 组件库（UiDialog/UiInput/UiSelect/UiButton/UiSection/UiCard 等 8 个）
- 全部主实体 CRUD 改为 Dialog/Drawer（Todo/Bookmark/日记/Reading/Study/StudyPlan）
- 笔记/日记编辑器使用 Ui 组件规范
- 分类系统合并：note_category/bookmark_category/file_category → 统一 `category` 表
- 废弃 note_tag 表及代码清理
- 统一分类管理页面（Tab 切换笔记/收藏夹/文件）
- 菜单迁移逻辑，自动过滤废弃菜单项

### 2026-07-11 第七阶段：工作台自定义布局
- `user_layout` 表 + 布局配置 CRUD API
- layoutStore（Pinia + localStorage 离线缓存）
- 侧边栏数据驱动（显示/隐藏/拖拽排序）
- Dashboard 卡片数据驱动（显示/隐藏/拖拽排序）
- 系统设置页（tabs 切换 + SortableJS 拖拽 + 恢复默认）
- 登录后自动同步用户布局配置

### 2026-07-11 第六阶段：体验打磨 + 后端优化 + 通知系统
- 搜索栏 Ctrl+K 快捷键提示
- 学习统计 SQL 聚合优化（SUM + DISTINCT 替代全量加载）
- 笔记 Markdown 导出（.md 文件下载）
- 通知系统 + NotificationBell 组件

### 2026-07-11 第五阶段：功能增强
- 笔记：标签颜色显示、阅读时间估算、MD 类型图标、最近编辑标识
- 待办：拖拽排序（sortablejs）、已完成折叠
- 学习记录：今日时长/本周统计/连续天数
- 日记：地点记录、配图上传
- 学习计划：剩余天数、超期标记
- 阅读记录：评分(1-5星)、阅读时长

### 2026-07-11 第四阶段：UI 优化
- Accent Color 主题色切换（5 种）、Command Palette、响应式适配
- Markdown 编辑器（md-editor-v3）、设计令牌刷新
- 侧边栏三段分组、共享组件库（EmptyState/StatCard/PageHeader）
- Dashboard 扩展、Topbar 增强、过渡动画、深色模式
- 空状态插画（9 套 SVG）

### 2026-07-11 第三阶段
- 统一标签系统（ph-tag 模块 + tag/tag_rel 表）
- Dashboard 统计 + ECharts 趋势图 + 全局搜索

### 2026-07-10 第二阶段
- Todo、文件管理、日记、收藏夹、学习计划、阅读记录模块

### 2026-07-09 第一阶段 MVP
- 项目初始化、用户认证、Markdown 笔记、学习记录
- Maven 多模块拆分、代码质量统一、Swagger 集成
- docs 体系搭建（8 文档）

---

## [0.1.0] — 2026-07-09
- 初始化项目仓库，完成项目规划文档
- docs 体系搭建：PROJECT / TECH_STACK / ROADMAP / DATABASE / API / STYLE_GUIDE / CHANGELOG

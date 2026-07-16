# Personal Hub - 版本记录

## [Unreleased]

### 2026-07-16 学习计划卡片布局调整
- **卡片信息层级**：标题+状态 → 软色标签（最多 2 个 +`+N`）→ 来源·作者 → 进度 → 点分日期 → 备注 → 底栏相对更新时间 + 操作
- **表格视图**：不变

### 2026-07-16 学习计划 Product UI 重构
- **列表**：传统 Table 改为 Product Table（行高留白、弱边框、主信息列）；支持 Table / Card 视图切换
- **列**：名称（标题+作者·来源）/ 浅色分类 Tag / 圆点状态 / Progress / 学习周期 / 来源图标 / ··· 操作菜单
- **工具栏**：搜索 + 分类 + 状态 + 排序（默认最近更新）+ 视图切换 + 新建
- **统计**：Header 展示学习中 / 已完成 / 暂停 / 未开始；`GET /api/study-plans/stats`
- **模型**：恢复 `progress`；状态文案改为未开始 / 学习中 / 已完成 / 已暂停

### 2026-07-16 学习计划表格化完全重构
- **列表**：卡片改为轻边框 `el-table`（分类标签 / 名称 / 来源 / 作者 / 起止 / 状态 / 地址 / 备注）
- **模型**：新增 `source`/`author`/`url`/`remark`；去掉 `goal`/`progress`；状态文案改为待学习/进行中/已完成/已暂停
- **分类**：复用统一标签（`tag_rel` + `entity_type=study_plan`），工具栏可按标签筛选
- **连带**：首页今日计划去掉进度条；全局搜索改为名称+来源+作者+备注
- **迁移**：`sql/migration-20260716-study-plan-table.sql`

### 2026-07-16 通知清空后不再重复展示
- **根因**：清空物理删除后，`/notifications/check` 因「无未读」再次生成超期/计划通知
- **修复**：`is_dismissed` 软清除；列表/未读排除已清空；同 type+related_id 含已读/已清空不再重复生成

### 2026-07-16 首页六宫格 + 外部快捷
- **布局**：2×3 等宽等高；默认今日 / 快捷 / 外部快捷 / 笔记 / 学习 / 阅读
- **外部快捷**：收藏夹 `show_on_dashboard`；编辑开关；`GET /api/bookmarks/dashboard`
- **资源盘点**：独立卡移除，四指标并入 Hero 可点链接
- **设置**：Dashboard 布局随默认项同步（含外部快捷）；本地 key `layout-dashboard-v5`

### 2026-07-16 首页资源盘点小统计
- **移除**：独立「待办任务」卡片（`pending_todos`），避免与「今日任务」重复
- **新增**：`resource_snapshot` 整行资源盘点（本月日记 / 收藏 / 文件 / 在读中），指标与 `/stats` KPI 错开
- **兼容**：`REMOVED_DASHBOARD_CODES` + 本地 key `layout-dashboard-v4`

### 2026-07-16 工作台 Bento 信息中心
- **布局重构**：稀疏两栏 → 12 列 Bento 网格；Hero 问候 + 超期入口
- **配置驱动**：`visibleDashboardCards` 真正控制显隐/排序；默认 today / quick / notes / studies / reading
- **组件拆分**：`dashboard/widgets/*`（Today / Quick / Notes / Studies / Reading / Todos）
- **去重**：移除与 `/stats` 重复的 KPI 条、近 7 日趋势、最近活动（改由数据统计页承载）
- **兼容**：`ensureDashboardCards` + `REMOVED_DASHBOARD_CODES`；本地 key `layout-dashboard-v3`

### 2026-07-15 Cursor 浏览器巡检方法文档化
- **Cursor 规则**：新增 `.cursor/rules/browser-qa-testing.mdc`（Browser MCP + Playwright + 日志对照）
- **文档**：新增 `docs/qa/CURSOR_BROWSER_TESTING.md`（完整方法、检查清单、产物规范）

### 2026-07-15 深链跳转修复（工作台 / 通知 → 列表弹窗）
- **根因**：待办/学习/阅读/日记使用 List+Dialog，却跳转未注册的 `/:id/edit`、`/new`，导致空白页
- **方案**：统一 `?edit=` / `?create=1` 深链；`useDeepLinkDialog` 消费 query 并打开弹窗
- **兼容**：路由 redirect 兼容旧路径（如 `/todos/1/edit` → `/todos?edit=1`）
- **修复点**：Dashboard 列表项与「开始学习」、notificationStore、笔记编辑 loading 骨架
- **附带**：工作台最近阅读显示 `bookTitle`；学习计划空状态补 `Plus` 图标；vitest + deepLink 单测

### 2026-07-15 WYSIWYG 编辑器重构（md-editor-v3 → Vditor IR）
- **编辑器替换**：md-editor-v3 → Vditor IR 模式，输入即渲染 WYSIWYG 体验
- **右键菜单**：完整上下文菜单系统，39 项操作（格式/标题/列表/表格/代码块/Mermaid/公式/图片上传等）
- **新组件**：NoteVditor + NoteMarkdownPreview + vditorSetup + context-menu（ContextMenu / EditorContextMenu / TableGridPicker / contextMenuActions）
- **模式简化**：去除分栏预览；保留 edit / preview / focus 三模式（快捷键 Ctrl+Shift+P、Ctrl+Shift+F、Esc）
- **列表/回收站**：卡片右键菜单（编辑/预览/收藏/删除/恢复），UI 统一
- **删除旧组件**：NoteMdEditor.vue、EditorPreviewPanel.vue、mdEditorToolbars.ts、mdEditorSetup.ts
- **依赖变更**：新增 vditor^3.11，移除 md-editor-v3

### 2026-07-15 专业 Markdown 编辑器
- **完整工具栏**：撤销/重做、标题、列表、任务、代码块、Mermaid、KaTeX、Prettier、目录、全屏等
- **组件封装**：`NoteMdEditor` + `EditorPreviewPanel`，统一 Mermaid/KaTeX 本地扩展（无 CDN 依赖）
- **预览增强**：TOC 目录、代码块复制、图片缩放（medium-zoom）、笔记图片鉴权代理
- **分栏默认开启**：`livePreview` 默认 true，比例 55:45；`Ctrl+Shift+L` 切换分栏
- **修复**：工具栏 `heading` → `title`（md-editor-v3 合法项）
- **新依赖**：mermaid ^11、katex ^0.17

### 2026-07-15 Logback 按天归档
- **Logback**：新增 `logback-spring.xml`，按日期滚动，`maxHistory=30`
- **双文件**：`personal-hub.log` + `personal-hub-error.log`（仅 ERROR）
- **部署**：`LOG_PATH` 环境变量；Compose / Dockerfile 挂载 `/data/personal-hub/logs`

### 2026-07-14 正确性 / 性能 / 安全优化
- **回收站清理**：`NoteResourceCleanupTask` 仅删除 DB 无对应行的孤立目录，不再清理软删除笔记文件
- **生产密钥**：`ProdSecretsValidator` + `application-prod.yml` 取消弱默认值，缺失/弱密钥拒绝启动
- **JWT query**：仅允许 `/api/notes/{id}/images|attachments/**` 使用 `?token=`
- **列表 N+1**：笔记列表批量分类/标签；标签 usage 一次聚合；文件列表分类 map 外提
- **笔记 excerpt**：`note_note.excerpt` + 创建/更新时生成；列表卡片优先显示摘要
- **Redis 缓存**：分类/标签列表 `@Cacheable`，收紧多态反序列化白名单；登录 Redis 限流
- **健康检查**：Actuator `/actuator/health`；Docker HEALTHCHECK 与 Nginx login `limit_req`
- **索引**：`idx_user_deleted_updated`；迁移脚本 `sql/migration-20260714-note-excerpt-index.sql`
- **前端拆包**：Vite `manualChunks`（vue / element / editor / echarts）
- **测试**：JWT query 白名单、excerpt、回收站清理决策单测

### 2026-07-14 Docker Compose + GitHub Actions 部署
- **部署文档**：新增 `docs/DEPLOYMENT.md`（Ubuntu 22.04、Docker 速览、Self-hosted Runner、运维与排障）
- **Compose 骨架**：`deploy/` 含前后端 Dockerfile、Nginx 反代、MySQL/Redis、`.env.example`
- **CI/CD**：`.github/workflows/deploy.yml`（`self-hosted`，push `main` / 手动触发）
- **生产配置**：`application-prod.yml` 支持 `MYSQL_*` / `REDIS_*` / `JWT_SECRET` 等环境变量覆盖

### 2026-07-14 沉浸式创作模式（Focus Mode）
- **沉浸式创作模式**：10 项功能全面提升笔记写作体验
- **全屏创作**：右上角全屏按钮 + Fullscreen API + Ctrl+Shift+F
- **实时预览**：Splitpanes 分栏（编辑 + Preview 并排），可拖拽调整比例（40:60～60:40），比例持久化
- **预览滚动同步**：左右栏百分比同步滚动，读写位置一致
- **阅读模式**：工具栏隐藏，仅标题 + 正文 + Preview，适合校对
- **Focus Mode**：隐藏左侧菜单/Header/Footer/面包屑/工具栏，仅标题 + 正文 + 状态栏
- **底部状态栏增强**：字数/段落数/阅读时间/最后保存时间/Markdown/UTF-8
- **状态记忆**：livePreview/fullscreen/splitRatio/focusMode 持久化到 localStorage
- **编辑器 Header 重构**：新增全屏 / 分栏预览 / Focus Mode 按钮
- **Preview 路由独立**：`/notes/:id/preview` 脱离 AppLayout 嵌套，避免侧边栏/顶部栏干扰
- **新依赖**：splitpanes ^4.1.2

### 2026-07-14 个人资料模块 + 待办重构 + 外观扩展
- **个人资料模块（Profile）**：新增 UserController（GET/PUT `/api/user/profile`，POST `/api/user/avatar`）
- **sys_user 表扩展**：新增 gender/birthday/phone/country/province/city/district/website/github/bio 10 个字段
- **头像系统**：独立 avatars 存储目录，支持 JPG/PNG/GIF/WebP，最大 5MB，公开访问 `/api/files/avatar/*`
- **ProfileDrawer 左侧抽屉**：点击右上角头像弹出，4 卡片布局（头像/基本信息/联系方式/个人简介），4 层级联地区选择器
- **待办任务重构**：Tab 分组导航（全部/今天/进行中/已完成/已超期），信息卡片布局，截止日期相对时间徽章（今天/明天/N天后/已超期N天）
- **todo_task 表扩展**：新增 completed_at 字段，toggleDone 时自动写入/清空
- **外观设置**：强调色从 5 种扩展到 9 种（新增粉色/红色/teal/靛蓝），CSS 变量 + layoutStore 类型同步
- **安全配置**：`/api/files/avatar/**` 放行公开访问
- **Topbar**：高度 64→48px，右上角头像图片显示，左上角 mini 头像取消

### 2026-07-13 统计页面 V4 全面重写
- 新增 8 个统计模块：KPI Summary、学习面积图、笔记柱状图、活跃热力图、Todo 环形图、分类/标签排行、活动时间线、学习洞察
- 后端新增 `GET /api/dashboard/detail` 综合统计接口（StatsVO，含全部模块数据）
- 后端新增热力图、分类统计、标签排行、活动时间线、连续天数、周环比、学习洞察等 12 个 SQL 查询
- 前端 StatsView.vue 完全重写：5 种图表类型（面积/柱状/热力图/环图/横向柱状）、时间范围联动、分类-标签切换
- 页面布局：顶部 KPI 4 卡 → 双栏图表网格 → 底部 Timeline + Insight 双栏
- 设计规范：16px 圆角 Card、统一 280px 图表高度、Lucide 图标、骨架屏加载、空状态/错误状态处理
- **统计卡片配置化**：新增 StatsManager 拖拽排序 + 显隐控制，集成到系统设置工作台
- **时间选择器全卡片生效**：3/7/15/30/90 天选项，分类/标签/活动/连续天数/洞察均按范围过滤
- **layoutStore 扩展**：新增 statsCards 状态、visibleStatsCards getter、save/reset/merge 方法
- 项目审计：生成 PROJECT_AUDIT.md，5 维评分全面评估
- API 层重构：删除 11 个冗余重导出 shim 文件，统一 import 路径为 `@/modules/*/api`
- 类型增强：UiDatePicker/UiSelect 的 `any` 类型替换为联合类型
- 工具函数统一：`estimateReadingTime` 删除 Preview.vue 内联副本，统一使用 `utils/readingTime.ts`
- CSS Token 迁移：global.css / markdown-prose.css 中 7 处原始像素值替换为 CSS 变量 token
- 大组件拆分：CategoryManage(649→599行) 提取 CategoryStatsCards 子组件；TagManage(647→539行) 提取 TagStatsCards + HotTagsBar 子组件
- Composable 抽象：新增 `useEntityDialog`（6 个 Dialog 共享生命周期），新增 `useStorageSync`（3 个 Store 共享双存储模式）
- 后端清理：删除 `TagService.getEntityIdsByTag()` 死方法，删除 `application-dev.yml` 注释行
- 重复代码消除：`MarkdownImportService` 中的文件读取逻辑复用 `ImportService.readFileContent()`
- 文档更新：简化 CLAUDE.md，生成 PROJECT_CLEANUP_REPORT.md
- CLAUDE.md 简化：移除 superpowers-zh 技能框架章节

### 2026-07-12 系统设置 V3 精简版 — 布局重构
- Tab 精简：6个 → 4个（工作台/阅读/外观/高级），高级合并通知+数据+实验功能
- 布局重构：去除内层 max-width 约束，内容自由填充 Card，页面居中 1080px
- 底部自动保存指示条：设置修改后短暂显示"✓ 已自动保存"
- Card 统一 16px 圆角、24px padding、间距 20px
- 组件级 max-width 移除，充分利用页面宽度
- 删除未使用文件：PlaceholderTab.vue、CacheSettings.vue（功能已合并到 DataManagement）
- 新增 AdvancedSettings.vue 统一管理高级设置

### 2026-07-12 系统设置 V2 Phase 2
- 新增通知设置模块：桌面通知权限请求、通知类型多选 Chip（4类）、通知音效开关+选择、免打扰模式+时间段
- 新增数据管理模块：缓存管理（清空缓存）+ 数据导出（模块选择+格式选择）+ 数据备份（立即备份/导入恢复）
- 新增实验功能模块：Mermaid/KaTeX/AI 助手/Backlink 开关面板，Beta 提示横幅
- 外观设置扩展：界面圆角（4档）、动画速度（4档）、界面密度（3档 舒适/标准/紧凑），通过 CSS 变量实时生效
- 新增 notificationConfigStore（Pinia）：通知配置 localStorage + 后端双持久化，含桌面通知权限管理
- 新增 featureFlagStore（Pinia）+ useFeatureFlag composable：实验功能开关响应式管理
- 新增后端 ExportController + BackupController：导出/备份 RESTful 接口
- 新增 systemApi.ts：前端导出/备份 API 调用封装
- SettingsView 通知/实验功能 Tab 从 PlaceholderTab 替换为正式组件；数据 Tab 替换为 DataManagement

### 2026-07-12 系统设置 V2 Phase 1
- 全面重写 SettingsView：自定义 Icon+Label Tab 导航（6个Tab），内容 fadeIn 切换动画，max-width 1080px
- 新增外观设置模块：主题切换（浅色/深色）+ 强调色选择（5色），从 AppLayout 迁入设置页，双向状态同步
- 新增缓存管理模块：localStorage 大小估算 + 清空缓存（保留登录态和主题偏好）
- 新增通用占位 Tab 组件（PlaceholderTab），用于通知/实验功能占位
- MenuManager / DashboardManager UI 对齐 Card/Section 规范（Section 分割线、13px/600w 标题、间距 32px）
- ReadingExperience 扩展：段落间距（1.0/1.2/1.5em）、代码字体（系统/等宽）、代码字号（同正文/13px/14px）
- readingConfigStore 新增 paragraphGap / codeFontSize / codeFontFamily 字段 + cssVars 导出
- layoutStore 新增 appearanceConfig 状态 + saveAppearanceConfig / resetAppearanceConfig / fetchAppearanceFromBackend
- 外观配置通过 `user_layout(layout_type='appearance')` 持久化，现有布局接口通用支持
- 顶栏外观快捷操作（主题/强调色）保持保留，操作同步到设置页

### 2026-07-12 标签管理 & 分类管理页面全面优化
- 分类管理：统计卡片（全部分类/正在使用/未使用）、Segment 风格模块切换、搜索+排序工具栏、Grid 卡片布局(300px+)、拖拽排序(SortableJS)、悬停阴影动画
  - 后端新增 `PUT /api/categories/sort` 批量排序接口 + `SortOrderDTO`
  - 修复笔记分类计数始终返回 0 的 bug（CategoryMapper.xml note 分支缺失）
- 标签管理：统计卡片（标签总数/引用总次数/本周新增）、热门标签条、搜索+颜色筛选+排序工具栏、Grid 卡片布局(240px)，左侧彩色色条、使用次数 Badge
- 两个页面统一 max-width 约束、按资源管理中心的视觉语言设计
- UiDialog 重写：新增三段固定布局（Header/Body/Footer），默认宽度 720px，16px 圆角，32px padding，fade+scale 弹出动画
- DiaryDialog 全面重写：心情卡片（emoji+label，绿色选中）、天气图标点选、DropZone 图片上传、无 Label 大标题设计
- TodoDialog 全面重写：优先级卡片、内联日期选择器、内容优先 layout
- BookmarkDialog 全面重写：Chip 风格分类/标签选择、无 Label
- Reading/StudyPlan/Study 从 el-drawer 迁移到 UiDialog，统一三段布局
- Tag/Category 对话框消除 el-form Label，对齐统一设计语言
- 删除旧文件：ReadingDrawer.vue、StudyDrawer.vue、StudyPlanDrawer.vue

### 2026-07-12 阅读配置迁移至系统设置 + 图片比例可调
- 新增 `readingConfigStore` (Pinia) 统一管理字号/宽度/行高/主题/图片比例，localStorage + 后端双持久化
- 系统设置页新增「阅读体验」Tab，替代预览页 popover 阅读设置
- 阅读设置包含：字号(14-22)、宽度(900-1280)、行高(1.4-2.0)、4 主题(follow/light/dark/sepia)、图片显示比例(60%-100%)
- 图片比例通过 CSS 变量 `--image-max-width` 动态控制，Preview.vue / Editor.vue 预览区同步生效
- 删除旧 `usePreviewSettings.ts` / `usePreviewTheme.ts` / `ReadingSettings.vue`
- 旧 localStorage key 自动迁移（`preview-reading-settings` + `preview-theme` → `reading-config`）
- 后端 `user_layout` 表新增 `layout_type='preview'` 支持
- JWT 过滤器支持 `?token=` 查询参数认证，解决 `<img>` 标签无法添加 Authorization header 的问题
- Preview.vue 图片 src 通过 `setupImageProxy()` 自动改写为 API 路径 + token

### 2026-07-12 Markdown 导入功能（文件 + 粘贴）
- 新增 `ph-knowledge/imports/` 组件包：ResourceScanner（扫描）、ResourceResolver（解析）、ResourceDownloader（下载）、MarkdownRewriter（重写）、ImportReport（报告）
- 方式一「从 Markdown 文件导入」：上传 .md 文件，自动下载网络图片/复制本地文件，支持 baseDir 参数解析相对路径
- 方式二「粘贴 Markdown 内容导入」：粘贴文本，自动下载网络图片和 Base64 图片，检测到相对路径时给出提示
- 导入后资源统一存储到 `notes/{noteId}/images/`，使用 UUID 文件名，自动重写 Markdown 引用
- 新增前端 ImportMarkdownDialog 组件（双 Tab 切换），笔记列表页增加导入入口
- 所有导入产生 ImportReport，记录成功/失败/跳过的资源明细

### 2026-07-12 预览页阅读体验全面重构
- 新增 DocLayout 统一文档布局组件（Header + 可折叠 TOC + 正文区），可复用于帮助文档、README 等页面
- 新增阅读设置面板：字号 A−/A+（14-22）、宽度三档（900/1100/1280）、行高滑块（1.4-2.0）、主题切换（浅色/深色/护眼绿）
- 新增全局 Markdown 排版系统 `markdown-prose.css`，统一 h1-h6/p/blockquote/pre/table 等全部元素样式
- 预览主题支持 4 套（跟随系统/浅色/深色/护眼绿），通过 CSS 变量桥接 md-editor-v3 内部颜色
- 正文左右 48px 留白，代码块/表格保持与正文对齐，图片可突破至全宽
- 标题 hover 显示 `#` 锚点链接，代码块 hover 显示复制按钮
- 新增 medium-zoom 图片点击放大
- 左侧目录默认展开、手动收起、可拖拽调整宽度（160-400px）
- 修复 md-editor-v3 硬编码 font-size/line-height/color 导致阅读设置不生效的问题
- 新增 `docs/PREVIEW_PAGE_SPEC.md` 开发规范文档

### 2026-07-12 笔记回收站改造 + 统一审计日志
- note_note 表新增 `deleted_at` / `delete_reason` 字段，用于回收站排序和自动归档预留
- 创建 `audit_log` 统一审计日志表，放入 ph-system 模块，所有业务模块可复用
- 删除/恢复笔记：写入审计日志 + 事务保护 + 删除时间管理
- 新增 `GET /api/notes/recycle` 回收站列表接口（按删除时间倒序）
- 新增 `GET /api/notes/{id}/preview` 只读预览接口（跳过 is_deleted 校验）
- 重写 RecycleBin.vue：展示分类/标签/时间列、分页搜索、查看按钮
- 新建 Preview.vue：只读 Markdown 渲染、左侧可拖拽标题大纲、回收站/预览模式自适应提示
- 笔记列表页增加预览按钮（新标签页打开）
- 回收站入口链接加入笔记列表页 header

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

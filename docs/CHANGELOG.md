# Personal Hub - 版本记录

## [Unreleased]

### 2026-07-17 全站页面优化批次 5–6
- 列表页去营销副标题；日记月历 overflow；搜索 EmptyState；分页边距；登录窄屏；删除 ProfileSettings；阅读类名 plan-page
- （原暂缓项已在「全站优化收尾」完成）

### 2026-07-17 全站列表 fill 批次 3–4
- **批次 3**：新增 `useProductViewMode`；学习计划 / 阅读记录接入；Product 样式抽公共组件暂缓（P2-1 注释标记）
- **批次 4**：笔记列表补 Table/Card 切换（默认 Card，`note-view` 持久化）；回收站接入 plan-page + fill + 矮屏降档 + 骨架屏 + 图标操作；`LIST_PAGE_SPEC` 文档化学习记录时间线例外

### 2026-07-17 文档同步
- 同步 PROJECT / STYLE_GUIDE / LIST_PAGE_SPEC / TECH_STACK / README / 前后端 README / CLAUDE 索引，反映一屏铺满、dueScope、设置标签云与全站优化收口

### 2026-07-17 全站优化收尾（原暂缓项）
- **P0-3 / P1-5**：学习计划、阅读、待办统一 `ListToolbar`；待办 Tab 改 lucide + 可横滚
- **P1-3 / P1-4**：Dashboard 窄屏可滚、Hero 压缩与资源入口简化
- **P1-7 / P1-8**：高级设置折叠；统计页吸顶筛选 + 分组折叠
- **P1-12 / P1-1·2**：分类标签 el-select + ListToolbar；列表路由隐藏面包屑
- **P2-1 / P2-7 / P2-8**：`product-list.css` 视图切换；命令面板进 `/search`；密度 Token
- **P2-4**：列表导出仅计划/阅读有接口，不做无 API 扩展

### 2026-07-17 全站列表 fill 批次 1–2
- 学习计划 / 阅读记录：手写 fill 改为 `useMainContentFill` + `useFillPageSize`（矮屏 10/8/6）
- 收藏夹 / 文件：接入 plan-page 一屏铺满、默认 size=10、pad 槽、分页 `total>0` 即显

### 2026-07-17 全站页面优化盘点
- 新增 `docs/qa/2026-07-17-全站页面优化盘点.md`：路由清单、LIST 对齐矩阵、P0–P2 优化项与落地批次

### 2026-07-17 设置·工作台二级 Tab
- 工作台菜单 / Dashboard / 统计统一为标签云形态，同页紧凑展示（无二级 Tab）
- 点击标签显隐、拖把手排序；菜单固定项（首页/设置）高亮不可隐藏

### 2026-07-17 一屏铺满
- Dashboard 外锁内滚；笔记 / 日记 / 学习记录 / 待办 fill + 矮屏降档 10/8/6
- 待办查询新增 `dueScope`（all|overdue|today|week|later|done），Tab 改服务端分页

### 2026-07-17 后端模块合并
- **重构**：Maven 模块 8→4：`ph-knowledge` / `ph-planning` / `ph-resource` / `ph-dashboard` 合并为 `ph-biz`；`ph-storage` 并入 `ph-common`
- **包名**：`com.personalhub.module.dashboard` → `com.personalhub.dashboard`（与 knowledge/planning/resource 对齐）
- **不变**：REST API、前端 `modules/` 目录

### 2026-07-17 Entity @Builder 改造
- **Entity**：统一 `@Data` + `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`；初值字段加 `@Builder.Default`
- **create**：Service 新建改为 `Xxx.builder()…build()`；update / VO 不变

### 2026-07-17 后端基础数据优化
- **枚举**：ReadingStatus / DiaryMood / StudyPlanStatus / TodoPriority，VO label 统一 `labelOf`
- **EntityGuard**：归属校验收口到 ph-common
- **默认值**：ReadingRecord / StudyPlan / Category 字段初值，create 去掉散落 `?: 0`

### 2026-07-16 阅读记录 Product 列表改造
- **列表**：Product Table/Card 切换、一屏铺满、PAGE_SIZE=10；卡片左侧小封面
- **工具栏**：搜索 + 状态 + 排序 + 视图 + 导出 ▾ + 添加书籍（无 Header 统计徽章）
- **导出**：`GET /api/readings/export?scope=filtered|all`，XLSX 内存流不落盘
- **排序**：`sortBy`/`sortDir`（默认 updatedAt desc）

### 2026-07-16 学习计划 XLSX 导出
- **入口**：工具栏「导出 ▾」（新建左侧）：导出当前 / 导出全部
- **接口**：`GET /api/study-plans/export?scope=filtered|all`，Apache POI 内存生成 `.xlsx`，直接下载不落盘

### 2026-07-16 外观设置生效 + 内容区宽度
- **修复**：圆角/动画/密度写入真实 CSS Token（`--radius-*` / `--transition` / `--sp-*`），刷新后仍生效
- **内容区宽度**：滑条 50%–100%，默认 80%；拖动即时预览

### 2026-07-16 docs 精简压缩
- 压缩 STYLE_GUIDE / API / DATABASE / DEPLOYMENT / PROJECT / PREVIEW / LIST / AUDIT / TECH_STACK / qa 报告（不含 superpowers）；章节骨架与笔记结构保留

### 2026-07-16 Product 列表页开发规范
- **新增** `docs/LIST_PAGE_SPEC.md`：Product Table/Card、一屏铺满、分页槽位、软标签与操作菜单约定
- **同步** `STYLE_GUIDE.md` Checklist / CLAUDE.md 文档索引

### 2026-07-16 学习计划卡片布局调整
- **卡片层级**：标题+状态 → 软色标签（最多 2 +`+N`）→ 来源·作者 → 进度 → 点分日期 → 备注 → 底栏更新时间 + 操作
- **表格视图**：不变

### 2026-07-16 学习计划 Product UI 重构
- **列表**：Product Table（行高留白、弱边框）；Table / Card 切换
- **列**：名称（标题+作者·来源）/ 分类 Tag / 圆点状态 / Progress / 学习周期 / 来源图标 / ··· 菜单
- **工具栏**：搜索 + 分类 + 状态 + 排序（默认最近更新）+ 视图切换 + 新建
- **统计**：Header 学习中/已完成/暂停/未开始；`GET /api/study-plans/stats`
- **模型**：恢复 `progress`；状态文案：未开始/学习中/已完成/已暂停

### 2026-07-16 学习计划表格化完全重构
- **列表**：轻边框 `el-table`（分类/名称/来源/作者/起止/状态/地址/备注）
- **模型**：新增 `source`/`author`/`url`/`remark`；去掉 `goal`/`progress`；状态：待学习/进行中/已完成/已暂停
- **分类**：`tag_rel` + `entity_type=study_plan`；工具栏标签筛选
- **连带**：首页今日计划去进度条；全局搜索改名称+来源+作者+备注
- **迁移**：`sql/migration-20260716-study-plan-table.sql`

### 2026-07-16 通知清空后不再重复展示
- **根因**：清空物理删除后 `/notifications/check` 因「无未读」再生成超期/计划通知
- **修复**：`is_dismissed` 软清除；列表/未读排除已清空；同 type+related_id 含已读/已清空不再重复生成

### 2026-07-16 首页六宫格 + 外部快捷
- **布局**：2×3 等宽等高；默认今日/快捷/外部快捷/笔记/学习/阅读
- **外部快捷**：收藏夹 `show_on_dashboard`；`GET /api/bookmarks/dashboard`
- **资源盘点**：独立卡移除，四指标并入 Hero 可点链接
- **设置**：Dashboard 布局同步；本地 key `layout-dashboard-v5`

### 2026-07-16 首页资源盘点小统计
- **移除**：独立「待办任务」卡（`pending_todos`）
- **新增**：`resource_snapshot`（本月日记/收藏/文件/在读中），与 `/stats` KPI 错开
- **兼容**：`REMOVED_DASHBOARD_CODES`；本地 key `layout-dashboard-v4`

### 2026-07-16 工作台 Bento 信息中心
- **布局**：12 列 Bento；Hero 问候 + 超期入口
- **配置**：`visibleDashboardCards` 控显隐/排序；默认 today/quick/notes/studies/reading
- **组件**：`dashboard/widgets/*`（Today/Quick/Notes/Studies/Reading/Todos）
- **去重**：移除与 `/stats` 重复 KPI/趋势/最近活动
- **兼容**：`ensureDashboardCards` + `REMOVED_DASHBOARD_CODES`；本地 key `layout-dashboard-v3`

### 2026-07-15 Cursor 浏览器巡检方法文档化
- **规则**：`.cursor/rules/browser-qa-testing.mdc`
- **文档**：`docs/qa/CURSOR_BROWSER_TESTING.md`

### 2026-07-15 深链跳转修复（工作台 / 通知 → 列表弹窗）
- **根因**：List+Dialog 模块跳转未注册 `/:id/edit`、`/new` → 空白页
- **方案**：`?edit=` / `?create=1` + `useDeepLinkDialog`；router redirect 兼容旧路径
- **修复**：Dashboard 列表项与「开始学习」、notificationStore、笔记编辑骨架
- **附带**：`bookTitle` 显示；学习计划 `Plus` 图标；vitest + deepLink 单测

### 2026-07-15 WYSIWYG 编辑器重构（md-editor-v3 → Vditor IR）
- **替换**：Vditor IR；39 项右键菜单（格式/标题/列表/表格/代码/Mermaid/公式/图片等）
- **新组件**：NoteVditor / NoteMarkdownPreview / vditorSetup / context-menu 系列
- **模式**：edit / preview / focus（Ctrl+Shift+P/F、Esc）；去除分栏预览
- **列表/回收站**：卡片右键菜单统一
- **删除**：NoteMdEditor、EditorPreviewPanel、mdEditorToolbars、mdEditorSetup
- **依赖**：+vditor^3.11，-md-editor-v3

### 2026-07-15 专业 Markdown 编辑器
- 完整工具栏（撤销/标题/列表/任务/代码/Mermaid/KaTeX/Prettier/目录/全屏）
- `NoteMdEditor` + `EditorPreviewPanel`；Mermaid/KaTeX 本地扩展
- 预览：TOC、代码复制、medium-zoom、图片鉴权代理；分栏默认 55:45（`Ctrl+Shift+L`）
- 修复 `heading` → `title`；+mermaid ^11、katex ^0.17

### 2026-07-15 Logback 按天归档
- `logback-spring.xml`：`maxHistory=30`；`personal-hub.log` + `personal-hub-error.log`
- `LOG_PATH`；Compose/Dockerfile 挂载 `/data/personal-hub/logs`

### 2026-07-14 正确性 / 性能 / 安全优化
- 回收站清理仅删孤立目录；`ProdSecretsValidator` 弱密钥拒绝启动
- JWT query 白名单 `/api/notes/{id}/images|attachments/**`
- 列表 N+1 优化；`note_note.excerpt` + 迁移 `sql/migration-20260714-note-excerpt-index.sql`
- Redis 缓存/限流；Actuator health + Docker HEALTHCHECK；Vite `manualChunks`

### 2026-07-14 Docker Compose + GitHub Actions 部署
- `docs/DEPLOYMENT.md`；`deploy/` Compose 骨架；`.github/workflows/deploy.yml`（self-hosted）
- `application-prod.yml` 支持 `MYSQL_*` / `REDIS_*` / `JWT_SECRET`

### 2026-07-14 沉浸式创作模式（Focus Mode）
- 全屏/分栏预览/阅读模式/Focus Mode；Splitpanes 比例持久化；滚动同步
- 状态栏增强；`/notes/:id/preview` 独立路由；+splitpanes ^4.1.2

### 2026-07-14 个人资料模块 + 待办重构 + 外观扩展
- Profile API + `sys_user` 10 字段扩展；头像 `/api/files/avatar/*`（5MB）
- ProfileDrawer 四卡片；待办 Tab 分组 + `completed_at`；强调色 5→9 种
- Topbar 64→48px

### 2026-07-13 统计页面 V4 全面重写
- `GET /api/dashboard/detail`（StatsVO）；8 统计模块 + 12 SQL 查询
- StatsView 重写（5 图表类型）；StatsManager 配置化；时间范围 3/7/15/30/90 天
- layoutStore `statsCards`；API shim 清理；`useEntityDialog` / `useStorageSync`；大组件拆分

### 2026-07-12 系统设置 V3 精简版 — 布局重构
- Tab 6→4（工作台/阅读/外观/高级）；1080px 居中；自动保存指示条；+AdvancedSettings

### 2026-07-12 系统设置 V2 Phase 2
- 通知/数据管理/实验功能模块；`notificationConfigStore` / `featureFlagStore`
- ExportController / BackupController；外观圆角/动画/密度 CSS 变量

### 2026-07-12 系统设置 V2 Phase 1
- SettingsView 重写（6 Tab）；外观从 AppLayout 迁入；`appearanceConfig` 后端持久化

### 2026-07-12 标签管理 & 分类管理页面全面优化
- 分类/标签统计卡 + Grid 布局 + 拖拽排序；`PUT /api/categories/sort`
- UiDialog 三段布局重写；Diary/Todo/Bookmark/Reading/Study/StudyPlan 对话框统一

### 2026-07-12 阅读配置迁移至系统设置 + 图片比例可调
- `readingConfigStore`；字号/宽度/行高/主题/图片比例 `--image-max-width`
- JWT `?token=` 认证；`user_layout layout_type='preview'`

### 2026-07-12 Markdown 导入功能（文件 + 粘贴）
- `ph-knowledge/imports/` 组件包；网络/Base64 图片下载；`ImportMarkdownDialog`

### 2026-07-12 预览页阅读体验全面重构
- DocLayout + 阅读设置面板；`markdown-prose.css`；4 主题；medium-zoom；`docs/PREVIEW_PAGE_SPEC.md`

### 2026-07-12 笔记回收站改造 + 统一审计日志
- `deleted_at` / `delete_reason`；`audit_log` 表；`GET /api/notes/recycle` / `preview`
- RecycleBin + Preview.vue 重写

### 2026-07-12 代码清理与配置优化
- application 分层 + springdoc；Swagger dev only；删除废弃文件/死类型/冗余路由

### 2026-07-11 第八阶段：CRUD UI 优化 + 分类系统合并
- Ui 组件库；Dialog/Drawer CRUD；统一 `category` 表；废弃 `note_tag`

### 2026-07-11 第七阶段：工作台自定义布局
- `user_layout` + layoutStore；侧栏/Dashboard 拖拽排序；系统设置页

### 2026-07-11 第六阶段：体验打磨 + 后端优化 + 通知系统
- Ctrl+K 搜索；学习统计 SQL 优化；笔记 MD 导出；NotificationBell

### 2026-07-11 第五阶段：功能增强
- 笔记标签/阅读时间；待办拖拽；学习/日记/计划/阅读字段增强

### 2026-07-11 第四阶段：UI 优化
- Accent Color / Command Palette / md-editor-v3 / Dashboard / 深色模式 / 空状态 SVG

### 2026-07-11 第三阶段
- 统一标签（ph-tag）；Dashboard 统计 + ECharts + 全局搜索

### 2026-07-10 第二阶段
- Todo、文件、日记、收藏夹、学习计划、阅读记录

### 2026-07-09 第一阶段 MVP
- 认证、Markdown 笔记、学习记录；Maven 多模块；docs 体系（8 文档）

---

## [0.1.0] — 2026-07-09
- 初始化仓库与规划文档；docs 体系：PROJECT / TECH_STACK / ROADMAP / DATABASE / API / STYLE_GUIDE / CHANGELOG

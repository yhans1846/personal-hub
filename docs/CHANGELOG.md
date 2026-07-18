# Personal Hub - 版本记录

> 职责：按日记录已落地变更。架构/规范正文不写在此。

## [Unreleased]

### 2026-07-19
- **设置**：四 Tab 间距统一（`settings-layout.css`：卡片间距 12 / 标题下 12 / 区块 20 / 小标题 10 / 行间 12）
- **外观**：系统主题新增「护眼」(`sepia`)；色板去掉纯白，顶栏/卡片/侧栏统一豆沙绿
- **修复**：深色下 `el-collapse` 白条；高级设置对齐阅读页（标题进 UiCard）；统一高级区行间距（区块 20 / 标题下 10 / 行间 12）
- **移除**：设置页「数据导出」占位（UI + `/api/export`）；列表 XLSX / 笔记 ZIP 导出保留
- **日志**：JWT 过滤器认证通过/无效改为 `trace`，避免通知轮询刷屏
- **UI**：全局隐藏页面/卡片滚动条（仍可滚轮与触控滚动）
- **外观 Token**：全站硬编码圆角/过渡改为 `--radius-*` / `--transition`；主题/强调色/密度/动画经 `applyAppearanceToDOM` + EP Token 桥接；深色下 Select/按钮/分页反色
- **通知音效**：内置 `src/assets/sounds/*.wav`；设置切换即时预览；未读增加时按偏好播放
- **演示数据**：`scripts/seed_demo_data.py`（admin 下全模块样例 + `D:/PersonalHub/uploads` 真实文件）

### 2026-07-18
- **日记配图资源包**：`diaries/{id}/images/` + `image_files`；新建先保存再上传；不进 `file_resource`；不做历史迁移
- **文件模块**：内嵌预览（图/PDF/文本）；上传/卡片可改分类；`type` 分组筛选；列表边界=本页上传
- **Dialog 编辑器气质**：`DialogTitleField` 等积木；业务弹窗（笔记除外）对齐；`UiDialog` size；日期 `ph-date-popper`
- **清理未用字段**：去 `bookmark_url.tags`/`favicon`、`file_resource.source`/`ref_id`/`stored_name`、`study_record.plan_id`
- **其它**：登录写 `audit_log(AUTH/LOGIN)`；日记 GPS 坐标；配图拖拽排序
- **文档**：`PAGE_SPEC`/`STYLE_GUIDE`/`PROJECT`/`README`/`DEPLOYMENT` 同步
- **库表**：`init.sql` 与 live 对齐；统一 `utf8mb4_general_ci`（仅库/表级）；清理已并入 init 的 alter/migration 脚本

### 2026-07-17
- **修复（QA）**：布局 `save` 幂等 upsert（含软删恢复 + DuplicateKey 回退）；上传异常（Multipart / 错误 Content-Type）→ 400「请上传文件」；收藏夹接入 `useDeepLinkDialog`
- **工作台**：快捷操作补齐知识区 6 项（笔记/日记/阅读/学习记录/待办/学习计划）
- **日记列表**：列表缩略图；卡片上封面（无图不占位）+ 下信息；配图支持点击预览
- **QA**：全功能/全按钮巡检报告与脚本；发现布局保存冲突、头像非 multipart、收藏深链未接等问题
- **后端规约**：Flags；toggleFavorite NPE；流关闭；PageParam `@Max`；BusinessException 统一；验证码不下发 emptyIndex；分类 SQL 迁 Mapper；踩坑点入 `STYLE_GUIDE`
- **治理（V2）**：删死代码/空 SQL；`formatTime`+`CurrentUser`+`EnumLabels`+CSS DRY；`category` 迁 `knowledge`；文档归档说明
- **文档**：八份主文档减重；`LIST/PREVIEW_PAGE_SPEC` → `PAGE_SPEC`；`DATABASE`/`init.sql` 对齐
- **登录 / 资料**：分屏 + 书架验证码三步流；ProfileDrawer Hero + Tab；面包屑统一
- **列表 / 后端**：fill 10/8/6 · ListToolbar · dueScope · 工作台标签云；Maven 8→4（`ph-biz`）；Entity Builder；`labelOf` + `EntityGuard`

### 2026-07-16
- **学习计划**：Product Table/Card；`source`/`author`/`url`/`remark`/`progress`；`tag_rel` 分类；stats + XLSX 导出；卡片层级与工具栏定型；迁移 `migration-20260716-study-plan-table`
- **阅读记录**：Product Table/Card + 小封面；排序/筛选；XLSX 导出（filtered|all）
- **列表规范**：`LIST_PAGE_SPEC`（后并于 `PAGE_SPEC`）；一屏铺满约定
- **工作台**：Bento 信息中心 → 六宫格 + 外部快捷（`show_on_dashboard`）；资源盘点并入 Hero；layout key 演进
- **通知**：`is_dismissed` 软清除，避免清空后重复生成
- **外观**：圆角/动画/密度 Token 生效；内容区宽度 50%–100%（默认 80%）
- **文档**：主文档精简压缩（不含 superpowers）

### 2026-07-15
- **笔记编辑器**：md-editor-v3 → Vditor IR；右键菜单/Focus/预览模式；列表与回收站卡片菜单统一
- **深链**：`?edit=`/`?create=1` + `useDeepLinkDialog`；工作台/通知跳转修空白页
- **运维**：Logback 按天归档 30 天；Compose 挂载 logs
- **QA**：Cursor 浏览器巡检规则与 `CURSOR_BROWSER_TESTING.md`

### 2026-07-14
- **部署**：Docker Compose + GitHub Actions self-hosted；`DEPLOYMENT.md`；prod 环境变量
- **资料 / 待办 / 外观**：Profile API + 用户字段扩展；头像上传；待办 Tab + `completed_at`；强调色扩展；Topbar 变矮
- **笔记体验**：Focus Mode / 分栏预览 / 独立 preview 路由
- **优化**：回收站清孤立目录；Prod 弱密钥校验；JWT query 白名单；列表 N+1；`excerpt`+索引；Redis；Actuator；Vite chunks

### 2026-07-13
- **统计 V4**：`GET /dashboard/detail`；StatsView 重写；时间范围可配；layoutStore `statsCards`
- **工程**：`useEntityDialog` / `useStorageSync`；大组件拆分；API shim 清理

### 2026-07-12
- **笔记**：回收站（`deleted_at`/`delete_reason`）+ `audit_log`；预览 DocLayout + 阅读主题；MD 导入（文件/粘贴）
- **阅读配置**：迁系统设置；字号/宽度/行高/主题/图片比例；`user_layout(preview)`；JWT `?token=`
- **设置**：SettingsView 多版迭代至 Tab 精简（工作台/阅读/外观/高级）；外观/通知/备份导出占位
- **分类标签**：管理页 Grid + 拖拽排序；`PUT /categories/sort`；业务 Dialog 统一
- **工程**：application 分层；Swagger 仅 dev；清理废弃代码

### 2026-07-11
- **阶段四–八**：Accent/深色/空状态/Command Palette；统一标签 + Dashboard/ECharts/搜索；通知铃；`user_layout` 可配侧栏/首页；Ui 组件库 + Dialog CRUD；统一 `category` 表
- **增强**：笔记标签/阅读时间；待办拖拽；学习/日记/计划/阅读字段增强；笔记 MD 导出；Ctrl+K

### 2026-07-10
- **第二阶段**：Todo、文件、日记、收藏夹、学习计划、阅读记录

### 2026-07-09
- **第一阶段 MVP**：认证、Markdown 笔记、学习记录；Maven 多模块；docs 体系初建

---

## [0.1.0] — 2026-07-09
- 初始化仓库与规划文档；docs 体系：PROJECT / TECH_STACK / ROADMAP / DATABASE / API / STYLE_GUIDE / CHANGELOG

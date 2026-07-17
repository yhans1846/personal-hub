# Personal Hub - 编码规范

## 通用原则
可读性优先 | 结构统一 | 中文注释 | 不写无用代码 | 不用过时 API

---

## 后端规范（Java / Spring Boot）

**强制遵守阿里巴巴开发手册：**

| 类别 | 要求 |
|------|------|
| 类/字段注释 | `/** 说明 */` 上方，禁止行尾 `//` |
| public 方法 | Javadoc 含 `@param`/`@return`/`@throws` |
| 常量 | `UPPER_SNAKE` + `final`，禁魔法值 |
| @Override | 不重复 Javadoc |
| @Bean | 需说明用途 |

### Swagger 注解规范
- Controller：`@Tag` + `@Operation`；Authentication 用 `@Parameter(hidden=true)`
- DTO/VO：`@Schema`（DTO 含 example）；Result/PageResult 已有，勿重复

### 公共组件编码思想

#### 1. 分页参数复用 `PageParam`
查询 DTO **必须继承** `PageParam`，禁止自建 `page`/`size`。

#### 2. 统一返回状态码枚举 `ResultCode`
`code` 必须用枚举（`Result.badRequest` / `Result.error(ResultCode.…)`），禁止硬编码数字。

#### 3. 关键路径必须记录日志
- Service 增删改 `log.info`；权限失败 `log.warn`
- 业务异常 `log.warn`；未知异常 `log.error`（含堆栈）
- 过滤器认证 `log.debug`
- 落盘：`ph-boot/.../logback-spring.xml` 按天滚动 30 天；`LOG_PATH` / `logging.file.path`

#### 4. 状态码文案用模块枚举
有固定 int 码表的字段（阅读状态、日记心情、计划状态、待办优先级）使用模块 `enums` 包枚举，VO 标签用 `XxxEnum.labelOf(code)`，禁止在 VO/Service 内嵌业务 switch。

#### 5. 归属校验用 EntityGuard
`selectById` 后校验「存在且属于当前用户」统一调用 `EntityGuard.requireOwned(entity, userId, Entity::getUserId, "xxx不存在")`，禁止手写 null + userId 判断（笔记回收站等额外条件可在 Guard 之后追加）。

#### 6. Entity 用 Lombok 四件套 + create 用 Builder
Entity：`@Data` + `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`（保证 MyBatis 无参）。有字段初值时加 `@Builder.Default`。  
Service **create** 优先 `Xxx.builder()…build()`；「DTO 为 null 则保留默认」的字段不要放进 builder 链，build 后再条件 `set`。  
**update** 继续用 setter。VO **不强制** `@Builder`，可用静态 `from`。

### 命名
| 类型 | 规则 | 示例 |
|------|------|------|
| 包名 | 小写单数 | `com.personalhub.controller` |
| 类名 | PascalCase | `NoteController` |
| 方法/变量 | camelCase | `getNoteList` |
| 常量 | UPPER_SNAKE | `MAX_PAGE_SIZE` |
| 表/字段 | snake_case | `note_note` / `created_at` |
| DTO/VO/Entity | XxxDTO / XxxVO / Xxx | |

### 分层职责
Controller：校验+返回（构造器注入）｜Service：业务｜Mapper：`BaseMapper`｜DTO 入参 / VO 出参 / Entity 不外露

### SQL 编写规范
- 简单 CRUD → MyBatis-Plus
- **复杂 SQL**（联表/聚合/动态条件）→ `mapper.xml`（`resources/mapper/`），禁止 Service 拼 SQL

### 时间 & Lombok
`LocalDateTime`｜Entity 四件套见 §6｜DTO/VO `@Data`｜`@RequiredArgsConstructor`(Controller/Service)

### Maven 多模块结构

#### 当前结构（4 模块）

```
personal-hub-server/
├── pom.xml
├── ph-common    # 含 storage SPI（com.personalhub.storage）
├── ph-system
├── ph-biz       # knowledge / planning / resource / dashboard
└── ph-boot      # resources: application.yml + -dev / -prod
```

模块内：`controller/ dto/ entity/ mapper/ service/ vo/`。Maven 不按每个业务拆 jar；细领域用 package。

---

## 前端规范（Vue 3 / TypeScript）

### 命名
| 类型 | 规则 | 示例 |
|------|------|------|
| 组件/页面 | PascalCase.vue | `NoteEditor.vue` |
| 组合函数 | useXxx | `useNoteList` |
| Store | useXxxStore | `useAuthStore` |
| TS / class | camelCase / kebab | `noteApi.ts` / `.note-card__title` |
| 类型 | 大写或 I 前缀 | `NoteVO` |

### 目录结构

```
personal-hub-web/src/
├── api/  components/(ui/)  composables/  layouts/
├── modules/
│   ├── category/  dashboard/  search/  stats/  system/
│   ├── knowledge/   # note(列表/Editor/RecycleBin) · diary · reading · study · tag
│   ├── planning/    # studyplan(Product Table/Card) · todo
│   └── resource/    # bookmark · file
├── router/  stores/  styles/  types/  utils/
```

### 组件 / API / Store
- 一律 `<script setup lang="ts">`
- API：`src/api/` 对外 + `modules/*/api.ts` 按领域聚合；禁止一表一文件
- Pinia：仅全局（auth/theme/layout/readingConfig/notification）；业务列表页内拉 API

### 依赖引入原则
评估体积与必要性；禁重复能力依赖（双图标库等）。编辑器：vditor。

---

## UI/UX 设计原则

个人知识库（Notion / Linear / Raycast / Apple），非 Admin 后台。

### 核心调性
简洁专注 · 留白舒适 · 内容优先 · **禁止**灰底密表/深色大菜单企业风

### CRUD 交互模式

整个系统统一 **Dialog**（Drawer 已弃用）。

#### ① Dialog（唯一编辑方式）
Todo/标签/分类/收藏/日记/阅读/学习/计划等：不跳页 → Dialog → 保存 → 关 → 刷新。  
**禁止** 列表 → 独立编辑页 → 返回（笔记 Markdown 编辑除外）。

#### ② Full Page
仅专注场景：Markdown 编辑器、大型配置。

### Dialog 设计规范

#### 尺寸
宽默认 720（700–860）；高 ≤85vh；Header/Footer 固定，Body 滚动

#### 圆角
16px（`--radius-xl`）

#### Padding
Header/Body 左右 32；Header 上下 32、Body 24、Footer 16

#### 三段布局
Header（标题+副标题+关闭）→ Body（内容优先）→ Footer（取消 Text按钮 + 保存 Primary，右对齐）

#### Header / Footer
标题 20px/600；副标题 14px 灰。Footer 不超过两按钮。

### 表单设计规范

创作界面，非传统表单。

#### 原则
消 Label（Placeholder 即说明）· 正文约 70% 视觉权重 · 元数据用 Chip/卡片 · 禁 el-form / Radio（元数据点选除外）

#### 常见元数据控件
心情/优先级：卡片 emoji｜天气：图标点选｜日期/分类/标签：Chip｜图：DropZone

#### 输入框
标题无边框 18/600｜正文无边框 minH 300｜元数据 14/400

### 页面布局规范
Page → Card → Section → Field（非 FormItem 堆叠）

### 整体布局
Topbar 56px｜Sidebar 240px｜Main max-width 1600、padding 32

### 侧边栏规范
工作区 / 管理 / 统计。一级 14/500/40px 圆角12，激活实心 accent；二级 13 灰缩进24；分组标题 11 大写灰

### 颜色规范（`styles/global.css`）

| Token | 浅色 | 深色 |
|-------|------|------|
| `--accent` | `#4F7BFF` | `#60A5FA` |
| `--accent-hover` | `#5B86FF` | `#93C5FD` |
| `--bg-body` | `#F8FAFC` | `#0B1120` |
| `--bg-card` | `#FFFFFF` | `#1E293B` |
| `--border-color` | `#EAEAEA` | `#334155` |
| `--text-primary` | `#0F172A` | `#F1F5F9` |
| `--text-secondary` | `#64748B` | `#94A3B8` |
| `--success` / `--warning` / `--danger` | `#22C55E` / `#F59E0B` / `#EF4444` | `#34D399` / `#FBBF24` / `#F87171` |

强调色切换（`data-accent`）：蓝/紫/青/橙/绿（设置页可扩至 9 色）。

### 字体与字号
页面标题 24/600｜卡片标题 16/600｜正文 14｜辅助 13｜标签 12/500  
字体：系统栈；等宽 SF Mono / Fira Code

### 间距体系（8px）
`4–48` → `--sp-1`…`--sp-12`

### 卡片设计
圆角 12/8｜边框 `var(--border-color)`｜阴影 `--shadow-md`｜Hover `translateY(-2px)` + `--shadow-lg`

### 按钮规范
主 Filled accent｜次 Outlined｜危险红｜禁 Element 默认蓝

### CRUD 页面映射规范

| 功能 | UI 模式 |
|------|---------|
| 多数实体 | Dialog |
| Study Plan | Dialog + Product 列表 |
| Markdown 编辑器 | Full Page |
| 日记 | Dialog |

禁独立 Create/Edit 页；禁 el-drawer。新增/编辑共用一 Dialog。

### 组件封装要求
布局用 `UiDialog/UiInput/UiTextarea/UiSelect/UiDatePicker/UiButton/UiSection/UiCard`，业务页不直接堆 Element 布局组件。

### 共享组件（`src/components/`）
| 组件 | 用途 | 关键 Props |
|------|------|-----------|
| `EmptyState` | 空状态 | icon, text, actionLabel?, illustration? |
| `StatCard` | 统计卡 | icon, value, label, color? |
| `PageHeader` | 页头 | title, subtitle?, 插槽 |
| `ListToolbar` | 列表工具栏 | search + `#filters` + `#actions` + createLabel? |
| `ListPagination` | 分页 | total, page, size（fill 页顶边距归零） |
| `CommandPalette` | Ctrl+K | 导航 + Enter 跳转 `/search?q=` |
| `NotificationBell` | 通知 | badge + popover |
| `SearchBar` | 搜索框 | placeholder, modelValue, debounce? |
| `ConfirmDialog` | 确认 | title, content |
| `MarkdownEditor` | MD | modelValue, previewOnly? |
| `NoteVditor` | 笔记 WYSIWYG（Vditor IR） | modelValue, editorId, theme, readonly?, onUploadImg? |
| `NoteMarkdownPreview` | 笔记只读预览 | content, editorId, noteId?, showToc?, showMeta? |
| `Tag` / `PriorityTag` / `ProgressCard` / `FileUploader` | 展示/上传 | 见组件 |
| `DocLayout` | 文档壳 Header+TOC+正文 | title, tocItems?, meta?, isTrash? + `#header-actions` |

### Dashboard Bento（`src/modules/dashboard/`）
- Hero → 12 列 Bento；`visibleDashboardCards` 显隐排序；`useMainContentFill` + DashCard 体内滚
- 默认：today_plan / quick_actions / external_links / recent_notes / recent_studies / recent_reading（span 4）
- 趋势/KPI/活动仅 `/stats`；已移除 weekly_trend / recent_activity / kpi_strip / pending_todos / resource_snapshot
- 外部快捷：收藏 `show_on_dashboard`；`DASHBOARD_CARD_SPAN` + `ensureDashboardCards` + `REMOVED_DASHBOARD_CODES`
- 窄屏（≤900）：整页可滚、Hero 压缩；资源 chips 收成链到 `/stats`
- 组件：`widgets/*` + `DashCard` + `format.ts`
- 设置「工作台」：菜单 / Dashboard / 统计统一**标签云**（点击显隐、拖把手排序）

#### UI 基础组件（`src/components/ui/`）
| 组件 | 用途 | 关键 |
|------|------|------|
| `UiDialog` | 三段弹窗 | width 默认 720，bodyMaxHeight 65vh |
| `UiInput` / `UiTextarea` / `UiSelect` / `UiDatePicker` / `UiButton` | 表单控件 | 透传 Element |
| `UiSection` / `UiCard` | 区块/卡片 | title / padding? |

### 工具函数（`src/utils/`）
| 文件 | 用途 |
|------|------|
| `readingTime.ts` | 阅读时长 / 相对时间 / 近 24h 编辑 |
| `deepLink.ts` | `buildEditPath` / `buildCreatePath` / `parseDeepLinkQuery` / 通知跳转 |

### Composable（`src/composables/`）
| 文件 | 用途 |
|------|------|
| `useDeepLinkDialog.ts` | 消费 `?edit=` / `?create=1`，开 Dialog 并清 URL |
| `useMainContentFill.ts` | 挂载/卸载 `.main-content--fill` |
| `useFillPageSize.ts` | 按内容区高度得到 10/8/6，resize 防抖后回调刷新列表 |
| `resolveFillPageSize.ts` | 高度 → pageSize 纯函数（单测） |
| `useProductViewMode.ts` | Table/Card 视图 + `localStorage` |

### 样式
| 文件 | 用途 |
|------|------|
| `styles/global.css` | 设计令牌 / 主题 / fill 布局 |
| `styles/product-list.css` | Product 视图切换（`.view-toggle`）公共样式 |
| `styles/markdown-prose.css` | Markdown 排版 |

### 路由约定
列表主路由可设 `meta.hideBreadcrumb: true`，`AppLayout` 隐藏面包屑，避免与 `PageHeader` 双标题。

### Editor Composable（`src/modules/knowledge/note/editor/`）
| 文件 | 用途 |
|------|------|
| `useAutoSave.ts` | 2s 防抖自动保存；Ctrl+S；路由离开守卫 |
| `useEditorMode.ts` | edit/preview/focus + 全屏；Ctrl+Shift+P/F，Esc |
| `useEditorPreferences.ts` | fullscreen/focusMode → localStorage |
| `useImageUpload.ts` | 校验 + noteId + 逐个上传 |
| `vditorSetup.ts` | Vditor IR、隐藏工具栏、CDN、主题 |
| `contextMenuActions.ts` | 39 项右键格式操作 |
| `parseToc.ts` | Markdown TOC |

### 新页面模板（CRUD 列表页）
`PageHeader` → 工具栏 → skeleton → `EmptyState` → Product 区 → `ListPagination`  
Dialog 编辑（笔记编辑除外）· 深链 `?edit=`/`?create=1`  
→ **`docs/LIST_PAGE_SPEC.md`**（参考 `studyplan/List.vue`）

### 图标规范
仅 lucide-vue-next / `@lucide/vue`｜导航 18 · 操作 14 · 空状态 48

### 交互规范
pointer + 200ms｜Focus accent inset｜卡片 Hover 上浮｜操作按钮 hover 显

### 空状态规范
禁纯文字「暂无数据」→ `EmptyState` + 插画 + CTA

### 动画标准
路由 fade 200ms｜收藏 fav-pop 350ms｜骨架 pulse 1.5s｜侧栏 250ms

### 响应式断点
| 断点 | 行为 |
|------|------|
| <768 | 侧栏 overlay；Dashboard 单列 |
| <1024 | Topbar 文字隐；统计 2 列 |

### Element Plus 使用规范
`global.css` 覆主题｜禁企业风 `el-table`（用 Product Table）｜表单走 Ui*｜分页 accent

### 深色模式
`data-theme="dark"`｜初随系统｜手动 → `localStorage` `theme-preference`

### Checklist（新增页面时对照）
1. PageHeader + EmptyState  
2. 列表遵循 `LIST_PAGE_SPEC.md`  
3. hover + 图标/`⋯` 操作  
4. 深色验证  
5. 768 响应式  
6. 骨架屏  
7. header→toolbar→list→pagination；筛选重置页码  
8. Dialog + `useDeepLinkDialog`  
9. 新建/编辑共用 Dialog  
10. 列表页用 `useMainContentFill` + `useFillPageSize`（禁手写 DOM class）  
11. 工具栏优先 `ListToolbar`；视图切换用 `useProductViewMode`  

### 相关文件
- Token：`styles/global.css`｜视图切换：`product-list.css`｜散文：`markdown-prose.css`  
- 列表：`LIST_PAGE_SPEC.md`｜预览：`PREVIEW_PAGE_SPEC.md`｜全站盘点：`qa/2026-07-17-全站页面优化盘点.md`  
- 组件：`src/components/`｜主题：`main.ts`

---

## Git Commit

```
<类型>: <简要描述>
```
类型: feat | fix | docs | style | refactor | perf | test | chore  
例：`feat: 新增笔记分类管理接口`

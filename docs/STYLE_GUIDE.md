# Personal Hub - 编码与设计系统

> 职责：编码约定、UI Token、共享组件目录。  
> 页面骨架/列表/预览契约 → `PAGE_SPEC.md`。模块结构 → `PROJECT.md`。

## 通用原则

可读优先 · 结构统一 · 中文注释 · 不写无用代码 · 不用过时 API

---

## 后端（Java / Spring Boot）

遵守阿里巴巴手册：类字段上方 Javadoc · public 含 `@param`/`@return` · 常量 `UPPER_SNAKE` · 禁魔法值。

| 项 | 约定 |
|----|------|
| Swagger | Controller `@Tag`+`@Operation`；DTO/VO `@Schema`；Auth 参数 `hidden` |
| 分页 | 查询 DTO 继承 `PageParam` |
| 返回码 | 用 `ResultCode` 枚举，禁硬编码数字 |
| 日志 | 增删改 info；权限/业务 warn；未知 error+堆栈；过滤器 debug |
| 状态文案 | 模块 `enums` + `labelOf`，禁 VO/Service 内嵌业务 switch |
| 归属 | `EntityGuard.requireOwned(...)` |
| Entity | `@Data`+`@Builder`+无参/全参；初值 `@Builder.Default`；create 用 builder，update 用 setter |
| 分层 | Controller 校验返回 · Service 业务 · Mapper · DTO/VO/Entity 不外露 |
| SQL | 简单 MP；联表/聚合进 `mapper.xml`，禁 Service 拼 SQL |
| 时间 | `LocalDateTime` |

命名：包小写 · 类 Pascal · 方法 camel · 表字段 snake · `XxxDTO/VO`。

---

## 前端（Vue 3 / TS）

| 类型 | 规则 |
|------|------|
| 组件页 | `PascalCase.vue` |
| composable / store | `useXxx` / `useXxxStore` |
| API 文件 | `camelCase.ts` |
| class | kebab |

- 一律 `<script setup lang="ts">`
- API：`src/api/` + `modules/*/api.ts`；禁一表一文件
- Pinia 仅全局（auth/theme/layout/readingConfig/notification）；列表页内拉 API
- 图标仅 lucide；编辑器 vditor

目录：`modules/{knowledge,planning,resource,system,dashboard,search,stats}`（`knowledge/category` 归知识域）· `components/(ui/)` · `composables/` · `styles/`

---

## UI / UX

气质：Notion / Linear / Raycast / Apple。禁灰底密表、深色大菜单企业风。

**编辑：** 默认 **Dialog**（笔记 Full Page 除外）；禁独立 Create/Edit 页；禁 `el-drawer`（资料抽屉为已批准例外）。

### Dialog

宽默认 720（700–860）· 高 ≤85vh · 圆角 `--radius-xl` · Header/Footer 固定 Body 滚 · Footer ≤2 按钮（取消+保存 Primary）

### 表单

消 Label（Placeholder 说明）· 正文权重大 · 元数据 Chip/卡片 · 禁堆 `el-form`；布局用 `Ui*` 组件。

### 壳层布局

Topbar 56 · Sidebar 240 · Main max 1600 / pad 32。侧栏：工作区/管理/统计；激活实心 accent。

### Token（`styles/global.css`）

| Token | 浅 | 深 |
|-------|----|----|
| `--accent` | `#4F7BFF` | `#60A5FA` |
| `--bg-body` / `--bg-card` | `#F8FAFC` / `#FFF` | `#0B1120` / `#1E293B` |
| `--border-color` | `#EAEAEA` | `#334155` |
| `--text-primary` / `--text-secondary` | `#0F172A` / `#64748B` | `#F1F5F9` / `#94A3B8` |
| success / warning / danger | 绿/橙/红系 | 对应亮色 |

`data-accent` 可切换强调色。字号：标题 24 · 卡题 16 · 正文 14 · 辅 13 · 标 12。间距 8px 体系。卡片圆角 12、Hover 上浮。按钮主 accent / 次描边 / 危险红。

深色：`data-theme="dark"` · 初随系统 · `localStorage` `theme-preference`。  
断点：&lt;768 侧栏 overlay；&lt;1024 Topbar 字隐。动画：路由 fade 200ms · 侧栏 250ms · 骨架 1.5s。空状态用 `EmptyState`，禁纯文字。

---

## 共享组件 / 工具

| 组件 | 用途 |
|------|------|
| `EmptyState` · `PageHeader` · `ListToolbar` · `ListPagination` | 列表壳 |
| `UiDialog` / `UiInput` / `UiButton` / `UiCard` | 基础 UI |
| `CommandPalette` · `NotificationBell` · `ProfileDrawer` · `AppLayout` | 全局壳层 |
| `NoteVditor` · `NoteMarkdownPreview` · `DocLayout` | 笔记/文档 |

| composable / util | 用途 |
|-------------------|------|
| `useDeepLinkDialog` · `useMainContentFill` · `useFillPageSize` · `useProductViewMode` | 列表页（细则 `PAGE_SPEC`） |
| `formatTime` · `readingTime` · `deepLink` | 日期/相对时间 / 深链 |

样式：`global.css`（Token）· `product-list.css` · `stats-row.css` · `markdown-prose.css`。

**Dashboard：** 12 列 Bento + `useMainContentFill`；趋势/KPI 在 `/stats`。设置「工作台」为标签云显隐排序。

**笔记编辑器 composable：** `useAutoSave` / `useEditorMode` / `useEditorPreferences` / `useImageUpload` / `vditorSetup` / `contextMenuActions` / `parseToc`（目录 `note/editor/`）。

---

## Git

```
<类型>: <简要描述>
```

`feat` | `fix` | `docs` | `style` | `refactor` | `perf` | `test` | `chore`

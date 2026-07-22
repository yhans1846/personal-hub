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
| 标志位 | 0/1 用 `Flags.YES` / `Flags.NO`，禁魔法数字 |
| 实体类型 | `EntityType.NOTE` 等，禁散落 `"note"`/`"bookmark"` 字符串 |
| 日志 | 增删改 info；权限/业务 warn；未知 error+堆栈；过滤器 debug |
| 状态文案 | 模块 `enums` + `labelOf`，禁 VO/Service 内嵌业务 switch |
| 归属 | `EntityGuard.requireOwned(...)` |
| 文件名 | `FilenameGuard.requireSafe(...)`，禁 `..` `/` `\` |
| 存储路径 | `StoragePaths`；上传校验 `FileUploadValidator`；门面 `FileAssetService` |
| 用户备份 | `com.personalhub.backup`（ZIP 导出/覆盖导入；路径安全 `BackupZipSupport`） |
| Entity | `@Data`+`@Builder`+无参/全参；初值 `@Builder.Default`；create 用 builder，update 用 setter |
| 分层 | Controller 校验返回 · Service 业务 · Mapper · DTO/VO/Entity 不外露 |
| SQL | 简单 MP；联表/聚合进 `mapper.xml`，禁 Service 拼 SQL / `JdbcTemplate` 硬编码 |
| 时间 | `LocalDateTime` |

命名：包小写 · 类 Pascal · 方法 camel · 表字段 snake · `XxxDTO/VO`。

### 后端踩坑点（对照阿里手册 · 已踩过）

| 坑 | 正确做法 |
|----|----------|
| `Integer == 1` 自动拆箱 | 用 `Integer.valueOf(Flags.YES).equals(x)` 或 `Objects.equals` |
| `entity.getUserId().equals(userId)` | 用 `EntityGuard.requireOwned` / `Objects.equals` |
| `getInputStream().readAllBytes()` 不关流 | `try (InputStream in = …)`；远端下载加体积上限 |
| 存储路径仅 `normalize()` | `LocalStorageServiceImpl.resolve` 须 `target.startsWith(root)`；文件名禁 `..` `/` `\` |
| 导入任意 URL / `file://` | `ResourceResolver` 禁 LFI；HTTP 禁内网地址且不跟随重定向 |
| `catch (Exception ignored) {}` | 收窄类型并打日志；禁止空 catch |
| `@Transactional` 内 broad catch | 失败应回滚或按用户隔离；勿吞掉后部分提交 |
| 分页只有 `@Min` | `PageParam` 必须 `@Max(100)`（`MAX_PAGE_SIZE`） |
| `throw new RuntimeException` / 未处理的 `IllegalArgumentException` | 业务用 `BusinessException`；全局已映射 `IllegalArgumentException`→400 |
| 魔法 `0`/`1` | `Flags.YES` / `Flags.NO` |
| `markAsRead(ids)` 空列表进 `.in()` | 空/null 直接 return |
| 验证码把答案字段下发客户端 | 书架验证码不下发 `emptyIndex`，由 `shelfBooks` 空串推导；答案只存 Redis |
| Service 内 `JdbcTemplate` / 字符串 SQL | 迁入对应 `Mapper` + `mapper.xml`（`#{}`）；禁在 Service 拼 SQL |
| 实体字段 `isDeleted` 等 | **存量保留**（改名成本高）；新字段勿用 `is` 前缀，用 `@TableField("is_xxx")` 映射列名 |
| `updateById` 更新含 null 字段 | MP 默认 `NOT_NULL` 策略，null 字段不生成 SET 子句。需要将字段更新为 null 时（如 `parentId`/`folderId` 置空），必须用 `LambdaUpdateWrapper` 显式 `.set(Entity::getField, null)`，禁止依赖 `updateById` |

完整手册正文见 `docs/alibaba-java-specification.md`（参考，非日常 SSOT）。

---

## 前端（Vue 3 / TS）

| 类型 | 规则 |
|------|------|
| 组件页 | `PascalCase.vue` |
| composable / store | `useXxx` / `useXxxStore` |
| API 文件 | `camelCase.ts` |
| class | kebab |

- 一律 `<script setup lang="ts">`
- 前端门禁：`pnpm lint`（0 warn）；`vue-tsc -b`（`pnpm build` 前置）；`pnpm knip`
- API：`src/api/` + `modules/*/api.ts`；禁一表一文件
- Pinia 仅全局（auth/theme/layout/readingConfig/notification/notificationConfig/featureFlags）；列表页内拉 API
- `themeStore`：外观（主题/强调色/圆角/密度/内容宽）；`layoutStore`：菜单/工作台/统计卡片
- 实验开关：`featureFlagStore.isEnabled`；未实现项 `available: false`（设置页禁用）
- 通知类型：`enabledTypes` 对齐后端 `TODO_OVERDUE` / `PLAN_DEADLINE` / `PLAN_COMPLETED`，列表与未读计数按配置过滤
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

Topbar 56 · Sidebar 240（可收成图标窄栏 56，状态记 localStorage）· Main max 1600 / pad 32。侧栏：工作区/管理/统计；激活实心 accent。

### Token（`styles/global.css`）

| Token | 浅 | 深 |
|-------|----|----|
| `--accent` | `#4F7BFF` | `#60A5FA` |
| `--bg-body` / `--bg-card` | `#F8FAFC` / `#FFF` | `#0B1120` / `#1E293B` |
| `--border-color` | `#EAEAEA` | `#334155` |
| `--text-primary` / `--text-secondary` | `#0F172A` / `#64748B` | `#F1F5F9` / `#94A3B8` |
| success / warning / danger | 绿/橙/红系 | 对应亮色 |

`data-accent` 可切换强调色。字号：标题 24 · 卡题 16 · 正文 14 · 辅 13 · 标 12。间距用 `--sp-*`（密度可缩放）。  
圆角：一律 `var(--radius-sm|md|lg|xl)`，由外观「界面圆角」写入；禁硬编码 `Npx`（头像等真圆可用 `50%`；开关等可 `--radius-full`）。  
动画：一律 `var(--transition)` / `--transition-duration`，禁写死 `150ms`/`0.2s`；`data-anim=off` 时全局过渡为 0。  
主题 / 强调色：只用 `--bg-*` / `--text-*` / `--accent*`；Element Plus 主色绑定 `--el-color-primary: var(--accent)`。  
`el-collapse` 须透明底 + `--border-color` 分割线（见 `global.css`），避免深色下露白条。  
密度：间距用 `--sp-*`（外观缩放）；表格等控件跟 `data-density`。内容宽用 `--content-max-width`。  
按钮主 accent / 次描边 / 危险红。列表工具栏：主操作 `create-label`（Primary）；次要操作用全局 `.toolbar-btn`，危险操作用 `.toolbar-btn--danger`；`#actions` 与主按钮同在 `.toolbar-right` 并排。

深色：`data-theme="dark"` · 护眼：`data-theme="sepia"`（豆沙绿，卡片/顶栏禁用纯白）· 初随系统 · `localStorage` `theme-preference`。  
断点：&lt;768 侧栏 overlay；≥768 侧栏可收成图标窄栏（底栏按钮）；&lt;1024 Topbar 字隐。图标/操作悬停提示用 `UiTooltip`（或 `el-tooltip` + `ph-tooltip`），**禁原生 `title` 气泡**（长文本截断可用 title）。动画：路由 fade 跟随 `--transition` · 侧栏同 · 骨架 1.5s。空状态用 `EmptyState`，禁纯文字。  
滚动条：全局隐藏（`global.css`），页面/卡片/弹窗仍可滚轮与触控滚动，禁露出可视滚动条。

---

## 共享组件 / 工具

| 组件 | 用途 |
|------|------|
| `EmptyState` · `PageHeader` · `ListToolbar` · `ListPagination` | 列表壳（`ListToolbar`：`#actions`+主按钮并排；全局 `.toolbar-btn` / `--danger`） |
| `UiTooltip` | 统一悬停提示（圆角/阴影/Token；替代原生 title） |
| `UiDialog` / `UiInput` / `UiButton` / `UiCard` | 基础 UI |
| `DialogTitleField` / `DialogPropGrid` / `DialogPropCard` | 编辑器气质：标题区 · 属性网格/块 |
| `DialogChoiceRow` / `DialogChipRow` / `DialogDateChip` / `DialogEditor` | 选择卡 · Chip · 日期 · 写作区 |
| `DialogFooterActions` | Footer |
| `FilePreviewDialog` · `ImageLightbox` | 文件预览 · 居中图片预览 |
| `CommandPalette` · `NotificationBell` · `ProfileDrawer` · `AppLayout` | 全局壳层 |
| `VirtualList` | 固定行高窗口化列表（通知下拉等） |
| `NoteVditor` · `NoteMarkdownPreview` · `DocLayout` | 笔记/文档 |

业务编辑表面（笔记除外）：骨架为 **标题区 → 属性块 → 写作区 → Footer**；壳用 `UiDialog`（`size` sm/md/lg）；Footer 用 `DialogFooterActions`。短表单（标签/分类）无写作区，字段仍进 `DialogPropCard`。`ProfileDrawer` 保持抽屉，表单包 `DialogPropCard`，按钮用 `UiButton`。细则见 `docs/superpowers/specs/2026-07-18-dialog-editor-character-design.md`。

| composable / util | 用途 |
|-------------------|------|
| `useDeepLinkDialog` · `useMainContentFill` · `useFillPageSize` · `useProductViewMode` | 列表页（细则 `PAGE_SPEC`） |
| `usePaginatedList` · `useEntityDialogHost` | 列表 fetch/分页/loading；Dialog 宿主 visible+editId |
| `useVirtualList` | 固定行高窗口化；卡片网格可用 `content-visibility: auto` |
| `useEntityDialog` · `useEntityFormSave` | Dialog 加载实体；统一 validate→create/update→提示（`invokeOnSavedAfterCreate: false` 可留窗） |
| `unwrapResult` / `unwrapPage` / `handleApiError`（`utils/apiResult`） | API 解包与错误提示 |
| `triggerBlobDownload`（`utils/file`） | Excel/通用 Blob 本地下载 |
| `useStatsCharts`（`modules/stats`） | 统计页 ECharts 生命周期与渲染 |
| `formatTime` · `readingTime` · `deepLink` | 日期/相对时间 / 深链 |
| `notificationSound` | 内置提示音播放 / 免打扰时段判断（`src/assets/sounds`） |
| `getFilePreviewUrl` · `getDiaryImagePreviewUrl` · `downloadFileBlob` · `getFilePreviewKind` | 鉴权预览 blob / 下载 / 类型判断（`utils/file.ts`） |

样式：`global.css`（Token）· `product-list.css` · `stats-row.css` · `markdown-prose.css`。

**Dashboard：** 12 列 Bento + `useMainContentFill`；趋势/KPI 在 `/stats`。设置「工作台」为标签云显隐排序。

**笔记编辑器 composable：** `useAutoSave` / `useEditorMode` / `useEditorPreferences` / `useImageUpload` / `vditorSetup` / `contextMenuActions` / `parseToc`（目录 `note/editor/`）。

---

## Git

```
<类型>: <简要描述>
```

`feat` | `fix` | `docs` | `style` | `refactor` | `perf` | `test` | `chore`

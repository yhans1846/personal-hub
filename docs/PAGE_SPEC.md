# 页面开发规范（PAGE_SPEC）

> 职责：页面形态与交互契约。Token/组件目录 → `STYLE_GUIDE.md`；接口参数 → `API.md`。  
> 参考：`studyplan/List.vue` · `note/Preview.vue`  
> 新形态（表单/设置等）追加本文，不另起 SPEC。

---

## 1. 选型

| 场景 | 形态 |
|------|------|
| 多列对照 | Product Table（禁默认企业风 `el-table`） |
| 浏览发现 | Product Card |
| 都要 | Table/Card + `useProductViewMode` |
| 时间线 | 学习记录例外（§2.4） |
| Markdown 只读 | DocLayout + `markdown-prose`（§3） |
| 编辑 | 默认 Dialog；笔记列表新建/编辑为**全屏 Overlay 工作区**（Teleport，非独立路由页）；只读预览仍独立页 |

禁：灰底密表、行内一堆 Primary、无必要独立编辑页。

---

## 2. 列表页

### 骨架

`plan-page` → `plan-top`（PageHeader + 徽章 + ListToolbar）→ `plan-middle`（loading / EmptyState / Table / Card）→ `plan-foot`（ListPagination）

### 一屏铺满

`useMainContentFill` + `useFillPageSize`（禁手写 class / 固定 PAGE_SIZE）。  
适用：计划、阅读、笔记、日记、学习记录、待办、回收站、Dashboard。

| 可视高 | PAGE_SIZE | Card 桌面 |
|--------|-----------|-----------|
| ≥640 | 10 | 5×2 |
| 520–639 | 8 | 4×2 |
| &lt;520 | 6 | 3×2 |

筛选/搜索/排序 → `page=1`；槽位 = PAGE_SIZE，不足 pad；`total>0` 显分页。

### 学习记录例外

`/study-records`：时间线 + fill + ListToolbar，无 Table/Card 切换。

### 收藏夹 / 文件例外

- `/bookmarks`、`/files`：一屏铺满 5×4（`size=20`，不足 pad）；大预览区 + 标题 + 底栏；工具栏无「分类管理」
- 侧栏「分类管理」页仍可用

### Product Table / Card

Table：弱横线、均分行、整行编辑；主列→状态点→软标签→progress→tertiary→`⋯`。  
Card：桌面 5×2；≤1100→4；≤720→2。标题|状态→标签(≤2)→来源·作者→进度→日期→备注→「更新于…」。

相对时间：刚刚 / N分 / N时 / N天 / YYYY-MM-DD。

### 工具栏与导出

ListToolbar + 可选状态徽章（非 KPI 大卡）。禁营销副标题。

| 导出 | 约定 |
|------|------|
| 入口 | 「新建」左 dropdown：当前 / 全部 |
| 格式 | XLSX（内存流） |
| 契约 | 见 `API.md` 对应 `/export` |

交互：Dialog · `useDeepLinkDialog` · 删后刷新 · 骨架 · lucide。

### 日记列表补充

- 列表/卡片缩略图走 `getDiaryImagePreviewUrl(diaryId, filename)`，非 `/api/files`
- 新建日记：**先保存**拿到 id 后再传配图；弹窗可停留编辑态继续上传

### 文件列表补充

- 点击卡片 → `FilePreviewDialog`（图 / PDF / txt·md；其余提示下载）
- 下载须鉴权 blob，禁裸 `window.open`
- 上传可选分类；卡片展示分类名（与收藏夹一致）；列表仅本页上传文件；工具栏无「分类管理」链接；一屏 5×4 铺满（`size=20`）

---

## 3. 预览 / 文档页

`DocLayout` + `markdown-prose` + `readingConfigStore`（设置页配置；Header 不嵌阅读 popover）。  
TOC 宽 220（收起 34，拖 160–400）。主题：`data-preview-theme` → `--preview-*` → `--prose-*`。  
持久化：`reading-config` + `user_layout(preview)` 防抖 500ms。

| 注意 | 约定 |
|------|------|
| 图 | `?token=` + `setupImageProxy` |
| `:deep()` | 仅盖库硬编码；排版进 prose CSS |
| TOC | `getElementById`，禁 textContent |
| 加载 | content → nextTick 挂 Proxy/Zoom/Copy/Anchors；换主题重调 Zoom |

---

## 4. 业务 Dialog（编辑器气质）

笔记除外：`UiDialog` + **标题区 → 属性块 → 写作区 → Footer**。  
组件目录见 `STYLE_GUIDE`；细则见规格 `2026-07-18-dialog-editor-character-design`。  
短表单（标签/分类）无写作区；`ProfileDrawer` 例外保留抽屉。

---

## 5. Checklist

**列表：** Header+Toolbar+Empty+Pagination · Table/Card 或例外 · 槽位/fill · Dialog+深链 · Token+骨架+深色  
**笔记：** 列表新建/编辑为**全屏 Overlay 工作区**（`List.vue` 托管 + Teleport → body）；**默认三栏分屏**（左 Markdown 编辑 · 中实时预览 · 右标题大纲；编辑与预览等宽，大纲约 15%，固定比例 v1 不可拖拽；**分屏时编辑/预览由两侧 pane 统一滚动并按比例联动**；大纲点击定位预览与编辑区，锚点规则与独立预览页一致，编辑侧剥离 IR marker 后对齐 id）；顶栏可编辑**标题** +「属性」**抽屉**（分类/标签等 meta，Esc 可关抽屉）；模式 `split`（默认）\| `edit` \| `preview`（顶栏切换；Ctrl+Shift+P 在 split↔preview；≤768 默认 edit 全宽）；仅顶栏 ✕ 关闭，**Esc 不关工作区**；脏关闭须确认；深链 `/notes?create=1`、`/notes?edit=<id>`（旧 `/notes/new`、`/notes/:id/edit` redirect）；只读预览仍 `/notes/:id/preview`；右键「归档」→`AUTO_ARCHIVE`；「移入回收站」→`USER_DELETE`；列表多选 →「导出 ZIP」（≤50，仅勾选）；回收站展示删除原因 +「清空回收站」；编辑右键可插入 Mermaid 围栏块与 KaTeX（行内 `$` / 块级 `$$`），IR/预览/文件 md 预览常开渲染；实验开关「双向链接」开启后支持 `[[标题]]` 补全、预览跳转与回链列表；预览配图点开为自定义灯箱（点遮罩/×/Esc 关闭）；**工作区近全宽**（绕过 `--content-max-width`，无专注模式）  
**命令面板：** Ctrl/Cmd+K 支持快捷新建（笔记/日记/阅读/学习/待办/计划）与页面/内容搜索  
**设置·数据：** 独立 Tab「数据管理」——缓存清理；立即备份（落盘历史+下载）；自动备份频率；历史下载/恢复/删除；导入 `.zip` 全量覆盖  
**设置·高级：** 通知偏好 · 实验功能（AI「即将推出」· 双向链接可开）  
**预览：** DocLayout+prose · readingConfig · §3 注意项  
**Dialog：** 积木骨架 · FooterActions · 日记配图先保存  
**文件：** 内嵌预览 · 鉴权下载 · md 预览与笔记共用 Vditor 配置（含 Mermaid/KaTeX）· 不与笔记/日记资源混库

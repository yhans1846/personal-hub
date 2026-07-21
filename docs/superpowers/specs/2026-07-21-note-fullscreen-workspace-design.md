# 笔记全屏工作区 Overlay（列表内编辑）

> 日期：2026-07-21 · 方案 A（列表托管 Overlay + Teleport）  
> 范围：列表新建/编辑走全屏工作区；深链对齐；旧编辑路由 redirect。命令面板等经 redirect 兼容。

## 目标

- 在笔记列表页点击「新建笔记」或编辑：当前页上覆盖**整页工作区**（非半透明弹窗），列表仍挂在下层。
- 关闭**仅**通过顶栏 ✕；**Esc 不关闭**工作区。
- 有未保存改动时，关闭前确认。

## 背景

- 现况：`List.vue` 对新建/编辑 `router.push('/notes/new'|'/notes/:id/edit')`，整页跳到 `Editor.vue`，离开列表上下文。
- 其它模块已用 `?create=1` / `?edit=` + `useDeepLinkDialog`；笔记编辑仍是独立路由页。
- 专注模式已移除；编辑宽栏与 `useMainContentEditor` 已就位，可直接复用于 overlay 内 Editor。

## 设计

### 1. 架构与入口

| 项 | 约定 |
|----|------|
| 宿主 | `List.vue` 托管 `NoteWorkspaceOverlay`，`Teleport` 到 `body` |
| 状态 | `workspace: null \| { mode: 'create' } \| { mode: 'edit', id: number }`；同时只开一个 |
| 新建 | 「新建笔记」→ 设 `workspace`，**不** `push` `/notes/new` |
| 编辑 | 行编辑 / 右键编辑 → 设 `workspace`，**不** `push` `/notes/:id/edit` |
| 深链 | `/notes?create=1`、`/notes?edit=<id>` → 打开后清理 query（对齐 `useDeepLinkDialog`） |
| 预览 | `/notes/:id/preview` **不变** |

### 2. 壳层 UI / 与 Editor

| 项 | 约定 |
|----|------|
| 外观 | `fixed` 铺满视口；背景 `var(--bg-body)`；`z-index` 高于 App 侧栏/顶栏 |
| 内容 | Overlay 内复用 `Editor`；布局与当前独立编辑页一致（宽栏、顶栏、状态栏） |
| Header | 「返回」改为 **✕**（`title="关闭"`），`emit('close')`；预览/保存态/收藏/浏览器全屏/更多保留 |
| 嵌入 | Editor 增加嵌入约定（如 `embedded` / `closeMode: 'overlay'`）：关口走 close，不 `router.back()` |
| Esc | **不**用于关闭 overlay；浏览器全屏退出、预览图 lightbox、wiki 建议框等 Esc 行为不变 |
| 脏关闭 | ✕ 且 dirty → 确认（保存并关闭 / 不保存关闭 / 取消）。因不离路由，`onBeforeRouteLeave` 管不到关 overlay，必须显式确认；`beforeunload` 仍保留 |

### 3. 数据流 / 关闭与刷新

```
List.workspace → NoteWorkspaceOverlay → Editor(embedded)
```

| 事件 | 行为 |
|------|------|
| 打开 | 可选同步 URL `?create=1` / `?edit=id`；深链消费后清 query |
| 新建首次保存得 id | Editor 内切编辑态；`workspace` 更新为 `{ mode: 'edit', id }` |
| 关闭成功 | `workspace = null`，销毁 Editor；**必做** `fetchList()` |
| 自动/手动保存 | 仍走现有 Note API，语义不变 |

### 4. 兼容路由

| 旧路径 | 行为 |
|--------|------|
| `/notes/new` | redirect → `/notes?create=1` |
| `/notes/:id/edit` | redirect → `/notes?edit=:id` |

命令面板 / 工作台若仍链旧路径，经 redirect 落到列表 + overlay。

### 5. 错误与边界

- 深链 `edit` 指向不存在/无权限笔记：打开 overlay 后由 Editor 既有加载错误处理（提示 + 可 ✕ 关闭），不静默空白卡住。
- 关闭确认框打开时，不因其它快捷键误关 overlay。
- Overlay 打开期间列表不可操作（被遮罩挡住即可，无需额外禁用逻辑）。

## 明确不做

- Esc 关闭工作区  
- 关 overlay 后浏览器「后退」再打开  
- 多笔记并排 / 同时多个 workspace  
- 改预览独立页为 overlay  
- 本轮重做命令面板入口文案（redirect 即可）

## 涉及文件（预期）

- `personal-hub-web/src/modules/knowledge/note/List.vue`
- 新建 `NoteWorkspaceOverlay.vue`（或等价命名）
- `Editor.vue` · `editor/EditorHeader.vue`（嵌入关口 / ✕）
- `router/index.ts`（redirect）
- 深链：`useDeepLinkDialog` 或列表内同语义接入
- 文档：`PAGE_SPEC.md` · `STYLE_GUIDE.md`（若补笔记深链说明）· `CHANGELOG.md`

## 验收

1. `/notes` 点「新建笔记」：全屏工作区盖住列表，URL 可为 `/notes` 或短暂 `?create=1` 后清理  
2. 编辑一篇笔记同理；关 ✕ 后回到列表且数据已刷新  
3. Esc **不**关工作区；浏览器全屏仍可用 Esc 退出全屏  
4. dirty 时 ✕ 出确认；选取消仍留在工作区  
5. `/notes/new`、`/notes/1/edit` 分别落到 create/edit overlay  
6. `/notes/:id/preview` 行为不变  
7. ≤768 无横向溢出、可关 ✕  

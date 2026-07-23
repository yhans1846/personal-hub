# 笔记编辑/新建 Overlay 路由化

> 日期：2026-07-23 · 已确认（方案 A）  
> 前置：知识空间导航、`NoteWorkspaceOverlay`、预览独立页 `/notes/:id/preview`  
> 范围：编辑/新建 URL 可刷新；保留现有全屏 Overlay 体验。  
> 非目标：独立整页编辑器、改预览路由、后端 API 变更。

## 1. 目标与成功标准

- 新建/编辑仍为列表上的全屏 Overlay（左知识空间 + `Editor`），视觉与交互不换壳。
- 地址可刷新、可分享、可浏览器回退。

成功标准：

1. 编辑中刷新：仍打开同一笔记的 Overlay（`/notes/:id/edit`）。
2. 新建中刷新：仍为新建 Overlay（`/notes/new`）；首次保存成功后 URL 变为 `/notes/{id}/edit`。
3. 关闭 Overlay → `/notes`（尽量保留进入前的夹选中/首页态）。
4. 命令面板「新建笔记」、深链、树内点笔记均走真实路由；列表/首页/工作台等入口**新开浏览器标签**打开编辑（与预览一致）；Overlay 内树切换仍在当前标签 `replace`。
5. 不再依赖「redirect 到 query 再开 Overlay」作为主路径。

## 2. 路由约定

| 路径 | 名称（建议） | 行为 |
|------|----------------|------|
| `/notes` | `NoteList` | 首页或列表；**无** Overlay |
| `/notes/new` | `NoteCreate` | 打开新建 Overlay；可选 query `folderId`（数字；缺省/非法 → 未分类） |
| `/notes/:id/edit` | `NoteEdit` | 打开编辑 Overlay；`id` 无效或无权 → 提示并回 `/notes` |
| `/notes/:id/preview` | `NotePreview` | **不变**（独立预览页） |
| `/notes/recycle` | `RecycleBin` | **不变** |

路由注册注意：`notes/new`、`notes/recycle` 必须写在 `notes/:id/...` 之前，避免被当成 id。

废弃作为主路径的行为（可短期兼容，见 §5）：

- `redirect: /notes?create=1`
- `redirect: /notes?edit=:id`

## 3. 状态同步（List ↔ Router）

宿主仍为 `List.vue`（或等价笔记壳）：路由匹配打开 Overlay，关闭写回路由。

| 事件 | 行为 |
|------|------|
| 进入 `NoteCreate` | `workspace = { mode: 'create', folderId }`；挂载 `NoteWorkspaceOverlay` |
| 进入 `NoteEdit` | `workspace = { mode: 'edit', id }` |
| 离开上述路由 → `/notes` | 关闭 Overlay（若有未保存，先走既有脏确认；取消则 `router` 回到编辑 URL） |
| Overlay 关闭（按钮/Esc 等） | 脏确认通过后 `router.push('/notes')`（或 `replace`，与「从哪来」策略一致；默认 `push` 便于回退再进编辑） |
| 树/最近/收藏切换笔记 | 脏确认后 `router.replace({ name: 'NoteEdit', params: { id } })` |
| 新建首次保存拿到 id | `router.replace({ name: 'NoteEdit', params: { id } })`，Overlay 不卸载闪断（session key 策略与现 `created` 同步一致） |

`useDeepLinkDialog` / 命令面板：改为 `router.push('/notes/new')` 或 `/notes/:id/edit`，不再以 query 为唯一入口。

## 4. UI / 壳层

- Overlay 内容：继续 `NoteWorkspaceOverlay` + `KnowledgeSpaceNav`（编辑非只读）。
- App 布局：编辑/新建路由仍落在带 App 侧栏的笔记布局子路由下（与现列表同壳）；**不要**做成预览那种顶层全屏无主侧栏（除非后续单独立项）。
- 浏览器标题：`meta.title` 建议「新建笔记」/「编辑笔记」或带笔记标题（标题可在加载后 `document.title` 更新，非必须一期）。

## 5. 兼容

一期建议直接改为真实组件路由（`NoteList` 同时处理子状态，或子路由 outlet）。若需平滑：

- 访问旧 `?create=1` / `?edit=` → 一次性 `replace` 到新路径后清除 query。

回收站、预览、导入深链不受影响。

## 6. 文档与验收

同步更新：`docs/PAGE_SPEC.md`（去掉「非独立路由页」表述）、`docs/CHANGELOG.md`；相关 home-shell / knowledge-space 规格可加一句「编辑 URL 见本规格」。

手测：

- [ ] `/notes/new` 刷新仍新建；保存后 URL 变 edit，再刷新仍是该笔记
- [ ] `/notes/{id}/edit` 刷新仍编辑；关返回 `/notes`
- [ ] 脏编辑时关页/切笔记/浏览器后退：确认框；取消不丢稿、URL 正确
- [ ] 命令面板新建 → `/notes/new`
- [ ] 预览路由与回收站无回归

## 7. 实现顺序（供计划拆分）

1. 路由表：真实 `NoteCreate` / `NoteEdit`（可与 `NoteList` 同组件 + 路由监听）
2. List：workspace ↔ route 双向同步；关掉 query 主路径
3. Overlay/Editor：保存后 replace；关闭 push `/notes`
4. 深链与命令面板
5. 文档与手测

实现前用 `writing-plans` 出任务清单。

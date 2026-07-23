# 笔记编辑 Overlay 路由化 实现计划

> **面向 AI 代理的工作者：** 可按任务顺序实现。步骤用复选框跟踪。

**目标：** 新建/编辑 Overlay 使用真实路由 `/notes/new`、`/notes/:id/edit`，刷新不丢编辑态。

**架构：** `NoteList` 组件同时挂载在三条路由上；用 `useRoute`/`useRouter` 双向同步 `workspace`。Overlay 与脏确认逻辑保留。

**技术栈：** Vue 3 + Vue Router 4

**规格：** `docs/superpowers/specs/2026-07-23-note-edit-route-design.md`

---

## 文件

| 文件 | 职责 |
|------|------|
| `personal-hub-web/src/router/index.ts` | `NoteCreate`/`NoteEdit` 指向 `List.vue`，去掉 redirect |
| `personal-hub-web/src/modules/knowledge/note/List.vue` | workspace ↔ route；开/关/切笔记推路由 |
| `personal-hub-web/src/modules/knowledge/note/NoteWorkspaceOverlay.vue` | 切笔记时 `emit('open-note')` 或由父级 replace（父级已监听即可） |
| `docs/PAGE_SPEC.md` / `docs/CHANGELOG.md` | 文档 |

---

### 任务 1：路由表

**文件：** `personal-hub-web/src/router/index.ts`

- [ ] **步骤 1：** 将 `notes/new`、`notes/:id/edit` 改为与 `notes` 相同 component；`notes/recycle` 保持在 `:id` 之前；meta.title 分别为「新建笔记」「编辑笔记」

```ts
{ path: 'notes', name: 'NoteList', meta: { title: '笔记' }, component: () => import('.../List.vue') },
{ path: 'notes/new', name: 'NoteCreate', meta: { title: '新建笔记' }, component: () => import('.../List.vue') },
{ path: 'notes/recycle', name: 'RecycleBin', ... },
{ path: 'notes/:id/edit', name: 'NoteEdit', meta: { title: '编辑笔记' }, component: () => import('.../List.vue') },
```

---

### 任务 2：List 路由同步

**文件：** `List.vue`

- [ ] **步骤 1：** `openCreate` → `router.push({ name: 'NoteCreate', query: folderId != null ? { folderId } : {} })`
- [ ] **步骤 2：** `openEdit(id)` → `router.push({ name: 'NoteEdit', params: { id: String(id) } })`
- [ ] **步骤 3：** `closeWorkspace` → 清 workspace + `router.push({ name: 'NoteList' })`（若已在 NoteList 则仅清状态）
- [ ] **步骤 4：** `watch(() => route.name / params / query)`：匹配 Create/Edit 时设置 workspace；回到 NoteList 时关闭
- [ ] **步骤 5：** 兼容旧 `?create=1`/`?edit=`：`replace` 到新路径
- [ ] **步骤 6：** `onWorkspaceNoteId` → `router.replace({ name: 'NoteEdit', params: { id } })`
- [ ] **步骤 7：** Overlay `@open-note`（若新增）或包装 `openEdit` 用 `replace` 避免历史堆叠；树切换用 replace

脏关闭：浏览器后退离开 Create/Edit 时，若 Overlay 仍开且 dirty，需拦截——优先在 `close` 路径已有确认；对 `onBeforeRouteLeave`：若目标不是 Edit/Create 且 workspace 开着，调 `editor.requestLeave`，拒绝则 `next(false)`。

---

### 任务 3：Overlay 切笔记通知父级

**文件：** `NoteWorkspaceOverlay.vue`

- [ ] **步骤 1：** `onOpenNote` 成功后 `emit('navigate-note', id)`（或复用已有）；List 里 `replace` 到 Edit
- [ ] **步骤 2：** 父级根据 route.params.id 同步 `note-id` prop（watch props.noteId）

---

### 任务 4：文档与验收

- [ ] 更新 PAGE_SPEC、CHANGELOG
- [ ] 手测：刷新 `/notes/new`、`/notes/1/edit`；保存后 URL；关闭回列表；命令面板

---

**Commit 建议（用户要求时再提交）：** `feat(notes): 编辑/新建 Overlay 使用真实路由`

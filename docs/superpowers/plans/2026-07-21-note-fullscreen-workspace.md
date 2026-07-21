# 笔记全屏工作区 Overlay 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 笔记列表内新建/编辑以全屏 Overlay 工作区完成；仅 ✕ 关闭且 Esc 不关；脏关闭需确认；旧路由 redirect 到深链。

**架构：** `List.vue` 持有 `workspace` 状态，经 `NoteWorkspaceOverlay`（Teleport → body）挂载嵌入态 `Editor`。Editor 用 props 决定新建/编辑 id，关口 `emit('close')`；路由 `/notes/new`、`/notes/:id/edit` 改为 redirect。

**技术栈：** Vue 3 · Vue Router · Element Plus MessageBox · 既有 `useDeepLinkDialog` / `useAutoSave`

**规格：** `docs/superpowers/specs/2026-07-21-note-fullscreen-workspace-design.md`

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `personal-hub-web/src/modules/knowledge/note/editor/EditorHeader.vue` | `closeMode: 'back' \| 'close'`；close 时显示 ✕「关闭」 |
| `personal-hub-web/src/modules/knowledge/note/Editor.vue` | props `embedded` + `initialNoteId`；关/删走 emit；嵌入时跳过 `useMainContentEditor`；脏关确认 |
| `personal-hub-web/src/modules/knowledge/note/NoteWorkspaceOverlay.vue` | Teleport 全屏壳，内嵌 Editor，转发 close / note-id |
| `personal-hub-web/src/modules/knowledge/note/List.vue` | `workspace` 状态、打开/关闭、`useDeepLinkDialog`、`fetchList` |
| `personal-hub-web/src/router/index.ts` | `notes/new`、`notes/:id/edit` → query redirect |
| `personal-hub-web/src/modules/knowledge/note/editor/__tests__/requestEditorClose.test.ts` | 脏关决策纯函数单测 |
| `personal-hub-web/src/modules/knowledge/note/editor/requestEditorClose.ts` | 脏关确认逻辑（可测） |
| `docs/PAGE_SPEC.md` · `docs/CHANGELOG.md` | 契约与变更 |

---

### 任务 1：脏关确认纯函数 + 单测（TDD）

**文件：**
- 创建：`personal-hub-web/src/modules/knowledge/note/editor/requestEditorClose.ts`
- 测试：`personal-hub-web/src/modules/knowledge/note/editor/__tests__/requestEditorClose.test.ts`

- [ ] **步骤 1：编写失败的测试**

```ts
import { describe, it, expect, vi } from 'vitest'
import { resolveEditorClose } from '../requestEditorClose'

describe('resolveEditorClose', () => {
  it('not dirty → allow immediately', async () => {
    const forceSave = vi.fn()
    const r = await resolveEditorClose({
      dirty: false,
      confirm: vi.fn(),
      forceSave,
    })
    expect(r).toBe('proceed')
    expect(forceSave).not.toHaveBeenCalled()
  })

  it('dirty + confirm save → forceSave then proceed', async () => {
    const forceSave = vi.fn().mockResolvedValue(undefined)
    const confirm = vi.fn().mockResolvedValue('save')
    const r = await resolveEditorClose({ dirty: true, confirm, forceSave })
    expect(forceSave).toHaveBeenCalled()
    expect(r).toBe('proceed')
  })

  it('dirty + discard → proceed without save', async () => {
    const forceSave = vi.fn()
    const confirm = vi.fn().mockResolvedValue('discard')
    const r = await resolveEditorClose({ dirty: true, confirm, forceSave })
    expect(forceSave).not.toHaveBeenCalled()
    expect(r).toBe('proceed')
  })

  it('dirty + cancel → abort', async () => {
    const forceSave = vi.fn()
    const confirm = vi.fn().mockResolvedValue('cancel')
    const r = await resolveEditorClose({ dirty: true, confirm, forceSave })
    expect(r).toBe('abort')
  })
})
```

- [ ] **步骤 2：运行测试验证失败**

```bash
cd personal-hub-web && npx vitest run src/modules/knowledge/note/editor/__tests__/requestEditorClose.test.ts
```

预期：FAIL（模块不存在）

- [ ] **步骤 3：编写最少实现**

```ts
export type CloseConfirmChoice = 'save' | 'discard' | 'cancel'
export type CloseResolve = 'proceed' | 'abort'

export async function resolveEditorClose(opts: {
  dirty: boolean
  confirm: () => Promise<CloseConfirmChoice>
  forceSave: () => Promise<unknown>
}): Promise<CloseResolve> {
  if (!opts.dirty) return 'proceed'
  const choice = await opts.confirm()
  if (choice === 'cancel') return 'abort'
  if (choice === 'save') {
    try {
      await opts.forceSave()
    } catch {
      /* 保存失败仍允许调用方决定；本版与路由离开一致：仍 proceed */
    }
    return 'proceed'
  }
  return 'proceed'
}
```

- [ ] **步骤 4：运行测试验证通过**

同步骤 2 命令；预期 PASS

- [ ] **步骤 5：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/editor/requestEditorClose.ts \
  personal-hub-web/src/modules/knowledge/note/editor/__tests__/requestEditorClose.test.ts
git commit -m "test(notes): 工作区脏关闭决策纯函数"
```

---

### 任务 2：EditorHeader 关闭态（✕）

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/editor/EditorHeader.vue`

- [ ] **步骤 1：增加 `closeMode` prop**

```ts
import { ArrowLeft, X, Star, MoreHorizontal, Download, Trash2, Eye, Edit3, Maximize2, Minimize2 } from 'lucide-vue-next'

defineProps<{
  saveStatus: SaveStatus
  isFavorite: boolean
  mode: EditorMode
  isFullscreen?: boolean
  /** back=返回列表；close=关闭 overlay */
  closeMode?: 'back' | 'close'
}>()
```

模板左侧：

```vue
<button
  class="header-btn"
  @click="emit('back')"
  :title="closeMode === 'close' ? '关闭' : '返回'"
>
  <X v-if="closeMode === 'close'" :size="18" />
  <ArrowLeft v-else :size="18" />
  <span>{{ closeMode === 'close' ? '关闭' : '返回' }}</span>
</button>
```

默认 `closeMode` 缺省为 `'back'`（兼容）。emit 名仍用 `back`（语义为「退出编辑」）。

- [ ] **步骤 2：目视确认**（或 Story 无则跳过）— 本地打开任意仍走路由的编辑页时仍显示「返回」。

- [ ] **步骤 3：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/editor/EditorHeader.vue
git commit -m "feat(notes): 编辑头支持关闭态 ✕"
```

---

### 任务 3：Editor 嵌入态

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/Editor.vue`
- 依赖：任务 1 的 `resolveEditorClose`

- [ ] **步骤 1：增加 props / emits**

```ts
const props = withDefaults(defineProps<{
  /** 嵌入列表 Overlay 时为 true */
  embedded?: boolean
  /** 编辑已有笔记；缺省/undefined = 新建 */
  initialNoteId?: number
}>(), {
  embedded: false,
})

const emit = defineEmits<{
  close: []
  /** 新建首次落库或 workspace 需同步 id */
  'note-id': [id: number]
}>()
```

- [ ] **步骤 2：用 props 替代 route.params 决定编辑态**

```ts
const isEdit = computed(() => props.initialNoteId != null)
// useAutoSave(form, props.initialNoteId)
// onMounted 加载：用 props.initialNoteId，勿读 route.params.id
```

路由页在任务 5 已 redirect，正常不再单独挂载 Editor；仍保留 `embedded === false` 时 `handleLeave` → `router.push('/notes')` 作兜底。

- [ ] **步骤 3：嵌入时跳过主区 class**

```ts
if (!props.embedded) {
  useMainContentEditor()
}
```

- [ ] **步骤 4：关闭 / 删除出口**

增加 `confirmEditorClose`（ElMessageBox，对齐 `useAutoSave` 文案）：

```ts
async function confirmEditorClose(): Promise<CloseConfirmChoice> {
  try {
    await ElMessageBox.confirm('有未保存的更改，是否保存后离开？', '提示', {
      confirmButtonText: '保存并离开',
      cancelButtonText: '不保存',
      distinguishCancelAndClose: true,
      type: 'warning',
    })
    return 'save'
  } catch (action) {
    if (action === 'cancel') return 'discard'
    return 'cancel'
  }
}

async function handleBack() {
  const result = await resolveEditorClose({
    dirty: saveStatus.value === 'dirty',
    confirm: confirmEditorClose,
    forceSave,
  })
  if (result === 'abort') return
  if (props.embedded) emit('close')
  else router.push('/notes')
}
```

`handleDelete` 成功后：`embedded` → `emit('close')`，否则 `router.push('/notes')`。

- [ ] **步骤 5：同步 noteId**

```ts
watch(noteId, (id) => {
  if (id != null) emit('note-id', id)
})
```

- [ ] **步骤 6：Header 接线**

```vue
<EditorHeader
  :close-mode="embedded ? 'close' : 'back'"
  ...
  @back="handleBack"
/>
```

- [ ] **步骤 7：确认无 Esc 关工作区** — 不要给 overlay/Editor 增加 Esc→close；保留 `useEditorMode` 仅退浏览器全屏。

- [ ] **步骤 8：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/Editor.vue
git commit -m "feat(notes): Editor 支持嵌入 Overlay 与脏关闭"
```

---

### 任务 4：NoteWorkspaceOverlay

**文件：**
- 创建：`personal-hub-web/src/modules/knowledge/note/NoteWorkspaceOverlay.vue`

- [ ] **步骤 1：实现组件**

```vue
<script setup lang="ts">
import Editor from './Editor.vue'

defineProps<{
  noteId?: number
}>()

const emit = defineEmits<{
  close: []
  'note-id': [id: number]
}>()
</script>

<template>
  <Teleport to="body">
    <div
      class="note-workspace-overlay"
      role="dialog"
      aria-modal="true"
      aria-label="笔记工作区"
    >
      <Editor
        embedded
        :initial-note-id="noteId"
        @close="emit('close')"
        @note-id="emit('note-id', $event)"
      />
    </div>
  </Teleport>
</template>

<style scoped>
.note-workspace-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000; /* 高于 App 侧栏/顶栏，低于 MessageBox 默认层亦可接受 */
  background: var(--bg-body);
}
</style>
```

**禁止：** 在此组件监听 `keydown` Escape 关闭。

- [ ] **步骤 2：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/NoteWorkspaceOverlay.vue
git commit -m "feat(notes): 新增全屏 NoteWorkspaceOverlay"
```

---

### 任务 5：List 接线 + 深链

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/List.vue`

- [ ] **步骤 1：workspace 状态**

```ts
import { ref } from 'vue'
import NoteWorkspaceOverlay from './NoteWorkspaceOverlay.vue'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'

type Workspace =
  | null
  | { mode: 'create' }
  | { mode: 'edit'; id: number }

const workspace = ref<Workspace>(null)

function openCreate() {
  workspace.value = { mode: 'create' }
}
function openEdit(id: number) {
  workspace.value = { mode: 'edit', id }
}
function closeWorkspace() {
  workspace.value = null
  fetchList()
}
function onWorkspaceNoteId(id: number) {
  if (workspace.value?.mode === 'create') {
    workspace.value = { mode: 'edit', id }
  }
}

useDeepLinkDialog({ openCreate, openEdit })

function goCreate() { openCreate() }
function goEdit(id: number) { openEdit(id) }
```

可移除对 `router.push('/notes/new'|.../edit)` 的依赖（若 `router` 仅为此用则可删 import）。

- [ ] **步骤 2：模板挂载**

```vue
<NoteWorkspaceOverlay
  v-if="workspace"
  :note-id="workspace.mode === 'edit' ? workspace.id : undefined"
  @close="closeWorkspace"
  @note-id="onWorkspaceNoteId"
/>
```

- [ ] **步骤 3：手动冒烟**（开发服）

1. `/notes` → 新建 → ✕ → 列表刷新  
2. 编辑 → Esc 不关 → ✕ 可关  
3. `/notes?create=1` 自动打开且 query 被清  

- [ ] **步骤 4：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/List.vue
git commit -m "feat(notes): 列表内全屏工作区新建/编辑"
```

---

### 任务 6：路由 redirect

**文件：**
- 修改：`personal-hub-web/src/router/index.ts`

- [ ] **步骤 1：替换新建/编辑路由**

将：

```ts
{ path: 'notes/new', name: 'NoteCreate', meta: { title: '新建笔记' }, component: () => import('...Editor.vue') },
{ path: 'notes/:id/edit', name: 'NoteEdit', meta: { title: '编辑笔记' }, component: () => import('...Editor.vue') },
```

改为（对齐 diaries / study-records）：

```ts
{ path: 'notes/new', name: 'NoteCreate', redirect: { path: '/notes', query: { create: '1' } } },
{ path: 'notes/:id/edit', name: 'NoteEdit', redirect: (to) => ({ path: '/notes', query: { edit: String(to.params.id) } }) },
```

保留 `/notes/:id/preview` 不变。`notes/recycle` 等路径不受影响（`notes/:id/edit` 与 `notes/recycle` 不冲突）。

- [ ] **步骤 2：验证外链**

命令面板「新建笔记」`/notes/new`、Dashboard `router.push('/notes/new')`、RecentNotesWidget ``/notes/${id}/edit`` 应落到列表 + overlay。

- [ ] **步骤 3：Commit**

```bash
git add personal-hub-web/src/router/index.ts
git commit -m "feat(notes): 新建/编辑路由 redirect 到列表深链"
```

---

### 任务 7：文档 + 验收

**文件：**
- 修改：`docs/PAGE_SPEC.md`、`docs/CHANGELOG.md`
- 可选：`docs/STYLE_GUIDE.md` 或 `docs/qa/CURSOR_BROWSER_TESTING.md` 深链表补 `/notes?create=1`

- [ ] **步骤 1：PAGE_SPEC**

将「笔记除外：Full Page 编辑」等表述改为：

- 列表新建/编辑：全屏 Overlay 工作区（Teleport）；仅 ✕ 关闭，Esc 不关工作区  
- 深链：`/notes?create=1`、`/notes?edit=<id>`；旧 `/notes/new`、`/notes/:id/edit` redirect  
- 只读预览仍为独立页 `/notes/:id/preview`

- [ ] **步骤 2：CHANGELOG** 当日条增加本功能一句。

- [ ] **步骤 3：按规格验收清单自测**

| # | 项 | 期望 |
|---|----|------|
| 1 | 新建 | overlay 盖列表 |
| 2 | 编辑 + ✕ | 回列表且刷新 |
| 3 | Esc | 不关工作区；全屏可退 |
| 4 | dirty ✕ | 确认；取消仍留 |
| 5 | `/notes/new`、`/notes/1/edit` | overlay |
| 6 | preview 路由 | 不变 |
| 7 | ≤768 | 可关、无横溢 |

- [ ] **步骤 4：Commit**

```bash
git add docs/PAGE_SPEC.md docs/CHANGELOG.md
git commit -m "docs: 笔记全屏工作区 Overlay 契约与 changelog"
```

---

## 规格覆盖自检

| 规格项 | 任务 |
|--------|------|
| 列表托管 Overlay + Teleport | 4、5 |
| 新建/编辑不 push 旧编辑页 | 5 |
| 深链 create/edit + 清 query | 5（useDeepLinkDialog） |
| ✕ 关闭 / Esc 不关 | 2、3、4 |
| 脏确认 | 1、3 |
| 关后 fetchList | 5 |
| 新建得 id 更新 workspace | 3、5 |
| 旧路由 redirect | 6 |
| preview 不动 | 6 |
| 文档 | 7 |

无占位符；类型名统一：`Workspace`、`resolveEditorClose`、`closeMode`、`embedded`、`initialNoteId`。

---

## 执行交接

计划已保存到 `docs/superpowers/plans/2026-07-21-note-fullscreen-workspace.md`。两种执行方式：

**1. 子代理驱动（推荐）** — 每个任务调度一个新的子代理，任务间审查，快速迭代  

**2. 内联执行** — 当前会话用 executing-plans，批量执行并设检查点  

选哪种方式？

# 知识空间侧栏 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将笔记左栏升级为「知识空间」导航壳（搜索 / 首页 / 最近 / 收藏 / 我的空间），三处共用，预览只读。

**架构：** 新组件 `KnowledgeSpaceNav` 组合搜索与分段；瘦身 `NoteFolderTree` 仅渲染「我的空间」；`NoteFolderShell` 挂载新壳。数据复用 `getRecentNotes`、`getNoteList({ isFavorite: true })`、现有 folder tree API；搜索为前端过滤。

**技术栈：** Vue 3 · TypeScript · 现有 Element/UiTooltip · CSS transition（无新依赖）

**规格：** `docs/superpowers/specs/2026-07-23-knowledge-space-nav-design.md`

---

## 文件结构

| 文件 | 职责 |
|------|------|
| 创建 `personal-hub-web/src/modules/knowledge/note/knowledgeSpaceFilter.ts` | 纯函数：按 query 过滤最近/收藏/树节点 |
| 创建 `personal-hub-web/src/modules/knowledge/note/KnowledgeSpaceNav.vue` | 壳：标题、搜索、首页、最近、收藏、收起；内嵌树 |
| 修改 `NoteFolderTree.vue` | 去掉首页/全部 UI；标题区由壳负责；保留树/拖拽/CRUD；视觉升级入口 |
| 修改 `NoteFolderTreeNode.vue` | 圆角行、数量弱化、折叠 transition、hover 菜单 |
| 修改 `NoteFolderShell.vue` | 内部改挂 `KnowledgeSpaceNav`，透传 props/events |
| 修改 `List.vue` | 确认 Shell 用法；去掉依赖「全部」的文案（若有） |
| 修改 `NoteWorkspaceOverlay.vue` / `Preview.vue` | 经 Shell 自动获得新壳；预览 `readonly` |
| 修改 `docs/PAGE_SPEC.md` · `docs/CHANGELOG.md` | 同步契约 |
| 可选小测 `knowledgeSpaceFilter.spec.ts` | 若项目已有 vitest；否则用手动验收清单 |

---

### 任务 1：过滤纯函数 +（可选）单测

**文件：**
- 创建：`personal-hub-web/src/modules/knowledge/note/knowledgeSpaceFilter.ts`
- 可选测试：同目录 `knowledgeSpaceFilter.spec.ts`（仅当 `package.json` 含 vitest）

- [ ] **步骤 1：实现过滤函数**

```ts
import type { NoteFolderNoteItem, NoteFolderVO } from '@/types/note'

export type NoteSummary = { id: number; title?: string | null }

export function matchQuery(title: string | null | undefined, q: string): boolean {
  const needle = q.trim().toLowerCase()
  if (!needle) return true
  return (title ?? '').toLowerCase().includes(needle)
}

export function filterNoteSummaries<T extends NoteSummary>(list: T[], q: string): T[] {
  if (!q.trim()) return list
  return list.filter((n) => matchQuery(n.title, q))
}

/** 递归过滤夹：保留名称匹配或子孙/笔记匹配的节点 */
export function filterFolderTree(nodes: NoteFolderVO[], q: string): NoteFolderVO[] {
  if (!q.trim()) return nodes
  const out: NoteFolderVO[] = []
  for (const n of nodes) {
    const children = filterFolderTree(n.children ?? [], q)
    const notes = (n.notes ?? []).filter((note) => matchQuery(note.title, q))
    const selfMatch = matchQuery(n.name, q)
    if (selfMatch || children.length || notes.length) {
      out.push({ ...n, children, notes: selfMatch ? n.notes : notes })
    }
  }
  return out
}

export function filterUncategorizedNotes(notes: NoteFolderNoteItem[], q: string): NoteFolderNoteItem[] {
  return filterNoteSummaries(notes, q)
}
```

说明：若 `selfMatch` 为 true，保留该夹下原 `notes`（便于展开浏览）；否则只留标题命中的笔记。

- [ ] **步骤 2：若无 vitest，跳过单测；有则写 2～3 个断言并跑通**

- [ ] **步骤 3：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/knowledgeSpaceFilter.ts
git commit -m "feat(notes): 知识空间侧栏搜索过滤纯函数"
```

---

### 任务 2：`KnowledgeSpaceNav` 壳（结构 + 最近/收藏接线）

**文件：**
- 创建：`KnowledgeSpaceNav.vue`
- 参考 API：`getRecentNotes`、`getNoteList`（`personal-hub-web/src/modules/knowledge/api.ts`）
- 类型：`NoteFolderSelection`、`NoteVO`

- [ ] **步骤 1：创建壳组件 props/emits（与 Shell 对齐）**

```ts
const props = defineProps<{
  modelValue: NoteFolderSelection
  activeNoteId?: number | null
  readonly?: boolean
}>()
const emit = defineEmits<{
  'update:modelValue': [value: NoteFolderSelection]
  changed: []
  loaded: [data: NoteFolderTreeVO]
  'open-note': [id: number]
  collapse: []
}>()
```

- [ ] **步骤 2：模板骨架**

顺序：标题「知识空间」+ 收起 → 搜索 input → 首页行 → 最近列表 → 收藏列表（`v-if="favorites.length"`）→ 「我的空间」标签 +（非 readonly 时）触发树新建的入口（可先 `@click` 调树 expose 或临时隐藏 ＋，任务 3 再接）→ `<NoteFolderTree hide-chrome …>`。

- [ ] **步骤 3：`onMounted` 拉最近与收藏**

```ts
import { getRecentNotes, getNoteList } from '@/modules/knowledge/api'
import { unwrapPage } from '@/utils/apiResult'

const recent = ref<NoteVO[]>([])
const favorites = ref<NoteVO[]>([])
const query = ref('')

async function loadSideLists() {
  const [r, f] = await Promise.all([
    unwrapPage(getRecentNotes(1, 5)),
    unwrapPage(getNoteList({ page: 1, size: 10, isFavorite: true })),
  ])
  recent.value = r.list
  favorites.value = f.list
}
```

（`unwrapPage` 以项目实际返回形状为准：若为 `{ records, total }` 则取 `records`。）

- [ ] **步骤 4：计算属性套用 `filterNoteSummaries`；笔记行 `@click` → `emit('open-note', id)`；首页 → `emit('update:modelValue', 'home')`**

- [ ] **步骤 5：基础样式（折中密度）**：分区标题 11px、行 `border-radius: 6px`、padding、搜索轻边框；先不追求动画完美

- [ ] **步骤 6：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/KnowledgeSpaceNav.vue
git commit -m "feat(notes): KnowledgeSpaceNav 壳与最近/收藏"
```

---

### 任务 3：瘦身 `NoteFolderTree` 并挂入壳

**文件：**
- 修改：`NoteFolderTree.vue`
- 修改：`KnowledgeSpaceNav.vue`
- 修改：`NoteFolderShell.vue`

- [ ] **步骤 1：给树增加 props**

```ts
/** 由 KnowledgeSpaceNav 托管标题/首页时为 true：不渲染首页/全部/旧「文件夹」头 */
embeddedInSpace?: boolean
/** 外部搜索词：过滤未分类笔记与 folder list（可选；也可由壳传入已过滤 tree——优先树内 filter） */
filterQuery?: string
```

当 `embeddedInSpace`：不渲染首页、全部、旧 `folder-tree-head` 标题新建区（新建改由壳「我的空间」旁 ＋ 调用 `defineExpose` 的 `createRoot` 或保留树内 footer 外的 head 仅 ＋）。

最简：`embeddedInSpace` 时仍保留树内部「新建」按钮于「我的空间」区右侧由壳放置；树暴露：

```ts
defineExpose({ reload: loadTree, createRoot: onCreateRoot })
```

- [ ] **步骤 2：壳内**

```vue
<div class="ks-section-head">
  <span>我的空间</span>
  <button v-if="!readonly" type="button" @click="treeRef?.createRoot?.()">＋</button>
</div>
<NoteFolderTree
  ref="treeRef"
  embedded-in-space
  :filter-query="query"
  :model-value="modelValue"
  :active-note-id="activeNoteId"
  :readonly="readonly"
  @update:model-value="emit('update:modelValue', $event)"
  @open-note="emit('open-note', $event)"
  @changed="emit('changed'); loadSideLists()"
  @loaded="emit('loaded', $event)"
  @collapse="emit('collapse')"
/>
```

收起按钮只在壳标题栏；树 `embeddedInSpace` 时不再渲染自己的 collapse。

- [ ] **步骤 3：树内用 `filterQuery` + `filterFolderTree` / `filterUncategorizedNotes` 得到展示数据**

- [ ] **步骤 4：`NoteFolderShell` 把 `NoteFolderTree` 换成 `KnowledgeSpaceNav`，props/events 原样透传**

- [ ] **步骤 5：本地打开 `/notes` 目测：知识空间标题、无全部、最近/收藏有数据、树仍可选夹**

- [ ] **步骤 6：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/NoteFolderTree.vue \
  personal-hub-web/src/modules/knowledge/note/KnowledgeSpaceNav.vue \
  personal-hub-web/src/modules/knowledge/note/NoteFolderShell.vue
git commit -m "feat(notes): 知识空间壳接管导航并瘦身文件夹树"
```

---

### 任务 4：树行视觉与折叠动画

**文件：**
- 修改：`NoteFolderTree.vue`、`NoteFolderTreeNode.vue` 样式与模板

- [ ] **步骤 1：行样式**

- `border-radius: 6px`；hover 淡底；`active` 用 `color-mix(in srgb, var(--accent) 12%, transparent)`
- `.folder-count`：默认 `opacity: 0`（或 0.25），`.folder-row:hover .folder-count { opacity: 0.55 }`
- 菜单按钮：默认隐藏，行 hover / `menuOpen` 时显示（readonly 已无菜单）

- [ ] **步骤 2：折叠动画**

子树包一层：

```vue
<Transition name="ks-fold">
  <div v-show="open && hasChildren" class="folder-children">
    ...
  </div>
</Transition>
```

```css
.ks-fold-enter-active, .ks-fold-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.ks-fold-enter-from, .ks-fold-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
```

（避免对超高子树做 height auto 动画卡顿；opacity+轻微位移即可达规格「约 180ms」。）

- [ ] **步骤 3：预览页 `:deep` 主题覆盖仍适用于新 class（必要时补 `KnowledgeSpaceNav` 选择器到 `Preview.vue`）**

- [ ] **步骤 4：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/NoteFolderTree.vue \
  personal-hub-web/src/modules/knowledge/note/NoteFolderTreeNode.vue \
  personal-hub-web/src/modules/knowledge/note/KnowledgeSpaceNav.vue \
  personal-hub-web/src/modules/knowledge/note/Preview.vue
git commit -m "style(notes): 知识空间树行圆角、弱数量与折叠动效"
```

---

### 任务 5：三处验收 + 文档

**文件：**
- 修改：`docs/PAGE_SPEC.md`、`docs/CHANGELOG.md`
- 可选修订：`2026-07-22-note-home-shell-design.md` 一句「左栏以知识空间规格为准」

- [ ] **步骤 1：手动验收**

| 场景 | 期望 |
|------|------|
| `/notes` | 知识空间；无全部；搜索过滤；最近≤5；收藏空隐 |
| Overlay | 同壳；点笔记切换（脏确认） |
| Preview | 同壳只读；无＋/拖拽/⋯；主题色统一 |
| 拖拽夹/笔记（列表） | 不回归 |

- [ ] **步骤 2：更新 PAGE_SPEC 笔记条目：左栏「知识空间」结构简述**

- [ ] **步骤 3：CHANGELOG 增加 2026-07-23 知识空间条目**

- [ ] **步骤 4：`pnpm exec vue-tsc --noEmit -p tsconfig.app.json`（在 `personal-hub-web`）通过**

- [ ] **步骤 5：Commit**

```bash
git add docs/PAGE_SPEC.md docs/CHANGELOG.md docs/superpowers/specs/2026-07-23-knowledge-space-nav-design.md
git commit -m "docs: 知识空间侧栏规格与 PAGE_SPEC/CHANGELOG"
```

（若规格已单独 commit，本步只提交文档增量。）

---

## 规格覆盖自检

| 规格需求 | 任务 |
|----------|------|
| 知识空间标题 / 无全部 | 2、3 |
| 内联搜索 | 1、2、3 |
| 最近 / 收藏 | 2 |
| 我的空间树 + 未分类 | 3（现状保留） |
| 视觉 / 动画 / 弱数量 | 4 |
| 三处 + preview readonly | 3、5 |
| 不新增后端表 | 全程复用 API |
| PAGE_SPEC / CHANGELOG | 5 |

---

## 执行交接

计划已保存到 `docs/superpowers/plans/2026-07-23-knowledge-space-nav.md`。

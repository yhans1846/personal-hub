# 笔记编辑器三栏布局 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** Editor 默认左编辑 / 中实时预览 / 右大纲；顶栏标题 + 属性抽屉；保留 Overlay 脏关与 Hub Token。

**架构：** 扩展 `EditorMode` 为 `split|edit|preview`（默认 `split`）；Header 承载标题与三模式；meta 迁入抽屉；分栏同时挂载 Vditor 与 Preview；大纲复用 `parseToc` + `PreviewToc`。

**技术栈：** Vue 3 · 既有 Vditor / NoteMarkdownPreview · Element Plus Drawer · Vitest

**规格：** `docs/superpowers/specs/2026-07-21-note-split-editor-design.md`

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `editor/useEditorMode.ts` | `split\|edit\|preview`；默认 split；≤768 默认 edit；Ctrl+Shift+P = split↔preview；`setMode` |
| `editor/debounceRef.ts` + test | 预览用 debounce（≤200ms）纯逻辑 |
| `editor/EditorOutline.vue` | 右栏大纲：parseToc + PreviewToc，emit scroll-to |
| `editor/EditorPropsDrawer.vue` | 分类/标签/只读 meta；v-model visible；Esc 关抽屉 |
| `editor/EditorHeader.vue` | 标题 v-model；属性按钮；三模式按钮；保留 ✕/收藏等 |
| `editor/EditorStatusBar.vue` | mode 文案含「分屏」 |
| `editor/NoteMarkdownPreview.vue` | 分屏 `showToc=false`；expose `scrollToHeading` |
| `Editor.vue` | 三栏布局接线 |
| `docs/PAGE_SPEC.md` · `CHANGELOG.md` | 契约 |

---

### 任务 1：useEditorMode 支持 split

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/editor/useEditorMode.ts`
- 测试：`personal-hub-web/src/modules/knowledge/note/editor/__tests__/useEditorMode.split.test.ts`

- [ ] **步骤 1：写失败测试**（抽可测逻辑到同文件导出的 helpers，或测返回 API）

因 composable 依赖 window，优先导出纯函数：

```ts
// 可放在 useEditorMode.ts 底部导出，或 debounce 旁的 modeCycle.ts
export type EditorMode = 'edit' | 'preview' | 'split'

export function nextPreviewToggle(mode: EditorMode): EditorMode {
  // Ctrl+Shift+P：split ↔ preview；若当前 edit 则进入 preview
  if (mode === 'preview') return 'split'
  return 'preview'
}

export function initialEditorMode(viewportWidth: number): EditorMode {
  return viewportWidth <= 768 ? 'edit' : 'split'
}
```

测试：

```ts
import { describe, it, expect } from 'vitest'
import { nextPreviewToggle, initialEditorMode } from '../useEditorMode'

describe('editor mode helpers', () => {
  it('toggle preview from split → preview', () => {
    expect(nextPreviewToggle('split')).toBe('preview')
  })
  it('toggle preview from preview → split', () => {
    expect(nextPreviewToggle('preview')).toBe('split')
  })
  it('toggle from edit → preview', () => {
    expect(nextPreviewToggle('edit')).toBe('preview')
  })
  it('mobile initial edit', () => {
    expect(initialEditorMode(375)).toBe('edit')
  })
  it('desktop initial split', () => {
    expect(initialEditorMode(1280)).toBe('split')
  })
})
```

- [ ] **步骤 2：跑测确认失败**

```bash
cd personal-hub-web
npx vitest run src/modules/knowledge/note/editor/__tests__/useEditorMode.split.test.ts
```

- [ ] **步骤 3：实现 helpers + 改造 composable**

```ts
export type EditorMode = 'edit' | 'preview' | 'split'

export function nextPreviewToggle(mode: EditorMode): EditorMode {
  if (mode === 'preview') return 'split'
  return 'preview'
}

export function initialEditorMode(viewportWidth: number): EditorMode {
  return viewportWidth <= 768 ? 'edit' : 'split'
}

export function useEditorMode() {
  const width = typeof window !== 'undefined' ? window.innerWidth : 1280
  const mode = ref<EditorMode>(initialEditorMode(width))
  // ...
  function togglePreview() {
    mode.value = nextPreviewToggle(mode.value)
  }
  function setMode(m: EditorMode) {
    mode.value = m
  }
  // handleKeydown Ctrl+Shift+P → togglePreview()
  return { mode, isFullscreen, togglePreview, setMode, toggleFullscreen, exitFullscreen }
}
```

删除旧的 `edit↔preview` 互斥假设；状态栏等后续任务适配 `split`。

- [ ] **步骤 4：跑测通过**

- [ ] **步骤 5：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/editor/useEditorMode.ts \
  personal-hub-web/src/modules/knowledge/note/editor/__tests__/useEditorMode.split.test.ts
git commit -m "feat(notes): 编辑模式支持 split 与快捷键切换"
```

---

### 任务 2：预览 debounce 工具

**文件：**
- 创建：`personal-hub-web/src/modules/knowledge/note/editor/debounceValue.ts`
- 测试：`personal-hub-web/src/modules/knowledge/note/editor/__tests__/debounceValue.test.ts`

- [ ] **步骤 1：失败测试**

```ts
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref, nextTick } from 'vue'
import { useDebouncedRef } from '../debounceValue'

describe('useDebouncedRef', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('updates after delay', async () => {
    const source = ref('a')
    const debounced = useDebouncedRef(source, 150)
    expect(debounced.value).toBe('a')
    source.value = 'b'
    expect(debounced.value).toBe('a')
    vi.advanceTimersByTime(150)
    await nextTick()
    expect(debounced.value).toBe('b')
  })
})
```

- [ ] **步骤 2：跑测失败 → 实现**

```ts
import { ref, watch, type Ref } from 'vue'

export function useDebouncedRef<T>(source: Ref<T>, delayMs: number): Ref<T> {
  const out = ref(source.value) as Ref<T>
  let timer: ReturnType<typeof setTimeout> | undefined
  watch(source, (v) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => { out.value = v }, delayMs)
  })
  return out
}
```

- [ ] **步骤 3：跑测通过并 Commit**

```bash
git commit -m "feat(notes): 预览内容 debounce 工具"
```

---

### 任务 3：EditorOutline

**文件：**
- 创建：`personal-hub-web/src/modules/knowledge/note/editor/EditorOutline.vue`

- [ ] **步骤 1：实现**

```vue
<script setup lang="ts">
import { computed } from 'vue'
import PreviewToc from '../preview/PreviewToc.vue'
import { parseTocFromMarkdown } from './parseToc'

const props = defineProps<{ content: string; activeId?: string }>()
const emit = defineEmits<{ 'scroll-to': [id: string] }>()

const items = computed(() => parseTocFromMarkdown(props.content))
</script>

<template>
  <aside class="editor-outline" aria-label="大纲">
    <p v-if="items.length === 0" class="outline-empty">暂无标题</p>
    <PreviewToc
      v-else
      :items="items"
      :active-id="activeId ?? ''"
      @scroll-to="emit('scroll-to', $event)"
    />
  </aside>
</template>

<style scoped>
.editor-outline {
  height: 100%;
  overflow: auto;
  border-left: 1px solid var(--border);
  background: var(--bg-body);
  padding: var(--sp-8);
}
.outline-empty {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: var(--sp-12);
}
/* 让 PreviewToc 占满右栏，忽略其内部拖拽宽 */
.editor-outline :deep(.toc-wrapper) {
  width: 100% !important;
}
</style>
```

注意：现有 `NoteMarkdownPreview` 里若写 `@select`，应统一为 `@scroll-to`（PreviewToc 实际事件名）；本任务新建 Outline 用 `@scroll-to`。

- [ ] **步骤 2：Commit**

```bash
git commit -m "feat(notes): 编辑器右侧大纲栏 EditorOutline"
```

---

### 任务 4：EditorPropsDrawer

**文件：**
- 创建：`personal-hub-web/src/modules/knowledge/note/editor/EditorPropsDrawer.vue`

- [ ] **步骤 1：实现**（ElDrawer，`close-on-press-escape` 默认 true，不关 Overlay）

```vue
<script setup lang="ts">
import type { CategoryVO } from '@/types/category'
import type { TagVO } from '@/types/tag'

defineProps<{
  visible: boolean
  categoryIds: number[]
  tagIds: number[]
  categories: CategoryVO[]
  tags: TagVO[]
  createdAtText?: string
  readingTimeText?: string
}>()

const emit = defineEmits<{
  'update:visible': [boolean]
  'update:categoryIds': [number[]]
  'update:tagIds': [number[]]
}>()
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="笔记属性"
    direction="rtl"
    size="320px"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="props-field">
      <label>分类</label>
      <el-select
        :model-value="categoryIds"
        multiple
        placeholder="选择分类"
        style="width: 100%"
        @update:model-value="emit('update:categoryIds', $event)"
      >
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </div>
    <div class="props-field">
      <label>标签</label>
      <el-select
        :model-value="tagIds"
        multiple
        placeholder="选择标签"
        style="width: 100%"
        @update:model-value="emit('update:tagIds', $event)"
      >
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
    </div>
    <p v-if="createdAtText" class="props-static">创建 {{ createdAtText }}</p>
    <p v-if="readingTimeText" class="props-static">阅读 {{ readingTimeText }}</p>
  </el-drawer>
</template>
```

样式用 Token 间距即可。

- [ ] **步骤 2：Commit**

```bash
git commit -m "feat(notes): 笔记属性抽屉 EditorPropsDrawer"
```

---

### 任务 5：EditorHeader 标题与三模式

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/editor/EditorHeader.vue`
- 修改：`EditorStatusBar.vue`（modeLabel 增加 split）

- [ ] **步骤 1：Header props/emits**

```ts
defineProps<{
  saveStatus: SaveStatus
  isFavorite: boolean
  mode: EditorMode
  isFullscreen?: boolean
  closeMode?: 'back' | 'close'
  title: string
}>()

const emit = defineEmits<{
  back: []
  'update:title': [string]
  openProps: []
  'update:mode': [EditorMode]  // 或 setMode
  toggleFavorite: []
  toggleFullscreen: []
  exportNote: []
  remove: []
}>()
```

模板中部：

```vue
<input
  class="header-title"
  :value="title"
  placeholder="请输入标题..."
  @input="emit('update:title', ($event.target as HTMLInputElement).value)"
/>
```

模式按钮组（示例）：

```vue
<div class="mode-group">
  <button :class="{ active: mode === 'edit' }" title="仅编辑" @click="emit('update:mode', 'edit')">…</button>
  <button :class="{ active: mode === 'split' }" title="分屏" @click="emit('update:mode', 'split')">…</button>
  <button :class="{ active: mode === 'preview' }" title="仅预览" @click="emit('update:mode', 'preview')">…</button>
</div>
<button class="header-btn" @click="emit('openProps')">属性</button>
```

移除旧的单一「预览/编辑」toggle（由三按钮替代）。图标可用 `Edit3` / `Columns2` / `Eye`（lucide）。

- [ ] **步骤 2：StatusBar**

```ts
case 'split': return '分屏模式'
```

- [ ] **步骤 3：Commit**

```bash
git commit -m "feat(notes): 编辑头支持标题与分屏模式切换"
```

---

### 任务 6：NoteMarkdownPreview 暴露跳转

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/editor/NoteMarkdownPreview.vue`

- [ ] **步骤 1：**

```ts
defineExpose({ scrollEl: scrollRef, scrollToHeading })
```

确认内嵌 TOC 使用 `@scroll-to="scrollToHeading"`（若仍为 `@select` 一并改掉）。

分屏由父级传 `:show-toc="false"`、`:show-meta="false"`（meta 已在抽屉）。

- [ ] **步骤 2：Commit**

```bash
git commit -m "fix(notes): 预览暴露 scrollToHeading 并修正 TOC 事件"
```

---

### 任务 7：Editor.vue 三栏接线

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/Editor.vue`

- [ ] **步骤 1：状态与 debounce**

```ts
const propsOpen = ref(false)
const previewRef = ref<InstanceType<typeof NoteMarkdownPreview> | null>(null)
const contentForPreview = useDebouncedRef(computed(() => form.value.content), 150)
// 若 useDebouncedRef 要 Ref：用 toRef(form.value, 'content') 不便，可：
const contentSource = computed(() => form.value.content)
const debouncedContent = useDebouncedRef(
  // 实现若只接受 Ref，则：
  // const contentRef = ref(form.value.content); watch(() => form.value.content, v => contentRef.value = v)
)
```

推荐实现方式：

```ts
const contentRef = computed(() => form.value.content)
// 扩展 debounceValue：接受 ComputedRef，或在 Editor 内：
const rawContent = ref(form.value.content)
watch(() => form.value.content, (v) => { rawContent.value = v })
const debouncedContent = useDebouncedRef(rawContent, 150)
```

`const { mode, setMode, togglePreview, ... } = useEditorMode()`

- [ ] **步骤 2：模板结构**

```vue
<EditorHeader
  :title="form.title"
  :mode="mode"
  ...
  @update:title="form.title = $event"
  @update:mode="setMode"
  @open-props="propsOpen = true"
/>
<EditorPropsDrawer
  v-model:visible="propsOpen"
  v-model:category-ids="form.categoryIds"
  v-model:tag-ids="form.tagIds"
  :categories="categories"
  :tags="tags"
  :created-at-text="createdAt ? formatRelativeTime(createdAt) : ''"
  :reading-time-text="readingTimeText"
/>

<div class="editor-split" :class="[`mode-${mode}`]">
  <div v-show="mode === 'edit' || mode === 'split'" class="pane pane-editor">
    <NoteVditor v-if="!initialLoading && (mode === 'edit' || mode === 'split')" ... />
  </div>
  <div v-show="mode === 'preview' || mode === 'split'" class="pane pane-preview">
    <NoteMarkdownPreview
      v-if="!initialLoading && (mode === 'preview' || mode === 'split')"
      ref="previewRef"
      :content="debouncedContent"
      :title="form.title"
      :show-toc="false"
      :show-meta="false"
      ...
    />
  </div>
  <div v-show="mode === 'split' || mode === 'preview'" class="pane pane-outline">
    <EditorOutline
      :content="debouncedContent"
      @scroll-to="(id) => previewRef?.scrollToHeading(id)"
    />
  </div>
</div>
```

删除正文上方旧 `editor-title` 与 `editor-meta` 块。

- [ ] **步骤 3：CSS**

```css
.editor-split {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}
.pane-editor { flex: 0 0 38%; min-width: 0; overflow: hidden; }
.pane-preview { flex: 0 0 42%; min-width: 0; overflow: hidden; }
.pane-outline { flex: 0 0 20%; min-width: 0; overflow: hidden; }
.mode-edit .pane-editor { flex: 1; }
.mode-preview .pane-preview { flex: 1; }
.mode-preview .pane-outline { flex: 0 0 20%; }
@media (max-width: 768px) {
  .editor-split { flex-direction: column; }
  .pane-editor, .pane-preview, .pane-outline { flex: 1 1 auto; width: 100%; }
  /* 小屏由 mode=edit 默认；大纲可后续用抽屉，v1 用 v-show 随 mode 即可 */
}
```

确保 `.editor-page` 仍为 column flex + `height:100%`（Overlay 内）。

- [ ] **步骤 4：手动冒烟清单**（开发服）

按规格验收 1–7；确认 Esc 不关 Overlay、可关属性抽屉。

- [ ] **步骤 5：Commit**

```bash
git commit -m "feat(notes): Editor 默认三栏分屏工作区"
```

---

### 任务 8：文档

**文件：**
- `docs/PAGE_SPEC.md`
- `docs/CHANGELOG.md`

- [ ] **步骤 1：** PAGE_SPEC 笔记条补充：默认三栏（编/预/大纲）；顶栏标题；属性抽屉；模式 split/edit/preview。

- [ ] **步骤 2：** CHANGELOG 2026-07-21 增加功能句。

- [ ] **步骤 3：Commit**

```bash
git commit -m "docs: 笔记三栏编辑器契约与 changelog"
```

---

## 规格覆盖自检

| 规格 | 任务 |
|------|------|
| 三栏默认 split | 1、7 |
| 右大纲 | 3、7 |
| Hub Token | 全任务无米色皮肤 |
| 顶栏标题 + 属性抽屉 | 4、5、7 |
| debounce ≤200ms | 2、7 |
| Ctrl+Shift+P split↔preview | 1 |
| ≤768 默认 edit | 1 |
| 分屏关内嵌 TOC | 6、7 |
| 独立预览页不改 | — |
| Esc 不关 Overlay / 可关抽屉 | 4、7 |
| 文档 | 8 |

类型名统一：`EditorMode`、`setMode`、`nextPreviewToggle`、`useDebouncedRef`、`EditorOutline`、`EditorPropsDrawer`。

---

## 执行交接

计划已保存到 `docs/superpowers/plans/2026-07-21-note-split-editor.md`。两种执行方式：

**1. 子代理驱动（推荐）** — 每任务新子代理 + 审查  

**2. 内联执行** — 本会话按计划推进  

选哪种？

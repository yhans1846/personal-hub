<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoteById, toggleFavorite, deleteNote } from '@/modules/knowledge/api'
import { getCategories } from '@/modules/knowledge/api'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAutoSave } from './editor/useAutoSave'
import { useEditorMode } from './editor/useEditorMode'
import { useImageUpload } from './editor/useImageUpload'
import EditorHeader from './editor/EditorHeader.vue'
import EditorStatusBar from './editor/EditorStatusBar.vue'
import EditorOutline from './editor/EditorOutline.vue'
import EditorPropsDrawer from './editor/EditorPropsDrawer.vue'
import NoteVditor from './editor/NoteVditor.vue'
import NoteMarkdownPreview from './editor/NoteMarkdownPreview.vue'
import NoteBacklinks from './NoteBacklinks.vue'
import { buildEditorId } from './editor/vditorSetup'
import { useDebouncedRef } from './editor/debounceValue'
import { useFeatureFlagStore } from '@/store/featureFlagStore'
import { useMainContentEditor } from '@/composables/useMainContentFill'
import { estimateReadingTime, formatRelativeTime } from '@/utils/readingTime'
import type { CategoryVO } from '@/types/category'
import type { TagVO } from '@/types/tag'
import type { CategoryItem, TagItem } from '@/types/note'
import { resolveEditorClose, type CloseConfirmChoice } from './editor/requestEditorClose'
import { bindSyncScroll } from './editor/syncScroll'

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

const route = useRoute()
const router = useRouter()
const featureFlags = useFeatureFlagStore()
if (!props.embedded) {
  useMainContentEditor()
}

const resolvedNoteId = computed(() => {
  if (props.initialNoteId != null) return props.initialNoteId
  if (!props.embedded && route.params.id != null && route.params.id !== '') {
    return Number(route.params.id)
  }
  return undefined
})
const isEdit = computed(() => resolvedNoteId.value != null)

const form = ref({ title: isEdit.value ? '' : '未命名笔记', content: '', categoryIds: [] as number[], tagIds: [] as number[] })
const categories = ref<CategoryVO[]>([])
const tags = ref<TagVO[]>([])
const isFavorite = ref(false)
const createdAt = ref('')
const initialLoading = ref(true)

const editorTheme = computed(() =>
  document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light',
)

const {
  status: saveStatus,
  noteId,
  lastSavedAt,
  save: forceSave,
  restoreDraft,
  clearDraft,
  markReady,
} = useAutoSave(form, resolvedNoteId.value)

/** 加载阶段是否因草稿与服务器不一致而需要 dirty */
const hydrationDirty = ref(false)
let hydrationDone = false

const editorId = computed(() => buildEditorId(noteId.value))

const {
  mode,
  isFullscreen,
  setMode,
  toggleFullscreen,
} = useEditorMode()

const propsOpen = ref(false)

const vditorPaneRef = ref<{ getScrollEl: () => HTMLElement | null } | null>(null)
const previewRef = ref<{
  scrollToHeading: (id: string) => void
  scrollEl?: { value: HTMLElement | null } | HTMLElement | null
} | null>(null)

const rawContent = ref(form.value.content)
watch(() => form.value.content, (v) => { rawContent.value = v })
const debouncedContent = useDebouncedRef(rawContent, 150)

let unbindSyncScroll: (() => void) | null = null

function resolvePreviewScrollEl(): HTMLElement | null {
  const exposed = previewRef.value?.scrollEl as unknown
  if (!exposed) return null
  if (exposed instanceof HTMLElement) return exposed
  if (typeof exposed === 'object' && exposed !== null && 'value' in exposed) {
    return (exposed as { value: HTMLElement | null }).value
  }
  return null
}

function setupSplitScrollSync() {
  unbindSyncScroll?.()
  unbindSyncScroll = null
  if (mode.value !== 'split') return
  const left = vditorPaneRef.value?.getScrollEl?.() ?? null
  const right = resolvePreviewScrollEl()
  if (!left || !right) return
  unbindSyncScroll = bindSyncScroll(left, right)
}

watch(
  () => [mode.value, initialLoading.value] as const,
  async () => {
    await nextTick()
    // 预览重渲染后高度变化，稍后再绑
    requestAnimationFrame(() => setupSplitScrollSync())
  },
)

watch(debouncedContent, async () => {
  if (mode.value !== 'split') return
  await nextTick()
  requestAnimationFrame(() => setupSplitScrollSync())
})

onBeforeUnmount(() => {
  unbindSyncScroll?.()
  unbindSyncScroll = null
})

const { handleUpload } = useImageUpload(noteId, forceSave)

onMounted(async () => {
  let shouldMarkDirty = false
  try {
    const [catRes, tagRes] = await Promise.all([getCategories('note'), getTags()])
    categories.value = catRes.data.data
    tags.value = tagRes.data.data
  } catch {
    ElMessage.error('加载分类/标签失败')
  }

  if (isEdit.value) {
    try {
      const id = resolvedNoteId.value as number
      const res = await getNoteById(id)
      const note = res.data.data
      form.value.title = note.title
      form.value.content = note.content
      form.value.categoryIds = note.categories.map((c: CategoryItem) => c.id)
      form.value.tagIds = note.tags.map((t: TagItem) => t.id)
      isFavorite.value = note.isFavorite === 1
      createdAt.value = note.createdAt

      const draftKey = `draft_note_${id}`
      const draft = localStorage.getItem(draftKey)
      if (draft) {
        try {
          const d = JSON.parse(draft)
          if (d.content && d.content !== note.content) {
            form.value.title = d.title || note.title
            form.value.content = d.content
            form.value.categoryIds = d.categoryIds || form.value.categoryIds
            form.value.tagIds = d.tagIds || form.value.tagIds
            shouldMarkDirty = true
            ElMessage.info('已恢复本地草稿')
          }
        } catch { /* ignore */ }
      }
    } catch {
      ElMessage.error('加载笔记失败')
    }
  } else if (restoreDraft()) {
    shouldMarkDirty = true
    ElMessage.info('已恢复未保存的草稿')
  }

  hydrationDirty.value = shouldMarkDirty
  initialLoading.value = false
  await nextTick()
  // 纯预览态无 Vditor，直接建立基线；编辑/分屏态等 Vditor @ready（避免编辑器初始化归一化误标 dirty）
  if (mode.value === 'preview') {
    markReady(hydrationDirty.value)
    hydrationDone = true
  }
})

function onVditorReady() {
  if (!hydrationDone) {
    nextTick(() => {
      markReady(hydrationDirty.value)
      hydrationDone = true
    })
  }
  nextTick(() => {
    requestAnimationFrame(() => setupSplitScrollSync())
  })
}

async function handleToggleFavorite() {
  if (!noteId.value) await forceSave()
  if (!noteId.value) return
  try {
    await toggleFavorite(noteId.value)
    isFavorite.value = !isFavorite.value
  } catch {
    ElMessage.error('操作失败')
  }
}

function handleExport() {
  const title = form.value.title || '无标题笔记'
  const content = `# ${title}\n\n${form.value.content || ''}`
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${title}.md`
  a.click()
  URL.revokeObjectURL(url)
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除这篇笔记？删除后可在回收站恢复。', '删除笔记', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    if (noteId.value) await deleteNote(noteId.value)
    clearDraft()
    ElMessage.success('笔记已删除')
    if (props.embedded) emit('close')
    else router.push('/notes')
  } catch { /* 取消 */ }
}

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

watch(noteId, (id) => {
  if (id != null) emit('note-id', id)
})

function onUploadImg(files: File[], callback: (urls: string[]) => void) {
  handleUpload(files, callback)
}

const readingTimeText = computed(() => estimateReadingTime(form.value.content))
</script>

<template>
  <div
    class="editor-page"
    :class="{
      'is-fullscreen': isFullscreen,
    }"
  >
    <EditorHeader
      :close-mode="embedded ? 'close' : 'back'"
      :title="form.title"
      :save-status="saveStatus"
      :is-favorite="isFavorite"
      :mode="mode"
      :is-fullscreen="isFullscreen"
      @back="handleBack"
      @update:title="(v) => (form.title = v)"
      @update:mode="setMode"
      @open-props="propsOpen = true"
      @toggle-favorite="handleToggleFavorite"
      @toggle-fullscreen="toggleFullscreen"
      @export-note="handleExport"
      @remove="handleDelete"
    />

    <div class="editor-body">
      <div class="editor-split" :class="`mode-${mode}`">
        <div v-if="initialLoading" class="editor-loading" aria-busy="true" aria-label="加载笔记中">
          <div class="editor-loading-bar title" />
          <div class="editor-loading-bar meta" />
          <div class="editor-loading-bar line" />
          <div class="editor-loading-bar line short" />
          <div class="editor-loading-bar line" />
        </div>

        <template v-else>
          <div
            v-show="mode === 'edit' || mode === 'split'"
            class="pane pane-editor"
          >
            <NoteVditor
              v-if="mode === 'edit' || mode === 'split'"
              ref="vditorPaneRef"
              v-model="form.content"
              compact
              :editor-id="editorId"
              :theme="editorTheme"
              :note-id="noteId"
              :on-upload-img="onUploadImg"
              class="editor-instance"
              @ready="onVditorReady"
            />
          </div>

          <div
            v-show="mode === 'preview' || mode === 'split'"
            class="pane pane-preview"
          >
            <NoteMarkdownPreview
              v-if="mode === 'preview' || mode === 'split'"
              ref="previewRef"
              :content="debouncedContent"
              :title="form.title"
              :show-toc="false"
              :show-meta="false"
              :theme="editorTheme"
              :editor-id="`${editorId}-preview`"
              :note-id="noteId"
            />
          </div>

          <div
            v-show="mode === 'split' || mode === 'preview'"
            class="pane pane-outline"
          >
            <EditorOutline
              :content="debouncedContent"
              @scroll-to="(id) => previewRef?.scrollToHeading?.(id)"
            />
          </div>
        </template>
      </div>

      <NoteBacklinks
        v-if="!initialLoading && noteId && featureFlags.isEnabled('backlink')"
        class="editor-backlinks"
        :note-id="noteId"
        :enabled="true"
      />
    </div>

    <EditorPropsDrawer
      v-model:visible="propsOpen"
      v-model:category-ids="form.categoryIds"
      v-model:tag-ids="form.tagIds"
      :categories="categories"
      :tags="tags"
      :created-at-text="createdAt ? formatRelativeTime(createdAt) : ''"
      :reading-time-text="readingTimeText"
    />

    <EditorStatusBar
      :content="form.content"
      :mode="mode"
      :save-status="saveStatus"
      :last-saved-at="lastSavedAt"
      :is-fullscreen="isFullscreen"
    />
  </div>
</template>

<style scoped>
.editor-backlinks {
  margin-top: var(--sp-8);
  padding: 0 24px 32px;
  flex-shrink: 0;
}
.editor-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-body);
  transition: background var(--transition-duration);
}
.editor-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}
.editor-split {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}
.pane {
  min-width: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.pane-editor {
  flex: 0 0 38%;
  /* 仅 IR 内滚动，便于与预览比例联动 */
  overflow: hidden;
}
.pane-preview { flex: 0 0 42%; }
.pane-outline { flex: 0 0 20%; }
.mode-edit .pane-editor { flex: 1; }
.mode-preview .pane-preview { flex: 1; }
.mode-preview .pane-outline { flex: 0 0 20%; }
.editor-instance {
  flex: 1;
  height: 100%;
  min-height: 0;
}
.editor-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 48px 24px;
}
.editor-loading-bar {
  height: 14px;
  border-radius: var(--radius-sm);
  background: linear-gradient(
    90deg,
    var(--bg-hover) 25%,
    color-mix(in srgb, var(--bg-hover) 60%, var(--bg-card)) 50%,
    var(--bg-hover) 75%
  );
  background-size: 200% 100%;
  animation: editor-skeleton 1.2s ease-in-out infinite;
}
.editor-loading-bar.title {
  height: 28px;
  width: 55%;
  margin-bottom: 8px;
}
.editor-loading-bar.meta {
  width: 36%;
  height: 12px;
}
.editor-loading-bar.line {
  width: 100%;
}
.editor-loading-bar.line.short {
  width: 68%;
}
@keyframes editor-skeleton {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
@media (max-width: 768px) {
  .pane-editor,
  .pane-preview,
  .pane-outline {
    flex: 1 1 auto;
    width: 100%;
  }
  .editor-split {
    flex-direction: column;
  }
}
</style>

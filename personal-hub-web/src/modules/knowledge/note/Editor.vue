<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoteById, toggleFavorite, deleteNote } from '@/modules/knowledge/api'
import { getCategories } from '@/api/categoryApi'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAutoSave } from './editor/useAutoSave'
import { useEditorMode } from './editor/useEditorMode'
import { useImageUpload } from './editor/useImageUpload'
import EditorHeader from './editor/EditorHeader.vue'
import EditorStatusBar from './editor/EditorStatusBar.vue'
import NoteVditor from './editor/NoteVditor.vue'
import NoteMarkdownPreview from './editor/NoteMarkdownPreview.vue'
import { buildEditorId } from './editor/vditorSetup'
import { estimateReadingTime, formatRelativeTime } from '@/utils/readingTime'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({ title: isEdit ? '' : '未命名笔记', content: '', categoryIds: [] as number[], tagIds: [] as number[] })
const categories = ref<any[]>([])
const tags = ref<any[]>([])
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
} = useAutoSave(form, isEdit ? Number(route.params.id) : undefined)

/** 加载阶段是否因草稿与服务器不一致而需要 dirty */
const hydrationDirty = ref(false)
let hydrationDone = false

const editorId = computed(() => buildEditorId(noteId.value))

const {
  mode,
  isFullscreen,
  togglePreview,
  toggleFocus,
  toggleFullscreen,
} = useEditorMode()

watch(mode, (val) => {
  if (val === 'focus') {
    document.body.classList.add('editor-focus-mode')
  } else {
    document.body.classList.remove('editor-focus-mode')
  }
})
onUnmounted(() => {
  document.body.classList.remove('editor-focus-mode')
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

  if (isEdit) {
    try {
      const res = await getNoteById(Number(route.params.id))
      const note = res.data.data
      form.value.title = note.title
      form.value.content = note.content
      form.value.categoryIds = note.categories.map((c: any) => c.id)
      form.value.tagIds = note.tags.map((t: any) => t.id)
      isFavorite.value = note.isFavorite === 1
      createdAt.value = note.createdAt

      const draftKey = `draft_note_${route.params.id}`
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
  // 预览态无 Vditor，直接建立基线；编辑态等 @ready（避免编辑器初始化归一化误标 dirty）
  if (mode.value === 'preview') {
    markReady(hydrationDirty.value)
    hydrationDone = true
  }
})

function onVditorReady() {
  if (hydrationDone) return
  nextTick(() => {
    markReady(hydrationDirty.value)
    hydrationDone = true
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
    router.push('/notes')
  } catch { /* 取消 */ }
}

function handleBack() {
  router.push('/notes')
}

function onUploadImg(files: File[], callback: (urls: string[]) => void) {
  handleUpload(files, callback)
}

function getCategoryNames(): string {
  return form.value.categoryIds
    .map(id => categories.value.find((c: any) => c.id === id)?.name)
    .filter(Boolean)
    .join('、')
}

function getTagNames(): string {
  return form.value.tagIds
    .map(id => tags.value.find((t: any) => t.id === id)?.name)
    .filter(Boolean)
    .join('、')
}

const savedTimeText = computed(() => {
  if (!lastSavedAt.value) return ''
  return formatRelativeTime(new Date(lastSavedAt.value).toISOString())
})
const readingTimeText = computed(() => estimateReadingTime(form.value.content))
</script>

<template>
  <div
    class="editor-page"
    :class="{
      'is-focus': mode === 'focus',
      'is-fullscreen': isFullscreen,
    }"
  >
    <EditorHeader
      :save-status="saveStatus"
      :is-favorite="isFavorite"
      :mode="mode"
      :is-fullscreen="isFullscreen"
      :is-focus-mode="mode === 'focus'"
      @back="handleBack"
      @toggle-favorite="handleToggleFavorite"
      @toggle-mode="togglePreview"
      @toggle-focus="toggleFocus"
      @toggle-fullscreen="toggleFullscreen"
      @export-note="handleExport"
      @remove="handleDelete"
    />

    <div
      class="editor-body"
      :class="{
        'is-focus': mode === 'focus',
        'is-fullscreen': isFullscreen,
        'is-preview': mode === 'preview',
      }"
    >
      <div class="editor-content-wrap" :class="{ 'is-focus-content': mode === 'focus' }">
        <input
          v-if="mode !== 'preview'"
          v-model="form.title"
          class="editor-title"
          placeholder="请输入标题..."
        />

        <div v-if="mode === 'edit' && !initialLoading" class="editor-meta">
          <el-popover trigger="click" placement="bottom" :width="280">
            <template #reference>
              <span class="meta-item">
                📂 {{ form.categoryIds.length ? getCategoryNames() : '分类' }}
              </span>
            </template>
            <el-select v-model="form.categoryIds" multiple placeholder="选择分类" style="width: 100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-popover>

          <el-popover trigger="click" placement="bottom" :width="280">
            <template #reference>
              <span class="meta-item">
                🏷 {{ form.tagIds.length ? getTagNames() : '标签' }}
              </span>
            </template>
            <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width: 100%">
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-popover>

          <span v-if="createdAt" class="meta-item static">🕒 {{ formatRelativeTime(createdAt) }}</span>
          <span v-if="readingTimeText" class="meta-item static">👁 {{ readingTimeText }}</span>
        </div>

        <div v-if="mode === 'edit'" class="editor-divider" />

        <div v-if="initialLoading" class="editor-loading" aria-busy="true" aria-label="加载笔记中">
          <div class="editor-loading-bar title" />
          <div class="editor-loading-bar meta" />
          <div class="editor-loading-bar line" />
          <div class="editor-loading-bar line short" />
          <div class="editor-loading-bar line" />
        </div>

        <NoteVditor
          v-else-if="mode === 'edit' || mode === 'focus'"
          v-model="form.content"
          :editor-id="editorId"
          :theme="editorTheme"
          :on-upload-img="onUploadImg"
          :class="{ 'editor-instance': true, 'focus-editor': mode === 'focus' }"
          @ready="onVditorReady"
        />

        <NoteMarkdownPreview
          v-if="!initialLoading && mode === 'preview'"
          :content="form.content"
          :title="form.title"
          :theme="editorTheme"
          :editor-id="`${editorId}-preview`"
          :note-id="noteId"
          :reading-time-text="readingTimeText"
          :category-names="getCategoryNames()"
          :tag-names="getTagNames()"
          :saved-time-text="savedTimeText"
        />
      </div>
    </div>

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
.editor-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-body);
  transition: background 0.2s;
}
.editor-body {
  flex: 1;
  overflow-y: auto;
  padding: 48px 0 32px;
  transition: padding var(--transition);
}
.editor-body.is-focus {
  padding: 0;
}
.editor-body.is-fullscreen,
.editor-body.is-preview {
  padding: 0;
}
.editor-content-wrap {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 48px;
}
.editor-content-wrap.is-focus-content {
  max-width: 1000px;
  padding: 0 48px;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.editor-page.is-focus .editor-content-wrap {
  max-width: none;
  height: 100%;
}
.editor-title {
  width: 100%;
  border: none;
  outline: none;
  font-size: 36px;
  font-weight: 600;
  color: var(--text-primary);
  background: transparent;
  padding: 0;
  margin-bottom: 20px;
  font-family: var(--font-sans);
  line-height: 1.3;
  letter-spacing: -0.02em;
}
.editor-title::placeholder {
  color: var(--text-placeholder);
}
.editor-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  transition: background var(--transition);
  font-size: var(--text-sm);
}
.meta-item:hover {
  background: var(--bg-hover);
  color: var(--text-secondary);
}
.meta-item.static {
  cursor: default;
}
.meta-item.static:hover {
  background: transparent;
  color: var(--text-tertiary);
}
.editor-divider {
  height: 1px;
  background: var(--border-color);
  margin-bottom: 24px;
}
.editor-instance {
  min-height: 60vh;
}
.editor-instance.focus-editor {
  flex: 1;
  min-height: 0;
  height: 100%;
}
.editor-loading {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 40vh;
  padding-top: 8px;
}
.editor-loading-bar {
  height: 14px;
  border-radius: 6px;
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
  .editor-content-wrap {
    padding: 0 20px;
  }
  .editor-body {
    padding: 24px 0 16px;
  }
  .editor-title {
    font-size: 28px;
  }
}
</style>

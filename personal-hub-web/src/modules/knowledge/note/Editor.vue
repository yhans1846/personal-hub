<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoteById, toggleFavorite, deleteNote } from '@/modules/knowledge/api'
import { getCategories } from '@/api/categoryApi'
import { getTags } from '@/modules/knowledge/api'
import { MdEditor, MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAutoSave } from './editor/useAutoSave'
import { useEditorMode } from './editor/useEditorMode'
import { useImageUpload } from './editor/useImageUpload'
import EditorHeader from './editor/EditorHeader.vue'
import EditorStatusBar from './editor/EditorStatusBar.vue'
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

// ─── 自动保存 ───
const {
  status: saveStatus,
  noteId,
  lastSavedAt,
  save: forceSave,
  restoreDraft,
  clearDraft,
} = useAutoSave(form, isEdit ? Number(route.params.id) : undefined)

// ─── 编辑器模式 ───
const { mode, togglePreview } = useEditorMode()

// ─── 预览模式下改写图片 src（相对路径 → API 路径 + token）───
function setupPreviewImageProxy() {
  nextTick(() => {
    const preview = document.querySelector('.md-editor-preview')
    if (!preview || !noteId.value) return
    const token = localStorage.getItem('token')
    if (!token) return

    preview.querySelectorAll('img').forEach((img) => {
      const src = img.getAttribute('src')
      if (!src) return
      if (src.startsWith('images/') || src.startsWith('attachments/')) {
        img.setAttribute('src', `/api/notes/${noteId.value}/${src}?token=${token}`)
      }
    })
  })
}
watch(mode, (val) => {
  if (val === 'preview') setupPreviewImageProxy()
})

// ─── 图片上传 ───
const { uploading, handleUpload } = useImageUpload(noteId, forceSave)

// ─── 加载数据 ───
onMounted(async () => {
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

      // 检查 localStorage 是否有更新的草稿
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
            ElMessage.info('已恢复本地草稿')
          }
        } catch { /* ignore */ }
      }
    } catch {
      ElMessage.error('加载笔记失败')
    }
  } else if (restoreDraft()) {
    ElMessage.info('已恢复未保存的草稿')
  }

  initialLoading.value = false
})

// ─── 收藏 ───
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

// ─── 导出 ───
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

// ─── 删除 ───
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

// ─── 返回 ───
function handleBack() {
  router.push('/notes')
}

// ─── 图片上传回调 ───
function onUploadImg(files: File[], callback: (urls: string[]) => void) {
  handleUpload(files, callback)
}

// ─── 预览区元信息 ───
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

// 工具条（编辑模式）
const EDIT_TOOLBARS = [
  'bold', 'italic', 'heading', '|',
  'quote', 'unorderedList', 'orderedList', '|',
  'code', 'link', 'image', 'table',
] as any
</script>

<template>
  <div class="editor-page" :class="{ 'is-focus': mode === 'focus' }">
    <!-- Header -->
    <EditorHeader
      v-show="mode !== 'focus'"
      :save-status="saveStatus"
      :is-favorite="isFavorite"
      :mode="mode"
      @back="handleBack"
      @toggle-favorite="handleToggleFavorite"
      @toggle-mode="togglePreview"
      @export-note="handleExport"
      @remove="handleDelete"
    />

    <!-- 主编辑区 -->
    <div class="editor-body" :class="{ 'is-focus': mode === 'focus' }">
      <div class="editor-content-wrap">
        <!-- 标题（预览模式不显示 input） -->
        <input
          v-if="mode !== 'preview'"
          v-model="form.title"
          class="editor-title"
          placeholder="请输入标题..."
        />

        <!-- 元信息（仅在编辑模式显示） -->
        <div v-if="mode === 'edit' && !initialLoading" class="editor-meta">
          <el-popover trigger="click" placement="bottom" :width="280">
            <template #reference>
              <span class="meta-item">
                📂 {{ form.categoryIds.length ? getCategoryNames() : '分类' }}
              </span>
            </template>
            <el-select
              v-model="form.categoryIds"
              multiple
              placeholder="选择分类"
              style="width: 100%"
            >
              <el-option
                v-for="c in categories"
                :key="c.id"
                :label="c.name"
                :value="c.id"
              />
            </el-select>
          </el-popover>

          <el-popover trigger="click" placement="bottom" :width="280">
            <template #reference>
              <span class="meta-item">
                🏷 {{ form.tagIds.length ? getTagNames() : '标签' }}
              </span>
            </template>
            <el-select
              v-model="form.tagIds"
              multiple
              placeholder="选择标签"
              style="width: 100%"
            >
              <el-option
                v-for="t in tags"
                :key="t.id"
                :label="t.name"
                :value="t.id"
              />
            </el-select>
          </el-popover>

          <span v-if="createdAt" class="meta-item static">🕒 {{ formatRelativeTime(createdAt) }}</span>
          <span v-if="readingTimeText" class="meta-item static">👁 {{ readingTimeText }}</span>
        </div>

        <!-- 分割线（编辑模式） -->
        <div v-if="mode === 'edit'" class="editor-divider" />

        <!-- 编辑器（编辑 + 专注模式） -->
        <MdEditor
          v-show="mode === 'edit' || mode === 'focus'"
          :key="mode"
          v-model="form.content"
          :theme="editorTheme"
          :toolbars="mode === 'focus' ? [] : EDIT_TOOLBARS"
          :preview="false"
          language="zh-CN"
          placeholder="开始写作..."
          :on-upload-img="onUploadImg"
          class="editor-instance"
          :class="{ 'focus-editor': mode === 'focus' }"
        />

        <!-- 预览模式 -->
        <div v-if="mode === 'preview'" class="preview-wrap markdown-prose">
          <h1 class="preview-title">{{ form.title || '无标题' }}</h1>
          <div class="preview-meta">
            <template v-if="getCategoryNames()">
              <span class="preview-meta-item">📂 {{ getCategoryNames() }}</span>
            </template>
            <template v-if="getTagNames()">
              <span class="preview-meta-item">🏷 {{ getTagNames() }}</span>
            </template>
            <span v-if="savedTimeText" class="preview-meta-item">🕒 {{ savedTimeText }}</span>
            <span v-if="readingTimeText" class="preview-meta-item">👁 {{ readingTimeText }}</span>
          </div>
          <MdPreview
            :model-value="form.content"
            :theme="editorTheme"
            :show-code-row-number="false"
          />
        </div>
      </div>
    </div>

    <!-- 状态栏 -->
    <EditorStatusBar
      v-show="mode !== 'focus'"
      :content="form.content"
      :mode="mode"
      :save-status="saveStatus"
    />
  </div>
</template>

<style scoped>
/* ===== 页面布局 ===== */
.editor-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-body);
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
.editor-header,
.editor-statusbar {
  transition: opacity 0.2s ease;
}
.editor-content-wrap {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 48px;
}
.editor-page.is-focus .editor-content-wrap {
  padding: 0 24px;
  max-width: none;
  height: 100%;
}

/* ===== 标题 ===== */
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

/* ===== 元信息行 ===== */
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

/* ===== 分割线 ===== */
.editor-divider {
  height: 1px;
  background: var(--border-color);
  margin-bottom: 24px;
}

/* ===== MdEditor 覆写 ===== */
.editor-instance {
  min-height: 60vh;
}
.editor-instance :deep(.md-editor) {
  border: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  background: transparent !important;
}
.editor-instance :deep(.md-editor-toolbar) {
  border: none !important;
  background: transparent !important;
  padding: 0 0 12px 0 !important;
  margin-bottom: 4px;
}
.editor-instance :deep(.md-editor-toolbar-item) {
  color: var(--text-tertiary);
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.editor-instance :deep(.md-editor-toolbar-item:hover) {
  background: var(--bg-hover);
  color: var(--text-primary);
}
.editor-instance :deep(.md-editor-toolbar-item.active) {
  color: var(--accent);
}
.editor-instance :deep(.md-editor-content) {
  background: transparent !important;
}
.editor-instance :deep(.md-editor-input) {
  -webkit-font-smoothing: antialiased;
}
.editor-instance :deep(.cm-editor) {
  background: transparent !important;
}
.editor-instance :deep(.cm-scroller) {
  font-family: var(--font-sans);
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-primary);
}
.editor-instance :deep(.md-editor-footer) {
  display: none !important;
}
/* 聚焦模式：完全隐藏工具栏 */
.editor-instance.focus-editor :deep(.md-editor-toolbar) {
  display: none !important;
}
.editor-instance.focus-editor :deep(.md-editor-toolbar-wrapper) {
  display: none !important;
}
.editor-instance.focus-editor {
  min-height: 100%;
  height: 100%;
}
.editor-instance.focus-editor :deep(.md-editor-content) {
  height: 100% !important;
}

/* ===== 预览模式 ===== */
.preview-wrap {
  padding-bottom: 48px;
  --prose-font-size: 18px;
  --prose-line-height: 1.8;
}
.preview-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
  letter-spacing: -0.02em;
  line-height: 1.3;
}
.preview-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 24px;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}
.preview-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.preview-wrap :deep(.md-editor) {
  background: transparent !important;
  box-shadow: none !important;
}
.preview-wrap :deep(.md-editor-preview) {
  background: transparent !important;
  padding: 0 !important;
}

/* 图片缩放 80% */
.preview-wrap :deep(.md-editor-preview img) {
  max-width: var(--image-max-width, 80%);
  height: auto;
}

/* 代码块 pre 默认有 16px padding，去掉 */
.preview-wrap :deep(.md-editor-preview pre) {
  padding: 0 !important;
}

/* 代码头部 sticky 导致滚动时跳动，改为自然跟随 */
.preview-wrap :deep(.md-editor-code .md-editor-code-head) {
  position: static !important;
}

/* ===== 响应式 ===== */
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
  .preview-title {
    font-size: 28px;
  }
}
</style>

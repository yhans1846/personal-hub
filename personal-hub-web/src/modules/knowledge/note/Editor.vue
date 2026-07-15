<script setup lang="ts">
import { ref, shallowRef, markRaw, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoteById, toggleFavorite, deleteNote } from '@/modules/knowledge/api'
import { getCategories } from '@/api/categoryApi'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAutoSave } from './editor/useAutoSave'
import { useEditorMode } from './editor/useEditorMode'
import { useEditorPreferences } from './editor/useEditorPreferences'
import { useImageUpload } from './editor/useImageUpload'
import EditorHeader from './editor/EditorHeader.vue'
import EditorStatusBar from './editor/EditorStatusBar.vue'
import NoteMdEditor from './editor/NoteMdEditor.vue'
import EditorPreviewPanel from './editor/EditorPreviewPanel.vue'
import { buildEditorId, FULL_TOOLBARS, SPLIT_TOOLBARS } from './editor/mdEditorToolbars'
import { estimateReadingTime, formatRelativeTime } from '@/utils/readingTime'

// ─── Splitpanes 动态导入 ───
const SplitpanesComponent = shallowRef<any>(null)
const PaneComponent = shallowRef<any>(null)
const splitpanesLoaded = ref(false)

import('splitpanes')
  .then((mod) => {
    SplitpanesComponent.value = markRaw(mod.Splitpanes)
    PaneComponent.value = markRaw(mod.Pane)
    splitpanesLoaded.value = true
    // 动态添加 splitpanes 样式
    import('splitpanes/dist/splitpanes.css')
  })
  .catch(() => {
    console.warn('splitpanes 未安装，分栏功能不可用')
  })

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

const editorId = computed(() => buildEditorId(noteId.value))

// ─── 自动保存 ───
const {
  status: saveStatus,
  noteId,
  lastSavedAt,
  save: forceSave,
  restoreDraft,
  clearDraft,
} = useAutoSave(form, isEdit ? Number(route.params.id) : undefined)

// ─── 编辑器模式 + 偏好记忆 ───
const {
  mode,
  livePreview,
  isFullscreen,
  togglePreview,
  toggleFocus,
  toggleLivePreview,
  toggleFullscreen,
} = useEditorMode()

const { prefs } = useEditorPreferences()

// ─── Focus Mode 全局状态（隐藏 AppLayout 的 sidebar/topbar/breadcrumb） ───
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

// ─── 分栏滚动同步 ───
let isSyncingScroll = false
const editorWrapRef = ref<HTMLElement | null>(null)
const previewPanelRef = ref<InstanceType<typeof EditorPreviewPanel> | null>(null)

function getPreviewScrollEl(): HTMLElement | null {
  return previewPanelRef.value?.scrollEl ?? null
}

function syncScroll(source: 'editor' | 'preview') {
  if (isSyncingScroll) return
  isSyncingScroll = true

  requestAnimationFrame(() => {
    try {
      if (source === 'editor') {
        const preview = getPreviewScrollEl()
        const editor = editorWrapRef.value
        if (!preview || !editor) return
        const sh = editor.scrollHeight - editor.clientHeight
        if (sh <= 0) return
        const ratio = editor.scrollTop / sh
        preview.scrollTop = ratio * (preview.scrollHeight - preview.clientHeight)
      } else {
        const editor = editorWrapRef.value
        const preview = getPreviewScrollEl()
        if (!editor || !preview) return
        const sh = preview.scrollHeight - preview.clientHeight
        if (sh <= 0) return
        const ratio = preview.scrollTop / sh
        editor.scrollTop = ratio * (editor.scrollHeight - editor.clientHeight)
      }
    } finally {
      isSyncingScroll = false
    }
  })
}

// ─── 分栏比例变化时保存 ───
function onSplitterMoved(event: any) {
  if (event?.[0]?.size) {
    prefs.splitRatio = Math.round(event[0].size)
  }
}

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
</script>

<template>
  <div
    class="editor-page"
    :class="{
      'is-focus': mode === 'focus',
      'is-fullscreen': isFullscreen,
    }"
  >
    <!-- Header -->
    <EditorHeader
      :save-status="saveStatus"
      :is-favorite="isFavorite"
      :mode="mode"
      :live-preview="livePreview"
      :is-fullscreen="isFullscreen"
      @back="handleBack"
      @toggle-favorite="handleToggleFavorite"
      @toggle-mode="togglePreview"
      @toggle-live-preview="toggleLivePreview"
      @toggle-focus="toggleFocus"
      @toggle-fullscreen="toggleFullscreen"
      @export-note="handleExport"
      @remove="handleDelete"
    />

    <!-- 主编辑区 -->
    <div
      class="editor-body"
      :class="{
        'is-focus': mode === 'focus',
        'is-split': mode === 'edit' && livePreview,
        'is-fullscreen': isFullscreen,
      }"
    >
      <!-- ─── 分栏模式（编辑 + 预览并排） ─── -->
      <template v-if="mode === 'edit' && livePreview && splitpanesLoaded && SplitpanesComponent">
        <component :is="SplitpanesComponent" class="splitpanes-editor" @resized="onSplitterMoved">
          <component :is="PaneComponent" :size="prefs.splitRatio" :min-size="30">
            <div ref="editorWrapRef" class="split-left" @scroll="syncScroll('editor')">
              <div class="split-editor-content">
                <input
                  v-model="form.title"
                  class="editor-title split-title"
                  placeholder="请输入标题..."
                />
                <NoteMdEditor
                  v-model="form.content"
                  :editor-id="editorId"
                  :toolbars="SPLIT_TOOLBARS"
                  :theme="editorTheme"
                  compact
                  :on-upload-img="onUploadImg"
                  class="split-instance"
                />
              </div>
            </div>
          </component>
          <component :is="PaneComponent" :size="100 - prefs.splitRatio" :min-size="30">
            <div class="split-right">
              <EditorPreviewPanel
                ref="previewPanelRef"
                :content="form.content"
                :title="form.title"
                :theme="editorTheme"
                :editor-id="`${editorId}-preview`"
                :note-id="noteId"
                :reading-time-text="readingTimeText"
                :category-names="getCategoryNames()"
                :tag-names="getTagNames()"
                :saved-time-text="savedTimeText"
                :show-meta="false"
                :show-toc="false"
                @scroll="syncScroll('preview')"
              />
            </div>
          </component>
        </component>
      </template>

      <!-- ─── fallback: splitpanes 未加载完成 ─── -->
      <template v-else-if="mode === 'edit' && livePreview && !splitpanesLoaded">
        <div class="editor-content-wrap">
          <div class="loading-split">加载分栏中...</div>
        </div>
      </template>

      <!-- ─── 单栏模式（编辑 / 预览 / 专注） ─── -->
      <template v-else>
        <div class="editor-content-wrap" :class="{ 'is-focus-content': mode === 'focus' }">
          <!-- 标题 -->
          <input
            v-if="mode !== 'preview'"
            v-model="form.title"
            class="editor-title"
            placeholder="请输入标题..."
          />

          <!-- 元信息 -->
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

          <!-- 分割线 -->
          <div v-if="mode === 'edit'" class="editor-divider" />

          <!-- 编辑器 -->
          <NoteMdEditor
            v-if="mode === 'edit' || mode === 'focus'"
            v-model="form.content"
            :editor-id="editorId"
            :toolbars="mode === 'focus' ? [] : FULL_TOOLBARS"
            :theme="editorTheme"
            :on-upload-img="onUploadImg"
            :class="{ 'editor-instance': true, 'focus-editor': mode === 'focus' }"
          />

          <!-- 预览模式 -->
          <EditorPreviewPanel
            v-if="mode === 'preview'"
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
      </template>
    </div>

    <!-- 状态栏 -->
    <EditorStatusBar
      :content="form.content"
      :mode="mode"
      :save-status="saveStatus"
      :last-saved-at="lastSavedAt"
      :live-preview="livePreview"
      :is-fullscreen="isFullscreen"
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
.editor-body.is-split {
  padding: 0;
  overflow: hidden;
}
.editor-body.is-fullscreen {
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
.loading-split {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-tertiary);
  font-size: var(--text-sm);
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
.split-title {
  font-size: 24px;
  margin-bottom: 12px;
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
.preview-wrap :deep(.md-editor-preview img) {
  max-width: var(--image-max-width, 80%);
  height: auto;
}
.preview-wrap :deep(.md-editor-preview pre) {
  padding: 0 !important;
}
.preview-wrap :deep(.md-editor-code .md-editor-code-head) {
  position: static !important;
}

/* ===== 分栏布局 ===== */
.splitpanes-editor {
  height: 100% !important;
}
.splitpanes-editor :deep(.splitpanes__splitter) {
  width: 4px;
  margin: 0 -2px;
  background: transparent;
  border: none;
  position: relative;
  z-index: 5;
  cursor: col-resize;
  transition: background 0.15s;
}
.splitpanes-editor :deep(.splitpanes__splitter:hover) {
  background: var(--accent);
  opacity: 0.3;
}
.splitpanes-editor :deep(.splitpanes__splitter::after) {
  display: none;
}

.split-left {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  background: var(--bg-body);
}
.split-right {
  height: 100%;
  overflow: hidden;
  background: var(--bg-card);
}
.split-editor-content {
  max-width: 100%;
}
.split-instance {
  min-height: 0;
  height: calc(100% - 50px);
}
.split-preview-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  letter-spacing: -0.02em;
  line-height: 1.3;
}
.split-right .preview-wrap {
  padding-bottom: 48px;
}
.split-right .preview-meta {
  margin-bottom: 16px;
}
.split-instance :deep(.md-editor-content) {
  height: 100% !important;
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
  .split-title {
    font-size: 20px;
  }
  .split-left {
    padding: 16px;
  }
  .split-right {
    padding: 16px;
  }
}
</style>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { FolderTree } from 'lucide-vue-next'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import type { NoteVO, NoteFolderSelection } from '@/types/note'
import { getNotePreview, restoreNote, exportNote } from '@/modules/knowledge/api'
import { useReadingConfigStore } from '@/store/readingConfigStore'
import { useReadingTheme } from './preview/useReadingTheme'
import DocLayout from '@/components/DocLayout.vue'
import type { TocItem } from './preview/PreviewToc.vue'
import { storeToRefs } from 'pinia'
import { estimateReadingTime } from '@/utils/readingTime'
import { formatUpdated } from '@/utils/formatTime'
import { buildPreviewOptions } from './editor/vditorSetup'
import { parseTocFromMarkdown, assignPreviewHeadingIds, headingIdFromText } from './editor/parseToc'
import { preparePreviewMarkdown, setupPreviewImageZoom } from './editor/previewEnhancements'
import NoteBacklinks from './NoteBacklinks.vue'
import NoteFolderShell from './NoteFolderShell.vue'
import { useFeatureFlagStore } from '@/store/featureFlagStore'
import UiTooltip from '@/components/UiTooltip.vue'

const route = useRoute()
const router = useRouter()
const readingStore = useReadingConfigStore()
const featureFlags = useFeatureFlagStore()
const { config } = storeToRefs(readingStore)
const { resolvedTheme } = useReadingTheme()

const note = ref<NoteVO | null>(null)
const loading = ref(true)
const error = ref('')
const activeHeading = ref('')
const toc = ref<TocItem[]>([])
const previewRef = ref<HTMLDivElement | null>(null)
const folderSelection = ref<NoteFolderSelection>('home')
const folderDrawerOpen = ref(false)

const loadedNote = computed(() => note.value as NoteVO)
const activeNoteId = computed(() => {
  const id = note.value?.id ?? Number(route.params.id)
  return Number.isFinite(id) && id > 0 ? id : null
})

let observer: IntersectionObserver | null = null
let disposeZoom: (() => void) | null = null
let renderSeq = 0

const isTrash = computed(() => note.value?.isDeleted === 1)

const previewTheme = computed<'light' | 'dark'>(() => {
  const t = resolvedTheme.value as string
  return t === 'dark' ? 'dark' : 'light'
})

function scrollToHeading(id: string) {
  activeHeading.value = id
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function getPreviewRoot(): HTMLElement | null {
  return previewRef.value
}

async function renderMarkdown() {
  if (!previewRef.value || !note.value?.content) return
  const seq = ++renderSeq
  disposeZoom?.()
  disposeZoom = null
  previewRef.value.innerHTML = ''
  const md = await preparePreviewMarkdown(note.value.content, featureFlags.isEnabled('backlink'))
  if (seq !== renderSeq || !previewRef.value) return
  await Vditor.preview(previewRef.value, md, buildPreviewOptions(previewTheme.value))
  if (seq !== renderSeq || !previewRef.value) return
  await nextTick()
  disposeZoom = await setupPreviewImageZoom(previewRef.value, note.value?.id)
  if (seq !== renderSeq) {
    disposeZoom?.()
    disposeZoom = null
    return
  }
  setupObserver()
  setupCodeCopy()
  setupExternalLinks()
  setupHeadingAnchors()
  setupWikiLinks()
}

function setupWikiLinks() {
  const preview = getPreviewRoot()
  if (!preview) return
  preview.querySelectorAll('a[href^="#wiki-missing:"]').forEach((a) => {
    a.classList.add('wiki-link--missing')
    ;(a as HTMLAnchorElement).addEventListener('click', (e) => {
      e.preventDefault()
      ElMessage.info('未找到对应笔记')
    })
  })
  preview.querySelectorAll('a[href^="/notes/"]').forEach((a) => {
    a.addEventListener('click', (e) => {
      const href = a.getAttribute('href')
      if (!href?.startsWith('/notes/')) return
      e.preventDefault()
      window.open(href, '_blank')
    })
  })
}

function setupObserver() {
  const preview = getPreviewRoot()
  if (!preview || !note.value?.content) return
  const headingEls = preview.querySelectorAll('h1, h2, h3, h4, h5, h6')
  if (!headingEls.length) return
  observer?.disconnect()
  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          const text = entry.target.textContent?.trim() ?? ''
          activeHeading.value = text.replace(/\s+/g, '-')
        }
      }
    },
    { rootMargin: '-64px 0px -60% 0px', threshold: 0 },
  )
  headingEls.forEach((el) => observer!.observe(el))
}

function setupCodeCopy() {
  const preview = getPreviewRoot()
  if (!preview) return
  preview.querySelectorAll('pre').forEach((pre) => {
    if (pre.querySelector('.code-copy-btn')) return
    const btn = document.createElement('button')
    btn.className = 'code-copy-btn'
    btn.textContent = '复制'
    btn.addEventListener('click', async () => {
      const code = pre.querySelector('code')
      if (!code) return
      try {
        await navigator.clipboard.writeText(code.textContent || '')
        btn.textContent = '已复制'
        setTimeout(() => { btn.textContent = '复制' }, 2000)
      } catch {
        btn.textContent = '失败'
      }
    })
    pre.style.position = 'relative'
    pre.appendChild(btn)
  })
}

function setupExternalLinks() {
  const preview = getPreviewRoot()
  if (!preview) return
  preview.querySelectorAll('a').forEach((a) => {
    const href = a.getAttribute('href') || ''
    if (href.startsWith('/notes/') || href.startsWith('#wiki-missing:')) return
    if (!a.getAttribute('target')) {
      a.setAttribute('target', '_blank')
      a.setAttribute('rel', 'noopener noreferrer')
    }
  })
}

function setupHeadingAnchors() {
  const preview = getPreviewRoot()
  if (!preview) return
  assignPreviewHeadingIds(preview)
  preview.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach((h) => {
    if (h.querySelector('.heading-anchor')) return
    const id = h.id || headingIdFromText(h.textContent ?? '')
    if (!id) return
    const anchor = document.createElement('a')
    anchor.className = 'heading-anchor'
    anchor.href = `#${id}`
    anchor.textContent = '#'
    anchor.setAttribute('aria-hidden', 'true')
    h.insertBefore(anchor, h.firstChild)
  })
}

async function loadNote(id: number) {
  loading.value = true
  error.value = ''
  note.value = null
  toc.value = []
  activeHeading.value = ''
  observer?.disconnect()
  disposeZoom?.()
  disposeZoom = null
  if (previewRef.value) previewRef.value.innerHTML = ''

  if (!id) {
    error.value = '笔记ID无效'
    loading.value = false
    return
  }
  try {
    const res = await getNotePreview(id)
    note.value = res.data.data
    if (note.value?.content) toc.value = parseTocFromMarkdown(note.value.content)
    if (note.value?.title) document.title = `${note.value.title} — 预览`
  } catch {
    error.value = '笔记不存在或无权访问'
  } finally {
    loading.value = false
  }
}

function onOpenNote(id: number) {
  if (id === activeNoteId.value) return
  router.push(`/notes/${id}/preview`)
}

onMounted(() => {
  readingStore.fetchFromBackend()
  loadNote(Number(route.params.id))
})

watch(
  () => Number(route.params.id),
  (id) => {
    if (!Number.isFinite(id) || id <= 0) return
    if (note.value?.id === id) return
    loadNote(id)
  },
)

watch(() => note.value?.content, () => {
  if (note.value?.content) {
    toc.value = parseTocFromMarkdown(note.value.content)
    renderMarkdown()
  }
}, { flush: 'post' })

watch(previewTheme, () => {
  renderMarkdown()
})

async function handleExport() {
  if (!note.value) return
  try {
    const res = await exportNote(note.value.id)
    const blob = new Blob([res.data], { type: 'application/zip' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${note.value.title || '笔记导出'}.zip`
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('导出失败')
  }
}

async function handleRestore() {
  if (!note.value) return
  try {
    await restoreNote(note.value.id)
    ElMessage.success('已恢复')
    window.close()
  } catch { /* handled */ }
}

function handleClose() {
  window.close()
}

onUnmounted(() => {
  renderSeq += 1
  observer?.disconnect()
  disposeZoom?.()
  disposeZoom = null
})
</script>

<template>
  <div class="preview-shell">
    <NoteFolderShell
      v-model="folderSelection"
      v-model:drawer-open="folderDrawerOpen"
      :active-note-id="activeNoteId"
      readonly
      @open-note="onOpenNote"
    />
    <div class="preview-main">
      <div class="preview-mobile-bar">
        <UiTooltip content="文件夹" placement="bottom">
          <button
            type="button"
            class="preview-folder-toggle"
            @click="folderDrawerOpen = !folderDrawerOpen"
          >
            <FolderTree :size="16" />
          </button>
        </UiTooltip>
      </div>

      <div v-if="loading" class="state-message">
        <div class="loading-spinner" />
        <span>加载中...</span>
      </div>

      <div v-else-if="error" class="state-message state-error">
        <p>{{ error }}</p>
        <button class="back-btn" @click="handleClose">关闭页面</button>
      </div>

      <DocLayout
        v-else-if="note"
        :title="loadedNote.title"
        :toc-items="toc"
        :active-heading="activeHeading"
        :is-trash="isTrash"
        :meta="{
          updatedAt: formatUpdated(loadedNote.updatedAt, ''),
          readingTime: estimateReadingTime(loadedNote.content),
        }"
        @back="handleClose"
        @scroll-to-heading="scrollToHeading"
      >
        <template #header-actions>
          <el-dropdown trigger="click" placement="bottom-end">
            <UiTooltip content="更多" placement="bottom">
              <button class="header-action-btn">
                <span style="letter-spacing: 2px">⋯</span>
              </button>
            </UiTooltip>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleExport">导出笔记</el-dropdown-item>
                <el-dropdown-item v-if="isTrash" @click="handleRestore">恢复笔记</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <div
          class="preview-content-wrap"
          :style="{
            maxWidth: config.readingWidth + 'px',
            fontSize: config.fontSize + 'px',
            lineHeight: config.lineHeight,
            '--prose-font-size': config.fontSize + 'px',
            '--prose-line-height': config.lineHeight,
            '--image-max-width': config.imageMaxWidth + '%',
          }"
        >
          <div class="preview-taxonomies">
            <span v-if="loadedNote.categories?.length" class="taxonomy-block">
              <span v-for="c in loadedNote.categories" :key="c.id" class="taxonomy-tag">{{ c.name }}</span>
            </span>
            <span v-if="loadedNote.tags?.length" class="taxonomy-block">
              <span
                v-for="t in loadedNote.tags"
                :key="t.id"
                class="taxonomy-tag"
                :style="t.color ? { '--tag-color': t.color } : undefined"
              >{{ t.name }}</span>
            </span>
          </div>

          <div v-if="loadedNote.content" class="preview-content markdown-prose">
            <div ref="previewRef" class="vditor-preview-body" />
          </div>
          <div v-else class="empty-content">笔记内容为空</div>
          <NoteBacklinks
            v-if="loadedNote.id"
            :note-id="loadedNote.id"
            :enabled="featureFlags.isEnabled('backlink')"
          />
        </div>
      </DocLayout>
    </div>
  </div>
</template>

<style scoped>
.preview-shell {
  display: flex;
  height: 100vh;
  min-height: 0;
  position: relative;
  background: var(--preview-bg);
  color: var(--preview-text);
}

/* 左树 / 窄轨 / 移动端栏：与阅读主题同一套 --preview-* */
.preview-shell :deep(.folder-tree) {
  background: var(--preview-bg);
  border-right-color: var(--preview-border);
}
.preview-shell :deep(.folder-tree-head) {
  height: 56px;
  box-sizing: border-box;
  padding: 0 12px;
  border-bottom: 1px solid var(--preview-border);
}
.preview-shell :deep(.folder-tree-title),
.preview-shell :deep(.folder-section-label),
.preview-shell :deep(.folder-count),
.preview-shell :deep(.folder-empty) {
  color: var(--preview-text);
  opacity: 0.55;
}
.preview-shell :deep(.folder-row) {
  color: var(--preview-text);
  opacity: 0.88;
}
.preview-shell :deep(.folder-row .folder-row-icon) {
  opacity: 0.55;
  color: var(--preview-text);
}
.preview-shell :deep(.folder-row:hover) {
  background: color-mix(in srgb, var(--preview-text) 6%, var(--preview-bg));
  opacity: 1;
}
.preview-shell :deep(.folder-row.active) {
  background: color-mix(in srgb, var(--accent) 12%, var(--preview-bg));
  color: var(--accent);
  opacity: 1;
}
.preview-shell :deep(.folder-row--note) {
  opacity: 0.78;
}
.preview-shell :deep(.folder-row--note.active) {
  opacity: 1;
}
.preview-shell :deep(.folder-icon-btn),
.preview-shell :deep(.folder-collapse-btn),
.preview-shell :deep(.folder-pane-expand),
.preview-shell :deep(.folder-expand) {
  color: var(--preview-text);
  opacity: 0.5;
}
.preview-shell :deep(.folder-icon-btn:hover),
.preview-shell :deep(.folder-collapse-btn:hover),
.preview-shell :deep(.folder-pane-expand:hover),
.preview-shell :deep(.folder-expand:hover) {
  color: var(--preview-heading);
  opacity: 1;
  background: color-mix(in srgb, var(--preview-text) 6%, var(--preview-bg));
}
.preview-shell :deep(.folder-pane-rail) {
  background: var(--preview-bg);
  border-right-color: var(--preview-border);
}
.preview-shell :deep(.folder-pane.open),
.preview-shell :deep(.folder-pane) {
  background: transparent;
}

.preview-main {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  position: relative;
  background: var(--preview-bg);
}
.preview-mobile-bar {
  display: none;
}
.preview-folder-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--preview-text);
  opacity: 0.55;
  cursor: pointer;
}
.preview-folder-toggle:hover {
  color: var(--preview-heading);
  opacity: 1;
  background: color-mix(in srgb, var(--preview-text) 6%, var(--preview-bg));
}
@media (max-width: 768px) {
  .preview-mobile-bar {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    padding: 6px 8px;
    border-bottom: 1px solid var(--preview-border);
    background: var(--preview-bg);
  }
  .preview-shell :deep(.folder-pane) {
    background: var(--preview-bg);
  }
}
.preview-main :deep(.doc-layout) {
  flex: 1;
  min-height: 0;
  height: auto;
}
.preview-main :deep(.header-action-btn) {
  color: var(--preview-text);
  opacity: 0.55;
}
.preview-main :deep(.header-action-btn:hover) {
  color: var(--preview-heading);
  opacity: 1;
  background: color-mix(in srgb, var(--preview-text) 6%, var(--preview-bg));
}
.vditor-preview-body :deep(a.wiki-link--missing) {
  color: var(--preview-text);
  opacity: 0.45;
  text-decoration: line-through;
  cursor: not-allowed;
}
.state-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--sp-3);
  flex: 1;
  height: 100%;
  color: var(--preview-text);
  opacity: 0.55;
  font-size: var(--text-sm);
  background: var(--preview-bg);
}
.state-error { color: var(--danger); }
.back-btn {
  padding: 6px 16px;
  font-size: var(--text-sm);
  color: var(--preview-text);
  opacity: 0.72;
  border: 1px solid var(--preview-border);
  background: transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
}
.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-color);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.preview-content-wrap {
  margin: 0 auto;
  padding: 0;
  color: var(--preview-text);
}
.preview-taxonomies {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
  margin-bottom: var(--sp-5);
}
.taxonomy-tag {
  display: inline-block;
  padding: 1px 8px;
  font-size: 12px;
  line-height: 1.6;
  background: color-mix(in srgb, var(--accent) 10%, var(--preview-bg, transparent));
  color: var(--accent);
  border-radius: var(--radius-sm);
}
.taxonomy-tag[style] {
  background: color-mix(in srgb, var(--tag-color) 15%, transparent);
  color: var(--tag-color);
}
.empty-content {
  text-align: center;
  padding: 48px 0;
  color: var(--preview-text);
  opacity: 0.45;
}
.header-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  color: var(--preview-text);
  opacity: 0.55;
  cursor: pointer;
}
:deep(.vditor-reset) {
  background: transparent !important;
  padding-inline: 48px !important;
  padding-block-start: 4px !important;
  font-size: var(--prose-font-size, 16px) !important;
  word-break: break-word !important;
  color: var(--prose-color) !important;
}
:deep(.vditor-reset p),
:deep(.vditor-reset li),
:deep(.vditor-reset blockquote) {
  line-height: inherit !important;
}
:deep(.vditor-reset h1),
:deep(.vditor-reset h2),
:deep(.vditor-reset h3),
:deep(.vditor-reset h4),
:deep(.vditor-reset h5),
:deep(.vditor-reset h6) {
  color: var(--prose-heading-color) !important;
}
:deep(.vditor-reset img) {
  max-width: var(--image-max-width, 80%);
  height: auto;
}
</style>

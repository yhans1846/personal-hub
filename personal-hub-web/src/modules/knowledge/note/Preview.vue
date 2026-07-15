<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import mediumZoom from 'medium-zoom'
import type { NoteVO } from '@/types/note'
import { getNotePreview, restoreNote, exportNote } from '@/modules/knowledge/api'
import { useReadingConfigStore } from '@/store/readingConfigStore'
import { useReadingTheme } from './preview/useReadingTheme'
import DocLayout from '@/components/DocLayout.vue'
import type { TocItem } from './preview/PreviewToc.vue'
import { storeToRefs } from 'pinia'
import { estimateReadingTime } from '@/utils/readingTime'
import { buildPreviewOptions } from './editor/vditorSetup'
import { parseTocFromMarkdown } from './editor/parseToc'

const route = useRoute()
const readingStore = useReadingConfigStore()
const { config } = storeToRefs(readingStore)
const { resolvedTheme } = useReadingTheme()

const note = ref<NoteVO | null>(null)
const loading = ref(true)
const error = ref('')
const activeHeading = ref('')
const toc = ref<TocItem[]>([])
const previewRef = ref<HTMLDivElement | null>(null)

const loadedNote = computed(() => note.value as NoteVO)

let observer: IntersectionObserver | null = null
let zoomInstance: ReturnType<typeof mediumZoom> | null = null

const isTrash = computed(() => note.value?.isDeleted === 1)

const previewTheme = computed<'light' | 'dark'>(() => {
  const t = resolvedTheme.value as string
  return t === 'dark' ? 'dark' : 'light'
})

function formatTime(dateStr?: string) {
  if (!dateStr) return ''
  return dateStr.slice(0, 16).replace('T', ' ')
}

const parseToc = parseTocFromMarkdown

function scrollToHeading(id: string) {
  activeHeading.value = id
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function getPreviewRoot(): HTMLElement | null {
  return previewRef.value
}

async function renderMarkdown() {
  if (!previewRef.value || !note.value?.content) return
  previewRef.value.innerHTML = ''
  await Vditor.preview(previewRef.value, note.value.content, buildPreviewOptions(previewTheme.value))
  await nextTick()
  setupImageProxy()
  setupObserver()
  setupImageZoom()
  setupCodeCopy()
  setupExternalLinks()
  setupHeadingAnchors()
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

function setupImageProxy() {
  const preview = getPreviewRoot()
  if (!preview || !note.value) return
  const noteId = note.value.id
  const token = localStorage.getItem('token')
  if (!token) return
  preview.querySelectorAll('img').forEach((img) => {
    const src = img.getAttribute('src')
    if (!src) return
    if (src.startsWith('images/') || src.startsWith('attachments/')) {
      img.setAttribute('src', `/api/notes/${noteId}/${src}?token=${token}`)
    }
  })
}

function setupImageZoom() {
  const preview = getPreviewRoot()
  if (!preview) return
  zoomInstance?.detach()
  zoomInstance = mediumZoom(preview.querySelectorAll('img'), {
    background: 'rgba(0, 0, 0, 0.6)',
    margin: 40,
  })
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
    if (!a.getAttribute('target')) {
      a.setAttribute('target', '_blank')
      a.setAttribute('rel', 'noopener noreferrer')
    }
  })
}

function setupHeadingAnchors() {
  const preview = getPreviewRoot()
  if (!preview) return
  preview.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach((h) => {
    if (h.querySelector('.heading-anchor')) return
    const id = h.textContent?.trim().replace(/\s+/g, '-') ?? ''
    if (!id) return
    h.id = id
    const anchor = document.createElement('a')
    anchor.className = 'heading-anchor'
    anchor.href = `#${id}`
    anchor.textContent = '#'
    anchor.setAttribute('aria-hidden', 'true')
    h.insertBefore(anchor, h.firstChild)
  })
}

onMounted(async () => {
  readingStore.fetchFromBackend()
  const id = Number(route.params.id)
  if (!id) {
    error.value = '笔记ID无效'
    loading.value = false
    return
  }
  try {
    const res = await getNotePreview(id)
    note.value = res.data.data
    if (note.value?.content) toc.value = parseToc(note.value.content)
    if (note.value?.title) document.title = `${note.value.title} — 预览`
  } catch {
    error.value = '笔记不存在或无权访问'
  } finally {
    loading.value = false
  }
})

watch(() => note.value?.content, () => {
  if (note.value?.content) {
    toc.value = parseToc(note.value.content)
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
  observer?.disconnect()
  zoomInstance?.detach()
})
</script>

<template>
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
      updatedAt: formatTime(loadedNote.updatedAt),
      readingTime: estimateReadingTime(loadedNote.content),
    }"
    @back="handleClose"
    @scroll-to-heading="scrollToHeading"
  >
    <template #header-actions>
      <el-dropdown trigger="click" placement="bottom-end">
        <button class="header-action-btn" title="更多">
          <span style="letter-spacing: 2px">⋯</span>
        </button>
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
    </div>
  </DocLayout>
</template>

<style scoped>
.state-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--sp-3);
  flex: 1;
  height: 100vh;
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  background: var(--preview-bg);
}
.state-error { color: var(--danger); }
.back-btn {
  padding: 6px 16px;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  background: transparent;
  border-radius: 6px;
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
  background: color-mix(in srgb, var(--accent) 10%, transparent);
  color: var(--accent);
  border-radius: 4px;
}
.taxonomy-tag[style] {
  background: color-mix(in srgb, var(--tag-color) 15%, transparent);
  color: var(--tag-color);
}
.empty-content {
  text-align: center;
  padding: 48px 0;
  color: var(--text-tertiary);
}
.header-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  border-radius: 6px;
  color: var(--text-tertiary);
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

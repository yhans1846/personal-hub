<script setup lang="ts">
import { computed, nextTick, onUnmounted, ref, watch } from 'vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import mediumZoom from 'medium-zoom'
import PreviewToc from '../preview/PreviewToc.vue'
import { parseTocFromMarkdown } from './parseToc'
import { setupMdEditor } from './mdEditorSetup'

setupMdEditor()

const props = withDefaults(defineProps<{
  content: string
  title?: string
  theme?: 'light' | 'dark'
  editorId: string
  noteId?: number | null
  showToc?: boolean
  showMeta?: boolean
  readingTimeText?: string
  categoryNames?: string
  tagNames?: string
  savedTimeText?: string
}>(), {
  title: '',
  theme: 'light',
  showToc: true,
  showMeta: true,
})

const emit = defineEmits<{
  scroll: []
}>()

const scrollRef = ref<HTMLElement | null>(null)
const activeHeading = ref('')
const tocItems = computed(() => parseTocFromMarkdown(props.content))

let zoomInstance: ReturnType<typeof mediumZoom> | null = null

function setupImageProxy() {
  nextTick(() => {
    const preview = scrollRef.value?.querySelector('.md-editor-preview')
    if (!preview || !props.noteId) return
    const token = localStorage.getItem('token')
    if (!token) return

    preview.querySelectorAll('img').forEach((img) => {
      const src = img.getAttribute('src')
      if (!src) return
      if (src.startsWith('images/') || src.startsWith('attachments/')) {
        img.setAttribute('src', `/api/notes/${props.noteId}/${src}?token=${token}`)
      }
    })
  })
}

function setupImageZoom() {
  nextTick(() => {
    const preview = scrollRef.value?.querySelector('.md-editor-preview')
    if (!preview) return
    zoomInstance?.detach()
    zoomInstance = mediumZoom(preview.querySelectorAll('img'), {
      background: 'rgba(0, 0, 0, 0.6)',
      margin: 40,
    })
  })
}

function setupCodeCopy() {
  nextTick(() => {
    const preview = scrollRef.value?.querySelector('.md-editor-preview')
    if (!preview) return

    preview.querySelectorAll('pre').forEach((pre) => {
      if (pre.querySelector('.ph-code-copy')) return
      const btn = document.createElement('button')
      btn.className = 'ph-code-copy'
      btn.type = 'button'
      btn.textContent = '复制'
      btn.addEventListener('click', async () => {
        const code = pre.querySelector('code')?.textContent ?? ''
        try {
          await navigator.clipboard.writeText(code)
          btn.textContent = '已复制'
          setTimeout(() => { btn.textContent = '复制' }, 1500)
        } catch { /* ignore */ }
      })
      pre.style.position = 'relative'
      pre.appendChild(btn)
    })
  })
}

function setupHeadingAnchors() {
  nextTick(() => {
    const preview = scrollRef.value?.querySelector('.md-editor-preview')
    if (!preview) return
    preview.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach((h) => {
      const text = h.textContent?.trim() ?? ''
      if (text) h.id = text.replace(/\s+/g, '-')
    })
  })
}

function enhancePreview() {
  setupImageProxy()
  setupHeadingAnchors()
  setupCodeCopy()
  setupImageZoom()
}

watch(() => [props.content, props.noteId], () => enhancePreview(), { flush: 'post' })

onUnmounted(() => {
  zoomInstance?.detach()
  zoomInstance = null
})

function scrollToHeading(id: string) {
  activeHeading.value = id
  const target = scrollRef.value?.querySelector(`#${CSS.escape(id)}`)
  target?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

defineExpose({ scrollEl: scrollRef })
</script>

<template>
  <div class="editor-preview-panel">
    <PreviewToc
      v-if="showToc"
      :items="tocItems"
      :active-id="activeHeading"
      @scroll-to="scrollToHeading"
    />

    <div ref="scrollRef" class="editor-preview-scroll" @scroll="emit('scroll')">
      <div class="preview-wrap markdown-prose">
        <h1 v-if="title" class="preview-title">{{ title || '无标题' }}</h1>
        <div v-if="showMeta" class="preview-meta">
          <span v-if="categoryNames" class="preview-meta-item">📂 {{ categoryNames }}</span>
          <span v-if="tagNames" class="preview-meta-item">🏷 {{ tagNames }}</span>
          <span v-if="savedTimeText" class="preview-meta-item">🕒 {{ savedTimeText }}</span>
          <span v-if="readingTimeText" class="preview-meta-item">👁 {{ readingTimeText }}</span>
        </div>
        <MdPreview
          :id="editorId"
          :model-value="content"
          :theme="theme"
          preview-theme="github"
          code-theme="atom"
          :show-code-row-number="true"
          :auto-detect-code="true"
          :no-mermaid="false"
          :no-katex="false"
          @on-html-changed="enhancePreview"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.editor-preview-panel {
  display: flex;
  height: 100%;
  overflow: hidden;
  background: var(--bg-card);
}

.editor-preview-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.preview-wrap {
  max-width: 760px;
  margin: 0 auto;
  padding-bottom: 48px;
  --prose-font-size: 17px;
  --prose-line-height: 1.8;
}

.preview-title {
  font-size: 32px;
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
  max-width: var(--image-max-width, 90%);
  height: auto;
  border-radius: var(--radius-sm);
}

.preview-wrap :deep(.md-editor-preview pre) {
  position: relative;
  padding-top: 2.2em !important;
}

.preview-wrap :deep(.ph-code-copy) {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  font-size: 11px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
}

.preview-wrap :deep(.ph-code-copy:hover) {
  color: var(--accent);
  border-color: var(--accent);
}
</style>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import mediumZoom from 'medium-zoom'
import PreviewToc from '../preview/PreviewToc.vue'
import { parseTocFromMarkdown } from './parseToc'
import { buildPreviewOptions } from './vditorSetup'

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

const emit = defineEmits<{ scroll: [] }>()

const scrollRef = ref<HTMLElement | null>(null)
const previewRef = ref<HTMLDivElement | null>(null)
const activeHeading = ref('')
const tocItems = ref(parseTocFromMarkdown(props.content))

let zoomInstance: ReturnType<typeof mediumZoom> | null = null

async function renderPreview() {
  if (!previewRef.value) return
  previewRef.value.innerHTML = ''
  await Vditor.preview(previewRef.value, props.content || '', buildPreviewOptions(props.theme))
  setupImageProxy()
  setupImageZoom()
  setupCodeCopy()
  tocItems.value = parseTocFromMarkdown(props.content)
}

function setupImageProxy() {
  nextTick(() => {
    const preview = previewRef.value
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
    const preview = previewRef.value
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
    const preview = previewRef.value
    if (!preview) return
    preview.querySelectorAll('pre').forEach((pre) => {
      if (pre.querySelector('.ph-code-copy')) return
      const btn = document.createElement('button')
      btn.className = 'ph-code-copy'
      btn.type = 'button'
      btn.textContent = '复制'
      btn.addEventListener('click', () => {
        const code = pre.querySelector('code')?.textContent ?? ''
        navigator.clipboard.writeText(code)
        btn.textContent = '已复制'
        setTimeout(() => { btn.textContent = '复制' }, 1500)
      })
      pre.style.position = 'relative'
      pre.appendChild(btn)
    })
  })
}

function onScroll() {
  emit('scroll')
}

function scrollToHeading(id: string) {
  activeHeading.value = id
  const el = previewRef.value?.querySelector(`#${CSS.escape(id)}`)
    ?? document.getElementById(id)
  el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

watch(() => [props.content, props.theme] as const, () => {
  renderPreview()
})

onMounted(() => {
  renderPreview()
})

onBeforeUnmount(() => {
  zoomInstance?.detach()
})

defineExpose({ scrollEl: scrollRef })
</script>

<template>
  <div ref="scrollRef" class="note-markdown-preview" @scroll="onScroll">
    <aside v-if="showToc && tocItems.length" class="preview-toc-wrap">
      <PreviewToc
        :items="tocItems"
        :active-id="activeHeading"
        @select="scrollToHeading"
      />
    </aside>
    <div class="preview-main">
      <h1 v-if="title" class="preview-title">{{ title }}</h1>
      <div v-if="showMeta && (categoryNames || tagNames || readingTimeText || savedTimeText)" class="preview-meta">
        <span v-if="categoryNames" class="preview-meta-item">📂 {{ categoryNames }}</span>
        <span v-if="tagNames" class="preview-meta-item">🏷 {{ tagNames }}</span>
        <span v-if="readingTimeText" class="preview-meta-item">👁 {{ readingTimeText }}</span>
        <span v-if="savedTimeText" class="preview-meta-item">💾 {{ savedTimeText }}</span>
      </div>
      <div
        :id="editorId"
        ref="previewRef"
        class="vditor-preview-body"
      />
    </div>
  </div>
</template>

<style scoped>
.note-markdown-preview {
  display: flex;
  gap: 24px;
  height: 100%;
  overflow-y: auto;
  padding: 24px 48px 48px;
}
.preview-toc-wrap {
  flex-shrink: 0;
  width: 200px;
  position: sticky;
  top: 0;
  align-self: flex-start;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}
.preview-main {
  flex: 1;
  min-width: 0;
  max-width: 900px;
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
.note-markdown-preview :deep(.vditor-reset) {
  background: transparent !important;
  color: var(--text-primary);
  font-size: 18px;
  line-height: 1.8;
  padding: 0 !important;
}
.note-markdown-preview :deep(.vditor-reset img) {
  max-width: var(--image-max-width, 80%);
  height: auto;
}
.note-markdown-preview :deep(.ph-code-copy) {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  font-size: 11px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
}
@media (max-width: 768px) {
  .note-markdown-preview {
    padding: 16px 20px 32px;
    flex-direction: column;
  }
  .preview-toc-wrap {
    width: 100%;
    position: static;
    max-height: none;
  }
  .preview-title {
    font-size: 28px;
  }
}
</style>

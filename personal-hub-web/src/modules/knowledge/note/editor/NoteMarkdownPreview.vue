<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import PreviewToc from '../preview/PreviewToc.vue'
import { parseTocFromMarkdown, assignPreviewHeadingIds } from './parseToc'
import { buildPreviewOptions } from './vditorSetup'
import { preparePreviewMarkdown, setupPreviewImageZoom } from './previewEnhancements'
import { useFeatureFlagStore } from '@/store/featureFlagStore'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

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
const featureFlags = useFeatureFlagStore()
const router = useRouter()

const scrollRef = ref<HTMLElement | null>(null)
const previewRef = ref<HTMLDivElement | null>(null)
const activeHeading = ref('')
const tocItems = ref(parseTocFromMarkdown(props.content))

let disposeZoom: (() => void) | null = null
let renderSeq = 0

async function renderPreview() {
  if (!previewRef.value) return
  const seq = ++renderSeq
  disposeZoom?.()
  disposeZoom = null
  previewRef.value.innerHTML = ''
  const md = await preparePreviewMarkdown(props.content || '', featureFlags.isEnabled('backlink'))
  if (seq !== renderSeq || !previewRef.value) return
  await Vditor.preview(previewRef.value, md, buildPreviewOptions(props.theme))
  if (seq !== renderSeq || !previewRef.value) return
  await nextTick()
  disposeZoom = await setupPreviewImageZoom(previewRef.value, props.noteId)
  if (seq !== renderSeq) {
    disposeZoom?.()
    disposeZoom = null
    return
  }
  setupCodeCopy()
  setupWikiMissingClicks()
  assignPreviewHeadingIds(previewRef.value)
  tocItems.value = parseTocFromMarkdown(props.content)
}

function setupWikiMissingClicks() {
  nextTick(() => {
    const preview = previewRef.value
    if (!preview) return
    preview.querySelectorAll('a[href^="#wiki-missing:"]').forEach((a) => {
      a.classList.add('wiki-link--missing')
      a.addEventListener('click', (e) => {
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
  const root = previewRef.value
  const container = scrollRef.value
  if (!root || !container) return
  const el = root.querySelector(`#${CSS.escape(id)}`) as HTMLElement | null
  if (!el) return
  // 在预览滚动容器内定位（避免只滚 window）
  const containerRect = container.getBoundingClientRect()
  const elRect = el.getBoundingClientRect()
  const nextTop = container.scrollTop + (elRect.top - containerRect.top) - 12
  container.scrollTo({ top: Math.max(0, nextTop), behavior: 'smooth' })
}

watch(() => [props.content, props.theme] as const, () => {
  renderPreview()
})

onMounted(() => {
  renderPreview()
})

onBeforeUnmount(() => {
  renderSeq += 1
  disposeZoom?.()
  disposeZoom = null
})

defineExpose({ scrollEl: scrollRef, scrollToHeading })
</script>

<template>
  <div ref="scrollRef" class="note-markdown-preview" @scroll="onScroll">
    <aside v-if="showToc && tocItems.length" class="preview-toc-wrap">
      <PreviewToc
        :items="tocItems"
        :active-id="activeHeading"
        @scroll-to="scrollToHeading"
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
  padding: 24px 0 48px;
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
  max-width: 100%;
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
.note-markdown-preview :deep(a.wiki-link--missing) {
  color: var(--text-tertiary);
  text-decoration: line-through;
  cursor: not-allowed;
}
</style>

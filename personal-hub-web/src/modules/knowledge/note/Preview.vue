<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import mediumZoom from 'medium-zoom'
import type { NoteVO } from '@/types/note'
import { getNotePreview, restoreNote, exportNote } from '@/modules/knowledge/api'
import { usePreviewSettings } from './preview/usePreviewSettings'
import { usePreviewTheme } from './preview/usePreviewTheme'
import DocLayout from '@/components/DocLayout.vue'
import type { TocItem } from './preview/PreviewToc.vue'
import ReadingSettings from './preview/ReadingSettings.vue'

const route = useRoute()
const { settings } = usePreviewSettings()
const { theme, resolvedTheme, setTheme } = usePreviewTheme()

const note = ref<NoteVO | null>(null)
const loading = ref(true)
const error = ref('')
const activeHeading = ref('')
const toc = ref<TocItem[]>([])

/** 非空后的 note，用于 slot 内类型缩窄 */
const loadedNote = computed(() => note.value as NoteVO)

let observer: IntersectionObserver | null = null
let zoomInstance: ReturnType<typeof mediumZoom> | null = null

const isTrash = computed(() => note.value?.isDeleted === 1)

/** MdPreview 只接受 light/dark，sepia 映射为 light */
const mdTheme = computed(() => resolvedTheme.value === 'sepia' ? 'light' : resolvedTheme.value)

function formatTime(dateStr?: string) {
  if (!dateStr) return ''
  return dateStr.slice(0, 16).replace('T', ' ')
}

/** 估算阅读时间 */
function estimateReadingTime(content?: string): string {
  if (!content) return ''
  const zhChars = (content.match(/[一-鿿]/g) || []).length
  const words = content.replace(/[一-鿿]/g, '').split(/\s+/).filter(Boolean).length
  const total = zhChars + words
  const mins = Math.max(1, Math.round(total / 500))
  return `${mins} 分钟`
}

// ====== 目录 ======

function parseToc(content: string): TocItem[] {
  const items: TocItem[] = []
  const lines = content.split('\n')
  for (const line of lines) {
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      const id = text.replace(/\s+/g, '-')
      items.push({ text, level, id })
    }
  }
  return items
}

function scrollToHeading(id: string) {
  activeHeading.value = id
  // 直接用 id 查找（setupHeadingAnchors 阶段已设置 h.id）
  const target = document.getElementById(id)
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function setupObserver() {
  if (!note.value?.content) return
  setTimeout(() => {
    const preview = document.querySelector('.md-editor-preview')
    if (!preview) return

    const headingEls = preview.querySelectorAll('h1, h2, h3, h4, h5, h6')
    if (!headingEls.length) return

    observer?.disconnect()
    observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            const text = entry.target.textContent?.trim() ?? ''
            const id = text.replace(/\s+/g, '-')
            activeHeading.value = id
          }
        }
      },
      { rootMargin: '-64px 0px -60% 0px', threshold: 0 }
    )
    headingEls.forEach((el) => observer!.observe(el))
  }, 200)
}

// ====== 图片缩放 ======

function setupImageZoom() {
  nextTick(() => {
    const preview = document.querySelector('.md-editor-preview')
    if (!preview) return

    zoomInstance?.detach()
    zoomInstance = mediumZoom(preview.querySelectorAll('img'), {
      background: 'rgba(0, 0, 0, 0.6)',
      margin: 40,
    })
  })
}

// ====== 代码复制 ======

function setupCodeCopy() {
  nextTick(() => {
    const preview = document.querySelector('.md-editor-preview')
    if (!preview) return

    preview.querySelectorAll('pre').forEach((pre) => {
      // 避免重复添加
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
  })
}

// ====== 数据加载 ======

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) {
    error.value = '笔记ID无效'
    loading.value = false
    return
  }
  try {
    const res = await getNotePreview(id)
    note.value = res.data.data
    if (note.value?.content) {
      toc.value = parseToc(note.value.content)
    }
    if (note.value?.title) {
      document.title = note.value.title + ' — 预览'
    }
  } catch {
    error.value = '笔记不存在或无权访问'
  } finally {
    loading.value = false
  }
})

// ====== 链接新窗口打开 ======

function setupExternalLinks() {
  nextTick(() => {
    const preview = document.querySelector('.md-editor-preview')
    if (!preview) return
    preview.querySelectorAll('a').forEach((a) => {
      if (!a.getAttribute('target')) {
        a.setAttribute('target', '_blank')
        a.setAttribute('rel', 'noopener noreferrer')
      }
    })
  })
}

// ====== 标题锚点 ======

function setupHeadingAnchors() {
  nextTick(() => {
    const preview = document.querySelector('.md-editor-preview')
    if (!preview) return
    preview.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach((h) => {
      if (h.querySelector('.heading-anchor')) return
      const id = h.textContent?.trim().replace(/\s+/g, '-') ?? ''
      if (id) {
        h.id = id
        const anchor = document.createElement('a')
        anchor.className = 'heading-anchor'
        anchor.href = `#${id}`
        anchor.textContent = '#'
        anchor.setAttribute('aria-hidden', 'true')
        h.insertBefore(anchor, h.firstChild)
      }
    })
  })
}

// 内容加载后设置
watch(() => note.value?.content, async () => {
  if (note.value?.content) {
    await nextTick()
    setupObserver()
    setupImageZoom()
    setupCodeCopy()
    setupExternalLinks()
    setupHeadingAnchors()
  }
})

// 主题切换时重新绑定 zoom（zoom 需要新图片引用）
watch(resolvedTheme, () => {
  setupImageZoom()
})

// ====== 操作 ======

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
  } catch {
    // 业务异常已由全局拦截器处理
  }
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

  <DocLayout v-else-if="note"
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
      <!-- Header 右侧操作区 -->
      <template #header-actions>
        <el-popover
          placement="bottom-end"
          :width="240"
          trigger="click"
          :show-arrow="false"
          popper-class="preview-settings-popper"
        >
          <template #reference>
            <button class="header-action-btn" title="阅读设置">Aa</button>
          </template>
          <ReadingSettings
            :settings="settings"
            :theme="theme"
            @update:settings="Object.assign(settings, $event)"
            @update:theme="setTheme($event as any)"
          />
        </el-popover>
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

      <!-- 正文 -->
      <div
        class="preview-content-wrap"
        :style="{
          maxWidth: settings.readingWidth + 'px',
          fontSize: settings.fontSize + 'px',
          lineHeight: settings.lineHeight,
          '--prose-font-size': settings.fontSize + 'px',
          '--prose-line-height': settings.lineHeight,
        }"
      >
        <!-- 分类/标签 -->
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

        <!-- Markdown 正文 -->
        <div v-if="loadedNote.content" class="preview-content markdown-prose">
          <MdPreview
            :model-value="loadedNote.content"
            :theme="mdTheme"
            :show-code-row-number="false"
          />
        </div>
        <div v-else class="empty-content">笔记内容为空</div>
      </div>
    </DocLayout>
</template>

<style scoped>
/* ====== 状态消息 ====== */
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
  transition: color var(--transition), border-color var(--transition);
}

.back-btn:hover {
  color: var(--text-primary);
  border-color: var(--text-tertiary);
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

/* ==== 正文区域 ==== */
.preview-content-wrap {
  margin: 0 auto;
  padding: 0;
  color: var(--preview-text);
}

/* 安全区边距统一由 DocLayout .doc-content 提供 */

/* 分类/标签行 */
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

/* Header 右侧操作按钮 */
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
  font-size: var(--text-sm);
  transition: color var(--transition), background var(--transition);
}

.header-action-btn:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

/* MdPreview 根节点与内部容器：背景透明 / 边距 / 字号 / 颜色由阅读主题驱动 */
:deep(.md-editor) {
  background: transparent !important;
}

:deep(.md-editor-preview) {
  background: transparent !important;
  padding-inline: 48px !important;
  padding-block-start: 4px !important;
  /* 基准字号 — 子元素的 em 单位会基于此缩放 */
  font-size: var(--prose-font-size, 16px) !important;
  word-break: break-word !important;
  /* 正文与标题颜色跟随阅读主题 */
  color: var(--prose-color) !important;
}

/* default-theme 内部元素硬编码了 line-height / word-break / 标题颜色，统一覆盖 */
:deep(.md-editor-preview p),
:deep(.md-editor-preview li),
:deep(.md-editor-preview blockquote) {
  line-height: inherit !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
}

:deep(.md-editor-preview h1),
:deep(.md-editor-preview h2),
:deep(.md-editor-preview h3),
:deep(.md-editor-preview h4),
:deep(.md-editor-preview h5),
:deep(.md-editor-preview h6) {
  word-break: break-word !important;
  color: var(--prose-heading-color) !important;
}

/* === 宽元素突破正文宽度 ===
   图片、Mermaid 可以撑满容器，通过负 margin 抵消父级 padding-inline: 48px */

/* 行内代码不额外缩进 */
:deep(.md-editor-preview code) {
  padding-left: 0 !important;
  padding-right: 0 !important;
}

:deep(.md-editor-preview pre) {
  padding: 0 !important;
}

:deep(.md-editor-preview figure) {
  margin-left: -48px !important;
  margin-right: -48px !important;
  max-width: calc(100% + 96px) !important;
}
</style>

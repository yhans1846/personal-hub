<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getNotePreview, restoreNote, exportNote } from '@/api/noteApi'
import { ElMessage } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { RotateCcw, Download, ArrowLeft, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import type { NoteVO } from '@/types/note'

const route = useRoute()

const note = ref<NoteVO | null>(null)
const loading = ref(true)
const error = ref('')
const activeHeading = ref('')

// 左侧目录栏宽度（可拖拽）
const tocWidth = ref(220)
const isResizing = ref(false)
const tocCollapsed = ref(false)
const TOC_MIN = 140
const TOC_MAX = 500

function toggleToc() {
  tocCollapsed.value = !tocCollapsed.value
}

function startResize(e: MouseEvent) {
  isResizing.value = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  const startX = e.clientX
  const startW = tocWidth.value

  const onMove = (ev: MouseEvent) => {
    const delta = ev.clientX - startX
    tocWidth.value = Math.min(TOC_MAX, Math.max(TOC_MIN, startW + delta))
  }
  const onUp = () => {
    isResizing.value = false
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

const editorTheme = computed(() =>
  document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light'
)

const isTrash = computed(() => note.value?.isDeleted === 1)
const bannerText = computed(() =>
  isTrash.value ? '该笔记位于回收站，仅支持查看。' : '预览模式'
)
const bannerType = computed(() => isTrash.value ? 'warning' : 'info')

/** 从 Markdown 提取标题大纲 */
interface TocItem {
  text: string
  level: number
  id: string
}
const toc = ref<TocItem[]>([])

function parseToc(content: string): TocItem[] {
  const items: TocItem[] = []
  const lines = content.split('\n')
  for (const line of lines) {
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      // 用 text 本身作为 key，通过 DOM 查找
      const id = text.replace(/\s+/g, '-')
      items.push({ text, level, id })
    }
  }
  return items
}

function scrollToHeading(id: string) {
  activeHeading.value = id
  const preview = document.querySelector('.md-editor-preview')
  if (!preview) return
  // 在渲染区域找到文本匹配的标题
  const allHeadings = preview.querySelectorAll('h1, h2, h3, h4, h5, h6')
  for (const h of allHeadings) {
    if (h.textContent?.trim() === id.replace(/-/g, ' ')) {
      h.scrollIntoView({ behavior: 'smooth', block: 'start' })
      break
    }
  }
}

let observer: IntersectionObserver | null = null

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
    headingEls.forEach(el => observer!.observe(el))
  }, 100)
}

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

// 内容加载后延迟设置滚动观察器
watch(() => note.value?.content, async () => {
  if (note.value?.content) {
    await nextTick()
    setTimeout(setupObserver, 200)
  }
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
  } catch {
    // 业务异常已由全局拦截器处理
  }
}

function handleClose() {
  window.close()
}
</script>

<template>
  <div class="preview-layout">
    <!-- 顶部提示条 -->
    <div class="preview-banner" :class="`banner--${bannerType}`">
      <div class="banner-left">
        <el-button size="small" text @click="handleClose">
          <ArrowLeft :size="15" style="margin-right: 4px" /> 返回
        </el-button>
      </div>
      <div class="banner-content">
        <span class="banner-icon">{{ isTrash ? '⚠' : '👁' }}</span>
        <span>{{ bannerText }}</span>
      </div>
      <div class="banner-actions">
        <el-button size="small" :icon="Download" @click="handleExport">
          导出
        </el-button>
        <el-button v-if="isTrash" size="small" type="warning" :icon="RotateCcw" @click="handleRestore">
          恢复笔记
        </el-button>
      </div>
    </div>

    <!-- 加载态 -->
    <div v-if="loading" class="state-message">加载中...</div>

    <!-- 错误态 -->
    <div v-else-if="error" class="state-message state-error">
      <p>{{ error }}</p>
      <el-button size="small" @click="handleClose">关闭页面</el-button>
    </div>

    <!-- 笔记内容 + 大纲 -->
    <div v-else-if="note" class="preview-body">
      <!-- 左侧大纲 -->
      <div class="toc-wrapper" :class="{ collapsed: tocCollapsed }" :style="{ width: tocCollapsed ? '0px' : tocWidth + 'px' }">
        <aside v-if="toc.length > 0" class="preview-toc">
          <div class="toc-header">目录</div>
          <nav class="toc-list">
            <button
              v-for="item in toc"
              :key="item.id"
              :class="['toc-item', `toc-level-${item.level}`, { active: activeHeading === item.id }]"
              @click="scrollToHeading(item.id)"
            >
              <span class="toc-dot" />
              <span class="toc-text">{{ item.text }}</span>
            </button>
          </nav>
        </aside>
        <!-- 占位（无大纲时） -->
        <aside v-else class="preview-toc preview-toc--empty">
          <div class="toc-header">目录</div>
          <p class="toc-empty">暂无标题</p>
        </aside>
        <button class="toc-toggle" @click="toggleToc">
          <ChevronLeft v-if="!tocCollapsed" :size="14" />
          <ChevronRight v-else :size="14" />
        </button>
      </div>
      <!-- 拖拽分隔条 -->
      <div
        class="toc-resize-handle"
        :class="{ active: isResizing }"
        @mousedown.prevent="startResize"
      />

      <!-- 右侧正文 -->
      <main class="preview-main">
        <article class="preview-article">
          <h1 class="preview-title">{{ note.title }}</h1>

          <div class="preview-meta">
            <span v-if="note.categories?.length" class="meta-block">
              <span class="meta-label">分类</span>
              <span v-for="c in note.categories" :key="c.id" class="meta-tag">{{ c.name }}</span>
            </span>
            <span v-if="note.tags?.length" class="meta-block">
              <span class="meta-label">标签</span>
              <span v-for="t in note.tags" :key="t.id" class="meta-tag" :style="t.color ? { '--tag-color': t.color } : undefined">{{ t.name }}</span>
            </span>
          </div>

          <div class="preview-divider" />

          <div class="preview-content" v-if="note.content">
            <MdPreview
              :model-value="note.content"
              :theme="editorTheme"
              :show-code-row-number="false"
            />
          </div>
          <div v-else class="empty-content">笔记内容为空</div>
        </article>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* ====== 整体布局 ====== */
.preview-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* ====== 顶部提示条 ====== */
.preview-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--sp-4);
  padding: var(--sp-3) var(--sp-5);
  flex-shrink: 0;
}

.banner-left {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.banner-left .el-button { color: var(--text-secondary); }
.banner-left .el-button:hover { color: var(--text-primary); }

.banner--warning {
  background: color-mix(in srgb, var(--warning) 12%, transparent);
  border-bottom: 1px solid color-mix(in srgb, var(--warning) 30%, transparent);
}

.banner--info {
  background: color-mix(in srgb, var(--accent) 8%, transparent);
  border-bottom: 1px solid color-mix(in srgb, var(--accent) 16%, transparent);
}

.banner-content {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  font-size: var(--text-sm);
  font-weight: 500;
}

.banner--warning .banner-content { color: var(--warning); }
.banner--info .banner-content { color: var(--accent); }

.banner-icon { font-size: 16px; }

.banner-actions {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

/* ====== 状态消息 ====== */
.state-message {
  text-align: center;
  padding: 64px 0;
  color: var(--text-tertiary);
}
.state-error { color: var(--danger); }

/* ====== 主体区域（大纲 + 正文） ====== */
.preview-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ====== 左侧大纲 ====== */
.toc-wrapper {
  display: flex;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  transition: width 0.2s ease;
}

.toc-wrapper.collapsed {
  border-right: none;
}

.preview-toc {
  flex: 1;
  overflow-y: auto;
  padding: var(--sp-6) var(--sp-4);
  background: var(--bg-card);
  scrollbar-width: none;
}

.preview-toc::-webkit-scrollbar {
  display: none;
}

.toc-wrapper:hover .preview-toc {
  scrollbar-width: thin;
}

.toc-wrapper:hover .preview-toc::-webkit-scrollbar {
  display: block;
  width: 4px;
}

.toc-wrapper:hover .preview-toc::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 2px;
}

.toc-toggle {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 48px;
  border: none;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-right: none;
  border-radius: 4px 0 0 4px;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: color var(--transition), background var(--transition);
}

.toc-wrapper.collapsed .toc-toggle {
  right: -16px;
  border: 1px solid var(--border-color);
  border-radius: 0 4px 4px 0;
}

.toc-toggle:hover {
  color: var(--accent);
  background: var(--bg-hover);
}

.toc-resize-handle {
  width: 4px;
  cursor: col-resize;
  flex-shrink: 0;
  background: transparent;
  transition: background 0.15s;
  position: relative;
  z-index: 1;
}

.toc-resize-handle:hover,
.toc-resize-handle.active {
  background: var(--accent);
}

.preview-toc--empty {
  display: flex;
  flex-direction: column;
}

.toc-header {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: var(--sp-3);
  padding: 0 var(--sp-2);
}

.toc-empty {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  padding: 0 var(--sp-2);
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.toc-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: var(--sp-1) var(--sp-2);
  font-size: var(--text-sm);
  line-height: 1.5;
  color: var(--text-tertiary);
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  text-align: left;
  width: 100%;
  transition: color var(--transition), background var(--transition);
}

.toc-item:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

.toc-item.active {
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

.toc-dot {
  display: inline-block;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: currentColor;
  margin-top: 6px;
  flex-shrink: 0;
}

.toc-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 缩进 */
.toc-level-1 { padding-left: var(--sp-2); }
.toc-level-2 { padding-left: calc(var(--sp-2) + var(--sp-3)); }
.toc-level-3 { padding-left: calc(var(--sp-2) + var(--sp-6)); }
.toc-level-4 { padding-left: calc(var(--sp-2) + var(--sp-9)); }
.toc-level-5 { padding-left: calc(var(--sp-2) + var(--sp-12)); }
.toc-level-6 { padding-left: calc(var(--sp-2) + var(--sp-15)); }

/* ====== 右侧正文 ====== */
.preview-main {
  flex: 1;
  overflow-y: auto;
  padding: var(--sp-6);
}

.preview-article {
  max-width: none;
  margin: 0 auto;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--sp-8);
}

.preview-title {
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1.3;
  color: var(--text-primary);
  margin: 0 0 var(--sp-4);
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-4);
  font-size: var(--text-xs);
}

.meta-block {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-label { color: var(--text-tertiary); }

.meta-tag {
  display: inline-block;
  padding: 0 6px;
  line-height: 1.6;
  background: color-mix(in srgb, var(--accent) 10%, transparent);
  color: var(--accent);
  border-radius: var(--radius-sm);
}

.meta-tag[style] {
  background: color-mix(in srgb, var(--tag-color) 15%, transparent);
  color: var(--tag-color);
}

.preview-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--sp-5) 0;
}

.preview-content {
  font-size: var(--text-sm);
  line-height: 1.75;
  color: var(--text-primary);
}

.empty-content {
  text-align: center;
  padding: 48px 0;
  color: var(--text-tertiary);
}

/* 暗色模式下的 Markdown 预览调整 */
:deep(.md-editor-preview) {
  background: transparent !important;
}
</style>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getNotePreview, restoreNote, exportNote } from '@/api/noteApi'
import { ElMessage } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { RotateCcw, Download, ArrowLeft } from 'lucide-vue-next'
import type { NoteVO } from '@/types/note'

const route = useRoute()

const note = ref<NoteVO | null>(null)
const loading = ref(true)
const error = ref('')
const activeHeading = ref('')

// 左侧目录栏宽度（可拖拽）
const tocWidth = ref(220)
const isResizing = ref(false)
const TOC_MIN = 140
const TOC_MAX = 500

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
    <!-- 顶部栏 -->
    <header class="preview-topbar">
      <div class="topbar-left">
        <button class="topbar-back" @click="handleClose">
          <ArrowLeft :size="15" /> 返回
        </button>
      </div>
      <div class="topbar-center">
        <span class="topbar-title">{{ note?.title || '加载中...' }}</span>
      </div>
      <div class="topbar-right">
        <template v-if="isTrash">
          <span class="trash-badge">回收站</span>
          <el-button size="small" type="warning" :icon="RotateCcw" @click="handleRestore">
            恢复
          </el-button>
        </template>
        <el-button size="small" :icon="Download" @click="handleExport">
          导出
        </el-button>
      </div>
    </header>

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
      <aside v-if="toc.length > 0" class="preview-toc" :style="{ width: tocWidth + 'px' }">
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
      <aside v-else class="preview-toc preview-toc--empty" :style="{ width: tocWidth + 'px' }">
        <div class="toc-header">目录</div>
        <p class="toc-empty">暂无标题</p>
      </aside>
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
  background: var(--bg-body);
}

/* ====== 顶部栏 ====== */
.preview-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--sp-4);
  padding: var(--sp-2) var(--sp-5);
  flex-shrink: 0;
  border-bottom: 1px solid var(--border-light);
  -webkit-app-region: drag;
}

.topbar-left { display: flex; align-items: center; -webkit-app-region: no-drag; }
.topbar-center { flex: 1; text-align: center; min-width: 0; }
.topbar-right { display: flex; align-items: center; gap: var(--sp-2); -webkit-app-region: no-drag; }

.topbar-back {
  display: flex; align-items: center; gap: var(--sp-1);
  background: none; border: none;
  color: var(--text-secondary); font-size: var(--text-sm);
  cursor: pointer; padding: 4px 8px; border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.topbar-back:hover { background: var(--bg-hover); color: var(--text-primary); }

.topbar-title {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trash-badge {
  font-size: 11px;
  color: var(--warning);
  background: color-mix(in srgb, var(--warning) 12%, transparent);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

/* ====== 状态消息 ====== */
.state-message {
  text-align: center;
  padding: 64px 0;
  color: var(--text-tertiary);
}
.state-error { color: var(--danger); }

/* ====== 主体区域 ====== */
.preview-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ====== 左侧目录 ====== */
.preview-toc {
  flex-shrink: 0;
  overflow-y: auto;
  padding: var(--sp-8) var(--sp-4);
  border-right: 1px solid var(--border-light);
}

.toc-resize-handle { display: none; }

.preview-toc--empty {
  display: flex;
  flex-direction: column;
}

.toc-header {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: var(--sp-4);
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
  gap: 0;
}

.toc-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 3px var(--sp-2);
  font-size: var(--text-xs);
  line-height: 1.6;
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
  background: color-mix(in srgb, var(--accent) 6%, transparent);
}

.toc-item.active {
  color: var(--accent);
  background: transparent;
  font-weight: 500;
}

.toc-dot {
  display: none;
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
  padding: var(--sp-10) var(--sp-12);
}

.preview-article {
  max-width: 720px;
  margin: 0 auto;
}

.preview-title {
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.3;
  color: var(--text-primary);
  margin: 0 0 var(--sp-6);
  letter-spacing: -0.02em;
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-4);
  font-size: var(--text-xs);
  margin-bottom: var(--sp-6);
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
  background: var(--border-light);
  margin: var(--sp-6) 0;
}

.preview-content {
  font-size: var(--text-base);
  line-height: 1.8;
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
  padding: 0 !important;
}

:deep(.md-editor-preview h1) { font-size: 1.75rem; font-weight: 700; margin-top: 2em; margin-bottom: 0.5em; padding-bottom: 0; border-bottom: none; }
:deep(.md-editor-preview h2) { font-size: 1.35rem; font-weight: 600; margin-top: 1.8em; margin-bottom: 0.4em; }
:deep(.md-editor-preview h3) { font-size: 1.1rem; font-weight: 600; margin-top: 1.5em; margin-bottom: 0.3em; }
:deep(.md-editor-preview p) { margin: 1em 0; }
:deep(.md-editor-preview code) { font-size: 0.85em; }
:deep(.md-editor-preview pre) { background: var(--bg-hover); border: 1px solid var(--border-light); border-radius: var(--radius-md); padding: var(--sp-4); overflow-x: auto; }
:deep(.md-editor-preview blockquote) { border-left: 3px solid var(--accent); padding-left: var(--sp-4); color: var(--text-secondary); margin: 1em 0; }
:deep(.md-editor-preview img) { border-radius: var(--radius-md); max-width: 100%; }
:deep(.md-editor-preview table) { border-collapse: collapse; width: 100%; font-size: var(--text-sm); }
:deep(.md-editor-preview th), :deep(.md-editor-preview td) { border: 1px solid var(--border-light); padding: var(--sp-2) var(--sp-3); text-align: left; }
:deep(.md-editor-preview th) { background: var(--bg-hover); font-weight: 600; }
:deep(.md-editor-preview hr) { border: none; border-top: 1px solid var(--border-light); margin: 2em 0; }
:deep(.md-editor-preview ul), :deep(.md-editor-preview ol) { padding-left: 1.5em; }
:deep(.md-editor-preview li) { margin: 0.4em 0; }
</style>

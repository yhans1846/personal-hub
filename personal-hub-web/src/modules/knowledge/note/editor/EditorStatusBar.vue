<script setup lang="ts">
import { computed } from 'vue'
import { estimateReadingTime } from '@/utils/readingTime'
import type { SaveStatus } from './useAutoSave'
import type { EditorMode } from './useEditorMode'

const props = defineProps<{
  content: string
  mode: EditorMode
  saveStatus: SaveStatus
  lastSavedAt?: number | null
  isFullscreen?: boolean
}>()

const wordCount = computed(() => {
  const text = props.content || ''
  const zh = (text.match(/[一-鿿]/g) || []).length
  const en = text.replace(/[一-鿿]/g, '').split(/\s+/).filter(Boolean).length
  return zh + en
})

const paragraphCount = computed(() => {
  const text = props.content || ''
  if (!text.trim()) return 0
  return text.split(/\n\s*\n/).filter(Boolean).length
})

const readingTime = computed(() => estimateReadingTime(props.content))

const modeLabel = computed(() => {
  switch (props.mode) {
    case 'edit': return '编辑模式'
    case 'preview': return '预览模式'
    case 'focus': return '专注模式'
  }
})

const saveStatusText = computed(() => {
  switch (props.saveStatus) {
    case 'saving': return '正在保存...'
    case 'success': return '✓ 已保存'
    case 'error': return '保存失败'
    case 'dirty': return '未保存'
    default: return ''
  }
})

const saveTimeText = computed(() => {
  if (!props.lastSavedAt) return ''
  const d = new Date(props.lastSavedAt)
  const h = d.getHours().toString().padStart(2, '0')
  const m = d.getMinutes().toString().padStart(2, '0')
  return `最后保存 ${h}:${m}`
})
</script>

<template>
  <footer
    class="editor-statusbar"
    :class="{ 'is-fullscreen': isFullscreen }"
  >
    <div class="statusbar-left">
      <span class="stat-item">{{ wordCount }} 字</span>
      <span class="stat-item separator">{{ paragraphCount }} 段</span>
      <span v-if="readingTime" class="stat-item separator">{{ readingTime }}</span>
      <span v-if="saveTimeText" class="stat-item separator save-time">{{ saveTimeText }}</span>
    </div>
    <div class="statusbar-right">
      <span v-if="saveStatus !== 'idle'" class="stat-item save-status" :class="saveStatus">
        {{ saveStatusText }}
      </span>
      <span class="stat-item separator mode-tag">{{ modeLabel }}</span>
      <span class="stat-item separator lang-tag">Markdown</span>
      <span class="stat-item lang-tag">UTF-8</span>
    </div>
  </footer>
</template>

<style scoped>
.editor-statusbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 32px;
  padding: 0 24px;
  border-top: 1px solid var(--border-color);
  flex-shrink: 0;
  background: var(--bg-card);
  z-index: 10;
  transition: background var(--transition-duration);
}
.editor-statusbar.is-fullscreen {
  background: var(--bg-body);
}
.stat-item {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.stat-item + .stat-item { margin-left: 16px; }
.stat-item.separator::before { content: '·'; margin-right: 16px; }
.mode-tag {
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  background: var(--bg-hover);
}
.lang-tag {
  font-family: var(--font-mono);
  font-size: 10px;
  opacity: 0.6;
}
.save-time {
  font-variant-numeric: tabular-nums;
}
.save-status.saving { color: var(--accent); }
.save-status.success { color: var(--success); }
.save-status.error { color: var(--danger); }
.save-status.dirty { color: var(--warning); }

@media (max-width: 768px) {
  .editor-statusbar {
    padding: 0 12px;
    font-size: 11px;
  }
  .stat-item + .stat-item { margin-left: 8px; }
  .stat-item.separator::before { margin-right: 8px; }
}
</style>

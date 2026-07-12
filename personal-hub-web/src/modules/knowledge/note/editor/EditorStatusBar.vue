<script setup lang="ts">
import { computed } from 'vue'
import { estimateReadingTime } from '@/utils/readingTime'
import type { SaveStatus } from './useAutoSave'
import type { EditorMode } from './useEditorMode'

const props = defineProps<{
  content: string
  mode: EditorMode
  saveStatus: SaveStatus
}>()

const wordCount = computed(() => {
  const text = props.content || ''
  const zh = (text.match(/[一-鿿]/g) || []).length
  const en = text.replace(/[一-鿿]/g, '').split(/\s+/).filter(Boolean).length
  return zh + en
})

const readingTime = computed(() => estimateReadingTime(props.content))

const modeLabel = computed(() => {
  switch (props.mode) {
    case 'edit': return '编辑模式'
    case 'preview': return '预览模式'
    case 'focus': return '专注模式'
  }
})
</script>

<template>
  <footer class="editor-statusbar">
    <div class="statusbar-left">
      <span class="stat-item">字数 {{ wordCount }}</span>
      <span v-if="readingTime" class="stat-item separator">{{ readingTime }}</span>
    </div>
    <div class="statusbar-right">
      <span class="stat-item mode-tag">{{ modeLabel }}</span>
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
}
.stat-item {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}
.stat-item + .stat-item { margin-left: 16px; }
.stat-item.separator::before { content: '·'; margin-right: 16px; }
.mode-tag {
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  background: var(--bg-hover);
}
</style>

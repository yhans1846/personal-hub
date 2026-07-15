<script setup lang="ts">
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import type { ToolbarNames } from 'md-editor-v3'
import { setupMdEditor } from './mdEditorSetup'

setupMdEditor()

const props = withDefaults(defineProps<{
  modelValue: string
  editorId: string
  toolbars: ToolbarNames[]
  theme: 'light' | 'dark'
  placeholder?: string
  compact?: boolean
  onUploadImg?: (files: File[], callback: (urls: string[]) => void) => void
}>(), {
  placeholder: '开始写作...',
  compact: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <MdEditor
    :id="editorId"
    :model-value="modelValue"
    :theme="theme"
    :toolbars="toolbars"
    :preview="false"
    language="zh-CN"
    :placeholder="placeholder"
    preview-theme="github"
    code-theme="atom"
    :show-toolbar-name="false"
    :scroll-auto="true"
    :auto-detect-code="true"
    :tab-width="2"
    :table-shape="[8, 6]"
    :no-mermaid="false"
    :no-katex="false"
    :no-prettier="false"
    :on-upload-img="onUploadImg"
    class="note-md-editor"
    :class="{ 'note-md-editor--compact': compact, 'note-md-editor--focus': toolbars.length === 0 }"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>

<style scoped>
.note-md-editor {
  min-height: 60vh;
}

.note-md-editor--compact {
  min-height: 0;
  height: 100%;
}

.note-md-editor--focus :deep(.md-editor-toolbar),
.note-md-editor--focus :deep(.md-editor-toolbar-wrapper) {
  display: none !important;
}

.note-md-editor :deep(.md-editor) {
  border: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  background: transparent !important;
}

.note-md-editor :deep(.md-editor-toolbar) {
  border: none !important;
  background: transparent !important;
  padding: 0 0 12px 0 !important;
  margin-bottom: 4px;
  flex-wrap: wrap;
  gap: 2px;
}

.note-md-editor :deep(.md-editor-toolbar-item) {
  color: var(--text-tertiary);
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}

.note-md-editor :deep(.md-editor-toolbar-item:hover) {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.note-md-editor :deep(.md-editor-toolbar-item.active) {
  color: var(--accent);
}

.note-md-editor :deep(.md-editor-content) {
  background: transparent !important;
}

.note-md-editor :deep(.md-editor-input) {
  -webkit-font-smoothing: antialiased;
}

.note-md-editor :deep(.cm-editor) {
  background: transparent !important;
}

.note-md-editor :deep(.cm-scroller) {
  font-family: var(--font-sans);
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-primary);
}

.note-md-editor :deep(.md-editor-footer) {
  display: none !important;
}

.note-md-editor--compact :deep(.md-editor-content) {
  height: 100% !important;
}

.note-md-editor--focus {
  min-height: 100%;
  height: 100%;
}

.note-md-editor--focus :deep(.md-editor-content) {
  height: 100% !important;
}
</style>

<script setup lang="ts">
import { computed } from 'vue'
import PreviewToc from '../preview/PreviewToc.vue'
import { parseTocFromMarkdown } from './parseToc'

const props = defineProps<{ content: string; activeId?: string }>()
const emit = defineEmits<{ 'scroll-to': [id: string] }>()

const items = computed(() => parseTocFromMarkdown(props.content))
</script>

<template>
  <aside class="editor-outline" aria-label="大纲">
    <p v-if="items.length === 0" class="outline-empty">暂无标题</p>
    <PreviewToc
      v-else
      :items="items"
      :active-id="activeId ?? ''"
      @scroll-to="emit('scroll-to', $event)"
    />
  </aside>
</template>

<style scoped>
.editor-outline {
  height: 100%;
  overflow: auto;
  border-left: 1px solid var(--border-color);
  background: var(--bg-body);
  padding: var(--sp-8, 8px);
}
.outline-empty {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: var(--sp-12, 12px);
}
.editor-outline :deep(.toc-wrapper) {
  width: 100% !important;
}
</style>

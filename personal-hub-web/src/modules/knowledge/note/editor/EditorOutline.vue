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
    <PreviewToc
      side="right"
      :items="items"
      :active-id="activeId ?? ''"
      @scroll-to="emit('scroll-to', $event)"
    />
  </aside>
</template>

<style scoped>
.editor-outline {
  height: 100%;
  min-height: 0;
  overflow: hidden;
  display: flex;
  /* 宽度随 PreviewToc 展开/收起，勿强制 100% 否则收起后展开钮错位且难找 */
  border-left: none;
  background: transparent;
  padding: 0;
}
.editor-outline :deep(.toc-wrapper) {
  height: 100%;
}
</style>

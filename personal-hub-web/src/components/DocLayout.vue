<script setup lang="ts">
import type { DocMeta } from '@/modules/knowledge/note/preview/PreviewHeader.vue'
import type { TocItem } from '@/modules/knowledge/note/preview/PreviewToc.vue'
import PreviewHeader from '@/modules/knowledge/note/preview/PreviewHeader.vue'
import PreviewToc from '@/modules/knowledge/note/preview/PreviewToc.vue'

defineProps<{
  title: string
  tocItems?: TocItem[]
  activeHeading?: string
  meta?: DocMeta
  isTrash?: boolean
}>()

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'scroll-to-heading', id: string): void
}>()
</script>

<template>
  <div class="doc-layout">
    <PreviewHeader
      :title="title"
      :is-trash="isTrash ?? false"
      :meta="meta"
      @back="emit('back')"
    >
      <template #actions>
        <slot name="header-actions" />
      </template>
    </PreviewHeader>

    <div class="doc-body">
      <PreviewToc
        :items="tocItems ?? []"
        :active-id="activeHeading ?? ''"
        @scroll-to="emit('scroll-to-heading', $event)"
      />

      <main class="doc-content">
        <article class="doc-article markdown-prose">
          <slot />
        </article>
      </main>
    </div>
  </div>
</template>

<style scoped>
.doc-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--preview-bg, var(--bg-body));
}

.doc-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.doc-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--sp-2) 0;
  scroll-behavior: smooth;
}

.doc-article {
  /* 内容居中，具体宽度由子页面控制 */
  margin: 0 auto;
}
</style>

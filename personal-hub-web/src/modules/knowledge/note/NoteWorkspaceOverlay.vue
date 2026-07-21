<script setup lang="ts">
import Editor from './Editor.vue'

defineProps<{
  noteId?: number
}>()

const emit = defineEmits<{
  close: []
  'note-id': [id: number]
}>()
</script>

<template>
  <Teleport to="body">
    <div
      class="note-workspace-overlay"
      role="dialog"
      aria-modal="true"
      aria-label="笔记工作区"
    >
      <Editor
        embedded
        :initial-note-id="noteId"
        @close="emit('close')"
        @note-id="emit('note-id', $event)"
      />
    </div>
  </Teleport>
</template>

<style scoped>
.note-workspace-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000; /* 高于 App 侧栏/顶栏，低于 MessageBox 默认层亦可接受 */
  background: var(--bg-body);
}
</style>

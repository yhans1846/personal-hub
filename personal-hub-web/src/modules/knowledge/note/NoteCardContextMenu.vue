<script setup lang="ts">
import { ref } from 'vue'
import ContextMenu from './editor/context-menu/ContextMenu.vue'
import ContextMenuItem from './editor/context-menu/ContextMenuItem.vue'
import ContextMenuSeparator from './editor/context-menu/ContextMenuSeparator.vue'

export type CardMenuEntry =
  | { type: 'item'; id: string; label: string; danger?: boolean }
  | { type: 'separator' }

defineProps<{
  entries: CardMenuEntry[]
}>()

const emit = defineEmits<{ action: [id: string] }>()

const menu = ref<{ x: number; y: number } | null>(null)

function openAt(e: MouseEvent) {
  e.preventDefault()
  e.stopPropagation()
  menu.value = { x: e.clientX, y: e.clientY }
}

function close() {
  menu.value = null
}

function onAction(id: string) {
  emit('action', id)
  close()
}

defineExpose({ openAt })
</script>

<template>
  <ContextMenu v-if="menu" :position="menu" @close="close">
    <template v-for="(entry, index) in entries" :key="`${entry.type}-${index}`">
      <ContextMenuSeparator v-if="entry.type === 'separator'" />
      <ContextMenuItem
        v-else
        :danger="entry.danger"
        @click="onAction(entry.id)"
      >
        {{ entry.label }}
      </ContextMenuItem>
    </template>
  </ContextMenu>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'

export interface ContextMenuPosition {
  x: number
  y: number
}

const props = withDefaults(defineProps<{
  position: ContextMenuPosition
  minWidth?: number
  maxWidth?: number
}>(), {
  minWidth: 176,
  maxWidth: 280,
})

const emit = defineEmits<{ close: [] }>()

const menuRef = ref<HTMLElement | null>(null)
const adjusted = ref({ ...props.position })

watch(() => props.position, (pos) => {
  adjusted.value = { ...pos }
  nextTick(adjustPosition)
})

function adjustPosition() {
  const el = menuRef.value
  if (!el) return
  const margin = 4
  const width = el.offsetWidth
  const height = el.offsetHeight
  const x = Math.max(margin, Math.min(props.position.x, window.innerWidth - width - margin))
  const preferredY = props.position.y + height + margin > window.innerHeight
    ? props.position.y - height
    : props.position.y
  const y = Math.max(margin, Math.min(preferredY, window.innerHeight - height - margin))
  adjusted.value = { x, y }
}

function onClose() {
  emit('close')
}

function onMenuClick(e: MouseEvent) {
  e.stopPropagation()
}

onMounted(() => {
  window.addEventListener('click', onClose)
  window.addEventListener('scroll', onClose, true)
  nextTick(adjustPosition)
})

onUnmounted(() => {
  window.removeEventListener('click', onClose)
  window.removeEventListener('scroll', onClose, true)
})
</script>

<template>
  <div
    ref="menuRef"
    class="ph-context-menu"
    :style="{ left: `${adjusted.x}px`, top: `${adjusted.y}px`, minWidth: `${minWidth}px`, maxWidth: `${maxWidth}px` }"
    @click="onMenuClick"
    @contextmenu.prevent
  >
    <slot />
  </div>
</template>

<style scoped>
.ph-context-menu {
  position: fixed;
  z-index: 10050;
  background: var(--bg-elevated, var(--bg-card));
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 4px;
  user-select: none;
}
</style>

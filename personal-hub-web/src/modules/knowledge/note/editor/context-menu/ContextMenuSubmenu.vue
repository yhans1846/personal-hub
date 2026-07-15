<script setup lang="ts">
import { ref } from 'vue'
import ContextMenuItem from './ContextMenuItem.vue'

defineProps<{ label: string }>()

const open = ref(false)
</script>

<template>
  <div class="ph-context-submenu" @mouseenter="open = true" @mouseleave="open = false">
    <ContextMenuItem class="submenu-trigger">
      <span>{{ label }}</span>
      <span class="arrow">›</span>
    </ContextMenuItem>
    <div v-show="open" class="submenu-panel">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.ph-context-submenu {
  position: relative;
}
.submenu-trigger {
  justify-content: space-between;
}
.arrow {
  color: var(--text-tertiary);
  font-size: 12px;
}
.submenu-panel {
  position: absolute;
  left: 100%;
  top: 0;
  min-width: 140px;
  margin-left: 4px;
  padding: 4px;
  background: var(--bg-elevated, var(--bg-card));
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  z-index: 1;
}
</style>

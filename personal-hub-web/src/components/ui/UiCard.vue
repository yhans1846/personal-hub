<script setup lang="ts">
withDefaults(defineProps<{
  padding?: string
  hoverable?: boolean
}>(), { padding: 'var(--sp-6)', hoverable: false })
</script>

<template>
  <div
    class="ui-card"
    :class="{ 'ui-card--hoverable': hoverable }"
    :style="{ padding }"
  >
    <slot />
  </div>
</template>

<style scoped>
.ui-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: box-shadow var(--transition), transform var(--transition);
}
.ui-card--hoverable:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
</style>

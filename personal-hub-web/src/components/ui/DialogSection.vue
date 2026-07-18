<script setup lang="ts">
defineProps<{
  /** 小节标签，如「优先级」；不传则只渲染内容区 */
  label?: string
}>()
</script>

<template>
  <div class="dialog-section">
    <div v-if="label" class="dialog-section__label">{{ label }}</div>
    <slot />
  </div>
</template>

<style scoped>
.dialog-section {
  margin-bottom: var(--sp-5);
}

.dialog-section:last-child {
  margin-bottom: 0;
}

.dialog-section__label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 500;
  margin-bottom: var(--sp-3);
  letter-spacing: 0.3px;
  text-transform: uppercase;
}
</style>

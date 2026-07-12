<script setup lang="ts">
defineProps<{
  icon: string
  title: string
  description: string
  hint?: string
}>()
</script>

<template>
  <div class="placeholder-tab">
    <div class="placeholder-content">
      <span class="placeholder-icon">{{ icon }}</span>
      <h3 class="placeholder-title">{{ title }}</h3>
      <p class="placeholder-desc">{{ description }}</p>
      <p v-if="hint" class="placeholder-hint">{{ hint }}</p>
    </div>
  </div>
</template>

<style scoped>
.placeholder-tab {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}
.placeholder-content {
  text-align: center;
  max-width: 360px;
}
.placeholder-icon {
  font-size: 48px;
  display: block;
  margin-bottom: var(--sp-4);
  opacity: 0.6;
}
.placeholder-title {
  margin: 0 0 var(--sp-2);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text-primary);
}
.placeholder-desc {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  line-height: 1.6;
}
.placeholder-hint {
  margin: var(--sp-2) 0 0;
  font-size: var(--text-xs);
  color: var(--text-placeholder);
}
</style>

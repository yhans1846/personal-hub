<script setup lang="ts">
defineProps<{
  label?: string
}>()
</script>

<template>
  <div class="dialog-prop-card">
    <div v-if="label" class="dialog-prop-card__label">{{ label }}</div>
    <div class="dialog-prop-card__body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.dialog-prop-card {
  background: var(--bg-card, var(--bg-elevated, #fff));
  border: 1px solid var(--border-light, var(--border));
  border-radius: var(--radius-lg);
  padding: var(--sp-3);
  min-width: 0;
}

.dialog-prop-card__label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 500;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  margin-bottom: var(--sp-2);
}

.dialog-prop-card__body {
  min-width: 0;
}
</style>

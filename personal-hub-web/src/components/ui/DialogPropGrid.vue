<script setup lang="ts">
withDefaults(defineProps<{
  cols?: 1 | 2
}>(), { cols: 2 })
</script>

<template>
  <div class="dialog-prop-grid" :class="`dialog-prop-grid--cols-${cols}`">
    <slot />
  </div>
</template>

<style scoped>
.dialog-prop-grid {
  display: grid;
  gap: 10px;
  margin-bottom: var(--sp-4);
}

.dialog-prop-grid--cols-1 {
  grid-template-columns: 1fr;
}

.dialog-prop-grid--cols-2 {
  grid-template-columns: 1fr 1fr;
}

@media (max-width: 560px) {
  .dialog-prop-grid--cols-2 {
    grid-template-columns: 1fr;
  }
}
</style>

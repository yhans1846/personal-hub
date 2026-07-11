<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
}>()
</script>

<template>
  <div class="ui-section">
    <div v-if="title || description" class="ui-section__header">
      <h3 v-if="title" class="ui-section__title">{{ title }}</h3>
      <p v-if="description" class="ui-section__desc">{{ description }}</p>
    </div>
    <div class="ui-section__body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.ui-section { margin-bottom: 24px; }
.ui-section__header { margin-bottom: 16px; }
.ui-section__title { font-size: 14px; font-weight: 600; color: var(--text-primary); margin: 0; }
.ui-section__desc { font-size: 13px; color: var(--text-secondary); margin: 4px 0 0; }
.ui-section__body { display: flex; flex-direction: column; gap: 20px; }
</style>

<script setup lang="ts">
defineProps<{
  tags: Array<{ id: number; name: string; color?: string; usageCount?: number }>
}>()
</script>

<template>
  <div v-if="tags.length > 0" class="hot-tags-bar">
    <span class="hot-tags-label">🔥 热门</span>
    <div class="hot-tags-list">
      <span
        v-for="tag in tags"
        :key="tag.id"
        class="hot-tag-item"
      >
        <span class="hot-tag-dot" :style="{ background: tag.color || '#409eff' }" />
        {{ tag.name }}
        <span class="hot-tag-count">{{ tag.usageCount }}</span>
      </span>
    </div>
  </div>
</template>

<style scoped>
.hot-tags-bar {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
  padding: var(--sp-3) var(--sp-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  flex-wrap: wrap;
}
.hot-tags-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--warning);
  white-space: nowrap;
}
.hot-tags-list {
  display: flex;
  gap: var(--sp-2);
  flex-wrap: wrap;
}
.hot-tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 100px;
  font-size: var(--text-xs);
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--bg-hover);
  border: 1px solid var(--border-light);
}
.hot-tag-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.hot-tag-count {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-tertiary);
  background: var(--bg-card);
  padding: 0 5px;
  border-radius: 4px;
  margin-left: 2px;
}
</style>

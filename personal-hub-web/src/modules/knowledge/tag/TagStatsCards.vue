<script setup lang="ts">
defineProps<{
  total: number
  totalUsage: number
  recentAdded: number
}>()
</script>

<template>
  <div class="stats-row">
    <div class="stat-card stat-card--total">
      <span class="stat-value">{{ total }}</span>
      <span class="stat-label">标签总数</span>
    </div>
    <div class="stat-card stat-card--usage">
      <span class="stat-value">{{ totalUsage }}</span>
      <span class="stat-label">引用总次数</span>
    </div>
    <div class="stat-card stat-card--recent">
      <span class="stat-value">+{{ recentAdded }}</span>
      <span class="stat-label">本周新增</span>
    </div>
  </div>
</template>

<style scoped>
/* 共享壳见 styles/stats-row.css */
.stats-row {
  margin-bottom: var(--sp-4);
}
.stat-card--total .stat-value { color: var(--accent); }
.stat-card--usage .stat-value { color: var(--success); }
.stat-card--recent .stat-value { color: var(--warning); }
</style>

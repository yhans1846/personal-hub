<script setup lang="ts">
defineProps<{
  total: number
  inUse: number
  unused: number
}>()
</script>

<template>
  <div class="stats-row">
    <div class="stat-card stat-card--total">
      <span class="stat-value">{{ total }}</span>
      <span class="stat-label">全部分类</span>
    </div>
    <div class="stat-card stat-card--active">
      <span class="stat-value">{{ inUse }}</span>
      <span class="stat-label">正在使用</span>
    </div>
    <div class="stat-card stat-card--inactive">
      <span class="stat-value">{{ unused }}</span>
      <span class="stat-label">未使用</span>
    </div>
  </div>
</template>

<style scoped>
/* 共享壳见 styles/stats-row.css */
.stats-row {
  margin-bottom: var(--sp-5);
}
.stat-card--total .stat-value { color: var(--accent); }
.stat-card--active .stat-value { color: var(--success); }
.stat-card--inactive .stat-value { color: var(--text-tertiary); }
</style>

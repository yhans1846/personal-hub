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
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--sp-3);
  margin-bottom: var(--sp-5);
}
.stat-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: var(--sp-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  transition: all var(--transition);
}
.stat-card:hover {
  box-shadow: var(--shadow-sm);
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 500;
}
.stat-card--total .stat-value { color: var(--accent); }
.stat-card--active .stat-value { color: var(--success); }
.stat-card--inactive .stat-value { color: var(--text-tertiary); }
</style>

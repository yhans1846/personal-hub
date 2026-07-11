<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  icon: Component
  value: string | number
  label: string
  color?: string
  append?: string
}>()
</script>

<template>
  <div
    class="stat-card"
    :style="{
      '--card-color': color || 'var(--accent)',
      '--card-bg': (color || 'var(--accent)') + '15'
    }"
  >
    <div class="stat-icon">
      <component :is="icon" :size="20" />
    </div>
    <div class="stat-body">
      <span class="stat-value">{{ value }}{{ append ? ' ' + append : '' }}</span>
      <span class="stat-label">{{ label }}</span>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-4); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  cursor: pointer; transition: all var(--transition);
}
.stat-card:hover { box-shadow: var(--shadow-lg); border-color: var(--accent-border); transform: translateY(-2px); }
.stat-icon {
  width: 40px; height: 40px; border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  background: var(--card-bg); color: var(--card-color); flex-shrink: 0;
}
.stat-body { display: flex; flex-direction: column; gap: 2px; }
.stat-value { font-size: var(--text-lg); font-weight: 700; color: var(--text-primary); line-height: 1.2; }
.stat-label { font-size: var(--text-xs); color: var(--text-tertiary); }
</style>

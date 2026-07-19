<script setup lang="ts">
import type { StatsVO } from '@/modules/dashboard/api'
import { BookMarked, FileText, CheckSquare, TrendingUp, Clock } from 'lucide-vue-next'
import { changeColor, changeIcon, changeText, formatReadingHours } from './statsFormat'

defineProps<{
  stats: StatsVO
}>()
</script>

<template>
  <div class="kpi-grid">
    <div class="kpi-card">
      <div class="kpi-icon" style="--kpi-color: var(--accent); --kpi-bg: var(--accent-light);">
        <FileText :size="18" />
      </div>
      <div class="kpi-body">
        <div class="kpi-value">{{ stats.noteCount }}</div>
        <div class="kpi-label">笔记</div>
      </div>
      <div class="kpi-change" :style="{ color: changeColor(stats.noteCountChange) }">
        <component :is="changeIcon(stats.noteCountChange)" :size="12" />
        <span>{{ changeText(stats.noteCountChange) }}</span>
      </div>
    </div>

    <div class="kpi-card">
      <div class="kpi-icon" style="--kpi-color: var(--info); --kpi-bg: var(--info-light);">
        <BookMarked :size="18" />
      </div>
      <div class="kpi-body">
        <div class="kpi-value">{{ formatReadingHours(stats.readingHours) }}</div>
        <div class="kpi-label">阅读</div>
      </div>
      <div class="kpi-change" :style="{ color: changeColor(stats.readingHoursChange) }">
        <component :is="changeIcon(stats.readingHoursChange)" :size="12" />
        <span>{{ changeText(stats.readingHoursChange) }}</span>
      </div>
    </div>

    <div class="kpi-card">
      <div class="kpi-icon" style="--kpi-color: var(--success); --kpi-bg: var(--success-light);">
        <CheckSquare :size="18" />
      </div>
      <div class="kpi-body">
        <div class="kpi-value">{{ Math.round(stats.todoCompletionRate) }}<span class="kpi-unit">%</span></div>
        <div class="kpi-label">Todo 完成率</div>
      </div>
      <div class="kpi-change" :style="{ color: changeColor(stats.todoCompletionChange) }">
        <component :is="changeIcon(stats.todoCompletionChange)" :size="12" />
        <span>{{ stats.todoCompletionChange != null && stats.todoCompletionChange > 0 ? '+' : '' }}{{ stats.todoCompletionChange ?? 0 }}pp</span>
      </div>
    </div>

    <div class="kpi-card">
      <div class="kpi-icon" style="--kpi-color: var(--warning); --kpi-bg: var(--warning-light);">
        <TrendingUp :size="18" />
      </div>
      <div class="kpi-body">
        <div class="kpi-value">{{ stats.streakDays }}<span class="kpi-unit">天</span></div>
        <div class="kpi-label">连续学习</div>
      </div>
      <div class="kpi-change" :style="{ color: 'var(--text-tertiary)' }">
        <Clock :size="12" />
        <span>最长 {{ stats.bestStreakDays }} 天</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--sp-4);
}
.kpi-card {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-4) var(--sp-5);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  transition: all var(--transition);
  position: relative;
  overflow: hidden;
}
.kpi-card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  border-color: var(--accent-border);
  transform: translateY(-1px);
}
.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--kpi-bg, var(--accent-light));
  color: var(--kpi-color, var(--accent));
  flex-shrink: 0;
}
.kpi-body { flex: 1; min-width: 0; }
.kpi-value {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  letter-spacing: -0.02em;
}
.kpi-unit {
  font-size: var(--text-base);
  font-weight: 500;
  color: var(--text-tertiary);
  margin-left: 1px;
}
.kpi-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin-top: 1px;
}
.kpi-change {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: var(--text-xs);
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
  position: absolute;
  top: var(--sp-3);
  right: var(--sp-4);
}
@media (max-width: 1024px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .kpi-grid { grid-template-columns: 1fr 1fr; }
  .kpi-card { padding: var(--sp-3); }
  .kpi-change { position: static; }
}
</style>

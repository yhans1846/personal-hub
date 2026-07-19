<script setup lang="ts">
import { ref, watch, nextTick, onUnmounted, type Ref } from 'vue'
import type { StatsVO } from '@/modules/dashboard/api'
import { BookOpen, FileText, CheckSquare, Layers } from 'lucide-vue-next'
import { useStatsCharts } from './useStatsCharts'

const props = defineProps<{
  stats: StatsVO
  hasCard: (code: string) => boolean
}>()

const catTagTab = defineModel<'category' | 'tag'>('catTagTab', { default: 'category' })

const studyChartRef = ref<HTMLDivElement>()
const noteChartRef = ref<HTMLDivElement>()
const todoChartRef = ref<HTMLDivElement>()
const catTagChartRef = ref<HTMLDivElement>()

const statsRef = ref(props.stats) as Ref<StatsVO | null>
watch(() => props.stats, (s) => { statsRef.value = s })

const { renderCharts, disposeCharts, handleResize, updateCatTagChart } = useStatsCharts({
  stats: statsRef,
  catTagTab,
  studyChartRef,
  noteChartRef,
  todoChartRef,
  catTagChartRef,
})

watch(
  () => props.stats,
  () => { nextTick(() => renderCharts()) },
  { immediate: true },
)

watch(catTagTab, () => { nextTick(() => updateCatTagChart()) })

onUnmounted(() => disposeCharts())

defineExpose({ handleResize, disposeCharts, renderCharts })
</script>

<template>
  <div class="chart-grid">
    <div v-show="hasCard('study-trend')" class="chart-card">
      <div class="chart-header">
        <BookOpen :size="16" stroke="var(--accent)" />
        <span>学习趋势</span>
      </div>
      <div ref="studyChartRef" class="chart-container" />
    </div>

    <div v-show="hasCard('note-trend')" class="chart-card">
      <div class="chart-header">
        <FileText :size="16" stroke="var(--success)" />
        <span>笔记新增趋势</span>
      </div>
      <div v-if="stats.avgDailyNotes > 0" class="chart-meta">
        <span>日均 {{ stats.avgDailyNotes.toFixed(1) }}</span>
        <span class="meta-divider">·</span>
        <span>最高 {{ stats.maxDailyNotes }}</span>
        <span v-if="stats.minDailyNotes > 0" class="meta-divider">·</span>
        <span v-if="stats.minDailyNotes > 0">最低 {{ stats.minDailyNotes }}</span>
      </div>
      <div ref="noteChartRef" class="chart-container" />
    </div>

    <div v-show="hasCard('todo-donut')" class="chart-card">
      <div class="chart-header">
        <CheckSquare :size="16" stroke="var(--warning)" />
        <span>Todo 完成率</span>
      </div>
      <div ref="todoChartRef" class="chart-container chart-container--donut" />
      <div class="todo-legend">
        <div class="legend-item">
          <span class="legend-dot" style="background: var(--success)" />
          <span>完成 {{ stats.todoDone }}</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: var(--warning)" />
          <span>进行中 {{ stats.todoPending }}</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: var(--danger)" />
          <span>超期 {{ stats.todoOverdue }}</span>
        </div>
      </div>
    </div>

    <div v-show="hasCard('cat-tag')" class="chart-card">
      <div class="chart-header">
        <Layers :size="16" stroke="var(--accent)" />
        <div class="chart-header-tabs">
          <button
            type="button"
            :class="['tab-btn', { active: catTagTab === 'category' }]"
            @click="catTagTab = 'category'"
          >分类</button>
          <button
            type="button"
            :class="['tab-btn', { active: catTagTab === 'tag' }]"
            @click="catTagTab = 'tag'"
          >标签</button>
        </div>
      </div>
      <div ref="catTagChartRef" class="chart-container" />
    </div>
  </div>
</template>

<style scoped>
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--sp-4);
}
.chart-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: var(--sp-4) var(--sp-5);
  overflow: hidden;
}
.chart-header {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--sp-2);
}
.chart-meta {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin-bottom: var(--sp-2);
}
.meta-divider { color: var(--border-color); }
.chart-header-tabs {
  display: flex;
  gap: 2px;
  margin-left: auto;
  background: var(--bg-hover);
  border-radius: var(--radius-sm);
  padding: 2px;
}
.tab-btn {
  padding: 3px 12px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-tertiary);
  font-size: var(--text-xs);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition);
  font-family: inherit;
}
.tab-btn.active {
  background: var(--bg-card);
  color: var(--text-primary);
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.chart-container { height: 280px; width: 100%; }
.chart-container--donut { height: 220px; }
.todo-legend {
  display: flex;
  justify-content: center;
  gap: var(--sp-5);
  padding: 0 0 var(--sp-1);
}
.legend-item {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  font-size: var(--text-xs);
  color: var(--text-secondary);
}
.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
@media (max-width: 1024px) {
  .chart-grid { grid-template-columns: 1fr; }
}
</style>

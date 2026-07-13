<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { getTrends } from '@/modules/dashboard/api'
import * as echarts from 'echarts'
import { BarChart3, BookOpen, FileText, CheckSquare, BookMarked } from 'lucide-vue-next'

const days = ref(30)
const loading = ref(true)
const studyChartRef = ref<HTMLDivElement>()
const noteChartRef = ref<HTMLDivElement>()
const todoChartRef = ref<HTMLDivElement>()
const readingChartRef = ref<HTMLDivElement>()

let studyChart: echarts.ECharts | null = null
let noteChart: echarts.ECharts | null = null
let todoChart: echarts.ECharts | null = null
let readingChart: echarts.ECharts | null = null

const chartOptions = {
  grid: { left: 50, right: 20, top: 30, bottom: 30 },
  tooltip: {
    trigger: 'axis' as const,
    formatter: (params: any) => {
      const item = params[0]
      return `${item.axisValue}<br/>${item.marker} ${item.seriesName}: ${item.value}`
    }
  },
  xAxis: { type: 'category' as const, axisLine: { lineStyle: { color: '#e5e7eb' } } },
  yAxis: { type: 'value' as const, splitLine: { lineStyle: { color: '#f3f4f6' } } },
}

function buildSeries(data: { date: string; value: number }[], name: string, color: string) {
  return {
    name,
    type: 'line' as const,
    smooth: true,
    symbol: 'circle' as const,
    symbolSize: 6,
    lineStyle: { width: 2, color },
    itemStyle: { color },
    areaStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: color + '30' },
        { offset: 1, color: color + '05' },
      ])
    },
    data: data.map(d => d.value),
  }
}

async function fetchData() {
  // 销毁旧图表实例，避免 DOM 重建后实例指向已移除的元素
  disposeCharts()
  loading.value = true

  try {
    const res = await getTrends(days.value)
    const data = res.data.data

    loading.value = false
    await nextTick()
    updateCharts(data)
  } finally {
    loading.value = false
  }
}

function disposeCharts() {
  studyChart?.dispose()
  noteChart?.dispose()
  todoChart?.dispose()
  readingChart?.dispose()
  studyChart = null
  noteChart = null
  todoChart = null
  readingChart = null
}

function updateCharts(data: { studyTrend: any[]; noteTrend: any[]; todoTrend: any[]; readingTrend: any[] }) {
  const commonX = data.studyTrend.map(d => d.date.slice(5))

  if (studyChartRef.value) {
    if (!studyChart) studyChart = echarts.init(studyChartRef.value)
    studyChart.setOption({
      ...chartOptions,
      xAxis: { ...chartOptions.xAxis, data: commonX },
      series: [buildSeries(data.studyTrend, '学习时长(分)', '#409eff')],
    })
  }

  if (noteChartRef.value) {
    if (!noteChart) noteChart = echarts.init(noteChartRef.value)
    noteChart.setOption({
      ...chartOptions,
      xAxis: { ...chartOptions.xAxis, data: data.noteTrend.map(d => d.date.slice(5)) },
      series: [buildSeries(data.noteTrend, '新建笔记', '#67c23a')],
    })
  }

  if (todoChartRef.value) {
    if (!todoChart) todoChart = echarts.init(todoChartRef.value)
    todoChart.setOption({
      ...chartOptions,
      xAxis: { ...chartOptions.xAxis, data: data.todoTrend.map(d => d.date.slice(5)) },
      series: [buildSeries(data.todoTrend, '新建待办', '#e6a23c')],
    })
  }

  if (readingChartRef.value) {
    if (!readingChart) readingChart = echarts.init(readingChartRef.value)
    readingChart.setOption({
      ...chartOptions,
      xAxis: { ...chartOptions.xAxis, data: data.readingTrend.map(d => d.date.slice(5)) },
      series: [buildSeries(data.readingTrend, '新增阅读', '#9b59b6')],
    })
  }
}

function handleResize() {
  studyChart?.resize()
  noteChart?.resize()
  todoChart?.resize()
  readingChart?.resize()
}

watch(days, fetchData)

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  studyChart?.dispose()
  noteChart?.dispose()
  todoChart?.dispose()
  readingChart?.dispose()
})
</script>

<template>
  <div class="stats-page">
    <div class="page-header">
      <h2>数据统计</h2>
      <div class="range-picker">
        <el-radio-group v-model="days" size="small">
          <el-radio-button :value="7">近 7 天</el-radio-button>
          <el-radio-button :value="30">近 30 天</el-radio-button>
          <el-radio-button :value="90">近 90 天</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 4" :key="i" class="skeleton-chart" />
    </div>

    <div v-else class="chart-grid">
      <div class="chart-card">
        <div class="chart-header">
          <BookOpen :size="16" stroke="#409eff" />
          <span>学习时长趋势</span>
        </div>
        <div ref="studyChartRef" class="chart-container" />
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <FileText :size="16" stroke="#67c23a" />
          <span>笔记新增趋势</span>
        </div>
        <div ref="noteChartRef" class="chart-container" />
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <CheckSquare :size="16" stroke="#e6a23c" />
          <span>待办新增趋势</span>
        </div>
        <div ref="todoChartRef" class="chart-container" />
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <BookMarked :size="16" stroke="#9b59b6" />
          <span>阅读新增趋势</span>
        </div>
        <div ref="readingChartRef" class="chart-container" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-page { margin: 0 auto; width: 100%; }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--sp-6);
}
.page-header h2 { font-size: var(--text-xl); font-weight: 600; }

.loading-skeleton { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--sp-4); }
.skeleton-chart { height: 320px; border-radius: var(--radius-lg); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.chart-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--sp-4); }
.chart-card {
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-lg); padding: var(--sp-4); overflow: hidden;
}
.chart-header {
  display: flex; align-items: center; gap: var(--sp-2);
  font-size: var(--text-sm); font-weight: 500; margin-bottom: var(--sp-3);
}
.chart-container { height: 280px; width: 100%; }
</style>

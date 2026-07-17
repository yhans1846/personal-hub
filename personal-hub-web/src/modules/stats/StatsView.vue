<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { getDetailedStats } from '@/modules/dashboard/api'
import type { StatsVO, InsightItem, ActivityItem, NamedStat } from '@/modules/dashboard/api'
import * as echarts from 'echarts'
import { useLayoutStore } from '@/store/layoutStore'
import { PageHeader } from '@/components'
import {
  BookOpen, FileText, CheckSquare, BookMarked,
  TrendingUp, Clock, Lightbulb, Layers, Tag,
  ArrowUp, ArrowDown, Minus
} from 'lucide-vue-next'

// ====== 状态 ======
const days = ref(7)
const loading = ref(true)
const stats = ref<StatsVO | null>(null)
const catTagTab = ref<'category' | 'tag'>('category')
const error = ref(false)
const activeSections = ref(['overview', 'trends', 'activity'])

// ====== 统计卡片配置 ======
const layoutStore = useLayoutStore()
const visibleCards = computed(() => layoutStore.visibleStatsCards)
const hasCard = (code: string) => visibleCards.value.some(c => c.code === code)

// 当卡片可见性变化时重新渲染图表
watch(visibleCards, () => {
  if (stats.value) nextTick(() => renderCharts())
})

// ====== 图表容器 ref ======
const studyChartRef = ref<HTMLDivElement>()
const noteChartRef = ref<HTMLDivElement>()
const todoChartRef = ref<HTMLDivElement>()
const catTagChartRef = ref<HTMLDivElement>()

// ====== 图表实例 ======
let studyChart: echarts.ECharts | null = null
let noteChart: echarts.ECharts | null = null
let todoChart: echarts.ECharts | null = null
let catTagChart: echarts.ECharts | null = null

// ====== 生命周期 ======
onMounted(async () => {
  await fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})

watch(days, () => { fetchData() })
watch(catTagTab, () => { if (stats.value) nextTick(() => updateCatTagChart()) })

// ====== 数据获取 ======
async function fetchData() {
  disposeCharts()
  loading.value = true
  error.value = false
  try {
    const res = await getDetailedStats(days.value)
    stats.value = res.data.data
    loading.value = false
    await nextTick()
    renderCharts()
  } catch {
    loading.value = false
    error.value = true
  }
}

// ====== 图表管理 ======
function renderCharts() {
  if (!stats.value) return
  renderStudyChart()
  renderNoteChart()
  renderTodoChart()
  renderCatTagChart()
}

function disposeCharts() {
  ;[studyChart, noteChart, todoChart, catTagChart].forEach(c => c?.dispose())
  studyChart = noteChart = todoChart = catTagChart = null
}

function handleResize() {
  ;[studyChart, noteChart, todoChart, catTagChart].forEach(c => c?.resize())
}

function onSectionChange() {
  nextTick(() => {
    window.setTimeout(() => handleResize(), 320)
  })
}

function initChart(el: HTMLDivElement, existing: echarts.ECharts | null): echarts.ECharts {
  existing?.dispose()
  return echarts.init(el)
}

// ====== 颜色系统 ======
const COLORS = {
  study: '#409eff',
  note: '#67c23a',
  todo: '#e6a23c',
  reading: '#9b59b6',
  active: '#409eff',
  success: '#67c23a',
  warning: '#e6a23c',
  danger: '#f56c6c',
}

// ====== 图表渲染函数 ======

/** ① 学习趋势 - 面积图 */
function renderStudyChart() {
  if (!studyChartRef.value || !stats.value?.studyTrend) return
  const data = stats.value.studyTrend
  const dates = data.map(d => d.date.slice(5))

  studyChart = initChart(studyChartRef.value, studyChart)
  studyChart.setOption({
    animationDuration: 800,
    grid: { left: 45, right: 16, top: 20, bottom: 25 },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const p = params[0]
        return `${p.axisValue}<br/>${p.marker} 学习时长: ${p.value} 分钟`
      },
    },
    xAxis: {
      type: 'category', data: dates,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { fontSize: 11, color: '#9ca3af' },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { fontSize: 11, color: '#9ca3af' },
    },
    series: [{
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 2, color: COLORS.study },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: COLORS.study + '25' },
          { offset: 1, color: COLORS.study + '03' },
        ]),
      },
      data: data.map(d => d.value),
    }],
  })
}

/** ② 笔记新增趋势 - 柱状图 */
function renderNoteChart() {
  if (!noteChartRef.value || !stats.value?.noteTrend) return
  const data = stats.value.noteTrend
  const dates = data.map(d => d.date.slice(5))
  const values = data.map(d => d.value)

  const max = stats.value.maxDailyNotes || 1
  const avg = stats.value.avgDailyNotes || 0

  noteChart = initChart(noteChartRef.value, noteChart)
  noteChart.setOption({
    animationDuration: 800,
    grid: { left: 45, right: 30, top: 20, bottom: 25 },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const p = params[0]
        let extra = ''
        if (params.length > 1 && params[1]?.seriesName === '平均线') {
          extra = `<br/>${params[1].marker} 日均: ${avg}`
        }
        return `${p.axisValue}<br/>${p.marker} 新增笔记: ${p.value}${extra}`
      },
    },
    xAxis: {
      type: 'category', data: dates,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { fontSize: 11, color: '#9ca3af' },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { fontSize: 11, color: '#9ca3af' },
      max: Math.ceil(max * 1.3),
    },
    series: [
      {
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: COLORS.note + 'cc' },
            { offset: 1, color: COLORS.note + '55' },
          ]),
          borderRadius: [3, 3, 0, 0],
        },
        data: values,
      },
      {
        type: 'line',
        name: '平均线',
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 1.5, type: 'dashed', color: COLORS.warning },
        data: values.map(() => avg),
        z: 2,
      },
    ],
  })
}

/** ③ Todo 完成率 - 环形图 */
function renderTodoChart() {
  if (!todoChartRef.value || !stats.value) return
  const s = stats.value

  todoChart = initChart(todoChartRef.value, todoChart)
  todoChart.setOption({
    animationDuration: 1000,
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: 'center',
        style: {
          text: `${Math.round(s.todoCompletionRate)}%`,
          fill: '#374151',
          fontSize: 28,
          fontWeight: 700,
          fontFamily: 'Inter, sans-serif',
        },
        z: 100,
      },
      {
        type: 'text',
        left: 'center',
        top: '55%',
        style: {
          text: '完成率',
          fill: '#9ca3af',
          fontSize: 12,
          fontFamily: 'Inter, sans-serif',
        },
        z: 100,
      },
    ],
    series: [{
      type: 'pie',
      radius: ['55%', '75%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { scale: false },
      data: [
        { value: s.todoDone, name: '完成', itemStyle: { color: COLORS.success } },
        { value: s.todoPending, name: '进行中', itemStyle: { color: COLORS.warning } },
        { value: s.todoOverdue, name: '超期', itemStyle: { color: COLORS.danger } },
      ].filter(d => d.value > 0),
    }],
  })
}

/** ⑤ 分类/标签统计 - 横向柱状图 */
function renderCatTagChart() {
  updateCatTagChart()
}

function updateCatTagChart() {
  if (!catTagChartRef.value || !stats.value) return
  const isCat = catTagTab.value === 'category'
  const rawData = isCat ? stats.value.categoryStats : stats.value.tagStats
  if (!rawData || rawData.length === 0) return

  // 取 Top 8，反向排序（底部最大）
  const data = [...rawData].slice(0, 8).reverse()
  const names = data.map(d => d.name)
  const values = data.map(d => d.count)
  const colors = data.map(d => d.color || COLORS.active)
  const maxVal = Math.max(...values, 1)

  catTagChart = initChart(catTagChartRef.value, catTagChart)
  catTagChart.setOption({
    animationDuration: 600,
    grid: { left: 10, right: 40, top: 10, bottom: 10, containLabel: true },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const p = params[0]
        return `${p.axisValue}<br/>${p.marker} ${p.value}`
      },
    },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { fontSize: 11, color: '#9ca3af' },
      max: maxVal * 1.15,
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { fontSize: 12, fontWeight: 500, color: '#374151' },
    },
    series: [{
      type: 'bar',
      barWidth: 20,
      data: values.map((v, i) => ({
        value: v,
        itemStyle: {
          color: colors[i % colors.length],
          borderRadius: [0, 4, 4, 0],
        },
      })),
      label: {
        show: true,
        position: 'right',
        fontSize: 12,
        fontWeight: 600,
        color: '#6b7280',
        formatter: (p: any) => p.value,
      },
    }],
  })
}

// ====== 格式化工具 ======
function formatDuration(minutes: number): string {
  if (minutes < 60) return `${minutes} 分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h} 小时 ${m} 分钟` : `${h} 小时`
}

function formatReadingHours(hours: number): string {
  if (hours < 1) return `${Math.round(hours * 60)} 分钟`
  return `${hours.toFixed(1)} 小时`
}

function changeIcon(change: number | null | undefined) {
  if (change == null || change === 0) return Minus
  return change > 0 ? ArrowUp : ArrowDown
}

function changeColor(change: number | null | undefined) {
  if (change == null || change === 0) return 'var(--text-tertiary)'
  return change > 0 ? 'var(--success)' : 'var(--danger)'
}

function changeText(change: number | null | undefined, unit = '%') {
  if (change == null || change === 0) return '持平'
  const prefix = change > 0 ? '+' : ''
  return `较上周 ${prefix}${change}${unit}`
}

/** 最近活动模块名映射 */
function moduleLabel(m: string): string {
  const map: Record<string, string> = {
    NOTE: '笔记', TODO: '待办', FILE: '文件',
    STUDY: '学习', READING: '阅读', BOOKMARK: '收藏',
    DIARY: '日记', STUDY_PLAN: '学习计划',
  }
  return map[m] || m
}

/** 最近活动操作映射 */
function actionLabel(a: string): string {
  const map: Record<string, string> = {
    CREATE: '新增', UPDATE: '更新', DELETE: '删除',
    RESTORE: '恢复', LOGIN: '登录',
  }
  return map[a] || a
}

/** 活动时间标签内容 - 分组用 */
function activityDateLabel(item: ActivityItem): string {
  if (!item.createdAt) return ''
  const d = new Date(item.createdAt)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  const diffDays = Math.floor((today.getTime() - target.getTime()) / 86400000)

  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays} 天前`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

// ====== 计算属性 ======

const groupedActivities = computed(() => {
  if (!stats.value?.recentActivity) return []
  const groups: { label: string; items: ActivityItem[] }[] = []
  let currentLabel = ''
  let currentGroup: ActivityItem[] = []

  for (const item of stats.value.recentActivity) {
    const label = activityDateLabel(item)
    if (label !== currentLabel) {
      if (currentGroup.length > 0) {
        groups.push({ label: currentLabel, items: currentGroup })
      }
      currentLabel = label
      currentGroup = [item]
    } else {
      currentGroup.push(item)
    }
  }
  if (currentGroup.length > 0) {
    groups.push({ label: currentLabel, items: currentGroup })
  }
  return groups
})
</script>

<template>
  <div class="stats-page">
    <PageHeader title="数据统计" subtitle="学习习惯与知识沉淀的可视化洞察" />

    <div class="stats-sticky-bar">
      <el-radio-group v-model="days" size="small">
        <el-radio-button :value="3">近 3 天</el-radio-button>
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="15">近 15 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
        <el-radio-button :value="90">近 90 天</el-radio-button>
      </el-radio-group>
    </div>

    <!-- ====== 错误状态 ====== -->
    <div v-if="error" class="state-box error-state">
      <p>加载失败，请稍后重试</p>
      <button class="retry-btn" @click="fetchData">重新加载</button>
    </div>

    <!-- ====== 加载骨架 ====== -->
    <div v-else-if="loading" class="loading-skeleton">
      <div class="skeleton-row">
        <div v-for="i in 4" :key="i" class="skeleton-kpi" />
      </div>
      <div class="skeleton-row">
        <div v-for="i in 2" :key="i" class="skeleton-chart" />
      </div>
      <div class="skeleton-row">
        <div v-for="i in 2" :key="i" class="skeleton-chart" />
      </div>
    </div>

    <!-- ====== 主内容 ====== -->
    <template v-else-if="stats">
      <el-collapse v-model="activeSections" class="stats-sections" @change="onSectionChange">

        <!-- ====== ① KPI Summary ====== -->
        <el-collapse-item v-if="hasCard('kpi')" title="核心指标" name="overview">
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
        </el-collapse-item>

        <!-- ====== 图表网格 ====== -->
        <el-collapse-item
          v-if="hasCard('study-trend') || hasCard('note-trend') || hasCard('todo-donut') || hasCard('cat-tag')"
          title="趋势分析"
          name="trends"
        >
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
              <div class="chart-meta" v-if="stats.avgDailyNotes > 0">
                <span>日均 {{ stats.avgDailyNotes.toFixed(1) }}</span>
                <span class="meta-divider">·</span>
                <span>最高 {{ stats.maxDailyNotes }}</span>
                <span class="meta-divider" v-if="stats.minDailyNotes > 0">·</span>
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
                    :class="['tab-btn', { active: catTagTab === 'category' }]"
                    @click="catTagTab = 'category'"
                  >分类</button>
                  <button
                    :class="['tab-btn', { active: catTagTab === 'tag' }]"
                    @click="catTagTab = 'tag'"
                  >标签</button>
                </div>
              </div>
              <div ref="catTagChartRef" class="chart-container" />
            </div>
          </div>
        </el-collapse-item>

        <!-- ====== 底部双栏 ====== -->
        <el-collapse-item
          v-if="hasCard('timeline') || hasCard('insight')"
          title="活动与洞察"
          name="activity"
        >
          <div class="bottom-grid" :class="{ 'bottom-grid--single': !hasCard('timeline') !== !hasCard('insight') }">
            <div v-show="hasCard('timeline')" class="bottom-card">
              <div class="chart-header">
                <Clock :size="16" stroke="var(--text-secondary)" />
                <span>最近活动</span>
              </div>
              <div class="timeline" v-if="groupedActivities.length > 0">
                <template v-for="group in groupedActivities" :key="group.label">
                  <div class="timeline-date">{{ group.label }}</div>
                  <div class="timeline-items">
                    <div v-for="item in group.items" :key="item.id" class="timeline-item">
                      <div class="tl-dot" :class="`tl-dot--${item.action.toLowerCase()}`" />
                      <div class="tl-body">
                        <span class="tl-module">{{ moduleLabel(item.module) }}</span>
                        <span class="tl-action">{{ actionLabel(item.action) }}</span>
                        <span class="tl-content">{{ item.content }}</span>
                      </div>
                      <span class="tl-time">{{ item.timeLabel }}</span>
                    </div>
                  </div>
                </template>
              </div>
              <div v-else class="empty-box">暂无活动记录</div>
            </div>

            <div v-show="hasCard('insight')" class="bottom-card">
              <div class="chart-header">
                <Lightbulb :size="16" stroke="var(--warning)" />
                <span>学习洞察</span>
              </div>
              <div class="insights" v-if="stats.insights.length > 0">
                <div v-for="(insight, i) in stats.insights" :key="i" class="insight-item" :style="{ '--delay': `${i * 0.1}s` }">
                  <span class="insight-icon">{{ insight.icon }}</span>
                  <div class="insight-body">
                    <span class="insight-title">{{ insight.title }}</span>
                    <span class="insight-desc">{{ insight.description }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="empty-box">数据不足，继续使用后将生成洞察</div>
            </div>
          </div>
        </el-collapse-item>

      </el-collapse>
    </template>
  </div>
</template>

<style scoped>
/* ============================
   布局
   ============================ */
.stats-page {
  margin: 0 auto;
  width: 100%;
  animation: fadeIn 0.4s ease;
}

.stats-sticky-bar {
  position: sticky;
  top: 0;
  z-index: 5;
  background: var(--bg-body);
  padding: var(--sp-2) 0 var(--sp-4);
  margin-top: calc(var(--sp-6) * -1 + var(--sp-2));
  margin-bottom: var(--sp-4);
}

.stats-sections {
  border: none;
}
.stats-sections :deep(.el-collapse-item__header) {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  background: transparent;
  border-bottom: 1px solid var(--border-color);
  height: auto;
  line-height: 1.4;
  padding: var(--sp-3) 0;
}
.stats-sections :deep(.el-collapse-item__wrap) {
  border-bottom: none;
}
.stats-sections :deep(.el-collapse-item__content) {
  padding: var(--sp-4) 0 var(--sp-2);
}

/* ====== 状态容器 ====== */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--sp-12) 0;
  gap: var(--sp-4);
  color: var(--text-tertiary);
  font-size: var(--text-sm);
}
.retry-btn {
  padding: var(--sp-2) var(--sp-5);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  color: var(--accent);
  cursor: pointer;
  font-size: var(--text-sm);
  transition: all var(--transition);
}
.retry-btn:hover {
  background: var(--accent-light);
  border-color: var(--accent-border);
}

/* ====== 骨架屏 ====== */
.loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--sp-4);
}
.skeleton-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--sp-4);
}
.skeleton-row:not(:first-child) {
  grid-template-columns: 1fr 1fr;
}
.skeleton-kpi {
  height: 88px;
  border-radius: var(--radius-lg);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}
.skeleton-chart {
  height: 320px;
  border-radius: var(--radius-lg);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}

/* ====== KPI 网格 ====== */
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
  border-radius: 16px;
  transition: all 0.2s ease;
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
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--kpi-bg, var(--accent-light));
  color: var(--kpi-color, var(--accent));
  flex-shrink: 0;
}
.kpi-body {
  flex: 1;
  min-width: 0;
}
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

/* ====== 图表网格 ====== */
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--sp-4);
}

.chart-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
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
.meta-divider {
  color: var(--border-color);
}

.chart-header-tabs {
  display: flex;
  gap: 2px;
  margin-left: auto;
  background: var(--bg-hover);
  border-radius: 6px;
  padding: 2px;
}
.tab-btn {
  padding: 3px 12px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--text-tertiary);
  font-size: var(--text-xs);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: inherit;
}
.tab-btn.active {
  background: var(--bg-card);
  color: var(--text-primary);
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

.chart-container {
  height: 280px;
  width: 100%;
}
.chart-container--donut {
  height: 220px;
}

/* ====== Todo 图例 ====== */
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

/* ====== 底部双栏 ====== */
.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--sp-4);
  align-items: stretch;
}
.bottom-grid--single {
  grid-template-columns: 1fr;
}
.bottom-grid--single .bottom-card {
  max-width: 600px;
}
.bottom-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: var(--sp-4) var(--sp-5);
  display: flex;
  flex-direction: column;
  height: 450px;
}
.bottom-card > .chart-header {
  flex-shrink: 0;
}
.bottom-card > .timeline,
.bottom-card > .insights {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  scrollbar-width: none;
}
.bottom-card > .timeline::-webkit-scrollbar,
.bottom-card > .insights::-webkit-scrollbar {
  display: none;
}

/* ====== Timeline ====== */
.timeline-date {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--text-secondary);
  padding: var(--sp-2) 0 var(--sp-1);
  margin-top: var(--sp-1);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}
.timeline-date:first-of-type {
  margin-top: 0;
}
.timeline-items {
  padding-left: var(--sp-1);
}
.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-3);
  padding: var(--sp-2) 0;
  position: relative;
}
.timeline-item::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 24px;
  bottom: -4px;
  width: 1px;
  background: var(--border-color);
}
.timeline-item:last-child::before {
  display: none;
}
.tl-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--border-color);
  flex-shrink: 0;
  margin-top: 4px;
  position: relative;
  z-index: 1;
}
.tl-dot--create { background: var(--success); }
.tl-dot--update { background: var(--accent); }
.tl-dot--delete { background: var(--danger); }
.tl-dot--restore { background: var(--warning); }
.tl-body {
  flex: 1;
  min-width: 0;
  font-size: var(--text-sm);
  color: var(--text-primary);
  line-height: 1.4;
}
.tl-module {
  font-weight: 500;
  margin-right: var(--sp-1);
}
.tl-action {
  color: var(--text-secondary);
  margin-right: var(--sp-1);
}
.tl-content {
  color: var(--text-tertiary);
  word-break: break-all;
}
.tl-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  flex-shrink: 0;
  margin-top: 2px;
}

/* ====== 洞察 ====== */
.insights {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  padding-top: var(--sp-1);
}
.insight-item {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  animation: slideUp 0.3s ease var(--delay, 0s) both;
}
.insight-icon {
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 1px;
}
.insight-body {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.insight-title {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
}
.insight-desc {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* ====== 空状态 ====== */
.empty-box {
  padding: var(--sp-8) 0;
  text-align: center;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

/* ====== 动画 ====== */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 0.3; }
}

/* ====== 响应式 ====== */
@media (max-width: 1024px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-grid { grid-template-columns: 1fr; }
  .bottom-grid { grid-template-columns: 1fr; }
  .skeleton-row { grid-template-columns: repeat(2, 1fr) !important; }
}
@media (max-width: 640px) {
  .stats-sticky-bar { margin-top: calc(var(--sp-4) * -1 + var(--sp-2)); }
  .kpi-grid { grid-template-columns: 1fr 1fr; }
  .kpi-card { padding: var(--sp-3); }
  .kpi-change { position: static; }
}
</style>

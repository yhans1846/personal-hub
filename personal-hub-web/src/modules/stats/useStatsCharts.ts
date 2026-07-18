import type { Ref } from 'vue'
import * as echarts from 'echarts'
import type { StatsVO } from '@/modules/dashboard/api'

export type UseStatsChartsOptions = {
  stats: Ref<StatsVO | null>
  catTagTab: Ref<'category' | 'tag'>
  studyChartRef: Ref<HTMLDivElement | undefined>
  noteChartRef: Ref<HTMLDivElement | undefined>
  todoChartRef: Ref<HTMLDivElement | undefined>
  catTagChartRef: Ref<HTMLDivElement | undefined>
}

function cssVar(name: string, fallback: string): string {
  const v = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  return v || fallback
}

function chartColors() {
  return {
    study: cssVar('--accent', '#4F7BFF'),
    note: cssVar('--success', '#22C55E'),
    todo: cssVar('--warning', '#F59E0B'),
    reading: cssVar('--info', '#6366F1'),
    active: cssVar('--accent', '#4F7BFF'),
    success: cssVar('--success', '#22C55E'),
    warning: cssVar('--warning', '#F59E0B'),
    danger: cssVar('--danger', '#EF4444'),
  }
}

const COLORS = new Proxy({} as Record<string, string>, {
  get(_target, prop: string) {
    const map = chartColors()
    return map[prop as keyof typeof map] ?? cssVar('--accent', '#4F7BFF')
  },
})

function chartAnimMs(): number {
  const anim = document.documentElement.getAttribute('data-anim')
  if (anim === 'off') return 0
  if (anim === 'slow') return 1200
  if (anim === 'fast') return 400
  return 800
}

export function useStatsCharts(options: UseStatsChartsOptions) {
  const { stats, catTagTab, studyChartRef, noteChartRef, todoChartRef, catTagChartRef } = options

  let studyChart: echarts.ECharts | null = null
  let noteChart: echarts.ECharts | null = null
  let todoChart: echarts.ECharts | null = null
  let catTagChart: echarts.ECharts | null = null

  function initChart(el: HTMLDivElement, existing: echarts.ECharts | null): echarts.ECharts {
    existing?.dispose()
    return echarts.init(el)
  }

  function renderStudyChart() {
    if (!studyChartRef.value || !stats.value?.studyTrend) return
    const data = stats.value.studyTrend
    const dates = data.map(d => d.date.slice(5))

    studyChart = initChart(studyChartRef.value, studyChart)
    studyChart.setOption({
      animationDuration: chartAnimMs(),
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

  function renderNoteChart() {
    if (!noteChartRef.value || !stats.value?.noteTrend) return
    const data = stats.value.noteTrend
    const dates = data.map(d => d.date.slice(5))
    const values = data.map(d => d.value)

    const max = stats.value.maxDailyNotes || 1
    const avg = stats.value.avgDailyNotes || 0

    noteChart = initChart(noteChartRef.value, noteChart)
    noteChart.setOption({
      animationDuration: chartAnimMs(),
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

  function renderTodoChart() {
    if (!todoChartRef.value || !stats.value) return
    const s = stats.value

    todoChart = initChart(todoChartRef.value, todoChart)
    todoChart.setOption({
      animationDuration: chartAnimMs(),
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

  function updateCatTagChart() {
    if (!catTagChartRef.value || !stats.value) return
    const isCat = catTagTab.value === 'category'
    const rawData = isCat ? stats.value.categoryStats : stats.value.tagStats
    if (!rawData || rawData.length === 0) return

    const data = [...rawData].slice(0, 8).reverse()
    const names = data.map(d => d.name)
    const values = data.map(d => d.count)
    const colors = data.map(d => d.color || COLORS.active)
    const maxVal = Math.max(...values, 1)

    catTagChart = initChart(catTagChartRef.value, catTagChart)
    catTagChart.setOption({
      animationDuration: chartAnimMs(),
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

  function renderCatTagChart() {
    updateCatTagChart()
  }

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

  return {
    renderCharts,
    disposeCharts,
    handleResize,
    updateCatTagChart,
  }
}

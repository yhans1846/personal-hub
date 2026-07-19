<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { getDetailedStats } from '@/modules/dashboard/api'
import type { StatsVO } from '@/modules/dashboard/api'
import { useLayoutStore } from '@/store/layoutStore'
import { PageHeader } from '@/components'
import StatsKpiSection from './StatsKpiSection.vue'
import StatsChartsSection from './StatsChartsSection.vue'
import StatsActivitySection from './StatsActivitySection.vue'

const days = ref(7)
const loading = ref(true)
const stats = ref<StatsVO | null>(null)
const catTagTab = ref<'category' | 'tag'>('category')
const error = ref(false)
const activeSections = ref(['overview', 'trends', 'activity'])
const chartsSectionRef = ref<InstanceType<typeof StatsChartsSection>>()

const layoutStore = useLayoutStore()
const visibleCards = computed(() => layoutStore.visibleStatsCards)
const hasCard = (code: string) => visibleCards.value.some(c => c.code === code)

const showTrends = computed(() =>
  hasCard('study-trend') || hasCard('note-trend') || hasCard('todo-donut') || hasCard('cat-tag'),
)
const showActivity = computed(() => hasCard('timeline') || hasCard('insight'))

onMounted(async () => {
  await fetchData()
  window.addEventListener('resize', onWindowResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onWindowResize)
})

watch(days, () => { fetchData() })
watch(visibleCards, () => {
  if (stats.value) nextTick(() => chartsSectionRef.value?.renderCharts())
})

function onWindowResize() {
  chartsSectionRef.value?.handleResize()
}

async function fetchData() {
  chartsSectionRef.value?.disposeCharts()
  loading.value = true
  error.value = false
  try {
    const res = await getDetailedStats(days.value)
    stats.value = res.data.data
    loading.value = false
  } catch {
    loading.value = false
    error.value = true
  }
}

function onSectionChange() {
  nextTick(() => {
    window.setTimeout(() => chartsSectionRef.value?.handleResize(), 320)
  })
}
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

    <div v-if="error" class="state-box error-state">
      <p>加载失败，请稍后重试</p>
      <button type="button" class="retry-btn" @click="fetchData">重新加载</button>
    </div>

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

    <template v-else-if="stats">
      <el-collapse v-model="activeSections" class="stats-sections" @change="onSectionChange">
        <el-collapse-item v-if="hasCard('kpi')" title="核心指标" name="overview">
          <StatsKpiSection :stats="stats" />
        </el-collapse-item>

        <el-collapse-item v-if="showTrends" title="趋势分析" name="trends">
          <StatsChartsSection
            ref="chartsSectionRef"
            v-model:cat-tag-tab="catTagTab"
            :stats="stats"
            :has-card="hasCard"
          />
        </el-collapse-item>

        <el-collapse-item v-if="showActivity" title="活动与洞察" name="activity">
          <StatsActivitySection :stats="stats" :has-card="hasCard" />
        </el-collapse-item>
      </el-collapse>
    </template>
  </div>
</template>

<style scoped>
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
  background: transparent;
}
.stats-sections :deep(.el-collapse-item__header) {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  background: transparent;
  height: auto;
  line-height: 1.4;
  padding: var(--sp-3) 0;
}
.stats-sections :deep(.el-collapse-item__wrap) {
  background: transparent;
  border: none;
}
.stats-sections :deep(.el-collapse-item__content) {
  padding: var(--sp-4) 0 var(--sp-2);
  background: transparent;
}
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
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 0.3; }
}
@media (max-width: 1024px) {
  .skeleton-row { grid-template-columns: repeat(2, 1fr) !important; }
}
@media (max-width: 640px) {
  .stats-sticky-bar { margin-top: calc(var(--sp-4) * -1 + var(--sp-2)); }
}
</style>

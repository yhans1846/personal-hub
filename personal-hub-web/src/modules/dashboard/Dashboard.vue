<script setup lang="ts">
import { ref, computed, onMounted, type Component } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/authStore'
import { useLayoutStore } from '@/store/layoutStore'
import { getTodayTodos, getStudyPlanList } from '@/modules/planning/api'
import { getStudyRecordList, getRecentNotes, getReadingList } from '@/modules/knowledge/api'
import { getDashboardBookmarks } from '@/modules/resource/api'
import { getDashboardStats, type DashboardStats } from '@/modules/dashboard/api'
import { Clock } from 'lucide-vue-next'
import type { TodoVO } from '@/types/todo'
import type { StudyRecordVO } from '@/types/study'
import type { NoteVO } from '@/types/note'
import type { ReadingVO } from '@/types/reading'
import type { StudyPlanVO } from '@/types/studyplan'
import type { BookmarkVO } from '@/types/bookmark'
import { buildCreatePath } from '@/utils/deepLink'
import { DASHBOARD_CARD_SPAN, REMOVED_DASHBOARD_CODES } from './defaultLayouts'
import { formatDuration } from './format'
import { useMainContentFill } from '@/composables/useMainContentFill'

import TodayWidget from './widgets/TodayWidget.vue'
import QuickActionsWidget from './widgets/QuickActionsWidget.vue'
import ExternalLinksWidget from './widgets/ExternalLinksWidget.vue'
import RecentNotesWidget from './widgets/RecentNotesWidget.vue'
import RecentStudiesWidget from './widgets/RecentStudiesWidget.vue'
import RecentReadingWidget from './widgets/RecentReadingWidget.vue'

const router = useRouter()
const authStore = useAuthStore()
const layoutStore = useLayoutStore()

useMainContentFill()

const stats = ref<DashboardStats | null>(null)
const recentNotes = ref<NoteVO[]>([])
const recentStudies = ref<StudyRecordVO[]>([])
const todayPlans = ref<StudyPlanVO[]>([])
const todayTodos = ref<TodoVO[]>([])
const recentReadings = ref<ReadingVO[]>([])
const dashboardLinks = ref<BookmarkVO[]>([])
const loading = ref(true)

const hour = new Date().getHours()
const greeting = hour < 6 ? '夜深了' : hour < 12 ? '上午好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'

const bentoCards = computed(() =>
  layoutStore.visibleDashboardCards.filter(c => !REMOVED_DASHBOARD_CODES.has(c.code)),
)

const widgetMap: Record<string, Component> = {
  today_plan: TodayWidget,
  quick_actions: QuickActionsWidget,
  external_links: ExternalLinksWidget,
  recent_notes: RecentNotesWidget,
  recent_studies: RecentStudiesWidget,
  recent_reading: RecentReadingWidget,
}

function cardSpan(code: string) {
  return DASHBOARD_CARD_SPAN[code] ?? 4
}

onMounted(async () => {
  loading.value = true
  try {
    const [
      statsRes, notesRes, studyRes, plansRes, readingsRes, todayTodosRes, linksRes,
    ] = await Promise.all([
      getDashboardStats().catch(() => null),
      getRecentNotes(1, 6).catch(() => null),
      getStudyRecordList({ page: 1, size: 6 }).catch(() => null),
      getStudyPlanList({ page: 1, size: 5, status: 1 }).catch(() => null),
      getReadingList({ page: 1, size: 6 }).catch(() => null),
      getTodayTodos().catch(() => null),
      getDashboardBookmarks(8).catch(() => null),
    ])
    if (statsRes) stats.value = statsRes.data.data
    if (notesRes) recentNotes.value = notesRes.data.data.records
    if (studyRes) recentStudies.value = studyRes.data.data.records
    if (plansRes) todayPlans.value = plansRes.data.data.records
    if (todayTodosRes) todayTodos.value = todayTodosRes.data.data
    if (readingsRes) recentReadings.value = readingsRes.data.data.records
    if (linksRes) dashboardLinks.value = linksRes.data.data
  } finally {
    loading.value = false
  }
})

async function handleToggleDone(id: number) {
  const { toggleDone } = await import('@/modules/planning/api')
  await toggleDone(id)
  todayTodos.value = todayTodos.value.filter(t => t.id !== id)
  if (stats.value) {
    stats.value = {
      ...stats.value,
      todoDone: stats.value.todoDone + 1,
      todoPending: Math.max(0, stats.value.todoPending - 1),
    }
  }
}

function quickCreate(type: string) {
  switch (type) {
    case 'note': router.push('/notes/new'); break
    case 'todo': router.push(buildCreatePath('/todos')); break
    case 'diary': router.push(buildCreatePath('/diaries')); break
    case 'study': router.push(buildCreatePath('/study-records')); break
    case 'reading': router.push(buildCreatePath('/readings')); break
    case 'plan': router.push(buildCreatePath('/study-plans')); break
  }
}

function widgetProps(code: string) {
  switch (code) {
    case 'today_plan':
      return { todayTodos: todayTodos.value, todayPlans: todayPlans.value }
    case 'external_links':
      return { links: dashboardLinks.value }
    case 'recent_notes':
      return { notes: recentNotes.value }
    case 'recent_studies':
      return { studies: recentStudies.value }
    case 'recent_reading':
      return { readings: recentReadings.value }
    default:
      return {}
  }
}
</script>

<template>
  <div class="dashboard">
    <div class="hero">
      <div class="hero-text">
        <h1>{{ authStore.user?.nickname || authStore.user?.username || '用户' }}，{{ greeting }}</h1>
        <p class="hero-sub" v-if="stats">
          本周学习 <strong>{{ formatDuration(stats.studyDurationThisWeek) }}</strong>
          · 笔记 {{ stats.noteCount }}
          · 待办 {{ stats.todoDone }}/{{ stats.todoTotal }}
        </p>
        <div v-if="stats" class="hero-resources">
          <router-link to="/diaries" class="resource-chip">本月日记 {{ stats.diaryCountThisMonth ?? 0 }}</router-link>
          <router-link to="/bookmarks" class="resource-chip">收藏 {{ stats.bookmarkCount ?? 0 }}</router-link>
          <router-link to="/files" class="resource-chip">文件 {{ stats.fileCount ?? 0 }}</router-link>
          <router-link to="/readings" class="resource-chip">在读 {{ stats.readingInProgress ?? 0 }}</router-link>
        </div>
        <router-link v-if="stats" to="/stats" class="hero-resources-compact">资源</router-link>
        <router-link to="/stats" class="stats-link">查看数据统计 →</router-link>
      </div>
      <button
        v-if="stats && stats.todoOverdue > 0"
        class="overdue-chip"
        @click="router.push('/todos')"
      >
        <Clock :size="14" />
        {{ stats.todoOverdue }} 个待办已超期
      </button>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 6" :key="i" class="skeleton-block" />
    </div>

    <div v-else class="bento">
      <div
        v-for="card in bentoCards"
        :key="card.code"
        class="bento-cell"
        :style="{ gridColumn: `span ${cardSpan(card.code)}` }"
      >
        <component
          :is="widgetMap[card.code]"
          v-if="widgetMap[card.code]"
          v-bind="widgetProps(card.code)"
          @toggle-done="handleToggleDone"
          @create="quickCreate"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: var(--sp-4);
  margin-bottom: var(--sp-5);
  flex-wrap: wrap;
  flex-shrink: 0;
}
.hero-text h1 {
  font-size: var(--text-2xl);
  font-weight: 600;
  margin: 0 0 var(--sp-1);
}
.hero-sub {
  margin: 0 0 var(--sp-2);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.hero-sub strong {
  color: var(--text-primary);
  font-weight: 600;
}
.hero-resources {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
  margin-bottom: var(--sp-2);
}
.resource-chip {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  text-decoration: none;
  padding: 4px 10px;
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  transition: color var(--transition), background var(--transition);
}
.resource-chip:hover {
  color: var(--accent);
  background: var(--accent-light);
}
.hero-resources-compact {
  display: none;
  font-size: var(--text-xs);
  color: var(--accent);
  text-decoration: none;
  margin-bottom: var(--sp-2);
}
.hero-resources-compact:hover { text-decoration: underline; }
.stats-link {
  font-size: var(--text-xs);
  color: var(--accent);
  text-decoration: none;
}
.stats-link:hover { text-decoration: underline; }
.overdue-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  padding: 8px 12px;
  border-radius: var(--radius-md);
  border: none;
  background: var(--danger-light);
  color: var(--danger);
  font-size: var(--text-xs);
  font-weight: 600;
  cursor: pointer;
  font-family: var(--font-sans);
  transition: opacity var(--transition);
}
.overdue-chip:hover { opacity: 0.85; }

.bento {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: var(--sp-4);
  align-items: stretch;
  align-content: stretch;
  grid-auto-rows: minmax(0, 1fr);
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.bento-cell {
  min-width: 0;
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.loading-skeleton {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: var(--sp-4);
}
.skeleton-block {
  height: 220px;
  border-radius: var(--radius-lg);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
  grid-column: span 4;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.55; }
}

@media (max-width: 900px) {
  .dashboard {
    overflow-x: hidden;
    overflow-y: auto;
  }
  .bento {
    flex: none;
    overflow: visible;
    grid-auto-rows: auto;
  }
  .bento-cell { grid-column: span 12 !important; min-height: 140px; }
  .skeleton-block { grid-column: span 12 !important; }
  .hero-resources { display: none; }
  .hero-resources-compact { display: inline-block; }
}
@media (max-width: 768px) {
  .hero-text h1 { font-size: var(--text-xl); }
  .hero-resources-compact { display: none; }
}
@media (max-width: 640px) {
  .hero {
    margin-bottom: var(--sp-3);
    gap: var(--sp-2);
  }
  .hero-sub { display: none; }
  .hero-text h1 { margin-bottom: var(--sp-1); }
}
@media (max-height: 640px) {
  .hero {
    margin-bottom: var(--sp-2);
    align-items: center;
  }
  .hero-text h1 {
    font-size: var(--text-lg);
    margin-bottom: 0;
  }
  .hero-sub,
  .hero-resources,
  .hero-resources-compact,
  .stats-link { display: none; }
  .overdue-chip { padding: 4px 8px; }
}
</style>

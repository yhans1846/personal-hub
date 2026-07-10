<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/authStore'
import { getTodoList } from '@/api/todoApi'
import { getStudyRecordList } from '@/api/studyApi'
import { getRecentNotes } from '@/api/noteApi'
import { getDashboardStats } from '@/api/dashboardApi'
import type { DashboardStats } from '@/api/dashboardApi'
import { getBookmarkList } from '@/api/bookmarkApi'
import { getReadingList } from '@/api/readingApi'
import { getStudyPlanList } from '@/api/studyplanApi'
import { Plus, BookOpen, FileText, CheckCircle, File, Bookmark, BookMarked, Target, Clock } from 'lucide-vue-next'
import StatCard from '@/components/StatCard.vue'
import type { TodoVO } from '@/types/todo'
import type { StudyRecordVO } from '@/types/study'
import type { NoteVO } from '@/types/note'
import type { BookmarkVO } from '@/types/bookmark'
import type { ReadingVO } from '@/types/reading'
import type { StudyPlanVO } from '@/types/studyplan'

const router = useRouter()
const authStore = useAuthStore()

const stats = ref<DashboardStats | null>(null)
const recentNotes = ref<NoteVO[]>([])
const pendingTodos = ref<TodoVO[]>([])
const recentStudies = ref<StudyRecordVO[]>([])
const todayPlans = ref<StudyPlanVO[]>([])
const recentReadings = ref<ReadingVO[]>([])
const recentBookmarks = ref<BookmarkVO[]>([])
const loading = ref(true)

const hour = new Date().getHours()
const greeting = hour < 6 ? '夜深了' : hour < 12 ? '上午好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'

onMounted(async () => {
  loading.value = true
  try {
    const [statsRes, notesRes, todosRes, studyRes, plansRes, readingsRes, bookmarksRes] = await Promise.all([
      getDashboardStats().catch(() => null),
      getRecentNotes(1, 5).catch(() => null),
      getTodoList({ page: 1, size: 5, isDone: false }).catch(() => null),
      getStudyRecordList({ page: 1, size: 5 }).catch(() => null),
      getStudyPlanList({ page: 1, size: 5, status: 1 }).catch(() => null),
      getReadingList({ page: 1, size: 5 }).catch(() => null),
      getBookmarkList({ page: 1, size: 5 }).catch(() => null)
    ])
    if (statsRes) stats.value = statsRes.data.data
    if (notesRes) recentNotes.value = notesRes.data.data.records
    if (todosRes) pendingTodos.value = todosRes.data.data.records
    if (studyRes) recentStudies.value = studyRes.data.data.records
    if (plansRes) todayPlans.value = plansRes.data.data.records
    if (readingsRes) recentReadings.value = readingsRes.data.data.records
    if (bookmarksRes) recentBookmarks.value = bookmarksRes.data.data.records
  } finally {
    loading.value = false
  }
})

function formatRelative(d: string) {
  if (!d) return ''
  const date = new Date(d)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

function formatDuration(minutes: number) {
  if (minutes < 60) return `${minutes} 分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h} 小时 ${m} 分钟` : `${h} 小时`
}

async function handleToggleDone(id: number) {
  const { toggleDone } = await import('@/api/todoApi')
  await toggleDone(id)
  pendingTodos.value = pendingTodos.value.filter(t => t.id !== id)
}

const statCards = [
  { key: 'noteCount' as const, icon: FileText, label: '笔记', color: 'var(--accent)', onClick: () => router.push('/notes') },
  { key: 'studyCount' as const, icon: BookOpen, label: '学习记录', color: 'var(--info)', onClick: () => router.push('/study-records') },
  { key: 'todoPending' as const, icon: CheckCircle, label: '待办任务', color: 'var(--warning)', onClick: () => router.push('/todos'),
    append: (s: DashboardStats) => s.todoTotal > 0 ? ` ${s.todoDone}/${s.todoTotal}` : '' },
  { key: 'fileCount' as const, icon: File, label: '文件', color: 'var(--success)', onClick: () => router.push('/files') },
  { key: 'diaryCount' as const, icon: Bookmark, label: '日记', color: 'var(--info)', onClick: () => router.push('/diaries') },
  { key: 'bookmarkCount' as const, icon: Bookmark, label: '收藏', color: 'var(--warning)', onClick: () => router.push('/bookmarks') },
  { key: 'readingCount' as const, icon: BookMarked, label: '阅读', color: 'var(--success)', onClick: () => router.push('/readings') },
  { key: 'studyPlanCount' as const, icon: Target, label: '学习计划', color: 'var(--text-secondary)', onClick: () => router.push('/study-plans') },
]
</script>

<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div>
        <h1>{{ authStore.user?.nickname || authStore.user?.username || '用户' }}，{{ greeting }} 👋</h1>
        <p class="welcome-sub">
          <span v-if="stats">共 {{ stats.noteCount }} 篇笔记，本周学习 {{ formatDuration(stats.studyDurationThisWeek) }}</span>
        </p>
      </div>
      <div class="quick-actions">
        <el-button type="primary" size="small" @click="router.push('/notes/new')">
          <Plus :size="14" />
          新建笔记
        </el-button>
        <el-button size="small" @click="router.push('/todos/new')">
          <CheckCircle :size="14" />
          新建任务
        </el-button>
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="loading-skeleton">
      <div class="skeleton-row">
        <div v-for="i in 4" :key="i" class="skeleton-card" />
      </div>
      <div class="skeleton-row" style="margin-top: var(--sp-4)">
        <div v-for="i in 3" :key="i" class="skeleton-card-wide" />
      </div>
    </div>

    <template v-else>
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <StatCard
          v-for="card in statCards" :key="card.key"
          :icon="card.icon"
          :value="stats ? stats[card.key] : 0"
          :label="card.label"
          :color="card.color"
          :append="card.append ? card.append(stats!) : undefined"
          @click="card.onClick"
        />
      </div>

      <!-- 待办/超期 -->
      <div v-if="stats && stats.todoOverdue > 0" class="overdue-bar" @click="router.push('/todos')">
        <Clock :size="14" />
        <span>{{ stats.todoOverdue }} 个待办已超期，点击查看</span>
      </div>

      <!-- 内容模块 - 2列网格 -->
      <div class="dashboard-grid">
        <!-- 今日计划 -->
        <div class="dash-card">
          <div class="dash-card-header">
            <h3>今日计划</h3>
            <router-link to="/study-plans" class="dash-card-more">查看全部</router-link>
          </div>
          <div v-if="todayPlans.length === 0" class="empty-state" style="padding: 32px 24px;">
            <div class="empty-state__text">暂无进行中的计划 📋</div>
          </div>
          <div v-else class="dash-list">
            <div v-for="plan in todayPlans" :key="plan.id" class="dash-list-item" @click="router.push(`/study-plans/${plan.id}/edit`)">
              <Target :size="16" stroke="var(--info)" />
              <div class="dash-list-content">
                <span class="dash-list-title">{{ plan.title }}</span>
                <div class="dash-list-meta">
                  <span class="text-tertiary" v-if="plan.progress !== undefined">进度 {{ plan.progress }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近阅读 -->
        <div class="dash-card">
          <div class="dash-card-header">
            <h3>最近阅读</h3>
            <router-link to="/readings" class="dash-card-more">查看全部</router-link>
          </div>
          <div v-if="recentReadings.length === 0" class="empty-state" style="padding: 32px 24px;">
            <div class="empty-state__text">还没有阅读记录 📖</div>
          </div>
          <div v-else class="dash-list">
            <div v-for="r in recentReadings" :key="r.id" class="dash-list-item" @click="router.push(`/readings/${r.id}/edit`)">
              <BookMarked :size="16" stroke="var(--success)" />
              <div class="dash-list-content">
                <span class="dash-list-title">{{ r.title }}</span>
                <div class="dash-list-meta">
                  <span class="text-tertiary" v-if="r.author">{{ r.author }}</span>
                  <span class="text-tertiary">{{ formatRelative(r.updatedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 待办任务 -->
        <div class="dash-card">
          <div class="dash-card-header">
            <h3>待办任务</h3>
            <router-link to="/todos" class="dash-card-more">查看全部</router-link>
          </div>
          <div v-if="pendingTodos.length === 0" class="empty-state" style="padding: 32px 24px;">
            <div class="empty-state__text">没有未完成的任务 🎉</div>
          </div>
          <div v-else class="dash-list">
            <div v-for="todo in pendingTodos" :key="todo.id" class="dash-list-item" @click="handleToggleDone(todo.id)">
              <div class="todo-check" :class="{ checked: todo.isDone === 1 }">
                <CheckCircle v-if="todo.isDone === 1" :size="12" stroke="#fff" />
              </div>
              <div class="dash-list-content">
                <span class="dash-list-title">{{ todo.title }}</span>
                <div class="dash-list-meta">
                  <el-tag :type="todo.priority === 1 ? 'danger' : todo.priority === 2 ? 'warning' : 'info'" size="small">{{ todo.priorityLabel }}</el-tag>
                  <span v-if="todo.dueDate" class="text-tertiary" :class="{ 'text-danger': todo.isOverdue }">{{ todo.dueDate }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近笔记 -->
        <div class="dash-card">
          <div class="dash-card-header">
            <h3>最近更新</h3>
            <router-link to="/notes" class="dash-card-more">查看全部</router-link>
          </div>
          <div v-if="recentNotes.length === 0" class="empty-state" style="padding: 32px 24px;">
            <div class="empty-state__text">还没有笔记，开始写第一篇吧 ✍️</div>
          </div>
          <div v-else class="dash-list">
            <div v-for="note in recentNotes" :key="note.id" class="dash-list-item" @click="router.push(`/notes/${note.id}/edit`)">
              <FileText :size="16" stroke="var(--accent)" />
              <div class="dash-list-content">
                <span class="dash-list-title">{{ note.title }}</span>
                <div class="dash-list-meta">
                  <span v-for="cat in note.categories" :key="cat.id" class="inline-tag">{{ cat.name }}</span>
                  <span class="text-tertiary">{{ formatRelative(note.updatedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近收藏 -->
        <div class="dash-card">
          <div class="dash-card-header">
            <h3>最近收藏</h3>
            <router-link to="/bookmarks" class="dash-card-more">查看全部</router-link>
          </div>
          <div v-if="recentBookmarks.length === 0" class="empty-state" style="padding: 32px 24px;">
            <div class="empty-state__text">还没有收藏 🔖</div>
          </div>
          <div v-else class="dash-list">
            <div v-for="bm in recentBookmarks" :key="bm.id" class="dash-list-item" @click="router.push(`/bookmarks/${bm.id}/edit`)">
              <Bookmark :size="16" stroke="var(--warning)" />
              <div class="dash-list-content">
                <span class="dash-list-title">{{ bm.title }}</span>
                <div class="dash-list-meta">
                  <span class="text-tertiary" v-if="bm.url">{{ bm.url }}</span>
                  <span class="text-tertiary">{{ formatRelative(bm.updatedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 学习记录 -->
        <div class="dash-card">
          <div class="dash-card-header">
            <h3>学习记录</h3>
            <router-link to="/study-records" class="dash-card-more">查看全部</router-link>
          </div>
          <div v-if="recentStudies.length === 0" class="empty-state" style="padding: 32px 24px;">
            <div class="empty-state__text">还没有学习记录 📚</div>
          </div>
          <div v-else class="dash-list">
            <div v-for="s in recentStudies" :key="s.id" class="dash-list-item" @click="router.push(`/study-records/${s.id}/edit`)">
              <BookOpen :size="16" stroke="var(--info)" />
              <div class="dash-list-content">
                <span class="dash-list-title">{{ s.subject }}</span>
                <div class="dash-list-meta">
                  <span class="text-tertiary">{{ s.duration }} 分钟</span>
                  <span class="text-tertiary">{{ s.date }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.dashboard { margin: 0 auto; width: 100%; }
.welcome-section {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: var(--sp-6);
}
.welcome-section h1 { font-size: var(--text-2xl); font-weight: 600; margin-bottom: var(--sp-1); }
.welcome-sub { font-size: var(--text-sm); color: var(--text-secondary); }
.quick-actions { display: flex; gap: var(--sp-2); }

/* 骨架屏 */
.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-4); }
.skeleton-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--sp-3); }
.skeleton-card { height: 80px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }
.skeleton-card-wide { height: 180px; border-radius: var(--radius-lg); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

/* 统计卡片（样式已移至 StatCard 组件） */
.stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: var(--sp-3); margin-bottom: var(--sp-4); }

/* 超期提醒 */
.overdue-bar {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: 8px var(--sp-4); margin-bottom: var(--sp-4);
  background: var(--danger-light, #fef0f0); color: var(--danger);
  border-radius: var(--radius-sm); font-size: var(--text-sm);
  cursor: pointer; transition: opacity var(--transition);
}
.overdue-bar:hover { opacity: 0.85; }

/* 卡片区域 - 2列网格 */
.dashboard-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--sp-5); }

.dash-card {
  background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); overflow: hidden;
  transition: all var(--transition);
}
.dash-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.dash-card-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--sp-4) var(--sp-6); border-bottom: 1px solid var(--border-light);
}
.dash-card-header h3 { font-size: var(--text-sm); font-weight: 600; }
.dash-card-more { font-size: var(--text-xs); color: var(--text-tertiary); text-decoration: none; }
.dash-card-more:hover { color: var(--accent); }

.dash-list { padding: var(--sp-2); }
.dash-list-item {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-4); border-radius: var(--radius-sm);
  cursor: pointer; transition: background var(--transition);
}
.dash-list-item:hover { background: var(--bg-hover); }
.dash-list-content { flex: 1; min-width: 0; }
.dash-list-title { font-size: var(--text-sm); display: block; margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.dash-list-meta { display: flex; align-items: center; gap: var(--sp-2); font-size: var(--text-xs); }
.inline-tag { display: inline-block; padding: 0 6px; border-radius: 3px; background: var(--accent-light); color: var(--accent); font-size: var(--text-xs); }
.todo-check { width: 18px; height: 18px; border-radius: 50%; border: 2px solid var(--border-color); display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: all var(--transition); }
.todo-check.checked { background: var(--accent); border-color: var(--accent); }
</style>

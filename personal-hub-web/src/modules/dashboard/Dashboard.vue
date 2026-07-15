<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/authStore'
import { useLayoutStore } from '@/store/layoutStore'
import { getTodoList, getTodayTodos } from '@/modules/planning/api'
import { getStudyRecordList } from '@/modules/knowledge/api'
import { getRecentNotes } from '@/modules/knowledge/api'
import { getDashboardStats } from '@/modules/dashboard/api'
import type { DashboardStats } from '@/modules/dashboard/api'
import { getBookmarkList } from '@/modules/resource/api'
import { getReadingList } from '@/modules/knowledge/api'
import { getStudyPlanList } from '@/modules/planning/api'
import { BookOpen, FileText, CheckCircle, BookMarked, Target, PenLine, Clock, History, TrendingUp, ArrowRight } from 'lucide-vue-next'
import TodoDialog from '@/modules/planning/todo/TodoDialog.vue'
import type { TodoVO } from '@/types/todo'
import type { StudyRecordVO } from '@/types/study'
import type { NoteVO } from '@/types/note'
import type { ReadingVO } from '@/types/reading'
import type { StudyPlanVO } from '@/types/studyplan'
import { buildCreatePath, buildEditPath } from '@/utils/deepLink'

const router = useRouter()
const authStore = useAuthStore()
const layoutStore = useLayoutStore()

const stats = ref<DashboardStats | null>(null)
const recentNotes = ref<NoteVO[]>([])
const pendingTodos = ref<TodoVO[]>([])
const recentStudies = ref<StudyRecordVO[]>([])
const todayPlans = ref<StudyPlanVO[]>([])
const todayTodos = ref<TodoVO[]>([])
const recentReadings = ref<ReadingVO[]>([])
const loading = ref(true)
const todoDialogVisible = ref(false)

const hour = new Date().getHours()
const greeting = hour < 6 ? '夜深了' : hour < 12 ? '上午好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'


// 最近访问 (localStorage)
const recentVisits = ref<{ path: string; title: string }[]>([])
function loadRecentVisits() {
  try {
    const raw = localStorage.getItem('recent-visits')
    return raw ? JSON.parse(raw).slice(0, 5) : []
  } catch { return [] }
}
recentVisits.value = loadRecentVisits()

onMounted(async () => {
  loading.value = true
  try {
    const [statsRes, notesRes, todosRes, studyRes, plansRes, readingsRes, todayTodosRes] = await Promise.all([
      getDashboardStats().catch(() => null),
      getRecentNotes(1, 5).catch(() => null),
      getTodoList({ page: 1, size: 5, isDone: false }).catch(() => null),
      getStudyRecordList({ page: 1, size: 5 }).catch(() => null),
      getStudyPlanList({ page: 1, size: 5, status: 1 }).catch(() => null),
      getReadingList({ page: 1, size: 5 }).catch(() => null),
      getTodayTodos().catch(() => null)
    ])
    if (statsRes) stats.value = statsRes.data.data
    if (notesRes) recentNotes.value = notesRes.data.data.records
    if (todosRes) pendingTodos.value = todosRes.data.data.records
    if (studyRes) recentStudies.value = studyRes.data.data.records
    if (plansRes) todayPlans.value = plansRes.data.data.records
    if (todayTodosRes) todayTodos.value = todayTodosRes.data.data
    if (readingsRes) recentReadings.value = readingsRes.data.data.records
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
  const { toggleDone } = await import('@/modules/planning/api')
  await toggleDone(id)
  pendingTodos.value = pendingTodos.value.filter(t => t.id !== id)
  todayTodos.value = todayTodos.value.filter(t => t.id !== id)
}

function quickCreate(type: string) {
  switch (type) {
    case 'note': router.push('/notes/new'); break
    case 'todo': todoDialogVisible.value = true; break
    case 'diary': router.push(buildCreatePath('/diaries')); break
    case 'study': router.push(buildCreatePath('/study-records')); break
  }
}
</script>

<template>
  <div class="dashboard">
    <!-- 欢迎行 -->
    <div class="welcome-row">
      <div class="welcome-left">
        <h1>{{ authStore.user?.nickname || authStore.user?.username || '用户' }}，{{ greeting }} 👋</h1>
        <p class="welcome-sub" v-if="stats">
          本周学习 <strong>{{ formatDuration(stats.studyDurationThisWeek) }}</strong>
        </p>
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 4" :key="i" class="skeleton-block" />
    </div>

    <!-- 两栏布局 -->
    <div v-else class="dashboard-grid">

      <!-- =========== 左栏 =========== -->
      <div class="main-col">

        <!-- 今日任务 -->
        <div class="main-card">
          <div class="card-header">
            <div class="card-header-left">
              <CheckCircle :size="16" class="card-icon text-accent" />
              <h3>今日任务</h3>
            </div>
            <router-link to="/todos" class="card-more">查看全部 <ArrowRight :size="12" /></router-link>
          </div>
          <div v-if="todayTodos.length === 0 && todayPlans.length === 0" class="card-empty">暂无今日计划 📋</div>
          <div v-else class="card-list">
            <div v-for="todo in todayTodos" :key="'t'+todo.id" class="list-item" @click="handleToggleDone(todo.id)">
              <div class="todo-check" :class="{ checked: todo.isDone === 1 }">
                <CheckCircle v-if="todo.isDone === 1" :size="12" stroke="#fff" />
              </div>
              <span class="list-title">{{ todo.title }}</span>
              <el-tag v-if="todo.priority === 1" type="danger" size="small">高</el-tag>
            </div>
            <div v-for="plan in todayPlans.slice(0, 3)" :key="plan.id" class="list-item" @click="router.push(buildEditPath('/study-plans', plan.id))">
              <Target :size="16" class="list-icon-info" />
              <span class="list-title">{{ plan.name }}</span>
              <span class="list-meta">{{ plan.progress }}%</span>
            </div>
          </div>
        </div>

        <!-- 最近编辑 + 最近学习 (两列并排) -->
        <div class="dual-col">
          <div class="main-card">
            <div class="card-header">
              <div class="card-header-left">
                <FileText :size="16" class="card-icon text-accent" />
                <h3>最近编辑</h3>
              </div>
              <router-link to="/notes" class="card-more"><ArrowRight :size="12" /></router-link>
            </div>
            <div v-if="recentNotes.length === 0" class="card-empty">暂无</div>
            <div v-else class="card-list">
              <div v-for="note in recentNotes.slice(0, 4)" :key="note.id" class="list-item" @click="router.push(`/notes/${note.id}/edit`)">
                <FileText :size="14" class="list-icon-accent" />
                <span class="list-title">{{ note.title }}</span>
              </div>
            </div>
          </div>

          <div class="main-card">
            <div class="card-header">
              <div class="card-header-left">
                <BookOpen :size="16" class="card-icon text-info" />
                <h3>最近学习</h3>
              </div>
              <router-link to="/study-records" class="card-more"><ArrowRight :size="12" /></router-link>
            </div>
            <div v-if="recentStudies.length === 0" class="card-empty">暂无</div>
            <div v-else class="card-list">
              <div v-for="s in recentStudies.slice(0, 4)" :key="s.id" class="list-item" @click="router.push(buildEditPath('/study-records', s.id))">
                <BookOpen :size="14" class="list-icon-info" />
                <span class="list-title">{{ s.subject }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近阅读 -->
        <div class="main-card">
          <div class="card-header">
            <div class="card-header-left">
              <BookMarked :size="16" class="card-icon text-accent" />
              <h3>最近阅读</h3>
            </div>
            <router-link to="/readings" class="card-more">查看全部 <ArrowRight :size="12" /></router-link>
          </div>
          <div v-if="recentReadings.length === 0" class="card-empty">还没有阅读记录 📖</div>
          <div v-else class="card-list">
            <div v-for="r in recentReadings.slice(0, 4)" :key="r.id" class="list-item" @click="router.push(buildEditPath('/readings', r.id))">
              <BookMarked :size="14" class="list-icon-accent" />
              <span class="list-title">{{ r.bookTitle }}</span>
              <span class="list-meta" v-if="r.author">{{ r.author }}</span>
            </div>
          </div>
        </div>

        <!-- 待办任务（默认隐藏） -->
        <div v-if="pendingTodos.length > 0 && layoutStore.visibleDashboardCards.find(c => c.code === 'pending_todos')?.visible" class="main-card">
          <div class="card-header">
            <div class="card-header-left">
              <CheckCircle :size="16" class="card-icon text-warning" />
              <h3>待办任务</h3>
            </div>
            <router-link to="/todos" class="card-more">查看全部 <ArrowRight :size="12" /></router-link>
          </div>
          <div class="card-list">
            <div v-for="todo in pendingTodos.slice(0, 5)" :key="todo.id" class="list-item" @click="handleToggleDone(todo.id)">
              <div class="todo-check" :class="{ checked: todo.isDone === 1 }">
                <CheckCircle v-if="todo.isDone === 1" :size="12" stroke="#fff" />
              </div>
              <span class="list-title">{{ todo.title }}</span>
              <span v-if="todo.dueDate" class="list-meta" :class="{ 'text-danger': todo.isOverdue }">{{ todo.dueDate }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- =========== 右栏 =========== -->
      <div class="sidebar-col">
        <!-- 快捷操作 -->
        <div class="side-section">
          <div class="side-section-header">
            <PenLine :size="14" />
            <span>快捷操作</span>
          </div>
          <div class="quick-actions">
            <button class="quick-btn" @click="quickCreate('note')">
              <FileText :size="18" />
              <span>新建笔记</span>
            </button>
            <button class="quick-btn" @click="quickCreate('todo')">
              <CheckCircle :size="18" />
              <span>新建任务</span>
            </button>
            <button class="quick-btn" @click="quickCreate('study')">
              <BookOpen :size="18" />
              <span>开始学习</span>
            </button>
          </div>
        </div>

        <!-- 本周统计 -->
        <div class="side-section">
          <div class="side-section-header">
            <TrendingUp :size="14" />
            <span>本周统计</span>
          </div>
          <div class="weekly-stats" v-if="stats">
            <div class="stat-row" @click="router.push('/notes')">
              <FileText :size="14" class="stat-icon" />
              <span class="stat-label">笔记</span>
              <span class="stat-value">{{ stats.noteCount }}</span>
            </div>
            <div class="stat-row" @click="router.push('/todos')">
              <CheckCircle :size="14" class="stat-icon" />
              <span class="stat-label">待办</span>
              <span class="stat-value">{{ stats.todoDone }}/{{ stats.todoTotal }}</span>
            </div>
            <div class="stat-row" @click="router.push('/study-records')">
              <BookOpen :size="14" class="stat-icon" />
              <span class="stat-label">学习</span>
              <span class="stat-value">{{ formatDuration(stats.studyDurationThisWeek) }}</span>
            </div>
            <div class="stat-row" @click="router.push('/readings')">
              <BookMarked :size="14" class="stat-icon" />
              <span class="stat-label">阅读</span>
              <span class="stat-value">{{ stats.readingInProgress > 0 ? `${stats.readingInProgress} 在读` : stats.readingCount }}</span>
            </div>
          </div>
        </div>

        <!-- 提示：超期待办 -->
        <div v-if="stats && stats.todoOverdue > 0" class="side-overdue" @click="router.push('/todos')">
          <Clock :size="14" />
          <span>{{ stats.todoOverdue }} 个待办已超期</span>
        </div>

        <!-- 最近访问 -->
        <div class="side-section" v-if="recentVisits.length > 0">
          <div class="side-section-header">
            <History :size="14" />
            <span>最近访问</span>
          </div>
          <div class="recent-visits">
            <router-link
              v-for="v in recentVisits" :key="v.path"
              :to="v.path"
              class="visit-item"
            >
              <Clock :size="12" />
              <span>{{ v.title }}</span>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 超期提醒（内联于右栏，不再单独显示） -->
  </div>

  <TodoDialog v-model="todoDialogVisible" @saved="todoDialogVisible = false" />
</template>

<style scoped>
.dashboard { margin: 0 auto; width: 100%; }

/* ---- 欢迎行 ---- */
.welcome-row {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: var(--sp-6); gap: var(--sp-4);
}
.welcome-left h1 { font-size: var(--text-2xl); font-weight: 600; margin-bottom: var(--sp-1); }
.welcome-sub { font-size: var(--text-sm); color: var(--text-secondary); }
.welcome-sub strong { color: var(--text-primary); font-weight: 600; }
/* ---- 骨架屏 ---- */
.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-4); }
.skeleton-block { height: 160px; border-radius: var(--radius-lg); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

/* ---- 两栏布局 ---- */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: var(--sp-5);
  align-items: start;
}

/* ===== 左栏 ===== */
.main-col { display: flex; flex-direction: column; gap: var(--sp-4); }

.main-card {
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-lg); overflow: hidden;
}

.card-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--sp-3) var(--sp-5);
  border-bottom: 1px solid var(--border-light);
}
.card-header-left { display: flex; align-items: center; gap: var(--sp-2); }
.card-header h3 { font-size: var(--text-sm); font-weight: 600; }
.card-icon { flex-shrink: 0; }
.card-more {
  display: flex; align-items: center; gap: 2px;
  font-size: var(--text-xs); color: var(--text-tertiary); text-decoration: none;
}
.card-more:hover { color: var(--accent); }

.card-empty { padding: var(--sp-6); text-align: center; font-size: var(--text-sm); color: var(--text-tertiary); }

.card-list { padding: var(--sp-1) 0; }
.list-item {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-5);
  cursor: pointer; transition: background var(--transition);
  font-size: var(--text-sm);
}
.list-item:hover { background: var(--bg-hover); }
.list-title { flex: 1; min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.list-meta { font-size: var(--text-xs); color: var(--text-tertiary); flex-shrink: 0; }
.list-icon-accent { color: var(--accent); flex-shrink: 0; }
.list-icon-info { color: var(--info); flex-shrink: 0; }

.todo-check {
  width: 18px; height: 18px; border-radius: 50%;
  border: 2px solid var(--border-color);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; transition: all var(--transition);
}
.todo-check.checked { background: var(--accent); border-color: var(--accent); }

/* 并排双栏 */
.dual-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--sp-4);
}

/* ===== 右栏 ===== */
.sidebar-col { display: flex; flex-direction: column; gap: var(--sp-4); }

.side-section {
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-lg); overflow: hidden;
}
.side-section-header {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: var(--sp-3) var(--sp-4);
  border-bottom: 1px solid var(--border-light);
  font-size: var(--text-xs); font-weight: 600; color: var(--text-secondary);
  text-transform: uppercase; letter-spacing: 0.3px;
}

/* 快捷操作 */
.quick-actions { padding: var(--sp-3); display: flex; flex-direction: column; gap: var(--sp-2); }
.quick-btn {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer; font-size: var(--text-sm);
  font-family: var(--font-sans);
  transition: all var(--transition);
}
.quick-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-light);
}

/* 本周统计 */
.weekly-stats { padding: var(--sp-2); }
.stat-row {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-sm);
  cursor: pointer; transition: background var(--transition);
}
.stat-row:hover { background: var(--bg-hover); }
.stat-icon { color: var(--text-tertiary); flex-shrink: 0; }
.stat-label { flex: 1; font-size: var(--text-sm); color: var(--text-secondary); }
.stat-value { font-size: var(--text-sm); font-weight: 600; color: var(--text-primary); }

/* 超期提醒（右栏内联） */
.side-overdue {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-sm);
  background: var(--danger-light);
  color: var(--danger);
  font-size: var(--text-xs);
  cursor: pointer; transition: opacity var(--transition);
}
.side-overdue:hover { opacity: 0.85; }

/* 最近访问 */
.recent-visits { padding: var(--sp-2); }
.visit-item {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-sm);
  font-size: var(--text-sm); color: var(--text-secondary);
  text-decoration: none; transition: background var(--transition);
}
.visit-item:hover { background: var(--bg-hover); color: var(--text-primary); }

/* ---- 响应式 ---- */
@media (max-width: 1024px) {
  .dashboard-grid { grid-template-columns: 1fr; }
  .sidebar-col { display: grid; grid-template-columns: 1fr 1fr; gap: var(--sp-4); }
}
@media (max-width: 768px) {
  .welcome-row { flex-direction: column; align-items: flex-start; gap: var(--sp-3); }
  .welcome-left h1 { font-size: var(--text-xl); }
  .dashboard-grid { grid-template-columns: 1fr; }
  .sidebar-col { grid-template-columns: 1fr; }
  .dual-col { grid-template-columns: 1fr; }
}
</style>

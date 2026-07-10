<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/authStore'
import { getTodoList } from '@/api/todoApi'
import { getStudyRecordList } from '@/api/studyApi'
import { getRecentNotes } from '@/api/noteApi'
import { Plus, BookOpen, FileText, CheckCircle } from 'lucide-vue-next'
import type { TodoVO } from '@/types/todo'
import type { StudyRecordVO } from '@/types/study'
import type { NoteVO } from '@/types/note'

const router = useRouter()
const authStore = useAuthStore()

const recentNotes = ref<NoteVO[]>([])
const pendingTodos = ref<TodoVO[]>([])
const recentStudies = ref<StudyRecordVO[]>([])
const loading = ref(true)

const hour = new Date().getHours()
const greeting = hour < 6 ? '夜深了' : hour < 12 ? '上午好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'

onMounted(async () => {
  loading.value = true
  try {
    const [notesRes, todosRes, studyRes] = await Promise.all([
      getRecentNotes(1, 5).catch(() => null),
      getTodoList({ page: 1, size: 5, isDone: false }).catch(() => null),
      getStudyRecordList({ page: 1, size: 5 }).catch(() => null)
    ])
    if (notesRes) recentNotes.value = notesRes.data.data.records
    if (todosRes) pendingTodos.value = todosRes.data.data.records
    if (studyRes) recentStudies.value = studyRes.data.data.records
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

async function handleToggleDone(id: number) {
  const { toggleDone } = await import('@/api/todoApi')
  await toggleDone(id)
  pendingTodos.value = pendingTodos.value.filter(t => t.id !== id)
}
</script>

<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div>
        <h1>{{ authStore.user?.nickname || authStore.user?.username || '用户' }}，{{ greeting }} 👋</h1>
        <p class="welcome-sub">今天有什么计划？</p>
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
      <div v-for="i in 3" :key="i" class="skeleton-card" />
    </div>

    <div v-else class="dashboard-grid">
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

      <!-- 最近学习 -->
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
  </div>
</template>

<style scoped>
.dashboard { max-width: var(--reading-max-width); margin: 0 auto; }
.welcome-section {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: var(--sp-8);
}
.welcome-section h1 { font-size: var(--text-2xl); font-weight: 600; margin-bottom: var(--sp-1); }
.welcome-sub { font-size: var(--text-sm); color: var(--text-secondary); }
.quick-actions { display: flex; gap: var(--sp-2); }

.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-4); }
.skeleton-card { height: 180px; border-radius: var(--radius-lg); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }
.dashboard-grid { display: flex; flex-direction: column; gap: var(--sp-5); }

.dash-card {
  background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); overflow: hidden;
  transition: box-shadow var(--transition);
}
.dash-card:hover { box-shadow: var(--shadow-md); }
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

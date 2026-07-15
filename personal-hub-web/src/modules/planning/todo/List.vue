<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { getTodoList, deleteTodo, toggleDone } from '@/modules/planning/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check, Pencil, Trash2, Calendar, GripVertical } from 'lucide-vue-next'
import { EmptyState, PageHeader } from '@/components'
import TodoDialog from './TodoDialog.vue'
import type { TodoVO, TodoQuery } from '@/types/todo'
import Sortable from 'sortablejs'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'

type TabKey = 'all' | 'overdue' | 'today' | 'week' | 'later' | 'done'

const list = ref<TodoVO[]>([])
const loading = ref(false)
const query = ref<TodoQuery>({ page: 1, size: 9999, keyword: '' })
const currentTab = ref<TabKey>('all')
const listRef = ref<HTMLElement | null>(null)
let sortable: Sortable | null = null

const dialogVisible = ref(false)
const editId = ref<number | undefined>()

function openCreate() {
  editId.value = undefined
  dialogVisible.value = true
}
function openEdit(id: number) {
  editId.value = id
  dialogVisible.value = true
}

useDeepLinkDialog({ openCreate, openEdit })

onMounted(async () => {
  await fetchList()
  await nextTick()
  initSortable()
})
onUnmounted(() => { sortable?.destroy() })

function initSortable() {
  if (listRef.value) {
    sortable = Sortable.create(listRef.value, {
      animation: 200,
      handle: '.drag-handle',
      ghostClass: 'todo-item--ghost',
      onEnd: (evt) => {
        const item = filteredList.value.splice(evt.oldIndex!, 1)[0]
        filteredList.value.splice(evt.newIndex!, 0, item)
      }
    })
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getTodoList(query.value)
    list.value = res.data.data.records
  } finally {
    loading.value = false
  }
}

// ── 日期工具 ──
function isSameDay(a: string | null, b: Date): boolean {
  if (!a) return false
  const d = new Date(a)
  return d.getFullYear() === b.getFullYear() &&
    d.getMonth() === b.getMonth() &&
    d.getDate() === b.getDate()
}
function diffDays(dateStr: string): number {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const d = new Date(dateStr)
  d.setHours(0, 0, 0, 0)
  return Math.round((d.getTime() - now.getTime()) / 86400000)
}
function dueDateLabel(todo: TodoVO): { text: string; type: 'overdue' | 'today' | 'tomorrow' | 'soon' | 'future' } {
  if (!todo.dueDate) return { text: '', type: 'future' }
  if (todo.isDone === 1) return { text: todo.dueDate, type: 'future' }
  const diff = diffDays(todo.dueDate)
  if (diff < 0) return { text: `已超期 ${-diff} 天`, type: 'overdue' }
  if (diff === 0) return { text: '今天', type: 'today' }
  if (diff === 1) return { text: '明天', type: 'tomorrow' }
  if (diff <= 3) return { text: `${diff} 天后`, type: 'soon' }
  return { text: todo.dueDate, type: 'future' }
}

const today = new Date()

// ── 分组数据 ──
interface Group { key: TabKey; label: string; icon: string; items: TodoVO[] }

const groups = computed<Group[]>(() => {
  const all: TodoVO[] = [...list.value]
  const overdue: TodoVO[] = []
  const dueToday: TodoVO[] = []
  const thisWeek: TodoVO[] = []
  const later: TodoVO[] = []
  const done: TodoVO[] = []

  for (const t of list.value) {
    if (t.isDone === 1) { done.push(t); continue }
    if (t.isOverdue) { overdue.push(t) }
    else if (t.dueDate && isSameDay(t.dueDate, today)) { dueToday.push(t) }
    else if (t.dueDate && diffDays(t.dueDate) <= 7) { thisWeek.push(t) }
    else { later.push(t) }
  }

  return [
    { key: 'all', label: '全部', icon: '📋', items: all },
    { key: 'overdue', label: '已超期', icon: '🔴', items: overdue },
    { key: 'today', label: '今天', icon: '🟡', items: dueToday },
    { key: 'week', label: '本周', icon: '🟢', items: thisWeek },
    { key: 'later', label: '以后', icon: '📦', items: later },
    { key: 'done', label: '已完成', icon: '✅', items: done },
  ]
})

const tabs = computed(() =>
  groups.value.map(g => ({
    key: g.key,
    label: g.label,
    icon: g.icon,
    count: g.items.length,
  }))
)

const filteredList = computed(() =>
  groups.value.find(g => g.key === currentTab.value)?.items || []
)

function onTabChange(key: TabKey) {
  currentTab.value = key
}

function goCreate() { openCreate() }
function goEdit(id: number) { openEdit(id) }

async function handleToggleDone(id: number) {
  await toggleDone(id)
  ElMessage.success('状态已更新')
  await fetchList()
}
async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该任务？', '提示', { type: 'warning' })
  await deleteTodo(id)
  ElMessage.success('已删除')
  await fetchList()
}

const priorityOptions = [
  { value: 1, label: '高', type: 'danger' as const },
  { value: 2, label: '中', type: 'warning' as const },
  { value: 3, label: '低', type: 'info' as const },
]
</script>

<template>
  <div>
    <PageHeader title="待办任务" subtitle="管理日常任务" />

    <!-- 快捷操作条 -->
    <div class="todo-bar">
      <span class="todo-bar-count">共 {{ list.length }} 项</span>
      <button class="todo-bar-btn primary" @click="goCreate">
        <Plus :size="14" /> 新建任务
      </button>
    </div>

    <!-- Tab 导航 -->
    <div class="todo-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="todo-tab"
        :class="{ active: currentTab === tab.key, 'tab-overdue': tab.key === 'overdue' && tab.count > 0 }"
        @click="onTabChange(tab.key)"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        {{ tab.label }}
        <span class="tab-count" :class="tab.key">{{ tab.count }}</span>
      </button>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 4" :key="i" class="skeleton-todo" />
    </div>

    <template v-else-if="filteredList.length === 0">
      <EmptyState :icon="CheckCircle" illustration="todo" text="没有匹配的任务" action-label="新建任务" :action-icon="Plus" @action="goCreate" />
    </template>

    <div v-else ref="listRef" class="todo-list">
      <div
        v-for="todo in filteredList"
        :key="todo.id"
        class="todo-card"
        :class="{ 'todo-card--done': todo.isDone === 1 }"
      >
        <div class="todo-card-left">
          <div class="drag-handle"><GripVertical :size="13" /></div>
          <label class="todo-checkbox" @click="handleToggleDone(todo.id)">
            <div class="todo-check" :class="{ checked: todo.isDone === 1 }">
              <Check v-if="todo.isDone === 1" :size="12" stroke="#fff" stroke-width="3" />
            </div>
          </label>
        </div>

        <div class="todo-card-body" @click="goEdit(todo.id)">
          <div class="todo-title-row">
            <span class="todo-title" :class="{ 'line-through': todo.isDone === 1 }">{{ todo.title }}</span>
            <el-tag
              v-if="todo.priority"
              :type="priorityOptions.find(p => p.value === todo.priority)?.type"
              size="small"
              class="priority-tag"
            >{{ todo.priorityLabel }}</el-tag>
          </div>

          <div v-if="todo.content" class="todo-desc">{{ todo.content.slice(0, 100) }}</div>

          <div class="todo-meta">
            <span
              v-if="todo.dueDate"
              class="due-badge"
              :class="`due--${dueDateLabel(todo).type}`"
            >
              <Calendar :size="12" />
              {{ dueDateLabel(todo).text }}
            </span>
          </div>
        </div>

        <div class="todo-card-actions">
          <span v-if="todo.isDone === 1 && todo.completedAt" class="completed-time">
            {{ todo.completedAt.slice(0, 16) }}
          </span>
          <button class="icon-btn" title="编辑" @click.stop="goEdit(todo.id)"><Pencil :size="13" /></button>
          <button class="icon-btn icon-btn--danger" title="删除" @click.stop="handleDelete(todo.id)"><Trash2 :size="13" /></button>
        </div>
      </div>
    </div>

    <TodoDialog v-model="dialogVisible" :entity-id="editId" @saved="fetchList" />
  </div>
</template>

<style scoped>
/* ── 操作栏 ── */
.todo-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--sp-4);
}
.todo-bar-count {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  user-select: none;
}
.todo-bar-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
}
.todo-bar-btn.primary {
  background: var(--accent);
  color: #fff;
}
.todo-bar-btn.primary:hover { opacity: 0.9; }

/* ── Tab ── */
.todo-tabs {
  display: flex;
  gap: 2px;
  background: var(--bg-hover);
  padding: 3px;
  border-radius: var(--radius-lg);
  margin-bottom: var(--sp-5);
  overflow-x: auto;
}
.todo-tab {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
}
.todo-tab:hover { color: var(--text-primary); }
.todo-tab.active {
  background: var(--bg-card);
  color: var(--text-primary);
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.tab-icon { font-size: 13px; line-height: 1; }
.tab-count {
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  padding: 0 5px;
  background: var(--border-color);
  color: var(--text-tertiary);
}
.tab-count.overdue { background: var(--danger-light, #fef0f0); color: var(--danger); }
.tab-count.today { background: var(--accent-light); color: var(--accent); }
.tab-count.done { background: var(--success-light, #e6f7e6); color: var(--success); }

/* ── 卡片列表 ── */
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.todo-card {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  border: 1px solid transparent;
  transition: all var(--transition);
}
.todo-card:hover:not(.todo-card--done) {
  border-color: var(--border-color);
  box-shadow: var(--shadow-sm);
}
.todo-card--done { cursor: default; }

.todo-card-left {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  padding-top: 2px;
  flex-shrink: 0;
}
.drag-handle {
  cursor: grab;
  color: var(--text-tertiary);
  opacity: 0;
  transition: opacity var(--transition);
  display: flex;
  align-items: center;
  padding: 2px;
}
.todo-card:hover .drag-handle { opacity: 0.4; }
.drag-handle:hover { opacity: 0.8 !important; }
.todo-item--ghost { opacity: 0.2; }

.todo-checkbox { cursor: pointer; flex-shrink: 0; }
.todo-check {
  width: 18px; height: 18px;
  border-radius: 50%;
  border: 2px solid var(--border-color);
  display: flex; align-items: center; justify-content: center;
  transition: all var(--transition);
}
.todo-check:hover { border-color: var(--accent); }
.todo-check.checked { background: var(--accent); border-color: var(--accent); }

.todo-card-body {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}
.todo-title-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: 4px;
}
.todo-title {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
}
.line-through { color: var(--text-tertiary); }
.priority-tag { flex-shrink: 0; }

.todo-desc {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  flex-wrap: wrap;
}
.due-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  height: 24px;
  padding: 0 10px;
  border-radius: 12px;
  letter-spacing: 0.02em;
}
.due--overdue {
  background: var(--danger-light, #fef0f0);
  color: var(--danger);
  border: 1px solid color-mix(in srgb, var(--danger) 20%, transparent);
}
.due--today {
  background: var(--accent-light);
  color: var(--accent);
  border: 1px solid color-mix(in srgb, var(--accent) 20%, transparent);
}
.due--tomorrow {
  background: #fff7e6;
  color: var(--warning, #e6a23c);
  border: 1px solid color-mix(in srgb, var(--warning) 20%, transparent);
}
.due--soon {
  background: var(--bg-hover);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}
.due--future {
  font-size: 11px;
  color: var(--text-tertiary);
  font-weight: 400;
  gap: 3px;
}

.todo-card-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity var(--transition);
  align-items: center;
  padding-top: 2px;
}
.todo-card:hover .todo-card-actions { opacity: 1; }
.todo-card--done .todo-card-actions { opacity: 1; }

.completed-time {
  font-size: 11px;
  color: var(--success);
  white-space: nowrap;
  margin-right: var(--sp-2);
}

.icon-btn {
  background: none; border: none; cursor: pointer;
  padding: 5px; border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  transition: all var(--transition);
  display: flex; align-items: center;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }

.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-3); }
.skeleton-todo { height: 72px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }
</style>

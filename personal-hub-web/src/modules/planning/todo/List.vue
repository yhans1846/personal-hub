<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { getTodoList, deleteTodo, toggleDone } from '@/modules/planning/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check, Pencil, Trash2, Calendar, GripVertical, CheckCircle, ListTodo, AlertCircle, Sun, CalendarRange, Archive, CheckCheck } from 'lucide-vue-next'
import { EmptyState, PageHeader, ListPagination, ListToolbar } from '@/components'
import TodoDialog from './TodoDialog.vue'
import type { TodoVO, TodoQuery } from '@/types/todo'
import Sortable from 'sortablejs'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'

type TabKey = 'all' | 'overdue' | 'today' | 'week' | 'later' | 'done'

const list = ref<TodoVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<TodoQuery>({ page: 1, size: 10, keyword: '', dueScope: 'all' })
const currentTab = ref<TabKey>('all')
const listRef = ref<HTMLElement | null>(null)
let sortable: Sortable | null = null

const dialogVisible = ref(false)
const editId = ref<number | undefined>()

useMainContentFill()
const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})

function openCreate() {
  editId.value = undefined
  dialogVisible.value = true
}
function openEdit(id: number) {
  editId.value = id
  dialogVisible.value = true
}

useDeepLinkDialog({ openCreate, openEdit })

watch(list, async () => {
  await nextTick()
  initSortable()
})

function initSortable() {
  sortable?.destroy()
  sortable = null
  if (listRef.value) {
    sortable = Sortable.create(listRef.value, {
      animation: 200,
      handle: '.drag-handle',
      ghostClass: 'todo-item--ghost',
      onEnd: (evt) => {
        const item = list.value.splice(evt.oldIndex!, 1)[0]
        list.value.splice(evt.newIndex!, 0, item)
      },
    })
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getTodoList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
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

const TAB_META: { key: TabKey; label: string; icon: typeof ListTodo }[] = [
  { key: 'all', label: '全部', icon: ListTodo },
  { key: 'overdue', label: '已超期', icon: AlertCircle },
  { key: 'today', label: '今天', icon: Sun },
  { key: 'week', label: '本周', icon: CalendarRange },
  { key: 'later', label: '以后', icon: Archive },
  { key: 'done', label: '已完成', icon: CheckCheck },
]

const tabs = computed(() =>
  TAB_META.map(t => ({
    ...t,
    count: currentTab.value === t.key ? total.value : undefined as number | undefined,
  })),
)

function onSearch() {
  query.value.page = 1
  fetchList()
}

function onTabChange(key: TabKey) {
  currentTab.value = key
  query.value.dueScope = key
  query.value.page = 1
  fetchList()
}

function onPageChange(page: number) {
  query.value.page = page
  fetchList()
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
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="待办任务" :subtitle="`共 ${total} 项`" />

      <ListToolbar
        :search="query.keyword ?? ''"
        search-placeholder="搜索任务..."
        search-width="200px"
        create-label="新建任务"
        @update:search="query.keyword = $event"
        @search="onSearch"
        @create="goCreate"
      />

      <div class="todo-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="todo-tab"
          :class="{ active: currentTab === tab.key }"
          @click="onTabChange(tab.key)"
        >
          <component :is="tab.icon" :size="14" class="tab-icon" />
          {{ tab.label }}
          <span v-if="tab.count != null" class="tab-count" :class="tab.key">{{ tab.count }}</span>
        </button>
      </div>
    </div>

    <div class="plan-middle">
      <div v-if="loading" class="loading-skeleton">
        <div v-for="i in pageSize" :key="i" class="skeleton-todo" />
      </div>

      <EmptyState
        v-else-if="list.length === 0"
        :icon="CheckCircle"
        illustration="todo"
        text="没有匹配的任务"
        action-label="新建任务"
        :action-icon="Plus"
        @action="goCreate"
      />

      <div v-else ref="listRef" class="todo-list">
        <div
          v-for="todo in list"
          :key="todo.id"
          class="todo-card"
          :class="{ 'todo-card--done': todo.isDone === 1 }"
        >
          <div class="todo-card-left">
            <div class="drag-handle" title="仅当前页可排序"><GripVertical :size="13" /></div>
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
        <div
          v-for="n in Math.max(0, pageSize - list.length)"
          :key="'pad-' + n"
          class="todo-card todo-card--pad"
          aria-hidden="true"
        />
      </div>
    </div>

    <div class="plan-foot">
      <ListPagination v-if="total > 0" :total="total" :page="query.page ?? 1" :size="pageSize" @update:page="onPageChange" />
    </div>

    <TodoDialog v-model="dialogVisible" :entity-id="editId" @saved="fetchList" />
  </div>
</template>

<style scoped>
.plan-page {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.plan-top { flex-shrink: 0; }
.plan-middle {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.plan-foot { flex-shrink: 0; padding-top: 8px; }

.todo-tabs {
  display: flex;
  gap: 2px;
  background: var(--bg-hover);
  padding: 3px;
  border-radius: var(--radius-lg);
  margin-top: var(--sp-3);
  margin-bottom: var(--sp-3);
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
.tab-icon { flex-shrink: 0; opacity: 0.85; }
.todo-tab.active .tab-icon { opacity: 1; }
.tab-count {
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: var(--radius-md);
  padding: 0 5px;
  background: var(--border-color);
  color: var(--text-tertiary);
}
.tab-count.overdue { background: var(--danger-light, #fef0f0); color: var(--danger); }
.tab-count.today { background: var(--accent-light); color: var(--accent); }
.tab-count.done { background: var(--success-light, #e6f7e6); color: var(--success); }

.todo-list {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: 8px;
  align-content: stretch;
}
@media (max-width: 768px) {
  .todo-list { grid-template-columns: 1fr; }
}
.todo-card {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  transition: all var(--transition);
  min-height: 0;
  min-width: 0;
  overflow: hidden;
}
.todo-card--pad {
  visibility: hidden;
  pointer-events: none;
  background: transparent;
  border-color: transparent;
}
.todo-card:hover:not(.todo-card--done):not(.todo-card--pad) {
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
  overflow: hidden;
}
.todo-title-row {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-2);
  margin-bottom: 4px;
}
.todo-title {
  flex: 1;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
  min-width: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}
.line-through { color: var(--text-tertiary); text-decoration: line-through; }
.priority-tag { flex-shrink: 0; }

.todo-desc {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
  word-break: break-word;
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
  border-radius: var(--radius-lg);
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

.loading-skeleton {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: 8px;
  flex: 1;
  min-height: 0;
}
@media (max-width: 768px) {
  .loading-skeleton { grid-template-columns: 1fr; }
}
.skeleton-todo { min-height: 64px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }
</style>

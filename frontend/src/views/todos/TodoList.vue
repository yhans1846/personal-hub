<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getTodoList, deleteTodo, toggleDone } from '@/api/todoApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, CheckCircle, Pencil, Trash2, Calendar, GripVertical } from 'lucide-vue-next'
import { EmptyState, PageHeader, ListToolbar, ListPagination } from '@/components'
import type { TodoVO, TodoQuery } from '@/types/todo'
import Sortable from 'sortablejs'

const router = useRouter()
const list = ref<TodoVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<TodoQuery>({ page: 1, size: 9999, keyword: '' })
const showDone = ref(false)
const listRef = ref<HTMLElement | null>(null)
let sortable: Sortable | null = null

onMounted(async () => {
  await fetchList()
  await nextTick()
  if (listRef.value) {
    sortable = Sortable.create(listRef.value, {
      animation: 200,
      handle: '.drag-handle',
      ghostClass: 'todo-item--ghost',
      onEnd: (evt) => {
        const item = list.value.splice(evt.oldIndex!, 1)[0]
        list.value.splice(evt.newIndex!, 0, item)
      }
    })
  }
})

onUnmounted(() => { sortable?.destroy() })

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

const pendingList = computed(() => list.value.filter(t => t.isDone !== 1))
const doneList = computed(() => list.value.filter(t => t.isDone === 1))

function onSearch() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }
function onFilterChange() { query.value.page = 1; fetchList() }
function goCreate() { router.push('/todos/new') }
function goEdit(id: number) { router.push(`/todos/${id}/edit`) }

async function handleToggleDone(id: number) {
  await toggleDone(id)
  ElMessage.success('状态已更新')
  fetchList()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该任务？', '提示', { type: 'warning' })
  await deleteTodo(id)
  ElMessage.success('已删除')
  fetchList()
}

const priorityOptions = [
  { value: '', label: '全部', type: '' },
  { value: 1, label: '高', type: 'danger' as const },
  { value: 2, label: '中', type: 'warning' as const },
  { value: 3, label: '低', type: 'info' as const }
]

const doneOptions = [
  { value: '', label: '全部' },
  { value: false, label: '未完成' },
  { value: true, label: '已完成' }
]
</script>

<template>
  <div>
    <PageHeader title="待办任务" subtitle="管理日常任务" />

    <ListToolbar :search="query.keyword" search-placeholder="搜索任务..." search-width="200px" create-label="新建任务" @update:search="query.keyword = $event" @search="onSearch" @create="goCreate">
      <template #filters>
        <el-select v-model="query.priority" placeholder="优先级" style="width:100px" clearable @change="onFilterChange">
          <el-option v-for="item in priorityOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
        <el-select v-model="query.isDone" placeholder="状态" style="width:120px" clearable @change="onFilterChange">
          <el-option v-for="item in doneOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
      </template>
    </ListToolbar>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 5" :key="i" class="skeleton-todo" />
    </div>

    <EmptyState v-else-if="list.length === 0" :icon="CheckCircle" illustration="todo" text="没有待办任务" action-label="新建任务" :action-icon="Plus" @action="goCreate" />

    <div v-else class="todos-wrapper">
      <!-- 未完成 -->
      <div ref="listRef" class="todo-list">
        <div v-for="todo in pendingList" :key="todo.id" class="todo-item">
          <div class="drag-handle"><GripVertical :size="14" /></div>
          <label class="todo-checkbox" @click="handleToggleDone(todo.id)">
            <div class="todo-check"><CheckCircle v-if="todo.isDone === 1" :size="12" stroke="#fff" /></div>
          </label>
          <div class="todo-body" @click="goEdit(todo.id)">
            <div class="todo-header">
              <span class="todo-title">{{ todo.title }}</span>
              <el-tag :type="priorityOptions.find(p => p.value === todo.priority)?.type || 'info'" size="small">{{ todo.priorityLabel }}</el-tag>
            </div>
            <div v-if="todo.content" class="todo-desc">{{ todo.content.slice(0, 80) }}</div>
            <div class="todo-footer">
              <span v-if="todo.dueDate" class="todo-date" :class="{ 'text-danger': todo.isOverdue }"><Calendar :size="12" /> {{ todo.dueDate }}</span>
            </div>
          </div>
          <div class="todo-actions">
            <button class="icon-btn" @click.stop="goEdit(todo.id)"><Pencil :size="14" /></button>
            <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(todo.id)"><Trash2 :size="14" /></button>
          </div>
        </div>
      </div>

      <!-- 已完成 -->
      <div v-if="doneList.length > 0" class="done-section">
        <div class="done-header" @click="showDone = !showDone">
          <CheckCircle :size="14" class="done-icon" />
          <span>已完成 {{ doneList.length }}</span>
          <span class="done-toggle">{{ showDone ? '收起' : '展开' }}</span>
        </div>
        <div v-show="showDone" class="todo-list">
          <div v-for="todo in doneList" :key="todo.id" class="todo-item todo-item--done">
            <div class="drag-handle" style="visibility:hidden"><GripVertical :size="14" /></div>
            <label class="todo-checkbox" @click="handleToggleDone(todo.id)">
              <div class="todo-check checked"><CheckCircle :size="12" stroke="#fff" /></div>
            </label>
            <div class="todo-body" @click="goEdit(todo.id)">
              <div class="todo-header">
                <span class="todo-title text-line-through">{{ todo.title }}</span>
                <el-tag :type="priorityOptions.find(p => p.value === todo.priority)?.type || 'info'" size="small">{{ todo.priorityLabel }}</el-tag>
              </div>
              <div class="todo-footer">
                <span v-if="todo.dueDate" class="todo-date"><Calendar :size="12" /> {{ todo.dueDate }}</span>
              </div>
            </div>
            <div class="todo-actions">
              <button class="icon-btn" @click.stop="goEdit(todo.id)"><Pencil :size="14" /></button>
              <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(todo.id)"><Trash2 :size="14" /></button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <ListPagination v-if="total > query.size" :total="total" :page="query.page" :size="query.size" @update:page="onPageChange" />
  </div>
</template>

<style scoped>
.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-3); }
.skeleton-todo { height: 80px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.todo-list { display: flex; flex-direction: column; gap: var(--sp-2); }
.todo-item {
  display: flex; align-items: flex-start; gap: var(--sp-3);
  padding: var(--sp-4) var(--sp-5); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  transition: all var(--transition);
}
.todo-item:hover { box-shadow: var(--shadow-sm); border-color: var(--accent-border); }
.todo-item:active { cursor: grabbing; }
.todo-item--done { opacity: 0.6; }

.drag-handle { padding: 6px 2px; cursor: grab; opacity: 0; transition: opacity var(--transition); flex-shrink: 0; color: var(--text-tertiary); display: flex; align-items: center; }
.todo-item:hover .drag-handle { opacity: 0.5; }
.drag-handle:hover { opacity: 0.9 !important; }
.todo-item--ghost { opacity: 0.3; border: 1px dashed var(--accent) !important; }

.todo-checkbox { padding-top: 2px; cursor: pointer; flex-shrink: 0; }
.todo-check { width: 20px; height: 20px; border-radius: 50%; border: 2px solid var(--border-color); display: flex; align-items: center; justify-content: center; transition: all var(--transition); }
.todo-check:hover { border-color: var(--accent); }
.todo-check.checked { background: var(--accent); border-color: var(--accent); }

.todo-body { flex: 1; min-width: 0; cursor: pointer; }
.todo-header { display: flex; align-items: center; gap: var(--sp-2); margin-bottom: 4px; }
.todo-title { font-size: var(--text-sm); font-weight: 500; }
.text-line-through { text-decoration: line-through; color: var(--text-tertiary); }
.todo-desc { font-size: var(--text-sm); color: var(--text-secondary); line-height: var(--leading-normal); display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 4px; }
.todo-footer { display: flex; align-items: center; gap: var(--sp-3); font-size: var(--text-xs); color: var(--text-tertiary); }
.todo-date { display: flex; align-items: center; gap: 3px; }

.todo-actions { display: flex; gap: var(--sp-1); flex-shrink: 0; opacity: 0; transition: opacity var(--transition); align-items: center; }
.todo-item:hover .todo-actions { opacity: 1; }

.icon-btn { background: none; border: none; cursor: pointer; padding: 6px; border-radius: var(--radius-sm); color: var(--text-tertiary); transition: all var(--transition); display: flex; align-items: center; }
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }

.done-section { margin-top: var(--sp-4); }
.done-header { display: flex; align-items: center; gap: var(--sp-2); padding: var(--sp-3) var(--sp-4); cursor: pointer; border-radius: var(--radius-md); transition: background var(--transition); font-size: var(--text-sm); color: var(--text-secondary); user-select: none; }
.done-header:hover { background: var(--bg-hover); }
.done-icon { color: var(--success); }
.done-toggle { margin-left: auto; font-size: var(--text-xs); color: var(--text-tertiary); }
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTodoList, deleteTodo, toggleDone } from '@/api/todoApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, CheckCircle, Pencil, Trash2, Calendar } from 'lucide-vue-next'
import { EmptyState, PageHeader } from '@/components'
import type { TodoVO, TodoQuery } from '@/types/todo'

const router = useRouter()
const list = ref<TodoVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<TodoQuery>({ page: 1, size: 20, keyword: '' })

onMounted(() => fetchList())

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

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索任务..." style="width:200px" clearable @clear="onSearch" @keyup.enter="onSearch">
          <template #prefix><Search :size="14" style="color: var(--text-tertiary)" /></template>
        </el-input>
        <el-select v-model="query.priority" placeholder="优先级" style="width:100px" clearable @change="onFilterChange">
          <el-option v-for="item in priorityOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
        <el-select v-model="query.isDone" placeholder="状态" style="width:120px" clearable @change="onFilterChange">
          <el-option v-for="item in doneOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
      </div>
      <el-button type="primary" @click="goCreate">
        <Plus :size="14" /> 新建任务
      </el-button>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 5" :key="i" class="skeleton-todo" />
    </div>

    <EmptyState v-else-if="list.length === 0" :icon="CheckCircle" text="没有待办任务" action-label="新建任务" :action-icon="Plus" @action="goCreate" />

    <div v-else class="todo-list">
      <div v-for="todo in list" :key="todo.id" class="todo-item" :class="{ 'todo-item--done': todo.isDone === 1 }">
        <label class="todo-checkbox" @click="handleToggleDone(todo.id)">
          <div class="todo-check" :class="{ checked: todo.isDone === 1 }">
            <CheckCircle v-if="todo.isDone === 1" :size="12" stroke="#fff" />
          </div>
        </label>
        <div class="todo-body" @click="goEdit(todo.id)">
          <div class="todo-header">
            <span class="todo-title" :class="{ 'text-line-through': todo.isDone === 1 }">{{ todo.title }}</span>
            <el-tag :type="priorityOptions.find(p => p.value === todo.priority)?.type || 'info'" size="small">
              {{ todo.priorityLabel }}
            </el-tag>
          </div>
          <div v-if="todo.content" class="todo-desc">{{ todo.content.slice(0, 80) }}</div>
          <div class="todo-footer">
            <span v-if="todo.dueDate" class="todo-date" :class="{ 'text-danger': todo.isOverdue }">
              <Calendar :size="12" /> {{ todo.dueDate }}
            </span>
          </div>
        </div>
        <div class="todo-actions">
          <button class="icon-btn" @click.stop="goEdit(todo.id)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(todo.id)"><Trash2 :size="14" /></button>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="total > query.size"
      v-model:current-page="query.page"
      :total="total" :page-size="query.size"
      layout="total, prev, pager, next"
      style="margin-top: var(--sp-6); justify-content: flex-end"
      @current-change="onPageChange"
    />
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
.todo-item--done { opacity: 0.6; }

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
</style>

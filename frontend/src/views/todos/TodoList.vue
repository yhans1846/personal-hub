<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getTodoList, deleteTodo, toggleDone } from '@/api/todoApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { TodoVO, TodoQuery } from '@/types/todo'

const router = useRouter()
const list = ref<TodoVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<TodoQuery>({ page: 1, size: 10, keyword: '' })

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

/** 优先级样式 */
function priorityType(p: number) {
  if (p === 1) return 'danger'
  if (p === 2) return 'warning'
  return 'info'
}

/** 优先级标签 */
const priorityOptions = [
  { value: undefined, label: '全部' },
  { value: 1, label: '高' },
  { value: 2, label: '中' },
  { value: 3, label: '低' }
]

/** 完成状态标签 */
const doneOptions = [
  { value: undefined, label: '全部' },
  { value: false, label: '未完成' },
  { value: true, label: '已完成' }
]
</script>

<template>
  <div>
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索任务标题" style="width:200px" clearable @clear="onSearch" @keyup.enter="onSearch" />
        <el-select v-model="query.priority" placeholder="优先级" style="width:100px" clearable @change="onFilterChange">
          <el-option v-for="item in priorityOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
        <el-select v-model="query.isDone" placeholder="状态" style="width:120px" clearable @change="onFilterChange">
          <el-option v-for="item in doneOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
      </div>
      <el-button type="primary" @click="goCreate">新建任务</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="完成" width="60">
        <template #default="{ row }">
          <el-checkbox :model-value="row.isDone === 1" @change="handleToggleDone(row.id)" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="任务标题" min-width="200">
        <template #default="{ row }">
          <span :class="{ 'task-done': row.isDone === 1, 'task-overdue': row.isOverdue }">
            {{ row.title }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="优先级" width="90">
        <template #default="{ row }">
          <el-tag :type="priorityType(row.priority)" size="small">
            {{ row.priorityLabel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="dueDate" label="截止日期" width="120">
        <template #default="{ row }">
          <span v-if="row.dueDate" :class="{ 'text-danger': row.isOverdue }">{{ row.dueDate }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" text @click="goEdit(row.id)">编辑</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      :total="total" :page-size="query.size"
      layout="total, prev, pager, next"
      style="margin-top:16px; justify-content:flex-end"
      @current-change="onPageChange"
    />
  </div>
</template>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.task-done { text-decoration: line-through; color: #999; }
.task-overdue { color: #f56c6c; font-weight: 500; }
.text-danger { color: #f56c6c; }
.text-muted { color: #c0c4cc; }
</style>

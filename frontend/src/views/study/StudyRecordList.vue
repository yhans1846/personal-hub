<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStudyRecordList, deleteStudyRecord } from '@/api/studyApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { StudyRecordVO, StudyRecordQuery } from '@/types/study'

const router = useRouter()
const list = ref<StudyRecordVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<StudyRecordQuery>({ page: 1, size: 10, keyword: '' })

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getStudyRecordList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onSearch() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }

function goCreate() { router.push('/study-records/new') }
function goEdit(id: number) { router.push(`/study-records/${id}/edit`) }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该学习记录？', '提示', { type: 'warning' })
  await deleteStudyRecord(id)
  ElMessage.success('已删除')
  fetchList()
}

function formatHours(min: number) {
  if (min < 60) return `${min}分钟`
  return `${(min / 60).toFixed(1)}小时`
}
</script>

<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索主题/内容" style="width:240px" clearable @clear="onSearch" @keyup.enter="onSearch" />
      <el-button type="primary" @click="goCreate">新建学习记录</el-button>
    </div>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="subject" label="学习主题" min-width="160" />
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column label="时长" width="100">
        <template #default="{ row }">{{ formatHours(row.duration) }}</template>
      </el-table-column>
      <el-table-column prop="reflection" label="心得" min-width="160" show-overflow-tooltip />
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
</style>

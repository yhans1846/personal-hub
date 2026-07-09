<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNoteList, restoreNote, permanentDeleteNote } from '@/api/noteApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { NoteVO } from '@/types/note'

const list = ref<NoteVO[]>([])
const loading = ref(false)

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getNoteList({ isDeleted: true, page: 1, size: 100 })
    list.value = res.data.data.records
  } finally {
    loading.value = false
  }
}

async function handleRestore(id: number) {
  await restoreNote(id)
  ElMessage.success('已恢复')
  fetchList()
}

async function handlePermanentDelete(id: number) {
  await ElMessageBox.confirm('永久删除后不可恢复，确定？', '警告', { type: 'error', confirmButtonText: '确定删除' })
  await permanentDeleteNote(id)
  ElMessage.success('已永久删除')
  fetchList()
}
</script>

<template>
  <div>
    <h3>回收站</h3>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="updatedAt" label="删除时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="primary" text @click="handleRestore(row.id)">恢复</el-button>
          <el-button size="small" type="danger" text @click="handlePermanentDelete(row.id)">永久删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && list.length === 0" description="回收站为空" />
  </div>
</template>

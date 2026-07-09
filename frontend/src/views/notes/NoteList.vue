<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNoteList, deleteNote, toggleFavorite } from '@/api/noteApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { NoteVO, NoteQuery } from '@/types/note'

const router = useRouter()
const list = ref<NoteVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<NoteQuery>({ page: 1, size: 10, keyword: '' })
onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getNoteList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onSearch() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }

function goCreate() { router.push('/notes/new') }
function goEdit(id: number) { router.push(`/notes/${id}/edit`) }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定要将此笔记移入回收站吗？', '提示', { type: 'warning' })
  await deleteNote(id)
  ElMessage.success('已移入回收站')
  fetchList()
}

async function handleToggleFavorite(note: NoteVO) {
  await toggleFavorite(note.id)
  note.isFavorite = note.isFavorite ? 0 : 1
  ElMessage.success(note.isFavorite ? '已收藏' : '已取消收藏')
}
</script>

<template>
  <div class="note-list">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索标题" style="width: 240px" clearable @clear="onSearch" @keyup.enter="onSearch" />
      <el-button type="primary" @click="goCreate">新建笔记</el-button>
    </div>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="goEdit(row.id)">{{ row.title }}</el-link>
          <el-tag v-if="row.isFavorite" type="warning" size="small" style="margin-left:8px">收藏</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="140">
        <template #default="{ row }">
          <el-tag v-for="c in row.categories" :key="c.id" size="small" style="margin-right:4px">{{ c.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签" width="160">
        <template #default="{ row }">
          <el-tag v-for="t in row.tags" :key="t.id" size="small" type="info" style="margin-right:4px">{{ t.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text @click="handleToggleFavorite(row)">
            {{ row.isFavorite ? '取消' : '收藏' }}
          </el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.page"
      :total="total"
      :page-size="query.size"
      layout="total, prev, pager, next"
      style="margin-top:16px; justify-content:flex-end"
      @current-change="onPageChange"
    />
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>

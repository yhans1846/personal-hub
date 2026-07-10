<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNoteList, restoreNote, permanentDeleteNote } from '@/api/noteApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trash2 } from 'lucide-vue-next'
import type { NoteVO } from '@/types/note'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

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
    <PageHeader title="回收站" subtitle="已删除的笔记可以在此恢复或永久清除" />

    <div v-if="loading" style="padding: 48px 0; text-align: center; color: var(--text-tertiary);">加载中...</div>

    <EmptyState v-else-if="list.length === 0" :icon="Trash2" text="回收站为空" />

    <div v-else class="recycle-list">
      <div v-for="note in list" :key="note.id" class="recycle-item">
        <div class="recycle-item-info">
          <span class="recycle-item-title">{{ note.title }}</span>
          <span class="recycle-item-date">删除于 {{ note.updatedAt?.slice(0, 16)?.replace('T', ' ') }}</span>
        </div>
        <div class="recycle-item-actions">
          <el-button size="small" text @click="handleRestore(note.id)">恢复</el-button>
          <el-button size="small" text type="danger" @click="handlePermanentDelete(note.id)">永久删除</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.recycle-list { display: flex; flex-direction: column; gap: var(--sp-2); }
.recycle-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--sp-4) var(--sp-5); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  transition: box-shadow var(--transition);
}
.recycle-item:hover { box-shadow: var(--shadow-sm); }
.recycle-item-info { display: flex; flex-direction: column; gap: 2px; }
.recycle-item-title { font-size: var(--text-sm); font-weight: 500; }
.recycle-item-date { font-size: var(--text-xs); color: var(--text-tertiary); }
.recycle-item-actions { display: flex; gap: var(--sp-2); flex-shrink: 0; opacity: 0; transition: opacity var(--transition); }
.recycle-item:hover .recycle-item-actions { opacity: 1; }
</style>

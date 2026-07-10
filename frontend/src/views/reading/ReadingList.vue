<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getReadingList, deleteReading } from '@/api/readingApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Pencil, Trash2, BookOpen, Book, BookCheck, Calendar, BookMarked } from 'lucide-vue-next'
import type { ReadingVO, ReadingQuery } from '@/types/reading'

const router = useRouter()
const list = ref<ReadingVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<ReadingQuery>({ page: 1, size: 20, keyword: '' })

onMounted(() => fetchList())
async function fetchList() {
  loading.value = true
  try { const r = await getReadingList(query.value); list.value = r.data.data.records; total.value = r.data.data.total }
  finally { loading.value = false }
}
function onSearch() { query.value.page = 1; fetchList() }
function onFilterChange() { query.value.page = 1; fetchList() }
function onPageChange(p: number) { query.value.page = p; fetchList() }
function goCreate() { router.push('/readings/new') }
function goEdit(id: number) { router.push(`/readings/${id}/edit`) }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该阅读记录？', '提示', { type: 'warning' })
  await deleteReading(id); ElMessage.success('已删除'); fetchList()
}

function statusIcon(s: number) { return s === 0 ? Book : s === 1 ? BookOpen : BookCheck }
function statusType(s: number) { return s === 0 ? 'info' : s === 1 ? 'primary' : 'success' }

const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 0, label: '未读' }, { value: 1, label: '在读' }, { value: 2, label: '读完' }
]
</script>

<template>
  <div>
    <PageHeader title="阅读记录" subtitle="共 {{ total }} 本" />
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索书名/作者..." style="width:220px" clearable @clear="onSearch" @keyup.enter="onSearch">
          <template #prefix><Search :size="14" style="color: var(--text-tertiary)" /></template>
        </el-input>
        <el-select v-model="query.status" placeholder="状态" style="width:120px" clearable @change="onFilterChange">
          <el-option v-for="s in statusOptions" :key="s.label" :value="s.value" :label="s.label" />
        </el-select>
      </div>
      <el-button type="primary" @click="goCreate"><Plus :size="14" /> 添加书籍</el-button>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 4" :key="i" class="skeleton-book" />
    </div>
    <EmptyState v-else-if="list.length === 0" :icon="BookMarked" illustration="reading" text="还没有阅读记录，添加一本书吧" action-label="添加书籍" :action-icon="Plus" @action="goCreate" />
    <div v-else class="book-grid">
      <div v-for="book in list" :key="book.id" class="book-card" @click="goEdit(book.id)">
        <div v-if="book.coverUrl" class="book-cover"><img :src="book.coverUrl" alt="" /></div>
        <div class="book-info">
          <div class="book-title">{{ book.bookTitle }}</div>
          <div v-if="book.author" class="book-author">{{ book.author }}</div>
          <div class="book-meta">
            <el-tag :type="statusType(book.status)" size="small">
              <component :is="statusIcon(book.status)" :size="12" /> {{ book.statusLabel }}
            </el-tag>
            <span class="book-chapter" v-if="book.totalChapters > 0">{{ book.currentChapter }}/{{ book.totalChapters }} 章</span>
          </div>
          <el-progress :percentage="book.progress" :stroke-width="6" :color="book.progress >= 100 ? 'var(--success)' : 'var(--accent)'" />
        </div>
        <div class="book-actions" @click.stop>
          <button class="icon-btn" @click.stop="goEdit(book.id)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(book.id)"><Trash2 :size="14" /></button>
        </div>
      </div>
    </div>
    <el-pagination v-if="total > (query.size ?? 20)" v-model:current-page="query.page" :total="total" :page-size="query.size" layout="total, prev, pager, next" style="margin-top:var(--sp-6);justify-content:flex-end" @current-change="onPageChange" />
  </div>
</template>

<style scoped>
.loading-skeleton { display:flex;flex-direction:column;gap:var(--sp-3) }
.skeleton-book { height:96px;border-radius:var(--radius-md);background:var(--bg-hover);animation:pulse 1.5s ease-in-out infinite }
.book-grid { display:flex;flex-direction:column;gap:var(--sp-2) }
.book-card {
  display:flex;align-items:flex-start;gap:var(--sp-4);padding:var(--sp-4) var(--sp-5);
  background:var(--bg-card);border:1px solid var(--border-color);border-radius:var(--radius-md);
  cursor:pointer;transition:all var(--transition);position:relative
}
.book-card:hover { box-shadow:var(--shadow-sm);border-color:var(--accent-border) }
.book-cover { width:48px;height:64px;border-radius:var(--radius-sm);overflow:hidden;flex-shrink:0;background:var(--bg-hover) }
.book-cover img { width:100%;height:100%;object-fit:cover }
.book-info { flex:1;min-width:0 }
.book-title { font-size:var(--text-sm);font-weight:600;margin-bottom:2px }
.book-author { font-size:var(--text-xs);color:var(--text-secondary);margin-bottom:var(--sp-2) }
.book-meta { display:flex;align-items:center;gap:var(--sp-3);margin-bottom:var(--sp-2) }
.book-chapter { font-size:var(--text-xs);color:var(--text-tertiary) }
.book-actions { position:absolute;top:var(--sp-3);right:var(--sp-3);display:flex;gap:var(--sp-1);opacity:0;transition:opacity var(--transition) }
.book-card:hover .book-actions { opacity:1 }
.icon-btn { background:none;border:none;cursor:pointer;padding:6px;border-radius:var(--radius-sm);color:var(--text-tertiary);transition:all var(--transition);display:flex }
.icon-btn:hover { color:var(--accent);background:var(--accent-light) }
.icon-btn--danger:hover { color:var(--danger);background:var(--danger-light) }
</style>

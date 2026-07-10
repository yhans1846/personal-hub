<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getBookmarkList, deleteBookmark, getBookmarkCategories } from '@/api/bookmarkApi'
import { getTags } from '@/api/tagApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Pencil, Trash2, FolderOpen, Tag, Bookmark } from 'lucide-vue-next'
import type { BookmarkVO, BookmarkQuery, BookmarkCategoryVO } from '@/types/bookmark'
import type { TagVO } from '@/types/tag'

const router = useRouter()
const list = ref<BookmarkVO[]>([])
const total = ref(0)
const loading = ref(false)
const categories = ref<BookmarkCategoryVO[]>([])
const tags = ref<TagVO[]>([])
const query = ref<BookmarkQuery>({ page: 1, size: 20, keyword: '' })

onMounted(() => { fetchCategories(); fetchTags(); fetchList() })

async function fetchCategories() {
  try {
    const res = await getBookmarkCategories()
    categories.value = res.data.data
  } catch { /* ignore */ }
}

async function fetchTags() {
  try {
    const res = await getTags()
    tags.value = res.data.data
  } catch { /* ignore */ }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getBookmarkList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onSearch() { query.value.page = 1; fetchList() }
function onFilterChange() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }
function goCreate() { router.push('/bookmarks/new') }
function goEdit(id: number) { router.push(`/bookmarks/${id}/edit`) }
function goCategories() { router.push('/bookmarks/categories') }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该收藏？', '提示', { type: 'warning' })
  await deleteBookmark(id)
  ElMessage.success('已删除')
  fetchList()
}

function extractDomain(url: string) {
  try { return new URL(url).hostname } catch { return url }
}

function getFaviconUrl(url: string) {
  try {
    const u = new URL(url)
    return `https://www.google.com/s2/favicons?domain=${u.hostname}&sz=32`
  } catch { return '' }
}
</script>

<template>
  <div>
    <PageHeader title="收藏夹" subtitle="共 {{ total }} 个收藏" />

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索标题/网址..." style="width:200px" clearable @clear="onSearch" @keyup.enter="onSearch">
          <template #prefix><Search :size="14" style="color: var(--text-tertiary)" /></template>
        </el-input>
        <el-select v-model="query.categoryId" placeholder="全部分类" style="width:130px" clearable @change="onFilterChange">
          <el-option v-for="c in categories" :key="c.id" :value="c.id" :label="c.name" />
        </el-select>
        <el-select v-model="query.tagId" placeholder="全部标签" style="width:130px" clearable @change="onFilterChange">
          <el-option v-for="t in tags" :key="t.id" :value="t.id" :label="t.name">
            <span style="display:flex;align-items:center;gap:6px">
              <span class="tag-dot" :style="{ background: t.color }" />
              {{ t.name }}
            </span>
          </el-option>
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button @click="goCategories">
          <FolderOpen :size="14" /> 分类管理
        </el-button>
        <el-button type="primary" @click="goCreate">
          <Plus :size="14" /> 新建收藏
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 6" :key="i" class="skeleton-card" />
    </div>

    <EmptyState v-else-if="list.length === 0" :icon="Bookmark" illustration="bookmark" text="还没有收藏，添加一个吧" action-label="新建收藏" :action-icon="Plus" @action="goCreate" />

    <div v-else class="bookmark-grid">
      <div v-for="item in list" :key="item.id" class="bookmark-card" @click="goEdit(item.id)">
        <div class="card-top">
          <img :src="item.favicon || getFaviconUrl(item.url)" class="card-favicon" alt="" @error="($event.target as HTMLImageElement).style.display='none'" />
          <div class="card-title">{{ item.title }}</div>
        </div>
        <div class="card-desc">{{ item.description || extractDomain(item.url) }}</div>
        <div class="card-footer">
          <span v-if="item.categoryName" class="card-category">
            <FolderOpen :size="11" /> {{ item.categoryName }}
          </span>
          <div class="card-tags">
            <span v-for="tag in (item.tags || [])" :key="tag.id" class="card-tag">
              <span class="tag-dot" :style="{ background: tag.color }" />
              {{ tag.name }}
            </span>
          </div>
          <div class="card-actions" @click.stop>
            <button class="icon-btn" @click.stop="goEdit(item.id)"><Pencil :size="13" /></button>
            <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(item.id)"><Trash2 :size="13" /></button>
          </div>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="total > (query.size ?? 20)"
      v-model:current-page="query.page"
      :total="total" :page-size="query.size"
      layout="total, prev, pager, next"
      style="margin-top: var(--sp-6); justify-content: flex-end"
      @current-change="onPageChange"
    />
  </div>
</template>

<style scoped>
.loading-skeleton { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: var(--sp-3); }
.skeleton-card { height: 140px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.bookmark-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: var(--sp-3); }
.bookmark-card {
  display: flex; flex-direction: column; gap: var(--sp-2);
  padding: var(--sp-4); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  cursor: pointer; transition: all var(--transition);
}
.bookmark-card:hover { box-shadow: var(--shadow-sm); border-color: var(--accent-border); }

.card-top { display: flex; align-items: center; gap: var(--sp-2); }
.card-favicon { width: 20px; height: 20px; border-radius: 4px; flex-shrink: 0; }
.card-title { font-size: var(--text-sm); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-desc { font-size: var(--text-xs); color: var(--text-secondary); line-height: var(--leading-normal); overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; flex: 1; }
.card-footer { display: flex; align-items: center; gap: var(--sp-2); font-size: var(--text-xs); color: var(--text-tertiary); flex-wrap: wrap; }
.card-category { display: flex; align-items: center; gap: 2px; white-space: nowrap; }
.card-tags { display: flex; gap: 4px; flex-wrap: wrap; flex: 1; }
.card-tag { display: flex; align-items: center; gap: 3px; background: var(--bg-hover); padding: 1px 6px; border-radius: 4px; white-space: nowrap; }
.card-actions { display: flex; gap: 2px; opacity: 0; transition: opacity var(--transition); }
.bookmark-card:hover .card-actions { opacity: 1; }
.icon-btn { background: none; border: none; cursor: pointer; padding: 4px; border-radius: var(--radius-sm); color: var(--text-tertiary); transition: all var(--transition); display: flex; }
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }

.toolbar-right { display: flex; gap: var(--sp-2); }
.tag-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; flex-shrink: 0; }
</style>

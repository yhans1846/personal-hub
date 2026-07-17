<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PageHeader, EmptyState, ListToolbar, ListPagination } from '@/components'
import { getBookmarkList, deleteBookmark, updateBookmark } from '@/modules/resource/api'
import { getCategories } from '@/api/categoryApi'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Pencil, Trash2, FolderOpen, Bookmark, House } from 'lucide-vue-next'
import BookmarkDialog from './BookmarkDialog.vue'
import type { BookmarkVO, BookmarkQuery } from '@/types/bookmark'
import type { TagVO } from '@/types/tag'
import type { CategoryVO } from '@/types/category'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'

const router = useRouter()
const list = ref<BookmarkVO[]>([])
const total = ref(0)
const loading = ref(false)
const categories = ref<CategoryVO[]>([])
const tags = ref<TagVO[]>([])
const query = ref<BookmarkQuery>({ page: 1, size: 10, keyword: '' })

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

onMounted(() => {
  fetchCategories()
  fetchTags()
})

async function fetchCategories() {
  try {
    categories.value = (await getCategories('bookmark')).data.data
  } catch { /* ignore */ }
}

async function fetchTags() {
  try {
    tags.value = (await getTags()).data.data
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
function goCreate() { openCreate() }
function goEdit(id: number) { openEdit(id) }
function goCategories() { router.push('/categories') }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该收藏？', '提示', { type: 'warning' })
  await deleteBookmark(id)
  ElMessage.success('已删除')
  fetchList()
}

async function toggleDashboard(item: BookmarkVO) {
  const next = item.showOnDashboard === 1 ? 0 : 1
  await updateBookmark(item.id, {
    title: item.title,
    url: item.url,
    description: item.description || '',
    favicon: item.favicon || undefined,
    categoryId: item.categoryId,
    tagIds: (item.tags || []).map(t => t.id),
    showOnDashboard: next,
  })
  item.showOnDashboard = next
  ElMessage.success(next === 1 ? '已展示到首页' : '已从首页移除')
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
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="收藏夹" :subtitle="`共 ${total} 个收藏`" />

      <ListToolbar
        :search="query.keyword ?? ''"
        search-placeholder="搜索标题/网址..."
        search-width="200px"
        create-label="新建收藏"
        @update:search="query.keyword = $event"
        @search="onSearch"
        @create="goCreate"
      >
        <template #filters>
          <el-select v-model="query.categoryId" placeholder="分类" style="width:130px" clearable @change="onFilterChange">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-select v-model="query.tagId" placeholder="标签" style="width:130px" clearable @change="onFilterChange">
            <el-option v-for="t in tags" :key="t.id" :value="t.id" :label="t.name">
              <span style="display:flex;align-items:center;gap:6px">
                <span class="tag-dot" :style="{ background: t.color }" />
                {{ t.name }}
              </span>
            </el-option>
          </el-select>
        </template>
        <template #actions>
          <el-button @click="goCategories">分类管理</el-button>
        </template>
      </ListToolbar>
    </div>

    <div class="plan-middle">
      <div v-if="loading" class="bookmark-grid">
        <div v-for="i in pageSize" :key="i" class="skeleton-card" />
      </div>

      <EmptyState
        v-else-if="list.length === 0"
        :icon="Bookmark"
        illustration="bookmark"
        text="还没有收藏，添加一个吧"
        action-label="新建收藏"
        :action-icon="Plus"
        @action="goCreate"
      />

      <div v-else class="bookmark-grid">
        <div v-for="item in list" :key="item.id" class="bookmark-card" @click="goEdit(item.id)">
          <div class="card-top">
            <img
              :src="item.favicon || getFaviconUrl(item.url)"
              class="card-favicon"
              alt=""
              @error="($event.target as HTMLImageElement).style.display='none'"
            />
            <div class="card-title">{{ item.title }}</div>
            <button
              class="home-toggle"
              :class="{ active: item.showOnDashboard === 1 }"
              :title="item.showOnDashboard === 1 ? '取消首页展示' : '展示到首页'"
              @click.stop="toggleDashboard(item)"
            >
              <House :size="14" />
            </button>
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
        <div
          v-for="n in Math.max(0, pageSize - list.length)"
          :key="'pad-' + n"
          class="bookmark-card bookmark-card--pad"
          aria-hidden="true"
        />
      </div>
    </div>

    <div class="plan-foot">
      <ListPagination v-if="total > 0" :total="total" :page="query.page ?? 1" :size="pageSize" @update:page="onPageChange" />
    </div>

    <BookmarkDialog v-model="dialogVisible" :entity-id="editId" @saved="fetchList" />
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

.bookmark-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: 8px;
}
@media (max-width: 1100px) {
  .bookmark-grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
}
@media (max-width: 720px) {
  .bookmark-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

.skeleton-card {
  min-height: 0;
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}

.bookmark-card {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  padding: var(--sp-3) var(--sp-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition);
  min-height: 0;
  min-width: 0;
  overflow: hidden;
}
.bookmark-card--pad {
  visibility: hidden;
  pointer-events: none;
  background: transparent;
  border-color: transparent;
}
.bookmark-card:hover:not(.bookmark-card--pad) {
  box-shadow: var(--shadow-sm);
  border-color: var(--accent-border);
}

.card-top { display: flex; align-items: center; gap: var(--sp-2); }
.card-favicon { width: 20px; height: 20px; border-radius: 4px; flex-shrink: 0; }
.card-title {
  font-size: var(--text-sm);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}
.home-toggle {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--bg-hover);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all var(--transition);
}
.home-toggle:hover,
.home-toggle.active {
  color: var(--accent);
  background: var(--accent-light);
}
.card-desc {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
  min-height: 0;
}
.card-footer {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  flex-wrap: wrap;
}
.card-category { display: flex; align-items: center; gap: 2px; white-space: nowrap; }
.card-tags { display: flex; gap: 4px; flex-wrap: wrap; flex: 1; min-width: 0; }
.card-tag {
  display: flex;
  align-items: center;
  gap: 3px;
  background: var(--bg-hover);
  padding: 1px 6px;
  border-radius: 4px;
  white-space: nowrap;
}
.card-actions { display: flex; gap: 2px; opacity: 0; transition: opacity var(--transition); }
.bookmark-card:hover .card-actions { opacity: 1; }
.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  transition: all var(--transition);
  display: flex;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
.tag-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; flex-shrink: 0; }
</style>

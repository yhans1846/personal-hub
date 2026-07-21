<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { PageHeader, EmptyState, ListToolbar, ListPagination } from '@/components'
import { getBookmarkList, deleteBookmark, updateBookmark } from '@/modules/resource/api'
import { getCategories } from '@/modules/knowledge/api'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Pencil, Trash2, FolderOpen, Bookmark, House } from 'lucide-vue-next'
import BookmarkDialog from './BookmarkDialog.vue'
import type { BookmarkVO, BookmarkQuery } from '@/types/bookmark'
import type { TagVO } from '@/types/tag'
import type { CategoryVO } from '@/types/category'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'
import { useEntityDialogHost, usePaginatedList, type PageQuery } from '@/composables/usePaginatedList'
import { handleApiError, unwrapPage, unwrapResult } from '@/utils/apiResult'

/** 一屏 5×4 铺满（与文件列表一致） */
const PAGE_SIZE = 20

const categories = ref<CategoryVO[]>([])
const tags = ref<TagVO[]>([])

type BookmarkListQuery = BookmarkQuery & PageQuery

const { list, total, loading, query, fetchList, onSearch, onPageChange } = usePaginatedList<BookmarkVO, BookmarkListQuery>({
  initialQuery: { page: 1, size: PAGE_SIZE, keyword: '' },
  fetchPage: (q) => unwrapPage(getBookmarkList(q)),
  errorMessage: '加载收藏失败',
})

const { dialogVisible, editId, openCreate, openEdit } = useEntityDialogHost()

useMainContentFill()

useDeepLinkDialog({ openCreate, openEdit })

onMounted(() => {
  fetchList()
  fetchCategories()
  fetchTags()
})

async function fetchCategories() {
  try {
    categories.value = await unwrapResult(getCategories('bookmark'))
  } catch { /* ignore */ }
}

async function fetchTags() {
  try {
    tags.value = await unwrapResult(getTags())
  } catch { /* ignore */ }
}

function onFilterChange() { query.value.page = 1; fetchList() }
function goCreate() { openCreate() }
function goEdit(id: number) { openEdit(id) }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该收藏？', '提示', { type: 'warning' })
  try {
    await deleteBookmark(id)
    ElMessage.success('已删除')
    fetchList()
  } catch (e) {
    handleApiError(e, '删除失败')
  }
}

async function toggleDashboard(item: BookmarkVO) {
  const next = item.showOnDashboard === 1 ? 0 : 1
  await updateBookmark(item.id, {
    title: item.title,
    url: item.url,
    description: item.description || '',
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
    return `https://www.google.com/s2/favicons?domain=${u.hostname}&sz=64`
  } catch { return '' }
}

function onFaviconError(e: Event) {
  const el = e.target as HTMLImageElement
  el.style.display = 'none'
  const fallback = el.nextElementSibling as HTMLElement | null
  if (fallback) fallback.style.display = 'block'
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
      </ListToolbar>
    </div>

    <div class="plan-middle">
      <div v-if="loading" class="bookmark-grid">
        <div v-for="i in PAGE_SIZE" :key="i" class="skeleton-card" />
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
        <div
          v-for="item in list"
          :key="item.id"
          class="bookmark-card"
          @click="goEdit(item.id)"
        >
          <div class="card-preview">
            <img
              :src="getFaviconUrl(item.url)"
              class="card-preview-favicon"
              alt=""
              @error="onFaviconError"
            />
            <Bookmark :size="28" class="card-preview-fallback" />
            <button
              type="button"
              class="home-toggle"
              :class="{ active: item.showOnDashboard === 1 }"
              :title="item.showOnDashboard === 1 ? '取消首页展示' : '展示到首页'"
              @click.stop="toggleDashboard(item)"
            >
              <House :size="14" />
            </button>
          </div>
          <div class="card-title" :title="item.title">{{ item.title }}</div>
          <div class="card-footer">
            <span v-if="item.categoryName" class="card-category">
              <FolderOpen :size="11" /> {{ item.categoryName }}
            </span>
            <span v-else class="card-category">{{ item.description || extractDomain(item.url) }}</span>
            <div class="card-tags">
              <span v-for="tag in (item.tags || []).slice(0, 2)" :key="tag.id" class="card-tag">
                <span class="tag-dot" :style="{ background: tag.color }" />
                {{ tag.name }}
              </span>
            </div>
            <div class="card-actions" @click.stop>
              <button type="button" class="icon-btn" title="编辑" @click.stop="goEdit(item.id)">
                <Pencil :size="13" />
              </button>
              <button type="button" class="icon-btn icon-btn--danger" title="删除" @click.stop="handleDelete(item.id)">
                <Trash2 :size="13" />
              </button>
            </div>
          </div>
        </div>
        <div
          v-for="n in Math.max(0, PAGE_SIZE - list.length)"
          :key="'pad-' + n"
          class="bookmark-card bookmark-card--pad"
          aria-hidden="true"
        />
      </div>
    </div>

    <div class="plan-foot">
      <ListPagination v-if="total > 0" :total="total" :page="query.page ?? 1" :size="PAGE_SIZE" @update:page="onPageChange" />
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
  box-sizing: border-box;
  min-width: 0;
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: border-color var(--transition), box-shadow var(--transition);
  overflow: hidden;
}
.bookmark-card--pad {
  visibility: hidden;
  pointer-events: none;
  background: transparent;
  border-color: transparent;
  cursor: default;
}
.bookmark-card:hover:not(.bookmark-card--pad) {
  box-shadow: var(--shadow-sm);
  border-color: var(--accent-border);
}

.card-preview {
  position: relative;
  flex: 1;
  min-height: 0;
  width: 100%;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: color-mix(in srgb, var(--accent) 8%, var(--bg-hover));
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-preview-favicon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  object-fit: contain;
}
.card-preview-fallback {
  display: none;
  flex-shrink: 0;
}
.home-toggle {
  position: absolute;
  top: 6px;
  right: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--bg-card) 88%, transparent);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all var(--transition);
}
.home-toggle:hover,
.home-toggle.active {
  color: var(--accent);
  background: var(--accent-light);
}

.card-title {
  flex-shrink: 0;
  font-size: var(--text-sm);
  font-weight: 500;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 2px;
  color: var(--text-primary);
}
.card-footer {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  padding: 0 2px;
  min-height: 22px;
}
.card-category {
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 42%;
  flex-shrink: 1;
  min-width: 0;
}
.card-tags {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}
.card-tag {
  display: flex;
  align-items: center;
  gap: 3px;
  background: var(--bg-hover);
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 72px;
}
.card-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity var(--transition);
}
.bookmark-card:hover:not(.bookmark-card--pad) .card-actions { opacity: 1; }
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

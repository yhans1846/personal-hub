<script setup lang="ts">
import { ref, computed } from 'vue'
import { PageHeader, EmptyState, ListPagination, ListToolbar } from '@/components'
import {
  getReadingList, deleteReading, createReading, exportReadings,
} from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  BookMarked, Plus, MoreHorizontal, LayoutList, LayoutGrid, Download, Star, Book,
} from 'lucide-vue-next'
import ReadingDialog from './ReadingDialog.vue'
import type { ReadingVO, ReadingQuery } from '@/types/reading'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'
import { useProductViewMode } from '@/composables/useProductViewMode'
import { useEntityDialogHost, usePaginatedList, type PageQuery } from '@/composables/usePaginatedList'
import { formatDate, formatRelativeUpdated, formatUpdated } from '@/utils/formatTime'
import { handleApiError, unwrapPage } from '@/utils/apiResult'
import { triggerBlobDownload } from '@/utils/file'

type ReadingListQuery = ReadingQuery & PageQuery
const coverLoadFailed = ref<Set<number>>(new Set())
const { viewMode, setViewMode } = useProductViewMode('reading-view', 'table')

const { list, total, loading, query, fetchList, onSearch, onPageChange } = usePaginatedList<ReadingVO, ReadingListQuery>({
  initialQuery: {
    page: 1,
    size: 10,
    keyword: '',
    sortBy: 'updatedAt',
    sortDir: 'desc',
  },
  fetchPage: (q) => unwrapPage(getReadingList(q)),
  onFetched: () => { coverLoadFailed.value = new Set() },
  errorMessage: '加载阅读记录失败',
})

const { dialogVisible: drawerVisible, editId, openCreate, openEdit } = useEntityDialogHost()

useDeepLinkDialog({ openCreate, openEdit })

useMainContentFill()
const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})

function onFilterChange() {
  query.value.page = 1
  fetchList()
}

const exporting = ref(false)

async function handleExport(scope: 'filtered' | 'all') {
  if (exporting.value) return
  exporting.value = true
  try {
    const filters = scope === 'filtered'
      ? {
          keyword: query.value.keyword,
          status: query.value.status,
          sortBy: query.value.sortBy,
          sortDir: query.value.sortDir,
        }
      : undefined
    const res = await exportReadings(scope, filters)
    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    triggerBlobDownload(blob, '阅读记录.xlsx')
    ElMessage.success(scope === 'all' ? '已导出全部记录' : '已导出当前筛选结果')
  } catch (e) {
    handleApiError(e, '导出失败')
  } finally {
    exporting.value = false
  }
}

const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 0, label: '未读' },
  { value: 1, label: '在读' },
  { value: 2, label: '读完' },
]

const sortOptions = [
  { value: 'updatedAt', label: '最近更新' },
  { value: 'createdAt', label: '创建时间' },
  { value: 'progress', label: '进度' },
  { value: 'startDate', label: '开始时间' },
  { value: 'bookTitle', label: '书名' },
]

function statusMeta(status: number) {
  if (status === 1) return { color: 'var(--accent)', label: '在读', done: false }
  if (status === 2) return { color: 'var(--success)', label: '读完', done: true }
  return { color: 'var(--text-tertiary)', label: '未读', done: false }
}

function chapterText(book: ReadingVO) {
  if (!book.totalChapters) return '—'
  return `${book.currentChapter ?? 0}/${book.totalChapters}`
}

/** 无封面时：书名首字 + 稳定软色背景 */
const COVER_PALETTE = [
  { bg: 'color-mix(in srgb, #5b8def 16%, transparent)', fg: '#4a7ad4' },
  { bg: 'color-mix(in srgb, #67c23a 16%, transparent)', fg: '#529b2e' },
  { bg: 'color-mix(in srgb, #e6a23c 16%, transparent)', fg: '#b88230' },
  { bg: 'color-mix(in srgb, #f56c6c 14%, transparent)', fg: '#c45656' },
  { bg: 'color-mix(in srgb, #909399 16%, transparent)', fg: '#73767a' },
  { bg: 'color-mix(in srgb, #9b6bdf 16%, transparent)', fg: '#7d57b2' },
]

function coverInitial(title: string) {
  const t = (title || '').trim()
  if (!t) return '书'
  const ch = Array.from(t)[0]
  return ch?.toUpperCase() || '书'
}

function coverPlaceholderStyle(title: string) {
  const t = title || ''
  let hash = 0
  for (let i = 0; i < t.length; i++) hash = (hash * 31 + t.charCodeAt(i)) >>> 0
  const c = COVER_PALETTE[hash % COVER_PALETTE.length]
  return { background: c.bg, color: c.fg }
}

function onCoverError(id: number) {
  const next = new Set(coverLoadFailed.value)
  next.add(id)
  coverLoadFailed.value = next
}

function showCoverImg(book: ReadingVO) {
  return !!book.coverUrl && !coverLoadFailed.value.has(book.id)
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该阅读记录？', '提示', { type: 'warning' })
  try {
    await deleteReading(id)
    ElMessage.success('已删除')
    fetchList()
  } catch (e) {
    handleApiError(e, '删除失败')
  }
}

async function handleCopy(book: ReadingVO) {
  await createReading({
    bookTitle: `${book.bookTitle}（副本）`,
    author: book.author,
    coverUrl: book.coverUrl,
    totalChapters: book.totalChapters,
    currentChapter: 0,
    progress: 0,
    status: 0,
    notes: book.notes,
    startDate: null,
    endDate: null,
  })
  ElMessage.success('已复制')
  fetchList()
}

function onRowAction(cmd: string, book: ReadingVO) {
  if (cmd === 'edit') openEdit(book.id)
  else if (cmd === 'copy') handleCopy(book)
  else if (cmd === 'delete') handleDelete(book.id)
}

const headerSubtitle = computed(() => `共 ${total.value} 本`)
</script>

<template>
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="阅读记录" :subtitle="headerSubtitle" />

      <ListToolbar
        :search="query.keyword ?? ''"
        search-placeholder="搜索书名/作者..."
        search-width="200px"
        create-label="添加书籍"
        @update:search="query.keyword = $event"
        @search="onSearch"
        @create="openCreate"
      >
        <template #filters>
          <el-select v-model="query.status" placeholder="状态" clearable style="width:110px" @change="onFilterChange">
            <el-option v-for="s in statusOptions" :key="s.label" :value="s.value" :label="s.label" />
          </el-select>
          <el-select v-model="query.sortBy" style="width:120px" @change="onFilterChange">
            <el-option v-for="s in sortOptions" :key="s.value" :value="s.value" :label="s.label" />
          </el-select>
          <div class="view-toggle">
            <button type="button" class="view-btn" :class="{ active: viewMode === 'table' }" title="列表" @click="setViewMode('table')">
              <LayoutList :size="15" />
            </button>
            <button type="button" class="view-btn" :class="{ active: viewMode === 'card' }" title="卡片" @click="setViewMode('card')">
              <LayoutGrid :size="15" />
            </button>
          </div>
        </template>
        <template #actions>
          <el-dropdown trigger="click" :disabled="exporting" @command="(cmd: string) => handleExport(cmd as 'filtered' | 'all')">
            <el-button :loading="exporting">
              <Download :size="14" /> 导出
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="filtered">导出当前</el-dropdown-item>
                <el-dropdown-item command="all">导出全部</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </ListToolbar>
    </div>

    <div class="plan-middle">
      <div v-if="loading" class="loading-skeleton">
        <div v-for="i in 5" :key="i" class="skeleton-row" />
      </div>

      <EmptyState
        v-else-if="list.length === 0"
        :icon="BookMarked"
        illustration="reading"
        text="还没有阅读记录，添加一本书吧"
        action-label="添加书籍"
        :action-icon="Plus"
        @action="openCreate"
      />

      <div v-else-if="viewMode === 'table'" class="product-table">
        <div class="pt-head">
          <div class="col-title">书名</div>
          <div class="col-author">作者</div>
          <div class="col-status">状态</div>
          <div class="col-progress">进度</div>
          <div class="col-chapter">章节</div>
          <div class="col-rating">评分</div>
          <div class="col-date">开始</div>
          <div class="col-date">结束</div>
          <div class="col-updated">更新</div>
          <div class="col-actions" />
        </div>
        <div class="pt-body">
          <div
            v-for="book in list"
            :key="book.id"
            class="pt-row"
            @click="openEdit(book.id)"
          >
            <div class="col-title">
              <div class="title-cell">
                <div v-if="showCoverImg(book)" class="thumb">
                  <img :src="book.coverUrl" alt="" @error="onCoverError(book.id)" />
                </div>
                <div
                  v-else-if="book.coverUrl"
                  class="thumb thumb--broken"
                  title="封面加载失败"
                >
                  <Book :size="14" />
                </div>
                <div v-else class="thumb thumb--placeholder" :style="coverPlaceholderStyle(book.bookTitle)">
                  {{ coverInitial(book.bookTitle) }}
                </div>
                <div class="name-title">{{ book.bookTitle }}</div>
              </div>
            </div>
            <div class="col-author cell-text">{{ book.author || '—' }}</div>
            <div class="col-status">
              <span class="status-dot-label">
                <template v-if="statusMeta(book.status).done">✅</template>
                <i v-else class="status-dot" :style="{ background: statusMeta(book.status).color }" />
                {{ statusMeta(book.status).label }}
              </span>
            </div>
            <div class="col-progress">
              <el-progress
                :percentage="book.progress ?? 0"
                :stroke-width="6"
                :color="(book.progress ?? 0) >= 100 ? '#67c23a' : 'var(--accent)'"
              />
            </div>
            <div class="col-chapter cell-text">{{ chapterText(book) }}</div>
            <div class="col-rating cell-text">
              <span v-if="book.rating" class="rating-cell">
                <Star :size="12" fill="var(--warning)" color="var(--warning)" /> {{ book.rating }}
              </span>
              <span v-else class="muted">—</span>
            </div>
            <div class="col-date cell-date">{{ formatDate(book.startDate, '—') }}</div>
            <div class="col-date cell-date">{{ formatDate(book.endDate, '—') }}</div>
            <div class="col-updated cell-date">{{ formatUpdated(book.updatedAt) }}</div>
            <div class="col-actions" @click.stop>
              <el-dropdown trigger="click" @command="(cmd: string) => onRowAction(cmd, book)">
                <button type="button" class="icon-action more-btn" @click.stop>
                  <MoreHorizontal :size="16" />
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="copy">复制</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <div
            v-for="n in Math.max(0, pageSize - list.length)"
            :key="'pad-' + n"
            class="pt-row pt-row--pad"
            aria-hidden="true"
          />
        </div>
      </div>

      <div v-else class="card-grid">
        <div
          v-for="book in list"
          :key="book.id"
          class="book-card"
          @click="openEdit(book.id)"
        >
          <div class="card-body">
            <div class="card-hero">
              <div v-if="showCoverImg(book)" class="cover">
                <img :src="book.coverUrl" alt="" @error="onCoverError(book.id)" />
              </div>
              <div
                v-else-if="book.coverUrl"
                class="cover cover--broken"
                title="封面加载失败"
              >
                <Book :size="32" />
              </div>
              <div v-else class="cover cover--placeholder" :style="coverPlaceholderStyle(book.bookTitle)">
                {{ coverInitial(book.bookTitle) }}
              </div>
              <div class="card-main">
                <div class="card-top">
                  <div class="name-title">{{ book.bookTitle }}</div>
                  <span class="status-dot-label">
                    <template v-if="statusMeta(book.status).done">✅</template>
                    <i v-else class="status-dot" :style="{ background: statusMeta(book.status).color }" />
                    {{ statusMeta(book.status).label }}
                  </span>
                </div>
                <div class="card-author">{{ book.author || '作者 —' }}</div>
                <div class="card-meta">
                  <span>{{ chapterText(book) }} 章</span>
                  <span v-if="book.rating" class="rating-cell">
                    <Star :size="13" fill="var(--warning)" color="var(--warning)" /> {{ book.rating }}/5
                  </span>
                </div>
              </div>
            </div>
            <div class="card-stats">
              <el-progress
                :percentage="book.progress ?? 0"
                :stroke-width="8"
                :color="(book.progress ?? 0) >= 100 ? '#67c23a' : 'var(--accent)'"
                class="card-progress"
              />
            </div>
          </div>
          <div class="card-footer" @click.stop>
            <span class="card-updated">{{ formatRelativeUpdated(book.updatedAt) }}</span>
            <el-dropdown trigger="click" @command="(cmd: string) => onRowAction(cmd, book)">
              <button type="button" class="icon-action" @click.stop>
                <MoreHorizontal :size="15" />
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="copy">复制</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <div
          v-for="n in Math.max(0, pageSize - list.length)"
          :key="'card-pad-' + n"
          class="book-card book-card--pad"
          aria-hidden="true"
        />
      </div>
    </div>

    <div class="plan-foot">
      <ListPagination
        v-if="list.length > 0 || total > 0"
        :total="total"
        :page="query.page ?? 1"
        :size="pageSize"
        @update:page="onPageChange"
      />
    </div>

    <ReadingDialog v-model="drawerVisible" :entity-id="editId" @saved="fetchList" />
  </div>
</template>

<style scoped>
/* 共享 .product-table / .card-grid / 单元格见 styles/product-list.css */
.plan-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
.plan-top { flex-shrink: 0; }
.plan-middle {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.plan-foot { flex-shrink: 0; padding-top: 8px; }

.loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.skeleton-row {
  flex: 1;
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 本页列宽 */
.pt-head, .pt-row {
  grid-template-columns:
    minmax(160px, 2fr)
    100px
    72px
    120px
    72px
    64px
    96px
    96px
    120px
    48px;
}
.pt-body {
  grid-template-rows: repeat(10, minmax(0, 1fr));
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.thumb {
  width: 32px;
  height: 32px;
  aspect-ratio: 1;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  background: var(--bg-hover);
}
.thumb img { width: 100%; height: 100%; object-fit: cover; }
.thumb--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0;
  user-select: none;
}
.thumb--broken {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-hover);
  color: var(--text-tertiary);
  border: 1px solid var(--border-light);
}

.rating-cell {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.icon-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--text-tertiary);
  border-radius: var(--radius-sm);
  cursor: pointer;
}
.icon-action:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

.book-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 14px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: border-color var(--transition-duration) ease, background var(--transition-duration) ease;
  min-height: 0;
  overflow: hidden;
}
.book-card:hover:not(.book-card--pad) {
  border-color: var(--accent-border, var(--border-color));
  background: var(--bg-hover);
}
.book-card--pad {
  cursor: default;
  pointer-events: none;
  opacity: 0.35;
  border-style: dashed;
}
.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
}
.card-hero {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  flex: 1;
  min-height: 0;
}
.cover {
  width: 96px;
  height: 96px;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
  background: var(--bg-hover);
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 700;
  user-select: none;
}
.cover--broken {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-hover);
  color: var(--text-tertiary);
  border: 1px solid var(--border-light);
}
.card-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 2px;
}
.card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}
.card-top .name-title {
  font-size: 16px;
  font-weight: 650;
  line-height: 1.35;
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-top .status-dot-label {
  flex-shrink: 0;
  font-size: 13px;
  margin-top: 2px;
}
.card-author {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: auto;
}
.card-stats {
  flex-shrink: 0;
}
.card-progress :deep(.el-progress__text) {
  font-size: 13px !important;
  min-width: 40px;
}
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
  flex-shrink: 0;
}
.card-updated {
  font-size: 12px;
  color: var(--text-tertiary);
}
</style>

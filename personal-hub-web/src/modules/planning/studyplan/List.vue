<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { PageHeader, EmptyState, ListPagination } from '@/components'
import {
  getStudyPlanList, getStudyPlanStats, deleteStudyPlan,
  createStudyPlan, updateStudyPlan, exportStudyPlans,
} from '@/modules/planning/api'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Target, Plus, MoreHorizontal, ExternalLink, LayoutList, LayoutGrid, Link2, Download,
} from 'lucide-vue-next'
import StudyPlanDialog from './StudyPlanDialog.vue'
import type { StudyPlanVO, StudyPlanQuery, StudyPlanStats } from '@/types/studyplan'
import type { TagVO } from '@/types/tag'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'
import { useProductViewMode } from '@/composables/useProductViewMode'

const list = ref<StudyPlanVO[]>([])
const total = ref(0)
const loading = ref(false)
const tags = ref<TagVO[]>([])
const stats = ref<StudyPlanStats>({ total: 0, pending: 0, learning: 0, done: 0, paused: 0 })
const { viewMode, setViewMode } = useProductViewMode('study-plan-view', 'table')
const query = ref<StudyPlanQuery>({
  page: 1,
  size: 10,
  keyword: '',
  sortBy: 'updatedAt',
  sortDir: 'desc',
})

const drawerVisible = ref(false)
const editId = ref<number | undefined>()

function openCreate() {
  editId.value = undefined
  drawerVisible.value = true
}

function openEdit(id: number) {
  editId.value = id
  drawerVisible.value = true
}

useDeepLinkDialog({ openCreate, openEdit })

useMainContentFill()
const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})

onMounted(() => {
  fetchTags()
  fetchStats()
})

async function fetchTags() {
  try {
    tags.value = (await getTags()).data.data
  } catch { /* ignore */ }
}

async function fetchStats() {
  try {
    stats.value = (await getStudyPlanStats()).data.data
  } catch { /* ignore */ }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getStudyPlanList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function refreshAll() {
  fetchStats()
  fetchList()
}

function onSearch() {
  query.value.page = 1
  fetchList()
}

function onFilterChange() {
  query.value.page = 1
  fetchList()
}

function onPageChange(page: number) {
  query.value.page = page
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
          tagId: query.value.tagId,
          sortBy: query.value.sortBy,
          sortDir: query.value.sortDir,
        }
      : undefined
    const res = await exportStudyPlans(scope, filters)
    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '学习计划.xlsx'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success(scope === 'all' ? '已导出全部计划' : '已导出当前筛选结果')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 0, label: '未开始' },
  { value: 1, label: '学习中' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已暂停' },
]

const sortOptions = [
  { value: 'updatedAt', label: '最近更新' },
  { value: 'createdAt', label: '创建时间' },
  { value: 'startDate', label: '开始时间' },
  { value: 'endDate', label: '完成时间' },
  { value: 'name', label: '名称' },
]

function statusMeta(status: number) {
  if (status === 1) return { color: '#67c23a', label: '学习中', done: false }
  if (status === 2) return { color: '#67c23a', label: '已完成', done: true }
  if (status === 3) return { color: '#e6a23c', label: '已暂停', done: false }
  return { color: '#c0c4cc', label: '未开始', done: false }
}

function sourceDisplay(source: string | null) {
  if (!source) return null
  const s = source.trim().toLowerCase()
  if (s.includes('b站') || s.includes('bilibili') || s === 'b') return { icon: '📺', text: 'Bilibili' }
  if (s.includes('网站') || s.includes('官网') || s.includes('web')) return { icon: '🌐', text: '官网' }
  if (s.includes('书') || s.includes('图书')) return { icon: '📘', text: '图书' }
  if (s.includes('udemy')) return { icon: '🎓', text: 'Udemy' }
  if (s.includes('文档')) return { icon: '📄', text: source }
  return { icon: '🔗', text: source }
}

function formatDate(d: string | null) {
  if (!d) return '—'
  return d.slice(0, 10)
}

function formatDateDot(d: string | null) {
  if (!d) return '—'
  return d.slice(0, 10).replace(/-/g, '.')
}

function formatDateRange(start: string | null, end: string | null) {
  return `${formatDateDot(start)} ~ ${formatDateDot(end)}`
}

function formatUpdated(d: string | null | undefined) {
  if (!d) return '—'
  const s = d.replace('T', ' ')
  return s.length >= 16 ? s.slice(0, 16) : s.slice(0, 10)
}

function formatRelativeUpdated(d: string | null | undefined) {
  if (!d) return '更新于 —'
  const t = new Date(d.replace(' ', 'T')).getTime()
  if (Number.isNaN(t)) return `更新于 ${formatDate(d)}`
  const diff = Date.now() - t
  const m = Math.floor(diff / 60000)
  if (m < 1) return '更新于 刚刚'
  if (m < 60) return `更新于 ${m} 分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `更新于 ${h} 小时前`
  const days = Math.floor(h / 24)
  if (days < 7) return `更新于 ${days} 天前`
  return `更新于 ${formatDate(d)}`
}

function sourceAuthorLine(source: string | null, author: string | null) {
  const src = sourceDisplay(source)
  const parts: string[] = []
  if (src) parts.push(`${src.icon} ${src.text}`)
  if (author?.trim()) parts.push(author.trim())
  if (parts.length === 0) return null
  return parts.join(' · ')
}

function visibleTags(planTags: StudyPlanVO['tags']) {
  const list = planTags || []
  return { shown: list.slice(0, 2), more: Math.max(0, list.length - 2) }
}

function softTagStyle(color?: string) {
  const c = color || '#909399'
  return {
    color: c,
    background: `color-mix(in srgb, ${c} 12%, transparent)`,
    borderColor: `color-mix(in srgb, ${c} 22%, transparent)`,
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该计划？', '提示', { type: 'warning' })
  await deleteStudyPlan(id)
  ElMessage.success('已删除')
  refreshAll()
}

async function handleCopy(plan: StudyPlanVO) {
  await createStudyPlan({
    name: `${plan.name}（副本）`,
    source: plan.source,
    author: plan.author,
    url: plan.url,
    remark: plan.remark,
    progress: 0,
    startDate: null,
    endDate: null,
    status: 0,
    tagIds: (plan.tags || []).map(t => t.id),
  })
  ElMessage.success('已复制')
  refreshAll()
}

async function handleArchive(plan: StudyPlanVO) {
  await updateStudyPlan(plan.id, {
    name: plan.name,
    source: plan.source,
    author: plan.author,
    url: plan.url,
    remark: plan.remark,
    progress: plan.progress,
    startDate: plan.startDate,
    endDate: plan.endDate,
    status: 3,
    tagIds: (plan.tags || []).map(t => t.id),
  })
  ElMessage.success('已归档（暂停）')
  refreshAll()
}

function onRowAction(cmd: string, plan: StudyPlanVO) {
  if (cmd === 'edit') openEdit(plan.id)
  else if (cmd === 'copy') handleCopy(plan)
  else if (cmd === 'archive') handleArchive(plan)
  else if (cmd === 'delete') handleDelete(plan.id)
}

const headerSubtitle = computed(() => `共 ${stats.value.total} 个计划`)
</script>

<template>
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="学习计划" :subtitle="headerSubtitle">
        <div class="stat-badges">
          <span class="stat-badge"><i class="dot learning" />学习中 {{ stats.learning }}</span>
          <span class="stat-badge"><i class="dot done" />已完成 {{ stats.done }}</span>
          <span class="stat-badge"><i class="dot paused" />暂停 {{ stats.paused }}</span>
          <span class="stat-badge"><i class="dot pending" />未开始 {{ stats.pending }}</span>
        </div>
      </PageHeader>

      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="query.keyword"
            placeholder="搜索计划..."
            clearable
            class="search-input"
            @clear="onSearch"
            @keyup.enter="onSearch"
          >
            <template #prefix>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
            </template>
          </el-input>
          <el-select v-model="query.tagId" placeholder="分类" clearable class="filter-select" @change="onFilterChange">
            <el-option v-for="t in tags" :key="t.id" :value="t.id" :label="t.name" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable class="filter-select" @change="onFilterChange">
            <el-option v-for="s in statusOptions" :key="s.label" :value="s.value" :label="s.label" />
          </el-select>
          <el-select v-model="query.sortBy" class="filter-select sort-select" @change="onFilterChange">
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
        </div>
        <div class="toolbar-actions">
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
          <el-button type="primary" @click="openCreate">
            <Plus :size="14" /> 新建计划
          </el-button>
        </div>
      </div>
    </div>

    <div class="plan-middle">
      <div v-if="loading" class="loading-skeleton">
        <div v-for="i in 5" :key="i" class="skeleton-row" />
      </div>

      <EmptyState
        v-else-if="list.length === 0"
        :icon="Target"
        illustration="plan"
        text="还没有学习计划，创建一个吧"
        action-label="新建计划"
        :action-icon="Plus"
        @action="openCreate"
      />

      <div v-else-if="viewMode === 'table'" class="product-table">
        <div class="pt-head">
          <div class="col-name">名称</div>
          <div class="col-cat">分类</div>
          <div class="col-status">状态</div>
          <div class="col-progress">进度</div>
          <div class="col-date">开始</div>
          <div class="col-date">结束</div>
          <div class="col-source">来源</div>
          <div class="col-author">作者</div>
          <div class="col-updated">更新</div>
          <div class="col-remark">备注</div>
          <div class="col-actions" />
        </div>
        <!-- 固定 10 行槽位，均分剩余高度，不满 10 条留空行位 -->
        <div class="pt-body">
          <div
            v-for="plan in list"
            :key="plan.id"
            class="pt-row"
            @click="openEdit(plan.id)"
          >
            <div class="col-name">
              <div class="name-title">{{ plan.name }}</div>
            </div>
            <div class="col-cat">
              <template v-if="plan.tags?.length">
                <span
                  v-for="t in plan.tags"
                  :key="t.id"
                  class="soft-tag"
                  :style="softTagStyle(t.color)"
                >{{ t.name }}</span>
              </template>
              <span v-else class="muted">—</span>
            </div>
            <div class="col-status">
              <span class="status-dot-label">
                <template v-if="statusMeta(plan.status).done">✅</template>
                <i v-else class="status-dot" :style="{ background: statusMeta(plan.status).color }" />
                {{ statusMeta(plan.status).label }}
              </span>
            </div>
            <div class="col-progress">
              <el-progress
                :percentage="plan.progress ?? 0"
                :stroke-width="6"
                :color="(plan.progress ?? 0) >= 100 ? '#67c23a' : 'var(--accent)'"
              />
            </div>
            <div class="col-date cell-date">{{ formatDate(plan.startDate) }}</div>
            <div class="col-date cell-date">{{ formatDate(plan.endDate) }}</div>
            <div class="col-source">
              <span v-if="sourceDisplay(plan.source)" class="source-chip">
                <span>{{ sourceDisplay(plan.source)!.icon }}</span>
                {{ sourceDisplay(plan.source)!.text }}
              </span>
              <span v-else class="muted">—</span>
            </div>
            <div class="col-author cell-text">{{ plan.author || '—' }}</div>
            <div class="col-updated cell-date">{{ formatUpdated(plan.updatedAt) }}</div>
            <div class="col-remark">
              <el-tooltip v-if="plan.remark" :content="plan.remark" placement="top" :show-after="300">
                <span class="remark-text">{{ plan.remark }}</span>
              </el-tooltip>
              <span v-else class="muted">—</span>
            </div>
            <div class="col-actions" @click.stop>
              <el-tooltip v-if="plan.url" content="打开课程" placement="top">
                <a :href="plan.url" class="icon-action" target="_blank" rel="noopener noreferrer" @click.stop>
                  <Link2 :size="15" />
                </a>
              </el-tooltip>
              <el-dropdown trigger="click" @command="(cmd: string) => onRowAction(cmd, plan)">
                <button type="button" class="icon-action more-btn" @click.stop>
                  <MoreHorizontal :size="16" />
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="copy">复制</el-dropdown-item>
                    <el-dropdown-item command="archive">归档</el-dropdown-item>
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
          v-for="plan in list"
          :key="plan.id"
          class="plan-card"
          @click="openEdit(plan.id)"
        >
          <div class="card-top">
            <div class="name-title">{{ plan.name }}</div>
            <span class="status-dot-label">
              <template v-if="statusMeta(plan.status).done">✅</template>
              <i v-else class="status-dot" :style="{ background: statusMeta(plan.status).color }" />
              {{ statusMeta(plan.status).label }}
            </span>
          </div>
          <div v-if="plan.tags?.length" class="card-tags">
            <span
              v-for="t in visibleTags(plan.tags).shown"
              :key="t.id"
              class="soft-tag"
              :style="softTagStyle(t.color)"
            >{{ t.name }}</span>
            <span v-if="visibleTags(plan.tags).more" class="tag-more">+{{ visibleTags(plan.tags).more }}</span>
          </div>
          <div class="card-source-line">
            <template v-if="sourceAuthorLine(plan.source, plan.author)">
              {{ sourceAuthorLine(plan.source, plan.author) }}
            </template>
            <span v-else class="muted">来源 —</span>
          </div>
          <el-progress
            :percentage="plan.progress ?? 0"
            :stroke-width="6"
            :color="(plan.progress ?? 0) >= 100 ? '#67c23a' : 'var(--accent)'"
            class="card-progress"
          />
          <div class="card-dates">{{ formatDateRange(plan.startDate, plan.endDate) }}</div>
          <el-tooltip v-if="plan.remark" :content="plan.remark" placement="top" :show-after="300">
            <div class="card-remark">{{ plan.remark }}</div>
          </el-tooltip>
          <div v-else class="card-remark card-remark--empty" />
          <div class="card-footer" @click.stop>
            <span class="card-updated">{{ formatRelativeUpdated(plan.updatedAt) }}</span>
            <div class="card-actions">
              <a v-if="plan.url" :href="plan.url" class="icon-action" target="_blank" rel="noopener noreferrer" @click.stop>
                <ExternalLink :size="14" />
              </a>
              <el-dropdown trigger="click" @command="(cmd: string) => onRowAction(cmd, plan)">
                <button type="button" class="icon-action" @click.stop>
                  <MoreHorizontal :size="15" />
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="copy">复制</el-dropdown-item>
                    <el-dropdown-item command="archive">归档</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
        <div
          v-for="n in Math.max(0, pageSize - list.length)"
          :key="'card-pad-' + n"
          class="plan-card plan-card--pad"
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

    <StudyPlanDialog v-model="drawerVisible" :entity-id="editId" @saved="refreshAll" />
  </div>
</template>

<style scoped>
/* Product list styles — shared extract deferred (盘点 P2-1) */
.plan-page {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.plan-top {
  flex-shrink: 0;
}
.plan-top :deep(.page-header) {
  margin-bottom: 12px;
}
.plan-middle {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.plan-foot {
  flex-shrink: 0;
  padding-top: 8px;
}
.plan-foot :deep(.el-pagination) {
  margin-top: 0 !important;
}

.stat-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.stat-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-hover);
  border-radius: 999px;
}
.stat-badge .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
}
.dot.learning, .dot.done { background: #67c23a; }
.dot.paused { background: #e6a23c; }
.dot.pending { background: #c0c4cc; }

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
}
.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.search-input { width: 200px; }
.filter-select { width: 120px; }
.sort-select { width: 130px; }

.view-toggle {
  display: inline-flex;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  overflow: hidden;
}
.view-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
}
.view-btn.active {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.skeleton-row {
  flex: 1;
  border-radius: 8px;
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}

/* Product table：表头 + 10 行均分剩余高度 */
.product-table {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-card);
}
.pt-head, .pt-row {
  display: grid;
  /* 名称 | 分类 | 状态 | 进度 | 开始 | 结束 | 来源 | 作者 | 更新 | 备注 | 操作 */
  grid-template-columns:
    minmax(148px, 1.8fr)
    92px
    72px
    128px
    102px
    102px
    104px
    96px
    136px
    minmax(120px, 1.4fr)
    72px;
  align-items: center;
  column-gap: 12px;
  padding: 0 16px;
}
.pt-head {
  flex-shrink: 0;
  height: 36px;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-tertiary);
  border-bottom: 1px solid var(--border-light);
}
.pt-body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-rows: repeat(10, minmax(0, 1fr));
}
.pt-row {
  min-height: 0;
  height: 100%;
  cursor: pointer;
  border-bottom: 1px solid var(--border-light);
  transition: background 0.15s ease;
}
.pt-row:last-child { border-bottom: none; }
.pt-row:hover:not(.pt-row--pad) { background: var(--bg-hover); }
.pt-row--pad {
  cursor: default;
  pointer-events: none;
}

.name-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cell-text {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.soft-tag {
  display: inline-flex;
  align-items: center;
  padding: 1px 7px;
  border-radius: 6px;
  font-size: 12px;
  border: 1px solid transparent;
  margin-right: 4px;
}
.muted { color: var(--text-placeholder); font-size: 13px; }

.status-dot-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}

.col-progress :deep(.el-progress__text) {
  font-size: 12px !important;
  min-width: 36px;
}

.cell-date {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.remark-text {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.source-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.col-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 2px;
}
.icon-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  text-decoration: none;
}
.icon-action:hover {
  background: var(--bg-card);
  color: var(--text-primary);
}

/* 固定 5×2：一页 10 条铺满中间区域，避免 auto-fill 出现 6+4 */
.card-grid {
  flex: 1;
  min-height: 0;
  height: 100%;
  overflow: hidden;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  grid-template-rows: repeat(2, minmax(0, 1fr));
  gap: 12px;
  align-content: stretch;
}
.plan-card {
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  background: var(--bg-card);
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
  overflow: hidden;
}
.plan-card:hover {
  background: var(--bg-hover);
  border-color: var(--border-color);
}
.plan-card--pad {
  cursor: default;
  pointer-events: none;
  visibility: hidden;
}
.card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
  flex-shrink: 0;
}
.card-tags {
  margin-top: 4px;
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2px;
}
.tag-more {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-left: 2px;
}
.card-source-line {
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 0;
}
.card-progress { margin-top: 12px; flex-shrink: 0; }
.card-dates {
  margin-top: 10px;
  font-size: 12px;
  color: var(--text-tertiary);
  flex-shrink: 0;
}
.card-remark {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-height: 0;
}
.card-remark--empty { min-height: 1.2em; }
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: auto;
  padding-top: 10px;
  flex-shrink: 0;
}
.card-updated {
  font-size: 12px;
  color: var(--text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-actions { display: flex; gap: 2px; flex-shrink: 0; }

/* 窄屏才降列数；桌面始终保持 5×2 */
@media (max-width: 1100px) {
  .card-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    grid-template-rows: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    grid-template-rows: repeat(5, minmax(0, 1fr));
  }
}
</style>

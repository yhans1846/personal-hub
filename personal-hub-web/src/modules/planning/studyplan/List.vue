<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { PageHeader, EmptyState, ListPagination, ListToolbar } from '@/components'
import {
  getStudyPlanList, getStudyPlanStats, deleteStudyPlan,
  createStudyPlan, updateStudyPlan, exportStudyPlans,
} from '@/modules/planning/api'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Target, Plus, MoreHorizontal, ExternalLink, LayoutList, LayoutGrid, Download,
} from 'lucide-vue-next'
import StudyPlanDialog from './StudyPlanDialog.vue'
import StudyPlanTable from './StudyPlanTable.vue'
import type { StudyPlanVO, StudyPlanQuery, StudyPlanStats } from '@/types/studyplan'
import type { TagVO } from '@/types/tag'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'
import { useProductViewMode } from '@/composables/useProductViewMode'
import { useEntityDialogHost, usePaginatedList, type PageQuery } from '@/composables/usePaginatedList'
import { formatDateDot, formatRelativeUpdated } from '@/utils/formatTime'
import { handleApiError, unwrapPage, unwrapResult } from '@/utils/apiResult'
import { triggerBlobDownload } from '@/utils/file'

type StudyPlanListQuery = StudyPlanQuery & PageQuery
const tags = ref<TagVO[]>([])
const stats = ref<StudyPlanStats>({ total: 0, pending: 0, learning: 0, done: 0, paused: 0 })
const { viewMode, setViewMode } = useProductViewMode('study-plan-view', 'table')

const { list, total, loading, query, fetchList, onSearch, onPageChange } = usePaginatedList<StudyPlanVO, StudyPlanListQuery>({
  initialQuery: {
    page: 1,
    size: 10,
    keyword: '',
    sortBy: 'updatedAt',
    sortDir: 'desc',
  },
  fetchPage: (q) => unwrapPage(getStudyPlanList(q)),
  errorMessage: '加载学习计划失败',
})

const { dialogVisible: drawerVisible, editId, openCreate, openEdit } = useEntityDialogHost()

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
    tags.value = await unwrapResult(getTags())
  } catch { /* ignore */ }
}

async function fetchStats() {
  try {
    stats.value = await unwrapResult(getStudyPlanStats())
  } catch { /* ignore */ }
}

function refreshAll() {
  fetchStats()
  fetchList()
}

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
          tagId: query.value.tagId,
          sortBy: query.value.sortBy,
          sortDir: query.value.sortDir,
        }
      : undefined
    const res = await exportStudyPlans(scope, filters)
    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    triggerBlobDownload(blob, '学习计划.xlsx')
    ElMessage.success(scope === 'all' ? '已导出全部计划' : '已导出当前筛选结果')
  } catch (e) {
    handleApiError(e, '导出失败')
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

function formatDateRange(start: string | null, end: string | null) {
  return `${formatDateDot(start)} ~ ${formatDateDot(end)}`
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
  try {
    await deleteStudyPlan(id)
    ElMessage.success('已删除')
    refreshAll()
  } catch (e) {
    handleApiError(e, '删除失败')
  }
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

      <ListToolbar
        :search="query.keyword ?? ''"
        search-placeholder="搜索计划..."
        search-width="200px"
        create-label="新建计划"
        @update:search="query.keyword = $event"
        @search="onSearch"
        @create="openCreate"
      >
        <template #filters>
          <el-select v-model="query.tagId" placeholder="分类" clearable style="width:120px" @change="onFilterChange">
            <el-option v-for="t in tags" :key="t.id" :value="t.id" :label="t.name" />
          </el-select>
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
            <button type="button" class="toolbar-btn" :disabled="exporting">
              <Download :size="14" /> 导出
            </button>
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
        :icon="Target"
        illustration="plan"
        text="还没有学习计划，创建一个吧"
        action-label="新建计划"
        :action-icon="Plus"
        @action="openCreate"
      />

      <StudyPlanTable
        v-else-if="viewMode === 'table'"
        :list="list"
        :page-size="pageSize"
        :status-meta="statusMeta"
        :soft-tag-style="softTagStyle"
        :source-display="sourceDisplay"
        :on-row-action="onRowAction"
        :open-edit="openEdit"
      />

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
/* 共享 .product-table / .card-grid / 单元格见 styles/product-list.css */
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
  margin-bottom: var(--sp-3);
}
.plan-middle {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.plan-foot {
  flex-shrink: 0;
  padding-top: var(--sp-2);
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
  border-radius: var(--radius-md);
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

.soft-tag {
  display: inline-flex;
  align-items: center;
  padding: 1px 7px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  border: 1px solid transparent;
  margin-right: 4px;
}

.icon-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  text-decoration: none;
}
.icon-action:hover {
  background: var(--bg-card);
  color: var(--text-primary);
}

/* 本页 card-grid 铺满补充（共享壳见 product-list.css） */
.card-grid {
  height: 100%;
  overflow: hidden;
  align-content: stretch;
}
.plan-card {
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: var(--sp-3) var(--sp-4);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  background: var(--bg-card);
  cursor: pointer;
  transition: background var(--transition-duration) ease, border-color var(--transition-duration) ease;
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
</style>

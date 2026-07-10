<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStudyPlanList, deleteStudyPlan } from '@/api/studyplanApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Pencil, Trash2, Calendar, Target, BookOpen } from 'lucide-vue-next'
import type { StudyPlanVO, StudyPlanQuery } from '@/types/studyplan'

const router = useRouter()
const list = ref<StudyPlanVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<StudyPlanQuery>({ page: 1, size: 20, keyword: '' })

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getStudyPlanList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally { loading.value = false }
}

function onSearch() { query.value.page = 1; fetchList() }
function onFilterChange() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }
function goCreate() { router.push('/study-plans/new') }
function goEdit(id: number) { router.push(`/study-plans/${id}/edit`) }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该计划？', '提示', { type: 'warning' })
  await deleteStudyPlan(id)
  ElMessage.success('已删除')
  fetchList()
}

function getStatusType(status: number) {
  if (status === 0) return 'info'
  if (status === 1) return 'primary'
  if (status === 2) return 'success'
  return 'danger'
}

const statusOptions = [
  { value: undefined, label: '全部状态' },
  { value: 0, label: '未开始' },
  { value: 1, label: '进行中' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已放弃' }
]
</script>

<template>
  <div>
    <div class="page-header">
      <h2>学习计划</h2>
      <p>共 {{ total }} 个计划</p>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索计划..." style="width:200px" clearable @clear="onSearch" @keyup.enter="onSearch">
          <template #prefix><Search :size="14" style="color: var(--text-tertiary)" /></template>
        </el-input>
        <el-select v-model="query.status" placeholder="状态" style="width:130px" clearable @change="onFilterChange">
          <el-option v-for="item in statusOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
      </div>
      <el-button type="primary" @click="goCreate">
        <Plus :size="14" /> 新建计划
      </el-button>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 3" :key="i" class="skeleton-plan" />
    </div>

    <div v-else-if="list.length === 0" class="empty-state">
      <div class="empty-state__icon"><Target :size="48" /></div>
      <div class="empty-state__text">还没有学习计划，创建一个吧</div>
      <el-button type="primary" @click="goCreate">新建计划</el-button>
    </div>

    <div v-else class="plan-list">
      <div v-for="plan in list" :key="plan.id" class="plan-card" @click="goEdit(plan.id)">
        <div class="plan-header">
          <div class="plan-name">{{ plan.name }}</div>
          <el-tag :type="getStatusType(plan.status)" size="small">{{ plan.statusLabel }}</el-tag>
        </div>
        <div v-if="plan.goal" class="plan-goal">{{ plan.goal }}</div>
        <div class="plan-progress">
          <el-progress :percentage="plan.progress" :stroke-width="8" :color="plan.progress >= 100 ? 'var(--success)' : 'var(--accent)'" />
        </div>
        <div class="plan-footer">
          <span v-if="plan.startDate || plan.endDate" class="plan-date">
            <Calendar :size="12" />
            {{ plan.startDate || '?' }} ~ {{ plan.endDate || '?' }}
          </span>
          <span class="plan-records">
            <BookOpen :size="12" /> {{ plan.recordCount }} 条记录
          </span>
        </div>
        <div class="plan-actions" @click.stop>
          <button class="icon-btn" @click.stop="goEdit(plan.id)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(plan.id)"><Trash2 :size="14" /></button>
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
.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-3); }
.skeleton-plan { height: 120px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.plan-list { display: flex; flex-direction: column; gap: var(--sp-3); }
.plan-card {
  padding: var(--sp-5); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  cursor: pointer; transition: all var(--transition); position: relative;
}
.plan-card:hover { box-shadow: var(--shadow-sm); border-color: var(--accent-border); }

.plan-header { display: flex; align-items: center; gap: var(--sp-2); margin-bottom: var(--sp-2); }
.plan-name { font-size: var(--text-base); font-weight: 600; flex: 1; }
.plan-goal { font-size: var(--text-sm); color: var(--text-secondary); margin-bottom: var(--sp-3); line-height: var(--leading-normal); }
.plan-progress { margin-bottom: var(--sp-3); }
.plan-footer { display: flex; align-items: center; gap: var(--sp-4); font-size: var(--text-xs); color: var(--text-tertiary); }
.plan-date, .plan-records { display: flex; align-items: center; gap: 3px; }

.plan-actions { position: absolute; top: var(--sp-3); right: var(--sp-3); display: flex; gap: var(--sp-1); opacity: 0; transition: opacity var(--transition); }
.plan-card:hover .plan-actions { opacity: 1; }

.icon-btn { background: none; border: none; cursor: pointer; padding: 6px; border-radius: var(--radius-sm); color: var(--text-tertiary); transition: all var(--transition); display: flex; }
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>

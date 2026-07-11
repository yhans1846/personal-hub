<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getStudyRecordList, deleteStudyRecord, getStudyStats } from '@/api/studyApi'
import type { StudyStats } from '@/api/studyApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BookOpen, Pencil, Trash2, Plus } from 'lucide-vue-next'
import type { StudyRecordVO, StudyRecordQuery } from '@/types/study'
import { PageHeader, EmptyState, ListToolbar, ListPagination } from '@/components'
import StudyDrawer from './StudyDrawer.vue'

const list = ref<StudyRecordVO[]>([])
const total = ref(0)
const loading = ref(false)
const stats = ref<StudyStats | null>(null)
const query = ref<StudyRecordQuery>({ page: 1, size: 20, keyword: '' })

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

onMounted(() => { fetchList(); fetchStats() })
async function fetchStats() {
  try {
    const res = await getStudyStats()
    stats.value = res.data.data
  } catch { /* ignore */ }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getStudyRecordList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onSearch() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }
function goCreate() { openCreate() }
function goEdit(id: number) { openEdit(id) }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该记录？', '提示', { type: 'warning' })
  await deleteStudyRecord(id)
  ElMessage.success('已删除')
  fetchList()
}

function formatHours(min: number) {
  if (min < 60) return `${min} 分钟`
  const h = Math.floor(min / 60)
  const m = min % 60
  return m > 0 ? `${h} 小时 ${m} 分钟` : `${h} 小时`
}

function groupByDate(items: StudyRecordVO[]) {
  const groups: Record<string, StudyRecordVO[]> = {}
  items.forEach(item => {
    const key = item.date || '未知日期'
    if (!groups[key]) groups[key] = []
    groups[key].push(item)
  })
  return groups
}

const groupedList = ref<Record<string, StudyRecordVO[]>>({})
watch(list, (val) => { groupedList.value = groupByDate(val) }, { immediate: true })
</script>

<template>
  <div>
    <PageHeader title="学习记录" :subtitle="`共 ${total} 条记录`" />

    <!-- 统计条 -->
    <div v-if="stats" class="study-stats">
      <div class="stat-item"><span class="stat-num">{{ stats.todayDuration }}</span> 分钟 <span class="stat-label">今日学习</span></div>
      <div class="stat-divider" />
      <div class="stat-item"><span class="stat-num">{{ stats.weekDuration }}</span> 分钟 <span class="stat-label">本周</span></div>
      <div class="stat-divider" />
      <div class="stat-item"><span class="stat-num">{{ stats.streak }}</span> 天 <span class="stat-label">连续学习</span></div>
    </div>

    <ListToolbar :search="query.keyword" search-placeholder="搜索学习记录..." search-width="240px" create-label="新建记录" @update:search="query.keyword = $event" @search="onSearch" @create="goCreate" />

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 5" :key="i" class="skeleton-study" />
    </div>

    <EmptyState v-else-if="list.length === 0" :icon="BookOpen" illustration="study" text="还没有学习记录，开始记录今天的学习吧" action-label="开始记录" @action="goCreate" />

    <!-- 时间线 -->
    <div v-else class="timeline">
      <div v-for="(records, date) in groupedList" :key="date" class="timeline-group">
        <div class="timeline-date">
          <span class="timeline-date-badge">{{ date }}</span>
          <span class="timeline-date-total">{{ records.reduce((s, r) => s + r.duration, 0) }} 分钟</span>
        </div>
        <div class="timeline-items">
          <div v-for="r in records" :key="r.id" class="tl-item" @click="goEdit(r.id)">
            <div class="tl-item-dot" />
            <div class="tl-item-content">
              <div class="tl-item-header">
                <span class="tl-item-subject">{{ r.subject }}</span>
                <span class="tl-item-duration">{{ formatHours(r.duration) }}</span>
              </div>
              <p v-if="r.content" class="tl-item-desc">{{ r.content.slice(0, 100) }}</p>
              <p v-if="r.reflection" class="tl-item-reflection">💡 {{ r.reflection.slice(0, 80) }}</p>
            </div>
            <div class="tl-item-actions" @click.stop>
              <button class="icon-btn" @click="goEdit(r.id)"><Pencil :size="14" /></button>
              <button class="icon-btn icon-btn--danger" @click="handleDelete(r.id)"><Trash2 :size="14" /></button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <ListPagination v-if="total > query.size" :total="total" :page="query.page" :size="query.size" @update:page="onPageChange" />

    <StudyDrawer v-model="drawerVisible" :entity-id="editId" @saved="fetchList" />
  </div>
</template>

<style scoped>
.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-3); }
.skeleton-study { height: 80px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.timeline { display: flex; flex-direction: column; gap: var(--sp-8); }
.timeline-date { display: flex; align-items: center; gap: var(--sp-3); margin-bottom: var(--sp-4); padding-left: 12px; }
.timeline-date-badge { font-size: var(--text-sm); font-weight: 600; }
.timeline-date-total { font-size: var(--text-xs); color: var(--text-tertiary); }
.timeline-items {
  display: flex; flex-direction: column; gap: var(--sp-2);
  position: relative; padding-left: 20px; border-left: 2px solid var(--border-color); margin-left: 8px;
}
.tl-item {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: var(--sp-3) var(--sp-4); border-radius: var(--radius-md);
  cursor: pointer; transition: background var(--transition); position: relative;
}
.tl-item:hover { background: var(--bg-hover); }
.tl-item-dot {
  position: absolute; left: -25px; top: 18px;
  width: 10px; height: 10px; border-radius: 50%;
  background: var(--accent); border: 2px solid var(--bg-body); flex-shrink: 0;
}
.tl-item-content { flex: 1; min-width: 0; }
.tl-item-header { display: flex; align-items: center; gap: var(--sp-2); margin-bottom: 4px; }
.tl-item-subject { font-size: var(--text-sm); font-weight: 600; }
.tl-item-duration { font-size: var(--text-xs); color: var(--accent); background: var(--accent-light); padding: 0 8px; border-radius: 4px; height: 20px; line-height: 20px; }
.tl-item-desc { font-size: var(--text-sm); color: var(--text-secondary); line-height: var(--leading-normal); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 2px; }
.tl-item-reflection { font-size: var(--text-xs); color: var(--text-tertiary); font-style: italic; }
.tl-item-actions { display: flex; gap: var(--sp-1); flex-shrink: 0; opacity: 0; transition: opacity var(--transition); align-items: center; }
.tl-item:hover .tl-item-actions { opacity: 1; }

.icon-btn {
  background: none; border: none; cursor: pointer;
  padding: 6px; border-radius: var(--radius-sm);
  color: var(--text-tertiary); transition: all var(--transition);
  display: flex; align-items: center;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }

.study-stats { display: flex; align-items: center; gap: var(--sp-4); padding: var(--sp-3) var(--sp-4); background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); margin-bottom: var(--sp-5); }
.stat-item { font-size: var(--text-xs); color: var(--text-secondary); }
.stat-num { font-size: var(--text-lg); font-weight: 700; color: var(--accent); }
.stat-label { color: var(--text-tertiary); }
.stat-divider { width: 1px; height: 28px; background: var(--border-color); }
</style>

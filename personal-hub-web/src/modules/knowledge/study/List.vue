<script setup lang="ts">
import { ref, watch } from 'vue'
import { getStudyRecordList, deleteStudyRecord, getStudyStats } from '@/modules/knowledge/api'
import type { StudyStats } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BookOpen, Pencil, Trash2 } from 'lucide-vue-next'
import type { StudyRecordVO, StudyRecordQuery } from '@/types/study'
import { PageHeader, EmptyState, ListToolbar, ListPagination } from '@/components'
import StudyDialog from './StudyDialog.vue'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'
import { useEntityDialogHost, usePaginatedList, type PageQuery } from '@/composables/usePaginatedList'
import { formatDuration } from '@/utils/formatTime'
import { handleApiError, unwrapPage, unwrapResult } from '@/utils/apiResult'

const stats = ref<StudyStats | null>(null)
type StudyListQuery = StudyRecordQuery & PageQuery

const { list, total, loading, query, fetchList, onSearch, onPageChange } = usePaginatedList<StudyRecordVO, StudyListQuery>({
  initialQuery: { page: 1, size: 10, keyword: '' },
  fetchPage: (q) => unwrapPage(getStudyRecordList(q)),
  errorMessage: '加载学习记录失败',
})

const { dialogVisible: drawerVisible, editId, openCreate, openEdit } = useEntityDialogHost()

useMainContentFill()
const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})

useDeepLinkDialog({ openCreate, openEdit })

async function fetchStats() {
  try {
    stats.value = await unwrapResult(getStudyStats())
  } catch { /* ignore */ }
}

fetchStats()

function goCreate() { openCreate() }
function goEdit(id: number) { openEdit(id) }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该记录？', '提示', { type: 'warning' })
  try {
    await deleteStudyRecord(id)
    ElMessage.success('已删除')
    fetchList()
  } catch (e) {
    handleApiError(e, '删除失败')
  }
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
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="学习记录" :subtitle="`共 ${total} 条记录`" />

      <div v-if="stats" class="study-stats">
        <div class="stat-item"><span class="stat-num">{{ stats.todayDuration }}</span> 分钟 <span class="stat-label">今日学习</span></div>
        <div class="stat-divider" />
        <div class="stat-item"><span class="stat-num">{{ stats.weekDuration }}</span> 分钟 <span class="stat-label">本周</span></div>
        <div class="stat-divider" />
        <div class="stat-item"><span class="stat-num">{{ stats.streak }}</span> 天 <span class="stat-label">连续学习</span></div>
      </div>

      <ListToolbar :search="query.keyword ?? ''" search-placeholder="搜索学习记录..." search-width="240px" create-label="新建记录" @update:search="query.keyword = $event" @search="onSearch" @create="goCreate" />
    </div>

    <div class="plan-middle">
      <div v-if="loading" class="loading-skeleton">
        <div v-for="i in pageSize" :key="i" class="skeleton-study" />
      </div>

      <EmptyState v-else-if="list.length === 0" :icon="BookOpen" illustration="study" text="还没有学习记录，开始记录今天的学习吧" action-label="开始记录" @action="goCreate" />

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
                  <span class="tl-item-duration">{{ formatDuration(r.duration) }}</span>
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
    </div>

    <div class="plan-foot">
      <ListPagination v-if="total > 0" :total="total" :page="query.page" :size="pageSize" @update:page="onPageChange" />
    </div>

    <StudyDialog v-model="drawerVisible" :entity-id="editId" @saved="fetchList" />
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

.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-3); flex: 1; }
.skeleton-study { flex: 1; min-height: 48px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.timeline {
  flex: 1;
  min-height: 0;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: var(--sp-6);
}
.timeline-date { display: flex; align-items: center; gap: var(--sp-3); margin-bottom: var(--sp-3); padding-left: 12px; }
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
.tl-item-duration { font-size: var(--text-xs); color: var(--accent); background: var(--accent-light); padding: 0 8px; border-radius: var(--radius-sm); height: 20px; line-height: 20px; }
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

.study-stats { display: flex; align-items: center; gap: var(--sp-4); padding: var(--sp-3) var(--sp-4); background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); margin-bottom: var(--sp-3); }
.stat-item { font-size: var(--text-xs); color: var(--text-secondary); }
.stat-num { font-size: var(--text-lg); font-weight: 700; color: var(--accent); }
.stat-label { color: var(--text-tertiary); }
.stat-divider { width: 1px; height: 28px; background: var(--border-color); }
</style>

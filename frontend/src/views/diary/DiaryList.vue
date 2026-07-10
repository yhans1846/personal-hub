<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDiaryList, deleteDiary } from '@/api/diaryApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Pencil, Trash2, PenLine, Sun, Cloud, CloudRain, Smile, Frown, Meh, MapPin } from 'lucide-vue-next'
import { EmptyState, PageHeader } from '@/components'
import type { DiaryVO, DiaryQuery } from '@/types/diary'

const router = useRouter()
const list = ref<DiaryVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<DiaryQuery>({ page: 1, size: 20, keyword: '' })

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getDiaryList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onSearch() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }
function onFilterChange() { query.value.page = 1; fetchList() }
function goCreate() { router.push('/diaries/new') }
function goEdit(id: number) { router.push(`/diaries/${id}/edit`) }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除这篇日记？', '提示', { type: 'warning' })
  await deleteDiary(id)
  ElMessage.success('已删除')
  fetchList()
}

function getMoodIcon(mood: number) {
  if (mood <= 2) return Smile
  if (mood === 3) return Meh
  return Frown
}

function getMoodColor(mood: number) {
  if (mood === 1) return 'var(--success)'
  if (mood === 2) return 'var(--accent)'
  if (mood === 3) return 'var(--text-secondary)'
  return 'var(--danger)'
}

function getWeatherIcon(weather: string | null) {
  if (!weather) return null
  const icons: Record<string, any> = { '晴': Sun, '多云': Cloud, '阴': Cloud, '雨': CloudRain, '雪': CloudRain }
  return icons[weather] || null
}

const moodOptions = [
  { value: '', label: '全部心情' },
  { value: 1, label: '很好' },
  { value: 2, label: '好' },
  { value: 3, label: '一般' },
  { value: 4, label: '不好' },
  { value: 5, label: '很差' }
]
</script>

<template>
  <div>
    <PageHeader title="日记" subtitle="记录每天的生活" />

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索日记..." style="width:200px" clearable @clear="onSearch" @keyup.enter="onSearch">
          <template #prefix><Search :size="14" style="color: var(--text-tertiary)" /></template>
        </el-input>
        <el-select v-model="query.mood" placeholder="心情" style="width:120px" clearable @change="onFilterChange">
          <el-option v-for="item in moodOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
        <el-date-picker
          v-model="query.month"
          type="month"
          value-format="YYYY-MM"
          placeholder="按月筛选"
          style="width:140px"
          clearable
          @change="onFilterChange"
        />
      </div>
      <el-button type="primary" @click="goCreate">
        <Plus :size="14" /> 写日记
      </el-button>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 5" :key="i" class="skeleton-diary" />
    </div>

    <EmptyState v-else-if="list.length === 0" :icon="PenLine" illustration="diary" text="还没有日记，开始记录吧" action-label="写日记" :action-icon="Plus" @action="goCreate" />

    <div v-else class="diary-list">
      <div v-for="entry in list" :key="entry.id" class="diary-item" @click="goEdit(entry.id)">
        <div class="diary-date">
          <div class="diary-date-day">{{ entry.date.slice(8) }}</div>
          <div class="diary-date-month">{{ entry.date.slice(0, 7) }}</div>
        </div>
        <div class="diary-body">
          <div class="diary-header">
            <span class="diary-title">{{ entry.title || '无题' }}</span>
            <div class="diary-meta">
              <span v-if="entry.mood" class="diary-mood" :style="{ color: getMoodColor(entry.mood) }">
                <component :is="getMoodIcon(entry.mood)" :size="14" />
                {{ entry.moodLabel }}
              </span>
              <span v-if="entry.weather" class="diary-weather">
                <component :is="getWeatherIcon(entry.weather)" v-if="getWeatherIcon(entry.weather)" :size="14" />
                {{ entry.weather }}
              </span>
              <span v-if="entry.location" class="diary-location"><MapPin :size="12" /> {{ entry.location }}</span>
            </div>
          </div>
          <div v-if="entry.contentSummary" class="diary-summary">{{ entry.contentSummary }}</div>
        </div>
        <div class="diary-actions" @click.stop>
          <button class="icon-btn" @click.stop="goEdit(entry.id)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(entry.id)"><Trash2 :size="14" /></button>
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
.skeleton-diary { height: 80px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.diary-list { display: flex; flex-direction: column; gap: var(--sp-2); }
.diary-item {
  display: flex; align-items: flex-start; gap: var(--sp-4);
  padding: var(--sp-4) var(--sp-5); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  cursor: pointer; transition: all var(--transition);
}
.diary-item:hover { box-shadow: var(--shadow-sm); border-color: var(--accent-border); }

.diary-date { text-align: center; flex-shrink: 0; width: 48px; padding-top: 2px; }
.diary-date-day { font-size: 24px; font-weight: 700; line-height: 1; color: var(--text-primary); }
.diary-date-month { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 2px; }

.diary-body { flex: 1; min-width: 0; }
.diary-header { display: flex; align-items: center; gap: var(--sp-3); margin-bottom: 4px; }
.diary-title { font-size: var(--text-sm); font-weight: 500; }
.diary-meta { display: flex; align-items: center; gap: var(--sp-2); margin-left: auto; font-size: var(--text-xs); }
.diary-mood, .diary-weather, .diary-location { display: flex; align-items: center; gap: 2px; white-space: nowrap; }
.diary-location { color: var(--text-tertiary); }
.diary-summary {
  font-size: var(--text-sm); color: var(--text-secondary);
  line-height: var(--leading-normal); overflow: hidden;
  display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical;
}

.diary-actions { display: flex; gap: var(--sp-1); flex-shrink: 0; opacity: 0; transition: opacity var(--transition); align-items: center; }
.diary-item:hover .diary-actions { opacity: 1; }

.icon-btn { background: none; border: none; cursor: pointer; padding: 6px; border-radius: var(--radius-sm); color: var(--text-tertiary); transition: all var(--transition); display: flex; align-items: center; }
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>

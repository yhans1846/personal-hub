<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getDiaryList, getDiaryByMonth, deleteDiary } from '@/api/diaryApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Pencil, Trash2, PenLine, Sun, Cloud, CloudRain, Smile, Frown, Meh, MapPin, ImageIcon, CalendarDays } from 'lucide-vue-next'
import { EmptyState, PageHeader, ListToolbar, ListPagination } from '@/components'
import DiaryDialog from './DiaryDialog.vue'
import type { DiaryVO, DiaryQuery } from '@/types/diary'

const list = ref<DiaryVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<DiaryQuery>({ page: 1, size: 20, keyword: '' })

const dialogVisible = ref(false)
const editId = ref<number | undefined>()
const initialDate = ref('')

function openCreate(date?: string) {
  editId.value = undefined
  initialDate.value = date || ''
  dialogVisible.value = true
}

function openEdit(id: number) {
  editId.value = id
  initialDate.value = ''
  dialogVisible.value = true
}
const showCalendar = ref(false)
const calendarMonth = ref(new Date())
const calendarEntries = ref<DiaryVO[]>([])
const calendarLoading = ref(false)

const WEEKDAYS = ['日', '一', '二', '三', '四', '五', '六']

const calendarDays = computed(() => {
  const year = calendarMonth.value.getFullYear()
  const month = calendarMonth.value.getMonth()
  const firstDay = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const days: { date: number; entries: DiaryVO[] }[] = []

  for (let i = 0; i < firstDay; i++) days.push({ date: 0, entries: [] })
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const entries = calendarEntries.value.filter(e => e.date === dateStr)
    days.push({ date: d, entries })
  }
  return days
})

async function loadCalendar() {
  calendarLoading.value = true
  try {
    const monthStr = `${calendarMonth.value.getFullYear()}-${String(calendarMonth.value.getMonth() + 1).padStart(2, '0')}`
    const res = await getDiaryByMonth(monthStr)
    calendarEntries.value = res.data.data || []
  } catch { calendarEntries.value = [] }
  finally { calendarLoading.value = false }
}

function toggleCalendar() {
  showCalendar.value = !showCalendar.value
  if (showCalendar.value) loadCalendar()
}

function prevMonth() { calendarMonth.value = new Date(calendarMonth.value.getFullYear(), calendarMonth.value.getMonth() - 1); loadCalendar() }
function nextMonth() { calendarMonth.value = new Date(calendarMonth.value.getFullYear(), calendarMonth.value.getMonth() + 1); loadCalendar() }

function goDate(day: number) {
  const d = `${calendarMonth.value.getFullYear()}-${String(calendarMonth.value.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  const existing = calendarEntries.value.find(e => e.date === d)
  if (existing) openEdit(existing.id)
  else openCreate(d)
}

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
function goCreate() { openCreate() }
function goEdit(id: number) { openEdit(id) }

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

    <ListToolbar :search="query.keyword" search-placeholder="搜索日记..." search-width="200px" create-label="写日记" @update:search="query.keyword = $event" @search="onSearch" @create="goCreate">
      <template #filters>
        <el-select v-model="query.mood" placeholder="心情" style="width:120px" clearable @change="onFilterChange">
          <el-option v-for="item in moodOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
        <el-date-picker v-model="query.month" type="month" value-format="YYYY-MM" placeholder="按月筛选" style="width:140px" clearable @change="onFilterChange" />
        <el-button :type="showCalendar ? 'default' : 'primary'" @click="toggleCalendar">
          <CalendarDays :size="14" /> {{ showCalendar ? '列表' : '月历' }}
        </el-button>
      </template>
    </ListToolbar>

    <!-- 日历视图 -->
    <div v-if="showCalendar" class="calendar-view">
      <div class="cal-header">
        <button class="cal-nav" @click="prevMonth">&lt;</button>
        <span class="cal-title">{{ calendarMonth.getFullYear() }} 年 {{ calendarMonth.getMonth() + 1 }} 月</span>
        <button class="cal-nav" @click="nextMonth">&gt;</button>
      </div>
      <div v-if="calendarLoading" class="loading-skeleton" style="padding: 16px 0;">
        <div v-for="i in 3" :key="i" class="skeleton-diary" style="height:60px" />
      </div>
      <div v-else class="cal-grid">
        <div v-for="wd in WEEKDAYS" :key="wd" class="cal-weekday">{{ wd }}</div>
        <div v-for="(day, idx) in calendarDays" :key="idx" class="cal-day" :class="{ 'cal-day--empty': day.date === 0, 'cal-day--has': day.entries.length > 0 }" @click="day.date > 0 && goDate(day.date)">
          <span class="cal-day-num">{{ day.date || '' }}</span>
          <div v-if="day.entries.length > 0" class="cal-day-dots">
            <span v-for="e in day.entries.slice(0, 3)" :key="e.id" class="cal-dot" :style="{ background: e.mood && e.mood <= 2 ? 'var(--accent)' : e.mood === 3 ? 'var(--text-tertiary)' : 'var(--warning)' }" />
          </div>
        </div>
      </div>
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
          <img v-if="entry.imageFileId" :src="`/api/files/${entry.imageFileId}/download`" class="diary-image" />
        </div>
        <div class="diary-actions" @click.stop>
          <button class="icon-btn" @click.stop="goEdit(entry.id)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(entry.id)"><Trash2 :size="14" /></button>
        </div>
      </div>
    </div>

    <ListPagination v-if="total > (query.size ?? 20)" :total="total" :page="query.page" :size="query.size" @update:page="onPageChange" />

    <DiaryDialog v-model="dialogVisible" :entity-id="editId" :initial-date="initialDate" @saved="fetchList" />
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
.diary-image { max-width: 80px; max-height: 60px; border-radius: var(--radius-sm); object-fit: cover; margin-top: 4px; }
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

.calendar-view { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: var(--sp-4); margin-bottom: var(--sp-5); }
.cal-header { display: flex; align-items: center; justify-content: center; gap: var(--sp-4); margin-bottom: var(--sp-4); }
.cal-nav { background: none; border: 1px solid var(--border-color); border-radius: var(--radius-sm); cursor: pointer; padding: 4px 12px; color: var(--text-secondary); font-size: var(--text-sm); transition: all var(--transition); }
.cal-nav:hover { background: var(--bg-hover); color: var(--text-primary); }
.cal-title { font-size: var(--text-base); font-weight: 600; min-width: 140px; text-align: center; }
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 2px; }
.cal-weekday { text-align: center; font-size: var(--text-xs); color: var(--text-tertiary); padding: 4px 0; font-weight: 500; }
.cal-day { aspect-ratio: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; border-radius: var(--radius-sm); cursor: pointer; transition: all var(--transition); position: relative; min-height: 48px; }
.cal-day:hover { background: var(--bg-hover); }
.cal-day--empty { cursor: default; }
.cal-day--empty:hover { background: transparent; }
.cal-day--has { font-weight: 600; }
.cal-day-num { font-size: var(--text-sm); line-height: 1; }
.cal-day-dots { display: flex; gap: 2px; margin-top: 4px; }
.cal-dot { width: 5px; height: 5px; border-radius: 50%; }
</style>

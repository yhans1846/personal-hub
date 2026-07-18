<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { getDiaryList, getDiaryByMonth, deleteDiary } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Pencil, Trash2, PenLine, Sun, Cloud, CloudRain, Smile, Frown, Meh, MapPin, Images, CalendarDays, LayoutList, LayoutGrid, Camera, Calendar, MoreHorizontal, Type } from 'lucide-vue-next'
import { EmptyState, PageHeader, ListToolbar, ListPagination } from '@/components'
import ImageLightbox from '@/components/ImageLightbox.vue'
import { getFilePreviewUrl, revokePreviewUrl } from '@/utils/file'
import DiaryDialog from './DiaryDialog.vue'
import type { DiaryVO, DiaryQuery } from '@/types/diary'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'
import { useProductViewMode } from '@/composables/useProductViewMode'

const list = ref<DiaryVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<DiaryQuery>({ page: 1, size: 10, keyword: '' })

// ---- 视图模式（localStorage 持久化） ----
const { viewMode, setViewMode } = useProductViewMode('diary-view', 'list')
const cardImages = ref<Map<number, string[]>>(new Map())
const lightboxOpen = ref(false)
const lightboxUrls = ref<string[]>([])
const lightboxIndex = ref(0)

function openLightbox(urls: string[], index: number) {
  if (!urls.length) return
  lightboxUrls.value = urls
  lightboxIndex.value = index
  lightboxOpen.value = true
}

async function loadCardImages(entries: DiaryVO[]) {
  const map = new Map<number, string[]>()
  const promises: Promise<void>[] = []
  for (const entry of entries) {
    const ids = entry.imageFileIds?.slice(0, 4) || []
    if (!ids.length) continue
    promises.push(
      (async () => {
        const urls = await Promise.all(ids.map(id => getFilePreviewUrl(id)))
        map.set(entry.id, urls)
      })()
    )
  }
  await Promise.all(promises)
  // 释放旧 blob URL
  cardImages.value.forEach(urls => urls.forEach(u => revokePreviewUrl(u)))
  cardImages.value = map
}

function revokeCardImages() {
  cardImages.value.forEach(urls => urls.forEach(u => revokePreviewUrl(u)))
  cardImages.value = new Map()
}

watch(viewMode, () => {
  // 列表/卡片都需要缩略图，避免一屏铺满时徽章被裁切看不见
  loadCardImages(list.value)
})

onUnmounted(() => revokeCardImages())

useMainContentFill()
const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})

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

useDeepLinkDialog({ openCreate, openEdit })

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

async function fetchList() {
  loading.value = true
  try {
    const res = await getDiaryList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
    loadCardImages(list.value)
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
  if (mood === 3) return 'var(--warning)'
  if (mood === 4) return '#F97316'
  return 'var(--danger)'
}

function getWeatherIcon(weather: string | null) {
  if (!weather) return null
  const icons: Record<string, any> = {
    晴: Sun, '☀️': Sun,
    多云: Cloud, '🌤️': Cloud,
    阴: Cloud, '☁️': Cloud,
    雨: CloudRain, '🌧️': CloudRain,
    雷阵雨: CloudRain, '⛈️': CloudRain,
    雪: CloudRain, '❄️': CloudRain,
  }
  return icons[weather] || null
}

function getWeatherColor(weather: string | null) {
  if (!weather) return 'var(--text-secondary)'
  const colors: Record<string, string> = {
    晴: '#EAB308', '☀️': '#EAB308',
    多云: '#64748B', '🌤️': '#64748B',
    阴: '#94A3B8', '☁️': '#94A3B8',
    雨: '#3B82F6', '🌧️': '#3B82F6',
    雷阵雨: '#6366F1', '⛈️': '#6366F1',
    雪: '#38BDF8', '❄️': '#38BDF8',
  }
  return colors[weather] || 'var(--text-secondary)'
}

/** 字数：优先正文，否则摘要 */
function charCount(entry: DiaryVO): number {
  const raw = (entry.content || entry.contentSummary || '').replace(/\s+/g, '')
  return raw.length
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
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="日记" />

      <ListToolbar :search="query.keyword" search-placeholder="搜索日记..." search-width="200px" create-label="写日记" @update:search="query.keyword = $event" @search="onSearch" @create="goCreate">
        <template #filters>
          <el-select v-model="query.mood" placeholder="心情" style="width:120px" clearable @change="onFilterChange">
            <el-option v-for="item in moodOptions" :key="item.label" :value="item.value" :label="item.label" />
          </el-select>
          <el-date-picker v-model="query.month" type="month" value-format="YYYY-MM" placeholder="按月筛选" style="width:140px" clearable popper-class="ph-date-popper" @change="onFilterChange" />
          <el-button :type="showCalendar ? 'default' : 'primary'" @click="toggleCalendar">
            <CalendarDays :size="14" /> {{ showCalendar ? '列表' : '月历' }}
          </el-button>
          <div class="view-toggle">
            <button type="button" class="view-btn" :class="{ active: viewMode === 'list' }" title="列表" @click="setViewMode('list')">
              <LayoutList :size="15" />
            </button>
            <button type="button" class="view-btn" :class="{ active: viewMode === 'card' }" title="卡片" @click="setViewMode('card')">
              <LayoutGrid :size="15" />
            </button>
          </div>
        </template>
      </ListToolbar>
    </div>

    <div class="plan-middle">
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
              <span v-for="e in day.entries.slice(0, 3)" :key="e.id" class="cal-dot" :style="{ background: getMoodColor(e.mood || 3) }" />
            </div>
          </div>
        </div>
      </div>

      <template v-else>
        <div v-if="loading" class="loading-skeleton">
          <div v-for="i in pageSize" :key="i" class="skeleton-diary" />
        </div>

        <EmptyState v-else-if="list.length === 0" :icon="PenLine" illustration="diary" text="还没有日记，开始记录吧" action-label="写日记" :action-icon="Plus" @action="goCreate" />

        <!-- ---- 列表模式 ---- -->
        <div v-else-if="viewMode === 'list'" class="diary-list">
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
                  <span v-if="entry.weather" class="diary-weather" :style="{ color: getWeatherColor(entry.weather) }">
                    <component :is="getWeatherIcon(entry.weather)" v-if="getWeatherIcon(entry.weather)" :size="14" />
                    {{ entry.weather }}
                  </span>
                  <span v-if="entry.location" class="diary-location"><MapPin :size="12" /> {{ entry.location }}</span>
                </div>
              </div>
              <div v-if="entry.contentSummary" class="diary-summary">{{ entry.contentSummary }}</div>
            </div>
            <!-- 配图缩略图：固定尺寸，避免一屏铺满行高把徽章裁掉 -->
            <div v-if="entry.imageFileIds?.length" class="diary-thumbs" @click.stop>
              <template v-if="cardImages.get(entry.id)?.length">
                <img
                  v-for="(url, i) in cardImages.get(entry.id)!.slice(0, 3)"
                  :key="i"
                  :src="url"
                  class="diary-thumb"
                  alt=""
                  @click="openLightbox(cardImages.get(entry.id)!, i)"
                />
                <span v-if="entry.imageFileIds.length > 3" class="diary-thumb-more">
                  +{{ entry.imageFileIds.length - 3 }}
                </span>
              </template>
              <span v-else class="diary-thumbs-pending">
                <Images :size="14" />
                {{ entry.imageFileIds.length }}
              </span>
            </div>
            <div class="diary-actions" @click.stop>
              <button class="icon-btn" @click.stop="goEdit(entry.id)"><Pencil :size="14" /></button>
              <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(entry.id)"><Trash2 :size="14" /></button>
            </div>
          </div>
          <div
            v-for="n in Math.max(0, pageSize - list.length)"
            :key="'pad-' + n"
            class="diary-item diary-item--pad"
            aria-hidden="true"
          />
        </div>

        <!-- ---- 卡片模式：上封面 + 下信息（对齐线框） ---- -->
        <div v-else class="card-grid diary-card-grid">
          <div
            v-for="entry in list"
            :key="entry.id"
            class="diary-card"
            @click="goEdit(entry.id)"
          >
            <!-- 封面：仅有配图时占位，点击预览 -->
            <div v-if="entry.imageFileIds?.length" class="dc-cover" @click.stop>
              <img
                v-if="cardImages.get(entry.id)?.[0]"
                :src="cardImages.get(entry.id)![0]"
                class="dc-cover-img"
                alt=""
                @click="openLightbox(cardImages.get(entry.id)!, 0)"
              />
              <span v-if="entry.imageFileIds.length > 1" class="dc-cover-count">
                <Camera :size="12" />
                {{ entry.imageFileIds.length }}
              </span>
            </div>

            <!-- 正文信息 -->
            <div class="dc-body">
              <div class="dc-title">{{ entry.title || '无题' }}</div>
              <div v-if="entry.contentSummary" class="dc-summary">{{ entry.contentSummary }}</div>

              <div class="dc-meta">
                <span v-if="entry.mood" class="dc-chip" :style="{ color: getMoodColor(entry.mood) }">
                  <component :is="getMoodIcon(entry.mood)" :size="13" />
                  {{ entry.moodLabel }}
                </span>
                <span v-if="entry.location" class="dc-chip dc-chip--muted">
                  <MapPin :size="12" />
                  {{ entry.location }}
                </span>
                <span v-else-if="entry.weather" class="dc-chip" :style="{ color: getWeatherColor(entry.weather) }">
                  <component :is="getWeatherIcon(entry.weather)" v-if="getWeatherIcon(entry.weather)" :size="13" />
                  {{ entry.weather }}
                </span>
              </div>

              <div class="dc-stats">
                <span class="dc-chip dc-chip--muted">
                  <Type :size="12" />
                  {{ charCount(entry) }}字
                </span>
                <span v-if="entry.imageFileIds?.length" class="dc-chip dc-chip--muted">
                  <Camera :size="12" />
                  {{ entry.imageFileIds.length }}张
                </span>
              </div>

              <div class="dc-foot">
                <span class="dc-date">
                  <Calendar :size="12" />
                  {{ entry.date }}
                </span>
                <el-dropdown trigger="click" @click.stop>
                  <button type="button" class="dc-more" @click.stop>
                    <MoreHorizontal :size="16" />
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="goEdit(entry.id)">
                        <Pencil :size="14" /> 编辑
                      </el-dropdown-item>
                      <el-dropdown-item divided @click="handleDelete(entry.id)">
                        <Trash2 :size="14" /> 删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
          <div
            v-for="n in Math.max(0, pageSize - list.length)"
            :key="'card-pad-' + n"
            class="diary-card diary-card--pad"
            aria-hidden="true"
          />
        </div>
      </template>
    </div>

    <div v-if="!showCalendar" class="plan-foot">
      <ListPagination v-if="total > 0" :total="total" :page="query.page" :size="pageSize" @update:page="onPageChange" />
    </div>

    <DiaryDialog v-model="dialogVisible" :entity-id="editId" :initial-date="initialDate" @saved="fetchList" />
    <ImageLightbox
      v-model="lightboxOpen"
      v-model:index="lightboxIndex"
      :urls="lightboxUrls"
    />
  </div>
</template>

<style scoped>
@import '@/styles/product-list.css';

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

.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-3); flex: 1; min-height: 0; }
.skeleton-diary { flex: 1; min-height: 48px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

/* ---- 列表模式 ---- */
.diary-list {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
}
.diary-item {
  display: flex; align-items: center; gap: var(--sp-4);
  padding: var(--sp-3) var(--sp-5); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  cursor: pointer; transition: all var(--transition);
  flex: 1;
  min-height: 64px;
}
.diary-item--pad {
  visibility: hidden;
  pointer-events: none;
  border: none;
  background: transparent;
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

.diary-thumbs {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  align-self: center;
}
.diary-thumb {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background: var(--bg-hover);
  display: block;
  cursor: zoom-in;
}
.diary-thumb-more,
.diary-thumbs-pending {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  min-width: 28px;
  height: 44px;
  padding: 0 6px;
  border-radius: var(--radius-sm);
  background: var(--bg-hover);
  color: var(--text-tertiary);
  font-size: var(--text-xs);
  font-weight: 600;
}

.diary-actions { display: flex; gap: var(--sp-1); flex-shrink: 0; opacity: 0; transition: opacity var(--transition); align-items: center; align-self: center; }
.diary-item:hover .diary-actions { opacity: 1; }

.icon-btn { background: none; border: none; cursor: pointer; padding: 6px; border-radius: var(--radius-sm); color: var(--text-tertiary); transition: all var(--transition); display: flex; align-items: center; }
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }

/* ---- 卡片模式：上封面 + 下信息 ---- */
.diary-card-grid {
  height: 100%;
  overflow: hidden;
  align-content: stretch;
}
.diary-card {
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 0;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  background: var(--bg-card);
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
  overflow: hidden;
}
.diary-card:hover {
  background: var(--bg-hover);
  border-color: var(--border-color);
  box-shadow: var(--shadow-sm);
}
.diary-card--pad {
  cursor: default;
  pointer-events: none;
  visibility: hidden;
}

.dc-cover {
  position: relative;
  flex: 0 0 42%;
  min-height: 72px;
  background: var(--bg-hover);
  overflow: hidden;
}
.dc-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  cursor: zoom-in;
}
.dc-cover-count {
  position: absolute;
  right: 8px;
  bottom: 8px;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 7px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.dc-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px 8px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.dc-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 0;
}
.dc-summary {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex-shrink: 0;
}
.dc-meta,
.dc-stats {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  flex-shrink: 0;
}
.dc-chip {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  line-height: 1;
  white-space: nowrap;
}
.dc-chip--muted {
  color: var(--text-tertiary);
}
.dc-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 4px;
  flex-shrink: 0;
}
.dc-date {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-tertiary);
}
.dc-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}
.dc-more:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

/* ---- 日历 ---- */
.calendar-view {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--sp-4);
}
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

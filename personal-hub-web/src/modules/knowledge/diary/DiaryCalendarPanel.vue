<script setup lang="ts">
import { computed } from 'vue'
import type { DiaryVO } from '@/types/diary'

const props = defineProps<{
  month: Date
  entries: DiaryVO[]
  loading: boolean
  getMoodColor: (mood: number) => string
}>()

const emit = defineEmits<{
  prev: []
  next: []
  'select-day': [day: number]
}>()

const WEEKDAYS = ['日', '一', '二', '三', '四', '五', '六'] as const

const calendarDays = computed(() => {
  const year = props.month.getFullYear()
  const month = props.month.getMonth()
  const firstDay = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const days: { date: number; entries: DiaryVO[] }[] = []

  for (let i = 0; i < firstDay; i++) days.push({ date: 0, entries: [] })
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const dayEntries = props.entries.filter(e => e.date === dateStr)
    days.push({ date: d, entries: dayEntries })
  }
  return days
})
</script>

<template>
  <div class="calendar-view">
    <div class="cal-header">
      <button type="button" class="cal-nav" @click="emit('prev')">&lt;</button>
      <span class="cal-title">{{ month.getFullYear() }} 年 {{ month.getMonth() + 1 }} 月</span>
      <button type="button" class="cal-nav" @click="emit('next')">&gt;</button>
    </div>
    <div v-if="loading" class="loading-skeleton" style="padding: 16px 0;">
      <div v-for="i in 3" :key="i" class="skeleton-diary" style="height:60px" />
    </div>
    <div v-else class="cal-grid">
      <div v-for="wd in WEEKDAYS" :key="wd" class="cal-weekday">{{ wd }}</div>
      <div
        v-for="(day, idx) in calendarDays"
        :key="idx"
        class="cal-day"
        :class="{ 'cal-day--empty': day.date === 0, 'cal-day--has': day.entries.length > 0 }"
        @click="day.date > 0 && emit('select-day', day.date)"
      >
        <span class="cal-day-num">{{ day.date || '' }}</span>
        <div v-if="day.entries.length > 0" class="cal-day-dots">
          <span
            v-for="e in day.entries.slice(0, 3)"
            :key="e.id"
            class="cal-dot"
            :style="{ background: getMoodColor(e.mood || 3) }"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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

.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-3); }
.skeleton-diary { border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }
</style>

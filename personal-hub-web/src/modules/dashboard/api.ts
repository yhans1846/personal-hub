import request from '@/api/request'
import type { Result } from '@/types/common'

/** Dashboard 统计数据 */
export interface DashboardStats {
  noteCount: number
  studyCount: number
  studyDurationTotal: number
  studyDurationThisWeek: number
  todoTotal: number
  todoDone: number
  todoPending: number
  todoOverdue: number
  fileCount: number
  diaryCount: number
  diaryCountThisMonth: number
  bookmarkCount: number
  readingCount: number
  readingInProgress: number
  readingCompleted: number
  readingDurationTotal: number
  studyPlanCount: number
}

/** 趋势数据点 */
export interface TrendPoint {
  date: string
  value: number
}

/** 趋势数据 */
export interface TrendData {
  studyTrend: TrendPoint[]
  noteTrend: TrendPoint[]
  todoTrend: TrendPoint[]
  readingTrend: TrendPoint[]
}

/** 搜索结果条目 */
export interface SearchItem {
  id: number
  title: string
  snippet: string
  date: string
  url: string
}

/** 搜索结果分组 */
export interface SearchGroup {
  type: string
  label: string
  icon: string
  count: number
  items: SearchItem[]
}

/** 搜索结果 */
export interface SearchResult {
  groups: SearchGroup[]
  total: number
}

/** 获取 Dashboard 统计数据 */
export function getDashboardStats() {
  return request.get<Result<DashboardStats>>('/dashboard/stats')
}

/** 获取趋势数据 */
export function getTrends(days = 30) {
  return request.get<Result<TrendData>>('/dashboard/trends', { params: { days } })
}

/** 全局搜索 */
export function globalSearch(keyword: string) {
  return request.get<Result<SearchResult>>('/dashboard/search', { params: { keyword } })
}

// ====== 统计页面专用类型 ======

/** 综合统计数据 */
export interface StatsVO {
  // KPI
  noteCount: number
  noteCountChange: number
  readingHours: number
  readingHoursChange: number
  todoCompletionRate: number
  todoCompletionChange: number
  streakDays: number
  bestStreakDays: number

  // 趋势
  studyTrend: TrendPoint[]
  noteTrend: TrendPoint[]

  // Todo
  todoDone: number
  todoPending: number
  todoOverdue: number

  // 分类/标签
  categoryStats: NamedStat[]
  tagStats: NamedStat[]

  // 最近活动
  recentActivity: ActivityItem[]

  // 洞察
  insights: InsightItem[]

  // 笔记柱状图辅助
  avgDailyNotes: number
  maxDailyNotes: number
  minDailyNotes: number
}

export interface NamedStat {
  name: string
  count: number
  color?: string
}

export interface ActivityItem {
  id: number
  module: string
  action: string
  content: string
  createdAt: string
  timeLabel: string
}

export interface InsightItem {
  type: string
  icon: string
  title: string
  description: string
}

/** 获取综合统计数据（统计页面专用） */
export function getDetailedStats(days = 30) {
  return request.get<Result<StatsVO>>('/dashboard/detail', { params: { days } })
}

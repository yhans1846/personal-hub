import request from './request'
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

/** 获取 Dashboard 统计数据 */
export function getDashboardStats() {
  return request.get<Result<DashboardStats>>('/dashboard/stats')
}

/** 获取趋势数据 */
export function getTrends(days = 30) {
  return request.get<Result<TrendData>>('/dashboard/trends', { params: { days } })
}

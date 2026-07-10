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

/** 获取 Dashboard 统计数据 */
export function getDashboardStats() {
  return request.get<Result<DashboardStats>>('/dashboard/stats')
}

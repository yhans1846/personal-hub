/** 统一标签（学习计划分类） */
export interface StudyPlanTag {
  id: number
  name: string
  color: string
}

/** 学习计划 */
export interface StudyPlanVO {
  id: number
  name: string
  source: string | null
  author: string | null
  url: string | null
  remark: string | null
  progress: number
  startDate: string | null
  endDate: string | null
  status: number
  statusLabel: string
  tags: StudyPlanTag[]
  createdAt: string
  updatedAt: string
}

/** 创建/编辑学习计划 */
export interface StudyPlanCreateDTO {
  name: string
  source?: string | null
  author?: string | null
  url?: string | null
  remark?: string | null
  progress?: number
  startDate?: string | null
  endDate?: string | null
  status?: number
  tagIds?: number[]
}

/** 查询参数 */
export interface StudyPlanQuery {
  page?: number
  size?: number
  keyword?: string
  status?: number | ''
  tagId?: number | ''
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

/** 状态统计 */
export interface StudyPlanStats {
  total: number
  pending: number
  learning: number
  done: number
  paused: number
}

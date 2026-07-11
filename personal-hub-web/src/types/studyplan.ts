/** 学习计划 */
export interface StudyPlanVO {
  id: number
  name: string
  goal: string
  progress: number
  startDate: string | null
  endDate: string | null
  status: number
  statusLabel: string
  recordCount: number
  createdAt: string
  updatedAt: string
}

/** 创建/编辑学习计划 */
export interface StudyPlanCreateDTO {
  name: string
  goal?: string
  progress?: number
  startDate?: string | null
  endDate?: string | null
  status?: number
}

/** 查询参数 */
export interface StudyPlanQuery {
  page?: number
  size?: number
  keyword?: string
  status?: number
}

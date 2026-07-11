/** 学习记录 */
export interface StudyRecordVO {
  id: number
  subject: string
  date: string
  duration: number
  content: string
  reflection: string
  createdAt: string
  updatedAt: string
}

/** 创建/编辑学习记录 */
export interface StudyRecordCreateDTO {
  subject: string
  date: string
  duration: number
  content?: string
  reflection?: string
}

/** 查询参数 */
export interface StudyRecordQuery {
  page?: number
  size?: number
  keyword?: string
  startDate?: string
  endDate?: string
}

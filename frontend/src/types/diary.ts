/** 日记 */
export interface DiaryVO {
  id: number
  date: string
  title: string
  content: string
  contentSummary: string
  mood: number
  moodLabel: string
  weather: string
  createdAt: string
  updatedAt: string
}

/** 创建/编辑日记 */
export interface DiaryCreateDTO {
  date?: string
  title?: string
  content?: string
  mood?: number
  weather?: string
}

/** 查询参数 */
export interface DiaryQuery {
  page?: number
  size?: number
  keyword?: string
  startDate?: string
  endDate?: string
  mood?: number
  month?: string
}

/** 待办任务 */
export interface TodoVO {
  id: number
  title: string
  content: string
  isDone: number
  priority: number
  priorityLabel: string
  dueDate: string | null
  isOverdue: boolean
  createdAt: string
  updatedAt: string
}

/** 创建/编辑待办任务 */
export interface TodoCreateDTO {
  title: string
  content?: string
  priority: number
  dueDate?: string | null
}

/** 查询参数 */
export interface TodoQuery {
  page?: number
  size?: number
  keyword?: string
  priority?: number
  isDone?: boolean
}

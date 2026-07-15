/** 笔记 */
export interface NoteVO {
  id: number
  title: string
  content: string
  /** 列表摘要（无全文时用于卡片预览） */
  excerpt?: string
  isFavorite: number
  isDeleted: number
  createdAt: string
  updatedAt: string
  deletedAt?: string
  deleteReason?: string
  categories: CategoryItem[]
  tags: TagItem[]
}

export interface CategoryItem {
  id: number
  name: string
}

export interface TagItem {
  id: number
  name: string
  color?: string
}

/** 创建/编辑笔记 */
export interface NoteCreateDTO {
  title: string
  content: string
  categoryIds: number[]
  tagIds: number[]
}

/** 导入报告 */
export interface ImportReport {
  total: number
  success: number
  skipped: number
  failed: number
  resources: ImportResourceResult[]
  noteId?: number
  rewrittenContent?: string
  warning?: string
}

/** 单条资源导入结果 */
export interface ImportResourceResult {
  originalRef: string
  resolvedPath?: string
  success: boolean
  skipped: boolean
  message: string
}

/** 笔记查询参数 */
export interface NoteQuery {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number
  tagId?: number
  isFavorite?: boolean
  isDeleted?: boolean
}

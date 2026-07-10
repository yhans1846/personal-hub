/** 笔记 */
export interface NoteVO {
  id: number
  title: string
  content: string
  isFavorite: number
  createdAt: string
  updatedAt: string
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

import type { TagVO } from './tag'

/** 收藏夹 */
export interface BookmarkVO {
  id: number
  title: string
  url: string
  description: string
  favicon: string
  categoryId: number | null
  categoryName: string
  showOnDashboard?: number
  tags: TagVO[]
  createdAt: string
  updatedAt: string
}

/** 创建/编辑收藏 */
export interface BookmarkCreateDTO {
  title: string
  url: string
  description?: string
  favicon?: string
  categoryId?: number | null
  tagIds?: number[]
  showOnDashboard?: number
}

/** 查询参数 */
export interface BookmarkQuery {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number | null
  tagId?: number
}

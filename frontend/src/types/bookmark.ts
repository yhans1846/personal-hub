/** 收藏夹 */
export interface BookmarkVO {
  id: number
  title: string
  url: string
  description: string
  favicon: string
  categoryId: number | null
  categoryName: string
  tags: string
  tagList: string[]
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
  tags?: string
}

/** 查询参数 */
export interface BookmarkQuery {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number | null
  tag?: string
}

/** 收藏夹分类 */
export interface BookmarkCategoryVO {
  id: number
  name: string
  sortOrder: number
  count: number
  createdAt: string
}

/** 收藏夹分类请求 */
export interface BookmarkCategoryDTO {
  name: string
  sortOrder?: number
}

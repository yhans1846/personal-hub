/** 文件资源 */
export interface FileVO {
  id: number
  name: string
  type: string
  typeIcon: string
  typeLabel: string
  size: number
  sizeFormatted: string
  mimeType: string | null
  categoryId: number | null
  categoryName: string | null
  createdAt: string
}

/** 查询参数 */
export interface FileQuery {
  page?: number
  size?: number
  keyword?: string
  type?: string
  categoryId?: number
}

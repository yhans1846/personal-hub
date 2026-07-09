/**
 * 统一返回结构
 */
export interface Result<T> {
  code: number
  message: string
  data: T
}

/**
 * 分页返回结构
 */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

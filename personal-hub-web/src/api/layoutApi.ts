import request from './request'
import type { Result, LayoutResponse, LayoutSaveRequest } from '@/types/layout'

/** 获取用户所有布局配置 */
export function getLayoutAll() {
  return request.get<Result<LayoutResponse[]>>('/layout')
}

/** 保存布局配置 */
export function saveLayout(data: LayoutSaveRequest) {
  return request.put<Result<void>>('/layout', data)
}

/** 恢复默认布局 */
export function resetLayout(layoutType: string) {
  return request.delete<Result<void>>(`/layout/${layoutType}`)
}

import request from './request'
import type { Result } from '@/types/common'
import type { TagVO, TagCreateDTO, TagUpdateDTO } from '@/types/tag'

/** 获取所有标签 */
export function getTags() {
  return request.get<Result<TagVO[]>>('/tags')
}

/** 创建标签 */
export function createTag(data: TagCreateDTO) {
  return request.post<Result<TagVO>>('/tags', data)
}

/** 更新标签 */
export function updateTag(id: number, data: TagUpdateDTO) {
  return request.put<Result<void>>(`/tags/${id}`, data)
}

/** 删除标签 */
export function deleteTag(id: number) {
  return request.delete<Result<void>>(`/tags/${id}`)
}

/** 绑定标签到实体 */
export function bindTag(tagId: number, entityType: string, entityId: number) {
  return request.post<Result<void>>(`/tags/${tagId}/bind`, null, { params: { entityType, entityId } })
}

/** 解绑标签 */
export function unbindTag(tagId: number, entityType: string, entityId: number) {
  return request.delete<Result<void>>(`/tags/${tagId}/unbind`, { params: { entityType, entityId } })
}

/** 获取实体的标签列表 */
export function getEntityTags(entityType: string, entityId: number) {
  return request.get<Result<TagVO[]>>('/tags/entities', { params: { entityType, entityId } })
}

/** 批量设置实体标签 */
export function setEntityTags(entityType: string, entityId: number, tagIds: number[]) {
  return request.put<Result<void>>(`/tags/entities/${entityType}/${entityId}`, tagIds)
}

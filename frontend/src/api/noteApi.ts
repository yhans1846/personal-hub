import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { NoteVO, NoteCreateDTO, NoteQuery } from '@/types/note'

/** 笔记列表 */
export function getNoteList(params: NoteQuery) {
  return request.get<Result<PageResult<NoteVO>>>('/notes', { params })
}

/** 笔记详情 */
export function getNoteById(id: number) {
  return request.get<Result<NoteVO>>(`/notes/${id}`)
}

/** 新建笔记 */
export function createNote(data: NoteCreateDTO) {
  return request.post<Result<NoteVO>>('/notes', data)
}

/** 编辑笔记 */
export function updateNote(id: number, data: NoteCreateDTO) {
  return request.put<Result<NoteVO>>(`/notes/${id}`, data)
}

/** 删除笔记（移入回收站） */
export function deleteNote(id: number) {
  return request.delete<Result<void>>(`/notes/${id}`)
}

/** 切换收藏 */
export function toggleFavorite(id: number) {
  return request.put<Result<void>>(`/notes/${id}/favorite`)
}

/** 最近编辑 */
export function getRecentNotes(page = 1, size = 10) {
  return request.get<Result<PageResult<NoteVO>>>('/notes/recent', { params: { page, size } })
}

/** 恢复笔记 */
export function restoreNote(id: number) {
  return request.patch<Result<void>>(`/notes/${id}/restore`)
}

/** 永久删除 */
export function permanentDeleteNote(id: number) {
  return request.delete<Result<void>>(`/notes/${id}/permanent`)
}

// ========== 分类 ==========

export function getCategories() {
  return request.get<Result<any[]>>('/note-categories')
}

export function createCategory(data: { name: string; sortOrder?: number }) {
  return request.post<Result<any>>('/note-categories', data)
}

export function updateCategory(id: number, data: { name: string; sortOrder?: number }) {
  return request.put<Result<any>>(`/note-categories/${id}`, data)
}

export function deleteCategory(id: number) {
  return request.delete<Result<void>>(`/note-categories/${id}`)
}

// ========== 标签 ==========

export function getTags() {
  return request.get<Result<any[]>>('/note-tags')
}

export function createTag(data: { name: string }) {
  return request.post<Result<any>>('/note-tags', data)
}

export function updateTag(id: number, data: { name: string }) {
  return request.put<Result<any>>(`/note-tags/${id}`, data)
}

export function deleteTag(id: number) {
  return request.delete<Result<void>>(`/note-tags/${id}`)
}

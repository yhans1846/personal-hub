import request from '@/api/request'
import type { Result, PageResult } from '@/types/common'
import type { BookmarkVO, BookmarkCreateDTO, BookmarkQuery } from '@/types/bookmark'
import type { FileVO, FileQuery } from '@/types/file'

// ====== 书签 ======
export function getBookmarkList(params: BookmarkQuery) {
  return request.get<Result<PageResult<BookmarkVO>>>('/bookmarks', { params })
}
export function getBookmarkById(id: number) {
  return request.get<Result<BookmarkVO>>(`/bookmarks/${id}`)
}
export function createBookmark(data: BookmarkCreateDTO) {
  return request.post<Result<BookmarkVO>>('/bookmarks', data)
}
export function updateBookmark(id: number, data: BookmarkCreateDTO) {
  return request.put<Result<BookmarkVO>>(`/bookmarks/${id}`, data)
}
export function deleteBookmark(id: number) {
  return request.delete<Result<void>>(`/bookmarks/${id}`)
}
export function getDashboardBookmarks(limit = 8) {
  return request.get<Result<BookmarkVO[]>>('/bookmarks/dashboard', { params: { limit } })
}

// ====== 文件 ======
export function getFileList(params: FileQuery) {
  return request.get<Result<PageResult<FileVO>>>('/files', { params })
}
export function uploadFile(file: File, categoryId?: number) {
  const form = new FormData()
  form.append('file', file)
  if (categoryId) form.append('categoryId', String(categoryId))
  return request.post<Result<FileVO>>('/files/upload', form)
}
export function getFileDownloadUrl(id: number) {
  return `/api/files/${id}/download`
}
export function deleteFile(id: number) {
  return request.delete<Result<void>>(`/files/${id}`)
}

import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { FileVO, FileQuery, FileCategory } from '@/types/file'

export function getFileList(params: FileQuery) {
  return request.get<Result<PageResult<FileVO>>>('/files', { params })
}

export function getFileById(id: number) {
  return request.get<Result<FileVO>>(`/files/${id}`)
}

export function uploadFile(file: File, categoryId?: number) {
  const form = new FormData()
  form.append('file', file)
  if (categoryId) form.append('categoryId', String(categoryId))
  return request.post<Result<FileVO>>('/files/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getFileDownloadUrl(id: number) {
  return `/api/files/${id}/download`
}

export function deleteFile(id: number) {
  return request.delete<Result<void>>(`/files/${id}`)
}

export function getFileCategories() {
  return request.get<Result<FileCategory[]>>('/file-categories')
}

export function createFileCategory(data: { name: string; sortOrder?: number }) {
  return request.post<Result<FileCategory>>('/file-categories', data)
}

export function updateFileCategory(id: number, data: { name: string; sortOrder?: number }) {
  return request.put<Result<FileCategory>>(`/file-categories/${id}`, data)
}

export function deleteFileCategory(id: number) {
  return request.delete<Result<void>>(`/file-categories/${id}`)
}

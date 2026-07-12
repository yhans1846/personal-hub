import request from '@/api/request'
import type { Result } from '@/types/common'
import type { CategoryVO, CategoryCreateDTO } from '@/types/category'

export function getCategories(type: string) {
  return request.get<Result<CategoryVO[]>>('/categories', { params: { type } })
}

export function createCategory(data: CategoryCreateDTO) {
  return request.post<Result<CategoryVO>>('/categories', data)
}

export function updateCategory(id: number, data: { name: string; sortOrder?: number }) {
  return request.put<Result<CategoryVO>>(`/categories/${id}`, data)
}

export function deleteCategory(id: number) {
  return request.delete<Result<void>>(`/categories/${id}`)
}

export function batchUpdateCategorySort(list: { id: number; sortOrder: number }[]) {
  return request.put<Result<void>>('/categories/sort', list)
}

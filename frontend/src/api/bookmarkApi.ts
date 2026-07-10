import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { BookmarkVO, BookmarkCreateDTO, BookmarkQuery, BookmarkCategoryVO, BookmarkCategoryDTO } from '@/types/bookmark'

// ====== 收藏夹 ======
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

// ====== 收藏夹分类 ======
export function getBookmarkCategories() {
  return request.get<Result<BookmarkCategoryVO[]>>('/bookmark-categories')
}

export function createBookmarkCategory(data: BookmarkCategoryDTO) {
  return request.post<Result<BookmarkCategoryVO>>('/bookmark-categories', data)
}

export function updateBookmarkCategory(id: number, data: BookmarkCategoryDTO) {
  return request.put<Result<BookmarkCategoryVO>>(`/bookmark-categories/${id}`, data)
}

export function deleteBookmarkCategory(id: number) {
  return request.delete<Result<void>>(`/bookmark-categories/${id}`)
}

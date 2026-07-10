import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { ReadingVO, ReadingCreateDTO, ReadingQuery } from '@/types/reading'

export function getReadingList(params: ReadingQuery) {
  return request.get<Result<PageResult<ReadingVO>>>('/readings', { params })
}
export function getReadingById(id: number) {
  return request.get<Result<ReadingVO>>(`/readings/${id}`)
}
export function createReading(data: ReadingCreateDTO) {
  return request.post<Result<ReadingVO>>('/readings', data)
}
export function updateReading(id: number, data: ReadingCreateDTO) {
  return request.put<Result<ReadingVO>>(`/readings/${id}`, data)
}
export function deleteReading(id: number) {
  return request.delete<Result<void>>(`/readings/${id}`)
}

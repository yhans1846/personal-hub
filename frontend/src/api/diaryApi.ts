import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { DiaryVO, DiaryCreateDTO, DiaryQuery } from '@/types/diary'

export function getDiaryList(params: DiaryQuery) {
  return request.get<Result<PageResult<DiaryVO>>>('/diaries', { params })
}

export function getDiaryByMonth(month: string) {
  return request.get<Result<DiaryVO[]>>('/diaries/month', { params: { month } })
}

export function getDiaryById(id: number) {
  return request.get<Result<DiaryVO>>(`/diaries/${id}`)
}

export function createDiary(data: DiaryCreateDTO) {
  return request.post<Result<DiaryVO>>('/diaries', data)
}

export function updateDiary(id: number, data: DiaryCreateDTO) {
  return request.put<Result<DiaryVO>>(`/diaries/${id}`, data)
}

export function deleteDiary(id: number) {
  return request.delete<Result<void>>(`/diaries/${id}`)
}

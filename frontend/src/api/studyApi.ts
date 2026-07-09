import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { StudyRecordVO, StudyRecordCreateDTO, StudyRecordQuery } from '@/types/study'

export function getStudyRecordList(params: StudyRecordQuery) {
  return request.get<Result<PageResult<StudyRecordVO>>>('/study-records', { params })
}

export function getStudyRecordById(id: number) {
  return request.get<Result<StudyRecordVO>>(`/study-records/${id}`)
}

export function createStudyRecord(data: StudyRecordCreateDTO) {
  return request.post<Result<StudyRecordVO>>('/study-records', data)
}

export function updateStudyRecord(id: number, data: StudyRecordCreateDTO) {
  return request.put<Result<StudyRecordVO>>(`/study-records/${id}`, data)
}

export function deleteStudyRecord(id: number) {
  return request.delete<Result<void>>(`/study-records/${id}`)
}

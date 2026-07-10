import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { StudyPlanVO, StudyPlanCreateDTO, StudyPlanQuery } from '@/types/studyplan'

export function getStudyPlanList(params: StudyPlanQuery) {
  return request.get<Result<PageResult<StudyPlanVO>>>('/study-plans', { params })
}

export function getStudyPlanById(id: number) {
  return request.get<Result<StudyPlanVO>>(`/study-plans/${id}`)
}

export function createStudyPlan(data: StudyPlanCreateDTO) {
  return request.post<Result<StudyPlanVO>>('/study-plans', data)
}

export function updateStudyPlan(id: number, data: StudyPlanCreateDTO) {
  return request.put<Result<StudyPlanVO>>(`/study-plans/${id}`, data)
}

export function deleteStudyPlan(id: number) {
  return request.delete<Result<void>>(`/study-plans/${id}`)
}

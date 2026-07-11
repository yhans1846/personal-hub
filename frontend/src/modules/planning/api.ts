import request from '@/api/request'
import type { Result, PageResult } from '@/types/common'
import type { TodoVO, TodoCreateDTO, TodoQuery } from '@/types/todo'
import type { StudyPlanVO, StudyPlanCreateDTO, StudyPlanQuery } from '@/types/studyplan'

// ====== 待办 ======
export function getTodoList(params: TodoQuery) {
  return request.get<Result<PageResult<TodoVO>>>('/todos', { params })
}
export function getTodoById(id: number) {
  return request.get<Result<TodoVO>>(`/todos/${id}`)
}
export function createTodo(data: TodoCreateDTO) {
  return request.post<Result<TodoVO>>('/todos', data)
}
export function updateTodo(id: number, data: TodoCreateDTO) {
  return request.put<Result<TodoVO>>(`/todos/${id}`, data)
}
export function deleteTodo(id: number) {
  return request.delete<Result<void>>(`/todos/${id}`)
}
export function toggleDone(id: number) {
  return request.patch<Result<TodoVO>>(`/todos/${id}/done`)
}
export function getTodayTodos() {
  return request.get<Result<TodoVO[]>>('/todos/today')
}

// ====== 学习计划 ======
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

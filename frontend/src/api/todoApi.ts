import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { TodoVO, TodoCreateDTO, TodoQuery } from '@/types/todo'

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

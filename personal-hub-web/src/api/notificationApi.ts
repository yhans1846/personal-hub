// 向后兼容：保留通知 API（系统模块的一部分）

import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { NotificationVO } from '@/types/notification'

export function getNotificationList(params?: { page?: number; size?: number }) {
  return request.get<Result<PageResult<NotificationVO>>>('/notifications', { params })
}

export function markAsRead(ids: number[]) {
  return request.put<Result<void>>('/notifications/read', ids)
}

export function markAllAsRead() {
  return request.put<Result<void>>('/notifications/read-all')
}

export function clearAllNotifications() {
  return request.delete<Result<void>>('/notifications')
}

export function checkNotifications() {
  return request.post<Result<void>>('/notifications/check')
}

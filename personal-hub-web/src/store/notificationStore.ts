import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getUnreadCount,
  getNotificationList,
  markAsRead as apiMarkAsRead,
  markAllAsRead as apiMarkAllAsRead,
  clearAllNotifications,
} from '@/api/notificationApi'
import type { NotificationVO } from '@/types/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const notifications = ref<NotificationVO[]>([])
  const loading = ref(false)

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.data.data
    } catch { /* ignore */ }
  }

  async function fetchRecent() {
    loading.value = true
    try {
      const res = await getNotificationList({ page: 1, size: 10 })
      notifications.value = res.data.data.records
    } catch { /* ignore */ }
    finally { loading.value = false }
  }

  async function markAsRead(ids: number[]) {
    await apiMarkAsRead(ids)
    notifications.value = notifications.value.map(n =>
      ids.includes(n.id) ? { ...n, isRead: true } : n
    )
    unreadCount.value = Math.max(0, unreadCount.value - ids.length)
  }

  async function markAllAsRead() {
    await apiMarkAllAsRead()
    notifications.value.forEach(n => { n.isRead = true })
    unreadCount.value = 0
  }

  async function clearAll() {
    await clearAllNotifications()
    notifications.value = []
    unreadCount.value = 0
  }

  function getRelatedUrl(n: NotificationVO): string {
    if (!n.relatedType || !n.relatedId) return ''
    const routes: Record<string, string> = {
      todo: `/todos/${n.relatedId}/edit`,
      study_plan: `/study-plans/${n.relatedId}/edit`,
    }
    return routes[n.relatedType] || ''
  }

  return { unreadCount, notifications, loading, fetchUnreadCount, fetchRecent, markAsRead, markAllAsRead, clearAll, getRelatedUrl }
})

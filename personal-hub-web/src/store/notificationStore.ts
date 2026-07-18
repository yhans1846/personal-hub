import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getNotificationList,
  markAsRead as apiMarkAsRead,
  markAllAsRead as apiMarkAllAsRead,
  clearAllNotifications,
} from '@/api/notificationApi'
import type { NotificationVO } from '@/types/notification'
import { getNotificationRelatedPath } from '@/utils/deepLink'
import {
  countUnreadByTypes,
  filterNotificationsByTypes,
} from '@/utils/notificationFilter'
import { useNotificationConfigStore } from '@/store/notificationConfigStore'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const notifications = ref<NotificationVO[]>([])
  const loading = ref(false)

  function enabledTypes(): string[] {
    return useNotificationConfigStore().config.enabledTypes
  }

  async function fetchUnreadCount() {
    try {
      // 拉取近期列表后按启用类型计数，与设置页过滤一致
      const res = await getNotificationList({ page: 1, size: 50 })
      unreadCount.value = countUnreadByTypes(res.data.data.records, enabledTypes())
    } catch { /* ignore */ }
  }

  async function fetchRecent() {
    loading.value = true
    try {
      const res = await getNotificationList({ page: 1, size: 20 })
      notifications.value = filterNotificationsByTypes(res.data.data.records, enabledTypes())
      unreadCount.value = countUnreadByTypes(res.data.data.records, enabledTypes())
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
    return getNotificationRelatedPath(n.relatedType, n.relatedId)
  }

  return { unreadCount, notifications, loading, fetchUnreadCount, fetchRecent, markAsRead, markAllAsRead, clearAll, getRelatedUrl }
})

import { defineStore } from 'pinia'
import { getLayoutAll, saveLayout, resetLayout } from '@/api/layoutApi'
import { useStorageSync } from '@/composables/useStorageSync'
import type { NotificationConfig } from '@/types/layout'

const STORAGE_KEY = 'notification-config'
const LAYOUT_TYPE = 'notification'

const DEFAULTS: NotificationConfig = {
  desktopEnabled: false,
  enabledTypes: ['note_reminder', 'todo_due', 'study_progress', 'system'],
  soundEnabled: true,
  soundName: 'default',
  doNotDisturb: false,
  dndStart: '22:00',
  dndEnd: '08:00',
}

const NOTIFICATION_TYPES = [
  { value: 'note_reminder', label: '新笔记提醒' },
  { value: 'todo_due', label: '待办到期提醒' },
  { value: 'study_progress', label: '学习计划进度' },
  { value: 'system', label: '系统更新通知' },
]

const SOUND_OPTIONS = [
  { value: 'default', label: '默认' },
  { value: 'bell', label: '铃声' },
  { value: 'chime', label: '钟琴' },
  { value: 'pop', label: '轻点' },
]

export const useNotificationConfigStore = defineStore('notificationConfig', () => {
  const sync = useStorageSync<NotificationConfig>({
    storageKey: STORAGE_KEY,
    defaults: DEFAULTS,
    fetchApi: async () => {
      const res = await getLayoutAll()
      const notifLayout = (res.data.data as any[]).find(
        (l: any) => l.layoutType === LAYOUT_TYPE,
      )
      return notifLayout?.layoutJson ? JSON.parse(notifLayout.layoutJson) : null
    },
    saveApi: async (data) => {
      await saveLayout({
        layoutType: LAYOUT_TYPE,
        layoutJson: JSON.stringify(data),
      })
    },
    resetApi: async () => resetLayout(LAYOUT_TYPE),
    debugLabel: 'NotificationConfig',
  })

  const config = sync.data
  const { fetchFromBackend } = sync

  function updateConfig(partial: Partial<NotificationConfig>) {
    Object.assign(config, partial)
    sync.saveToLocal(config)
    sync.saveToBackend(config)
  }

  function toggleNotificationType(type: string) {
    const idx = config.enabledTypes.indexOf(type)
    if (idx >= 0) {
      config.enabledTypes.splice(idx, 1)
    } else {
      config.enabledTypes.push(type)
    }
    sync.saveToLocal(config)
    sync.saveToBackend(config)
  }

  async function resetConfig() {
    await sync.resetConfig()
  }

  /** 请求桌面通知权限，并同步 desktopEnabled */
  async function requestDesktopPermission(): Promise<boolean> {
    if (!('Notification' in window)) return false
    if (Notification.permission === 'granted') {
      updateConfig({ desktopEnabled: true })
      return true
    }
    if (Notification.permission === 'denied') {
      updateConfig({ desktopEnabled: false })
      return false
    }
    const result = await Notification.requestPermission()
    const granted = result === 'granted'
    updateConfig({ desktopEnabled: granted })
    return granted
  }

  // 初始化加载
  sync.fetchFromBackend()

  return {
    config,
    NOTIFICATION_TYPES,
    SOUND_OPTIONS,
    updateConfig,
    toggleNotificationType,
    resetConfig,
    requestDesktopPermission,
    fetchFromBackend,
  }
})

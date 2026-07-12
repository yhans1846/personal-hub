import { reactive } from 'vue'
import { defineStore } from 'pinia'
import { getLayoutAll, saveLayout, resetLayout } from '@/api/layoutApi'
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
  { value: 'bell', label: 'Bell' },
  { value: 'chime', label: 'Chime' },
  { value: 'pop', label: 'Pop' },
]

function loadFromLocal(): Partial<NotificationConfig> {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) return JSON.parse(stored)
  } catch { /* ignore */ }
  return {}
}

function saveToLocal(config: NotificationConfig) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
}

export const useNotificationConfigStore = defineStore('notificationConfig', () => {
  const config = reactive<NotificationConfig>({ ...DEFAULTS, ...loadFromLocal() })

  async function fetchFromBackend() {
    try {
      const res = await getLayoutAll()
      const notifLayout = (res.data.data as any[]).find((l: any) => l.layoutType === LAYOUT_TYPE)
      if (notifLayout?.layoutJson) {
        const parsed = JSON.parse(notifLayout.layoutJson)
        Object.assign(config, { ...DEFAULTS, ...parsed })
        saveToLocal({ ...config })
      }
    } catch {
      console.warn('[NotificationConfig] 后端加载失败，使用本地缓存')
    }
  }

  async function saveToBackend() {
    try {
      await saveLayout({
        layoutType: LAYOUT_TYPE,
        layoutJson: JSON.stringify({ ...config }),
      })
    } catch {
      console.warn('[NotificationConfig] 后端保存失败')
    }
  }

  function updateConfig(partial: Partial<NotificationConfig>) {
    Object.assign(config, partial)
    saveToLocal({ ...config })
    saveToBackend()
  }

  function toggleNotificationType(type: string) {
    const idx = config.enabledTypes.indexOf(type)
    if (idx >= 0) {
      config.enabledTypes.splice(idx, 1)
    } else {
      config.enabledTypes.push(type)
    }
    saveToLocal({ ...config })
    saveToBackend()
  }

  async function resetConfig() {
    Object.assign(config, { ...DEFAULTS })
    saveToLocal({ ...config })
    try {
      await resetLayout(LAYOUT_TYPE)
    } catch {
      console.warn('[NotificationConfig] 重置后端失败')
    }
  }

  /** 请求桌面通知权限 */
  async function requestDesktopPermission(): Promise<boolean> {
    if (!('Notification' in window)) return false
    if (Notification.permission === 'granted') return true
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
  fetchFromBackend()

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

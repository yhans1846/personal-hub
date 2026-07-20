import { defineStore } from 'pinia'
import { getLayoutAll, saveLayout, resetLayout } from '@/api/layoutApi'
import { useStorageSync } from '@/composables/useStorageSync'
import type { LayoutResponse, NotificationConfig } from '@/types/layout'
import {
  NOTIFICATION_TYPE_OPTIONS,
  NOTIFICATION_TYPE_VALUES,
  normalizeEnabledTypes,
} from '@/utils/notificationFilter'

const STORAGE_KEY = 'notification-config'
/** 对齐设置 Tab key=advanced；兼容旧值 notification */
const LAYOUT_TYPE = 'advanced'
const LEGACY_LAYOUT_TYPE = 'notification'

const DEFAULTS: NotificationConfig = {
  desktopEnabled: false,
  enabledTypes: [...NOTIFICATION_TYPE_VALUES],
  soundEnabled: true,
  soundName: 'default',
  doNotDisturb: false,
  dndStart: '22:00',
  dndEnd: '08:00',
}

const SOUND_OPTIONS = [
  { value: 'default', label: '默认' },
  { value: 'bell', label: '铃声' },
  { value: 'chime', label: '钟琴' },
  { value: 'pop', label: '轻点' },
]

function sanitizeConfig(raw: NotificationConfig | null): NotificationConfig | null {
  if (!raw) return null
  return {
    ...DEFAULTS,
    ...raw,
    enabledTypes: normalizeEnabledTypes(raw.enabledTypes),
  }
}

export const useNotificationConfigStore = defineStore('notificationConfig', () => {
  const sync = useStorageSync<NotificationConfig>({
    storageKey: STORAGE_KEY,
    defaults: DEFAULTS,
    afterLoad: (data) => {
      if (data.enabledTypes) {
        data.enabledTypes = normalizeEnabledTypes(data.enabledTypes)
      }
    },
    fetchApi: async () => {
      const res = await getLayoutAll()
      const layouts: LayoutResponse[] = res.data.data ?? []
      const notifLayout = layouts.find((l) => l.layoutType === LAYOUT_TYPE)
        ?? layouts.find((l) => l.layoutType === LEGACY_LAYOUT_TYPE)
      if (!notifLayout?.layoutJson) return null
      try {
        return sanitizeConfig(JSON.parse(notifLayout.layoutJson) as NotificationConfig)
      } catch {
        return null
      }
    },
    saveApi: async (data) => {
      await saveLayout({
        layoutType: LAYOUT_TYPE,
        layoutJson: JSON.stringify({
          ...data,
          enabledTypes: normalizeEnabledTypes(data.enabledTypes),
        }),
      })
    },
    resetApi: async () => resetLayout(LAYOUT_TYPE),
    debugLabel: 'NotificationConfig',
  })

  const config = sync.data
  // 启动时迁移本地旧类型
  config.enabledTypes = normalizeEnabledTypes(config.enabledTypes)

  function updateConfig(partial: Partial<NotificationConfig>) {
    Object.assign(config, partial)
    if (partial.enabledTypes) {
      config.enabledTypes = normalizeEnabledTypes(partial.enabledTypes)
    }
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
    config.enabledTypes = normalizeEnabledTypes(config.enabledTypes)
    sync.saveToLocal(config)
    sync.saveToBackend(config)
  }

  async function resetConfig() {
    await sync.resetConfig()
    config.enabledTypes = normalizeEnabledTypes(config.enabledTypes)
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

  sync.fetchFromBackend()

  return {
    config,
    NOTIFICATION_TYPES: NOTIFICATION_TYPE_OPTIONS,
    SOUND_OPTIONS,
    updateConfig,
    toggleNotificationType,
    resetConfig,
    requestDesktopPermission,
  }
})

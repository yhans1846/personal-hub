import { reactive, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import { getLayoutAll, saveLayout, resetLayout } from '@/api/layoutApi'

// ========== 类型 ==========

export type PreviewTheme = 'follow' | 'light' | 'dark' | 'sepia'

export interface ReadingConfig {
  fontSize: number          // 可选: 14, 16, 18, 20, 22
  readingWidth: number      // 可选: 900, 1100, 1280
  lineHeight: number        // 范围: 1.4 - 2.0, step 0.2
  theme: PreviewTheme       // follow / light / dark / sepia
  imageMaxWidth: number     // 可选: 60, 70, 80, 90, 100 (百分比)
}

// ========== 常量 ==========

const STORAGE_KEY = 'reading-config'
const LAYOUT_TYPE = 'preview'

const DEFAULTS: ReadingConfig = {
  fontSize: 18,
  readingWidth: 1100,  // 修正：原默认值 960 不在 UI 可选项中，改用 1100（标准）
  lineHeight: 1.8,
  theme: 'follow',
  imageMaxWidth: 80,
}

// ========== 工具函数 ==========

function loadFromLocal(): Partial<ReadingConfig> {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const parsed = JSON.parse(stored)
      // 旧值迁移：readingWidth=960 → 1100
      if (parsed.readingWidth === 960) parsed.readingWidth = 1100
      return parsed
    }
  } catch { /* ignore */ }
  return {}
}

function saveToLocal(config: ReadingConfig) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
}

/** 从旧 localStorage key 迁移数据到新 key */
function migrateOldKeys() {
  const oldSettings = localStorage.getItem('preview-reading-settings')
  const oldTheme = localStorage.getItem('preview-theme')
  if (!oldSettings && !oldTheme) return

  // 新 key 已有数据时不覆盖
  if (localStorage.getItem(STORAGE_KEY)) return

  const merged: any = {}
  if (oldSettings) {
    try {
      const parsed = JSON.parse(oldSettings)
      if (parsed.readingWidth === 960) parsed.readingWidth = 1100
      Object.assign(merged, parsed)
    } catch { /* ignore */ }
  }
  if (oldTheme) {
    merged.theme = oldTheme
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...DEFAULTS, ...merged }))

  // 清理旧 key
  localStorage.removeItem('preview-reading-settings')
  localStorage.removeItem('preview-theme')
}

/** 简易防抖（无外部依赖） */
function debounce<T extends (...args: any[]) => any>(fn: T, ms: number) {
  let timer: ReturnType<typeof setTimeout> | null = null
  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      timer = null
      fn(...args)
    }, ms)
  }
}

// ========== Store ==========

export const useReadingConfigStore = defineStore('readingConfig', () => {
  // 初始化：旧key迁移 → 本地加载
  migrateOldKeys()
  const config = reactive<ReadingConfig>({ ...DEFAULTS, ...loadFromLocal() })
  const loaded = ref(false)

  // ---- 后端同步 ----

  async function fetchFromBackend() {
    try {
      const res = await getLayoutAll()
      const previewLayout = (res.data.data as any[]).find(
        (l: any) => l.layoutType === LAYOUT_TYPE,
      )
      if (previewLayout?.layoutJson) {
        const parsed = JSON.parse(previewLayout.layoutJson)
        if (parsed.readingWidth === 960) parsed.readingWidth = 1100
        Object.assign(config, { ...DEFAULTS, ...parsed })
        saveToLocal(config)
      }
    } catch {
      console.warn('[ReadingConfig] 后端加载失败，使用本地缓存')
    } finally {
      loaded.value = true
    }
  }

  async function saveToBackend() {
    try {
      await saveLayout({
        layoutType: LAYOUT_TYPE,
        layoutJson: JSON.stringify(config),
      })
    } catch {
      console.warn('[ReadingConfig] 后端保存失败')
    }
  }

  const debouncedSaveToBackend = debounce(saveToBackend, 500)

  // ---- 公共操作 ----

  /** 部分更新配置（即时写 localStorage，防抖写后端） */
  async function updateConfig(partial: Partial<ReadingConfig>) {
    Object.assign(config, partial)
    saveToLocal(config)
    debouncedSaveToBackend()
  }

  /** 恢复默认 */
  async function resetConfig() {
    Object.assign(config, { ...DEFAULTS })
    saveToLocal(config)
    // 立即写后端（不防抖）
    try {
      await resetLayout(LAYOUT_TYPE)
    } catch {
      console.warn('[ReadingConfig] 重置后端失败')
    }
  }

  // 初始化时异步加载后端数据
  fetchFromBackend()

  return {
    config,
    loaded,
    updateConfig,
    resetConfig,
    fetchFromBackend,
  }
})

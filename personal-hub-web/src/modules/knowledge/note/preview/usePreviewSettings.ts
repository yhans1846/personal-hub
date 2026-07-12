import { reactive, watch } from 'vue'

const STORAGE_KEY = 'preview-reading-settings'

export interface PreviewSettings {
  fontSize: number     // 16 | 18 | 20 | 22
  readingWidth: number // 960 | 1100 | 1280
  lineHeight: number   // 1.6 | 1.8 | 2.0
}

const DEFAULTS: PreviewSettings = {
  fontSize: 18,
  readingWidth: 960,
  lineHeight: 1.8,
}

function loadFromStorage(): PreviewSettings {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const parsed = JSON.parse(stored)
      return { ...DEFAULTS, ...parsed }
    }
  } catch {
    /* ignore */
  }
  return { ...DEFAULTS }
}

function saveToStorage(settings: PreviewSettings) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
}

export function usePreviewSettings() {
  const settings = reactive<PreviewSettings>(loadFromStorage())

  // 持久化到 localStorage
  watch(
    () => ({ ...settings }),
    (val) => saveToStorage(val as PreviewSettings),
    { deep: true }
  )

  function reset() {
    Object.assign(settings, DEFAULTS)
  }

  return { settings, reset }
}

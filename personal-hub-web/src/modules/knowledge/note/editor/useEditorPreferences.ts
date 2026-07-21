import { reactive, watch } from 'vue'

export interface EditorPreferences {
  /** 是否全屏 */
  fullscreen: boolean
}

const STORAGE_KEY = 'editor-preferences'
const DEFAULTS: EditorPreferences = {
  fullscreen: false,
}

function load(): EditorPreferences {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw) as Record<string, unknown>
      return {
        fullscreen: !!parsed.fullscreen,
      }
    }
  } catch { /* ignore */ }
  return { ...DEFAULTS }
}

function save(prefs: EditorPreferences) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs))
}

let instance: { prefs: EditorPreferences; reset: () => void } | null = null

export function useEditorPreferences() {
  if (!instance) {
    const prefs = reactive<EditorPreferences>(load()) as EditorPreferences

    watch(
      () => prefs.fullscreen,
      () => save({ ...prefs }),
    )

    function reset() {
      Object.assign(prefs, DEFAULTS)
      save({ ...prefs })
    }

    instance = { prefs, reset }
  }

  return instance
}

import { reactive, watch } from 'vue'

export interface EditorPreferences {
  /** 是否开启实时预览（分栏） */
  livePreview: boolean
  /** 是否开启 Focus Mode */
  focusMode: boolean
  /** 分栏比例（左侧编辑器宽度百分比） */
  splitRatio: number
  /** 是否全屏 */
  fullscreen: boolean
}

const STORAGE_KEY = 'editor-preferences'
const DEFAULTS: EditorPreferences = {
  livePreview: true,
  focusMode: false,
  splitRatio: 55,
  fullscreen: false,
}

function load(): EditorPreferences {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return { ...DEFAULTS, ...JSON.parse(raw) }
  } catch { /* ignore */ }
  return { ...DEFAULTS }
}

function save(prefs: EditorPreferences) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs))
}

// ─── 模块级单例 ───
let instance: { prefs: EditorPreferences; reset: () => void } | null = null

/**
 * 编辑器偏好持久化（单例）
 *
 * 将用户的状态偏好写入 localStorage，下次进入编辑器时自动恢复。
 * 多次调用返回同一实例，避免多组件间状态不一致。
 */
export function useEditorPreferences() {
  if (!instance) {
    const prefs = reactive<EditorPreferences>(load()) as EditorPreferences

    const unwatch = watch(
      () => [prefs.livePreview, prefs.focusMode, prefs.splitRatio, prefs.fullscreen] as const,
      () => {
        save({ ...prefs })
      },
    )

    function reset() {
      Object.assign(prefs, DEFAULTS)
      save({ ...prefs })
    }

    instance = { prefs, reset }
    // 提供清理方法（仅测试用）
    ;(instance as any).__unwatch = unwatch
  }

  return instance
}

export type EditorPreferencesReturn = ReturnType<typeof useEditorPreferences>

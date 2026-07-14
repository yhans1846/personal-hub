import { ref, onUnmounted, watch } from 'vue'
import { useEditorPreferences } from './useEditorPreferences'

export type EditorMode = 'edit' | 'preview' | 'focus'

/**
 * 编辑器模式管理
 *
 * mode: edit / preview / focus — 三种互斥模式，决定编辑区渲染形态
 * livePreview: 是否开启分栏实时预览（仅在 edit 模式下生效）
 * isFullscreen: 浏览器全屏状态跟踪
 *
 * 用户偏好自动持久化到 localStorage。
 */
export function useEditorMode() {
  const { prefs } = useEditorPreferences()

  const mode = ref<EditorMode>('edit')
  const livePreview = ref(prefs.livePreview)
  const isFullscreen = ref(prefs.fullscreen)

  // 同步回 prefs（仅持久化有意义的值）
  watch(livePreview, (v) => { prefs.livePreview = v })
  watch(isFullscreen, (v) => { prefs.fullscreen = v })
  watch(mode, (m) => {
    if (m === 'focus') prefs.focusMode = true
    else prefs.focusMode = false
  })

  // ─── 切换函数 ───

  function togglePreview() {
    mode.value = mode.value === 'preview' ? 'edit' : 'preview'
  }

  function toggleFocus() {
    mode.value = mode.value === 'focus' ? 'edit' : 'focus'
  }

  function exitFocus() {
    if (mode.value === 'focus') mode.value = 'edit'
  }

  function toggleLivePreview() {
    // 切换分栏预览时自动回到 edit 模式
    livePreview.value = !livePreview.value
    if (livePreview.value && mode.value === 'preview') {
      mode.value = 'edit'
    }
  }

  /** 切换浏览器全屏 */
  async function toggleFullscreen() {
    if (!document.fullscreenElement) {
      try {
        await document.documentElement.requestFullscreen()
        isFullscreen.value = true
      } catch { /* 浏览器可能阻止全屏 */ }
    } else {
      try {
        await document.exitFullscreen()
        isFullscreen.value = false
      } catch { /* ignore */ }
    }
  }

  async function enterFullscreen() {
    if (!document.fullscreenElement) {
      try {
        await document.documentElement.requestFullscreen()
        isFullscreen.value = true
      } catch { /* ignore */ }
    }
  }

  async function exitFullscreen() {
    if (document.fullscreenElement) {
      try {
        await document.exitFullscreen()
        isFullscreen.value = false
      } catch { /* ignore */ }
    }
  }

  // ─── 键盘快捷键 ───

  function handleKeydown(e: KeyboardEvent) {
    const isCtrl = e.ctrlKey || e.metaKey

    // Ctrl+Shift+P: 切换编辑/预览
    if (isCtrl && e.shiftKey && (e.key === 'P' || e.key === 'p')) {
      e.preventDefault()
      togglePreview()
      return
    }

    // Ctrl+Shift+F: 浏览器全屏
    if (isCtrl && e.shiftKey && (e.key === 'F' || e.key === 'f')) {
      e.preventDefault()
      toggleFullscreen()
      return
    }

    // Esc: 退出全屏 或 退出 Focus Mode
    if (e.key === 'Escape') {
      if (document.fullscreenElement) {
        exitFullscreen()
        e.preventDefault()
        return
      }
      if (mode.value === 'focus') {
        exitFocus()
        e.preventDefault()
        return
      }
    }
  }

  // 监听全屏变化事件（用户按 F11 或 Esc 等）
  function onFullscreenChange() {
    isFullscreen.value = !!document.fullscreenElement
  }

  if (typeof window !== 'undefined') {
    window.addEventListener('keydown', handleKeydown)
    document.addEventListener('fullscreenchange', onFullscreenChange)
  }

  onUnmounted(() => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('keydown', handleKeydown)
      document.removeEventListener('fullscreenchange', onFullscreenChange)
    }
  })

  return {
    mode,
    livePreview,
    isFullscreen,
    togglePreview,
    toggleFocus,
    exitFocus,
    toggleLivePreview,
    toggleFullscreen,
    enterFullscreen,
    exitFullscreen,
  }
}

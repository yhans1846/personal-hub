import { ref, onUnmounted, watch } from 'vue'
import { useEditorPreferences } from './useEditorPreferences'

export type EditorMode = 'edit' | 'preview' | 'focus'

/**
 * 编辑器模式管理
 *
 * mode: edit / preview / focus
 * isFullscreen: 浏览器全屏状态
 */
export function useEditorMode() {
  const { prefs } = useEditorPreferences()

  const mode = ref<EditorMode>('edit')
  const isFullscreen = ref(prefs.fullscreen)

  watch(isFullscreen, (v) => { prefs.fullscreen = v })
  watch(mode, (m) => {
    prefs.focusMode = m === 'focus'
  })

  function togglePreview() {
    mode.value = mode.value === 'preview' ? 'edit' : 'preview'
  }

  function toggleFocus() {
    mode.value = mode.value === 'focus' ? 'edit' : 'focus'
  }

  function exitFocus() {
    if (mode.value === 'focus') mode.value = 'edit'
  }

  async function toggleFullscreen() {
    if (!document.fullscreenElement) {
      try {
        await document.documentElement.requestFullscreen()
        isFullscreen.value = true
      } catch { /* ignore */ }
    } else {
      try {
        await document.exitFullscreen()
        isFullscreen.value = false
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

  function handleKeydown(e: KeyboardEvent) {
    const isCtrl = e.ctrlKey || e.metaKey

    if (isCtrl && e.shiftKey && (e.key === 'P' || e.key === 'p')) {
      e.preventDefault()
      togglePreview()
      return
    }

    if (isCtrl && e.shiftKey && (e.key === 'F' || e.key === 'f')) {
      e.preventDefault()
      toggleFullscreen()
      return
    }

    if (e.key === 'Escape') {
      if (document.fullscreenElement) {
        exitFullscreen()
        e.preventDefault()
        return
      }
      if (mode.value === 'focus') {
        exitFocus()
        e.preventDefault()
      }
    }
  }

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
    isFullscreen,
    togglePreview,
    toggleFocus,
    exitFocus,
    toggleFullscreen,
    exitFullscreen,
  }
}

import { ref, onUnmounted, watch } from 'vue'
import { useEditorPreferences } from './useEditorPreferences'

export type EditorMode = 'edit' | 'preview' | 'split'

export function nextPreviewToggle(mode: EditorMode): EditorMode {
  if (mode === 'preview') return 'split'
  return 'preview'
}

export function initialEditorMode(viewportWidth: number): EditorMode {
  return viewportWidth <= 768 ? 'edit' : 'split'
}

/**
 * 编辑器模式管理
 *
 * mode: edit / preview / split
 * isFullscreen: 浏览器全屏状态
 */
export function useEditorMode() {
  const { prefs } = useEditorPreferences()

  const viewportWidth = typeof window !== 'undefined' ? window.innerWidth : 1280
  const mode = ref<EditorMode>(initialEditorMode(viewportWidth))
  const isFullscreen = ref(prefs.fullscreen)

  watch(isFullscreen, (v) => { prefs.fullscreen = v })

  function setMode(m: EditorMode) {
    mode.value = m
  }

  function togglePreview() {
    mode.value = nextPreviewToggle(mode.value)
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
    setMode,
    togglePreview,
    toggleFullscreen,
    exitFullscreen,
  }
}

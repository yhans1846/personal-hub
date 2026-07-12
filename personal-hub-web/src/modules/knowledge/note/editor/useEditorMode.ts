import { ref, onUnmounted } from 'vue'

export type EditorMode = 'edit' | 'preview' | 'focus'

export function useEditorMode() {
  const mode = ref<EditorMode>('edit')

  function togglePreview() {
    mode.value = mode.value === 'preview' ? 'edit' : 'preview'
  }

  function toggleFocus() {
    mode.value = mode.value === 'focus' ? 'edit' : 'focus'
  }

  function exitFocus() {
    if (mode.value === 'focus') mode.value = 'edit'
  }

  function handleKeydown(e: KeyboardEvent) {
    // Ctrl+Shift+P: 切换编辑/预览
    if ((e.ctrlKey || e.metaKey) && e.shiftKey && (e.key === 'P' || e.key === 'p')) {
      e.preventDefault()
      togglePreview()
    }
    // Ctrl+Shift+R: 切换专注模式
    if ((e.ctrlKey || e.metaKey) && e.shiftKey && (e.key === 'R' || e.key === 'r')) {
      e.preventDefault()
      toggleFocus()
    }
    // Ctrl+Shift+F: 全屏（浏览器全屏）
    if ((e.ctrlKey || e.metaKey) && e.shiftKey && (e.key === 'F' || e.key === 'f')) {
      e.preventDefault()
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen?.()
      } else {
        document.exitFullscreen?.()
      }
    }
    // Esc: 退出专注模式
    if (e.key === 'Escape' && mode.value === 'focus') {
      exitFocus()
    }
  }

  if (typeof window !== 'undefined') {
    window.addEventListener('keydown', handleKeydown)
  }

  onUnmounted(() => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('keydown', handleKeydown)
    }
  })

  return { mode, togglePreview, toggleFocus, exitFocus }
}

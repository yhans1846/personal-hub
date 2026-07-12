import { computed, onMounted, onUnmounted, watch } from 'vue'
import { useReadingConfigStore, type PreviewTheme } from '@/store/readingConfigStore'
import { storeToRefs } from 'pinia'

/**
 * 主题 DOM 应用 composable — 从 readingConfigStore 读取主题，
 * 管理 html[data-preview-theme] 属性及系统主题跟随。
 * 仅在 Preview.vue 中使用。
 */
export function useReadingTheme() {
  const store = useReadingConfigStore()
  const { config } = storeToRefs(store)

  /** 解析后的实际主题（follow → light/dark） */
  const resolvedTheme = computed<'light' | 'dark'>(() => {
    if (config.value.theme === 'follow') {
      return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light'
    }
    return config.value.theme as 'light' | 'dark'
  })

  /** 将当前主题应用到 DOM */
  function apply() {
    document.documentElement.setAttribute('data-preview-theme', config.value.theme)
  }

  // 监听系统 data-theme 变化（follow 模式同步）
  let observer: MutationObserver | null = null

  onMounted(() => {
    apply()
    observer = new MutationObserver(() => {
      if (config.value.theme === 'follow') {
        apply()
      }
    })
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme'],
    })
  })

  onUnmounted(() => {
    observer?.disconnect()
  })

  watch(() => config.value.theme, apply)

  return {
    theme: computed(() => config.value.theme),
    resolvedTheme,
  }
}

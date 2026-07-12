import { ref, watch, computed, onMounted, onUnmounted } from 'vue'

export type PreviewTheme = 'follow' | 'light' | 'dark' | 'sepia'

const STORAGE_KEY = 'preview-theme'
const ALL_THEMES: PreviewTheme[] = ['follow', 'light', 'dark', 'sepia']

function loadTheme(): PreviewTheme {
  try {
    const stored = localStorage.getItem(STORAGE_KEY) as PreviewTheme | null
    if (stored && ALL_THEMES.includes(stored)) return stored
  } catch {
    /* ignore */
  }
  return 'follow'
}

export function usePreviewTheme() {
  const theme = ref<PreviewTheme>(loadTheme())

  /** 解析出实际渲染主题（follow 时跟随系统） */
  const resolvedTheme = computed(() => {
    if (theme.value === 'follow') {
      return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light'
    }
    return theme.value
  })

  /** 将主题应用到 data-preview-theme */
  function apply(t: PreviewTheme) {
    document.documentElement.setAttribute('data-preview-theme', t)
    localStorage.setItem(STORAGE_KEY, t)
  }

  // 监听系统 data-theme 变化（follow 模式需同步）
  let observer: MutationObserver | null = null

  onMounted(() => {
    apply(theme.value)
    observer = new MutationObserver(() => {
      if (theme.value === 'follow') {
        apply('follow')
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

  watch(theme, (t) => apply(t))

  function setTheme(t: PreviewTheme) {
    theme.value = t
  }

  return { theme, resolvedTheme, setTheme }
}

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import './styles/global.css'
import './styles/markdown-prose.css'
import './styles/product-list.css'
import './styles/stats-row.css'
import App from './App.vue'
import router from './router'
import { applyUiFontToDOM } from './utils/uiFonts'

// 尽早应用 UI 字体，避免首屏系统栈闪一下
try {
  const raw = localStorage.getItem('appearance-config')
  const parsed = raw ? JSON.parse(raw) as { uiFont?: string } : null
  applyUiFontToDOM(parsed?.uiFont ?? 'source-sans')
} catch {
  applyUiFontToDOM('source-sans')
}

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')

// 主题初始化（优先用户偏好，其次系统偏好）
const storedTheme = localStorage.getItem('theme-preference')
if (storedTheme) {
  document.documentElement.setAttribute('data-theme', storedTheme)
  window.__themeUserOverride = true
} else {
  const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)')
  function updateTheme(e: MediaQueryListEvent | MediaQueryList) {
    if (window.__themeUserOverride) return
    document.documentElement.setAttribute('data-theme', e.matches ? 'dark' : 'light')
  }
  darkModeQuery.addEventListener('change', updateTheme)
  updateTheme(darkModeQuery)
}

// 强调色初始化
const savedAccent = localStorage.getItem('accent-preference')
if (savedAccent) {
  document.documentElement.setAttribute('data-accent', savedAccent)
}

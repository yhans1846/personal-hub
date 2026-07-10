import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/global.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')

// 主题初始化（优先用户偏好，其次系统偏好）
const storedTheme = localStorage.getItem('theme-preference')
if (storedTheme) {
  document.documentElement.setAttribute('data-theme', storedTheme)
  ;(window as any).__themeUserOverride = true
} else {
  const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)')
  function updateTheme(e: MediaQueryListEvent | MediaQueryList) {
    if ((window as any).__themeUserOverride) return
    document.documentElement.setAttribute('data-theme', e.matches ? 'dark' : 'light')
  }
  darkModeQuery.addEventListener('change', updateTheme)
  updateTheme(darkModeQuery)
}

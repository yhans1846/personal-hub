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

// 监听系统深色模式
const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)')
function updateTheme(e: MediaQueryListEvent | MediaQueryList) {
  document.documentElement.setAttribute('data-theme', e.matches ? 'dark' : 'light')
}
darkModeQuery.addEventListener('change', updateTheme)
updateTheme(darkModeQuery)

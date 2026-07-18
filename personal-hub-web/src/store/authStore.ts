import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '@/modules/system/api'
import { useLayoutStore } from '@/store/layoutStore'
import router from '@/router'
import type { AuthUser } from '@/types/user'

/**
 * 用户认证状态管理
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<AuthUser | null>(JSON.parse(localStorage.getItem('user') || 'null'))

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUser(newUser: AuthUser | null) {
    user.value = newUser
    if (newUser) {
      localStorage.setItem('user', JSON.stringify(newUser))
    } else {
      localStorage.removeItem('user')
    }
  }

  /** 登录；redirect=false 时由调用方自行跳转（用于成功动画） */
  async function login(
    username: string,
    password: string,
    captchaId: string,
    sliderX: number,
    options?: { redirect?: boolean },
  ) {
    const res = await loginApi(username, password, captchaId, sliderX)
    const { token: t, user: u } = res.data.data
    setToken(t)
    setUser(u)
    useLayoutStore().fetchLayout()
    if (options?.redirect !== false) {
      router.push('/')
    }
  }

  /** 退出登录 */
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  function isLoggedIn() {
    return !!token.value
  }

  return { token, user, setToken, setUser, login, logout, isLoggedIn }
})

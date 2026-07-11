import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '@/api/authApi'
import router from '@/router'

/**
 * 用户认证状态管理
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<any>(JSON.parse(localStorage.getItem('user') || 'null'))

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUser(newUser: any) {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  /** 登录 */
  async function login(username: string, password: string) {
    const res = await loginApi(username, password)
    const { token: t, user: u } = res.data.data
    setToken(t)
    setUser(u)
    router.push('/')
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

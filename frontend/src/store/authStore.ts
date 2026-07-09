import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户认证状态管理
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<any>(null)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUser(newUser: any) {
    user.value = newUser
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  function isLoggedIn() {
    return !!token.value
  }

  return { token, user, setToken, setUser, logout, isLoggedIn }
})

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('kanban_user') || 'null'))
  const token = ref(localStorage.getItem('kanban_token') || '')

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await authApi.login({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('kanban_token', res.data.token)
    localStorage.setItem('kanban_user', JSON.stringify(res.data.user))
    return res
  }

  async function register(data) {
    const res = await authApi.register(data)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('kanban_token', res.data.token)
    localStorage.setItem('kanban_user', JSON.stringify(res.data.user))
    return res
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('kanban_token')
    localStorage.removeItem('kanban_user')
  }

  return { user, token, isLoggedIn, login, register, logout }
})

import { defineStore } from 'pinia'
import { ref } from 'vue'
import router from '@/router'
import { login as loginApi, logout as logoutApi } from '@/api/login'
import { getCurrentUser } from '@/api/user'
import { setRememberedLogin } from '@/utils/rememberedLogin'

export const useUserStore = defineStore('user', () => {
  const username = ref(localStorage.getItem('xxl_username') || '')
  const role = ref(Number(localStorage.getItem('xxl_role')) || 0)
  const isLoggedIn = ref(!!localStorage.getItem('xxl_username'))

  const isAdmin = () => role.value === 1

  async function login(form) {
    const res = await loginApi(form)
    if (res.data && res.data.code === 200) {
      try {
        const currentUser = await getCurrentUser()
        username.value = currentUser.data?.data?.username || form.userName
        role.value = Number(currentUser.data?.data?.role) || 0
      } catch (_) {
        username.value = form.userName
        role.value = 0
      }
      isLoggedIn.value = true
      localStorage.setItem('xxl_username', username.value)
      localStorage.setItem('xxl_role', role.value)
      setRememberedLogin(username.value, !!form.ifRemember)
      router.push('/dashboard')
    }
    return res
  }

  async function logout() {
    try { await logoutApi() } catch (_) { /* ignore */ }
    username.value = ''
    role.value = 0
    isLoggedIn.value = false
    localStorage.removeItem('xxl_user')
    localStorage.removeItem('xxl_username')
    localStorage.removeItem('xxl_role')
    router.push('/login')
  }

  return { username, role, isLoggedIn, isAdmin, login, logout }
})

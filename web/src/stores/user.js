import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import router from '@/router'
import { login as loginApi, logout as logoutApi } from '@/api/login'

export const useUserStore = defineStore('user', () => {
  const username = ref(localStorage.getItem('xxl_user') || '')
  const role = ref(Number(localStorage.getItem('xxl_role')) || 0)
  const isLoggedIn = ref(!!localStorage.getItem('xxl_user'))

  const isAdmin = () => role.value === 1

  async function login(form) {
    const res = await loginApi(form)
    if (res.data && res.data.code === 200) {
      username.value = form.userName
      isLoggedIn.value = true
      localStorage.setItem('xxl_user', form.userName)
      // 探测角色：用原始 axios 避免拦截器弹框
      try {
        const r = await axios.post('/xxl-job-admin/user/pageList', null, {
          params: { start: 0, length: 1, username: '', role: -1 },
          withCredentials: true
        })
        if (r.data?.recordsTotal !== undefined) {
          role.value = 1
        }
      } catch (_) {
        role.value = 0
      }
      localStorage.setItem('xxl_role', role.value)
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
    localStorage.removeItem('xxl_role')
    router.push('/login')
  }

  return { username, role, isLoggedIn, isAdmin, login, logout }
})

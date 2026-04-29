/**
 * 用户状态管理
 * - token / 用户信息管理
 * - 登录由 LoginView 直接调用 API 完成
 * - 退出登录：清除状态，跳转 /login
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export const useUserStore = defineStore('user', () => {
  const router = useRouter()

  const token = ref<string>(localStorage.getItem('access_token') || '')
  const username = ref<string>(localStorage.getItem('h5_username') || '')
  const avatar = ref<string>('')
  const roles = ref<string[]>([])

  /** 退出登录 */
  function logout(): void {
    token.value = ''
    username.value = ''
    avatar.value = ''
    roles.value = []
    localStorage.removeItem('access_token')
    localStorage.removeItem('h5_username')
    router.push('/login')
  }

  return {
    token,
    username,
    avatar,
    roles,
    logout
  }
})

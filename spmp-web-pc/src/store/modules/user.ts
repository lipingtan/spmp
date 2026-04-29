/**
 * 用户状态管理
 * - token / 用户信息 / 权限 / 菜单
 * - 对接后端认证和个人中心 API
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login as loginApi, logout as logoutApi, getCaptcha as getCaptchaApi } from '@/api/auth'
import { getProfile } from '@/api/profile'
import { getUserMenuTree } from '@/api/menu'
import type { LoginParams, TokenResult, CaptchaResult } from '@/api/auth'
import type { ProfileInfo } from '@/api/profile'
import type { MenuTreeItem } from '@/api/menu'

export const useUserStore = defineStore('user', () => {
  const router = useRouter()

  const token = ref<string>(localStorage.getItem('access_token') || '')
  const refreshTokenVal = ref<string>(localStorage.getItem('refresh_token') || '')
  const userId = ref<number>(0)
  const username = ref<string>('')
  const realName = ref<string>('')
  const avatar = ref<string>('')
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])
  const menus = ref<MenuTreeItem[]>([])

  /** 登录 */
  async function login(params: LoginParams): Promise<TokenResult> {
    const result = await loginApi(params)
    token.value = result.accessToken
    refreshTokenVal.value = result.refreshToken
    localStorage.setItem('access_token', result.accessToken)
    localStorage.setItem('refresh_token', result.refreshToken)
    userId.value = result.userId
    username.value = result.username
    realName.value = result.realName
    avatar.value = result.avatar || ''
    return result
  }

  /** 获取用户信息 */
  async function fetchUserInfo(): Promise<ProfileInfo> {
    const info = await getProfile()
    userId.value = info.userId
    username.value = info.username
    realName.value = info.realName
    avatar.value = info.avatar || ''
    roles.value = info.roles || []
    permissions.value = info.permissions || []
    return info
  }

  /** 获取用户菜单 */
  async function fetchMenus(): Promise<MenuTreeItem[]> {
    const tree = await getUserMenuTree()
    menus.value = tree
    return tree
  }

  /** 获取验证码 */
  async function fetchCaptcha(): Promise<CaptchaResult> {
    return getCaptchaApi()
  }

  /** 登出 */
  async function logout(): Promise<void> {
    try {
      await logoutApi()
    } catch {
      // 忽略登出接口错误
    }
    resetState()
    router.push('/login')
  }

  /** 重置状态 */
  function resetState(): void {
    token.value = ''
    refreshTokenVal.value = ''
    userId.value = 0
    username.value = ''
    realName.value = ''
    avatar.value = ''
    roles.value = []
    permissions.value = []
    menus.value = []
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
  }

  /** 检查权限 */
  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm)
  }

  return {
    token, refreshTokenVal, userId, username, realName, avatar,
    roles, permissions, menus,
    login, fetchUserInfo, fetchMenus, fetchCaptcha, logout, resetState, hasPermission
  }
})

/**
 * 路由守卫
 * - 未认证页面直接放行
 * - 无 Token 重定向 /login（携带 redirect）
 * - 已登录访问 /login 重定向 /home
 * - 登录后自动加载用户信息和菜单
 * - 路由切换时自动添加 TagsView 标签
 */

import type { Router } from 'vue-router'
import { useTagsViewStore } from '@/store/modules/tags-view'
import { useUserStore } from '@/store/modules/user'
import { checkInitStatus } from '@/api/init'

/** 初始化状态缓存，避免每次路由跳转都请求 */
let initChecked = false
let isInitialized = true
/** 用户信息是否已加载 */
let userInfoLoaded = false

export function setupRouterGuard(router: Router): void {
  router.beforeEach(async (to, _from, next) => {
    // 初始化页面直接放行
    if (to.path === '/init') {
      return next()
    }

    // 首次访问时检查初始化状态
    if (!initChecked) {
      try {
        const status = await checkInitStatus()
        isInitialized = status.initialized
      } catch {
        isInitialized = true
      }
      initChecked = true
    }

    // 未初始化，重定向到初始化页面
    if (!isInitialized) {
      return next({ path: '/init' })
    }

    const token = localStorage.getItem('access_token')

    // 不需要认证的页面直接放行
    if (to.meta.requiresAuth === false) {
      return next()
    }

    // 未登录，跳转登录页
    if (!token) {
      return next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }

    // 已登录访问登录页，重定向到首页
    if (to.path === '/login') {
      return next({ path: '/home' })
    }

    // 登录后首次访问，加载用户信息和菜单
    if (!userInfoLoaded) {
      try {
        const userStore = useUserStore()
        await userStore.fetchUserInfo()
        await userStore.fetchMenus()
        userInfoLoaded = true
      } catch {
        // 加载失败（Token 过期等），清除状态跳转登录
        localStorage.removeItem('access_token')
        userInfoLoaded = false
        return next({ path: '/login', query: { redirect: to.fullPath } })
      }
    }

    // 路由切换时添加标签
    const tagsViewStore = useTagsViewStore()
    tagsViewStore.addView(to)

    next()
  })
}

/** 重置守卫状态（登出时调用） */
export function resetGuardState(): void {
  userInfoLoaded = false
}

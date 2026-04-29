/**
 * H5 端路由守卫
 * - 未认证页面直接放行
 * - 无 Token 重定向 /login（携带 redirect）
 * - 已登录访问 /login 重定向 /home
 * - 根据 Tab 索引设置过渡动画方向
 */

import type { Router } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import { TAB_INDEX_MAP } from './static-routes'

export function setupRouterGuard(router: Router): void {
  router.beforeEach((to, from, next) => {
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

    // 根据 Tab 索引判断过渡动画方向
    const appStore = useAppStore()
    const toIndex = TAB_INDEX_MAP[to.path]
    const fromIndex = TAB_INDEX_MAP[from.path]

    if (toIndex !== undefined && fromIndex !== undefined) {
      // 两个都是 Tab 页面，根据索引判断方向
      appStore.setTransitionName(toIndex > fromIndex ? 'slide-left' : 'slide-right')
    } else {
      // 非 Tab 页面间切换，默认左滑进入
      appStore.setTransitionName('slide-left')
    }

    next()
  })
}

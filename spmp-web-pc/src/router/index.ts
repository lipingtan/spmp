/**
 * 路由实例
 * - createWebHistory 模式
 * - 注册 constantRoutes + asyncRoutes
 * - 预留 setupDynamicRoutes()
 */

import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes, asyncRoutes } from './static-routes'

const router = createRouter({
  history: createWebHistory(),
  routes: [...constantRoutes, ...asyncRoutes],
  scrollBehavior: () => ({ top: 0 })
})

/**
 * 动态路由注册（预留）
 * 后续从后端 API 获取菜单权限后，使用 router.addRoute() 动态注册
 */
// export function setupDynamicRoutes(menus: MenuItem[]): void {
//   1. 调用 GET /api/v1/auth/menus 获取用户菜单权限
//   2. 将菜单数据转换为 RouteRecordRaw[]
//   3. 使用 router.addRoute() 动态注册
//   4. 添加 404 兜底路由（动态路由注册后再添加）
// }

export default router

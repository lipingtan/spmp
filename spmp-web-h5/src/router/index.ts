/**
 * H5 端路由实例
 * - createWebHistory 模式
 * - 注册 constantRoutes + tabRoutes
 */

import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes, tabRoutes } from './static-routes'

const router = createRouter({
  history: createWebHistory(),
  routes: [...constantRoutes, ...tabRoutes],
  scrollBehavior: () => ({ top: 0 })
})

export default router

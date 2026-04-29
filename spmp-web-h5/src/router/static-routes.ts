/**
 * H5 端静态路由配置
 * - constantRoutes：不需要布局的路由（登录页、404）
 * - tabRoutes：TabBarLayout 布局下 5 个 Tab 子路由
 */

import type { RouteRecordRaw } from 'vue-router'

/** 不需要布局的路由 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'h5-login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'h5-register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  // 业主管理 — 需要认证的独立页面
  {
    path: '/certify',
    name: 'h5-certify',
    component: () => import('@/views/certify/index.vue'),
    meta: { title: '房产认证' }
  },
  {
    path: '/mine/properties',
    name: 'h5-properties',
    component: () => import('@/views/mine/properties.vue'),
    meta: { title: '我的房产' }
  },
  {
    path: '/mine/family',
    name: 'h5-family',
    component: () => import('@/views/mine/family.vue'),
    meta: { title: '家庭成员' }
  },
  {
    path: '/mine/profile',
    name: 'h5-profile',
    component: () => import('@/views/mine/profile.vue'),
    meta: { title: '个人信息' }
  },
  {
    path: '/workorder/:id',
    name: 'h5-workorder-detail',
    component: () => import('@/views/workorder/detail.vue'),
    meta: { title: '工单详情' }
  },
  {
    path: '/notice/:id',
    name: 'h5-notice-detail',
    component: () => import('@/views/notice/detail.vue'),
    meta: { title: '公告详情' }
  },
  {
    path: '/repair/dashboard',
    name: 'h5-repair-dashboard',
    component: () => import('@/views/repair/dashboard.vue'),
    meta: { title: '维修工作台' }
  },
  {
    path: '/repair/pending',
    name: 'h5-repair-pending',
    component: () => import('@/views/repair/pending.vue'),
    meta: { title: '待处理工单' }
  },
  {
    path: '/repair/history',
    name: 'h5-repair-history',
    component: () => import('@/views/repair/history.vue'),
    meta: { title: '历史工单' }
  },
  {
    path: '/repair/handle/:id',
    name: 'h5-repair-handle',
    component: () => import('@/views/repair/handle.vue'),
    meta: { title: '处理工单' }
  },
  // 缴费 — 账单详情和缴费记录
  {
    path: '/billing/detail/:id',
    name: 'h5-bill-detail',
    component: () => import('@/views/billing/BillDetailView.vue'),
    meta: { title: '账单详情' }
  },
  {
    path: '/billing/records',
    name: 'h5-payment-records',
    component: () => import('@/views/billing/PaymentRecordsView.vue'),
    meta: { title: '缴费记录' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'h5-not-found',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: { title: '页面不存在', requiresAuth: false }
  }
]

/** Tab 栏路由 */
export const tabRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/TabBarLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'h5-home',
        component: () => import('@/views/home/HomeView.vue'),
        meta: { title: '首页', tabBar: true, requiresAuth: false }
      },
      {
        path: 'workorder',
        name: 'h5-workorder',
        component: () => import('@/views/workorder/WorkorderView.vue'),
        meta: { title: '报修', tabBar: true }
      },
      {
        path: 'billing',
        name: 'h5-billing',
        component: () => import('@/views/billing/BillingView.vue'),
        meta: { title: '缴费', tabBar: true }
      },
      {
        path: 'notice',
        name: 'h5-notice',
        component: () => import('@/views/notice/NoticeView.vue'),
        meta: { title: '公告', tabBar: true, requiresAuth: false }
      },
      {
        path: 'mine',
        name: 'h5-mine',
        component: () => import('@/views/mine/MineView.vue'),
        meta: { title: '我的', tabBar: true, requiresAuth: false }
      }
    ]
  }
]

/**
 * Tab 路由路径与索引映射
 * 用于路由守卫中判断滑动方向
 */
export const TAB_INDEX_MAP: Record<string, number> = {
  '/home': 0,
  '/workorder': 1,
  '/billing': 2,
  '/notice': 3,
  '/mine': 4
}

/**
 * 静态路由配置
 * - constantRoutes：不需要布局的路由（登录页、404）
 * - asyncRoutes：需要布局的业务路由（框架搭建阶段静态配置，后续改为动态注册）
 */

import type { RouteRecordRaw } from 'vue-router'

/** 不需要布局的路由 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/init',
    name: 'system-init',
    component: () => import('@/views/init/InitPage.vue'),
    meta: { title: '系统初始化', requiresAuth: false, hidden: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false, hidden: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: { title: '404', requiresAuth: false, hidden: true }
  }
]

/** 需要布局的业务路由（后续改为动态注册） */
export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/AppLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/home/HomeView.vue'),
        meta: { title: '首页', icon: 'HomeFilled', affix: true }
      },
      // 系统管理
      {
        path: 'system/users',
        name: 'system-users',
        component: () => import('@/views/system/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'user:user:list' }
      },
      {
        path: 'system/roles',
        name: 'system-roles',
        component: () => import('@/views/system/role/RoleList.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', permission: 'user:role:list' }
      },
      {
        path: 'system/menus',
        name: 'system-menus',
        component: () => import('@/views/system/menu/MenuList.vue'),
        meta: { title: '菜单管理', icon: 'Menu', permission: 'user:menu:list' }
      },
      // 日志管理
      {
        path: 'log/login-logs',
        name: 'login-logs',
        component: () => import('@/views/system/log/LoginLogList.vue'),
        meta: { title: '登录日志', icon: 'Document', permission: 'user:log:list' }
      },
      {
        path: 'log/operation-logs',
        name: 'operation-logs',
        component: () => import('@/views/system/log/OperationLogList.vue'),
        meta: { title: '操作日志', icon: 'Notebook', permission: 'user:log:list' }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/profile/ProfilePage.vue'),
        meta: { title: '个人中心', icon: 'Avatar', hidden: true }
      },
      // 基础数据管理
      {
        path: 'base/districts',
        name: 'base-districts',
        component: () => import('@/views/base/district/index.vue'),
        meta: { title: '片区管理', icon: 'Location', permission: 'base:district:list' }
      },
      {
        path: 'base/communities',
        name: 'base-communities',
        component: () => import('@/views/base/community/index.vue'),
        meta: { title: '小区管理', icon: 'House', permission: 'base:community:list' }
      },
      {
        path: 'base/buildings',
        name: 'base-buildings',
        component: () => import('@/views/base/building/index.vue'),
        meta: { title: '楼栋管理', icon: 'School', permission: 'base:building:list' }
      },
      {
        path: 'base/units',
        name: 'base-units',
        component: () => import('@/views/base/unit/index.vue'),
        meta: { title: '单元管理', icon: 'Menu', permission: 'base:unit:list' }
      },
      {
        path: 'base/houses',
        name: 'base-houses',
        component: () => import('@/views/base/house/index.vue'),
        meta: { title: '房屋管理', icon: 'Key', permission: 'base:house:list' }
      },
      // 业主管理
      {
        path: 'owner/owners',
        name: 'owner-list',
        component: () => import('@/views/owner/owner/index.vue'),
        meta: { title: '业主列表', icon: 'UserFilled', permission: 'owner:owner:list' }
      },
      {
        path: 'owner/owners/:id',
        name: 'owner-detail',
        component: () => import('@/views/owner/owner/detail.vue'),
        meta: { title: '业主详情', icon: 'UserFilled', hidden: true }
      },
      {
        path: 'owner/certifications',
        name: 'owner-certification',
        component: () => import('@/views/owner/certification/index.vue'),
        meta: { title: '认证审批', icon: 'Stamp', permission: 'owner:certification:list' }
      },
      {
        path: 'workorder/list',
        name: 'workorder-list',
        component: () => import('@/views/workorder/order/index.vue'),
        meta: { title: '工单列表', icon: 'Tickets', permission: 'workorder:list' }
      },
      {
        path: 'workorder/list/:id',
        name: 'workorder-detail',
        component: () => import('@/views/workorder/order/detail.vue'),
        meta: { title: '工单详情', icon: 'Tickets', hidden: true }
      },
      {
        path: 'workorder/statistics',
        name: 'workorder-statistics',
        component: () => import('@/views/workorder/statistics/index.vue'),
        meta: { title: '工单统计', icon: 'DataAnalysis', permission: 'workorder:statistics' }
      },
      // 缴费管理
      {
        path: 'billing/bills',
        name: 'billing-bills',
        component: () => import('@/views/billing/bills/index.vue'),
        meta: { title: '账单管理', icon: 'Wallet', permission: 'billing:bill:list' }
      },
      {
        path: 'billing/overdue',
        name: 'billing-overdue',
        component: () => import('@/views/billing/overdue/index.vue'),
        meta: { title: '逾期催收', icon: 'Warning', permission: 'billing:overdue:list' }
      },
      {
        path: 'billing/config',
        name: 'billing-config',
        component: () => import('@/views/billing/config/index.vue'),
        meta: { title: '费用配置', icon: 'Setting', permission: 'billing:config:list' }
      },
      {
        path: 'billing/statistics',
        name: 'billing-statistics',
        component: () => import('@/views/billing/statistics/index.vue'),
        meta: { title: '收费统计', icon: 'DataAnalysis', permission: 'billing:statistics' }
      },
      {
        path: 'notice/list',
        name: 'notice-list',
        component: () => import('@/views/notice/list/index.vue'),
        meta: { title: '公告列表', icon: 'Bell', permission: 'notice:list' }
      },
      {
        path: 'notice/create',
        name: 'notice-create',
        component: () => import('@/views/notice/create/index.vue'),
        meta: { title: '发布公告', icon: 'Edit', permission: 'notice:create' }
      },
      {
        path: 'notice/approve',
        name: 'notice-approve',
        component: () => import('@/views/notice/approve/index.vue'),
        meta: { title: '审批列表', icon: 'Stamp', permission: 'notice:approve' }
      },
      {
        path: 'notice/detail/:id',
        name: 'notice-detail',
        component: () => import('@/views/notice/detail/index.vue'),
        meta: { title: '公告统计详情', icon: 'DataAnalysis', hidden: true }
      },
      {
        path: 'access',
        name: 'access',
        component: () => import('@/views/placeholder/PlaceholderView.vue'),
        meta: { title: '门禁管理', icon: 'Lock' }
      }
    ]
  }
]

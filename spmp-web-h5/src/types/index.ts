/**
 * SPMP H5 端 TypeScript 类型定义
 * 包含 API 响应、分页、用户、菜单、应用状态等通用类型
 */

// ==================== API 通用类型 ====================

/** 后端统一响应结构（与后端 Result<T> 对齐） */
export interface ApiResponse<T = any> {
  /** 业务状态码，200 表示成功 */
  code: number
  /** 提示信息 */
  message: string
  /** 响应数据 */
  data: T
  /** 链路追踪 ID */
  traceId?: string
}

/** 分页请求参数 */
export interface PageParams {
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
}

/** 分页响应数据 */
export interface PageResult<T> {
  /** 数据列表 */
  list: T[]
  /** 总记录数 */
  total: number
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
}

// ==================== 用户相关类型 ====================

/** 登录请求参数 */
export interface LoginParams {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
}

/** 登录响应数据 */
export interface LoginResult {
  /** 访问令牌 */
  token: string
  /** 刷新令牌 */
  refreshToken?: string
}

/** 用户信息 */
export interface UserInfo {
  /** 用户 ID */
  userId: string
  /** 用户名 */
  username: string
  /** 真实姓名 */
  realName: string
  /** 头像地址 */
  avatar: string
  /** 角色列表 */
  roles: string[]
  /** 权限列表 */
  permissions: string[]
}

// ==================== 菜单与路由类型 ====================

/** 菜单项（后端返回 / 前端静态配置） */
export interface MenuItem {
  /** 菜单 ID */
  id: string
  /** 父级菜单 ID */
  parentId: string | null
  /** 路由 name */
  name: string
  /** 路由 path */
  path: string
  /** 组件路径 */
  component: string
  /** 菜单标题 */
  title: string
  /** 菜单图标 */
  icon?: string
  /** 排序 */
  sort: number
  /** 是否隐藏 */
  hidden: boolean
  /** 子菜单 */
  children?: MenuItem[]
}

// ==================== 应用状态类型 ====================

/** 应用全局状态（H5 端仅需 locale，无 sidebarCollapsed） */
export interface AppState {
  /** 当前语言 */
  locale: string
}

// ==================== 扩展 vue-router RouteMeta ====================

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题（导航栏、Tab 栏） */
    title?: string
    /** 是否需要认证（默认 true） */
    requiresAuth?: boolean
    /** 是否为底部 Tab 栏页面 */
    tabBar?: boolean
    /** 是否在菜单中隐藏 */
    hidden?: boolean
  }
}

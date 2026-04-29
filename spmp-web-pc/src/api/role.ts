/**
 * 角色管理 API
 */
import request from '@/utils/request'

export interface RoleQuery {
  roleName?: string
  roleCode?: string
  status?: number
  pageNum: number
  pageSize: number
}

export interface RolePageItem {
  id: number
  roleName: string
  roleCode: string
  dataPermissionLevel: string
  status: number
  sort: number
  remark: string
  createTime: string
}

export interface RoleSimple {
  id: number
  roleName: string
  roleCode: string
}

export interface RoleCreateParams {
  roleName: string
  roleCode: string
  dataPermissionLevel: string
  sort: number
  remark?: string
}

export interface RoleUpdateParams {
  roleName?: string
  dataPermissionLevel?: string
  status?: number
  sort?: number
  remark?: string
}

export interface DataPermissionConfig {
  dataPermissionLevel: string
  dataIds: number[]
}

/** 角色分页查询 */
export function listRoles(params: RoleQuery) {
  return request.get('/user/roles', { params })
}

/** 角色全量列表 */
export function listAllRoles(): Promise<RoleSimple[]> {
  return request.get('/user/roles/list')
}

/** 新增角色 */
export function createRole(data: RoleCreateParams): Promise<void> {
  return request.post('/user/roles', data)
}

/** 编辑角色 */
export function updateRole(id: number, data: RoleUpdateParams): Promise<void> {
  return request.put(`/user/roles/${id}`, data)
}

/** 删除角色 */
export function deleteRole(id: number): Promise<void> {
  return request.delete(`/user/roles/${id}`)
}

/** 查询角色菜单 */
export function getRoleMenuIds(id: number): Promise<number[]> {
  return request.get(`/user/roles/${id}/menus`)
}

/** 分配菜单权限 */
export function assignMenus(id: number, menuIds: number[]): Promise<void> {
  return request.put(`/user/roles/${id}/menus`, menuIds)
}

/** 配置数据权限 */
export function configDataPermission(id: number, data: DataPermissionConfig): Promise<void> {
  return request.put(`/user/roles/${id}/data-permission`, data)
}

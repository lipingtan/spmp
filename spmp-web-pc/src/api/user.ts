/**
 * 用户管理 API
 */
import request from '@/utils/request'

export interface UserQuery {
  username?: string
  realName?: string
  phone?: string
  status?: number
  roleId?: number
  pageNum: number
  pageSize: number
}

export interface UserPageItem {
  id: number
  username: string
  realName: string
  phone: string
  status: number
  roles: string[]
  createTime: string
}

export interface UserCreateParams {
  username: string
  realName: string
  phone: string
  roleIds: number[]
}

export interface UserUpdateParams {
  realName?: string
  phone?: string
  roleIds?: number[]
  status?: number
}

/** 用户分页查询 */
export function listUsers(params: UserQuery) {
  return request.get('/user/users', { params })
}

/** 新增用户 */
export function createUser(data: UserCreateParams): Promise<void> {
  return request.post('/user/users', data)
}

/** 编辑用户 */
export function updateUser(id: number, data: UserUpdateParams): Promise<void> {
  return request.put(`/user/users/${id}`, data)
}

/** 删除用户 */
export function deleteUser(id: number): Promise<void> {
  return request.delete(`/user/users/${id}`)
}

/** 批量删除 */
export function batchDeleteUsers(ids: number[]): Promise<void> {
  return request.delete('/user/users/batch', { data: ids })
}

/** 状态切换 */
export function updateUserStatus(id: number, status: number): Promise<void> {
  return request.put(`/user/users/${id}/status?status=${status}`)
}

/** 重置密码 */
export function resetPassword(id: number): Promise<void> {
  return request.put(`/user/users/${id}/reset-password`)
}

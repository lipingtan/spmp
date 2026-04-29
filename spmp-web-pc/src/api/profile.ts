/**
 * 个人中心 API
 */
import request from '@/utils/request'

export interface ProfileInfo {
  userId: number
  username: string
  realName: string
  phone: string
  avatar: string
  roles: string[]
  permissions: string[]
  dataPermissionLevel: string
}

export interface ProfileUpdateParams {
  realName?: string
  phone?: string
}

export interface PasswordUpdateParams {
  oldPassword: string
  newPassword: string
}

/** 获取个人信息 */
export function getProfile(): Promise<ProfileInfo> {
  return request.get('/user/profile')
}

/** 修改个人信息 */
export function updateProfile(data: ProfileUpdateParams): Promise<void> {
  return request.put('/user/profile', data)
}

/** 修改密码 */
export function updatePassword(data: PasswordUpdateParams): Promise<void> {
  return request.put('/user/profile/password', data)
}

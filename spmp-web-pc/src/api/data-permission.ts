/**
 * 数据权限 API
 */
import request from '@/utils/request'

export interface OptionItem {
  id: number
  name: string
}

export interface DataPermissionOptions {
  areas: OptionItem[]
  communities: OptionItem[]
  buildings: OptionItem[]
}

/** 获取数据权限可选范围 */
export function getDataPermissionOptions(): Promise<DataPermissionOptions> {
  return request.get('/user/data-permission/options')
}

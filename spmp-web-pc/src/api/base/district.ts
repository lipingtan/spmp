/**
 * 片区管理 API
 */
import request from '@/utils/request'

/** 片区查询参数 */
export interface DistrictQuery {
  districtName?: string
  districtCode?: string
  status?: number
  pageNum: number
  pageSize: number
}

/** 片区分页列表项 */
export interface DistrictPageItem {
  id: number
  districtName: string
  districtCode: string
  status: number
  remark: string
  communityCount: number
  createTime: string
  updateTime: string
}

/** 片区新增参数 */
export interface DistrictCreateParams {
  districtName: string
  remark?: string
}

/** 片区编辑参数 */
export interface DistrictUpdateParams {
  districtName: string
  remark?: string
}

/** 片区分页查询 */
export function listDistricts(params: DistrictQuery) {
  return request.get('/base/districts', { params })
}

/** 新增片区 */
export function createDistrict(data: DistrictCreateParams): Promise<void> {
  return request.post('/base/districts', data)
}

/** 编辑片区 */
export function updateDistrict(id: number, data: DistrictUpdateParams): Promise<void> {
  return request.put(`/base/districts/${id}`, data)
}

/** 删除片区 */
export function deleteDistrict(id: number): Promise<void> {
  return request.delete(`/base/districts/${id}`)
}

/** 停用/启用片区 */
export function changeDistrictStatus(id: number, status: number): Promise<void> {
  return request.put(`/base/districts/${id}/status`, { status })
}

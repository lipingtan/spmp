/**
 * 小区管理 API
 */
import request from '@/utils/request'

/** 小区查询参数 */
export interface CommunityQuery {
  communityName?: string
  communityCode?: string
  districtId?: number
  status?: number
  pageNum: number
  pageSize: number
}

/** 小区分页列表项 */
export interface CommunityPageItem {
  id: number
  communityName: string
  communityCode: string
  address: string
  districtId: number
  districtName?: string
  phone: string
  propertyCompany: string
  area: number
  houseCount: number
  status: number
  remark: string
  createTime: string
}

/** 小区新增参数 */
export interface CommunityCreateParams {
  communityName: string
  communityCode?: string
  address: string
  districtId: number
  phone?: string
  propertyCompany?: string
  area?: number
  longitude?: number
  latitude?: number
  image?: string
  remark?: string
}

/** 小区编辑参数 */
export interface CommunityUpdateParams {
  communityName: string
  address: string
  phone?: string
  propertyCompany?: string
  area?: number
  longitude?: number
  latitude?: number
  image?: string
  remark?: string
}

/** 导入结果 */
export interface ImportResult {
  totalCount: number
  successCount: number
  failCount: number
  errorMessages?: string[]
}

/** 小区分页查询 */
export function listCommunities(params: CommunityQuery) {
  return request.get('/base/communities', { params })
}

/** 新增小区 */
export function createCommunity(data: CommunityCreateParams): Promise<void> {
  return request.post('/base/communities', data)
}

/** 编辑小区 */
export function updateCommunity(id: number, data: CommunityUpdateParams): Promise<void> {
  return request.put(`/base/communities/${id}`, data)
}

/** 删除小区 */
export function deleteCommunity(id: number): Promise<void> {
  return request.delete(`/base/communities/${id}`)
}

/** 停用/启用小区 */
export function changeCommunityStatus(id: number, status: number): Promise<void> {
  return request.put(`/base/communities/${id}/status`, { status })
}

/** Excel 导入小区 */
export function importCommunities(file: File): Promise<ImportResult> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/base/communities/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 下载小区导入模板 */
export function downloadCommunityTemplate() {
  return request.get('/base/communities/import-template', { responseType: 'blob' })
}

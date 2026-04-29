/**
 * 房屋管理 API
 */
import request from '@/utils/request'
import type { ImportResult } from './community'

/** 房屋查询参数 */
export interface HouseQuery {
  houseCode?: string
  communityId?: number
  buildingId?: number
  unitId?: number
  floor?: number
  houseStatus?: string
  houseType?: string
  pageNum: number
  pageSize: number
}

/** 房屋分页列表项 */
export interface HousePageItem {
  id: number
  houseCode: string
  unitId: number
  unitName?: string
  buildingId?: number
  buildingName?: string
  communityId?: number
  communityName?: string
  floor: number
  buildingArea: number
  usableArea: number
  houseStatus: string
  houseType: string
  remark: string
  createTime: string
}

/** 房屋新增参数 */
export interface HouseCreateParams {
  houseCode: string
  unitId: number
  floor: number
  buildingArea: number
  usableArea?: number
  houseStatus: string
  houseType: string
  remark?: string
}

/** 房屋编辑参数 */
export interface HouseUpdateParams {
  houseCode: string
  floor: number
  buildingArea: number
  usableArea?: number
  houseType: string
  remark?: string
}

/** 房屋状态变更参数 */
export interface HouseStatusChangeParams {
  newStatus: string
}

/** 房屋状态变更历史项 */
export interface HouseStatusLog {
  id: number
  houseId: number
  oldStatus: string
  newStatus: string
  changeTime: string
  operatorId: number
}

/** 房屋分页查询 */
export function listHouses(params: HouseQuery) {
  return request.get('/base/houses', { params })
}

/** 新增房屋 */
export function createHouse(data: HouseCreateParams): Promise<void> {
  return request.post('/base/houses', data)
}

/** 编辑房屋 */
export function updateHouse(id: number, data: HouseUpdateParams): Promise<void> {
  return request.put(`/base/houses/${id}`, data)
}

/** 删除房屋 */
export function deleteHouse(id: number): Promise<void> {
  return request.delete(`/base/houses/${id}`)
}

/** 房屋状态变更 */
export function changeHouseStatus(id: number, data: HouseStatusChangeParams): Promise<void> {
  return request.put(`/base/houses/${id}/status`, data)
}

/** 查询房屋状态变更历史 */
export function getHouseStatusLogs(id: number): Promise<HouseStatusLog[]> {
  return request.get(`/base/houses/${id}/status-logs`)
}

/** Excel 导入房屋 */
export function importHouses(file: File): Promise<ImportResult> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/base/houses/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 下载房屋导入模板 */
export function downloadHouseTemplate() {
  return request.get('/base/houses/import-template', { responseType: 'blob' })
}

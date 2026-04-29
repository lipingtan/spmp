/**
 * 楼栋管理 API
 */
import request from '@/utils/request'
import type { ImportResult } from './community'

/** 楼栋查询参数 */
export interface BuildingQuery {
  buildingName?: string
  buildingCode?: string
  communityId?: number
  buildingType?: string
  status?: number
  pageNum: number
  pageSize: number
}

/** 楼栋分页列表项 */
export interface BuildingPageItem {
  id: number
  buildingName: string
  buildingCode: string
  communityId: number
  communityName?: string
  aboveGroundFloors: number
  undergroundFloors: number
  unitsPerFloor: number
  buildingType: string
  status: number
  remark: string
  createTime: string
}

/** 楼栋新增参数 */
export interface BuildingCreateParams {
  buildingName: string
  buildingCode: string
  communityId: number
  aboveGroundFloors: number
  undergroundFloors: number
  unitsPerFloor: number
  buildingType: string
  remark?: string
}

/** 楼栋编辑参数 */
export interface BuildingUpdateParams {
  buildingName: string
  buildingCode: string
  aboveGroundFloors: number
  undergroundFloors: number
  unitsPerFloor: number
  buildingType: string
  remark?: string
}

/** 楼栋分页查询 */
export function listBuildings(params: BuildingQuery) {
  return request.get('/base/buildings', { params })
}

/** 新增楼栋 */
export function createBuilding(data: BuildingCreateParams): Promise<void> {
  return request.post('/base/buildings', data)
}

/** 编辑楼栋 */
export function updateBuilding(id: number, data: BuildingUpdateParams): Promise<void> {
  return request.put(`/base/buildings/${id}`, data)
}

/** 删除楼栋 */
export function deleteBuilding(id: number): Promise<void> {
  return request.delete(`/base/buildings/${id}`)
}

/** 停用/启用楼栋 */
export function changeBuildingStatus(id: number, status: number): Promise<void> {
  return request.put(`/base/buildings/${id}/status`, { status })
}

/** Excel 导入楼栋 */
export function importBuildings(file: File): Promise<ImportResult> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/base/buildings/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 下载楼栋导入模板 */
export function downloadBuildingTemplate() {
  return request.get('/base/buildings/import-template', { responseType: 'blob' })
}

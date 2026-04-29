/**
 * 单元管理 API
 */
import request from '@/utils/request'
import type { ImportResult } from './community'

/** 单元查询参数 */
export interface UnitQuery {
  unitName?: string
  unitCode?: string
  buildingId?: number
  status?: number
  pageNum: number
  pageSize: number
}

/** 单元分页列表项 */
export interface UnitPageItem {
  id: number
  unitName: string
  unitCode: string
  buildingId: number
  buildingName?: string
  status: number
  remark: string
  createTime: string
}

/** 单元新增参数 */
export interface UnitCreateParams {
  unitName: string
  unitCode: string
  buildingId: number
  remark?: string
}

/** 单元编辑参数 */
export interface UnitUpdateParams {
  unitName: string
  unitCode: string
  remark?: string
}

/** 单元分页查询 */
export function listUnits(params: UnitQuery) {
  return request.get('/base/units', { params })
}

/** 新增单元 */
export function createUnit(data: UnitCreateParams): Promise<void> {
  return request.post('/base/units', data)
}

/** 编辑单元 */
export function updateUnit(id: number, data: UnitUpdateParams): Promise<void> {
  return request.put(`/base/units/${id}`, data)
}

/** 删除单元 */
export function deleteUnit(id: number): Promise<void> {
  return request.delete(`/base/units/${id}`)
}

/** 停用/启用单元 */
export function changeUnitStatus(id: number, status: number): Promise<void> {
  return request.put(`/base/units/${id}/status`, { status })
}

/** Excel 导入单元 */
export function importUnits(file: File): Promise<ImportResult> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/base/units/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 下载单元导入模板 */
export function downloadUnitTemplate() {
  return request.get('/base/units/import-template', { responseType: 'blob' })
}

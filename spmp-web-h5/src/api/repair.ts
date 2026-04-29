/**
 * 维修人员端 API（H5）
 * 工作台/待处理/历史/接单/完成/转派
 */
import request from '@/utils/request'

// ==================== DTO 类型 ====================

/** 完成维修参数 */
export interface WorkOrderCompleteDTO {
  repairDescription: string
  repairDuration: number
  imageUrls?: string[]
  materials?: RepairMaterialDTO[]
}

/** 维修材料参数 */
export interface RepairMaterialDTO {
  materialName: string
  quantity: number
  unit?: string
  unitPrice: number
}

/** H5 工单查询参数 */
export interface H5WorkOrderQueryDTO {
  status?: string
  pageNum: number
  pageSize: number
}

// ==================== VO 类型 ====================

/** 工作台 VO */
export interface RepairDashboardVO {
  todayPendingCount: number
  monthlyCompletedCount: number
}

/** 工单简要信息 */
export interface WorkOrderSimpleVO {
  id: number
  orderNo: string
  orderType: string
  status: string
  description: string
  addressDesc: string
  createTime: string
  actualCompleteTime: string | null
}

// ==================== API 方法 ====================

/** 工作台 */
export function getDashboard(): Promise<RepairDashboardVO> {
  return request.get('/workorder/h5/repair/dashboard')
}

/** 工单详情（维修人员端） */
export function getRepairOrderDetail(id: number) {
  return request.get(`/workorder/h5/repair/orders/${id}`)
}

/** 待处理工单列表 */
export function listPendingOrders(params: H5WorkOrderQueryDTO) {
  return request.get('/workorder/h5/repair/pending', { params })
}

/** 历史工单列表 */
export function listHistoryOrders(params: H5WorkOrderQueryDTO) {
  return request.get('/workorder/h5/repair/history', { params })
}

/** 接单 */
export function acceptWorkOrder(id: number): Promise<void> {
  return request.put(`/workorder/h5/repair/orders/${id}/accept`)
}

/** 完成维修 */
export function completeWorkOrder(id: number, data: WorkOrderCompleteDTO): Promise<void> {
  return request.put(`/workorder/h5/repair/orders/${id}/complete`, data)
}

/** 转派工单 */
export function transferWorkOrder(id: number, reason: string): Promise<void> {
  return request.put(`/workorder/h5/repair/orders/${id}/transfer`, null, { params: { reason } })
}

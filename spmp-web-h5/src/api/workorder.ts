/**
 * 工单管理 API（H5 业主端）
 * 提交报修/我的工单/详情/验收/评价/催单/取消
 */
import request from '@/utils/request'

// ==================== DTO 类型 ====================

/** 提交报修参数 */
export interface WorkOrderCreateDTO {
  orderType: string
  description: string
  addressType: string
  communityId: number
  houseId?: number
  buildingId?: number
  unitId?: number
  imageUrls?: string[]
}

/** 验收参数 */
export interface WorkOrderVerifyDTO {
  passed: boolean
  rejectReason?: string
  rejectImageUrls?: string[]
  score?: number
  evaluateContent?: string
}

/** 评价参数 */
export interface WorkOrderEvaluateDTO {
  score: number
  content?: string
}

/** 取消参数 */
export interface WorkOrderCancelDTO {
  cancelReason: string
}

/** H5 工单查询参数 */
export interface H5WorkOrderQueryDTO {
  status?: string
  pageNum: number
  pageSize: number
}

// ==================== VO 类型 ====================

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

/** 工单详情 */
export interface WorkOrderDetailVO {
  id: number
  orderNo: string
  orderType: string
  addressType: string
  communityId: number
  communityName: string
  houseId: number | null
  houseName: string | null
  buildingId: number | null
  buildingName: string | null
  reporterId: number
  reporterName: string
  description: string
  status: string
  repairUserId: number | null
  repairUserName: string | null
  rejectCount: number
  urgeCount: number
  expectedCompleteTime: string | null
  actualStartTime: string | null
  actualCompleteTime: string | null
  repairDuration: number | null
  cancelReason: string | null
  createTime: string
  reportImages: string[]
  dispatchRecords: DispatchRecordVO[]
  materials: RepairMaterialVO[]
  evaluation: EvaluationVO | null
  logs: WorkOrderLogVO[]
}

/** 派发记录 */
export interface DispatchRecordVO {
  id: number
  repairUserId: number
  repairUserName: string
  dispatchType: string
  remark: string | null
  dispatchTime: string
}

/** 维修材料 */
export interface RepairMaterialVO {
  id: number
  materialName: string
  quantity: number
  unit: string | null
  unitPrice: number
  totalPrice: number
}

/** 评价信息 */
export interface EvaluationVO {
  id: number
  score: number
  content: string | null
  evaluateType: string
}

/** 操作日志 */
export interface WorkOrderLogVO {
  id: number
  action: string
  fromStatus: string | null
  toStatus: string | null
  operatorName: string | null
  remark: string | null
  operateTime: string
}

// ==================== API 方法 ====================

/** 提交报修 */
export function createWorkOrder(data: WorkOrderCreateDTO): Promise<number> {
  return request.post('/workorder/h5/orders', data)
}

/** 我的工单列表 */
export function listMyWorkOrders(params: H5WorkOrderQueryDTO) {
  return request.get('/workorder/h5/orders/mine', { params })
}

/** 工单详情 */
export function getWorkOrderDetail(id: number): Promise<WorkOrderDetailVO> {
  return request.get(`/workorder/h5/orders/${id}`)
}

/** 验收工单 */
export function verifyWorkOrder(id: number, data: WorkOrderVerifyDTO): Promise<void> {
  return request.put(`/workorder/h5/orders/${id}/verify`, data)
}

/** 评价工单 */
export function evaluateWorkOrder(id: number, data: WorkOrderEvaluateDTO): Promise<void> {
  return request.post(`/workorder/h5/orders/${id}/evaluate`, data)
}

/** 催单 */
export function urgeWorkOrder(id: number): Promise<void> {
  return request.post(`/workorder/h5/orders/${id}/urge`)
}

/** 取消工单 */
export function cancelWorkOrder(id: number, data: WorkOrderCancelDTO): Promise<void> {
  return request.put(`/workorder/h5/orders/${id}/cancel`, data)
}

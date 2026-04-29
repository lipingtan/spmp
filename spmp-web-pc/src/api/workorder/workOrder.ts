/**
 * 工单管理 API
 * 工单列表/详情/派发/取消/统计/维修人员列表
 */
import request from '@/utils/request'

// ==================== DTO 类型 ====================

/** 工单分页查询参数 */
export interface WorkOrderQueryDTO {
  status?: string
  orderType?: string
  communityId?: number
  buildingId?: number
  startDate?: string
  endDate?: string
  keyword?: string
  pageNum: number
  pageSize: number
}

/** 派发工单参数 */
export interface WorkOrderDispatchDTO {
  repairUserId: number
  remark?: string
  expectedCompleteTime?: string
}

/** 取消/关闭工单参数 */
export interface WorkOrderCancelDTO {
  cancelReason: string
  cancelType?: string
}

/** 统计查询参数 */
export interface StatisticsQueryDTO {
  timeRange?: string
  startDate?: string
  endDate?: string
  communityId?: number
  buildingId?: number
}

// ==================== VO 类型 ====================

/** 工单列表 VO */
export interface WorkOrderListVO {
  id: number
  orderNo: string
  orderType: string
  addressType: string
  communityId: number
  reporterName: string
  status: string
  repairUserId: number | null
  rejectCount: number
  urgeCount: number
  createTime: string
  actualCompleteTime: string | null
  repairDuration: number | null
}

/** 工单详情 VO */
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
  unitId: number | null
  unitName: string | null
  reporterId: number
  reporterName: string
  reporterPhone: string
  description: string
  status: string
  repairUserId: number | null
  repairUserName: string | null
  rejectCount: number
  urgeCount: number
  lastUrgeTime: string | null
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

/** 派发记录 VO */
export interface DispatchRecordVO {
  id: number
  repairUserId: number
  repairUserName: string
  dispatchType: string
  dispatcherName: string | null
  remark: string | null
  dispatchTime: string
}

/** 维修材料 VO */
export interface RepairMaterialVO {
  id: number
  materialName: string
  quantity: number
  unit: string | null
  unitPrice: number
  totalPrice: number
}

/** 评价 VO */
export interface EvaluationVO {
  id: number
  score: number
  content: string | null
  evaluatorId: number
  evaluateTime: string
  evaluateType: string
}

/** 操作日志 VO */
export interface WorkOrderLogVO {
  id: number
  action: string
  fromStatus: string | null
  toStatus: string | null
  operatorId: number | null
  operatorName: string | null
  operatorType: string | null
  remark: string | null
  operateTime: string
}

/** 统计看板 VO */
export interface StatisticsVO {
  pendingCount: number
  inProgressCount: number
  monthlyCompletedCount: number
  avgRepairDuration: number
  avgScore: number
  dailyTrend: TrendDataVO[]
  monthlyScoreTrend: TrendDataVO[]
}

/** 趋势图数据 VO */
export interface TrendDataVO {
  date: string
  value: number
}

/** 维修人员选项 VO */
export interface RepairStaffVO {
  userId: number
  realName: string
  phone: string
  currentWorkload: number
}

// ==================== API 方法 ====================

/** 工单分页查询 */
export function listWorkOrders(params: WorkOrderQueryDTO) {
  return request.get('/workorder/orders', { params })
}

/** 工单详情 */
export function getWorkOrderDetail(id: number): Promise<WorkOrderDetailVO> {
  return request.get(`/workorder/orders/${id}`)
}

/** 派发工单 */
export function dispatchWorkOrder(id: number, data: WorkOrderDispatchDTO): Promise<void> {
  return request.put(`/workorder/orders/${id}/dispatch`, data)
}

/** 取消/关闭工单 */
export function cancelWorkOrder(id: number, data: WorkOrderCancelDTO): Promise<void> {
  return request.put(`/workorder/orders/${id}/cancel`, data)
}

/** 统计看板 */
export function getStatistics(params: StatisticsQueryDTO): Promise<StatisticsVO> {
  return request.get('/workorder/statistics', { params })
}

/** 维修人员列表 */
export function listRepairStaff(communityId?: number): Promise<RepairStaffVO[]> {
  return request.get('/workorder/orders/staff', { params: { communityId } })
}

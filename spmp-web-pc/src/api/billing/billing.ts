import request from '@/utils/request'

export interface BillQueryDTO {
  status?: string
  feeType?: string
  communityId?: number
  buildingId?: number
  billingPeriod?: string
  keyword?: string
  pageNum: number
  pageSize: number
}

export interface BillListVO {
  id: number
  billNo: string
  feeType: string
  feeTypeName?: string
  amount: number
  reduceAmount: number
  paidAmount: number
  paidTime?: string
  status: string
  statusName?: string
  ownerName: string
  billingPeriod: string
  dueDate: string
}

export interface StatisticsVO {
  totalReceivable: number
  totalReceived: number
  collectionRate: number
  overdueAmount: number
  overdueCount: number
  reduceAmount: number
  trends: Array<{ period: string; receivable: number; received: number; collectionRate: number }>
}

export interface BillDetailVO extends BillListVO {
  batchNo?: string
  billingMethod?: string
  refundAmount?: number
  houseId?: number
  houseFullName?: string
  communityId?: number
  communityName?: string
  buildingId?: number
  buildingName?: string
  usageAmount?: number
  unitPrice?: number
  houseArea?: number
  paidTime?: string
  remark?: string
}

export interface BillGenerateDTO {
  feeType: string
  billingPeriod: string
  buildingId?: number
  unitId?: number
}

export interface BillCreateDTO {
  ownerId: number
  ownerName?: string
  houseId: number
  buildingId?: number
  communityId: number
  feeType: string
  billingPeriod: string
  amount: number
  usageAmount?: number
  unitPrice?: number
  houseArea?: number
  dueDate?: string
  remark?: string
}

export interface BillReduceDTO {
  reduceAmount: number
  reason: string
}

export interface BillRefundDTO {
  refundAmount: number
  refundReason: string
}

export interface BillCancelDTO {
  cancelReason: string
}

export function listBills(params: BillQueryDTO) {
  return request.get('/billing/bills', { params })
}

export function getBillDetail(id: number) {
  return request.get<any, BillDetailVO>(`/billing/bills/${id}`)
}

export function generatePreview(data: BillGenerateDTO) {
  return request.post('/billing/bills/generate/preview', data)
}

export function generateBills(data: BillGenerateDTO) {
  return request.post<any, string>('/billing/bills/generate', data)
}

export function getGenerateProgress(batchNo: string) {
  return request.get('/billing/bills/generate/progress/' + batchNo)
}

export function regenerateBills(batchNo: string, data: BillGenerateDTO) {
  return request.post('/billing/bills/regenerate/' + batchNo, data)
}

export function createBill(data: BillCreateDTO) {
  return request.post<any, number>('/billing/bills', data)
}

export function applyReduce(id: number, data: BillReduceDTO) {
  return request.post(`/billing/bills/${id}/reduce`, data)
}

export function approveReduce(id: number, data: { approved: boolean; approveRemark?: string }) {
  return request.put(`/billing/bills/${id}/reduce/approve`, data)
}

export function revokeReduce(id: number) {
  return request.put(`/billing/bills/${id}/reduce/revoke`)
}

export function refundBill(id: number, data: BillRefundDTO) {
  return request.post(`/billing/bills/${id}/refund`, data)
}

export function cancelBill(id: number, data: BillCancelDTO) {
  return request.put(`/billing/bills/${id}/cancel`, data)
}

export function exportBills(params: Record<string, any>) {
  return request.get('/billing/bills/export', { params })
}

export function getBillStatistics(params: Record<string, any>) {
  return request.get<any, StatisticsVO>('/billing/statistics', { params })
}

export function listFeeConfigs(params: Record<string, any>) {
  return request.get('/billing/config', { params })
}

export function listOverdueBills(params: Record<string, any>) {
  return request.get('/billing/overdue/bills', { params })
}

export function urgeBills(data: { billIds: number[]; urgeType: string; feedback?: string }) {
  return request.post('/billing/overdue/urge', data)
}

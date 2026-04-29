import request from '@/utils/request'

export interface H5BillQuery {
  status?: string
  feeType?: string
  pageNum?: number
  pageSize?: number
}

export interface PaymentVO {
  id: number
  paymentNo: string
  totalAmount: number
  payMethod: string
  payMethodName?: string
  status: string
  statusName?: string
  paidTime?: string
  createTime?: string
}

export interface ReduceRecordVO {
  id: number
  reduceAmount: number
  reason?: string
  approveTime?: string
}

export interface H5BillListVO {
  id: number
  billNo: string
  feeType: string
  feeTypeName: string
  amount: number
  reduceAmount: number
  payAmount: number
  status: string
  statusName: string
  houseFullName?: string
  billingPeriod: string
  dueDate: string
  overdue: boolean
}

export interface H5BillDetailVO {
  id: number
  billNo: string
  feeType: string
  feeTypeName: string
  amount: number
  reduceAmount: number
  payAmount: number
  status: string
  statusName: string
  houseFullName?: string
  billingPeriod: string
  dueDate: string
  usageAmount?: number
  unitPrice?: number
  houseArea?: number
  paidTime?: string
  createTime?: string
  remark?: string
  payments?: PaymentVO[]
  reduceRecords?: ReduceRecordVO[]
}

export function getMyBills(params: H5BillQuery) {
  return request.get<any, any>('/billing/h5/bills/mine', { params })
}

export function getBillDetail(id: number) {
  return request.get<any, H5BillDetailVO>(`/billing/h5/bills/${id}`)
}

export function createPayment(billIds: number[]) {
  return request.post<any, number>('/billing/h5/payments', { billIds, payMethod: 'MOCK' })
}

export function paymentCallback(paymentId: number, callbackSign?: string) {
  return request.post<any, void>('/billing/h5/payments/callback', null, {
    params: { paymentId, callbackSign }
  })
}

export function getPaymentHistory(params: { pageNum: number; pageSize: number }) {
  return request.get<any, any>('/billing/h5/payments/history', { params })
}

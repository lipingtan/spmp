import request from '@/utils/request'

export type ModuleStatus = 'SUCCESS' | 'NO_PERMISSION' | 'ERROR' | 'EMPTY'
export type TimeRange = 'MONTH' | 'QUARTER' | 'YEAR'

export interface ModuleResponse<T> {
  status: ModuleStatus
  message: string
  data: T
}

export interface DashboardKpi {
  pendingCount: number
  inProgressCount: number
  monthlyCompletedCount: number
  avgRepairDuration: number
  totalReceivable: number
  totalReceived: number
  collectionRate: number
  overdueAmount: number
  recentNoticeCount: number
}

export interface WorkorderTrendPoint {
  date: string
  value: number
}

export interface BillingTrendPoint {
  period: string
  receivable: number
  received: number
  collectionRate: number
}

export function getDashboardKpi(timeRange: TimeRange) {
  return request.get('/dashboard/kpi', { params: { timeRange } }) as Promise<ModuleResponse<DashboardKpi>>
}

export function getDashboardWorkorderTrend(timeRange: TimeRange) {
  return request.get('/dashboard/trend/workorder', { params: { timeRange } }) as Promise<ModuleResponse<WorkorderTrendPoint[]>>
}

export function getDashboardBillingTrend(timeRange: TimeRange) {
  return request.get('/dashboard/trend/billing', { params: { timeRange } }) as Promise<ModuleResponse<BillingTrendPoint[]>>
}


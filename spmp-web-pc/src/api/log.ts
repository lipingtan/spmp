/**
 * 日志 API
 */
import request from '@/utils/request'

export interface LoginLogQuery {
  username?: string
  loginIp?: string
  loginResult?: number
  startTime?: string
  endTime?: string
  pageNum: number
  pageSize: number
}

export interface LoginLogItem {
  id: number
  username: string
  loginIp: string
  loginLocation: string
  browser: string
  os: string
  loginTime: string
  loginResult: number
  failReason: string
}

export interface OperationLogQuery {
  operatorName?: string
  module?: string
  operationType?: string
  startTime?: string
  endTime?: string
  pageNum: number
  pageSize: number
}

export interface OperationLogItem {
  id: number
  operatorId: number
  operatorName: string
  module: string
  operationType: string
  description: string
  requestMethod: string
  requestUrl: string
  requestParams: string
  responseResult: string
  operationIp: string
  operationTime: string
  costTime: number
}

/** 登录日志查询 */
export function listLoginLogs(params: LoginLogQuery) {
  return request.get('/user/login-logs', { params })
}

/** 操作日志查询 */
export function listOperationLogs(params: OperationLogQuery) {
  return request.get('/user/operation-logs', { params })
}

/**
 * 部署初始化 API
 */

import request from '@/utils/request'

/** 初始化配置 */
export interface InitConfig {
  database: {
    host: string
    port: number
    databaseName: string
    username: string
    password: string
  }
  redis: {
    host: string
    port: number
    password: string
    database: number
  }
}

/** 初始化状态 */
export interface InitStatus {
  initialized: boolean
}

/** 连通性测试结果 */
export interface ConnectionTestResult {
  databaseConnected: boolean
  databaseMessage: string
  redisConnected: boolean
  redisMessage: string
}

/** 初始化结果 */
export interface InitResult {
  success: boolean
  message: string
  modeSwitched: boolean
  summary: {
    databaseHost: string
    databaseName: string
    redisHost: string
    scriptsExecuted: number
    scriptNames: string[]
  }
}

/** 检查初始化状态 */
export function checkInitStatus(): Promise<InitStatus> {
  return request.get('/init/status')
}

/** 测试连通性 */
export function testConnection(config: InitConfig): Promise<ConnectionTestResult> {
  return request.post('/init/test-connection', config)
}

/** 执行初始化 */
export function executeInit(config: InitConfig): Promise<InitResult> {
  return request.post('/init/execute', config)
}

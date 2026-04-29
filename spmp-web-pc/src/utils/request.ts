/**
 * Axios 请求封装
 * - 创建 Axios 实例（baseURL 从环境变量读取、超时 15000ms）
 * - 请求拦截器：注入 Token
 * - 响应拦截器：统一错误处理、401 跳转登录页
 */

import axios from 'axios'
import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'

/** 创建 Axios 实例 */
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

/** 请求拦截器：从 localStorage 读取 Token 注入 Authorization 头 */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/** 响应拦截器：检查业务状态码、处理 401 */
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    // 业务状态码非 200，提示错误信息
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 分页响应：包含 total 字段时返回完整对象（保留分页元数据）
    if ((res as any).total !== undefined) {
      return res as any
    }
    return res.data as any
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token 过期或未登录，清除 Token 并跳转登录页
      localStorage.removeItem('access_token')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service

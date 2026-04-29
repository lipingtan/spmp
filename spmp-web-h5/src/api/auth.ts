/**
 * 认证 API（H5 端）
 * 业主手机号密码登录、短信验证码登录
 */
import request from '@/utils/request'

// ==================== 类型定义 ====================

export interface LoginParams {
  username: string
  password: string
  captchaCode: string
  captchaKey: string
  clientType: string
}

export interface SmsLoginParams {
  phone: string
  smsCode: string
  clientType: string
}

export interface TokenResult {
  accessToken: string
  refreshToken: string
  userId: number
  username: string
  realName: string
  avatar: string
}

export interface CaptchaResult {
  captchaKey: string
  captchaImage: string
}

// ==================== API 方法 ====================

/** 获取图形验证码 */
export function getCaptcha(): Promise<CaptchaResult> {
  return request.get('/user/auth/captcha')
}

/** 用户名密码登录 */
export function login(data: LoginParams): Promise<TokenResult> {
  return request.post('/user/auth/login', data)
}

/** 手机号短信验证码登录 */
export function loginBySms(data: SmsLoginParams): Promise<TokenResult> {
  return request.post('/user/auth/login/sms', data)
}

/** 发送短信验证码 */
export function sendSmsCode(phone: string): Promise<void> {
  return request.post(`/user/auth/sms-code?phone=${phone}`)
}

/** 登出 */
export function logout(): Promise<void> {
  return request.post('/user/auth/logout')
}

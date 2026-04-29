/**
 * 业主 API（H5 端）
 * 注册、个人信息接口
 */
import request from '@/utils/request'

// ==================== DTO 类型 ====================

/** H5 注册参数 */
export interface H5RegisterDTO {
  /** 手机号（必填） */
  phone: string
  /** 密码（必填） */
  password: string
  /** 业主姓名（必填） */
  ownerName: string
  /** 身份证号（可选） */
  idCard?: string
  /** 验证码（演示用，固定 123456 通过） */
  captcha: string
}

// ==================== VO 类型 ====================

/** 房产绑定简要信息 */
export interface PropertyBindingItem {
  id: number
  /** 小区名称 */
  communityName: string
  /** 楼栋名称 */
  buildingName: string
  /** 单元名称 */
  unitName: string
  /** 房屋编号 */
  houseName: string
  /** 关系类型：OWNER/TENANT/FAMILY */
  relationType: string
  /** 绑定时间 */
  bindingTime: string
}

/** H5 个人信息 VO */
export interface H5ProfileVO {
  id: number
  /** 姓名 */
  name: string
  /** 手机号（脱敏） */
  phoneMasked: string
  /** 身份证号（脱敏） */
  idCardMasked: string
  /** 性别：0-未知 1-男 2-女 */
  gender: number
  /** 头像 */
  avatar: string
  /** 邮箱 */
  email: string
  /** 业主状态：UNCERTIFIED/CERTIFYING/CERTIFIED/DISABLED */
  status: string
  /** 房产绑定列表 */
  propertyBindings: PropertyBindingItem[]
}

interface RawPropertyBindingItem {
  id?: number
  communityName?: string
  buildingName?: string
  unitName?: string
  houseName?: string
  houseCode?: string
  relationType?: string
  bindingTime?: string
}

interface RawH5ProfileVO {
  id?: number
  name?: string
  ownerName?: string
  phoneMasked?: string
  idCardMasked?: string
  gender?: number
  avatar?: string
  email?: string
  status?: string
  ownerStatus?: string
  propertyBindings?: RawPropertyBindingItem[]
  properties?: RawPropertyBindingItem[]
}

function normalizePropertyBindings(list?: RawPropertyBindingItem[]): PropertyBindingItem[] {
  if (!Array.isArray(list)) return []
  return list.map((item) => ({
    id: item.id || 0,
    communityName: item.communityName || '',
    buildingName: item.buildingName || '',
    unitName: item.unitName || '',
    houseName: item.houseName || item.houseCode || '',
    relationType: item.relationType || '',
    bindingTime: item.bindingTime || ''
  }))
}

function normalizeProfile(data: RawH5ProfileVO): H5ProfileVO {
  const bindings = data.propertyBindings || data.properties || []
  return {
    id: data.id || 0,
    name: data.name || data.ownerName || '',
    phoneMasked: data.phoneMasked || '',
    idCardMasked: data.idCardMasked || '',
    gender: data.gender ?? 0,
    avatar: data.avatar || '',
    email: data.email || '',
    status: data.status || data.ownerStatus || 'UNCERTIFIED',
    propertyBindings: normalizePropertyBindings(bindings)
  }
}

// ==================== API 方法 ====================

/** 业主注册 */
export function register(data: H5RegisterDTO): Promise<void> {
  return request.post('/owner/h5/register', data)
}

/** 获取个人信息 */
export function getProfile(): Promise<H5ProfileVO> {
  return request.get<any, RawH5ProfileVO>('/owner/h5/profile').then((res) => normalizeProfile(res))
}

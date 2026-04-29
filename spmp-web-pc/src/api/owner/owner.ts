/**
 * 业主管理 API
 * 业主 CRUD + 停用/启用接口
 */
import request from '@/utils/request'

// ==================== DTO 类型 ====================

/** 业主新增参数 */
export interface OwnerCreateDTO {
  /** 姓名（必填） */
  ownerName: string
  /** 手机号（必填） */
  phone: string
  /** 身份证号（必填） */
  idCard: string
  /** 性别：0-未知 1-男 2-女 */
  gender?: number
  /** 头像 */
  avatar?: string
  /** 邮箱 */
  email?: string
  /** 紧急联系人 */
  emergencyContact?: string
  /** 紧急联系电话 */
  emergencyPhone?: string
}

/** 业主编辑参数 */
export interface OwnerUpdateDTO {
  /** 姓名（必填） */
  ownerName: string
  /** 手机号（必填） */
  phone: string
  /** 身份证号（必填） */
  idCard: string
  /** 性别 */
  gender?: number
  /** 头像 */
  avatar?: string
  /** 邮箱 */
  email?: string
  /** 紧急联系人 */
  emergencyContact?: string
  /** 紧急联系电话 */
  emergencyPhone?: string
}

/** 业主分页查询参数 */
export interface OwnerPageDTO {
  /** 姓名（模糊查询） */
  name?: string
  /** 手机号（模糊查询） */
  phone?: string
  /** 小区 ID */
  communityId?: number
  /** 楼栋 ID */
  buildingId?: number
  /** 业主状态：UNCERTIFIED/CERTIFYING/CERTIFIED/DISABLED */
  status?: string
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
}

/** 业主停用/启用参数 */
export interface OwnerStatusDTO {
  /** 状态：DISABLED-停用 / 恢复原状态 */
  status: string
  /** 停用原因 */
  reason?: string
}

// ==================== VO 类型 ====================

/** 业主详情 VO */
export interface OwnerVO {
  id: number
  ownerName: string
  name: string
  phone: string
  phoneMasked: string
  idCard: string
  idCardMasked: string
  gender: number
  avatar: string
  email: string
  emergencyContact: string
  emergencyPhone: string
  emergencyPhoneMasked: string
  ownerStatus: string
  ownerSource: string
  userId: number | null
  createTime: string
  updateTime: string
}

/** 业主列表 VO */
export interface OwnerListVO {
  id: number
  ownerName: string
  phoneMasked: string
  idCardMasked: string
  gender: number
  ownerStatus: string
  ownerSource: string
  communityName: string
  buildingName: string
  unitName: string
  houseName: string
  createTime: string
}

// ==================== API 方法 ====================

/** 业主分页查询 */
export function listOwners(params: OwnerPageDTO) {
  return request.get('/owner/owners', { params })
}

/** 新增业主 */
export function createOwner(data: OwnerCreateDTO): Promise<void> {
  return request.post('/owner/owners', data)
}

/** 获取业主详情 */
export function getOwner(id: number): Promise<OwnerVO> {
  return request.get(`/owner/owners/${id}`)
}

/** 编辑业主 */
export function updateOwner(id: number, data: OwnerUpdateDTO): Promise<void> {
  return request.put(`/owner/owners/${id}`, data)
}

/** 删除业主 */
export function deleteOwner(id: number): Promise<void> {
  return request.delete(`/owner/owners/${id}`)
}

/** 停用/启用业主 */
export function changeOwnerStatus(id: number, data: OwnerStatusDTO): Promise<void> {
  return request.put(`/owner/owners/${id}/status`, data)
}

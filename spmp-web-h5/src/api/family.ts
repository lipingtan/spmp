/**
 * 家庭成员 API（H5 端）
 * 家庭成员列表、添加、删除接口
 */
import request from '@/utils/request'

// ==================== DTO 类型 ====================

/** 添加家庭成员参数 */
export interface FamilyMemberCreateDTO {
  /** 姓名（必填） */
  name: string
  /** 手机号（必填） */
  phone: string
  /** 身份证号（必填） */
  idCard: string
  /** 与业主关系（必填）：SPOUSE-配偶 / PARENT-父母 / CHILD-子女 / OTHER-其他 */
  relation: string
}

// ==================== VO 类型 ====================

/** 家庭成员 VO */
export interface FamilyMemberVO {
  id: number
  /** 姓名 */
  name: string
  /** 手机号（脱敏） */
  phoneMasked: string
  /** 身份证号（脱敏） */
  idCardMasked: string
  /** 与业主关系：SPOUSE/PARENT/CHILD/OTHER */
  relation: string
  /** 创建时间 */
  createTime: string
}

interface RawFamilyMemberCreateDTO {
  memberName: string
  phone: string
  idCard: string
  relation: string
}

interface RawFamilyMemberVO {
  id?: number
  name?: string
  memberName?: string
  phoneMasked?: string
  idCardMasked?: string
  relation?: string
  createTime?: string
}

function normalizeFamilyMember(item: RawFamilyMemberVO): FamilyMemberVO {
  return {
    id: item.id || 0,
    name: item.name || item.memberName || '',
    phoneMasked: item.phoneMasked || '',
    idCardMasked: item.idCardMasked || '',
    relation: item.relation || '',
    createTime: item.createTime || ''
  }
}

function toCreatePayload(data: FamilyMemberCreateDTO): RawFamilyMemberCreateDTO {
  return {
    memberName: data.name,
    phone: data.phone,
    idCard: data.idCard,
    relation: data.relation
  }
}

// ==================== API 方法 ====================

/** 获取家庭成员列表 */
export function listFamilyMembers(): Promise<FamilyMemberVO[]> {
  return request.get<any, RawFamilyMemberVO[]>('/owner/h5/family-members').then((res) => (Array.isArray(res) ? res.map(normalizeFamilyMember) : []))
}

/** 添加家庭成员 */
export function addFamilyMember(data: FamilyMemberCreateDTO): Promise<void> {
  return request.post('/owner/h5/family-members', toCreatePayload(data))
}

/** 删除家庭成员 */
export function deleteFamilyMember(id: number): Promise<void> {
  return request.delete(`/owner/h5/family-members/${id}`)
}

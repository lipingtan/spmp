/**
 * 房产绑定 API
 * 房产绑定/解绑接口
 */
import request from '@/utils/request'

// ==================== DTO 类型 ====================

/** 房产绑定参数 */
export interface PropertyBindingCreateDTO {
  /** 业主 ID */
  ownerId: number
  /** 房屋 ID */
  houseId: number
  /** 关系类型：OWNER-业主 / TENANT-租户 / FAMILY-家属 */
  relationType: string
}

// ==================== VO 类型 ====================

/** 房产绑定 VO */
export interface PropertyBindingVO {
  id: number
  ownerId: number
  houseId: number
  communityId: number
  communityName: string
  buildingId: number
  buildingName: string
  unitId: number
  unitName: string
  houseCode: string
  relationType: string
  bindTime: string
  unbindTime: string | null
}

// ==================== API 方法 ====================

/** 房产绑定 */
export function bindProperty(data: PropertyBindingCreateDTO): Promise<void> {
  return request.post('/owner/property-bindings', data)
}

/** 解除绑定 */
export function unbindProperty(id: number): Promise<void> {
  return request.delete(`/owner/property-bindings/${id}`)
}

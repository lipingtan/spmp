/**
 * 认证审批 API
 * 认证申请列表、单条审批、批量审批接口
 */
import request from '@/utils/request'

// ==================== DTO 类型 ====================

/** 认证申请分页查询参数 */
export interface CertificationQueryDTO {
  /** 认证状态：PENDING/APPROVED/REJECTED（对应后端 certStatus 字段） */
  certStatus?: string
  /** 小区 ID */
  communityId?: number
  /** 楼栋 ID */
  buildingId?: number
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
}

/** 单条审批参数 */
export interface CertificationApproveDTO {
  /** 审批动作：APPROVE-通过 / REJECT-驳回（对应后端 action 字段） */
  action: string
  /** 驳回原因（驳回时必填） */
  rejectReason?: string
}

/** 批量审批参数 */
export interface CertificationBatchApproveDTO {
  /** 认证申请 ID 列表 */
  ids: number[]
  /** 审批动作：APPROVE-通过 / REJECT-驳回（对应后端 action 字段） */
  action: string
  /** 驳回原因（驳回时必填） */
  rejectReason?: string
}

// ==================== VO 类型 ====================

/** 认证申请 VO */
export interface CertificationVO {
  id: number
  ownerId: number
  ownerName: string
  communityId: number
  communityName: string
  buildingId: number
  buildingName: string
  unitId: number
  unitName: string
  houseId: number
  houseName: string
  /** 认证状态（后端返回 certStatus，同时兼容 status 别名） */
  certStatus: string
  status: string
  rejectReason: string | null
  applyTime: string
  approveTime: string | null
  approverName: string | null
}

// ==================== API 方法 ====================

/** 认证申请分页查询 */
export function listCertifications(params: CertificationQueryDTO) {
  return request.get('/owner/certifications', { params })
}

/** 单条审批 */
export function approveCertification(id: number, data: CertificationApproveDTO): Promise<void> {
  return request.put(`/owner/certifications/${id}/approve`, data)
}

/** 批量审批 */
export function batchApproveCertifications(data: CertificationBatchApproveDTO): Promise<void> {
  return request.put('/owner/certifications/batch-approve', data)
}

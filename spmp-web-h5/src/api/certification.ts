/**
 * 认证 API（H5 端）
 * 提交认证申请、我的认证记录接口
 */
import request from '@/utils/request'

// ==================== DTO 类型 ====================

/** H5 提交认证申请参数 */
export interface H5CertificationCreateDTO {
  /** 小区 ID */
  communityId: number
  /** 楼栋 ID */
  buildingId: number
  /** 单元 ID */
  unitId: number
  /** 房屋 ID */
  houseId: number
}

// ==================== VO 类型 ====================

/** 认证记录 VO */
export interface CertificationVO {
  id: number
  /** 小区名称 */
  communityName: string
  /** 楼栋名称 */
  buildingName: string
  /** 单元名称 */
  unitName: string
  /** 房屋编号 */
  houseName: string
  /** 认证状态：PENDING-待审批 / APPROVED-已通过 / REJECTED-已驳回 */
  status: string
  /** 驳回原因 */
  rejectReason: string | null
  /** 申请时间 */
  applyTime: string
  /** 审批时间 */
  approveTime: string | null
}

// ==================== API 方法 ====================

/** 提交认证申请 */
export function submitCertification(data: H5CertificationCreateDTO): Promise<void> {
  return request.post('/owner/h5/certifications', data)
}

/** 获取我的认证记录 */
export function myCertifications(): Promise<CertificationVO[]> {
  return request.get('/owner/h5/certifications')
}

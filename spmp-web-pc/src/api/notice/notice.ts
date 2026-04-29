/**
 * 公告管理 API（PC 管理端）
 */
import request from '@/utils/request'

export interface NoticeCreateDTO {
  title: string
  content: string
  noticeType: 'NORMAL' | 'EMERGENCY' | 'ACTIVITY'
  topFlag?: number
  expireTime?: string
  scopeList: Array<{ scopeType: 'ALL' | 'COMMUNITY' | 'BUILDING'; targetId?: number }>
}

export interface NoticeQueryDTO {
  title?: string
  noticeType?: string
  status?: string
  startTime?: string
  endTime?: string
  pageNum: number
  pageSize: number
}

export interface NoticeRepushDTO {
  bizSerialNo: string
  remark?: string
}

export interface NoticeApproveDTO {
  action: 'APPROVE' | 'REJECT'
  remark?: string
}

export interface NoticeListVO {
  id: number
  title: string
  noticeType: string
  status: string
  topFlag: number
  publishTime?: string
  expireTime?: string
  expired?: boolean
  read?: boolean
}

export interface NoticeReadStatsVO {
  announcementId: number
  targetCount: number
  readCount: number
  unreadCount: number
  readRate: number
}

export interface NoticeUnreadUserVO {
  userId: number
  userName: string
  communityId?: number
  buildingId?: number
}

export function createNotice(data: NoticeCreateDTO) {
  return request.post('/notice/announcements', data)
}

export function listNotices(params: NoticeQueryDTO) {
  return request.get('/notice/announcements', { params })
}

export function withdrawNotice(id: number, remark?: string) {
  return request.put(`/notice/announcements/${id}/withdraw`, null, { params: { remark } })
}

export function repushNotice(id: number, data: NoticeRepushDTO) {
  return request.post(`/notice/announcements/${id}/repush`, data)
}

export function listApprovals(params: { title?: string; pageNum: number; pageSize: number }) {
  return request.get('/notice/approvals', { params })
}

export function approveNotice(id: number, data: NoticeApproveDTO) {
  return request.put(`/notice/approvals/${id}`, data)
}

export function getReadStats(id: number) {
  return request.get(`/notice/announcements/${id}/stats`)
}

export function listUnreadUsers(id: number, pageNum = 1, pageSize = 20) {
  return request.get(`/notice/announcements/${id}/unread-users`, { params: { pageNum, pageSize } })
}

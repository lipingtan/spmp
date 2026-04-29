/**
 * 公告 API（H5 业主端）
 */
import request from '@/utils/request'

export interface H5NoticeQueryDTO {
  noticeType?: string
  pageNum: number
  pageSize: number
}

export interface H5NoticeListVO {
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

export interface H5NoticeDetailVO {
  id: number
  title: string
  content: string
  noticeType: string
  status: string
  topFlag: number
  publishTime?: string
  expireTime?: string
  expired?: boolean
  read?: boolean
}

export function listMyNotices(params: H5NoticeQueryDTO) {
  return request.get('/h5/notice/announcements', { params })
}

export function getNoticeDetail(id: number): Promise<H5NoticeDetailVO> {
  return request.get(`/h5/notice/announcements/${id}`)
}

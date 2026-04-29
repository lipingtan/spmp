/**
 * 公共文件上传 API（H5 端）
 */
import request from '@/utils/request'

const UPLOAD_URL = '/common/files/upload'
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
const MAX_SIZE = 5 * 1024 * 1024
const MAX_COUNT = 5

export interface UploadResult {
  url: string
  name: string
}

export const UPLOAD_CONSTANTS = {
  ALLOWED_TYPES,
  MAX_SIZE,
  MAX_COUNT,
  ACCEPT: '.jpg,.jpeg,.png,.gif,.webp'
} as const

export function uploadFile(file: File, category = 'workorder'): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', category)
  return request.post(UPLOAD_URL, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function validateFile(file: File): string | null {
  if (!ALLOWED_TYPES.includes(file.type)) {
    return '仅支持 JPG/PNG/GIF/WEBP 格式'
  }
  if (file.size > MAX_SIZE) {
    return '图片大小不能超过 5MB'
  }
  return null
}

export async function uploadFiles(files: File[], category = 'workorder'): Promise<string[]> {
  const urls: string[] = []
  for (const file of files) {
    const error = validateFile(file)
    if (error) {
      throw new Error(error)
    }
    const url = await uploadFile(file, category)
    urls.push(url)
  }
  return urls
}

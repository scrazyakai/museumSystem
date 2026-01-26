import http from './http'
import type { ExhibitItem, MediaKind, PageResult } from './items'

// 重新导出类型供其他模块使用
export type { ExhibitItem, MediaKind }

// 管理端展品查询参数
export interface AdminItemQueryParams {
  current: number
  size: number
  keyword?: string
  mediaKind?: MediaKind
}

// 新增展品请求参数
export interface AddItemRequest {
  title: string
  description?: string
  mediaKind: MediaKind
  mediaUrl: string
  coverUrl?: string
  startTime?: string
  endTime?: string
}

// 更新展品请求参数
export interface UpdateItemRequest {
  id: number
  title?: string
  description?: string
  mediaUrl?: string
  coverUrl?: string
  startTime?: string
  endTime?: string
}

// OSS上传响应
export interface OssUploadResponse {
  url: string
  mediaKind: MediaKind
  originalName: string
  size: number
}

/**
 * 上传文件到OSS
 */
export function uploadToOss(file: File): Promise<OssUploadResponse> {
  const formData = new FormData()
  formData.append('file', file)

  return http.post<OssUploadResponse>('/api/admin/oss/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }) as any
}

/**
 * 获取展品列表（管理端）
 */
export function getAdminItems(params: AdminItemQueryParams): Promise<PageResult<ExhibitItem>> {
  return http.get<PageResult<ExhibitItem>>('/api/admin/items', { params }) as any
}

/**
 * 新增展品
 */
export function addItem(data: AddItemRequest): Promise<number> {
  return http.post<number>('/api/admin/items', data) as any
}

/**
 * 更新展品
 */
export function updateItem(data: UpdateItemRequest): Promise<void> {
  return http.put('/api/admin/items', data)
}

/**
 * 上架展品
 */
export function publishItem(id: number): Promise<void> {
  return http.patch(`/api/admin/items/${id}/up`)
}

/**
 * 下架展品
 */
export function unpublishItem(id: number): Promise<void> {
  return http.patch(`/api/admin/items/${id}/down`)
}

/**
 * 恢复展品
 */
export function restoreItem(id: number): Promise<void> {
  return http.patch(`/api/admin/items/${id}/restore`)
}

/**
 * 删除展品
 */
export function deleteItem(id: number): Promise<void> {
  return http.delete(`/api/admin/items/${id}`)
}

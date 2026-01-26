import http from './http'

// 媒体类型
export type MediaKind = 'IMAGE' | 'VIDEO'

// 展品信息
export interface ExhibitItem {
  id: number
  title: string
  description: string | null
  mediaKind: MediaKind
  mediaUrl: string
  coverUrl: string | null
  startTime: string | null
  endTime: string | null
  creatorId: number | null
  createdAt: string
  updatedAt: string
}

// 分页查询参数
export interface ItemQueryParams {
  current: number
  size: number
  mediaKind?: MediaKind
}

// 分页响应
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 获取展品列表（用户端）
 */
export function getItems(params: ItemQueryParams): Promise<PageResult<ExhibitItem>> {
  return http.get<PageResult<ExhibitItem>>('/api/items', { params }) as any
}

/**
 * 获取展品详情
 */
export function getItemDetail(id: number): Promise<ExhibitItem> {
  return http.get<ExhibitItem>(`/api/items/${id}`) as any
}

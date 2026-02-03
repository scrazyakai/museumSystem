import http from './http'

// 库存信息
export interface QuotaInfo {
  id: number
  visitDate: string
  capacity: number
  reservedCount: number
  remainingCount: number
  status: number
  createdAt: string
  updatedAt: string
}

// 查询库存请求参数
export interface QuotaQueryRequest {
  visitDate: string // 格式：yyyy-MM-DD
}

// 更新库存请求参数
export interface QuotaUpdateRequest {
  visitDate: string // 格式：yyyy-MM-DD
  capacity: number
}

/**
 * 获取指定日期的库存信息
 */
export function getQuotaInfo(visitDate: string): Promise<QuotaInfo> {
  return http.post<QuotaInfo>('/api/quota/info', { visitDate }) as any
}

/**
 * 更新指定日期的库存（管理员）
 */
export function updateQuota(data: QuotaUpdateRequest): Promise<boolean> {
  return http.post<boolean>('/api/quota/update', data) as any
}

/**
 * 手动创建未来N天的库存（管理员）
 */
export function createFutureQuota(days: number): Promise<string> {
  return http.post<string>('/api/quota/create', null, { params: { days } }) as any
}

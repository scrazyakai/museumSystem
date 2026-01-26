import http from './http'

// 管理员评论信息
export interface AdminComment {
  id: number
  itemId: number
  itemTitle: string
  userId: number
  username: string
  content: string
  status: number  // 0: 已隐藏, 1: 正常
  createdAt: string
}

// 分页响应接口
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 管理员评论查询参数
export interface AdminCommentQueryParams {
  current: number
  size: number
  itemId?: number
  itemTitle?: string
  status?: number  // 0: 已隐藏, 1: 正常
}

/**
 * 分页查询评论列表（管理员）
 */
export function getAdminComments(params: AdminCommentQueryParams): Promise<PageResult<AdminComment>> {
  return http.get<PageResult<AdminComment>>('/api/admin/comments', { params }) as any
}

/**
 * 删除评论（管理员）
 */
export function deleteAdminComment(id: number): Promise<void> {
  return http.post(`/api/admin/comments/delete/${id}`)
}

/**
 * 隐藏评论
 */
export function hideComment(id: number): Promise<void> {
  return http.post(`/api/admin/comments/${id}/hide`)
}

/**
 * 显示评论
 */
export function showComment(id: number): Promise<void> {
  return http.post(`/api/admin/comments/${id}/show`)
}

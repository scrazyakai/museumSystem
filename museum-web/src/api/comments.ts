import http from './http'
import type { PageResult } from './items'

// 评论信息
export interface Comment {
  id: number
  itemId: number
  userId: number
  username: string
  content: string
  createdAt: string
  avatarURL?: string
}

// 发表评论请求参数
export interface AddCommentRequest {
  content: string
}

// 评论查询参数
export interface CommentQueryParams {
  current: number
  size: number
}

/**
 * 获取展品评论列表
 */
export function getComments(itemId: number, params: CommentQueryParams): Promise<PageResult<Comment>> {
  return http.get<PageResult<Comment>>(`/api/items/${itemId}/comments/list`, { params }) as any
}

/**
 * 发表评论
 */
export function addComment(itemId: number, data: AddCommentRequest): Promise<number> {
  return http.post<number>(`/api/items/${itemId}/comments/add`, data) as any
}

/**
 * 删除评论
 */
export function deleteComment(commentId: number): Promise<void> {
  return http.delete(`/api/items/comments/${commentId}`)
}

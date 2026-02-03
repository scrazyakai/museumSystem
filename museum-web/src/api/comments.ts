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
  liked: number // 0: 未点赞, 1: 已点赞
  likecount?: number // 点赞总数（后端可能返回likeCount）
  likeCount?: number // 点赞总数（驼峰命名）
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

/**
 * 点赞评论
 */
export function likeComment(commentId: number): Promise<boolean> {
  return http.post<boolean>(`/api/comment-like/like?commentId=${commentId}`) as any
}

/**
 * 取消点赞评论
 */
export function unlikeComment(commentId: number): Promise<boolean> {
  return http.post<boolean>(`/api/comment-like/cancel?commentId=${commentId}`) as any
}

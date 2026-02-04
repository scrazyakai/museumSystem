import http from './http'

// 消息分类枚举
export enum NoticeCategory {
  BOOKING = 1, // 预约
  ANNOUNCEMENT = 2, // 公告
  ACTIVITY = 3, // 活动
  LECTURE = 4, // 讲座
  SYSTEM = 5 // 系统
}

// 阅读状态枚举
export enum ReadFlag {
  UNREAD = 0, // 未读
  READ = 1 // 已读
}

// 用户消息VO
export interface UserNoticeVO {
  id: number
  userId: number
  noticeId: number
  title: string
  content: string
  category: NoticeCategory
  readFlag: ReadFlag
  createdAt: string
  readTime?: string
}

// 分页响应
export interface NoticePageResponse {
  records: UserNoticeVO[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 获取用户消息列表
 * @param page 页码
 * @param size 每页大小
 * @param category 分类（可选）
 * @param readFlag 阅读状态（可选）
 */
export function getMyNotices(
  page?: number,
  size?: number,
  category?: NoticeCategory,
  readFlag?: ReadFlag
): Promise<NoticePageResponse> {
  const params: Record<string, any> = {
    page: page || 1,
    size: size || 10
  }

  if (category !== undefined) {
    params.category = category
  }

  if (readFlag !== undefined) {
    params.readFlag = readFlag
  }

  return http.get<NoticePageResponse>('/api/notice/my', { params }) as any
}

/**
 * 获取消息详情
 * @param id 消息ID
 */
export function getNoticeDetail(id: number): Promise<UserNoticeVO> {
  return http.get<UserNoticeVO>('/api/notice/detail', {
    params: { id }
  }) as any
}

/**
 * 标记消息为已读
 * @param id 消息ID
 */
export function markNoticeAsRead(id: number): Promise<boolean> {
  return http.put<boolean>(`/api/notice/${id}/read`) as any
}

/**
 * 批量标记消息为已读
 * @param ids 消息ID列表
 */
export function batchMarkAsRead(ids: number[]): Promise<boolean> {
  return http.put<boolean>('/api/notice/batch-read', { ids }) as any
}

/**
 * 删除消息
 * @param id 消息ID
 */
export function deleteNotice(id: number): Promise<boolean> {
  return http.delete<boolean>(`/api/notice/${id}`) as any
}

/**
 * 全部标记为已读
 */
export function markAllAsRead(): Promise<boolean> {
  return http.post<boolean>('/api/notice/read-all') as any
}

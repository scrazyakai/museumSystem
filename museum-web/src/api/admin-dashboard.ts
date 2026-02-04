import http from './http'

// 统计数据接口
export interface DashboardStats {
  todayBookedCount: number      // 今日预约数
  todayTotalQuota: number       // 今日总配额
  totalCommentCount: number     // 总评论数
  pendingCommentCount: number   // 待审核评论数
  totalUserCount: number        // 总用户数
  monthlyBookingCount: number   // 本月预约数
  todayVerifiedCount: number    // 今日实名认证数
  todayCancelledCount: number   // 今日取消预约数
}

/**
 * 获取管理员统计数据
 */
export function getDashboardStats(): Promise<DashboardStats> {
  return http.get<DashboardStats>('/api/admin/dashboard/stats') as any
}

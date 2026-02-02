import http from './http'
import type { UserProfile } from './user'

// 扩展 UserProfile 用于预约页面显示
export enum BookingStatus {
  BOOKED = 1,      // 已预约
  CANCELLED = 2,   // 已取消
  RESCHEDULED = 3, // 已改签
  VERIFIED = 4,    // 已核验
  EXPIRED = 5      // 已过期
}

// 预约状态文本映射
export const BookingStatusText: Record<BookingStatus, string> = {
  [BookingStatus.BOOKED]: '已预约',
  [BookingStatus.CANCELLED]: '已取消',
  [BookingStatus.RESCHEDULED]: '已改签',
  [BookingStatus.VERIFIED]: '已核验',
  [BookingStatus.EXPIRED]: '已过期'
}

// 预约状态颜色映射
export const BookingStatusColor: Record<BookingStatus, string> = {
  [BookingStatus.BOOKED]: 'success',
  [BookingStatus.CANCELLED]: 'info',
  [BookingStatus.RESCHEDULED]: 'warning',
  [BookingStatus.VERIFIED]: '',
  [BookingStatus.EXPIRED]: 'danger'
}

// 预约信息
export interface Booking {
  id: number
  visitDate: string
  ticketCode: string
  status: BookingStatus
  cancelReason: string | null
  verifyTime: string | null
  createdAt: string
  updatedAt: string
}

// 创建预约请求
export interface CreateBookingRequest {
  visitDate: string // 格式：yyyy-MM-DD
}

// 改签预约请求
export interface RescheduleBookingRequest {
  bookingId: number
  newVisitDate: string // 格式：yyyy-MM-DD
}

// 取消预约请求
export interface CancelBookingRequest {
  bookingId: number
  cancelReason?: string
}

// 分页查询参数
export interface BookingQueryParams {
  page?: number
  size?: number
  status?: BookingStatus
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
 * 创建预约
 */
export function createBooking(data: CreateBookingRequest): Promise<Booking> {
  return http.post<Booking>('/api/bookings/create', data) as any
}

/**
 * 改签预约
 */
export function rescheduleBooking(data: RescheduleBookingRequest): Promise<Booking> {
  return http.post<Booking>('/api/bookings/reschedule', data) as any
}

/**
 * 取消预约
 */
export function cancelBooking(data: CancelBookingRequest): Promise<boolean> {
  return http.post<boolean>('/api/bookings/cancel', data) as any
}

/**
 * 获取我的预约列表
 */
export function getMyBookings(params: BookingQueryParams): Promise<PageResult<Booking>> {
  return http.get<PageResult<Booking>>('/api/bookings/my', { params }) as any
}

/**
 * 获取预约详情
 */
export function getBookingDetail(id: number): Promise<Booking> {
  return http.get<Booking>('/api/bookings/detail', { params: { id } }) as any
}

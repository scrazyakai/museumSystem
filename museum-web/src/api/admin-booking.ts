import http from './http'
import type { Booking, BookingStatus, PageResult } from './booking'

/**
 * 管理员获取预约列表
 */
export interface AdminBookingQueryParams {
  page?: number
  size?: number
  status?: BookingStatus
  visitDate?: string
  ticketCode?: string
}

/**
 * 获取所有预约列表（管理员）
 */
export function getAllBookings(params: AdminBookingQueryParams): Promise<PageResult<Booking>> {
  return http.post<PageResult<Booking>>('/api/admin/bookings/query', params) as any
}

/**
 * 核验入馆（管理员）
 */
export interface VerifyBookingRequest {
  ticketCode: string
}

export function verifyBooking(data: VerifyBookingRequest): Promise<Booking> {
  return http.post<Booking>('/api/admin/bookings/verify', data) as any
}

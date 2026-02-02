import http from './http'

// 用户详细信息
export interface UserProfile {
  username: string
  avatarUrl: string | null
  phone: string | null
  realName: string | null
  idNo: string | null
}

// 更新用户信息请求
export interface UpdateProfileRequest {
  avatarUrl?: string
  phone?: string
}

// 实名认证请求
export interface RealNameVerifyRequest {
  realName: string
  idCard: string
}

/**
 * 获取当前用户详细信息
 */
export function getUserProfile(): Promise<UserProfile> {
  return http.get<UserProfile>('/api/sys-user/detail') as any
}

/**
 * 更新用户信息
 */
export function updateProfile(data: UpdateProfileRequest): Promise<boolean> {
  return http.put<boolean>('/api/sys-user/profile', data) as any
}

/**
 * 实名认证
 */
export function realNameVerify(data: RealNameVerifyRequest): Promise<boolean> {
  return http.post<boolean>('/api/sys-user/verify', data) as any
}

/**
 * 修改密码
 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export function changePassword(data: ChangePasswordRequest): Promise<boolean> {
  return http.post<boolean>('/api/sys-user/change-password', data) as any
}

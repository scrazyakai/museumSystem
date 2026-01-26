import http from './http'

// 用户注册请求参数
export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  nickname?: string
  phone?: string
}

// 用户登录请求参数
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应数据
export interface LoginResponse {
  id: number
  username: string
  nickname: string
  avatarUrl: string | null
  role: string
  token: string
}

// 用户信息响应数据
export interface UserInfoResponse {
  id: number
  token: string
}

/**
 * 用户注册
 * @param data 注册信息
 * @returns Promise<number> 用户ID
 */
export function register(data: RegisterRequest): Promise<number> {
  return http.post<number>('/api/sys-user/register', data) as any
}

/**
 * 用户登录（包含管理员登录）
 * @param data 登录信息
 * @returns Promise<LoginResponse> 登录响应数据
 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return http.post<LoginResponse>('/api/sys-user/login', data) as any
}

/**
 * 用户登出
 * @returns Promise<boolean>
 */
export function logout(): Promise<boolean> {
  return http.post<boolean>('/api/sys-user/logout') as any
}

/**
 * 获取当前用户信息
 * @returns Promise<UserInfoResponse>
 */
export function getUserInfo(): Promise<UserInfoResponse> {
  return http.get<UserInfoResponse>('/api/sys-user/info') as any
}

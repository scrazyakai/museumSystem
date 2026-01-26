import http from './http'

// 用户信息接口
export interface UserInfo {
  id: number
  username: string
  passwordHash: string
  avatarUrl: string | null
  phone: string | null
  qqOpenid: string | null
  wechatOpenid: string | null
  realName: string | null
  idCard: string | null
  allowPush: number
  allowFootprint: number
  status: number
  role: string
  createdAt: string
  updatedAt: string
}

// 分页响应接口
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 用户查询请求参数
export interface UserQueryParams {
  page?: number
  size?: number
  keyword?: string
  status?: number
}

/**
 * 分页查询用户列表
 */
export function getUserList(params: UserQueryParams): Promise<PageResult<UserInfo>> {
  return http.get<PageResult<UserInfo>>('/api/admin/users', { params }) as any
}

/**
 * 获取用户详情
 */
export function getUserDetail(id: number): Promise<UserInfo> {
  return http.get<UserInfo>(`/api/admin/users/${id}`) as any
}

/**
 * 禁用用户
 */
export function banUser(id: number): Promise<void> {
  return http.put(`/api/admin/users/${id}/ban`)
}

/**
 * 解禁用户
 */
export function unbanUser(id: number): Promise<void> {
  return http.put(`/api/admin/users/${id}/unban`)
}

/**
 * 导出用户列表
 */
export function exportUsers(params: { keyword?: string; status?: number }): Promise<Blob> {
  return http.get('/api/admin/users/export', {
    params,
    responseType: 'blob'
  })
}

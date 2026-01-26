const TOKEN_KEY = 'token'
const TOKEN_NAME_KEY = 'tokenName'
const LOGIN_TYPE_KEY = 'loginType'

// Token 操作
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// TokenName 操作
export function getTokenName(): string | null {
  return localStorage.getItem(TOKEN_NAME_KEY)
}

export function setTokenName(tokenName: string): void {
  localStorage.setItem(TOKEN_NAME_KEY, tokenName)
}

export function removeTokenName(): void {
  localStorage.removeItem(TOKEN_NAME_KEY)
}

// LoginType 操作
export function getLoginType(): string | null {
  return localStorage.getItem(LOGIN_TYPE_KEY)
}

export function setLoginType(loginType: 'user' | 'admin'): void {
  localStorage.setItem(LOGIN_TYPE_KEY, loginType)
}

export function removeLoginType(): void {
  localStorage.removeItem(LOGIN_TYPE_KEY)
}

// 清除所有认证信息
export function clearAuth(): void {
  removeToken()
  removeTokenName()
  removeLoginType()
}

// 检查是否已登录
export function isAuthed(): boolean {
  return !!getToken()
}

// 检查登录类型是否匹配
export function isLoginTypeMatched(expectedType: 'user' | 'admin'): boolean {
  const currentType = getLoginType()
  return currentType === expectedType
}

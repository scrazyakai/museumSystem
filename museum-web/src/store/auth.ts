import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { LoginResponse } from '@/api/auth'
import { getToken, setToken, clearAuth, setLoginType, getLoginType } from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  // 用户信息
  const userInfo = ref<LoginResponse | null>(null)
  
  // 登录类型
  const loginType = ref<'user' | 'admin' | null>(null)
  
  // 是否已登录（只要有 token 就认为已登录）
  const isAuthed = computed(() => !!getToken())
  
  // 是否是管理员
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  
  // 设置用户信息
  function setUser(user: LoginResponse, type: 'user' | 'admin') {
    userInfo.value = user
    loginType.value = type
    
    // 保存 token 和登录类型到 localStorage
    setToken(user.token)
    setLoginType(type)
  }
  
  // 清除用户信息
  function clearUser() {
    userInfo.value = null
    loginType.value = null
    clearAuth()
  }
  
  // 从 localStorage 恢复登录状态
  function restoreAuth() {
    const token = getToken()
    const type = getLoginType()
    
    if (token && type) {
      loginType.value = type as 'user' | 'admin'
      // 注意：这里不恢复完整的 userInfo，因为需要从后端获取最新信息
      // 只标记已登录状态
    }
  }
  
  return {
    userInfo,
    loginType,
    isAuthed,
    isAdmin,
    setUser,
    clearUser,
    restoreAuth
  }
})

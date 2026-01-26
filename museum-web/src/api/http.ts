import axios, { AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, getTokenName, clearAuth } from '@/utils/auth'

// 创建 axios 实例
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000
}) as AxiosInstance

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    const token = getToken()
    const tokenName = getTokenName() || 'satoken'
    
    if (token) {
      config.headers[tokenName] = token
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
http.interceptors.response.use(
  (response) => {
    // 如果响应类型是 blob，直接返回（用于文件下载）
    if (response.config.responseType === 'blob') {
      return response.data
    }

    const { code, data, message } = response.data

    // code 为 0 表示成功
    if (code === 0) {
      return data as any  // 返回实际的业务数据
    }

    // 处理业务错误
    if (message) {
      ElMessage.error(message)
    }

    return Promise.reject({ code, data, message })
  },
  (error) => {
    const { response } = error

    if (response) {
      const { status, data } = response

      // 401 未登录或 token 过期
      if (status === 401 || data?.code === 401) {
        clearAuth()
        const currentPath = router.currentRoute.value.path

        // 判断当前是用户端还是管理端
        if (currentPath.startsWith('/admin')) {
          router.push('/login/admin')
        } else {
          router.push('/login/user')
        }
        ElMessage.error('登录已过期，请重新登录')
        return Promise.reject(error)
      }

      // 402 无权限（非管理员访问管理员接口）
      if (status === 402 || data?.code === 402) {
        ElMessage.error(data?.message || '无权限访问')
        return Promise.reject(error)
      }

      // 其他错误
      const message = data?.message || error.message || '请求失败'
      ElMessage.error(message)
    } else {
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络')
    }

    return Promise.reject(error)
  }
)

export default http

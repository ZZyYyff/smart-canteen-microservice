import axios from 'axios'
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import {
  getToken,
  setToken,
  removeToken,
  getRefreshToken,
  setRefreshToken,
  removeRefreshToken,
  removeUserInfo,
} from '@/utils/auth'
import { useAuthStore } from '@/stores/auth'

// 3 秒内相同的错误消息不重复弹出
let lastErrorMsg = ''
let lastErrorTime = 0

function showErrorOnce(msg: string) {
  const now = Date.now()
  if (msg !== lastErrorMsg || now - lastErrorTime > 3000) {
    lastErrorMsg = msg
    lastErrorTime = now
    ElMessage.error(msg)
  }
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// ===== Token 刷新状态 =====
let isRefreshing = false
let pendingRequests: Array<{
  resolve: (token: string) => void
  reject: (err: unknown) => void
}> = []

function queueRequest(): Promise<string> {
  return new Promise<string>((resolve, reject) => {
    pendingRequests.push({ resolve, reject })
  })
}

function resolvePendingRequests(token: string) {
  pendingRequests.forEach((p) => p.resolve(token))
  pendingRequests = []
}

function rejectPendingRequests(err: unknown) {
  pendingRequests.forEach((p) => p.reject(err))
  pendingRequests = []
}

async function doRefreshToken(): Promise<{ token: string; refreshToken: string }> {
  const refreshTokenValue = getRefreshToken()
  if (!refreshTokenValue) {
    throw new Error('无 RefreshToken')
  }

  // 用独立 axios 实例调用刷新接口，避免拦截器无限循环
  const baseURL = import.meta.env.VITE_API_BASE_URL
  const res: AxiosResponse = await axios.post(
    `${baseURL}/api/users/refresh-token`,
    { refreshToken: refreshTokenValue },
    { headers: { 'Content-Type': 'application/json' } },
  )

  if (res.data?.code === 200 && res.data?.data?.token) {
    return {
      token: res.data.data.token,
      refreshToken: res.data.data.refreshToken || res.data.data.token,
    }
  }
  throw new Error(res.data?.message || '刷新失败')
}

// ===== 请求拦截器 =====
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ===== 响应拦截器 =====
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    showErrorOnce(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  async (error) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // 401 处理
    if (error.response?.status === 401) {
      // 如果是刷新接口本身失败，直接清空登录态跳转
      if (originalRequest.url?.includes('/api/users/refresh-token')) {
        removeToken()
        removeRefreshToken()
        removeUserInfo()
        ElMessage.warning('登录已过期，请重新登录')
        window.location.href = '/login'
        return Promise.reject(error)
      }

      // 已经重试过了，不再重试
      if (originalRequest._retry) {
        removeToken()
        removeRefreshToken()
        removeUserInfo()
        ElMessage.warning('登录已过期，请重新登录')
        window.location.href = '/login'
        return Promise.reject(error)
      }

      // 正在刷新中，排队等待
      if (isRefreshing) {
        try {
          const newToken = await queueRequest()
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          originalRequest._retry = true
          return request(originalRequest)
        } catch {
          return Promise.reject(error)
        }
      }

      // 开始刷新
      isRefreshing = true
      try {
        const result = await doRefreshToken()
        // 更新本地存储
        setToken(result.token)
        setRefreshToken(result.refreshToken)
        // 更新 auth store
        try {
          const authStore = useAuthStore()
          authStore.token = result.token
          authStore.refreshToken = result.refreshToken
        } catch { /* store may not be initialized yet */ }
        // 解决排队请求
        resolvePendingRequests(result.token)
        // 重试原请求
        originalRequest.headers.Authorization = `Bearer ${result.token}`
        originalRequest._retry = true
        return request(originalRequest)
      } catch (refreshError) {
        // 刷新失败
        rejectPendingRequests(refreshError)
        removeToken()
        removeRefreshToken()
        removeUserInfo()
        try {
          const authStore = useAuthStore()
          authStore.logout()
        } catch { /* store may not be initialized yet */ }
        ElMessage.warning('登录已过期，请重新登录')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // 其他 HTTP 错误
    const msg = error.response?.data?.message || error.message || '网络异常'
    showErrorOnce(msg)
    return Promise.reject(error)
  },
)

export default request

import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken, removeUserInfo } from '@/utils/auth'

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

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    showErrorOnce(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      removeUserInfo()
      window.location.href = '/login'
      return Promise.reject(error)
    }
    // HTTP 错误（网络不通、500等）统一提示
    const msg = error.response?.data?.message || error.message || '网络异常'
    showErrorOnce(msg)
    return Promise.reject(error)
  },
)

export default request

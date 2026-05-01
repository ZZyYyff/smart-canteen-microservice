import request from './request'
import type { UserVO, LoginVO } from '@/types/api'

interface LoginParams {
  phone?: string
  studentNo?: string
  password: string
}

interface RegisterParams {
  phone: string
  studentNo: string
  password: string
  nickname?: string
}

interface UpdateUserParams {
  nickname?: string
  phone?: string
}

/** 用户注册 */
export function register(params: RegisterParams): Promise<UserVO> {
  return request.post('/api/users/register', params)
}

/** 用户登录 */
export function login(params: LoginParams): Promise<LoginVO> {
  return request.post('/api/users/login', params)
}

/** 刷新 Token */
export function refreshToken(refreshToken: string): Promise<LoginVO> {
  return request.post('/api/users/refresh-token', { refreshToken })
}

/** 查询当前用户信息 */
export function getCurrentUser(): Promise<UserVO> {
  return request.get('/api/users/me')
}

/** 修改当前用户信息 */
export function updateCurrentUser(params: UpdateUserParams): Promise<UserVO> {
  return request.put('/api/users/me', params)
}

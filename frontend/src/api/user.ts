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

interface AdminUserListResult {
  total: number
  page: number
  size: number
  list: UserVO[]
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

// ==================== 管理员接口 ====================

/** 管理员查询用户列表 */
export function getAdminUserList(params: {
  keyword?: string
  filterRole?: string
  status?: string
  page?: number
  size?: number
}): Promise<AdminUserListResult> {
  return request.get('/api/users/admin/list', { params })
}

/** 管理员启用用户 */
export function enableUser(id: number): Promise<void> {
  return request.put(`/api/users/admin/${id}/enable`)
}

/** 管理员禁用用户 */
export function disableUser(id: number): Promise<void> {
  return request.put(`/api/users/admin/${id}/disable`)
}

/** 管理员修改用户角色 */
export function updateUserRole(id: number, role: string): Promise<void> {
  return request.put(`/api/users/admin/${id}/role`, { role })
}

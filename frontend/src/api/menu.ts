import request from './request'
import type { DishVO, DailyMenuVO } from '@/types/api'

interface DishParams {
  name: string
  price: number
  description?: string
  imageUrl?: string
  stock: number
  warningStock?: number
}

/** 获取今日菜单 */
export function getTodayMenu(): Promise<DailyMenuVO[]> {
  return request.get('/api/menus/today')
}

/** 查询菜品列表 */
export function listDishes(params?: { name?: string; status?: string }): Promise<DishVO[]> {
  return request.get('/api/menus/dishes', { params })
}

/** 查询菜品详情 */
export function getDishById(id: number): Promise<DishVO> {
  return request.get(`/api/menus/dishes/${id}`)
}

/** 新增菜品 */
export function createDish(params: DishParams): Promise<DishVO> {
  return request.post('/api/menus/dishes', params)
}

/** 修改菜品 */
export function updateDish(id: number, params: DishParams): Promise<DishVO> {
  return request.put(`/api/menus/dishes/${id}`, params)
}

/** 删除菜品 */
export function deleteDish(id: number): Promise<void> {
  return request.delete(`/api/menus/dishes/${id}`)
}

/** 上架菜品 */
export function onSaleDish(id: number): Promise<DishVO> {
  return request.put(`/api/menus/dishes/${id}/on-sale`)
}

/** 下架菜品 */
export function offSaleDish(id: number): Promise<DishVO> {
  return request.put(`/api/menus/dishes/${id}/off-sale`)
}

// ==================== 每日菜单 ====================

interface DailyMenuParams {
  menuDate: string
  mealPeriod: string
  startTime: string
  endTime: string
  dishIds: number[]
}

interface DailyMenuQueryParams {
  menuDate?: string
  mealPeriod?: string
}

/** 查询每日菜单列表 —— GET /api/menus/daily */
export function getDailyMenuList(params?: DailyMenuQueryParams): Promise<DailyMenuVO[]> {
  return request.get('/api/menus/daily', { params })
}

/** 创建每日菜单 —— POST /api/menus/daily */
export function createDailyMenu(params: DailyMenuParams): Promise<DailyMenuVO> {
  return request.post('/api/menus/daily', params)
}

/** 编辑每日菜单 —— PUT /api/menus/daily/{id} */
export function updateDailyMenu(id: number, params: DailyMenuParams): Promise<DailyMenuVO> {
  return request.put(`/api/menus/daily/${id}`, params)
}

/** 删除每日菜单 —— DELETE /api/menus/daily/{id} */
export function deleteDailyMenu(id: number): Promise<void> {
  return request.delete(`/api/menus/daily/${id}`)
}

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

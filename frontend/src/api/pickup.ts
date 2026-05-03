import request from './request'
import type { PickupWindowVO, PickupQueueVO } from '@/types/api'

/** 查询取餐窗口列表 */
export function listWindows(): Promise<PickupWindowVO[]> {
  return request.get('/api/pickup/windows')
}

/** 查询窗口排队队列 */
export function getWindowQueue(windowId: number): Promise<PickupQueueVO[]> {
  return request.get(`/api/pickup/windows/${windowId}/queue`)
}

/** 叫下一号 */
export function callNext(windowId: number): Promise<PickupQueueVO> {
  return request.post(`/api/pickup/windows/${windowId}/call-next`)
}

/** 取餐核销 */
export function verifyPickup(params: { pickupNo: number; pickupCode: string }): Promise<PickupQueueVO> {
  return request.post('/api/pickup/verify', params)
}

// ==================== 窗口管理 ====================

interface CreateWindowParams {
  name: string
  location?: string
}

/** 新增取餐窗口 */
export function createWindow(params: CreateWindowParams): Promise<PickupWindowVO> {
  return request.post('/api/pickup/windows', params)
}

/** 启用取餐窗口 */
export function enableWindow(id: number): Promise<void> {
  return request.put(`/api/pickup/windows/${id}/enable`)
}

/** 停用取餐窗口 */
export function disableWindow(id: number): Promise<void> {
  return request.put(`/api/pickup/windows/${id}/disable`)
}

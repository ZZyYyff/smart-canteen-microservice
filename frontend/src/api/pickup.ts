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

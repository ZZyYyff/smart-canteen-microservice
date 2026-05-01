import request from './request'
import type { OrderVO } from '@/types/api'

interface CreateOrderItem {
  dishId: number
  dishName: string
  price: number
  quantity: number
}

interface CreateOrderParams {
  windowId: number
  items: CreateOrderItem[]
}

/** 创建订单 */
export function createOrder(params: CreateOrderParams): Promise<OrderVO> {
  return request.post('/api/orders', params)
}

/** 查询当前用户订单 */
export function getMyOrders(): Promise<OrderVO[]> {
  return request.get('/api/orders/my')
}

/** 查询订单详情 */
export function getOrderById(id: number): Promise<OrderVO> {
  return request.get(`/api/orders/${id}`)
}

/** 取消订单 */
export function cancelOrder(id: number): Promise<OrderVO> {
  return request.put(`/api/orders/${id}/cancel`)
}

/** 商家查询待处理订单 */
export function getMerchantPendingOrders(): Promise<OrderVO[]> {
  return request.get('/api/orders/merchant/pending')
}

/** 商家接单 */
export function acceptOrder(id: number): Promise<OrderVO> {
  return request.put(`/api/orders/${id}/accept`)
}

/** 开始制作 */
export function cookingOrder(id: number): Promise<OrderVO> {
  return request.put(`/api/orders/${id}/cooking`)
}

/** 制作完成（备餐完成，等待取餐） */
export function readyOrder(id: number): Promise<OrderVO> {
  return request.put(`/api/orders/${id}/ready`)
}

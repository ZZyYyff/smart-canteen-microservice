/** 用户信息 VO（对应后端 LoginVO.user / UserVO） */
export interface UserVO {
  id: number
  phone: string
  studentNo: string
  nickname: string
  role: string
  status: string
  createdAt: string
}

/** 登录返回（对应后端 LoginVO） */
export interface LoginVO {
  token: string
  refreshToken: string
  user: UserVO
}

/** 菜品 VO */
export interface DishVO {
  id: number
  name: string
  price: number
  description: string
  imageUrl: string
  stock: number
  warningStock: number
  status: string
  statusDesc: string
  lowStock: boolean
  createdAt: string
}

/** 每日菜单 VO */
export interface DailyMenuVO {
  id: number
  menuDate: string
  mealPeriod: string
  mealPeriodDesc: string
  startTime: string
  endTime: string
  status: string
  dishes: DishVO[]
}

/** 订单明细 VO */
export interface OrderItemVO {
  dishId: number
  dishName: string
  price: number
  quantity: number
}

/** 订单 VO */
export interface OrderVO {
  id: number
  userId: number
  windowId: number
  totalAmount: number
  status: string
  statusDesc: string
  pickupNo: number
  pickupCode: string
  items: OrderItemVO[]
  createdAt: string
}

/** 取餐窗口 VO */
export interface PickupWindowVO {
  id: number
  name: string
  location: string
  status: string
  statusDesc: string
  createdAt: string
}

/** 取餐队列 VO */
export interface PickupQueueVO {
  id: number
  windowId: number
  windowName: string
  orderId: number
  pickupNo: number
  status: string
  statusDesc: string
  queueTime: string
  callTime: string
  finishTime: string
}

/** 后端统一返回格式 Result<T> */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

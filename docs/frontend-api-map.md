# 前端接口映射文档

> 基于后端代码实际扫描生成，未做任何推测。生成日期：2026-05-01。

---

## 1. 网关基础信息

| 项目 | 值 |
|------|-----|
| 网关地址 | `http://localhost:8080` |
| 鉴权方式 | JWT Bearer Token（请求头 `Authorization: Bearer <token>`） |
| Token 有效期 | AccessToken 24 小时，RefreshToken 7 天 |
| 网关透传头 | `X-User-Id`（用户 ID）、`X-User-Role`（用户角色） |
| 限流策略 | 每秒补充 10 令牌，突发容量 20 |

### JWT 放行路径（无需 Token）

| 路径 | 说明 |
|------|------|
| `/api/users/login` | 登录 |
| `/api/users/register` | 注册 |
| `/api/users/refresh-token` | 刷新 Token |
| `/ws/pickup/screen` | 大屏 WebSocket |

其余所有 `/api/**` 路径均需携带 JWT。

---

## 2. 统一返回格式

所有接口返回 `Result<T>`，JSON 结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 常见错误码

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 / Token 无效或已过期 |
| 403 | 无权限 / 账号被禁用 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 业务异常（通用） |
| 2001 | 用户不存在 |
| 2002 | 密码错误 |
| 2003 | Token 已过期 |
| 2004 | 用户名已存在 |
| 3001 | 菜品不存在 |
| 3002 | 库存不足 |
| 4001 | 订单不存在 |
| 4002 | 订单状态异常 |
| 4003 | 当前订单状态不允许取消 |
| 5001 | 取餐码错误 |
| 5002 | 该订单已取餐 |
| 5003 | 取餐队列为空 |

---

## 3. 接口详细清单

### 3.1 用户服务（user-service，端口 9001）

#### POST /api/users/register — 用户注册

- **JWT**：不需要
- **前端页面**：注册页

**请求体**（JSON）：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 是 | 手机号，格式 `1[3-9]xxxxxxxxx` |
| studentNo | String | 是 | 学工号 |
| password | String | 是 | 密码，6-20 位 |
| nickname | String | 否 | 昵称，不传则自动生成 |

**返回 `data`**（UserVO）：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户 ID |
| phone | String | 手机号 |
| studentNo | String | 学工号 |
| nickname | String | 昵称 |
| role | String | 角色（STUDENT / MERCHANT / ADMIN） |
| status | String | 状态（NORMAL / DISABLED） |
| createdAt | String | 创建时间（ISO 8601） |

> 注意：注册接口固定注册为 STUDENT 角色，无商家/管理员注册入口。

---

#### POST /api/users/login — 用户登录

- **JWT**：不需要
- **前端页面**：登录页

**请求体**（JSON）：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 二选一 | 手机号 |
| studentNo | String | 二选一 | 学工号 |
| password | String | 是 | 密码 |

**返回 `data`**（LoginVO）：
| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | 访问令牌（24h 有效） |
| refreshToken | String | 刷新令牌（7 天有效） |
| user | UserVO | 用户基本信息（结构同上） |

---

#### POST /api/users/refresh-token — 刷新 Token

- **JWT**：不需要
- **前端页面**：无（前端拦截器自动调用）

**请求体**（JSON）：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | String | 是 | 已获取的 refreshToken |

**返回 `data`**：同登录接口（LoginVO），下发新的 token 和 refreshToken。

---

#### GET /api/users/me — 获取当前用户信息

- **JWT**：需要
- **前端页面**：个人中心

**请求头**：网关自动注入 `X-User-Id`

**返回 `data`**（UserVO）：同注册返回结构。

---

#### PUT /api/users/me — 修改当前用户信息

- **JWT**：需要
- **前端页面**：个人中心 / 编辑资料

**请求头**：网关自动注入 `X-User-Id`

**请求体**（JSON）：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | String | 否 | 新昵称 |
| phone | String | 否 | 新手机号（需校验唯一性） |

**返回 `data`**（UserVO）：更新后的用户信息。

---

### 3.2 菜品与菜单服务（menu-service，端口 9002）

#### POST /api/menus/dishes — 新增菜品

- **JWT**：需要
- **建议角色**：MERCHANT / ADMIN
- **前端页面**：商家后台 → 菜品管理

**请求体**（JSON）：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 菜品名称 |
| price | BigDecimal | 是 | 价格，必须 > 0 |
| description | String | 否 | 描述 |
| imageUrl | String | 否 | 图片 URL |
| stock | Integer | 是 | 库存，必须 > 0 |
| warningStock | Integer | 否 | 库存预警阈值，默认 0 |

**返回 `data`**（DishVO）：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 菜品 ID |
| name | String | 菜品名称 |
| price | BigDecimal | 价格 |
| description | String | 描述 |
| imageUrl | String | 图片 URL |
| stock | Integer | 当前库存 |
| warningStock | Integer | 预警阈值 |
| status | String | 状态枚举值（ON_SALE / OFF_SALE） |
| statusDesc | String | 状态中文描述 |
| lowStock | Boolean | 是否低库存预警 |
| createdAt | String | 创建时间 |

---

#### GET /api/menus/dishes — 菜品列表

- **JWT**：需要
- **前端页面**：菜品列表 / 首页菜单

**查询参数**（可选）：
| 参数 | 类型 | 说明 |
|------|------|------|
| name | String | 按名称模糊筛选 |
| status | String | 按状态筛选（ON_SALE / OFF_SALE） |

**返回 `data`**：`List<DishVO>`

---

#### GET /api/menus/dishes/{id} — 菜品详情

- **JWT**：需要
- **前端页面**：菜品详情页

**返回 `data`**（DishVO）

---

#### PUT /api/menus/dishes/{id} — 修改菜品

- **JWT**：需要
- **建议角色**：MERCHANT / ADMIN
- **前端页面**：商家后台 → 编辑菜品

**请求体**：同 DishDTO（创建菜品的全部字段）

**返回 `data`**（DishVO）

---

#### DELETE /api/menus/dishes/{id} — 删除菜品

- **JWT**：需要
- **建议角色**：MERCHANT / ADMIN
- **前端页面**：商家后台 → 菜品管理

**返回**：`Result<Void>`（无 data）

---

#### PUT /api/menus/dishes/{id}/on-sale — 上架菜品

- **JWT**：需要
- **建议角色**：MERCHANT / ADMIN
- **前端页面**：商家后台 → 菜品管理

**返回 `data`**（DishVO）

---

#### PUT /api/menus/dishes/{id}/off-sale — 下架菜品

- **JWT**：需要
- **建议角色**：MERCHANT / ADMIN
- **前端页面**：商家后台 → 菜品管理

**返回 `data`**（DishVO）

---

#### POST /api/menus/daily — 创建每日菜单

- **JWT**：需要
- **建议角色**：MERCHANT / ADMIN
- **前端页面**：商家后台 → 每日菜单配置

**请求体**（JSON）：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| menuDate | LocalDate | 是 | 菜单日期（如 "2026-05-01"） |
| mealPeriod | String | 是 | 餐段：BREAKFAST / LUNCH / DINNER |
| startTime | LocalTime | 是 | 售卖开始时间（如 "07:00"） |
| endTime | LocalTime | 是 | 售卖结束时间（如 "09:00"） |
| dishIds | List\<Long\> | 是 | 菜品 ID 列表，至少 1 个 |

**返回 `data`**（DailyMenuVO）：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 菜单 ID |
| menuDate | String | 菜单日期 |
| mealPeriod | String | 餐段枚举值 |
| mealPeriodDesc | String | 餐段中文描述 |
| startTime | String | 售卖开始时间 |
| endTime | String | 售卖结束时间 |
| status | String | 菜单状态（ACTIVE / INACTIVE） |
| dishes | List\<DishVO\> | 关联菜品列表 |

---

#### GET /api/menus/today — 今日菜单

- **JWT**：需要
- **前端页面**：首页 / 用户点餐页

**返回 `data`**：`List<DailyMenuVO>`

---

#### 内部接口（Feign 调用，前端不应直接使用）

| 方法 | 路径 | 说明 |
|------|------|------|
| PUT | `/api/menus/dishes/{id}/stock/deduct` | 扣减库存 |
| PUT | `/api/menus/dishes/{id}/stock/restore` | 恢复库存 |

> 这两个接口已被网关暴露，后端未做权限控制。前端请勿直接调用，库存操作应通过下单/取消订单流程间接触发。

---

### 3.3 订单服务（order-service，端口 9003）

#### POST /api/orders — 创建订单

- **JWT**：需要
- **建议角色**：STUDENT（实际未在后端强制校验）
- **前端页面**：确认下单页

**请求头**：网关自动注入 `X-User-Id`

**请求体**（JSON）：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| windowId | Long | 是 | 取餐窗口 ID |
| items | List\<OrderItemDTO\> | 是 | 菜品列表，至少 1 项 |

**OrderItemDTO**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dishId | Long | 是 | 菜品 ID |
| dishName | String | 是 | 菜品名称（快照） |
| price | BigDecimal | 是 | 单价，必须 > 0 |
| quantity | Integer | 是 | 数量，至少为 1 |

**返回 `data`**（OrderVO）：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单 ID |
| userId | Long | 下单用户 ID |
| windowId | Long | 取餐窗口 ID |
| totalAmount | BigDecimal | 订单总金额 |
| status | String | 订单状态枚举值 |
| statusDesc | String | 订单状态中文描述 |
| pickupNo | Integer | 取餐号 |
| pickupCode | String | 取餐码（核销验证码） |
| items | List\<OrderItemVO\> | 订单明细 |
| createdAt | String | 下单时间 |

**OrderItemVO**：
| 字段 | 类型 | 说明 |
|------|------|------|
| dishId | Long | 菜品 ID |
| dishName | String | 菜品名称 |
| price | BigDecimal | 单价 |
| quantity | Integer | 数量 |

---

#### GET /api/orders/{id} — 查询订单详情

- **JWT**：需要
- **前端页面**：订单详情页

**路径参数**：`id` — 订单 ID

**返回 `data`**（OrderVO）

---

#### GET /api/orders/my — 我的订单

- **JWT**：需要
- **前端页面**：我的订单列表

**请求头**：网关自动注入 `X-User-Id`

**返回 `data`**：`List<OrderVO>`

---

#### GET /api/orders/merchant/pending — 商家待处理订单

- **JWT**：需要
- **建议角色**：MERCHANT
- **前端页面**：商家后台 → 订单管理

**返回 `data`**：`List<OrderVO>`（状态为 CREATED 的订单）

---

#### PUT /api/orders/{id}/cancel — 取消订单

- **JWT**：需要
- **建议角色**：STUDENT
- **前端页面**：订单详情 → 取消按钮

**路径参数**：`id` — 订单 ID
**请求头**：`X-User-Id`（后端校验仅订单所属用户可取消）

**返回 `data`**（OrderVO）

**限制**：仅 CREATED 状态可取消。

---

#### PUT /api/orders/{id}/accept — 商家接单

- **JWT**：需要
- **建议角色**：MERCHANT
- **前端页面**：商家后台 → 接单按钮

**状态流转**：CREATED → ACCEPTED

**返回 `data`**（OrderVO）

---

#### PUT /api/orders/{id}/cooking — 开始制作

- **JWT**：需要
- **建议角色**：MERCHANT
- **前端页面**：商家后台 → 制作按钮

**状态流转**：ACCEPTED → COOKING

**返回 `data`**（OrderVO）

---

#### PUT /api/orders/{id}/ready — 备餐完成

- **JWT**：需要
- **建议角色**：MERCHANT
- **前端页面**：商家后台 → 备餐完成按钮

**状态流转**：COOKING → WAIT_PICKUP

**副作用**：后端自动调用 pickup-service 将订单加入取餐队列。

**返回 `data`**（OrderVO）

---

#### PUT /api/orders/{id}/complete — 取餐完成

- **JWT**：需要
- **说明**：由 pickup-service 核销后 Feign 调用，前端也可手动触发
- **前端页面**：商家后台（备用）

**状态流转**：WAIT_PICKUP → COMPLETED

**返回 `data`**（OrderVO）

---

### 3.4 取餐服务（pickup-service，端口 9004）

#### POST /api/pickup/windows — 新增取餐窗口

- **JWT**：需要
- **建议角色**：ADMIN
- **前端页面**：管理员后台 → 窗口管理

**请求体**（JSON）：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 窗口名称（如 "1号窗口"） |
| location | String | 否 | 窗口位置描述 |

**返回 `data`**（PickupWindowVO）：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 窗口 ID |
| name | String | 窗口名称 |
| location | String | 位置 |
| status | String | 状态枚举值（ACTIVE / DISABLED） |
| statusDesc | String | 状态中文描述 |
| createdAt | String | 创建时间 |

---

#### GET /api/pickup/windows — 窗口列表

- **JWT**：需要
- **前端页面**：点餐页（选择取餐窗口）、管理后台

**返回 `data`**：`List<PickupWindowVO>`

---

#### PUT /api/pickup/windows/{id}/enable — 启用窗口

- **JWT**：需要
- **建议角色**：ADMIN
- **前端页面**：管理员后台 → 窗口管理

**返回**：`Result<Void>`

---

#### PUT /api/pickup/windows/{id}/disable — 停用窗口

- **JWT**：需要
- **建议角色**：ADMIN
- **前端页面**：管理员后台 → 窗口管理

**返回**：`Result<Void>`

---

#### GET /api/pickup/windows/{windowId}/queue — 窗口排队队列

- **JWT**：需要
- **前端页面**：大屏展示 / 商家后台 → 叫号面板

**返回 `data`**：`List<PickupQueueVO>`

**PickupQueueVO**：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 队列记录 ID |
| windowId | Long | 窗口 ID |
| windowName | String | 窗口名称（仅 getWindowQueue 返回时有值） |
| orderId | Long | 关联订单 ID |
| pickupNo | Integer | 取餐号 |
| status | String | 队列状态枚举值 |
| statusDesc | String | 队列状态中文描述 |
| queueTime | String | 加入队列时间 |
| callTime | String | 叫号时间 |
| finishTime | String | 取餐完成时间 |

> 注意：此接口仅返回 WAITING 状态的记录。

---

#### POST /api/pickup/windows/{windowId}/call-next — 叫下一号

- **JWT**：需要
- **建议角色**：MERCHANT
- **前端页面**：商家后台 → 叫号按钮

**路径参数**：`windowId` — 窗口 ID

**返回 `data`**（PickupQueueVO）：被叫号的队列记录。

**副作用**：
1. 队列记录状态 WAITING → CALLED
2. **通过 WebSocket 向大屏推送叫号消息**

---

#### POST /api/pickup/verify — 取餐核销

- **JWT**：需要
- **建议角色**：MERCHANT
- **前端页面**：商家后台 → 核销输入框 / 扫码核销

**请求体**（JSON）：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pickupNo | Integer | 是 | 取餐号 |
| pickupCode | String | 是 | 取餐码 |

**返回 `data`**（PickupQueueVO）

**核销条件**：
1. pickupNo 对应的队列记录存在
2. pickupCode 匹配
3. 队列状态必须为 CALLED（已叫号）
4. 不能重复核销

**副作用**：
1. 队列状态 CALLED → FINISHED
2. Feign 调用 order-service 将订单状态 WAIT_PICKUP → COMPLETED

---

#### 内部接口（Feign 调用，前端不应直接使用）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/pickup/queue` | 加入取餐队列（由 order-service ready 时调用） |

---

### 3.5 WebSocket — 大屏实时推送

- **JWT**：不需要（放行路径）
- **前端页面**：食堂大屏

**连接地址**：`ws://localhost:8080/ws/pickup/screen`

**协议**：WebSocket

**推送消息格式**（ScreenMessage JSON）：
```json
{
  "type": "CALL",
  "windowId": 1,
  "windowName": "1号窗口",
  "currentPickupNo": 5,
  "waitingQueue": [
    {
      "id": 2,
      "windowId": 1,
      "windowName": "1号窗口",
      "orderId": 12,
      "pickupNo": 6,
      "status": "WAITING",
      "statusDesc": "排队中",
      "queueTime": "2026-05-01T12:00:00",
      "callTime": null,
      "finishTime": null
    }
  ],
  "timestamp": "2026-05-01T12:05:00",
  "message": "请 5 号到【1号窗口】取餐"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| type | String | 消息类型，当前仅有 "CALL" |
| windowId | Long | 窗口 ID |
| windowName | String | 窗口名称 |
| currentPickupNo | Integer | 当前叫号号码 |
| waitingQueue | List\<PickupQueueVO\> | 更新后的等待队列 |
| timestamp | String | 推送时间 |
| message | String | 大屏展示文案 |

**连接行为**：
- 连接后只接收推送，不处理上行消息
- 多个大屏客户端可同时连接，广播到所有连接

---

## 4. 枚举速查表

### 4.1 订单状态（OrderStatus）

| 枚举值 | 中文描述 | 说明 |
|--------|----------|------|
| CREATED | 已创建 | 用户已下单，等待商家接单 |
| ACCEPTED | 商家已接单 | 商家确认接单 |
| COOKING | 制作中 | 商家正在备餐 |
| WAIT_PICKUP | 待取餐 | 备餐完成，等待用户取餐 |
| COMPLETED | 已完成 | 用户已取餐，订单完成 |
| CANCELLED | 已取消 | 订单已取消（仅 CREATED 状态可取消） |

**状态流转图**：
```
CREATED → ACCEPTED → COOKING → WAIT_PICKUP → COMPLETED
    ↓
CANCELLED
```

### 4.2 菜品状态（DishStatus）

| 枚举值 | 中文描述 |
|--------|----------|
| ON_SALE | 上架中 |
| OFF_SALE | 已下架 |

### 4.3 用户角色（UserRole）

| 枚举值 | 中文描述 | 说明 |
|--------|----------|------|
| STUDENT | 学生 | 默认角色，可点餐 |
| MERCHANT | 商家 | 接单、备餐、叫号、核销 |
| ADMIN | 管理员 | 管理窗口、管理菜品 |

### 4.4 用户状态（UserStatus）

| 枚举值 | 中文描述 |
|--------|----------|
| NORMAL | 正常 |
| DISABLED | 已禁用 |

### 4.5 取餐队列状态（PickupQueueStatus）

| 枚举值 | 中文描述 | 说明 |
|--------|----------|------|
| WAITING | 排队中 | 等待叫号 |
| CALLED | 已叫号 | 已叫号，等待取餐核销 |
| FINISHED | 已取餐 | 核销完成 |
| CANCELLED | 已取消 | 取餐号已取消 |

### 4.6 窗口状态（WindowStatus）

| 枚举值 | 中文描述 |
|--------|----------|
| ACTIVE | 启用 |
| DISABLED | 停用 |

### 4.7 餐段（MealPeriod）

| 枚举值 | 中文描述 |
|--------|----------|
| BREAKFAST | 早餐 |
| LUNCH | 午餐 |
| DINNER | 晚餐 |

---

## 5. 前端页面规划建议

基于后端接口，前端至少需要以下页面：

| 页面 | 路由（建议） | 需要的接口 | JWT |
|------|-------------|-----------|-----|
| 登录页 | `/login` | POST /api/users/login | 否 |
| 注册页 | `/register` | POST /api/users/register | 否 |
| 首页/点餐 | `/` | GET /api/menus/today, GET /api/menus/dishes, GET /api/pickup/windows | 是 |
| 菜品详情 | `/dish/:id` | GET /api/menus/dishes/{id} | 是 |
| 确认下单 | `/order/confirm` | POST /api/orders | 是 |
| 我的订单 | `/orders` | GET /api/orders/my | 是 |
| 订单详情 | `/order/:id` | GET /api/orders/{id}, PUT /api/orders/{id}/cancel | 是 |
| 个人中心 | `/profile` | GET /api/users/me, PUT /api/users/me | 是 |
| 商家后台-订单 | `/merchant/orders` | GET /api/orders/merchant/pending, PUT accept/cooking/ready | 是 |
| 商家后台-叫号核销 | `/merchant/pickup` | GET /api/pickup/windows/{id}/queue, POST call-next, POST /api/pickup/verify | 是 |
| 商家后台-菜品管理 | `/merchant/dishes` | POST/PUT/DELETE /api/menus/dishes | 是 |
| 商家后台-每日菜单 | `/merchant/menus` | POST /api/menus/daily | 是 |
| 管理员-窗口管理 | `/admin/windows` | POST/GET /api/pickup/windows, PUT enable/disable | 是 |
| 食堂大屏 | `/screen` | WebSocket ws://host:8080/ws/pickup/screen | 否 |

---

## 6. 前端对接注意事项

以下问题基于代码实际扫描发现，供前端对接时参考：

### 6.1 角色权限未在后端强制校验

所有接口仅通过网关 JWT 层校验登录状态，Controller 层**未校验 X-User-Role**。例如：取消订单接口任何角色都能调用（只要能通过 JWT 校验），只是订单所属用户校验在后端 Service 层做了。前端应在前端层面限制页面/按钮的可见性，避免用户误操作。

### 6.2 无商家/管理员注册入口

`/api/users/register` 固定注册为 STUDENT 角色。商家和管理员账号需通过数据库直接插入。前端登录页无需提供"角色选择"。

### 6.3 内部 Feign 接口已暴露

以下接口是为微服务间 Feign 调用设计的，但已被网关路由暴露到公网：

- `PUT /api/menus/dishes/{id}/stock/deduct` — 扣库存
- `PUT /api/menus/dishes/{id}/stock/restore` — 恢复库存
- `POST /api/pickup/queue` — 加入取餐队列

这些接口没有权限校验，**前端请勿直接调用**。库存变更是通过下单/取消订单流程间接触发的；加入取餐队列是备餐完成时自动触发的。

### 6.4 缺少分页支持

菜品列表（GET /api/menus/dishes）、订单列表（GET /api/orders/my）、队列列表（GET /api/pickup/windows/{id}/queue）均**无分页参数**，返回全部记录。数据量大时前端需自行处理。

### 6.5 无菜品图片上传接口

DishDTO 中有 `imageUrl` 字段（字符串），但后端未提供文件上传接口。前端可让用户填入图片 URL，或后续扩展上传接口。

### 6.6 商家订单接口仅返回 CREATED 状态

`GET /api/orders/merchant/pending` 只返回 `CREATED`（已创建）状态的订单。接单后的订单没有单独的商家视角列表接口。如需追踪在途订单，需使用通用查询接口或前端自己维护。

### 6.7 JSON 字段命名

后端使用 camelCase（驼峰命名）序列化 JSON，与 Java 对象字段名一致。前端无需做 snake_case 转换。

### 6.8 取餐窗口需"启用"后才可用于下单

创建窗口时默认状态为 ACTIVE。如果窗口被停用（DISABLED），下单时选择该窗口后端不会主动校验窗口状态，但取餐叫号时可能遇到窗口已停用的问题。建议前端在展示可选窗口列表时过滤 DISABLED 的窗口。

### 6.9 大屏 WebSocket 仅推送 CALL 消息

当前 WebSocket 仅在"叫下一号"时推送 `type: "CALL"` 消息。以下场景**不会**推送：
- 新订单加入队列
- 核销完成
- 窗口创建/启用/停用

大屏的初始数据（等待队列）需要通过 REST API `GET /api/pickup/windows/{windowId}/queue` 主动拉取。

### 6.10 PickupQueueVO 不含用户信息

队列 VO 不包含 `userId`、`nickname` 等用户字段。大屏只能展示取餐号，无法展示用户昵称。这是后端接口设计决定的，前端无法绕过。

### 6.11 Token 过期处理

AccessToken 24 小时过期。前端应在收到 401 时自动调用 `/api/users/refresh-token` 用 refreshToken 换取新 token，再重试原请求。若 refreshToken 也过期（7 天），则跳转登录页。

### 6.12 命名风格不统一（已知问题）

- common 模块同时存在 `ResultCode` 和 `ErrorCode` 两套错误码枚举，功能重叠但值不一致（如业务异常：ResultCode 无 1001，ErrorCode 的 BUSINESS_ERROR=1001）。后端实际混用，前端按 code 数值判断即可。
- `ErrorResponse.java` 存在于 common 模块但未被任何 Controller 使用（全局异常处理器直接返回 Result）。

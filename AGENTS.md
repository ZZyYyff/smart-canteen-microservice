# AGENTS.md — 智能食堂点餐与取餐微服务系统

## 项目概况

面向校园/园区的智能食堂微服务系统，支持用户在线点餐、商家接单备餐、用户扫码或取餐码取餐、食堂大屏实时显示取餐队列。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 构建 | Maven | 多模块 |
| 框架 | Spring Boot | 3.2.x |
| 微服务 | Spring Cloud | 2023.x |
| 注册发现 | Spring Cloud Alibaba Nacos Discovery | — |
| 远程调用 | Spring Cloud OpenFeign | — |
| 网关 | Spring Cloud Gateway | — |
| 数据库 | MySQL | 8 |
| 缓存 | Redis | — |
| 认证 | JWT | — |
| 实时通信 | WebSocket | — |
| 测试 | JUnit 5 + Mockito | — |
| 部署 | Docker + K3S | — |

## 模块结构

```
smart-canteen/
├── common/                 # 公共模块（通用类、工具、异常、Result）
├── gateway-service/        # 网关服务（路由、JWT 校验）
├── user-service/           # 用户服务（注册、登录、个人信息）
├── menu-service/           # 菜品与菜单服务（菜品 CRUD、分类、库存）
├── order-service/          # 订单服务（下单、状态流转）
├── pickup-service/         # 取餐与排队服务（取餐队列、核销、大屏 WebSocket）
└── docs/                   # 文档
```

## 编码规范

### 分层架构（每模块内部）

```
controller/    → 仅参数接收、调用 Service、返回 Result<T>（不写业务逻辑）
service/       → 业务逻辑、事务管理、跨服务调用编排
mapper/        → 数据访问（MyBatis / JPA Repository）
entity/        → 数据库实体
dto/           → 数据传输对象（跨层/跨服务）
vo/            → 视图对象（返回前端）
```

### 统一返回格式

所有 Controller 接口返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 异常处理

- 业务异常统一使用 `BusinessException`（定义在 common 模块）
- 全局异常处理器 `GlobalExceptionHandler` 捕获并转为 `Result`

### 命名规范

- 类名、方法名：英文，语义清晰（如 `OrderService.createOrder`）
- 注释：中文
- 实体类名与表名对应：`User` → `users`
- DTO 后缀 `DTO`，VO 后缀 `VO`

### 避免超大文件

- 单个类不超过 300 行
- 单个方法不超过 50 行
- 职责单一，及时拆分

## 微服务交互规则

### 服务注册

每个服务必须有 `application.yml`，配置 `spring.cloud.nacos.discovery`，启动后注册到 Nacos。

### 服务间调用

服务间调用使用 OpenFeign，定义 `feign/` 包存放 Feign 接口：

| 调用方 | 被调用方 | 场景 |
|--------|----------|------|
| order-service | menu-service | 下单时扣减库存 |
| order-service | pickup-service | 订单进入"待取餐"后加入取餐队列 |
| pickup-service | order-service | 核销成功后修改订单状态为"已取餐" |

### 网关

- `gateway-service` 统一校验 JWT
- 登录（`/api/users/login`）、注册（`/api/users/register`）和 Token 刷新（`/api/users/refresh-token`）放行
- 大屏 WebSocket（`/ws/pickup/screen`）放行
- 其余接口要求请求头 `Authorization: Bearer <token>`

### WebSocket

- `pickup-service` 提供 WebSocket 端点，向食堂大屏推送实时取餐队列

## 编译与测试要求

1. 代码必须可编译运行，**禁止伪代码或 TODO 占位**
2. 每完成一个模块后运行 `mvn test` 并修复所有错误
3. 核心功能（下单、扣库存、取餐核销、排队推送）必须覆盖单元测试或集成测试
4. 测试类放在各模块 `src/test/java` 下，包名与源码对应

## 文档要求

在 `docs/` 目录生成以下文档（Markdown 格式）：

1. 需求规格说明书
2. 概要设计说明书
3. 详细设计说明书
4. 测试用例
5. 测试报告
6. 架构设计文档
7. K3S 部署方案说明
8. 前端接口映射文档 (frontend-api-map.md)

## 工作原则

- **先规划再编码**：每阶段开始前明确要实现的模块与接口清单，经确认后执行
- **逐模块推进**：按 common → user-service → menu-service → order-service → pickup-service → gateway-service 顺序开发
- **每模块自测通过后再进入下一模块**
- **保持项目随时可编译**：不在中途遗留编译错误
- **不猜测**：需求模糊时主动确认，不自行发挥

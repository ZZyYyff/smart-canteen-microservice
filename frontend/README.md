# 智能食堂点餐与取餐系统 — 前端

## 1. 项目介绍

本项目是"智能食堂点餐与取餐微服务系统"的前端应用，面向校园/园区食堂场景，提供学生在线点餐、商家接单备餐、食堂大屏实时叫号等功能。

前端采用 Vue 3 全家桶构建，通过 HTTP REST API 与后端 Spring Cloud 微服务集群通信，通过 WebSocket 接收食堂大屏实时推送。

## 2. 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 | 3.4.x |
| 构建 | Vite | 5.3.x |
| 语言 | TypeScript | 5.5.x |
| 路由 | Vue Router | 4.4.x |
| 状态管理 | Pinia | 2.1.x |
| UI 组件库 | Element Plus | 2.7.x |
| HTTP 客户端 | Axios | 1.7.x |
| 日期处理 | dayjs | 1.11.x |
| 二维码生成 | qrcode.vue | 3.4.x |
| 样式预处理 | Sass | 1.77.x |

## 3. 页面功能说明

### 3.1 公共页面

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | `/login` | 支持手机号/学工号 + 密码登录，登录后按角色自动跳转 |
| 注册页 | `/register` | 新用户注册（手机号、学工号、昵称、密码），默认注册为学生 |
| 取餐大屏 | `/screen/pickup` | 全屏深色背景，WebSocket 实时接收叫号推送，适合大屏投屏 |

### 3.2 学生端

| 页面 | 路由 | 说明 |
|------|------|------|
| 首页 | `/student/home` | 统计卡片、待取餐提醒、快捷操作入口 |
| 今日菜单 | `/student/menu` | 按餐段展示菜品卡片，支持加入购物车 |
| 购物车 | `/student/cart` | 菜品数量调整、删除、选择取餐窗口、提交订单 |
| 我的订单 | `/student/orders` | 订单列表，状态标签，点击查看详情 |
| 订单详情 | `/student/orders/:id` | 订单明细、取餐号、取餐码二维码、取消订单 |
| 个人中心 | `/student/profile` | 查看/修改个人信息（昵称、手机号） |

### 3.3 商家端

| 页面 | 路由 | 说明 |
|------|------|------|
| 工作台 | `/merchant/home` | 今日统计（订单数、待接单、制作中、待取餐） |
| 菜品管理 | `/merchant/menu` | 菜品增删改查、上下架、库存管理、低库存预警 |
| 订单处理 | `/merchant/orders` | 按状态筛选，接单 → 制作 → 备餐完成 状态流转 |
| 叫号核销 | `/merchant/pickup` | 窗口选择、叫下一号、取餐码核销 |

### 3.4 管理员端

| 页面 | 路由 | 说明 |
|------|------|------|
| 仪表盘 | `/admin/home` | 系统统计、模块说明、快速入口 |
| 用户管理 | `/admin/users` | 用户列表占位（后端暂未提供接口） |
| 菜单管理 | `/admin/menus` | 全局菜品管理，支持搜索和状态筛选 |
| 系统配置 | `/admin/system` | 环境信息、服务模块、功能清单 |

## 4. 目录结构

```
frontend/
├── index.html                       # 入口 HTML
├── package.json                     # 依赖与脚本
├── tsconfig.json                    # TypeScript 配置
├── tsconfig.node.json               # Node 端 TS 配置
├── vite.config.ts                   # Vite 构建与代理配置
├── .env.development                 # 开发环境变量
├── .env.production                  # 生产环境变量
├── public/
│   └── vite.svg                     # 网站图标
└── src/
    ├── main.ts                      # 应用入口，注册 Element Plus / Pinia / Router
    ├── App.vue                      # 根组件
    ├── env.d.ts                     # 环境变量类型声明
    ├── api/                         # API 请求封装
    │   ├── request.ts               # Axios 实例、JWT 拦截器、响应解包、401 处理
    │   ├── user.ts                  # 用户接口（登录、注册、个人信息）
    │   ├── menu.ts                  # 菜品与菜单接口
    │   ├── order.ts                 # 订单接口
    │   └── pickup.ts                # 取餐窗口与队列接口
    ├── router/
    │   └── index.ts                 # 路由配置、角色守卫、登录拦截
    ├── stores/
    │   ├── auth.ts                  # 用户认证状态（token、userInfo、role）
    │   ├── cart.ts                  # 购物车状态（localStorage 持久化）
    │   └── pickup.ts                # 大屏 WebSocket 状态
    ├── types/
    │   └── api.ts                   # 共享 TypeScript 类型定义
    ├── utils/
    │   ├── auth.ts                  # token / userInfo 的 localStorage 存取
    │   └── websocket.ts             # WebSocket 客户端（自动重连）
    ├── layouts/
    │   ├── StudentLayout.vue        # 学生端布局（顶部导航）
    │   ├── MerchantLayout.vue       # 商家端布局（侧边菜单 + 顶部栏）
    │   ├── AdminLayout.vue          # 管理员端布局（侧边菜单 + 顶部栏）
    │   └── ScreenLayout.vue         # 大屏布局（全屏深色）
    ├── components/
    │   ├── AppHeader.vue            # 顶部栏组件
    │   ├── SideMenu.vue             # 侧边菜单组件
    │   ├── StatCard.vue             # 统计卡片组件
    │   ├── EmptyState.vue           # 空数据占位组件
    │   ├── OrderStatusTag.vue       # 订单状态标签组件
    │   └── DishCard.vue             # 菜品卡片组件
    ├── views/
    │   ├── auth/
    │   │   ├── Login.vue            # 登录页
    │   │   └── Register.vue         # 注册页
    │   ├── student/                 # 学生端页面（6 个）
    │   ├── merchant/                # 商家端页面（4 个）
    │   ├── admin/                   # 管理员端页面（4 个）
    │   └── screen/
    │       └── PickupScreen.vue     # 大屏页面
    └── styles/
        ├── global.scss              # 全局样式 Reset
        └── global.css               # CSS 变量、布局类、组件样式
```

## 5. 环境要求

| 依赖 | 版本要求 |
|------|----------|
| Node.js | ≥ 18.x |
| npm | ≥ 9.x |
| 后端服务 | 网关 8080 端口可访问 |
| 浏览器 | Chrome / Edge / Firefox 最新版 |

## 6. 安装依赖

```bash
cd frontend
npm install
```

## 7. 开发环境运行

```bash
npm run dev
```

默认启动在 `http://localhost:5173`。

Vite 开发服务器已配置代理：
- `/api` 请求代理到 `http://localhost:8080`
- `/ws` WebSocket 代理到 `ws://localhost:8080`

因此本地开发时无需处理跨域问题。

如需直连后端网关（不经过 Vite 代理），可修改 `.env.development`：

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=ws://localhost:8080
```

## 8. 打包

```bash
npm run build
```

产出在 `dist/` 目录，可直接部署到 Nginx 等静态服务器。

生产环境配置在 `.env.production` 中：

```env
VITE_API_BASE_URL=/api
VITE_WS_BASE_URL=/ws
```

生产部署时需在 Nginx 中配置反向代理将 `/api` 和 `/ws` 转发到后端网关。

Nginx 配置示例：

```nginx
server {
    listen 80;
    server_name canteen.example.com;

    root /var/www/smart-canteen/dist;
    index index.html;

    # 前端 SPA 路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端网关
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
    }

    # WebSocket 代理
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

## 9. 后端网关地址配置

| 环境 | 变量 | 默认值 |
|------|------|--------|
| 开发 | `VITE_API_BASE_URL` | `http://localhost:8080`（或通过 Vite 代理） |
| 生产 | `VITE_API_BASE_URL` | `/api`（Nginx 反向代理） |

## 10. WebSocket 地址配置

| 环境 | 变量 | 默认值 |
|------|------|--------|
| 开发 | `VITE_WS_BASE_URL` | `ws://localhost:8080`（或通过 Vite 代理） |
| 生产 | `VITE_WS_BASE_URL` | `/ws`（Nginx 反向代理） |

WebSocket 端点固定为 `/ws/pickup/screen`，由 pickup-service 提供。

## 11. 登录演示账号

后端默认注册接口创建的是**学生**角色账号。商家和管理员账号需直接在数据库中插入或通过数据库工具创建。

如需演示不同角色，可使用以下方式在数据库中预设账号：

```sql
-- 学生账号（可通过注册页面创建）
-- 手机号: 13800000001  密码: 123456

-- 商家账号（需直接插入）
INSERT INTO users (phone, student_no, password, nickname, role, status, created_at, updated_at)
VALUES ('13800000002', 'M001', '$2a$10$...', '张商家', 'MERCHANT', 'NORMAL', NOW(), NOW());
-- 密码需使用 BCrypt 加密，可在后端 user-service 测试类中生成

-- 管理员账号（需直接插入）
INSERT INTO users (phone, student_no, password, nickname, role, status, created_at, updated_at)
VALUES ('13800000003', 'A001', '$2a$10$...', '李管理', 'ADMIN', 'NORMAL', NOW(), NOW());
```

> 提示：BCrypt 加密后的密码可通过运行后端 `UserServiceTest` 测试或在线 BCrypt 工具生成。推荐测试密码为 `123456`。

## 12. 学生端演示流程

1. 打开浏览器访问 `http://localhost:5173`
2. 点击"立即注册"，填写手机号、学工号、昵称、密码，完成注册
3. 注册成功后跳转登录页，输入手机号和密码登录
4. 登录后进入学生首页，可看到统计卡片和待取餐提醒
5. 点击"去点餐"或顶部"今日菜单"，浏览今日菜品
6. 点击菜品上的"加入购物车"按钮
7. 点击顶部"购物车"角标进入购物车页
8. 调整菜品数量，选择取餐窗口，点击"提交订单"
9. 下单成功后自动跳转订单详情页，可查看取餐号和取餐码
10. 点击顶部"我的订单"可查看所有订单
11. 点击"个人中心"可修改昵称和手机号

## 13. 商家端演示流程

1. 使用商家账号登录，自动进入商家后台
2. 首页可看到今日订单统计和最近订单
3. 点击左侧"菜品管理"，可新增菜品（填写名称、价格、库存等）
4. 菜品创建后默认为上架状态，可点击"下架"暂停售卖
5. 点击左侧"订单管理"进入订单处理页
6. 选择"待接单" Tab，点击"接单"按钮
7. 切换到"已接单" Tab，点击"开始制作"
8. 切换到"制作中" Tab，点击"制作完成"（此时订单进入取餐队列）
9. 点击左侧"叫号核销"进入取餐管理页
10. 选择取餐窗口，点击"叫下一号"
11. 此时大屏接收到 WebSocket 推送，显示叫号信息
12. 学生出示取餐码，在右侧输入取餐号和取餐码，点击"确认核销"

## 14. 大屏演示流程

1. 直接在浏览器访问 `http://localhost:5173/screen/pickup`
2. 大屏以全屏深色模式显示，顶部展示当前日期和时间
3. 页面自动连接 WebSocket（`ws://localhost:8080/ws/pickup/screen`）
4. 左侧大面积显示当前叫号（160px 超大号数字）
5. 右侧显示等待队列（最多 8 条）
6. 商家每次"叫下一号"时，大屏实时更新
7. 如 WebSocket 未连接，顶部显示"正在重连..."橙色提示条
8. 无叫号时显示"暂无叫号信息"

> 建议使用 Chrome 全屏模式（F11）投屏到食堂大屏幕。

## 15. 常见问题

### 登录失败

**现象**：点击登录后提示"用户不存在"或"密码错误"。

**排查**：
1. 确认后端服务已启动，网关（8080）可访问
2. 确认账号已在数据库中注册
3. 尝试注册新账号后重新登录
4. 检查浏览器控制台 Network 面板，确认请求是否到达后端

### 跨域问题

**现象**：浏览器控制台提示 CORS 错误。

**解决**：
1. 开发环境使用 `npm run dev` 启动，Vite 已配置代理，不会出现跨域
2. 如果直连后端网关（不经过 Vite 代理），需确保后端网关已配置 CORS 允许
3. 生产环境使用 Nginx 反向代理，前后端同域，不存在跨域

### 接口 401

**现象**：页面提示"Token 无效或已过期"。

**原因**：JWT AccessToken 有效期为 24 小时。Token 过期后需重新登录，或调用刷新接口使用 RefreshToken（7 天有效）换取新 Token。

**解决**：退出重新登录。生产环境中可在 Axios 拦截器中自动刷新 Token（当前已预留拦截器代码位置）。

### 菜单加载失败

**现象**：学生端"今日菜单"页面显示空白或"暂无菜品"。

**排查**：
1. 确认 menu-service（9002）已启动且注册到 Nacos
2. 确认商家已在后台创建菜品并上架
3. 确认商家已创建**当天的**每日菜单
4. 检查菜品状态是否为 ON_SALE

### WebSocket 连接失败

**现象**：大屏页面显示"正在重连..."，叫号信息不更新。

**排查**：
1. 确认 pickup-service（9004）已启动
2. 确认 WebSocket 端点 `/ws/pickup/screen` 未被网关限流阻挡
3. 检查 `VITE_WS_BASE_URL` 配置是否正确
4. 大屏会自动重连（退避间隔：2s → 3s → 4.5s → 上限 30s）
5. 即使 WebSocket 不可用，页面也不会崩溃，仅显示"暂无叫号信息"

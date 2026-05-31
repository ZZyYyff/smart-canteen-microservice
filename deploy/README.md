# deploy — Docker Compose 部署

## 文件说明

| 文件 | 说明 |
|------|------|
| `docker-compose.yml` | MySQL 8 + Redis 7 + Nacos 2.3.2 容器编排 |
| `sql/init.sql` | 数据库建表 + 演示数据初始化脚本 |

## 端口映射

| 服务 | 宿主机端口 | 容器端口 |
|------|:---:|:---:|
| MySQL | 3307 | 3306 |
| Redis | 6379 | 6379 |
| Nacos | 8848 | 8848 |
| Nacos gRPC | 9848 | 9848 |

> MySQL 宿主机使用 3307，避免与本机已安装的 MySQL（3306）冲突。

## 启动

```bash
cd deploy
docker compose up -d
```

## 查看状态

```bash
docker compose ps
```

三个容器均显示 `Up` 和 `healthy` 即为正常。

## 查看日志

```bash
# 全部日志
docker compose logs

# 单个服务日志
docker compose logs mysql
docker compose logs redis
docker compose logs nacos
```

## 停止

```bash
# 停止但不删除容器和数据
docker compose down

# 停止并删除数据卷（会清空数据库）
docker compose down -v
```

## 数据库初始化

`sql/init.sql` 在容器首次启动时自动执行，创建以下表并插入演示数据：

- `users` — 用户表（3 个内置账号）
- `dishes` — 菜品表（5 个演示菜品）
- `orders` / `order_items` — 订单表
- `pickup_windows` / `pickup_queue` — 取餐窗口与队列表
- `daily_menus` / `daily_menu_items` — 每日菜单表

## 进入 MySQL 容器

```bash
docker exec -it smart-canteen-mysql mysql -uroot -proot smart_canteen
```

## Nacos 控制台

http://localhost:8848/nacos

账号 / 密码：`nacos` / `nacos`

## 演示账号

数据库初始化后包含 3 个内置用户，密码均为 `123456`：

| 角色 | 手机号 | 学工号 | 昵称 |
|------|--------|--------|------|
| 学生 | `13800000001` | `2024001` | 张三 |
| 商家 | `13800000002` | `2024002` | 李四 |
| 管理员 | `13800000003` | `2024003` | 管理员 |

## 中文编码

所有数据库连接已配置 UTF-8：
- MySQL 容器启动参数：`--character-set-server=utf8mb4` + `--collation-server=utf8mb4_unicode_ci`
- JDBC URL 包含 `characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci`
- Spring Boot 配置 `server.servlet.encoding.charset=UTF-8`

如果出现中文乱码，可执行修复脚本：

```bash
docker exec -i smart-canteen-mysql mysql -uroot -proot --default-character-set=utf8mb4 < sql/fix-demo-data-encoding.sql
```

## 常见问题

### 端口冲突

MySQL 宿主机使用 3307 映射，避免与本地已安装的 MySQL（3306）冲突。如果 3307 也被占用，修改 `docker-compose.yml` 中的 host port。

### Nacos 启动较慢

Nacos 容器健康检查需要等待 HTTP 就绪响应。首次启动可能需 30-60 秒，如果后端服务报 `UNAVAILABLE: io exception`，等待片刻后重试即可。

### 数据库无默认今日菜单

`init.sql` 不包含每日菜单（`daily_menus`）的初始化数据。**演示前必须由商家登录前端或通过 API 创建今日菜单**，否则学生端"今日菜单"页面为空。

### 中文乱码

如果数据库中的中文数据显示为乱码：
1. 确认 JDBC URL 包含 `characterEncoding=UTF-8`
2. 运行 `fix-demo-data-encoding.sql` 修复已有数据
3. 检查 MySQL 容器字符集：`docker exec smart-canteen-mysql mysql -uroot -proot -e "SHOW VARIABLES LIKE 'character%';"`

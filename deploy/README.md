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

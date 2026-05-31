# k8s — K3S / Kubernetes 部署配置

## 文件说明

| 文件 | 说明 |
|------|------|
| `namespace.yaml` | 命名空间定义 |
| `mysql.yaml` | MySQL 8 Deployment + Service |
| `redis.yaml` | Redis 7 Deployment + Service |
| `nacos.yaml` | Nacos 2.3.2 Deployment + Service |
| `user-service.yaml` | 用户服务 Deployment + Service (:9001) |
| `menu-service.yaml` | 菜品服务 Deployment + Service (:9002) |
| `order-service.yaml` | 订单服务 Deployment + Service (:9003) |
| `pickup-service.yaml` | 取餐服务 Deployment + Service (:9004) |
| `gateway-service.yaml` | 网关服务 Deployment + Service (:8080) |
| `configmap.yaml` | ConfigMap 公共配置 |
| `ingress.yaml` | Ingress 入口规则 |

## 部署前提

- K3S 或 Kubernetes 集群已就绪
- `kubectl` 已配置指向目标集群
- 各微服务 Docker 镜像已构建并推送至集群可访问的镜像仓库

## 部署步骤

### 1. 创建命名空间

```bash
kubectl apply -f namespace.yaml
```

### 2. 部署基础设施

```bash
kubectl apply -f mysql.yaml
kubectl apply -f redis.yaml
kubectl apply -f nacos.yaml
```

等待 Pod 就绪：

```bash
kubectl -n smart-canteen get pods -w
```

### 3. 部署 ConfigMap

```bash
kubectl apply -f configmap.yaml
```

### 4. 部署微服务

```bash
kubectl apply -f user-service.yaml
kubectl apply -f menu-service.yaml
kubectl apply -f order-service.yaml
kubectl apply -f pickup-service.yaml
kubectl apply -f gateway-service.yaml
```

### 5. 部署 Ingress

```bash
kubectl apply -f ingress.yaml
```

### 6. 查看部署状态

```bash
kubectl -n smart-canteen get all
```

## 服务端口

| 服务 | Cluster Port | 说明 |
|------|:---:|------|
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 注册中心 |
| Nacos gRPC | 9848 | 服务发现 gRPC |
| Gateway | 8080 | API 网关 |
| User Service | 9001 | 用户服务 |
| Menu Service | 9002 | 菜品服务 |
| Order Service | 9003 | 订单服务 |
| Pickup Service | 9004 | 取餐服务 |

## 镜像构建

各微服务需先构建 Docker 镜像，Dockerfile 位于各模块根目录。

```bash
# 示例
cd user-service
docker build -t smart-canteen/user-service:latest .
```

## 详细部署方案

参见 [docs/07-K3S部署方案说明.md](../docs/07-K3S部署方案说明.md)。

## 课程演示注意事项

> ⚠️ 本 K3S 部署配置为课程演示环境设计，以下限制需注意：

| 限制 | 说明 | 影响 |
|------|------|------|
| MySQL 使用 emptyDir | 数据存储在 Pod 临时目录 | Pod 重启后数据库数据丢失，演示过程中**不要重启 MySQL Pod** |
| 密码明文 | YAML 中 `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD` 等以明文书写 | 课程演示可接受，生产环境需使用 K8S Secret |
| Nacos 无持久化 | standalone 模式使用内嵌 Derby | Pod 重启后服务需重新注册（会自愈，约等待 30 秒） |
| 镜像标签 `:latest` | `imagePullPolicy: IfNotPresent` | 更新镜像后需手动删除旧 Pod 触发重新拉取 |
| 无资源限制 | 未设置 `resources.requests/limits` | 内存不足时 Pod 可能被 OOM Kill |

### 演示前检查清单

- [ ] 所有 Pod 状态为 `Running`
- [ ] Nacos 控制台（`http://<node-ip>:8848/nacos`）中 5 个服务均已注册
- [ ] 商家已登录前端创建今日菜单（数据库中无默认菜单数据）
- [ ] 大屏页面 WebSocket 连接正常

## 删除部署

```bash
kubectl delete -f ingress.yaml
kubectl delete -f gateway-service.yaml
kubectl delete -f pickup-service.yaml
kubectl delete -f order-service.yaml
kubectl delete -f menu-service.yaml
kubectl delete -f user-service.yaml
kubectl delete -f configmap.yaml
kubectl delete -f nacos.yaml
kubectl delete -f redis.yaml
kubectl delete -f mysql.yaml
kubectl delete -f namespace.yaml
```

> 删除 namespace 会同时删除该命名空间下的所有资源。

## 集群验证提醒

- K3S 真实集群部署需要在 Ubuntu VM 或 K3S 环境中**人工验证**
- 首次部署建议按顺序逐个 apply 并等待 Pod ready 后再继续
- 如果 Pod 处于 `ImagePullBackOff`，检查镜像是否已构建并导入 K3S（`k3s ctr images import`）

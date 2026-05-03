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

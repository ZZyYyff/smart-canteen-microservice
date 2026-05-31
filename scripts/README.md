# scripts — 一键启动与停止脚本

## 文件说明

| 文件 | 说明 |
|------|------|
| `start-backend.ps1` | PowerShell 启动脚本（自动 Docker + 编译 + 启动 5 个微服务） |
| `stop-backend.ps1` | PowerShell 停止脚本（三层兜底停止所有进程） |
| `start-backend.bat` | CMD 启动入口（双击可用） |
| `stop-backend.bat` | CMD 停止入口（双击可用） |
| `cleanup-vm.sh` | Ubuntu VM 磁盘清理脚本（K3S 环境维护用） |

## 使用方法

### 启动

**PowerShell（推荐）：**

```powershell
cd smart-canteen
.\scripts\start-backend.ps1
```

**CMD：** 双击 `scripts\start-backend.bat`

### 停止

```powershell
# 仅停止 Java 微服务，保留 MySQL/Redis/Nacos
.\scripts\stop-backend.ps1

# 连 Docker 基础设施一起停
.\scripts\stop-backend.ps1 -WithDocker
```

**CMD：** 双击 `scripts\stop-backend.bat`

## start-backend.ps1 执行流程

| 步骤 | 内容 |
|:--:|------|
| 1 | 检查 `docker`、`java`、`mvn` 命令是否可用 |
| 2 | 停止旧的后端进程（端口检测 + Java 命令行扫描） |
| 3 | `docker compose up -d` 启动基础设施 |
| 4 | 等待 MySQL :3307、Redis :6379、Nacos :8848 / :9848 就绪 |
| 5 | `mvn clean package -DskipTests` 编译项目 |
| 6 | 按序启动 `user-service → menu-service → order-service → pickup-service → gateway-service`，每个使用 `mvn spring-boot:run` |

## stop-backend.ps1 停止策略

| 策略 | 方式 |
|:--:|------|
| 1 | 读取 `logs/pids/*.pid` 按逆序停止 |
| 2 | 通过 `Get-NetTCPConnection` 检测端口占用并停止 |
| 3 | 扫描 `java.exe` 命令行关键字，强制清理残留进程 |

## 日志与 PID

启动后自动在项目根目录生成：

```
logs/
├── startup.log               # 启动脚本自身日志
├── user-service.out.log      # 用户服务标准输出
├── user-service.err.log      # 用户服务标准错误
├── menu-service.out.log      # 菜品服务标准输出
├── menu-service.err.log      # 菜品服务标准错误
├── order-service.out.log     # 订单服务标准输出
├── order-service.err.log     # 订单服务标准错误
├── pickup-service.out.log    # 取餐服务标准输出
├── pickup-service.err.log    # 取餐服务标准错误
├── gateway-service.out.log   # 网关标准输出
├── gateway-service.err.log   # 网关标准错误
└── pids/
    ├── user-service.pid
    ├── menu-service.pid
    ├── order-service.pid
    ├── pickup-service.pid
    └── gateway-service.pid
```

## 环境要求

- Windows PowerShell 5.1 或更高
- Docker Desktop 已安装并运行
- Java 17+，`java` 命令在 PATH 中
- Maven 3.8+，`mvn` 命令在 PATH 中
- 项目根目录包含 `pom.xml` 和所有子模块

## 常见问题

### 旧进程残留导致端口占用

如果启动失败提示端口被占用，先执行停止脚本再重新启动：

```powershell
.\scripts\stop-backend.ps1
.\scripts\start-backend.ps1
```

### Nacos gRPC 端口未就绪

Nacos 2.x 需要 gRPC 端口（9848）就绪后服务才能正常注册。脚本已等待该端口，但如果服务仍报 `UNAVAILABLE: io exception`，说明 Nacos 内部初始化未完成，等待 15 秒后重启对应服务即可。

### Windows 控制台中文乱码

Java 启动已添加 `-Dfile.encoding=UTF-8`，服务日志文件（`logs/*.log`）中文正常。Windows 控制台可能出现中文显示为乱码，这是控制台编码问题，不影响功能。

### 启动超时

编译步骤（`mvn clean package`）在首次运行时需要下载依赖，可能耗时较长。后续启动因为增量编译会明显加快。如果端口等待超时（默认 90 秒），检查对应服务的 `logs/<service>.err.log` 日志。如果 `gateway-service` 超时，可能是 Nacos gRPC 未就绪，等待 Nacos 完全启动后重试。

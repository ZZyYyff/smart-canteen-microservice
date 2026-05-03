# scripts — 一键启动与停止脚本

## 文件说明

| 文件 | 说明 |
|------|------|
| `start-backend.ps1` | PowerShell 启动脚本（自动 Docker + 编译 + 启动 5 个微服务） |
| `stop-backend.ps1` | PowerShell 停止脚本（三层兜底停止所有进程） |
| `start-backend.bat` | CMD 启动入口（双击可用） |
| `stop-backend.bat` | CMD 停止入口（双击可用） |

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

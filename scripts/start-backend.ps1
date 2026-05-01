#Requires -Version 5.1
<#
.SYNOPSIS
    Smart Canteen — One-click backend startup
.DESCRIPTION
    Stops old processes, starts Docker infra, waits for ports,
    builds the project, then launches all 5 microservices via mvn spring-boot:run.
#>

$ErrorActionPreference = "Stop"

# ---------------------------------------------------------------
# Paths
# ---------------------------------------------------------------
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$LogDir       = Join-Path $ProjectRoot "logs"
$PidDir       = Join-Path $LogDir "pids"
$ComposeFile  = Join-Path $ProjectRoot "deploy\docker-compose.yml"

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
New-Item -ItemType Directory -Force -Path $PidDir | Out-Null

# ---------------------------------------------------------------
# Helper: timestamped log
# ---------------------------------------------------------------
function Write-Step {
    param([string]$Msg)
    $time = Get-Date -Format "HH:mm:ss"
    Write-Host "[$time] $Msg"
}

# ---------------------------------------------------------------
# Helper: test if a TCP port is listening
# ---------------------------------------------------------------
function Test-PortOpen {
    param([string]$HostAddr, [int]$Port)
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $task = $client.ConnectAsync($HostAddr, $Port)
        if ($task.Wait(1500)) {
            $client.Close()
            $client.Dispose()
            return $true
        }
        $client.Close()
        $client.Dispose()
        return $false
    } catch {
        return $false
    }
}

# ---------------------------------------------------------------
# Helper: wait for a TCP port with timeout
# ---------------------------------------------------------------
function Wait-Port {
    param(
        [string]$Name,
        [string]$HostName,
        [int]$Port,
        [int]$TimeoutSeconds = 90
    )
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        if (Test-PortOpen -HostAddr $HostName -Port $Port) {
            Write-Step "$Name ${HostName}:$Port is ready"
            return $true
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
    }
    Write-Step "ERROR: $Name ${HostName}:$Port not ready after ${TimeoutSeconds}s"
    return $false
}

# ---------------------------------------------------------------
# Helper: stop a process by PID
# ---------------------------------------------------------------
function Stop-ByProcessId {
    param([int]$ProcessId, [string]$Label)
    try {
        $proc = Get-Process -Id $ProcessId -ErrorAction Stop
        Write-Step "  Stopping $Label (PID=$ProcessId) ..."
        $proc.Kill()
        $proc.WaitForExit(3000)
        Write-Step "  Stopped $Label (PID=$ProcessId)"
    } catch {
        Write-Step "  $Label PID=$ProcessId not running, skip"
    }
}

# ---------------------------------------------------------------
# Helper: stop processes listening on a given port
# ---------------------------------------------------------------
function Stop-ByPort {
    param([string]$Label, [int]$Port)
    try {
        $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue `
            | Select-Object -First 1
        if ($conn) {
            $targetId = $conn.OwningProcess
            if ($targetId -and $targetId -ne 0) {
                Stop-ByProcessId -ProcessId $targetId -Label "$Label (port $Port)"
                return
            }
        }
    } catch { }
    Write-Step "  Port $Port is free, skip $Label"
}

# ---------------------------------------------------------------
# Helper: kill java.exe processes by command-line keywords
# ---------------------------------------------------------------
function Stop-JavaByKeyword {
    param([string[]]$Keywords)
    try {
        $allJava = Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue
        if (-not $allJava) { return }
        $matched = @($allJava | Where-Object {
            $cmd = $_.CommandLine
            if (-not $cmd) { return $false }
            foreach ($k in $Keywords) {
                if ($cmd.IndexOf($k, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
                    return $true
                }
            }
            return $false
        })
        foreach ($p in $matched) {
            $cid = $p.ProcessId
            Write-Step "  Found leftover java.exe PID=$cid, killing..."
            Stop-Process -Id $cid -Force -ErrorAction SilentlyContinue
            Write-Step "  Killed PID=$cid"
        }
        if ($matched.Count -eq 0) {
            Write-Step "  No leftover java.exe processes found"
        }
    } catch {
        Write-Step "  WMI query failed: $_"
    }
}

# ===============================================================
#  STEP 1 — Check prerequisites
# ===============================================================
Write-Step "===== Step 1: Prerequisites ====="
$missing = @()
foreach ($cmd in @("docker", "java", "mvn")) {
    $found = Get-Command $cmd -ErrorAction SilentlyContinue
    if ($found) {
        Write-Step "  OK: $cmd"
    } else {
        Write-Step "  MISSING: $cmd"
        $missing += $cmd
    }
}
if ($missing.Count -gt 0) {
    Write-Step "FATAL: Missing commands: $($missing -join ', ')"
    exit 1
}

# ===============================================================
#  STEP 2 — Stop old backend processes
# ===============================================================
Write-Step "===== Step 2: Stop old backend processes ====="

$portMap = @(
    @{Label="gateway-service"; Port=8080},
    @{Label="pickup-service";  Port=9004},
    @{Label="order-service";   Port=9003},
    @{Label="menu-service";    Port=9002},
    @{Label="user-service";    Port=9001}
)

Write-Step "--- 2a: Stop by port ---"
foreach ($entry in $portMap) {
    Stop-ByPort -Label $entry.Label -Port $entry.Port
}

Write-Step "--- 2b: Stop by java command line ---"
Stop-JavaByKeyword -Keywords @(
    "smart-canteen",
    "gateway-service",
    "pickup-service",
    "order-service",
    "menu-service",
    "user-service"
)

Write-Step "--- 2c: Clean PID files ---"
Get-ChildItem -Path $PidDir -Filter "*.pid" -ErrorAction SilentlyContinue `
    | ForEach-Object { Remove-Item $_.FullName -Force }

Start-Sleep -Seconds 3
Write-Step "Old processes cleaned up"

# ===============================================================
#  STEP 3 — Start Docker infrastructure
# ===============================================================
Write-Step "===== Step 3: Docker compose up ====="
Push-Location (Join-Path $ProjectRoot "deploy")

$prevEAP = $ErrorActionPreference
$ErrorActionPreference = "Continue"

& docker compose up -d
$dockerExit = $LASTEXITCODE

$ErrorActionPreference = $prevEAP
Pop-Location

if ($dockerExit -ne 0) {
    Write-Step "FATAL: docker compose failed (exit=$dockerExit)"
    exit 1
}
Write-Step "Docker compose started successfully"

# ===============================================================
#  STEP 4 — Wait for infrastructure ports
# ===============================================================
Write-Step "===== Step 4: Wait for infrastructure ports ====="
$allReady = $true
$allReady = (Wait-Port -Name "MySQL" -HostName "localhost" -Port 3307 -TimeoutSeconds 90) -and $allReady
$allReady = (Wait-Port -Name "Redis" -HostName "localhost" -Port 6379 -TimeoutSeconds 60) -and $allReady
$allReady = (Wait-Port -Name "Nacos" -HostName "localhost" -Port 8848 -TimeoutSeconds 90) -and $allReady

if (-not $allReady) {
    Write-Step "FATAL: Infrastructure ports not ready"
    exit 1
}

Write-Step "Wait extra 5s for Nacos to fully start..."
Start-Sleep -Seconds 5

# ===============================================================
#  STEP 5 — Build project
# ===============================================================
Write-Step "===== Step 5: mvn clean package ====="
Push-Location $ProjectRoot

$prevEAP = $ErrorActionPreference
$ErrorActionPreference = "Continue"

& mvn clean package -DskipTests
$mvnExit = $LASTEXITCODE

$ErrorActionPreference = $prevEAP
Pop-Location

if ($mvnExit -ne 0) {
    Write-Step "FATAL: Build failed (exit=$mvnExit)"
    exit 1
}
Write-Step "Build succeeded"

# ===============================================================
#  STEP 6 — Start microservices with mvn spring-boot:run
# ===============================================================
Write-Step "===== Step 6: Start microservices (mvn spring-boot:run) ====="

$services = @(
    @{Name="user-service";    Port=9001},
    @{Name="menu-service";    Port=9002},
    @{Name="order-service";   Port=9003},
    @{Name="pickup-service";  Port=9004},
    @{Name="gateway-service"; Port=8080}
)

foreach ($svc in $services) {
    $svcName = $svc.Name
    $svcPort = $svc.Port
    Write-Step "--- $svcName :$svcPort ---"

    # Check if port already in use
    if (Test-PortOpen -HostAddr "127.0.0.1" -Port $svcPort) {
        Write-Step "  Port $svcPort already in use, skip $svcName"
        continue
    }

    # Log files
    $outLog = Join-Path $LogDir "${svcName}.out.log"
    $errLog = Join-Path $LogDir "${svcName}.err.log"
    $pidFile = Join-Path $PidDir "${svcName}.pid"

    # Start with mvn spring-boot:run (same as manual startup)
    Write-Step "  Starting: mvn -pl $svcName spring-boot:run"
    try {
        $prevEAP = $ErrorActionPreference
        $ErrorActionPreference = "Continue"

        $proc = Start-Process -FilePath "mvn" `
            -ArgumentList @(
                "-pl", $svcName,
                "spring-boot:run"
            ) `
            -WorkingDirectory $ProjectRoot `
            -NoNewWindow `
            -RedirectStandardOutput $outLog `
            -RedirectStandardError $errLog `
            -PassThru

        $ErrorActionPreference = $prevEAP

        # Wait for the service port to become available
        Write-Step "  Waiting for port $svcPort ..."
        $portReady = Wait-Port -Name $svcName -HostName "127.0.0.1" -Port $svcPort -TimeoutSeconds 90

        if (-not $portReady) {
            Write-Step "  ===== ERROR: $svcName failed to start ====="
            Write-Step "  Port: $svcPort"
            Write-Step "  Error log: $errLog"
            Write-Step "  --- Last 50 lines of error log ---"
            if (Test-Path $errLog) {
                $tail = Get-Content $errLog -Tail 50 -ErrorAction SilentlyContinue
                foreach ($line in $tail) { Write-Step "  $line" }
            } else {
                Write-Step "  (error log file not found)"
            }
            Write-Step "  --- End of error log ---"
            continue
        }

        Write-Step "  OK: $svcName port $svcPort is listening, PID=$($proc.Id)"
        $proc.Id | Out-File -FilePath $pidFile -NoNewline -Encoding ASCII
    } catch {
        Write-Step "  ERROR: failed to start $svcName : $_"
    }
}

# ===============================================================
#  DONE
# ===============================================================
Write-Step "===== Startup complete ====="
Write-Host ""
Write-Host "  Gateway (entry) : http://localhost:8080"
Write-Host "  User Service    : http://localhost:9001"
Write-Host "  Menu Service    : http://localhost:9002"
Write-Host "  Order Service   : http://localhost:9003"
Write-Host "  Pickup Service  : http://localhost:9004"
Write-Host "  Nacos Console   : http://localhost:8848/nacos  (nacos/nacos)"
Write-Host ""
Write-Host "  Logs : $LogDir"
Write-Host "  PIDs : $PidDir"
Write-Host ""
Write-Host "  Stop backend: .\scripts\stop-backend.ps1"
Write-Host "  Stop + Docker: .\scripts\stop-backend.ps1 -WithDocker"

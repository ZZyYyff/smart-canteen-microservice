#Requires -Version 5.1
<#
.SYNOPSIS
    Smart Canteen — One-click backend startup
.DESCRIPTION
    Stops old processes, starts Docker infra, waits for ports,
    builds the project, then launches all 5 microservices via mvn spring-boot:run.
#>

$ErrorActionPreference = "Stop"

# -------- paths --------
$ScriptDir   = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\.."
$DeployDir   = "$ProjectRoot\deploy"
$LogDir      = "$ProjectRoot\logs"
$PidDir      = "$LogDir\pids"

# -------- ensure directories --------
$null = New-Item -ItemType Directory -Force -Path $LogDir
$null = New-Item -ItemType Directory -Force -Path $PidDir

# -------- service definitions (start order matters) --------
$services = @(
    @{ Name="user-service";     Port=9001 },
    @{ Name="menu-service";     Port=9002 },
    @{ Name="order-service";    Port=9003 },
    @{ Name="pickup-service";   Port=9004 },
    @{ Name="gateway-service";  Port=8080 }
)

$infraPorts = @(
    @{ Name="MySQL"; Host="localhost"; Port=3307 },
    @{ Name="Redis"; Host="localhost"; Port=6379 },
    @{ Name="Nacos"; Host="localhost"; Port=8848 }
)

# -------- helpers --------
function Write-Step { Write-Host "`n>>> $args" -ForegroundColor Cyan }

function Kill-Port {
    param([int]$Port)
    $entries = @(netstat -ano 2>$null | Select-String ":$Port " | Select-String "LISTENING")
    foreach ($e in $entries) {
        $parts = -split ($e -replace '\s+', ' ')
        $processId = [int]$parts[-1]
        if ($processId -and $processId -ne 0) {
            Write-Host "  Stopping PID $processId on port ${Port}..."
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue | Out-Null
        }
    }
}

function Kill-Service-ByName {
    param([string]$Keyword)
    Get-WmiObject Win32_Process -Filter "name='java.exe' or name='java'" -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -match $Keyword } |
        ForEach-Object {
            Write-Host "  Stopping $Keyword (PID $($_.ProcessId))..."
            Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue | Out-Null
        }
}

function Kill-PidFile {
    param([string]$ServiceName)
    $pidFile = "$PidDir\$ServiceName.pid"
    if (Test-Path $pidFile) {
        $processId = [int](Get-Content $pidFile -Raw).Trim()
        if ($processId -gt 0) {
            try   { Stop-Process -Id $processId -Force -ErrorAction Stop | Out-Null }
            catch { Write-Host "  PID $processId already stopped" }
        }
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
    }
}

function Wait-Port {
    param([string]$Name, [string]$HostName, [int]$Port, [int]$TimeoutSec = 90)
    Write-Host "  Waiting for $Name on ${HostName}:${Port} ..." -NoNewline
    $elapsed = 0
    while ($elapsed -lt $TimeoutSec) {
        try {
            $tcp = New-Object System.Net.Sockets.TcpClient
            if ($tcp.ConnectAsync($HostName, $Port).Wait(1000)) {
                $tcp.Close()
                Write-Host " OK (${elapsed}s)" -ForegroundColor Green
                return
            }
            $tcp.Close()
        } catch { }
        Start-Sleep -Seconds 1
        $elapsed++
        Write-Host "." -NoNewline
    }
    throw "$Name did not start on ${HostName}:${Port} within ${TimeoutSec}s"
}

# ============================================================
# STEP 1 — stop old services
# ============================================================
Write-Step "Stopping old backend services..."
foreach ($svc in $services) {
    Write-Host "  [$($svc.Name)]"
    Kill-PidFile    $svc.Name
    Kill-Port       $svc.Port
    Kill-Service-ByName $svc.Name
}
Start-Sleep -Seconds 2

# ============================================================
# STEP 2 — start Docker infrastructure
# ============================================================
Write-Step "Starting Docker infrastructure..."
Push-Location $DeployDir
$prevEA = $ErrorActionPreference
$ErrorActionPreference = "Continue"
& docker compose up -d
$dockerCode = $LASTEXITCODE
$ErrorActionPreference = $prevEA
Pop-Location
if ($dockerCode -ne 0) {
    throw "docker compose up -d failed (exit code $dockerCode)"
}

# ============================================================
# STEP 3 — wait for infra ports
# ============================================================
Write-Step "Waiting for infrastructure ports..."
foreach ($inf in $infraPorts) {
    Wait-Port -Name $inf.Name -HostName $inf.Host -Port $inf.Port
}

# ============================================================
# STEP 4 — build project
# ============================================================
Write-Step "Building project (mvn clean package -DskipTests)..."
Push-Location $ProjectRoot
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Pop-Location
    throw "Maven build failed"
}
Pop-Location

# ============================================================
# STEP 5 — start microservices
# ============================================================
Write-Step "Starting microservices (mvn spring-boot:run)..."
foreach ($svc in $services) {
    Write-Host ""
    Write-Host "--- Starting $($svc.Name) on port $($svc.Port) ---"
    $outLog = "$LogDir\$($svc.Name).out.log"
    $errLog = "$LogDir\$($svc.Name).err.log"
    $pidFile = "$PidDir\$($svc.Name).pid"

    $proc = Start-Process -FilePath "mvn" `
        -ArgumentList "-pl","$($svc.Name)","spring-boot:run" `
        -WorkingDirectory $ProjectRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $outLog `
        -RedirectStandardError $errLog `
        -PassThru

    $proc.Id | Out-File -FilePath $pidFile -Encoding ASCII
    Write-Host "  Started PID $($proc.Id), logs: $outLog"

    try {
        Wait-Port -Name $($svc.Name) -HostName "localhost" -Port $($svc.Port) -TimeoutSec 90
    } catch {
        Write-Host "`n[ERROR] $($svc.Name) failed to listen on port $($svc.Port)" -ForegroundColor Red
        Write-Host "[ERROR] --- Last 80 lines of $errLog ---" -ForegroundColor Red
        if (Test-Path $errLog) { Get-Content $errLog -Tail 80 | ForEach-Object { Write-Host $_ -ForegroundColor Red } }
        Write-Host "[ERROR] --- Last 80 lines of $outLog ---" -ForegroundColor Red
        if (Test-Path $outLog) { Get-Content $outLog -Tail 80 | ForEach-Object { Write-Host $_ -ForegroundColor Red } }
        throw "$($svc.Name) startup failed"
    }
}

# ============================================================
# STEP 6 — final report
# ============================================================
Write-Host ""
Write-Host "=" * 50 -ForegroundColor Green
Write-Host "  All services started!" -ForegroundColor Green
Write-Host "=" * 50 -ForegroundColor Green
Write-Host ""
Write-Host "  Gateway:       http://localhost:8080"
Write-Host "  User Service:  http://localhost:9001"
Write-Host "  Menu Service:  http://localhost:9002"
Write-Host "  Order Service: http://localhost:9003"
Write-Host "  Pickup Service:http://localhost:9004"
Write-Host "  Nacos:         http://localhost:8848/nacos"
Write-Host "  MySQL:         localhost:3307"
Write-Host "  Redis:         localhost:6379"
Write-Host ""
Write-Host "Verify: Get-NetTCPConnection -LocalPort 8080,9001,9002,9003,9004,3307,6379,8848 -State Listen"

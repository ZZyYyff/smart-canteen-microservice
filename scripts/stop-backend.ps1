#Requires -Version 5.1
<#
.SYNOPSIS
    Smart Canteen — Stop backend services
.DESCRIPTION
    Stops all microservices (reverse order). Use -WithDocker to also stop MySQL/Redis/Nacos.
    Does NOT delete database volumes.
.PARAMETER WithDocker
    Also run 'docker compose down' (without -v)
#>

param([switch]$WithDocker)

$ErrorActionPreference = "Continue"

# -------- paths --------
$ScriptDir   = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\.."
$DeployDir   = "$ProjectRoot\deploy"
$LogDir      = "$ProjectRoot\logs"
$PidDir      = "$LogDir\pids"

# -------- service definitions (stop in reverse: gateway first) --------
$services = @(
    @{ Name="gateway-service";  Port=8080 },
    @{ Name="pickup-service";   Port=9004 },
    @{ Name="order-service";    Port=9003 },
    @{ Name="menu-service";     Port=9002 },
    @{ Name="user-service";     Port=9001 }
)

Write-Host "=== Stopping Smart Canteen backend ==="

# -------- stop microservices --------
foreach ($svc in $services) {
    Write-Host "`n[$($svc.Name)] port $($svc.Port)"

    # strategy 1: pid file
    $pidFile = "$PidDir\$($svc.Name).pid"
    if (Test-Path $pidFile) {
        $processId = [int](Get-Content $pidFile -Raw).Trim()
        if ($processId -gt 0) {
            Write-Host "  Stopping via PID file: $processId"
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue | Out-Null
        }
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
    } else {
        Write-Host "  No PID file"
    }

    # strategy 2: port
    $entries = @(netstat -ano 2>$null | Select-String ":$($svc.Port) " | Select-String "LISTENING")
    foreach ($e in $entries) {
        $parts = -split ($e -replace '\s+', ' ')
        $processId = [int]$parts[-1]
        if ($processId -and $processId -ne 0) {
            Write-Host "  Stopping via port: PID $processId"
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue | Out-Null
        }
    }

    # strategy 3: keyword
    Get-WmiObject Win32_Process -Filter "name='java.exe'" -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -match $svc.Name } |
        ForEach-Object {
            Write-Host "  Stopping via keyword '$($svc.Name)': PID $($_.ProcessId)"
            Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue | Out-Null
        }
}

# -------- wait for ports to release --------
Write-Host "`nWaiting for ports to release..."
foreach ($svc in $services) {
    $wait = 0
    while ($wait -lt 15) {
        $still = netstat -ano 2>$null | Select-String ":$($svc.Port) " | Select-String "LISTENING"
        if (-not $still) { break }
        Start-Sleep -Seconds 1
        $wait++
    }
    if ($wait -ge 15) {
        Write-Host "  WARNING: port $($svc.Port) still in use" -ForegroundColor Yellow
    } else {
        Write-Host "  Port $($svc.Port) released" -ForegroundColor Green
    }
}

# -------- docker compose down (only with -WithDocker) --------
if ($WithDocker) {
    Write-Host "`nStopping Docker infrastructure..."
    Push-Location $DeployDir
    $prevEA = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & docker compose down
    $dockerCode = $LASTEXITCODE
    $ErrorActionPreference = $prevEA
    Pop-Location
    if ($dockerCode -ne 0) {
        Write-Host "  WARNING: docker compose down exit code $dockerCode" -ForegroundColor Yellow
    }

    # confirm infra ports released
    @(3307, 6379, 8848) | ForEach-Object {
        $wait = 0
        while ($wait -lt 10) {
            $still = netstat -ano 2>$null | Select-String ":$_ " | Select-String "LISTENING"
            if (-not $still) { break }
            Start-Sleep -Seconds 1
            $wait++
        }
        if ($wait -ge 10) {
            Write-Host "  WARNING: port $_ still in use" -ForegroundColor Yellow
        } else {
            Write-Host "  Port $_ released" -ForegroundColor Green
        }
    }
}

Write-Host "`n=== Done ==="

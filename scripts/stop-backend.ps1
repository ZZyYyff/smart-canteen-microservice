#Requires -Version 5.1
<#
.SYNOPSIS
    Smart Canteen — One-click backend shutdown
.DESCRIPTION
    Stops all 5 microservices via PID files, port detection, and java.exe scanning.
    Optionally shuts down Docker infrastructure with -WithDocker.
.PARAMETER WithDocker
    Also runs docker compose down to stop MySQL, Redis, Nacos.
#>

param(
    [switch]$WithDocker
)

$ErrorActionPreference = "Continue"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$PidDir       = Join-Path $ProjectRoot "logs\pids"

Write-Host ""
Write-Host "===== Stop Smart Canteen Backend ====="

# Service definitions (reverse order: gateway first, user last)
$services = @(
    @{Name="gateway-service"; Port=8080},
    @{Name="pickup-service";  Port=9004},
    @{Name="order-service";   Port=9003},
    @{Name="menu-service";    Port=9002},
    @{Name="user-service";    Port=9001}
)

# ---------------------------------------------------------------
# Strategy 1 — Stop by PID files
# ---------------------------------------------------------------
Write-Host ""
Write-Host "--- Strategy 1: Stop by PID files ---"

foreach ($svc in $services) {
    $svcName = $svc.Name
    $pidFile = Join-Path $PidDir "${svcName}.pid"
    Write-Host "  [$svcName]"

    if (-not (Test-Path $pidFile)) {
        Write-Host "    Skip: PID file not found: $pidFile"
        continue
    }

    try {
        $processIdFromFile = [int](Get-Content $pidFile -Raw).Trim()
    } catch {
        Write-Host "    Warning: Cannot read PID file, removing it"
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
        continue
    }

    try {
        $proc = Get-Process -Id $processIdFromFile -ErrorAction Stop
    } catch {
        Write-Host "    Info: Process PID=$processIdFromFile already gone"
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
        continue
    }

    Write-Host "    Stopping $svcName (PID=$processIdFromFile) ..."
    try {
        $null = $proc.CloseMainWindow()
        $null = $proc.WaitForExit(3000)
    } catch { }

    if (-not $proc.HasExited) {
        Write-Host "    Force killing PID=$processIdFromFile ..."
        try {
            $proc.Kill()
            $null = $proc.WaitForExit(3000)
        } catch {
            Write-Host "    Warning: Force kill failed: $_"
        }
    }

    Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
    Write-Host "    OK: $svcName stopped"
}

# ---------------------------------------------------------------
# Strategy 2 — Stop by TCP port
# ---------------------------------------------------------------
Write-Host ""
Write-Host "--- Strategy 2: Stop by port ---"

foreach ($svc in $services) {
    $svcName = $svc.Name
    $svcPort = $svc.Port
    Write-Host "  [$svcName :$svcPort]"

    # Try Get-NetTCPConnection (silent)
    $conn = $null
    $netTcpOk = $true
    try {
        $conn = Get-NetTCPConnection -LocalPort $svcPort -State Listen -ErrorAction SilentlyContinue `
            | Select-Object -First 1
    } catch {
        $netTcpOk = $false
    }

    if ($netTcpOk) {
        if ($conn) {
            $targetId = $conn.OwningProcess
            if ($targetId -and $targetId -ne 0) {
                Write-Host "    Port $svcPort used by PID=$targetId, stopping..."
                Stop-Process -Id $targetId -Force -ErrorAction SilentlyContinue
                Write-Host "    OK: $svcName stopped via port $svcPort"
                continue
            }
        }
        Write-Host "    Skip: port $svcPort is free"
        continue
    }

    # Fallback: netstat
    Write-Host "    Info: Get-NetTCPConnection unavailable, trying netstat..."
    try {
        $result = netstat -ano 2>$null `
            | Select-String ":$svcPort " `
            | Select-String "LISTENING" `
            | Select-Object -First 1

        if (-not $result) {
            Write-Host "    Skip: port $svcPort is free"
            continue
        }

        $line  = $result.ToString().Trim()
        $parts = $line -split '\s+'
        $targetId = [int]$parts[-1]

        if (-not $targetId -or $targetId -eq 0) {
            Write-Host "    Skip: cannot parse owning process for port $svcPort"
            continue
        }

        Write-Host "    Port $svcPort used by PID=$targetId, stopping..."
        Stop-Process -Id $targetId -Force -ErrorAction SilentlyContinue
        Write-Host "    OK: $svcName stopped via port $svcPort (netstat)"
    } catch {
        Write-Host ("Info: netstat also failed for port {0}: {1}" -f $svcPort, $_.Exception.Message)
    }
}

# ---------------------------------------------------------------
# Strategy 3 — Kill leftover java.exe by command line
# ---------------------------------------------------------------
Write-Host ""
Write-Host "--- Strategy 3: Kill leftover java processes ---"

$keywords = @(
    "smart-canteen",
    "gateway-service",
    "pickup-service",
    "order-service",
    "menu-service",
    "user-service"
)

try {
    $allJava = Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue

    if (-not $allJava) {
        Write-Host "  No java.exe processes found"
    } else {
        $matched = @($allJava | Where-Object {
            $cmd = $_.CommandLine
            if (-not $cmd) { return $false }
            foreach ($k in $keywords) {
                if ($cmd.IndexOf($k, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
                    return $true
                }
            }
            return $false
        })

        if ($matched.Count -gt 0) {
            foreach ($p in $matched) {
                $cid = $p.ProcessId
                Write-Host "  Found leftover java.exe PID=$cid, killing..."
                Stop-Process -Id $cid -Force -ErrorAction SilentlyContinue
                Write-Host "  OK: Killed PID=$cid"
            }
        } else {
            Write-Host "  No matching java.exe processes found"
        }
    }
} catch {
    Write-Host "  Info: WMI query failed: $_"
}

# ---------------------------------------------------------------
# Final confirmation — check all ports
# ---------------------------------------------------------------
Write-Host ""
Write-Host "--- Final check: port status ---"

$allPorts = @(8080, 9001, 9002, 9003, 9004)
$remaining = @()

foreach ($port in $allPorts) {
    try {
        $c = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue `
            | Select-Object -First 1
        if ($c) {
            $remaining += "${port}(PID=$($c.OwningProcess))"
        }
    } catch { }
}

if ($remaining.Count -gt 0) {
    Write-Host "  WARNING: Ports still in use: $($remaining -join ', ')"
} else {
    Write-Host "  OK: All service ports are free"
}

# ---------------------------------------------------------------
# Docker infrastructure (only with -WithDocker)
# ---------------------------------------------------------------
if ($WithDocker) {
    Write-Host ""
    Write-Host "===== Shutting down Docker infrastructure ====="

    $ComposeFile = Join-Path $ProjectRoot "deploy\docker-compose.yml"
    Push-Location (Join-Path $ProjectRoot "deploy")

    & docker compose down
    $dockerExit = $LASTEXITCODE

    Pop-Location

    if ($dockerExit -ne 0) {
        Write-Host "WARNING: docker compose down exited with code $dockerExit"
    } else {
        Write-Host "OK: Docker infrastructure stopped"
    }
} else {
    Write-Host ""
    Write-Host "Tip: use -WithDocker to also stop MySQL/Redis/Nacos"
    Write-Host "     .\scripts\stop-backend.ps1 -WithDocker"
}

Write-Host ""
Write-Host "===== Backend stopped ====="

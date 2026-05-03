@echo off
chcp 65001 > nul

echo ============================================
echo  Smart Canteen — Stop Backend
echo ============================================
echo Project: %~dp0..
echo.

cd /d "%~dp0.."

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-backend.ps1"

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Stop failed with exit code %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Stop complete.
pause

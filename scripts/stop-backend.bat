@echo off
chcp 65001 > nul

echo Stopping Smart Canteen backend...
echo BAT path: %~dp0
echo Project path: %~dp0..

cd /d "%~dp0.."

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-backend.ps1"

pause

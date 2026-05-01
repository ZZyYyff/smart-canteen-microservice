@echo off
chcp 65001 > nul
cd /d "%~dp0.."
echo Starting Smart Canteen backend...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-backend.ps1"
pause

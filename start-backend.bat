@echo off
chcp 65001 >nul
title LibraDesk Backend

setlocal

set "ROOT=%~dp0"
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"
set "START_BACKEND=%ROOT%\backend\start-backend-background.ps1"

if not exist "%START_BACKEND%" (
    echo [ERROR] Khong tim thay script backend:
    echo %START_BACKEND%
    echo.
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%START_BACKEND%" -Root "%ROOT%"

if errorlevel 1 (
    echo.
    pause
    exit /b 1
)

exit /b 0

@echo off
chcp 65001 >nul
title LibraDesk Backend

setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..\..") do set "ROOT=%%~fI"
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

@echo off
chcp 65001 >nul
title Start LibraDesk

setlocal

set "ROOT=%~dp0"
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"
set "START_SCRIPT=%ROOT%\start-libradesk.ps1"

if not exist "%START_SCRIPT%" (
    echo [ERROR] Khong tim thay script:
    echo %START_SCRIPT%
    echo.
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%START_SCRIPT%" -Root "%ROOT%"

if errorlevel 1 (
    exit /b 1
)

exit /b 0

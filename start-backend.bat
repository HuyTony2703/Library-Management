@echo off
chcp 65001 >nul
title LibraDesk Backend

setlocal

set "ROOT=%~dp0"
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"
set "RUN_BACKEND=%ROOT%\backend\run-backend.ps1"

if not exist "%RUN_BACKEND%" (
    echo [ERROR] Khong tim thay script backend:
    echo %RUN_BACKEND%
    echo.
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%RUN_BACKEND%" -Root "%ROOT%"

echo.
echo Backend da dung.
pause

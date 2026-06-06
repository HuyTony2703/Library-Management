@echo off
chcp 65001 >nul
title Start LibraDesk

setlocal

set "ROOT=%~dp0"
set "APP_EXE=%ROOT%release\LibraDesk-1.0.0-portable.exe"
set "APP_EXE_FALLBACK=%ROOT%release\win-unpacked\LibraDesk.exe"

echo ==========================================
echo              START LIBRADESK
echo ==========================================
echo.

if not exist "%APP_EXE%" if exist "%APP_EXE_FALLBACK%" (
    set "APP_EXE=%APP_EXE_FALLBACK%"
)

if not exist "%APP_EXE%" (
    echo [ERROR] Khong tim thay LibraDesk.exe:
    echo %APP_EXE%
    echo %APP_EXE_FALLBACK%
    echo.
    echo Vi tri script hien tai:
    echo %ROOT%
    echo.
    echo Script dang tim app tai:
    echo %APP_EXE%
    echo.
    pause
    exit /b 1
)

echo Dang mo LibraDesk...
echo App se tu hoi cau hinh SQL Server trong lan chay dau tien.
start "" "%APP_EXE%"

echo.
echo Da mo LibraDesk.
exit /b 0

@echo off
chcp 65001 >nul
title Start LibraDesk

setlocal

set "ROOT=%~dp0"
set "BACKEND_BAT=%ROOT%start-backend.bat"
set "APP_EXE=%ROOT%release\win-unpacked\LibraDesk.exe"

echo ==========================================
echo              START LIBRADESK
echo ==========================================
echo.

if not exist "%BACKEND_BAT%" (
    echo [ERROR] Khong tim thay start-backend.bat:
    echo %BACKEND_BAT%
    echo.
    pause
    exit /b 1
)

if not exist "%APP_EXE%" (
    echo [ERROR] Khong tim thay LibraDesk.exe:
    echo %APP_EXE%
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

echo [1/3] Kiem tra backend tai http://localhost:8080/api/health ...

powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8080/api/health' -TimeoutSec 2 | Out-Null; exit 0 } catch { exit 1 }"

if errorlevel 1 (
    echo Backend chua chay.
    echo Dang mo backend...
    echo.

    start "LibraDesk Backend" "%BACKEND_BAT%"

    echo [2/3] Dang doi backend khoi dong toi da 60 giay...

    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ok=$false; for($i=1; $i -le 60; $i++){ try { Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8080/api/health' -TimeoutSec 2 | Out-Null; $ok=$true; break } catch { Start-Sleep -Seconds 1 } }; if($ok){ exit 0 } else { exit 1 }"

    if errorlevel 1 (
        echo.
        echo [ERROR] Backend khong khoi dong duoc sau 60 giay.
        echo Hay xem cua so "LibraDesk Backend" de biet loi.
        echo.
        pause
        exit /b 1
    )
) else (
    echo Backend da dang chay.
)

echo.
echo [3/3] Dang mo LibraDesk...
start "" "%APP_EXE%"

echo.
echo Da mo LibraDesk.
exit /b 0

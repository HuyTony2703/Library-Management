@echo off
chcp 65001 >nul
title Reset LibraDesk Database Config

setlocal

set "DB_CONFIG_FILE=%APPDATA%\LibraDesk\db-config.properties"
set "DB_PASSWORD_FILE=%APPDATA%\LibraDesk\db-password.dpapi"

echo ==========================================
echo      RESET LIBRADESK DATABASE CONFIG
echo ==========================================
echo.

echo [1/3] Kiem tra backend dang chay tren port 8080...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$connections = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue; if (-not $connections) { Write-Host 'Khong co backend nao dang listen port 8080.'; exit 0 }; $processIds = $connections | Select-Object -ExpandProperty OwningProcess -Unique; foreach ($processId in $processIds) { try { $process = Get-Process -Id $processId -ErrorAction Stop; Write-Host ('Dang tat process port 8080: {0} (PID {1})' -f $process.ProcessName, $processId); Stop-Process -Id $processId -Force -ErrorAction Stop; Write-Host ('Da tat PID {0}.' -f $processId) } catch { Write-Host ('Khong the tat PID {0}: {1}' -f $processId, $_.Exception.Message); exit 1 } }"

if errorlevel 1 (
    echo.
    echo [ERROR] Khong the tat backend dang chay tren port 8080.
    echo Hay mo Task Manager va tat process dang dung port 8080, sau do chay lai file nay.
    echo.
    pause
    exit /b 1
)

echo.
echo [2/3] Xoa cau hinh database da luu...

if exist "%DB_CONFIG_FILE%" (
    del /f /q "%DB_CONFIG_FILE%"
    echo Da xoa:
    echo %DB_CONFIG_FILE%
) else (
    echo Khong tim thay:
    echo %DB_CONFIG_FILE%
)

echo.

if exist "%DB_PASSWORD_FILE%" (
    del /f /q "%DB_PASSWORD_FILE%"
    echo Da xoa:
    echo %DB_PASSWORD_FILE%
) else (
    echo Khong tim thay:
    echo %DB_PASSWORD_FILE%
)

echo.
echo [3/3] Hoan tat reset.
echo Lan sau chay start-backend.bat hoac start-libradesk.bat, he thong se yeu cau nhap lai thong tin SQL Server.
echo.
echo Cua so nay se tu dong dong sau 5 giay...
timeout /t 5 /nobreak >nul
exit /b 0

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
echo Lan sau chay start-backend.bat, he thong se yeu cau nhap lai thong tin SQL Server.
echo.
pause

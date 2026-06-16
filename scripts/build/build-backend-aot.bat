@echo off
chcp 65001 >nul
title Build LibraDesk Backend AOT JAR

setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..\..") do set "ROOT=%%~fI"
set "BACKEND_DIR=%ROOT%\backend"
set "RELEASE_DIR=%ROOT%\release"
set "TARGET_JAR=%BACKEND_DIR%\target\backend-0.0.1-SNAPSHOT.jar"
set "RELEASE_JAR=%RELEASE_DIR%\backend-0.0.1-SNAPSHOT.jar"

echo ==========================================
echo       BUILD LIBRADESK BACKEND AOT JAR
echo ==========================================
echo.

if not exist "%BACKEND_DIR%\mvnw.cmd" (
    echo [ERROR] Khong tim thay Maven Wrapper:
    echo %BACKEND_DIR%\mvnw.cmd
    pause
    exit /b 1
)

if not exist "%RELEASE_DIR%" (
    mkdir "%RELEASE_DIR%"
)

cd /d "%BACKEND_DIR%"
call "%BACKEND_DIR%\mvnw.cmd" -Pnative -DskipTests package

if errorlevel 1 (
    echo.
    echo [ERROR] Build AOT JAR that bai.
    pause
    exit /b 1
)

copy /y "%TARGET_JAR%" "%RELEASE_JAR%" >nul

echo.
echo [OK] Da cap nhat AOT JAR:
echo %RELEASE_JAR%
pause

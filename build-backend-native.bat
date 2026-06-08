@echo off
chcp 65001 >nul
title Build LibraDesk Backend Native

setlocal

set "ROOT=%~dp0"
set "BACKEND_DIR=%ROOT%backend"
set "RELEASE_DIR=%ROOT%release"
set "TARGET_EXE=%BACKEND_DIR%\target\backend.exe"
set "RELEASE_EXE=%RELEASE_DIR%\backend.exe"

echo ==========================================
echo       BUILD LIBRADESK BACKEND NATIVE
echo ==========================================
echo.

set "NATIVE_IMAGE_FOUND="

where native-image >nul 2>nul
if not errorlevel 1 set "NATIVE_IMAGE_FOUND=1"

if not defined NATIVE_IMAGE_FOUND if defined GRAALVM_HOME if exist "%GRAALVM_HOME%\bin\native-image.cmd" set "NATIVE_IMAGE_FOUND=1"
if not defined NATIVE_IMAGE_FOUND if defined GRAALVM_HOME if exist "%GRAALVM_HOME%\bin\native-image.exe" set "NATIVE_IMAGE_FOUND=1"
if not defined NATIVE_IMAGE_FOUND if defined JAVA_HOME if exist "%JAVA_HOME%\bin\native-image.cmd" set "NATIVE_IMAGE_FOUND=1"
if not defined NATIVE_IMAGE_FOUND if defined JAVA_HOME if exist "%JAVA_HOME%\bin\native-image.exe" set "NATIVE_IMAGE_FOUND=1"

if not defined NATIVE_IMAGE_FOUND (
    echo [ERROR] Khong tim thay native-image trong PATH.
    echo Hay cai GraalVM JDK 21 va Native Image, cau hinh JAVA_HOME/GRAALVM_HOME,
    echo va cai Visual Studio Build Tools C++.
    echo.
    pause
    exit /b 1
)

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
call "%BACKEND_DIR%\mvnw.cmd" -Pnative -DskipTests native:compile

if errorlevel 1 (
    echo.
    echo [ERROR] Build native executable that bai.
    pause
    exit /b 1
)

if not exist "%TARGET_EXE%" (
    echo.
    echo [ERROR] Khong tim thay native executable:
    echo %TARGET_EXE%
    pause
    exit /b 1
)

copy /y "%TARGET_EXE%" "%RELEASE_EXE%" >nul

echo.
echo [OK] Da cap nhat native backend:
echo %RELEASE_EXE%
pause

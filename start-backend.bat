@echo off
chcp 65001 >nul
title LibraDesk Backend

setlocal

set "ROOT=%~dp0"
set "BACKEND_DIR=%ROOT%backend"
set "LOCAL_CONFIG=%BACKEND_DIR%\local-db.bat"

echo ==========================================
echo        LIBRADESK BACKEND SERVER
echo ==========================================
echo.

if not exist "%BACKEND_DIR%" (
    echo [ERROR] Khong tim thay thu muc backend:
    echo %BACKEND_DIR%
    echo.
    pause
    exit /b 1
)

cd /d "%BACKEND_DIR%"

if exist "%LOCAL_CONFIG%" (
    echo Doc cau hinh database local:
    echo %LOCAL_CONFIG%
    call "%LOCAL_CONFIG%"
    echo.
)

echo [1/3] Kiem tra Java...
java -version >nul 2>&1

if errorlevel 1 (
    echo [ERROR] May chua cai Java hoac Java chua nam trong PATH.
    echo Vui long cai JDK 21 truoc khi chay backend.
    echo.
    pause
    exit /b 1
)

echo [OK] Java da san sang.
echo.

echo [2/3] Kiem tra file backend JAR...

set "JAR_FILE="

if exist "%BACKEND_DIR%\target" (
    for /f "delims=" %%F in ('dir /b /a-d "%BACKEND_DIR%\target\*.jar" 2^>nul') do (
        echo %%F | findstr /I "original sources javadoc" >nul
        if errorlevel 1 (
            set "JAR_FILE=%BACKEND_DIR%\target\%%F"
        )
    )
)

echo.
echo [3/3] Dang khoi dong backend...
echo.

if defined JAR_FILE (
    echo Chay backend bang file JAR:
    echo %JAR_FILE%
    echo.
    java -jar "%JAR_FILE%"
) else (
    echo Khong tim thay file JAR.
    echo Se chay backend bang Maven Wrapper.
    echo.

    if not exist "%BACKEND_DIR%\mvnw.cmd" (
        echo [ERROR] Khong tim thay mvnw.cmd.
        echo.
        pause
        exit /b 1
    )

    call "%BACKEND_DIR%\mvnw.cmd" spring-boot:run
)

echo.
echo Backend da dung.
pause

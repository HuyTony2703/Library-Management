@echo off
chcp 65001 >nul
title Start LibraDesk

setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"
set "START_SCRIPT=%ROOT%\scripts\runtime\start-libradesk.ps1"
set "BACKEND_JAR=%ROOT%\release\backend-0.0.1-SNAPSHOT.jar"
set "BACKEND_EXE=%ROOT%\release\backend.exe"
set "APP_EXE=%ROOT%\release\LibraDesk-1.0.0-portable.exe"
set "APP_EXE_FALLBACK=%ROOT%\release\win-ia32-unpacked\LibraDesk.exe"
set "APP_EXE_X64_FALLBACK=%ROOT%\release\win-unpacked\LibraDesk.exe"
set "ELECTRON_CMD=%ROOT%\frontend\node_modules\.bin\electron.cmd"
set "ELECTRON_EXE=%ROOT%\frontend\node_modules\electron\dist\electron.exe"
set "VITE_CMD=%ROOT%\frontend\node_modules\.bin\vite.cmd"
set "FRONTEND_DIST=%ROOT%\frontend\dist\index.html"
set "POWERSHELL_EXE=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
set "NPM_CMD=npm.cmd"
set "HAS_APP_RELEASE=0"

if not exist "%START_SCRIPT%" (
    echo [ERROR] Khong tim thay script:
    echo %START_SCRIPT%
    echo.
    pause
    exit /b 1
)

if not exist "%BACKEND_JAR%" if not exist "%BACKEND_EXE%" (
    echo [ERROR] Khong tim thay backend release artifact.
    echo Can co mot trong hai file:
    echo %BACKEND_JAR%
    echo %BACKEND_EXE%
    echo.
    echo Hay build backend truoc khi chay app:
    echo scripts\build\build-backend-aot.bat
    echo.
    pause
    exit /b 1
)

where npm.cmd >nul 2>nul
if errorlevel 1 set "NPM_CMD=npm"

if exist "%APP_EXE%" set "HAS_APP_RELEASE=1"
if exist "%APP_EXE_FALLBACK%" set "HAS_APP_RELEASE=1"
if exist "%APP_EXE_X64_FALLBACK%" set "HAS_APP_RELEASE=1"

if "%HAS_APP_RELEASE%"=="0" if not exist "%VITE_CMD%" if exist "%ROOT%\frontend\package-lock.json" (
    echo [INFO] Frontend dependencies chua co. Dang cai dat bang npm ci...
    pushd "%ROOT%\frontend" >nul
    call "%NPM_CMD%" ci
    set "NPM_EXIT=!ERRORLEVEL!"
    popd >nul
    if not "!NPM_EXIT!"=="0" (
        if exist "%APP_EXE%" (
            echo [WARN] npm ci that bai. Se tiep tuc bang app release da build.
        ) else if exist "%APP_EXE_FALLBACK%" (
            echo [WARN] npm ci that bai. Se tiep tuc bang app release da build.
        ) else (
            echo [ERROR] npm ci that bai. Khong the chuan bi frontend local.
            echo Hay kiem tra Node.js/npm va ket noi mang, sau do chay lai.
            pause
            exit /b !NPM_EXIT!
        )
    )
)

if "%HAS_APP_RELEASE%"=="0" if not exist "%FRONTEND_DIST%" if exist "%VITE_CMD%" (
    echo [INFO] Chua co frontend dist. Dang build frontend...
    pushd "%ROOT%\frontend" >nul
    call "%NPM_CMD%" run build
    set "BUILD_EXIT=!ERRORLEVEL!"
    popd >nul
    if not "!BUILD_EXIT!"=="0" (
        if exist "%APP_EXE%" (
            echo [WARN] npm run build that bai. Se tiep tuc bang app release da build.
        ) else if exist "%APP_EXE_FALLBACK%" (
            echo [WARN] npm run build that bai. Se tiep tuc bang app release da build.
        ) else (
            echo [ERROR] npm run build that bai. Khong the chuan bi frontend local.
            pause
            exit /b !BUILD_EXIT!
        )
    )
)

if not exist "%APP_EXE%" if not exist "%APP_EXE_FALLBACK%" if not exist "%ELECTRON_CMD%" if not exist "%VITE_CMD%" (
    if exist "%APP_EXE_X64_FALLBACK%" goto :skip_app_missing_error
    echo [ERROR] Khong tim thay ung dung LibraDesk da build.
    echo Can co mot trong cac file:
    echo %APP_EXE%
    echo %APP_EXE_FALLBACK%
    echo %APP_EXE_X64_FALLBACK%
    echo %ELECTRON_CMD%
    echo %VITE_CMD%
    echo.
    echo Hay build frontend desktop truoc khi chay:
    echo cd frontend
    echo npm run dist:win
    echo.
    pause
    exit /b 1
)
:skip_app_missing_error

if not exist "%APP_EXE%" if not exist "%APP_EXE_FALLBACK%" if not exist "%FRONTEND_DIST%" (
    if exist "%APP_EXE_X64_FALLBACK%" goto :skip_dist_missing_error
    echo [ERROR] Khong tim thay frontend dist de chay Electron local:
    echo %FRONTEND_DIST%
    echo.
    echo Hay build frontend truoc khi chay:
    echo cd frontend
    echo npm run build
    echo.
    pause
    exit /b 1
)
:skip_dist_missing_error

if not exist "%POWERSHELL_EXE%" (
    set "POWERSHELL_EXE=powershell"
)

pushd "%ROOT%" >nul
"%POWERSHELL_EXE%" -NoProfile -ExecutionPolicy Bypass -File "%START_SCRIPT%" -Root "%ROOT%"
set "EXIT_CODE=%ERRORLEVEL%"
popd >nul

if not "%EXIT_CODE%"=="0" (
    echo.
    echo [ERROR] LibraDesk khoi dong that bai. Ma loi: %EXIT_CODE%
    echo Hay xem thong bao phia tren hoac log backend trong %%APPDATA%%\LibraDesk.
    echo.
    pause
    exit /b %EXIT_CODE%
)

exit /b 0

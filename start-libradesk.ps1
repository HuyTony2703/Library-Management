param(
    [string]$Root = $PSScriptRoot
)

$ErrorActionPreference = "Stop"

$Root = $Root.Trim().Trim('"').TrimEnd("\")
$backendBat = Join-Path $Root "start-backend.bat"
$backendScript = Join-Path $Root "backend\run-backend.ps1"
$appExe = Join-Path $Root "release\LibraDesk-1.0.0-portable.exe"
$appExeFallback = Join-Path $Root "release\win-unpacked\LibraDesk.exe"
$configDir = Join-Path $env:APPDATA "LibraDesk"
$dbConfigFile = Join-Path $configDir "db-config.properties"
$dbPasswordFile = Join-Path $configDir "db-password.dpapi"
$backendOutLogFile = Join-Path $configDir "backend.out.log"
$backendErrLogFile = Join-Path $configDir "backend.err.log"
$healthUrl = "http://localhost:8080/api/health"
$warmupUrl = "http://localhost:8080/api/warmup"
$startTime = Get-Date

function Write-Step {
    param([string]$Message)

    $elapsed = [Math]::Round(((Get-Date) - $startTime).TotalSeconds, 2)
    Write-Host ("[{0,6:0.00}s] {1}" -f $elapsed, $Message)
}

function Test-BackendHealth {
    try {
        Invoke-RestMethod -Uri $healthUrl -TimeoutSec 1 | Out-Null
        return $true
    } catch {
        return $false
    }
}

function Wait-BackendHealth {
    param([int]$TimeoutSeconds = 60)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-BackendHealth) {
            return $true
        }

        Start-Sleep -Milliseconds 500
    }

    return $false
}

Write-Host "=========================================="
Write-Host "             START LIBRADESK"
Write-Host "=========================================="
Write-Host ""

if (-not (Test-Path -Path $backendBat)) {
    Write-Host "[ERROR] Khong tim thay start-backend.bat:"
    Write-Host $backendBat
    exit 1
}

if (-not (Test-Path -Path $backendScript)) {
    Write-Host "[ERROR] Khong tim thay backend runner:"
    Write-Host $backendScript
    exit 1
}

if ((-not (Test-Path -Path $appExe)) -and (Test-Path -Path $appExeFallback)) {
    $appExe = $appExeFallback
}

if (-not (Test-Path -Path $appExe)) {
    Write-Host "[ERROR] Khong tim thay LibraDesk.exe:"
    Write-Host $appExe
    Write-Host $appExeFallback
    exit 1
}

Write-Step "Kiem tra backend tai $healthUrl ..."

if (Test-BackendHealth) {
    Write-Step "Backend da dang chay."
} else {
    if ((Test-Path -Path $dbConfigFile) -and (Test-Path -Path $dbPasswordFile)) {
        New-Item -ItemType Directory -Path $configDir -Force | Out-Null
        Write-Step "Backend chua chay. Dang mo backend trong nen..."
        Start-Process `
            -FilePath "powershell" `
            -WindowStyle Hidden `
            -ArgumentList @(
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-File", $backendScript,
                "-Root", $Root
            ) `
            -RedirectStandardOutput $backendOutLogFile `
            -RedirectStandardError $backendErrLogFile
    } else {
        Write-Step "Backend chua co cau hinh DB. Dang mo console de nhap lan dau..."
        Start-Process -FilePath $backendBat
    }

    Write-Step "Dang doi backend khoi dong, polling moi 500ms..."
    if (-not (Wait-BackendHealth -TimeoutSeconds 60)) {
        Write-Host ""
        Write-Host "[ERROR] Backend khong khoi dong duoc sau 60 giay."
        Write-Host "Hay chay start-backend.bat truc tiep de xem log loi."
        Write-Host "Neu backend chay nen, log nam tai:"
        Write-Host $backendOutLogFile
        Write-Host $backendErrLogFile
        exit 1
    }

    Write-Step "Backend da san sang."
}

Write-Step "Dang chay warm-up backend trong nen..."
Start-Process `
    -FilePath "powershell" `
    -WindowStyle Hidden `
    -ArgumentList @(
        "-NoProfile",
        "-ExecutionPolicy", "Bypass",
        "-Command",
        "try { Invoke-RestMethod -Uri '$warmupUrl' -TimeoutSec 10 | Out-Null } catch { }"
    ) | Out-Null

Write-Step "Dang mo LibraDesk..."
Start-Process -FilePath $appExe

Write-Step "Da mo LibraDesk."

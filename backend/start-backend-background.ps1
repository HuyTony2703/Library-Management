param(
    [string]$Root = (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))
)

$ErrorActionPreference = "Stop"

$Root = $Root.Trim().Trim('"').TrimEnd("\")
$backendScript = Join-Path $Root "backend\run-backend.ps1"
$configDir = Join-Path $env:APPDATA "LibraDesk"
$backendOutLogFile = Join-Path $configDir "backend.out.log"
$backendErrLogFile = Join-Path $configDir "backend.err.log"
$healthUrl = "http://localhost:8080/api/health"

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
Write-Host "       LIBRADESK BACKEND SERVER"
Write-Host "=========================================="
Write-Host ""

if (-not (Test-Path -Path $backendScript)) {
    Write-Host "[ERROR] Khong tim thay backend runner:"
    Write-Host $backendScript
    exit 1
}

if (Test-BackendHealth) {
    Write-Host "Backend da dang chay tai $healthUrl."
    Write-Host "Cua so nay se tu dong dong sau 3 giay..."
    Start-Sleep -Seconds 3
    exit 0
}

Write-Host "[1/3] Kiem tra cau hinh backend..."
& powershell -NoProfile -ExecutionPolicy Bypass -File $backendScript -Root $Root -ValidateOnly

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Khong the chuan bi backend."
    exit 1
}

New-Item -ItemType Directory -Path $configDir -Force | Out-Null
Remove-Item -LiteralPath $backendOutLogFile, $backendErrLogFile -Force -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "[2/3] Dang mo backend trong nen..."
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

Write-Host "[3/3] Dang doi backend san sang, polling moi 500ms..."

if (-not (Wait-BackendHealth -TimeoutSeconds 60)) {
    Write-Host ""
    Write-Host "[ERROR] Backend khong khoi dong duoc sau 60 giay."
    Write-Host "Log backend:"
    Write-Host $backendOutLogFile
    Write-Host $backendErrLogFile
    exit 1
}

Write-Host ""
Write-Host "[OK] Backend da chay nen thanh cong."
Write-Host "Log backend:"
Write-Host $backendOutLogFile
Write-Host $backendErrLogFile
Write-Host ""
Write-Host "Cua so nay se tu dong dong sau 3 giay..."
Start-Sleep -Seconds 3
exit 0

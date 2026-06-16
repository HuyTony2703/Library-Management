param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
)

$ErrorActionPreference = "Stop"

$Root = $Root.Trim().Trim('"').TrimEnd("\")
$backendBat = Join-Path $Root "scripts\runtime\start-backend.bat"
$backendScript = Join-Path $Root "backend\run-backend.ps1"
$resetDbConfigBat = Join-Path $Root "scripts\runtime\reset-db-config.bat"
$frontendDir = Join-Path $Root "frontend"
$electronCmd = Join-Path $frontendDir "node_modules\.bin\electron.cmd"
$electronExe = Join-Path $frontendDir "node_modules\electron\dist\electron.exe"
$viteCmd = Join-Path $frontendDir "node_modules\.bin\vite.cmd"
$frontendDistIndex = Join-Path $frontendDir "dist\index.html"
$appExe = Join-Path $Root "release\LibraDesk-1.0.0-portable.exe"
$configDir = Join-Path $env:APPDATA "LibraDesk"
$dbConfigFile = Join-Path $configDir "db-config.properties"
$dbPasswordFile = Join-Path $configDir "db-password.dpapi"
$backendOutLogFile = Join-Path $configDir "backend.out.log"
$backendErrLogFile = Join-Path $configDir "backend.err.log"
$frontendOutLogFile = Join-Path $configDir "frontend.out.log"
$frontendErrLogFile = Join-Path $configDir "frontend.err.log"
$appExeFallbacks = @(
    (Join-Path $Root "release\win-ia32-unpacked\LibraDesk.exe"),
    (Join-Path $Root "release\win-unpacked\LibraDesk.exe")
)
$healthUrl = "http://localhost:8080/api/health"
$warmupUrl = "http://localhost:8080/api/warmup"
$frontendPreviewPort = 4173
$frontendPreviewUrl = "http://127.0.0.1:$frontendPreviewPort/"
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
    param(
        [int]$TimeoutSeconds = 60,
        [string]$WaitingMessage = "Dang doi backend san sang..."
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastNotice = Get-Date

    while ((Get-Date) -lt $deadline) {
        if (Test-BackendHealth) {
            return $true
        }

        if (((Get-Date) - $lastNotice).TotalSeconds -ge 5) {
            Write-Step $WaitingMessage
            $lastNotice = Get-Date
        }

        Start-Sleep -Milliseconds 500
    }

    return $false
}

function Get-AppLaunchCandidates {
    $candidates = @()

    if (Test-Path -Path $appExe) {
        $candidates += $appExe
    }

    foreach ($fallback in $appExeFallbacks) {
        if (Test-Path -Path $fallback) {
            $candidates += $fallback
        }
    }

    return $candidates | Select-Object -Unique
}

function Test-ElectronFallback {
    return (Test-Path -Path $electronCmd) -and (Test-Path -Path $electronExe) -and (Test-Path -Path $frontendDistIndex)
}

function Test-VitePreviewFallback {
    return (Test-Path -Path $viteCmd) -and (Test-Path -Path $frontendDistIndex)
}

function Test-FrontendPreviewHealth {
    try {
        Invoke-WebRequest -Uri $frontendPreviewUrl -TimeoutSec 1 -UseBasicParsing | Out-Null
        return $true
    } catch {
        return $false
    }
}

function Wait-FrontendPreview {
    param([int]$TimeoutSeconds = 15)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-FrontendPreviewHealth) {
            return $true
        }

        Start-Sleep -Milliseconds 500
    }

    return $false
}

function Start-LibraDeskFrontend {
    if ($env:ELECTRON_RUN_AS_NODE) {
        Write-Step "Dang xoa bien moi truong ELECTRON_RUN_AS_NODE de Electron mo dung cua so app..."
        Remove-Item Env:ELECTRON_RUN_AS_NODE -ErrorAction SilentlyContinue
    }

    $launchCandidates = @(Get-AppLaunchCandidates)

    foreach ($candidate in $launchCandidates) {
        try {
            Write-Step "Dang mo LibraDesk bang file da build: $candidate"
            Start-Process -FilePath $candidate -ErrorAction Stop
            return
        } catch {
            Write-Host "[WARN] Khong mo duoc file nay:"
            Write-Host $candidate
            Write-Host $_.Exception.Message
            Write-Host ""
        }
    }

    if (Test-ElectronFallback) {
        Write-Step "Dang mo LibraDesk bang Electron local tu frontend\dist..."
        Start-Process -FilePath $electronCmd -WorkingDirectory $frontendDir -ArgumentList "." -ErrorAction Stop
        return
    }

    if (Test-VitePreviewFallback) {
        if (-not (Test-FrontendPreviewHealth)) {
            New-Item -ItemType Directory -Path $configDir -Force | Out-Null
            Write-Step "Portable/unpacked khong mo duoc. Dang chay frontend build bang Vite preview..."
            Start-Process `
                -FilePath "cmd.exe" `
                -WindowStyle Hidden `
                -WorkingDirectory $frontendDir `
                -ArgumentList @(
                    "/c",
                    "`"$viteCmd`" preview --host 127.0.0.1 --port $frontendPreviewPort"
                ) `
                -RedirectStandardOutput $frontendOutLogFile `
                -RedirectStandardError $frontendErrLogFile
        }

        if (-not (Wait-FrontendPreview -TimeoutSeconds 15)) {
            throw "Khong mo duoc frontend preview. Hay xem log: $frontendOutLogFile va $frontendErrLogFile"
        }

        Write-Step "Dang mo LibraDesk tren trinh duyet: $frontendPreviewUrl"
        Start-Process -FilePath $frontendPreviewUrl -ErrorAction Stop
        return
    }

    throw "Khong mo duoc LibraDesk. Portable/unpacked bi chan hoac khong ton tai, Electron local thieu electron.exe, va Vite preview chua san sang."
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

if ((@(Get-AppLaunchCandidates).Count -eq 0) -and (-not (Test-ElectronFallback)) -and (-not (Test-VitePreviewFallback))) {
    Write-Host "[ERROR] Khong tim thay LibraDesk.exe:"
    Write-Host $appExe
    foreach ($fallback in $appExeFallbacks) {
        Write-Host $fallback
    }
    Write-Host ""
    Write-Host "Electron fallback cung chua san sang:"
    Write-Host $electronCmd
    Write-Host $electronExe
    Write-Host $frontendDistIndex
    Write-Host ""
    Write-Host "Vite preview fallback cung chua san sang:"
    Write-Host $viteCmd
    exit 1
}

Write-Step "Kiem tra backend tai $healthUrl ..."

if (Test-BackendHealth) {
    Write-Step "Backend da dang chay."
} else {
    $hasDbConfig = ((Test-Path -Path $dbConfigFile) -and (Test-Path -Path $dbPasswordFile)) -or (Test-Path -Path (Join-Path $configDir "database-config.json"))

    if ($hasDbConfig) {
        Write-Step "Backend chua chay. Dang chay backend runner de kiem tra va khoi dong..."
    } else {
        Write-Step "Backend chua co cau hinh DB. Dang chay backend runner de nhap thong tin SQL Server..."
    }

    Write-Host "Backend runner se chay ngay trong terminal nay de khong bi an prompt nhap DB."
    Write-Host "Neu he thong hoi SQL Server, hay nhap truc tiep tai terminal nay."
    Write-Host ""

    & $backendBat
    $backendExitCode = $LASTEXITCODE

    if ($backendExitCode -ne 0) {
        Write-Host ""
        Write-Host "[ERROR] Backend runner ket thuc voi ma loi: $backendExitCode"
        Write-Host "Hay xem thong bao ngay phia tren de biet buoc nao bi loi."
        exit $backendExitCode
    }

    Write-Step "Kiem tra lai backend sau khi runner ket thuc..."
    if (-not (Wait-BackendHealth -TimeoutSeconds 30 -WaitingMessage "Van dang doi backend hoan tat khoi dong...")) {
        Write-Host ""
        Write-Host "[ERROR] Backend runner da ket thuc nhung health endpoint van chua san sang."
        Write-Host ""
        Write-Host "Huong xu ly nhanh:"
        Write-Host "1. Chay scripts\runtime\reset-db-config.bat de xoa cau hinh database da luu."
        Write-Host "2. Chay lai start-libradesk.bat va nhap lai thong tin SQL Server."
        Write-Host ""
        Write-Host "File reset:"
        Write-Host $resetDbConfigBat
        Write-Host ""
        Write-Host "Neu van loi, hay chay scripts\runtime\start-backend.bat truc tiep de xem log chi tiet."
        Write-Host "Neu backend chay nen, log nam tai:"
        Write-Host $backendOutLogFile
        Write-Host $backendErrLogFile
        Write-Host ""
        Write-Host "Cua so nay se tu dong dong sau 8 giay..."
        Start-Sleep -Seconds 8
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

Start-LibraDeskFrontend
Write-Step "Da mo LibraDesk."

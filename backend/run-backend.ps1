param(
    [string]$Root = (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))
)

$ErrorActionPreference = "Stop"

$Root = $Root.Trim().Trim('"').TrimEnd("\")
$backendDir = Join-Path $Root "backend"
$releaseBackendJar = Join-Path $Root "release\backend-0.0.1-SNAPSHOT.jar"
$configDir = Join-Path $env:APPDATA "LibraDesk"
$configFile = Join-Path $configDir "db-config.properties"
$passwordFile = Join-Path $configDir "db-password.dpapi"
$setupConfigScript = Join-Path $backendDir "setup-db-config.ps1"

function Read-DbConfig {
    param([string]$Path)

    $config = @{}
    foreach ($line in Get-Content -Path $Path) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith("#")) {
            continue
        }

        $separatorIndex = $trimmed.IndexOf("=")
        if ($separatorIndex -le 0) {
            continue
        }

        $key = $trimmed.Substring(0, $separatorIndex).Trim()
        $value = $trimmed.Substring($separatorIndex + 1).Trim()
        $config[$key] = $value
    }

    return $config
}

function Read-EncryptedPassword {
    param([string]$Path)

    $encrypted = (Get-Content -Raw -Path $Path).Trim()
    $secure = ConvertTo-SecureString $encrypted
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)

    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

Write-Host "=========================================="
Write-Host "       LIBRADESK BACKEND SERVER"
Write-Host "=========================================="
Write-Host ""

if (-not (Test-Path -Path $backendDir)) {
    Write-Host "[ERROR] Khong tim thay thu muc backend:"
    Write-Host $backendDir
    exit 1
}

Set-Location $backendDir

if (-not (Test-Path -Path $configFile)) {
    Write-Host "Chua co cau hinh database."
    Write-Host "Vui long nhap thong tin SQL Server cho lan chay dau tien."
    Write-Host ""

    & powershell -NoProfile -ExecutionPolicy Bypass -File $setupConfigScript

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[ERROR] Khong the tao cau hinh database."
        exit 1
    }
}

if (-not (Test-Path -Path $passwordFile)) {
    Write-Host "[ERROR] Khong tim thay file mat khau database da ma hoa:"
    Write-Host $passwordFile
    Write-Host ""
    Write-Host "Hay xoa thu muc $configDir roi chay lai de thiet lap lai."
    exit 1
}

Write-Host "Doc cau hinh database:"
Write-Host $configFile
Write-Host ""

$dbConfig = Read-DbConfig -Path $configFile

if (-not $dbConfig.DB_URL) {
    Write-Host "[ERROR] DB_URL chua duoc cau hinh."
    exit 1
}

if (-not $dbConfig.DB_USERNAME) {
    Write-Host "[ERROR] DB_USERNAME chua duoc cau hinh."
    exit 1
}

$env:DB_URL = $dbConfig.DB_URL
$env:DB_USERNAME = $dbConfig.DB_USERNAME
$env:DB_PASSWORD = Read-EncryptedPassword -Path $passwordFile

if (-not $env:DB_PASSWORD) {
    Write-Host "[ERROR] Khong the giai ma DB_PASSWORD."
    Write-Host "Hay xoa thu muc $configDir roi chay lai de thiet lap lai."
    exit 1
}

Write-Host "[1/3] Kiem tra Java..."
& cmd.exe /c "java -version >nul 2>nul"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] May chua cai Java hoac Java chua nam trong PATH."
    Write-Host "Vui long cai JDK 21 truoc khi chay backend."
    exit 1
}

Write-Host "[OK] Java da san sang."
Write-Host ""
Write-Host "[2/3] Kiem tra file backend JAR..."

$jarFile = $null

if (Test-Path -Path $releaseBackendJar) {
    $jarFile = $releaseBackendJar
} else {
    $targetDir = Join-Path $backendDir "target"
    if (Test-Path -Path $targetDir) {
        $jarFile = Get-ChildItem -Path $targetDir -Filter "*.jar" |
            Where-Object { $_.Name -notmatch "original|sources|javadoc" } |
            Select-Object -First 1 -ExpandProperty FullName
    }
}

Write-Host ""
Write-Host "[3/3] Dang khoi dong backend..."
Write-Host ""

if ($jarFile) {
    Write-Host "Chay backend bang file JAR:"
    Write-Host $jarFile
    Write-Host ""
    & java -jar $jarFile
    exit $LASTEXITCODE
}

Write-Host "Khong tim thay file JAR."
Write-Host "Se chay backend bang Maven Wrapper."
Write-Host ""

$mavenWrapper = Join-Path $backendDir "mvnw.cmd"
if (-not (Test-Path -Path $mavenWrapper)) {
    Write-Host "[ERROR] Khong tim thay mvnw.cmd."
    exit 1
}

& $mavenWrapper spring-boot:run
exit $LASTEXITCODE

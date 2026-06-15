param(
    [string]$Root = (Split-Path -Parent (Split-Path -Parent $PSScriptRoot)),
    [switch]$ValidateOnly,
    [switch]$NoSetupPrompt
)

$ErrorActionPreference = "Stop"

$Root = $Root.Trim().Trim('"').TrimEnd("\")
$backendDir = Join-Path $Root "backend"
$releaseBackendJar = Join-Path $Root "release\backend-0.0.1-SNAPSHOT.jar"
$releaseBackendNative = Join-Path $Root "release\backend.exe"
$configDir = Join-Path $env:APPDATA "LibraDesk"
$configFile = Join-Path $configDir "db-config.properties"
$passwordFile = Join-Path $configDir "db-password.dpapi"
$jsonConfigFile = Join-Path $configDir "database-config.json"
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

function Read-JsonDbConfig {
    param([string]$Path)

    $json = Get-Content -Raw -Path $Path | ConvertFrom-Json
    $hostValue = [string]$json.host
    $portValue = [string]$json.port
    $databaseValue = [string]$json.database
    $usernameValue = [string]$json.username
    $passwordValue = [string]$json.password

    if (-not $hostValue -or -not $databaseValue -or -not $usernameValue) {
        throw "database-config.json thieu host, database hoac username."
    }

    $server = $hostValue
    if ($portValue) {
        $server = "$hostValue`:$portValue"
    }

    return @{
        DB_URL = "jdbc:sqlserver://$server;databaseName=$databaseValue;encrypt=true;trustServerCertificate=true"
        DB_USERNAME = $usernameValue
        DB_PASSWORD = $passwordValue
        SOURCE = $Path
    }
}

function New-SqlConnectionStringFromJdbcUrl {
    param(
        [string]$JdbcUrl,
        [string]$Username,
        [string]$Password
    )

    if (-not $JdbcUrl.StartsWith("jdbc:sqlserver://")) {
        throw "DB_URL khong dung dinh dang jdbc:sqlserver://"
    }

    $withoutPrefix = $JdbcUrl.Substring("jdbc:sqlserver://".Length)
    $parts = $withoutPrefix.Split(";")
    $server = $parts[0]
    $database = ""

    foreach ($part in $parts) {
        $separatorIndex = $part.IndexOf("=")
        if ($separatorIndex -le 0) {
            continue
        }

        $key = $part.Substring(0, $separatorIndex).Trim()
        $value = $part.Substring($separatorIndex + 1).Trim()

        if ($key.Equals("databaseName", [StringComparison]::OrdinalIgnoreCase)) {
            $database = $value
        }
    }

    if (-not $server) {
        throw "DB_URL thieu SQL Server host/port."
    }

    if (-not $database) {
        throw "DB_URL thieu databaseName."
    }

    Add-Type -AssemblyName System.Data
    $sqlClientServer = $server
    if ($sqlClientServer -match "^([^\\,]+):(\d+)$") {
        $sqlClientServer = "$($matches[1]),$($matches[2])"
    }

    $builder = New-Object System.Data.SqlClient.SqlConnectionStringBuilder
    $builder["Data Source"] = $sqlClientServer
    $builder["Initial Catalog"] = $database
    $builder["User ID"] = $Username
    $builder["Password"] = $Password
    $builder["Encrypt"] = $true
    $builder["TrustServerCertificate"] = $true
    $builder["Connect Timeout"] = 5
    return $builder.ConnectionString
}

function Test-SqlConnection {
    param([string]$ConnectionString)

    $connection = New-Object System.Data.SqlClient.SqlConnection $ConnectionString
    try {
        $connection.Open()
    } finally {
        $connection.Dispose()
    }
}

function Test-AotJar {
    param([string]$Path)

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead($Path)

    try {
        foreach ($entry in $zip.Entries) {
            if ($entry.FullName.EndsWith("__ApplicationContextInitializer.class")) {
                return $true
            }
        }

        return $false
    } finally {
        $zip.Dispose()
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

if ((Test-Path -Path $configFile) -and (Test-Path -Path $passwordFile)) {
    Write-Host "Doc cau hinh database:"
    Write-Host $configFile
    Write-Host ""

    $dbConfig = Read-DbConfig -Path $configFile
    $env:DB_URL = $dbConfig.DB_URL
    $env:DB_USERNAME = $dbConfig.DB_USERNAME
    $env:DB_PASSWORD = Read-EncryptedPassword -Path $passwordFile
} elseif (Test-Path -Path $jsonConfigFile) {
    Write-Host "Doc cau hinh database:"
    Write-Host $jsonConfigFile
    Write-Host ""

    $dbConfig = Read-JsonDbConfig -Path $jsonConfigFile
    $env:DB_URL = $dbConfig.DB_URL
    $env:DB_USERNAME = $dbConfig.DB_USERNAME
    $env:DB_PASSWORD = $dbConfig.DB_PASSWORD
} else {
    if ($NoSetupPrompt) {
        Write-Host "[ERROR] Chua co cau hinh database."
        Write-Host "Hay chay start-backend.bat hoac start-libradesk.bat trong cua so hien thi de nhap cau hinh DB."
        exit 1
    }

    Write-Host "Chua co cau hinh database."
    Write-Host "Vui long nhap thong tin SQL Server cho lan chay dau tien."
    Write-Host ""

    & powershell -NoProfile -ExecutionPolicy Bypass -File $setupConfigScript

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[ERROR] Khong the tao cau hinh database."
        exit 1
    }

    $dbConfig = Read-DbConfig -Path $configFile
    $env:DB_URL = $dbConfig.DB_URL
    $env:DB_USERNAME = $dbConfig.DB_USERNAME
    $env:DB_PASSWORD = Read-EncryptedPassword -Path $passwordFile
}

if (-not $env:DB_URL) {
    Write-Host "[ERROR] DB_URL chua duoc cau hinh."
    exit 1
}

if (-not $env:DB_USERNAME) {
    Write-Host "[ERROR] DB_USERNAME chua duoc cau hinh."
    exit 1
}

if (-not $env:DB_PASSWORD) {
    Write-Host "[ERROR] DB_PASSWORD chua duoc cau hinh."
    Write-Host "Hay xoa thu muc $configDir roi chay lai de thiet lap lai."
    exit 1
}

Write-Host "[1/4] Kiem tra ket noi database..."

try {
    $connectionString = New-SqlConnectionStringFromJdbcUrl `
        -JdbcUrl $env:DB_URL `
        -Username $env:DB_USERNAME `
        -Password $env:DB_PASSWORD
    Test-SqlConnection -ConnectionString $connectionString
} catch {
    Write-Host "[ERROR] Khong ket noi duoc SQL Server bang cau hinh da luu."
    Write-Host ""
    Write-Host "Nguyen nhan thuong gap:"
    Write-Host "- Sai SQL username/password"
    Write-Host "- Sai SQL Server host/port"
    Write-Host "- Sai databaseName"
    Write-Host "- SQL Server chua bat TCP/IP hoac chua chay"
    Write-Host ""
    Write-Host "Cau hinh database da luu se duoc reset."
    Remove-Item -LiteralPath $configFile, $passwordFile, $jsonConfigFile -Force -ErrorAction SilentlyContinue
    Write-Host ""
    Write-Host "Chi tiet loi:"
    Write-Host $_.Exception.Message
    Write-Host ""

    if ($NoSetupPrompt) {
        Write-Host "Tien trinh nen se khong hoi nhap lai cau hinh de tranh bi treo."
        Write-Host "Hay chay start-backend.bat hoac start-libradesk.bat de nhap lai cau hinh DB."
        exit 1
    }

    Write-Host "Vui long nhap lai thong tin SQL Server."
    Write-Host ""

    & powershell -NoProfile -ExecutionPolicy Bypass -File $setupConfigScript

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[ERROR] Khong the tao lai cau hinh database."
        exit 1
    }

    $dbConfig = Read-DbConfig -Path $configFile
    $env:DB_URL = $dbConfig.DB_URL
    $env:DB_USERNAME = $dbConfig.DB_USERNAME
    $env:DB_PASSWORD = Read-EncryptedPassword -Path $passwordFile
}

Write-Host "[OK] Ket noi database thanh cong."
Write-Host ""

Write-Host "[2/4] Kiem tra Java..."
& cmd.exe /c "java -version >nul 2>nul"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] May chua cai Java hoac Java chua nam trong PATH."
    Write-Host "Vui long cai JDK 21 truoc khi chay backend."
    exit 1
}

Write-Host "[OK] Java da san sang."
Write-Host ""
Write-Host "[3/4] Kiem tra file backend JAR..."

if ((Test-Path -Path $releaseBackendNative) -or (Test-Path -Path $releaseBackendJar)) {
    $hasBackendArtifact = $true
} else {
    $hasBackendArtifact = $false
}

if (-not $hasBackendArtifact) {
    Write-Host "[ERROR] Khong tim thay backend release artifact:"
    Write-Host $releaseBackendJar
    Write-Host $releaseBackendNative
    Write-Host ""
    Write-Host "Hay build backend release truoc khi chay."
    exit 1
}

if ($ValidateOnly) {
    Write-Host ""
    Write-Host "[OK] Backend da san sang de khoi dong."
    exit 0
}

Write-Host ""
Write-Host "[4/4] Dang khoi dong backend..."
Write-Host ""

if (Test-Path -Path $releaseBackendNative) {
    Write-Host "Chay backend bang native executable:"
    Write-Host $releaseBackendNative
    Write-Host ""
    & $releaseBackendNative
    exit $LASTEXITCODE
}

Write-Host "Chay backend bang file JAR:"
Write-Host $releaseBackendJar
Write-Host ""

$javaArgs = @("-XX:TieredStopAtLevel=1", "-Xms128m", "-Xmx512m")

if (Test-AotJar -Path $releaseBackendJar) {
    Write-Host "AOT mode: enabled"
    $javaArgs += "-Dspring.aot.enabled=true"
} else {
    Write-Host "AOT mode: disabled"
}

$javaArgs += @("-jar", $releaseBackendJar)
& java @javaArgs
exit $LASTEXITCODE

param(
    [string]$ConfigDir = (Join-Path $env:APPDATA "LibraDesk")
)

$ErrorActionPreference = "Stop"

$configFile = Join-Path $ConfigDir "db-config.properties"
$passwordFile = Join-Path $ConfigDir "db-password.dpapi"
$jsonConfigFile = Join-Path $ConfigDir "database-config.json"

function ConvertTo-PlainText {
    param([System.Security.SecureString]$SecureValue)

    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureValue)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

function New-SqlConnectionString {
    param(
        [string]$Server,
        [string]$Database,
        [string]$Username,
        [string]$Password
    )

    Add-Type -AssemblyName System.Data
    $sqlClientServer = $Server
    if ($sqlClientServer -match "^([^\\,]+):(\d+)$") {
        $sqlClientServer = "$($matches[1]),$($matches[2])"
    }

    $builder = New-Object System.Data.SqlClient.SqlConnectionStringBuilder
    $builder["Data Source"] = $sqlClientServer
    $builder["Initial Catalog"] = $Database
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

function Read-RequiredValue {
    param(
        [string]$Prompt,
        [string]$DefaultValue = ""
    )

    $emptyAttempts = 0
    while ($true) {
        if ($DefaultValue) {
            $value = Read-Host "$Prompt [$DefaultValue]"
            if ([string]::IsNullOrWhiteSpace($value)) {
                return $DefaultValue
            }
        } else {
            $value = Read-Host $Prompt
        }

        if (-not [string]::IsNullOrWhiteSpace($value)) {
            return $value.Trim()
        }

        $emptyAttempts++
        if ($emptyAttempts -ge 3) {
            throw "Khong nhan duoc gia tri cho: $Prompt"
        }

        Write-Host "Gia tri nay khong duoc de trong."
    }
}

New-Item -ItemType Directory -Path $ConfigDir -Force | Out-Null

Write-Host "=========================================="
Write-Host "      THIET LAP DATABASE LIBRADESK"
Write-Host "=========================================="
Write-Host ""
Write-Host "Thong tin nay chi can nhap lan dau."
Write-Host "Mat khau se duoc ma hoa theo tai khoan Windows hien tai."
Write-Host ""

while ($true) {
    $server = Read-RequiredValue "SQL Server host/port" "localhost:1433"
    $database = Read-RequiredValue "Database name" "QuanLyThuVien"
    $username = Read-RequiredValue "SQL username"

    $emptyPasswordAttempts = 0
    while ($true) {
        $securePassword = Read-Host "SQL password" -AsSecureString
        if ($securePassword.Length -gt 0) {
            break
        }

        $emptyPasswordAttempts++
        if ($emptyPasswordAttempts -ge 3) {
            throw "Khong nhan duoc SQL password."
        }

        Write-Host "Mat khau khong duoc de trong."
    }

    if ($server -match "^\d+$") {
        $server = "localhost:$server"
    }

    $dbUrl = "jdbc:sqlserver://$server;databaseName=$database;encrypt=true;trustServerCertificate=true"

    Write-Host ""
    Write-Host "Kiem tra ket noi SQL Server..."

    $plainPassword = ConvertTo-PlainText $securePassword
    $connectionString = New-SqlConnectionString `
        -Server $server `
        -Database $database `
        -Username $username `
        -Password $plainPassword

    try {
        Test-SqlConnection -ConnectionString $connectionString
    } catch {
        Remove-Item -LiteralPath $configFile, $passwordFile, $jsonConfigFile -Force -ErrorAction SilentlyContinue

        Write-Host ""
        Write-Host "[ERROR] Khong ket noi duoc SQL Server voi thong tin vua nhap."
        Write-Host ""
        Write-Host "Thong tin vua nhap da duoc reset. Vui long nhap lai."
        Write-Host ""
        Write-Host "Hay kiem tra lai:"
        Write-Host "- SQL Server host/port"
        Write-Host "- Database name"
        Write-Host "- SQL username"
        Write-Host "- SQL password"
        Write-Host "- SQL Server da bat TCP/IP va port 1433 neu dung localhost:1433"
        Write-Host ""
        Write-Host "Chi tiet loi:"
        Write-Host $_.Exception.Message
        Write-Host ""
        continue
    }

    Write-Host "[OK] Ket noi SQL Server thanh cong."

    @(
        "DB_URL=$dbUrl"
        "DB_USERNAME=$username"
    ) | Set-Content -Path $configFile -Encoding ASCII

    $securePassword | ConvertFrom-SecureString | Set-Content -Path $passwordFile -Encoding ASCII
    Remove-Item -LiteralPath $jsonConfigFile -Force -ErrorAction SilentlyContinue

    Write-Host ""
    Write-Host "[OK] Da luu cau hinh database tai:"
    Write-Host $configFile
    break
}

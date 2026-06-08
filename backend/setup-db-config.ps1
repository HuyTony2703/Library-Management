param(
    [string]$ConfigDir = (Join-Path $env:APPDATA "LibraDesk")
)

$ErrorActionPreference = "Stop"

$configFile = Join-Path $ConfigDir "db-config.properties"
$passwordFile = Join-Path $ConfigDir "db-password.dpapi"

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

@(
    "DB_URL=$dbUrl"
    "DB_USERNAME=$username"
) | Set-Content -Path $configFile -Encoding ASCII

$securePassword | ConvertFrom-SecureString | Set-Content -Path $passwordFile -Encoding ASCII

Write-Host ""
Write-Host "[OK] Da luu cau hinh database tai:"
Write-Host $configFile

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

    if ($sqlClientServer -notmatch "^(tcp|np|lpc):") {
        $sqlClientServer = "tcp:$sqlClientServer"
    }

    $builder = New-Object System.Data.SqlClient.SqlConnectionStringBuilder
    $builder["Data Source"] = $sqlClientServer
    $builder["Initial Catalog"] = $Database
    $builder["User ID"] = $Username
    $builder["Password"] = $Password
    $builder["Encrypt"] = $true
    $builder["TrustServerCertificate"] = $true
    $builder["Connect Timeout"] = 4
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

function Normalize-SqlServerInput {
    param([string]$Server)

    $normalized = $Server.Trim()

    if ($normalized -match "^\d+$") {
        return "localhost:$normalized"
    }

    if ($normalized -match "^[,:](\d+)$") {
        return "localhost:$($matches[1])"
    }

    if ($normalized -match "^([^\\,]+),(\d+)$") {
        return "$($matches[1]):$($matches[2])"
    }

    return $normalized
}

function Get-SqlServerCandidates {
    param([string]$ServerInput)

    $normalized = Normalize-SqlServerInput -Server $ServerInput
    $candidates = New-Object System.Collections.Generic.List[string]
    $candidates.Add($normalized)

    if ($normalized -match "^(localhost|127\.0\.0\.1|\.)(:1433)?$") {
        $candidates.Add("localhost:1433")
        $candidates.Add("localhost")
        $candidates.Add("localhost\SQLEXPRESS")
        $candidates.Add(".\SQLEXPRESS")
    }

    return $candidates | Select-Object -Unique
}

function Test-LocalSqlPort1433Listening {
    try {
        $listener = Get-NetTCPConnection -LocalPort 1433 -State Listen -ErrorAction Stop | Select-Object -First 1
        return $null -ne $listener
    } catch {
        return $false
    }
}

function New-JdbcUrl {
    param(
        [string]$Server,
        [string]$Database
    )

    return "jdbc:sqlserver://$Server;databaseName=$Database;encrypt=true;trustServerCertificate=true"
}

New-Item -ItemType Directory -Path $ConfigDir -Force | Out-Null

Write-Host "=========================================="
Write-Host "      THIET LAP DATABASE LIBRADESK"
Write-Host "=========================================="
Write-Host ""
Write-Host "Thong tin nay chi can nhap lan dau."
Write-Host "Mat khau se duoc ma hoa theo tai khoan Windows hien tai."
Write-Host "Goi y: bam Enter de dung localhost:1433, hoac nhap localhost\SQLEXPRESS neu dung SQL Server Express."
Write-Host "Neu chi nhap 1433, he thong se tu hieu la localhost:1433."
Write-Host ""

while ($true) {
    $serverInput = Read-RequiredValue "SQL Server host/port" "localhost:1433"
    $serverCandidates = @(Get-SqlServerCandidates -ServerInput $serverInput)
    $server = $serverCandidates[0]

    if ($server -ne $serverInput.Trim()) {
        Write-Host "Da hieu SQL Server la: $server"
    }

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

    Write-Host ""
    Write-Host "Kiem tra ket noi SQL Server..."

    $plainPassword = ConvertTo-PlainText $securePassword
    $connectedServer = $null
    $connectionErrors = @()

    foreach ($candidate in $serverCandidates) {
        try {
            Write-Host "Dang thu: $candidate"
            $connectionString = New-SqlConnectionString `
                -Server $candidate `
                -Database $database `
                -Username $username `
                -Password $plainPassword

            Test-SqlConnection -ConnectionString $connectionString
            $connectedServer = $candidate
            break
        } catch {
            $connectionErrors += "- ${candidate}: $($_.Exception.Message)"
        }
    }

    if (-not $connectedServer) {
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
        if (($serverCandidates -contains "localhost:1433") -and -not (Test-LocalSqlPort1433Listening)) {
            Write-Host ""
            Write-Host "[GOI Y] SQL Server tren may nay chua lang nghe cong 1433."
            Write-Host "Hay bat TCP/IP trong SQL Server Configuration Manager, dat IPAll TCP Port = 1433,"
            Write-Host "xoa TCP Dynamic Ports neu co, roi restart service SQL Server (MSSQLSERVER)."
        }
        Write-Host ""
        Write-Host "Chi tiet loi:"
        $connectionErrors | ForEach-Object { Write-Host $_ }
        Write-Host ""
        continue
    }

    $server = $connectedServer
    $dbUrl = New-JdbcUrl -Server $server -Database $database

    Write-Host "[OK] Ket noi SQL Server thanh cong."
    Write-Host "Server dung de luu cau hinh: $server"

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

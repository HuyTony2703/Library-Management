# Backend E2E Test - LibraDesk
#
# Kiem thu luong chay chinh cua backend REST API dang chay:
#   - Health endpoint
#   - Login theo 3 vai tro (admin, thu thu, doc gia)
#   - Phan quyen theo role tren cac nhom endpoint
#   - Doc du lieu cho tung vai tro
#   - Mutation co cleanup (create librarian, favorite)
#
# Cach chay:
#   powershell -ExecutionPolicy Bypass -File scripts\test\e2e-backend.ps1 [-BaseUrl http://localhost:8080]
#
# Exit code: 0 = tat ca PASS, 1 = co it nhat 1 FAIL.

param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

$script:PassCount = 0
$script:FailCount = 0
$script:AdminToken = ""
$script:ThuThuToken = ""
$script:DocGiaToken = ""

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token = "",
        [string]$Body = $null
    )

    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    try {
        $params = @{
            Uri = "$BaseUrl$Path"
            Method = $Method
            Headers = $headers
            UseBasicParsing = $true
            TimeoutSec = 15
        }

        if ($Body) {
            $params["ContentType"] = "application/json"
            $params["Body"] = $Body
        }

        $resp = Invoke-WebRequest @params

        return @{
            Status = [int]$resp.StatusCode
            Content = $resp.Content
        }
    } catch {
        $status = 0
        $content = ""

        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode.value__
        }

        return @{
            Status = $status
            Content = $content
        }
    }
}

function Login {
    param([string]$Username)

    $body = @{ usernameOrEmail = $Username; password = "123456" } | ConvertTo-Json
    $result = Invoke-Api -Method "POST" -Path "/api/auth/login" -Body $body

    if ($result.Status -ne 200) {
        throw "Login $Username failed with status $($result.Status)"
    }

    $data = $result.Content | ConvertFrom-Json

    return $data
}

function Test-Case {
    param(
        [string]$Code,
        [string]$Name,
        [scriptblock]$Body
    )

    try {
        $ok = & $Body

        if ($ok) {
            $script:PassCount++
            Write-Host "PASS  $Code - $Name"
        } else {
            $script:FailCount++
            Write-Host "FAIL  $Code - $Name"
        }
    } catch {
        $script:FailCount++
        Write-Host "FAIL  $Code - $Name (exception: $($_.Exception.Message))"
    }
}

Write-Host ""
Write-Host "=== LibraDesk Backend E2E Test ==="
Write-Host "Base URL: $BaseUrl"
Write-Host ""

# ---------------------------------------------------------------- Health
Test-Case -Code "H-001" -Name "GET /api/health khong token -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/health"
    return $result.Status -eq 200
}

Test-Case -Code "H-002" -Name "GET /api/auth/me khong token -> 401" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/auth/me"
    return $result.Status -eq 401
}

# ---------------------------------------------------------------- Login
Test-Case -Code "A-001" -Name "Login admin -> 200, role QUAN_TRI_VIEN" -Body {
    $data = Login -Username "admin"
    $script:AdminToken = $data.token
    return $data.tenVaiTro -eq "QUAN_TRI_VIEN"
}

Test-Case -Code "A-002" -Name "Login thuthu01 -> 200, role THU_THU" -Body {
    $data = Login -Username "thuthu01"
    $script:ThuThuToken = $data.token
    return $data.tenVaiTro -eq "THU_THU"
}

Test-Case -Code "A-003" -Name "Login docgia01 -> 200, role DOC_GIA" -Body {
    $data = Login -Username "docgia01"
    $script:DocGiaToken = $data.token
    return $data.tenVaiTro -eq "DOC_GIA"
}

Test-Case -Code "A-004" -Name "Login sai mat khau -> 400 (BUSINESS_ERROR)" -Body {
    $body = @{ usernameOrEmail = "admin"; password = "wrongpass" } | ConvertTo-Json
    $result = Invoke-Api -Method "POST" -Path "/api/auth/login" -Body $body
    return $result.Status -eq 400
}

# ---------------------------------------------------------------- Permission - Admin
Test-Case -Code "P-001" -Name "Admin GET /api/admin/librarians -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/admin/librarians" -Token $script:AdminToken
    return $result.Status -eq 200
}

Test-Case -Code "P-002" -Name "Thu thu GET /api/admin/librarians -> 403" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/admin/librarians" -Token $script:ThuThuToken
    return $result.Status -eq 403
}

Test-Case -Code "P-003" -Name "Doc gia GET /api/admin/librarians -> 403" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/admin/librarians" -Token $script:DocGiaToken
    return $result.Status -eq 403
}

Test-Case -Code "P-004" -Name "Admin GET /api/admin/rules/current -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/admin/rules/current" -Token $script:AdminToken
    return $result.Status -eq 200
}

# ---------------------------------------------------------------- Permission - Staff
Test-Case -Code "P-005" -Name "Thu thu GET /api/staff/me/context -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/staff/me/context" -Token $script:ThuThuToken
    return $result.Status -eq 200
}

Test-Case -Code "P-006" -Name "Admin GET /api/staff/me/context -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/staff/me/context" -Token $script:AdminToken
    return $result.Status -eq 200
}

Test-Case -Code "P-007" -Name "Doc gia GET /api/staff/me/context -> 403" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/staff/me/context" -Token $script:DocGiaToken
    return $result.Status -eq 403
}

Test-Case -Code "P-008" -Name "Thu thu GET /api/staff/readers/search?q=DG001 -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/staff/readers/search?q=DG001&limit=5" -Token $script:ThuThuToken
    return $result.Status -eq 200
}

Test-Case -Code "P-009" -Name "Doc gia GET /api/staff/readers/search -> 403" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/staff/readers/search?q=DG001&limit=5" -Token $script:DocGiaToken
    return $result.Status -eq 403
}

# ---------------------------------------------------------------- Permission - Reader
Test-Case -Code "P-010" -Name "Doc gia GET /api/reader/me -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/reader/me" -Token $script:DocGiaToken
    return $result.Status -eq 200
}

Test-Case -Code "P-011" -Name "Thu thu GET /api/reader/me -> 403" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/reader/me" -Token $script:ThuThuToken
    return $result.Status -eq 403
}

# ---------------------------------------------------------------- Read flows
Test-Case -Code "R-001" -Name "Doc gia GET /api/reader/books -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/reader/books" -Token $script:DocGiaToken
    return $result.Status -eq 200
}

Test-Case -Code "R-002" -Name "Doc gia GET /api/reader/loans/current -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/reader/loans/current" -Token $script:DocGiaToken
    return $result.Status -eq 200
}

Test-Case -Code "R-003" -Name "Doc gia GET /api/reader/notifications -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/reader/notifications" -Token $script:DocGiaToken
    return $result.Status -eq 200
}

Test-Case -Code "R-004" -Name "Doc gia GET /api/reader/rules/current -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/reader/rules/current" -Token $script:DocGiaToken
    return $result.Status -eq 200
}

Test-Case -Code "R-005" -Name "Thu thu GET /api/staff/catalog/titles/search?q=DG -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/staff/catalog/titles/search?q=DG&limit=5" -Token $script:ThuThuToken
    return $result.Status -eq 200
}

Test-Case -Code "R-005b" -Name "Thu thu search voi q rong -> 400 (VALIDATION_ERROR)" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/staff/catalog/titles/search?q=&limit=5" -Token $script:ThuThuToken
    return $result.Status -eq 400
}

Test-Case -Code "R-006" -Name "Admin GET /api/admin/reports/debts -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/admin/reports/debts" -Token $script:AdminToken
    return $result.Status -eq 200
}

# ---------------------------------------------------------------- Mutation - create librarian + cleanup
Test-Case -Code "M-001" -Name "Admin POST /api/admin/librarians -> 200" -Body {
    $stamp = Get-Date -Format "HHmmssfff"
    $payload = @{
        maNhanVien = "NV_E2E_$stamp"
        maTaiKhoan = "TK_E2E_$stamp"
        tenDangNhap = "e2e_thu_$stamp"
        matKhau = "123456"
        emailDangNhap = "e2e_thu_$stamp@library.vn"
        maChiNhanh = "CN_TD"
        hoTen = "E2E Test Librarian"
        ngaySinh = "1998-01-01"
        email = "e2e_thu_$stamp@library.vn"
        soDienThoai = "0933333333"
        diaChi = "TP.HCM"
    } | ConvertTo-Json

    $result = Invoke-Api -Method "POST" -Path "/api/admin/librarians" -Token $script:AdminToken -Body $payload

    if ($result.Status -eq 200 -and $result.Content) {
        $created = $result.Content | ConvertFrom-Json
        if ($created.maNhanVien) {
            $script:CreatedLibrarianId = $created.maNhanVien
        }
    }

    return $result.Status -eq 200
}

Test-Case -Code "M-002" -Name "Admin GET librarian vua tao -> 200" -Body {
    if (-not $script:CreatedLibrarianId) {
        return $false
    }

    $result = Invoke-Api -Method "GET" -Path "/api/admin/librarians/$($script:CreatedLibrarianId)" -Token $script:AdminToken
    return $result.Status -eq 200
}

Test-Case -Code "M-003" -Name "Thu thu khong duoc tao librarian -> 403" -Body {
    $stamp = Get-Date -Format "HHmmssfff"
    $payload = @{
        maNhanVien = "NV_E2E_FORBID_$stamp"
        maTaiKhoan = "TK_E2E_FORBID_$stamp"
        tenDangNhap = "e2e_forbid_$stamp"
        matKhau = "123456"
        emailDangNhap = "e2e_forbid_$stamp@library.vn"
        maChiNhanh = "CN_TD"
        hoTen = "E2E Forbidden"
        ngaySinh = "1998-01-01"
        email = "e2e_forbid_$stamp@library.vn"
        soDienThoai = "0933333333"
        diaChi = "TP.HCM"
    } | ConvertTo-Json

    $result = Invoke-Api -Method "POST" -Path "/api/admin/librarians" -Token $script:ThuThuToken -Body $payload
    return $result.Status -eq 403
}

# Cleanup librarian da tao (khong tinh vao ket qua)
if ($script:CreatedLibrarianId) {
    try {
        Invoke-Api -Method "DELETE" -Path "/api/admin/librarians/$($script:CreatedLibrarianId)?mode=hard" -Token $script:AdminToken | Out-Null
    } catch {
        # best-effort cleanup
    }
}

# ---------------------------------------------------------------- Mutation - reader favorite + cleanup
Test-Case -Code "M-004" -Name "Doc gia GET /api/reader/favorites -> 200" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/reader/favorites" -Token $script:DocGiaToken
    return $result.Status -eq 200
}

Test-Case -Code "M-005" -Name "Doc gia lay ma dau sach tu books de test" -Body {
    $result = Invoke-Api -Method "GET" -Path "/api/reader/books" -Token $script:DocGiaToken

    if ($result.Status -ne 200) {
        return $false
    }

    try {
        $list = $result.Content | ConvertFrom-Json
        $items = @()

        if ($list -is [System.Array]) {
            $items = $list
        } elseif ($list.items) {
            $items = $list.items
        }

        if ($items.Count -gt 0 -and $items[0].maDauSach) {
            $script:TestMaDauSach = [string]$items[0].maDauSach
            return $true
        }
    } catch {
        return $false
    }

    return $false
}

Test-Case -Code "M-006" -Name "Doc gia POST favorite dau sach -> 200" -Body {
    if (-not $script:TestMaDauSach) {
        return $false
    }

    $result = Invoke-Api -Method "POST" -Path "/api/reader/favorites/$($script:TestMaDauSach)" -Token $script:DocGiaToken
    return $result.Status -eq 200
}

Test-Case -Code "M-007" -Name "Doc gia GET favorite exists -> true" -Body {
    if (-not $script:TestMaDauSach) {
        return $false
    }

    $result = Invoke-Api -Method "GET" -Path "/api/reader/favorites/$($script:TestMaDauSach)/exists" -Token $script:DocGiaToken

    if ($result.Status -ne 200) {
        return $false
    }

    $data = $result.Content | ConvertFrom-Json
    return $data.exists -eq $true
}

Test-Case -Code "M-008" -Name "Doc gia DELETE favorite -> 200" -Body {
    if (-not $script:TestMaDauSach) {
        return $false
    }

    $result = Invoke-Api -Method "DELETE" -Path "/api/reader/favorites/$($script:TestMaDauSach)" -Token $script:DocGiaToken
    return $result.Status -eq 200
}

Test-Case -Code "M-009" -Name "Doc gia GET favorite exists sau khi xoa -> false" -Body {
    if (-not $script:TestMaDauSach) {
        return $false
    }

    $result = Invoke-Api -Method "GET" -Path "/api/reader/favorites/$($script:TestMaDauSach)/exists" -Token $script:DocGiaToken

    if ($result.Status -ne 200) {
        return $false
    }

    $data = $result.Content | ConvertFrom-Json
    return $data.exists -eq $false
}

# ---------------------------------------------------------------- Tong ket
Write-Host ""
Write-Host "=== Ket qua ==="
Write-Host "PASS: $script:PassCount"
Write-Host "FAIL: $script:FailCount"

if ($script:FailCount -gt 0) {
    Write-Host "RESULT: FAIL"
    exit 1
} else {
    Write-Host "RESULT: PASS"
    exit 0
}
# Backend Permission Test

> **Trạng thái:** Biên bản kiểm thử lịch sử, không phải kết quả của lần build hiện tại. Ngày, branch, port và dữ liệu bên dưới ghi lại đúng môi trường đã chạy khi lập biên bản.

Ngày chạy: 2026-06-09

Branch: `test`

Backend runtime test: `http://localhost:8081`, chạy từ source hiện tại với SQL Server integrated security.

## Build

```powershell
.\mvnw.cmd clean compile -DskipTests
```

Kết quả: `BUILD SUCCESS`.

## Role Contract

- DB `VAITRO.TenVaiTro`: `QUAN_TRI_VIEN`, `THU_THU`, `DOC_GIA`
- Spring authorities: `ROLE_QUAN_TRI_VIEN`, `ROLE_THU_THU`, `ROLE_DOC_GIA`
- `SecurityConfig` dùng `hasRole(...)` / `hasAnyRole(...)` với role không có tiền tố `ROLE_`
- Token filter tạo `SimpleGrantedAuthority` từ `AuthUser.getRoleAuthority()`, hiện lấy theo `tenVaiTro`

## Runtime Results

Tài khoản test:

- Admin: `admin / 123456`
- Thủ thư: `thuthu01 / 123456`
- Độc giả: `docgia01 / 123456`

| Test | Kết quả |
| --- | --- |
| `GET /api/health` không token | `200` |
| `GET /api/auth/me` không token | `401` |
| `GET /api/admin/comments` không token | `401` |
| `GET /api/staff/readers/DG001/debts` không token | `401` |
| Login admin trả `tenVaiTro` | `QUAN_TRI_VIEN` |
| Login thủ thư trả `tenVaiTro` | `THU_THU` |
| Login độc giả trả `tenVaiTro` | `DOC_GIA` |
| Admin gọi `GET /api/admin/comments` | `200` |
| Admin gọi `GET /api/staff/readers/DG001/debts` | `200` |
| Thủ thư gọi `GET /api/staff/readers/DG001/debts` | `200` |
| Thủ thư gọi `GET /api/admin/comments` | `403` |
| Độc giả gọi `GET /api/admin/comments` | `403` |
| Độc giả gọi `GET /api/staff/readers/DG001/debts` | `403` |
| Độc giả gọi `GET /api/books` | `200` |
| Độc giả gọi `POST /api/categories` | `403` |

## Auth Response Check

`GET /api/auth/me` với token thủ thư trả:

- `maTaiKhoan=TK_THUTHU01`
- `tenDangNhap=thuthu01`
- `maVaiTro=VT_THU_THU`
- `tenVaiTro=THU_THU`
- `maNhanVien=NV_TT001`
- `maDocGia=null`

## Notes

- `SecurityConfig` đã có `/api/admin/**` chỉ cho `QUAN_TRI_VIEN`.
- `/api/staff/**` cho `THU_THU` và `QUAN_TRI_VIEN`.
- Endpoint cũ như `/api/reports/**`, `/api/readers/**`, `/api/books/**`, `/api/book-copies/**` được giữ để tránh vỡ app hiện tại.
- Test locked account chưa chạy vì seed hiện tại không có credential demo cho tài khoản trạng thái `Khóa` hoặc `Ngừng hoạt động`.

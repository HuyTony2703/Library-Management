# Hướng dẫn chạy SQL Server cho LibraDesk

Tài liệu này dành cho trường hợp cài mới hoặc launcher báo không kết nối được database.

## 1. Xác nhận SQL Server đang chạy

Mở `Services` trên Windows và tìm một trong các dịch vụ:

- `SQL Server (MSSQLSERVER)` với default instance.
- `SQL Server (SQLEXPRESS)` với SQL Server Express.
- Tên instance riêng do bạn đã cài đặt.

Trạng thái dịch vụ phải là `Running`.

## 2. Xác định đúng server name

Các dạng thường gặp:

| Cấu hình | Giá trị ví dụ |
|---|---|
| Default instance qua TCP | `localhost:1433` |
| SQL Express theo instance name | `localhost\SQLEXPRESS` |
| Máy khác trong mạng | `192.168.1.20:1433` |

Không nhập riêng `1433` vào ô host/port. Nếu dùng port, giá trị đầy đủ phải giống `localhost:1433`.

## 3. Bật TCP/IP khi dùng host và port

1. Mở SQL Server Configuration Manager.
2. Chọn `SQL Server Network Configuration`.
3. Mở `Protocols for <instance>`.
4. Bật `TCP/IP`.
5. Trong TCP/IP Properties, kiểm tra port tại `IPAll`.
6. Khởi động lại dịch vụ SQL Server.

Nếu SQL Server dùng dynamic port, nên xác định đúng port hiện tại hoặc cấu hình port cố định cho môi trường demo.

## 4. Tạo database và dữ liệu demo

Trong SSMS hoặc Azure Data Studio, chạy lần lượt:

```text
database/scripts/01_full_database.sql
database/scripts/02_seed_demo_data.sql
```

Sau đó kiểm tra:

```sql
USE QuanLyThuVien;
GO

SELECT COUNT(*) AS SoTaiKhoan FROM TAIKHOAN;
SELECT COUNT(*) AS SoDocGia FROM DOCGIA;
SELECT COUNT(*) AS SoCuonSach FROM CUONSACH;
```

## 5. Kiểm tra phương thức đăng nhập

### SQL Authentication

Nếu dùng tài khoản như `sa`:

- SQL Server phải bật Mixed Mode Authentication.
- Login phải được enable.
- Login phải có quyền truy cập `QuanLyThuVien`.
- Không để mật khẩu trống.

### Windows Authentication

Launcher hiện hỏi SQL username/password để tạo cấu hình runtime. Nếu môi trường chỉ dùng Windows Authentication, hãy chạy backend từ IDE với cấu hình phù hợp hoặc tạo một SQL login riêng cho ứng dụng.

## 6. Nhập cấu hình trong launcher

Ví dụ với SQL Server local:

```text
SQL Server host/port: localhost:1433
Database name: QuanLyThuVien
SQL username: sa
SQL password: ********
```

Không nhập:

```text
SQL Server host/port: 1433
```

vì `1433` chỉ là port, không phải địa chỉ server.

## 7. Reset cấu hình sai

Đóng app, sau đó chạy:

```bat
scripts\runtime\reset-db-config.bat
start-libradesk.bat
```

Script reset sẽ dừng backend đang giữ port `8080` và xóa cấu hình database trong `%APPDATA%\LibraDesk`.

## 8. Đọc log backend

Chạy backend riêng:

```bat
scripts\runtime\start-backend.bat
```

Hoặc xem các file:

```text
%APPDATA%\LibraDesk\backend.out.log
%APPDATA%\LibraDesk\backend.err.log
```

## 9. Giải thích lỗi thường gặp

### `The server was not found or was not accessible`

Nguyên nhân thường là sai host/instance, dịch vụ chưa chạy, TCP/IP chưa bật hoặc firewall chặn port.

### `Login failed for user`

Username/password sai, SQL Authentication chưa bật hoặc login chưa có quyền vào database.

### `Cannot open database QuanLyThuVien`

Database chưa được tạo, nhập sai tên database hoặc login không có quyền truy cập.

### Backend treo lâu khi khởi động

Connection timeout với SQL Server có thể khiến backend chờ. Hãy chạy `start-backend.bat` để xem lỗi trực tiếp thay vì mở lại launcher nhiều lần.

## 10. Checklist cuối

- [ ] SQL Server service đang chạy.
- [ ] Host/instance đúng.
- [ ] TCP/IP và port đúng nếu dùng TCP.
- [ ] Database `QuanLyThuVien` đã được tạo.
- [ ] Đã chạy seed demo.
- [ ] SQL login đăng nhập được bằng SSMS.
- [ ] Health endpoint trả kết quả tại `http://localhost:8080/api/health`.

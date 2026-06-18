# Database

Thư mục `database/` chứa script SQL Server cho LibraDesk.

## Thứ Tự Khởi Tạo

Chạy theo thứ tự sau trên database `QuanLyThuVien`:

```text
database/scripts/01_full_database.sql
database/scripts/02_seed_demo_data.sql
```

Trong đó:

- `01_full_database.sql`: tạo schema, bảng, ràng buộc và dữ liệu nền cần thiết.
- `02_seed_demo_data.sql`: tạo dữ liệu demo đầy đủ cho admin, thủ thư, độc giả, sách, mượn trả, nợ, phiếu thu, bình luận và thông báo.

`02_seed_demo_data.sql` cũng có các case demo khoản nợ để kiểm tra màn thu tiền:

| Độc giả | Tình huống |
|---|---|
| `DG024` | Nợ trả trễ, chưa thanh toán |
| `DG025` | Nợ hỏng sách, chưa thanh toán |
| `DG026` | Nhiều khoản nợ, đã thanh toán một phần |

## Script Phụ Trợ

Các script sau dùng để kiểm thử hoặc reset từng nhóm dữ liệu, không bắt buộc chạy khi cài mới:

```text
03_test_queries.sql
04_reader_notification_extra.sql
04_reader_portal_extra.sql
05_reader_membership_extra.sql
06_reader_comment_rating_reset.sql
07_reader_favorites_reset.sql
```

## Cấu Hình Runtime

Khi chạy app bằng `start-libradesk.bat`, backend sẽ hỏi cấu hình SQL Server nếu chưa có. Cấu hình được lưu trong:

```text
%APPDATA%\LibraDesk
```

Muốn nhập lại cấu hình database:

```bat
scripts\runtime\reset-db-config.bat
start-libradesk.bat
```

## Tài Khoản Demo

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Admin | `admin` | `123456` |
| Thủ thư | `thuthu01` | `123456` |
| Độc giả | `docgia01` | `123456` |

Các độc giả demo nợ dùng chung mật khẩu seed mặc định nếu tài khoản được bật trong dữ liệu demo.

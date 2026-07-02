# Cơ sở dữ liệu LibraDesk

Thư mục `database/` chứa schema, dữ liệu demo và các script hỗ trợ cho Microsoft SQL Server.

## Yêu cầu

- SQL Server đang hoạt động.
- Tài khoản có quyền tạo database, bảng, khóa ngoại và dữ liệu.
- SQL Server Management Studio, Azure Data Studio hoặc công cụ chạy T-SQL tương đương.

Database mặc định của ứng dụng là `QuanLyThuVien`.

## Khởi tạo lần đầu

Chạy đúng thứ tự:

| Thứ tự | Script | Mục đích |
|---:|---|---|
| 1 | `scripts/01_full_database.sql` | Tạo database, bảng, ràng buộc và dữ liệu nền |
| 2 | `scripts/02_seed_demo_data.sql` | Thêm tài khoản và dữ liệu demo |
| 8 | `scripts/08_admin_modernization_staff_context.sql` | Tạo quan hệ nhiều chi nhánh cho nhân viên và backfill chi nhánh hiện tại làm mặc định |
| 9 | `scripts/09_admin_modernization_payment_integrity.sql` | Bổ sung metadata idempotency cho phiếu thu, index khóa nợ ổn định và unique phiếu thu + khoản nợ |
| 10 | `scripts/10_admin_modernization_copy_list_indexes.sql` | Bổ sung index cho danh sách cuốn sách phân trang theo chi nhánh, trạng thái, ngày nhập và vị trí |
| 11 | `scripts/11_admin_modernization_copy_actions.sql` | Tạo lịch sử chuyển trạng thái và vị trí cuốn sách, lưu actor, lý do và before-after |
| 12 | `scripts/12_admin_modernization_reader_list_indexes.sql` | Bổ sung index cho phân trang, lọc, sắp xếp độc giả và lịch sử gói |
| 13 | `scripts/13_admin_modernization_reader_state.sql` | Tách borrowing/login lock và lifecycle event, backfill khóa legacy |
| 14 | `scripts/14_admin_modernization_reader_password_reset.sql` | Bổ sung trường phục vụ revoke token, force-change, audit và rate-limit reset mật khẩu độc giả |

`01_full_database.sql` tự tạo `QuanLyThuVien` nếu database chưa tồn tại, sau đó chuyển ngữ cảnh sang database này.

> `01_full_database.sql` là script khởi tạo đầy đủ. Không nên chạy lại trên database đã có dữ liệu nếu chưa kiểm tra nội dung và sao lưu.

## Dữ liệu demo

### Tài khoản

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Quản trị viên | `admin` | `123456` |
| Thủ thư | `thuthu01` | `123456` |
| Độc giả | `docgia01` | `123456` |

### Trường hợp công nợ

| Độc giả | Dữ liệu kiểm thử |
|---|---|
| `DG024` | Nợ trả trễ, chưa thanh toán |
| `DG025` | Nợ làm hỏng sách, chưa thanh toán |
| `DG026` | Có nhiều khoản nợ và đã thanh toán một phần |

Seed còn chứa sách sẵn có, đang mượn, đặt trước, bị hỏng, bị mất; phiếu mượn, phiếu trả, khoản phạt, phiếu thu, gói thành viên, bình luận và thông báo.

## Script hỗ trợ

Các file sau không bắt buộc khi cài mới:

| Script | Công dụng |
|---|---|
| `scripts/03_test_queries.sql` | Truy vấn kiểm tra dữ liệu và quan hệ chính |
| `scripts/04_reader_notification_extra.sql` | Dữ liệu bổ sung cho thông báo độc giả |
| `scripts/04_reader_portal_extra.sql` | Dữ liệu bổ sung cho cổng độc giả |
| `scripts/05_reader_membership_extra.sql` | Dữ liệu gói thành viên |
| `scripts/06_reader_comment_rating_reset.sql` | Reset dữ liệu đánh giá và bình luận |
| `scripts/07_reader_favorites_reset.sql` | Reset dữ liệu sách yêu thích |

Chỉ chạy script hỗ trợ khi bạn hiểu dữ liệu mà script sẽ thêm, sửa hoặc xóa.

## Cấu hình kết nối của ứng dụng

Backend mặc định dùng connection string:

```text
jdbc:sqlserver://localhost:1433;databaseName=QuanLyThuVien;encrypt=true;trustServerCertificate=true
```

Khi chạy `start-libradesk.bat` lần đầu, hệ thống yêu cầu:

- SQL Server host và port, ví dụ `localhost:1433`.
- Tên database, mặc định `QuanLyThuVien`.
- SQL username.
- SQL password.

Cấu hình được lưu tại `%APPDATA%\LibraDesk`. Mật khẩu được bảo vệ theo tài khoản Windows hiện tại.

Để nhập lại cấu hình:

```bat
scripts\runtime\reset-db-config.bat
start-libradesk.bat
```

## Kiểm tra sau khi cài

1. Mở database `QuanLyThuVien`.
2. Kiểm tra các bảng `TAIKHOAN`, `DOCGIA`, `DAUSACH`, `CUONSACH`, `PHIEUMUON`, `KHOANNO` và `PHIEUTHU`.
3. Chạy `scripts/03_test_queries.sql`.
4. Khởi động app và đăng nhập bằng một tài khoản demo.
5. Kiểm tra health endpoint: `http://localhost:8080/api/health`.

## Xử lý lỗi

Hướng dẫn chi tiết nằm tại [notes/run-database-guide.md](notes/run-database-guide.md). Các kiểm tra nhanh:

- SQL Server service đã chạy chưa.
- TCP/IP đã bật chưa.
- Port trong launcher có đúng không.
- Database `QuanLyThuVien` có tồn tại không.
- SQL Authentication có được bật không nếu dùng tài khoản SQL.
- Firewall có chặn port SQL Server không.

## Tài liệu liên quan

- [Mô tả các bảng chính](notes/table-description.md)
- [Quy tắc nghiệp vụ](notes/business-rules.md)
- [Hướng dẫn chạy và xử lý lỗi database](notes/run-database-guide.md)
- [README tổng quan](../README.md)

## Quy tắc thay đổi schema và seed

- Script schema phải có thứ tự thực thi rõ ràng và tránh phụ thuộc trạng thái thủ công ngoài tài liệu.
- Seed demo nên idempotent: chạy lại không tạo bản ghi trùng hoặc phá dữ liệu đã có.
- Không đổi/xóa mã demo đang được frontend, test hoặc tài liệu tham chiếu nếu chưa cập nhật đồng bộ.
- Thay đổi liên quan nhiều bảng phải giữ đúng khóa ngoại và được kiểm tra trong transaction khi triển khai qua backend.
- Không đặt mật khẩu thật, connection string thật hoặc dữ liệu cá nhân thật trong script SQL.
- Sau khi sửa, chạy lại truy vấn kiểm tra và thử các luồng mượn, trả, nợ, thu tiền có liên quan.

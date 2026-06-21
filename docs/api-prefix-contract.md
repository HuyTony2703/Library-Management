# Hợp đồng prefix API

Tài liệu này xác định ranh giới endpoint theo vai trò. Mục tiêu là tránh gọi sai quyền, tạo endpoint trùng và trộn API giữa các module frontend.

## Quy ước

| Prefix | Người dùng | Mục đích |
|---|---|---|
| `/api/auth/**` | Người dùng đã/đang đăng nhập tùy endpoint | Đăng nhập, hồ sơ và đổi mật khẩu |
| `/api/admin/**` | Quản trị viên | Quản trị tài khoản, quy định, báo cáo và chức năng admin-only |
| `/api/staff/**` | Thủ thư và quản trị viên | Mượn, trả, công nợ và thu tiền |
| `/api/reader/**` | Độc giả | Cổng độc giả và dữ liệu của chính tài khoản |
| `/api/options/**` | Người dùng đã đăng nhập | Danh mục dùng cho select/picker |
| `/api/health`, `/api/warmup` | Runtime | Health check và warm-up |

Một số endpoint nghiệp vụ cũ như `/api/books`, `/api/book-copies`, `/api/readers` và `/api/reports` vẫn được giữ để tương thích với giao diện hiện tại. Không đổi hoặc xóa nếu chưa cập nhật toàn bộ nơi sử dụng.

## API quản trị viên

Các nhóm chính:

- `/api/admin/librarians`: quản lý tài khoản thủ thư.
- `/api/admin/rules`: phiên bản quy định hệ thống.
- `/api/admin/reports`: báo cáo tổng hợp.
- `/api/admin/comments`: kiểm duyệt bình luận ở phạm vi quản trị.

Chỉ vai trò `QUAN_TRI_VIEN` được truy cập `/api/admin/**`.

## API thủ thư

Các nhóm chính:

- `/api/staff/loans`: lập và xem phiếu mượn.
- `/api/staff/returns`: lập và xem phiếu trả.
- `/api/staff/readers/{maDocGia}/current-loans`: sách độc giả đang mượn.
- `/api/staff/readers/{maDocGia}/debts`: công nợ của độc giả.
- `/api/staff/payments`: lập và xem phiếu thu.
- `/api/staff/readers/{maDocGia}/payments`: lịch sử phiếu thu.

Vai trò `THU_THU` và `QUAN_TRI_VIEN` được truy cập `/api/staff/**`.

## API độc giả

Các nhóm chính:

- `/api/reader/me`: hồ sơ hiện tại.
- `/api/reader/books`: tra cứu và xem chi tiết đầu sách.
- `/api/reader/loans`: sách đang mượn, gia hạn và lịch sử gia hạn.
- `/api/reader/reservations`: đặt trước và hủy đặt trước.
- `/api/reader/notifications`: danh sách, đánh dấu đọc và xóa thông báo.
- `/api/reader/membership`: gói hiện tại, danh sách gói, lịch sử và mua/gia hạn.
- `/api/reader/favorites`: sách yêu thích.
- `/api/reader/books/{maDauSach}/ratings`: đánh giá đầu sách.
- `/api/reader/books/{maDauSach}/comments`: bình luận đầu sách.
- `/api/reader/recommendations`: gợi ý sách.
- `/api/reader/rules/current`: quy định đang áp dụng.

Độc giả chỉ được thao tác dữ liệu thuộc tài khoản hiện tại; backend phải lấy danh tính từ token thay vì tin mã độc giả do client tự gửi.

## Quy tắc frontend

| API | Module nên dùng |
|---|---|
| Admin | `frontend/src/api/adminApi.js` |
| Staff | `frontend/src/api/staffApi.js` |
| Reader | `frontend/src/api/readerApi.js` |
| Auth | `frontend/src/api/authApi.js` |
| Dùng chung/legacy | `frontend/src/api/libraryApi.js` |

- Mọi module dùng `apiClient.js` cho token và xử lý lỗi chung.
- Không tự nối base URL trong page.
- Không gọi endpoint admin từ giao diện thủ thư.
- Khi thêm endpoint mới, cập nhật tài liệu mapping frontend/API.

## Kiểm tra phân quyền tối thiểu

- Không token gọi API protected phải nhận `401`.
- Đúng token nhưng sai vai trò phải nhận `403`.
- Admin gọi được API staff.
- Thủ thư không gọi được API admin-only.
- Độc giả không gọi được API staff/admin.

Xem kết quả kiểm thử mẫu tại [backend-permission-test.md](backend-permission-test.md).

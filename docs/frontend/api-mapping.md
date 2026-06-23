# Mapping giao diện frontend và API

Tài liệu này giúp tìm nhanh màn hình, module API và nhóm endpoint liên quan. Source code trong `frontend/src/api/` là nguồn chính xác nhất khi endpoint thay đổi.

## Dùng chung

| Màn hình/chức năng | Module | Endpoint chính |
|---|---|---|
| Đăng nhập | `authApi.js` | `POST /api/auth/login` |
| Tài khoản hiện tại | `authApi.js` | `GET /api/auth/me` |
| Đổi mật khẩu | `authApi.js` | `POST /api/auth/change-password` |
| Cập nhật hồ sơ | `authApi.js` | `PUT /api/auth/profile` |
| Danh mục cho select | `libraryApi.js` | `GET /api/options/**` |

## Admin và thủ thư

| Màn hình | Module | Endpoint chính |
|---|---|---|
| Quản lý đầu sách | `libraryApi.js` | `/api/books`, `/api/authors`, `/api/categories`, `/api/publishers` |
| Quản lý cuốn sách | `libraryApi.js` | `/api/book-copies`, `/api/options/book-locations`, `/api/options/book-copy-statuses` |
| Quản lý độc giả | `libraryApi.js` | `/api/readers`, `/api/options/reader-groups`, `/api/options/membership-plans` |
| Mượn sách | `staffApi.js` | `/api/staff/loans`, `/api/staff/readers/{id}/current-loans` |
| Trả sách | `staffApi.js` | `/api/staff/returns`, `/api/staff/readers/{id}/current-loans` |
| Thu tiền | `staffApi.js` | `/api/staff/readers/{id}/debts`, `/api/staff/payments` |
| Kiểm duyệt bình luận | `adminApi.js` hoặc API tương ứng theo quyền | `/api/admin/comments` |

## Chỉ quản trị viên

| Màn hình | Module | Endpoint chính |
|---|---|---|
| Tài khoản thủ thư | `adminApi.js` | `/api/admin/librarians` |
| Quy định hệ thống | `adminApi.js` | `/api/admin/rules` |
| Báo cáo hệ thống | `adminApi.js` | `/api/admin/reports/**` |

Admin có thể dùng các màn hình nghiệp vụ thủ thư, nhưng thủ thư không được gọi API admin-only.

## Độc giả

| Màn hình | Module | Endpoint chính |
|---|---|---|
| Trang chủ/hồ sơ | `readerApi.js` | `/api/reader/me` |
| Tra cứu sách | `readerApi.js` | `/api/reader/books` |
| Chi tiết sách | `readerApi.js` | `/api/reader/books/{id}`, copies, ratings và comments |
| Sách đang mượn | `readerApi.js` | `/api/reader/loans/current` |
| Gia hạn | `readerApi.js` | `/api/reader/loans/{id}/renew` |
| Đặt trước | `readerApi.js` | `/api/reader/reservations` |
| Sách yêu thích | `readerApi.js` | `/api/reader/favorites` |
| Thông báo | `readerApi.js` | `/api/reader/notifications` |
| Gói thành viên | `readerApi.js` | `/api/reader/membership/**` |
| Quy định | `readerApi.js` | `/api/reader/rules/current` |
| Gợi ý sách | `readerApi.js` | `/api/reader/recommendations` |

## Checklist khi thêm màn hình hoặc endpoint

- [ ] Endpoint nằm đúng prefix vai trò.
- [ ] SecurityConfig cấp đúng quyền.
- [ ] API module dùng `apiClient.js`.
- [ ] Page xử lý loading, empty và error.
- [ ] Mã trạng thái được đổi thành tiếng Việt khi hiển thị.
- [ ] Không render JSON response thô.
- [ ] Cập nhật Postman collection nếu endpoint dùng cho kiểm thử thủ công.
- [ ] Cập nhật file mapping này.

## Luồng dữ liệu chuẩn của một màn hình

```text
Page/Component
  -> module API theo vai trò
  -> apiClient (base URL, token, parse lỗi)
  -> backend controller/service
  -> DTO response
  -> state của page
  -> DataTable/card/modal/toast
```

Page chỉ quản lý trạng thái giao diện và payload form. Logic ghép URL, gắn token và chuẩn hóa lỗi thuộc API layer; quy tắc nghiệp vụ thuộc backend. Khi mutation thành công, ưu tiên reload dữ liệu từ API thay vì tự giả lập trạng thái server ở frontend.

# API Prefix Contract

## 1. Mục tiêu

Tài liệu này dùng để chia ranh giới API giữa người 1 và người 2, tránh tạo trùng endpoint, sửa chung file hoặc làm vỡ chức năng của nhau.

## 2. Quy ước prefix

| Prefix | Người phụ trách | Vai trò sử dụng | Mục đích |
|---|---|---|---|
| `/api/admin/**` | Người 1 | QUAN_TRI_VIEN | Quản trị hệ thống |
| `/api/staff/**` | Người 1 | THU_THU, QUAN_TRI_VIEN | Nghiệp vụ thủ thư |
| `/api/reader/**` | Người 2 | DOC_GIA | Giao diện độc giả |
| `/api/public/**` | Cả nhóm | Không cần login hoặc dùng chung | Dữ liệu công khai |
| `/api/auth/**` | Không tự ý sửa | Tất cả | Đăng nhập, thông tin tài khoản |
| `/api/options/**` | Không tự ý sửa | Tất cả role đã login | Danh mục dùng chung |

## 3. Người 1 được tạo API

### Admin

- `GET /api/admin/librarians`
- `POST /api/admin/librarians`
- `PUT /api/admin/librarians/{id}`
- `PATCH /api/admin/librarians/{id}/status`
- `POST /api/admin/librarians/{id}/reset-password`

- `GET /api/admin/rules/current`
- `GET /api/admin/rules/history`
- `POST /api/admin/rules`
- `POST /api/admin/rules/{id}/activate`

- `GET /api/admin/reports/overview`
- `GET /api/admin/reports/debts`
- `GET /api/admin/reports/current-loans`
- `GET /api/admin/reports/borrow-by-category`
- `GET /api/admin/reports/late-returns`
- `GET /api/admin/reports/payments`

- `GET /api/admin/comments`
- `PATCH /api/admin/comments/{id}/hide`
- `PATCH /api/admin/comments/{id}/delete`
- `PATCH /api/admin/comments/{id}/restore`

### Staff

- `GET /api/staff/loans/{id}`
- `POST /api/staff/loans`

- `GET /api/staff/returns/{id}`
- `POST /api/staff/returns`

- `GET /api/staff/readers/{id}/debts`
- `GET /api/staff/readers/{id}/current-loans`

- `GET /api/staff/payments/{id}`
- `POST /api/staff/payments`
- `GET /api/staff/readers/{id}/payments`

## 4. Người 2 được tạo API

- `GET /api/reader/books`
- `GET /api/reader/books/{id}`
- `GET /api/reader/loans`
- `POST /api/reader/loans/{id}/renew`
- `GET /api/reader/reservations`
- `POST /api/reader/reservations`
- `DELETE /api/reader/reservations/{id}`
- `GET /api/reader/notifications`
- `PATCH /api/reader/notifications/{id}/read`
- `GET /api/reader/membership`
- `POST /api/reader/membership/purchase`
- `POST /api/reader/comments`
- `PUT /api/reader/comments/{id}`
- `DELETE /api/reader/comments/{id}`
- `GET /api/reader/favorites`
- `POST /api/reader/favorites`
- `DELETE /api/reader/favorites/{id}`
- `GET /api/reader/recommendations/random`
- `GET /api/reader/guide`

## 5. Quy tắc không đụng nhau

- Người 1 không tạo hoặc sửa controller `/api/reader/**`.
- Người 2 không tạo hoặc sửa controller `/api/admin/**`, `/api/staff/**`.
- Không sửa trực tiếp `libraryApi.js` để thêm API mới.
- Người 1 dùng `adminApi.js` và `staffApi.js`.
- Người 2 dùng `readerApi.js`.
- Không tự ý đổi endpoint cũ nếu chưa báo nhóm.

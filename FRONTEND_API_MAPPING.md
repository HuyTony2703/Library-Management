# Mapping màn hình frontend với API backend

## 1. Màn hình đăng nhập
API cần:
- POST /api/auth/login
- GET /api/auth/me

Trạng thái:
- [ ] Đã test Postman
- [ ] Frontend đã gọi được

---

## 2. Màn hình Dashboard
API cần:
- GET /api/reports/debts
- GET /api/reports/current-loans

Trạng thái:
- [ ] Đã test Postman
- [ ] Frontend đã gọi được

---

## 3. Quản lý đầu sách
API cần:
- GET /api/books
- GET /api/books/{id}
- POST /api/books
- PUT /api/books/{id}
- DELETE /api/books/{id}
- GET /api/categories
- GET /api/authors
- GET /api/publishers

Trạng thái:
- [ ] Đã test Postman
- [ ] Frontend đã gọi được

---

## 4. Quản lý cuốn sách
API cần:
- GET /api/book-copies
- GET /api/book-copies/{id}
- POST /api/book-copies
- PUT /api/book-copies/{id}
- DELETE /api/book-copies/{id}
- GET /api/options/branches
- GET /api/options/book-locations
- GET /api/options/book-copy-statuses

Trạng thái:
- [ ] Đã test Postman
- [ ] Frontend đã gọi được

---

## 5. Quản lý độc giả
API cần:
- GET /api/readers
- GET /api/readers/{id}
- POST /api/readers
- PUT /api/readers/{id}
- DELETE /api/readers/{id}
- GET /api/options/reader-groups
- GET /api/options/membership-plans

Trạng thái:
- [ ] Đã test Postman
- [ ] Frontend đã gọi được

---

## 6. Mượn sách
API cần:
- POST /api/loans
- GET /api/loans/{id}
- GET /api/readers/{maDocGia}
- GET /api/book-copies/{maCuonSach}
- GET /api/readers/{maDocGia}/current-loans

Trạng thái:
- [ ] Đã test Postman
- [ ] Frontend đã gọi được

---

## 7. Trả sách
API cần:
- POST /api/returns
- GET /api/returns/{id}
- GET /api/readers/{maDocGia}/current-loans

Trạng thái:
- [ ] Đã test Postman
- [ ] Frontend đã gọi được

---

## 8. Khoản nợ / phiếu thu
API cần:
- GET /api/readers/{maDocGia}/debts
- GET /api/options/payment-methods
- POST /api/payments
- GET /api/payments/{id}
- GET /api/readers/{maDocGia}/payments

Trạng thái:
- [ ] Đã test Postman
- [ ] Frontend đã gọi được

---

## 9. Báo cáo
API cần:
- GET /api/reports/debts
- GET /api/reports/current-loans
- GET /api/reports/borrow-by-category?month=...&year=...
- GET /api/reports/late-returns?month=...&year=...

Trạng thái:
- [ ] Đã test Postman
- [ ] Frontend đã gọi được

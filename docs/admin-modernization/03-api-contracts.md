# Hợp đồng API đề xuất cho admin modernization

## 1. Nguyên tắc chung

- Prefix hiện tại được giữ theo security contract của dự án; endpoint mới ưu tiên `/api/staff/**` cho nghiệp vụ thủ thư và `/api/admin/**` cho admin-only.
- JSON dùng tên field ổn định; ví dụ dưới đây dùng tên tiếng Anh để dễ phân biệt contract mới. **OPEN DECISION:** giữ DTO tiếng Việt hay chuẩn hóa contract mới sang tiếng Anh.
- UI hiển thị page từ 1. Nếu backend framework dùng page 0, controller/adapter phải chuyển đổi rõ.
- Thời gian dùng ISO-8601; ngày dùng `YYYY-MM-DD`; tiền dùng số thập phân chính xác/backend `BigDecimal`, VND không nhận fraction nếu chính sách hiện tại.
- Error phải có `code`, `message`, optional `fieldErrors`, `details`, `correlationId`.
- Actor staff không lấy từ body.

## 2. Envelope phân trang

### Request

```http
GET /api/books?page=1&pageSize=20&search=java&sort=title,asc
```

Quy tắc:

- `page >= 1`.
- `pageSize ∈ {20,50,100}` hoặc tối đa 100.
- Sort field qua allowlist.
- Multi-value filter có thể lặp query param hoặc comma list; phải thống nhất toàn API.

### Response

```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "totalItems": 2438,
  "totalPages": 122,
  "sort": [{ "field": "title", "direction": "asc" }],
  "facets": {}
}
```

## 3. Error contract

```json
{
  "code": "COPY_NOT_BORROWABLE",
  "message": "Cuốn sách không ở trạng thái có thể mượn",
  "fieldErrors": [],
  "details": {
    "copyId": "CS001",
    "currentStatus": "TT_DANGMUON"
  },
  "correlationId": "..."
}
```

HTTP đề xuất:

- `400`: request/validation sai.
- `401`: chưa xác thực/token hết hạn.
- `403`: không có quyền/branch.
- `404`: entity không tồn tại.
- `409`: conflict trạng thái/trùng/đồng thời/idempotency payload khác.
- `422`: nghiệp vụ không thỏa nếu dự án muốn tách với 409; cần thống nhất.

## 4. Staff context

### GET `/api/staff/me/context`

```json
{
  "accountId": "TK001",
  "staffId": "NV001",
  "staffName": "Nguyễn Văn A",
  "role": { "id": "VT_THUTHU", "name": "Thủ thư" },
  "defaultBranch": { "id": "CN_TD", "name": "Thư viện Trung tâm" },
  "allowedBranches": [
    { "id": "CN_TD", "name": "Thư viện Trung tâm" }
  ],
  "permissions": ["STAFF_CONTEXT_READ", "LOAN_CREATE", "RETURN_CREATE", "PAYMENT_CREATE"],
  "operational": true,
  "operationalBlockReason": null
}
```

Quy tắc hiện tại:

- Reader gọi endpoint này bị 403 và không nhận dữ liệu staff/branch.
- Tài khoản hoặc nhân viên đã khóa/ngừng hoạt động không được xác thực chỉ dựa vào token cũ.
- Admin không liên kết nhân viên vẫn dùng được khu vực quản trị, nhưng context trả `staffId = null`, danh sách chi nhánh rỗng, `operational = false` và `operationalBlockReason = "STAFF_PROFILE_REQUIRED"`.
- Nhân viên chưa có chi nhánh được phép trả `operational = false` với `NO_ALLOWED_BRANCH`; có allowed branch nhưng thiếu default trả `NO_DEFAULT_BRANCH`.
- Hành vi admin trực tiếp lập mượn/trả/thu vẫn là decision gate riêng; cho tới khi chốt, branch authorization từ chối context không operational.

## 5. Option/search contract

### Generic option

```json
{
  "value": "DS001",
  "label": "Cơ sở dữ liệu nâng cao",
  "secondaryLabel": "ISBN 9786041234567",
  "metadata": {}
}
```

### Search titles

```http
GET /api/staff/catalog/titles/search?q=database&limit=15&activeOnly=true
```

Tìm mã/tên/ISBN; exact match đứng đầu.

### Search readers

```http
GET /api/staff/readers/search?q=DG001&limit=15&activeOnly=false
```

Trả mã, tên, nhóm, contact rút gọn, profile/card status; không trả PII dư thừa.

### Search book copies

```http
GET /api/staff/book-copies/search?q=8938505&branchId=CN_TD&borrowableOnly=true&limit=15
```

Trả copy ID/barcode/title/ISBN/location/status/reservation context tối thiểu.

### Location options

```http
GET /api/options/book-locations?branchId=CN_TD&areaId=KHU_A&shelfId=KE_03&q=N04
```

```json
[
  {
    "value": "VT_M01_N04",
    "label": "Kho mở tầng 2 · Kệ CNTT-03 · Ngăn 04",
    "metadata": {
      "branchId": "CN_TD",
      "areaId": "KHU_MO_T2",
      "areaName": "Kho mở tầng 2",
      "shelfId": "KE_CNTT_03",
      "shelfName": "Kệ CNTT-03",
      "active": true
    }
  }
]
```

## 6. Đầu sách

### GET `/api/books`

Filter:

```text
page, pageSize, search, statusIds, categoryIds, authorIds,
publisherIds, yearFrom, yearTo, language, hasIsbn, hasCover, sort
```

Item đề xuất:

```json
{
  "id": "DS001",
  "title": "Cơ sở dữ liệu nâng cao",
  "isbn": "9786041234567",
  "authors": [{ "id": "TG001", "name": "Nguyễn Văn A" }],
  "categories": [{ "id": "TL_CNTT", "name": "Công nghệ thông tin" }],
  "publisher": { "id": "NXB_DHQG", "name": "NXB ĐHQG-HCM" },
  "publicationYear": 2024,
  "status": { "code": "ACTIVE", "label": "Đang hiển thị" },
  "copySummary": { "total": 12, "available": 9, "borrowed": 3 },
  "updatedAt": "2026-06-19T14:30:00+07:00",
  "version": 3
}
```

### POST `/api/books`

```json
{
  "title": "Cơ sở dữ liệu nâng cao",
  "isbn": "9786041234567",
  "authorIds": ["TG001"],
  "categoryIds": ["TL_CNTT"],
  "publisherId": "NXB_DHQG",
  "publicationYear": 2024,
  "language": "vi",
  "pageCount": 420,
  "referenceValue": 180000,
  "description": "...",
  "coverAssetId": null
}
```

Mã do server sinh nếu quyết định áp dụng. ISBN duplicate trả 409 kèm existing record.

### PUT/PATCH `/api/books/{id}`

Không nhận status/actor; dùng `version` hoặc `If-Match` nếu optimistic locking được triển khai.

### GET `/api/books/{id}/copies-by-branch`

Trả danh sách bản vật lý nhóm theo chi nhánh. Admin thấy toàn bộ; thủ thư chỉ thấy các chi nhánh thuộc staff context hiện hành.

### GET `/api/books/{id}/history?limit=50`

Trả audit mới nhất của đầu sách, gồm actor, thời gian, lý do và snapshot trước/sau.

### POST `/api/books/{id}/deactivate`

```json
{ "reason": "Ngừng khai thác theo quyết định thanh lọc" }
```

### POST `/api/books/{id}/reactivate`

```json
{ "reason": "Bổ sung bản tái bản" }
```

### DELETE `/api/books/{id}`

Admin-only hard delete; backend preflight liên kết. Soft delete không dùng endpoint này trong contract mới.

### GET `/api/books/{id}/delete-preflight`

Admin-only. Trả `canDelete` và số liên kết theo nhóm cuốn sách, đặt trước, đánh giá, bình luận, yêu thích và audit.

Compatibility: `DELETE ?mode=soft` và `PATCH /restore` không còn là lifecycle API. Client phải chuyển sang `POST /deactivate` hoặc `POST /reactivate` với `reason` bắt buộc.

## 7. Cuốn sách

### GET `/api/book-copies`

Filter:

```text
page, pageSize, search, titleIds, branchIds, locationIds,
areaIds, shelfIds, statusIds, importedFrom, importedTo,
hasBarcode, hasQr, sort
```

Item trả title/branch/location/status dạng object, không chỉ mã.

- Search phía server theo mã cuốn, barcode, QR, tên đầu sách và ISBN.
- Admin có thể lọc mọi chi nhánh; thủ thư chỉ được query các chi nhánh trong staff context, request vượt scope trả 403.
- Preset frontend chỉ ánh xạ sang các filter trên, không tạo contract riêng.
- Khi có `page`, endpoint trả `PageResponse`. Contract legacy không có `page` vẫn trả mảng để giữ tương thích; admin thấy toàn bộ, thủ thư chỉ nhận cuốn thuộc branch scope.

### GET `/api/book-copies/location-filter-options?branchIds=`

Trả `areas`, `shelves`, `locations` theo branch scope để dựng cascade khu/kho → kệ → vị trí.

### GET `/api/book-copies/{id}`

Trả chi tiết cơ bản kèm tên đầu sách, ISBN, tên chi nhánh, khu/kệ, label vị trí và label trạng thái. Thủ thư không thể đọc cuốn ngoài branch scope.

### POST `/api/book-copies/batch`

Auto barcode:

```json
{
  "titleId": "DS001",
  "branchId": "CN_TD",
  "locationId": "VT_M01_N04",
  "importDate": "2026-06-19",
  "quantity": 10,
  "barcodeMode": "AUTO",
  "note": "Nhập bổ sung"
}
```

Manual barcode:

```json
{
  "titleId": "DS001",
  "branchId": "CN_TD",
  "locationId": "VT_M01_N04",
  "importDate": "2026-06-19",
  "barcodeMode": "MANUAL",
  "copies": [
    { "barcode": "8938505974192", "note": null },
    { "barcode": "8938505974193", "note": "Bìa xước nhẹ" }
  ]
}
```

Response:

```json
{
  "batchId": "BATCH-20260619-0012",
  "created": 2,
  "copies": [
    { "id": "CS000123", "barcode": "8938505974192", "status": "TT_SANCO" }
  ]
}
```

### POST `/api/book-copies/{id}/move-location`

```json
{ "locationId": "VT_NEW", "reason": "Sắp xếp lại kho" }
```

### POST `/api/book-copies/{id}/transfer-branch`

Contract phụ thuộc OPEN DECISION cross-branch/in-transit.

### POST `/api/book-copies/{id}/condition-events`

```json
{
  "action": "MARK_DAMAGED",
  "severity": "MEDIUM",
  "damageTypes": ["TORN_COVER"],
  "description": "Bìa rách",
  "reason": "Phát hiện khi kiểm kê"
}
```

Action allowlist: `MARK_DAMAGED`, `MARK_LOST`, `WITHDRAW`, `RESTORE_AFTER_REPAIR`, `RESTORE_FOUND`; backend state machine quyết định hợp lệ.

## 8. Độc giả

### GET `/api/readers`

Filter:

```text
page, pageSize, search, groupIds, planIds, profileStatuses,
cardStatuses, membershipStatuses, accountStatuses,
cardExpiryFrom, cardExpiryTo, membershipExpiryFrom,
membershipExpiryTo, borrowEligibility, sort
```

Item đề xuất gồm profile/card/membership/account/eligibility/summary tách biệt.

### POST `/api/readers`

`ReaderCreateRequest` gồm profile, group, optional initial membership và account provisioning. Card/membership expiry do backend/rule tính.

### PATCH `/api/readers/{id}/profile`

Chỉ profile fields; không password/status/card/membership.

### POST `/api/readers/{id}/card-renewals`

```json
{ "requestedStartDate": "2026-07-20", "reason": "Gia hạn thẻ" }
```

Ngày cuối do backend tính theo rule.

### POST `/api/readers/{id}/membership-changes`

```json
{ "planId": "GOI_THUONG", "effectiveDate": "2026-06-19", "reason": "Đổi gói" }
```

### POST `/api/readers/{id}/locks`

```json
{
  "scopes": ["BORROWING", "LOGIN"],
  "reason": "Vi phạm quy định",
  "lockedUntil": "2026-06-30",
  "note": "Đã thông báo"
}
```

### POST `/api/readers/{id}/unlock`

```json
{ "scopes": ["BORROWING"], "reason": "Đã xử lý vi phạm" }
```

### POST `/api/readers/{id}/deactivate` và `/reactivate`

Nhận `reason`; reactivate trả effective state và warnings nếu thẻ/gói hết hạn.

### GET `/api/staff/readers/{id}/borrowing-context`

```json
{
  "reader": { "id": "DG001", "name": "Nguyễn Thị Minh Anh" },
  "card": { "expiryDate": "2026-07-15", "status": "VALID", "remainingDays": 26 },
  "membership": { "planId": "GOI_THUONG", "status": "ACTIVE", "expiryDate": "2026-06-30" },
  "quota": { "current": 3, "maximum": 5, "remaining": 2 },
  "overdue": { "count": 0, "maxDays": 0 },
  "debt": { "outstanding": 0, "blockingThreshold": 100000 },
  "eligible": true,
  "warnings": [],
  "blockingReasons": []
}
```

### GET `/api/staff/readers/{id}/current-loans`

Trả danh sách cuốn đang mượn của độc giả gồm mã cuốn/barcode, đầu sách, chi nhánh,
ngày mượn, hạn trả, trạng thái và `overdueDays`. Endpoint chỉ đọc, dùng để hiển thị
phần thu gọn trong bước chọn độc giả; không tạo hoặc cập nhật phiếu mượn.

### POST `/api/readers/{id}/password-reset`

```json
{
  "mode": "GENERATE_TEMPORARY",
  "forceChange": true,
  "revokeSessions": true,
  "reason": "Độc giả quên mật khẩu",
  "verificationEmail": "reader@example.com",
  "verificationDateOfBirth": "2000-01-02"
}
```

Chỉ hỗ trợ `GENERATE_TEMPORARY` khi chưa có hạ tầng gửi reset link. Backend bắt buộc đối chiếu cả email và ngày sinh, áp dụng rate-limit cấu hình và không tin dữ liệu hồ sơ từ frontend ngoài mục đích đối chiếu.

Response có `temporaryPassword`, `mustChangePassword`, `sessionsRevoked`, `issuedAt`; đặt `Cache-Control: no-store`. Temporary secret chỉ trả một lần, không lưu plaintext và không xuất hiện trong audit/log/token.

## 9. Mượn sách

### GET exact barcode `/api/staff/book-copies/by-barcode/{barcode}`

Query `branchId`; trả copy/title/location/status/reservation/borrowable reasons.

### POST `/api/staff/loans/preview`

```json
{
  "readerId": "DG001",
  "branchId": "CN_TD",
  "copyIds": ["CS001", "CS018"]
}
```

Response có reader eligibility, warnings/blocks, quota before/after và item rule/due date.

### POST `/api/staff/loans`

Header:

```text
Idempotency-Key: UUID
```

Body:

```json
{
  "readerId": "DG001",
  "branchId": "CN_TD",
  "copyIds": ["CS001", "CS018"],
  "note": "Mượn tại quầy",
  "override": null
}
```

Không có staff ID hoặc due date. Response gồm server loan ID, actor, branch, createdAt, status và item due dates/rules.

Trong giai đoạn tương thích, backend vẫn chấp nhận field legacy `maNhanVienLap` nếu client cũ gửi. Field này là optional, không được dùng làm actor; nếu khác staff ID của authenticated principal, request bị từ chối 403. Cả endpoint `/api/staff/loans` và `/api/loans` legacy đều áp dụng cùng quy tắc.

## 10. Trả sách

### GET `/api/staff/open-loans/by-barcode/{barcode}`

Trả copy, title, open loan detail, reader, borrow branch, borrowed/due, overdue preview.

### GET `/api/staff/readers/{id}/open-loans`

Trả enriched item dùng làm checkbox selector; không chỉ mã.

### POST `/api/staff/returns/preview`

```json
{
  "items": [
    { "loanDetailId": "CTM001", "condition": { "type": "NORMAL" } },
    {
      "loanDetailId": "CTM002",
      "condition": {
        "type": "DAMAGED",
        "damageTypes": ["TORN_COVER"],
        "severity": "MEDIUM",
        "description": "Bìa rách"
      }
    }
  ]
}
```

Response trả late/damage/total fine, next copy status, warnings/blocking reasons.

### POST `/api/staff/returns`

Header idempotency. Body giống preview thêm note/approved adjustment reference; không actor/branch/amount tùy ý. Response gồm return ID, items, debt IDs, hold instructions và print payload data.

Trong API hiện tại trước khi hoàn tất preview/idempotency, backend vẫn nhận `maChiNhanh` và đối chiếu với allowed branches trong staff context; release này tiếp tục bắt buộc chi nhánh nhận trùng chi nhánh mượn. Field legacy `maNhanVienNhan` là optional để tương thích, không được dùng làm actor; nếu khác staff ID của authenticated principal, request bị từ chối 403. Cả `/api/staff/returns` và `/api/returns` legacy áp dụng cùng quy tắc. Chi tiết mượn được khóa pessimistic và kiểm tra lại trạng thái/phiếu trả sau khi khóa để chặn hai request trả đồng thời.

## 11. Công nợ và thu tiền

### GET `/api/staff/debtors/search`

```http
GET /api/staff/debtors/search?q=DG001&outstandingOnly=true&limit=15
```

### GET `/api/staff/readers/{id}/debt-context`

Trả totals, count, oldest debt, borrowing impact.

### GET `/api/staff/readers/{id}/debts`

Filter status/type/date/sort; item có source reference và amounts.

### POST `/api/staff/payments/preview`

Auto:

```json
{ "readerId": "DG001", "mode": "AUTO", "amount": 100000, "paymentMethodId": "PT_TIEN_MAT" }
```

Manual:

```json
{
  "readerId": "DG001",
  "mode": "MANUAL",
  "paymentMethodId": "PT_TIEN_MAT",
  "allocations": [
    { "debtId": "NO001", "amount": 25000 },
    { "debtId": "NO002", "amount": 50000 }
  ]
}
```

Response trả balance before/after và allocation before/applied/after/status.

### POST `/api/staff/payments`

Header idempotency. Body như preview cộng method-specific fields (`cashReceived`, `externalTransactionId`, note). Không staff ID/payment ID. Server sinh ID, xác định actor/branch, khóa debt và tính lại.

Contract chuyển tiếp P0 hiện tại vẫn nhận `maPhieuThu`, `maDocGia`, `maPhuongThuc`, `soTienThu` và optional `chiTietNo`. `maNhanVienThu` chỉ còn là field legacy optional: backend không dùng làm actor và trả 403 nếu khác staff ID của principal. Client nên gửi header `Idempotency-Key` (tối đa 100 ký tự); nếu thiếu, backend tạm dùng `maPhieuThu` làm key tương thích. Cùng key và cùng actor/payload trả lại phiếu đã tạo, còn payload khác bị từ chối. Backend khóa pessimistic các khoản nợ theo thứ tự ổn định, đọc lại remaining, kiểm tra tổng allocations bằng tổng phiếu và ghi audit before/after cho từng khoản trong cùng transaction.

Database migration bắt buộc: `09_admin_modernization_payment_integrity.sql` thêm metadata/fingerprint idempotency nullable cho dữ liệu cũ, unique filtered index trên key, index debt `(reader,status,date,id)` và unique `(receipt,debt)`.

### POST `/api/admin/payments/{id}/reverse`

```json
{ "reason": "Thu nhầm độc giả", "approvalReference": "..." }
```

Contract quyền/approval theo OPEN DECISION. Response gồm reversal ID, restored allocations và debt states.

## 12. Bulk action contract

### Selected IDs

```json
{
  "action": "WITHDRAW",
  "scope": { "type": "SELECTED_IDS", "ids": ["CS001", "CS002"] },
  "reason": "Thanh lý"
}
```

### All matching

```json
{
  "action": "WITHDRAW",
  "scope": {
    "type": "FILTERED_QUERY",
    "filters": { "branchIds": ["CN_TD"], "statusIds": ["TT_HONG"] },
    "excludedIds": ["CS015"]
  },
  "reason": "Thanh lý đợt tháng 6"
}
```

Response:

```json
{
  "requested": 128,
  "succeeded": 124,
  "failed": 4,
  "errors": [{ "id": "CS019", "code": "INVALID_STATE", "message": "Cuốn đang được mượn" }]
}
```

## 13. Idempotency contract

- Header bắt buộc cho create loan/return/payment.
- Scope key theo endpoint + authenticated account + key.
- Lưu request fingerprint, response/status và thời hạn retention.
- Cùng key + fingerprint trả kết quả cũ.
- Cùng key + payload khác trả 409 `IDEMPOTENCY_KEY_REUSED`.

## 14. Optimistic locking — OPEN DECISION

Danh mục/hồ sơ có thể dùng `version` hoặc ETag/If-Match để tránh cập nhật ghi đè. Giao dịch lưu thông/tài chính vẫn cần pessimistic/row locking trong transaction.

## 15. Compatibility strategy

- Có thể giữ endpoint cũ trong giai đoạn migration nhưng đánh dấu deprecated.
- Adapter frontend cũ không được làm backend tiếp tục tin actor/branch client.
- Không đổi response tất cả endpoint trong một release nếu chưa migrate consumers.
- Tài liệu mapping cũ → mới phải cập nhật khi triển khai từng milestone.

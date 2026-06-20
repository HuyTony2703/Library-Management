# Kế hoạch nghiệm thu admin modernization

## 1. Mục đích và quy ước

Tài liệu này chuyển yêu cầu thành kịch bản có thể kiểm chứng. Mỗi case có mã, lớp kiểm thử và kết quả mong đợi. Khi một `OPEN DECISION` chưa chốt, case tương ứng ở trạng thái `BLOCKED BY DECISION`, không tự chọn hành vi.

Các lớp kiểm thử:

- **FE:** component/UI/E2E frontend.
- **API:** controller/contract/authorization.
- **SVC:** service/business rule/transaction.
- **DB:** constraint/index/migration/concurrency.
- **SEC:** security/abuse case.
- **UAT:** nghiệp vụ thủ thư thực tế.

Môi trường test phải dùng database/fixture riêng; không thao tác dữ liệu production.

## 2. Baseline và migration

### BASE-001 — Build baseline

**Lớp:** API/FE. Chạy backend tests, frontend lint/build trước thay đổi. Ghi nhận pass/fail hiện tại để không quy lỗi cũ cho thay đổi mới.

### BASE-002 — Migration trên database có dữ liệu

**Lớp:** DB. Chạy migration trên snapshot test có đầu sách, cuốn, độc giả, mượn/trả/nợ/thu. Mong đợi không mất liên kết hoặc thay đổi số tiền/trạng thái ngoài mục đích migration.

### BASE-003 — Migration chạy lặp/version

**Lớp:** DB. Cơ chế migration không tạo duplicate object/data khi deploy lại theo quy trình dự án.

## 3. Staff context, quyền và chi nhánh

### AUTH-001 — Thủ thư nhận đúng context

**Lớp:** API/FE. Login thủ thư; context trả đúng account/staff/role/default branch/allowed branches; sidebar/header hiển thị đúng.

### AUTH-002 — Reader không nhận staff context

**Lớp:** SEC/API. Reader gọi staff context bị 403 hoặc response không lộ staff/branch permission.

### AUTH-003 — Tài khoản khóa

**Lớp:** SEC. Account/staff không hoạt động không được tạo giao dịch dù token cũ còn hiệu lực theo chính sách token.

### AUTH-004 — Giả mạo actor

**Lớp:** SEC/SVC. Gửi `maNhanVienLap/Nhan/Thu` của người khác; backend bỏ qua hoặc từ chối, audit/phiếu luôn ghi principal.

### AUTH-005 — Sai chi nhánh

**Lớp:** SEC/SVC. Thủ thư CN-A gửi branch CN-B hoặc thao tác cuốn CN-B; backend 403/409 phù hợp, không thay đổi dữ liệu.

### AUTH-006 — Admin operational identity

**Lớp:** API/UAT. `BLOCKED BY DECISION`: xác nhận hành vi đã chốt khi admin không có staff profile.

## 4. DataTable và nền tảng UI

### TABLE-001 — Tên dài

**Lớp:** FE/UAT. Tên đầu sách/họ tên dài xuống dòng 2–3 dòng, căn trái; có cách xem toàn bộ; không che cột action.

### TABLE-002 — Local mode

**Lớp:** FE. Bảng chi tiết phiếu nhận mảng nhỏ, phân trang/render đúng và không phát request server không cần thiết.

### TABLE-003 — Server pagination

**Lớp:** FE/API. Danh sách 2.438 mục chỉ tải trang 20; footer `1–20 / 2.438`; trang sau trả đúng 20 mục.

### TABLE-004 — Page size và go-to-page

**Lớp:** FE. 20/50/100 hoạt động; đổi size về trang 1; nhập 0, âm, chữ hoặc vượt tổng được validate/clamp theo contract.

### TABLE-005 — URL state

**Lớp:** FE/E2E. Search/filter/sort/page tồn tại trong URL; reload, back, forward giữ trạng thái.

### TABLE-006 — Stable sort

**Lớp:** API/DB. Nhiều record trùng giá trị sort không nhảy/mất/trùng giữa trang; ID làm tie-breaker.

### TABLE-007 — Stale request

**Lớp:** FE. Gõ nhanh nhiều query; response cũ về sau không ghi đè query mới.

### TABLE-008 — Selection mode

**Lớp:** FE/UAT. Mặc định không có checkbox; bật chọn mới xuất hiện; thoát mode xóa/giữ selection theo contract rõ.

### TABLE-009 — Header selection

**Lớp:** FE. Checkbox header có none/indeterminate/all; chỉ chọn trang hiện tại.

### TABLE-010 — All matching

**Lớp:** FE/API. Sau chọn cả trang có action chọn toàn bộ kết quả; payload dùng filter + excluded IDs, không tải mọi ID.

### TABLE-011 — Query change resets selection

**Lớp:** FE. Đổi filter/search sau selection xóa selection và thông báo; không bulk nhầm query cũ.

### TABLE-012 — Accessibility

**Lớp:** FE. Tab/Enter/Space/Escape, aria-label, focus ring, sort announcement và checkbox header dùng được bằng bàn phím.

## 5. AsyncEntityPicker

### PICK-001 — Không tải toàn bộ entity

**Lớp:** FE/API. Mở picker không tải hàng nghìn bản ghi; gõ ≥ ngưỡng mới search giới hạn kết quả.

### PICK-002 — Exact scan

**Lớp:** FE/API. Quét mã/barcode chính xác + Enter chọn đúng entity mà không cần dropdown nhiều kết quả.

### PICK-003 — Vietnamese search

**Lớp:** API/DB. `nguyen nhat anh` tìm được `Nguyễn Nhật Ánh` theo khả năng/collation đã chốt.

### PICK-004 — Multi-select

**Lớp:** FE. Chọn nhiều tác giả/thể loại, không trùng, xóa chip, keyboard navigation đúng.

### PICK-005 — Error/empty

**Lớp:** FE. Network error có retry; không kết quả có thông điệp/action phù hợp; không mất selection cũ ngoài ý muốn.

## 6. Đầu sách

### BOOK-001 — Paged search/filter/sort

**Lớp:** FE/API. Tìm mã/tên/ISBN/tác giả; filter status/category/author/publisher/year; server trả total đúng.

### BOOK-002 — Form không có dữ liệu mẫu

**Lớp:** FE. Form mới trống ngoài default nghiệp vụ hợp lệ (ngôn ngữ/năm nếu chốt); không `Sách test`, mã TG/TL/NXB hard-code.

### BOOK-003 — Picker danh mục

**Lớp:** FE/API. Tác giả/thể loại/NXB chọn bằng tên, payload gửi IDs; không cần nhập comma codes.

### BOOK-004 — ISBN validation

**Lớp:** FE/API/SVC. ISBN checksum sai báo field error; ISBN trùng trả 409 và existing record; không tạo duplicate.

### BOOK-005 — Duplicate without ISBN

**Lớp:** UAT/API. Tên/tác giả/NXB/năm gần giống tạo warning theo chính sách; override nếu có lưu lý do.

### BOOK-006 — Drawer state preservation

**Lớp:** FE. Mở/đóng drawer không mất page/filter/sort; deep link nếu contract áp dụng.

### BOOK-007 — Deactivate

**Lớp:** SVC/DB/UAT. Lý do bắt buộc; đầu sách ẩn khỏi reader catalog/đặt mới; copies/active loans/history không bị đổi/xóa.

### BOOK-008 — Reactivate

**Lớp:** SVC. Khôi phục có lý do, audit, copies giữ nguyên trạng thái.

### BOOK-009 — Hard delete guard

**Lớp:** SEC/SVC/DB. Librarian bị 403; admin cũng bị chặn khi có bất kỳ liên kết; record không bị xóa phần nào.

## 7. Cuốn sách

### COPY-001 — Filters/presets

**Lớp:** FE/API. Status/branch/date/title/location/barcode filters kết hợp AND giữa nhóm, OR trong nhóm; preset giữ filter không liên quan.

### COPY-002 — Branch-scoped view

**Lớp:** SEC/API. Thủ thư không thể query copies ngoài allowed branches bằng sửa URL/request.

### COPY-003 — Dependent location

**Lớp:** FE/API/SVC. Chọn branch chỉ thấy locations thuộc branch; đổi branch xóa selection cũ; backend chặn location sai branch.

### COPY-004 — Batch auto IDs

**Lớp:** SVC/DB. Tạo 10 copies tạo đủ 10 ID/barcode duy nhất; status Sẵn có; một lỗi rollback toàn lô theo contract.

### COPY-005 — Concurrent batch

**Lớp:** DB/SVC. Hai request đồng thời không trùng ID/barcode và không bỏ qua unique validation.

### COPY-006 — Manual barcode duplicate

**Lớp:** FE/API/DB. Barcode trùng trong lô hoặc database bị báo đúng dòng; không tạo lô phần còn lại nếu atomic.

### COPY-007 — Operational state tampering

**Lớp:** SEC/API. PUT generic cố đặt Đang mượn/Đang đặt trước bị từ chối.

### COPY-008 — Condition transitions

**Lớp:** SVC. Sẵn có → Hỏng/Mất/Ngừng lưu thông cần lý do; Đang mượn bị chặn; restore chỉ từ state hợp lệ; audit before/after.

### COPY-009 — Move location

**Lớp:** SVC/DB. Lưu location cũ/mới, actor/reason; sai branch/không hoạt động bị chặn.

### COPY-010 — QR/privacy

**Lớp:** SEC. QR không chứa reader, current loan, token, status mutable hoặc PII.

## 8. Độc giả và mật khẩu

### READER-001 — Name layout/search

**Lớp:** FE/API. Tên dài hiển thị đủ; search mã/tên/email/phone; sort họ tên theo quy tắc đã chốt.

### READER-002 — Filter expiry

**Lớp:** API. Sắp hết hạn 30 ngày chỉ gồm today…today+30, không gồm đã hết hạn; thẻ và gói tách riêng.

### READER-003 — Derived expiration

**Lớp:** SVC. Thẻ ngày hôm qua = expired; không có endpoint chỉnh tay sang Hết hạn; validation không phụ thuộc job.

### READER-004 — Lock scopes

**Lớp:** SVC/SEC. Borrow lock chặn mượn nhưng vẫn cho trả/thu; login lock chặn login theo policy; lý do/thời hạn/actor lưu đúng.

### READER-005 — Unlock does not renew

**Lớp:** SVC. Mở khóa độc giả có thẻ hết hạn vẫn không eligible; reason audit.

### READER-006 — Deactivate obligations

**Lớp:** SVC/UAT. Độc giả còn mượn/nợ/reservation được chặn hoặc đưa chờ đóng đúng quyết định; không xóa lịch sử.

### READER-007 — Reactivate re-evaluation

**Lớp:** SVC. Reactivate trả warnings/effective state, không đặt mù quáng thành eligible.

### READER-008 — Profile update DTO

**Lớp:** API/SEC. Update profile không yêu cầu/nhận password/account/status fields; mass assignment bị chặn.

### PASS-001 — Reset permission

**Lớp:** SEC. Admin/librarian được hoặc bị chặn đúng scope đã chốt; reader không reset người khác; reason bắt buộc.

### PASS-002 — Temporary password

**Lớp:** API/SEC. Secret đủ mạnh, chỉ response một lần, không xuất hiện trong log/database plaintext; force-change true.

### PASS-003 — Force change

**Lớp:** E2E. Login bằng temporary password chỉ vào change-password; đổi xong mới dùng chức năng khác.

### PASS-004 — Token revocation

**Lớp:** SEC. Token phát hành trước reset bị từ chối khi revokeSessions=true; token mới hoạt động.

### PASS-005 — Rate/audit

**Lớp:** SEC. Reset liên tục bị giới hạn theo policy; audit có actor/reason nhưng không secret/hash/token.

## 9. Mượn sách

### LOAN-001 — Card scan/reader search

**Lớp:** FE/API/UAT. Scan exact chọn reader; async search không tải all; focus chuyển sang copy input.

### LOAN-002 — Borrowing context

**Lớp:** SVC/FE. Hiển thị card/membership/quota/overdue/debt; warning/block rõ code/message.

### LOAN-003 — Block conditions

**Lớp:** SVC. Hồ sơ khóa, thẻ hết hạn, quota đầy và các rule đã chốt chặn; database không đổi.

### LOAN-004 — Copy scan

**Lớp:** FE/API. Barcode đúng branch/Sẵn có thêm giỏ; duplicate không thêm; sai branch/status/reservation hiển thị lý do.

### LOAN-005 — Per-item due date

**Lớp:** SVC. Hai thể loại/rule khác nhau có hạn trả khác; client-supplied due date bị bỏ qua/từ chối.

### LOAN-006 — Quota after cart

**Lớp:** FE/SVC. `current + cart` vượt max bị chặn; xóa item cập nhật preview đúng.

### LOAN-007 — Actor spoof

**Lớp:** SEC. Payload mã nhân viên khác không ảnh hưởng phiếu/audit.

### LOAN-008 — Concurrent borrow

**Lớp:** SVC/DB. Hai request cùng copy: đúng một thành công; request kia conflict; không tạo phiếu rỗng/partial.

### LOAN-009 — Atomic multi-copy

**Lớp:** SVC/DB. Một trong 3 copies không hợp lệ thì không copy nào chuyển Đang mượn và không có phiếu/chi tiết orphan.

### LOAN-010 — Idempotency

**Lớp:** API/SVC. Cùng key/payload trả cùng loan; cùng key/payload khác 409; không double-loan.

### LOAN-011 — Reservation handoff

**Lớp:** SVC. `BLOCKED BY DECISION`: đúng reader có thể nhận hold, sai reader bị chặn, reservation chuyển đúng state.

### LOAN-012 — Result/reset

**Lớp:** FE/UAT. Result có ID/item due dates/actor/branch; print payload đúng; new transaction xóa reader/cart và focus thẻ.

## 10. Trả sách

### RETURN-001 — Barcode open-loan lookup

**Lớp:** API/FE. Scan copy đang mượn trả reader/title/borrow/due/branch; auto-select reader.

### RETURN-002 — Already returned/not borrowed

**Lớp:** SVC/FE. Cuốn Sẵn có/đã trả báo rõ phiếu/thời gian nếu có; không thêm giỏ.

### RETURN-003 — One reader per cart

**Lớp:** FE/SVC. Quét copy của reader khác bị chặn; không tự đổi reader hoặc trộn payload.

### RETURN-004 — Current loans selector

**Lớp:** FE. Checkbox thêm/xóa cart đồng bộ scan; quá hạn tô đỏ và hiện số ngày; không còn global borrowers table.

### RETURN-005 — Late fine server time

**Lớp:** SVC. Client sửa số ngày/tiền không ảnh hưởng; backend tính theo rule/server date; boundary due date đúng policy.

### RETURN-006 — Normal condition

**Lớp:** SVC. Không damage fine; loan detail đóng; copy Sẵn có hoặc hold theo reservation rule.

### RETURN-007 — Damaged/lost

**Lớp:** SVC/FE. Thiếu severity/description theo policy bị field error; backend đề xuất fine; copy Hỏng/Mất; debt nguồn đúng.

### RETURN-008 — Fine adjustment authorization

**Lớp:** SEC/SVC. Không quyền không chỉnh được; có quyền phải reason/approval; audit lưu proposed/final.

### RETURN-009 — Cross branch

**Lớp:** SVC. Theo quyết định: bị chặn rõ hoặc tạo workflow in-transit; không đặt Sẵn có sai branch.

### RETURN-010 — Atomic batch

**Lớp:** SVC/DB. Một detail đã trả làm toàn transaction rollback; không debt/copy/loan partial.

### RETURN-011 — Idempotency/concurrency

**Lớp:** SVC/DB. Hai quầy trả cùng detail: một thành công; request lặp trả cùng result.

### RETURN-012 — Loan header status

**Lớp:** SVC. Trả một phần giữ phiếu Đang mượn; xử lý hết mới Đã trả hết.

### RETURN-013 — Debt creation

**Lớp:** SVC/DB. Late/damage debts có type/source/amount/status đúng; response trả debt IDs để chuyển payment.

## 11. Thu tiền

### PAY-001 — Debtor search/context

**Lớp:** FE/API. Search outstanding readers, không tải all; chọn reader hiển thị total/count/oldest/borrowing impact.

### PAY-002 — Paid debts hidden

**Lớp:** FE/API. Default chỉ unpaid/partial; paid chỉ xem lịch sử, không checkbox/allocation.

### PAY-003 — Auto allocation order

**Lớp:** SVC/FE. Amount phân bổ oldest-first ổn định; UI preview đúng order/before/after.

### PAY-004 — Manual partial allocation

**Lớp:** FE/SVC. Chọn 2 debts, nhập partial; total tự bằng sum; bỏ chọn xóa allocation; không có nút sync tổng.

### PAY-005 — Invalid amount

**Lớp:** FE/API/SVC. Zero/negative/greater-than-remaining/fraction invalid bị chặn; database không đổi.

### PAY-006 — Payment methods

**Lớp:** FE/API. Danh sách từ DB, chỉ active; không hard-code; method fields/validation đúng.

### PAY-007 — Actor/payment ID

**Lớp:** SEC. Client không chọn actor/receipt ID; server ID duy nhất; audit đúng principal/branch.

### PAY-008 — Concurrent collection

**Lớp:** SVC/DB. Hai quầy thu cùng debt: không `paid > incurred`; request sau nhận conflict/new remaining.

### PAY-009 — Idempotency

**Lớp:** API/SVC. Double click/network retry không tạo hai receipts hoặc trừ hai lần.

### PAY-010 — External reference unique

**Lớp:** DB/SVC. Hai receipt dùng cùng transfer reference bị chặn; retry idempotent vẫn trả receipt cũ.

### PAY-011 — Receipt immutability

**Lớp:** SEC/API. Successful receipt không update/delete qua API/UI.

### PAY-012 — Reversal

**Lớp:** SVC/DB/SEC. Đúng quyền/reason/approval; allocations hoàn lại đúng; receipt gốc giữ; double reversal bị chặn.

### PAY-013 — Cash handling

**Lớp:** FE/SVC. Nếu triển khai, cash received >= total, change đúng; debt chỉ áp dụng total, không cash received.

### PAY-014 — Electronic pending

**Lớp:** SVC. `BLOCKED BY DECISION`: pending không giảm debt; success mới giảm; failed không thay đổi.

## 12. Barcode, bulk, export và RFID

### CODE-001 — Keyboard scanner

**Lớp:** UAT/FE. Scanner nhập nhanh + Enter; một request exact; focus giữ; success/error feedback khác nhau.

### CODE-002 — Label stability

**Lớp:** UAT. Chuyển vị trí/chi nhánh không đổi barcode/copy ID; label scan vẫn tra đúng.

### BULK-001 — Selected IDs

**Lớp:** API/SVC. Bulk action đúng selected IDs; validation từng record; result count/errors đúng.

### BULK-002 — All matching/exclusions

**Lớp:** API/SEC. Backend rebuild filter, áp dụng exclusions, branch/permission; client không thao tác record ngoài query/quyền.

### BULK-003 — Partial result visibility

**Lớp:** FE/UAT. Nếu bulk contract cho partial, UI hiển thị rõ success/fail và IDs/reasons; không báo success chung sai.

### EXPORT-001 — Scope

**Lớp:** FE/API. Phân biệt current page/selected/all matching; file phản ánh đúng filter.

### EXPORT-002 — PII authorization

**Lớp:** SEC. Thủ thư/admin chỉ xuất cột và branch được phép; reader PII không rò rỉ.

### RFID-001 — Hardware gate

**Lớp:** Process. Không đánh dấu RFID hoàn tất khi chưa có device/standard/protocol acceptance riêng.

## 13. Audit

### AUDIT-001 — Coverage

**Lớp:** SVC/DB. Mỗi action bắt buộc trong business rules tạo đúng một audit event hoặc event theo transaction design.

### AUDIT-002 — Actor/source/before-after

**Lớp:** DB. Actor/role/branch/server time/entity/source/reason/before-after đúng và có correlation.

### AUDIT-003 — Secret exclusion

**Lớp:** SEC. Password/hash/reset token/full payment credential không xuất hiện trong app log, audit, error hoặc response ngoài one-time secret.

### AUDIT-004 — Immutability/access

**Lớp:** SEC. Librarian không update/delete audit; quyền xem đúng scope; admin action xem/xuất được audit theo policy.

## 14. Hiệu năng và độ tin cậy

### PERF-001 — Không còn findAll cho màn hình lớn

**Lớp:** API. Books/copies/readers list request chỉ lấy page; memory/response không tăng tuyến tính toàn bảng.

### PERF-002 — N+1

**Lớp:** DB. Page 20 records không tạo query riêng cho từng author/category/branch/location; đo query count.

### PERF-003 — Index/query plan

**Lớp:** DB. Query phổ biến branch+status, title+status+date, reader expiry, debt date dùng index phù hợp; không table scan bất hợp lý ở dữ liệu mục tiêu.

### PERF-004 — Lock scope

**Lớp:** SVC/DB. Transaction lock chỉ record cần thiết, không giữ qua network/external call; không deadlock phổ biến trong load test.

### REL-001 — Network ambiguity

**Lớp:** E2E. Mất response sau server commit; retry với idempotency trả kết quả cũ, UI không yêu cầu thu/mượn/trả lần nữa.

## 15. Chuỗi E2E bắt buộc

### E2E-001 — Catalog đến payment

1. Admin tạo đầu sách không trùng.
2. Thủ thư nhập 3 cuốn tại allowed branch.
3. Tạo độc giả/thẻ/gói hợp lệ.
4. Mượn 2 cuốn bằng scan.
5. Trả 1 cuốn bình thường quá hạn.
6. Trả 1 cuốn hỏng.
7. Hai khoản nợ được tạo đúng.
8. Thu tự động một phần.
9. Thu manual phần còn lại.
10. Dư nợ 0, phiếu/chi tiết/audit nhất quán.

### E2E-002 — Quyền chi nhánh

Lặp chuỗi với copy branch khác; mọi list/search/action bị giới hạn đúng, không IDOR qua ID trực tiếp.

### E2E-003 — Concurrent circulation

Hai session mượn cùng copy, trả cùng detail và thu cùng debt; invariant database giữ đúng.

### E2E-004 — Password reset

Reset → token cũ bị thu hồi → temporary password login → buộc đổi → token mới dùng được → audit không secret.

## 16. Definition of Done nghiệm thu

Một task chỉ pass khi:

- Automated tests liên quan pass.
- Frontend lint/build pass.
- Migration được kiểm tra trên dữ liệu fixture.
- Acceptance cases của task pass hoặc được đánh dấu `BLOCKED BY DECISION` có lý do.
- Không còn Critical/High security finding chưa xử lý.
- Tài liệu requirements/business/API/roadmap được cập nhật nếu implementation khác contract.

# Roadmap hiện đại hóa khu vực quản trị

## 1. Cách sử dụng roadmap

Roadmap này là thứ tự triển khai, không phải thứ tự các chương trong tài liệu yêu cầu. Mỗi task phải được triển khai, test, review và tạo checkpoint trước khi chuyển tiếp. `P0` là release gate: tính năng không được phát hành nếu P0 liên quan chưa hoàn tất; P0/P1 có thể được phát triển xen kẽ khi không phụ thuộc nhau.

Ký hiệu:

- **P0**: toàn vẹn nghiệp vụ, bảo mật hoặc dữ liệu.
- **P1**: chức năng cốt lõi và UX vận hành.
- **P2**: vận hành nâng cao/tối ưu.
- **S/M/L**: ước lượng tương đối, không phải số ngày.

## 2. Dependency tổng quát

```text
Quyết định nghiệp vụ
  └─ F0 Staff context + branch authorization
       ├─ Transaction attribution (loan/return/payment)
       ├─ Reader security/status
       └─ API search/pagination foundation
            ├─ DataTable v2 → Books → Copies → Readers
            └─ AsyncEntityPicker → Copies → Readers → Loans → Returns → Payments

Copies + Readers context → Loans
Loans/open-loan lookup → Returns
Returns/debt generation → Payments
Core flows stable → Bulk/Barcode printing/RFID/Export/Reconciliation
```

## 3. Milestone M0 — Quyết định và baseline

### M0-T01 — Chốt OPEN DECISION cốt lõi (P0, M)

**Dependency:** không.

**Phạm vi:** chốt branch scope, admin operational identity, cross-branch return, ID format, borrow blockers, fine formula, reservation handoff, password reset scope, electronic payment và reversal.

**Deliverable:** ADR hoặc cập nhật `01-business-rules.md`; không code khi quyết định ảnh hưởng schema còn mở.

### M0-T02 — Baseline test và fixture (P0, M)

**Dependency:** M0-T01 phần liên quan.

**Phạm vi:** ghi nhận test hiện có, build/lint baseline, tạo test fixture riêng cho admin/librarian/reader/branches/books/copies/debts; không dùng production data.

**Definition of Done:** biết rõ test nào pass/fail trước thay đổi; fixture tái lập được.

## 4. Milestone M1 — Danh tính, quyền và audit nền

### M1-T01 — Authenticated staff context (P0, M)

**Dependency:** M0-T01 admin identity/branch scope.

**Backend:** mở rộng principal/auth response hoặc `/api/staff/me/context`; trả staff, role, default/allowed branches; xử lý reader/admin rõ.

**Frontend:** lưu staff context trong AuthContext; chưa thay giao diện lớn.

**Database:** migration quan hệ allowed branches nếu quyết định nhiều-nhiều.

**Tests:** librarian/admin/reader/locked account/no branch.

### M1-T02 — Branch authorization service (P0, M)

**Dependency:** M1-T01.

Tạo cơ chế dùng chung kiểm tra view/operate branch; không lặp logic tùy tiện ở từng service.

### M1-T03 — Audit event nền (P0, M)

**Dependency:** M1-T01.

Chuẩn hóa actor, branch, action code, entity, before/after, reason, source/correlation; không ghi secret.

### M1-T04 — Principal-based loan attribution (P0, S)

**Dependency:** M1-T01, M1-T02, M1-T03.

Backend bỏ qua/loại `maNhanVienLap` client, kiểm tra branch, test spoofing; giữ row lock hiện có.

### M1-T05 — Principal-based return attribution (P0, S)

**Dependency:** M1-T01, M1-T02, M1-T03.

Backend bỏ qua/loại `maNhanVienNhan`, kiểm tra branch và trả trùng.

### M1-T06 — Principal-based payment attribution (P0, S)

**Dependency:** M1-T01, M1-T02, M1-T03.

Backend bỏ qua/loại `maNhanVienThu`, chuẩn bị transaction lock/idempotency nền.

## 5. Milestone M2 — Nền tảng dữ liệu và component dùng chung

### M2-T01 — Page/search/sort contract (P1, M)

**Dependency:** M0-T02.

Chuẩn hóa DTO phân trang, validation page size, allowlist sort, filter kiểu; pilot endpoint books và giữ compatibility khi cần.

### M2-T02 — DataTable v2 core (P1, L)

**Dependency:** M2-T01.

Local/server mode, column layout/wrap, pagination, URL query, loading/error/empty, stale request protection.

### M2-T03 — DataTable selection (P1, M)

**Dependency:** M2-T02.

Selection mode, header indeterminate, select page/all matching descriptor, excluded IDs, bulk action bar, reset on query change.

### M2-T04 — AsyncEntityPicker core (P1, M)

**Dependency:** search API convention trong M2-T01.

Single/multi, debounce, abort, keyboard, exact match, metadata, reusable adapters.

### M2-T05 — Drawer/filter primitives (P1, M)

**Dependency:** M2-T02.

Drawer giữ URL/query state, filter panel, active chips, date range, permission-aware actions.

## 6. Milestone M3 — Đầu sách làm pilot

### M3-T01 — Books paged list (P1, M)

**Dependency:** M2-T01, M2-T02.

Server pagination/search/sort/filter; tên wrap; count copy summary; URL state.

### M3-T02 — Books selection và column settings (P1, S)

**Dependency:** M2-T03, M3-T01.

Selection chỉ khi bật; column visibility cơ bản; chưa cần bulk mutation backend.

### M3-T03 — Catalog entity search endpoints (P1, M)

**Dependency:** M2-T04.

Search authors/categories/publishers/titles; permission/active filtering; response option có metadata.

### M3-T04 — Book create/edit form (P1, L)

**Dependency:** M3-T03, quyết định ID/ISBN.

Split DTO, picker tác giả/thể loại/NXB, ISBN validation/duplicate, blank production defaults, field errors, unsaved guard.

### M3-T05 — Book drawer (P1, M)

**Dependency:** M3-T01.

Tổng quan, copies by branch, history; navigation giữ list state.

### M3-T06 — Book visibility lifecycle (P0/P1, M)

**Dependency:** M1-T03, M3-T05, quyết định reservation effect.

Deactivate/restore có lý do; hard-delete preflight và admin-only; audit.

## 7. Milestone M4 — Cuốn sách

### M4-T01 — Copies paged list và facets (P1, L)

**Dependency:** M2 platform, M3-T03.

Filter trạng thái/branch/date/title/location/barcode; presets; labels thay mã; branch scope; index review.

### M4-T02 — Branch-dependent location API (P1, M)

**Dependency:** M1-T02.

Option có branch/area/shelf/location metadata; validation membership/capacity nếu có.

### M4-T03 — Server ID/barcode allocation (P0, M)

**Dependency:** quyết định ID format.

Sequence/unique constraint/concurrency tests; QR identifier policy.

### M4-T04 — Batch create copies (P1, L)

**Dependency:** M3-T03, M4-T02, M4-T03.

Wizard/picker/quantity/barcode modes/preview; API batch atomic; Sẵn có server-enforced.

### M4-T05 — Copy detail/history drawer (P1, M)

**Dependency:** M4-T01, M1-T03.

Metadata, current state, history location/state/circulation.

### M4-T06 — Copy transition actions (P0/P1, L)

**Dependency:** M1-T03, M4-T05, state decisions.

Move location/branch, damage/lost/withdraw/restore; state machine; reason/audit; block operational transitions.

## 8. Milestone M5 — Độc giả

### M5-T01 — Readers paged list (P1, L)

**Dependency:** M2 platform.

Search/filter/sort/presets; full-name layout; effective card/membership/profile/account states; summary.

### M5-T02 — Reader drawer (P1, L)

**Dependency:** M5-T01.

Profile, membership, loans, debts, history, security tabs; protect PII.

### M5-T03 — Split reader DTOs (P0, M)

**Dependency:** M5-T02 design.

Create/profile/card/membership/status/password requests; remove fake password from update.

### M5-T04 — Reader state transitions (P0, L)

**Dependency:** M1-T03, M5-T03, reader decisions.

Derived expiry; scoped lock/unlock/deactivate/reactivate; reasons/times; restore re-evaluation.

### M5-T05 — Borrowing context (P0/P1, L)

**Dependency:** M5-T04, borrow rule decisions.

Eligibility, warnings/blocking reasons, quota, overdue, debt, reservation summary; tests rule combinations.

### M5-T06 — Secure password reset (P0, L)

**Dependency:** M1-T01, M5-T03, password decision.

Temporary/reset flow, force-change flag, token revocation, permissions/rate limit/audit; no secret logging.

## 9. Milestone M6 — Mượn sách

### M6-T01 — Reader search/select UI (P1, M)

**Dependency:** M2-T04, M5-T05, M1 context.

Card scan/async search, summary card, warning/block, collapsed current loans.

### M6-T02 — Borrowable copy lookup (P1, M)

**Dependency:** M2-T04, M4 list/state, reservation decision.

Barcode exact lookup/title search scoped branch; copy eligibility metadata.

### M6-T03 — Loan cart và preview (P1, L)

**Dependency:** M6-T01, M6-T02.

Duplicate/quota checks, item rule/due preview, warnings, keyboard flow; bỏ available-all table.

### M6-T04 — Atomic create loan (P0, L)

**Dependency:** M1-T04, M4 state, M6-T03, ID/idempotency.

Server ID, principal actor, revalidate/lock/all-or-nothing, reservation handoff, audit.

### M6-T05 — Loan result/print payload (P1, M)

**Dependency:** M6-T04.

Success summary, print/email data, reset/focus new transaction; basic barcode keyboard workflow included.

## 10. Milestone M7 — Trả sách

### M7-T01 — Open-loan lookup (P1, M)

**Dependency:** M6 data, M2-T04.

Lookup barcode/copy/loan-detail; enriched reader/book/due/branch/fine preview context.

### M7-T02 — Reader open-loans selector và return cart (P1, L)

**Dependency:** M7-T01.

Auto reader from first scan, one reader/cart, checkbox current loans, batch cart, remove global borrower table.

### M7-T03 — Condition/fine preview (P0/P1, L)

**Dependency:** fine decisions, M7-T02.

Normal/damaged/lost assessment, backend late/damage computation, adjustment authorization/audit.

### M7-T04 — Atomic batch return (P0, L)

**Dependency:** M1-T05, M7-T03, reservation handoff decision.

Server ID, revalidate/lock, all-or-nothing/idempotency, update loan/copy, create debts, reservation hold.

### M7-T05 — Return result/print/payment handoff (P1, M)

**Dependency:** M7-T04.

Result summary, debt IDs, hold label, print payload, preselect debts on payment navigation.

## 11. Milestone M8 — Thu tiền

### M8-T01 — Debtor search và debt context (P1, M)

**Dependency:** M2-T04, debt data.

Async outstanding-only search, summary, debt list filters/source links; hide paid default.

### M8-T02 — Auto/manual payment UI (P1, L)

**Dependency:** M8-T01, payment methods API.

Tabs, allocation edit, derived total, sticky summary, method-specific fields; bỏ hard-code và sync button.

### M8-T03 — Payment preview (P0/P1, M)

**Dependency:** M8-T02.

Server auto/manual preview, stable ordering, remaining before/after, validation.

### M8-T04 — Atomic/idempotent payment (P0, L)

**Dependency:** M1-T06, M8-T03, ID/idempotency.

Lock debts, server actor/ID, unique external ref, update statuses, audit, payment state.

### M8-T05 — Receipt result/print (P1, M)

**Dependency:** M8-T04.

Receipt allocations/balance/cash change/reference; print/email payload.

### M8-T06 — Payment reversal (P0, L)

**Dependency:** reversal decision, M8-T04.

Immutable success receipt; reversal transaction restores debts, permission/approval/audit, no double reversal.

## 12. Milestone M9 — Vận hành nâng cao

### M9-T01 — Generic bulk action contract (P2, L)

**Dependency:** M2-T03, state/permission services.

Selected IDs/all matching/excluded IDs; result report; triển khai lần lượt books/copies/readers.

### M9-T02 — Barcode label printing (P2, M)

**Dependency:** M4-T03/M4-T04.

Template, preview, one/batch, stable identifier, printer testing.

### M9-T03 — Export (P2, M/L)

**Dependency:** paged/filter contracts, permission policy.

Selected/page/all matching; server-side export; async job cho tập lớn; PII controls.

### M9-T04 — Cash shift/reconciliation (P2, L)

**Dependency:** M8 stable, electronic/cash decisions.

Ca/quỹ, đối soát phương thức, discrepancy workflow.

### M9-T05 — RFID discovery/implementation (P2, XL)

**Dependency:** RFID hardware OPEN DECISION.

Tách project: device proof-of-concept, tag model, reader/writer API, inventory/security integration.

## 13. Milestone M10 — Quality gates

### M10-T01 — Security review (P0, L)

Spoof actor/branch, IDOR, password/token, status/fine/money tampering, injection, PII, audit.

### M10-T02 — Concurrency/idempotency suite (P0, L)

Đồng thời mượn cùng cuốn, trả trùng, thu cùng debt, duplicate request và transaction rollback.

### M10-T03 — Performance review (P1, M)

N+1, query/index/count/search, page size, lock scope, export/bulk.

### M10-T04 — End-to-end/UAT (P0/P1, L)

Chuỗi catalog → inventory → reader → loan → return → debt → payment → reversal; quyền/branch và lỗi mạng.

## 14. Release gate

Một milestone chỉ được xem là hoàn tất khi:

1. Migration và rollback/compatibility được mô tả.
2. Backend tests, frontend lint/build và test liên quan pass.
3. Security P0 của tính năng hoàn tất.
4. Acceptance tests tương ứng trong `04-acceptance-tests.md` pass.
5. Tài liệu API và OPEN DECISION được cập nhật.
6. Không còn hard-code production hoặc dữ liệu test mặc định.

# Backlog triển khai hiện đại hóa khu vực quản trị

## 1. Mục đích và cách dùng

Tài liệu này là backlog triển khai có dependency, thay cho roadmap cấp cao trước đây. Mỗi task là một đơn vị có thể phát triển, review, migration và kiểm thử độc lập. Một task chỉ được chuyển sang `Done` khi toàn bộ test và acceptance criteria của chính task đạt.

Quy ước:

- **P0:** toàn vẹn nghiệp vụ, bảo mật, tiền hoặc dữ liệu; là release gate.
- **P1:** chức năng cốt lõi và UX vận hành.
- **P2:** vận hành nâng cao.
- **S/M/L:** ước lượng tương đối, không phải số ngày.
- “File dự kiến ảnh hưởng” là phạm vi dự báo để lập kế hoạch, không phải quyền sửa tất cả file trong danh sách.
- Task migration phải có forward migration, kiểm tra dữ liệu hiện hữu và phương án rollback/roll-forward.

Không gom nhiều màn hình vào một task. Component hoặc hạ tầng dùng chung được phép là một task riêng nếu không đồng thời migrate các màn hình tiêu thụ nó.

## 2. Baseline quyết định đã chốt

Backlog này coi các quyết định sau là invariant, không còn là `OPEN DECISION`:

1. Thủ thư có một chi nhánh mặc định và nhiều chi nhánh được phép; ban đầu seed mỗi thủ thư một chi nhánh.
2. Release hiện tại chỉ cho trả tại chi nhánh mượn.
3. Định danh hai lớp: UUID/ULID kỹ thuật và mã nghiệp vụ/barcode dễ đọc do server sinh bằng sequence/counter.
4. Điều kiện mượn dùng rule cấu hình, phân biệt blocker/warning/override có quyền và audit.
5. Trạng thái độc giả tách account, lifecycle, lock và hiệu lực thẻ/gói suy ra.
6. Trạng thái cuốn tách lưu thông, tình trạng vật lý và điều chuyển; operational state chỉ do service nghiệp vụ đổi.
7. Sau trả bình thường, reservation hợp lệ lâu nhất tại cùng chi nhánh được giữ theo FIFO.
8. Phạt hỏng/mất do backend tính theo rule cấu hình; điều chỉnh cần quyền, lý do và phê duyệt theo ngưỡng.
9. Admin và thủ thư có permission riêng được reset mật khẩu độc giả sau xác minh; bắt buộc đổi mật khẩu và thu hồi phiên cũ.
10. Phiếu thu thành công bất biến; thủ thư tạo yêu cầu đảo và quản lý/admin khác người phê duyệt theo maker-checker.

Các tham số chưa có giá trị cụ thể như ngưỡng nợ, mức phạt, thời gian giữ chỗ và ngưỡng cần phê duyệt phải được lưu dưới dạng cấu hình/version; không được hard-code để lấp chỗ trống.

## 3. Decision gate còn mở

Các nội dung sau chưa được file quyết định chốt. Task phụ thuộc phải dừng ở contract/test fixture, không tự suy diễn hành vi production:

| Gate | Nội dung cần chốt | Task bị chặn trực tiếp |
|---|---|---|
| `DG-ADMIN-ACTOR` | Admin không có staff profile có được lập mượn/trả/thu hay không | `AUTH-02`, `LOAN-05`, `RETURN-05`, `PAY-05` |
| `DG-EXPIRY` | Ngày hết hạn còn hiệu lực hết ngày hay hết từ đầu ngày; lịch nghỉ/hạn theo thẻ/gói | `READER-05`, `LOAN-04`, `RETURN-04` |
| `DG-TITLE-RESERVATION` | Xử lý reservation đang mở khi ẩn đầu sách | `BOOK-05` |
| `DG-READER-CLOSE` | Đóng hồ sơ còn nghĩa vụ bị chặn hay chuyển chờ đóng | `READER-04` |
| `DG-DUPLICATE-TITLE` | Quyền override cảnh báo gần trùng khi không có ISBN | `BOOK-03` |
| `DG-COPY-TRANSFER` | Ownership và workflow chuyển cuốn giữa chi nhánh | Không nằm trong release này; task tương lai |
| `DG-ELECTRONIC-PAYMENT` | Thanh toán điện tử đồng bộ hay webhook/pending | `PAY-04`, `PAY-05` cho phương thức điện tử |
| `DG-AUDIT` | Retention, quyền xem, before/after và same-transaction hay outbox | `AUDIT-01` |
| `DG-API-NAMING` | DTO mới dùng tiếng Việt hay tiếng Anh | `API-01` và mọi API mới |
| `DG-OPTIMISTIC-LOCK` | Entity danh mục/hồ sơ dùng version hay ETag | `BOOK-03`, `COPY-06`, `READER-03` |
| `DG-RFID` | Thiết bị, chuẩn tag, SDK, privacy và security gate | `OPS-07` |

## 4. Dependency tổng quát

```text
BASE-01 → BASE-02
BASE-02 → AUTH-01 → AUTH-02 → AUTH-03
AUTH-02 → AUDIT-01
BASE-02 → ID-01 → IDEM-01
API-01 → UI-01 → UI-02/UI-04
API-01 → UI-03

UI/API foundation → BOOK → COPY → LOAN → RETURN → PAYMENT
                     └──────→ READER ────┘

Core flows stable → OPS bulk/labels/export → QA release suite
```

## 5. Epic A — Baseline, migration và contract nền

### BASE-01 — Baseline build và fixture (P0)

- **Mục tiêu:** Có đường chuẩn tái lập để phân biệt lỗi cũ và regression mới.
- **Dependency:** Không.
- **Phạm vi:** Ghi nhận backend test, frontend lint/build; tạo fixture test cho admin, librarian, reader, hai chi nhánh, sách, cuốn, reservation, loan, debt và receipt.
- **Ngoài phạm vi:** Sửa lỗi nghiệp vụ hoặc UI hiện có.
- **File dự kiến ảnh hưởng:** `backend/src/test/**`, `frontend/src/**/__tests__/**` hoặc cấu trúc test được chọn, `database/scripts/*test*`, tài liệu test.
- **API/database migration:** Không đổi API/schema production; fixture chỉ dùng database test.
- **Test bắt buộc:** `BASE-001`; chạy được trên database sạch và snapshot test.
- **Acceptance criteria:** Có lệnh/script tái lập; kết quả pass/fail hiện tại được ghi; không dùng dữ liệu production.
- **Rủi ro:** Fixture không đại diện quan hệ lịch sử và làm test migration sai lệch.
- **Ước lượng tương đối:** M.

### BASE-02 — Cơ chế migration có version (P0)

- **Mục tiêu:** Mọi thay đổi schema sau đó được triển khai theo version, có kiểm tra và rollback/roll-forward.
- **Dependency:** `BASE-01`.
- **Phạm vi:** Chọn Flyway/Liquibase hoặc convention tương đương; baseline schema hiện hữu; quy tắc đặt tên/chạy migration.
- **Ngoài phạm vi:** Thêm field nghiệp vụ của các epic sau.
- **File dự kiến ảnh hưởng:** `backend/pom.xml`, `backend/src/main/resources/application.properties`, thư mục migration mới, `database/README_DATABASE.md`.
- **API/database migration:** Tạo migration ledger/baseline; không đổi dữ liệu nghiệp vụ.
- **Test bắt buộc:** `BASE-002`, `BASE-003`; migrate database sạch và database có dữ liệu demo.
- **Acceptance criteria:** Deploy lặp không tạo object trùng; Hibernate `validate` pass; có hướng dẫn rollback/restore.
- **Rủi ro:** Baseline sai version làm môi trường hiện hữu không khởi động.
- **Ước lượng tương đối:** M.

### API-01 — Page, error và compatibility contract (P1)

- **Mục tiêu:** Chuẩn hóa contract mà các danh sách và picker mới cùng sử dụng.
- **Dependency:** `BASE-01`, `DG-API-NAMING`.
- **Phạm vi:** Page envelope, page size tối đa, allowlist sort, stable tie-break, typed filter, error code/details/correlation và adapter compatibility.
- **Ngoài phạm vi:** Migrate bất kỳ màn hình cụ thể nào.
- **File dự kiến ảnh hưởng:** DTO page/error mới, `GlobalExceptionHandler.java`, API client helpers và `03-api-contracts.md`.
- **API/database migration:** Không migration; thêm contract dùng chung và giữ endpoint cũ trong giai đoạn chuyển tiếp.
- **Test bắt buộc:** page validation, sort không hợp lệ, correlation ID, runtime error không lộ nội dung nội bộ.
- **Acceptance criteria:** Contract có version/compatibility rõ; controller pilot có thể dùng mà không copy logic.
- **Rủi ro:** Đổi error shape làm frontend cũ mất thông báo.
- **Ước lượng tương đối:** M.

### ID-01 — Bộ cấp định danh hai lớp phía server (P0)

- **Mục tiêu:** Loại bỏ ID `Date.now()`/hard-code và cấp ID kỹ thuật cùng mã nghiệp vụ an toàn đồng thời.
- **Dependency:** `BASE-02`.
- **Phạm vi:** UUID/ULID kỹ thuật; sequence/counter cho mã cuốn, phiếu mượn/trả/thu và barcode; API nội bộ cấp mã.
- **Ngoài phạm vi:** Đổi toàn bộ PK hiện hữu trong một lần hoặc in nhãn.
- **File dự kiến ảnh hưởng:** service ID allocator mới, entity/repository liên quan, migration và test concurrency.
- **API/database migration:** Sequence/counter, cột business code nếu PK hiện tại cần giữ tương thích, unique index và backfill có kiểm chứng.
- **Test bắt buộc:** `COPY-004`, `COPY-005`, `PAY-007`; sinh song song không trùng, rollback không tái dùng sai mã.
- **Acceptance criteria:** Client không cần cung cấp ID mới; mã ổn định, không chứa vị trí/trạng thái mutable.
- **Rủi ro:** Chuyển PK trực tiếp gây ảnh hưởng FK lớn; ưu tiên compatibility mapping.
- **Ước lượng tương đối:** L.

### IDEM-01 — Hạ tầng idempotency dùng chung (P0)

- **Mục tiêu:** Cho create loan/return/payment trả lại kết quả cũ khi retry cùng request.
- **Dependency:** `BASE-02`, `ID-01`, `AUTH-02`.
- **Phạm vi:** Key scope theo endpoint+actor, request fingerprint, stored response/status, TTL và conflict khi cùng key khác payload.
- **Ngoài phạm vi:** Tích hợp vào từng endpoint nghiệp vụ; mỗi epic làm riêng.
- **File dự kiến ảnh hưởng:** filter/interceptor/service idempotency mới, repository/entity, security context, migration.
- **API/database migration:** Bảng `IDEMPOTENCY_RECORD` và unique scope; header `Idempotency-Key`.
- **Test bắt buộc:** cùng key/cùng payload, cùng key/khác payload, request song song, lỗi rollback.
- **Acceptance criteria:** Thư viện dùng chung có thể bọc một transaction mà không ghi kết quả trước commit.
- **Rủi ro:** Lưu response nhạy cảm hoặc TTL quá dài; cần redaction/retention.
- **Ước lượng tương đối:** L.

## 6. Epic B — Danh tính, chi nhánh và audit

### AUTH-01 — Migration allowed branches (P0)

- **Mục tiêu:** Biểu diễn một chi nhánh mặc định và nhiều chi nhánh được phép cho mỗi nhân viên.
- **Dependency:** `BASE-02`.
- **Phạm vi:** Quan hệ `NHANVIEN_CHINHANH`, default flag, effective dates, backfill từ `NHANVIEN.MaChiNhanh`.
- **Ngoài phạm vi:** UI đổi chi nhánh hoặc authorization service.
- **File dự kiến ảnh hưởng:** migration, entity/repository nhân viên-chi nhánh, admin librarian DTO/service.
- **API/database migration:** Unique nhân viên+chi nhánh; tối đa một default đang hiệu lực; giữ cột cũ tạm thời để compatibility.
- **Test bắt buộc:** migrate dữ liệu hiện hữu, nhân viên không chi nhánh, duplicate/default conflict.
- **Acceptance criteria:** Mỗi thủ thư hiện hữu có đúng một allowed/default branch sau backfill.
- **Rủi ro:** Nhân viên có `MaChiNhanh NULL`; phải báo cáo ngoại lệ thay vì tự gán.
- **Ước lượng tương đối:** M.

### AUTH-02 — Staff context API (P0)

- **Mục tiêu:** Backend trả account, staff, role, default branch, allowed branches và permissions từ principal/database.
- **Dependency:** `AUTH-01`, `API-01`, `DG-ADMIN-ACTOR`.
- **Phạm vi:** `GET /api/staff/me/context`; re-check trạng thái account/staff; behavior reader/admin rõ.
- **Ngoài phạm vi:** Migrate form mượn/trả/thu.
- **File dự kiến ảnh hưởng:** `AuthUser`, `AuthService`, security filter/service, controller/DTO staff context.
- **API/database migration:** Endpoint mới; dùng schema `AUTH-01`.
- **Test bắt buộc:** `AUTH-001`, `AUTH-002`, `AUTH-003`, `AUTH-006`.
- **Acceptance criteria:** Không tin branch/permission trong client token cũ; account/staff bị khóa không nhận context hợp lệ.
- **Rủi ro:** Re-check DB mỗi request tăng tải; cần cache ngắn nhưng không làm chậm revoke.
- **Ước lượng tương đối:** M.

### AUTH-03 — Branch authorization service (P0)

- **Mục tiêu:** Có một cơ chế backend dùng chung để kiểm tra quyền xem/thao tác theo chi nhánh.
- **Dependency:** `AUTH-02`.
- **Phạm vi:** API/service guard cho view/operate; error 403 ổn định; helper query branch scope.
- **Ngoài phạm vi:** Áp dụng đồng loạt vào mọi màn hình; từng task màn hình tích hợp riêng.
- **File dự kiến ảnh hưởng:** security/authorization package mới, repository specifications/helpers, error codes.
- **API/database migration:** Không migration mới.
- **Test bắt buộc:** `AUTH-005`, IDOR bằng sửa path/query/body, allowed branch hết hiệu lực.
- **Acceptance criteria:** Service có test độc lập; caller không thể truyền một branch rồi bỏ qua kiểm tra.
- **Rủi ro:** Guard chỉ ở controller có thể bị bypass bởi service nội bộ; enforce tại service boundary.
- **Ước lượng tương đối:** M.

### AUTH-04 — Branch context trên giao diện dùng chung (P1)

- **Mục tiêu:** Hiển thị chi nhánh mặc định và cho đổi trong allowed branches mà không để client tự mở rộng quyền.
- **Dependency:** `AUTH-02`.
- **Phạm vi:** Auth context frontend, header branch switcher, event xóa/revalidate giỏ đang mở.
- **Ngoài phạm vi:** Bố cục riêng của màn mượn/trả/thu.
- **File dự kiến ảnh hưởng:** `frontend/src/context/AuthContext.jsx`, `AppLayout.jsx`, API auth/staff và shared state.
- **API/database migration:** Dùng `GET /api/staff/me/context`; không migration.
- **Test bắt buộc:** đổi branch, reload, branch hết quyền, reader không thấy switcher, sự kiện reset cart.
- **Acceptance criteria:** Chỉ option server trả mới chọn được; đổi branch không giữ selection/cart thuộc branch cũ.
- **Rủi ro:** State chi nhánh rải rác ở page; phải có một nguồn sự thật.
- **Ước lượng tương đối:** M.

### AUDIT-01 — Audit event có cấu trúc (P0)

- **Mục tiêu:** Thay log text best-effort bằng event có actor/branch/action/entity/before-after/reason/source/correlation và semantics đã chốt.
- **Dependency:** `BASE-02`, `AUTH-02`, `DG-AUDIT`.
- **Phạm vi:** Schema, writer, redaction, access bất biến và adapter cho `ActivityLogService` cũ.
- **Ngoài phạm vi:** Bổ sung coverage cho mọi màn hình trong một task; từng task nghiệp vụ phát event riêng.
- **File dự kiến ảnh hưởng:** `ActivityLogService.java`, entity/repository/controller audit, security config, migration.
- **API/database migration:** Mở rộng `NHATKYHOATDONG` hoặc bảng event mới; index actor/entity/time; endpoint page/filter admin.
- **Test bắt buộc:** `AUDIT-002`–`AUDIT-004`; rollback nghiệp vụ; secret/password/payment redaction.
- **Acceptance criteria:** Event chỉ tồn tại theo transaction design đã chốt; librarian không sửa/xóa/xem vượt quyền.
- **Rủi ro:** Before/after chứa PII lớn; cần allowlist field và retention.
- **Ước lượng tương đối:** L.

## 7. Epic C — Component giao diện dùng chung

### UI-01 — DataTable v2 core (P1)

- **Mục tiêu:** Bảng dùng chung hỗ trợ local/server mode, wrap cột, pagination đầy đủ và request state an toàn.
- **Dependency:** `API-01`.
- **Phạm vi:** Column width/minWidth/wrap/sticky, loading/error/empty, 20/50/100, first/last/go-to, sort, URL state, stale-request guard.
- **Ngoài phạm vi:** Selection/bulk và migrate một page nghiệp vụ cụ thể.
- **File dự kiến ảnh hưởng:** `frontend/src/components/DataTable.jsx`, `frontend/src/index.css`, test component.
- **API/database migration:** Chỉ consume page contract; không migration.
- **Test bắt buộc:** `TABLE-001`–`TABLE-007`, `TABLE-012`.
- **Acceptance criteria:** Local mode không phát request; server mode controlled; tên dài đọc được mà action không bị che.
- **Rủi ro:** Thay CSS global làm vỡ bảng reader/admin cũ; style phải scoped/versioned.
- **Ước lượng tương đối:** L.

### UI-02 — DataTable selection mode (P1)

- **Mục tiêu:** Checkbox chỉ xuất hiện khi bật và phân biệt selected page với all matching.
- **Dependency:** `UI-01`.
- **Phạm vi:** None/partial/all header, selected IDs, filtered descriptor, exclusions, sticky action bar, reset khi query đổi.
- **Ngoài phạm vi:** Gọi bulk mutation của books/copies/readers.
- **File dự kiến ảnh hưởng:** DataTable selection hook/component, CSS và tests.
- **API/database migration:** Chuẩn payload selection dùng chung; không endpoint mutation.
- **Test bắt buộc:** `TABLE-008`–`TABLE-011`, keyboard/ARIA selection.
- **Acceptance criteria:** Không có checkbox mặc định; query cũ không thể được bulk sau khi filter đổi.
- **Rủi ro:** All-matching bị hiểu nhầm là toàn database; UI phải luôn hiển thị query scope.
- **Ước lượng tương đối:** M.

### UI-03 — AsyncEntityPicker (P1)

- **Mục tiêu:** Component tìm entity không tải toàn bộ dữ liệu và hỗ trợ quét exact.
- **Dependency:** `API-01`.
- **Phạm vi:** Single/multi, debounce, abort, keyboard, exact match, chips, metadata, retry/empty/error.
- **Ngoài phạm vi:** Search endpoint cụ thể cho từng entity.
- **File dự kiến ảnh hưởng:** component/hook picker mới, shared styles và tests.
- **API/database migration:** Generic option/search adapter; không migration.
- **Test bắt buộc:** `PICK-001`, `PICK-002`, `PICK-004`, `PICK-005`.
- **Acceptance criteria:** Response cũ không ghi đè; duplicate selection bị chặn; scanner+Enter hoạt động.
- **Rủi ro:** Accessibility combobox phức tạp; cần test bàn phím/screen reader.
- **Ước lượng tương đối:** L.

### UI-04 — Drawer và filter primitives (P1)

- **Mục tiêu:** Drawer/filter dùng chung giữ page/query state và hỗ trợ active filter chips.
- **Dependency:** `UI-01`.
- **Phạm vi:** Drawer route/query, filter panel, date range, chips, clear/reset, permission-aware action slot.
- **Ngoài phạm vi:** Nội dung drawer từng màn hình.
- **File dự kiến ảnh hưởng:** shared drawer/filter components, router helpers, styles/tests.
- **API/database migration:** Không.
- **Test bắt buộc:** mở/đóng/back/forward, focus trap/Escape, query preservation.
- **Acceptance criteria:** Drawer không làm mất filter/page và trả focus đúng dòng mở.
- **Rủi ro:** Deep link và modal stacking; quy định một drawer hoạt động tại một thời điểm.
- **Ước lượng tương đối:** M.

## 8. Epic D — Màn hình đầu sách

### BOOK-01 — Danh sách đầu sách phân trang (P1)

- **Mục tiêu:** Màn đầu sách không dùng `findAll()` và hiển thị tên/summary đúng ưu tiên.
- **Dependency:** `API-01`, `UI-01`.
- **Phạm vi:** Search mã/tên/ISBN/tác giả; filter trạng thái/thể loại/tác giả/NXB/năm; stable sort; copy summary; URL state.
- **Ngoài phạm vi:** Form, drawer, lifecycle và bulk mutation.
- **File dự kiến ảnh hưởng:** `BooksPage.jsx`, `libraryApi.js`, `DauSachController/Service/Repository`, response DTO, CSS.
- **API/database migration:** Nâng `GET /api/books` theo page contract; index search/filter và query tránh N+1.
- **Test bắt buộc:** `BOOK-001`, `TABLE-001`, `TABLE-003`–`TABLE-007`, `PERF-001`–`PERF-003`.
- **Acceptance criteria:** Page 20 chỉ tải 20; tên chiếm khoảng 35–40% và wrap; total/facet đúng.
- **Rủi ro:** Count/join tác giả-thể loại làm duplicate rows; cần query/projection tách hợp lý.
- **Ước lượng tương đối:** L.

### BOOK-02 — Search danh mục cho form đầu sách (P1)

- **Mục tiêu:** Tác giả, thể loại và NXB chọn bằng tên thay vì nhập mã.
- **Dependency:** `UI-03`, `API-01`.
- **Phạm vi:** Search endpoint giới hạn kết quả, exact ưu tiên, active filter và adapters picker.
- **Ngoài phạm vi:** Lưu đầu sách hoặc tạo nhanh danh mục.
- **File dự kiến ảnh hưởng:** controller/service/repository tác giả-thể loại-NXB, `libraryApi.js`, picker adapters.
- **API/database migration:** Endpoint `/api/staff/catalog/{authors|categories|publishers}/search`; index tên nếu cần.
- **Test bắt buộc:** `BOOK-003`, `PICK-001`, `PICK-003` theo collation/search strategy.
- **Acceptance criteria:** Không tải toàn bộ danh mục; payload option có ID, label và metadata cần thiết.
- **Rủi ro:** Tìm tiếng Việt không dấu chưa được chốt kỹ thuật; ghi rõ capability thực tế.
- **Ước lượng tương đối:** M.

### BOOK-03 — Form tạo/sửa đầu sách (P1)

- **Mục tiêu:** Form sạch dữ liệu test, DTO tách biệt và validation ISBN/near-duplicate đúng.
- **Dependency:** `BOOK-02`, `ID-01`, `DG-DUPLICATE-TITLE`, `DG-OPTIMISTIC-LOCK`.
- **Phạm vi:** Create/update DTO; picker; ISBN normalize/checksum/conflict; field errors; unsaved guard.
- **Ngoài phạm vi:** Ẩn/khôi phục/hard delete và drawer.
- **File dự kiến ảnh hưởng:** `BooksPage.jsx` hoặc form mới, `DauSachRequest/Response/Service`, API client.
- **API/database migration:** `POST/PATCH /api/books`; unique ISBN hiện có được giữ; version column nếu chốt.
- **Test bắt buộc:** `BOOK-002`–`BOOK-005`, mass assignment và concurrent edit theo quyết định.
- **Acceptance criteria:** Không nhập comma codes/ID; ISBN trùng không tạo bản ghi; ID do server trả.
- **Rủi ro:** Near-duplicate false positive; chỉ warning/override theo policy đã chốt.
- **Ước lượng tương đối:** L.

### BOOK-04 — Drawer chi tiết đầu sách (P1)

- **Mục tiêu:** Xem metadata và bản vật lý mà không nhồi mọi cột vào bảng.
- **Dependency:** `BOOK-01`, `UI-04`.
- **Phạm vi:** Tab Tổng quan, Bản vật lý theo chi nhánh, Lịch sử; giữ list query state.
- **Ngoài phạm vi:** Chỉnh sửa/lifecycle trong drawer.
- **File dự kiến ảnh hưởng:** component drawer đầu sách, API detail/summary/history và styles.
- **API/database migration:** Mở rộng `GET /api/books/{id}` hoặc endpoint detail/history; không migration nếu audit/history đã có.
- **Test bắt buộc:** `BOOK-006`, branch visibility của copy summary, focus/deep link.
- **Acceptance criteria:** Đóng drawer quay đúng trang/filter; không lộ copy ngoài allowed branch cho librarian.
- **Rủi ro:** Tab lịch sử phụ thuộc audit coverage; hiển thị trạng thái chưa có rõ ràng.
- **Ước lượng tương đối:** M.

### BOOK-05 — Lifecycle đầu sách (P0/P1)

- **Mục tiêu:** Thay DELETE soft mode bằng deactivate/reactivate có lý do; hard delete admin-only có preflight.
- **Dependency:** `AUDIT-01`, `BOOK-04`, `DG-TITLE-RESERVATION`.
- **Phạm vi:** API/action UI, reason, actor/time, reservation policy, hard-delete dependency report.
- **Ngoài phạm vi:** Bulk lifecycle.
- **File dự kiến ảnh hưởng:** `BooksPage.jsx`, `DauSachController/Service`, security config, audit integration.
- **API/database migration:** `/deactivate`, `/reactivate`, admin hard-delete/preflight; lifecycle event/history nếu audit không đủ.
- **Test bắt buộc:** `BOOK-007`–`BOOK-009`, role/IDOR, rollback khi có liên kết.
- **Acceptance criteria:** Ẩn không đổi cuốn/loan; librarian không hard delete; mọi action có audit.
- **Rủi ro:** Xử lý reservation sai gây mất hàng chờ; task bị chặn tới khi gate chốt.
- **Ước lượng tương đối:** M.

## 9. Epic E — Màn hình cuốn sách

### COPY-01 — Mô hình trạng thái cuốn tách biệt (P0)

- **Mục tiêu:** Tách lưu thông, tình trạng vật lý và điều chuyển; khóa operational transition khỏi CRUD generic.
- **Dependency:** `BASE-02`, `AUDIT-01`.
- **Phạm vi:** Domain/state machine, backfill trạng thái hiện hữu, transition history và validation nguồn/đích.
- **Ngoài phạm vi:** Workflow chuyển chi nhánh và UI action cụ thể.
- **File dự kiến ảnh hưởng:** entity/service/repository cuốn, DTO, migration, rule tests.
- **API/database migration:** Cột/bảng state axes và `COPY_STATE_EVENT`; giữ mapping mã cũ trong compatibility period.
- **Test bắt buộc:** `COPY-007`, các transition của `BR-COPY-02/03`, migrate mọi trạng thái seed.
- **Acceptance criteria:** PUT generic không thể tạo `Đang mượn/Đang đặt trước`; history có actor/source/reason.
- **Rủi ro:** Hai nguồn trạng thái cũ/mới lệch nhau; cần một adapter và cutoff rõ.
- **Ước lượng tương đối:** L.

### COPY-02 — Danh sách cuốn sách phân trang (P1)

- **Mục tiêu:** Màn cuốn sách có server search/filter/preset và branch scope.
- **Dependency:** `COPY-01`, `AUTH-03`, `UI-01`, `BOOK-01`.
- **Phạm vi:** Search mã/barcode/tên/ISBN; filter status/branch/date/title/location/has-code; labels; presets.
- **Ngoài phạm vi:** Batch create, drawer và condition actions.
- **File dự kiến ảnh hưởng:** `BookCopiesPage.jsx`, `libraryApi.js`, `CuonSachController/Service/Repository`, DTO.
- **API/database migration:** Nâng `GET /api/book-copies`; index branch/state/date/title/location/barcode; query tránh N+1.
- **Test bắt buộc:** `COPY-001`, `COPY-002`, `PERF-001`–`PERF-003`.
- **Acceptance criteria:** Không query/view ngoài allowed branches; page/facet/labels chính xác.
- **Rủi ro:** Filter theo state axes mới có semantics OR/AND khó hiểu; contract phải cố định.
- **Ước lượng tương đối:** L.

### COPY-03 — Vị trí phụ thuộc chi nhánh (P0/P1)

- **Mục tiêu:** Chỉ chọn và lưu vị trí thuộc chi nhánh đã chọn.
- **Dependency:** `AUTH-03`, `UI-03`.
- **Phạm vi:** API cascade branch→khu→kệ→vị trí; metadata; backend membership validation.
- **Ngoài phạm vi:** Chuyển vị trí cuốn đang tồn tại.
- **File dự kiến ảnh hưởng:** `OptionController.java`, repository vị trí/kệ/khu, API client và picker vị trí.
- **API/database migration:** `GET /api/options/book-locations?branchId=...`; constraint/trigger consistency nếu thiết kế chọn.
- **Test bắt buộc:** `COPY-003`, inactive branch/location, sửa request gửi location chi nhánh khác.
- **Acceptance criteria:** Đổi branch xóa location cũ; backend chặn dù client bypass UI.
- **Rủi ro:** `CUONSACH.MaChiNhanh` trùng dữ liệu suy ra từ location; migration phải phát hiện mismatch.
- **Ước lượng tương đối:** M.

### COPY-04 — Batch tạo cuốn API (P0/P1)

- **Mục tiêu:** Tạo nhiều bản cùng đầu sách atomic, server sinh ID/barcode và ép trạng thái ban đầu hợp lệ.
- **Dependency:** `ID-01`, `COPY-01`, `COPY-03`, `AUTH-03`, `AUDIT-01`.
- **Phạm vi:** Auto/manual barcode, quantity limit, title/branch/location validation, transaction và per-row conflict details.
- **Ngoài phạm vi:** Wizard frontend và in tem.
- **File dự kiến ảnh hưởng:** controller/service/DTO batch copy, repositories, barcode allocator.
- **API/database migration:** `POST /api/book-copies/batch`; unique barcode/QR giữ nguyên; batch/event metadata nếu cần.
- **Test bắt buộc:** `COPY-004`–`COPY-006`, actor/branch tampering và rollback lô.
- **Acceptance criteria:** Một lỗi rollback toàn lô; không trạng thái operational; audit biết batch và copies tạo.
- **Rủi ro:** Lô quá lớn giữ lock lâu; giới hạn quantity và không nhận hàng nghìn item đồng bộ.
- **Ước lượng tương đối:** L.

### COPY-05 — Wizard tạo lô cuốn sách (P1)

- **Mục tiêu:** Thủ thư chọn đầu sách/chi nhánh/vị trí và preview lô mà không nhập mã kỹ thuật.
- **Dependency:** `COPY-04`, `BOOK-02`, `AUTH-04`, `UI-03`.
- **Phạm vi:** Picker đầu sách, branch read-only/allowed, location cascade, quantity/barcode mode, preview/result.
- **Ngoài phạm vi:** In nhãn và chỉnh sửa cuốn sau tạo.
- **File dự kiến ảnh hưởng:** `BookCopiesPage.jsx` hoặc wizard mới, API client, form tests.
- **API/database migration:** Consume batch API; không migration.
- **Test bắt buộc:** picker exact, manual duplicate mapping, branch change reset, retry không submit ngầm.
- **Acceptance criteria:** Không có ID/hard-code; kết quả hiển thị đúng các mã server cấp.
- **Rủi ro:** Network ambiguity trước khi batch có idempotency; UI phải tra kết quả/không tự gửi lại mù.
- **Ước lượng tương đối:** L.

### COPY-06 — Drawer và lịch sử cuốn sách (P1)

- **Mục tiêu:** Hiển thị nhận diện, vị trí, state axes và lịch sử trong một drawer.
- **Dependency:** `COPY-02`, `COPY-01`, `UI-04`.
- **Phạm vi:** Tab Tổng quan, Vị trí/tình trạng, Lưu thông và audit history.
- **Ngoài phạm vi:** Thực hiện transition trong drawer.
- **File dự kiến ảnh hưởng:** copy drawer, detail/history endpoints, DTO/projections.
- **API/database migration:** `GET /api/book-copies/{id}` enriched và `/history`; dùng state events.
- **Test bắt buộc:** branch IDOR, history order, preserve list state.
- **Acceptance criteria:** Không chỉ hiển thị mã; state hiện tại khớp event cuối; deep link hợp lệ.
- **Rủi ro:** Join lịch sử lớn; endpoint phải phân trang nếu vượt ngưỡng.
- **Ước lượng tương đối:** M.

### COPY-07 — Action tình trạng và chuyển vị trí (P0/P1)

- **Mục tiêu:** Thay select tự do bằng action Hỏng/Mất/Ngừng lưu thông/Khôi phục và Di chuyển vị trí có lý do.
- **Dependency:** `COPY-01`, `COPY-03`, `COPY-06`, `AUTH-03`, `AUDIT-01`.
- **Phạm vi:** Endpoint/action UI, transition guard, before/after, reason; chặn cuốn đang mượn/giữ.
- **Ngoài phạm vi:** Chuyển chi nhánh và sửa chữa nhiều bước.
- **File dự kiến ảnh hưởng:** `BookCopiesPage.jsx`, copy drawer/actions, `CuonSachService/Controller`, state service.
- **API/database migration:** `/condition-events`, `/move-location`; dùng event tables đã tạo.
- **Test bắt buộc:** `COPY-008`, `COPY-009`, role/branch, invalid transition và rollback.
- **Acceptance criteria:** Không chỉnh operational state; mọi action có actor/reason/history.
- **Rủi ro:** Đánh dấu Mất cuốn đang mượn cần đi qua return/lost workflow, không action inventory.
- **Ước lượng tương đối:** L.

## 10. Epic F — Màn hình độc giả và bảo mật tài khoản

### READER-01 — Migration trạng thái độc giả tách biệt (P0)

- **Mục tiêu:** Tách account, lifecycle, borrowing/login lock và derived card/membership status.
- **Dependency:** `BASE-02`, `AUDIT-01`.
- **Phạm vi:** Reader lifecycle/lock/card event schema, backfill; không lưu “Hết hạn” như action thủ công mới.
- **Ngoài phạm vi:** UI, reset password và eligibility rule.
- **File dự kiến ảnh hưởng:** entity/repository reader/account/membership, migration và mapping compatibility.
- **API/database migration:** Bảng lock/lifecycle/card event; index expiry; giữ `DOCGIA.TrangThai` tạm thời nếu cần adapter.
- **Test bắt buộc:** `READER-003`–`READER-005`, migration active/expired/locked/inactive samples.
- **Acceptance criteria:** Có thể biểu diễn thẻ hết hạn nhưng hồ sơ active, borrow lock nhưng login vẫn hoạt động.
- **Rủi ro:** Backfill không biết lý do khóa cũ; đánh dấu legacy reason thay vì bịa dữ liệu.
- **Ước lượng tương đối:** L.

### READER-02 — Danh sách độc giả phân trang (P1)

- **Mục tiêu:** Màn độc giả tìm/lọc/sort server-side và trình bày họ tên đầy đủ.
- **Dependency:** `READER-01`, `UI-01`, `API-01`.
- **Phạm vi:** Search mã/tên/email/phone; filter nhóm/gói/profile/card/membership/account/eligibility; presets expiry/locked.
- **Ngoài phạm vi:** Drawer và transition actions.
- **File dự kiến ảnh hưởng:** `ReadersPage.jsx`, `libraryApi.js`, `DocGiaController/Service/Repository`, list DTO.
- **API/database migration:** Nâng `GET /api/readers`; index name/status/expiry/membership; query tránh N+1.
- **Test bắt buộc:** `READER-001`, `READER-002`, `PERF-001`–`PERF-003`, PII field allowlist.
- **Acceptance criteria:** Họ tên 240–300px, wrap; expiry card/gói không trộn với lifecycle.
- **Rủi ro:** Sort tên tiếng Việt cần collation/quy tắc rõ; ghi nhận behavior test.
- **Ước lượng tương đối:** L.

### READER-03 — DTO và form hồ sơ độc giả (P0/P1)

- **Mục tiêu:** Tách create/profile update khỏi password, membership và state để chặn mass assignment.
- **Dependency:** `READER-01`, `ID-01`, `DG-OPTIMISTIC-LOCK`.
- **Phạm vi:** `ReaderCreateRequest`, `ReaderProfileUpdateRequest`, form không chứa mật khẩu giả khi sửa.
- **Ngoài phạm vi:** Card renewal, membership change, lock và password reset.
- **File dự kiến ảnh hưởng:** `ReadersPage.jsx`/form mới, `DocGiaController/Service`, DTO và API client.
- **API/database migration:** `POST /api/readers`, `PATCH /api/readers/{id}/profile`; version nếu chốt.
- **Test bắt buộc:** `READER-008`, duplicate username/email, password/status field injection.
- **Acceptance criteria:** Profile update không nhận/đòi password; ID do server cấp; create account atomic.
- **Rủi ro:** Endpoint legacy còn cho mass assignment; phải deprecate/adapter có allowlist.
- **Ước lượng tương đối:** M.

### READER-04 — Lifecycle và lock actions (P0)

- **Mục tiêu:** Khóa/mở khóa/ngừng hoạt động/khôi phục qua transition riêng có scope, lý do và đánh giá nghĩa vụ.
- **Dependency:** `READER-01`, `READER-03`, `AUTH-03`, `AUDIT-01`, `DG-READER-CLOSE`.
- **Phạm vi:** Borrow/login scopes, thời hạn, deactivate/reactivate và effective warnings.
- **Ngoài phạm vi:** Gia hạn thẻ/gói và reset mật khẩu.
- **File dự kiến ảnh hưởng:** reader controller/service/state service, drawer actions, security/account validation.
- **API/database migration:** `/locks`, `/unlock`, `/deactivate`, `/reactivate`; dùng event schema.
- **Test bắt buộc:** `READER-004`–`READER-007`, trả/thu vẫn hoạt động khi borrow lock.
- **Acceptance criteria:** Mở khóa không gia hạn/xóa nợ; restore không đặt mù quáng thành eligible.
- **Rủi ro:** Đóng hồ sơ còn nghĩa vụ chưa chốt; task không code nhánh này trước gate.
- **Ước lượng tương đối:** L.

### READER-05 — Gia hạn thẻ và thay đổi gói (P1)

- **Mục tiêu:** Thay update trực tiếp bằng event bất biến cho card/membership.
- **Dependency:** `READER-01`, `READER-03`, `DG-EXPIRY`.
- **Phạm vi:** Card renewal, membership change/renewal, tính ngày server-side, history append-only.
- **Ngoài phạm vi:** Thu tiền mua gói nếu chưa tích hợp tài chính.
- **File dự kiến ảnh hưởng:** reader/membership services, DTO/controller, drawer tab gói/thẻ.
- **API/database migration:** `/card-renewals`, `/membership-changes`; index/history constraint; không sửa bản ghi lịch sử cũ.
- **Test bắt buộc:** boundary date/timezone, overlapping membership, history immutability.
- **Acceptance criteria:** Hạn mới do backend tính; event cũ không bị ghi đè; UI hiển thị effective period.
- **Rủi ro:** Quy tắc ngày hết hạn chưa chốt; gate bắt buộc.
- **Ước lượng tương đối:** L.

### READER-06 — Borrowing policy và context (P0/P1)

- **Mục tiêu:** Tính eligibility bằng rule version, blocker/warning/override thay cho điều kiện hard-code.
- **Dependency:** `READER-04`, `READER-05`, `DG-EXPIRY`.
- **Phạm vi:** Ngưỡng nợ/quá hạn/gói/quota, stable reason codes, policy version và reader context endpoint.
- **Ngoài phạm vi:** UI lập phiếu mượn và thực hiện override.
- **File dự kiến ảnh hưởng:** rule entities/service/admin rules, reader context controller/DTO, repositories debt/loan/membership.
- **API/database migration:** `GET /api/staff/readers/{id}/borrowing-context`; bảng/field rule version và override event.
- **Test bắt buộc:** `LOAN-002`, `LOAN-003`, tổ hợp rule, timezone, policy version cũ/mới.
- **Acceptance criteria:** Không hard-code threshold; response có `eligible`, warnings và blockingReasons có code.
- **Rủi ro:** Chưa có giá trị seed chính thức; triển khai engine nhưng không bật production rule chưa duyệt.
- **Ước lượng tương đối:** L.

### READER-07 — Drawer độc giả (P1)

- **Mục tiêu:** Gom hồ sơ, gói, sách mượn, nợ, lịch sử và bảo mật vào drawer có phân quyền PII.
- **Dependency:** `READER-02`, `READER-04`–`READER-06`, `UI-04`.
- **Phạm vi:** Các tab read-only và action slot theo permission; giữ list state.
- **Ngoài phạm vi:** Implement logic reset password; task `PASS-02` làm riêng.
- **File dự kiến ảnh hưởng:** reader drawer components, detail/history API clients và projections.
- **API/database migration:** Detail/context endpoints hiện có được compose; không migration mới.
- **Test bắt buộc:** PII authorization, tab lazy load, drawer state, current loans/debts accuracy.
- **Acceptance criteria:** Bảng chính không lộ PII dư thừa; action chỉ hiện khi backend context có permission.
- **Rủi ro:** Một payload quá lớn/N+1; mỗi tab tải riêng và phân trang history.
- **Ước lượng tương đối:** L.

### PASS-01 — Token revocation và force-change nền (P0)

- **Mục tiêu:** Token phát trước reset/khóa mất hiệu lực và tài khoản mật khẩu tạm chỉ được đổi mật khẩu.
- **Dependency:** `BASE-02`, `AUTH-02`.
- **Phạm vi:** `MustChangePassword`, `PasswordChangedAt`, `TokenVersion` hoặc cơ chế tương đương; token issued-at; security gate.
- **Ngoài phạm vi:** Endpoint reset độc giả và UI.
- **File dự kiến ảnh hưởng:** `TaiKhoan`, `TokenService`, `TokenAuthenticationFilter`, auth/change-password service, migration.
- **API/database migration:** Cột account security và token claims/version; compatibility logout cho token cũ.
- **Test bắt buộc:** `PASS-003`, `PASS-004`, account/staff lock với token đang sống.
- **Acceptance criteria:** Token cũ bị 401 sau revoke; force-change không truy cập nghiệp vụ khác.
- **Rủi ro:** Deploy làm logout toàn bộ user; cần kế hoạch compatibility có chủ đích.
- **Ước lượng tương đối:** L.

### PASS-02 — Reset mật khẩu độc giả có permission (P0)

- **Mục tiêu:** Admin hoặc thủ thư có `READER_PASSWORD_RESET` reset sau xác minh, rate limit và audit không secret.
- **Dependency:** `PASS-01`, `READER-03`, `AUDIT-01`, `AUTH-03`.
- **Phạm vi:** Xác minh tối thiểu hai thuộc tính, temporary secret one-time, reason, revoke, force-change và rate limit.
- **Ngoài phạm vi:** Email/SMS reset link nếu chưa có hạ tầng gửi.
- **File dự kiến ảnh hưởng:** reader password controller/service/DTO, permission config, rate limiter và audit.
- **API/database migration:** `POST /api/readers/{id}/password-reset`; reset event/rate data nếu cần.
- **Test bắt buộc:** `PASS-001`–`PASS-005`, secret exclusion, same-branch/scope theo permission.
- **Acceptance criteria:** Không đọc password cũ; secret chỉ trả một lần; token cũ vô hiệu; audit không chứa secret.
- **Rủi ro:** Xác minh bằng dữ liệu dễ biết; checklist xác minh cần được chính sách vận hành phê duyệt.
- **Ước lượng tương đối:** L.

### PASS-03 — UI reset mật khẩu độc giả (P1)

- **Mục tiêu:** Cung cấp action reset an toàn trong drawer độc giả.
- **Dependency:** `PASS-02`, `READER-07`.
- **Phạm vi:** Dialog xác minh/lý do, permission-aware action, one-time temporary secret và cảnh báo giao bí mật.
- **Ngoài phạm vi:** Hiển thị/sửa mật khẩu hiện tại hoặc lưu secret trong local storage.
- **File dự kiến ảnh hưởng:** reader drawer security tab, password dialog, API client và tests.
- **API/database migration:** Consume reset API; không migration.
- **Test bắt buộc:** permission hidden/403, copy secret một lần, close clears secret, no console/log leak.
- **Acceptance criteria:** Reload/back không khôi phục secret; UI yêu cầu xác nhận trước reset.
- **Rủi ro:** Screenshot/clipboard vẫn là rủi ro vận hành; cần hướng dẫn rõ.
- **Ước lượng tương đối:** M.

## 11. Epic G — Màn hình mượn sách

### LOAN-01 — Attribution và branch guard cho create loan (P0)

- **Mục tiêu:** Phiếu mượn luôn ghi actor principal và branch đã authorize.
- **Dependency:** `AUTH-03`, `AUDIT-01`, `DG-ADMIN-ACTOR`.
- **Phạm vi:** Bỏ/đối chiếu `maNhanVienLap`, kiểm tra selected branch, audit actor/branch; giữ copy row lock hiện có.
- **Ngoài phạm vi:** UI mới, ID/idempotency và reservation handoff.
- **File dự kiến ảnh hưởng:** `MuonSachRequest/Service`, `StaffLoanController`, security tests.
- **API/database migration:** Body mới không có staff ID; không migration.
- **Test bắt buộc:** `AUTH-004`, `AUTH-005`, `LOAN-007`.
- **Acceptance criteria:** Spoof actor/branch không thay đổi dữ liệu; phiếu/audit khớp principal.
- **Rủi ro:** Endpoint legacy vẫn được gọi; adapter phải không tiếp tục tin actor client.
- **Ước lượng tương đối:** M.

### LOAN-02 — Search độc giả và cuốn có thể mượn (P1)

- **Mục tiêu:** Cung cấp lookup giới hạn cho reader-first và scan cuốn theo branch.
- **Dependency:** `UI-03`, `READER-06`, `COPY-01`, `AUTH-03`.
- **Phạm vi:** Reader search; copy/title/barcode exact; reservation/borrowable metadata; không tải bảng tất cả cuốn.
- **Ngoài phạm vi:** Loan cart và commit.
- **File dự kiến ảnh hưởng:** staff search controllers/services/repositories, `staffApi.js`, picker adapters.
- **API/database migration:** `/api/staff/readers/search`, `/book-copies/search`, `/book-copies/by-barcode/{barcode}`; search indexes.
- **Test bắt buộc:** `LOAN-001`, `LOAN-004`, `PICK-001`–`PICK-003`, branch IDOR.
- **Acceptance criteria:** Exact barcode một request; cuốn giữ cho người khác trả reason rõ; kết quả giới hạn.
- **Rủi ro:** Lookup và commit cách nhau; metadata chỉ là preview, create phải revalidate.
- **Ước lượng tương đối:** L.

### LOAN-03 — Loan preview API (P0/P1)

- **Mục tiêu:** Preview quota, blocker/warning, rule và hạn trả từng cuốn trước commit.
- **Dependency:** `READER-06`, `LOAN-02`, `COPY-01`, `DG-EXPIRY`.
- **Phạm vi:** `POST /api/staff/loans/preview`; duplicate/quota/reservation checks; không lock dài hạn.
- **Ngoài phạm vi:** Tạo phiếu hoặc override commit.
- **File dự kiến ảnh hưởng:** loan preview DTO/service/controller, rule engine and tests.
- **API/database migration:** Endpoint preview; không migration ngoài rule tables đã có.
- **Test bắt buộc:** `LOAN-002`, `LOAN-003`, `LOAN-005`, `LOAN-006`.
- **Acceptance criteria:** Due date server-calculated per item; preview ghi rõ không bảo đảm commit.
- **Rủi ro:** UI hiểu preview là reservation; copy phải được revalidate khi create.
- **Ước lượng tương đối:** L.

### LOAN-04 — Giao diện reader-first, giỏ mượn và preview (P1)

- **Mục tiêu:** Thay form mã/hard-code và bảng cuốn sẵn có bằng scan/picker/cart.
- **Dependency:** `AUTH-04`, `LOAN-02`, `LOAN-03`.
- **Phạm vi:** Reader summary, collapsed current loans, scan copy, duplicate guard, item due date, keyboard focus và warnings.
- **Ngoài phạm vi:** Logic commit backend và in phiếu.
- **File dự kiến ảnh hưởng:** `StaffLoansPage.jsx`, `staffApi.js`, loan cart components/styles/tests.
- **API/database migration:** Consume search/context/preview; không migration.
- **Test bắt buộc:** `LOAN-001`, `LOAN-004`, `LOAN-006`, accessibility scanner flow.
- **Acceptance criteria:** Không nhập actor/branch/ID; không tải toàn bộ copies; branch change xóa giỏ.
- **Rủi ro:** Preview request quá thường xuyên; debounce và hủy request cũ.
- **Ước lượng tương đối:** L.

### LOAN-05 — Atomic/idempotent create loan và nhận hold (P0)

- **Mục tiêu:** Tạo phiếu all-or-nothing, server ID, principal actor, idempotency và đúng reservation owner.
- **Dependency:** `LOAN-01`, `LOAN-03`, `ID-01`, `IDEM-01`, `COPY-01`, quyết định reservation FIFO đã chốt, `DG-ADMIN-ACTOR`.
- **Phạm vi:** Revalidate reader/copies/quota, row lock, transition copy, consume hold, audit và stored idempotent response.
- **Ngoài phạm vi:** Print/email và override phức tạp ngoài policy engine.
- **File dự kiến ảnh hưởng:** `MuonSachService`, repositories loan/copy/reservation, DTO/controller và audit.
- **API/database migration:** `POST /api/staff/loans` contract mới; reservation active constraints/index nếu thiếu.
- **Test bắt buộc:** `LOAN-008`–`LOAN-011`, `REL-001`, transaction rollback.
- **Acceptance criteria:** Một copy lỗi không tạo partial; đúng hold reader mới mượn được; retry trả cùng loan.
- **Rủi ro:** Thứ tự lock copy/reservation gây deadlock; quy định lock order và load test.
- **Ước lượng tương đối:** L.

### LOAN-06 — Kết quả và bản in phiếu mượn (P1)

- **Mục tiêu:** Hiển thị/in dữ liệu server trả và reset giao dịch an toàn.
- **Dependency:** `LOAN-05`.
- **Phạm vi:** Result ID, actor/branch/items/due dates/rules, print stylesheet/payload, new-transaction focus.
- **Ngoài phạm vi:** Hệ thống email hoặc máy in chuyên dụng.
- **File dự kiến ảnh hưởng:** loan result modal/page, print component, response DTO.
- **API/database migration:** Mở rộng response/result endpoint; không migration.
- **Test bắt buộc:** `LOAN-012`, refresh/result lookup, print snapshot.
- **Acceptance criteria:** In không phụ thuộc state form cũ; bắt đầu giao dịch mới xóa reader/cart và focus thẻ.
- **Rủi ro:** In từ client có khác biệt trình duyệt; xác định browser hỗ trợ.
- **Ước lượng tương đối:** M.

## 12. Epic H — Màn hình trả sách

### RETURN-01 — Lookup open loan theo barcode/độc giả (P1)

- **Mục tiêu:** Quét cuốn tìm đúng open loan và tự suy ra độc giả.
- **Dependency:** `LOAN-05`, `AUTH-03`, `UI-03`.
- **Phạm vi:** Exact barcode/copy/detail lookup; enriched open loans của reader; same-branch validation.
- **Ngoài phạm vi:** Preview tình trạng/phạt và tạo phiếu.
- **File dự kiến ảnh hưởng:** staff return lookup controller/service/repository, `staffApi.js`.
- **API/database migration:** `/open-loans/by-barcode/{barcode}`, `/readers/{id}/open-loans`; index copy/status/due.
- **Test bắt buộc:** `RETURN-001`, `RETURN-002`, `RETURN-009` theo quyết định cấm cross-branch.
- **Acceptance criteria:** Trả khác chi nhánh bị chặn rõ; response có reader/title/due/overdue/borrow branch.
- **Rủi ro:** Bản ghi lịch sử trùng barcode; query phải chỉ chọn open detail duy nhất.
- **Ước lượng tương đối:** M.

### RETURN-02 — Giỏ trả sách trên giao diện (P1)

- **Mục tiêu:** Cho scan/chọn nhiều cuốn của một độc giả và bỏ bảng global borrowers.
- **Dependency:** `RETURN-01`, `AUTH-04`.
- **Phạm vi:** Auto reader từ cuốn đầu, one-reader cart, checkbox open loans, overdue emphasis, remove global report.
- **Ngoài phạm vi:** Tính fine và commit.
- **File dự kiến ảnh hưởng:** `StaffReturnsPage.jsx`, return cart components/styles/tests.
- **API/database migration:** Consume lookup endpoints; không migration.
- **Test bắt buộc:** `RETURN-003`, `RETURN-004`, duplicate scan, branch change reset.
- **Acceptance criteria:** Không trộn độc giả; scan và checkbox đồng bộ; không nhập actor/branch.
- **Rủi ro:** Một cuốn được trả ở quầy khác sau khi vào cart; preview/commit phải revalidate.
- **Ước lượng tương đối:** L.

### RETURN-03 — Rule phạt hỏng/mất và assessment (P0)

- **Mục tiêu:** Backend tính đề xuất theo trị giá/severity/rule version và kiểm soát override.
- **Dependency:** `BASE-02`, `AUDIT-01`, quyết định công thức đã chốt.
- **Phạm vi:** Damage types/severity, formula config, proposed/final amount, threshold approval, reason/approver.
- **Ngoài phạm vi:** Late fine và create return.
- **File dự kiến ảnh hưởng:** rule entities/admin rules, damage assessment service/DTO, migration.
- **API/database migration:** Bảng rule/assessment/version; permission fine adjustment; không nhận final amount tùy ý.
- **Test bắt buộc:** `RETURN-007`, `RETURN-008`, zero/missing value, rule version history.
- **Acceptance criteria:** Không hard-code tỷ lệ; client sửa số tiền không quyết định kết quả; override được audit.
- **Rủi ro:** Trị giá sách bằng 0/thiếu; cần rule fallback được cấu hình trước activation.
- **Ước lượng tương đối:** L.

### RETURN-04 — Return preview API (P0/P1)

- **Mục tiêu:** Tính phạt trễ, damage/loss, next copy state và hold instruction trước commit.
- **Dependency:** `RETURN-01`, `RETURN-03`, `READER-06`, `DG-EXPIRY`.
- **Phạm vi:** Multi-item preview, server date/rule snapshot, same reader/branch, proposed adjustment.
- **Ngoài phạm vi:** Lock/commit hoặc tạo debt.
- **File dự kiến ảnh hưởng:** return preview controller/service/DTO, late fine rule service.
- **API/database migration:** `POST /api/staff/returns/preview`; không migration mới.
- **Test bắt buộc:** `RETURN-005`–`RETURN-009`, boundary due date, tampered client fields.
- **Acceptance criteria:** Response nêu next state `AVAILABLE/ON_HOLD/DAMAGED/LOST`; cross-branch luôn block release này.
- **Rủi ro:** Reservation có thể đổi sau preview; commit phải lock/recalculate.
- **Ước lượng tương đối:** L.

### RETURN-05 — Atomic/idempotent batch return và FIFO hold (P0)

- **Mục tiêu:** Trả nhiều cuốn atomic, khóa bản ghi, tạo debt và giao cuốn bình thường cho reservation FIFO cùng chi nhánh.
- **Dependency:** `RETURN-04`, `ID-01`, `IDEM-01`, `AUTH-03`, `AUDIT-01`, `DG-ADMIN-ACTOR`.
- **Phạm vi:** Principal actor, same branch, lock detail/copy/reservation, server ID, update loan/copy, debts, notification/hold expiry.
- **Ngoài phạm vi:** Cross-branch return và payment tự động.
- **File dự kiến ảnh hưởng:** `TraSachService`, repositories return/loan/copy/debt/reservation, controller/DTO.
- **API/database migration:** `POST /api/staff/returns` mới; active reservation unique/index, hold event và idempotency.
- **Test bắt buộc:** `RETURN-010`–`RETURN-013`, `LOAN-011`, `REL-001`, concurrent return.
- **Acceptance criteria:** Không partial; retry trả cùng result; reservation lâu nhất hợp lệ nhận hold; loan header đúng.
- **Rủi ro:** Lock nhiều bảng gây deadlock; xác định lock order copy→loan detail→reservation→debt.
- **Ước lượng tương đối:** L.

### RETURN-06 — Giao diện tình trạng, kết quả và chuyển thu tiền (P1)

- **Mục tiêu:** Hoàn thiện return cart với severity/preview và kết quả có debt/hold/payment handoff.
- **Dependency:** `RETURN-02`, `RETURN-04`, `RETURN-05`.
- **Phạm vi:** Condition per row, preview totals, override permission UI, result/print, link mở payment với debts preselected.
- **Ngoài phạm vi:** Logic thu tiền và upload ảnh nếu chưa có asset service.
- **File dự kiến ảnh hưởng:** `StaffReturnsPage.jsx`, return condition/result components, API client, print styles.
- **API/database migration:** Consume preview/create/result; không migration.
- **Test bắt buộc:** `RETURN-006`–`RETURN-008`, result debt IDs, hold instruction, reset flow.
- **Acceptance criteria:** Không có ô nhập tiền phạt tự do; UI hiển thị proposed/final/reason rõ.
- **Rủi ro:** Permission UI không thay backend authorization; mọi adjustment vẫn revalidate server-side.
- **Ước lượng tương đối:** L.

## 13. Epic I — Màn hình thu tiền và đảo phiếu

### PAY-01 — Attribution và khóa tài chính nền (P0)

- **Mục tiêu:** Phiếu thu ghi principal/branch và debt có cơ chế row lock cho transaction sau.
- **Dependency:** `AUTH-03`, `AUDIT-01`, `DG-ADMIN-ACTOR`.
- **Phạm vi:** Bỏ/đối chiếu `maNhanVienThu`, repository `findForUpdate`, stable debt order.
- **Ngoài phạm vi:** UI, idempotency và create payment contract hoàn chỉnh.
- **File dự kiến ảnh hưởng:** `ThanhToanService`, `PhieuThuRequest`, debt repository, staff controller.
- **API/database migration:** Body không có staff ID; index `(reader,status,date,id)`.
- **Test bắt buộc:** `AUTH-004`, `AUTH-005`, lock/concurrent update test.
- **Acceptance criteria:** Actor/branch spoof không hiệu lực; query lock có thứ tự ổn định.
- **Rủi ro:** Lock scope quá rộng gây chặn quầy; chỉ lock debts được phân bổ.
- **Ước lượng tương đối:** M.

### PAY-02 — Search người nợ và debt context (P1)

- **Mục tiêu:** Không tải báo cáo toàn cục; tìm độc giả còn nợ và hiển thị tổng quan/khoản chưa trả.
- **Dependency:** `UI-03`, `API-01`, `AUTH-03`.
- **Phạm vi:** Debtor search, total/count/oldest/borrowing impact, unpaid default, source links.
- **Ngoài phạm vi:** Payment preview/create.
- **File dự kiến ảnh hưởng:** staff debt controllers/services/repositories, `staffApi.js`, payment reader picker.
- **API/database migration:** `/debtors/search`, `/readers/{id}/debt-context`, paged/filter debts; debt indexes.
- **Test bắt buộc:** `PAY-001`, `PAY-002`, PII/branch scope và stable sort.
- **Acceptance criteria:** Paid debt không selectable; không tải toàn bộ độc giả/khoản nợ.
- **Rủi ro:** Độc giả không gắn branch rõ; branch scope tài chính phải theo giao dịch/permission đã chốt.
- **Ước lượng tương đối:** M.

### PAY-03 — Payment preview API (P0/P1)

- **Mục tiêu:** Preview auto oldest-first hoặc manual partial với before/after chính xác.
- **Dependency:** `PAY-01`, `PAY-02`.
- **Phạm vi:** Auto/manual validation, stable tie-break, payment method capability và derived total.
- **Ngoài phạm vi:** Ghi receipt/debt và electronic webhook.
- **File dự kiến ảnh hưởng:** payment preview DTO/service/controller, payment method repository.
- **API/database migration:** `POST /api/staff/payments/preview`; thêm active/capability cho payment method nếu cần.
- **Test bắt buộc:** `PAY-003`–`PAY-006`, VND fraction, amount zero/over remaining.
- **Acceptance criteria:** Manual total bằng allocations; auto order date+ID; preview không thay đổi debt.
- **Rủi ro:** Dư nợ đổi sau preview; create phải đọc/khóa lại.
- **Ước lượng tương đối:** M.

### PAY-04 — Giao diện Thu tự động/Chọn khoản nợ (P1)

- **Mục tiêu:** Thay UI hard-code bằng hai tab rõ, debt context và sticky summary.
- **Dependency:** `PAY-02`, `PAY-03`, `AUTH-04`, `DG-ELECTRONIC-PAYMENT` cho method điện tử.
- **Phạm vi:** Async debtor picker, unpaid list, auto amount, manual allocations, derived total, payment methods API.
- **Ngoài phạm vi:** Commit backend, receipt printing và reversal.
- **File dự kiến ảnh hưởng:** `StaffPaymentsPage.jsx`, payment components/styles, `staffApi.js`.
- **API/database migration:** Consume context/preview/methods; không migration.
- **Test bắt buộc:** `PAY-001`–`PAY-006`, tab switch reset rules, keyboard/accessibility.
- **Acceptance criteria:** Không mã nhân viên/phiếu; không nút sync tổng; paid debt ẩn mặc định.
- **Rủi ro:** Chuyển tab làm mất allocation không báo; cần confirm/reset contract rõ.
- **Ước lượng tương đối:** L.

### PAY-05 — Atomic/idempotent create payment (P0)

- **Mục tiêu:** Khóa debts, tạo receipt server-side, chống double collection và external reference trùng.
- **Dependency:** `PAY-01`, `PAY-03`, `ID-01`, `IDEM-01`, `AUDIT-01`, `DG-ADMIN-ACTOR`, `DG-ELECTRONIC-PAYMENT` cho điện tử.
- **Phạm vi:** Re-read remaining, auto/manual allocate, server actor/ID, idempotency, success state và immutable receipt.
- **Ngoài phạm vi:** Reversal và pending webhook nếu gate chưa chốt.
- **File dự kiến ảnh hưởng:** `ThanhToanService`, payment/debt repositories/entities, controller/DTO.
- **API/database migration:** `POST /api/staff/payments`; unique external reference, unique receipt+debt, check amount >0.
- **Test bắt buộc:** `PAY-007`–`PAY-011`, `REL-001`, concurrent two-counter payment.
- **Acceptance criteria:** Không `paid > incurred`; retry trả cùng receipt; successful receipt không update/delete.
- **Rủi ro:** Constraint lỗi sau nhiều update; validate và lock trước, nhưng vẫn giữ DB guard.
- **Ước lượng tương đối:** L.

### PAY-06 — Kết quả và bản in phiếu thu (P1)

- **Mục tiêu:** Hiển thị receipt bất biến với allocations/balance/method/actor/branch và bản in.
- **Dependency:** `PAY-05`.
- **Phạm vi:** Result lookup, print payload, cash received/change nếu method hỗ trợ, new transaction reset.
- **Ngoài phạm vi:** Email, đối soát ca/quỹ và reversal UI.
- **File dự kiến ảnh hưởng:** payment result/print components, response DTO/result endpoint.
- **API/database migration:** Enriched `GET /api/staff/payments/{id}`; không migration mới.
- **Test bắt buộc:** receipt refresh, print snapshot, PII masking, amount/allocation equality.
- **Acceptance criteria:** Bản in lấy từ persisted receipt, không từ preview/form state.
- **Rủi ro:** Dữ liệu method-specific nhạy cảm; chỉ hiển thị reference đã mask.
- **Ước lượng tương đối:** M.

### PAY-07 — Reversal request và maker-checker backend (P0)

- **Mục tiêu:** Thủ thư tạo yêu cầu, người khác có quyền phê duyệt/từ chối; approval tạo compensating entry và phục hồi debt atomic.
- **Dependency:** `PAY-05`, `AUDIT-01`, quyết định maker-checker đã chốt.
- **Phạm vi:** Request/approve/reject states, separation of duties, reason, approval, no double reversal.
- **Ngoài phạm vi:** UI librarian/admin và gateway refund điện tử chưa chốt.
- **File dự kiến ảnh hưởng:** reversal entities/repositories/service/controllers/security permissions, migration.
- **API/database migration:** Bảng reversal/request/allocations; `/reversal-requests`, admin approve/reject hoặc contract tương đương.
- **Test bắt buộc:** `PAY-012`, requester tự approve bị chặn, concurrent approval, restored debt status.
- **Acceptance criteria:** Phiếu gốc không sửa/xóa; một receipt chỉ đảo thành công một lần; audit có requester/approver.
- **Rủi ro:** Receipt đã được reversal một phần/điện tử; release đầu chỉ cho full reversal nếu chưa chốt partial.
- **Ước lượng tương đối:** L.

### PAY-08 — UI thủ thư yêu cầu đảo phiếu (P1)

- **Mục tiêu:** Cho thủ thư gửi yêu cầu đảo từ chi tiết phiếu thu mà không tự thay đổi công nợ.
- **Dependency:** `PAY-07`, `PAY-06`.
- **Phạm vi:** Action yêu cầu, reason bắt buộc, xác nhận, trạng thái yêu cầu và liên kết phiếu gốc.
- **Ngoài phạm vi:** Màn phê duyệt admin, chỉnh sửa receipt, partial reversal và cash reconciliation.
- **File dự kiến ảnh hưởng:** staff receipt detail/action, staff API client, dialog/status components.
- **API/database migration:** Consume create/get reversal-request API; không migration.
- **Test bắt buộc:** permission, duplicate request, receipt đã đảo, reason rỗng và trạng thái chờ.
- **Acceptance criteria:** Gửi yêu cầu không đổi allocation/debt; thủ thư không có action tự approve.
- **Rủi ro:** Frontend ẩn nút không đủ; backend vẫn phải enforce maker-checker.
- **Ước lượng tương đối:** M.

### PAY-09 — UI admin/quản lý phê duyệt đảo phiếu (P1)

- **Mục tiêu:** Cung cấp màn hình riêng để người có quyền khác requester phê duyệt hoặc từ chối.
- **Dependency:** `PAY-07`.
- **Phạm vi:** Danh sách yêu cầu chờ, chi tiết phiếu/allocation/debt trước-sau, approve/reject và reason.
- **Ngoài phạm vi:** Tạo yêu cầu phía thủ thư, sửa receipt, partial reversal và gateway refund.
- **File dự kiến ảnh hưởng:** admin reversal page/routes, admin API client, approval detail components.
- **API/database migration:** Consume list/detail/approve/reject APIs; không migration.
- **Test bắt buộc:** role routing, requester tự approve bị chặn, stale/already processed state, reject reason.
- **Acceptance criteria:** Approval thành công cập nhật trạng thái và dữ liệu debt từ response persisted; không optimistic UI tài chính.
- **Rủi ro:** Hai quản lý xử lý đồng thời; backend conflict phải được UI hiển thị rõ.
- **Ước lượng tương đối:** M.

## 14. Epic J — Bulk, barcode, export và RFID

### OPS-01 — Bulk action framework (P2)

- **Mục tiêu:** Backend nhận selected IDs hoặc filtered query+exclusions và trả kết quả từng record.
- **Dependency:** `UI-02`, `AUTH-03`, `AUDIT-01`, state services.
- **Phạm vi:** Generic scope parser, permission/branch revalidation, per-item result và size/job policy.
- **Ngoài phạm vi:** Bật bulk đồng thời trên books/copies/readers; mỗi màn hình là task riêng sau này.
- **File dự kiến ảnh hưởng:** bulk DTO/service framework, query filter validators, audit.
- **API/database migration:** Generic/internal bulk contract; job table nếu chọn async cho tập lớn.
- **Test bắt buộc:** `BULK-001`–`BULK-003`, query tampering và exclusions.
- **Acceptance criteria:** Không tải mọi ID; không thao tác ngoài quyền/query; partial result không bị che.
- **Rủi ro:** Transaction một lô quá lớn; quy định chunk/job và semantics partial rõ.
- **Ước lượng tương đối:** L.

### OPS-02 — In nhãn barcode cuốn sách (P2)

- **Mục tiêu:** Preview/in một hoặc nhiều nhãn mà không đổi định danh.
- **Dependency:** `COPY-04`, `COPY-05`, `OPS-01` nếu in all-matching.
- **Phạm vi:** Template, one/selected/batch, reprint reason/history tối thiểu và printer UAT.
- **Ngoài phạm vi:** RFID và import barcode nhà cung cấp.
- **File dự kiến ảnh hưởng:** copy label components/endpoints, print styles, optional print history migration.
- **API/database migration:** Print payload/label endpoint; print event nếu cần audit vận hành.
- **Test bắt buộc:** `CODE-001`, `CODE-002`, scan nhãn sau move location/branch.
- **Acceptance criteria:** Barcode ổn định/unique; QR không chứa PII hoặc trạng thái mutable.
- **Rủi ro:** Kích thước/máy in khác nhau; cần danh sách thiết bị hỗ trợ.
- **Ước lượng tương đối:** M.

### OPS-03 — Export theo scope và quyền (P2)

- **Mục tiêu:** Tạo framework export current page/selected/all matching với branch và PII allowlist.
- **Dependency:** `OPS-01`, page/filter contracts của màn hình được export.
- **Phạm vi:** Scope parser, streaming/async job, file safety, audit và adapter interface.
- **Ngoài phạm vi:** Bật nút hoặc triển khai adapter cho đầu sách, cuốn sách hay độc giả.
- **File dự kiến ảnh hưởng:** export service/controller, UI action dùng chung, job/storage nếu tập lớn.
- **API/database migration:** Export endpoint/job table tùy kích thước; audit export nhạy cảm.
- **Test bắt buộc:** Unit/integration scope, CSV injection, branch/column allowlist và job authorization.
- **Acceptance criteria:** Adapter giả lập xuất đúng snapshot/filter/scope; không rò cột hoặc branch ngoài allowlist.
- **Rủi ro:** Export lớn gây memory pressure; dùng streaming/async job.
- **Ước lượng tương đối:** L.

### OPS-04 — Export màn đầu sách (P2)

- **Mục tiêu:** Cho màn đầu sách xuất current page/selected/all matching theo đúng filter.
- **Dependency:** `OPS-03`, `BOOK-01`, `UI-02`.
- **Phạm vi:** Adapter/cột catalog, action UI và filename/metadata filter.
- **Ngoài phạm vi:** Export cuốn sách, độc giả hoặc dữ liệu PII.
- **File dự kiến ảnh hưởng:** `BooksPage.jsx`, books export adapter/controller và API client.
- **API/database migration:** Books export endpoint/adapter; không migration mới.
- **Test bắt buộc:** `EXPORT-001`, selected/excluded IDs, filter/sort snapshot và CSV injection.
- **Acceptance criteria:** File chỉ chứa đầu sách đúng scope và cột được công bố; không tải toàn bộ vào trình duyệt.
- **Rủi ro:** Tác giả/thể loại nhiều-nhiều làm nhân bản dòng; serializer phải gom đúng.
- **Ước lượng tương đối:** M.

### OPS-05 — Export màn cuốn sách (P2)

- **Mục tiêu:** Cho màn cuốn sách xuất dữ liệu trong allowed branches theo scope selection.
- **Dependency:** `OPS-03`, `COPY-02`, `UI-02`.
- **Phạm vi:** Adapter/cột inventory, branch/state/location filters và action UI.
- **Ngoài phạm vi:** Export đầu sách, độc giả và lịch sử lưu thông chi tiết.
- **File dự kiến ảnh hưởng:** `BookCopiesPage.jsx`, copies export adapter/controller và API client.
- **API/database migration:** Copies export endpoint/adapter; không migration mới.
- **Test bắt buộc:** `EXPORT-001`, `EXPORT-002` phần branch, all-matching/exclusions và barcode column policy.
- **Acceptance criteria:** Không xuất cuốn ngoài allowed branches dù sửa payload; trạng thái/label phản ánh snapshot.
- **Rủi ro:** Export barcode có thể bị dùng sai; permission và audit phải rõ.
- **Ước lượng tương đối:** M.

### OPS-06 — Export màn độc giả (P2)

- **Mục tiêu:** Cho màn độc giả xuất tập dữ liệu tối thiểu theo permission PII và filter đã áp dụng.
- **Dependency:** `OPS-03`, `READER-02`, `UI-02`.
- **Phạm vi:** Adapter reader, column allowlist theo role, action UI và audit export nhạy cảm.
- **Ngoài phạm vi:** Export mật khẩu, token, địa chỉ/ngày sinh nếu không có quyền riêng.
- **File dự kiến ảnh hưởng:** `ReadersPage.jsx`, readers export adapter/controller, permission config và API client.
- **API/database migration:** Readers export endpoint/adapter; không migration mới.
- **Test bắt buộc:** `EXPORT-001`, `EXPORT-002`, PII masking/omission, selected/all-matching.
- **Acceptance criteria:** File không chứa secret hoặc PII ngoài allowlist; audit ghi actor, filter, cột và số dòng.
- **Rủi ro:** Export PII là rủi ro cao; nên giới hạn quyền và retention file.
- **Ước lượng tương đối:** L.

### OPS-07 — RFID discovery, chưa implementation (P2)

- **Mục tiêu:** Chỉ tạo backlog implementation sau proof-of-concept thiết bị và decision `DG-RFID`.
- **Dependency:** `DG-RFID`.
- **Phạm vi:** Device matrix, tag model, SDK/protocol, privacy, anti-theft và inventory acceptance.
- **Ngoài phạm vi:** Thêm field RFID hoặc giả lập RFID bằng barcode trước khi gate pass.
- **File dự kiến ảnh hưởng:** Tài liệu ADR/PoC riêng; chưa sửa production source/schema.
- **API/database migration:** Chưa có cho tới khi PoC được duyệt.
- **Test bắt buộc:** `RFID-001`, scan/read/write lab tests theo thiết bị thật.
- **Acceptance criteria:** Có thiết bị, vendor/protocol và UAT cụ thể trước khi estimate implementation.
- **Rủi ro:** Vendor lock-in, privacy và thiết bị không tương thích; hiện chưa thể estimate đáng tin.
- **Ước lượng tương đối:** L cho discovery; implementation ước lượng lại.

## 15. Epic K — Quality gate và phát hành

### QA-01 — Security regression suite (P0)

- **Mục tiêu:** Tự động hóa kiểm thử actor/branch/IDOR/token/state/fine/payment/PII cho các task đã hoàn tất.
- **Dependency:** Các P0 liên quan của epic B, F, G, H, I.
- **Phạm vi:** API/security integration tests, abuse payloads và permission matrix.
- **Ngoài phạm vi:** Sửa lỗi phát hiện; tạo bug task riêng theo màn hình.
- **File dự kiến ảnh hưởng:** `backend/src/test/**`, frontend route tests, security fixtures.
- **API/database migration:** Không.
- **Test bắt buộc:** `AUTH-*`, `PASS-*`, `COPY-007`, `LOAN-007`, `RETURN-008/009`, `PAY-007/011/012`, `AUDIT-*`.
- **Acceptance criteria:** Mỗi lỗ P0 có regression test thất bại trên behavior cũ và pass trên behavior mới.
- **Rủi ro:** Test role-only bỏ sót branch/permission; matrix phải gồm allowed/disallowed branch.
- **Ước lượng tương đối:** L.

### QA-02 — Concurrency và idempotency suite (P0)

- **Mục tiêu:** Chứng minh các transaction cạnh tranh không tạo dữ liệu partial hoặc trừ/đổi trạng thái hai lần.
- **Dependency:** `LOAN-05`, `RETURN-05`, `PAY-05`, `PAY-07`.
- **Phạm vi:** Concurrent borrow, return, collect, reverse; same/different idempotency key; rollback/deadlock retry.
- **Ngoài phạm vi:** Load test giao diện.
- **File dự kiến ảnh hưởng:** backend integration/concurrency tests và SQL verification helpers.
- **API/database migration:** Không.
- **Test bắt buộc:** `LOAN-008`–`LOAN-010`, `RETURN-010/011`, `PAY-008`–`PAY-010/012`, `PERF-004`, `REL-001`.
- **Acceptance criteria:** Chỉ một transaction thắng; transaction thua trả conflict/result ổn định; không orphan/overpayment.
- **Rủi ro:** H2 không mô phỏng SQL Server lock; phải chạy trên SQL Server test thật.
- **Ước lượng tương đối:** L.

### QA-03 — Performance và query-plan gate (P1)

- **Mục tiêu:** Xác nhận list/search/context không `findAll`, N+1 hoặc scan bất hợp lý ở dữ liệu mục tiêu.
- **Dependency:** `BOOK-01`, `COPY-02`, `READER-02`, `LOAN-02`, `PAY-02`.
- **Phạm vi:** Seed dữ liệu quy mô, query count, execution plan, latency budget và index review.
- **Ngoài phạm vi:** Tối ưu tính năng chưa hoàn tất.
- **File dự kiến ảnh hưởng:** performance tests/fixtures, migration index điều chỉnh theo evidence.
- **API/database migration:** Chỉ thêm/sửa index khi query plan chứng minh cần; ghi rollback.
- **Test bắt buộc:** `PERF-001`–`PERF-004`.
- **Acceptance criteria:** Page 20 không query theo từng row; page size max 100; lock không giữ qua network call.
- **Rủi ro:** Dữ liệu demo quá nhỏ làm kết quả giả; cần volume và phân bố gần production.
- **Ước lượng tương đối:** M.

### QA-04 — E2E/UAT và release gate (P0/P1)

- **Mục tiêu:** Nghiệm thu chuỗi catalog→copy→reader→loan→return→debt→payment→reversal theo quyền/chi nhánh.
- **Dependency:** Tất cả task P0/P1 thuộc release, `QA-01`–`QA-03`.
- **Phạm vi:** E2E-001 đến E2E-004, scanner keyboard, lỗi mạng/retry, print smoke test và migration rehearsal.
- **Ngoài phạm vi:** RFID và các P2 chưa chọn release.
- **File dự kiến ảnh hưởng:** E2E tests, UAT scripts, release checklist; không sửa feature trong task này.
- **API/database migration:** Chạy toàn bộ migration trên snapshot; không tạo migration nghiệp vụ mới.
- **Test bắt buộc:** `E2E-001`–`E2E-004`, build/lint/backend tests và rollback rehearsal.
- **Acceptance criteria:** Không còn P0 mở; tài liệu API/business rule/decision cập nhật; không hard-code production; UAT ký nhận.
- **Rủi ro:** Dùng task QA để sửa trực tiếp nhiều màn hình sẽ phá độc lập; mọi lỗi phải tách bug task đúng owner.
- **Ước lượng tương đối:** L.

## 16. Thứ tự đưa vào sprint

Thứ tự khuyến nghị không đồng nghĩa mọi task phải chạy tuần tự:

1. `BASE-01` → `BASE-02` → đóng các decision gate liên quan.
2. Song song: `AUTH-01/02/03`, `API-01`, `ID-01`, `AUDIT-01`.
3. `UI-01`–`UI-04` sau contract nền; không migrate page trong chính task component.
4. Đầu sách làm pilot: `BOOK-01`–`BOOK-05`.
5. Cuốn sách và độc giả có thể chạy song song sau nền: `COPY-*`, `READER-*`, `PASS-*`.
6. Mượn chỉ bắt đầu khi copy state và borrowing context ổn định: `LOAN-*`.
7. Trả phụ thuộc loan/open-loan và fine rule: `RETURN-*`.
8. Thu tiền phụ thuộc debt semantics từ return: `PAY-*`.
9. `OPS-*` sau core; `QA-*` chạy tăng dần, nhưng `QA-04` là release gate cuối.

## 17. Definition of Done cho mọi task

Một task chỉ hoàn tất khi:

1. Không vượt “Phạm vi” và không lén triển khai nội dung “Ngoài phạm vi”.
2. Dependency đã `Done` hoặc có mock/contract được chấp thuận rõ ràng.
3. Migration có kiểm tra dữ liệu hiện hữu và rollback/roll-forward.
4. API có validation, authorization, error code và compatibility note.
5. Test bắt buộc pass trên môi trường phù hợp; concurrency dùng SQL Server thật khi cần.
6. Acceptance criteria có bằng chứng test/UAT.
7. Audit không chứa secret; actor/branch lấy từ principal/context.
8. Không còn hard-code dữ liệu production hoặc mã test mặc định.
9. `03-api-contracts.md`, `04-acceptance-tests.md` và decision register được cập nhật nếu contract thay đổi.
10. Mỗi task là một PR/checkpoint độc lập; lỗi ngoài task được ghi thành backlog item mới.

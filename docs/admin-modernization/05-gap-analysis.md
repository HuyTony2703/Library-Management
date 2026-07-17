                                                                                            
                                                                                            
                                                                                            
                                                                                            # Gap analysis cho admin modernization

## 1. Phạm vi và phương pháp kiểm tra

Tài liệu này đối chiếu các yêu cầu trong `docs/admin-modernization/00-requirements.md` đến `04-acceptance-tests.md` với implementation hiện có tại thời điểm **2026-06-20**.

Kết luận không được suy ra từ tên file. Việc đánh giá dựa trên việc đọc:

- Component, page, API client và auth context của frontend.
- Controller, DTO, service, repository, entity, security filter, token service và exception handler của backend.
- DDL, constraint, index, view và các script bổ sung trong `database/scripts/`.
- Cấu hình JPA và bộ kiểm thử hiện có.

Quy ước trạng thái:

- **ĐẦY ĐỦ**: implementation hiện tại đáp ứng yêu cầu và có bảo vệ phù hợp ở các tầng liên quan.
- **MỘT PHẦN**: đã có logic hoặc dữ liệu nền có thể tái sử dụng, nhưng chưa đáp ứng toàn bộ luồng, quyền, hiệu năng hoặc tiêu chí nghiệm thu.
- **CHƯA CÓ**: chưa có implementation đáp ứng yêu cầu; một field hoặc bảng đơn lẻ không được xem là đã hỗ trợ tính năng.
- **XUNG ĐỘT**: code/schema hiện tại cho phép hoặc bắt buộc hành vi trái với đặc tả.

Đây là tài liệu phân tích. Không có source code hoặc database nào được thay đổi khi lập tài liệu này.

## 2. Tóm tắt mức độ đáp ứng

| Nhóm yêu cầu | Trạng thái tổng thể | Phần đã có đáng kể | Khoảng trống chính |
|---|---|---|---|
| DataTable dùng chung | MỘT PHẦN | Render bảng, local pagination, trạng thái rỗng, action column | Server pagination, filter/sort, URL state, selection mode, page size, go-to-page, wrap tên, accessibility |
| AsyncEntityPicker | CHƯA CÓ | Một số option endpoint dạng danh sách | Không có combobox bất đồng bộ, search endpoint phân trang, scan exact, debounce/cancel |
| Đầu sách | MỘT PHẦN | CRUD, ISBN unique, tác giả/thể loại nhiều-nhiều, soft hide/restore | Paged list, filter/sort, picker tên, drawer, lý do lifecycle, audit, hard-delete guard |
| Cuốn sách | MỘT PHẦN | CRUD, barcode/QR unique, danh mục chi nhánh/vị trí/trạng thái | Batch create, server-generated ID/barcode, dependent location, state machine, lịch sử trạng thái, branch scope |
| Độc giả | MỘT PHẦN | CRUD, gói hiện tại, trạng thái cơ bản, soft deactivate/restore | Paged list, drawer, lock scope/reason, derived expiry, renew card, obligation guard, reset password độc giả |
| Staff context và quyền chi nhánh | MỘT PHẦN | Token có `maNhanVien`, role-based route security, nhân viên có một chi nhánh | Không có staff context chuẩn, backend vẫn tin actor/branch từ request, không enforce branch scope |
| Mượn sách | MỘT PHẦN | Giao dịch nhiều cuốn, transaction, quota/debt/overdue checks, pessimistic copy lock | Picker/cart UI đúng nghĩa, preview, actor từ principal, barcode lookup, idempotency, reservation handoff |
| Trả sách | MỘT PHẦN | Backend nhận nhiều chi tiết, tính phạt trễ, tạo nợ, unique chống trả lại | UI chỉ một cuốn, không preview, phạt hỏng/mất do client nhập, thiếu lock/idempotency, không reservation handoff |
| Thu tiền | MỘT PHẦN | Auto allocation và manual partial allocation đã hoạt động | UI hai tab/context, actor từ principal, lock khoản nợ, idempotency, external-reference unique, reversal |
| Barcode/QR | MỘT PHẦN | Field và unique filtered index đã có | Không lookup exact, scan workflow, sinh/in tem, checksum/format/opaque payload policy |
| RFID | CHƯA CÓ | Không có implementation liên quan | Chưa chốt phần cứng, chuẩn tag, reader SDK, privacy và quy trình vận hành |
| Audit log | MỘT PHẦN | Bảng log, service và endpoint admin xem log | Không bao phủ CRUD/state/reset; log dạng text, không before/after/reason/branch/correlation; transaction semantics chưa đúng đặc tả |
| Bulk action/export/print | CHƯA CÓ hoặc MỘT PHẦN rất nhỏ | Frontend lặp từng DELETE; modal kết quả nghiệp vụ | Không có bulk contract phía server, all-matching, export theo scope, in phiếu/tem chuẩn |
| Automated acceptance tests | CHƯA CÓ | Một test `contextLoads()` | Không có unit/integration/concurrency/security/E2E cho các tiêu chí trong `04-acceptance-tests.md` |

Không có nhóm màn hình lớn nào hiện đạt **ĐẦY ĐỦ** theo toàn bộ đặc tả. Tuy nhiên, một số invariant nhỏ đã được hỗ trợ đầy đủ và nên giữ lại:

- ISBN khác null là duy nhất bằng `UX_DAUSACH_ISBN`.
- Barcode và QR khác null là duy nhất bằng `UX_CUONSACH_MAVACH` và `UX_CUONSACH_QRCODE`.
- Một cuốn không thể có hai chi tiết mượn đang mở nhờ `UX_CTPM_CUONSACH_DANGMUON`.
- Một chi tiết mượn không thể được gắn vào hai chi tiết trả nhờ `CHITIETPHIEUTRA.MaChiTietMuon UNIQUE`.
- Backend trả sách đã nhận danh sách nhiều item trong một request.
- Backend thu tiền đã hỗ trợ cả phân bổ tự động và phân bổ thủ công/từng phần.
- Các nghiệp vụ mượn, trả, thu tiền chính đều chạy trong transaction; riêng mức bảo vệ đồng thời khác nhau và được phân tích bên dưới.

## 3. Nền tảng dùng chung

### 3.1 DataTable

**Trạng thái: MỘT PHẦN**

#### Frontend hiện có

- `frontend/src/components/DataTable.jsx` nhận toàn bộ `rows`, chia trang bằng `slice()` trong trình duyệt và mặc định chỉ hiển thị 6 dòng.
- Component chỉ có nút trang trước/sau; không có chọn page size, số trang, đầu/cuối hoặc “Đi đến trang”.
- Component không quản lý filter, sort, loading, request race, URL state hay selection mode.
- `frontend/src/index.css` dùng `table-layout: fixed`; mọi `th/td` mặc định `white-space: nowrap`, `overflow: hidden` và `text-overflow: ellipsis`. Đây là nguyên nhân trực tiếp làm tên đầu sách/họ tên bị cắt.
- Checkbox được mỗi page tự chèn thành một column và luôn chiếm chỗ. Không có nút bật/tắt “Chọn hàng loạt”.
- “Chọn tất cả” ở các page hiện chọn toàn bộ mảng đã lọc phía client, không phân biệt trang hiện tại với toàn bộ kết quả lọc phía server.

#### Backend/database hiện có

- `DauSachService.getAll()`, `CuonSachService.getAll()` và `DocGiaService.getAll()` gọi `findAll()`.
- Controller trả `List`, không trả page envelope, `totalElements`, `totalPages`, stable sort hay applied filters.
- Các service mapping đầu sách/cuốn sách/độc giả thực hiện truy vấn phụ theo từng row, tạo nguy cơ N+1.
- Schema có một số index nghiệp vụ, nhưng chưa có bộ index đầy đủ theo tổ hợp filter/sort dự kiến của các màn quản trị.

#### Security

- Role-based access đã tồn tại, nhưng chưa có allow-list sort field, giới hạn `size`, branch scope hoặc giới hạn export/bulk theo quyền.
- Nếu chỉ thêm query động mà không allow-list field/sort, implementation có thể tạo rủi ro injection hoặc query đắt; contract mới phải kiểm soát tại backend.

#### Gap so với acceptance criteria

- `TABLE-001` đến `TABLE-012`: chưa đạt, ngoại trừ local pagination cơ bản của `TABLE-002`.
- `PERF-001`, `PERF-002`, `PERF-003`: chưa đạt.
- Cần giữ local mode cho bảng nhỏ, nhưng các màn dữ liệu lớn phải dùng server mode.

#### File liên quan

- Frontend: `frontend/src/components/DataTable.jsx`, `frontend/src/index.css`, `frontend/src/pages/BooksPage.jsx`, `BookCopiesPage.jsx`, `ReadersPage.jsx`.
- Backend: `DauSachController.java`, `CuonSachController.java`, `DocGiaController.java` và ba service/repository tương ứng.
- Database: `database/scripts/01_full_database.sql`, `database/scripts/04_reader_portal_extra.sql`.

### 3.2 AsyncEntityPicker

**Trạng thái: CHƯA CÓ**

- Không có component combobox/entity picker dùng chung trong frontend.
- Các form dùng input mã thô, `<select>` tải toàn bộ option, hoặc danh sách/bảng toàn cục.
- `/api/options/branches`, `/book-locations`, `/book-copy-statuses`, `/reader-groups`, `/membership-plans`, `/payment-methods` có thể tái sử dụng cho danh mục nhỏ.
- `/api/reader/books?keyword=` có SQL tìm theo mã, tên, ISBN, tác giả, thể loại, nhưng endpoint chỉ dành cho reader, không phân trang và trả toàn bộ kết quả. Có thể tái sử dụng ý tưởng/query, không nên cho staff gọi trực tiếp contract reader.
- Chưa có search endpoint cho độc giả, đầu sách và cuốn sách với `q`, `limit`, branch/status scope; chưa có exact barcode lookup.

`PICK-001` đến `PICK-005` đều chưa đạt.

## 4. Quản lý đầu sách

**Trạng thái tổng thể: MỘT PHẦN**

### 4.1 Frontend

Đã có:

- Danh sách, tìm kiếm local, thêm, sửa, ngừng hiển thị, khôi phục và xóa cứng.
- Chọn nhiều dòng và thực hiện xóa bằng cách gọi tuần tự từng endpoint.

Chưa có hoặc xung đột:

- Không có filter trạng thái, thể loại, tác giả, NXB, năm xuất bản; không sort/pagination phía server.
- Cột tên không được ưu tiên 35–40%, không wrap 2–3 dòng; CSS chung cắt ellipsis.
- Không có tùy chỉnh cột hoặc drawer chi tiết.
- Form nhập mã NXB và chuỗi mã tác giả/thể loại phân cách dấu phẩy, buộc thủ thư nhớ mã.
- Form tạo sinh mã bằng `Date.now()` ở frontend và chứa dữ liệu mẫu/hard-code. Điều này trái `BR-ID-01` và `BOOK-002`.
- Checkbox luôn hiện; bulk delete chỉ là vòng lặp request, không có kết quả từng item hoặc all-matching contract.

### 4.2 Backend

Đã có:

- CRUD, validation tồn tại NXB/tác giả/thể loại, năm xuất bản, số trang, trị giá.
- Kiểm tra ISBN trùng tại service và unique index tại database.
- Quan hệ nhiều-nhiều tác giả và thể loại.
- Soft hide/restore và hard delete.

Chưa có hoặc xung đột:

- `GET /api/books` trả toàn bộ dữ liệu và mapping gây N+1.
- Response chỉ trả danh sách mã tác giả/thể loại, thiếu tên và copy summary cho drawer/list.
- Không normalize/check checksum ISBN; không phát hiện gần trùng khi không có ISBN.
- Mã đầu sách do client cung cấp.
- Disable/restore không nhận lý do, không ghi actor/time, không audit.
- Không kiểm tra đặt trước đang mở khi ẩn đầu sách.
- Hard delete xóa junction trước rồi xóa đầu sách; không có preflight rõ ràng, lý do, quyền admin-only hoặc thông báo dependency thân thiện.

### 4.3 Database

Đã có:

- PK đầu sách; unique filtered ISBN; FK NXB; junction table tác giả/thể loại; check năm, trang, trị giá, trạng thái.

Cần migration:

- Index phục vụ `status + normalized title`, `publisher + year`, và reverse index cho junction theo tác giả/thể loại.
- Nếu cần tìm không dấu/case-insensitive ổn định: cột/search strategy hoặc full-text index theo quyết định kỹ thuật.
- Lifecycle/audit event hoặc trường/history cho deactivate/reactivate với reason/actor/time.
- `row_version` nếu chốt optimistic locking.
- Cơ chế cấp mã phía server nếu không chỉ dùng UUID/sequence ứng dụng.

### 4.4 Security và acceptance

- Security hiện cho cả librarian và admin gọi hard delete qua cùng `/api/books/**`; trái yêu cầu hard delete là ngoại lệ có quyền cao.
- Không có branch issue trực tiếp với bản ghi thư mục, nhưng export/bulk và PII audit vẫn cần quyền.
- `BOOK-001`, `003`–`009` chưa đạt đầy đủ; `BOOK-004` chỉ đạt phần uniqueness, chưa đạt normalize/checksum; `BOOK-002` không đạt.

### 4.5 File liên quan

- Frontend: `frontend/src/pages/BooksPage.jsx`, `frontend/src/components/DataTable.jsx`, `frontend/src/api/libraryApi.js`, `frontend/src/index.css`.
- Backend: `DauSachController.java`, `DauSachService.java`, `DauSachRequest.java`, `DauSachResponse.java`, `DauSachRepository.java`, repository tác giả/thể loại/NXB.
- Database: các bảng `DAUSACH`, `DAUSACH_TACGIA`, `DAUSACH_THELOAI`, `TACGIA`, `THELOAI`, `NHAXUATBAN` trong `01_full_database.sql`.

## 5. Quản lý cuốn sách

**Trạng thái tổng thể: MỘT PHẦN**

### 5.1 Frontend

- Có danh sách, local search, CRUD, soft/hard delete, restore và option trạng thái.
- Chưa có filter tối thiểu: trạng thái, chi nhánh, khoảng ngày nhập, đầu sách, vị trí, có/chưa barcode; chưa có preset.
- Form nhập tay mã đầu sách, chi nhánh và vị trí. API option đã tồn tại nhưng page chưa sử dụng đầy đủ.
- Không có dependent location theo chi nhánh, batch create, server preview mã, drawer/history hoặc barcode printing.
- Form sửa cho phép chọn mọi trạng thái, gồm `TT_DANGMUON` và `TT_DANGDATTRUOC`; đây là xung đột trực tiếp với state transition rule.
- Frontend đặt `TT_SANCO` khi tạo, nhưng đây không phải bảo vệ vì client khác có thể gửi trạng thái khác.

### 5.2 Backend

Đã có:

- CRUD; kiểm tra tồn tại đầu sách, chi nhánh, vị trí, trạng thái.
- Kiểm tra barcode/QR trùng.
- `CuonSachRepository.findByIdForUpdate()` đã có và được nghiệp vụ mượn sử dụng.

Chưa có hoặc xung đột:

- `GET /api/book-copies` dùng `findAll()` và mapping N+1.
- Create nhận mã cuốn và trạng thái từ client; API có thể tạo trực tiếp cuốn “đang mượn/đặt trước”.
- Update cho đổi tự do đầu sách, chi nhánh, vị trí và trạng thái, không state machine, reason, actor hay audit.
- Chỉ kiểm tra vị trí tồn tại; không kiểm tra vị trí thuộc chi nhánh đã chọn.
- Không kiểm tra đầu sách/chi nhánh đang hoạt động khi tạo.
- Disable có thể chuyển cuốn đang mượn/được giữ sang ngừng lưu thông mà không xử lý nghĩa vụ.
- Restore luôn đưa về sẵn có mà không xét reservation/loan.
- Chưa có batch create, ID/barcode allocation, transfer workflow hoặc condition event endpoint.

### 5.3 Database

Đã có:

- Barcode/QR unique filtered; FK đến đầu sách, chi nhánh, vị trí và trạng thái.
- Vị trí có chuỗi `VITRISACH -> KESACH -> KHU -> CHINHANH`.

Khoảng trống:

- `CUONSACH.MaChiNhanh` và chi nhánh suy ra từ vị trí có thể không khớp; schema chưa có constraint/trigger bảo đảm consistency.
- Không có lịch sử trạng thái, lý do tình trạng, người thao tác, thời điểm, transfer/in-transit.
- Không có sequence/counter bảo đảm batch concurrent không trùng mã.
- Không có RFID tag field/unique index.
- Cần index theo các facet `MaChiNhanh`, `MaTrangThai`, `NgayNhapSach`, `MaDauSach`, `MaViTri`; index reader portal hiện chỉ hỗ trợ một phần theo đầu sách/trạng thái/ngày.

### 5.4 Security và acceptance

- Không enforce branch scope; thủ thư có thể xem/sửa cuốn chi nhánh khác nếu biết mã.
- Hard delete không admin-only và không preflight lịch sử.
- `COPY-001`–`COPY-005`, `COPY-007`–`COPY-010` chưa đạt; `COPY-006` đạt uniqueness ở service/database nhưng UI chưa có luồng batch/manual chuẩn.

### 5.5 File liên quan

- Frontend: `BookCopiesPage.jsx`, `libraryApi.js`, `DataTable.jsx`, `index.css`.
- Backend: `CuonSachController.java`, `CuonSachService.java`, `CuonSachRequest.java`, `CuonSachRepository.java`, `OptionController.java`.
- Database: `CUONSACH`, `TRANGTHAICUONSACH`, `CHINHANH`, `KHU`, `KESACH`, `VITRISACH`.

## 6. Quản lý độc giả và reset mật khẩu

**Trạng thái tổng thể: MỘT PHẦN; reset mật khẩu độc giả: CHƯA CÓ**

### 6.1 Frontend

- Có CRUD, local search, chọn nhóm/gói, thay gói, soft deactivate/restore và hard delete.
- Họ tên vẫn bị nowrap/ellipsis; không có width 220–280px và wrap.
- Không có filter nhóm/gói/trạng thái/hạn gói/hạn thẻ, preset hoặc sort server-side.
- Không có drawer với hồ sơ, gói, sách mượn, nợ, lịch sử giao dịch.
- Không có lock/unlock có lý do/thời hạn/scope; chỉ có ngừng hoạt động/khôi phục.
- Input mật khẩu bị disable khi sửa, nhưng form vẫn giữ giá trị mẫu và DTO dùng chung vẫn có field mật khẩu. Đây là dấu hiệu contract create/update chưa tách đúng.
- Không có UI reset mật khẩu độc giả.

### 6.2 Backend

Đã có:

- Tạo độc giả đồng thời tạo tài khoản; hash mật khẩu bằng password encoder.
- Kiểm tra duplicate mã, username, email; validation nhóm/gói/tuổi.
- Cập nhật hồ sơ và thay gói; disable/restore/hard delete.
- Admin có endpoint reset mật khẩu **nhân viên thủ thư**, có thể tái sử dụng pattern quyền/audit ở mức ý tưởng.

Chưa có hoặc xung đột:

- List dùng `findAll()` và mapping tài khoản/gói hiện tại theo row gây N+1.
- Update dùng chung `DocGiaRequest`; backend bỏ qua password nhưng contract vẫn buộc frontend mang field không liên quan.
- Thay gói sửa bản ghi lịch sử gói gần nhất thay vì luôn append một event/lịch sử bất biến.
- `TrangThai` độc giả trộn lifecycle, lock và expiry trong một chuỗi; “Hết hạn” không được derive nhất quán từ ngày.
- Disable chỉ đổi `DOCGIA.TrangThai`, không khóa `TAIKHOAN`; token cũ vẫn có thể truy cập các endpoint dựa trên role token.
- Restore đặt thẳng “Hoạt động”, không đánh giá lại ngày hết hạn, lock, nợ hoặc nghĩa vụ.
- Hard delete xóa account/profile/membership, không có retention/anonymization/preflight nghiệp vụ rõ ràng.
- Không có card renewal, lock event/scope, borrowing context hoặc password reset độc giả.

### 6.3 Reset mật khẩu và token

- `TaiKhoan` chỉ có password hash, status, ngày tạo và lần đăng nhập cuối; không có `mustChangePassword`, `passwordChangedAt` hay `tokenVersion`.
- JWT chứa role/reader/staff identity và hết hạn 480 phút. Filter xác thực token mà không re-check trạng thái tài khoản/nhân viên trong database.
- Đổi hoặc reset mật khẩu không thu hồi token đã phát hành.
- Endpoint reset thủ thư hiện nhận mật khẩu mới do admin nhập; không có mật khẩu tạm một lần, force-change, rate limit hoặc reason bắt buộc.
- Vì vậy `PASS-001`–`PASS-005` đều chưa đạt cho độc giả; code reset thủ thư chỉ là nền tham khảo, không phải implementation đáp ứng yêu cầu.

### 6.4 Database migration cần thiết

- `TAIKHOAN`: `MustChangePassword`, `PasswordChangedAt`, `TokenVersion` hoặc session/token revocation table.
- Bảng reader lock/state event: scope, reason, start/end, actor, status, reversal/unlock reference.
- Bảng/card renewal event hoặc lifecycle event; không ghi đè lịch sử.
- Index độc giả theo họ tên/trạng thái/hạn thẻ; lịch sử gói theo độc giả/trạng thái/ngày kết thúc.
- Retention/anonymization strategy trước khi cho phép hard delete.
- `row_version` nếu chốt optimistic locking.

### 6.5 Security và acceptance

- Role guard tồn tại, nhưng chưa có permission chi tiết cho reset và chưa có branch ownership policy cho độc giả.
- Deactivate độc giả không vô hiệu hóa token/tài khoản là rủi ro P0.
- `READER-001`–`READER-008` chưa đạt đầy đủ; một phần profile/membership CRUD đã có nhưng không đủ state semantics.

### 6.6 File liên quan

- Frontend: `ReadersPage.jsx`, `AuthContext.jsx`, `libraryApi.js`, `adminApi.js`.
- Backend: `DocGiaController.java`, `DocGiaService.java`, `DocGiaRequest.java`, `TaiKhoan.java`, `DocGia.java`, `AuthService.java`, `TokenService.java`, `TokenAuthenticationFilter.java`, `AdminLibrarianService.java`.
- Database: `TAIKHOAN`, `DOCGIA`, `LICHSUGOITHANHVIEN`, `NHOMDOCGIA`, `GOITHANHVIEN`.

## 7. Danh tính nhân viên, quyền và chi nhánh

**Trạng thái: MỘT PHẦN, có xung đột bảo mật P0**

### 7.1 Hiện có

- JWT/AuthUser có `maNhanVien`; `/api/auth/me` trả danh tính cơ bản.
- `NHANVIEN` có một `MaChiNhanh` nullable.
- SecurityConfig phân quyền theo role cho `/api/staff/**`, `/api/admin/**` và các endpoint legacy.
- Nghiệp vụ mượn/trả kiểm tra mã chi nhánh tồn tại và cuốn/phiếu thuộc chi nhánh request ở một số bước.

### 7.2 Khoảng trống và xung đột

- Không có `GET /api/staff/me/context` trả branch mặc định, allowed branches và permissions.
- `MuonSachRequest`, `TraSachRequest`, `PhieuThuRequest` vẫn nhận mã nhân viên từ client; service lưu giá trị đó thay vì bắt buộc lấy từ principal.
- Frontend hard-code `NV_TT001`; kẻ có token librarian có thể giả actor khác nếu mã đó tồn tại.
- Branch cũng do client gửi. Backend không đối chiếu branch với `NHANVIEN.MaChiNhanh` hoặc allowed branches.
- Admin không có staff profile vẫn có role gọi endpoint staff, nhưng behavior tạo giao dịch chưa được định nghĩa.
- Token không re-check account/employee status; một token đã cấp có thể còn hiệu lực sau khi nhân viên/tài khoản bị khóa.
- Frontend ẩn/hiện menu theo role chỉ là UX, không thay thế backend authorization.

### 7.3 Database migration phụ thuộc quyết định

- Nếu một nhân viên chỉ thuộc một chi nhánh: có thể giữ `NHANVIEN.MaChiNhanh`, nhưng cần NOT NULL/exception policy và quyền riêng.
- Nếu nhiều chi nhánh: thêm `NHANVIEN_CHINHANH` với default/allowed flag, effective dates và unique constraint.
- Nếu dùng permission chi tiết: thêm role-permission hoặc staff-permission mapping.
- Các phiếu nên tiếp tục snapshot actor/branch, nhưng các giá trị phải do backend gắn từ context đã authorize.

### 7.4 Acceptance

- `AUTH-001`, `AUTH-004`, `AUTH-005`, `AUTH-006` chưa đạt.
- `AUTH-002` đạt một phần vì reader không được vào `/api/staff/**`, nhưng endpoint context chưa tồn tại.
- `AUTH-003` chỉ đúng ở lần login mới; token đang tồn tại chưa bị thu hồi nên chưa đạt toàn diện.

## 8. Mượn sách

**Trạng thái tổng thể: MỘT PHẦN**

### 8.1 Frontend

- Page hiện là form mã thô, hard-code reader/actor/branch/copy và cho nhập danh sách mã cuốn phân cách dấu phẩy.
- Có hiển thị bảng toàn bộ cuốn “sẵn có” tải từ API và bảng sách độc giả đang mượn.
- Chưa có reader-first picker, thẻ borrowing context, barcode lookup, cart, per-item preview hoặc branch context.
- “Sách đang mượn” chưa thu gọn/cảnh báo ngày trễ đúng vai trò; response hiện cũng thiếu tên sách để trình bày tốt.
- Có result modal sau submit, nhưng chưa có payload in phiếu hoặc reset workflow hoàn chỉnh.

### 8.2 Backend

Đã có mạnh và nên tái sử dụng:

- Tạo một phiếu với nhiều cuốn trong transaction.
- Kiểm tra reader active, hạn thẻ, nợ, sách quá hạn, gói đang dùng và quota.
- Tính hạn trả theo rule/gói/thể loại và cap theo hạn thẻ.
- Khóa bi quan từng cuốn qua `findByIdForUpdate`, kiểm tra branch và `TT_SANCO`, rồi đổi sang đang mượn trong cùng transaction.
- Chặn duplicate copy trong request; database chặn một cuốn có hai khoản mượn đang mở.
- Có endpoint lấy current loans của độc giả.

Chưa có hoặc xung đột:

- Actor và branch tin request client.
- Client tự cấp mã phiếu và chi tiết; không idempotency. Retry sau timeout có thể tạo kết quả không rõ ràng hoặc lỗi duplicate thay vì trả cùng kết quả.
- Không có preview endpoint; UI không biết đầy đủ blocker/due date trước commit.
- Không có reader search, copy search hoặc exact barcode endpoint cho staff.
- Current-loans response thiếu tên đầu sách, barcode, branch name và số ngày trễ.
- Không kiểm tra reservation ownership/handoff khi mượn cuốn đang được giữ.
- Hạn thẻ đúng ngày hiện tại bị xem là hết hạn vì dùng `!expiry.isAfter(today)`; đặc tả phải chốt rõ ngày hết hạn còn dùng được hết ngày hay không.
- Chính sách hiện tại chặn mọi khoản nợ dương và bắt buộc gói active; đây là implementation đang có nhưng vẫn là OPEN DECISION trong đặc tả.

### 8.3 Database

- Có PK/FK/check và unique filtered cho một cuốn đang mượn.
- Có rule version snapshot ở phiếu/chi tiết, hỗ trợ tính nhất quán lịch sử.
- Cần idempotency storage/unique key, index current loans theo reader/status/due date, và index/search barcode.
- Nếu mã do server sinh, cần cơ chế cấp mã an toàn đồng thời.
- Reservation cần constraint/index chống active duplicate và transaction handoff rõ ràng.

### 8.4 Security và acceptance

- `LOAN-008` và phần atomic của `LOAN-009` có nền tốt nhờ transaction + pessimistic copy lock + unique index, nhưng vẫn cần concurrency tests.
- `LOAN-003`, `005`, `006` được backend hỗ trợ một phần/đáng kể.
- `LOAN-001`, `002`, `004`, `007`, `010`, `011` chưa đạt.
- `LOAN-012` chỉ có result modal cơ bản, chưa đạt print/reset contract.

### 8.5 File liên quan

- Frontend: `frontend/src/pages/staff/StaffLoansPage.jsx`, `frontend/src/api/staffApi.js`.
- Backend: `StaffLoanController.java`, `MuonSachController.java` legacy, `MuonSachService.java`, `MuonSachRequest.java`, `CuonSachRepository.java`, các repository phiếu/chi tiết/rule/debt/membership.
- Database: `PHIEUMUON`, `CHITIETPHIEUMUON`, `CUONSACH`, `QUYDINHMUON_THELOAI`, `PHIENBANQUYDINH`, `PHIEUDATTRUOC`.

## 9. Trả sách

**Trạng thái tổng thể: MỘT PHẦN**

### 9.1 Frontend

- Có thể chọn một chi tiết từ bảng sách đang mượn và submit một item.
- Page vẫn yêu cầu nhập reader/actor/branch và mã chi tiết; chưa ưu tiên quét barcode.
- DTO backend hỗ trợ list nhưng UI không có return cart nhiều cuốn.
- Bảng toàn cục “Độc giả đang mượn sách” vẫn nằm trong màn lập phiếu, làm loãng luồng nghiệp vụ.
- Tiền phạt hỏng/mất do thủ thư nhập tự do; không có severity, suggested fine hoặc override reason/permission.

### 9.2 Backend

Đã có mạnh và nên tái sử dụng:

- `TraSachRequest` nhận danh sách item và service xử lý nhiều chi tiết trong một transaction.
- Xác minh cùng độc giả, cùng chi nhánh mượn và chưa trả.
- Tính ngày trễ/tiền trễ từ server time và rule; tạo khoản nợ trễ/hỏng/mất.
- Cập nhật chi tiết mượn, trạng thái cuốn và trạng thái phiếu mượn.
- Unique constraint `MaChiTietMuon` tại chi tiết trả bảo vệ cuối cùng khỏi ghi hai lần.

Chưa có hoặc xung đột:

- Actor/branch/reader vẫn do client gửi; actor có thể bị giả mạo.
- Không có barcode/open-loan lookup hoặc preview endpoint.
- Tiền phạt hỏng/mất do client quyết định; backend chỉ yêu cầu bằng 0 cho bình thường và lớn hơn 0 cho hỏng/mất.
- Không có severity/formula/cap/override permission/reason.
- Service kiểm tra rồi lưu nhưng không khóa bi quan chi tiết mượn/cuốn. Unique DB giảm rủi ro duplicate return, nhưng request cạnh tranh có thể vẫn thực thi cập nhật/nợ trước khi một transaction thất bại; cần test và lock rõ ràng.
- Không idempotency; retry không trả lại kết quả cũ.
- Cuốn bình thường được đưa thẳng về sẵn có, không xét hàng đợi đặt trước.
- Chỉ hỗ trợ trả cùng chi nhánh mượn; đây là behavior hiện tại, trong khi cross-branch return là OPEN DECISION.

### 9.3 Database migration

- Giữ unique `CHITIETPHIEUTRA.MaChiTietMuon`.
- Thêm idempotency key/unique request key.
- Thêm damage assessment/condition event: severity, base amount, suggested amount, override delta/reason/approver.
- Thêm index open-loan lookup theo barcode/copy/status.
- Nếu hỗ trợ trả khác chi nhánh: snapshot receiving branch, ownership branch, transit/transfer event.
- Reservation handoff/history nếu cuốn sau trả được giữ cho người kế tiếp.

### 9.4 Security và acceptance

- `RETURN-003`, `005`, `006`, `012`, `013` có logic backend một phần.
- `RETURN-002` có check service + unique DB nhưng chưa có idempotent response.
- `RETURN-001`, `004`, `007`, `008`, `011` chưa đạt; `009` blocked by decision; `010` có backend multi-item/transaction nhưng UI và concurrency guard chưa đủ.

### 9.5 File liên quan

- Frontend: `StaffReturnsPage.jsx`, `staffApi.js`.
- Backend: `StaffReturnController.java`, `TraSachController.java`, `TraSachService.java`, `TraSachRequest.java`, `ChiTietPhieuTraRepository.java`, `ChiTietPhieuMuonRepository.java`.
- Database: `PHIEUTRA`, `CHITIETPHIEUTRA`, `CHITIETPHIEUMUON`, `CUONSACH`, `KHOANNO`, `PHIEUDATTRUOC`.

## 10. Thu tiền

**Trạng thái tổng thể: MỘT PHẦN; core allocation đã có**

### 10.1 Frontend

- Có chọn độc giả, load khoản nợ, chọn khoản/từng số tiền và chế độ tự phân bổ.
- Chưa tổ chức thành hai tab “Thu tự động” và “Chọn khoản nợ”.
- Tìm độc giả còn nợ dựa vào báo cáo/toàn bộ dữ liệu thay vì async debtor search.
- Khoản đã thanh toán không ẩn mặc định; chưa có debt context/tổng quan rõ ràng và sticky receipt summary.
- Phương thức thanh toán bị hard-code dù option API đã có.
- Mã nhân viên hard-code và cho sửa.
- Client tự tạo mã phiếu; chưa có preview, idempotency, in phiếu chuẩn hoặc reversal.

### 10.2 Backend

Đã có đầy đủ ở phạm vi hẹp:

- Manual allocation: chọn khoản nợ và số tiền áp dụng, hỗ trợ thanh toán một phần.
- Auto allocation: phân bổ vào khoản cũ trước theo `NgayPhatSinh ASC`.
- Kiểm tra khoản nợ thuộc độc giả, không trùng trong request, số tiền không vượt dư nợ và tổng allocation bằng số thu.
- Lưu phiếu, chi tiết phân bổ và cập nhật trạng thái khoản nợ trong transaction.
- Có endpoint phương thức thanh toán, debts, receipt detail và payment history.

Chưa có hoặc xung đột:

- Actor từ request; field còn nullable trong schema.
- Auto ordering không có tie-break ID ổn định khi hai khoản cùng timestamp.
- Không khóa bi quan các khoản nợ khi thu; hai request đồng thời có thể cùng đọc dư nợ cũ. Check constraint ngăn vượt tổng ở database, nhưng không bảo đảm trải nghiệm/idempotent result và có thể rollback muộn.
- Không idempotency, preview, server-generated receipt ID hoặc unique external transaction reference.
- `PHIEUTHU.SoTienThu` database cho phép 0 (`>= 0`) trong khi nghiệp vụ thực tế cần `> 0`.
- Payment method table không có active status; API trả tất cả.
- Không có reversal/compensating entry; trạng thái “Đã hủy” tồn tại nhưng không có quy trình phục hồi allocation/debt.
- Không có branch/cash shift snapshot hoặc cash reconciliation.
- Electronic payment pending/webhook behavior chưa được định nghĩa.

### 10.3 Database migration

- Unique filtered index cho `MaGiaoDichNgoai` khi khác null.
- Idempotency key và unique scope theo operation/account.
- Unique `(MaPhieuThu, MaKhoanNo)` ở `CHITIETPHIEUTHU_NO` để bảo vệ duplicate allocation ở DB.
- Check `SoTienThu > 0`; cân nhắc invariant tổng chi tiết bằng tổng phiếu bằng transaction/procedure/service test.
- Reversal table hoặc immutable compensating receipt liên kết phiếu gốc, reason, approver, time.
- Payment method active flag; branch/shift fields nếu có quản lý quỹ.
- Index debt theo `(MaDocGia, TrangThai, NgayPhatSinh, MaKhoanNo)`.

### 10.4 Security và acceptance

- `PAY-003`–`PAY-005` có core backend đáng kể; cần stable tie-break và UI/preview.
- `PAY-006` endpoint có nhưng UI chưa dùng.
- `PAY-001`, `002`, `007`–`014` chưa đạt đầy đủ; `PAY-012`–`014` phụ thuộc OPEN DECISION.

### 10.5 File liên quan

- Frontend: `StaffPaymentsPage.jsx`, `staffApi.js`, `libraryApi.js`.
- Backend: `StaffPaymentController.java`, `ThanhToanController.java`, `ThanhToanService.java`, `PhieuThuRequest.java`, `KhoanNoRepository.java`, `PhieuThuRepository.java`.
- Database: `KHOANNO`, `PHIEUTHU`, `CHITIETPHIEUTHU_NO`, `PHUONGTHUCTHANHTOAN`, `VW_TONGNO_DOCGIA`.

## 11. Barcode, QR, RFID, bulk, export và in

### 11.1 Barcode/QR — MỘT PHẦN

- Schema và entity đã lưu `MaVach`, `MaQRCode`; database đảm bảo unique khi khác null.
- CRUD cuốn sách cho nhập barcode/QR thủ công.
- Chưa có server allocation, format rule, checksum, exact lookup, scanner keyboard workflow, camera scan, label generation/printing hoặc reprint audit.
- QR hiện là chuỗi client gửi, chưa có policy bảo đảm payload chỉ là opaque ID và không chứa PII.
- `CODE-001`, `CODE-002`, `COPY-010` chưa đạt; uniqueness là nền có thể giữ.

### 11.2 RFID — CHƯA CÓ

- Không có field tag, reader integration, inventory workflow, anti-theft integration hoặc privacy policy.
- Không được code RFID trước khi chốt `BR-RFID-01` và `RFID-001`.

### 11.3 Bulk/export/print

- Frontend bulk hiện lặp từng DELETE tuần tự; không có một transaction/job hoặc response per-item chuẩn.
- Không có `selectedIds`/`allMatching + exclusions` contract phía backend.
- Không có export theo filter/selection với PII authorization.
- Result modal nghiệp vụ có thể tái sử dụng UI, nhưng chưa có print-ready payload/template cho phiếu mượn, trả, thu hoặc tem barcode.

## 12. Audit log

**Trạng thái: MỘT PHẦN**

### 12.1 Hiện có

- `NHATKYHOATDONG` lưu account, action, target type/id, server time, IP và mô tả text.
- `ActivityLogService.logSafe()` được gọi trong mượn, trả, thu tiền và một số chức năng admin.
- Endpoint `/api/activity-logs` chỉ admin được truy cập và có giới hạn số lượng.

### 12.2 Khoảng trống/xung đột

- CRUD/lifecycle đầu sách, cuốn sách, độc giả không ghi audit.
- Reset mật khẩu độc giả chưa có; reset thủ thư có log nhưng thiếu reason/force-change/revocation metadata.
- Log không có actor staff ID/role/branch, source, event type chuẩn, before/after structured JSON, reason, correlation/request/idempotency ID.
- `ActivityLogService` dùng `REQUIRES_NEW` và nuốt lỗi. Audit có thể commit độc lập trước khi outer transaction hoàn tất, trái yêu cầu “chỉ commit khi nghiệp vụ commit”; đồng thời lỗi audit không làm thất bại nghiệp vụ nên coverage không được bảo đảm.
- Không có immutability control ngoài việc UI/API không cung cấp sửa; chưa có retention/archive hoặc field-level redaction policy.
- Endpoint trả list giới hạn, chưa có page/filter theo actor/entity/time.

`AUDIT-001`–`AUDIT-004` chưa đạt đầy đủ.

### 12.3 Migration cần thiết

- Mở rộng `NHATKYHOATDONG` hoặc tạo bảng audit event mới với actor account/staff/role, branch, source, event type, entity type/id, reason, before/after, correlation/idempotency ID và server timestamp.
- Index theo time, actor, entity và event type.
- Thiết kế outbox/after-commit hoặc ghi cùng transaction tùy mức bắt buộc; không giữ semantics `REQUIRES_NEW` hiện tại nếu yêu cầu atomic audit.
- Retention, archive, access control và masking phải được chốt trước migration.

## 13. Endpoint hiện có có thể tái sử dụng

“Tái sử dụng” dưới đây có nghĩa là giữ logic/contract phù hợp hoặc mở rộng có kiểm soát; không mặc nhiên coi endpoint cũ đã đạt contract mới.

| Endpoint hiện có | Có thể tái sử dụng | Giới hạn hiện tại |
|---|---|---|
| `GET /api/auth/me` | Nền identity, `maNhanVien`, role | Thiếu branch/allowedBranches/permissions; token state không re-check DB |
| `GET /api/options/branches` | Branch option nhỏ | Chưa scope theo nhân viên; chỉ label/value |
| `GET /api/options/book-locations` | Query join vị trí-kệ-khu | Không nhận `branchId`, không trả branch metadata |
| `GET /api/options/book-copy-statuses` | Danh mục hiển thị | Không phân loại operational/manual transition |
| `GET /api/options/reader-groups` | Picker danh mục nhỏ | Không search/paging, thường chấp nhận được nếu tập nhỏ |
| `GET /api/options/membership-plans` | Picker gói active | Cần giữ effective-policy metadata ở context khác |
| `GET /api/options/payment-methods` | Thay hard-code frontend | Schema chưa có active flag/capability |
| `GET /api/books/{id}` | Load chi tiết cơ bản | Response thiếu tên tác giả/thể loại và copy summary |
| `GET /api/book-copies/{id}` | Load cuốn theo mã | Thiếu title/branch/location labels, transition/history |
| `GET /api/readers/{id}` | Load hồ sơ/gói cơ bản | Thiếu borrowing/debt/state context |
| `GET /api/staff/readers/{id}/current-loans` | Nền cảnh báo mượn/bảng chọn trả | Response chưa enriched; chưa barcode/open-loan search |
| `POST /api/staff/loans` | Core transaction/quota/due-date/copy lock | Phải bỏ actor/client ID, thêm branch auth/idempotency/reservation |
| `POST /api/staff/returns` | Core multi-item/late fine/debt | Phải thêm actor context, lock, preview, damage policy, idempotency |
| `GET /api/staff/readers/{id}/debts` | Load debts | Trả cả paid; thiếu summary/filter/stable order |
| `POST /api/staff/payments` | Core auto/manual allocation | Phải thêm actor, locks, ID/idempotency, external ref/reversal policy |
| `GET /api/staff/payments/{id}` | Result/receipt data nền | Cần print payload, branch/actor snapshot/reversal links |
| `GET /api/reader/books?keyword=` | Tái sử dụng ý tưởng SQL catalog search | Reader-only, không phân trang; không dùng trực tiếp cho staff picker |
| Reader reservation endpoints | Nền reservation lifecycle/locking | Không tích hợp với staff loan/return handoff; thiếu active duplicate DB constraint |
| `GET /api/activity-logs` | Nền màn tra cứu audit | Contract/log schema chưa đủ và chưa phân trang chuẩn |
| `GlobalExceptionHandler` | Nền error envelope | Thiếu `details`, correlation ID và error codes nghiệp vụ chi tiết; handler `RuntimeException` có thể trả message nội bộ |

## 14. Endpoint cần thay đổi hoặc bổ sung

### 14.1 Thay đổi endpoint hiện có

1. `GET /api/books`, `/api/book-copies`, `/api/readers`: thêm page envelope, allow-listed sort, filter, stable tie-break; không còn trả toàn bộ mặc định.
2. `POST/PUT /api/books`: tách create/update DTO, server cấp ID, trả label đầy đủ; normalize ISBN và near-duplicate warning.
3. `DELETE /api/books|book-copies|readers`: bỏ `mode=hard` khỏi thao tác thường; thay lifecycle action có reason. Hard delete admin-only và có preflight.
4. `POST/PUT /api/book-copies`: không nhận operational status tự do; validate branch-location; tách move/transfer/condition action.
5. `PUT /api/readers/{id}`: tách profile khỏi account/password/membership/state.
6. `POST /api/staff/loans|returns|payments`: bỏ/ignore-and-reject actor từ body; derive actor từ principal, authorize branch, server cấp ID, nhận idempotency key.
7. `GET current-loans/debts`: enriched response, filter open/unpaid mặc định và stable order.
8. `/api/options/book-locations`: nhận branch và trả metadata cần thiết.
9. Error contract: thêm stable business error code, details/correlation ID; không lộ raw runtime/database detail.

### 14.2 Endpoint mới tối thiểu

- `GET /api/staff/me/context`.
- `GET /api/staff/books/search?q=&limit=` hoặc contract search catalog dùng chung có role phù hợp.
- `GET /api/staff/readers/search?q=&limit=`.
- `GET /api/staff/book-copies/search?q=&branchId=&status=&limit=`.
- `GET /api/staff/book-copies/by-barcode/{barcode}`.
- `GET /api/options/book-locations?branchId=`.
- `POST /api/books/{id}/deactivate`, `/reactivate`; hard-delete preflight/action admin-only.
- `POST /api/book-copies/batch`.
- `POST /api/book-copies/{id}/move-location`, `/transfer-branch`, `/condition-events`.
- `POST /api/readers/{id}/card-renewals`, `/membership-changes`, `/locks`, `/unlock`, `/deactivate`, `/reactivate`.
- `GET /api/staff/readers/{id}/borrowing-context`.
- `POST /api/readers/{id}/password-reset` theo quyền đã chốt.
- `POST /api/staff/loans/preview`.
- `GET /api/staff/open-loans/by-barcode/{barcode}` và `GET /api/staff/readers/{id}/open-loans`.
- `POST /api/staff/returns/preview`.
- `GET /api/staff/debtors/search` và `GET /api/staff/readers/{id}/debt-context`.
- `POST /api/staff/payments/preview`.
- `POST /api/admin/payments/{id}/reverse` sau khi chốt quyền/phê duyệt.
- Bulk endpoints hỗ trợ selected IDs hoặc all-matching + exclusions.
- Export/print endpoints hoặc print-ready response theo scope đã authorize.

## 15. Kế hoạch database migration cần thiết

### 15.1 Điều kiện nền trước mọi migration

- Backend đang cấu hình `spring.jpa.hibernate.ddl-auto=validate`; Hibernate không tự tạo/đổi schema.
- Repository hiện dùng các file SQL chạy theo thứ tự thủ công; không có Flyway/Liquibase trong `pom.xml` và không có bảng version migration chuẩn.
- Trước P0 nên chọn công cụ/version convention, baseline database hiện có, backup/rollback và test migration trên bản sao có dữ liệu. Các file `04_*`, `05_*` hiện tại là script bổ sung nhưng không thay thế migration ledger tự động.

### 15.2 P0 bắt buộc

1. Token/password revocation fields hoặc session revocation table; `must_change_password` và password timestamps.
2. Staff allowed-branch model theo quyết định một-nhiều hay nhiều-nhiều.
3. Structured audit/event schema và index; chọn transaction/outbox semantics.
4. Idempotency storage/unique constraints cho loan, return, payment.
5. Pessimistic/optimistic concurrency strategy: lock query không nhất thiết cần DDL, nhưng `row_version` cần migration nếu được chọn.
6. Payment external reference unique filtered index; allocation unique constraint; receipt amount constraint.
7. Reader lock/lifecycle/card-renewal history với actor/reason/effective period.
8. Damage/loss assessment và override audit fields/tables nếu triển khai trả sách hiện đại.

### 15.3 P1 hiệu năng và thao tác

1. Index phục vụ page/filter/sort cho books, copies, readers, memberships, open loans, debts, reservations và audit.
2. Server-side ID/barcode allocation bằng sequence/counter/UUID strategy đã chốt.
3. Copy state/location/branch consistency và transition history.
4. Reservation active uniqueness/handoff support.
5. Payment method active/capability metadata.
6. Search normalization/full-text strategy nếu yêu cầu tìm tiếng Việt không dấu ở quy mô lớn.

### 15.4 P2 vận hành

- Barcode label/reprint metadata.
- Cash shift/reconciliation nếu áp dụng.
- Export job/history nếu dữ liệu lớn.
- RFID schema chỉ sau hardware discovery và privacy decision.

## 16. Xung đột nổi bật giữa đặc tả và code/schema

| Đặc tả | Code/schema hiện tại | Mức độ |
|---|---|---|
| Actor giao dịch lấy từ principal | Body nhận và service lưu `maNhanVienLap/Nhan/Thu` | P0 security |
| Branch phải được authorize | Client gửi branch; chỉ kiểm tra tồn tại/quan hệ cuốn, không kiểm tra quyền nhân viên | P0 security |
| Reset mật khẩu phải revoke token/force change | Token sống tối đa 480 phút, không token version; không reader reset | P0 security |
| Operational copy status chỉ do nghiệp vụ | CRUD copy cho client đặt mọi status | P0 business integrity |
| Hỏng/mất có công thức và override kiểm soát | Client nhập số tiền; backend chỉ kiểm tra dấu | P0 financial integrity |
| Payment concurrent/idempotent | Không debt lock/idempotency/external-ref unique | P0 financial integrity |
| Return concurrent/idempotent | Có unique final guard nhưng không row lock/idempotency | P0 reliability |
| Audit commit cùng nghiệp vụ | Audit dùng `REQUIRES_NEW`, best-effort và dạng text | P0 compliance |
| Reader expiry là derived state | Schema/service lưu status chuỗi và restore thẳng Active | P0/P1 business |
| Lifecycle action có reason/actor/history | Soft delete/restore trực tiếp, không reason/audit | P0/P1 |
| Server pagination/filter/sort | `findAll()` + client slice | P1 scalability |
| Tên được wrap | CSS toàn cục nowrap/ellipsis | P1 usability |
| Location phụ thuộc branch | Option trả toàn bộ; CRUD không validate ownership | P0/P1 integrity |
| ID do server cấp | Nhiều form dùng `Date.now()`/hard-code, request yêu cầu ID | P0/P1 reliability |
| Payment method lấy API | Staff payment page hard-code | P1 consistency |
| Return cart nhiều item | DTO/service hỗ trợ list nhưng UI gửi đúng một item | P1 UI gap |
| Auto/manual payment thành hai mode rõ | Core backend có nhưng UI chưa tách luồng | P1 UI gap |
| Migration có version/repeatability test | SQL thủ công, không migration framework | P0 delivery risk |

## 17. Rủi ro bảo mật và nghiệp vụ hiện tại

### 17.1 Critical/High

1. **Giả mạo nhân viên trên phiếu:** librarian có thể gửi mã nhân viên khác tồn tại; dữ liệu phiếu và audit attribution sai.
2. **Vượt phạm vi chi nhánh:** không có backend branch authorization cho CRUD cuốn và giao dịch.
3. **Token không bị thu hồi:** khóa account/employee, đổi/reset mật khẩu không vô hiệu hóa token đang dùng.
4. **Chuyển trạng thái cuốn trái nghiệp vụ:** CRUD có thể đặt đang mượn/đặt trước hoặc ngừng lưu thông trong khi còn nghĩa vụ.
5. **Phạt hỏng/mất tùy ý:** số tiền do client quyết định, không formula/approval/reason.
6. **Thu tiền đồng thời:** thiếu lock/idempotency có thể gây race, rollback khó hiểu hoặc ghi nhận không nhất quán dưới tải.
7. **Hard delete quá rộng:** librarian có thể yêu cầu hard delete sách/cuốn/độc giả; thiếu retention/preflight/audit.

### 17.2 Medium

1. `findAll()` và N+1 có thể làm chậm/kiệt tài nguyên khi dữ liệu tăng.
2. Retry sau timeout có thể tạo duplicate/error không xác định vì thiếu idempotency.
3. Audit thiếu coverage/structure và có transaction semantics không khớp nghiệp vụ.
4. Reader deactivate không đồng bộ account; restore không đánh giá lại điều kiện.
5. Branch và location cuốn có thể không khớp.
6. Payment external reference không unique; allocation DB chưa unique theo receipt/debt.
7. Error handler bắt mọi `RuntimeException` thành 400 và trả message exception, có thể che lỗi server hoặc lộ chi tiết nội bộ.
8. Auto debt allocation không stable khi cùng timestamp.
9. Reservation service có row lock khi giữ cuốn nhưng chưa có database unique cho một active reservation của cùng reader/title; request đồng thời vẫn cần kiểm thử/bảo vệ.

### 17.3 Delivery/quality

- Chỉ có `BackendApplicationTests.contextLoads()`; các invariant tài chính, concurrent circulation và security chưa có regression suite.
- Không có migration framework/version ledger; môi trường có thể lệch schema.
- Frontend hard-code dữ liệu demo trong form nghiệp vụ có nguy cơ tạo dữ liệu sai nếu người dùng submit không để ý.

## 18. OPEN DECISION bắt buộc chốt trước khi code

Các quyết định dưới đây là blocker thực sự. Không nên để AI hoặc developer tự chọn ngầm.

### 18.1 Phải chốt trước P0

1. **BR-AUTH-OD1 — Allowed branches:** một nhân viên chỉ một chi nhánh hay nhiều chi nhánh; ai được chuyển/default branch.
2. **BR-AUTH-OD2 — Admin operational identity:** admin không có hồ sơ nhân viên có được lập phiếu không; nếu có thì actor/branch nào.
3. **BR-ID-OD1 — Format mã:** mã đầu sách, cuốn, phiếu, chi tiết, nợ và audit do sequence, UUID hay format nghiệp vụ nào sinh.
4. **BR-READER-OD1 — Eligibility:** ngưỡng nợ, quá hạn và việc gói active có bắt buộc; code hiện đang chặn mọi nợ dương và bắt buộc gói.
5. **BR-READER-OD2 — Đóng hồ sơ còn nghĩa vụ:** chặn tuyệt đối hay trạng thái chờ đóng.
6. **BR-PASS-OD1 — Quyền reset:** thủ thư có được reset độc giả không, xác minh danh tính, rate limit, cách giao mật khẩu tạm.
7. **BR-RETURN-OD1 — Trả khác chi nhánh:** cấm, cho phép trực tiếp hay tạo in-transit/transfer.
8. **BR-RETURN-OD2 — Phạt và reservation handoff:** severity/formula/cap/override; cuốn trả về có chuyển sang giữ chỗ không.
9. **BR-PAY-OD1 — Thanh toán điện tử:** đồng bộ hay pending/webhook; khi nào được ghi giảm nợ.
10. **BR-PAY-OD2 — Reversal/quỹ:** ai được hủy, có cần phê duyệt kép, ca/quỹ có trong phạm vi release không.
11. **BR-AUDIT-OD1 — Audit:** retention, before/after detail, quyền xem, atomic cùng transaction hay outbox bắt buộc.
12. **Optimistic locking:** entity nào có `rowVersion`; phối hợp thế nào với pessimistic lock cho circulation/finance.
13. **Ngày hết hạn:** thẻ/gói có hiệu lực hết ngày ghi trên thẻ hay hết từ đầu ngày đó. Code hiện coi đúng ngày là hết hạn.
14. **API DTO naming:** giữ tiếng Việt để tương thích hay contract mới dùng tiếng Anh; cần quyết định trước khi tạo component/client mới.

### 18.2 Phải chốt trước phần catalog/copy

1. **BR-TITLE-OD1:** đầu sách bị ẩn thì xử lý reservation đang mở thế nào.
2. **BR-COPY-OD1:** có trạng thái chờ kỹ thuật/đang sửa/đang vận chuyển hay không.
3. **BR-COPY-OD2:** transfer ownership và vị trí trong quá trình chuyển chi nhánh.
4. Quy tắc near-duplicate khi không có ISBN và quyền override.
5. Hard-delete retention/anonymization và thế nào là “dữ liệu nhập nhầm chưa phát sinh liên kết”.

### 18.3 Phải chốt trước mượn/trả

1. **BR-LOAN-OD1:** lịch nghỉ, hạn trả theo ngày làm việc và cap theo thẻ/gói.
2. **BR-LOAN-OD2:** có cấm mượn hai bản cùng đầu sách không.
3. Reservation ownership/handoff khi quét cuốn đang được giữ.
4. Chính sách override blocker và permission/approval tương ứng.

### 18.4 Phải chốt trước P2

1. **BR-RFID-01:** hardware, chuẩn tag, SDK, offline mode, privacy và anti-theft integration.
2. Barcode format/checksum, nội dung QR, quy trình reprint/mất tem.
3. Export PII scope, watermark/audit/retention.
4. Cash shift/reconciliation có nằm trong phạm vi sản phẩm hay tích hợp hệ thống khác.

## 19. Quan hệ với roadmap P0/P1/P2

Gap analysis xác nhận thứ tự trong `02-roadmap.md` là hợp lý, nhưng nên diễn giải như sau:

1. **P0 không chỉ là một phase UI.** Phải đóng các lỗ actor/branch/token/state/concurrency trước khi đưa UI mới vào vận hành; nếu không UI đẹp hơn vẫn gọi contract không an toàn.
2. **DataTable v2 và AsyncEntityPicker là nền P1 dùng chung**, triển khai sau khi page/search/staff-context contract tối thiểu ổn định. Không nên sửa từng bảng bằng CSS/page-local picker riêng.
3. **Giữ và bọc lại core nghiệp vụ tốt đang có:** copy lock khi mượn, multi-item return DTO, auto/manual payment allocation, unique constraints. Không cần viết lại toàn bộ service từ đầu.
4. **Mỗi màn hình chỉ migrate sau khi dependency đạt:** books làm pilot cho paging/table/picker; copies thêm branch/state; readers thêm lifecycle/password; rồi loan, return, payment.
5. **P2 chỉ bắt đầu sau P0/P1 release gate:** barcode print/bulk/export dựa trên ID, permission, audit và selection contract; RFID tiếp tục blocked cho tới khi có hardware decision.

## 20. Baseline nghiệm thu hiện tại

- Build test backend hiện chỉ kiểm tra Spring context; không đủ chứng minh các acceptance case.
- Chưa có test frontend/component được tìm thấy cho DataTable, picker hoặc các page nghiệp vụ.
- Chưa có integration test SQL Server cho constraint/migration.
- Chưa có security test actor/branch/token revocation.
- Chưa có concurrency test cho borrow/return/payment.
- Chưa có E2E chuỗi catalog → copy → loan → return → debt → payment.

Do đó, trước khi sửa implementation, cần biến các case P0 trong `04-acceptance-tests.md` thành baseline tests. Các hành vi hiện có được ghi nhận trong tài liệu này không được xem là an toàn lâu dài nếu chưa có test khóa lại.

# Quy tắc nghiệp vụ hiện đại hóa quản trị thư viện

## 1. Phạm vi và nguồn sự thật

Tài liệu này quy định các invariant mà frontend, backend và database phải cùng tuân thủ. Nếu giao diện cho phép một thao tác nhưng backend từ chối theo quy tắc này, backend là nguồn xác thực cuối. Các mục `OPEN DECISION` phải được chốt trước khi triển khai phần phụ thuộc.

## 2. Danh tính, vai trò và chi nhánh

### BR-AUTH-01 — Danh tính người thao tác

- Nhân viên lập phiếu mượn, nhận trả, thu tiền hoặc thay đổi trạng thái phải lấy từ authenticated principal.
- Client không được quyết định actor bằng `maNhanVienLap`, `maNhanVienNhan`, `maNhanVienThu` hoặc trường tương tự.
- Nếu giữ các trường cũ để tương thích, backend phải bỏ qua hoặc đối chiếu với principal và từ chối sai lệch.

### BR-AUTH-02 — Staff context

Context tối thiểu gồm account ID, staff ID, tên, role, default branch và allowed branches.

### BR-AUTH-03 — Phạm vi chi nhánh

- Thủ thư chỉ xem/thao tác dữ liệu trong allowed branches.
- Frontend khóa lựa chọn để hỗ trợ UX; backend bắt buộc kiểm tra lại.
- Đổi ngữ cảnh chi nhánh phải xóa/revalidate giỏ giao dịch.

**OPEN DECISION BR-AUTH-OD1:** mô hình allowed branches là một chi nhánh từ `NHANVIEN.MaChiNhanh` hay quan hệ nhiều-nhiều.

**OPEN DECISION BR-AUTH-OD2:** admin không có staff profile có được tạo giao dịch nghiệp vụ trực tiếp hay không.

## 3. Định danh và mã nghiệp vụ

### BR-ID-01

- Mã đầu sách, mã cuốn, barcode, mã phiếu mượn/trả/thu phải duy nhất.
- Mã giao dịch và mã cuốn nên sinh phía server bằng sequence/cơ chế an toàn đồng thời.
- Không dùng `Date.now()` ở frontend làm nguồn duy nhất.
- Định danh ổn định không chứa vị trí/trạng thái dễ thay đổi.

### BR-ID-02 — Idempotency

- Create loan/return/payment phải chống request lặp.
- Cùng idempotency key và cùng actor/payload trả lại kết quả cũ; payload khác phải bị từ chối.

**OPEN DECISION BR-ID-OD1:** format mã cụ thể cho từng entity.

## 4. Đầu sách

### BR-TITLE-01 — Bản ghi thư mục

Đầu sách chứa metadata dùng chung; cuốn sách chứa thông tin bản vật lý. Không dùng trạng thái đầu sách thay trạng thái lưu thông của cuốn.

### BR-TITLE-02 — Trùng dữ liệu

- ISBN phải được chuẩn hóa và kiểm tra checksum nếu có.
- ISBN trùng chính xác phải báo conflict và liên kết bản ghi hiện có.
- Không có ISBN thì cảnh báo bản ghi gần giống theo tên/tác giả/NXB/năm.

### BR-TITLE-03 — Ngừng hiển thị

- Ngừng hiển thị giữ dữ liệu/lịch sử.
- Không tự đổi trạng thái các cuốn, không thay đổi phiếu mượn hiện tại.
- Không cho đặt trước mới; xử lý đặt trước đang mở theo quyết định riêng.
- Phải lưu lý do, actor và thời gian.

### BR-TITLE-04 — Hard delete

Chỉ admin được xóa cứng và chỉ khi không có liên kết cuốn, mượn/trả, đặt trước, yêu thích, bình luận, đánh giá, báo cáo hoặc lịch sử cần giữ.

**OPEN DECISION BR-TITLE-OD1:** xử lý đặt trước đang mở khi đầu sách bị ẩn.

## 5. Cuốn sách và trạng thái lưu thông

### BR-COPY-01 — Tạo cuốn

- Đầu sách, chi nhánh và vị trí phải tồn tại/hoạt động.
- Vị trí phải thuộc chi nhánh.
- Cuốn mới mặc định Sẵn có hoặc trạng thái kỹ thuật riêng nếu được chốt.
- Mã cuốn/barcode không trùng; batch tạo atomic với lô nhỏ.

### BR-COPY-02 — Chuyển trạng thái do nghiệp vụ

- `Sẵn có → Đang mượn`: chỉ create loan.
- `Sẵn có → Đang đặt trước`: chỉ reservation/hold workflow.
- `Đang đặt trước → Đang mượn`: nhận sách đặt trước.
- `Đang đặt trước → Sẵn có`: hủy/hết hạn giữ chỗ.
- `Đang mượn → Sẵn có/Hỏng/Mất`: chỉ return/lost workflow.

### BR-COPY-03 — Thay đổi vật lý thủ công

- Sẵn có có thể chuyển Hỏng/Mất/Ngừng lưu thông qua action riêng.
- Hỏng/Mất/Ngừng lưu thông chỉ về Sẵn có qua action khôi phục có lý do.
- Không dùng select tự do trong form edit.
- Nếu cuốn đang mượn/giữ chỗ, action thủ công phải bị chặn hoặc tạo yêu cầu chờ được thiết kế riêng.

### BR-COPY-04 — Chuyển vị trí/chi nhánh

- Chuyển vị trí lưu trước/sau, lý do và actor.
- Chuyển chi nhánh không phải update field thông thường; nếu có vận chuyển phải có trạng thái/quy trình riêng.

**OPEN DECISION BR-COPY-OD1:** có trạng thái Chờ xử lý kỹ thuật/Đang sửa chữa/Đang vận chuyển hay không.

**OPEN DECISION BR-COPY-OD2:** quy trình chuyển chi nhánh và ownership.

## 6. Độc giả, thẻ, gói và tài khoản

### BR-READER-01 — Tách trạng thái

- Hồ sơ: Hoạt động/Khóa/Ngừng hoạt động.
- Thẻ: Còn hạn/Sắp hết hạn/Hết hạn, suy ra từ ngày.
- Gói: Đang sử dụng/Sắp hết hạn/Hết hạn/Chưa có.
- Tài khoản: Hoạt động/Khóa đăng nhập/Buộc đổi mật khẩu.
- Borrow eligibility là kết quả tổng hợp có danh sách lý do.

### BR-READER-02 — Hết hạn

- Thẻ hết hạn khi `NgayHetHanThe < ngày nghiệp vụ hiện tại` theo timezone thư viện.
- Không có action chỉnh tay sang Hết hạn.
- Job đồng bộ nếu có không thay thế validation trực tiếp tại nghiệp vụ.

### BR-READER-03 — Khóa

- Khóa có scope (borrowing/login), lý do, thời gian và actor.
- Mở khóa có lý do.
- Mở khóa không tự gia hạn thẻ/gói hoặc xóa nợ/quá hạn.

### BR-READER-04 — Ngừng hoạt động

- Không cho phát sinh mượn/đặt trước mới.
- Giữ toàn bộ lịch sử.
- Trước khi đóng phải kiểm tra sách đang mượn, nợ và đặt trước; chính sách đóng chờ cần được chốt.
- Restore phải tính lại hiệu lực thẻ/gói/account.

### BR-READER-05 — Borrow eligibility

Kết quả tối thiểu xem xét hồ sơ, thẻ, gói nếu bắt buộc, quota, sách quá hạn, ngưỡng nợ và override. Backend trả `eligible`, warnings và blocking reasons có code ổn định.

**OPEN DECISION BR-READER-OD1:** ngưỡng nợ/quá hạn và gói bắt buộc.

**OPEN DECISION BR-READER-OD2:** đóng hồ sơ khi còn nghĩa vụ dùng trạng thái chờ hay bị chặn tuyệt đối.

## 7. Mật khẩu và phiên đăng nhập

### BR-PASS-01

- Không hiển thị mật khẩu/hash; không đưa password vào profile update.
- Reset là endpoint/action riêng, bắt buộc quyền và lý do.

### BR-PASS-02

- Mật khẩu tạm phải đủ mạnh, chỉ hiển thị một lần và buộc đổi lần sau.
- Reset link nếu dùng phải one-time và có thời hạn.
- Audit không chứa password/hash/token.

### BR-PASS-03 — Thu hồi phiên

- Nếu chọn revoke sessions, token cũ phải mất hiệu lực thực sự qua token version hoặc `PasswordChangedAt` + token issued-at.
- Chỉ thay password hash là chưa đủ với token stateless.

**OPEN DECISION BR-PASS-OD1:** thủ thư có quyền reset không, cách xác minh danh tính và giới hạn số lần.

## 8. Mượn sách

### BR-LOAN-01 — Reader-first

- Chọn/quét độc giả và lấy borrowing context trước khi thêm cuốn.
- Blocking reason chặn xác nhận; warning cho phép tiếp tục.

### BR-LOAN-02 — Copy eligibility

- Cuốn tồn tại, thuộc chi nhánh, ở trạng thái cho mượn và không bị giữ cho người khác.
- Không thêm trùng một cuốn trong giỏ.
- Nếu chính sách cấm trùng đầu sách, backend kiểm tra cả khoản đang mượn/giỏ.

### BR-LOAN-03 — Quota và hạn trả

- `current + new <= max`.
- Hạn trả tính từng cuốn theo rule snapshot; không tin ngày client.
- Rule áp dụng phải được lưu để lịch sử không đổi khi quy định mới ban hành.

### BR-LOAN-04 — Atomicity

- Preview không giữ chỗ và không thay validation cuối.
- Create loan lock các cuốn, revalidate reader/copies/quota, tạo phiếu/chi tiết, chuyển trạng thái và commit atomic.
- Một cuốn lỗi thì không âm thầm tạo phiếu phần còn lại.

### BR-LOAN-05 — Override

Override chỉ với quyền; lưu điều kiện bị ghi đè, lý do, actor và approver nếu cần.

**OPEN DECISION BR-LOAN-OD1:** lịch nghỉ/giới hạn hạn trả theo thẻ/gói.

**OPEN DECISION BR-LOAN-OD2:** chính sách cấm mượn hai bản cùng đầu sách.

## 9. Trả sách

### BR-RETURN-01 — Xác định giao dịch

- Barcode/copy ID/loan-detail ID phải truy được đúng open loan.
- Cuốn đầu tiên xác định độc giả; một phiếu hiện chỉ chứa chi tiết của một độc giả.
- Độc giả bị khóa/hết hạn vẫn được trả sách.

### BR-RETURN-02 — Chi nhánh

- Theo hiện trạng, chi nhánh trả phải trùng chi nhánh mượn.
- Nếu cho trả khác chi nhánh, cần workflow riêng; không chỉ bỏ validation.

### BR-RETURN-03 — Phạt trễ

- Backend dùng server time/ngày nghiệp vụ và rule snapshot để tính số ngày/mức phạt.
- Frontend chỉ hiển thị preview.

### BR-RETURN-04 — Hỏng/mất

- Tình trạng gồm Bình thường/Hỏng/Mất; hỏng có loại/mức độ/mô tả.
- Mức phạt đề xuất dựa trên trị giá/quy định; client không quyết định tiền cuối.
- Điều chỉnh lưu đề xuất, giá trị cuối, lý do và phê duyệt.

### BR-RETURN-05 — Atomicity và trạng thái

- Lock chi tiết/cuốn, chặn trả trùng, tạo phiếu/chi tiết, cập nhật loan/copy, tạo nợ và xử lý reservation trong một transaction.
- Phiếu mượn chỉ Đã trả hết khi không còn chi tiết mở.
- Bình thường → Sẵn có hoặc Đang đặt trước nếu có hold hợp lệ; Hỏng → Hỏng; Mất → Mất.

**OPEN DECISION BR-RETURN-OD1:** trả khác chi nhánh.

**OPEN DECISION BR-RETURN-OD2:** công thức phạt và reservation queue sau trả.

## 10. Khoản nợ và thu tiền

### BR-DEBT-01

- Khoản nợ lưu nguồn, loại, số phát sinh, đã thanh toán, còn lại và trạng thái.
- `0 <= đã thanh toán <= phát sinh`.
- Trạng thái: Chưa thanh toán/Thanh toán một phần/Đã thanh toán.

### BR-PAY-01 — Auto allocation

- Phân bổ vào khoản nợ hợp lệ cũ nhất trước với thứ tự ổn định.
- Amount phải > 0 và <= tổng dư nợ; không tự tạo credit balance.

### BR-PAY-02 — Manual allocation

- Từng allocation > 0 và <= remaining.
- Tất cả debt IDs thuộc cùng độc giả và chưa trả hết.
- Tổng phiếu được suy ra/đối chiếu bằng tổng allocations.

### BR-PAY-03 — Atomicity

- Lock debts, re-read remaining, validate, tạo phiếu/chi tiết, cập nhật debts và commit atomic.
- Unique external transaction ID nếu có.
- Idempotency chống trừ nợ hai lần.

### BR-PAY-04 — Phiếu bất biến và reversal

- Phiếu thành công không sửa/xóa.
- Sai sót xử lý bằng reversal có lý do/quyền/phê duyệt.
- Reversal hoàn allocations, tính lại debt status, giữ phiếu gốc và không được đảo hai lần.

### BR-PAY-05 — Phương thức

- Danh mục lấy từ database và chỉ phương thức hoạt động.
- Tiền mặt có thể lưu tiền khách đưa/tiền thừa.
- Điện tử có external reference; chỉ cập nhật nợ khi trạng thái thành công.

**OPEN DECISION BR-PAY-OD1:** payment điện tử đồng bộ hay bất đồng bộ.

**OPEN DECISION BR-PAY-OD2:** quyền/phê duyệt reversal và quản lý ca/quỹ.

## 11. Barcode, QR và RFID

### BR-CODE-01

- Barcode duy nhất, ổn định, không phụ thuộc vị trí/trạng thái.
- Exact scan không cần tải danh sách; duplicate scan báo lỗi và không thêm lần hai.
- QR chỉ chứa identifier/URL không nhạy cảm.

### BR-CODE-02

- In nhãn không thay đổi định danh.
- Batch in theo selected/page/all matching phải có scope rõ.

### BR-RFID-01 — OPEN DECISION

RFID chưa triển khai trước khi chốt thiết bị, chuẩn tag, giao thức, security gate và quy trình kiểm kê.

## 12. Audit và dữ liệu nhạy cảm

### BR-AUDIT-01

Các action bắt buộc audit: create/update/deactivate/restore/hard delete, status transition, branch/location move, password reset, override, loan/return/payment, fine adjustment, reversal, bulk action và export nhạy cảm.

### BR-AUDIT-02

Audit chứa actor, role, branch, action, entity, server time, before/after, reason, source/correlation. Không chứa secret.

### BR-AUDIT-03

Người dùng nghiệp vụ không được sửa/xóa audit. Quyền xem và retention phải theo chính sách.

**OPEN DECISION BR-AUDIT-OD1:** retention, mức chi tiết before/after và quyền xem audit.

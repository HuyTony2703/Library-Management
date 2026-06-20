# Hiện đại hóa khu vực quản trị và nghiệp vụ thư viện

## 1. Mục đích tài liệu

Tài liệu này là đặc tả yêu cầu tổng thể cho việc hiện đại hóa các màn hình dành cho quản trị viên và thủ thư của LibraDesk. Nội dung bao phủ nền tảng bảng dữ liệu, quản lý đầu sách, cuốn sách, độc giả, mượn sách, trả sách, thu tiền, phân quyền theo nhân viên/chi nhánh, barcode/RFID và nhật ký nghiệp vụ.

Các tài liệu liên quan:

- `01-business-rules.md`: quy tắc nghiệp vụ và chuyển trạng thái.
- `02-roadmap.md`: thứ tự triển khai và dependency.
- `03-api-contracts.md`: hợp đồng API đề xuất.
- `04-acceptance-tests.md`: tiêu chí và kịch bản nghiệm thu.

Quy ước:

- **Hiện có**: hành vi đã xác nhận trong source code/schema hiện tại.
- **Đề xuất**: trạng thái đích cần triển khai.
- **OPEN DECISION**: quyết định nghiệp vụ chưa được chốt và không được tự suy diễn khi triển khai.

## 2. Nguyên tắc thiết kế chung

1. Giao diện nghiệp vụ đi theo luồng “quét/tìm đối tượng → xác nhận ngữ cảnh → chọn nghiệp vụ → kiểm tra điều kiện → hoàn tất”, không yêu cầu người dùng nhớ và nhập mã kỹ thuật.
2. Backend là nguồn xác thực cuối cho danh tính người thao tác, chi nhánh, quyền, trạng thái, hạn mức, tiền phạt và số tiền.
3. Dữ liệu danh mục lớn phải tìm kiếm, lọc, sort và phân trang phía server.
4. Các thao tác tài chính/trạng thái phải có transaction, kiểm tra đồng thời và audit.
5. Không xóa cứng dữ liệu đã phát sinh nghiệp vụ. Ưu tiên ngừng hiển thị, ngừng lưu thông, khóa hoặc ngừng hoạt động.
6. Không hard-code mã nhân viên, chi nhánh, đầu sách, độc giả, trạng thái hoặc phương thức thanh toán trong production UI.
7. Các component dùng chung phải hỗ trợ nâng cấp từng màn hình, không buộc phải thay toàn hệ thống trong một lần.

## 3. Nền tảng DataTable dùng chung

### 3.1 Hiện có

- `DataTable` nhận toàn bộ mảng dữ liệu, mặc định hiển thị 6 dòng và phân trang ở frontend.
- CSS dùng `table-layout: fixed`, `white-space: nowrap` và `text-overflow: ellipsis` cho mọi ô, làm tên sách và họ tên bị cắt.
- Checkbox lựa chọn luôn chiếm một cột trên các màn hình quản lý.
- Pagination chỉ có trước/sau và số trang hiện tại/tổng trang.
- Đầu sách, cuốn sách và độc giả hiện lấy toàn bộ dữ liệu qua các service/repository kiểu `findAll()`.
- Bulk delete hiện có xu hướng lặp từng ID và gọi nhiều request độc lập.

### 3.2 Đề xuất frontend

DataTable mới phải có hai chế độ:

- **Local mode** cho bảng nhỏ như chi tiết phiếu, vài khoản nợ của một độc giả hoặc kết quả vừa tạo.
- **Controlled/server mode** cho đầu sách, cuốn sách, độc giả, phiếu và báo cáo lớn.

Khả năng bắt buộc:

- Cấu hình theo cột: `width`, `minWidth`, căn lề, `wrap`, số dòng tối đa, sticky.
- Cột tên/họ tên căn trái và xuống dòng 2–3 dòng; có tooltip/drawer để đọc đầy đủ.
- Loading, error, empty state và giữ dữ liệu cũ trong lúc đổi trang nếu phù hợp.
- Page size 20/50/100; hiển thị `1–20 / 2.438`.
- Trang đầu, trang trước, dãy số trang, trang sau, trang cuối và ô “Đi đến trang”.
- Sort có trạng thái none/asc/desc; URL giữ search/filter/sort/page.
- Hủy request cũ hoặc chống response cũ ghi đè response mới.
- Chế độ `Chọn hàng loạt`; checkbox chỉ xuất hiện khi bật.
- Checkbox header có trạng thái chưa chọn/chọn một phần/chọn cả trang.
- Phân biệt “chọn trang hiện tại” và “chọn toàn bộ kết quả phù hợp bộ lọc”.
- Thanh hành động sticky hiển thị số mục đã chọn và các action hợp lệ.
- Tùy chỉnh cột và mật độ có thể triển khai sau nền tảng tối thiểu.

### 3.3 Đề xuất backend/database

- Chuẩn hóa request `page`, `pageSize`, `search`, `sort` và filter có kiểu.
- Chuẩn hóa response `items`, `page`, `pageSize`, `totalItems`, `totalPages`.
- Allowlist trường sort; không nối chuỗi sort trực tiếp vào SQL.
- Sort ổn định bằng mã định danh làm khóa phụ.
- `pageSize` tối đa 100.
- Có index phù hợp cho các filter/sort thường dùng.
- Bulk API nhận selected IDs hoặc filtered query + excluded IDs; không gửi hàng nghìn request từ frontend.

### 3.4 Security

- Backend kiểm tra quyền từng bulk action và từng dòng.
- Query “all matching” phải được dựng lại từ filter hợp lệ phía server.
- Export phải giới hạn dữ liệu theo vai trò/chi nhánh và bảo vệ dữ liệu cá nhân.

### 3.5 Acceptance summary

- Tên dài có thể đọc đầy đủ.
- Không tải toàn bộ danh mục lớn.
- Reload/back/forward giữ query state.
- Selection qua trang hoạt động đúng và bị xóa khi query thay đổi.
- Bulk action báo số thành công/thất bại, không thành công một phần trong im lặng.

## 4. Component tìm kiếm thực thể dùng chung

### 4.1 Hiện có

- Nhiều form nhập mã trực tiếp hoặc chuỗi mã phân cách dấu phẩy.
- Danh mục chi nhánh, vị trí, trạng thái cuốn, nhóm độc giả, gói và phương thức thanh toán đã có option endpoint, nhưng chưa được dùng đồng đều.

### 4.2 Đề xuất

`AsyncEntityPicker` dùng chung phải hỗ trợ:

- Single-select và multi-select.
- Tìm kiếm bất đồng bộ có debounce và hủy request cũ.
- Tìm exact-match theo mã/barcode/ISBN.
- Điều hướng bàn phím, loading/error/empty state.
- Label chính là tên; mã và metadata là thông tin phụ.
- Không tải toàn bộ entity lớn vào `<select>`.
- Có thể cấu hình cho độc giả, đầu sách, cuốn sách, tác giả, thể loại, NXB.
- Multi-select hiển thị chip, ngăn chọn trùng và hỗ trợ thêm nhanh entity nếu có quyền.

## 5. Quản lý đầu sách

### 5.1 Hiện có

- Danh sách tìm kiếm ở frontend trên toàn bộ dữ liệu.
- Checkbox luôn hiển thị.
- Tên đầu sách chịu CSS nowrap/ellipsis.
- Form nhập mã NXB, danh sách mã tác giả và thể loại bằng text.
- Form tạo có giá trị mẫu/hard-code phục vụ test.
- Trạng thái chính là hoạt động/ngừng hiển thị; soft delete được biểu diễn bằng HTTP DELETE mode.

### 5.2 Đề xuất frontend

- Toolbar: tìm kiếm lớn, trạng thái, thể loại, tác giả, NXB, khoảng năm, tùy chỉnh cột, chọn hàng loạt và thêm đầu sách.
- Cột tên chiếm khoảng 35–40%, tối thiểu khoảng 320px; hiển thị tác giả dòng phụ.
- Cột ưu tiên: tên, ISBN, NXB/năm, tổng bản/sẵn có, trạng thái và thao tác.
- Click dòng mở drawer giữ nguyên page/filter; drawer có Tổng quan, Bản vật lý, Lịch sử.
- Form chia nhóm nhận diện, tác giả/thể loại, xuất bản, trị giá, ảnh bìa và mô tả.
- Tác giả/thể loại dùng async multi-select; NXB dùng async select.
- Kiểm tra ISBN-10/13, cảnh báo trùng ISBN và bản ghi gần giống.
- Không có dữ liệu test mặc định; mã nên do backend sinh theo quyết định.

### 5.3 Đề xuất backend/database

- Danh sách phân trang với filter/sort phía server và summary số cuốn.
- Endpoint tìm tác giả/thể loại/NXB/đầu sách cho picker.
- Tách create/update DTO; lỗi validation theo trường.
- API ngừng hiển thị/khôi phục có ngữ nghĩa rõ và nhận lý do.
- Lưu audit người tạo/cập nhật/thay đổi trạng thái.
- Hard delete chỉ khi không có cuốn, mượn/trả, đặt trước, yêu thích, bình luận, đánh giá hoặc liên kết lịch sử khác.

### 5.4 Security

- Chỉ vai trò được phép mới tạo/sửa/ẩn/khôi phục.
- Xóa cứng chỉ admin và phải kiểm tra backend.
- Không cho client tự gắn người thực hiện.

### 5.5 Acceptance summary

- Không cần nhớ mã tác giả/thể loại/NXB.
- Tìm được bằng mã, tên, ISBN, tác giả.
- Ẩn đầu sách không tự đổi trạng thái cuốn hoặc xóa lịch sử.
- Không thể xóa cứng đầu sách đã có liên kết.

## 6. Quản lý cuốn sách

### 6.1 Hiện có

- Danh sách tải toàn bộ cuốn và lọc phía frontend.
- Form nhập mã đầu sách, chi nhánh và vị trí bằng text dù option API đã tồn tại.
- Trạng thái có select khi sửa, gồm cả trạng thái nghiệp vụ như đang mượn/đặt trước.
- Mã cuốn và giá trị mặc định được sinh/hard-code ở frontend.

### 6.2 Đề xuất frontend

- Search theo mã cuốn, barcode, tên đầu sách, ISBN.
- Filter tối thiểu: trạng thái, chi nhánh, khoảng ngày nhập, đầu sách, khu/kho/kệ/vị trí, có/chưa có barcode/QR.
- Preset: Sẵn có, Đang mượn, Hỏng/mất, Nhập trong 30 ngày.
- Vị trí là cascade chi nhánh → khu/kho → kệ → vị trí.
- Bảng hiển thị tên đầu sách, tên chi nhánh và label vị trí thay cho chỉ mã.
- Drawer hiển thị nhận diện, vị trí, lịch sử lưu thông và trạng thái.
- Form nhập hỗ trợ một hoặc nhiều bản cùng đầu sách.
- Đầu sách dùng picker mã/tên/ISBN; chi nhánh theo quyền; vị trí phụ thuộc chi nhánh.
- Ngày nhập mặc định hôm nay; số lượng có giới hạn.
- Barcode có chế độ tự tạo, quét mã có sẵn hoặc bổ sung sau; preview lô và kết quả in nhãn.
- Không dùng form sửa chung để đổi chi nhánh/vị trí/trạng thái; dùng action riêng.

### 6.3 Đề xuất backend/database

- Mã cuốn và barcode cấp phía server bằng sequence/quy tắc duy nhất, an toàn đồng thời.
- API batch tạo lô trong transaction; trạng thái ban đầu do backend ép là Sẵn có.
- Option vị trí nhận `branchId` và trả metadata có cấu trúc.
- API chuyển vị trí, chuyển chi nhánh, báo hỏng/mất/ngừng lưu thông/khôi phục riêng.
- Lưu lịch sử vị trí và trạng thái, lý do, người thực hiện và nguồn nghiệp vụ.
- Index theo chi nhánh/trạng thái/ngày nhập/đầu sách/barcode.

### 6.4 Security

- Thủ thư chỉ thao tác trong allowed branches.
- `Đang mượn` chỉ do nghiệp vụ mượn; `Đang đặt trước` chỉ do đặt trước; trạng thái sau trả do nghiệp vụ trả.
- Backend kiểm tra vị trí thuộc chi nhánh và cuốn có đủ điều kiện chuyển trạng thái.

### 6.5 Acceptance summary

- Không nhập mã đầu sách/chi nhánh/vị trí bằng tay.
- Có thể tạo lô không trùng mã/barcode.
- Không thể sửa tay sang đang mượn/đặt trước.
- Mọi thay đổi vật lý có lý do và audit.

## 7. Quản lý độc giả

### 7.1 Hiện có

- Danh sách tải toàn bộ và lọc frontend; họ tên bị cắt.
- Form create/update dùng chung request có mật khẩu bắt buộc; form sửa vô hiệu hóa password và backend update không đổi password.
- Backend chỉ có ngừng hoạt động và khôi phục thẳng về hoạt động.
- Trạng thái hồ sơ, hạn thẻ, gói và tài khoản chưa được thể hiện tách bạch trong UI.

### 7.2 Đề xuất frontend

- Họ tên rộng 240–300px, căn trái, xuống dòng; mã và nhóm ở dòng phụ.
- Filter nhóm, loại gói, trạng thái hồ sơ, hạn thẻ, hạn gói, trạng thái tài khoản/điều kiện mượn khi cần.
- Preset rõ nghĩa: Thẻ sắp hết hạn, Gói sắp hết hạn, Thẻ/gói đã hết hạn, Đang khóa.
- Sort họ tên/tên gọi, ngày lập thẻ, hạn thẻ, hạn gói.
- Drawer có Hồ sơ, Gói thành viên, Sách đang mượn, Nợ, Lịch sử giao dịch và Tài khoản & bảo mật.
- Cột điều kiện mượn hiển thị đủ điều kiện hoặc các lý do chặn.
- Các action riêng: gia hạn thẻ, đổi/gia hạn gói, khóa/mở khóa, ngừng hoạt động/khôi phục, reset password.

### 7.3 Đề xuất backend/database

- Tách trạng thái hồ sơ, hiệu lực thẻ, hiệu lực gói, trạng thái tài khoản và borrow eligibility.
- `Hết hạn` được suy ra từ `NgayHetHanThe`, không phải lựa chọn thủ công.
- Khóa có scope, lý do, thời gian bắt đầu/kết thúc, người thực hiện.
- Ngừng hoạt động giữ lịch sử; restore phải tính lại hạn thẻ/gói thay vì đặt mù quáng về hoạt động.
- Tách `ReaderCreateRequest`, `ReaderProfileUpdateRequest`, card/membership/status/password request.
- Borrowing context trả warning và blocking reason có mã.

### 7.4 Reset mật khẩu

- Không xem hoặc sửa mật khẩu hiện tại.
- Endpoint riêng tạo mật khẩu tạm, nhận mật khẩu mới hoặc reset link theo chính sách.
- Bắt buộc đổi ở lần đăng nhập tiếp theo.
- Thu hồi token cũ bằng token version hoặc password-changed-at/issued-at.
- Mật khẩu tạm chỉ hiển thị một lần; không ghi vào audit.
- Admin có quyền theo chính sách; thủ thư chỉ trong phạm vi được phép và có lý do.

### 7.5 Security

- Bảo vệ dữ liệu cá nhân; bảng chính không hiển thị ngày sinh/địa chỉ không cần thiết.
- Không xóa cứng hồ sơ có lịch sử; nếu pháp lý yêu cầu, dùng quy trình ẩn danh hóa có kiểm soát.
- Reset password có rate limit, audit và scope quyền.

### 7.6 Acceptance summary

- Hạn thẻ/gói không bị trộn với trạng thái hồ sơ.
- Mở khóa không tự làm thẻ hết hạn hợp lệ.
- Password không nằm trong update profile.
- Token cũ bị vô hiệu sau reset theo lựa chọn.

## 8. Mượn sách

### 8.1 Hiện có

- Form hard-code độc giả, nhân viên, chi nhánh và mã cuốn mẫu.
- Mã cuốn được nhập chuỗi phân cách dấu phẩy.
- Trang tải toàn bộ cuốn rồi lọc Sẵn có để hiển thị bảng.
- Backend nhận và lưu `maNhanVienLap` từ request; tuy có kiểm tra quy tắc và row lock cho cuốn.
- “Sách đang mượn của…” hiển thị như bảng lớn phía dưới.

### 8.2 Đề xuất frontend

Luồng:

1. Quét thẻ hoặc tìm độc giả theo mã/tên/email/điện thoại.
2. Hiển thị borrowing context: hạn thẻ, gói, quota, quá hạn, nợ, đặt trước và điều kiện mượn.
3. Chi nhánh/nhân viên read-only từ staff context.
4. Quét barcode/mã cuốn hoặc tìm theo tên/ISBN.
5. Thêm cuốn vật lý cụ thể vào giỏ; chặn trùng/sai chi nhánh/trạng thái.
6. Preview quy định và hạn trả từng cuốn.
7. Xác nhận; hiển thị/in/gửi kết quả.

- Bỏ bảng toàn bộ cuốn sẵn có; thay bằng picker theo nhu cầu.
- “Sách đang mượn” thu gọn, làm nổi bật số ngày quá hạn.
- Phân biệt warning và blocking reason; override chỉ với quyền, lý do và phê duyệt nếu áp dụng.
- Tối ưu bàn phím/máy quét; focus tự chuyển thẻ → sách.

### 8.3 Đề xuất backend/database

- Nhân viên lấy từ principal; không tin `maNhanVienLap` client.
- Chi nhánh lấy/kiểm tra theo staff context.
- Search reader, borrowing context, search/lookup copy và loan preview endpoint.
- Mã phiếu sinh phía server.
- Create loan revalidate trong transaction, row lock, all-or-nothing và idempotency.
- Hạn trả tính từng cuốn theo phiên bản quy định, gói, nhóm, thể loại và giới hạn thẻ nếu có.
- Cập nhật đặt trước khi nhận cuốn đã giữ chỗ.

### 8.4 Security

- Không giả mạo nhân viên/chi nhánh.
- Override lưu điều kiện bị ghi đè, lý do, người thực hiện/phê duyệt.
- Audit đầy đủ danh sách cuốn, rule, hạn trả và máy trạm nếu cần.

### 8.5 Acceptance summary

- Có thể hoàn tất bằng quét thẻ và quét sách.
- Một cuốn không được thêm hai lần hoặc mượn đồng thời.
- Preview không thay validation cuối.
- Double submit không tạo hai phiếu.

## 9. Trả sách

### 9.1 Hiện có

- UI trả từng cuốn dù request DTO hỗ trợ danh sách chi tiết.
- Form nhập độc giả, nhân viên, chi nhánh và mã chi tiết mượn.
- Backend tính ngày trễ/phạt trễ, kiểm tra cùng chi nhánh, cập nhật trạng thái và tạo nợ; tiền phạt hỏng/mất hiện được client gửi.
- Có bảng sách đang mượn của độc giả và bảng toàn cục độc giả đang mượn.

### 9.2 Đề xuất frontend

- Ưu tiên quét barcode/mã cuốn/mã chi tiết mượn; cuốn đầu tiên tự suy ra độc giả.
- Hỗ trợ tìm độc giả khi barcode hỏng hoặc báo mất.
- Tự hiển thị và khóa độc giả, tên/mã cuốn, ngày mượn, hạn trả, số ngày trễ, chi nhánh và phạt dự kiến.
- “Sách đang mượn của độc giả” thành bảng checkbox chọn trả.
- Xóa bảng toàn cục khỏi màn hình giao dịch; chuyển sang tra cứu/báo cáo.
- Giỏ trả nhiều cuốn nhưng chỉ một độc giả/phiếu.
- Mỗi dòng chọn tình trạng Bình thường/Hỏng/Mất; hỏng có loại, mức độ, mô tả, ảnh nếu có.
- Preview phạt trễ, phạt hỏng/mất và tổng phát sinh.

### 9.3 Đề xuất backend/database

- Nhân viên/chi nhánh từ principal và context.
- Lookup open loan theo barcode/copy/loan-detail; return preview.
- Backend tính tiền phạt cuối theo rule; điều chỉnh cần quyền/lý do.
- Mã phiếu server-generated; batch return trong transaction, row lock, all-or-nothing và idempotency.
- Đóng từng chi tiết; chỉ đóng phiếu mượn khi xử lý hết.
- Tạo khoản nợ chi tiết theo loại/nguồn.
- Trả bình thường phải kiểm tra hàng chờ đặt trước trước khi đặt Sẵn có.

### 9.4 Security

- Không trả trùng, không trộn độc giả, không giả mạo nhân viên/chi nhánh/phạt.
- Lưu công thức phạt, mức đề xuất, mức cuối, lý do và người phê duyệt.

### 9.5 Acceptance summary

- Trả được nhiều cuốn bằng scan.
- Cuốn đầu tiên xác định độc giả.
- Giao dịch atomic và chống double-submit.
- Khoản nợ, trạng thái cuốn và đặt trước được cập nhật nhất quán.

## 10. Thu tiền

### 10.1 Hiện có

- Backend hỗ trợ manual allocation và auto allocation khoản cũ trước.
- UI có bảng độc giả còn nợ, bảng khoản nợ, selection và cả trường tổng tiền độc lập, dẫn đến cần nút đồng bộ tổng.
- Nhân viên thu hard-code và cho sửa; phương thức thanh toán hard-code dù option API đã tồn tại.
- Backend nhận `maNhanVienThu` từ request và kiểm tra tổng phân bổ bằng tổng thu.

### 10.2 Đề xuất frontend

- Async picker độc giả còn nợ theo mã/tên/điện thoại/email.
- Thẻ tổng quan tổng nợ, số khoản, khoản cũ nhất và ảnh hưởng quyền mượn.
- Hai tab rõ:
  - **Thu tự động**: nhập tổng tiền; backend preview phân bổ khoản cũ trước.
  - **Chọn khoản nợ**: chọn khoản, nhập số áp dụng; tổng phiếu được suy ra từ allocations.
- Ẩn khoản đã thanh toán mặc định; có tra cứu lịch sử.
- Tóm tắt sticky: số khoản, dư nợ trước, tổng thu, dư nợ sau.
- Phương thức lấy từ API; field phụ theo tiền mặt/chuyển khoản/ví/POS.
- Không nhập mã nhân viên hoặc mã phiếu.

### 10.3 Đề xuất backend/database

- Nhân viên/chi nhánh từ principal/context.
- Payment preview cho auto/manual.
- Mã phiếu server-generated; khóa khoản nợ, chống thu vượt và xử lý atomic.
- Idempotency và unique mã giao dịch ngoài.
- Phiếu thành công không sửa/xóa; sai sót xử lý bằng reversal có lý do/phê duyệt.
- Với điện tử, hỗ trợ trạng thái chờ/thành công/thất bại nếu tích hợp thực tế.
- Lưu snapshot chi nhánh/quầy nếu cần đối soát.

### 10.4 Security

- Không giả mạo nhân viên, số tiền, allocation hoặc khoản nợ của người khác.
- Không lưu dữ liệu thanh toán nhạy cảm không cần thiết.
- Reversal có quyền, audit và không thực hiện hai lần.

### 10.5 Acceptance summary

- Auto/manual rõ ràng và tổng luôn khớp allocations.
- Hai quầy không thể cùng thu vượt một khoản.
- Double submit không tạo hai phiếu.
- Phiếu thành công bất biến; reversal phục hồi công nợ đúng.

## 11. Barcode và RFID

### 11.1 Barcode P1

- Máy quét dạng keyboard wedge phải dùng được trong picker mượn/trả.
- Exact match, Enter để xác nhận, focus ổn định, âm báo/thông báo thành công/lỗi.
- Barcode duy nhất, sinh/kiểm tra phía server.
- QR chỉ chứa định danh/URL ổn định, không chứa dữ liệu nhạy cảm hoặc trạng thái dễ thay đổi.

### 11.2 Barcode P2

- Preview/in nhãn một cuốn hoặc lô.
- Template nhãn, lịch sử in nếu cần.
- Import/quét mã nhà cung cấp và phát hiện trùng/lỗi.

### 11.3 RFID P2 — OPEN DECISION

- Chưa chốt phần cứng, chuẩn tag, giao thức reader/writer, quy trình anti-theft và kiểm kê.
- RFID phải là project riêng sau khi có thiết bị và yêu cầu tích hợp cụ thể; không giả lập bằng logic barcode.

## 12. Audit log

### 12.1 Hiện có

- Có `ActivityLogService`/bảng nhật ký cho một số nghiệp vụ, nhưng mức độ cấu trúc và coverage cần kiểm chứng theo từng action.

### 12.2 Đề xuất

Audit chuẩn cần có:

- Actor account/staff, role, branch và máy trạm nếu cần.
- Action code và entity type/id.
- Timestamp server.
- Giá trị trước/sau có cấu trúc cho thay đổi quan trọng.
- Lý do, scope, thời hạn và người phê duyệt.
- Source transaction: phiếu mượn/trả/thu/đặt trước/kiểm kê.
- Correlation/idempotency key khi phù hợp.
- Không ghi mật khẩu, hash, reset token hoặc dữ liệu thanh toán nhạy cảm.
- Audit bất biến với người dùng thường; chính sách retention và quyền xem cần chốt.

## 13. OPEN DECISION tổng hợp

1. Thủ thư có được thao tác liên chi nhánh hay chỉ chi nhánh gắn với nhân viên?
2. Admin không gắn `NhanVien` sẽ được phép trực tiếp lập mượn/trả/thu hay phải có staff profile?
3. Có cho trả khác chi nhánh không? Nếu có, quy trình vận chuyển/phí/trạng thái thế nào?
4. Quy tắc sinh mã đầu sách, cuốn, barcode và các phiếu.
5. Ngưỡng nợ/quá hạn nào chặn mượn; có override không và ai phê duyệt?
6. Gói thành viên có bắt buộc để mượn không?
7. Hạn trả có điều chỉnh theo hạn thẻ/gói/ngày nghỉ không?
8. Công thức phạt hỏng/mất, khấu hao, phí xử lý và quyền điều chỉnh.
9. Khi trả có hàng chờ đặt trước, quy tắc chọn độc giả/chi nhánh/hạn giữ chỗ.
10. Có cho thủ thư reset password không; phạm vi và bước xác minh danh tính?
11. Payment điện tử là xác nhận thủ công hay tích hợp webhook/reconciliation?
12. Quy trình reversal và người phê duyệt.
13. Chính sách hard delete, retention và ẩn danh hóa dữ liệu độc giả.
14. RFID: thiết bị, chuẩn tag và phạm vi tích hợp.

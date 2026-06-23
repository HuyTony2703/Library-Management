# Tài liệu LibraDesk

Trang này là mục lục tài liệu kỹ thuật. Nếu bạn chỉ muốn cài và chạy ứng dụng, hãy bắt đầu từ [README ở thư mục gốc](../README.md).

## Lộ trình đọc đề xuất

### Người mới tham gia project

1. [README tổng quan](../README.md)
2. [Hướng dẫn database](../database/README_DATABASE.md)
3. [Frontend React/Electron](../frontend/README.md)
4. [Cấu trúc backend](backend/structure.md)
5. [Mapping màn hình và API](frontend/api-mapping.md)

### Người kiểm thử

1. [Postman collection](api/library-desktop-app.postman_collection.json)
2. [Kiểm thử phân quyền backend](backend-permission-test.md)
3. [Hợp đồng prefix API](api-prefix-contract.md)
4. [Kế hoạch nghiệm thu admin](admin-modernization/04-acceptance-tests.md)

## Tài liệu hiện hành

| Chủ đề | File | Nội dung |
|---|---|---|
| API theo vai trò | [api-prefix-contract.md](api-prefix-contract.md) | Quy ước `/api/admin`, `/api/staff`, `/api/reader` |
| Frontend và API | [frontend/api-mapping.md](frontend/api-mapping.md) | Màn hình nào gọi endpoint nào |
| Phân quyền | [backend-permission-test.md](backend-permission-test.md) | Ma trận quyền và cách kiểm tra |
| Cấu trúc backend | [backend/structure.md](backend/structure.md) | Các package và luồng xử lý |
| Backend chi tiết | [backend/complete-documentation.md](backend/complete-documentation.md) | Tài liệu tham khảo theo lớp |
| Database | [../database/README_DATABASE.md](../database/README_DATABASE.md) | Cài đặt, seed và xử lý lỗi |
| Script | [../scripts/README.md](../scripts/README.md) | Build, runtime và log |

## Hồ sơ thiết kế admin modernization

Thư mục [admin-modernization](admin-modernization/README.md) lưu yêu cầu, quy tắc, roadmap, API đề xuất, acceptance test và gap analysis. Đây là hồ sơ thiết kế theo thời điểm, vì vậy có thể khác một phần so với code hiện tại.

Khi có xung đột, ưu tiên theo thứ tự:

1. Schema và source code đang chạy.
2. Hợp đồng API hiện hành.
3. Tài liệu thiết kế/tham khảo.

## Nguyên tắc cập nhật tài liệu

- Dùng tiếng Việt UTF-8 và thuật ngữ thống nhất: quản trị viên, thủ thư, độc giả.
- Lệnh phải ghi rõ chạy từ thư mục gốc hay thư mục con.
- Không ghi mật khẩu thật, connection string thật hoặc token vào Markdown.
- Khi thay đổi endpoint, cập nhật cả `api-prefix-contract.md` và `frontend/api-mapping.md`.
- Khi thay đổi schema hoặc seed, cập nhật `database/README_DATABASE.md`.

## Thông tin tối thiểu của tài liệu

Tài liệu mô tả thiết kế hoặc kết quả kiểm thử nên ghi rõ:

- Phạm vi và đối tượng đọc.
- Source/schema/commit được dùng để đối chiếu.
- Trạng thái: hiện hành, đề xuất hay lưu trữ tham khảo.
- Lệnh hoặc dữ liệu cần để tái hiện kết quả.
- Các quyết định còn mở và người chịu trách nhiệm xác nhận.

Không dùng tài liệu đề xuất làm bằng chứng tính năng đã hoàn thành; cần đối chiếu code, database và acceptance test.

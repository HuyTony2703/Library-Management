# Hồ sơ admin modernization

Thư mục này lưu hồ sơ phân tích và thiết kế cho việc hiện đại hóa khu vực quản trị và nghiệp vụ thư viện.

> Đây là tài liệu thiết kế theo thời điểm, không phải hướng dẫn chạy ứng dụng. Khi nội dung khác source code hiện tại, source code, schema và hợp đồng API hiện hành được ưu tiên.

## Thứ tự đọc

| Thứ tự | Tài liệu | Câu hỏi được trả lời |
|---:|---|---|
| 1 | [00-requirements.md](00-requirements.md) | Hệ thống cần đạt những gì? |
| 2 | [01-business-rules.md](01-business-rules.md) | Các quy tắc nào không được vi phạm? |
| 3 | [02-roadmap.md](02-roadmap.md) | Nên triển khai theo thứ tự nào? |
| 4 | [03-api-contracts.md](03-api-contracts.md) | Contract API đề xuất ra sao? |
| 5 | [04-acceptance-tests.md](04-acceptance-tests.md) | Kiểm chứng yêu cầu bằng cách nào? |
| 6 | [05-gap-analysis.md](05-gap-analysis.md) | Code hiện tại còn thiếu gì so với thiết kế? |

## Cách dùng trạng thái

- `ĐỀ XUẤT`: chưa chắc đã tồn tại trong code.
- `OPEN DECISION`: cần quyết định trước khi triển khai.
- `BLOCKED BY DECISION`: chưa thể nghiệm thu vì còn quyết định mở.
- `ĐẦY ĐỦ`, `MỘT PHẦN`, `CHƯA CÓ`: kết quả gap analysis tại thời điểm ghi trong tài liệu.

## Khi cập nhật

1. Ghi ngày chụp trạng thái nếu tài liệu dựa trên code hiện tại.
2. Không đánh dấu hoàn thành chỉ dựa trên giao diện; phải kiểm tra backend, database và phân quyền.
3. Nếu endpoint đề xuất đã được triển khai khác contract, cập nhật cả contract và mapping frontend/API.
4. Giữ mã yêu cầu, business rule và acceptance test ổn định để dễ truy vết.

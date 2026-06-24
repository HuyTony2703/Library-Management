# Quy tắc nghiệp vụ chính

Tài liệu này tóm tắt các quy tắc mà schema, backend và giao diện LibraDesk cần tuân theo. Khi thay đổi quy định, cần kiểm tra đồng thời database, service và UI.

## 1. Tài khoản và độc giả

- Mỗi độc giả phải gắn với một tài khoản đăng nhập.
- Một tài khoản chỉ có một vai trò.
- Nhóm độc giả gồm học sinh, sinh viên, giáo viên và nhóm khác.
- Nhóm độc giả ảnh hưởng đến giá gói thành viên, không trực tiếp quyết định quyền mượn.
- Tuổi tối thiểu, tuổi tối đa và thời hạn thẻ lấy từ phiên bản quy định đang áp dụng.
- Thẻ hết hạn, tài khoản bị khóa hoặc ngừng hoạt động phải chặn nghiệp vụ cần đăng nhập/mượn sách tương ứng.

## 2. Đầu sách và cuốn sách

- Một đầu sách có thể có nhiều tác giả, nhiều thể loại và nhiều cuốn sách vật lý.
- Mỗi cuốn sách có mã riêng; mã vạch và mã QR phải xác định đúng một cuốn nếu được khai báo.
- Cuốn sách mới luôn bắt đầu ở trạng thái `Sẵn có`.
- Năm xuất bản phải nằm trong khoảng quy định đang áp dụng.
- Trạng thái cuốn sách gồm: sẵn có, đang mượn, đang đặt trước, bị mất, bị hỏng và ngừng lưu thông.
- Trị giá đầu sách là cơ sở tham khảo khi xử lý mất hoặc hỏng.

## 3. Mượn sách

Chỉ cho mượn khi:

- Thẻ độc giả còn hạn.
- Độc giả không có nợ chưa thanh toán.
- Không có sách quá hạn chưa trả.
- Cuốn sách đang sẵn có, hoặc đang được giữ đúng cho độc giả đó.
- Chưa vượt số sách tối đa của gói thành viên.

Hạn trả thực tế là ngày sớm hơn giữa:

1. Ngày mượn cộng số ngày mượn theo gói và thể loại.
2. Ngày hết hạn thẻ trừ một ngày.

Tạo phiếu mượn và cập nhật trạng thái các cuốn phải nằm trong cùng transaction.

## 4. Trả sách

- Một cuốn trong phiếu mượn có thể được trả riêng; hệ thống hỗ trợ trả từng phần.
- Tiền phạt trễ bằng số ngày trễ nhân mức phạt mỗi ngày.
- Phạt hỏng hoặc mất do thủ thư nhập dựa trên tình trạng thực tế và quy định.
- Sau khi trả, cuốn sách chuyển thành sẵn có, bị hỏng hoặc bị mất.
- Phiếu trả, chi tiết trả, khoản nợ và trạng thái sách phải được cập nhật nguyên tử.

## 5. Khoản nợ và phiếu thu

- Phiếu thu dùng cho tiền phạt hoặc tiền mua/gia hạn gói thành viên.
- Thu tiền phạt không được vượt tổng nợ còn lại.
- Có thể tự phân bổ vào khoản nợ cũ nhất hoặc chọn khoản nợ cụ thể.
- Mỗi khoản nợ lưu số tiền phát sinh, đã thanh toán và còn lại.
- Không được áp dụng tiền vào khoản nợ của độc giả khác hoặc khoản đã thanh toán hết.
- Tạo phiếu thu, chi tiết phân bổ và cập nhật nợ phải nằm trong cùng transaction.

## 6. Gói thành viên

- Độc giả có thể đăng ký, gia hạn, nâng cấp hoặc hạ cấp gói.
- Giá phụ thuộc nhóm độc giả.
- Quyền mượn phụ thuộc gói thành viên.
- Mọi thay đổi gói phải được ghi vào lịch sử gói thành viên.

## 7. Đặt trước

- Độc giả đặt trước theo đầu sách; hệ thống có thể gán cuốn cụ thể khi có sách phù hợp.
- Thời gian giữ chỗ lấy từ quy định hiện hành.
- Quá hạn giữ chỗ thì phiếu chuyển sang hết hạn.
- Trạng thái gồm: đang chờ, đã giữ chỗ, đã mượn, đã hủy và đã hết hạn.

## 8. Đánh giá và bình luận

- Đánh giá và bình luận gắn với đầu sách, không gắn trực tiếp với cuốn vật lý.
- Mỗi độc giả có một đánh giá chính cho một đầu sách và có thể cập nhật đánh giá đó.
- Bình luận hiển thị sau khi đăng.
- Quản trị viên hoặc thủ thư có thể ẩn/xóa nội dung vi phạm; thao tác phải được xác nhận và ghi log.
- Trạng thái hiển thị gồm: hiển thị, đã ẩn và đã xóa nếu dùng xóa mềm.

## 9. Quy định hệ thống

- Chỉ quản trị viên được tạo và kích hoạt phiên bản quy định.
- Thay đổi quy định không ghi đè lịch sử; mỗi thay đổi tạo phiên bản mới.
- Các tham số gồm tuổi độc giả, hạn thẻ, năm xuất bản, hạn mức mượn, thời gian mượn, tiền phạt, giá gói, thời gian giữ chỗ và số ngày nhắc hạn.
- Nghiệp vụ phát sinh dùng phiên bản đang có hiệu lực tại thời điểm xử lý.

## 10. Thông báo

Các sự kiện có thể tạo thông báo:

1. Sách sắp đến hạn trả.
2. Sách đã quá hạn trả.
3. Phát sinh tiền phạt.
4. Sách đặt trước đã có.
5. Mua hoặc gia hạn gói thành viên thành công.
6. Gói thành viên sắp hết hạn.
7. Tài khoản hoặc thẻ độc giả thay đổi trạng thái.

Hệ thống lưu lịch sử thông báo, trạng thái gửi email, số lần thử và thời điểm gửi gần nhất để tránh gửi trùng và hỗ trợ kiểm tra lỗi.

## 11. Phân quyền

- Quản trị viên có toàn bộ quyền của thủ thư và thêm quyền quản trị hệ thống.
- Thủ thư quản lý nghiệp vụ sách, độc giả, mượn trả, thu tiền và kiểm duyệt bình luận trong phạm vi được cấp.
- Độc giả chỉ truy cập dữ liệu và thao tác của chính mình, trừ dữ liệu sách công khai.
- Backend là nơi quyết định quyền cuối cùng; ẩn nút trên frontend không thay thế kiểm tra quyền API.

## 12. Nhật ký hoạt động

Các thao tác quan trọng như tạo/sửa/xóa dữ liệu, mượn trả, thu tiền, đổi quy định, kiểm duyệt và quản lý tài khoản phải ghi nhật ký với người thực hiện, hành động, đối tượng và thời điểm.

## 13. Transaction và tính nhất quán

- Tạo phiếu mượn, chi tiết mượn và đổi trạng thái cuốn sách phải thành công hoặc rollback cùng nhau.
- Trả sách, tính phạt, tạo khoản nợ và cập nhật trạng thái cuốn sách phải nằm trong một transaction nghiệp vụ.
- Thu tiền phải tạo phiếu thu, chi tiết phân bổ và cập nhật số tiền đã thanh toán một cách nguyên tử.
- Không dựa vào dữ liệu gửi từ frontend để quyết định số tiền phạt, quyền sở hữu hoặc trạng thái cuối; backend phải đọc lại dữ liệu hiện hành.
- Các thao tác lặp do retry cần được bảo vệ bằng mã nghiệp vụ duy nhất hoặc kiểm tra tồn tại để tránh tạo phiếu trùng.
- Báo cáo chỉ đọc dữ liệu đã commit và không được làm thay đổi trạng thái nghiệp vụ.

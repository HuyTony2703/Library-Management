# Thành viên:
| Họ tên | MSSV | Vai trò |
|---|---|---|
| Lê Trí Cao | 24520206 | Backend, database, nghiệp vụ |
| Tô Ngọc Huy | 24520698 | Frontend, Electron, giao diện |
| Lê Tuấn Dương | 24520359 | Tài liệu, kiểm thử, báo cáo |

# Hướng Dẫn Sử Dụng LibraDesk

LibraDesk là ứng dụng desktop quản lý thư viện. Tài liệu này dành cho người sử dụng app, không phải tài liệu hướng dẫn lập trình.

---

## 1. Trước khi mở app

Trước khi dùng LibraDesk, cần đảm bảo hệ thống backend đã được bật.

Nếu bạn được giao file chạy backend, hãy mở:

```text
start-backend.bat
```

Sau đó đợi cửa sổ backend chạy ổn. Khi backend đã chạy, mở ứng dụng:

```text
LibraDesk.exe
```

Hoặc mở bản cài đặt:

```text
LibraDesk Setup 1.0.0.exe
```

Nếu app mở được nhưng đăng nhập không được, hãy kiểm tra backend đã chạy chưa.

---

## 2. Đăng nhập

Tại màn hình đăng nhập, nhập tài khoản được cấp.

Tài khoản demo thường dùng:

```text
Tên đăng nhập: thuthu01
Mật khẩu: 123456
```

Sau khi đăng nhập thành công, hệ thống sẽ chuyển vào màn hình chính.

Nếu đăng nhập thất bại, có thể do:

```text
Sai tên đăng nhập.
Sai mật khẩu.
Backend chưa chạy.
Tài khoản đã bị khóa.
```

---

## 3. Giao diện chính

Sau khi đăng nhập, màn hình chính gồm:

```text
Thanh menu bên trái
Thanh tìm kiếm phía trên
Khu vực nội dung ở giữa
Thông tin tài khoản ở góc dưới bên trái
Nút đăng xuất ở góc trên bên phải
```

Các mục trong menu:

```text
Tổng quan
Đầu sách
Cuốn sách
Độc giả
Mượn sách
Trả sách
Thu tiền
Báo cáo
```

---

## 4. Màn hình Tổng quan

Màn hình Tổng quan dùng để xem nhanh tình trạng thư viện.

Người dùng có thể xem:

```text
Số lượng đầu sách
Số lượng cuốn sách
Số lượng độc giả
Tổng nợ còn lại
Danh sách độc giả đang có nợ
Biểu đồ sách đang mượn
```

Màn hình này dùng để kiểm tra nhanh trước khi thực hiện các thao tác nghiệp vụ.

---

## 5. Quản lý đầu sách

Vào menu:

```text
Đầu sách
```

Màn hình này hiển thị danh sách đầu sách trong thư viện.

Thông tin thường gồm:

```text
Mã đầu sách
Tên đầu sách
ISBN
Năm xuất bản
Trị giá
Trạng thái
```

Ví dụ dữ liệu mẫu:

```text
F01 - Frieren tập 1
```

Dùng màn hình này để tra cứu thông tin sách theo đầu sách.

---

## 6. Quản lý cuốn sách

Vào menu:

```text
Cuốn sách
```

Màn hình này hiển thị từng bản sách vật lý trong thư viện.

Một đầu sách có thể có nhiều cuốn sách. Ví dụ:

```text
Frieren tập 1 có các cuốn:
F01-001
F01-002
```

Thông tin thường gồm:

```text
Mã cuốn sách
Mã đầu sách
Chi nhánh
Vị trí
Trạng thái
Mã vạch
```

Các trạng thái quan trọng:

```text
Sẵn có
Đang được mượn
Đang được đặt trước
Bị mất
Bị hỏng
Ngừng lưu thông
```

Khi mượn sách thành công, trạng thái cuốn sách sẽ chuyển sang đang mượn.

Khi trả sách bình thường, trạng thái cuốn sách sẽ quay lại sẵn có.

---

## 7. Quản lý độc giả

Vào menu:

```text
Độc giả
```

Màn hình này dùng để xem danh sách độc giả.

Thông tin thường gồm:

```text
Mã độc giả
Họ tên
Email
Số điện thoại
Ngày hết hạn thẻ
Trạng thái
```

Ví dụ dữ liệu mẫu:

```text
DG001 - Lê Văn A
```

Trước khi cho mượn sách, nên kiểm tra độc giả:

```text
Còn hạn thẻ hay không
Đang hoạt động hay không
Có đang nợ hay không
Có sách quá hạn chưa trả hay không
```

---

## 8. Mượn sách

Vào menu:

```text
Mượn sách
```

Màn hình này dùng để lập phiếu mượn.

### 8.1. Thông tin cần nhập

```text
Mã phiếu mượn
Mã độc giả
Mã nhân viên lập
Mã chi nhánh
Mã cuốn sách
Ghi chú
```

Ví dụ:

```text
Mã phiếu mượn: PM_FE_00000001
Mã độc giả: DG001
Mã nhân viên lập: NV_TT001
Mã chi nhánh: CN_TD
Mã cuốn sách: F01-001
```

### 8.2. Cách thao tác

```text
1. Nhập mã độc giả.
2. Nhập mã nhân viên lập.
3. Nhập mã chi nhánh.
4. Nhập mã cuốn sách cần mượn.
5. Nhấn Tạo phiếu mượn.
```

Nếu thành công, hệ thống sẽ báo:

```text
Tạo phiếu mượn thành công
```

Và hiển thị kết quả phiếu mượn.

### 8.3. Các lỗi có thể gặp khi mượn sách

```text
Độc giả không tồn tại.
Nhân viên lập phiếu không tồn tại.
Chi nhánh không tồn tại.
Thẻ độc giả đã hết hạn.
Độc giả đang có nợ.
Độc giả có sách quá hạn chưa trả.
Cuốn sách không ở trạng thái sẵn có.
Cuốn sách không thuộc chi nhánh đã chọn.
Mã phiếu mượn đã tồn tại.
```

Nếu gặp lỗi cuốn sách không sẵn có, hãy chọn cuốn khác hoặc trả cuốn đó trước.

---

## 9. Trả sách

Vào menu:

```text
Trả sách
```

Màn hình này dùng để lập phiếu trả.

### 9.1. Thông tin cần nhập

```text
Mã phiếu trả
Mã độc giả
Mã nhân viên nhận
Mã chi nhánh
Mã chi tiết mượn
Tình trạng khi trả
Tiền phạt hỏng/mất nếu có
```

Ví dụ:

```text
Mã phiếu trả: PTR_FE_00000001
Mã độc giả: DG001
Mã nhân viên nhận: NV_TT001
Mã chi nhánh: CN_TD
Mã chi tiết mượn: CTM_PM_FE_00000001_01
Tình trạng khi trả: Bình thường
Tiền phạt hỏng/mất: 0
```

### 9.2. Tình trạng khi trả

Có 3 lựa chọn:

```text
Bình thường
Hỏng
Mất
```

Nếu sách bình thường:

```text
Tiền phạt hỏng/mất = 0
```

Nếu sách hỏng hoặc mất:

```text
Thủ thư nhập số tiền phạt tương ứng.
```

### 9.3. Sau khi trả sách

Nếu trả bình thường:

```text
Cuốn sách quay lại trạng thái sẵn có.
Phiếu mượn được cập nhật.
```

Nếu trả trễ:

```text
Hệ thống tính số ngày trễ.
Hệ thống tạo khoản nợ tiền phạt trễ.
```

Nếu sách hỏng hoặc mất:

```text
Hệ thống tạo khoản nợ hỏng/mất sách.
Cuốn sách chuyển sang trạng thái hỏng hoặc mất.
```

---

## 10. Thu tiền

Vào menu:

```text
Thu tiền
```

Màn hình này dùng để xem nợ và lập phiếu thu.

### 10.1. Xem khoản nợ

Nhập mã độc giả, ví dụ:

```text
DG003
```

Sau đó nhấn:

```text
Xem nợ
```

Hệ thống sẽ hiển thị:

```text
Mã khoản nợ
Lý do
Số tiền phát sinh
Số tiền còn lại
Trạng thái
```

### 10.2. Tạo phiếu thu

Thông tin cần nhập:

```text
Mã phiếu thu
Mã khoản nợ
Số tiền thu
```

Ví dụ:

```text
Mã phiếu thu: PT_FE_00000001
Mã khoản nợ: NO_DG003_TRE_01
Số tiền thu: 3000
```

Sau đó nhấn:

```text
Tạo phiếu thu
```

Nếu thành công, hệ thống báo:

```text
Thu tiền thành công
```

### 10.3. Lưu ý khi thu tiền

Hệ thống không cho phép thu vượt quá số tiền nợ còn lại.

Ví dụ khoản nợ còn lại 3.000 đồng thì không được thu 10.000 đồng cho khoản nợ đó.

---

## 11. Báo cáo

Vào menu:

```text
Báo cáo
```

Màn hình này dùng để xem thống kê thư viện.

Thông tin cần nhập:

```text
Tháng
Năm
```

Sau đó nhấn:

```text
Xem báo cáo
```

Các báo cáo thường có:

```text
Báo cáo mượn sách theo thể loại
Báo cáo sách trả trễ
```

Nếu báo cáo không có dữ liệu, có thể do tháng/năm được chọn chưa phát sinh nghiệp vụ.

---

## 12. Đăng xuất

Nhấn nút:

```text
Đăng xuất
```

ở góc trên bên phải.

Sau khi đăng xuất, hệ thống quay lại màn hình đăng nhập.

---

## 13. Quy trình sử dụng đề xuất cho demo

Khi demo app, nên thao tác theo thứ tự:

```text
1. Mở backend.
2. Mở LibraDesk.
3. Đăng nhập bằng tài khoản thủ thư.
4. Vào Tổng quan để giới thiệu dashboard.
5. Vào Đầu sách để xem F01.
6. Vào Cuốn sách để xem F01-001 và F01-002.
7. Vào Độc giả để xem DG001.
8. Vào Mượn sách, cho DG001 mượn F01-001.
9. Vào Cuốn sách kiểm tra F01-001 đã chuyển trạng thái.
10. Vào Trả sách, trả cuốn vừa mượn.
11. Vào Thu tiền để xem khoản nợ mẫu.
12. Vào Báo cáo để xem thống kê.
13. Kiểm tra nhật ký hoạt động nếu cần.
```

---

## 14. Dữ liệu mẫu thường dùng

| Mục đích | Mã |
|---|---|
| Tài khoản thủ thư | `thuthu01` |
| Nhân viên thủ thư | `NV_TT001` |
| Độc giả mẫu | `DG001` |
| Chi nhánh | `CN_TD` |
| Đầu sách | `F01` |
| Cuốn sách mẫu | `F01-001`, `F01-002` |
| Khoản nợ mẫu | `NO_DG003_TRE_01` |
| Phương thức thanh toán | `PT_TIEN_MAT` |

---

## 15. Cách xử lý lỗi thường gặp

### 15.1. Không đăng nhập được

Kiểm tra:

```text
Backend đã chạy chưa.
Tên đăng nhập có đúng không.
Mật khẩu có đúng không.
Tài khoản có bị khóa không.
```

### 15.2. App báo Failed to fetch

Thường do backend chưa chạy.

Cách xử lý:

```text
Đóng app.
Chạy backend.
Mở lại app.
Đăng nhập lại.
```

### 15.3. Mượn sách bị lỗi cuốn sách không sẵn có

Nguyên nhân:

```text
Cuốn sách đã được mượn.
Cuốn sách bị hỏng.
Cuốn sách bị mất.
Cuốn sách ngừng lưu thông.
```

Cách xử lý:

```text
Chọn cuốn sách khác.
Hoặc trả sách trước rồi mượn lại.
```

### 15.4. Trả sách báo sai mã chi tiết mượn

Nguyên nhân:

```text
Nhập sai mã chi tiết mượn.
Phiếu mượn chưa tồn tại.
Chi tiết mượn đã được trả trước đó.
```

Cách xử lý:

```text
Xem lại kết quả sau khi tạo phiếu mượn.
Copy đúng mã chi tiết mượn.
Thực hiện trả sách lại.
```

### 15.5. Thu tiền bị lỗi vượt quá khoản nợ

Nguyên nhân:

```text
Số tiền thu lớn hơn số tiền còn lại của khoản nợ.
```

Cách xử lý:

```text
Nhập số tiền nhỏ hơn hoặc bằng số tiền còn lại.
```

---

## 16. Ghi chú

- App desktop cần backend đang chạy để sử dụng được.
- Dữ liệu trong app lấy trực tiếp từ database SQL Server.
- Khi thao tác mượn, trả, thu tiền, hệ thống sẽ cập nhật dữ liệu thật trong database.
- Trước khi demo nhiều lần, nên reset dữ liệu demo để tránh lỗi trùng mã hoặc sách đã bị mượn.

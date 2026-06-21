# Mô tả các bảng chính

Schema đầy đủ nằm trong `database/scripts/01_full_database.sql`. Tài liệu này nhóm bảng theo nghiệp vụ để người đọc dễ tìm quan hệ.

## Tài khoản và tổ chức

| Bảng | Mục đích |
|---|---|
| `VAITRO` | Danh mục vai trò quản trị viên, thủ thư và độc giả |
| `TAIKHOAN` | Thông tin đăng nhập, vai trò và trạng thái tài khoản |
| `CHINHANH` | Thông tin các chi nhánh thư viện |
| `NHANVIEN` | Hồ sơ nhân viên và chi nhánh làm việc |
| `NHATKYHOATDONG` | Nhật ký thao tác quan trọng trong hệ thống |

## Độc giả và gói thành viên

| Bảng | Mục đích |
|---|---|
| `NHOMDOCGIA` | Nhóm học sinh, sinh viên, giáo viên và nhóm khác |
| `GOITHANHVIEN` | Danh mục gói thành viên |
| `DOCGIA` | Hồ sơ, tài khoản, nhóm và thời hạn thẻ độc giả |
| `LICHSUGOITHANHVIEN` | Lịch sử đăng ký, gia hạn hoặc thay đổi gói |

## Kho và danh mục sách

| Bảng | Mục đích |
|---|---|
| `KHU` | Khu vực sách trong một chi nhánh |
| `KESACH` | Kệ thuộc khu vực |
| `VITRISACH` | Vị trí cụ thể dùng để xếp cuốn sách |
| `NHAXUATBAN` | Danh mục nhà xuất bản |
| `TACGIA` | Danh mục tác giả |
| `THELOAI` | Danh mục thể loại |
| `DAUSACH` | Thông tin chung: tên, ISBN, năm xuất bản và trị giá |
| `DAUSACH_TACGIA` | Quan hệ nhiều-nhiều giữa đầu sách và tác giả |
| `DAUSACH_THELOAI` | Quan hệ nhiều-nhiều giữa đầu sách và thể loại |
| `TRANGTHAICUONSACH` | Danh mục trạng thái cuốn sách |
| `CUONSACH` | Từng bản sách vật lý, vị trí, mã vạch, QR và trạng thái |

## Quy định

| Bảng | Mục đích |
|---|---|
| `PHIENBANQUYDINH` | Phiên bản và thời điểm áp dụng quy định |
| `THAMSOQUYDINH` | Các tham số chung của từng phiên bản |
| `GIAGOI_THEONHOM` | Giá gói theo nhóm độc giả |
| `QUYDINHGOI` | Hạn mức sách và số lần gia hạn theo gói |
| `QUYDINHMUON_THELOAI` | Số ngày mượn theo gói và thể loại |

## Mượn, gia hạn và trả

| Bảng | Mục đích |
|---|---|
| `PHIEUMUON` | Thông tin chung của một giao dịch mượn |
| `CHITIETPHIEUMUON` | Từng cuốn sách và hạn trả trong phiếu mượn |
| `LICHSUGIAHAN` | Lịch sử gia hạn từng chi tiết mượn |
| `PHIEUTRA` | Thông tin chung của giao dịch trả |
| `CHITIETPHIEUTRA` | Tình trạng trả, ngày trễ và tiền phạt của từng cuốn |

## Công nợ và thanh toán

| Bảng | Mục đích |
|---|---|
| `LOAIKHOANNO` | Danh mục loại nợ như trả trễ, hỏng hoặc mất sách |
| `PHUONGTHUCTHANHTOAN` | Danh mục phương thức thanh toán |
| `KHOANNO` | Số tiền phát sinh, đã trả, còn lại và trạng thái nợ |
| `PHIEUTHU` | Thông tin mỗi lần thu tiền |
| `CHITIETPHIEUTHU_NO` | Phân bổ tiền của phiếu thu vào từng khoản nợ |

## Tương tác độc giả

| Bảng | Mục đích |
|---|---|
| `PHIEUDATTRUOC` | Đặt trước đầu sách/cuốn sách và thời gian giữ chỗ |
| `DANHGIA` | Điểm đánh giá của độc giả theo đầu sách |
| `BINHLUAN` | Bình luận và trạng thái kiểm duyệt |
| `SACHYEUTHICH` | Danh sách đầu sách yêu thích của độc giả |
| `LOAITHONGBAO` | Danh mục loại thông báo |
| `THONGBAO` | Nội dung, kênh gửi và lịch sử gửi thông báo |

## Quan hệ cần nhớ

- `TAIKHOAN` liên kết một-một với `DOCGIA` hoặc `NHANVIEN` tùy vai trò.
- `DAUSACH` liên kết một-nhiều với `CUONSACH`.
- `PHIEUMUON` liên kết một-nhiều với `CHITIETPHIEUMUON`.
- Một chi tiết mượn có thể được trả bằng một chi tiết trả tương ứng.
- `DOCGIA` liên kết một-nhiều với `KHOANNO` và `PHIEUTHU`.
- `PHIEUTHU` và `KHOANNO` liên kết qua `CHITIETPHIEUTHU_NO`.
- Các bảng quy định tham chiếu `PHIENBANQUYDINH` để giữ lịch sử thay đổi.

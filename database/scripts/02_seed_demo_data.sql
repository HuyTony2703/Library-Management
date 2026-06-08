USE QuanLyThuVien;
GO

/* =========================================================
   02_seed_demo_data.sql
   Dữ liệu mẫu dùng để demo hệ thống
   ========================================================= */

/* 1. Chi nhánh */
IF NOT EXISTS (SELECT 1 FROM CHINHANH WHERE MaChiNhanh = 'CN_TD')
INSERT INTO CHINHANH(MaChiNhanh, TenChiNhanh, DiaChi, SoDienThoai, Email)
VALUES
('CN_TD', N'Chi nhánh Thủ Đức', N'Thủ Đức, TP.HCM', '0280000000', 'thuduc@library.vn');

/* 2. Tài khoản mẫu */
IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_ADMIN')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES
('TK_ADMIN', N'admin', N'$2a$10$demo_hash_admin', 'admin@library.vn', 'VT_ADMIN');

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_THUTHU01')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES
('TK_THUTHU01', N'thuthu01', N'$2a$10$demo_hash_thuthu', 'thuthu01@library.vn', 'VT_THU_THU');

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG001')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES
('TK_DG001', N'docgia01', N'$2a$10$demo_hash_docgia', 'docgia01@gmail.com', 'VT_DOC_GIA');

/* 3. Nhân viên */
IF NOT EXISTS (SELECT 1 FROM NHANVIEN WHERE MaNhanVien = 'NV_ADMIN')
INSERT INTO NHANVIEN(MaNhanVien, MaTaiKhoan, MaChiNhanh, HoTen, NgaySinh, Email, SoDienThoai, DiaChi)
VALUES
('NV_ADMIN', 'TK_ADMIN', NULL, N'Quản trị viên', '1990-01-01', 'admin@library.vn', '0900000000', N'TP.HCM');

IF NOT EXISTS (SELECT 1 FROM NHANVIEN WHERE MaNhanVien = 'NV_TT001')
INSERT INTO NHANVIEN(MaNhanVien, MaTaiKhoan, MaChiNhanh, HoTen, NgaySinh, Email, SoDienThoai, DiaChi)
VALUES
('NV_TT001', 'TK_THUTHU01', 'CN_TD', N'Nguyễn Thủ Thư', '1995-05-20', 'thuthu01@library.vn', '0911111111', N'Thủ Đức');

/* 4. Độc giả */
IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG001')
INSERT INTO DOCGIA(
    MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe
)
VALUES
(
    'DG001', 'TK_DG001', 'NHOM_SINHVIEN', N'Lê Văn A', '2005-06-15',
    N'TP.HCM', 'docgia01@gmail.com', '0922222222',
    CAST(GETDATE() AS DATE),
    DATEADD(MONTH, 6, CAST(GETDATE() AS DATE))
);

/* 5. Vị trí sách */
IF NOT EXISTS (SELECT 1 FROM KHU WHERE MaKhu = 'KHU_MANGA')
INSERT INTO KHU(MaKhu, MaChiNhanh, TenKhu, MoTa)
VALUES
('KHU_MANGA', 'CN_TD', N'Khu Manga', N'Khu vực truyện tranh');

IF NOT EXISTS (SELECT 1 FROM KESACH WHERE MaKeSach = 'KE_M01')
INSERT INTO KESACH(MaKeSach, MaKhu, TenKeSach, MoTa)
VALUES
('KE_M01', 'KHU_MANGA', N'Kệ Manga 01', N'Kệ truyện tranh số 1');

IF NOT EXISTS (SELECT 1 FROM VITRISACH WHERE MaViTri = 'VT_M01_N01')
INSERT INTO VITRISACH(MaViTri, MaKeSach, MaViTriHienThi, MoTa)
VALUES
('VT_M01_N01', 'KE_M01', N'Ngăn 01', N'Ngăn đầu tiên của kệ Manga 01');

/* 6. Nhà xuất bản, tác giả, thể loại */
IF NOT EXISTS (SELECT 1 FROM NHAXUATBAN WHERE MaNhaXuatBan = 'NXB_TRE')
INSERT INTO NHAXUATBAN(MaNhaXuatBan, TenNhaXuatBan, DiaChi)
VALUES
('NXB_TRE', N'Nhà xuất bản Trẻ', N'TP.HCM');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_YAMADA')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES
('TG_YAMADA', N'Kanehito Yamada', N'Tác giả truyện Frieren');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_MANGA')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES
('TL_MANGA', N'Manga', N'Truyện tranh Nhật Bản');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_GIAOTRINH')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES
('TL_GIAOTRINH', N'Giáo trình', N'Sách học tập');

/* 7. Đầu sách */
IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'F01')
INSERT INTO DAUSACH(
    MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia
)
VALUES
(
    'F01', 'NXB_TRE', N'Frieren tập 1', '9780000000011', 2023,
    N'Tiếng Việt', 200, N'Truyện fantasy phiêu lưu', NULL, 55000
);

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'F01' AND MaTacGia = 'TG_YAMADA')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro)
VALUES
('F01', 'TG_YAMADA', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'F01' AND MaTheLoai = 'TL_MANGA')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai)
VALUES
('F01', 'TL_MANGA');

/* 8. Cuốn sách */
IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'F01-001')
INSERT INTO CUONSACH(
    MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode
)
VALUES
('F01-001', 'F01', 'CN_TD', 'VT_M01_N01', 'TT_SANCO', 'BAR-F01-001', 'QR-F01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'F01-002')
INSERT INTO CUONSACH(
    MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode
)
VALUES
('F01-002', 'F01', 'CN_TD', 'VT_M01_N01', 'TT_SANCO', 'BAR-F01-002', 'QR-F01-002');

/* 9. Phiên bản quy định */
IF NOT EXISTS (SELECT 1 FROM PHIENBANQUYDINH WHERE MaPhienBan = 'QD_V1')
INSERT INTO PHIENBANQUYDINH(MaPhienBan, TenPhienBan, NgayApDung, MaNhanVienThayDoi, GhiChu, TrangThai)
VALUES
('QD_V1', N'Quy định mặc định v1', SYSDATETIME(), 'NV_ADMIN', N'Phiên bản quy định đầu tiên', N'Đang áp dụng');

IF NOT EXISTS (SELECT 1 FROM THAMSOQUYDINH WHERE MaThamSo = 'TS_QD_V1')
INSERT INTO THAMSOQUYDINH(
    MaThamSo, MaPhienBan, TuoiToiThieu, TuoiToiDa,
    ThoiHanTheTheoThang, KhoangCachNamXuatBan,
    SoNgayNhacTruocHan, SoNgayGiuDatTruoc, MucPhatTreMoiNgay
)
VALUES
('TS_QD_V1', 'QD_V1', 18, 55, 6, 8, 3, 2, 1000);

IF NOT EXISTS (SELECT 1 FROM GIAGOI_THEONHOM WHERE MaGiaGoi = 'GG_SV_THUONG')
INSERT INTO GIAGOI_THEONHOM(
    MaGiaGoi, MaPhienBan, MaGoiThanhVien, MaNhomDocGia, GiaTien, ThoiHanGoiTheoNgay
)
VALUES
('GG_SV_THUONG', 'QD_V1', 'GOI_THUONG', 'NHOM_SINHVIEN', 0, 180);

IF NOT EXISTS (SELECT 1 FROM QUYDINHGOI WHERE MaQuyDinhGoi = 'QDG_THUONG_V1')
INSERT INTO QUYDINHGOI(
    MaQuyDinhGoi, MaPhienBan, MaGoiThanhVien, SoSachMuonToiDa, SoLanGiaHanToiDa
)
VALUES
('QDG_THUONG_V1', 'QD_V1', 'GOI_THUONG', 5, 1);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_THUONG_MANGA_V1')
INSERT INTO QUYDINHMUON_THELOAI(
    MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan
)
VALUES
('QDM_THUONG_MANGA_V1', 'QD_V1', 'GOI_THUONG', 'TL_MANGA', 4, 2);

/* 10. Gói thành viên của độc giả */
IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG001_01')
INSERT INTO LICHSUGOITHANHVIEN(
    MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu
)
VALUES
(
    'LSG_DG001_01', 'DG001', 'GOI_THUONG', NULL,
    CAST(GETDATE() AS DATE),
    DATEADD(DAY, 180, CAST(GETDATE() AS DATE)),
    N'Đang sử dụng',
    N'Gói mặc định cho dữ liệu demo'
);
GO

USE QuanLyThuVien;
GO

/* =========================================================
   03_seed_more_demo_data.sql
   Dữ liệu mẫu bổ sung dựa trên 02_seed_demo_data.sql
   Chạy sau:
   01_create_tables.sql
   02_seed_demo_data.sql
   ========================================================= */

SET NOCOUNT ON;

/* =========================================================
   0. ĐẢM BẢO DỮ LIỆU DANH MỤC CỐT LÕI
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM VAITRO WHERE MaVaiTro = 'VT_DOC_GIA')
INSERT INTO VAITRO(MaVaiTro, TenVaiTro, MoTa)
VALUES ('VT_DOC_GIA', N'DOC_GIA', N'Độc giả');

IF NOT EXISTS (SELECT 1 FROM VAITRO WHERE MaVaiTro = 'VT_THU_THU')
INSERT INTO VAITRO(MaVaiTro, TenVaiTro, MoTa)
VALUES ('VT_THU_THU', N'THU_THU', N'Thủ thư');

IF NOT EXISTS (SELECT 1 FROM VAITRO WHERE MaVaiTro = 'VT_ADMIN')
INSERT INTO VAITRO(MaVaiTro, TenVaiTro, MoTa)
VALUES ('VT_ADMIN', N'QUAN_TRI_VIEN', N'Quản trị viên');

IF NOT EXISTS (SELECT 1 FROM NHOMDOCGIA WHERE MaNhomDocGia = 'NHOM_HOCSINH')
INSERT INTO NHOMDOCGIA(MaNhomDocGia, TenNhomDocGia, MoTa)
VALUES ('NHOM_HOCSINH', N'Học sinh', N'Nhóm độc giả học sinh');

IF NOT EXISTS (SELECT 1 FROM NHOMDOCGIA WHERE MaNhomDocGia = 'NHOM_SINHVIEN')
INSERT INTO NHOMDOCGIA(MaNhomDocGia, TenNhomDocGia, MoTa)
VALUES ('NHOM_SINHVIEN', N'Sinh viên', N'Nhóm độc giả sinh viên');

IF NOT EXISTS (SELECT 1 FROM NHOMDOCGIA WHERE MaNhomDocGia = 'NHOM_GIAOVIEN')
INSERT INTO NHOMDOCGIA(MaNhomDocGia, TenNhomDocGia, MoTa)
VALUES ('NHOM_GIAOVIEN', N'Giáo viên', N'Nhóm độc giả giáo viên');

IF NOT EXISTS (SELECT 1 FROM NHOMDOCGIA WHERE MaNhomDocGia = 'NHOM_KHAC')
INSERT INTO NHOMDOCGIA(MaNhomDocGia, TenNhomDocGia, MoTa)
VALUES ('NHOM_KHAC', N'Khác', N'Nhóm độc giả khác');

IF NOT EXISTS (SELECT 1 FROM GOITHANHVIEN WHERE MaGoiThanhVien = 'GOI_THUONG')
INSERT INTO GOITHANHVIEN(MaGoiThanhVien, TenGoi, MoTa)
VALUES ('GOI_THUONG', N'Thường', N'Gói thành viên cơ bản');

IF NOT EXISTS (SELECT 1 FROM GOITHANHVIEN WHERE MaGoiThanhVien = 'GOI_VIP')
INSERT INTO GOITHANHVIEN(MaGoiThanhVien, TenGoi, MoTa)
VALUES ('GOI_VIP', N'VIP', N'Gói thành viên nâng cao');

IF NOT EXISTS (SELECT 1 FROM GOITHANHVIEN WHERE MaGoiThanhVien = 'GOI_PREMIUM')
INSERT INTO GOITHANHVIEN(MaGoiThanhVien, TenGoi, MoTa)
VALUES ('GOI_PREMIUM', N'Premium', N'Gói thành viên cao cấp');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_SANCO')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_SANCO', N'Sẵn có', N'Cuốn sách đang sẵn sàng cho mượn');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_DANGMUON')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_DANGMUON', N'Đang được mượn', N'Cuốn sách đang được độc giả mượn');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_DANGDATTRUOC')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_DANGDATTRUOC', N'Đang được đặt trước', N'Cuốn sách đang được giữ chỗ');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_MAT')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_MAT', N'Bị mất', N'Cuốn sách đã bị mất');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_HONG')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_HONG', N'Bị hỏng', N'Cuốn sách bị hư hỏng');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_NGUNGLUUTHONG')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_NGUNGLUUTHONG', N'Ngừng lưu thông', N'Cuốn sách không còn được cho mượn');

IF NOT EXISTS (SELECT 1 FROM LOAIKHOANNO WHERE MaLoaiKhoanNo = 'NO_TRA_TRE')
INSERT INTO LOAIKHOANNO(MaLoaiKhoanNo, TenLoaiKhoanNo, MoTa)
VALUES ('NO_TRA_TRE', N'Trả trễ', N'Nợ phát sinh do trả sách trễ hạn');

IF NOT EXISTS (SELECT 1 FROM LOAIKHOANNO WHERE MaLoaiKhoanNo = 'NO_MAT_SACH')
INSERT INTO LOAIKHOANNO(MaLoaiKhoanNo, TenLoaiKhoanNo, MoTa)
VALUES ('NO_MAT_SACH', N'Mất sách', N'Nợ phát sinh do làm mất sách');

IF NOT EXISTS (SELECT 1 FROM LOAIKHOANNO WHERE MaLoaiKhoanNo = 'NO_HONG_SACH')
INSERT INTO LOAIKHOANNO(MaLoaiKhoanNo, TenLoaiKhoanNo, MoTa)
VALUES ('NO_HONG_SACH', N'Hỏng sách', N'Nợ phát sinh do làm hỏng sách');

IF NOT EXISTS (SELECT 1 FROM PHUONGTHUCTHANHTOAN WHERE MaPhuongThuc = 'PT_TIEN_MAT')
INSERT INTO PHUONGTHUCTHANHTOAN(MaPhuongThuc, TenPhuongThuc, MoTa)
VALUES ('PT_TIEN_MAT', N'Tiền mặt', N'Thanh toán bằng tiền mặt');

IF NOT EXISTS (SELECT 1 FROM PHUONGTHUCTHANHTOAN WHERE MaPhuongThuc = 'PT_CHUYEN_KHOAN')
INSERT INTO PHUONGTHUCTHANHTOAN(MaPhuongThuc, TenPhuongThuc, MoTa)
VALUES ('PT_CHUYEN_KHOAN', N'Chuyển khoản', N'Thanh toán bằng chuyển khoản');

IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = 'TB_SAP_DEN_HAN')
INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
VALUES ('TB_SAP_DEN_HAN', N'Sắp đến hạn trả', N'Thông báo nhắc độc giả sắp đến hạn trả sách');

IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = 'TB_BI_PHAT')
INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
VALUES ('TB_BI_PHAT', N'Bị tính tiền phạt', N'Thông báo phát sinh tiền phạt');

IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = 'TB_SACH_DA_CO')
INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
VALUES ('TB_SACH_DA_CO', N'Sách đặt trước đã có', N'Thông báo sách đặt trước đã sẵn sàng');

/* =========================================================
   1. CHI NHÁNH, NHÂN VIÊN, TÀI KHOẢN NHÂN VIÊN
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM CHINHANH WHERE MaChiNhanh = 'CN_Q1')
INSERT INTO CHINHANH(MaChiNhanh, TenChiNhanh, DiaChi, SoDienThoai, Email)
VALUES ('CN_Q1', N'Chi nhánh Quận 1', N'Quận 1, TP.HCM', '0281111111', 'quan1@library.vn');

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_THUTHU02')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_THUTHU02', N'thuthu02', N'$2a$10$demo_hash_thuthu02', 'thuthu02@library.vn', 'VT_THU_THU');

IF NOT EXISTS (SELECT 1 FROM NHANVIEN WHERE MaNhanVien = 'NV_TT002')
INSERT INTO NHANVIEN(MaNhanVien, MaTaiKhoan, MaChiNhanh, HoTen, NgaySinh, Email, SoDienThoai, DiaChi)
VALUES ('NV_TT002', 'TK_THUTHU02', 'CN_Q1', N'Trần Minh Thư', '1997-09-12', 'thuthu02@library.vn', '0912222222', N'Quận 1');

/* =========================================================
   2. ĐỘC GIẢ VÀ TÀI KHOẢN ĐỘC GIẢ
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG002')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG002', N'docgia02', N'$2a$10$demo_hash_docgia02', 'docgia02@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG002')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG002', 'TK_DG002', 'NHOM_SINHVIEN', N'Nguyễn Minh Khang', '2004-03-21', N'Thủ Đức, TP.HCM', 'docgia02@gmail.com', '0922222202', CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE)));

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG003')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG003', N'docgia03', N'$2a$10$demo_hash_docgia03', 'docgia03@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG003')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG003', 'TK_DG003', 'NHOM_HOCSINH', N'Phạm Gia Hân', '2007-11-05', N'Bình Thạnh, TP.HCM', 'docgia03@gmail.com', '0922222203', CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE)));

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG004')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG004', N'docgia04', N'$2a$10$demo_hash_docgia04', 'docgia04@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG004')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG004', 'TK_DG004', 'NHOM_GIAOVIEN', N'Hoàng Thanh Mai', '1988-08-14', N'Quận 1, TP.HCM', 'docgia04@gmail.com', '0922222204', CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE)));

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG005')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG005', N'docgia05', N'$2a$10$demo_hash_docgia05', 'docgia05@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG005')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG005', 'TK_DG005', 'NHOM_SINHVIEN', N'Võ Nhật Nam', '2003-12-01', N'Gò Vấp, TP.HCM', 'docgia05@gmail.com', '0922222205', CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE)));

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG006')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG006', N'docgia06', N'$2a$10$demo_hash_docgia06', 'docgia06@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG006')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG006', 'TK_DG006', 'NHOM_KHAC', N'Đặng Khánh Linh', '1999-04-18', N'Quận 7, TP.HCM', 'docgia06@gmail.com', '0922222206', CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE)));

/* =========================================================
   3. VỊ TRÍ SÁCH
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM KHU WHERE MaKhu = 'KHU_GIAOTRINH')
INSERT INTO KHU(MaKhu, MaChiNhanh, TenKhu, MoTa)
VALUES ('KHU_GIAOTRINH', 'CN_TD', N'Khu Giáo trình', N'Khu sách học tập và giáo trình');

IF NOT EXISTS (SELECT 1 FROM KHU WHERE MaKhu = 'KHU_TONGHOP_Q1')
INSERT INTO KHU(MaKhu, MaChiNhanh, TenKhu, MoTa)
VALUES ('KHU_TONGHOP_Q1', 'CN_Q1', N'Khu Tổng hợp', N'Khu sách tổng hợp tại Quận 1');

IF NOT EXISTS (SELECT 1 FROM KESACH WHERE MaKeSach = 'KE_G01')
INSERT INTO KESACH(MaKeSach, MaKhu, TenKeSach, MoTa)
VALUES ('KE_G01', 'KHU_GIAOTRINH', N'Kệ Giáo trình 01', N'Kệ giáo trình và sách chuyên ngành');

IF NOT EXISTS (SELECT 1 FROM KESACH WHERE MaKeSach = 'KE_Q1_01')
INSERT INTO KESACH(MaKeSach, MaKhu, TenKeSach, MoTa)
VALUES ('KE_Q1_01', 'KHU_TONGHOP_Q1', N'Kệ Tổng hợp 01', N'Kệ tổng hợp chi nhánh Quận 1');

IF NOT EXISTS (SELECT 1 FROM VITRISACH WHERE MaViTri = 'VT_G01_N01')
INSERT INTO VITRISACH(MaViTri, MaKeSach, MaViTriHienThi, MoTa)
VALUES ('VT_G01_N01', 'KE_G01', N'Ngăn 01', N'Ngăn sách giáo trình');

IF NOT EXISTS (SELECT 1 FROM VITRISACH WHERE MaViTri = 'VT_Q1_01_N01')
INSERT INTO VITRISACH(MaViTri, MaKeSach, MaViTriHienThi, MoTa)
VALUES ('VT_Q1_01_N01', 'KE_Q1_01', N'Ngăn 01', N'Ngăn sách tổng hợp');

/* =========================================================
   4. NHÀ XUẤT BẢN, TÁC GIẢ, THỂ LOẠI
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM NHAXUATBAN WHERE MaNhaXuatBan = 'NXB_KIMDONG')
INSERT INTO NHAXUATBAN(MaNhaXuatBan, TenNhaXuatBan, DiaChi)
VALUES ('NXB_KIMDONG', N'Nhà xuất bản Kim Đồng', N'Hà Nội');

IF NOT EXISTS (SELECT 1 FROM NHAXUATBAN WHERE MaNhaXuatBan = 'NXB_GIAODUC')
INSERT INTO NHAXUATBAN(MaNhaXuatBan, TenNhaXuatBan, DiaChi)
VALUES ('NXB_GIAODUC', N'Nhà xuất bản Giáo Dục', N'Hà Nội');

IF NOT EXISTS (SELECT 1 FROM NHAXUATBAN WHERE MaNhaXuatBan = 'NXB_DHQG')
INSERT INTO NHAXUATBAN(MaNhaXuatBan, TenNhaXuatBan, DiaChi)
VALUES ('NXB_DHQG', N'Nhà xuất bản Đại học Quốc gia TP.HCM', N'TP.HCM');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_ABE')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_ABE', N'Tsukasa Abe', N'Họa sĩ minh họa Frieren');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_GOSHO')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_GOSHO', N'Gosho Aoyama', N'Tác giả truyện trinh thám');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_NNA')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_NNA', N'Nguyễn Nhật Ánh', N'Tác giả văn học Việt Nam');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_ROBERT')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_ROBERT', N'Robert C. Martin', N'Tác giả sách lập trình');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_ANDREW')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_ANDREW', N'Andrew Hunt', N'Tác giả sách công nghệ phần mềm');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_VANHOC')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES ('TL_VANHOC', N'Văn học', N'Tác phẩm văn học');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_CNTT')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES ('TL_CNTT', N'Công nghệ thông tin', N'Sách chuyên ngành CNTT');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_KYNANG')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES ('TL_KYNANG', N'Kỹ năng', N'Sách kỹ năng và phát triển bản thân');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_THIEUNHI')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES ('TL_THIEUNHI', N'Thiếu nhi', N'Sách dành cho thiếu nhi');

/* =========================================================
   5. ĐẦU SÁCH, TÁC GIẢ, THỂ LOẠI
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'F02')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('F02', 'NXB_TRE', N'Frieren tập 2', '9780000000012', 2023, N'Tiếng Việt', 196, N'Tiếp tục hành trình fantasy của Frieren', NULL, 55000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'CONAN01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('CONAN01', 'NXB_KIMDONG', N'Thám tử lừng danh Conan tập 1', '9780000000021', 2022, N'Tiếng Việt', 184, N'Truyện tranh trinh thám', NULL, 30000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'DOR01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('DOR01', 'NXB_KIMDONG', N'Doraemon tập 1', '9780000000031', 2021, N'Tiếng Việt', 190, N'Truyện tranh thiếu nhi', NULL, 25000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'MATBIEC')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('MATBIEC', 'NXB_TRE', N'Mắt biếc', '9780000000041', 2019, N'Tiếng Việt', 300, N'Tác phẩm văn học Việt Nam', NULL, 85000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'CLEAN01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('CLEAN01', 'NXB_DHQG', N'Clean Code', '9780000000051', 2020, N'Tiếng Việt', 450, N'Sách về kỹ thuật viết mã sạch', NULL, 180000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'PRAG01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('PRAG01', 'NXB_DHQG', N'The Pragmatic Programmer', '9780000000061', 2021, N'Tiếng Việt', 380, N'Sách kỹ năng lập trình thực dụng', NULL, 165000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'JAVA01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('JAVA01', 'NXB_GIAODUC', N'Lập trình Java căn bản', '9780000000071', 2024, N'Tiếng Việt', 320, N'Giáo trình nhập môn Java', NULL, 120000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'CSDL01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('CSDL01', 'NXB_DHQG', N'Cơ sở dữ liệu', '9780000000081', 2023, N'Tiếng Việt', 360, N'Giáo trình cơ sở dữ liệu', NULL, 135000);

-- Liên kết tác giả
IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'F02' AND MaTacGia = 'TG_YAMADA')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('F02', 'TG_YAMADA', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'F02' AND MaTacGia = 'TG_ABE')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('F02', 'TG_ABE', N'Minh họa');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'CONAN01' AND MaTacGia = 'TG_GOSHO')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('CONAN01', 'TG_GOSHO', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'MATBIEC' AND MaTacGia = 'TG_NNA')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('MATBIEC', 'TG_NNA', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'CLEAN01' AND MaTacGia = 'TG_ROBERT')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('CLEAN01', 'TG_ROBERT', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'PRAG01' AND MaTacGia = 'TG_ANDREW')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('PRAG01', 'TG_ANDREW', N'Tác giả');

-- Liên kết thể loại
IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'F02' AND MaTheLoai = 'TL_MANGA')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('F02', 'TL_MANGA');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'CONAN01' AND MaTheLoai = 'TL_MANGA')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('CONAN01', 'TL_MANGA');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'DOR01' AND MaTheLoai = 'TL_MANGA')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('DOR01', 'TL_MANGA');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'DOR01' AND MaTheLoai = 'TL_THIEUNHI')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('DOR01', 'TL_THIEUNHI');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'MATBIEC' AND MaTheLoai = 'TL_VANHOC')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('MATBIEC', 'TL_VANHOC');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'CLEAN01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('CLEAN01', 'TL_CNTT');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'PRAG01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('PRAG01', 'TL_CNTT');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'PRAG01' AND MaTheLoai = 'TL_KYNANG')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('PRAG01', 'TL_KYNANG');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'JAVA01' AND MaTheLoai = 'TL_GIAOTRINH')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('JAVA01', 'TL_GIAOTRINH');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'JAVA01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('JAVA01', 'TL_CNTT');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'CSDL01' AND MaTheLoai = 'TL_GIAOTRINH')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('CSDL01', 'TL_GIAOTRINH');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'CSDL01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('CSDL01', 'TL_CNTT');

/* =========================================================
   6. CUỐN SÁCH
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'F02-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('F02-001', 'F02', 'CN_TD', 'VT_M01_N01', 'TT_SANCO', 'BAR-F02-001', 'QR-F02-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'F02-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('F02-002', 'F02', 'CN_TD', 'VT_M01_N01', 'TT_SANCO', 'BAR-F02-002', 'QR-F02-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'CONAN01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('CONAN01-001', 'CONAN01', 'CN_TD', 'VT_M01_N01', 'TT_SANCO', 'BAR-CONAN01-001', 'QR-CONAN01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'CONAN01-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('CONAN01-002', 'CONAN01', 'CN_Q1', 'VT_Q1_01_N01', 'TT_SANCO', 'BAR-CONAN01-002', 'QR-CONAN01-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'DOR01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('DOR01-001', 'DOR01', 'CN_TD', 'VT_M01_N01', 'TT_SANCO', 'BAR-DOR01-001', 'QR-DOR01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'DOR01-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('DOR01-002', 'DOR01', 'CN_Q1', 'VT_Q1_01_N01', 'TT_SANCO', 'BAR-DOR01-002', 'QR-DOR01-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'MATBIEC-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('MATBIEC-001', 'MATBIEC', 'CN_Q1', 'VT_Q1_01_N01', 'TT_SANCO', 'BAR-MATBIEC-001', 'QR-MATBIEC-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'CLEAN01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('CLEAN01-001', 'CLEAN01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-CLEAN01-001', 'QR-CLEAN01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'CLEAN01-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('CLEAN01-002', 'CLEAN01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-CLEAN01-002', 'QR-CLEAN01-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'PRAG01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('PRAG01-001', 'PRAG01', 'CN_Q1', 'VT_Q1_01_N01', 'TT_SANCO', 'BAR-PRAG01-001', 'QR-PRAG01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'JAVA01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('JAVA01-001', 'JAVA01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-JAVA01-001', 'QR-JAVA01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'CSDL01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('CSDL01-001', 'CSDL01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-CSDL01-001', 'QR-CSDL01-001');

/* =========================================================
   7. QUY ĐỊNH BỔ SUNG
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM GIAGOI_THEONHOM WHERE MaGiaGoi = 'GG_HS_THUONG')
INSERT INTO GIAGOI_THEONHOM(MaGiaGoi, MaPhienBan, MaGoiThanhVien, MaNhomDocGia, GiaTien, ThoiHanGoiTheoNgay)
VALUES ('GG_HS_THUONG', 'QD_V1', 'GOI_THUONG', 'NHOM_HOCSINH', 0, 180);

IF NOT EXISTS (SELECT 1 FROM GIAGOI_THEONHOM WHERE MaGiaGoi = 'GG_GV_THUONG')
INSERT INTO GIAGOI_THEONHOM(MaGiaGoi, MaPhienBan, MaGoiThanhVien, MaNhomDocGia, GiaTien, ThoiHanGoiTheoNgay)
VALUES ('GG_GV_THUONG', 'QD_V1', 'GOI_THUONG', 'NHOM_GIAOVIEN', 0, 180);

IF NOT EXISTS (SELECT 1 FROM GIAGOI_THEONHOM WHERE MaGiaGoi = 'GG_SV_VIP')
INSERT INTO GIAGOI_THEONHOM(MaGiaGoi, MaPhienBan, MaGoiThanhVien, MaNhomDocGia, GiaTien, ThoiHanGoiTheoNgay)
VALUES ('GG_SV_VIP', 'QD_V1', 'GOI_VIP', 'NHOM_SINHVIEN', 50000, 180);

IF NOT EXISTS (SELECT 1 FROM GIAGOI_THEONHOM WHERE MaGiaGoi = 'GG_GV_VIP')
INSERT INTO GIAGOI_THEONHOM(MaGiaGoi, MaPhienBan, MaGoiThanhVien, MaNhomDocGia, GiaTien, ThoiHanGoiTheoNgay)
VALUES ('GG_GV_VIP', 'QD_V1', 'GOI_VIP', 'NHOM_GIAOVIEN', 80000, 180);

IF NOT EXISTS (SELECT 1 FROM GIAGOI_THEONHOM WHERE MaGiaGoi = 'GG_KHAC_PREMIUM')
INSERT INTO GIAGOI_THEONHOM(MaGiaGoi, MaPhienBan, MaGoiThanhVien, MaNhomDocGia, GiaTien, ThoiHanGoiTheoNgay)
VALUES ('GG_KHAC_PREMIUM', 'QD_V1', 'GOI_PREMIUM', 'NHOM_KHAC', 150000, 180);

IF NOT EXISTS (SELECT 1 FROM QUYDINHGOI WHERE MaQuyDinhGoi = 'QDG_VIP_V1')
INSERT INTO QUYDINHGOI(MaQuyDinhGoi, MaPhienBan, MaGoiThanhVien, SoSachMuonToiDa, SoLanGiaHanToiDa)
VALUES ('QDG_VIP_V1', 'QD_V1', 'GOI_VIP', 8, 2);

IF NOT EXISTS (SELECT 1 FROM QUYDINHGOI WHERE MaQuyDinhGoi = 'QDG_PREMIUM_V1')
INSERT INTO QUYDINHGOI(MaQuyDinhGoi, MaPhienBan, MaGoiThanhVien, SoSachMuonToiDa, SoLanGiaHanToiDa)
VALUES ('QDG_PREMIUM_V1', 'QD_V1', 'GOI_PREMIUM', 12, 3);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_THUONG_GIAOTRINH_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_THUONG_GIAOTRINH_V1', 'QD_V1', 'GOI_THUONG', 'TL_GIAOTRINH', 7, 3);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_THUONG_CNTT_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_THUONG_CNTT_V1', 'QD_V1', 'GOI_THUONG', 'TL_CNTT', 7, 3);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_VIP_MANGA_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_VIP_MANGA_V1', 'QD_V1', 'GOI_VIP', 'TL_MANGA', 7, 3);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_VIP_CNTT_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_VIP_CNTT_V1', 'QD_V1', 'GOI_VIP', 'TL_CNTT', 14, 5);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_PREMIUM_CNTT_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_PREMIUM_CNTT_V1', 'QD_V1', 'GOI_PREMIUM', 'TL_CNTT', 21, 7);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_THUONG_VANHOC_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_THUONG_VANHOC_V1', 'QD_V1', 'GOI_THUONG', 'TL_VANHOC', 10, 3);

/* =========================================================
   8. THANH TOÁN GÓI VÀ LỊCH SỬ GÓI THÀNH VIÊN
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUTHU WHERE MaPhieuThu = 'PT_GOI_DG002')
INSERT INTO PHIEUTHU(MaPhieuThu, MaDocGia, MaNhanVienThu, MaPhuongThuc, LoaiThu, SoTienThu, NgayThu, TrangThai, GhiChu)
VALUES ('PT_GOI_DG002', 'DG002', 'NV_TT001', 'PT_CHUYEN_KHOAN', N'Thu tiền mua gói', 50000, DATEADD(DAY, -5, SYSDATETIME()), N'Thành công', N'Độc giả mua gói VIP');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG002_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG002_01', 'DG002', 'GOI_VIP', 'PT_GOI_DG002', CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói VIP demo');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG003_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG003_01', 'DG003', 'GOI_THUONG', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói thường demo');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG004_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG004_01', 'DG004', 'GOI_VIP', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói VIP giáo viên demo');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG005_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG005_01', 'DG005', 'GOI_PREMIUM', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói Premium demo');

/* =========================================================
   9. MƯỢN SÁCH ĐANG DIỄN RA
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG002_01')
INSERT INTO PHIEUMUON(MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh, MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu)
VALUES ('PM_DG002_01', 'DG002', 'NV_TT001', 'CN_TD', 'QD_V1', DATEADD(DAY, -1, SYSDATETIME()), N'Đang mượn', N'Phiếu mượn demo đang hoạt động');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG002_F02_001')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG002_F02_001', 'PM_DG002_01', 'F02-001', 'QDM_VIP_MANGA_V1', DATEADD(DAY, -1, SYSDATETIME()), DATEADD(DAY, 6, SYSDATETIME()), NULL, N'Đang mượn');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG002_DOR01_001')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG002_DOR01_001', 'PM_DG002_01', 'DOR01-001', 'QDM_VIP_MANGA_V1', DATEADD(DAY, -1, SYSDATETIME()), DATEADD(DAY, 6, SYSDATETIME()), NULL, N'Đang mượn');

UPDATE CUONSACH SET MaTrangThai = 'TT_DANGMUON'
WHERE MaCuonSach IN ('F02-001', 'DOR01-001')
  AND MaTrangThai <> 'TT_DANGMUON';

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG005_01')
INSERT INTO PHIEUMUON(MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh, MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu)
VALUES ('PM_DG005_01', 'DG005', 'NV_TT001', 'CN_TD', 'QD_V1', DATEADD(DAY, -2, SYSDATETIME()), N'Đang mượn', N'Phiếu mượn giáo trình demo');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG005_JAVA01_001')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG005_JAVA01_001', 'PM_DG005_01', 'JAVA01-001', 'QDM_PREMIUM_CNTT_V1', DATEADD(DAY, -2, SYSDATETIME()), DATEADD(DAY, 19, SYSDATETIME()), NULL, N'Đang mượn');

UPDATE CUONSACH SET MaTrangThai = 'TT_DANGMUON'
WHERE MaCuonSach = 'JAVA01-001'
  AND MaTrangThai <> 'TT_DANGMUON';

/* =========================================================
   10. MƯỢN/TRẢ SÁCH ĐÃ PHÁT SINH PHẠT
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG003_01')
INSERT INTO PHIEUMUON(MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh, MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu)
VALUES ('PM_DG003_01', 'DG003', 'NV_TT001', 'CN_TD', 'QD_V1', DATEADD(DAY, -10, SYSDATETIME()), N'Đã trả hết', N'Phiếu mượn demo trả trễ');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG003_CONAN01_001')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG003_CONAN01_001', 'PM_DG003_01', 'CONAN01-001', 'QDM_THUONG_MANGA_V1', DATEADD(DAY, -10, SYSDATETIME()), DATEADD(DAY, -6, SYSDATETIME()), DATEADD(DAY, -1, SYSDATETIME()), N'Đã trả');

IF NOT EXISTS (SELECT 1 FROM PHIEUTRA WHERE MaPhieuTra = 'PTR_DG003_01')
INSERT INTO PHIEUTRA(MaPhieuTra, MaDocGia, MaNhanVienNhan, MaChiNhanh, NgayTra, GhiChu)
VALUES ('PTR_DG003_01', 'DG003', 'NV_TT001', 'CN_TD', DATEADD(DAY, -1, SYSDATETIME()), N'Phiếu trả trễ demo');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTRA WHERE MaChiTietTra = 'CTT_DG003_CONAN01_001')
INSERT INTO CHITIETPHIEUTRA(MaChiTietTra, MaPhieuTra, MaChiTietMuon, TinhTrangKhiTra, SoNgayTre, TienPhatTre, TienPhatHongMat, GhiChu)
VALUES ('CTT_DG003_CONAN01_001', 'PTR_DG003_01', 'CTM_DG003_CONAN01_001', N'Bình thường', 5, 5000, 0, N'Trả trễ 5 ngày');

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG003_TRE_01')
INSERT INTO KHOANNO(MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra, SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai)
VALUES ('NO_DG003_TRE_01', 'DG003', 'NO_TRA_TRE', 'CTT_DG003_CONAN01_001', 5000, 2000, DATEADD(DAY, -1, SYSDATETIME()), N'Trả trễ Conan tập 1 trong 5 ngày', N'Thanh toán một phần');

IF NOT EXISTS (SELECT 1 FROM PHIEUTHU WHERE MaPhieuThu = 'PT_NO_DG003_01')
INSERT INTO PHIEUTHU(MaPhieuThu, MaDocGia, MaNhanVienThu, MaPhuongThuc, LoaiThu, SoTienThu, NgayThu, TrangThai, GhiChu)
VALUES ('PT_NO_DG003_01', 'DG003', 'NV_TT001', 'PT_TIEN_MAT', N'Thu tiền phạt', 2000, SYSDATETIME(), N'Thành công', N'Đóng một phần tiền phạt trả trễ');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTHU_NO WHERE MaChiTietPhieuThu = 'CTPT_NO_DG003_01')
INSERT INTO CHITIETPHIEUTHU_NO(MaChiTietPhieuThu, MaPhieuThu, MaKhoanNo, SoTienApDung)
VALUES ('CTPT_NO_DG003_01', 'PT_NO_DG003_01', 'NO_DG003_TRE_01', 2000);

UPDATE CUONSACH SET MaTrangThai = 'TT_SANCO'
WHERE MaCuonSach = 'CONAN01-001';

/* =========================================================
   11. ĐẶT TRƯỚC SÁCH
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUDATTRUOC WHERE MaPhieuDatTruoc = 'PDT_DG004_CLEAN01')
INSERT INTO PHIEUDATTRUOC(MaPhieuDatTruoc, MaDocGia, MaDauSach, MaCuonSachDuocGiu, MaChiNhanh, NgayDat, NgayHetHanGiuCho, TrangThai, GhiChu)
VALUES ('PDT_DG004_CLEAN01', 'DG004', 'CLEAN01', 'CLEAN01-002', 'CN_TD', SYSDATETIME(), DATEADD(DAY, 2, SYSDATETIME()), N'Đã giữ chỗ', N'Giữ chỗ sách Clean Code cho giáo viên');

UPDATE CUONSACH SET MaTrangThai = 'TT_DANGDATTRUOC'
WHERE MaCuonSach = 'CLEAN01-002';

/* =========================================================
   12. ĐÁNH GIÁ, BÌNH LUẬN, THÔNG BÁO, NHẬT KÝ
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM DANHGIA WHERE MaDanhGia = 'DGIA_DG001_F01')
INSERT INTO DANHGIA(MaDanhGia, MaDocGia, MaDauSach, SoSao, NoiDung, NgayDanhGia, TrangThai)
VALUES ('DGIA_DG001_F01', 'DG001', 'F01', 5, N'Sách hay, nội dung nhẹ nhàng và cảm động.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM DANHGIA WHERE MaDanhGia = 'DGIA_DG002_F02')
INSERT INTO DANHGIA(MaDanhGia, MaDocGia, MaDauSach, SoSao, NoiDung, NgayDanhGia, TrangThai)
VALUES ('DGIA_DG002_F02', 'DG002', 'F02', 4, N'Phần tiếp theo hấp dẫn, hình vẽ đẹp.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM DANHGIA WHERE MaDanhGia = 'DGIA_DG005_JAVA01')
INSERT INTO DANHGIA(MaDanhGia, MaDocGia, MaDauSach, SoSao, NoiDung, NgayDanhGia, TrangThai)
VALUES ('DGIA_DG005_JAVA01', 'DG005', 'JAVA01', 4, N'Phù hợp cho người mới học Java.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM BINHLUAN WHERE MaBinhLuan = 'BL_DG001_F01_01')
INSERT INTO BINHLUAN(MaBinhLuan, MaDocGia, MaDauSach, NoiDung, NgayBinhLuan, TrangThai)
VALUES ('BL_DG001_F01_01', 'DG001', 'F01', N'Mình thích cách truyện khai thác cảm xúc sau chuyến phiêu lưu.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM BINHLUAN WHERE MaBinhLuan = 'BL_DG002_CLEAN01_01')
INSERT INTO BINHLUAN(MaBinhLuan, MaDocGia, MaDauSach, NoiDung, NgayBinhLuan, TrangThai)
VALUES ('BL_DG002_CLEAN01_01', 'DG002', 'CLEAN01', N'Sách rất nên đọc khi bắt đầu làm project phần mềm.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG002_HANTRA_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG002_HANTRA_01', 'TK_DG002', 'TB_SAP_DEN_HAN', N'Sách sắp đến hạn trả', N'Bạn có sách sắp đến hạn trả, vui lòng trả hoặc gia hạn đúng hạn.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG003_PHAT_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG003_PHAT_01', 'TK_DG003', 'TB_BI_PHAT', N'Bạn có khoản phạt mới', N'Bạn bị phạt 5.000đ do trả sách trễ 5 ngày.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG004_SACHDACO_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG004_SACHDACO_01', 'TK_DG004', 'TB_SACH_DA_CO', N'Sách đặt trước đã có', N'Sách Clean Code đã được giữ chỗ cho bạn trong 2 ngày.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

IF NOT EXISTS (SELECT 1 FROM NHATKYHOATDONG WHERE HanhDong = N'Tạo dữ liệu demo bổ sung' AND MaDoiTuongTacDong = '03_seed_more_demo_data')
INSERT INTO NHATKYHOATDONG(MaTaiKhoan, HanhDong, DoiTuongTacDong, MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet)
VALUES ('TK_ADMIN', N'Tạo dữ liệu demo bổ sung', N'SEED_DATA', '03_seed_more_demo_data', SYSDATETIME(), '127.0.0.1', N'Thêm dữ liệu demo cho sách, độc giả, mượn trả, phạt, đặt trước, đánh giá và thông báo');

GO

USE QuanLyThuVien;
GO

IF NOT EXISTS (SELECT 1 FROM CHINHANH WHERE MaChiNhanh = 'CN001')
INSERT INTO CHINHANH(MaChiNhanh, TenChiNhanh, DiaChi, SoDienThoai, Email, TrangThai)
VALUES ('CN001', N'Chi nhánh Thủ Đức', N'Thủ Đức, TP.HCM', '0900000000', 'thuduc@library.vn', N'Hoạt động');

IF NOT EXISTS (SELECT 1 FROM KHU WHERE MaKhu = 'KHU_MANGA')
INSERT INTO KHU(MaKhu, MaChiNhanh, TenKhu, MoTa)
VALUES ('KHU_MANGA', 'CN001', N'Khu Manga', N'Khu sách truyện tranh');

IF NOT EXISTS (SELECT 1 FROM KESACH WHERE MaKeSach = 'KE_MANGA_01')
INSERT INTO KESACH(MaKeSach, MaKhu, TenKeSach, MoTa)
VALUES ('KE_MANGA_01', 'KHU_MANGA', N'Kệ Manga 01', N'Kệ manga số 1');

IF NOT EXISTS (SELECT 1 FROM VITRISACH WHERE MaViTri = 'VT_MANGA_01')
INSERT INTO VITRISACH(MaViTri, MaKeSach, MaViTriHienThi, MoTa)
VALUES ('VT_MANGA_01', 'KE_MANGA_01', N'Ngăn 01', N'Ngăn đầu tiên của kệ Manga');

USE QuanLyThuVien;
GO

UPDATE TAIKHOAN
SET MatKhauHash = '$2a$10$jd8Oy3CRckc/x3lKiuID4.9ZyqQrvDnrgLir8gcigbYENUtv5dAQm'
WHERE MaTaiKhoan IN ('TK_ADMIN', 'TK_THUTHU01');


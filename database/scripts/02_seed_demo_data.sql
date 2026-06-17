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
('TK_DG001', N'docgia01', N'$2a$10$jd8Oy3CRckc/x3lKiuID4.9ZyqQrvDnrgLir8gcigbYENUtv5dAQm', 'docgia01@gmail.com', 'VT_DOC_GIA');

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

IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = 'TB_QUA_HAN_TRA')
INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
VALUES ('TB_QUA_HAN_TRA', N'Sách đã quá hạn trả', N'Thông báo nhắc độc giả có sách đã quá hạn trả');

IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = 'TB_PHAT_SINH_PHAT')
INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
VALUES ('TB_PHAT_SINH_PHAT', N'Phát sinh tiền phạt', N'Thông báo phát sinh tiền phạt mới');

IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = 'TB_MUA_GOI_TC')
INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
VALUES ('TB_MUA_GOI_TC', N'Mua hoặc gia hạn gói thành viên thành công', N'Thông báo mua hoặc gia hạn gói thành viên thành công');

IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = 'TB_GOI_SAP_HET_HAN')
INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
VALUES ('TB_GOI_SAP_HET_HAN', N'Gói thành viên sắp hết hạn', N'Thông báo nhắc gói thành viên sắp hết hạn');

IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = 'TB_TAIKHOAN_THE_DOI_TRANGTHAI')
INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
VALUES ('TB_TAIKHOAN_THE_DOI_TRANGTHAI', N'Tài khoản hoặc thẻ độc giả thay đổi trạng thái', N'Thông báo khi tài khoản hoặc thẻ độc giả thay đổi trạng thái');

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
WHERE MaTaiKhoan IN ('TK_ADMIN', 'TK_THUTHU01', 'TK_DG001');

USE QuanLyThuVien;
GO

/* =========================================================
   04_seed_extra_demo_data.sql
   Dữ liệu mẫu bổ sung thêm
   Chạy sau:
   01_create_tables.sql
   02_seed_demo_data.sql
   03_seed_more_demo_data.sql

   Ghi chú:
   - Giữ đúng phong cách seed hiện tại: IF NOT EXISTS
   - Bổ sung dữ liệu để demo: quản lý thủ thư, độc giả, sách,
     mượn/trả, quá hạn, mất/hỏng, đặt trước, thông báo, đánh giá,
     bình luận, nhật ký hoạt động.
   ========================================================= */

SET NOCOUNT ON;

/* =========================================================
   0. BỔ SUNG LOẠI THÔNG BÁO PHỤC VỤ DEMO

   Lưu ý sửa lỗi:
   - Bảng LOAITHONGBAO đang có UNIQUE theo TenLoaiThongBao.
   - Vì vậy phải kiểm tra cả MaLoaiThongBao và TenLoaiThongBao.
   - Nếu tên loại thông báo đã tồn tại với mã khác, script sẽ dùng lại mã đã có.
   ========================================================= */

DECLARE @MaLoaiTB_QuaHan NVARCHAR(50);
DECLARE @MaLoaiTB_SapHetHanThe NVARCHAR(50);
DECLARE @MaLoaiTB_HetHanGoi NVARCHAR(50);
DECLARE @MaLoaiTB_GiaHanThanhCong NVARCHAR(50);

SELECT TOP 1 @MaLoaiTB_QuaHan = MaLoaiThongBao
FROM LOAITHONGBAO
WHERE MaLoaiThongBao = 'TB_QUA_HAN' OR TenLoaiThongBao = N'Quá hạn trả sách';

IF @MaLoaiTB_QuaHan IS NULL
BEGIN
    INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
    VALUES ('TB_QUA_HAN', N'Quá hạn trả sách', N'Thông báo độc giả đã quá hạn trả sách');
    SET @MaLoaiTB_QuaHan = 'TB_QUA_HAN';
END

SELECT TOP 1 @MaLoaiTB_SapHetHanThe = MaLoaiThongBao
FROM LOAITHONGBAO
WHERE MaLoaiThongBao = 'TB_SAP_HET_HAN_THE' OR TenLoaiThongBao = N'Sắp hết hạn thẻ';

IF @MaLoaiTB_SapHetHanThe IS NULL
BEGIN
    INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
    VALUES ('TB_SAP_HET_HAN_THE', N'Sắp hết hạn thẻ', N'Thông báo thẻ độc giả sắp hết hạn');
    SET @MaLoaiTB_SapHetHanThe = 'TB_SAP_HET_HAN_THE';
END

SELECT TOP 1 @MaLoaiTB_HetHanGoi = MaLoaiThongBao
FROM LOAITHONGBAO
WHERE MaLoaiThongBao = 'TB_HET_HAN_GOI' OR TenLoaiThongBao = N'Hết hạn gói thành viên';

IF @MaLoaiTB_HetHanGoi IS NULL
BEGIN
    INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
    VALUES ('TB_HET_HAN_GOI', N'Hết hạn gói thành viên', N'Thông báo gói thành viên đã hết hạn');
    SET @MaLoaiTB_HetHanGoi = 'TB_HET_HAN_GOI';
END

SELECT TOP 1 @MaLoaiTB_GiaHanThanhCong = MaLoaiThongBao
FROM LOAITHONGBAO
WHERE MaLoaiThongBao = 'TB_GIA_HAN_THANH_CONG' OR TenLoaiThongBao = N'Gia hạn thành công';

IF @MaLoaiTB_GiaHanThanhCong IS NULL
BEGIN
    INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
    VALUES ('TB_GIA_HAN_THANH_CONG', N'Gia hạn thành công', N'Thông báo gia hạn mượn sách thành công');
    SET @MaLoaiTB_GiaHanThanhCong = 'TB_GIA_HAN_THANH_CONG';
END

/* =========================================================
   1. CHI NHÁNH, THỦ THƯ, TÀI KHOẢN THỦ THƯ
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM CHINHANH WHERE MaChiNhanh = 'CN_BTH')
INSERT INTO CHINHANH(MaChiNhanh, TenChiNhanh, DiaChi, SoDienThoai, Email)
VALUES ('CN_BTH', N'Chi nhánh Bình Thạnh', N'Bình Thạnh, TP.HCM', '0282222222', 'binhthanh@library.vn');

IF NOT EXISTS (SELECT 1 FROM CHINHANH WHERE MaChiNhanh = 'CN_Q7')
INSERT INTO CHINHANH(MaChiNhanh, TenChiNhanh, DiaChi, SoDienThoai, Email)
VALUES ('CN_Q7', N'Chi nhánh Quận 7', N'Quận 7, TP.HCM', '0283333333', 'quan7@library.vn');

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_THUTHU03')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_THUTHU03', N'thuthu03', N'$2a$10$demo_hash_thuthu03', 'thuthu03@library.vn', 'VT_THU_THU');

IF NOT EXISTS (SELECT 1 FROM NHANVIEN WHERE MaNhanVien = 'NV_TT003')
INSERT INTO NHANVIEN(MaNhanVien, MaTaiKhoan, MaChiNhanh, HoTen, NgaySinh, Email, SoDienThoai, DiaChi)
VALUES ('NV_TT003', 'TK_THUTHU03', 'CN_BTH', N'Lê Bảo Ngọc', '1996-02-10', 'thuthu03@library.vn', '0913333333', N'Bình Thạnh');

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_THUTHU04')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_THUTHU04', N'thuthu04', N'$2a$10$demo_hash_thuthu04', 'thuthu04@library.vn', 'VT_THU_THU');

IF NOT EXISTS (SELECT 1 FROM NHANVIEN WHERE MaNhanVien = 'NV_TT004')
INSERT INTO NHANVIEN(MaNhanVien, MaTaiKhoan, MaChiNhanh, HoTen, NgaySinh, Email, SoDienThoai, DiaChi)
VALUES ('NV_TT004', 'TK_THUTHU04', 'CN_Q7', N'Phan Quốc Huy', '1994-12-22', 'thuthu04@library.vn', '0914444444', N'Quận 7');

/* =========================================================
   2. ĐỘC GIẢ VÀ TÀI KHOẢN ĐỘC GIẢ
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG007')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG007', N'docgia07', N'$2a$10$demo_hash_docgia07', 'docgia07@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG007')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG007', 'TK_DG007', 'NHOM_SINHVIEN', N'Bùi Anh Tuấn', '2005-01-09', N'Thủ Đức, TP.HCM', 'docgia07@gmail.com', '0922222207', CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE)));

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG008')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG008', N'docgia08', N'$2a$10$demo_hash_docgia08', 'docgia08@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG008')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG008', 'TK_DG008', 'NHOM_GIAOVIEN', N'Trương Mỹ Linh', '1985-07-19', N'Quận 7, TP.HCM', 'docgia08@gmail.com', '0922222208', DATEADD(MONTH, -1, CAST(GETDATE() AS DATE)), DATEADD(MONTH, 5, CAST(GETDATE() AS DATE)));

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG009')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG009', N'docgia09', N'$2a$10$demo_hash_docgia09', 'docgia09@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG009')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG009', 'TK_DG009', 'NHOM_HOCSINH', N'Ngô Gia Bảo', '2008-10-03', N'Bình Thạnh, TP.HCM', 'docgia09@gmail.com', '0922222209', DATEADD(MONTH, -2, CAST(GETDATE() AS DATE)), DATEADD(MONTH, 4, CAST(GETDATE() AS DATE)));

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG010')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG010', N'docgia10', N'$2a$10$demo_hash_docgia10', 'docgia10@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG010')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG010', 'TK_DG010', 'NHOM_KHAC', N'Đỗ Minh Quân', '1998-06-26', N'Gò Vấp, TP.HCM', 'docgia10@gmail.com', '0922222210', DATEADD(MONTH, -1, CAST(GETDATE() AS DATE)), DATEADD(MONTH, 5, CAST(GETDATE() AS DATE)));

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG011')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG011', N'docgia11', N'$2a$10$demo_hash_docgia11', 'docgia11@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG011')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG011', 'TK_DG011', 'NHOM_SINHVIEN', N'Huỳnh Nhật Minh', '2004-09-11', N'Tân Bình, TP.HCM', 'docgia11@gmail.com', '0922222211', DATEADD(MONTH, -7, CAST(GETDATE() AS DATE)), DATEADD(DAY, -3, CAST(GETDATE() AS DATE)));

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG012')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG012', N'docgia12', N'$2a$10$demo_hash_docgia12', 'docgia12@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG012')
INSERT INTO DOCGIA(MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen, NgaySinh, DiaChi, Email, SoDienThoai, NgayLapThe, NgayHetHanThe)
VALUES ('DG012', 'TK_DG012', 'NHOM_GIAOVIEN', N'Vũ Thị Thu Hà', '1991-04-04', N'Quận 1, TP.HCM', 'docgia12@gmail.com', '0922222212', DATEADD(MONTH, -5, CAST(GETDATE() AS DATE)), DATEADD(DAY, 5, CAST(GETDATE() AS DATE)));

/* =========================================================
   3. VỊ TRÍ SÁCH BỔ SUNG
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM KHU WHERE MaKhu = 'KHU_VANHOC_BTH')
INSERT INTO KHU(MaKhu, MaChiNhanh, TenKhu, MoTa)
VALUES ('KHU_VANHOC_BTH', 'CN_BTH', N'Khu Văn học', N'Khu sách văn học tại Bình Thạnh');

IF NOT EXISTS (SELECT 1 FROM KHU WHERE MaKhu = 'KHU_CNTT_BTH')
INSERT INTO KHU(MaKhu, MaChiNhanh, TenKhu, MoTa)
VALUES ('KHU_CNTT_BTH', 'CN_BTH', N'Khu Công nghệ thông tin', N'Khu sách CNTT tại Bình Thạnh');

IF NOT EXISTS (SELECT 1 FROM KHU WHERE MaKhu = 'KHU_THIEUNHI_Q7')
INSERT INTO KHU(MaKhu, MaChiNhanh, TenKhu, MoTa)
VALUES ('KHU_THIEUNHI_Q7', 'CN_Q7', N'Khu Thiếu nhi', N'Khu sách thiếu nhi tại Quận 7');

IF NOT EXISTS (SELECT 1 FROM KESACH WHERE MaKeSach = 'KE_VH_BTH_01')
INSERT INTO KESACH(MaKeSach, MaKhu, TenKeSach, MoTa)
VALUES ('KE_VH_BTH_01', 'KHU_VANHOC_BTH', N'Kệ Văn học 01', N'Kệ văn học Việt Nam');

IF NOT EXISTS (SELECT 1 FROM KESACH WHERE MaKeSach = 'KE_CNTT_BTH_01')
INSERT INTO KESACH(MaKeSach, MaKhu, TenKeSach, MoTa)
VALUES ('KE_CNTT_BTH_01', 'KHU_CNTT_BTH', N'Kệ CNTT 01', N'Kệ sách lập trình và hệ thống');

IF NOT EXISTS (SELECT 1 FROM KESACH WHERE MaKeSach = 'KE_TN_Q7_01')
INSERT INTO KESACH(MaKeSach, MaKhu, TenKeSach, MoTa)
VALUES ('KE_TN_Q7_01', 'KHU_THIEUNHI_Q7', N'Kệ Thiếu nhi 01', N'Kệ sách thiếu nhi');

IF NOT EXISTS (SELECT 1 FROM VITRISACH WHERE MaViTri = 'VT_VH_BTH_01')
INSERT INTO VITRISACH(MaViTri, MaKeSach, MaViTriHienThi, MoTa)
VALUES ('VT_VH_BTH_01', 'KE_VH_BTH_01', N'Ngăn 01', N'Ngăn sách văn học nổi bật');

IF NOT EXISTS (SELECT 1 FROM VITRISACH WHERE MaViTri = 'VT_CNTT_BTH_01')
INSERT INTO VITRISACH(MaViTri, MaKeSach, MaViTriHienThi, MoTa)
VALUES ('VT_CNTT_BTH_01', 'KE_CNTT_BTH_01', N'Ngăn 01', N'Ngăn sách lập trình');

IF NOT EXISTS (SELECT 1 FROM VITRISACH WHERE MaViTri = 'VT_TN_Q7_01')
INSERT INTO VITRISACH(MaViTri, MaKeSach, MaViTriHienThi, MoTa)
VALUES ('VT_TN_Q7_01', 'KE_TN_Q7_01', N'Ngăn 01', N'Ngăn sách thiếu nhi');

/* =========================================================
   4. NHÀ XUẤT BẢN, TÁC GIẢ, THỂ LOẠI BỔ SUNG
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM NHAXUATBAN WHERE MaNhaXuatBan = 'NXB_LAO_DONG')
INSERT INTO NHAXUATBAN(MaNhaXuatBan, TenNhaXuatBan, DiaChi)
VALUES ('NXB_LAO_DONG', N'Nhà xuất bản Lao Động', N'Hà Nội');

IF NOT EXISTS (SELECT 1 FROM NHAXUATBAN WHERE MaNhaXuatBan = 'NXB_THONGTIN')
INSERT INTO NHAXUATBAN(MaNhaXuatBan, TenNhaXuatBan, DiaChi)
VALUES ('NXB_THONGTIN', N'Nhà xuất bản Thông tin và Truyền thông', N'Hà Nội');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_EIICHIRO')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_EIICHIRO', N'Eiichiro Oda', N'Tác giả truyện One Piece');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_KISHIMOTO')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_KISHIMOTO', N'Masashi Kishimoto', N'Tác giả truyện Naruto');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_MARTIN_FOWLER')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_MARTIN_FOWLER', N'Martin Fowler', N'Tác giả sách thiết kế phần mềm');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_KATHY')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_KATHY', N'Kathy Sierra', N'Tác giả sách Java');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_TANENBAUM')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_TANENBAUM', N'Andrew S. Tanenbaum', N'Tác giả sách hệ điều hành và mạng máy tính');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_MANGMAYTINH')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES ('TL_MANGMAYTINH', N'Mạng máy tính', N'Sách về mạng máy tính');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_HEDIEUHANH')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES ('TL_HEDIEUHANH', N'Hệ điều hành', N'Sách về hệ điều hành');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_KIENTRUC_PM')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES ('TL_KIENTRUC_PM', N'Kiến trúc phần mềm', N'Sách về kiến trúc và thiết kế phần mềm');

/* =========================================================
   5. ĐẦU SÁCH, TÁC GIẢ, THỂ LOẠI BỔ SUNG
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'ONEPIECE01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('ONEPIECE01', 'NXB_KIMDONG', N'One Piece tập 1', '9780000000091', 2021, N'Tiếng Việt', 208, N'Truyện tranh phiêu lưu', NULL, 32000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'NARUTO01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('NARUTO01', 'NXB_KIMDONG', N'Naruto tập 1', '9780000000101', 2020, N'Tiếng Việt', 192, N'Truyện tranh ninja', NULL, 30000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'HOAVANG')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('HOAVANG', 'NXB_TRE', N'Tôi thấy hoa vàng trên cỏ xanh', '9780000000111', 2018, N'Tiếng Việt', 380, N'Tác phẩm văn học Việt Nam', NULL, 95000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'REFACTOR01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('REFACTOR01', 'NXB_DHQG', N'Refactoring', '9780000000121', 2022, N'Tiếng Việt', 520, N'Sách về cải tiến cấu trúc mã nguồn', NULL, 220000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'HEADJAVA')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('HEADJAVA', 'NXB_GIAODUC', N'Head First Java', '9780000000131', 2023, N'Tiếng Việt', 600, N'Sách Java cho người mới học', NULL, 210000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'OS01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('OS01', 'NXB_THONGTIN', N'Hệ điều hành hiện đại', '9780000000141', 2019, N'Tiếng Việt', 480, N'Giáo trình hệ điều hành', NULL, 150000);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'NETWORK01')
INSERT INTO DAUSACH(MaDauSach, MaNhaXuatBan, TenDauSach, ISBN, NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia)
VALUES ('NETWORK01', 'NXB_THONGTIN', N'Mạng máy tính căn bản', '9780000000151', 2020, N'Tiếng Việt', 420, N'Giáo trình mạng máy tính', NULL, 145000);

-- Liên kết tác giả
IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'ONEPIECE01' AND MaTacGia = 'TG_EIICHIRO')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('ONEPIECE01', 'TG_EIICHIRO', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'NARUTO01' AND MaTacGia = 'TG_KISHIMOTO')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('NARUTO01', 'TG_KISHIMOTO', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'HOAVANG' AND MaTacGia = 'TG_NNA')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('HOAVANG', 'TG_NNA', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'REFACTOR01' AND MaTacGia = 'TG_MARTIN_FOWLER')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('REFACTOR01', 'TG_MARTIN_FOWLER', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'HEADJAVA' AND MaTacGia = 'TG_KATHY')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('HEADJAVA', 'TG_KATHY', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'OS01' AND MaTacGia = 'TG_TANENBAUM')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('OS01', 'TG_TANENBAUM', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'NETWORK01' AND MaTacGia = 'TG_TANENBAUM')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro) VALUES ('NETWORK01', 'TG_TANENBAUM', N'Tác giả');

-- Liên kết thể loại
IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'ONEPIECE01' AND MaTheLoai = 'TL_MANGA')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('ONEPIECE01', 'TL_MANGA');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'NARUTO01' AND MaTheLoai = 'TL_MANGA')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('NARUTO01', 'TL_MANGA');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'HOAVANG' AND MaTheLoai = 'TL_VANHOC')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('HOAVANG', 'TL_VANHOC');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'REFACTOR01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('REFACTOR01', 'TL_CNTT');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'REFACTOR01' AND MaTheLoai = 'TL_KIENTRUC_PM')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('REFACTOR01', 'TL_KIENTRUC_PM');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'HEADJAVA' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('HEADJAVA', 'TL_CNTT');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'HEADJAVA' AND MaTheLoai = 'TL_GIAOTRINH')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('HEADJAVA', 'TL_GIAOTRINH');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'OS01' AND MaTheLoai = 'TL_HEDIEUHANH')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('OS01', 'TL_HEDIEUHANH');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'OS01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('OS01', 'TL_CNTT');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'NETWORK01' AND MaTheLoai = 'TL_MANGMAYTINH')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('NETWORK01', 'TL_MANGMAYTINH');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'NETWORK01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai) VALUES ('NETWORK01', 'TL_CNTT');

/* =========================================================
   6. CUỐN SÁCH BỔ SUNG
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'ONEPIECE01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('ONEPIECE01-001', 'ONEPIECE01', 'CN_TD', 'VT_M01_N01', 'TT_SANCO', 'BAR-ONEPIECE01-001', 'QR-ONEPIECE01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'ONEPIECE01-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('ONEPIECE01-002', 'ONEPIECE01', 'CN_BTH', 'VT_VH_BTH_01', 'TT_SANCO', 'BAR-ONEPIECE01-002', 'QR-ONEPIECE01-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'NARUTO01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('NARUTO01-001', 'NARUTO01', 'CN_Q7', 'VT_TN_Q7_01', 'TT_SANCO', 'BAR-NARUTO01-001', 'QR-NARUTO01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'NARUTO01-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('NARUTO01-002', 'NARUTO01', 'CN_TD', 'VT_M01_N01', 'TT_SANCO', 'BAR-NARUTO01-002', 'QR-NARUTO01-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'HOAVANG-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('HOAVANG-001', 'HOAVANG', 'CN_BTH', 'VT_VH_BTH_01', 'TT_SANCO', 'BAR-HOAVANG-001', 'QR-HOAVANG-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'REFACTOR01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('REFACTOR01-001', 'REFACTOR01', 'CN_BTH', 'VT_CNTT_BTH_01', 'TT_SANCO', 'BAR-REFACTOR01-001', 'QR-REFACTOR01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'REFACTOR01-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('REFACTOR01-002', 'REFACTOR01', 'CN_Q1', 'VT_Q1_01_N01', 'TT_SANCO', 'BAR-REFACTOR01-002', 'QR-REFACTOR01-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'HEADJAVA-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('HEADJAVA-001', 'HEADJAVA', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-HEADJAVA-001', 'QR-HEADJAVA-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'HEADJAVA-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('HEADJAVA-002', 'HEADJAVA', 'CN_BTH', 'VT_CNTT_BTH_01', 'TT_SANCO', 'BAR-HEADJAVA-002', 'QR-HEADJAVA-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'OS01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('OS01-001', 'OS01', 'CN_BTH', 'VT_CNTT_BTH_01', 'TT_SANCO', 'BAR-OS01-001', 'QR-OS01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'NETWORK01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('NETWORK01-001', 'NETWORK01', 'CN_Q7', 'VT_TN_Q7_01', 'TT_SANCO', 'BAR-NETWORK01-001', 'QR-NETWORK01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'NETWORK01-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('NETWORK01-002', 'NETWORK01', 'CN_BTH', 'VT_CNTT_BTH_01', 'TT_SANCO', 'BAR-NETWORK01-002', 'QR-NETWORK01-002');

/* =========================================================
   7. QUY ĐỊNH MƯỢN BỔ SUNG CHO THỂ LOẠI MỚI
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_THUONG_MANGMAYTINH_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_THUONG_MANGMAYTINH_V1', 'QD_V1', 'GOI_THUONG', 'TL_MANGMAYTINH', 7, 3);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_THUONG_HEDIEUHANH_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_THUONG_HEDIEUHANH_V1', 'QD_V1', 'GOI_THUONG', 'TL_HEDIEUHANH', 7, 3);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_VIP_KIENTRUC_PM_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_VIP_KIENTRUC_PM_V1', 'QD_V1', 'GOI_VIP', 'TL_KIENTRUC_PM', 14, 5);

IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_PREMIUM_HEDIEUHANH_V1')
INSERT INTO QUYDINHMUON_THELOAI(MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai, SoNgayMuon, SoNgayGiaHanMoiLan)
VALUES ('QDM_PREMIUM_HEDIEUHANH_V1', 'QD_V1', 'GOI_PREMIUM', 'TL_HEDIEUHANH', 21, 7);

/* =========================================================
   8. THANH TOÁN GÓI VÀ LỊCH SỬ GÓI THÀNH VIÊN
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUTHU WHERE MaPhieuThu = 'PT_GOI_DG007')
INSERT INTO PHIEUTHU(MaPhieuThu, MaDocGia, MaNhanVienThu, MaPhuongThuc, LoaiThu, SoTienThu, NgayThu, TrangThai, GhiChu)
VALUES ('PT_GOI_DG007', 'DG007', 'NV_TT003', 'PT_TIEN_MAT', N'Thu tiền mua gói', 50000, DATEADD(DAY, -3, SYSDATETIME()), N'Thành công', N'Độc giả mua gói VIP tại Bình Thạnh');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG007_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG007_01', 'DG007', 'GOI_VIP', 'PT_GOI_DG007', DATEADD(DAY, -3, CAST(GETDATE() AS DATE)), DATEADD(DAY, 177, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói VIP demo bổ sung');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG008_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG008_01', 'DG008', 'GOI_PREMIUM', NULL, DATEADD(DAY, -20, CAST(GETDATE() AS DATE)), DATEADD(DAY, 160, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói Premium giáo viên demo');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG009_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG009_01', 'DG009', 'GOI_THUONG', NULL, DATEADD(DAY, -30, CAST(GETDATE() AS DATE)), DATEADD(DAY, 150, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói thường demo');

IF NOT EXISTS (SELECT 1 FROM PHIEUTHU WHERE MaPhieuThu = 'PT_GOI_DG010')
INSERT INTO PHIEUTHU(MaPhieuThu, MaDocGia, MaNhanVienThu, MaPhuongThuc, LoaiThu, SoTienThu, NgayThu, TrangThai, GhiChu)
VALUES ('PT_GOI_DG010', 'DG010', 'NV_TT004', 'PT_CHUYEN_KHOAN', N'Thu tiền mua gói', 150000, DATEADD(DAY, -15, SYSDATETIME()), N'Thành công', N'Độc giả mua gói Premium');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG010_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG010_01', 'DG010', 'GOI_PREMIUM', 'PT_GOI_DG010', DATEADD(DAY, -15, CAST(GETDATE() AS DATE)), DATEADD(DAY, 165, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói Premium demo');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG011_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG011_01', 'DG011', 'GOI_THUONG', NULL, DATEADD(DAY, -190, CAST(GETDATE() AS DATE)), DATEADD(DAY, -10, CAST(GETDATE() AS DATE)), N'Hết hạn', N'Gói thường đã hết hạn để demo thông báo');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG012_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG012_01', 'DG012', 'GOI_VIP', NULL, DATEADD(DAY, -175, CAST(GETDATE() AS DATE)), DATEADD(DAY, 5, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói VIP sắp hết hạn để demo nhắc nhở');

/* =========================================================
   9. MƯỢN SÁCH ĐANG DIỄN RA
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG007_01')
INSERT INTO PHIEUMUON(MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh, MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu)
VALUES ('PM_DG007_01', 'DG007', 'NV_TT003', 'CN_BTH', 'QD_V1', DATEADD(DAY, -2, SYSDATETIME()), N'Đang mượn', N'Phiếu mượn demo tại chi nhánh Bình Thạnh');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG007_REFACTOR01_001')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG007_REFACTOR01_001', 'PM_DG007_01', 'REFACTOR01-001', 'QDM_VIP_KIENTRUC_PM_V1', DATEADD(DAY, -2, SYSDATETIME()), DATEADD(DAY, 12, SYSDATETIME()), NULL, N'Đang mượn');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG007_HEADJAVA_002')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG007_HEADJAVA_002', 'PM_DG007_01', 'HEADJAVA-002', 'QDM_VIP_CNTT_V1', DATEADD(DAY, -2, SYSDATETIME()), DATEADD(DAY, 12, SYSDATETIME()), NULL, N'Đang mượn');

UPDATE CUONSACH SET MaTrangThai = 'TT_DANGMUON'
WHERE MaCuonSach IN ('REFACTOR01-001', 'HEADJAVA-002')
  AND MaTrangThai <> 'TT_DANGMUON';

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG008_01')
INSERT INTO PHIEUMUON(MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh, MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu)
VALUES ('PM_DG008_01', 'DG008', 'NV_TT004', 'CN_Q7', 'QD_V1', DATEADD(DAY, -1, SYSDATETIME()), N'Đang mượn', N'Phiếu mượn giáo viên tại Quận 7');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG008_NETWORK01_001')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG008_NETWORK01_001', 'PM_DG008_01', 'NETWORK01-001', 'QDM_PREMIUM_CNTT_V1', DATEADD(DAY, -1, SYSDATETIME()), DATEADD(DAY, 20, SYSDATETIME()), NULL, N'Đang mượn');

UPDATE CUONSACH SET MaTrangThai = 'TT_DANGMUON'
WHERE MaCuonSach = 'NETWORK01-001'
  AND MaTrangThai <> 'TT_DANGMUON';

/* =========================================================
   10. MƯỢN SÁCH ĐANG QUÁ HẠN
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG009_01')
INSERT INTO PHIEUMUON(MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh, MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu)
VALUES ('PM_DG009_01', 'DG009', 'NV_TT003', 'CN_BTH', 'QD_V1', DATEADD(DAY, -12, SYSDATETIME()), N'Đang mượn', N'Phiếu mượn demo quá hạn');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG009_ONEPIECE01_001')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG009_ONEPIECE01_001', 'PM_DG009_01', 'ONEPIECE01-001', 'QDM_THUONG_MANGA_V1', DATEADD(DAY, -12, SYSDATETIME()), DATEADD(DAY, -8, SYSDATETIME()), NULL, N'Đang mượn');

UPDATE CUONSACH SET MaTrangThai = 'TT_DANGMUON'
WHERE MaCuonSach = 'ONEPIECE01-001'
  AND MaTrangThai <> 'TT_DANGMUON';

/* =========================================================
   11. GIA HẠN MƯỢN SÁCH
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG012_01')
INSERT INTO PHIEUMUON(MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh, MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu)
VALUES ('PM_DG012_01', 'DG012', 'NV_TT002', 'CN_Q1', 'QD_V1', DATEADD(DAY, -5, SYSDATETIME()), N'Đang mượn', N'Phiếu mượn đã gia hạn demo');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG012_REFACTOR01_002')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG012_REFACTOR01_002', 'PM_DG012_01', 'REFACTOR01-002', 'QDM_VIP_KIENTRUC_PM_V1', DATEADD(DAY, -5, SYSDATETIME()), DATEADD(DAY, 14, SYSDATETIME()), NULL, N'Đang mượn');

UPDATE CUONSACH SET MaTrangThai = 'TT_DANGMUON'
WHERE MaCuonSach = 'REFACTOR01-002'
  AND MaTrangThai <> 'TT_DANGMUON';

/* =========================================================
   12. MƯỢN/TRẢ ĐÃ PHÁT SINH PHẠT DO HỎNG SÁCH
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG010_01')
INSERT INTO PHIEUMUON(MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh, MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu)
VALUES ('PM_DG010_01', 'DG010', 'NV_TT003', 'CN_BTH', 'QD_V1', DATEADD(DAY, -9, SYSDATETIME()), N'Đã trả hết', N'Phiếu mượn có phát sinh hỏng sách');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG010_OS01_001')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG010_OS01_001', 'PM_DG010_01', 'OS01-001', 'QDM_PREMIUM_HEDIEUHANH_V1', DATEADD(DAY, -9, SYSDATETIME()), DATEADD(DAY, 12, SYSDATETIME()), DATEADD(DAY, -1, SYSDATETIME()), N'Đã trả');

IF NOT EXISTS (SELECT 1 FROM PHIEUTRA WHERE MaPhieuTra = 'PTR_DG010_01')
INSERT INTO PHIEUTRA(MaPhieuTra, MaDocGia, MaNhanVienNhan, MaChiNhanh, NgayTra, GhiChu)
VALUES ('PTR_DG010_01', 'DG010', 'NV_TT003', 'CN_BTH', DATEADD(DAY, -1, SYSDATETIME()), N'Phiếu trả có ghi nhận sách bị hỏng');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTRA WHERE MaChiTietTra = 'CTT_DG010_OS01_001')
INSERT INTO CHITIETPHIEUTRA(MaChiTietTra, MaPhieuTra, MaChiTietMuon, TinhTrangKhiTra, SoNgayTre, TienPhatTre, TienPhatHongMat, GhiChu)
VALUES ('CTT_DG010_OS01_001', 'PTR_DG010_01', 'CTM_DG010_OS01_001', N'Bình thường', 0, 0, 70000, N'Sách bị rách bìa, phạt hỏng sách. Tình trạng được ghi chú tại đây để tránh sai CHECK CK_CTPT_TINHTRANG');

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG010_HONG_01')
INSERT INTO KHOANNO(MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra, SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai)
VALUES ('NO_DG010_HONG_01', 'DG010', 'NO_HONG_SACH', 'CTT_DG010_OS01_001', 70000, 0, DATEADD(DAY, -1, SYSDATETIME()), N'Làm hỏng sách Hệ điều hành hiện đại', N'Chưa thanh toán');

UPDATE CUONSACH SET MaTrangThai = 'TT_HONG'
WHERE MaCuonSach = 'OS01-001';

/* =========================================================
   13. MƯỢN/TRẢ ĐÃ PHÁT SINH PHẠT DO MẤT SÁCH
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG011_01')
INSERT INTO PHIEUMUON(MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh, MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu)
VALUES ('PM_DG011_01', 'DG011', 'NV_TT004', 'CN_Q7', 'QD_V1', DATEADD(DAY, -18, SYSDATETIME()), N'Đã trả hết', N'Phiếu mượn có phát sinh mất sách');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG011_NARUTO01_001')
INSERT INTO CHITIETPHIEUMUON(MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon, NgayMuon, HanTra, NgayTraThucTe, TrangThai)
VALUES ('CTM_DG011_NARUTO01_001', 'PM_DG011_01', 'NARUTO01-001', 'QDM_THUONG_MANGA_V1', DATEADD(DAY, -18, SYSDATETIME()), DATEADD(DAY, -14, SYSDATETIME()), DATEADD(DAY, -2, SYSDATETIME()), N'Đã trả');

IF NOT EXISTS (SELECT 1 FROM PHIEUTRA WHERE MaPhieuTra = 'PTR_DG011_01')
INSERT INTO PHIEUTRA(MaPhieuTra, MaDocGia, MaNhanVienNhan, MaChiNhanh, NgayTra, GhiChu)
VALUES ('PTR_DG011_01', 'DG011', 'NV_TT004', 'CN_Q7', DATEADD(DAY, -2, SYSDATETIME()), N'Phiếu trả xử lý mất sách');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTRA WHERE MaChiTietTra = 'CTT_DG011_NARUTO01_001')
INSERT INTO CHITIETPHIEUTRA(MaChiTietTra, MaPhieuTra, MaChiTietMuon, TinhTrangKhiTra, SoNgayTre, TienPhatTre, TienPhatHongMat, GhiChu)
VALUES ('CTT_DG011_NARUTO01_001', 'PTR_DG011_01', 'CTM_DG011_NARUTO01_001', N'Bình thường', 12, 12000, 30000, N'Trễ 12 ngày và làm mất sách. Tình trạng mất sách được ghi chú tại đây để tránh sai CHECK CK_CTPT_TINHTRANG');

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG011_MAT_01')
INSERT INTO KHOANNO(MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra, SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai)
VALUES ('NO_DG011_MAT_01', 'DG011', 'NO_MAT_SACH', 'CTT_DG011_NARUTO01_001', 42000, 20000, DATEADD(DAY, -2, SYSDATETIME()), N'Trễ hạn và làm mất Naruto tập 1', N'Thanh toán một phần');

IF NOT EXISTS (SELECT 1 FROM PHIEUTHU WHERE MaPhieuThu = 'PT_NO_DG011_01')
INSERT INTO PHIEUTHU(MaPhieuThu, MaDocGia, MaNhanVienThu, MaPhuongThuc, LoaiThu, SoTienThu, NgayThu, TrangThai, GhiChu)
VALUES ('PT_NO_DG011_01', 'DG011', 'NV_TT004', 'PT_TIEN_MAT', N'Thu tiền phạt', 20000, DATEADD(DAY, -1, SYSDATETIME()), N'Thành công', N'Đóng một phần tiền phạt mất sách');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTHU_NO WHERE MaChiTietPhieuThu = 'CTPT_NO_DG011_01')
INSERT INTO CHITIETPHIEUTHU_NO(MaChiTietPhieuThu, MaPhieuThu, MaKhoanNo, SoTienApDung)
VALUES ('CTPT_NO_DG011_01', 'PT_NO_DG011_01', 'NO_DG011_MAT_01', 20000);

UPDATE CUONSACH SET MaTrangThai = 'TT_MAT'
WHERE MaCuonSach = 'NARUTO01-001';

/* =========================================================
   14. ĐẶT TRƯỚC SÁCH
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUDATTRUOC WHERE MaPhieuDatTruoc = 'PDT_DG006_HEADJAVA')
INSERT INTO PHIEUDATTRUOC(MaPhieuDatTruoc, MaDocGia, MaDauSach, MaCuonSachDuocGiu, MaChiNhanh, NgayDat, NgayHetHanGiuCho, TrangThai, GhiChu)
VALUES ('PDT_DG006_HEADJAVA', 'DG006', 'HEADJAVA', 'HEADJAVA-001', 'CN_TD', SYSDATETIME(), DATEADD(DAY, 2, SYSDATETIME()), N'Đã giữ chỗ', N'Giữ chỗ sách Head First Java cho độc giả');

UPDATE CUONSACH SET MaTrangThai = 'TT_DANGDATTRUOC'
WHERE MaCuonSach = 'HEADJAVA-001';

IF NOT EXISTS (SELECT 1 FROM PHIEUDATTRUOC WHERE MaPhieuDatTruoc = 'PDT_DG010_HOAVANG')
INSERT INTO PHIEUDATTRUOC(MaPhieuDatTruoc, MaDocGia, MaDauSach, MaCuonSachDuocGiu, MaChiNhanh, NgayDat, NgayHetHanGiuCho, TrangThai, GhiChu)
VALUES ('PDT_DG010_HOAVANG', 'DG010', 'HOAVANG', 'HOAVANG-001', 'CN_BTH', DATEADD(DAY, -3, SYSDATETIME()), DATEADD(DAY, -1, SYSDATETIME()), N'Đã giữ chỗ', N'Phiếu đặt trước đã quá thời gian giữ chỗ; dùng trạng thái hợp lệ theo CHECK CK_PDT_TRANGTHAI');

IF NOT EXISTS (SELECT 1 FROM PHIEUDATTRUOC WHERE MaPhieuDatTruoc = 'PDT_DG012_ONEPIECE')
INSERT INTO PHIEUDATTRUOC(MaPhieuDatTruoc, MaDocGia, MaDauSach, MaCuonSachDuocGiu, MaChiNhanh, NgayDat, NgayHetHanGiuCho, TrangThai, GhiChu)
VALUES ('PDT_DG012_ONEPIECE', 'DG012', 'ONEPIECE01', 'ONEPIECE01-002', 'CN_BTH', SYSDATETIME(), DATEADD(DAY, 2, SYSDATETIME()), N'Đã giữ chỗ', N'Giữ chỗ One Piece tập 1');

UPDATE CUONSACH SET MaTrangThai = 'TT_DANGDATTRUOC'
WHERE MaCuonSach = 'ONEPIECE01-002';

/* =========================================================
   15. ĐÁNH GIÁ VÀ BÌNH LUẬN
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM DANHGIA WHERE MaDanhGia = 'DGIA_DG007_REFACTOR01')
INSERT INTO DANHGIA(MaDanhGia, MaDocGia, MaDauSach, SoSao, NoiDung, NgayDanhGia, TrangThai)
VALUES ('DGIA_DG007_REFACTOR01', 'DG007', 'REFACTOR01', 5, N'Sách rất hữu ích khi muốn cải thiện cấu trúc code.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM DANHGIA WHERE MaDanhGia = 'DGIA_DG008_NETWORK01')
INSERT INTO DANHGIA(MaDanhGia, MaDocGia, MaDauSach, SoSao, NoiDung, NgayDanhGia, TrangThai)
VALUES ('DGIA_DG008_NETWORK01', 'DG008', 'NETWORK01', 4, N'Nội dung phù hợp để ôn lại kiến thức mạng máy tính.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM DANHGIA WHERE MaDanhGia = 'DGIA_DG010_HOAVANG')
INSERT INTO DANHGIA(MaDanhGia, MaDocGia, MaDauSach, SoSao, NoiDung, NgayDanhGia, TrangThai)
VALUES ('DGIA_DG010_HOAVANG', 'DG010', 'HOAVANG', 5, N'Tác phẩm giàu cảm xúc và dễ đọc.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM DANHGIA WHERE MaDanhGia = 'DGIA_DG012_ONEPIECE01')
INSERT INTO DANHGIA(MaDanhGia, MaDocGia, MaDauSach, SoSao, NoiDung, NgayDanhGia, TrangThai)
VALUES ('DGIA_DG012_ONEPIECE01', 'DG012', 'ONEPIECE01', 5, N'Truyện hấp dẫn, phù hợp để đọc giải trí.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM BINHLUAN WHERE MaBinhLuan = 'BL_DG007_REFACTOR01_01')
INSERT INTO BINHLUAN(MaBinhLuan, MaDocGia, MaDauSach, NoiDung, NgayBinhLuan, TrangThai)
VALUES ('BL_DG007_REFACTOR01_01', 'DG007', 'REFACTOR01', N'Mình thích phần giải thích cách tách hàm và cải thiện tên biến.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM BINHLUAN WHERE MaBinhLuan = 'BL_DG008_NETWORK01_01')
INSERT INTO BINHLUAN(MaBinhLuan, MaDocGia, MaDauSach, NoiDung, NgayBinhLuan, TrangThai)
VALUES ('BL_DG008_NETWORK01_01', 'DG008', 'NETWORK01', N'Phần mô hình OSI được trình bày khá rõ ràng.', SYSDATETIME(), N'Hiển thị');

IF NOT EXISTS (SELECT 1 FROM BINHLUAN WHERE MaBinhLuan = 'BL_DG012_HEADJAVA_01')
INSERT INTO BINHLUAN(MaBinhLuan, MaDocGia, MaDauSach, NoiDung, NgayBinhLuan, TrangThai)
VALUES ('BL_DG012_HEADJAVA_01', 'DG012', 'HEADJAVA', N'Sách Java này có cách trình bày trực quan, dễ tiếp cận.', SYSDATETIME(), N'Hiển thị');

/* =========================================================
   16. THÔNG BÁO CHO ĐỘC GIẢ
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG007_GIAHAN_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG007_GIAHAN_01', 'TK_DG007', @MaLoaiTB_GiaHanThanhCong, N'Gia hạn mượn sách thành công', N'Phiếu mượn của bạn đã được gia hạn theo quy định hiện hành.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG009_QUAHAN_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG009_QUAHAN_01', 'TK_DG009', @MaLoaiTB_QuaHan, N'Sách đã quá hạn trả', N'Bạn đang có sách quá hạn trả, vui lòng trả sách sớm để tránh phát sinh thêm tiền phạt.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG010_HONG_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG010_HONG_01', 'TK_DG010', 'TB_BI_PHAT', N'Bạn có khoản phạt hỏng sách', N'Bạn có khoản phạt 70.000đ do làm hỏng sách Hệ điều hành hiện đại.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG011_MAT_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG011_MAT_01', 'TK_DG011', 'TB_BI_PHAT', N'Bạn có khoản phạt mất sách', N'Bạn có khoản phạt do trả trễ và làm mất sách Naruto tập 1.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG011_HETHANGOI_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG011_HETHANGOI_01', 'TK_DG011', @MaLoaiTB_HetHanGoi, N'Gói thành viên đã hết hạn', N'Gói thành viên của bạn đã hết hạn, vui lòng gia hạn để tiếp tục sử dụng đầy đủ chức năng.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG012_SAPHETHANTHE_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG012_SAPHETHANTHE_01', 'TK_DG012', @MaLoaiTB_SapHetHanThe, N'Thẻ độc giả sắp hết hạn', N'Thẻ độc giả của bạn sẽ hết hạn trong vài ngày tới, vui lòng liên hệ thủ thư để gia hạn.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG006_SACHDACO_01')
INSERT INTO THONGBAO(MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao, TieuDe, NoiDung, NgayTao, GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail)
VALUES ('TB_DG006_SACHDACO_01', 'TK_DG006', 'TB_SACH_DA_CO', N'Sách đặt trước đã có', N'Sách Head First Java đã được giữ chỗ cho bạn trong 2 ngày.', SYSDATETIME(), 1, 1, N'Chờ gửi', 0);

/* =========================================================
   17. NHẬT KÝ HOẠT ĐỘNG DEMO
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM NHATKYHOATDONG WHERE HanhDong = N'Tạo dữ liệu demo mở rộng' AND MaDoiTuongTacDong = '04_seed_extra_demo_data')
INSERT INTO NHATKYHOATDONG(MaTaiKhoan, HanhDong, DoiTuongTacDong, MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet)
VALUES ('TK_ADMIN', N'Tạo dữ liệu demo mở rộng', N'SEED_DATA', '04_seed_extra_demo_data', SYSDATETIME(), '127.0.0.1', N'Thêm dữ liệu demo mở rộng cho chi nhánh, thủ thư, độc giả, sách, mượn trả, phạt, đặt trước và thông báo');

IF NOT EXISTS (SELECT 1 FROM NHATKYHOATDONG WHERE HanhDong = N'Tạo phiếu mượn demo' AND MaDoiTuongTacDong = 'PM_DG007_01')
INSERT INTO NHATKYHOATDONG(MaTaiKhoan, HanhDong, DoiTuongTacDong, MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet)
VALUES ('TK_THUTHU03', N'Tạo phiếu mượn demo', N'PHIEUMUON', 'PM_DG007_01', DATEADD(DAY, -2, SYSDATETIME()), '127.0.0.1', N'Thủ thư tạo phiếu mượn cho độc giả DG007');

IF NOT EXISTS (SELECT 1 FROM NHATKYHOATDONG WHERE HanhDong = N'Xử lý hỏng sách demo' AND MaDoiTuongTacDong = 'NO_DG010_HONG_01')
INSERT INTO NHATKYHOATDONG(MaTaiKhoan, HanhDong, DoiTuongTacDong, MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet)
VALUES ('TK_THUTHU03', N'Xử lý hỏng sách demo', N'KHOANNO', 'NO_DG010_HONG_01', DATEADD(DAY, -1, SYSDATETIME()), '127.0.0.1', N'Tạo khoản nợ do độc giả làm hỏng sách');

IF NOT EXISTS (SELECT 1 FROM NHATKYHOATDONG WHERE HanhDong = N'Xử lý mất sách demo' AND MaDoiTuongTacDong = 'NO_DG011_MAT_01')
INSERT INTO NHATKYHOATDONG(MaTaiKhoan, HanhDong, DoiTuongTacDong, MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet)
VALUES ('TK_THUTHU04', N'Xử lý mất sách demo', N'KHOANNO', 'NO_DG011_MAT_01', DATEADD(DAY, -2, SYSDATETIME()), '127.0.0.1', N'Tạo khoản nợ do độc giả làm mất sách');

IF NOT EXISTS (SELECT 1 FROM NHATKYHOATDONG WHERE HanhDong = N'Tạo phiếu đặt trước demo' AND MaDoiTuongTacDong = 'PDT_DG006_HEADJAVA')
INSERT INTO NHATKYHOATDONG(MaTaiKhoan, HanhDong, DoiTuongTacDong, MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet)
VALUES ('TK_THUTHU01', N'Tạo phiếu đặt trước demo', N'PHIEUDATTRUOC', 'PDT_DG006_HEADJAVA', SYSDATETIME(), '127.0.0.1', N'Tạo phiếu đặt trước và giữ chỗ sách Head First Java');

GO

USE QuanLyThuVien;
GO

/* =========================================================
   05_seed_special_cases.sql
   Dữ liệu đặc biệt để demo:
   - Độc giả còn nợ
   - Độc giả làm hỏng sách
   - Độc giả làm mất sách
   - Độc giả bị phạt trả trễ
   - Độc giả đã thanh toán đủ tiền phạt
   - Độc giả đang mượn quá hạn chưa trả
   - Một vài sách mới phục vụ test
   ========================================================= */

SET NOCOUNT ON;

/* =========================================================
   0. ĐẢM BẢO DỮ LIỆU DANH MỤC CẦN CÓ
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM NHOMDOCGIA WHERE MaNhomDocGia = 'NHOM_SINHVIEN')
INSERT INTO NHOMDOCGIA(MaNhomDocGia, TenNhomDocGia, MoTa)
VALUES ('NHOM_SINHVIEN', N'Sinh viên', N'Nhóm độc giả sinh viên');

IF NOT EXISTS (SELECT 1 FROM GOITHANHVIEN WHERE MaGoiThanhVien = 'GOI_THUONG')
INSERT INTO GOITHANHVIEN(MaGoiThanhVien, TenGoi, MoTa)
VALUES ('GOI_THUONG', N'Thường', N'Gói thành viên cơ bản');

IF NOT EXISTS (SELECT 1 FROM THELOAI WHERE MaTheLoai = 'TL_CNTT')
INSERT INTO THELOAI(MaTheLoai, TenTheLoai, MoTa)
VALUES ('TL_CNTT', N'Công nghệ thông tin', N'Sách chuyên ngành CNTT');

IF NOT EXISTS (SELECT 1 FROM NHAXUATBAN WHERE MaNhaXuatBan = 'NXB_DHQG')
INSERT INTO NHAXUATBAN(MaNhaXuatBan, TenNhaXuatBan, DiaChi)
VALUES ('NXB_DHQG', N'Nhà xuất bản Đại học Quốc gia TP.HCM', N'TP.HCM');

IF NOT EXISTS (SELECT 1 FROM LOAIKHOANNO WHERE MaLoaiKhoanNo = 'NO_TRA_TRE')
INSERT INTO LOAIKHOANNO(MaLoaiKhoanNo, TenLoaiKhoanNo, MoTa)
VALUES ('NO_TRA_TRE', N'Trả trễ', N'Nợ phát sinh do trả sách trễ hạn');

IF NOT EXISTS (SELECT 1 FROM LOAIKHOANNO WHERE MaLoaiKhoanNo = 'NO_HONG_SACH')
INSERT INTO LOAIKHOANNO(MaLoaiKhoanNo, TenLoaiKhoanNo, MoTa)
VALUES ('NO_HONG_SACH', N'Hỏng sách', N'Nợ phát sinh do làm hỏng sách');

IF NOT EXISTS (SELECT 1 FROM LOAIKHOANNO WHERE MaLoaiKhoanNo = 'NO_MAT_SACH')
INSERT INTO LOAIKHOANNO(MaLoaiKhoanNo, TenLoaiKhoanNo, MoTa)
VALUES ('NO_MAT_SACH', N'Mất sách', N'Nợ phát sinh do làm mất sách');

IF NOT EXISTS (SELECT 1 FROM PHUONGTHUCTHANHTOAN WHERE MaPhuongThuc = 'PT_TIEN_MAT')
INSERT INTO PHUONGTHUCTHANHTOAN(MaPhuongThuc, TenPhuongThuc, MoTa)
VALUES ('PT_TIEN_MAT', N'Tiền mặt', N'Thanh toán bằng tiền mặt');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_SANCO')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_SANCO', N'Sẵn có', N'Cuốn sách đang sẵn sàng cho mượn');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_DANGMUON')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_DANGMUON', N'Đang được mượn', N'Cuốn sách đang được độc giả mượn');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_HONG')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_HONG', N'Bị hỏng', N'Cuốn sách bị hư hỏng');

IF NOT EXISTS (SELECT 1 FROM TRANGTHAICUONSACH WHERE MaTrangThai = 'TT_MAT')
INSERT INTO TRANGTHAICUONSACH(MaTrangThai, TenTrangThai, MoTa)
VALUES ('TT_MAT', N'Bị mất', N'Cuốn sách đã bị mất');

IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = 'TB_BI_PHAT')
INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
VALUES ('TB_BI_PHAT', N'Bị tính tiền phạt', N'Thông báo phát sinh tiền phạt');

DECLARE @MaLoaiTB_QuaHan NVARCHAR(50);

SELECT TOP 1 @MaLoaiTB_QuaHan = MaLoaiThongBao
FROM LOAITHONGBAO
WHERE MaLoaiThongBao = 'TB_QUA_HAN'
   OR TenLoaiThongBao = N'Quá hạn trả sách';

IF @MaLoaiTB_QuaHan IS NULL
BEGIN
    INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
    VALUES ('TB_QUA_HAN', N'Quá hạn trả sách', N'Thông báo độc giả đã quá hạn trả sách');

    SET @MaLoaiTB_QuaHan = 'TB_QUA_HAN';
END

/* Vị trí sách giáo trình/CNTT nếu chưa có */
IF NOT EXISTS (SELECT 1 FROM KHU WHERE MaKhu = 'KHU_GIAOTRINH')
INSERT INTO KHU(MaKhu, MaChiNhanh, TenKhu, MoTa)
VALUES ('KHU_GIAOTRINH', 'CN_TD', N'Khu Giáo trình', N'Khu sách học tập và giáo trình');

IF NOT EXISTS (SELECT 1 FROM KESACH WHERE MaKeSach = 'KE_G01')
INSERT INTO KESACH(MaKeSach, MaKhu, TenKeSach, MoTa)
VALUES ('KE_G01', 'KHU_GIAOTRINH', N'Kệ Giáo trình 01', N'Kệ giáo trình và sách chuyên ngành');

IF NOT EXISTS (SELECT 1 FROM VITRISACH WHERE MaViTri = 'VT_G01_N01')
INSERT INTO VITRISACH(MaViTri, MaKeSach, MaViTriHienThi, MoTa)
VALUES ('VT_G01_N01', 'KE_G01', N'Ngăn 01', N'Ngăn sách giáo trình');

/* Quy định mượn sách CNTT cho gói thường */
IF NOT EXISTS (SELECT 1 FROM QUYDINHMUON_THELOAI WHERE MaQuyDinhMuon = 'QDM_THUONG_CNTT_V1')
INSERT INTO QUYDINHMUON_THELOAI(
    MaQuyDinhMuon, MaPhienBan, MaGoiThanhVien, MaTheLoai,
    SoNgayMuon, SoNgayGiaHanMoiLan
)
VALUES ('QDM_THUONG_CNTT_V1', 'QD_V1', 'GOI_THUONG', 'TL_CNTT', 7, 3);

/* =========================================================
   1. THÊM MỘT VÀI SÁCH MỚI
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_TANENBAUM')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_TANENBAUM', N'Andrew S. Tanenbaum', N'Tác giả sách hệ điều hành và mạng máy tính');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_HORSTMANN')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_HORSTMANN', N'Cay S. Horstmann', N'Tác giả sách lập trình Java');

IF NOT EXISTS (SELECT 1 FROM TACGIA WHERE MaTacGia = 'TG_LUTZ')
INSERT INTO TACGIA(MaTacGia, TenTacGia, MoTa)
VALUES ('TG_LUTZ', N'Mark Lutz', N'Tác giả sách lập trình Python');

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'OS01')
INSERT INTO DAUSACH(
    MaDauSach, MaNhaXuatBan, TenDauSach, ISBN,
    NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia
)
VALUES (
    'OS01', 'NXB_DHQG', N'Hệ điều hành hiện đại', '9780000000201',
    2022, N'Tiếng Việt', 520, N'Sách chuyên ngành hệ điều hành', NULL, 200000
);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'NET01')
INSERT INTO DAUSACH(
    MaDauSach, MaNhaXuatBan, TenDauSach, ISBN,
    NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia
)
VALUES (
    'NET01', 'NXB_DHQG', N'Mạng máy tính căn bản', '9780000000202',
    2021, N'Tiếng Việt', 410, N'Sách nhập môn mạng máy tính', NULL, 150000
);

IF NOT EXISTS (SELECT 1 FROM DAUSACH WHERE MaDauSach = 'PYTHON01')
INSERT INTO DAUSACH(
    MaDauSach, MaNhaXuatBan, TenDauSach, ISBN,
    NamXuatBan, NgonNgu, SoTrang, MoTa, AnhBia, TriGia
)
VALUES (
    'PYTHON01', 'NXB_DHQG', N'Lập trình Python cơ bản', '9780000000203',
    2024, N'Tiếng Việt', 360, N'Sách nhập môn Python cho sinh viên CNTT', NULL, 125000
);

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'OS01' AND MaTacGia = 'TG_TANENBAUM')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro)
VALUES ('OS01', 'TG_TANENBAUM', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'NET01' AND MaTacGia = 'TG_TANENBAUM')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro)
VALUES ('NET01', 'TG_TANENBAUM', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_TACGIA WHERE MaDauSach = 'PYTHON01' AND MaTacGia = 'TG_LUTZ')
INSERT INTO DAUSACH_TACGIA(MaDauSach, MaTacGia, VaiTro)
VALUES ('PYTHON01', 'TG_LUTZ', N'Tác giả');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'OS01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai)
VALUES ('OS01', 'TL_CNTT');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'NET01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai)
VALUES ('NET01', 'TL_CNTT');

IF NOT EXISTS (SELECT 1 FROM DAUSACH_THELOAI WHERE MaDauSach = 'PYTHON01' AND MaTheLoai = 'TL_CNTT')
INSERT INTO DAUSACH_THELOAI(MaDauSach, MaTheLoai)
VALUES ('PYTHON01', 'TL_CNTT');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'OS01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('OS01-001', 'OS01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-OS01-001', 'QR-OS01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'OS01-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('OS01-002', 'OS01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-OS01-002', 'QR-OS01-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'NET01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('NET01-001', 'NET01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-NET01-001', 'QR-NET01-001');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'NET01-002')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('NET01-002', 'NET01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-NET01-002', 'QR-NET01-002');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'PYTHON01-001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode)
VALUES ('PYTHON01-001', 'PYTHON01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-PYTHON01-001', 'QR-PYTHON01-001');

/* =========================================================
   2. THÊM ĐỘC GIẢ PHỤC VỤ CASE ĐẶC BIỆT
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG020')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG020', N'docgia20', N'$2a$10$demo_hash_docgia20', 'docgia20@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG020')
INSERT INTO DOCGIA(
    MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen,
    NgaySinh, DiaChi, Email, SoDienThoai,
    NgayLapThe, NgayHetHanThe
)
VALUES (
    'DG020', 'TK_DG020', 'NHOM_SINHVIEN', N'Trần Quốc Bảo',
    '2004-09-10', N'Thủ Đức, TP.HCM', 'docgia20@gmail.com', '0922222220',
    CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE))
);

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG021')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG021', N'docgia21', N'$2a$10$demo_hash_docgia21', 'docgia21@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG021')
INSERT INTO DOCGIA(
    MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen,
    NgaySinh, DiaChi, Email, SoDienThoai,
    NgayLapThe, NgayHetHanThe
)
VALUES (
    'DG021', 'TK_DG021', 'NHOM_SINHVIEN', N'Mai Anh Thư',
    '2005-01-22', N'Bình Thạnh, TP.HCM', 'docgia21@gmail.com', '0922222221',
    CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE))
);

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG022')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG022', N'docgia22', N'$2a$10$demo_hash_docgia22', 'docgia22@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG022')
INSERT INTO DOCGIA(
    MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen,
    NgaySinh, DiaChi, Email, SoDienThoai,
    NgayLapThe, NgayHetHanThe
)
VALUES (
    'DG022', 'TK_DG022', 'NHOM_SINHVIEN', N'Nguyễn Đức Phúc',
    '2003-12-05', N'Gò Vấp, TP.HCM', 'docgia22@gmail.com', '0922222222',
    CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE))
);

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG023')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG023', N'docgia23', N'$2a$10$demo_hash_docgia23', 'docgia23@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG023')
INSERT INTO DOCGIA(
    MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen,
    NgaySinh, DiaChi, Email, SoDienThoai,
    NgayLapThe, NgayHetHanThe
)
VALUES (
    'DG023', 'TK_DG023', 'NHOM_SINHVIEN', N'Lâm Minh Quân',
    '2004-04-18', N'Quận 7, TP.HCM', 'docgia23@gmail.com', '0922222223',
    CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE))
);

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG020_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG020_01', 'DG020', 'GOI_THUONG', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói thường demo');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG021_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG021_01', 'DG021', 'GOI_THUONG', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói thường demo');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG022_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG022_01', 'DG022', 'GOI_THUONG', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói thường demo');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG023_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG023_01', 'DG023', 'GOI_THUONG', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói thường demo');

/* =========================================================
   3. CASE 1 — DG020 LÀM HỎNG SÁCH, CÒN NỢ MỘT PHẦN
   Sách: OS01-001
   Phạt hỏng: 70.000
   Đã trả: 20.000
   Còn nợ: 50.000
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG020_HONG_01')
INSERT INTO PHIEUMUON(
    MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh,
    MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu
)
VALUES (
    'PM_DG020_HONG_01', 'DG020', 'NV_TT001', 'CN_TD',
    'QD_V1', DATEADD(DAY, -5, SYSDATETIME()), N'Đã trả hết',
    N'Phiếu mượn demo: độc giả làm hỏng sách'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG020_OS01_001')
INSERT INTO CHITIETPHIEUMUON(
    MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon,
    NgayMuon, HanTra, NgayTraThucTe, TrangThai
)
VALUES (
    'CTM_DG020_OS01_001', 'PM_DG020_HONG_01', 'OS01-001', 'QDM_THUONG_CNTT_V1',
    DATEADD(DAY, -5, SYSDATETIME()), DATEADD(DAY, -1, SYSDATETIME()),
    DATEADD(DAY, -1, SYSDATETIME()), N'Đã trả'
);

IF NOT EXISTS (SELECT 1 FROM PHIEUTRA WHERE MaPhieuTra = 'PTR_DG020_HONG_01')
INSERT INTO PHIEUTRA(MaPhieuTra, MaDocGia, MaNhanVienNhan, MaChiNhanh, NgayTra, GhiChu)
VALUES (
    'PTR_DG020_HONG_01', 'DG020', 'NV_TT001', 'CN_TD',
    DATEADD(DAY, -1, SYSDATETIME()), N'Phiếu trả có ghi nhận sách bị hỏng'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTRA WHERE MaChiTietTra = 'CTT_DG020_OS01_001')
INSERT INTO CHITIETPHIEUTRA(
    MaChiTietTra, MaPhieuTra, MaChiTietMuon,
    TinhTrangKhiTra, SoNgayTre, TienPhatTre, TienPhatHongMat, GhiChu
)
VALUES (
    'CTT_DG020_OS01_001', 'PTR_DG020_HONG_01', 'CTM_DG020_OS01_001',
    N'Bình thường', 0, 0, 70000,
    N'Độc giả DG020 làm rách bìa và bung gáy sách OS01-001. Phạt hỏng sách 70.000đ.'
);

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG020_HONG_01')
INSERT INTO KHOANNO(
    MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra,
    SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai
)
VALUES (
    'NO_DG020_HONG_01', 'DG020', 'NO_HONG_SACH', 'CTT_DG020_OS01_001',
    70000, 20000, DATEADD(DAY, -1, SYSDATETIME()),
    N'Làm hỏng sách Hệ điều hành hiện đại - OS01-001',
    N'Thanh toán một phần'
);

IF NOT EXISTS (SELECT 1 FROM PHIEUTHU WHERE MaPhieuThu = 'PT_NO_DG020_HONG_01')
INSERT INTO PHIEUTHU(
    MaPhieuThu, MaDocGia, MaNhanVienThu, MaPhuongThuc,
    LoaiThu, SoTienThu, NgayThu, TrangThai, GhiChu
)
VALUES (
    'PT_NO_DG020_HONG_01', 'DG020', 'NV_TT001', 'PT_TIEN_MAT',
    N'Thu tiền phạt', 20000, SYSDATETIME(), N'Thành công',
    N'Độc giả DG020 đóng trước 20.000đ tiền phạt hỏng sách'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTHU_NO WHERE MaChiTietPhieuThu = 'CTPT_NO_DG020_HONG_01')
INSERT INTO CHITIETPHIEUTHU_NO(MaChiTietPhieuThu, MaPhieuThu, MaKhoanNo, SoTienApDung)
VALUES ('CTPT_NO_DG020_HONG_01', 'PT_NO_DG020_HONG_01', 'NO_DG020_HONG_01', 20000);

UPDATE CUONSACH
SET MaTrangThai = 'TT_HONG'
WHERE MaCuonSach = 'OS01-001'
  AND MaTrangThai <> 'TT_HONG';

/* =========================================================
   4. CASE 2 — DG021 LÀM MẤT SÁCH, CHƯA THANH TOÁN
   Sách: NET01-001
   Trễ: 6 ngày x 1.000 = 6.000
   Phạt mất sách: 150.000
   Tổng còn nợ: 156.000
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG021_MAT_01')
INSERT INTO PHIEUMUON(
    MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh,
    MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu
)
VALUES (
    'PM_DG021_MAT_01', 'DG021', 'NV_TT001', 'CN_TD',
    'QD_V1', DATEADD(DAY, -15, SYSDATETIME()), N'Đã trả hết',
    N'Phiếu mượn demo: độc giả báo mất sách khi trả'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG021_NET01_001')
INSERT INTO CHITIETPHIEUMUON(
    MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon,
    NgayMuon, HanTra, NgayTraThucTe, TrangThai
)
VALUES (
    'CTM_DG021_NET01_001', 'PM_DG021_MAT_01', 'NET01-001', 'QDM_THUONG_CNTT_V1',
    DATEADD(DAY, -15, SYSDATETIME()), DATEADD(DAY, -8, SYSDATETIME()),
    DATEADD(DAY, -2, SYSDATETIME()), N'Đã trả'
);

IF NOT EXISTS (SELECT 1 FROM PHIEUTRA WHERE MaPhieuTra = 'PTR_DG021_MAT_01')
INSERT INTO PHIEUTRA(MaPhieuTra, MaDocGia, MaNhanVienNhan, MaChiNhanh, NgayTra, GhiChu)
VALUES (
    'PTR_DG021_MAT_01', 'DG021', 'NV_TT001', 'CN_TD',
    DATEADD(DAY, -2, SYSDATETIME()), N'Phiếu trả có ghi nhận sách bị mất'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTRA WHERE MaChiTietTra = 'CTT_DG021_NET01_001')
INSERT INTO CHITIETPHIEUTRA(
    MaChiTietTra, MaPhieuTra, MaChiTietMuon,
    TinhTrangKhiTra, SoNgayTre, TienPhatTre, TienPhatHongMat, GhiChu
)
VALUES (
    'CTT_DG021_NET01_001', 'PTR_DG021_MAT_01', 'CTM_DG021_NET01_001',
    N'Bình thường', 6, 6000, 150000,
    N'Độc giả DG021 làm mất sách NET01-001. Trễ 6 ngày, phạt trễ 6.000đ và phạt mất sách 150.000đ.'
);

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG021_TRE_01')
INSERT INTO KHOANNO(
    MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra,
    SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai
)
VALUES (
    'NO_DG021_TRE_01', 'DG021', 'NO_TRA_TRE', 'CTT_DG021_NET01_001',
    6000, 0, DATEADD(DAY, -2, SYSDATETIME()),
    N'Trả trễ sách Mạng máy tính căn bản 6 ngày',
    N'Chưa thanh toán'
);

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG021_MAT_01')
INSERT INTO KHOANNO(
    MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra,
    SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai
)
VALUES (
    'NO_DG021_MAT_01', 'DG021', 'NO_MAT_SACH', 'CTT_DG021_NET01_001',
    150000, 0, DATEADD(DAY, -2, SYSDATETIME()),
    N'Làm mất sách Mạng máy tính căn bản - NET01-001',
    N'Chưa thanh toán'
);

UPDATE CUONSACH
SET MaTrangThai = 'TT_MAT'
WHERE MaCuonSach = 'NET01-001'
  AND MaTrangThai <> 'TT_MAT';

/* =========================================================
   5. CASE 3 — DG022 TRẢ TRỄ NHƯNG ĐÃ THANH TOÁN ĐỦ
   Sách: PYTHON01-001
   Trễ: 4 ngày x 1.000 = 4.000
   Đã thanh toán đủ: 4.000
   Sách quay lại trạng thái sẵn có
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG022_TRE_01')
INSERT INTO PHIEUMUON(
    MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh,
    MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu
)
VALUES (
    'PM_DG022_TRE_01', 'DG022', 'NV_TT001', 'CN_TD',
    'QD_V1', DATEADD(DAY, -12, SYSDATETIME()), N'Đã trả hết',
    N'Phiếu mượn demo: trả trễ nhưng đã thanh toán đủ'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG022_PYTHON01_001')
INSERT INTO CHITIETPHIEUMUON(
    MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon,
    NgayMuon, HanTra, NgayTraThucTe, TrangThai
)
VALUES (
    'CTM_DG022_PYTHON01_001', 'PM_DG022_TRE_01', 'PYTHON01-001', 'QDM_THUONG_CNTT_V1',
    DATEADD(DAY, -12, SYSDATETIME()), DATEADD(DAY, -8, SYSDATETIME()),
    DATEADD(DAY, -4, SYSDATETIME()), N'Đã trả'
);

IF NOT EXISTS (SELECT 1 FROM PHIEUTRA WHERE MaPhieuTra = 'PTR_DG022_TRE_01')
INSERT INTO PHIEUTRA(MaPhieuTra, MaDocGia, MaNhanVienNhan, MaChiNhanh, NgayTra, GhiChu)
VALUES (
    'PTR_DG022_TRE_01', 'DG022', 'NV_TT001', 'CN_TD',
    DATEADD(DAY, -4, SYSDATETIME()), N'Phiếu trả trễ đã thu đủ tiền phạt'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTRA WHERE MaChiTietTra = 'CTT_DG022_PYTHON01_001')
INSERT INTO CHITIETPHIEUTRA(
    MaChiTietTra, MaPhieuTra, MaChiTietMuon,
    TinhTrangKhiTra, SoNgayTre, TienPhatTre, TienPhatHongMat, GhiChu
)
VALUES (
    'CTT_DG022_PYTHON01_001', 'PTR_DG022_TRE_01', 'CTM_DG022_PYTHON01_001',
    N'Bình thường', 4, 4000, 0,
    N'Độc giả DG022 trả trễ 4 ngày, sách bình thường'
);

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG022_TRE_01')
INSERT INTO KHOANNO(
    MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra,
    SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai
)
VALUES (
    'NO_DG022_TRE_01', 'DG022', 'NO_TRA_TRE', 'CTT_DG022_PYTHON01_001',
    4000, 4000, DATEADD(DAY, -4, SYSDATETIME()),
    N'Trả trễ sách Lập trình Python cơ bản 4 ngày',
    N'Đã thanh toán'
);

IF NOT EXISTS (SELECT 1 FROM PHIEUTHU WHERE MaPhieuThu = 'PT_NO_DG022_TRE_01')
INSERT INTO PHIEUTHU(
    MaPhieuThu, MaDocGia, MaNhanVienThu, MaPhuongThuc,
    LoaiThu, SoTienThu, NgayThu, TrangThai, GhiChu
)
VALUES (
    'PT_NO_DG022_TRE_01', 'DG022', 'NV_TT001', 'PT_TIEN_MAT',
    N'Thu tiền phạt', 4000, DATEADD(DAY, -4, SYSDATETIME()), N'Thành công',
    N'Độc giả DG022 thanh toán đủ tiền phạt trả trễ'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTHU_NO WHERE MaChiTietPhieuThu = 'CTPT_NO_DG022_TRE_01')
INSERT INTO CHITIETPHIEUTHU_NO(MaChiTietPhieuThu, MaPhieuThu, MaKhoanNo, SoTienApDung)
VALUES ('CTPT_NO_DG022_TRE_01', 'PT_NO_DG022_TRE_01', 'NO_DG022_TRE_01', 4000);

UPDATE CUONSACH
SET MaTrangThai = 'TT_SANCO'
WHERE MaCuonSach = 'PYTHON01-001'
  AND MaTrangThai <> 'TT_SANCO';

/* =========================================================
   6. CASE 4 — DG023 ĐANG MƯỢN QUÁ HẠN, CHƯA TRẢ
   Sách: OS01-002
   Chưa tạo KHOANNO vì chưa trả sách
   Dùng để test màn hình nhắc quá hạn / gửi thông báo
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG023_QUAHAN_01')
INSERT INTO PHIEUMUON(
    MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh,
    MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu
)
VALUES (
    'PM_DG023_QUAHAN_01', 'DG023', 'NV_TT001', 'CN_TD',
    'QD_V1', DATEADD(DAY, -10, SYSDATETIME()), N'Đang mượn',
    N'Phiếu mượn demo: đang quá hạn nhưng chưa trả'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG023_OS01_002')
INSERT INTO CHITIETPHIEUMUON(
    MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon,
    NgayMuon, HanTra, NgayTraThucTe, TrangThai
)
VALUES (
    'CTM_DG023_OS01_002', 'PM_DG023_QUAHAN_01', 'OS01-002', 'QDM_THUONG_CNTT_V1',
    DATEADD(DAY, -10, SYSDATETIME()), DATEADD(DAY, -3, SYSDATETIME()),
    NULL, N'Đang mượn'
);

UPDATE CUONSACH
SET MaTrangThai = 'TT_DANGMUON'
WHERE MaCuonSach = 'OS01-002'
  AND MaTrangThai <> 'TT_DANGMUON';

/* =========================================================
   7. THÔNG BÁO CHO CÁC CASE ĐẶC BIỆT
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG020_HONG_01')
INSERT INTO THONGBAO(
    MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao,
    TieuDe, NoiDung, NgayTao,
    GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail
)
VALUES (
    'TB_DG020_HONG_01', 'TK_DG020', 'TB_BI_PHAT',
    N'Bạn có khoản phạt hỏng sách',
    N'Bạn bị phạt 70.000đ do làm hỏng sách Hệ điều hành hiện đại. Bạn đã thanh toán 20.000đ, còn nợ 50.000đ.',
    SYSDATETIME(), 1, 1, N'Chờ gửi', 0
);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG021_MAT_01')
INSERT INTO THONGBAO(
    MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao,
    TieuDe, NoiDung, NgayTao,
    GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail
)
VALUES (
    'TB_DG021_MAT_01', 'TK_DG021', 'TB_BI_PHAT',
    N'Bạn có khoản phạt mất sách',
    N'Bạn bị phạt 156.000đ do trả trễ 6 ngày và làm mất sách Mạng máy tính căn bản.',
    SYSDATETIME(), 1, 1, N'Chờ gửi', 0
);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG022_TRE_01')
INSERT INTO THONGBAO(
    MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao,
    TieuDe, NoiDung, NgayTao,
    GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail
)
VALUES (
    'TB_DG022_TRE_01', 'TK_DG022', 'TB_BI_PHAT',
    N'Bạn đã thanh toán tiền phạt',
    N'Bạn đã thanh toán đủ 4.000đ tiền phạt trả trễ sách Lập trình Python cơ bản.',
    SYSDATETIME(), 1, 1, N'Chờ gửi', 0
);

IF NOT EXISTS (SELECT 1 FROM THONGBAO WHERE MaThongBao = 'TB_DG023_QUAHAN_01')
INSERT INTO THONGBAO(
    MaThongBao, MaTaiKhoanNhan, MaLoaiThongBao,
    TieuDe, NoiDung, NgayTao,
    GuiTrongApp, GuiEmail, TrangThaiEmail, SoLanThuGuiEmail
)
VALUES (
    'TB_DG023_QUAHAN_01', 'TK_DG023', @MaLoaiTB_QuaHan,
    N'Sách đã quá hạn trả',
    N'Bạn đang mượn sách Hệ điều hành hiện đại quá hạn 3 ngày. Vui lòng trả sách sớm để tránh phát sinh thêm tiền phạt.',
    SYSDATETIME(), 1, 1, N'Chờ gửi', 0
);

/* =========================================================
   8. NHẬT KÝ HOẠT ĐỘNG
   ========================================================= */

IF NOT EXISTS (
    SELECT 1 FROM NHATKYHOATDONG
    WHERE HanhDong = N'Xử lý hỏng sách demo'
      AND MaDoiTuongTacDong = 'NO_DG020_HONG_01'
)
INSERT INTO NHATKYHOATDONG(
    MaTaiKhoan, HanhDong, DoiTuongTacDong,
    MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet
)
VALUES (
    'TK_THUTHU01', N'Xử lý hỏng sách demo', N'KHOANNO',
    'NO_DG020_HONG_01', SYSDATETIME(), '127.0.0.1',
    N'Tạo khoản nợ do độc giả DG020 làm hỏng sách OS01-001'
);

IF NOT EXISTS (
    SELECT 1 FROM NHATKYHOATDONG
    WHERE HanhDong = N'Xử lý mất sách demo'
      AND MaDoiTuongTacDong = 'NO_DG021_MAT_01'
)
INSERT INTO NHATKYHOATDONG(
    MaTaiKhoan, HanhDong, DoiTuongTacDong,
    MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet
)
VALUES (
    'TK_THUTHU01', N'Xử lý mất sách demo', N'KHOANNO',
    'NO_DG021_MAT_01', SYSDATETIME(), '127.0.0.1',
    N'Tạo khoản nợ do độc giả DG021 làm mất sách NET01-001'
);

IF NOT EXISTS (
    SELECT 1 FROM NHATKYHOATDONG
    WHERE HanhDong = N'Thu tiền phạt demo'
      AND MaDoiTuongTacDong = 'PT_NO_DG020_HONG_01'
)
INSERT INTO NHATKYHOATDONG(
    MaTaiKhoan, HanhDong, DoiTuongTacDong,
    MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet
)
VALUES (
    'TK_THUTHU01', N'Thu tiền phạt demo', N'PHIEUTHU',
    'PT_NO_DG020_HONG_01', SYSDATETIME(), '127.0.0.1',
    N'Thu một phần 20.000đ tiền phạt hỏng sách của DG020'
);

IF NOT EXISTS (
    SELECT 1 FROM NHATKYHOATDONG
    WHERE HanhDong = N'Tạo phiếu mượn quá hạn demo'
      AND MaDoiTuongTacDong = 'PM_DG023_QUAHAN_01'
)
INSERT INTO NHATKYHOATDONG(
    MaTaiKhoan, HanhDong, DoiTuongTacDong,
    MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet
)
VALUES (
    'TK_THUTHU01', N'Tạo phiếu mượn quá hạn demo', N'PHIEUMUON',
    'PM_DG023_QUAHAN_01', SYSDATETIME(), '127.0.0.1',
    N'Tạo dữ liệu demo độc giả đang mượn sách quá hạn chưa trả'
);

/* =========================================================
   9. BỔ SUNG DEMO KHOẢN NỢ ĐANG CÒN TIỀN PHẢI THU
   Các case này dùng cho màn Thu tiền phạt / Độc giả còn nợ:
   - DG024: nợ trả trễ, chưa thanh toán
   - DG025: nợ hỏng sách, chưa thanh toán
   - DG026: nhiều khoản nợ, đã thanh toán một phần
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG024')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG024', N'docgia24', N'$2a$10$demo_hash_docgia24', 'docgia24@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG024')
INSERT INTO DOCGIA(
    MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen,
    NgaySinh, DiaChi, Email, SoDienThoai,
    NgayLapThe, NgayHetHanThe
)
VALUES (
    'DG024', 'TK_DG024', 'NHOM_SINHVIEN', N'Đặng Hoài Nam',
    '2004-08-16', N'Tân Bình, TP.HCM', 'docgia24@gmail.com', '0922222224',
    CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE))
);

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG025')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG025', N'docgia25', N'$2a$10$demo_hash_docgia25', 'docgia25@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG025')
INSERT INTO DOCGIA(
    MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen,
    NgaySinh, DiaChi, Email, SoDienThoai,
    NgayLapThe, NgayHetHanThe
)
VALUES (
    'DG025', 'TK_DG025', 'NHOM_GIAOVIEN', N'Phan Thảo Vy',
    '1995-03-12', N'Quận 3, TP.HCM', 'docgia25@gmail.com', '0922222225',
    CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE))
);

IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE MaTaiKhoan = 'TK_DG026')
INSERT INTO TAIKHOAN(MaTaiKhoan, TenDangNhap, MatKhauHash, EmailDangNhap, MaVaiTro)
VALUES ('TK_DG026', N'docgia26', N'$2a$10$demo_hash_docgia26', 'docgia26@gmail.com', 'VT_DOC_GIA');

IF NOT EXISTS (SELECT 1 FROM DOCGIA WHERE MaDocGia = 'DG026')
INSERT INTO DOCGIA(
    MaDocGia, MaTaiKhoan, MaNhomDocGia, HoTen,
    NgaySinh, DiaChi, Email, SoDienThoai,
    NgayLapThe, NgayHetHanThe
)
VALUES (
    'DG026', 'TK_DG026', 'NHOM_KHAC', N'Bùi Minh Khôi',
    '1999-11-04', N'Quận 10, TP.HCM', 'docgia26@gmail.com', '0922222226',
    CAST(GETDATE() AS DATE), DATEADD(MONTH, 6, CAST(GETDATE() AS DATE))
);

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG024_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG024_01', 'DG024', 'GOI_THUONG', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói thường demo cho case nợ trả trễ');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG025_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG025_01', 'DG025', 'GOI_THUONG', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói thường demo cho case hỏng sách');

IF NOT EXISTS (SELECT 1 FROM LICHSUGOITHANHVIEN WHERE MaLichSuGoi = 'LSG_DG026_01')
INSERT INTO LICHSUGOITHANHVIEN(MaLichSuGoi, MaDocGia, MaGoiThanhVien, MaPhieuThu, NgayBatDau, NgayKetThuc, TrangThai, GhiChu)
VALUES ('LSG_DG026_01', 'DG026', 'GOI_THUONG', NULL, CAST(GETDATE() AS DATE), DATEADD(DAY, 180, CAST(GETDATE() AS DATE)), N'Đang sử dụng', N'Gói thường demo cho case nhiều khoản nợ');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'DEBT_TRE_001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode, GhiChu)
VALUES ('DEBT_TRE_001', 'CLEAN01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-DEBT-TRE-001', 'QR-DEBT-TRE-001', N'Bản demo tạo khoản nợ trả trễ');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'DEBT_HONG_001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode, GhiChu)
VALUES ('DEBT_HONG_001', 'CSDL01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-DEBT-HONG-001', 'QR-DEBT-HONG-001', N'Bản demo tạo khoản nợ hỏng sách');

IF NOT EXISTS (SELECT 1 FROM CUONSACH WHERE MaCuonSach = 'DEBT_MULTI_001')
INSERT INTO CUONSACH(MaCuonSach, MaDauSach, MaChiNhanh, MaViTri, MaTrangThai, MaVach, MaQRCode, GhiChu)
VALUES ('DEBT_MULTI_001', 'NETWORK01', 'CN_TD', 'VT_G01_N01', 'TT_SANCO', 'BAR-DEBT-MULTI-001', 'QR-DEBT-MULTI-001', N'Bản demo tạo nhiều khoản nợ');

/* DG024: nợ trả trễ, chưa thanh toán */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG024_TRE_01')
INSERT INTO PHIEUMUON(
    MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh,
    MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu
)
VALUES (
    'PM_DG024_TRE_01', 'DG024', 'NV_TT001', 'CN_TD',
    'QD_V1', DATEADD(DAY, -26, SYSDATETIME()), N'Đã trả hết',
    N'Demo nợ trả trễ chưa thanh toán'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG024_TRE_01')
INSERT INTO CHITIETPHIEUMUON(
    MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon,
    NgayMuon, HanTra, NgayTraThucTe, TrangThai
)
VALUES (
    'CTM_DG024_TRE_01', 'PM_DG024_TRE_01', 'DEBT_TRE_001', 'QDM_THUONG_CNTT_V1',
    DATEADD(DAY, -26, SYSDATETIME()), DATEADD(DAY, -19, SYSDATETIME()),
    DATEADD(DAY, -1, SYSDATETIME()), N'Đã trả'
);

IF NOT EXISTS (SELECT 1 FROM PHIEUTRA WHERE MaPhieuTra = 'PTR_DG024_TRE_01')
INSERT INTO PHIEUTRA(MaPhieuTra, MaDocGia, MaNhanVienNhan, MaChiNhanh, NgayTra, GhiChu)
VALUES ('PTR_DG024_TRE_01', 'DG024', 'NV_TT001', 'CN_TD', DATEADD(DAY, -1, SYSDATETIME()), N'Trả trễ 18 ngày, chưa thu tiền phạt');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTRA WHERE MaChiTietTra = 'CTT_DG024_TRE_01')
INSERT INTO CHITIETPHIEUTRA(
    MaChiTietTra, MaPhieuTra, MaChiTietMuon,
    TinhTrangKhiTra, SoNgayTre, TienPhatTre, TienPhatHongMat, GhiChu
)
VALUES (
    'CTT_DG024_TRE_01', 'PTR_DG024_TRE_01', 'CTM_DG024_TRE_01',
    N'Bình thường', 18, 18000, 0,
    N'Độc giả DG024 trả trễ 18 ngày, phát sinh 18.000đ tiền phạt'
);

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG024_TRE_01')
INSERT INTO KHOANNO(
    MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra,
    SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai
)
VALUES (
    'NO_DG024_TRE_01', 'DG024', 'NO_TRA_TRE', 'CTT_DG024_TRE_01',
    18000, 0, DATEADD(DAY, -1, SYSDATETIME()),
    N'Trả trễ sách Clean Code 18 ngày',
    N'Chưa thanh toán'
);

UPDATE CUONSACH
SET MaTrangThai = 'TT_SANCO'
WHERE MaCuonSach = 'DEBT_TRE_001';

/* DG025: nợ hỏng sách, chưa thanh toán */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG025_HONG_01')
INSERT INTO PHIEUMUON(
    MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh,
    MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu
)
VALUES (
    'PM_DG025_HONG_01', 'DG025', 'NV_TT001', 'CN_TD',
    'QD_V1', DATEADD(DAY, -8, SYSDATETIME()), N'Đã trả hết',
    N'Demo nợ hỏng sách chưa thanh toán'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG025_HONG_01')
INSERT INTO CHITIETPHIEUMUON(
    MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon,
    NgayMuon, HanTra, NgayTraThucTe, TrangThai
)
VALUES (
    'CTM_DG025_HONG_01', 'PM_DG025_HONG_01', 'DEBT_HONG_001', 'QDM_THUONG_CNTT_V1',
    DATEADD(DAY, -8, SYSDATETIME()), DATEADD(DAY, -1, SYSDATETIME()),
    DATEADD(DAY, -1, SYSDATETIME()), N'Hỏng'
);

IF NOT EXISTS (SELECT 1 FROM PHIEUTRA WHERE MaPhieuTra = 'PTR_DG025_HONG_01')
INSERT INTO PHIEUTRA(MaPhieuTra, MaDocGia, MaNhanVienNhan, MaChiNhanh, NgayTra, GhiChu)
VALUES ('PTR_DG025_HONG_01', 'DG025', 'NV_TT001', 'CN_TD', DATEADD(DAY, -1, SYSDATETIME()), N'Ghi nhận sách bị hỏng nặng');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTRA WHERE MaChiTietTra = 'CTT_DG025_HONG_01')
INSERT INTO CHITIETPHIEUTRA(
    MaChiTietTra, MaPhieuTra, MaChiTietMuon,
    TinhTrangKhiTra, SoNgayTre, TienPhatTre, TienPhatHongMat, GhiChu
)
VALUES (
    'CTT_DG025_HONG_01', 'PTR_DG025_HONG_01', 'CTM_DG025_HONG_01',
    N'Hỏng', 0, 0, 45000,
    N'Độc giả DG025 làm ướt và rách nhiều trang sách Cơ sở dữ liệu'
);

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG025_HONG_01')
INSERT INTO KHOANNO(
    MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra,
    SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai
)
VALUES (
    'NO_DG025_HONG_01', 'DG025', 'NO_HONG_SACH', 'CTT_DG025_HONG_01',
    45000, 0, DATEADD(DAY, -1, SYSDATETIME()),
    N'Làm hỏng sách Cơ sở dữ liệu',
    N'Chưa thanh toán'
);

UPDATE CUONSACH
SET MaTrangThai = 'TT_HONG'
WHERE MaCuonSach = 'DEBT_HONG_001';

/* DG026: nhiều khoản nợ, đã thu một phần */

IF NOT EXISTS (SELECT 1 FROM PHIEUMUON WHERE MaPhieuMuon = 'PM_DG026_MULTI_01')
INSERT INTO PHIEUMUON(
    MaPhieuMuon, MaDocGia, MaNhanVienLap, MaChiNhanh,
    MaPhienBanQuyDinh, NgayMuon, TrangThai, GhiChu
)
VALUES (
    'PM_DG026_MULTI_01', 'DG026', 'NV_TT001', 'CN_TD',
    'QD_V1', DATEADD(DAY, -20, SYSDATETIME()), N'Đã trả hết',
    N'Demo độc giả có nhiều khoản nợ và đã thanh toán một phần'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUMUON WHERE MaChiTietMuon = 'CTM_DG026_MULTI_01')
INSERT INTO CHITIETPHIEUMUON(
    MaChiTietMuon, MaPhieuMuon, MaCuonSach, MaQuyDinhMuon,
    NgayMuon, HanTra, NgayTraThucTe, TrangThai
)
VALUES (
    'CTM_DG026_MULTI_01', 'PM_DG026_MULTI_01', 'DEBT_MULTI_001', 'QDM_THUONG_CNTT_V1',
    DATEADD(DAY, -20, SYSDATETIME()), DATEADD(DAY, -13, SYSDATETIME()),
    DATEADD(DAY, -3, SYSDATETIME()), N'Mất'
);

IF NOT EXISTS (SELECT 1 FROM PHIEUTRA WHERE MaPhieuTra = 'PTR_DG026_MULTI_01')
INSERT INTO PHIEUTRA(MaPhieuTra, MaDocGia, MaNhanVienNhan, MaChiNhanh, NgayTra, GhiChu)
VALUES ('PTR_DG026_MULTI_01', 'DG026', 'NV_TT001', 'CN_TD', DATEADD(DAY, -3, SYSDATETIME()), N'Trả trễ và báo mất sách');

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTRA WHERE MaChiTietTra = 'CTT_DG026_MULTI_01')
INSERT INTO CHITIETPHIEUTRA(
    MaChiTietTra, MaPhieuTra, MaChiTietMuon,
    TinhTrangKhiTra, SoNgayTre, TienPhatTre, TienPhatHongMat, GhiChu
)
VALUES (
    'CTT_DG026_MULTI_01', 'PTR_DG026_MULTI_01', 'CTM_DG026_MULTI_01',
    N'Mất', 10, 10000, 90000,
    N'Độc giả DG026 trả trễ 10 ngày và làm mất sách Mạng máy tính căn bản'
);

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG026_TRE_01')
INSERT INTO KHOANNO(
    MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra,
    SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai
)
VALUES (
    'NO_DG026_TRE_01', 'DG026', 'NO_TRA_TRE', 'CTT_DG026_MULTI_01',
    10000, 5000, DATEADD(DAY, -3, SYSDATETIME()),
    N'Trả trễ sách Mạng máy tính căn bản 10 ngày',
    N'Thanh toán một phần'
);

IF NOT EXISTS (SELECT 1 FROM KHOANNO WHERE MaKhoanNo = 'NO_DG026_MAT_01')
INSERT INTO KHOANNO(
    MaKhoanNo, MaDocGia, MaLoaiKhoanNo, MaChiTietTra,
    SoTienPhatSinh, SoTienDaThanhToan, NgayPhatSinh, LyDo, TrangThai
)
VALUES (
    'NO_DG026_MAT_01', 'DG026', 'NO_MAT_SACH', 'CTT_DG026_MULTI_01',
    90000, 20000, DATEADD(DAY, -3, SYSDATETIME()),
    N'Làm mất sách Mạng máy tính căn bản',
    N'Thanh toán một phần'
);

IF NOT EXISTS (SELECT 1 FROM PHIEUTHU WHERE MaPhieuThu = 'PT_NO_DG026_01')
INSERT INTO PHIEUTHU(
    MaPhieuThu, MaDocGia, MaNhanVienThu, MaPhuongThuc,
    LoaiThu, SoTienThu, NgayThu, TrangThai, GhiChu
)
VALUES (
    'PT_NO_DG026_01', 'DG026', 'NV_TT001', 'PT_TIEN_MAT',
    N'Thu tiền phạt', 25000, DATEADD(DAY, -2, SYSDATETIME()), N'Thành công',
    N'Thu một phần cho hai khoản nợ của DG026'
);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTHU_NO WHERE MaChiTietPhieuThu = 'CTPT_DG026_TRE_01')
INSERT INTO CHITIETPHIEUTHU_NO(MaChiTietPhieuThu, MaPhieuThu, MaKhoanNo, SoTienApDung)
VALUES ('CTPT_DG026_TRE_01', 'PT_NO_DG026_01', 'NO_DG026_TRE_01', 5000);

IF NOT EXISTS (SELECT 1 FROM CHITIETPHIEUTHU_NO WHERE MaChiTietPhieuThu = 'CTPT_DG026_MAT_01')
INSERT INTO CHITIETPHIEUTHU_NO(MaChiTietPhieuThu, MaPhieuThu, MaKhoanNo, SoTienApDung)
VALUES ('CTPT_DG026_MAT_01', 'PT_NO_DG026_01', 'NO_DG026_MAT_01', 20000);

UPDATE CUONSACH
SET MaTrangThai = 'TT_MAT'
WHERE MaCuonSach = 'DEBT_MULTI_001';

IF NOT EXISTS (
    SELECT 1 FROM NHATKYHOATDONG
    WHERE HanhDong = N'Tạo khoản nợ demo'
      AND MaDoiTuongTacDong = 'NO_DG026_MAT_01'
)
INSERT INTO NHATKYHOATDONG(
    MaTaiKhoan, HanhDong, DoiTuongTacDong,
    MaDoiTuongTacDong, ThoiGian, DiaChiIP, MoTaChiTiet
)
VALUES (
    'TK_THUTHU01', N'Tạo khoản nợ demo', N'KHOANNO',
    'NO_DG026_MAT_01', SYSDATETIME(), '127.0.0.1',
    N'Tạo dữ liệu mẫu: DG026 có nhiều khoản nợ và đã thu một phần'
);

GO

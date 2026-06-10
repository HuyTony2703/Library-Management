        /* =========================================================
           DATABASE: QuanLyThuVien
           ========================================================= */

        IF DB_ID(N'QuanLyThuVien') IS NULL
        BEGIN
            CREATE DATABASE QuanLyThuVien;
        END
        GO

        USE QuanLyThuVien;
        GO

        /* =========================================================
           A. TÀI KHOẢN, VAI TRÒ, NHÂN VIÊN
           ========================================================= */

        CREATE TABLE VAITRO (
            MaVaiTro VARCHAR(20) PRIMARY KEY,
            TenVaiTro NVARCHAR(50) NOT NULL UNIQUE,
            MoTa NVARCHAR(255)
        );

        CREATE TABLE TAIKHOAN (
            MaTaiKhoan VARCHAR(30) PRIMARY KEY,
            TenDangNhap NVARCHAR(100) NOT NULL UNIQUE,
            MatKhauHash NVARCHAR(255) NOT NULL,
            EmailDangNhap VARCHAR(255) NOT NULL UNIQUE,
            MaVaiTro VARCHAR(20) NOT NULL,
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
            NgayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            LanDangNhapCuoi DATETIME2 NULL,

            CONSTRAINT FK_TAIKHOAN_VAITRO 
                FOREIGN KEY (MaVaiTro) REFERENCES VAITRO(MaVaiTro),

            CONSTRAINT CK_TAIKHOAN_TRANGTHAI 
                CHECK (TrangThai IN (N'Hoạt động', N'Khóa', N'Ngừng hoạt động'))
        );

        CREATE TABLE CHINHANH (
            MaChiNhanh VARCHAR(30) PRIMARY KEY,
            TenChiNhanh NVARCHAR(150) NOT NULL,
            DiaChi NVARCHAR(255) NOT NULL,
            SoDienThoai VARCHAR(20),
            Email VARCHAR(255),
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',

            CONSTRAINT CK_CHINHANH_TRANGTHAI
                CHECK (TrangThai IN (N'Hoạt động', N'Ngừng hoạt động'))
        );

        CREATE TABLE NHANVIEN (
            MaNhanVien VARCHAR(30) PRIMARY KEY,
            MaTaiKhoan VARCHAR(30) NOT NULL UNIQUE,
            MaChiNhanh VARCHAR(30) NULL,
            HoTen NVARCHAR(150) NOT NULL,
            NgaySinh DATE,
            Email VARCHAR(255),
            SoDienThoai VARCHAR(20),
            DiaChi NVARCHAR(255),
            NgayVaoLam DATE NOT NULL DEFAULT CAST(SYSDATETIME() AS DATE),
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang làm',

            CONSTRAINT FK_NHANVIEN_TAIKHOAN 
                FOREIGN KEY (MaTaiKhoan) REFERENCES TAIKHOAN(MaTaiKhoan),

            CONSTRAINT FK_NHANVIEN_CHINHANH 
                FOREIGN KEY (MaChiNhanh) REFERENCES CHINHANH(MaChiNhanh),

            CONSTRAINT CK_NHANVIEN_TRANGTHAI 
                CHECK (TrangThai IN (N'Đang làm', N'Nghỉ việc', N'Tạm khóa'))
        );

        CREATE TABLE NHATKYHOATDONG (
            MaNhatKy BIGINT IDENTITY(1,1) PRIMARY KEY,
            MaTaiKhoan VARCHAR(30) NULL,
            HanhDong NVARCHAR(100) NOT NULL,
            DoiTuongTacDong NVARCHAR(100),
            MaDoiTuongTacDong VARCHAR(50),
            ThoiGian DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            DiaChiIP VARCHAR(45),
            MoTaChiTiet NVARCHAR(MAX),

            CONSTRAINT FK_NHATKY_TAIKHOAN
                FOREIGN KEY (MaTaiKhoan) REFERENCES TAIKHOAN(MaTaiKhoan)
        );

        /* =========================================================
           B. ĐỘC GIẢ, NHÓM ĐỘC GIẢ, GÓI THÀNH VIÊN
           ========================================================= */

        CREATE TABLE NHOMDOCGIA (
            MaNhomDocGia VARCHAR(30) PRIMARY KEY,
            TenNhomDocGia NVARCHAR(100) NOT NULL UNIQUE,
            MoTa NVARCHAR(255)
        );

        CREATE TABLE GOITHANHVIEN (
            MaGoiThanhVien VARCHAR(30) PRIMARY KEY,
            TenGoi NVARCHAR(100) NOT NULL UNIQUE,
            MoTa NVARCHAR(255),
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',

            CONSTRAINT CK_GOITHANHVIEN_TRANGTHAI
                CHECK (TrangThai IN (N'Hoạt động', N'Ngừng áp dụng'))
        );

        CREATE TABLE DOCGIA (
            MaDocGia VARCHAR(30) PRIMARY KEY,
            MaTaiKhoan VARCHAR(30) NOT NULL UNIQUE,
            MaNhomDocGia VARCHAR(30) NOT NULL,
            HoTen NVARCHAR(150) NOT NULL,
            NgaySinh DATE NOT NULL,
            DiaChi NVARCHAR(255),
            Email VARCHAR(255) NOT NULL UNIQUE,
            SoDienThoai VARCHAR(20),
            NgayLapThe DATE NOT NULL DEFAULT CAST(SYSDATETIME() AS DATE),
            NgayHetHanThe DATE NOT NULL,
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',

            CONSTRAINT FK_DOCGIA_TAIKHOAN
                FOREIGN KEY (MaTaiKhoan) REFERENCES TAIKHOAN(MaTaiKhoan),

            CONSTRAINT FK_DOCGIA_NHOMDOCGIA
                FOREIGN KEY (MaNhomDocGia) REFERENCES NHOMDOCGIA(MaNhomDocGia),

            CONSTRAINT CK_DOCGIA_TRANGTHAI
                CHECK (TrangThai IN (N'Hoạt động', N'Hết hạn', N'Khóa', N'Ngừng hoạt động')),

            CONSTRAINT CK_DOCGIA_NGAYTHE
                CHECK (NgayHetHanThe > NgayLapThe)
        );

        /* =========================================================
           D. VỊ TRÍ SÁCH VÀ CHI NHÁNH
           ========================================================= */

        CREATE TABLE KHU (
            MaKhu VARCHAR(30) PRIMARY KEY,
            MaChiNhanh VARCHAR(30) NOT NULL,
            TenKhu NVARCHAR(100) NOT NULL,
            MoTa NVARCHAR(255),

            CONSTRAINT FK_KHU_CHINHANH
                FOREIGN KEY (MaChiNhanh) REFERENCES CHINHANH(MaChiNhanh)
        );

        CREATE TABLE KESACH (
            MaKeSach VARCHAR(30) PRIMARY KEY,
            MaKhu VARCHAR(30) NOT NULL,
            TenKeSach NVARCHAR(100) NOT NULL,
            MoTa NVARCHAR(255),

            CONSTRAINT FK_KESACH_KHU
                FOREIGN KEY (MaKhu) REFERENCES KHU(MaKhu)
        );

        CREATE TABLE VITRISACH (
            MaViTri VARCHAR(30) PRIMARY KEY,
            MaKeSach VARCHAR(30) NOT NULL,
            MaViTriHienThi NVARCHAR(100) NOT NULL,
            MoTa NVARCHAR(255),

            CONSTRAINT FK_VITRISACH_KESACH
                FOREIGN KEY (MaKeSach) REFERENCES KESACH(MaKeSach)
        );

        /* =========================================================
           E. SÁCH, ĐẦU SÁCH, CUỐN SÁCH
           ========================================================= */

        CREATE TABLE NHAXUATBAN (
            MaNhaXuatBan VARCHAR(30) PRIMARY KEY,
            TenNhaXuatBan NVARCHAR(150) NOT NULL,
            DiaChi NVARCHAR(255),
            Email VARCHAR(255),
            SoDienThoai VARCHAR(20)
        );

        CREATE TABLE TACGIA (
            MaTacGia VARCHAR(30) PRIMARY KEY,
            TenTacGia NVARCHAR(150) NOT NULL,
            MoTa NVARCHAR(255)
        );

        CREATE TABLE THELOAI (
            MaTheLoai VARCHAR(30) PRIMARY KEY,
            TenTheLoai NVARCHAR(100) NOT NULL UNIQUE,
            MoTa NVARCHAR(255),
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',

            CONSTRAINT CK_THELOAI_TRANGTHAI
                CHECK (TrangThai IN (N'Hoạt động', N'Ngừng áp dụng'))
        );

        CREATE TABLE DAUSACH (
            MaDauSach VARCHAR(30) PRIMARY KEY,
            MaNhaXuatBan VARCHAR(30) NULL,
            TenDauSach NVARCHAR(200) NOT NULL,
            ISBN VARCHAR(30) NULL,
            NamXuatBan INT NOT NULL,
            NgonNgu NVARCHAR(50),
            SoTrang INT,
            MoTa NVARCHAR(MAX),
            AnhBia NVARCHAR(500),
            TriGia DECIMAL(18,2) NOT NULL,
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',

            CONSTRAINT FK_DAUSACH_NHAXUATBAN
                FOREIGN KEY (MaNhaXuatBan) REFERENCES NHAXUATBAN(MaNhaXuatBan),

            CONSTRAINT CK_DAUSACH_NAMXB
                CHECK (NamXuatBan > 0),

            CONSTRAINT CK_DAUSACH_SOTRANG
                CHECK (SoTrang IS NULL OR SoTrang > 0),

            CONSTRAINT CK_DAUSACH_TRIGIA
                CHECK (TriGia >= 0),

            CONSTRAINT CK_DAUSACH_TRANGTHAI
                CHECK (TrangThai IN (N'Hoạt động', N'Ngừng hiển thị'))
        );

        CREATE UNIQUE INDEX UX_DAUSACH_ISBN 
        ON DAUSACH(ISBN)
        WHERE ISBN IS NOT NULL;

        CREATE TABLE DAUSACH_TACGIA (
            MaDauSach VARCHAR(30) NOT NULL,
            MaTacGia VARCHAR(30) NOT NULL,
            VaiTro NVARCHAR(100),

            CONSTRAINT PK_DAUSACH_TACGIA
                PRIMARY KEY (MaDauSach, MaTacGia),

            CONSTRAINT FK_DSTG_DAUSACH
                FOREIGN KEY (MaDauSach) REFERENCES DAUSACH(MaDauSach),

            CONSTRAINT FK_DSTG_TACGIA
                FOREIGN KEY (MaTacGia) REFERENCES TACGIA(MaTacGia)
        );

        CREATE TABLE DAUSACH_THELOAI (
            MaDauSach VARCHAR(30) NOT NULL,
            MaTheLoai VARCHAR(30) NOT NULL,

            CONSTRAINT PK_DAUSACH_THELOAI
                PRIMARY KEY (MaDauSach, MaTheLoai),

            CONSTRAINT FK_DSTL_DAUSACH
                FOREIGN KEY (MaDauSach) REFERENCES DAUSACH(MaDauSach),

            CONSTRAINT FK_DSTL_THELOAI
                FOREIGN KEY (MaTheLoai) REFERENCES THELOAI(MaTheLoai)
        );

        CREATE TABLE TRANGTHAICUONSACH (
            MaTrangThai VARCHAR(30) PRIMARY KEY,
            TenTrangThai NVARCHAR(100) NOT NULL UNIQUE,
            MoTa NVARCHAR(255)
        );

        CREATE TABLE CUONSACH (
            MaCuonSach VARCHAR(40) PRIMARY KEY,
            MaDauSach VARCHAR(30) NOT NULL,
            MaChiNhanh VARCHAR(30) NOT NULL,
            MaViTri VARCHAR(30) NOT NULL,
            MaTrangThai VARCHAR(30) NOT NULL,
            MaVach VARCHAR(100) NULL,
            MaQRCode VARCHAR(100) NULL,
            NgayNhapSach DATE NOT NULL DEFAULT CAST(SYSDATETIME() AS DATE),
            GhiChu NVARCHAR(255),

            CONSTRAINT FK_CUONSACH_DAUSACH
                FOREIGN KEY (MaDauSach) REFERENCES DAUSACH(MaDauSach),

            CONSTRAINT FK_CUONSACH_CHINHANH
                FOREIGN KEY (MaChiNhanh) REFERENCES CHINHANH(MaChiNhanh),

            CONSTRAINT FK_CUONSACH_VITRISACH
                FOREIGN KEY (MaViTri) REFERENCES VITRISACH(MaViTri),

            CONSTRAINT FK_CUONSACH_TRANGTHAI
                FOREIGN KEY (MaTrangThai) REFERENCES TRANGTHAICUONSACH(MaTrangThai)
        );

        CREATE UNIQUE INDEX UX_CUONSACH_MAVACH
        ON CUONSACH(MaVach)
        WHERE MaVach IS NOT NULL;

        CREATE UNIQUE INDEX UX_CUONSACH_QRCODE
        ON CUONSACH(MaQRCode)
        WHERE MaQRCode IS NOT NULL;

        /* =========================================================
           C. QUY ĐỊNH HỆ THỐNG
           ========================================================= */

        CREATE TABLE PHIENBANQUYDINH (
            MaPhienBan VARCHAR(30) PRIMARY KEY,
            TenPhienBan NVARCHAR(100) NOT NULL,
            NgayApDung DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            MaNhanVienThayDoi VARCHAR(30) NULL,
            GhiChu NVARCHAR(255),
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang áp dụng',

            CONSTRAINT FK_PBQD_NHANVIEN
                FOREIGN KEY (MaNhanVienThayDoi) REFERENCES NHANVIEN(MaNhanVien),

            CONSTRAINT CK_PBQD_TRANGTHAI
                CHECK (TrangThai IN (N'Đang áp dụng', N'Ngừng áp dụng', N'Dự thảo'))
        );

        CREATE TABLE THAMSOQUYDINH (
            MaThamSo VARCHAR(30) PRIMARY KEY,
            MaPhienBan VARCHAR(30) NOT NULL UNIQUE,
            TuoiToiThieu INT NOT NULL,
            TuoiToiDa INT NOT NULL,
            ThoiHanTheTheoThang INT NOT NULL,
            KhoangCachNamXuatBan INT NOT NULL,
            SoNgayNhacTruocHan INT NOT NULL,
            SoNgayGiuDatTruoc INT NOT NULL,
            MucPhatTreMoiNgay DECIMAL(18,2) NOT NULL,

            CONSTRAINT FK_THAMSO_PBQD
                FOREIGN KEY (MaPhienBan) REFERENCES PHIENBANQUYDINH(MaPhienBan),

            CONSTRAINT CK_THAMSO_TUOI
                CHECK (TuoiToiThieu > 0 AND TuoiToiDa >= TuoiToiThieu),

            CONSTRAINT CK_THAMSO_DUONG
                CHECK (
                    ThoiHanTheTheoThang > 0
                    AND KhoangCachNamXuatBan >= 0
                    AND SoNgayNhacTruocHan >= 0
                    AND SoNgayGiuDatTruoc >= 0
                    AND MucPhatTreMoiNgay >= 0
                )
        );

        CREATE TABLE GIAGOI_THEONHOM (
            MaGiaGoi VARCHAR(30) PRIMARY KEY,
            MaPhienBan VARCHAR(30) NOT NULL,
            MaGoiThanhVien VARCHAR(30) NOT NULL,
            MaNhomDocGia VARCHAR(30) NOT NULL,
            GiaTien DECIMAL(18,2) NOT NULL,
            ThoiHanGoiTheoNgay INT NOT NULL,

            CONSTRAINT FK_GGTN_PBQD
                FOREIGN KEY (MaPhienBan) REFERENCES PHIENBANQUYDINH(MaPhienBan),

            CONSTRAINT FK_GGTN_GOI
                FOREIGN KEY (MaGoiThanhVien) REFERENCES GOITHANHVIEN(MaGoiThanhVien),

            CONSTRAINT FK_GGTN_NHOM
                FOREIGN KEY (MaNhomDocGia) REFERENCES NHOMDOCGIA(MaNhomDocGia),

            CONSTRAINT UQ_GGTN
                UNIQUE (MaPhienBan, MaGoiThanhVien, MaNhomDocGia),

            CONSTRAINT CK_GGTN_GIA
                CHECK (GiaTien >= 0 AND ThoiHanGoiTheoNgay > 0)
        );

        CREATE TABLE QUYDINHGOI (
            MaQuyDinhGoi VARCHAR(30) PRIMARY KEY,
            MaPhienBan VARCHAR(30) NOT NULL,
            MaGoiThanhVien VARCHAR(30) NOT NULL,
            SoSachMuonToiDa INT NOT NULL,
            SoLanGiaHanToiDa INT NOT NULL,

            CONSTRAINT FK_QDG_PBQD
                FOREIGN KEY (MaPhienBan) REFERENCES PHIENBANQUYDINH(MaPhienBan),

            CONSTRAINT FK_QDG_GOI
                FOREIGN KEY (MaGoiThanhVien) REFERENCES GOITHANHVIEN(MaGoiThanhVien),

            CONSTRAINT UQ_QDG
                UNIQUE (MaPhienBan, MaGoiThanhVien),

            CONSTRAINT CK_QDG_SO
                CHECK (SoSachMuonToiDa > 0 AND SoLanGiaHanToiDa >= 0)
        );

        CREATE TABLE QUYDINHMUON_THELOAI (
            MaQuyDinhMuon VARCHAR(30) PRIMARY KEY,
            MaPhienBan VARCHAR(30) NOT NULL,
            MaGoiThanhVien VARCHAR(30) NOT NULL,
            MaTheLoai VARCHAR(30) NOT NULL,
            SoNgayMuon INT NOT NULL,
            SoNgayGiaHanMoiLan INT NOT NULL,

            CONSTRAINT FK_QDMTL_PBQD
                FOREIGN KEY (MaPhienBan) REFERENCES PHIENBANQUYDINH(MaPhienBan),

            CONSTRAINT FK_QDMTL_GOI
                FOREIGN KEY (MaGoiThanhVien) REFERENCES GOITHANHVIEN(MaGoiThanhVien),

            CONSTRAINT FK_QDMTL_THELOAI
                FOREIGN KEY (MaTheLoai) REFERENCES THELOAI(MaTheLoai),

            CONSTRAINT UQ_QDMTL
                UNIQUE (MaPhienBan, MaGoiThanhVien, MaTheLoai),

            CONSTRAINT CK_QDMTL_SO
                CHECK (SoNgayMuon > 0 AND SoNgayGiaHanMoiLan >= 0)
        );

        /* =========================================================
           F. MƯỢN SÁCH
           ========================================================= */

        CREATE TABLE PHIEUMUON (
            MaPhieuMuon VARCHAR(30) PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaNhanVienLap VARCHAR(30) NOT NULL,
            MaChiNhanh VARCHAR(30) NOT NULL,
            MaPhienBanQuyDinh VARCHAR(30) NOT NULL,
            NgayMuon DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang mượn',
            GhiChu NVARCHAR(255),

            CONSTRAINT FK_PM_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT FK_PM_NHANVIEN
                FOREIGN KEY (MaNhanVienLap) REFERENCES NHANVIEN(MaNhanVien),

            CONSTRAINT FK_PM_CHINHANH
                FOREIGN KEY (MaChiNhanh) REFERENCES CHINHANH(MaChiNhanh),

            CONSTRAINT FK_PM_PBQD
                FOREIGN KEY (MaPhienBanQuyDinh) REFERENCES PHIENBANQUYDINH(MaPhienBan),

            CONSTRAINT CK_PM_TRANGTHAI
                CHECK (TrangThai IN (N'Đang mượn', N'Đã trả hết', N'Đã hủy'))
        );

        CREATE TABLE CHITIETPHIEUMUON (
            MaChiTietMuon VARCHAR(30) PRIMARY KEY,
            MaPhieuMuon VARCHAR(30) NOT NULL,
            MaCuonSach VARCHAR(40) NOT NULL,
            MaQuyDinhMuon VARCHAR(30) NOT NULL,
            NgayMuon DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            HanTra DATETIME2 NOT NULL,
            NgayTraThucTe DATETIME2 NULL,
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang mượn',

            CONSTRAINT FK_CTPM_PM
                FOREIGN KEY (MaPhieuMuon) REFERENCES PHIEUMUON(MaPhieuMuon),

            CONSTRAINT FK_CTPM_CUONSACH
                FOREIGN KEY (MaCuonSach) REFERENCES CUONSACH(MaCuonSach),

            CONSTRAINT FK_CTPM_QDMTL
                FOREIGN KEY (MaQuyDinhMuon) REFERENCES QUYDINHMUON_THELOAI(MaQuyDinhMuon),

            CONSTRAINT CK_CTPM_NGAY
                CHECK (HanTra > NgayMuon),

            CONSTRAINT CK_CTPM_TRANGTHAI
                CHECK (TrangThai IN (N'Đang mượn', N'Đã trả', N'Mất', N'Hỏng', N'Đã hủy'))
        );

        CREATE UNIQUE INDEX UX_CTPM_CUONSACH_DANGMUON
        ON CHITIETPHIEUMUON(MaCuonSach)
        WHERE TrangThai = N'Đang mượn';

        /* =========================================================
           G. GIA HẠN SÁCH
           ========================================================= */

        CREATE TABLE LICHSUGIAHAN (
            MaGiaHan VARCHAR(30) PRIMARY KEY,
            MaChiTietMuon VARCHAR(30) NOT NULL,
            MaDocGia VARCHAR(30) NOT NULL,
            NgayGiaHan DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            HanTraCu DATETIME2 NOT NULL,
            HanTraMoi DATETIME2 NOT NULL,
            SoNgayGiaHan INT NOT NULL,
            LanGiaHanThu INT NOT NULL,
            TrangThai NVARCHAR(30) NOT NULL,
            LyDoTuChoi NVARCHAR(255) NULL,

            CONSTRAINT FK_LSGH_CTPM
                FOREIGN KEY (MaChiTietMuon) REFERENCES CHITIETPHIEUMUON(MaChiTietMuon),

            CONSTRAINT FK_LSGH_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT CK_LSGH_NGAY
                CHECK (HanTraMoi >= HanTraCu AND SoNgayGiaHan >= 0 AND LanGiaHanThu > 0),

            CONSTRAINT CK_LSGH_TRANGTHAI
                CHECK (TrangThai IN (N'Thành công', N'Từ chối'))
        );

        /* =========================================================
           H. TRẢ SÁCH
           ========================================================= */

        CREATE TABLE PHIEUTRA (
            MaPhieuTra VARCHAR(30) PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaNhanVienNhan VARCHAR(30) NOT NULL,
            MaChiNhanh VARCHAR(30) NOT NULL,
            NgayTra DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            GhiChu NVARCHAR(255),

            CONSTRAINT FK_PT_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT FK_PT_NHANVIEN
                FOREIGN KEY (MaNhanVienNhan) REFERENCES NHANVIEN(MaNhanVien),

            CONSTRAINT FK_PT_CHINHANH
                FOREIGN KEY (MaChiNhanh) REFERENCES CHINHANH(MaChiNhanh)
        );

        CREATE TABLE CHITIETPHIEUTRA (
            MaChiTietTra VARCHAR(30) PRIMARY KEY,
            MaPhieuTra VARCHAR(30) NOT NULL,
            MaChiTietMuon VARCHAR(30) NOT NULL UNIQUE,
            TinhTrangKhiTra NVARCHAR(30) NOT NULL,
            SoNgayTre INT NOT NULL DEFAULT 0,
            TienPhatTre DECIMAL(18,2) NOT NULL DEFAULT 0,
            TienPhatHongMat DECIMAL(18,2) NOT NULL DEFAULT 0,
            GhiChu NVARCHAR(255),

            CONSTRAINT FK_CTPT_PT
                FOREIGN KEY (MaPhieuTra) REFERENCES PHIEUTRA(MaPhieuTra),

            CONSTRAINT FK_CTPT_CTPM
                FOREIGN KEY (MaChiTietMuon) REFERENCES CHITIETPHIEUMUON(MaChiTietMuon),

            CONSTRAINT CK_CTPT_TINHTRANG
                CHECK (TinhTrangKhiTra IN (N'Bình thường', N'Hỏng', N'Mất')),

            CONSTRAINT CK_CTPT_TIEN
                CHECK (SoNgayTre >= 0 AND TienPhatTre >= 0 AND TienPhatHongMat >= 0)
        );

        /* =========================================================
           I. KHOẢN NỢ, PHẠT, THANH TOÁN
           ========================================================= */

        CREATE TABLE LOAIKHOANNO (
            MaLoaiKhoanNo VARCHAR(30) PRIMARY KEY,
            TenLoaiKhoanNo NVARCHAR(100) NOT NULL UNIQUE,
            MoTa NVARCHAR(255)
        );

        CREATE TABLE PHUONGTHUCTHANHTOAN (
            MaPhuongThuc VARCHAR(30) PRIMARY KEY,
            TenPhuongThuc NVARCHAR(100) NOT NULL UNIQUE,
            MoTa NVARCHAR(255)
        );

        CREATE TABLE KHOANNO (
            MaKhoanNo VARCHAR(30) PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaLoaiKhoanNo VARCHAR(30) NOT NULL,
            MaChiTietTra VARCHAR(30) NULL,
            SoTienPhatSinh DECIMAL(18,2) NOT NULL,
            SoTienDaThanhToan DECIMAL(18,2) NOT NULL DEFAULT 0,
            SoTienConLai AS (SoTienPhatSinh - SoTienDaThanhToan) PERSISTED,
            NgayPhatSinh DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            LyDo NVARCHAR(255),
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Chưa thanh toán',

            CONSTRAINT FK_KHOANNO_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT FK_KHOANNO_LOAI
                FOREIGN KEY (MaLoaiKhoanNo) REFERENCES LOAIKHOANNO(MaLoaiKhoanNo),

            CONSTRAINT FK_KHOANNO_CTPT
                FOREIGN KEY (MaChiTietTra) REFERENCES CHITIETPHIEUTRA(MaChiTietTra),

            CONSTRAINT CK_KHOANNO_TIEN
                CHECK (
                    SoTienPhatSinh >= 0
                    AND SoTienDaThanhToan >= 0
                    AND SoTienDaThanhToan <= SoTienPhatSinh
                ),

            CONSTRAINT CK_KHOANNO_TRANGTHAI
                CHECK (TrangThai IN (N'Chưa thanh toán', N'Thanh toán một phần', N'Đã thanh toán'))
        );

        CREATE TABLE PHIEUTHU (
            MaPhieuThu VARCHAR(30) PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaNhanVienThu VARCHAR(30) NULL,
            MaPhuongThuc VARCHAR(30) NOT NULL,
            LoaiThu NVARCHAR(50) NOT NULL,
            SoTienThu DECIMAL(18,2) NOT NULL,
            NgayThu DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            MaGiaoDichNgoai VARCHAR(100) NULL,
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Thành công',
            GhiChu NVARCHAR(255),

            CONSTRAINT FK_PHIEUTHU_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT FK_PHIEUTHU_NHANVIEN
                FOREIGN KEY (MaNhanVienThu) REFERENCES NHANVIEN(MaNhanVien),

            CONSTRAINT FK_PHIEUTHU_PTTT
                FOREIGN KEY (MaPhuongThuc) REFERENCES PHUONGTHUCTHANHTOAN(MaPhuongThuc),

            CONSTRAINT CK_PHIEUTHU_LOAITHU
                CHECK (LoaiThu IN (N'Thu tiền phạt', N'Thu tiền mua gói')),

            CONSTRAINT CK_PHIEUTHU_TIEN
                CHECK (SoTienThu >= 0),

            CONSTRAINT CK_PHIEUTHU_TRANGTHAI
                CHECK (TrangThai IN (N'Chờ xử lý', N'Thành công', N'Thất bại', N'Đã hủy'))
        );

        CREATE TABLE CHITIETPHIEUTHU_NO (
            MaChiTietPhieuThu VARCHAR(30) PRIMARY KEY,
            MaPhieuThu VARCHAR(30) NOT NULL,
            MaKhoanNo VARCHAR(30) NOT NULL,
            SoTienApDung DECIMAL(18,2) NOT NULL,

            CONSTRAINT FK_CTPTN_PHIEUTHU
                FOREIGN KEY (MaPhieuThu) REFERENCES PHIEUTHU(MaPhieuThu),

            CONSTRAINT FK_CTPTN_KHOANNO
                FOREIGN KEY (MaKhoanNo) REFERENCES KHOANNO(MaKhoanNo),

            CONSTRAINT CK_CTPTN_TIEN
                CHECK (SoTienApDung > 0)
        );

        CREATE TABLE LICHSUGOITHANHVIEN (
            MaLichSuGoi VARCHAR(30) PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaGoiThanhVien VARCHAR(30) NOT NULL,
            MaPhieuThu VARCHAR(30) NULL,
            NgayBatDau DATE NOT NULL,
            NgayKetThuc DATE NOT NULL,
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang sử dụng',
            GhiChu NVARCHAR(255),

            CONSTRAINT FK_LSGTV_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT FK_LSGTV_GOI
                FOREIGN KEY (MaGoiThanhVien) REFERENCES GOITHANHVIEN(MaGoiThanhVien),

            CONSTRAINT FK_LSGTV_PHIEUTHU
                FOREIGN KEY (MaPhieuThu) REFERENCES PHIEUTHU(MaPhieuThu),

            CONSTRAINT CK_LSGTV_NGAY
                CHECK (NgayKetThuc > NgayBatDau),

            CONSTRAINT CK_LSGTV_TRANGTHAI
                CHECK (TrangThai IN (N'Đang sử dụng', N'Hết hạn', N'Đã hủy', N'Đã nâng cấp'))
        );

        /* =========================================================
           J. ĐẶT TRƯỚC SÁCH
           ========================================================= */

        CREATE TABLE PHIEUDATTRUOC (
            MaPhieuDatTruoc VARCHAR(30) PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaDauSach VARCHAR(30) NOT NULL,
            MaCuonSachDuocGiu VARCHAR(40) NULL,
            MaChiNhanh VARCHAR(30) NOT NULL,
            NgayDat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            NgayHetHanGiuCho DATETIME2 NULL,
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang chờ',
            GhiChu NVARCHAR(255),

            CONSTRAINT FK_PDT_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT FK_PDT_DAUSACH
                FOREIGN KEY (MaDauSach) REFERENCES DAUSACH(MaDauSach),

            CONSTRAINT FK_PDT_CUONSACH
                FOREIGN KEY (MaCuonSachDuocGiu) REFERENCES CUONSACH(MaCuonSach),

            CONSTRAINT FK_PDT_CHINHANH
                FOREIGN KEY (MaChiNhanh) REFERENCES CHINHANH(MaChiNhanh),

            CONSTRAINT CK_PDT_TRANGTHAI
                CHECK (TrangThai IN (N'Đang chờ', N'Đã giữ chỗ', N'Đã mượn', N'Đã hủy', N'Đã hết hạn'))
        );

        CREATE UNIQUE INDEX UX_PDT_CUONSACH_DAGIUCHOO
        ON PHIEUDATTRUOC(MaCuonSachDuocGiu)
        WHERE TrangThai = N'Đã giữ chỗ' AND MaCuonSachDuocGiu IS NOT NULL;

        /* =========================================================
           K. ĐÁNH GIÁ VÀ BÌNH LUẬN SÁCH
           ========================================================= */

        CREATE TABLE DANHGIA (
            MaDanhGia VARCHAR(30) PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaDauSach VARCHAR(30) NOT NULL,
            SoSao INT NOT NULL,
            NoiDung NVARCHAR(MAX),
            NgayDanhGia DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hiển thị',

            CONSTRAINT FK_DANHGIA_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT FK_DANHGIA_DAUSACH
                FOREIGN KEY (MaDauSach) REFERENCES DAUSACH(MaDauSach),

            CONSTRAINT UQ_DANHGIA_DOCGIA_DAUSACH
                UNIQUE (MaDocGia, MaDauSach),

            CONSTRAINT CK_DANHGIA_SOSAO
                CHECK (SoSao BETWEEN 1 AND 5),

            CONSTRAINT CK_DANHGIA_TRANGTHAI
                CHECK (TrangThai IN (N'Hiển thị', N'Đã ẩn', N'Đã xóa'))
        );

        CREATE TABLE BINHLUAN (
            MaBinhLuan VARCHAR(30) PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaDauSach VARCHAR(30) NOT NULL,
            NoiDung NVARCHAR(MAX) NOT NULL,
            NgayBinhLuan DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hiển thị',
            MaNhanVienXuLy VARCHAR(30) NULL,
            LyDoAnXoa NVARCHAR(255) NULL,

            CONSTRAINT FK_BINHLUAN_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT FK_BINHLUAN_DAUSACH
                FOREIGN KEY (MaDauSach) REFERENCES DAUSACH(MaDauSach),

            CONSTRAINT FK_BINHLUAN_NHANVIEN
                FOREIGN KEY (MaNhanVienXuLy) REFERENCES NHANVIEN(MaNhanVien),

            CONSTRAINT CK_BINHLUAN_TRANGTHAI
                CHECK (TrangThai IN (N'Hiển thị', N'Đã ẩn', N'Đã xóa'))
        );

        CREATE TABLE SACHYEUTHICH (
            MaYeuThich VARCHAR(30) PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaDauSach VARCHAR(30) NOT NULL,
            NgayThem DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

            CONSTRAINT FK_SYT_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES DOCGIA(MaDocGia),

            CONSTRAINT FK_SYT_DAUSACH
                FOREIGN KEY (MaDauSach) REFERENCES DAUSACH(MaDauSach),

            CONSTRAINT UQ_SYT_DOCGIA_DAUSACH
                UNIQUE (MaDocGia, MaDauSach)
        );

        /* =========================================================
           L. THÔNG BÁO VÀ EMAIL
           ========================================================= */

        CREATE TABLE LOAITHONGBAO (
            MaLoaiThongBao VARCHAR(30) PRIMARY KEY,
            TenLoaiThongBao NVARCHAR(100) NOT NULL UNIQUE,
            MoTa NVARCHAR(255)
        );

        CREATE TABLE THONGBAO (
            MaThongBao VARCHAR(30) PRIMARY KEY,
            MaTaiKhoanNhan VARCHAR(30) NOT NULL,
            MaLoaiThongBao VARCHAR(30) NOT NULL,
            TieuDe NVARCHAR(200) NOT NULL,
            NoiDung NVARCHAR(MAX) NOT NULL,
            NgayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
            GuiTrongApp BIT NOT NULL DEFAULT 1,
            GuiEmail BIT NOT NULL DEFAULT 0,
            TrangThaiEmail NVARCHAR(30) NOT NULL DEFAULT N'Không gửi',
            SoLanThuGuiEmail INT NOT NULL DEFAULT 0,
            ThoiGianGuiEmailCuoi DATETIME2 NULL,
            DaDoc BIT NOT NULL DEFAULT 0,
            ThoiGianDoc DATETIME2 NULL,

            CONSTRAINT FK_THONGBAO_TAIKHOAN
                FOREIGN KEY (MaTaiKhoanNhan) REFERENCES TAIKHOAN(MaTaiKhoan),

            CONSTRAINT FK_THONGBAO_LOAI
                FOREIGN KEY (MaLoaiThongBao) REFERENCES LOAITHONGBAO(MaLoaiThongBao),

            CONSTRAINT CK_THONGBAO_EMAIL
                CHECK (TrangThaiEmail IN (N'Không gửi', N'Chờ gửi', N'Thành công', N'Thất bại')),

            CONSTRAINT CK_THONGBAO_SOLAN
                CHECK (SoLanThuGuiEmail >= 0)
        );

        /* =========================================================
           INDEX TỐI ƯU TRUY VẤN
           ========================================================= */

        CREATE INDEX IX_TAIKHOAN_VAITRO ON TAIKHOAN(MaVaiTro);
        CREATE INDEX IX_DOCGIA_NHOM ON DOCGIA(MaNhomDocGia);
        CREATE INDEX IX_NHANVIEN_CHINHANH ON NHANVIEN(MaChiNhanh);

        CREATE INDEX IX_CUONSACH_DAUSACH ON CUONSACH(MaDauSach);
        CREATE INDEX IX_CUONSACH_DAUSACH_TRANGTHAI_NGAYNHAP ON CUONSACH(MaDauSach, MaTrangThai, NgayNhapSach DESC);
        CREATE INDEX IX_CUONSACH_CHINHANH_TRANGTHAI ON CUONSACH(MaChiNhanh, MaTrangThai);
        CREATE INDEX IX_DAUSACH_TEN ON DAUSACH(TenDauSach);

        CREATE INDEX IX_PHIEUMUON_DOCGIA ON PHIEUMUON(MaDocGia);
        CREATE INDEX IX_PHIEUMUON_CHINHANH ON PHIEUMUON(MaChiNhanh);
        CREATE INDEX IX_CTPM_PHIEUMUON ON CHITIETPHIEUMUON(MaPhieuMuon);
        CREATE INDEX IX_CTPM_CUONSACH ON CHITIETPHIEUMUON(MaCuonSach);
        CREATE INDEX IX_CTPM_CUONSACH_NGAYMUON ON CHITIETPHIEUMUON(MaCuonSach, NgayMuon DESC);

        CREATE INDEX IX_PHIEUTRA_DOCGIA ON PHIEUTRA(MaDocGia);
        CREATE INDEX IX_KHOANNO_DOCGIA ON KHOANNO(MaDocGia);
        CREATE INDEX IX_THONGBAO_TAIKHOAN ON THONGBAO(MaTaiKhoanNhan);
        CREATE INDEX IX_THONGBAO_TAIKHOAN_DADOC ON THONGBAO(MaTaiKhoanNhan, DaDoc, NgayTao);
        CREATE INDEX IX_SACHYEUTHICH_DOCGIA_NGAYTHEM ON SACHYEUTHICH(MaDocGia, NgayThem DESC);
        CREATE INDEX IX_SACHYEUTHICH_DAUSACH ON SACHYEUTHICH(MaDauSach);
        CREATE INDEX IX_DANHGIA_DAUSACH_TRANGTHAI_SOSAO ON DANHGIA(MaDauSach, TrangThai, SoSao);

        /* =========================================================
           DỮ LIỆU DANH MỤC MẶC ĐỊNH
           ========================================================= */

        MERGE VAITRO AS T
        USING (VALUES
            ('VT_DOC_GIA', N'DOC_GIA', N'Độc giả'),
            ('VT_THU_THU', N'THU_THU', N'Thủ thư'),
            ('VT_ADMIN', N'QUAN_TRI_VIEN', N'Quản trị viên')
        ) AS S(MaVaiTro, TenVaiTro, MoTa)
        ON T.MaVaiTro = S.MaVaiTro
        WHEN NOT MATCHED THEN
            INSERT (MaVaiTro, TenVaiTro, MoTa)
            VALUES (S.MaVaiTro, S.TenVaiTro, S.MoTa);

        MERGE NHOMDOCGIA AS T
        USING (VALUES
            ('NHOM_HOCSINH', N'Học sinh', N'Nhóm độc giả học sinh'),
            ('NHOM_SINHVIEN', N'Sinh viên', N'Nhóm độc giả sinh viên'),
            ('NHOM_GIAOVIEN', N'Giáo viên', N'Nhóm độc giả giáo viên'),
            ('NHOM_KHAC', N'Khác', N'Nhóm độc giả khác')
        ) AS S(MaNhomDocGia, TenNhomDocGia, MoTa)
        ON T.MaNhomDocGia = S.MaNhomDocGia
        WHEN NOT MATCHED THEN
            INSERT (MaNhomDocGia, TenNhomDocGia, MoTa)
            VALUES (S.MaNhomDocGia, S.TenNhomDocGia, S.MoTa);

        MERGE GOITHANHVIEN AS T
        USING (VALUES
            ('GOI_THUONG', N'Thường', N'Gói thành viên cơ bản', N'Hoạt động'),
            ('GOI_VIP', N'VIP', N'Gói thành viên nâng cao', N'Hoạt động'),
            ('GOI_PREMIUM', N'Premium', N'Gói thành viên cao cấp', N'Hoạt động')
        ) AS S(MaGoiThanhVien, TenGoi, MoTa, TrangThai)
        ON T.MaGoiThanhVien = S.MaGoiThanhVien
        WHEN NOT MATCHED THEN
            INSERT (MaGoiThanhVien, TenGoi, MoTa, TrangThai)
            VALUES (S.MaGoiThanhVien, S.TenGoi, S.MoTa, S.TrangThai);

        MERGE TRANGTHAICUONSACH AS T
        USING (VALUES
            ('TT_SANCO', N'Sẵn có', N'Cuốn sách đang sẵn sàng cho mượn'),
            ('TT_DANGMUON', N'Đang được mượn', N'Cuốn sách đang được độc giả mượn'),
            ('TT_DANGDATTRUOC', N'Đang được đặt trước', N'Cuốn sách đang được giữ chỗ'),
            ('TT_MAT', N'Bị mất', N'Cuốn sách đã bị mất'),
            ('TT_HONG', N'Bị hỏng', N'Cuốn sách bị hư hỏng'),
            ('TT_NGUNGLUUTHONG', N'Ngừng lưu thông', N'Cuốn sách không còn được cho mượn')
        ) AS S(MaTrangThai, TenTrangThai, MoTa)
        ON T.MaTrangThai = S.MaTrangThai
        WHEN NOT MATCHED THEN
            INSERT (MaTrangThai, TenTrangThai, MoTa)
            VALUES (S.MaTrangThai, S.TenTrangThai, S.MoTa);

        MERGE LOAIKHOANNO AS T
        USING (VALUES
            ('NO_TRA_TRE', N'Trả trễ', N'Nợ phát sinh do trả sách trễ hạn'),
            ('NO_MAT_SACH', N'Mất sách', N'Nợ phát sinh do làm mất sách'),
            ('NO_HONG_SACH', N'Hỏng sách', N'Nợ phát sinh do làm hỏng sách'),
            ('NO_KHAC', N'Khác', N'Khoản nợ khác')
        ) AS S(MaLoaiKhoanNo, TenLoaiKhoanNo, MoTa)
        ON T.MaLoaiKhoanNo = S.MaLoaiKhoanNo
        WHEN NOT MATCHED THEN
            INSERT (MaLoaiKhoanNo, TenLoaiKhoanNo, MoTa)
            VALUES (S.MaLoaiKhoanNo, S.TenLoaiKhoanNo, S.MoTa);

        MERGE PHUONGTHUCTHANHTOAN AS T
        USING (VALUES
            ('PT_TIEN_MAT', N'Tiền mặt', N'Thanh toán bằng tiền mặt'),
            ('PT_CHUYEN_KHOAN', N'Chuyển khoản', N'Thanh toán bằng chuyển khoản'),
            ('PT_VI_DIEN_TU', N'Ví điện tử', N'Thanh toán bằng ví điện tử'),
            ('PT_ONLINE', N'Cổng thanh toán online', N'Thanh toán qua cổng thanh toán online')
        ) AS S(MaPhuongThuc, TenPhuongThuc, MoTa)
        ON T.MaPhuongThuc = S.MaPhuongThuc
        WHEN NOT MATCHED THEN
            INSERT (MaPhuongThuc, TenPhuongThuc, MoTa)
            VALUES (S.MaPhuongThuc, S.TenPhuongThuc, S.MoTa);

        MERGE LOAITHONGBAO AS T
        USING (VALUES
            ('TB_SAP_DEN_HAN', N'Sắp đến hạn trả', N'Thông báo nhắc độc giả sắp đến hạn trả sách'),
            ('TB_GIA_HAN_TC', N'Gia hạn thành công', N'Thông báo gia hạn sách thành công'),
            ('TB_BI_PHAT', N'Bị tính tiền phạt', N'Thông báo phát sinh tiền phạt'),
            ('TB_DAT_TRUOC_TC', N'Đặt trước thành công', N'Thông báo đặt trước sách thành công'),
            ('TB_SACH_DA_CO', N'Sách đặt trước đã có', N'Thông báo sách đặt trước đã sẵn sàng')
        ) AS S(MaLoaiThongBao, TenLoaiThongBao, MoTa)
        ON T.MaLoaiThongBao = S.MaLoaiThongBao
        WHEN NOT MATCHED THEN
            INSERT (MaLoaiThongBao, TenLoaiThongBao, MoTa)
            VALUES (S.MaLoaiThongBao, S.TenLoaiThongBao, S.MoTa);
        GO

        /* =========================================================
           M. VIEW / BÁO CÁO
           ========================================================= */

        CREATE OR ALTER VIEW VW_TONGNO_DOCGIA AS
        SELECT 
            dg.MaDocGia,
            dg.HoTen,
            CAST(ISNULL(SUM(
                CASE 
                    WHEN kn.TrangThai <> N'Đã thanh toán' 
                    THEN kn.SoTienConLai 
                    ELSE 0 
                END
            ), 0) AS DECIMAL(18,2)) AS TongNoConLai
        FROM DOCGIA dg
        LEFT JOIN KHOANNO kn ON dg.MaDocGia = kn.MaDocGia
        GROUP BY dg.MaDocGia, dg.HoTen;
        GO

        CREATE OR ALTER VIEW VW_SOSACH_DANGMUON AS
        SELECT 
            dg.MaDocGia,
            dg.HoTen,
            COUNT(ctpm.MaChiTietMuon) AS SoSachDangMuon
        FROM DOCGIA dg
        LEFT JOIN PHIEUMUON pm 
            ON dg.MaDocGia = pm.MaDocGia
        LEFT JOIN CHITIETPHIEUMUON ctpm 
            ON pm.MaPhieuMuon = ctpm.MaPhieuMuon
            AND ctpm.TrangThai = N'Đang mượn'
        GROUP BY dg.MaDocGia, dg.HoTen;
        GO

        CREATE OR ALTER VIEW VW_BAOCAO_MUON_THELOAI AS
        WITH LuotMuon AS (
            SELECT 
                MONTH(ctpm.NgayMuon) AS Thang,
                YEAR(ctpm.NgayMuon) AS Nam,
                tl.MaTheLoai,
                tl.TenTheLoai,
                COUNT_BIG(*) AS SoLuotMuon
            FROM CHITIETPHIEUMUON ctpm
            INNER JOIN CUONSACH cs ON ctpm.MaCuonSach = cs.MaCuonSach
            INNER JOIN DAUSACH_THELOAI dstl ON cs.MaDauSach = dstl.MaDauSach
            INNER JOIN THELOAI tl ON dstl.MaTheLoai = tl.MaTheLoai
            GROUP BY 
                MONTH(ctpm.NgayMuon),
                YEAR(ctpm.NgayMuon),
                tl.MaTheLoai,
                tl.TenTheLoai
        ),
        Tong AS (
            SELECT 
                Thang,
                Nam,
                SUM(SoLuotMuon) AS TongLuotMuon
            FROM LuotMuon
            GROUP BY Thang, Nam
        )
        SELECT 
            lm.Thang,
            lm.Nam,
            lm.MaTheLoai,
            lm.TenTheLoai,
            lm.SoLuotMuon,
            CAST(
                CASE 
                    WHEN t.TongLuotMuon = 0 THEN 0
                    ELSE lm.SoLuotMuon * 100.0 / t.TongLuotMuon
                END AS DECIMAL(5,2)
            ) AS TiLePhanTram
        FROM LuotMuon lm
        INNER JOIN Tong t 
            ON lm.Thang = t.Thang 
            AND lm.Nam = t.Nam;
        GO

        CREATE OR ALTER VIEW VW_BAOCAO_TRA_TRE AS
        SELECT 
            MONTH(pt.NgayTra) AS Thang,
            YEAR(pt.NgayTra) AS Nam,
            cs.MaCuonSach,
            ds.TenDauSach,
            dg.MaDocGia,
            dg.HoTen AS HoTenDocGia,
            ctpm.NgayMuon,
            ctpm.HanTra,
            ctpm.NgayTraThucTe,
            ctpt.SoNgayTre,
            ctpt.TienPhatTre
        FROM CHITIETPHIEUTRA ctpt
        INNER JOIN PHIEUTRA pt ON ctpt.MaPhieuTra = pt.MaPhieuTra
        INNER JOIN CHITIETPHIEUMUON ctpm ON ctpt.MaChiTietMuon = ctpm.MaChiTietMuon
        INNER JOIN PHIEUMUON pm ON ctpm.MaPhieuMuon = pm.MaPhieuMuon
        INNER JOIN DOCGIA dg ON pm.MaDocGia = dg.MaDocGia
        INNER JOIN CUONSACH cs ON ctpm.MaCuonSach = cs.MaCuonSach
        INNER JOIN DAUSACH ds ON cs.MaDauSach = ds.MaDauSach
        WHERE ctpt.SoNgayTre > 0;
        GO

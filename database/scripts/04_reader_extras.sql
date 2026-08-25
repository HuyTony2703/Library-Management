
/* =========================================================
   BỔ SUNG 1: Thông báo độc giả - cột DaDoc/ThoiGianDoc, danh mục loại thông báo, index và dữ liệu mẫu (gộp từ 04_reader_notification_extra.sql)
   ========================================================= */
USE QuanLyThuVien;
GO

IF COL_LENGTH('dbo.THONGBAO', 'DaDoc') IS NULL
BEGIN
    ALTER TABLE dbo.THONGBAO
    ADD DaDoc BIT NOT NULL
        CONSTRAINT DF_THONGBAO_DADOC DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.THONGBAO', 'ThoiGianDoc') IS NULL
BEGIN
    ALTER TABLE dbo.THONGBAO
    ADD ThoiGianDoc DATETIME2 NULL;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM LOAITHONGBAO
    WHERE MaLoaiThongBao = 'TB_MUA_GOI_TC'
)
BEGIN
    INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
    VALUES (
        'TB_MUA_GOI_TC',
        N'Mua gói thành công',
        N'Thông báo mua hoặc gia hạn gói độc giả thành công'
    );
END
GO

MERGE LOAITHONGBAO AS T
USING (VALUES
    ('TB_SAP_DEN_HAN', N'Sách sắp đến hạn trả', N'Thông báo nhắc độc giả sắp đến hạn trả sách'),
    ('TB_QUA_HAN_TRA', N'Sách đã quá hạn trả', N'Thông báo nhắc độc giả có sách đã quá hạn trả'),
    ('TB_PHAT_SINH_PHAT', N'Phát sinh tiền phạt', N'Thông báo phát sinh tiền phạt mới'),
    ('TB_SACH_DA_CO', N'Sách đặt trước đã có', N'Thông báo sách đặt trước đã sẵn sàng'),
    ('TB_MUA_GOI_TC', N'Mua hoặc gia hạn gói thành viên thành công', N'Thông báo mua hoặc gia hạn gói thành viên thành công'),
    ('TB_GOI_SAP_HET_HAN', N'Gói thành viên sắp hết hạn', N'Thông báo nhắc gói thành viên sắp hết hạn'),
    ('TB_TAIKHOAN_THE_DOI_TRANGTHAI', N'Tài khoản hoặc thẻ độc giả thay đổi trạng thái', N'Thông báo khi tài khoản hoặc thẻ độc giả thay đổi trạng thái')
) AS S(MaLoaiThongBao, TenLoaiThongBao, MoTa)
ON T.MaLoaiThongBao = S.MaLoaiThongBao
WHEN MATCHED THEN
    UPDATE SET TenLoaiThongBao = S.TenLoaiThongBao, MoTa = S.MoTa
WHEN NOT MATCHED THEN
    INSERT (MaLoaiThongBao, TenLoaiThongBao, MoTa)
    VALUES (S.MaLoaiThongBao, S.TenLoaiThongBao, S.MoTa);
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_THONGBAO_TAIKHOAN_DADOC'
      AND object_id = OBJECT_ID('dbo.THONGBAO')
)
BEGIN
    CREATE INDEX IX_THONGBAO_TAIKHOAN_DADOC
    ON dbo.THONGBAO(MaTaiKhoanNhan, DaDoc, NgayTao);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM dbo.THONGBAO
    WHERE MaThongBao = 'TB_DG001_TEST_01'
)
BEGIN
    INSERT INTO dbo.THONGBAO(
        MaThongBao,
        MaTaiKhoanNhan,
        MaLoaiThongBao,
        TieuDe,
        NoiDung,
        NgayTao,
        GuiTrongApp,
        GuiEmail,
        TrangThaiEmail,
        SoLanThuGuiEmail,
        DaDoc,
        ThoiGianDoc
    )
    VALUES (
        'TB_DG001_TEST_01',
        'TK_DG001',
        'TB_SAP_DEN_HAN',
        N'Sách sắp đến hạn trả',
        N'Bạn có sách sắp đến hạn trả, vui lòng trả hoặc gia hạn đúng hạn.',
        SYSDATETIME(),
        1,
        0,
        N'Không gửi',
        0,
        0,
        NULL
    );
END
GO

SELECT TOP 5
    MaThongBao,
    MaTaiKhoanNhan,
    MaLoaiThongBao,
    TieuDe,
    DaDoc,
    ThoiGianDoc
FROM dbo.THONGBAO
ORDER BY NgayTao DESC;
GO

GO

/* =========================================================
   BỔ SUNG 2: Cổng độc giả - bảng SACHYEUTHICH và index tối ưu truy vấn (gộp từ 04_reader_portal_extra.sql)
   ========================================================= */
USE QuanLyThuVien;
GO

IF OBJECT_ID('dbo.SACHYEUTHICH', 'U') IS NULL
BEGIN
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
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_SACHYEUTHICH_DOCGIA_NGAYTHEM'
      AND object_id = OBJECT_ID('dbo.SACHYEUTHICH')
)
BEGIN
    CREATE INDEX IX_SACHYEUTHICH_DOCGIA_NGAYTHEM
    ON SACHYEUTHICH(MaDocGia, NgayThem DESC);
END
GO

IF OBJECT_ID('dbo.SACHYEUTHICH', 'U') IS NOT NULL
AND NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_SACHYEUTHICH_DAUSACH'
      AND object_id = OBJECT_ID('dbo.SACHYEUTHICH')
)
BEGIN
    CREATE INDEX IX_SACHYEUTHICH_DAUSACH
    ON SACHYEUTHICH(MaDauSach);
END
GO

IF OBJECT_ID('dbo.CUONSACH', 'U') IS NOT NULL
AND NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_CUONSACH_DAUSACH_TRANGTHAI_NGAYNHAP'
      AND object_id = OBJECT_ID('dbo.CUONSACH')
)
BEGIN
    CREATE INDEX IX_CUONSACH_DAUSACH_TRANGTHAI_NGAYNHAP
    ON CUONSACH(MaDauSach, MaTrangThai, NgayNhapSach DESC);
END
GO

IF OBJECT_ID('dbo.CHITIETPHIEUMUON', 'U') IS NOT NULL
AND NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_CTPM_CUONSACH_NGAYMUON'
      AND object_id = OBJECT_ID('dbo.CHITIETPHIEUMUON')
)
BEGIN
    CREATE INDEX IX_CTPM_CUONSACH_NGAYMUON
    ON CHITIETPHIEUMUON(MaCuonSach, NgayMuon DESC);
END
GO

IF OBJECT_ID('dbo.DANHGIA', 'U') IS NOT NULL
AND NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_DANHGIA_DAUSACH_TRANGTHAI_SOSAO'
      AND object_id = OBJECT_ID('dbo.DANHGIA')
)
BEGIN
    CREATE INDEX IX_DANHGIA_DAUSACH_TRANGTHAI_SOSAO
    ON DANHGIA(MaDauSach, TrangThai, SoSao);
END
GO

GO

/* =========================================================
   BỔ SUNG 3: Gói thành viên - danh mục gói, phương thức thanh toán, giá gói VIP sinh viên (gộp từ 05_reader_membership_extra.sql)
   ========================================================= */
USE QuanLyThuVien;
GO

IF NOT EXISTS (
    SELECT 1
    FROM LOAITHONGBAO
    WHERE MaLoaiThongBao = 'TB_MUA_GOI_TC'
)
BEGIN
    INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
    VALUES (
        'TB_MUA_GOI_TC',
        N'Mua gói thành công',
        N'Thông báo độc giả mua hoặc gia hạn gói thành công'
    );
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM GOITHANHVIEN
    WHERE MaGoiThanhVien = 'GOI_VIP'
)
BEGIN
    INSERT INTO GOITHANHVIEN(MaGoiThanhVien, TenGoi, MoTa, TrangThai)
    VALUES (
        'GOI_VIP',
        N'VIP',
        N'Gói thành viên nâng cao',
        N'Hoạt động'
    );
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM PHUONGTHUCTHANHTOAN
    WHERE MaPhuongThuc = 'PT_TIEN_MAT'
)
BEGIN
    INSERT INTO PHUONGTHUCTHANHTOAN(MaPhuongThuc, TenPhuongThuc, MoTa)
    VALUES ('PT_TIEN_MAT', N'Tiền mặt', N'Thanh toán bằng tiền mặt');
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM PHUONGTHUCTHANHTOAN
    WHERE MaPhuongThuc = 'PT_CHUYEN_KHOAN'
)
BEGIN
    INSERT INTO PHUONGTHUCTHANHTOAN(MaPhuongThuc, TenPhuongThuc, MoTa)
    VALUES ('PT_CHUYEN_KHOAN', N'Chuyển khoản', N'Thanh toán bằng chuyển khoản');
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM PHUONGTHUCTHANHTOAN
    WHERE MaPhuongThuc = 'PT_VI_DIEN_TU'
)
BEGIN
    INSERT INTO PHUONGTHUCTHANHTOAN(MaPhuongThuc, TenPhuongThuc, MoTa)
    VALUES ('PT_VI_DIEN_TU', N'Ví điện tử', N'Thanh toán bằng ví điện tử');
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM GIAGOI_THEONHOM
    WHERE MaGiaGoi = 'GG_SV_VIP'
)
BEGIN
    INSERT INTO GIAGOI_THEONHOM(
        MaGiaGoi,
        MaPhienBan,
        MaGoiThanhVien,
        MaNhomDocGia,
        GiaTien,
        ThoiHanGoiTheoNgay
    )
    VALUES (
        'GG_SV_VIP',
        'QD_V1',
        'GOI_VIP',
        'NHOM_SINHVIEN',
        50000,
        180
    );
END
GO

GO

/* =========================================================
   RESET 1: Xóa và tạo lại dữ liệu đánh giá/bình luận demo của DG001 (gộp từ 06_reader_comment_rating_reset.sql)
   ========================================================= */
USE QuanLyThuVien;
GO

DELETE FROM BINHLUAN
WHERE MaDocGia = 'DG001'
  AND MaDauSach = 'F01'
  AND MaBinhLuan LIKE 'BL20%';

DELETE FROM DANHGIA
WHERE MaDocGia = 'DG001'
  AND MaDauSach = 'CLEAN01'
  AND MaDanhGia LIKE 'DGIA20%';
GO

IF NOT EXISTS (
    SELECT 1
    FROM DANHGIA
    WHERE MaDanhGia = 'DGIA_DG001_F01'
)
BEGIN
    INSERT INTO DANHGIA(
        MaDanhGia,
        MaDocGia,
        MaDauSach,
        SoSao,
        NoiDung,
        NgayDanhGia,
        TrangThai
    )
    VALUES (
        'DGIA_DG001_F01',
        'DG001',
        'F01',
        5,
        N'Sách hay, nội dung nhẹ nhàng và cảm động.',
        SYSDATETIME(),
        N'Hiển thị'
    );
END
GO

GO

/* =========================================================
   RESET 2: Xóa dữ liệu sách yêu thích demo của DG001 (gộp từ 07_reader_favorites_reset.sql)
   ========================================================= */
USE QuanLyThuVien;
GO

DELETE FROM SACHYEUTHICH
WHERE MaDocGia = 'DG001'
  AND MaDauSach = 'F01';
GO

GO

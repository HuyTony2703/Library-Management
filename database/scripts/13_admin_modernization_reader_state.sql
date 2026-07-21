USE QuanLyThuVien;

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.DOCGIA_KHOA', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.DOCGIA_KHOA (
            MaKhoa BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            PhamVi VARCHAR(20) NOT NULL,
            LyDoKhoa NVARCHAR(500) NOT NULL,
            KhoaLuc DATETIME2 NOT NULL CONSTRAINT DF_DOCGIA_KHOA_LUC DEFAULT SYSDATETIME(),
            KhoaDen DATE NULL,
            GhiChu NVARCHAR(1000) NULL,
            MaTaiKhoanKhoa VARCHAR(30) NULL,
            DuLieuLegacy BIT NOT NULL CONSTRAINT DF_DOCGIA_KHOA_LEGACY DEFAULT 0,
            MoKhoaLuc DATETIME2 NULL,
            LyDoMoKhoa NVARCHAR(500) NULL,
            MaTaiKhoanMoKhoa VARCHAR(30) NULL,
            CONSTRAINT CK_DOCGIA_KHOA_PHAMVI CHECK (PhamVi IN ('BORROWING', 'LOGIN')),
            CONSTRAINT CK_DOCGIA_KHOA_NGAY CHECK (KhoaDen IS NULL OR KhoaDen >= CAST(KhoaLuc AS DATE)),
            CONSTRAINT CK_DOCGIA_KHOA_MO CHECK (
                (MoKhoaLuc IS NULL AND LyDoMoKhoa IS NULL AND MaTaiKhoanMoKhoa IS NULL)
                OR (MoKhoaLuc IS NOT NULL AND LyDoMoKhoa IS NOT NULL AND MaTaiKhoanMoKhoa IS NOT NULL)
            ),
            CONSTRAINT CK_DOCGIA_KHOA_ACTOR CHECK (DuLieuLegacy = 1 OR MaTaiKhoanKhoa IS NOT NULL),
            CONSTRAINT FK_DOCGIA_KHOA_DOCGIA FOREIGN KEY (MaDocGia) REFERENCES dbo.DOCGIA(MaDocGia),
            CONSTRAINT FK_DOCGIA_KHOA_ACTOR FOREIGN KEY (MaTaiKhoanKhoa) REFERENCES dbo.TAIKHOAN(MaTaiKhoan),
            CONSTRAINT FK_DOCGIA_MOKHOA_ACTOR FOREIGN KEY (MaTaiKhoanMoKhoa) REFERENCES dbo.TAIKHOAN(MaTaiKhoan)
        );
    END;

    IF OBJECT_ID(N'dbo.DOCGIA_VONGDOI_EVENT', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.DOCGIA_VONGDOI_EVENT (
            MaSuKien BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            HanhDong VARCHAR(30) NOT NULL,
            TrangThaiTruoc NVARCHAR(30) NOT NULL,
            TrangThaiSau NVARCHAR(30) NOT NULL,
            LyDo NVARCHAR(500) NOT NULL,
            MaTaiKhoan VARCHAR(30) NOT NULL,
            ThoiGian DATETIME2 NOT NULL CONSTRAINT DF_DOCGIA_VONGDOI_TIME DEFAULT SYSDATETIME(),
            CONSTRAINT CK_DOCGIA_VONGDOI_ACTION CHECK (HanhDong IN ('DEACTIVATE', 'REACTIVATE')),
            CONSTRAINT FK_DOCGIA_VONGDOI_DOCGIA FOREIGN KEY (MaDocGia) REFERENCES dbo.DOCGIA(MaDocGia),
            CONSTRAINT FK_DOCGIA_VONGDOI_ACTOR FOREIGN KEY (MaTaiKhoan) REFERENCES dbo.TAIKHOAN(MaTaiKhoan)
        );
    END;

    /* Giữ ý nghĩa khóa legacy; không bịa lý do hay actor ngoài dữ liệu hiện hữu. */
    INSERT INTO dbo.DOCGIA_KHOA (MaDocGia, PhamVi, LyDoKhoa, KhoaLuc, MaTaiKhoanKhoa, DuLieuLegacy)
    SELECT dg.MaDocGia, 'BORROWING', N'Khóa legacy - chưa có lý do lịch sử', SYSDATETIME(), NULL, 1
    FROM dbo.DOCGIA dg
    WHERE dg.TrangThai = N'Khóa'
      AND NOT EXISTS (SELECT 1 FROM dbo.DOCGIA_KHOA dk WHERE dk.MaDocGia = dg.MaDocGia AND dk.PhamVi = 'BORROWING');

    INSERT INTO dbo.DOCGIA_KHOA (MaDocGia, PhamVi, LyDoKhoa, KhoaLuc, MaTaiKhoanKhoa, DuLieuLegacy)
    SELECT dg.MaDocGia, 'LOGIN', N'Khóa đăng nhập legacy - chưa có lý do lịch sử', SYSDATETIME(), NULL, 1
    FROM dbo.DOCGIA dg
    INNER JOIN dbo.TAIKHOAN tk ON tk.MaTaiKhoan = dg.MaTaiKhoan
    WHERE tk.TrangThai = N'Khóa'
      AND NOT EXISTS (SELECT 1 FROM dbo.DOCGIA_KHOA dk WHERE dk.MaDocGia = dg.MaDocGia AND dk.PhamVi = 'LOGIN');

    UPDATE tk SET TrangThai = N'Hoạt động'
    FROM dbo.TAIKHOAN tk
    INNER JOIN dbo.DOCGIA dg ON dg.MaTaiKhoan = tk.MaTaiKhoan
    WHERE tk.TrangThai = N'Khóa';

    UPDATE dbo.DOCGIA
    SET TrangThai = N'Hoạt động'
    WHERE TrangThai IN (N'Khóa', N'Hết hạn');

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_DOCGIA_KHOA_OPEN_SCOPE'
                   AND object_id = OBJECT_ID(N'dbo.DOCGIA_KHOA'))
        CREATE UNIQUE INDEX UX_DOCGIA_KHOA_OPEN_SCOPE
            ON dbo.DOCGIA_KHOA(MaDocGia, PhamVi) WHERE MoKhoaLuc IS NULL;

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_DOCGIA_VONGDOI_READER_TIME'
                   AND object_id = OBJECT_ID(N'dbo.DOCGIA_VONGDOI_EVENT'))
        CREATE INDEX IX_DOCGIA_VONGDOI_READER_TIME
            ON dbo.DOCGIA_VONGDOI_EVENT(MaDocGia, ThoiGian DESC, MaSuKien DESC);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;


/* =========================================================
   MIGRATION 08: Phân công nhân viên - chi nhánh (gộp từ 08_admin_modernization_staff_context.sql)
   ========================================================= */
/* ================================================================
   Migration: 08_admin_modernization_staff_context
   Purpose  : Add versioned staff-to-branch assignments and backfill
              the current NHANVIEN.MaChiNhanh as the default branch.
   Safety   : Idempotent for repeated deployment; no destructive DDL.
   ================================================================ */

USE QuanLyThuVien;
GO

SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID('dbo.NHANVIEN_CHINHANH', 'U') IS NULL
    BEGIN
        CREATE TABLE dbo.NHANVIEN_CHINHANH
        (
            MaNhanVien VARCHAR(30) NOT NULL,
            MaChiNhanh VARCHAR(30) NOT NULL,
            LaMacDinh BIT NOT NULL CONSTRAINT DF_NVCN_LAMACDINH DEFAULT 0,
            NgayBatDau DATE NOT NULL CONSTRAINT DF_NVCN_NGAYBATDAU DEFAULT CAST(SYSDATETIME() AS DATE),
            NgayKetThuc DATE NULL,
            TrangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_NVCN_TRANGTHAI DEFAULT N'Hoạt động',

            CONSTRAINT PK_NHANVIEN_CHINHANH
                PRIMARY KEY (MaNhanVien, MaChiNhanh),

            CONSTRAINT FK_NVCN_NHANVIEN
                FOREIGN KEY (MaNhanVien) REFERENCES dbo.NHANVIEN(MaNhanVien),

            CONSTRAINT FK_NVCN_CHINHANH
                FOREIGN KEY (MaChiNhanh) REFERENCES dbo.CHINHANH(MaChiNhanh),

            CONSTRAINT CK_NVCN_NGAY
                CHECK (NgayKetThuc IS NULL OR NgayKetThuc >= NgayBatDau),

            CONSTRAINT CK_NVCN_TRANGTHAI
                CHECK (TrangThai IN (N'Hoạt động', N'Ngừng hoạt động'))
        );
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'UX_NVCN_MACDINH_HOATDONG'
          AND object_id = OBJECT_ID('dbo.NHANVIEN_CHINHANH')
    )
    BEGIN
        CREATE UNIQUE INDEX UX_NVCN_MACDINH_HOATDONG
            ON dbo.NHANVIEN_CHINHANH(MaNhanVien)
            WHERE LaMacDinh = 1 AND TrangThai = N'Hoạt động';
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IX_NVCN_CHINHANH_TRANGTHAI'
          AND object_id = OBJECT_ID('dbo.NHANVIEN_CHINHANH')
    )
    BEGIN
        CREATE INDEX IX_NVCN_CHINHANH_TRANGTHAI
            ON dbo.NHANVIEN_CHINHANH(MaChiNhanh, TrangThai, NgayBatDau, NgayKetThuc)
            INCLUDE (MaNhanVien, LaMacDinh);
    END;

    INSERT INTO dbo.NHANVIEN_CHINHANH
    (
        MaNhanVien,
        MaChiNhanh,
        LaMacDinh,
        NgayBatDau,
        NgayKetThuc,
        TrangThai
    )
    SELECT
        nv.MaNhanVien,
        nv.MaChiNhanh,
        CASE
            WHEN EXISTS
            (
                SELECT 1
                FROM dbo.NHANVIEN_CHINHANH existingDefault
                WHERE existingDefault.MaNhanVien = nv.MaNhanVien
                  AND existingDefault.LaMacDinh = 1
                  AND existingDefault.TrangThai = N'Hoạt động'
            ) THEN 0
            ELSE 1
        END,
        COALESCE(nv.NgayVaoLam, CAST(SYSDATETIME() AS DATE)),
        NULL,
        N'Hoạt động'
    FROM dbo.NHANVIEN nv
    WHERE nv.MaChiNhanh IS NOT NULL
      AND NOT EXISTS
      (
          SELECT 1
          FROM dbo.NHANVIEN_CHINHANH existing
          WHERE existing.MaNhanVien = nv.MaNhanVien
            AND existing.MaChiNhanh = nv.MaChiNhanh
      );

    UPDATE currentAssignment
    SET
        currentAssignment.LaMacDinh = 1,
        currentAssignment.NgayKetThuc = NULL,
        currentAssignment.TrangThai = N'Hoạt động'
    FROM dbo.NHANVIEN_CHINHANH currentAssignment
    INNER JOIN dbo.NHANVIEN nv
        ON nv.MaNhanVien = currentAssignment.MaNhanVien
       AND nv.MaChiNhanh = currentAssignment.MaChiNhanh
    WHERE nv.MaChiNhanh IS NOT NULL
      AND NOT EXISTS
      (
          SELECT 1
          FROM dbo.NHANVIEN_CHINHANH existingDefault
          WHERE existingDefault.MaNhanVien = currentAssignment.MaNhanVien
            AND existingDefault.LaMacDinh = 1
            AND existingDefault.TrangThai = N'Hoạt động'
      );

    ;WITH DefaultOperationalBranch AS
    (
        SELECT TOP (1) cn.MaChiNhanh
        FROM dbo.CHINHANH cn
        WHERE cn.TrangThai = N'Hoạt động'
        ORDER BY cn.MaChiNhanh
    ),
    AdminStaffWithoutBranch AS
    (
        SELECT nv.MaNhanVien, branch.MaChiNhanh
        FROM dbo.NHANVIEN nv
        INNER JOIN dbo.TAIKHOAN tk
            ON tk.MaTaiKhoan = nv.MaTaiKhoan
        INNER JOIN dbo.VAITRO vt
            ON vt.MaVaiTro = tk.MaVaiTro
        CROSS JOIN DefaultOperationalBranch branch
        WHERE vt.TenVaiTro = 'QUAN_TRI_VIEN'
          AND NOT EXISTS
          (
              SELECT 1
              FROM dbo.NHANVIEN_CHINHANH existing
              WHERE existing.MaNhanVien = nv.MaNhanVien
                AND existing.TrangThai = N'Hoạt động'
                AND existing.NgayKetThuc IS NULL
          )
    )
    INSERT INTO dbo.NHANVIEN_CHINHANH
    (
        MaNhanVien,
        MaChiNhanh,
        LaMacDinh,
        NgayBatDau,
        NgayKetThuc,
        TrangThai
    )
    SELECT
        adminStaff.MaNhanVien,
        adminStaff.MaChiNhanh,
        1,
        CAST(SYSDATETIME() AS DATE),
        NULL,
        N'Hoạt động'
    FROM AdminStaffWithoutBranch adminStaff
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.NHANVIEN_CHINHANH existing
        WHERE existing.MaNhanVien = adminStaff.MaNhanVien
          AND existing.MaChiNhanh = adminStaff.MaChiNhanh
    );

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

GO

/* =========================================================
   MIGRATION 09: Toàn vẹn thanh toán và idempotency phiếu thu (gộp từ 09_admin_modernization_payment_integrity.sql)
   ========================================================= */
/* ================================================================
   Migration: 09_admin_modernization_payment_integrity
   Purpose  : Add payment idempotency metadata and indexes supporting
              stable debt locking and unique receipt allocations.
   Safety   : Idempotent, non-destructive, and aborts before adding the
              unique allocation index if existing duplicates are found.
   ================================================================ */

USE QuanLyThuVien;
GO

SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.PHIEUTHU', 'IdempotencyKey') IS NULL
    BEGIN
        ALTER TABLE dbo.PHIEUTHU
            ADD IdempotencyKey VARCHAR(100) NULL;
    END;

    IF COL_LENGTH('dbo.PHIEUTHU', 'RequestFingerprint') IS NULL
    BEGIN
        ALTER TABLE dbo.PHIEUTHU
            ADD RequestFingerprint VARCHAR(64) NULL;
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'UX_PHIEUTHU_IDEMPOTENCY_KEY'
          AND object_id = OBJECT_ID('dbo.PHIEUTHU')
    )
    BEGIN
        EXEC(N'CREATE UNIQUE INDEX UX_PHIEUTHU_IDEMPOTENCY_KEY
            ON dbo.PHIEUTHU(IdempotencyKey)
            WHERE IdempotencyKey IS NOT NULL');
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IX_KHOANNO_DOCGIA_TRANGTHAI_NGAY_MA'
          AND object_id = OBJECT_ID('dbo.KHOANNO')
    )
    BEGIN
        CREATE INDEX IX_KHOANNO_DOCGIA_TRANGTHAI_NGAY_MA
            ON dbo.KHOANNO(MaDocGia, TrangThai, NgayPhatSinh, MaKhoanNo)
            INCLUDE (SoTienPhatSinh, SoTienDaThanhToan);
    END;

    IF EXISTS
    (
        SELECT 1
        FROM dbo.CHITIETPHIEUTHU_NO
        GROUP BY MaPhieuThu, MaKhoanNo
        HAVING COUNT(*) > 1
    )
    BEGIN
        THROW 51009,
              'Không thể tạo unique index: CHITIETPHIEUTHU_NO có khoản nợ lặp trong cùng phiếu thu.',
              1;
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'UX_CTPTN_PHIEUTHU_KHOANNO'
          AND object_id = OBJECT_ID('dbo.CHITIETPHIEUTHU_NO')
    )
    BEGIN
        CREATE UNIQUE INDEX UX_CTPTN_PHIEUTHU_KHOANNO
            ON dbo.CHITIETPHIEUTHU_NO(MaPhieuThu, MaKhoanNo);
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

GO

/* =========================================================
   MIGRATION 10: Index danh sách cuốn sách phân trang (gộp từ 10_admin_modernization_copy_list_indexes.sql)
   ========================================================= */
USE QuanLyThuVien;
GO

SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE name = N'IX_CUONSACH_CHINHANH_TRANGTHAI_NGAYNHAP'
          AND object_id = OBJECT_ID(N'dbo.CUONSACH')
    )
    BEGIN
        CREATE INDEX IX_CUONSACH_CHINHANH_TRANGTHAI_NGAYNHAP
            ON dbo.CUONSACH (MaChiNhanh, MaTrangThai, NgayNhapSach DESC)
            INCLUDE (MaDauSach, MaViTri, MaVach, MaQRCode);
    END;

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE name = N'IX_CUONSACH_VITRI_NGAYNHAP'
          AND object_id = OBJECT_ID(N'dbo.CUONSACH')
    )
    BEGIN
        CREATE INDEX IX_CUONSACH_VITRI_NGAYNHAP
            ON dbo.CUONSACH (MaViTri, NgayNhapSach DESC)
            INCLUDE (MaChiNhanh, MaDauSach, MaTrangThai, MaVach, MaQRCode);
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

GO

/* =========================================================
   MIGRATION 11: Lịch sử sự kiện trạng thái và vị trí cuốn sách (gộp từ 11_admin_modernization_copy_actions.sql)
   ========================================================= */
USE QuanLyThuVien;

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.CUONSACH_TRANGTHAI_EVENT', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.CUONSACH_TRANGTHAI_EVENT (
            MaSuKien BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            MaCuonSach VARCHAR(40) NOT NULL,
            HanhDong VARCHAR(40) NOT NULL,
            TrangThaiTruoc VARCHAR(30) NOT NULL,
            TrangThaiSau VARCHAR(30) NOT NULL,
            MucDo VARCHAR(20) NULL,
            LoaiHong NVARCHAR(600) NULL,
            MoTa NVARCHAR(1000) NULL,
            LyDo NVARCHAR(500) NOT NULL,
            MaTaiKhoan VARCHAR(30) NOT NULL,
            ThoiGian DATETIME2 NOT NULL CONSTRAINT DF_CUONSACH_TT_EVENT_THOIGIAN DEFAULT SYSDATETIME(),
            CONSTRAINT FK_CUONSACH_TT_EVENT_CUONSACH FOREIGN KEY (MaCuonSach) REFERENCES dbo.CUONSACH(MaCuonSach),
            CONSTRAINT FK_CUONSACH_TT_EVENT_TRUOC FOREIGN KEY (TrangThaiTruoc) REFERENCES dbo.TRANGTHAICUONSACH(MaTrangThai),
            CONSTRAINT FK_CUONSACH_TT_EVENT_SAU FOREIGN KEY (TrangThaiSau) REFERENCES dbo.TRANGTHAICUONSACH(MaTrangThai),
            CONSTRAINT FK_CUONSACH_TT_EVENT_TAIKHOAN FOREIGN KEY (MaTaiKhoan) REFERENCES dbo.TAIKHOAN(MaTaiKhoan)
        );
    END;

    IF OBJECT_ID(N'dbo.CUONSACH_VITRI_EVENT', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.CUONSACH_VITRI_EVENT (
            MaSuKien BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            MaCuonSach VARCHAR(40) NOT NULL,
            MaViTriTruoc VARCHAR(30) NOT NULL,
            MaViTriSau VARCHAR(30) NOT NULL,
            LyDo NVARCHAR(500) NOT NULL,
            MaTaiKhoan VARCHAR(30) NOT NULL,
            ThoiGian DATETIME2 NOT NULL CONSTRAINT DF_CUONSACH_VT_EVENT_THOIGIAN DEFAULT SYSDATETIME(),
            CONSTRAINT FK_CUONSACH_VT_EVENT_CUONSACH FOREIGN KEY (MaCuonSach) REFERENCES dbo.CUONSACH(MaCuonSach),
            CONSTRAINT FK_CUONSACH_VT_EVENT_TRUOC FOREIGN KEY (MaViTriTruoc) REFERENCES dbo.VITRISACH(MaViTri),
            CONSTRAINT FK_CUONSACH_VT_EVENT_SAU FOREIGN KEY (MaViTriSau) REFERENCES dbo.VITRISACH(MaViTri),
            CONSTRAINT FK_CUONSACH_VT_EVENT_TAIKHOAN FOREIGN KEY (MaTaiKhoan) REFERENCES dbo.TAIKHOAN(MaTaiKhoan)
        );
    END;

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE name = N'IX_CUONSACH_TT_EVENT_COPY_TIME'
          AND object_id = OBJECT_ID(N'dbo.CUONSACH_TRANGTHAI_EVENT')
    )
        CREATE INDEX IX_CUONSACH_TT_EVENT_COPY_TIME
            ON dbo.CUONSACH_TRANGTHAI_EVENT(MaCuonSach, ThoiGian DESC, MaSuKien DESC);

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE name = N'IX_CUONSACH_VT_EVENT_COPY_TIME'
          AND object_id = OBJECT_ID(N'dbo.CUONSACH_VITRI_EVENT')
    )
        CREATE INDEX IX_CUONSACH_VT_EVENT_COPY_TIME
            ON dbo.CUONSACH_VITRI_EVENT(MaCuonSach, ThoiGian DESC, MaSuKien DESC);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

GO

/* =========================================================
   MIGRATION 12: Index danh sách độc giả và lịch sử gói (gộp từ 12_admin_modernization_reader_list_indexes.sql)
   ========================================================= */
USE QuanLyThuVien;

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE name = N'IX_DOCGIA_TRANGTHAI_HANTHE_HOTEN'
          AND object_id = OBJECT_ID(N'dbo.DOCGIA')
    )
        CREATE INDEX IX_DOCGIA_TRANGTHAI_HANTHE_HOTEN
            ON dbo.DOCGIA(TrangThai, NgayHetHanThe, HoTen, MaDocGia)
            INCLUDE (MaTaiKhoan, MaNhomDocGia, NgayLapThe);

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE name = N'IX_DOCGIA_NHOM_HOTEN'
          AND object_id = OBJECT_ID(N'dbo.DOCGIA')
    )
        CREATE INDEX IX_DOCGIA_NHOM_HOTEN
            ON dbo.DOCGIA(MaNhomDocGia, HoTen, MaDocGia)
            INCLUDE (TrangThai, NgayLapThe, NgayHetHanThe);

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE name = N'IX_LSGTV_DOCGIA_NGAYKETTHUC'
          AND object_id = OBJECT_ID(N'dbo.LICHSUGOITHANHVIEN')
    )
        CREATE INDEX IX_LSGTV_DOCGIA_NGAYKETTHUC
            ON dbo.LICHSUGOITHANHVIEN(MaDocGia, NgayKetThuc DESC, NgayBatDau DESC)
            INCLUDE (MaGoiThanhVien, TrangThai, MaPhieuThu);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

GO

/* =========================================================
   MIGRATION 13: Tách khóa mượn/đăng nhập độc giả và vòng đời (gộp từ 13_admin_modernization_reader_state.sql)
   ========================================================= */
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

GO

/* =========================================================
   MIGRATION 14: Reset mật khẩu độc giả và bảo mật tài khoản (gộp từ 14_admin_modernization_reader_password_reset.sql)
   ========================================================= */
USE QuanLyThuVien;

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.TAIKHOAN', 'MustChangePassword') IS NULL
        ALTER TABLE dbo.TAIKHOAN ADD MustChangePassword BIT NOT NULL
            CONSTRAINT DF_TAIKHOAN_MUST_CHANGE_PASSWORD DEFAULT 0 WITH VALUES;

    IF COL_LENGTH('dbo.TAIKHOAN', 'PasswordChangedAt') IS NULL
        ALTER TABLE dbo.TAIKHOAN ADD PasswordChangedAt DATETIME2 NULL;

    IF COL_LENGTH('dbo.TAIKHOAN', 'TokenVersion') IS NULL
        ALTER TABLE dbo.TAIKHOAN ADD TokenVersion BIGINT NOT NULL
            CONSTRAINT DF_TAIKHOAN_TOKEN_VERSION DEFAULT 0 WITH VALUES;

    EXEC(N'UPDATE dbo.TAIKHOAN
        SET PasswordChangedAt = COALESCE(PasswordChangedAt, NgayTao)
        WHERE PasswordChangedAt IS NULL');

    IF OBJECT_ID(N'dbo.DOCGIA_PASSWORD_RESET_EVENT', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.DOCGIA_PASSWORD_RESET_EVENT (
            MaSuKien BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            MaDocGia VARCHAR(30) NOT NULL,
            MaTaiKhoan VARCHAR(30) NOT NULL,
            MaTaiKhoanThucHien VARCHAR(30) NOT NULL,
            PhuongThucXacMinh VARCHAR(40) NOT NULL,
            LyDo NVARCHAR(500) NOT NULL,
            KetQua VARCHAR(20) NOT NULL,
            ThoiGian DATETIME2 NOT NULL CONSTRAINT DF_DOCGIA_PASSWORD_RESET_TIME DEFAULT SYSDATETIME(),
            CONSTRAINT CK_DOCGIA_PASSWORD_RESET_RESULT CHECK (KetQua IN ('DENIED', 'SUCCESS')),
            CONSTRAINT FK_DOCGIA_PASSWORD_RESET_READER FOREIGN KEY (MaDocGia) REFERENCES dbo.DOCGIA(MaDocGia),
            CONSTRAINT FK_DOCGIA_PASSWORD_RESET_ACCOUNT FOREIGN KEY (MaTaiKhoan) REFERENCES dbo.TAIKHOAN(MaTaiKhoan),
            CONSTRAINT FK_DOCGIA_PASSWORD_RESET_ACTOR FOREIGN KEY (MaTaiKhoanThucHien) REFERENCES dbo.TAIKHOAN(MaTaiKhoan)
        );
    END;

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_READER_PASSWORD_RESET_READER_TIME'
                   AND object_id = OBJECT_ID(N'dbo.DOCGIA_PASSWORD_RESET_EVENT'))
        CREATE INDEX IX_READER_PASSWORD_RESET_READER_TIME
            ON dbo.DOCGIA_PASSWORD_RESET_EVENT(MaDocGia, ThoiGian DESC);

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_READER_PASSWORD_RESET_ACTOR_TIME'
                   AND object_id = OBJECT_ID(N'dbo.DOCGIA_PASSWORD_RESET_EVENT'))
        CREATE INDEX IX_READER_PASSWORD_RESET_ACTOR_TIME
            ON dbo.DOCGIA_PASSWORD_RESET_EVENT(MaTaiKhoanThucHien, ThoiGian DESC);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

GO

/* =========================================================
   MIGRATION 15: Toàn vẹn phiếu mượn và idempotency (gộp từ 15_admin_modernization_loan_integrity.sql)
   ========================================================= */
/* ================================================================
   Migration: 15_admin_modernization_loan_integrity
   Purpose  : Add loan idempotency metadata and indexes supporting
              create-loan retry safety.
   Safety   : Idempotent and non-destructive.
   ================================================================ */

USE QuanLyThuVien;
GO

SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.PHIEUMUON', 'IdempotencyKey') IS NULL
    BEGIN
        ALTER TABLE dbo.PHIEUMUON
            ADD IdempotencyKey VARCHAR(100) NULL;
    END;

    IF COL_LENGTH('dbo.PHIEUMUON', 'RequestFingerprint') IS NULL
    BEGIN
        ALTER TABLE dbo.PHIEUMUON
            ADD RequestFingerprint VARCHAR(64) NULL;
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'UX_PHIEUMUON_IDEMPOTENCY_KEY'
          AND object_id = OBJECT_ID('dbo.PHIEUMUON')
    )
    BEGIN
        EXEC(N'CREATE UNIQUE INDEX UX_PHIEUMUON_IDEMPOTENCY_KEY
            ON dbo.PHIEUMUON(IdempotencyKey)
            WHERE IdempotencyKey IS NOT NULL');
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IX_CTPM_CURRENT_READER_DUE'
          AND object_id = OBJECT_ID('dbo.CHITIETPHIEUMUON')
    )
    BEGIN
        CREATE INDEX IX_CTPM_CURRENT_READER_DUE
            ON dbo.CHITIETPHIEUMUON(TrangThai, HanTra, MaPhieuMuon)
            INCLUDE (MaCuonSach, MaQuyDinhMuon, NgayMuon);
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

GO

/* =========================================================
   MIGRATION 16: Quy định phạt hỏng/mất sách (gộp từ 16_admin_modernization_return_assessment.sql)
   ========================================================= */
USE QuanLyThuVien;

-- Admin modernization: return assessment, damage/lost fine rules, and fine adjustment audit.
-- Safe to rerun; keeps existing return data intact.

IF OBJECT_ID('dbo.QUYDINH_PHAT_HONGMAT', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.QUYDINH_PHAT_HONGMAT (
        MaQuyDinhPhat VARCHAR(30) NOT NULL PRIMARY KEY,
        TinhTrang NVARCHAR(50) NOT NULL,
        MucDo VARCHAR(20) NOT NULL,
        TyLePhat DECIMAL(7,4) NOT NULL,
        SoTienToiThieu DECIMAL(18,2) NOT NULL CONSTRAINT DF_QUYDINH_PHAT_HONGMAT_Min DEFAULT 0,
        SoTienToiDa DECIMAL(18,2) NULL,
        TrangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_QUYDINH_PHAT_HONGMAT_Status DEFAULT N'Đang áp dụng',
        GhiChu NVARCHAR(255) NULL,
        CONSTRAINT CK_QUYDINH_PHAT_HONGMAT_TyLe CHECK (TyLePhat >= 0),
        CONSTRAINT CK_QUYDINH_PHAT_HONGMAT_Min CHECK (SoTienToiThieu >= 0),
        CONSTRAINT CK_QUYDINH_PHAT_HONGMAT_Max CHECK (SoTienToiDa IS NULL OR SoTienToiDa >= SoTienToiThieu)
    );

    CREATE UNIQUE INDEX UX_QUYDINH_PHAT_HONGMAT_Active
        ON dbo.QUYDINH_PHAT_HONGMAT(TinhTrang, MucDo)
        WHERE TrangThai = N'Đang áp dụng';
END;

IF NOT EXISTS (SELECT 1 FROM dbo.QUYDINH_PHAT_HONGMAT WHERE MaQuyDinhPhat = 'RETURN_DMG_LOW_V1')
BEGIN
    INSERT INTO dbo.QUYDINH_PHAT_HONGMAT
        (MaQuyDinhPhat, TinhTrang, MucDo, TyLePhat, SoTienToiThieu, SoTienToiDa, GhiChu)
    VALUES
        ('RETURN_DMG_LOW_V1', N'Hỏng', 'LOW', 0.2500, 0, NULL, N'Hư hỏng nhẹ, tính theo trị giá đầu sách'),
        ('RETURN_DMG_MEDIUM_V1', N'Hỏng', 'MEDIUM', 0.5000, 0, NULL, N'Hư hỏng vừa, tính theo trị giá đầu sách'),
        ('RETURN_DMG_HIGH_V1', N'Hỏng', 'HIGH', 0.7500, 0, NULL, N'Hư hỏng nặng, tính theo trị giá đầu sách'),
        ('RETURN_LOST_FULL_V1', N'Mất', 'FULL', 1.0000, 0, NULL, N'Mất sách, tính toàn bộ trị giá đầu sách');
END;

IF OBJECT_ID('dbo.PHIEUTRA_DIEUCHINH_PHAT', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.PHIEUTRA_DIEUCHINH_PHAT (
        MaDieuChinh VARCHAR(40) NOT NULL PRIMARY KEY,
        MaPhieuTra VARCHAR(30) NOT NULL,
        MaChiTietTra VARCHAR(30) NOT NULL,
        MaChiTietMuon VARCHAR(30) NOT NULL,
        TienPhatDeXuat DECIMAL(18,2) NOT NULL,
        TienPhatCuoiCung DECIMAL(18,2) NOT NULL,
        LyDoDieuChinh NVARCHAR(500) NOT NULL,
        MaNhanVienDieuChinh VARCHAR(30) NOT NULL,
        ThoiGianDieuChinh DATETIME2 NOT NULL,
        CONSTRAINT FK_PHIEUTRA_DIEUCHINH_PHAT_PhieuTra
            FOREIGN KEY (MaPhieuTra) REFERENCES dbo.PHIEUTRA(MaPhieuTra),
        CONSTRAINT FK_PHIEUTRA_DIEUCHINH_PHAT_ChiTietTra
            FOREIGN KEY (MaChiTietTra) REFERENCES dbo.CHITIETPHIEUTRA(MaChiTietTra),
        CONSTRAINT FK_PHIEUTRA_DIEUCHINH_PHAT_ChiTietMuon
            FOREIGN KEY (MaChiTietMuon) REFERENCES dbo.CHITIETPHIEUMUON(MaChiTietMuon),
        CONSTRAINT FK_PHIEUTRA_DIEUCHINH_PHAT_NhanVien
            FOREIGN KEY (MaNhanVienDieuChinh) REFERENCES dbo.NHANVIEN(MaNhanVien),
        CONSTRAINT CK_PHIEUTRA_DIEUCHINH_PHAT_Amounts
            CHECK (TienPhatDeXuat >= 0 AND TienPhatCuoiCung >= 0)
    );

    CREATE INDEX IX_PHIEUTRA_DIEUCHINH_PHAT_PhieuTra
        ON dbo.PHIEUTRA_DIEUCHINH_PHAT(MaPhieuTra);
END;

GO

/* =========================================================
   MIGRATION 17: Toàn vẹn phiếu trả và idempotency (gộp từ 17_admin_modernization_return_integrity.sql)
   ========================================================= */
USE QuanLyThuVien;

-- Admin modernization: return creation integrity and idempotency.
-- Safe to rerun; nullable columns preserve existing return data.

IF COL_LENGTH('dbo.PHIEUTRA', 'IdempotencyKey') IS NULL
BEGIN
    ALTER TABLE dbo.PHIEUTRA
        ADD IdempotencyKey VARCHAR(100) NULL;
END;

IF COL_LENGTH('dbo.PHIEUTRA', 'RequestFingerprint') IS NULL
BEGIN
    ALTER TABLE dbo.PHIEUTRA
        ADD RequestFingerprint VARCHAR(64) NULL;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_PHIEUTRA_IdempotencyKey'
      AND object_id = OBJECT_ID('dbo.PHIEUTRA')
)
BEGIN
    EXEC(N'CREATE UNIQUE INDEX UX_PHIEUTRA_IdempotencyKey
        ON dbo.PHIEUTRA(IdempotencyKey)
        WHERE IdempotencyKey IS NOT NULL');
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_CHITIETPHIEUMUON_ReturnOpen'
      AND object_id = OBJECT_ID('dbo.CHITIETPHIEUMUON')
)
BEGIN
    CREATE INDEX IX_CHITIETPHIEUMUON_ReturnOpen
        ON dbo.CHITIETPHIEUMUON(MaChiTietMuon, TrangThai, MaPhieuMuon)
        INCLUDE (MaCuonSach, HanTra, NgayTraThucTe);
END;

GO

/* =========================================================
   MIGRATION 18: Hoàn tất metadata phiếu thu (tiền khách đưa, tiền thừa) (gộp từ 18_admin_modernization_payment_receipt_completion.sql)
   ========================================================= */
/* ================================================================
   Migration: 18_admin_modernization_payment_receipt_completion
   Purpose  : Complete staff payment receipt transaction metadata:
              cash tender/change and unique external transaction ID.
   Safety   : Idempotent, non-destructive, nullable columns for old data,
              and aborts before unique external index if duplicates exist.
   ================================================================ */

USE QuanLyThuVien;
GO

SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.PHIEUTHU', 'TienKhachDua') IS NULL
    BEGIN
        ALTER TABLE dbo.PHIEUTHU
            ADD TienKhachDua DECIMAL(18,2) NULL;
    END;

    IF COL_LENGTH('dbo.PHIEUTHU', 'TienThua') IS NULL
    BEGIN
        ALTER TABLE dbo.PHIEUTHU
            ADD TienThua DECIMAL(18,2) NULL;
    END;

    IF OBJECT_ID('dbo.CK_PHIEUTHU_TIENMAT', 'C') IS NULL
    BEGIN
        EXEC(N'ALTER TABLE dbo.PHIEUTHU
            ADD CONSTRAINT CK_PHIEUTHU_TIENMAT
            CHECK (
                (TienKhachDua IS NULL AND TienThua IS NULL)
                OR (TienKhachDua >= SoTienThu AND TienThua = TienKhachDua - SoTienThu)
            )');
    END;

    IF EXISTS
    (
        SELECT 1
        FROM dbo.PHIEUTHU
        WHERE MaGiaoDichNgoai IS NOT NULL
        GROUP BY MaGiaoDichNgoai
        HAVING COUNT(*) > 1
    )
    BEGIN
        THROW 51018,
              'Khong the tao unique index: PHIEUTHU co MaGiaoDichNgoai bi trung.',
              1;
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'UX_PHIEUTHU_MAGIAODICHNGOAI'
          AND object_id = OBJECT_ID('dbo.PHIEUTHU')
    )
    BEGIN
        EXEC(N'CREATE UNIQUE INDEX UX_PHIEUTHU_MAGIAODICHNGOAI
            ON dbo.PHIEUTHU(MaGiaoDichNgoai)
            WHERE MaGiaoDichNgoai IS NOT NULL');
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

GO

/* =========================================================
   MIGRATION 19: Hủy/hồi phiếu thu (gộp từ 19_admin_modernization_payment_reversal.sql)
   ========================================================= */
/* ================================================================
   Migration: 19_admin_modernization_payment_reversal
   Purpose  : Add immutable payment reversal ledger for successful
              receipt cancellation/undo without editing original receipt.
   Safety   : Idempotent, non-destructive, and enforces one reversal per
              original receipt through a unique constraint.
   ================================================================ */

USE QuanLyThuVien;
GO

SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID('dbo.PHIEUTHU_REVERSAL', 'U') IS NULL
    BEGIN
        CREATE TABLE dbo.PHIEUTHU_REVERSAL (
            MaDaoPhieuThu VARCHAR(30) PRIMARY KEY,
            MaPhieuThuGoc VARCHAR(30) NOT NULL,
            MaDocGia VARCHAR(30) NOT NULL,
            MaNhanVienDao VARCHAR(30) NOT NULL,
            SoTienHoan DECIMAL(18,2) NOT NULL,
            LyDo NVARCHAR(255) NOT NULL,
            ApprovalReference VARCHAR(100) NOT NULL,
            NgayDao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

            CONSTRAINT FK_PTREV_PHIEUTHU
                FOREIGN KEY (MaPhieuThuGoc) REFERENCES dbo.PHIEUTHU(MaPhieuThu),

            CONSTRAINT FK_PTREV_DOCGIA
                FOREIGN KEY (MaDocGia) REFERENCES dbo.DOCGIA(MaDocGia),

            CONSTRAINT FK_PTREV_NHANVIEN
                FOREIGN KEY (MaNhanVienDao) REFERENCES dbo.NHANVIEN(MaNhanVien),

            CONSTRAINT CK_PTREV_SOTIEN
                CHECK (SoTienHoan > 0)
        );
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'UX_PTREV_PHIEUTHU_GOC'
          AND object_id = OBJECT_ID('dbo.PHIEUTHU_REVERSAL')
    )
    BEGIN
        CREATE UNIQUE INDEX UX_PTREV_PHIEUTHU_GOC
            ON dbo.PHIEUTHU_REVERSAL(MaPhieuThuGoc);
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

GO

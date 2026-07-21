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

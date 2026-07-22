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

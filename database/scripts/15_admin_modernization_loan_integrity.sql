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

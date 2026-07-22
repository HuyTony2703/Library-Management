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

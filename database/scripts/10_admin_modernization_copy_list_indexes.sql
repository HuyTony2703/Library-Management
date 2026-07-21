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

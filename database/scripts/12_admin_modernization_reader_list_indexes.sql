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

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

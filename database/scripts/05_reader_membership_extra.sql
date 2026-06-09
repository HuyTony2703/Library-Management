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

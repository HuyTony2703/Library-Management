USE QuanLyThuVien;
GO

IF COL_LENGTH('dbo.THONGBAO', 'DaDoc') IS NULL
BEGIN
    ALTER TABLE dbo.THONGBAO
    ADD DaDoc BIT NOT NULL
        CONSTRAINT DF_THONGBAO_DADOC DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.THONGBAO', 'ThoiGianDoc') IS NULL
BEGIN
    ALTER TABLE dbo.THONGBAO
    ADD ThoiGianDoc DATETIME2 NULL;
END
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
        N'Thông báo mua hoặc gia hạn gói độc giả thành công'
    );
END
GO

MERGE LOAITHONGBAO AS T
USING (VALUES
    ('TB_SAP_DEN_HAN', N'Sách sắp đến hạn trả', N'Thông báo nhắc độc giả sắp đến hạn trả sách'),
    ('TB_QUA_HAN_TRA', N'Sách đã quá hạn trả', N'Thông báo nhắc độc giả có sách đã quá hạn trả'),
    ('TB_PHAT_SINH_PHAT', N'Phát sinh tiền phạt', N'Thông báo phát sinh tiền phạt mới'),
    ('TB_SACH_DA_CO', N'Sách đặt trước đã có', N'Thông báo sách đặt trước đã sẵn sàng'),
    ('TB_MUA_GOI_TC', N'Mua hoặc gia hạn gói thành viên thành công', N'Thông báo mua hoặc gia hạn gói thành viên thành công'),
    ('TB_GOI_SAP_HET_HAN', N'Gói thành viên sắp hết hạn', N'Thông báo nhắc gói thành viên sắp hết hạn'),
    ('TB_TAIKHOAN_THE_DOI_TRANGTHAI', N'Tài khoản hoặc thẻ độc giả thay đổi trạng thái', N'Thông báo khi tài khoản hoặc thẻ độc giả thay đổi trạng thái')
) AS S(MaLoaiThongBao, TenLoaiThongBao, MoTa)
ON T.MaLoaiThongBao = S.MaLoaiThongBao
WHEN MATCHED THEN
    UPDATE SET TenLoaiThongBao = S.TenLoaiThongBao, MoTa = S.MoTa
WHEN NOT MATCHED THEN
    INSERT (MaLoaiThongBao, TenLoaiThongBao, MoTa)
    VALUES (S.MaLoaiThongBao, S.TenLoaiThongBao, S.MoTa);
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_THONGBAO_TAIKHOAN_DADOC'
      AND object_id = OBJECT_ID('dbo.THONGBAO')
)
BEGIN
    CREATE INDEX IX_THONGBAO_TAIKHOAN_DADOC
    ON dbo.THONGBAO(MaTaiKhoanNhan, DaDoc, NgayTao);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM dbo.THONGBAO
    WHERE MaThongBao = 'TB_DG001_TEST_01'
)
BEGIN
    INSERT INTO dbo.THONGBAO(
        MaThongBao,
        MaTaiKhoanNhan,
        MaLoaiThongBao,
        TieuDe,
        NoiDung,
        NgayTao,
        GuiTrongApp,
        GuiEmail,
        TrangThaiEmail,
        SoLanThuGuiEmail,
        DaDoc,
        ThoiGianDoc
    )
    VALUES (
        'TB_DG001_TEST_01',
        'TK_DG001',
        'TB_SAP_DEN_HAN',
        N'Sách sắp đến hạn trả',
        N'Bạn có sách sắp đến hạn trả, vui lòng trả hoặc gia hạn đúng hạn.',
        SYSDATETIME(),
        1,
        0,
        N'Không gửi',
        0,
        0,
        NULL
    );
END
GO

SELECT TOP 5
    MaThongBao,
    MaTaiKhoanNhan,
    MaLoaiThongBao,
    TieuDe,
    DaDoc,
    ThoiGianDoc
FROM dbo.THONGBAO
ORDER BY NgayTao DESC;
GO

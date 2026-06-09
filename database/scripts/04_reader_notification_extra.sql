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

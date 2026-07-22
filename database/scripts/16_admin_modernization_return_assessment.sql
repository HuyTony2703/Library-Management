USE QuanLyThuVien;

-- Admin modernization: return assessment, damage/lost fine rules, and fine adjustment audit.
-- Safe to rerun; keeps existing return data intact.

IF OBJECT_ID('dbo.QUYDINH_PHAT_HONGMAT', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.QUYDINH_PHAT_HONGMAT (
        MaQuyDinhPhat VARCHAR(30) NOT NULL PRIMARY KEY,
        TinhTrang NVARCHAR(50) NOT NULL,
        MucDo VARCHAR(20) NOT NULL,
        TyLePhat DECIMAL(7,4) NOT NULL,
        SoTienToiThieu DECIMAL(18,2) NOT NULL CONSTRAINT DF_QUYDINH_PHAT_HONGMAT_Min DEFAULT 0,
        SoTienToiDa DECIMAL(18,2) NULL,
        TrangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_QUYDINH_PHAT_HONGMAT_Status DEFAULT N'Đang áp dụng',
        GhiChu NVARCHAR(255) NULL,
        CONSTRAINT CK_QUYDINH_PHAT_HONGMAT_TyLe CHECK (TyLePhat >= 0),
        CONSTRAINT CK_QUYDINH_PHAT_HONGMAT_Min CHECK (SoTienToiThieu >= 0),
        CONSTRAINT CK_QUYDINH_PHAT_HONGMAT_Max CHECK (SoTienToiDa IS NULL OR SoTienToiDa >= SoTienToiThieu)
    );

    CREATE UNIQUE INDEX UX_QUYDINH_PHAT_HONGMAT_Active
        ON dbo.QUYDINH_PHAT_HONGMAT(TinhTrang, MucDo)
        WHERE TrangThai = N'Đang áp dụng';
END;

IF NOT EXISTS (SELECT 1 FROM dbo.QUYDINH_PHAT_HONGMAT WHERE MaQuyDinhPhat = 'RETURN_DMG_LOW_V1')
BEGIN
    INSERT INTO dbo.QUYDINH_PHAT_HONGMAT
        (MaQuyDinhPhat, TinhTrang, MucDo, TyLePhat, SoTienToiThieu, SoTienToiDa, GhiChu)
    VALUES
        ('RETURN_DMG_LOW_V1', N'Hỏng', 'LOW', 0.2500, 0, NULL, N'Hư hỏng nhẹ, tính theo trị giá đầu sách'),
        ('RETURN_DMG_MEDIUM_V1', N'Hỏng', 'MEDIUM', 0.5000, 0, NULL, N'Hư hỏng vừa, tính theo trị giá đầu sách'),
        ('RETURN_DMG_HIGH_V1', N'Hỏng', 'HIGH', 0.7500, 0, NULL, N'Hư hỏng nặng, tính theo trị giá đầu sách'),
        ('RETURN_LOST_FULL_V1', N'Mất', 'FULL', 1.0000, 0, NULL, N'Mất sách, tính toàn bộ trị giá đầu sách');
END;

IF OBJECT_ID('dbo.PHIEUTRA_DIEUCHINH_PHAT', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.PHIEUTRA_DIEUCHINH_PHAT (
        MaDieuChinh VARCHAR(40) NOT NULL PRIMARY KEY,
        MaPhieuTra VARCHAR(30) NOT NULL,
        MaChiTietTra VARCHAR(30) NOT NULL,
        MaChiTietMuon VARCHAR(30) NOT NULL,
        TienPhatDeXuat DECIMAL(18,2) NOT NULL,
        TienPhatCuoiCung DECIMAL(18,2) NOT NULL,
        LyDoDieuChinh NVARCHAR(500) NOT NULL,
        MaNhanVienDieuChinh VARCHAR(30) NOT NULL,
        ThoiGianDieuChinh DATETIME2 NOT NULL,
        CONSTRAINT FK_PHIEUTRA_DIEUCHINH_PHAT_PhieuTra
            FOREIGN KEY (MaPhieuTra) REFERENCES dbo.PHIEUTRA(MaPhieuTra),
        CONSTRAINT FK_PHIEUTRA_DIEUCHINH_PHAT_ChiTietTra
            FOREIGN KEY (MaChiTietTra) REFERENCES dbo.CHITIETPHIEUTRA(MaChiTietTra),
        CONSTRAINT FK_PHIEUTRA_DIEUCHINH_PHAT_ChiTietMuon
            FOREIGN KEY (MaChiTietMuon) REFERENCES dbo.CHITIETPHIEUMUON(MaChiTietMuon),
        CONSTRAINT FK_PHIEUTRA_DIEUCHINH_PHAT_NhanVien
            FOREIGN KEY (MaNhanVienDieuChinh) REFERENCES dbo.NHANVIEN(MaNhanVien),
        CONSTRAINT CK_PHIEUTRA_DIEUCHINH_PHAT_Amounts
            CHECK (TienPhatDeXuat >= 0 AND TienPhatCuoiCung >= 0)
    );

    CREATE INDEX IX_PHIEUTRA_DIEUCHINH_PHAT_PhieuTra
        ON dbo.PHIEUTRA_DIEUCHINH_PHAT(MaPhieuTra);
END;

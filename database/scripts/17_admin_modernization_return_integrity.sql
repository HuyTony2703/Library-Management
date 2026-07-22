USE QuanLyThuVien;

-- Admin modernization: return creation integrity and idempotency.
-- Safe to rerun; nullable columns preserve existing return data.

IF COL_LENGTH('dbo.PHIEUTRA', 'IdempotencyKey') IS NULL
BEGIN
    ALTER TABLE dbo.PHIEUTRA
        ADD IdempotencyKey VARCHAR(100) NULL;
END;

IF COL_LENGTH('dbo.PHIEUTRA', 'RequestFingerprint') IS NULL
BEGIN
    ALTER TABLE dbo.PHIEUTRA
        ADD RequestFingerprint VARCHAR(64) NULL;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_PHIEUTRA_IdempotencyKey'
      AND object_id = OBJECT_ID('dbo.PHIEUTRA')
)
BEGIN
    EXEC(N'CREATE UNIQUE INDEX UX_PHIEUTRA_IdempotencyKey
        ON dbo.PHIEUTRA(IdempotencyKey)
        WHERE IdempotencyKey IS NOT NULL');
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_CHITIETPHIEUMUON_ReturnOpen'
      AND object_id = OBJECT_ID('dbo.CHITIETPHIEUMUON')
)
BEGIN
    CREATE INDEX IX_CHITIETPHIEUMUON_ReturnOpen
        ON dbo.CHITIETPHIEUMUON(MaChiTietMuon, TrangThai, MaPhieuMuon)
        INCLUDE (MaCuonSach, HanTra, NgayTraThucTe);
END;

/* ================================================================
   Migration: 08_admin_modernization_staff_context
   Purpose  : Add versioned staff-to-branch assignments and backfill
              the current NHANVIEN.MaChiNhanh as the default branch.
   Safety   : Idempotent for repeated deployment; no destructive DDL.
   ================================================================ */

USE QuanLyThuVien;
GO

SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID('dbo.NHANVIEN_CHINHANH', 'U') IS NULL
    BEGIN
        CREATE TABLE dbo.NHANVIEN_CHINHANH
        (
            MaNhanVien VARCHAR(30) NOT NULL,
            MaChiNhanh VARCHAR(30) NOT NULL,
            LaMacDinh BIT NOT NULL CONSTRAINT DF_NVCN_LAMACDINH DEFAULT 0,
            NgayBatDau DATE NOT NULL CONSTRAINT DF_NVCN_NGAYBATDAU DEFAULT CAST(SYSDATETIME() AS DATE),
            NgayKetThuc DATE NULL,
            TrangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_NVCN_TRANGTHAI DEFAULT N'Hoạt động',

            CONSTRAINT PK_NHANVIEN_CHINHANH
                PRIMARY KEY (MaNhanVien, MaChiNhanh),

            CONSTRAINT FK_NVCN_NHANVIEN
                FOREIGN KEY (MaNhanVien) REFERENCES dbo.NHANVIEN(MaNhanVien),

            CONSTRAINT FK_NVCN_CHINHANH
                FOREIGN KEY (MaChiNhanh) REFERENCES dbo.CHINHANH(MaChiNhanh),

            CONSTRAINT CK_NVCN_NGAY
                CHECK (NgayKetThuc IS NULL OR NgayKetThuc >= NgayBatDau),

            CONSTRAINT CK_NVCN_TRANGTHAI
                CHECK (TrangThai IN (N'Hoạt động', N'Ngừng hoạt động'))
        );
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'UX_NVCN_MACDINH_HOATDONG'
          AND object_id = OBJECT_ID('dbo.NHANVIEN_CHINHANH')
    )
    BEGIN
        CREATE UNIQUE INDEX UX_NVCN_MACDINH_HOATDONG
            ON dbo.NHANVIEN_CHINHANH(MaNhanVien)
            WHERE LaMacDinh = 1 AND TrangThai = N'Hoạt động';
    END;

    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IX_NVCN_CHINHANH_TRANGTHAI'
          AND object_id = OBJECT_ID('dbo.NHANVIEN_CHINHANH')
    )
    BEGIN
        CREATE INDEX IX_NVCN_CHINHANH_TRANGTHAI
            ON dbo.NHANVIEN_CHINHANH(MaChiNhanh, TrangThai, NgayBatDau, NgayKetThuc)
            INCLUDE (MaNhanVien, LaMacDinh);
    END;

    INSERT INTO dbo.NHANVIEN_CHINHANH
    (
        MaNhanVien,
        MaChiNhanh,
        LaMacDinh,
        NgayBatDau,
        NgayKetThuc,
        TrangThai
    )
    SELECT
        nv.MaNhanVien,
        nv.MaChiNhanh,
        CASE
            WHEN EXISTS
            (
                SELECT 1
                FROM dbo.NHANVIEN_CHINHANH existingDefault
                WHERE existingDefault.MaNhanVien = nv.MaNhanVien
                  AND existingDefault.LaMacDinh = 1
                  AND existingDefault.TrangThai = N'Hoạt động'
            ) THEN 0
            ELSE 1
        END,
        COALESCE(nv.NgayVaoLam, CAST(SYSDATETIME() AS DATE)),
        NULL,
        N'Hoạt động'
    FROM dbo.NHANVIEN nv
    WHERE nv.MaChiNhanh IS NOT NULL
      AND NOT EXISTS
      (
          SELECT 1
          FROM dbo.NHANVIEN_CHINHANH existing
          WHERE existing.MaNhanVien = nv.MaNhanVien
            AND existing.MaChiNhanh = nv.MaChiNhanh
      );

    UPDATE currentAssignment
    SET
        currentAssignment.LaMacDinh = 1,
        currentAssignment.NgayKetThuc = NULL,
        currentAssignment.TrangThai = N'Hoạt động'
    FROM dbo.NHANVIEN_CHINHANH currentAssignment
    INNER JOIN dbo.NHANVIEN nv
        ON nv.MaNhanVien = currentAssignment.MaNhanVien
       AND nv.MaChiNhanh = currentAssignment.MaChiNhanh
    WHERE nv.MaChiNhanh IS NOT NULL
      AND NOT EXISTS
      (
          SELECT 1
          FROM dbo.NHANVIEN_CHINHANH existingDefault
          WHERE existingDefault.MaNhanVien = currentAssignment.MaNhanVien
            AND existingDefault.LaMacDinh = 1
            AND existingDefault.TrangThai = N'Hoạt động'
      );

    ;WITH DefaultOperationalBranch AS
    (
        SELECT TOP (1) cn.MaChiNhanh
        FROM dbo.CHINHANH cn
        WHERE cn.TrangThai = N'Hoạt động'
        ORDER BY cn.MaChiNhanh
    ),
    AdminStaffWithoutBranch AS
    (
        SELECT nv.MaNhanVien, branch.MaChiNhanh
        FROM dbo.NHANVIEN nv
        INNER JOIN dbo.TAIKHOAN tk
            ON tk.MaTaiKhoan = nv.MaTaiKhoan
        INNER JOIN dbo.VAITRO vt
            ON vt.MaVaiTro = tk.MaVaiTro
        CROSS JOIN DefaultOperationalBranch branch
        WHERE vt.TenVaiTro = 'QUAN_TRI_VIEN'
          AND NOT EXISTS
          (
              SELECT 1
              FROM dbo.NHANVIEN_CHINHANH existing
              WHERE existing.MaNhanVien = nv.MaNhanVien
                AND existing.TrangThai = N'Hoạt động'
                AND existing.NgayKetThuc IS NULL
          )
    )
    INSERT INTO dbo.NHANVIEN_CHINHANH
    (
        MaNhanVien,
        MaChiNhanh,
        LaMacDinh,
        NgayBatDau,
        NgayKetThuc,
        TrangThai
    )
    SELECT
        adminStaff.MaNhanVien,
        adminStaff.MaChiNhanh,
        1,
        CAST(SYSDATETIME() AS DATE),
        NULL,
        N'Hoạt động'
    FROM AdminStaffWithoutBranch adminStaff
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.NHANVIEN_CHINHANH existing
        WHERE existing.MaNhanVien = adminStaff.MaNhanVien
          AND existing.MaChiNhanh = adminStaff.MaChiNhanh
    );

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

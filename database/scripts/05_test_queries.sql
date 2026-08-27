USE QuanLyThuVien;
GO

/* =========================================================
   05_test_queries.sql
   Các câu truy vấn kiểm tra database
   ========================================================= */

/* 1. Kiểm tra số lượng dữ liệu chính */
SELECT COUNT(*) AS SoTaiKhoan FROM TAIKHOAN;
SELECT COUNT(*) AS SoDocGia FROM DOCGIA;
SELECT COUNT(*) AS SoDauSach FROM DAUSACH;
SELECT COUNT(*) AS SoCuonSach FROM CUONSACH;
SELECT COUNT(*) AS SoPhieuMuon FROM PHIEUMUON;
SELECT COUNT(*) AS SoKhoanNo FROM KHOANNO;

/* 2. Kiểm tra danh sách tài khoản */
SELECT 
    tk.MaTaiKhoan,
    tk.TenDangNhap,
    tk.EmailDangNhap,
    vt.TenVaiTro,
    tk.TrangThai
FROM TAIKHOAN tk
JOIN VAITRO vt ON tk.MaVaiTro = vt.MaVaiTro;

/* 3. Kiểm tra danh sách độc giả */
SELECT
    dg.MaDocGia,
    dg.HoTen,
    ndg.TenNhomDocGia,
    dg.Email,
    dg.NgayLapThe,
    dg.NgayHetHanThe,
    dg.TrangThai
FROM DOCGIA dg
JOIN NHOMDOCGIA ndg ON dg.MaNhomDocGia = ndg.MaNhomDocGia;

/* 4. Kiểm tra danh sách sách */
SELECT
    ds.MaDauSach,
    cs.MaCuonSach,
    ds.TenDauSach,
    tl.TenTheLoai,
    tg.TenTacGia,
    cn.TenChiNhanh,
    vt.MaViTriHienThi,
    tt.TenTrangThai
FROM CUONSACH cs
JOIN DAUSACH ds ON cs.MaDauSach = ds.MaDauSach
JOIN CHINHANH cn ON cs.MaChiNhanh = cn.MaChiNhanh
JOIN VITRISACH vt ON cs.MaViTri = vt.MaViTri
JOIN TRANGTHAICUONSACH tt ON cs.MaTrangThai = tt.MaTrangThai
LEFT JOIN DAUSACH_THELOAI dstl ON ds.MaDauSach = dstl.MaDauSach
LEFT JOIN THELOAI tl ON dstl.MaTheLoai = tl.MaTheLoai
LEFT JOIN DAUSACH_TACGIA dstg ON ds.MaDauSach = dstg.MaDauSach
LEFT JOIN TACGIA tg ON dstg.MaTacGia = tg.MaTacGia;

/* 5. Kiểm tra sách đang sẵn có */
SELECT
    cs.MaCuonSach,
    ds.TenDauSach,
    tt.TenTrangThai
FROM CUONSACH cs
JOIN DAUSACH ds ON cs.MaDauSach = ds.MaDauSach
JOIN TRANGTHAICUONSACH tt ON cs.MaTrangThai = tt.MaTrangThai
WHERE tt.TenTrangThai = N'Sẵn có';

/* 6. Kiểm tra tổng nợ độc giả */
SELECT * FROM VW_TONGNO_DOCGIA;

/* 7. Kiểm tra số sách đang mượn */
SELECT * FROM VW_SOSACH_DANGMUON;

/* 8. Kiểm tra báo cáo mượn sách theo thể loại */
SELECT * FROM VW_BAOCAO_MUON_THELOAI;

/* 9. Kiểm tra báo cáo sách trả trễ */
SELECT * FROM VW_BAOCAO_TRA_TRE;

/* 10. Kiểm tra quy định hiện hành */
SELECT
    pb.MaPhienBan,
    pb.TenPhienBan,
    ts.TuoiToiThieu,
    ts.TuoiToiDa,
    ts.ThoiHanTheTheoThang,
    ts.KhoangCachNamXuatBan,
    ts.SoNgayNhacTruocHan,
    ts.SoNgayGiuDatTruoc,
    ts.MucPhatTreMoiNgay
FROM PHIENBANQUYDINH pb
JOIN THAMSOQUYDINH ts ON pb.MaPhienBan = ts.MaPhienBan
WHERE pb.TrangThai = N'Đang áp dụng';
GO

SELECT N'Số độc giả' AS NoiDung, COUNT(*) AS SoLuong FROM DOCGIA
UNION ALL
SELECT N'Số đầu sách', COUNT(*) FROM DAUSACH
UNION ALL
SELECT N'Số cuốn sách', COUNT(*) FROM CUONSACH
UNION ALL
SELECT N'Số phiếu mượn', COUNT(*) FROM PHIEUMUON
UNION ALL
SELECT N'Số khoản nợ', COUNT(*) FROM KHOANNO
UNION ALL
SELECT N'Số phiếu đặt trước', COUNT(*) FROM PHIEUDATTRUOC;
GO

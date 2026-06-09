package com.library.backend.service.admin;

import com.library.backend.dto.AdminReportResponses.BorrowByCategoryReportResponse;
import com.library.backend.dto.AdminReportResponses.CurrentLoanReportResponse;
import com.library.backend.dto.AdminReportResponses.DebtReportResponse;
import com.library.backend.dto.AdminReportResponses.LateReturnReportResponse;
import com.library.backend.dto.AdminReportResponses.OverviewResponse;
import com.library.backend.dto.AdminReportResponses.PaymentReportResponse;
import com.library.backend.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminReportService {

    private final JdbcTemplate jdbcTemplate;

    public AdminReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public OverviewResponse getOverview(Integer month, Integer year) {
        int resolvedMonth = resolveMonth(month);
        int resolvedYear = resolveYear(year);

        Long totalBookCopies = queryLong("""
                SELECT COUNT(*)
                FROM CUONSACH
                """);

        Long availableBookCopies = queryLong("""
                SELECT COUNT(*)
                FROM CUONSACH
                WHERE MaTrangThai = 'TT_SANCO'
                """);

        Long borrowedBookCopies = queryLong("""
                SELECT COUNT(*)
                FROM CUONSACH
                WHERE MaTrangThai = 'TT_DANGMUON'
                """);

        Long activeReaders = queryLong("""
                SELECT COUNT(*)
                FROM DOCGIA
                WHERE TrangThai = N'Hoạt động'
                """);

        Long loansThisMonth = queryLong("""
                SELECT COUNT(*)
                FROM CHITIETPHIEUMUON
                WHERE MONTH(NgayMuon) = ?
                  AND YEAR(NgayMuon) = ?
                """, resolvedMonth, resolvedYear);

        Long lateReturnsThisMonth = queryLong("""
                SELECT COUNT(*)
                FROM VW_BAOCAO_TRA_TRE
                WHERE Thang = ?
                  AND Nam = ?
                  AND SoNgayTre > 0
                """, resolvedMonth, resolvedYear);

        BigDecimal totalDebt = queryDecimal("""
                SELECT CAST(ISNULL(SUM(SoTienConLai), 0) AS DECIMAL(18,2))
                FROM KHOANNO
                WHERE SoTienConLai > 0
                """);

        BigDecimal paymentsThisMonth = queryDecimal("""
                SELECT CAST(ISNULL(SUM(SoTienThu), 0) AS DECIMAL(18,2))
                FROM PHIEUTHU
                WHERE MONTH(NgayThu) = ?
                  AND YEAR(NgayThu) = ?
                  AND TrangThai = N'Thành công'
                """, resolvedMonth, resolvedYear);

        return new OverviewResponse(
                totalBookCopies,
                availableBookCopies,
                borrowedBookCopies,
                activeReaders,
                loansThisMonth,
                lateReturnsThisMonth,
                totalDebt,
                paymentsThisMonth
        );
    }

    public List<DebtReportResponse> getDebts() {
        String sql = """
                SELECT
                    MaDocGia,
                    HoTen,
                    TongNoConLai
                FROM VW_TONGNO_DOCGIA
                WHERE TongNoConLai > 0
                ORDER BY TongNoConLai DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new DebtReportResponse(
                        rs.getString("MaDocGia"),
                        rs.getString("HoTen"),
                        rs.getBigDecimal("TongNoConLai")
                )
        );
    }

    public List<CurrentLoanReportResponse> getCurrentLoans() {
        String sql = """
                SELECT
                    ctm.MaChiTietMuon,
                    pm.MaPhieuMuon,
                    dg.MaDocGia,
                    dg.HoTen AS HoTenDocGia,
                    ctm.MaCuonSach,
                    ds.TenDauSach,
                    ctm.NgayMuon,
                    ctm.HanTra,
                    DATEDIFF(DAY, SYSDATETIME(), ctm.HanTra) AS SoNgayConLai
                FROM CHITIETPHIEUMUON ctm
                INNER JOIN PHIEUMUON pm ON ctm.MaPhieuMuon = pm.MaPhieuMuon
                INNER JOIN DOCGIA dg ON pm.MaDocGia = dg.MaDocGia
                INNER JOIN CUONSACH cs ON ctm.MaCuonSach = cs.MaCuonSach
                INNER JOIN DAUSACH ds ON cs.MaDauSach = ds.MaDauSach
                WHERE ctm.TrangThai = N'Đang mượn'
                ORDER BY ctm.HanTra ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CurrentLoanReportResponse(
                        rs.getString("MaChiTietMuon"),
                        rs.getString("MaPhieuMuon"),
                        rs.getString("MaDocGia"),
                        rs.getString("HoTenDocGia"),
                        rs.getString("MaCuonSach"),
                        rs.getString("TenDauSach"),
                        getDateTime(rs.getTimestamp("NgayMuon")),
                        getDateTime(rs.getTimestamp("HanTra")),
                        rs.getInt("SoNgayConLai")
                )
        );
    }

    public List<BorrowByCategoryReportResponse> getBorrowByCategory(Integer month, Integer year) {
        int resolvedMonth = resolveMonth(month);
        int resolvedYear = resolveYear(year);

        String sql = """
                SELECT
                    Thang,
                    Nam,
                    MaTheLoai,
                    TenTheLoai,
                    SoLuotMuon,
                    TiLePhanTram
                FROM VW_BAOCAO_MUON_THELOAI
                WHERE Thang = ?
                  AND Nam = ?
                ORDER BY SoLuotMuon DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new BorrowByCategoryReportResponse(
                        rs.getInt("Thang"),
                        rs.getInt("Nam"),
                        rs.getString("MaTheLoai"),
                        rs.getString("TenTheLoai"),
                        rs.getLong("SoLuotMuon"),
                        rs.getBigDecimal("TiLePhanTram")
                ),
                resolvedMonth,
                resolvedYear
        );
    }

    public List<LateReturnReportResponse> getLateReturns(Integer month, Integer year) {
        int resolvedMonth = resolveMonth(month);
        int resolvedYear = resolveYear(year);

        String sql = """
                SELECT
                    Thang,
                    Nam,
                    MaCuonSach,
                    TenDauSach,
                    MaDocGia,
                    HoTenDocGia,
                    NgayMuon,
                    HanTra,
                    NgayTraThucTe,
                    SoNgayTre,
                    TienPhatTre
                FROM VW_BAOCAO_TRA_TRE
                WHERE Thang = ?
                  AND Nam = ?
                  AND SoNgayTre > 0
                ORDER BY SoNgayTre DESC, TienPhatTre DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new LateReturnReportResponse(
                        rs.getInt("Thang"),
                        rs.getInt("Nam"),
                        rs.getString("MaCuonSach"),
                        rs.getString("TenDauSach"),
                        rs.getString("MaDocGia"),
                        rs.getString("HoTenDocGia"),
                        getDateTime(rs.getTimestamp("NgayMuon")),
                        getDateTime(rs.getTimestamp("HanTra")),
                        getDateTime(rs.getTimestamp("NgayTraThucTe")),
                        rs.getInt("SoNgayTre"),
                        rs.getBigDecimal("TienPhatTre")
                ),
                resolvedMonth,
                resolvedYear
        );
    }

    public List<PaymentReportResponse> getPayments(Integer month, Integer year) {
        int resolvedMonth = resolveMonth(month);
        int resolvedYear = resolveYear(year);

        String sql = """
                SELECT
                    pt.MaPhieuThu,
                    pt.MaDocGia,
                    dg.HoTen AS HoTenDocGia,
                    pt.MaNhanVienThu,
                    pttt.TenPhuongThuc,
                    pt.LoaiThu,
                    pt.SoTienThu,
                    pt.NgayThu,
                    pt.TrangThai
                FROM PHIEUTHU pt
                INNER JOIN DOCGIA dg ON pt.MaDocGia = dg.MaDocGia
                LEFT JOIN PHUONGTHUCTHANHTOAN pttt ON pt.MaPhuongThuc = pttt.MaPhuongThuc
                WHERE MONTH(pt.NgayThu) = ?
                  AND YEAR(pt.NgayThu) = ?
                ORDER BY pt.NgayThu DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new PaymentReportResponse(
                        rs.getString("MaPhieuThu"),
                        rs.getString("MaDocGia"),
                        rs.getString("HoTenDocGia"),
                        rs.getString("MaNhanVienThu"),
                        rs.getString("TenPhuongThuc"),
                        rs.getString("LoaiThu"),
                        rs.getBigDecimal("SoTienThu"),
                        getDateTime(rs.getTimestamp("NgayThu")),
                        rs.getString("TrangThai")
                ),
                resolvedMonth,
                resolvedYear
        );
    }

    private Long queryLong(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private BigDecimal queryDecimal(String sql, Object... args) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return value == null ? BigDecimal.ZERO : value;
    }

    private LocalDateTime getDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private int resolveMonth(Integer month) {
        int resolvedMonth = month == null ? LocalDate.now().getMonthValue() : month;

        if (resolvedMonth < 1 || resolvedMonth > 12) {
            throw new BusinessException("Tháng báo cáo phải nằm trong khoảng 1 đến 12");
        }

        return resolvedMonth;
    }

    private int resolveYear(Integer year) {
        int resolvedYear = year == null ? LocalDate.now().getYear() : year;

        if (resolvedYear < 2000 || resolvedYear > 2100) {
            throw new BusinessException("Năm báo cáo phải nằm trong khoảng 2000 đến 2100");
        }

        return resolvedYear;
    }
}

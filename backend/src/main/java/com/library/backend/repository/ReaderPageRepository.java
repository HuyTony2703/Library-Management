package com.library.backend.repository;

import com.library.backend.dto.PageResponse;
import com.library.backend.dto.EntityPickerOptionResponse;
import com.library.backend.dto.ReaderDebtItemResponse;
import com.library.backend.dto.ReaderListItemResponse;
import com.library.backend.dto.ReaderListQuery;
import com.library.backend.dto.ReaderLoanItemResponse;
import com.library.backend.dto.ReaderMembershipResponse;
import com.library.backend.dto.ReaderOverviewResponse;
import com.library.backend.dto.ReaderTransactionResponse;
import com.library.backend.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class ReaderPageRepository {
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "fullName", "dg.HoTen COLLATE Vietnamese_100_CI_AI",
            "cardIssued", "dg.NgayLapThe",
            "cardExpiry", "dg.NgayHetHanThe",
            "membershipExpiry", "gm.NgayKetThuc"
    );
    private static final String LIST_CORE_FROM = """
            FROM DOCGIA dg
            INNER JOIN NHOMDOCGIA ndg ON ndg.MaNhomDocGia = dg.MaNhomDocGia
            INNER JOIN TAIKHOAN tk ON tk.MaTaiKhoan = dg.MaTaiKhoan
            OUTER APPLY (
                SELECT TOP 1 lsg.MaGoiThanhVien, gtv.TenGoi, lsg.NgayBatDau, lsg.NgayKetThuc, lsg.TrangThai
                FROM LICHSUGOITHANHVIEN lsg
                INNER JOIN GOITHANHVIEN gtv ON gtv.MaGoiThanhVien = lsg.MaGoiThanhVien
                WHERE lsg.MaDocGia = dg.MaDocGia
                ORDER BY CASE WHEN :today BETWEEN lsg.NgayBatDau AND lsg.NgayKetThuc
                                   AND lsg.TrangThai = N'Đang sử dụng' THEN 0 ELSE 1 END,
                         lsg.NgayKetThuc DESC, lsg.NgayBatDau DESC, lsg.MaLichSuGoi DESC
            ) gm
            OUTER APPLY (
                SELECT TOP 1 1 AS DangKhoaDangNhap
                FROM DOCGIA_KHOA dk
                WHERE dk.MaDocGia = dg.MaDocGia AND dk.PhamVi = 'LOGIN' AND dk.MoKhoaLuc IS NULL
                  AND (dk.KhoaDen IS NULL OR dk.KhoaDen >= :today)
            ) login_lock
            """;
    private static final String SUMMARY_APPLIES = """
            OUTER APPLY (
                SELECT COUNT_BIG(*) AS SoSachDangMuon
                FROM PHIEUMUON pm
                INNER JOIN CHITIETPHIEUMUON ctm ON ctm.MaPhieuMuon = pm.MaPhieuMuon
                WHERE pm.MaDocGia = dg.MaDocGia AND ctm.TrangThai = N'Đang mượn'
            ) loan_summary
            OUTER APPLY (
                SELECT CAST(COALESCE(SUM(kn.SoTienConLai), 0) AS DECIMAL(18,2)) AS TongNo
                FROM KHOANNO kn
                WHERE kn.MaDocGia = dg.MaDocGia AND kn.TrangThai <> N'Đã thanh toán'
            ) debt_summary
            """;
    private static final String LIST_FROM = LIST_CORE_FROM + SUMMARY_APPLIES;
    private static final String PAGE_SUMMARY_APPLIES = """
            OUTER APPLY (
                SELECT COUNT_BIG(*) AS SoSachDangMuon
                FROM PHIEUMUON pm
                INNER JOIN CHITIETPHIEUMUON ctm ON ctm.MaPhieuMuon = pm.MaPhieuMuon
                WHERE pm.MaDocGia = p.MaDocGia AND ctm.TrangThai = N'Đang mượn'
            ) loan_summary
            OUTER APPLY (
                SELECT CAST(COALESCE(SUM(kn.SoTienConLai), 0) AS DECIMAL(18,2)) AS TongNo
                FROM KHOANNO kn
                WHERE kn.MaDocGia = p.MaDocGia AND kn.TrangThai <> N'Đã thanh toán'
            ) debt_summary
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ReaderPageRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResponse<ReaderListItemResponse> findPage(ReaderListQuery query, LocalDate today) {
        MapSqlParameterSource params = baseParams(query, today);
        List<String> conditions = conditions(query, today, params);
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        String order = parseSort(query.sort());
        Long total = jdbcTemplate.queryForObject("SELECT COUNT_BIG(*) " + LIST_CORE_FROM + where, params, Long.class);
        long totalItems = total == null ? 0 : total;
        params.addValue("offset", (query.page() - 1) * query.pageSize()).addValue("pageSize", query.pageSize());
        List<ReaderListItemResponse> items = jdbcTemplate.query("""
                WITH p AS (
                    SELECT ROW_NUMBER() OVER (ORDER BY
                    """ + order + ") AS PageOrder, " + """
                           dg.MaDocGia, dg.HoTen, dg.MaNhomDocGia, ndg.TenNhomDocGia,
                           dg.TrangThai AS TrangThaiHoSo, dg.NgayLapThe, dg.NgayHetHanThe,
                           gm.MaGoiThanhVien, gm.TenGoi, gm.NgayKetThuc,
                           CASE WHEN login_lock.DangKhoaDangNhap = 1 THEN N'Khóa đăng nhập' ELSE tk.TrangThai END AS TrangThaiTaiKhoan
                    """ + LIST_CORE_FROM + where + " ORDER BY " + order +
                    " OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY) " + """
                SELECT p.MaDocGia, p.HoTen, p.MaNhomDocGia, p.TenNhomDocGia,
                       p.TrangThaiHoSo, p.NgayLapThe, p.NgayHetHanThe,
                       p.MaGoiThanhVien, p.TenGoi, p.NgayKetThuc, p.TrangThaiTaiKhoan,
                       COALESCE(loan_summary.SoSachDangMuon, 0) AS SoSachDangMuon,
                       COALESCE(debt_summary.TongNo, 0) AS TongNo
                FROM p
                """ + PAGE_SUMMARY_APPLIES + " ORDER BY p.PageOrder", params,
                (rs, rowNum) -> new ReaderListItemResponse(
                        rs.getString("MaDocGia"), rs.getString("HoTen"), rs.getString("MaNhomDocGia"),
                        rs.getString("TenNhomDocGia"), rs.getString("TrangThaiHoSo"),
                        date(rs.getDate("NgayLapThe")), date(rs.getDate("NgayHetHanThe")),
                        cardStatus(date(rs.getDate("NgayHetHanThe")), today),
                        rs.getString("MaGoiThanhVien"), rs.getString("TenGoi"), date(rs.getDate("NgayKetThuc")),
                        membershipStatus(date(rs.getDate("NgayKetThuc")), today),
                        rs.getString("TrangThaiTaiKhoan"), rs.getLong("SoSachDangMuon"), rs.getBigDecimal("TongNo")
                ));
        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + query.pageSize() - 1) / query.pageSize());
        return new PageResponse<>(items, query.page(), query.pageSize(), totalItems, totalPages);
    }

    public List<EntityPickerOptionResponse> searchForPicker(String query, int limit) {
        String normalized = query.trim();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("query", normalized)
                .addValue("pattern", "%" + escapeLike(normalized) + "%")
                .addValue("limit", limit);
        return jdbcTemplate.query("""
                SELECT TOP (:limit) dg.MaDocGia, dg.HoTen, dg.Email, dg.SoDienThoai,
                       dg.TrangThai, dg.NgayHetHanThe
                FROM DOCGIA dg
                WHERE dg.MaDocGia LIKE :pattern ESCAPE '\\'
                   OR dg.HoTen COLLATE Vietnamese_100_CI_AI LIKE :pattern ESCAPE '\\'
                   OR dg.Email LIKE :pattern ESCAPE '\\'
                   OR dg.SoDienThoai LIKE :pattern ESCAPE '\\'
                ORDER BY CASE WHEN UPPER(dg.MaDocGia) = UPPER(:query) THEN 0 ELSE 1 END,
                         dg.HoTen COLLATE Vietnamese_100_CI_AI, dg.MaDocGia
                """, params, (rs, rowNum) -> {
            Map<String, Object> metadata = new LinkedHashMap<>();
            if (rs.getString("Email") != null) metadata.put("email", rs.getString("Email"));
            if (rs.getString("SoDienThoai") != null) metadata.put("phone", rs.getString("SoDienThoai"));
            metadata.put("profileStatus", rs.getString("TrangThai"));
            if (rs.getDate("NgayHetHanThe") != null) metadata.put("cardExpiresAt", date(rs.getDate("NgayHetHanThe")));
            String readerId = rs.getString("MaDocGia");
            return new EntityPickerOptionResponse(readerId, rs.getString("HoTen"), readerId,
                    metadata, readerId.equalsIgnoreCase(normalized));
        });
    }

    public ReaderOverviewResponse findOverview(String readerId, LocalDate today) {
        MapSqlParameterSource params = new MapSqlParameterSource("readerId", readerId).addValue("today", today);
        return jdbcTemplate.query("""
                SELECT dg.MaDocGia, dg.MaTaiKhoan, tk.TenDangNhap, dg.HoTen, dg.NgaySinh,
                       dg.DiaChi, dg.Email, dg.SoDienThoai, dg.MaNhomDocGia, ndg.TenNhomDocGia,
                       dg.TrangThai AS TrangThaiHoSo, dg.NgayLapThe, dg.NgayHetHanThe,
                       CASE WHEN login_lock.DangKhoaDangNhap = 1 THEN N'Khóa đăng nhập' ELSE tk.TrangThai END AS TrangThaiTaiKhoan,
                       gm.MaGoiThanhVien, gm.TenGoi, gm.NgayBatDau, gm.NgayKetThuc,
                       COALESCE(loan_summary.SoSachDangMuon, 0) AS SoSachDangMuon,
                       COALESCE(debt_summary.TongNo, 0) AS TongNo
                """ + LIST_FROM + " WHERE dg.MaDocGia = :readerId", params, (rs, rowNum) ->
                new ReaderOverviewResponse(
                        rs.getString("MaDocGia"), rs.getString("MaTaiKhoan"), rs.getString("TenDangNhap"),
                        rs.getString("HoTen"), date(rs.getDate("NgaySinh")), rs.getString("DiaChi"),
                        rs.getString("Email"), rs.getString("SoDienThoai"), rs.getString("MaNhomDocGia"),
                        rs.getString("TenNhomDocGia"), rs.getString("TrangThaiHoSo"),
                        date(rs.getDate("NgayLapThe")), date(rs.getDate("NgayHetHanThe")),
                        cardStatus(date(rs.getDate("NgayHetHanThe")), today), rs.getString("TrangThaiTaiKhoan"),
                        rs.getString("MaGoiThanhVien"), rs.getString("TenGoi"), date(rs.getDate("NgayBatDau")),
                        date(rs.getDate("NgayKetThuc")), membershipStatus(date(rs.getDate("NgayKetThuc")), today),
                        rs.getLong("SoSachDangMuon"), rs.getBigDecimal("TongNo")
                )).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy độc giả"));
    }

    public List<ReaderMembershipResponse> findMemberships(String readerId, LocalDate today) {
        return jdbcTemplate.query("""
                SELECT lsg.MaLichSuGoi, lsg.MaGoiThanhVien, gtv.TenGoi,
                       lsg.NgayBatDau, lsg.NgayKetThuc, lsg.GhiChu
                FROM LICHSUGOITHANHVIEN lsg
                INNER JOIN GOITHANHVIEN gtv ON gtv.MaGoiThanhVien = lsg.MaGoiThanhVien
                WHERE lsg.MaDocGia = :readerId
                ORDER BY lsg.NgayBatDau DESC, lsg.NgayKetThuc DESC, lsg.MaLichSuGoi DESC
                """, new MapSqlParameterSource("readerId", readerId), (rs, rowNum) ->
                new ReaderMembershipResponse(rs.getString("MaLichSuGoi"), rs.getString("MaGoiThanhVien"),
                        rs.getString("TenGoi"), date(rs.getDate("NgayBatDau")), date(rs.getDate("NgayKetThuc")),
                        membershipStatus(date(rs.getDate("NgayKetThuc")), today), rs.getString("GhiChu")));
    }

    public List<ReaderLoanItemResponse> findCurrentLoans(String readerId, LocalDateTime now) {
        return jdbcTemplate.query("""
                SELECT pm.MaPhieuMuon, ctm.MaChiTietMuon, cs.MaCuonSach, cs.MaVach,
                       ds.MaDauSach, ds.TenDauSach, pm.MaChiNhanh, cn.TenChiNhanh,
                       ctm.NgayMuon, ctm.HanTra, ctm.TrangThai
                FROM PHIEUMUON pm
                INNER JOIN CHITIETPHIEUMUON ctm ON ctm.MaPhieuMuon = pm.MaPhieuMuon
                INNER JOIN CUONSACH cs ON cs.MaCuonSach = ctm.MaCuonSach
                INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
                INNER JOIN CHINHANH cn ON cn.MaChiNhanh = pm.MaChiNhanh
                WHERE pm.MaDocGia = :readerId AND ctm.TrangThai = N'Đang mượn'
                ORDER BY ctm.HanTra, ctm.MaChiTietMuon
                """, new MapSqlParameterSource("readerId", readerId), (rs, rowNum) -> {
            LocalDateTime due = rs.getTimestamp("HanTra").toLocalDateTime();
            return new ReaderLoanItemResponse(rs.getString("MaPhieuMuon"), rs.getString("MaChiTietMuon"),
                    rs.getString("MaCuonSach"), rs.getString("MaVach"), rs.getString("MaDauSach"),
                    rs.getString("TenDauSach"), rs.getString("MaChiNhanh"), rs.getString("TenChiNhanh"),
                    rs.getTimestamp("NgayMuon").toLocalDateTime(), due,
                    overdueDays(due, now),
                    rs.getString("TrangThai"));
        });
    }

    static long overdueDays(LocalDateTime due, LocalDateTime now) {
        if (!due.isBefore(now)) return 0;
        return Math.max(1, ChronoUnit.DAYS.between(due.toLocalDate(), now.toLocalDate()));
    }

    public List<ReaderDebtItemResponse> findDebts(String readerId) {
        return jdbcTemplate.query("""
                SELECT kn.MaKhoanNo, kn.MaLoaiKhoanNo, lkn.TenLoaiKhoanNo,
                       kn.SoTienPhatSinh, kn.SoTienDaThanhToan, kn.SoTienConLai,
                       kn.NgayPhatSinh, kn.LyDo, kn.TrangThai
                FROM KHOANNO kn
                INNER JOIN LOAIKHOANNO lkn ON lkn.MaLoaiKhoanNo = kn.MaLoaiKhoanNo
                WHERE kn.MaDocGia = :readerId
                ORDER BY kn.NgayPhatSinh DESC, kn.MaKhoanNo DESC
                """, new MapSqlParameterSource("readerId", readerId), (rs, rowNum) ->
                new ReaderDebtItemResponse(rs.getString("MaKhoanNo"), rs.getString("MaLoaiKhoanNo"),
                        rs.getString("TenLoaiKhoanNo"), rs.getBigDecimal("SoTienPhatSinh"),
                        rs.getBigDecimal("SoTienDaThanhToan"), rs.getBigDecimal("SoTienConLai"),
                        rs.getTimestamp("NgayPhatSinh").toLocalDateTime(), rs.getString("LyDo"), rs.getString("TrangThai")));
    }

    public PageResponse<ReaderTransactionResponse> findTransactions(String readerId, int page, int pageSize) {
        MapSqlParameterSource params = new MapSqlParameterSource("readerId", readerId)
                .addValue("offset", (page - 1) * pageSize).addValue("pageSize", pageSize);
        String events = """
                SELECT 'LOAN' AS Loai, pm.MaPhieuMuon AS Ma, pm.NgayMuon AS ThoiGian,
                       pm.TrangThai, CAST(NULL AS DECIMAL(18,2)) AS SoTien, N'Phiếu mượn' AS MoTa
                FROM PHIEUMUON pm WHERE pm.MaDocGia = :readerId
                UNION ALL
                SELECT 'RETURN', pt.MaPhieuTra, pt.NgayTra, N'Đã nhận', CAST(NULL AS DECIMAL(18,2)), N'Phiếu trả'
                FROM PHIEUTRA pt WHERE pt.MaDocGia = :readerId
                UNION ALL
                SELECT 'PAYMENT', pthu.MaPhieuThu, pthu.NgayThu, pthu.TrangThai, pthu.SoTienThu, pthu.LoaiThu
                FROM PHIEUTHU pthu WHERE pthu.MaDocGia = :readerId
                UNION ALL
                SELECT 'MEMBERSHIP', lsg.MaLichSuGoi, CAST(lsg.NgayBatDau AS DATETIME2), lsg.TrangThai,
                       CAST(NULL AS DECIMAL(18,2)), N'Lịch sử gói thành viên'
                FROM LICHSUGOITHANHVIEN lsg WHERE lsg.MaDocGia = :readerId
                """;
        Long total = jdbcTemplate.queryForObject("SELECT COUNT_BIG(*) FROM (" + events + ") e", params, Long.class);
        long totalItems = total == null ? 0 : total;
        List<ReaderTransactionResponse> items = jdbcTemplate.query("SELECT * FROM (" + events + ") e " +
                        "ORDER BY e.ThoiGian DESC, e.Ma DESC OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY",
                params, (rs, rowNum) -> new ReaderTransactionResponse(rs.getString("Loai"), rs.getString("Ma"),
                        rs.getTimestamp("ThoiGian").toLocalDateTime(), rs.getString("TrangThai"),
                        rs.getBigDecimal("SoTien"), rs.getString("MoTa")));
        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + pageSize - 1) / pageSize);
        return new PageResponse<>(items, page, pageSize, totalItems, totalPages);
    }

    public List<String> findMatchingIds(ReaderListQuery query, LocalDate today, List<String> excludedIds) {
        return findMatchingIds(query, today, excludedIds, null);
    }

    public List<String> findMatchingIds(ReaderListQuery query, LocalDate today, List<String> excludedIds, Integer maxRows) {
        MapSqlParameterSource params = baseParams(query, today);
        List<String> filterConditions = conditions(query, today, params);
        List<String> allConditions = new ArrayList<>(filterConditions);
        if (excludedIds != null && !excludedIds.isEmpty()) {
            allConditions.add("dg.MaDocGia NOT IN (:excludedIds)");
            params.addValue("excludedIds", excludedIds);
        }
        String where = allConditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", allConditions);
        String top = "";
        if (maxRows != null) {
            top = "TOP (:maxRows) ";
            params.addValue("maxRows", maxRows);
        }
        return jdbcTemplate.queryForList(
                "SELECT " + top + "dg.MaDocGia " + LIST_CORE_FROM + where + " ORDER BY dg.MaDocGia ASC",
                params,
                String.class
        );
    }

    public List<ReaderListItemResponse> findListItemsByIds(List<String> readerIds, LocalDate today) {
        if (readerIds == null || readerIds.isEmpty()) return List.of();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("today", today)
                .addValue("readerIds", readerIds);
        return jdbcTemplate.query("""
                SELECT dg.MaDocGia, dg.HoTen, dg.MaNhomDocGia, ndg.TenNhomDocGia,
                       dg.TrangThai AS TrangThaiHoSo, dg.NgayLapThe, dg.NgayHetHanThe,
                       gm.MaGoiThanhVien, gm.TenGoi, gm.NgayKetThuc,
                       CASE WHEN login_lock.DangKhoaDangNhap = 1 THEN N'KhĂ³a Ä‘Äƒng nháº­p' ELSE tk.TrangThai END AS TrangThaiTaiKhoan,
                       COALESCE(loan_summary.SoSachDangMuon, 0) AS SoSachDangMuon,
                       COALESCE(debt_summary.TongNo, 0) AS TongNo
                """ + LIST_FROM + """
                WHERE dg.MaDocGia IN (:readerIds)
                ORDER BY dg.MaDocGia ASC
                """, params, (rs, rowNum) -> new ReaderListItemResponse(
                rs.getString("MaDocGia"), rs.getString("HoTen"), rs.getString("MaNhomDocGia"),
                rs.getString("TenNhomDocGia"), rs.getString("TrangThaiHoSo"),
                date(rs.getDate("NgayLapThe")), date(rs.getDate("NgayHetHanThe")),
                cardStatus(date(rs.getDate("NgayHetHanThe")), today),
                rs.getString("MaGoiThanhVien"), rs.getString("TenGoi"), date(rs.getDate("NgayKetThuc")),
                membershipStatus(date(rs.getDate("NgayKetThuc")), today),
                rs.getString("TrangThaiTaiKhoan"), rs.getLong("SoSachDangMuon"), rs.getBigDecimal("TongNo")
        ));
    }

    static String cardStatus(LocalDate expiry, LocalDate today) {
        if (expiry == null || expiry.isBefore(today)) return "EXPIRED";
        return expiry.isAfter(today.plusDays(30)) ? "VALID" : "EXPIRING";
    }

    static String membershipStatus(LocalDate expiry, LocalDate today) {
        if (expiry == null) return "NONE";
        if (expiry.isBefore(today)) return "EXPIRED";
        return expiry.isAfter(today.plusDays(30)) ? "VALID" : "EXPIRING";
    }

    private MapSqlParameterSource baseParams(ReaderListQuery query, LocalDate today) {
        return new MapSqlParameterSource().addValue("today", today).addValue("soon", today.plusDays(30));
    }

    private List<String> conditions(ReaderListQuery query, LocalDate today, MapSqlParameterSource params) {
        List<String> result = new ArrayList<>();
        if (query.search() != null && !query.search().isBlank()) {
            params.addValue("search", "%" + escapeLike(query.search().trim()) + "%");
            result.add("(dg.MaDocGia LIKE :search ESCAPE '\\' OR dg.HoTen COLLATE Vietnamese_100_CI_AI LIKE :search ESCAPE '\\' OR dg.Email LIKE :search ESCAPE '\\' OR dg.SoDienThoai LIKE :search ESCAPE '\\' OR tk.TenDangNhap LIKE :search ESCAPE '\\')");
        }
        addIn(result, params, "dg.MaNhomDocGia", "groupIds", query.groupIds());
        addIn(result, params, "gm.MaGoiThanhVien", "planIds", query.planIds());
        addIn(result, params, "dg.TrangThai", "profileStatuses", query.profileStatuses());
        addIn(result, params, "tk.TrangThai", "accountStatuses", query.accountStatuses());
        addStatus(result, "dg.NgayHetHanThe", query.cardStatus(), false);
        addStatus(result, "gm.NgayKetThuc", query.membershipStatus(), true);
        addRange(result, params, "dg.NgayHetHanThe", "cardExpiry", query.cardExpiryFrom(), query.cardExpiryTo());
        addRange(result, params, "gm.NgayKetThuc", "membershipExpiry", query.membershipExpiryFrom(), query.membershipExpiryTo());
        if (Boolean.TRUE.equals(query.locked())) result.add("EXISTS (SELECT 1 FROM DOCGIA_KHOA dk WHERE dk.MaDocGia = dg.MaDocGia AND dk.MoKhoaLuc IS NULL AND (dk.KhoaDen IS NULL OR dk.KhoaDen >= :today))");
        return result;
    }

    private void addStatus(List<String> conditions, String column, String status, boolean nullable) {
        if (status == null || status.isBlank()) return;
        switch (status) {
            case "VALID" -> conditions.add(column + " > :soon");
            case "EXPIRING" -> conditions.add(column + " BETWEEN :today AND :soon");
            case "EXPIRED" -> conditions.add(column + " < :today");
            case "NONE" -> { if (nullable) conditions.add(column + " IS NULL"); }
            default -> throw new IllegalArgumentException("Trạng thái hạn không hợp lệ: " + status);
        }
    }

    private void addRange(List<String> conditions, MapSqlParameterSource params, String column, String prefix, LocalDate from, LocalDate to) {
        if (from != null) { conditions.add(column + " >= :" + prefix + "From"); params.addValue(prefix + "From", from); }
        if (to != null) { conditions.add(column + " <= :" + prefix + "To"); params.addValue(prefix + "To", to); }
    }

    private void addIn(List<String> conditions, MapSqlParameterSource params, String column, String name, List<String> values) {
        if (values != null && !values.isEmpty()) { conditions.add(column + " IN (:" + name + ")"); params.addValue(name, values); }
    }

    static String parseSort(String sort) {
        String[] parts = (sort == null ? "fullName,asc" : sort).split(",", -1);
        if (parts.length != 2 || !SORT_COLUMNS.containsKey(parts[0]) || !(parts[1].equals("asc") || parts[1].equals("desc"))) {
            throw new IllegalArgumentException("Sort độc giả không hợp lệ");
        }
        return SORT_COLUMNS.get(parts[0]) + " " + parts[1].toUpperCase() + ", dg.MaDocGia ASC";
    }

    private static LocalDate date(Date value) { return value == null ? null : value.toLocalDate(); }
    private String escapeLike(String value) { return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_"); }
}

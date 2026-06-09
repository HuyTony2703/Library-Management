package com.library.backend.service.reader;

import com.library.backend.dto.reader.ReaderRecommendationBookResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ReaderRecommendationService {

    private static final String BOOK_ACTIVE = "Hoạt động";
    private static final String COPY_AVAILABLE = "TT_SANCO";
    private static final String RATING_VISIBLE = "Hiển thị";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JdbcTemplate jdbcTemplate;

    public ReaderRecommendationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReaderRecommendationBookResponse> getRandomRecommendations(
            String maDocGia,
            Integer limit
    ) {
        return getRecommendations(maDocGia, "random", limit);
    }

    public List<ReaderRecommendationBookResponse> getRecommendations(
            String maDocGia,
            String type,
            Integer limit
    ) {
        int safeLimit = normalizeLimit(limit);
        String normalizedType = normalizeType(type);
        String orderBy = getOrderBy(normalizedType);

        String sql = """
                SELECT TOP (%d)
                    ds.MaDauSach,
                    ds.TenDauSach,
                    ds.AnhBia,
                    ds.NamXuatBan,
                    ds.TriGia,
                    ds.TrangThai,
                    COALESCE(copyStats.TongSoCuon, 0) AS TongSoCuon,
                    COALESCE(copyStats.SoCuonSanCo, 0) AS SoCuonSanCo,
                    copyStats.NgayNhapGanNhat,
                    COALESCE(borrowStats.SoLuotMuon, 0) AS SoLuotMuon,
                    COALESCE(favoriteStats.SoLuotYeuThich, 0) AS SoLuotYeuThich,
                    COALESCE(ratingStats.DiemTrungBinh, 0) AS DiemTrungBinh,
                    COALESCE(ratingStats.TongSoDanhGia, 0) AS TongSoDanhGia,
                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM SACHYEUTHICH syt
                            WHERE syt.MaDocGia = ?
                              AND syt.MaDauSach = ds.MaDauSach
                        )
                        THEN 1
                        ELSE 0
                    END AS DaYeuThich
                FROM DAUSACH ds
                OUTER APPLY (
                    SELECT
                        COUNT(*) AS TongSoCuon,
                        SUM(CASE WHEN cs.MaTrangThai = ? THEN 1 ELSE 0 END) AS SoCuonSanCo,
                        MAX(cs.NgayNhapSach) AS NgayNhapGanNhat
                    FROM CUONSACH cs
                    WHERE cs.MaDauSach = ds.MaDauSach
                ) copyStats
                OUTER APPLY (
                    SELECT COUNT(*) AS SoLuotMuon
                    FROM CHITIETPHIEUMUON ctm
                    INNER JOIN CUONSACH csBorrow
                        ON ctm.MaCuonSach = csBorrow.MaCuonSach
                    WHERE csBorrow.MaDauSach = ds.MaDauSach
                      AND ctm.NgayMuon >= DATEADD(DAY, -90, SYSDATETIME())
                ) borrowStats
                OUTER APPLY (
                    SELECT COUNT(*) AS SoLuotYeuThich
                    FROM SACHYEUTHICH sytAll
                    WHERE sytAll.MaDauSach = ds.MaDauSach
                ) favoriteStats
                OUTER APPLY (
                    SELECT
                        CAST(ISNULL(AVG(CAST(dg.SoSao AS DECIMAL(10,2))), 0) AS DECIMAL(10,2)) AS DiemTrungBinh,
                        COUNT(*) AS TongSoDanhGia
                    FROM DANHGIA dg
                    WHERE dg.MaDauSach = ds.MaDauSach
                      AND dg.TrangThai = ?
                ) ratingStats
                WHERE ds.TrangThai = ?
                ORDER BY %s
                """.formatted(safeLimit, orderBy);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRecommendation(rs, normalizedType),
                maDocGia,
                COPY_AVAILABLE,
                RATING_VISIBLE,
                BOOK_ACTIVE
        );
    }

    private ReaderRecommendationBookResponse mapRecommendation(
            ResultSet rs,
            String type
    ) throws SQLException {
        return new ReaderRecommendationBookResponse(
                rs.getString("MaDauSach"),
                rs.getString("TenDauSach"),
                rs.getString("AnhBia"),
                rs.getObject("NamXuatBan") == null ? null : rs.getInt("NamXuatBan"),
                rs.getBigDecimal("TriGia"),
                rs.getString("TrangThai"),
                rs.getInt("SoCuonSanCo"),
                rs.getInt("TongSoCuon"),
                rs.getBoolean("DaYeuThich"),
                buildHighlight(rs, type)
        );
    }

    private String buildHighlight(ResultSet rs, String type) throws SQLException {
        if ("trending".equals(type)) {
            int borrowCount = rs.getInt("SoLuotMuon");
            return borrowCount + " lượt mượn gần đây";
        }

        if ("favorite".equals(type)) {
            int favoriteCount = rs.getInt("SoLuotYeuThich");
            return favoriteCount + " lượt yêu thích";
        }

        if ("top-rated".equals(type)) {
            BigDecimal score = rs.getBigDecimal("DiemTrungBinh");
            int ratingCount = rs.getInt("TongSoDanhGia");
            String formattedScore = score == null
                    ? "0"
                    : score.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();

            return formattedScore + " sao · " + ratingCount + " đánh giá";
        }

        Date importedDate = rs.getDate("NgayNhapGanNhat");
        if (importedDate != null) {
            return "Mới nhập " + importedDate.toLocalDate().format(DATE_FORMATTER);
        }

        return "Sách đang hoạt động";
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "new";
        }

        String normalized = type.trim().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "new", "trending", "favorite", "top-rated", "random" -> normalized;
            default -> "new";
        };
    }

    private String getOrderBy(String type) {
        String availableFirst = """
                CASE
                    WHEN COALESCE(copyStats.SoCuonSanCo, 0) > 0 THEN 0
                    ELSE 1
                END
                """;

        return switch (type) {
            case "trending" -> availableFirst + """
                    ,
                    COALESCE(borrowStats.SoLuotMuon, 0) DESC,
                    COALESCE(favoriteStats.SoLuotYeuThich, 0) DESC,
                    ds.TenDauSach ASC
                    """;
            case "favorite" -> availableFirst + """
                    ,
                    COALESCE(favoriteStats.SoLuotYeuThich, 0) DESC,
                    COALESCE(ratingStats.DiemTrungBinh, 0) DESC,
                    ds.TenDauSach ASC
                    """;
            case "top-rated" -> availableFirst + """
                    ,
                    COALESCE(ratingStats.DiemTrungBinh, 0) DESC,
                    COALESCE(ratingStats.TongSoDanhGia, 0) DESC,
                    ds.TenDauSach ASC
                    """;
            case "random" -> availableFirst + ", NEWID()";
            default -> availableFirst + """
                    ,
                    copyStats.NgayNhapGanNhat DESC,
                    ds.NamXuatBan DESC,
                    ds.TenDauSach ASC
                    """;
        };
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 12;
        }

        if (limit > 24) {
            return 24;
        }

        return limit;
    }
}

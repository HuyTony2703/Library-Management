package com.library.backend.service.reader;

import com.library.backend.dto.reader.ReaderRecommendationBookResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReaderRecommendationService {

    private static final String BOOK_ACTIVE = "Hoạt động";
    private static final String COPY_AVAILABLE = "TT_SANCO";

    private final JdbcTemplate jdbcTemplate;

    public ReaderRecommendationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReaderRecommendationBookResponse> getRandomRecommendations(
            String maDocGia,
            Integer limit
    ) {
        int safeLimit = normalizeLimit(limit);

        String sql = """
                SELECT TOP (%d)
                    ds.MaDauSach,
                    ds.TenDauSach,
                    ds.AnhBia,
                    ds.NamXuatBan,
                    ds.TriGia,
                    ds.TrangThai,
                    COUNT(cs.MaCuonSach) AS TongSoCuon,
                    SUM(CASE WHEN cs.MaTrangThai = ? THEN 1 ELSE 0 END) AS SoCuonSanCo,
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
                LEFT JOIN CUONSACH cs
                    ON ds.MaDauSach = cs.MaDauSach
                WHERE ds.TrangThai = ?
                GROUP BY
                    ds.MaDauSach,
                    ds.TenDauSach,
                    ds.AnhBia,
                    ds.NamXuatBan,
                    ds.TriGia,
                    ds.TrangThai
                ORDER BY
                    CASE
                        WHEN SUM(CASE WHEN cs.MaTrangThai = ? THEN 1 ELSE 0 END) > 0
                        THEN 0
                        ELSE 1
                    END,
                    NEWID()
                """.formatted(safeLimit);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReaderRecommendationBookResponse(
                        rs.getString("MaDauSach"),
                        rs.getString("TenDauSach"),
                        rs.getString("AnhBia"),
                        rs.getObject("NamXuatBan") == null ? null : rs.getInt("NamXuatBan"),
                        rs.getBigDecimal("TriGia"),
                        rs.getString("TrangThai"),
                        rs.getInt("SoCuonSanCo"),
                        rs.getInt("TongSoCuon"),
                        rs.getBoolean("DaYeuThich")
                ),
                COPY_AVAILABLE,
                maDocGia,
                BOOK_ACTIVE,
                COPY_AVAILABLE
        );
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 6;
        }

        if (limit > 20) {
            return 20;
        }

        return limit;
    }
}

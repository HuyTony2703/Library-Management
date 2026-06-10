package com.library.backend.service.reader;

import com.library.backend.dto.reader.FavoriteBookResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReaderFavoriteService {

    private static final String BOOK_ACTIVE = "Hoạt động";
    private static final String COPY_AVAILABLE = "TT_SANCO";

    private final JdbcTemplate jdbcTemplate;

    public ReaderFavoriteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FavoriteBookResponse> getMyFavorites(String maDocGia) {
        String sql = """
                SELECT
                    syt.MaYeuThich,
                    syt.MaDocGia,
                    ds.MaDauSach,
                    ds.TenDauSach,
                    ds.AnhBia,
                    ds.NamXuatBan,
                    ds.TriGia,
                    ds.TrangThai AS TrangThaiDauSach,
                    syt.NgayThem,
                    (
                        SELECT COUNT(*)
                        FROM CUONSACH cs
                        WHERE cs.MaDauSach = ds.MaDauSach
                          AND cs.MaTrangThai = ?
                    ) AS SoCuonSanCo
                FROM SACHYEUTHICH syt
                INNER JOIN DAUSACH ds
                    ON syt.MaDauSach = ds.MaDauSach
                WHERE syt.MaDocGia = ?
                ORDER BY syt.NgayThem DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapFavorite(rs),
                COPY_AVAILABLE,
                maDocGia
        );
    }

    public boolean existsFavorite(String maDocGia, String maDauSach) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM SACHYEUTHICH
                WHERE MaDocGia = ?
                  AND MaDauSach = ?
                """,
                Integer.class,
                maDocGia,
                maDauSach
        );

        return count != null && count > 0;
    }

    @Transactional
    public FavoriteBookResponse addFavorite(String maDocGia, String maDauSach) {
        validateBookExists(maDauSach);

        if (existsFavorite(maDocGia, maDauSach)) {
            return getFavoriteByBook(maDocGia, maDauSach);
        }

        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO SACHYEUTHICH
                    (
                        MaYeuThich,
                        MaDocGia,
                        MaDauSach,
                        NgayThem
                    )
                    VALUES (?, ?, ?, SYSDATETIME())
                    """,
                    generateId("YT"),
                    maDocGia,
                    maDauSach
            );
        } catch (DuplicateKeyException ex) {
            throw new BusinessException("Sách này đã có trong danh sách yêu thích");
        }

        return getFavoriteByBook(maDocGia, maDauSach);
    }

    @Transactional
    public void removeFavorite(String maDocGia, String maDauSach) {
        int updated = jdbcTemplate.update(
                """
                DELETE FROM SACHYEUTHICH
                WHERE MaDocGia = ?
                  AND MaDauSach = ?
                """,
                maDocGia,
                maDauSach
        );

        if (updated == 0) {
            throw new ResourceNotFoundException("Sách chưa có trong danh sách yêu thích");
        }
    }

    private FavoriteBookResponse getFavoriteByBook(String maDocGia, String maDauSach) {
        String sql = """
                SELECT
                    syt.MaYeuThich,
                    syt.MaDocGia,
                    ds.MaDauSach,
                    ds.TenDauSach,
                    ds.AnhBia,
                    ds.NamXuatBan,
                    ds.TriGia,
                    ds.TrangThai AS TrangThaiDauSach,
                    syt.NgayThem,
                    (
                        SELECT COUNT(*)
                        FROM CUONSACH cs
                        WHERE cs.MaDauSach = ds.MaDauSach
                          AND cs.MaTrangThai = ?
                    ) AS SoCuonSanCo
                FROM SACHYEUTHICH syt
                INNER JOIN DAUSACH ds
                    ON syt.MaDauSach = ds.MaDauSach
                WHERE syt.MaDocGia = ?
                  AND syt.MaDauSach = ?
                """;

        List<FavoriteBookResponse> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapFavorite(rs),
                COPY_AVAILABLE,
                maDocGia,
                maDauSach
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sách yêu thích");
        }

        return result.get(0);
    }

    private void validateBookExists(String maDauSach) {
        if (maDauSach == null || maDauSach.isBlank()) {
            throw new BusinessException("Mã đầu sách không được để trống");
        }

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM DAUSACH
                WHERE MaDauSach = ?
                  AND TrangThai = ?
                """,
                Integer.class,
                maDauSach,
                BOOK_ACTIVE
        );

        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Đầu sách không tồn tại hoặc đã ngừng hoạt động");
        }
    }

    private FavoriteBookResponse mapFavorite(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new FavoriteBookResponse(
                rs.getString("MaYeuThich"),
                rs.getString("MaDocGia"),
                rs.getString("MaDauSach"),
                rs.getString("TenDauSach"),
                rs.getString("AnhBia"),
                rs.getObject("NamXuatBan") == null ? null : rs.getInt("NamXuatBan"),
                rs.getBigDecimal("TriGia"),
                rs.getString("TrangThaiDauSach"),
                rs.getInt("SoCuonSanCo"),
                toLocalDateTime(rs.getTimestamp("NgayThem"))
        );
    }

    private String generateId(String prefix) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return prefix + time + random;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}

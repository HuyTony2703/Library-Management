package com.library.backend.service.reader;

import com.library.backend.dto.reader.RatingRequest;
import com.library.backend.dto.reader.RatingSummaryResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReaderRatingService {

    private static final String RATING_VISIBLE = "Hiển thị";
    private static final String BOOK_ACTIVE = "Hoạt động";

    private final JdbcTemplate jdbcTemplate;

    public ReaderRatingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RatingSummaryResponse getRatingSummary(String maDauSach, String maDocGia) {
        validateBookExists(maDauSach);

        RatingAggregate aggregate = getAggregate(maDauSach);
        MyRating myRating = getMyRating(maDauSach, maDocGia);

        return new RatingSummaryResponse(
                maDauSach,
                aggregate.diemTrungBinh(),
                aggregate.tongSoDanhGia(),
                aggregate.soSao1(),
                aggregate.soSao2(),
                aggregate.soSao3(),
                aggregate.soSao4(),
                aggregate.soSao5(),
                myRating == null ? null : myRating.maDanhGia(),
                myRating == null ? null : myRating.soSao(),
                myRating == null ? null : myRating.noiDung()
        );
    }

    @Transactional
    public RatingSummaryResponse createRating(String maDocGia, String maDauSach, RatingRequest request) {
        validateBookExists(maDauSach);
        validateRatingRequest(request);

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM DANHGIA
                WHERE MaDocGia = ?
                  AND MaDauSach = ?
                """,
                Integer.class,
                maDocGia,
                maDauSach
        );

        if (count != null && count > 0) {
            throw new BusinessException("Bạn đã đánh giá đầu sách này. Hãy dùng chức năng cập nhật đánh giá.");
        }

        jdbcTemplate.update(
                """
                INSERT INTO DANHGIA
                (
                    MaDanhGia,
                    MaDocGia,
                    MaDauSach,
                    SoSao,
                    NoiDung,
                    NgayDanhGia,
                    TrangThai
                )
                VALUES (?, ?, ?, ?, ?, SYSDATETIME(), ?)
                """,
                generateId("DGIA"),
                maDocGia,
                maDauSach,
                request.getSoSao(),
                cleanText(request.getNoiDung()),
                RATING_VISIBLE
        );

        return getRatingSummary(maDauSach, maDocGia);
    }

    @Transactional
    public RatingSummaryResponse updateMyRating(String maDocGia, String maDauSach, RatingRequest request) {
        validateBookExists(maDauSach);
        validateRatingRequest(request);

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM DANHGIA
                WHERE MaDocGia = ?
                  AND MaDauSach = ?
                """,
                Integer.class,
                maDocGia,
                maDauSach
        );

        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Bạn chưa đánh giá đầu sách này");
        }

        jdbcTemplate.update(
                """
                UPDATE DANHGIA
                SET SoSao = ?,
                    NoiDung = ?,
                    NgayDanhGia = SYSDATETIME(),
                    TrangThai = ?
                WHERE MaDocGia = ?
                  AND MaDauSach = ?
                """,
                request.getSoSao(),
                cleanText(request.getNoiDung()),
                RATING_VISIBLE,
                maDocGia,
                maDauSach
        );

        return getRatingSummary(maDauSach, maDocGia);
    }

    private RatingAggregate getAggregate(String maDauSach) {
        String sql = """
                SELECT
                    COUNT(*) AS TongSoDanhGia,
                    CAST(ISNULL(AVG(CAST(SoSao AS DECIMAL(10,2))), 0) AS DECIMAL(10,2)) AS DiemTrungBinh,
                    SUM(CASE WHEN SoSao = 1 THEN 1 ELSE 0 END) AS SoSao1,
                    SUM(CASE WHEN SoSao = 2 THEN 1 ELSE 0 END) AS SoSao2,
                    SUM(CASE WHEN SoSao = 3 THEN 1 ELSE 0 END) AS SoSao3,
                    SUM(CASE WHEN SoSao = 4 THEN 1 ELSE 0 END) AS SoSao4,
                    SUM(CASE WHEN SoSao = 5 THEN 1 ELSE 0 END) AS SoSao5
                FROM DANHGIA
                WHERE MaDauSach = ?
                  AND TrangThai = ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new RatingAggregate(
                        rs.getBigDecimal("DiemTrungBinh"),
                        rs.getInt("TongSoDanhGia"),
                        rs.getInt("SoSao1"),
                        rs.getInt("SoSao2"),
                        rs.getInt("SoSao3"),
                        rs.getInt("SoSao4"),
                        rs.getInt("SoSao5")
                ),
                maDauSach,
                RATING_VISIBLE
        );
    }

    private MyRating getMyRating(String maDauSach, String maDocGia) {
        List<MyRating> result = jdbcTemplate.query(
                """
                SELECT TOP 1 MaDanhGia, SoSao, NoiDung
                FROM DANHGIA
                WHERE MaDauSach = ?
                  AND MaDocGia = ?
                ORDER BY NgayDanhGia DESC
                """,
                (rs, rowNum) -> new MyRating(
                        rs.getString("MaDanhGia"),
                        rs.getInt("SoSao"),
                        rs.getString("NoiDung")
                ),
                maDauSach,
                maDocGia
        );

        return result.isEmpty() ? null : result.get(0);
    }

    private void validateBookExists(String maDauSach) {
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
            throw new ResourceNotFoundException("Đầu sách không tồn tại hoặc đã ngừng hiển thị");
        }
    }

    private void validateRatingRequest(RatingRequest request) {
        if (request == null || request.getSoSao() == null) {
            throw new BusinessException("Số sao không được để trống");
        }

        if (request.getSoSao() < 1 || request.getSoSao() > 5) {
            throw new BusinessException("Số sao phải nằm trong khoảng từ 1 đến 5");
        }

        if (request.getNoiDung() != null && request.getNoiDung().trim().length() > 1000) {
            throw new BusinessException("Nội dung đánh giá không được vượt quá 1000 ký tự");
        }
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateId(String prefix) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return prefix + time + random;
    }

    private record RatingAggregate(
            BigDecimal diemTrungBinh,
            int tongSoDanhGia,
            int soSao1,
            int soSao2,
            int soSao3,
            int soSao4,
            int soSao5
    ) {
    }

    private record MyRating(String maDanhGia, Integer soSao, String noiDung) {
    }
}

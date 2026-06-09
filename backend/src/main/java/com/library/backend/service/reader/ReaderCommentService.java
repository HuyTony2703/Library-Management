package com.library.backend.service.reader;

import com.library.backend.dto.reader.CommentRequest;
import com.library.backend.dto.reader.CommentResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReaderCommentService {

    private static final String COMMENT_VISIBLE = "Hiển thị";
    private static final String COMMENT_DELETED = "Đã xóa";
    private static final String BOOK_ACTIVE = "Hoạt động";

    private final JdbcTemplate jdbcTemplate;

    public ReaderCommentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CommentResponse> getComments(String maDauSach, String maDocGiaHienTai) {
        validateBookExists(maDauSach);

        String sql = """
                SELECT
                    bl.MaBinhLuan,
                    bl.MaDocGia,
                    dg.HoTen AS HoTenDocGia,
                    bl.MaDauSach,
                    bl.NoiDung,
                    bl.NgayBinhLuan,
                    bl.TrangThai
                FROM BINHLUAN bl
                INNER JOIN DOCGIA dg
                    ON bl.MaDocGia = dg.MaDocGia
                WHERE bl.MaDauSach = ?
                  AND bl.TrangThai = ?
                ORDER BY bl.NgayBinhLuan DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapComment(rs, maDocGiaHienTai),
                maDauSach,
                COMMENT_VISIBLE
        );
    }

    @Transactional
    public CommentResponse createComment(String maDocGia, String maDauSach, CommentRequest request) {
        validateBookExists(maDauSach);
        validateCommentRequest(request);

        String maBinhLuan = generateId("BL");

        jdbcTemplate.update(
                """
                INSERT INTO BINHLUAN
                (
                    MaBinhLuan,
                    MaDocGia,
                    MaDauSach,
                    NoiDung,
                    NgayBinhLuan,
                    TrangThai
                )
                VALUES (?, ?, ?, ?, SYSDATETIME(), ?)
                """,
                maBinhLuan,
                maDocGia,
                maDauSach,
                request.getNoiDung().trim(),
                COMMENT_VISIBLE
        );

        return getCommentById(maBinhLuan, maDocGia);
    }

    @Transactional
    public CommentResponse updateComment(String maDocGia, String maBinhLuan, CommentRequest request) {
        validateCommentRequest(request);

        CommentOwnerInfo info = getCommentOwnerInfo(maBinhLuan);

        if (!maDocGia.equals(info.maDocGia())) {
            throw new BusinessException("Bạn chỉ được sửa bình luận của chính mình");
        }

        if (!COMMENT_VISIBLE.equals(info.trangThai())) {
            throw new BusinessException("Chỉ được sửa bình luận đang hiển thị");
        }

        jdbcTemplate.update(
                """
                UPDATE BINHLUAN
                SET NoiDung = ?
                WHERE MaBinhLuan = ?
                  AND MaDocGia = ?
                """,
                request.getNoiDung().trim(),
                maBinhLuan,
                maDocGia
        );

        return getCommentById(maBinhLuan, maDocGia);
    }

    @Transactional
    public void deleteComment(String maDocGia, String maBinhLuan) {
        CommentOwnerInfo info = getCommentOwnerInfo(maBinhLuan);

        if (!maDocGia.equals(info.maDocGia())) {
            throw new BusinessException("Bạn chỉ được xóa bình luận của chính mình");
        }

        if (!COMMENT_VISIBLE.equals(info.trangThai())) {
            throw new BusinessException("Chỉ được xóa bình luận đang hiển thị");
        }

        jdbcTemplate.update(
                """
                UPDATE BINHLUAN
                SET TrangThai = ?,
                    LyDoAnXoa = N'Độc giả tự xóa bình luận'
                WHERE MaBinhLuan = ?
                  AND MaDocGia = ?
                """,
                COMMENT_DELETED,
                maBinhLuan,
                maDocGia
        );
    }

    private CommentResponse getCommentById(String maBinhLuan, String maDocGiaHienTai) {
        String sql = """
                SELECT
                    bl.MaBinhLuan,
                    bl.MaDocGia,
                    dg.HoTen AS HoTenDocGia,
                    bl.MaDauSach,
                    bl.NoiDung,
                    bl.NgayBinhLuan,
                    bl.TrangThai
                FROM BINHLUAN bl
                INNER JOIN DOCGIA dg
                    ON bl.MaDocGia = dg.MaDocGia
                WHERE bl.MaBinhLuan = ?
                """;

        List<CommentResponse> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapComment(rs, maDocGiaHienTai),
                maBinhLuan
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy bình luận");
        }

        return result.get(0);
    }

    private CommentOwnerInfo getCommentOwnerInfo(String maBinhLuan) {
        List<CommentOwnerInfo> result = jdbcTemplate.query(
                """
                SELECT MaBinhLuan, MaDocGia, TrangThai
                FROM BINHLUAN
                WHERE MaBinhLuan = ?
                """,
                (rs, rowNum) -> new CommentOwnerInfo(
                        rs.getString("MaBinhLuan"),
                        rs.getString("MaDocGia"),
                        rs.getString("TrangThai")
                ),
                maBinhLuan
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy bình luận");
        }

        return result.get(0);
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

    private void validateCommentRequest(CommentRequest request) {
        if (request == null || request.getNoiDung() == null || request.getNoiDung().isBlank()) {
            throw new BusinessException("Nội dung bình luận không được để trống");
        }

        if (request.getNoiDung().trim().length() > 1000) {
            throw new BusinessException("Nội dung bình luận không được vượt quá 1000 ký tự");
        }
    }

    private CommentResponse mapComment(java.sql.ResultSet rs, String maDocGiaHienTai)
            throws java.sql.SQLException {
        return new CommentResponse(
                rs.getString("MaBinhLuan"),
                rs.getString("MaDocGia"),
                rs.getString("HoTenDocGia"),
                rs.getString("MaDauSach"),
                rs.getString("NoiDung"),
                toLocalDateTime(rs.getTimestamp("NgayBinhLuan")),
                rs.getString("TrangThai"),
                rs.getString("MaDocGia").equals(maDocGiaHienTai)
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

    private record CommentOwnerInfo(String maBinhLuan, String maDocGia, String trangThai) {
    }
}

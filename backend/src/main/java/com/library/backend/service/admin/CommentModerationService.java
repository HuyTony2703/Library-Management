package com.library.backend.service.admin;

import com.library.backend.dto.CommentModerationResponse;
import com.library.backend.dto.ModerateCommentRequest;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.service.ActivityLogService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentModerationService {

    private static final String TT_HIEN_THI = "Hiển thị";
    private static final String TT_DA_AN = "Đã ẩn";
    private static final String TT_DA_XOA = "Đã xóa";
    private static final String STATUS_ALL = "Tất cả";

    private final JdbcTemplate jdbcTemplate;
    private final ActivityLogService activityLogService;

    public CommentModerationService(
            JdbcTemplate jdbcTemplate,
            ActivityLogService activityLogService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.activityLogService = activityLogService;
    }

    public List<CommentModerationResponse> getComments(
            String status,
            String maDauSach,
            String keyword
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    bl.MaBinhLuan,
                    bl.MaDocGia,
                    dg.HoTen AS HoTenDocGia,
                    bl.MaDauSach,
                    ds.TenDauSach,
                    bl.NoiDung,
                    bl.NgayBinhLuan,
                    bl.TrangThai,
                    bl.MaNhanVienXuLy,
                    nv.HoTen AS HoTenNhanVienXuLy,
                    bl.LyDoAnXoa
                FROM BINHLUAN bl
                INNER JOIN DOCGIA dg ON bl.MaDocGia = dg.MaDocGia
                INNER JOIN DAUSACH ds ON bl.MaDauSach = ds.MaDauSach
                LEFT JOIN NHANVIEN nv ON bl.MaNhanVienXuLy = nv.MaNhanVien
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (!isBlank(status) && !STATUS_ALL.equals(status)) {
            validateStatus(status);
            sql.append(" AND bl.TrangThai = ? ");
            params.add(status);
        }

        if (!isBlank(maDauSach)) {
            sql.append(" AND bl.MaDauSach = ? ");
            params.add(maDauSach.trim());
        }

        if (!isBlank(keyword)) {
            sql.append("""
                    AND (
                        bl.NoiDung LIKE ?
                        OR dg.HoTen LIKE ?
                        OR ds.TenDauSach LIKE ?
                    )
                    """);
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        sql.append(" ORDER BY bl.NgayBinhLuan DESC ");

        return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
    }

    public CommentModerationResponse getById(String maBinhLuan) {
        String sql = """
                SELECT
                    bl.MaBinhLuan,
                    bl.MaDocGia,
                    dg.HoTen AS HoTenDocGia,
                    bl.MaDauSach,
                    ds.TenDauSach,
                    bl.NoiDung,
                    bl.NgayBinhLuan,
                    bl.TrangThai,
                    bl.MaNhanVienXuLy,
                    nv.HoTen AS HoTenNhanVienXuLy,
                    bl.LyDoAnXoa
                FROM BINHLUAN bl
                INNER JOIN DOCGIA dg ON bl.MaDocGia = dg.MaDocGia
                INNER JOIN DAUSACH ds ON bl.MaDauSach = ds.MaDauSach
                LEFT JOIN NHANVIEN nv ON bl.MaNhanVienXuLy = nv.MaNhanVien
                WHERE bl.MaBinhLuan = ?
                """;

        List<CommentModerationResponse> result = jdbcTemplate.query(sql, this::mapRow, maBinhLuan);

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy bình luận: " + maBinhLuan);
        }

        return result.get(0);
    }

    @Transactional
    public CommentModerationResponse hide(String maBinhLuan, ModerateCommentRequest request) {
        return moderate(maBinhLuan, request, TT_DA_AN, "Ẩn bình luận", true);
    }

    @Transactional
    public CommentModerationResponse delete(String maBinhLuan, ModerateCommentRequest request) {
        if (request == null) {
            throw new BusinessException("Thiếu dữ liệu xử lý bình luận");
        }

        CommentModerationResponse current = getById(maBinhLuan);
        validateEmployee(request.getMaNhanVienXuLy());

        if (isBlank(request.getLyDoAnXoa())) {
            throw new BusinessException("Vui lòng nhập lý do xử lý bình luận");
        }

        jdbcTemplate.update(
                "DELETE FROM BINHLUAN WHERE MaBinhLuan = ?",
                maBinhLuan
        );

        activityLogService.logSafe(
                "Xóa bình luận",
                "BINHLUAN",
                maBinhLuan,
                "Xóa vĩnh viễn bình luận " + maBinhLuan
                        + " bởi nhân viên " + request.getMaNhanVienXuLy().trim()
                        + ". Lý do: " + request.getLyDoAnXoa().trim()
        );

        return current;
    }

    @Transactional
    public CommentModerationResponse restore(String maBinhLuan, ModerateCommentRequest request) {
        return moderate(maBinhLuan, request, TT_HIEN_THI, "Khôi phục bình luận", false);
    }

    private CommentModerationResponse moderate(
            String maBinhLuan,
            ModerateCommentRequest request,
            String newStatus,
            String action,
            boolean requireReason
    ) {
        if (request == null) {
            throw new BusinessException("Thiếu dữ liệu xử lý bình luận");
        }

        CommentModerationResponse current = getById(maBinhLuan);
        validateEmployee(request.getMaNhanVienXuLy());

        if (requireReason && isBlank(request.getLyDoAnXoa())) {
            throw new BusinessException("Vui lòng nhập lý do xử lý bình luận");
        }

        if (newStatus.equals(current.getTrangThai())) {
            throw new BusinessException("Bình luận đã ở trạng thái: " + newStatus);
        }

        String reasonToSave = TT_HIEN_THI.equals(newStatus) ? null : request.getLyDoAnXoa().trim();

        jdbcTemplate.update(
                """
                UPDATE BINHLUAN
                SET TrangThai = ?,
                    MaNhanVienXuLy = ?,
                    LyDoAnXoa = ?
                WHERE MaBinhLuan = ?
                """,
                newStatus,
                request.getMaNhanVienXuLy().trim(),
                reasonToSave,
                maBinhLuan
        );

        activityLogService.logSafe(
                action,
                "BINHLUAN",
                maBinhLuan,
                action + " " + maBinhLuan
                        + " bởi nhân viên " + request.getMaNhanVienXuLy().trim()
        );

        return getById(maBinhLuan);
    }

    private void validateEmployee(String maNhanVien) {
        if (isBlank(maNhanVien)) {
            throw new BusinessException("Mã nhân viên xử lý không được để trống");
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NHANVIEN WHERE MaNhanVien = ?",
                Integer.class,
                maNhanVien.trim()
        );

        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Nhân viên xử lý không tồn tại");
        }
    }

    private void validateStatus(String status) {
        if (!TT_HIEN_THI.equals(status)
                && !TT_DA_AN.equals(status)
                && !TT_DA_XOA.equals(status)) {
            throw new BusinessException("Trạng thái bình luận không hợp lệ");
        }
    }

    private CommentModerationResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp ngayBinhLuan = rs.getTimestamp("NgayBinhLuan");

        return new CommentModerationResponse(
                rs.getString("MaBinhLuan"),
                rs.getString("MaDocGia"),
                rs.getString("HoTenDocGia"),
                rs.getString("MaDauSach"),
                rs.getString("TenDauSach"),
                rs.getString("NoiDung"),
                toDateTime(ngayBinhLuan),
                rs.getString("TrangThai"),
                rs.getString("MaNhanVienXuLy"),
                rs.getString("HoTenNhanVienXuLy"),
                rs.getString("LyDoAnXoa")
        );
    }

    private LocalDateTime toDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

package com.library.backend.service.reader;

import com.library.backend.dto.reader.ReaderNotificationResponse;
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
public class ReaderNotificationService {

    private final JdbcTemplate jdbcTemplate;

    public ReaderNotificationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReaderNotificationResponse> getMyNotifications(String maTaiKhoan) {
        String sql = """
                SELECT
                    tb.MaThongBao,
                    tb.MaLoaiThongBao,
                    ltb.TenLoaiThongBao,
                    tb.TieuDe,
                    tb.NoiDung,
                    tb.NgayTao,
                    tb.GuiTrongApp,
                    tb.GuiEmail,
                    tb.TrangThaiEmail,
                    tb.SoLanThuGuiEmail,
                    tb.DaDoc,
                    tb.ThoiGianDoc
                FROM THONGBAO tb
                INNER JOIN LOAITHONGBAO ltb
                    ON tb.MaLoaiThongBao = ltb.MaLoaiThongBao
                WHERE tb.MaTaiKhoanNhan = ?
                  AND tb.GuiTrongApp = 1
                ORDER BY tb.DaDoc ASC, tb.NgayTao DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ReaderNotificationResponse(
                rs.getString("MaThongBao"),
                rs.getString("MaLoaiThongBao"),
                rs.getString("TenLoaiThongBao"),
                rs.getString("TieuDe"),
                rs.getString("NoiDung"),
                toLocalDateTime(rs.getTimestamp("NgayTao")),
                rs.getBoolean("GuiTrongApp"),
                rs.getBoolean("GuiEmail"),
                rs.getString("TrangThaiEmail"),
                rs.getInt("SoLanThuGuiEmail"),
                rs.getBoolean("DaDoc"),
                toLocalDateTime(rs.getTimestamp("ThoiGianDoc"))
        ), maTaiKhoan);
    }

    public int countUnread(String maTaiKhoan) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM THONGBAO
                WHERE MaTaiKhoanNhan = ?
                  AND GuiTrongApp = 1
                  AND DaDoc = 0
                """,
                Integer.class,
                maTaiKhoan
        );

        return count == null ? 0 : count;
    }

    @Transactional
    public void markAsRead(String maTaiKhoan, String maThongBao) {
        Integer exists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM THONGBAO
                WHERE MaThongBao = ?
                  AND MaTaiKhoanNhan = ?
                  AND GuiTrongApp = 1
                """,
                Integer.class,
                maThongBao,
                maTaiKhoan
        );

        if (exists == null || exists == 0) {
            throw new ResourceNotFoundException("Không tìm thấy thông báo");
        }

        jdbcTemplate.update(
                """
                UPDATE THONGBAO
                SET DaDoc = 1,
                    ThoiGianDoc = COALESCE(ThoiGianDoc, SYSDATETIME())
                WHERE MaThongBao = ?
                  AND MaTaiKhoanNhan = ?
                """,
                maThongBao,
                maTaiKhoan
        );
    }

    @Transactional
    public int markAllAsRead(String maTaiKhoan) {
        return jdbcTemplate.update(
                """
                UPDATE THONGBAO
                SET DaDoc = 1,
                    ThoiGianDoc = COALESCE(ThoiGianDoc, SYSDATETIME())
                WHERE MaTaiKhoanNhan = ?
                  AND GuiTrongApp = 1
                  AND DaDoc = 0
                """,
                maTaiKhoan
        );
    }

    @Transactional
    public void deleteNotification(String maTaiKhoan, String maThongBao) {
        Integer exists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM THONGBAO
                WHERE MaThongBao = ?
                  AND MaTaiKhoanNhan = ?
                  AND GuiTrongApp = 1
                """,
                Integer.class,
                maThongBao,
                maTaiKhoan
        );

        if (exists == null || exists == 0) {
            throw new ResourceNotFoundException("Không tìm thấy thông báo");
        }

        jdbcTemplate.update(
                """
                UPDATE THONGBAO
                SET GuiTrongApp = 0
                WHERE MaThongBao = ?
                  AND MaTaiKhoanNhan = ?
                """,
                maThongBao,
                maTaiKhoan
        );
    }

    @Transactional
    public String createInAppNotification(
            String maTaiKhoanNhan,
            String maLoaiThongBao,
            String tieuDe,
            String noiDung,
            boolean guiEmail
    ) {
        if (isBlank(maTaiKhoanNhan)) {
            throw new BusinessException("Mã tài khoản nhận không được để trống");
        }

        if (isBlank(maLoaiThongBao)) {
            throw new BusinessException("Mã loại thông báo không được để trống");
        }

        if (!existsNotificationType(maLoaiThongBao)) {
            throw new ResourceNotFoundException("Loại thông báo không tồn tại: " + maLoaiThongBao);
        }

        String maThongBao = generateId("TB");

        jdbcTemplate.update(
                """
                INSERT INTO THONGBAO
                (
                    MaThongBao,
                    MaTaiKhoanNhan,
                    MaLoaiThongBao,
                    TieuDe,
                    NoiDung,
                    NgayTao,
                    GuiTrongApp,
                    GuiEmail,
                    TrangThaiEmail,
                    SoLanThuGuiEmail,
                    DaDoc,
                    ThoiGianDoc
                )
                VALUES
                (
                    ?, ?, ?, ?, ?,
                    SYSDATETIME(),
                    1,
                    ?,
                    ?,
                    0,
                    0,
                    NULL
                )
                """,
                maThongBao,
                maTaiKhoanNhan,
                maLoaiThongBao,
                tieuDe,
                noiDung,
                guiEmail,
                guiEmail ? "Chờ gửi" : "Không gửi"
        );

        return maThongBao;
    }

    private boolean existsNotificationType(String maLoaiThongBao) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM LOAITHONGBAO
                WHERE MaLoaiThongBao = ?
                """,
                Integer.class,
                maLoaiThongBao
        );

        return count != null && count > 0;
    }

    private String generateId(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return prefix + "_" + timestamp + "_" + random;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

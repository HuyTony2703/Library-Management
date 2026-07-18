package com.library.backend.service;

import com.library.backend.dto.ReaderPasswordResetRequest;
import com.library.backend.dto.ReaderPasswordResetResponse;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ReaderPasswordResetService {
    public static final String PERMISSION = "READER_PASSWORD_RESET";
    private static final char[] UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final char[] LOWER = "abcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final char[] DIGITS = "23456789".toCharArray();
    private static final char[] SPECIAL = "!@#$%*-_".toCharArray();
    private static final char[] ALL = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%*-_".toCharArray();

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;
    private final SecureRandom secureRandom;
    private final int windowMinutes;
    private final int maxPerReader;
    private final int maxPerActor;

    @Autowired
    public ReaderPasswordResetService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            ActivityLogService activityLogService,
            @Value("${app.security.reader-password-reset.window-minutes}") int windowMinutes,
            @Value("${app.security.reader-password-reset.max-per-reader}") int maxPerReader,
            @Value("${app.security.reader-password-reset.max-per-actor}") int maxPerActor
    ) {
        this(jdbcTemplate, passwordEncoder, activityLogService, new SecureRandom(), windowMinutes, maxPerReader, maxPerActor);
    }

    ReaderPasswordResetService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder,
                               ActivityLogService activityLogService, SecureRandom secureRandom,
                               int windowMinutes, int maxPerReader, int maxPerActor) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
        this.secureRandom = secureRandom;
        this.windowMinutes = positive(windowMinutes, "window-minutes");
        this.maxPerReader = positive(maxPerReader, "max-per-reader");
        this.maxPerActor = positive(maxPerActor, "max-per-actor");
    }

    @Transactional(noRollbackFor = CatalogValidationException.class)
    public ReaderPasswordResetResponse reset(String readerId, ReaderPasswordResetRequest request, AuthUser actor) {
        requirePermission(actor);
        if (request.mode() != ReaderPasswordResetRequest.Mode.GENERATE_TEMPORARY
                || !Boolean.TRUE.equals(request.forceChange()) || !Boolean.TRUE.equals(request.revokeSessions())) {
            throw validation("mode", "Reset độc giả bắt buộc tạo mật khẩu tạm, force-change và thu hồi phiên cũ");
        }

        ReaderAccount reader = lockReader(readerId);
        LocalDateTime now = LocalDateTime.now();
        enforceRateLimit(readerId, actor.getMaTaiKhoan(), now.minusMinutes(windowMinutes));

        long eventId = insertDeniedAttempt(reader, actor, request, now);
        if (!reader.email().equalsIgnoreCase(request.verificationEmail().trim())
                || !reader.dateOfBirth().equals(request.verificationDateOfBirth())) {
            throw new CatalogValidationException(HttpStatus.BAD_REQUEST, "READER_VERIFICATION_FAILED",
                    "Thông tin xác minh độc giả không khớp", Map.of(), null);
        }

        String temporaryPassword = generateTemporaryPassword();
        int updated = jdbcTemplate.update("""
                UPDATE TAIKHOAN
                SET MatKhauHash = ?, MustChangePassword = 1, PasswordChangedAt = ?, TokenVersion = TokenVersion + 1
                WHERE MaTaiKhoan = ?
                """, passwordEncoder.encode(temporaryPassword), now, reader.accountId());
        if (updated != 1) throw new ResourceNotFoundException("Tài khoản độc giả không tồn tại");

        jdbcTemplate.update("UPDATE DOCGIA_PASSWORD_RESET_EVENT SET KetQua = 'SUCCESS' WHERE MaSuKien = ?", eventId);
        activityLogService.logAsAccountSafe(actor.getMaTaiKhoan(), "Reset mật khẩu độc giả", "TAIKHOAN",
                reader.accountId(), "Reset mật khẩu độc giả " + readerId + "; lý do: " + request.reason().trim());

        return new ReaderPasswordResetResponse(readerId, temporaryPassword, true, true, now);
    }

    private void requirePermission(AuthUser actor) {
        if (actor != null
                && RoleConstants.LIBRARIAN.equals(actor.getTenVaiTro())
                && (actor.getMaNhanVien() == null || actor.getMaNhanVien().isBlank())) {
            throw new AccessDeniedException("Khong co staff profile de reset mat khau doc gia");
        }
        if (actor == null || !(RoleConstants.ADMIN.equals(actor.getTenVaiTro())
                || RoleConstants.LIBRARIAN.equals(actor.getTenVaiTro()))) {
            throw new AccessDeniedException("Không có quyền reset mật khẩu độc giả");
        }
    }

    private ReaderAccount lockReader(String readerId) {
        return jdbcTemplate.query("""
                SELECT dg.MaDocGia, dg.MaTaiKhoan, dg.Email, dg.NgaySinh
                FROM DOCGIA dg WITH (UPDLOCK, HOLDLOCK)
                WHERE dg.MaDocGia = ?
                """, (rs, n) -> new ReaderAccount(rs.getString("MaDocGia"), rs.getString("MaTaiKhoan"),
                rs.getString("Email"), date(rs.getDate("NgaySinh"))), readerId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy độc giả"));
    }

    private void enforceRateLimit(String readerId, String actorId, LocalDateTime cutoff) {
        Integer readerAttempts = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM DOCGIA_PASSWORD_RESET_EVENT WITH (UPDLOCK, HOLDLOCK)
                WHERE MaDocGia = ? AND ThoiGian >= ?
                """, Integer.class, readerId, cutoff);
        Integer actorAttempts = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM DOCGIA_PASSWORD_RESET_EVENT WITH (UPDLOCK, HOLDLOCK)
                WHERE MaTaiKhoanThucHien = ? AND ThoiGian >= ?
                """, Integer.class, actorId, cutoff);
        if ((readerAttempts != null && readerAttempts >= maxPerReader)
                || (actorAttempts != null && actorAttempts >= maxPerActor)) {
            throw new CatalogValidationException(HttpStatus.TOO_MANY_REQUESTS, "PASSWORD_RESET_RATE_LIMITED",
                    "Đã vượt giới hạn reset mật khẩu trong cửa sổ thời gian cấu hình", Map.of(),
                    Map.of("retryWindowMinutes", windowMinutes));
        }
    }

    private long insertDeniedAttempt(ReaderAccount reader, AuthUser actor,
                                     ReaderPasswordResetRequest request, LocalDateTime now) {
        jdbcTemplate.update("""
                INSERT INTO DOCGIA_PASSWORD_RESET_EVENT
                    (MaDocGia, MaTaiKhoan, MaTaiKhoanThucHien, PhuongThucXacMinh, LyDo, KetQua, ThoiGian)
                VALUES (?, ?, ?, 'EMAIL_AND_DOB', ?, 'DENIED', ?)
                """, reader.readerId(), reader.accountId(), actor.getMaTaiKhoan(), request.reason().trim(), now);
        return jdbcTemplate.queryForObject("SELECT CAST(SCOPE_IDENTITY() AS BIGINT)", Long.class);
    }

    private String generateTemporaryPassword() {
        char[] value = new char[16];
        value[0] = random(UPPER); value[1] = random(LOWER); value[2] = random(DIGITS); value[3] = random(SPECIAL);
        for (int i = 4; i < value.length; i++) value[i] = random(ALL);
        for (int i = value.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char current = value[i]; value[i] = value[j]; value[j] = current;
        }
        return new String(value);
    }

    private char random(char[] source) { return source[secureRandom.nextInt(source.length)]; }
    private static int positive(int value, String name) {
        if (value < 1) throw new IllegalArgumentException(name + " phải lớn hơn 0");
        return value;
    }
    private static LocalDate date(Date value) { return value == null ? null : value.toLocalDate(); }
    private static CatalogValidationException validation(String field, String message) {
        return new CatalogValidationException(HttpStatus.BAD_REQUEST, "PASSWORD_RESET_VALIDATION", message,
                Map.of(field, message), null);
    }
    private record ReaderAccount(String readerId, String accountId, String email, LocalDate dateOfBirth) {}
}

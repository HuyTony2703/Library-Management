package com.library.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.backend.dto.BookDeletePreflightResponse;
import com.library.backend.dto.DauSachResponse;
import com.library.backend.entity.DauSach;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DauSachLifecycleService {
    private static final String ACTIVE = "Hoạt động";
    private static final String INACTIVE = "Ngừng hiển thị";
    private static final String OBJECT_TYPE = "DAUSACH";

    private final DauSachRepository dauSachRepository;
    private final DauSachService dauSachService;
    private final ActivityLogService activityLogService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DauSachLifecycleService(
            DauSachRepository dauSachRepository,
            DauSachService dauSachService,
            ActivityLogService activityLogService,
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    ) {
        this.dauSachRepository = dauSachRepository;
        this.dauSachService = dauSachService;
        this.activityLogService = activityLogService;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DauSachResponse deactivate(String bookId, String reason, AuthUser user) {
        requireStaff(user);
        reason = requireReason(reason);
        DauSach book = requireBook(bookId);
        if (INACTIVE.equals(book.getTrangThai())) throw new RuntimeException("Đầu sách đã ngừng hiển thị");

        Long openReservations = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM PHIEUDATTRUOC
                WHERE MaDauSach = ? AND TrangThai IN (N'Đang chờ', N'Đã giữ chỗ')
                """, Long.class, bookId);
        if (openReservations != null && openReservations > 0) {
            throw new CatalogValidationException(
                    HttpStatus.CONFLICT,
                    "OPEN_RESERVATIONS",
                    "Không thể ngừng hiển thị khi còn đặt trước đang mở",
                    Map.of("reason", "Hãy xử lý các đặt trước đang mở trước"),
                    Map.of("openReservations", openReservations)
            );
        }

        Map<String, Object> before = snapshot(book);
        book.setTrangThai(INACTIVE);
        dauSachRepository.saveAndFlush(book);
        audit(user, "Ngừng hiển thị đầu sách", bookId, reason, before, snapshot(book));
        return dauSachService.getById(bookId);
    }

    @Transactional
    public DauSachResponse reactivate(String bookId, String reason, AuthUser user) {
        requireStaff(user);
        reason = requireReason(reason);
        DauSach book = requireBook(bookId);
        if (ACTIVE.equals(book.getTrangThai())) throw new RuntimeException("Đầu sách đang hoạt động");

        Map<String, Object> before = snapshot(book);
        book.setTrangThai(ACTIVE);
        dauSachRepository.saveAndFlush(book);
        audit(user, "Khôi phục hiển thị đầu sách", bookId, reason, before, snapshot(book));
        return dauSachService.getById(bookId);
    }

    @Transactional(readOnly = true)
    public BookDeletePreflightResponse preflight(String bookId, AuthUser user) {
        requireAdmin(user);
        requireBook(bookId);
        return buildPreflight(bookId);
    }

    @Transactional
    public void hardDelete(String bookId, AuthUser user) {
        requireAdmin(user);
        DauSach book = requireBook(bookId);
        BookDeletePreflightResponse preflight = buildPreflight(bookId);
        if (!preflight.canDelete()) {
            throw new CatalogValidationException(
                    HttpStatus.CONFLICT,
                    "BOOK_HAS_DEPENDENCIES",
                    "Không thể xóa cứng đầu sách đã có liên kết",
                    Map.of(),
                    Map.of("dependencies", preflight.dependencies())
            );
        }

        audit(user, "Xóa cứng đầu sách", bookId, "Xóa bản ghi không có liên kết", snapshot(book), null);
        jdbcTemplate.update("DELETE FROM DAUSACH_TACGIA WHERE MaDauSach = ?", bookId);
        jdbcTemplate.update("DELETE FROM DAUSACH_THELOAI WHERE MaDauSach = ?", bookId);
        dauSachRepository.delete(book);
        dauSachRepository.flush();
    }

    private BookDeletePreflightResponse buildPreflight(String bookId) {
        Map<String, Long> dependencies = new LinkedHashMap<>();
        addCount(dependencies, "copies", "CUONSACH", bookId);
        addCount(dependencies, "reservations", "PHIEUDATTRUOC", bookId);
        addCount(dependencies, "ratings", "DANHGIA", bookId);
        addCount(dependencies, "comments", "BINHLUAN", bookId);
        addCount(dependencies, "favorites", "SACHYEUTHICH", bookId);
        Long auditCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NHATKYHOATDONG WHERE DoiTuongTacDong = ? AND MaDoiTuongTacDong = ?",
                Long.class, OBJECT_TYPE, bookId
        );
        if (auditCount != null && auditCount > 0) dependencies.put("auditLogs", auditCount);
        return new BookDeletePreflightResponse(dependencies.isEmpty(), dependencies);
    }

    private void addCount(Map<String, Long> dependencies, String key, String table, String bookId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE MaDauSach = ?", Long.class, bookId
        );
        if (count != null && count > 0) dependencies.put(key, count);
    }

    private void audit(
            AuthUser user,
            String action,
            String bookId,
            String reason,
            Map<String, Object> before,
            Map<String, Object> after
    ) {
        try {
            String detail = objectMapper.writeValueAsString(Map.of(
                    "reason", reason.trim(),
                    "before", before == null ? Map.of() : before,
                    "after", after == null ? Map.of() : after
            ));
            activityLogService.logAsAccount(user.getMaTaiKhoan(), action, OBJECT_TYPE, bookId, detail);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Không tạo được dữ liệu audit đầu sách", ex);
        }
    }

    private Map<String, Object> snapshot(DauSach book) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", book.getMaDauSach());
        snapshot.put("title", book.getTenDauSach());
        snapshot.put("isbn", book.getIsbn());
        snapshot.put("status", book.getTrangThai());
        return snapshot;
    }

    private DauSach requireBook(String bookId) {
        return dauSachRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));
    }

    private void requireStaff(AuthUser user) {
        if (user == null || (!RoleConstants.ADMIN.equals(user.getTenVaiTro())
                && !RoleConstants.LIBRARIAN.equals(user.getTenVaiTro()))) {
            throw new AccessDeniedException("Không có quyền thay đổi vòng đời đầu sách");
        }
    }

    private void requireAdmin(AuthUser user) {
        if (user == null || !RoleConstants.ADMIN.equals(user.getTenVaiTro())) {
            throw new AccessDeniedException("Chỉ quản trị viên được xóa cứng đầu sách");
        }
    }

    private String requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new CatalogValidationException(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "Lý do không được để trống",
                    Map.of("reason", "Lý do không được để trống"),
                    null
            );
        }
        return reason.trim();
    }
}

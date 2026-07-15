package com.library.backend.service;

import com.library.backend.dto.BookCopyActionResponse;
import com.library.backend.dto.BookCopyConditionRequest;
import com.library.backend.dto.BookCopyMoveRequest;
import com.library.backend.entity.ChiNhanh;
import com.library.backend.entity.CuonSach;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.repository.ChiNhanhRepository;
import com.library.backend.repository.CuonSachRepository;
import com.library.backend.repository.ViTriSachRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BookCopyActionService {
    static final String AVAILABLE = "TT_SANCO";
    static final String BORROWED = "TT_DANGMUON";
    static final String RESERVED = "TT_DANGDATTRUOC";
    static final String DAMAGED = "TT_HONG";
    static final String LOST = "TT_MAT";
    static final String WITHDRAWN = "TT_NGUNGLUUTHONG";

    private final CuonSachRepository copyRepository;
    private final ChiNhanhRepository branchRepository;
    private final ViTriSachRepository locationRepository;
    private final BranchAuthorizationService branchAuthorizationService;
    private final JdbcTemplate jdbcTemplate;
    private final ActivityLogService activityLogService;

    public BookCopyActionService(
            CuonSachRepository copyRepository,
            ChiNhanhRepository branchRepository,
            ViTriSachRepository locationRepository,
            BranchAuthorizationService branchAuthorizationService,
            JdbcTemplate jdbcTemplate,
            ActivityLogService activityLogService
    ) {
        this.copyRepository = copyRepository;
        this.branchRepository = branchRepository;
        this.locationRepository = locationRepository;
        this.branchAuthorizationService = branchAuthorizationService;
        this.jdbcTemplate = jdbcTemplate;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public BookCopyActionResponse applyCondition(String copyId, BookCopyConditionRequest request, AuthUser user) {
        CuonSach copy = requireLockedCopy(copyId);
        authorizeCurrentBranch(user, copy.getMaChiNhanh());
        String before = copy.getMaTrangThai();
        String after = resolveTargetStatus(before, request.action());
        ensureNoOpenBusinessLink(copyId, before);
        LocalDateTime occurredAt = LocalDateTime.now();

        copy.setMaTrangThai(after);
        copyRepository.saveAndFlush(copy);
        boolean damageAction = request.action() == BookCopyConditionRequest.Action.MARK_DAMAGED;
        jdbcTemplate.update("""
                INSERT INTO CUONSACH_TRANGTHAI_EVENT
                    (MaCuonSach, HanhDong, TrangThaiTruoc, TrangThaiSau, MucDo, LoaiHong, MoTa, LyDo, MaTaiKhoan, ThoiGian)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, copyId, request.action().name(), before, after,
                damageAction && request.severity() != null ? request.severity().name() : null,
                damageAction ? joinDamageTypes(request.damageTypes()) : null,
                trimToNull(request.description()), request.reason().trim(),
                requireAccountId(user), occurredAt);
        activityLogService.logAsAccount(requireAccountId(user), "BOOK_COPY_" + request.action().name(),
                "BOOK_COPY", copyId, "status=" + before + "->" + after + ", reason=" + request.reason().trim());
        return new BookCopyActionResponse(copyId, request.action().name(), before, after,
                copy.getMaViTri(), copy.getMaViTri(), request.reason().trim(), occurredAt);
    }

    @Transactional
    public BookCopyActionResponse moveLocation(String copyId, BookCopyMoveRequest request, AuthUser user) {
        CuonSach copy = requireLockedCopy(copyId);
        authorizeCurrentBranch(user, copy.getMaChiNhanh());
        ensureMovable(copy);
        if (!locationRepository.existsActiveInBranch(request.locationId(), copy.getMaChiNhanh())) {
            throw validation("locationId", "Vị trí không thuộc chi nhánh hiện tại của cuốn sách");
        }
        String before = copy.getMaViTri();
        if (before.equals(request.locationId())) {
            throw validation("locationId", "Vị trí mới phải khác vị trí hiện tại");
        }
        LocalDateTime occurredAt = LocalDateTime.now();
        copy.setMaViTri(request.locationId());
        copyRepository.saveAndFlush(copy);
        jdbcTemplate.update("""
                INSERT INTO CUONSACH_VITRI_EVENT
                    (MaCuonSach, MaViTriTruoc, MaViTriSau, LyDo, MaTaiKhoan, ThoiGian)
                VALUES (?, ?, ?, ?, ?, ?)
                """, copyId, before, request.locationId(), request.reason().trim(), requireAccountId(user), occurredAt);
        activityLogService.logAsAccount(requireAccountId(user), "BOOK_COPY_MOVE_LOCATION", "BOOK_COPY", copyId,
                "location=" + before + "->" + request.locationId() + ", reason=" + request.reason().trim());
        return new BookCopyActionResponse(copyId, "MOVE_LOCATION", copy.getMaTrangThai(), copy.getMaTrangThai(),
                before, request.locationId(), request.reason().trim(), occurredAt);
    }

    private String resolveTargetStatus(String current, BookCopyConditionRequest.Action action) {
        if (BORROWED.equals(current) || RESERVED.equals(current)) {
            throw conflict("Cuốn đang mượn/đặt trước chỉ được đổi trạng thái bởi nghiệp vụ tương ứng");
        }
        return switch (action) {
            case MARK_DAMAGED -> requireTransition(current, AVAILABLE, DAMAGED, "Chỉ cuốn Sẵn có mới có thể báo hỏng thủ công");
            case MARK_LOST -> requireTransition(current, AVAILABLE, LOST, "Cuốn đang mượn phải báo mất qua nghiệp vụ trả/mất");
            case WITHDRAW -> requireTransition(current, AVAILABLE, WITHDRAWN, "Chỉ cuốn Sẵn có mới có thể ngừng lưu thông");
            case RESTORE_AFTER_REPAIR -> requireTransition(current, DAMAGED, AVAILABLE, "Chỉ cuốn Bị hỏng mới có thể khôi phục sau sửa chữa");
            case RESTORE_FOUND -> requireTransition(current, LOST, AVAILABLE, "Chỉ cuốn Bị mất mới có thể khôi phục sau khi tìm lại");
        };
    }

    private String requireTransition(String current, String expected, String target, String message) {
        if (!expected.equals(current)) throw conflict(message);
        return target;
    }

    private void ensureMovable(CuonSach copy) {
        if (BORROWED.equals(copy.getMaTrangThai()) || RESERVED.equals(copy.getMaTrangThai())) {
            throw conflict("Cuốn đang mượn/đặt trước không thể chuyển vị trí thủ công");
        }
        if (LOST.equals(copy.getMaTrangThai())) {
            throw conflict("Cuốn đang bị đánh dấu mất không thể chuyển vị trí");
        }
        ensureNoOpenBusinessLink(copy.getMaCuonSach(), copy.getMaTrangThai());
    }

    private void ensureNoOpenBusinessLink(String copyId, String state) {
        if (BORROWED.equals(state) || RESERVED.equals(state)) return;
        Integer openLoans = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM CHITIETPHIEUMUON WHERE MaCuonSach = ? AND TrangThai = N'Đang mượn'",
                Integer.class, copyId);
        Integer activeHolds = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PHIEUDATTRUOC WHERE MaCuonSachDuocGiu = ? AND TrangThai = N'Đã giữ chỗ'",
                Integer.class, copyId);
        if ((openLoans != null && openLoans > 0) || (activeHolds != null && activeHolds > 0)) {
            throw conflict("Cuốn có liên kết mượn/giữ chỗ đang mở; không thể thao tác thủ công");
        }
    }

    private CuonSach requireLockedCopy(String copyId) {
        return copyRepository.findByIdForUpdate(copyId)
                .orElseThrow(() -> new CatalogValidationException(HttpStatus.NOT_FOUND, "COPY_NOT_FOUND",
                        "Không tìm thấy cuốn sách", Map.of(), null));
    }

    private void authorizeCurrentBranch(AuthUser user, String branchId) {
        if (user == null) throw new AccessDeniedException("Thiếu tài khoản xác thực");
        ChiNhanh branch = branchRepository.findById(branchId)
                .orElseThrow(() -> validation("branchId", "Chi nhánh của cuốn sách không tồn tại"));
        if (!"Hoạt động".equalsIgnoreCase(branch.getTrangThai())) {
            throw conflict("Chi nhánh của cuốn sách đã ngừng hoạt động");
        }
        if (!RoleConstants.ADMIN.equals(user.getTenVaiTro())) {
            branchAuthorizationService.requireAllowedBranch(user, branchId);
        }
    }

    private String requireAccountId(AuthUser user) {
        if (user == null || user.getMaTaiKhoan() == null || user.getMaTaiKhoan().isBlank()) {
            throw new AccessDeniedException("Không xác định được tài khoản thao tác");
        }
        return user.getMaTaiKhoan();
    }

    private String joinDamageTypes(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        return values.stream().map(String::trim).filter(value -> !value.isBlank()).distinct().reduce((a, b) -> a + "," + b).orElse(null);
    }

    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private CatalogValidationException validation(String field, String message) {
        return new CatalogValidationException(HttpStatus.BAD_REQUEST, "COPY_ACTION_VALIDATION", message, Map.of(field, message), null);
    }
    private CatalogValidationException conflict(String message) {
        return new CatalogValidationException(HttpStatus.CONFLICT, "INVALID_COPY_TRANSITION", message, Map.of(), null);
    }
}

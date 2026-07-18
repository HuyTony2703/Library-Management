package com.library.backend.service;

import com.library.backend.dto.ReaderEligibilityResponse;
import com.library.backend.dto.ReaderLifecycleRequest;
import com.library.backend.dto.ReaderLockRequest;
import com.library.backend.dto.ReaderStateActionResponse;
import com.library.backend.dto.ReaderUnlockRequest;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.security.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReaderStateService {
    private static final String ACTIVE = "Hoạt động";
    private static final String INACTIVE = "Ngừng hoạt động";

    private final JdbcTemplate jdbcTemplate;
    private final ActivityLogService activityLogService;

    public ReaderStateService(JdbcTemplate jdbcTemplate, ActivityLogService activityLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public ReaderStateActionResponse lock(String readerId, ReaderLockRequest request, AuthUser actor) {
        ReaderRow reader = lockReader(readerId);
        LocalDateTime now = LocalDateTime.now();
        for (ReaderLockRequest.Scope scope : request.scopes()) {
            closeExpiredLock(readerId, scope, actor.getMaTaiKhoan(), now);
            if (hasActiveLock(readerId, scope, now.toLocalDate())) {
                throw validation("scopes", "Độc giả đã có khóa " + scope.name() + " còn hiệu lực");
            }
            jdbcTemplate.update("""
                    INSERT INTO DOCGIA_KHOA
                        (MaDocGia, PhamVi, LyDoKhoa, KhoaLuc, KhoaDen, GhiChu, MaTaiKhoanKhoa)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, readerId, scope.name(), clean(request.reason()), now, request.lockedUntil(),
                    cleanNullable(request.note()), actor.getMaTaiKhoan());
        }
        audit(actor, readerId, "Khóa độc giả", request.reason());
        return response(readerId, "LOCK", reader.profileStatus(), now);
    }

    @Transactional
    public ReaderStateActionResponse unlock(String readerId, ReaderUnlockRequest request, AuthUser actor) {
        ReaderRow reader = lockReader(readerId);
        LocalDateTime now = LocalDateTime.now();
        for (ReaderLockRequest.Scope scope : request.scopes()) {
            int changed = jdbcTemplate.update("""
                    UPDATE DOCGIA_KHOA
                    SET MoKhoaLuc = ?, LyDoMoKhoa = ?, MaTaiKhoanMoKhoa = ?
                    WHERE MaDocGia = ? AND PhamVi = ? AND MoKhoaLuc IS NULL
                      AND (KhoaDen IS NULL OR KhoaDen >= CAST(? AS DATE))
                    """, now, clean(request.reason()), actor.getMaTaiKhoan(), readerId, scope.name(), now);
            if (changed == 0) throw validation("scopes", "Không có khóa " + scope.name() + " còn hiệu lực để mở");
        }
        audit(actor, readerId, "Mở khóa độc giả", request.reason());
        return response(readerId, "UNLOCK", reader.profileStatus(), now);
    }

    @Transactional
    public ReaderStateActionResponse deactivate(String readerId, ReaderLifecycleRequest request, AuthUser actor) {
        ReaderRow reader = lockReader(readerId);
        if (INACTIVE.equals(reader.profileStatus())) throw validation("reason", "Hồ sơ đã ngừng hoạt động");
        ReaderEligibilityResponse current = eligibility(readerId);
        if (hasObligations(current.obligations())) {
            throw new CatalogValidationException(HttpStatus.CONFLICT, "READER_CLOSE_DECISION_PENDING",
                    "Chính sách đóng hồ sơ khi còn nghĩa vụ chưa được chốt", Map.of(),
                    Map.of("obligations", current.obligations()));
        }
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("UPDATE DOCGIA SET TrangThai = ? WHERE MaDocGia = ?", INACTIVE, readerId);
        addLifecycleEvent(readerId, "DEACTIVATE", reader.profileStatus(), INACTIVE, request.reason(), actor, now);
        audit(actor, readerId, "Ngừng hoạt động độc giả", request.reason());
        return response(readerId, "DEACTIVATE", INACTIVE, now);
    }

    @Transactional
    public ReaderStateActionResponse reactivate(String readerId, ReaderLifecycleRequest request, AuthUser actor) {
        ReaderRow reader = lockReader(readerId);
        if (ACTIVE.equals(reader.profileStatus())) throw validation("reason", "Hồ sơ đang hoạt động");
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("UPDATE DOCGIA SET TrangThai = ? WHERE MaDocGia = ?", ACTIVE, readerId);
        addLifecycleEvent(readerId, "REACTIVATE", reader.profileStatus(), ACTIVE, request.reason(), actor, now);
        audit(actor, readerId, "Khôi phục độc giả", request.reason());
        return response(readerId, "REACTIVATE", ACTIVE, now);
    }

    @Transactional(readOnly = true)
    public ReaderEligibilityResponse eligibility(String readerId) {
        LocalDate today = LocalDate.now();
        ContextRow row = jdbcTemplate.query("""
                SELECT dg.MaDocGia, dg.HoTen, dg.TrangThai AS TrangThaiHoSo, dg.NgayHetHanThe,
                       tk.TrangThai AS TrangThaiTaiKhoan,
                       gm.MaGoiThanhVien, gm.TenGoi, gm.NgayKetThuc,
                       quota.MaPhienBan, quota.SoSachMuonToiDa,
                       COALESCE(loans.SoDangMuon, 0) AS SoDangMuon,
                       COALESCE(loans.SoQuaHan, 0) AS SoQuaHan,
                       COALESCE(loans.SoNgayQuaHanToiDa, 0) AS SoNgayQuaHanToiDa,
                       COALESCE(debts.TongNo, 0) AS TongNo,
                       COALESCE(reservations.SoDatTruoc, 0) AS SoDatTruoc
                FROM DOCGIA dg
                INNER JOIN TAIKHOAN tk ON tk.MaTaiKhoan = dg.MaTaiKhoan
                OUTER APPLY (
                    SELECT TOP 1 lsg.MaGoiThanhVien, gtv.TenGoi, lsg.NgayKetThuc
                    FROM LICHSUGOITHANHVIEN lsg
                    INNER JOIN GOITHANHVIEN gtv ON gtv.MaGoiThanhVien = lsg.MaGoiThanhVien
                    WHERE lsg.MaDocGia = dg.MaDocGia AND lsg.TrangThai = N'Đang sử dụng'
                      AND CAST(SYSDATETIME() AS DATE) BETWEEN lsg.NgayBatDau AND lsg.NgayKetThuc
                    ORDER BY lsg.NgayKetThuc DESC, lsg.MaLichSuGoi DESC
                ) gm
                OUTER APPLY (
                    SELECT TOP 1 p.MaPhienBan, q.SoSachMuonToiDa
                    FROM PHIENBANQUYDINH p
                    INNER JOIN QUYDINHGOI q ON q.MaPhienBan = p.MaPhienBan
                    WHERE p.TrangThai = N'Đang áp dụng' AND q.MaGoiThanhVien = gm.MaGoiThanhVien
                    ORDER BY p.NgayApDung DESC, p.MaPhienBan DESC
                ) quota
                OUTER APPLY (
                    SELECT COUNT_BIG(*) AS SoDangMuon,
                           SUM(CASE WHEN ctm.HanTra < SYSDATETIME() THEN 1 ELSE 0 END) AS SoQuaHan,
                           MAX(CASE WHEN ctm.HanTra < SYSDATETIME()
                                    THEN DATEDIFF(DAY, ctm.HanTra, SYSDATETIME()) ELSE 0 END) AS SoNgayQuaHanToiDa
                    FROM PHIEUMUON pm JOIN CHITIETPHIEUMUON ctm ON ctm.MaPhieuMuon = pm.MaPhieuMuon
                    WHERE pm.MaDocGia = dg.MaDocGia AND ctm.TrangThai = N'Đang mượn'
                ) loans
                OUTER APPLY (
                    SELECT CAST(COALESCE(SUM(SoTienConLai), 0) AS DECIMAL(18,2)) AS TongNo
                    FROM KHOANNO WHERE MaDocGia = dg.MaDocGia AND TrangThai <> N'Đã thanh toán'
                ) debts
                OUTER APPLY (
                    SELECT COUNT_BIG(*) AS SoDatTruoc FROM PHIEUDATTRUOC
                    WHERE MaDocGia = dg.MaDocGia AND TrangThai IN (N'Đang chờ', N'Đã giữ chỗ')
                ) reservations
                WHERE dg.MaDocGia = ?
                """, (rs, n) -> new ContextRow(rs.getString("MaDocGia"), rs.getString("HoTen"),
                rs.getString("TrangThaiHoSo"), date(rs.getDate("NgayHetHanThe")),
                rs.getString("TrangThaiTaiKhoan"), rs.getString("MaGoiThanhVien"), rs.getString("TenGoi"),
                date(rs.getDate("NgayKetThuc")), rs.getString("MaPhienBan"),
                (Integer) rs.getObject("SoSachMuonToiDa"), rs.getLong("SoDangMuon"), rs.getLong("SoQuaHan"),
                rs.getLong("SoNgayQuaHanToiDa"), rs.getBigDecimal("TongNo"), rs.getLong("SoDatTruoc")), readerId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy độc giả"));

        List<ReaderEligibilityResponse.ActiveLock> locks = activeLocks(readerId, today);
        List<ReaderEligibilityResponse.Reason> blocking = new ArrayList<>();
        List<ReaderEligibilityResponse.Reason> warnings = new ArrayList<>();
        if (!ACTIVE.equals(row.profileStatus())) blocking.add(reason("PROFILE_INACTIVE", "Hồ sơ không hoạt động"));
        String cardStatus = cardStatus(row.cardExpiry(), today);
        if ("EXPIRED".equals(cardStatus)) blocking.add(reason("CARD_EXPIRED", "Thẻ độc giả đã hết hạn"));
        if (locks.stream().anyMatch(lock -> "BORROWING".equals(lock.scope())))
            blocking.add(reason("BORROWING_LOCKED", "Độc giả đang bị khóa quyền mượn"));
        if (locks.stream().anyMatch(lock -> "LOGIN".equals(lock.scope())))
            warnings.add(reason("LOGIN_LOCKED", "Tài khoản đang bị khóa đăng nhập; quyền mượn được đánh giá riêng"));
        if (row.overdueLoans() > 0) blocking.add(reason("OVERDUE_LOANS", "Độc giả có sách quá hạn chưa trả"));
        if (row.debt().compareTo(BigDecimal.ZERO) > 0) blocking.add(reason("OUTSTANDING_DEBT", "Độc giả đang có nợ chưa thanh toán"));
        String membershipStatus = membershipStatus(row.membershipExpiry(), today);
        if (!"ACTIVE".equals(membershipStatus)) blocking.add(reason("MEMBERSHIP_INACTIVE", "Độc giả không có gói thành viên còn hiệu lực"));
        if (row.borrowMaximum() == null) {
            blocking.add(reason("BORROW_QUOTA_UNAVAILABLE", "Chưa có quy định số sách tối đa đang áp dụng cho gói thành viên"));
        } else if (row.currentLoans() >= row.borrowMaximum()) {
            blocking.add(reason("BORROW_QUOTA_REACHED", "Độc giả đã đạt số sách mượn tối đa"));
        }
        Long remaining = row.cardExpiry() == null ? null : ChronoUnit.DAYS.between(today, row.cardExpiry());
        String effectiveAccountStatus = locks.stream().anyMatch(lock -> "LOGIN".equals(lock.scope()))
                ? "Khóa đăng nhập" : row.accountStatus();
        return new ReaderEligibilityResponse(
                new ReaderEligibilityResponse.Reader(row.id(), row.name(), row.profileStatus(), effectiveAccountStatus),
                new ReaderEligibilityResponse.Card(row.cardExpiry(), cardStatus, remaining),
                new ReaderEligibilityResponse.Membership(row.membershipPlanId(), row.membershipPlanName(), membershipStatus, row.membershipExpiry()),
                new ReaderEligibilityResponse.Quota(row.currentLoans(), row.borrowMaximum(),
                        row.borrowMaximum() == null ? null : Math.max(0, row.borrowMaximum() - (int) row.currentLoans()),
                        row.policyVersion()),
                new ReaderEligibilityResponse.Overdue(row.overdueLoans(), row.maxOverdueDays()),
                new ReaderEligibilityResponse.Debt(row.debt(), BigDecimal.ZERO),
                new ReaderEligibilityResponse.Obligations(row.currentLoans(), row.overdueLoans(), row.reservations(), row.debt()),
                locks, blocking.isEmpty(), warnings, blocking);
    }

    private ReaderRow lockReader(String readerId) {
        return jdbcTemplate.query("SELECT MaDocGia, TrangThai FROM DOCGIA WITH (UPDLOCK, HOLDLOCK) WHERE MaDocGia = ?",
                (rs, n) -> new ReaderRow(rs.getString("MaDocGia"), normalizeLegacyStatus(rs.getString("TrangThai"))), readerId)
                .stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy độc giả"));
    }

    private void closeExpiredLock(String readerId, ReaderLockRequest.Scope scope, String actor, LocalDateTime now) {
        jdbcTemplate.update("""
                UPDATE DOCGIA_KHOA SET MoKhoaLuc = ?, LyDoMoKhoa = N'Hết thời hạn khóa', MaTaiKhoanMoKhoa = ?
                WHERE MaDocGia = ? AND PhamVi = ? AND MoKhoaLuc IS NULL AND KhoaDen < CAST(? AS DATE)
                """, now, actor, readerId, scope.name(), now);
    }

    private boolean hasActiveLock(String readerId, ReaderLockRequest.Scope scope, LocalDate today) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM DOCGIA_KHOA WHERE MaDocGia = ? AND PhamVi = ?
                  AND MoKhoaLuc IS NULL AND (KhoaDen IS NULL OR KhoaDen >= ?)
                """, Integer.class, readerId, scope.name(), today);
        return count != null && count > 0;
    }

    private List<ReaderEligibilityResponse.ActiveLock> activeLocks(String readerId, LocalDate today) {
        return jdbcTemplate.query("""
                SELECT PhamVi, LyDoKhoa, KhoaDen FROM DOCGIA_KHOA
                WHERE MaDocGia = ? AND MoKhoaLuc IS NULL AND (KhoaDen IS NULL OR KhoaDen >= ?)
                ORDER BY PhamVi
                """, (rs, n) -> new ReaderEligibilityResponse.ActiveLock(rs.getString("PhamVi"),
                rs.getString("LyDoKhoa"), date(rs.getDate("KhoaDen"))), readerId, today);
    }

    private void addLifecycleEvent(String readerId, String action, String before, String after,
                                   String reason, AuthUser actor, LocalDateTime now) {
        jdbcTemplate.update("""
                INSERT INTO DOCGIA_VONGDOI_EVENT
                    (MaDocGia, HanhDong, TrangThaiTruoc, TrangThaiSau, LyDo, MaTaiKhoan, ThoiGian)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, readerId, action, before, after, clean(reason), actor.getMaTaiKhoan(), now);
    }

    private ReaderStateActionResponse response(String readerId, String action, String status, LocalDateTime now) {
        return new ReaderStateActionResponse(readerId, action, status, now, eligibility(readerId));
    }

    private void audit(AuthUser actor, String readerId, String action, String reason) {
        activityLogService.logAsAccountSafe(actor.getMaTaiKhoan(), action, "DOCGIA", readerId,
                action + "; lý do: " + clean(reason));
    }

    private static boolean hasObligations(ReaderEligibilityResponse.Obligations value) {
        return value.currentLoans() > 0 || value.activeReservations() > 0
                || value.outstandingDebt().compareTo(BigDecimal.ZERO) > 0;
    }
    private static String normalizeLegacyStatus(String status) {
        return INACTIVE.equals(status) ? INACTIVE : ACTIVE;
    }
    private static String cardStatus(LocalDate expiry, LocalDate today) {
        if (expiry == null || expiry.isBefore(today)) return "EXPIRED";
        return expiry.isAfter(today.plusDays(30)) ? "VALID" : "EXPIRING";
    }
    private static String membershipStatus(LocalDate expiry, LocalDate today) {
        if (expiry == null) return "NONE";
        if (expiry.isBefore(today)) return "EXPIRED";
        return "ACTIVE";
    }
    private static ReaderEligibilityResponse.Reason reason(String code, String message) {
        return new ReaderEligibilityResponse.Reason(code, message);
    }
    private static String clean(String value) { return value.trim(); }
    private static String cleanNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    private static LocalDate date(Date value) { return value == null ? null : value.toLocalDate(); }
    private static CatalogValidationException validation(String field, String message) {
        return new CatalogValidationException(HttpStatus.BAD_REQUEST, "READER_STATE_VALIDATION", message,
                Map.of(field, message), null);
    }

    private record ReaderRow(String id, String profileStatus) {}
    private record ContextRow(String id, String name, String profileStatus, LocalDate cardExpiry,
                              String accountStatus, String membershipPlanId, String membershipPlanName,
                              LocalDate membershipExpiry, String policyVersion, Integer borrowMaximum,
                              long currentLoans, long overdueLoans, long maxOverdueDays,
                              BigDecimal debt, long reservations) {}
}

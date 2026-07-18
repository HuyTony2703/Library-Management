package com.library.backend.service;

import com.library.backend.dto.LoanCopyOptionResponse;
import com.library.backend.dto.LoanPreviewRequest;
import com.library.backend.dto.LoanPreviewResponse;
import com.library.backend.dto.LoanReasonResponse;
import com.library.backend.dto.ReaderEligibilityResponse;
import com.library.backend.dto.StaffContextResponse;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.security.AuthUser;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LoanPreviewService {
    static final String AVAILABLE = "TT_SANCO";
    static final String RESERVED = "TT_DANGDATTRUOC";
    private static final ZoneId LIBRARY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String COPY_SELECT = """
            SELECT cs.MaCuonSach, cs.MaVach, cs.MaDauSach, ds.TenDauSach, ds.ISBN,
                   cs.MaChiNhanh, cn.TenChiNhanh, cs.MaViTri, vt.MaViTriHienThi,
                   cs.MaTrangThai, tt.TenTrangThai,
                   reservation.MaPhieuDatTruoc, reservation.MaDocGia AS MaDocGiaDatTruoc,
                   reservation.TrangThai AS TrangThaiDatTruoc, reservation.NgayHetHanGiuCho
            FROM CUONSACH cs
            INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
            INNER JOIN CHINHANH cn ON cn.MaChiNhanh = cs.MaChiNhanh
            INNER JOIN VITRISACH vt ON vt.MaViTri = cs.MaViTri
            INNER JOIN TRANGTHAICUONSACH tt ON tt.MaTrangThai = cs.MaTrangThai
            OUTER APPLY (
                SELECT TOP 1 pdt.MaPhieuDatTruoc, pdt.MaDocGia, pdt.TrangThai, pdt.NgayHetHanGiuCho
                FROM PHIEUDATTRUOC pdt
                WHERE pdt.MaCuonSachDuocGiu = cs.MaCuonSach AND pdt.TrangThai = N'Đã giữ chỗ'
                ORDER BY CASE WHEN pdt.NgayHetHanGiuCho IS NULL OR pdt.NgayHetHanGiuCho >= SYSDATETIME() THEN 0 ELSE 1 END,
                         pdt.NgayDat DESC, pdt.MaPhieuDatTruoc DESC
            ) reservation
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ReaderStateService readerStateService;
    private final StaffContextService staffContextService;

    public LoanPreviewService(
            NamedParameterJdbcTemplate jdbcTemplate,
            ReaderStateService readerStateService,
            StaffContextService staffContextService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.readerStateService = readerStateService;
        this.staffContextService = staffContextService;
    }

    @Transactional(readOnly = true)
    public List<LoanCopyOptionResponse> search(
            String query,
            String readerId,
            int limit,
            AuthUser user
    ) {
        String normalized = requireQuery(query);
        validateLimit(limit);
        readerStateService.eligibility(readerId);
        StaffContextResponse.BranchSummary branch = resolveBranch(user, null);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("query", normalized)
                .addValue("pattern", "%" + escapeLike(normalized) + "%")
                .addValue("readerId", readerId)
                .addValue("branchId", branch.id())
                .addValue("limit", limit);
        String sql = "SELECT TOP (:limit) * FROM (" + COPY_SELECT + """
                WHERE cs.MaChiNhanh = :branchId
                  AND (cs.MaCuonSach LIKE :pattern ESCAPE '\\'
                       OR cs.MaVach LIKE :pattern ESCAPE '\\'
                       OR ds.TenDauSach COLLATE Vietnamese_100_CI_AI LIKE :pattern ESCAPE '\\'
                       OR ds.ISBN LIKE :pattern ESCAPE '\\')
                  AND (cs.MaTrangThai = 'TT_SANCO'
                       OR (cs.MaTrangThai = 'TT_DANGDATTRUOC'
                           AND reservation.MaDocGia = :readerId
                           AND (reservation.NgayHetHanGiuCho IS NULL OR reservation.NgayHetHanGiuCho >= SYSDATETIME())))
                ) eligible_copies
                ORDER BY CASE WHEN UPPER(MaCuonSach) = UPPER(:query) OR UPPER(MaVach) = UPPER(:query) THEN 0 ELSE 1 END,
                         TenDauSach COLLATE Vietnamese_100_CI_AI, MaCuonSach
                """;
        LocalDateTime now = LocalDateTime.now(LIBRARY_ZONE);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> toOption(mapCopy(rs), readerId, branch.id(), normalized, now))
                .stream().filter(LoanCopyOptionResponse::borrowable).toList();
    }

    @Transactional(readOnly = true)
    public LoanCopyOptionResponse exactLookup(String code, String readerId, AuthUser user) {
        String normalized = requireQuery(code);
        readerStateService.eligibility(readerId);
        StaffContextResponse.BranchSummary branch = resolveBranch(user, null);
        MapSqlParameterSource params = new MapSqlParameterSource("code", normalized);
        CopyRow copy = jdbcTemplate.query(COPY_SELECT + """
                        WHERE UPPER(cs.MaCuonSach) = UPPER(:code) OR UPPER(cs.MaVach) = UPPER(:code)
                        """, params, (rs, rowNum) -> mapCopy(rs)).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy barcode hoặc mã cuốn khớp chính xác"));
        return toOption(copy, readerId, branch.id(), normalized, LocalDateTime.now(LIBRARY_ZONE));
    }

    @Transactional(readOnly = true)
    public LoanPreviewResponse preview(LoanPreviewRequest request, AuthUser user) {
        StaffContextResponse.BranchSummary branch = resolveBranch(user, request.branchId());
        List<String> copyIds = request.copyIds().stream().map(String::trim).toList();
        ReaderEligibilityResponse reader = readerStateService.eligibility(request.readerId());
        Set<String> uniqueIds = new LinkedHashSet<>(copyIds);
        if (uniqueIds.size() != copyIds.size()) {
            return duplicatePreview(request.readerId(), branch, copyIds, reader);
        }

        LocalDateTime now = LocalDateTime.now(LIBRARY_ZONE);
        Map<String, CopyRow> copies = findCopies(uniqueIds, reader.quota().policyVersion(), reader.membership().planId());
        List<LoanPreviewResponse.Item> items = new ArrayList<>();
        List<LoanReasonResponse> warnings = convert(reader.warnings());
        List<LoanReasonResponse> blocking = convert(reader.blockingReasons());

        long after = reader.quota().current() + copyIds.size();
        if (reader.quota().maximum() != null && after > reader.quota().maximum()) {
            blocking.add(reason("BORROW_QUOTA_EXCEEDED",
                    "Giỏ làm vượt hạn mức: đang mượn " + reader.quota().current()
                            + ", thêm " + copyIds.size() + ", tối đa " + reader.quota().maximum()));
        }

        for (String copyId : copyIds) {
            CopyRow copy = copies.get(copyId);
            if (copy == null) {
                LoanReasonResponse missing = reason("COPY_NOT_FOUND", "Không tìm thấy cuốn sách " + copyId);
                blocking.add(missing);
                items.add(missingItem(copyId, missing));
                continue;
            }
            CopyEligibility eligibility = evaluateCopy(copy, branch.id(), request.readerId(), now);
            List<LoanReasonResponse> itemWarnings = new ArrayList<>(eligibility.warnings());
            List<LoanReasonResponse> itemBlocking = new ArrayList<>(eligibility.blockingReasons());
            LocalDateTime dueAt = null;
            if (itemBlocking.isEmpty()) {
                if (copy.ruleId() == null || copy.borrowDays() == null) {
                    itemBlocking.add(reason("BORROW_RULE_UNAVAILABLE",
                            "Chưa có quy định mượn phù hợp cho cuốn " + copy.copyId()));
                } else {
                    dueAt = expectedDueAt(now, copy.borrowDays(), reader.card().expiryDate());
                    if (dueAt == null) {
                        itemBlocking.add(reason("CARD_TOO_CLOSE_TO_EXPIRY",
                                "Hạn thẻ không đủ để mượn cuốn " + copy.copyId()));
                    }
                }
            }
            blocking.addAll(itemBlocking);
            warnings.addAll(itemWarnings);
            items.add(toPreviewItem(copy, request.readerId(), itemWarnings, itemBlocking, dueAt));
        }

        List<LoanReasonResponse> distinctWarnings = distinct(warnings);
        List<LoanReasonResponse> distinctBlocking = distinct(blocking);
        Integer remainingAfter = reader.quota().maximum() == null
                ? null : Math.max(0, reader.quota().maximum() - (int) after);
        return new LoanPreviewResponse(
                request.readerId(), new LoanPreviewResponse.Branch(branch.id(), branch.name()), now,
                distinctBlocking.isEmpty(),
                new LoanPreviewResponse.Quota(reader.quota().current(), copyIds.size(), after,
                        reader.quota().maximum(), remainingAfter),
                items, distinctWarnings, distinctBlocking,
                "Preview không giữ chỗ; backend sẽ kiểm tra lại khi tạo phiếu mượn."
        );
    }

    private Map<String, CopyRow> findCopies(Set<String> copyIds, String policyVersion, String membershipPlanId) {
        if (copyIds.isEmpty()) return Map.of();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("copyIds", copyIds)
                .addValue("policyVersion", policyVersion)
                .addValue("membershipPlanId", membershipPlanId);
        String sql = """
                SELECT cs.MaCuonSach, cs.MaVach, cs.MaDauSach, ds.TenDauSach, ds.ISBN,
                       cs.MaChiNhanh, cn.TenChiNhanh, cs.MaViTri, vt.MaViTriHienThi,
                       cs.MaTrangThai, tt.TenTrangThai,
                       reservation.MaPhieuDatTruoc, reservation.MaDocGia AS MaDocGiaDatTruoc,
                       reservation.TrangThai AS TrangThaiDatTruoc, reservation.NgayHetHanGiuCho,
                       borrow_rule.MaQuyDinhMuon, borrow_rule.SoNgayMuon
                FROM CUONSACH cs
                INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
                INNER JOIN CHINHANH cn ON cn.MaChiNhanh = cs.MaChiNhanh
                INNER JOIN VITRISACH vt ON vt.MaViTri = cs.MaViTri
                INNER JOIN TRANGTHAICUONSACH tt ON tt.MaTrangThai = cs.MaTrangThai
                OUTER APPLY (
                    SELECT TOP 1 pdt.MaPhieuDatTruoc, pdt.MaDocGia, pdt.TrangThai, pdt.NgayHetHanGiuCho
                    FROM PHIEUDATTRUOC pdt
                    WHERE pdt.MaCuonSachDuocGiu = cs.MaCuonSach AND pdt.TrangThai = N'Đã giữ chỗ'
                    ORDER BY CASE WHEN pdt.NgayHetHanGiuCho IS NULL OR pdt.NgayHetHanGiuCho >= SYSDATETIME() THEN 0 ELSE 1 END,
                             pdt.NgayDat DESC, pdt.MaPhieuDatTruoc DESC
                ) reservation
                OUTER APPLY (
                    SELECT TOP 1 qdm.MaQuyDinhMuon, qdm.SoNgayMuon
                    FROM DAUSACH_THELOAI dstl
                    INNER JOIN QUYDINHMUON_THELOAI qdm ON qdm.MaTheLoai = dstl.MaTheLoai
                    WHERE dstl.MaDauSach = cs.MaDauSach
                      AND qdm.MaPhienBan = :policyVersion
                      AND qdm.MaGoiThanhVien = :membershipPlanId
                    ORDER BY qdm.SoNgayMuon, qdm.MaQuyDinhMuon
                ) borrow_rule
                WHERE cs.MaCuonSach IN (:copyIds)
                """;
        Map<String, CopyRow> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, params, (rs, rowNum) -> mapCopy(rs, true))
                .forEach(copy -> result.put(copy.copyId(), copy));
        return result;
    }

    private LoanPreviewResponse duplicatePreview(
            String readerId,
            StaffContextResponse.BranchSummary branch,
            List<String> copyIds,
            ReaderEligibilityResponse reader
    ) {
        LoanReasonResponse duplicate = reason("DUPLICATE_COPY", "Giỏ mượn có mã cuốn bị trùng");
        long after = reader.quota().current() + copyIds.size();
        Integer remainingAfter = reader.quota().maximum() == null
                ? null : Math.max(0, reader.quota().maximum() - (int) after);
        return new LoanPreviewResponse(readerId, new LoanPreviewResponse.Branch(branch.id(), branch.name()),
                LocalDateTime.now(LIBRARY_ZONE), false,
                new LoanPreviewResponse.Quota(reader.quota().current(), copyIds.size(), after,
                        reader.quota().maximum(), remainingAfter),
                List.of(), convert(reader.warnings()),
                distinct(java.util.stream.Stream.concat(convert(reader.blockingReasons()).stream(), java.util.stream.Stream.of(duplicate)).toList()),
                "Preview không giữ chỗ; backend sẽ kiểm tra lại khi tạo phiếu mượn.");
    }

    private StaffContextResponse.BranchSummary resolveBranch(AuthUser user, String requestedBranchId) {
        StaffContextResponse context = staffContextService.getContext(user);
        if (!context.operational() || context.defaultBranch() == null) {
            throw new AccessDeniedException("Staff context chưa có chi nhánh mặc định hợp lệ");
        }
        StaffContextResponse.BranchSummary branch = context.defaultBranch();
        if (requestedBranchId != null && !requestedBranchId.isBlank() && !branch.id().equals(requestedBranchId)) {
            throw new AccessDeniedException("Chi nhánh request không khớp staff context");
        }
        return branch;
    }

    static CopyEligibility evaluateCopy(CopyRow copy, String branchId, String readerId, LocalDateTime now) {
        List<LoanReasonResponse> warnings = new ArrayList<>();
        List<LoanReasonResponse> blocking = new ArrayList<>();
        if (!branchId.equals(copy.branchId())) {
            blocking.add(reason("COPY_WRONG_BRANCH", "Cuốn sách không thuộc chi nhánh phục vụ hiện tại"));
        }

        boolean hasReservation = copy.reservationId() != null;
        boolean reservationExpired = hasReservation && copy.reservationExpiresAt() != null
                && copy.reservationExpiresAt().isBefore(now);
        boolean ownedReservation = hasReservation && readerId.equals(copy.reservationReaderId());
        if (hasReservation && reservationExpired) {
            blocking.add(reason("RESERVATION_EXPIRED", "Phiếu giữ chỗ của cuốn sách đã hết hạn"));
        } else if (hasReservation && !ownedReservation) {
            blocking.add(reason("RESERVED_FOR_ANOTHER_READER", "Cuốn sách đang được giữ cho độc giả khác"));
        } else if (hasReservation) {
            warnings.add(reason("RESERVATION_OWNED_BY_READER", "Cuốn sách đang được giữ đúng cho độc giả đã chọn"));
        }

        if (!AVAILABLE.equals(copy.statusId()) && !RESERVED.equals(copy.statusId())) {
            blocking.add(reason("COPY_STATUS_NOT_BORROWABLE", "Trạng thái cuốn sách không cho phép mượn"));
        } else if (RESERVED.equals(copy.statusId()) && !hasReservation) {
            blocking.add(reason("RESERVATION_CONTEXT_MISSING", "Cuốn đang giữ chỗ nhưng thiếu reservation hợp lệ"));
        }
        return new CopyEligibility(distinct(warnings), distinct(blocking));
    }

    static LocalDateTime expectedDueAt(LocalDateTime borrowedAt, int borrowDays, LocalDate cardExpiry) {
        if (cardExpiry == null) return null;
        LocalDateTime policyDue = borrowedAt.plusDays(borrowDays);
        LocalDateTime cardDue = cardExpiry.minusDays(1).atTime(23, 59, 59);
        LocalDateTime result = policyDue.isBefore(cardDue) ? policyDue : cardDue;
        return result.isAfter(borrowedAt) ? result : null;
    }

    private LoanCopyOptionResponse toOption(
            CopyRow copy,
            String readerId,
            String branchId,
            String query,
            LocalDateTime now
    ) {
        CopyEligibility eligibility = evaluateCopy(copy, branchId, readerId, now);
        boolean exact = copy.copyId().equalsIgnoreCase(query)
                || (copy.barcode() != null && copy.barcode().equalsIgnoreCase(query));
        return new LoanCopyOptionResponse(
                copy.copyId(), copy.titleName(), copy.barcode() == null ? copy.copyId() : copy.barcode(), exact,
                copy.copyId(), copy.barcode(), copy.titleId(), copy.titleName(), copy.isbn(),
                copy.branchId(), copy.branchName(), copy.locationId(), copy.locationLabel(),
                copy.statusId(), copy.statusName(), reservation(copy, readerId),
                eligibility.blockingReasons().isEmpty(), eligibility.warnings(), eligibility.blockingReasons()
        );
    }

    private LoanPreviewResponse.Item toPreviewItem(
            CopyRow copy,
            String readerId,
            List<LoanReasonResponse> warnings,
            List<LoanReasonResponse> blocking,
            LocalDateTime dueAt
    ) {
        return new LoanPreviewResponse.Item(
                copy.copyId(), copy.barcode(), copy.titleId(), copy.titleName(), copy.isbn(),
                copy.locationId(), copy.locationLabel(), copy.statusId(), copy.statusName(),
                reservation(copy, readerId), copy.ruleId(), copy.borrowDays(), dueAt,
                blocking.isEmpty(), warnings, blocking
        );
    }

    private LoanPreviewResponse.Item missingItem(String copyId, LoanReasonResponse missing) {
        return new LoanPreviewResponse.Item(copyId, null, null, null, null, null, null,
                null, null, null, null, null, null, false, List.of(), List.of(missing));
    }

    private LoanCopyOptionResponse.Reservation reservation(CopyRow copy, String readerId) {
        if (copy.reservationId() == null) return null;
        return new LoanCopyOptionResponse.Reservation(
                copy.reservationId(), copy.reservationReaderId(), copy.reservationStatus(),
                copy.reservationExpiresAt(), readerId.equals(copy.reservationReaderId())
        );
    }

    private CopyRow mapCopy(ResultSet rs) throws SQLException {
        return mapCopy(rs, false);
    }

    private CopyRow mapCopy(ResultSet rs, boolean withRule) throws SQLException {
        Timestamp expiry = rs.getTimestamp("NgayHetHanGiuCho");
        return new CopyRow(
                rs.getString("MaCuonSach"), rs.getString("MaVach"), rs.getString("MaDauSach"),
                rs.getString("TenDauSach"), rs.getString("ISBN"), rs.getString("MaChiNhanh"),
                rs.getString("TenChiNhanh"), rs.getString("MaViTri"), rs.getString("MaViTriHienThi"),
                rs.getString("MaTrangThai"), rs.getString("TenTrangThai"),
                rs.getString("MaPhieuDatTruoc"), rs.getString("MaDocGiaDatTruoc"),
                rs.getString("TrangThaiDatTruoc"), expiry == null ? null : expiry.toLocalDateTime(),
                withRule ? rs.getString("MaQuyDinhMuon") : null,
                withRule ? (Integer) rs.getObject("SoNgayMuon") : null
        );
    }

    private List<LoanReasonResponse> convert(List<ReaderEligibilityResponse.Reason> reasons) {
        if (reasons == null) return new ArrayList<>();
        return reasons.stream().map(item -> reason(item.code(), item.message()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private static List<LoanReasonResponse> distinct(List<LoanReasonResponse> reasons) {
        Map<String, LoanReasonResponse> result = new LinkedHashMap<>();
        reasons.forEach(item -> result.putIfAbsent(item.code() + "\u0000" + item.message(), item));
        return List.copyOf(result.values());
    }

    private static LoanReasonResponse reason(String code, String message) {
        return new LoanReasonResponse(code, message);
    }

    private String requireQuery(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Từ khóa cuốn sách không được để trống");
        return value.trim();
    }

    private void validateLimit(int limit) {
        if (limit < 1 || limit > 50) throw new IllegalArgumentException("Giới hạn kết quả phải từ 1 đến 50");
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    record CopyEligibility(List<LoanReasonResponse> warnings, List<LoanReasonResponse> blockingReasons) {
    }

    record CopyRow(
            String copyId,
            String barcode,
            String titleId,
            String titleName,
            String isbn,
            String branchId,
            String branchName,
            String locationId,
            String locationLabel,
            String statusId,
            String statusName,
            String reservationId,
            String reservationReaderId,
            String reservationStatus,
            LocalDateTime reservationExpiresAt,
            String ruleId,
            Integer borrowDays
    ) {
    }
}

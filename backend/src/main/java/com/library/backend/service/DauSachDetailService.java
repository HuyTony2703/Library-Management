package com.library.backend.service;

import com.library.backend.dto.BookBranchCopiesResponse;
import com.library.backend.dto.BookCopyDetailResponse;
import com.library.backend.dto.NhatKyHoatDongResponse;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.repository.NhatKyHoatDongRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DauSachDetailService {
    private static final String OBJECT_TYPE = "DAUSACH";

    private final DauSachRepository dauSachRepository;
    private final NhatKyHoatDongRepository activityRepository;
    private final StaffContextService staffContextService;
    private final JdbcTemplate jdbcTemplate;

    public DauSachDetailService(
            DauSachRepository dauSachRepository,
            NhatKyHoatDongRepository activityRepository,
            StaffContextService staffContextService,
            JdbcTemplate jdbcTemplate
    ) {
        this.dauSachRepository = dauSachRepository;
        this.activityRepository = activityRepository;
        this.staffContextService = staffContextService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<BookBranchCopiesResponse> getCopiesByBranch(String bookId, AuthUser user) {
        requireBook(bookId);
        List<String> allowedBranches = allowedBranches(user);
        if (!RoleConstants.ADMIN.equals(user.getTenVaiTro()) && allowedBranches.isEmpty()) return List.of();

        String branchFilter = RoleConstants.ADMIN.equals(user.getTenVaiTro())
                ? ""
                : " AND cs.MaChiNhanh IN (" + String.join(",", java.util.Collections.nCopies(allowedBranches.size(), "?")) + ")";
        String sql = """
                SELECT cs.MaCuonSach, cs.MaChiNhanh, cn.TenChiNhanh,
                       cs.MaViTri, vt.MaViTriHienThi, cs.MaTrangThai, tt.TenTrangThai,
                       cs.MaVach, cs.MaQRCode, cs.NgayNhapSach
                FROM CUONSACH cs
                INNER JOIN CHINHANH cn ON cn.MaChiNhanh = cs.MaChiNhanh
                INNER JOIN VITRISACH vt ON vt.MaViTri = cs.MaViTri
                INNER JOIN TRANGTHAICUONSACH tt ON tt.MaTrangThai = cs.MaTrangThai
                WHERE cs.MaDauSach = ?
                """ + branchFilter + " ORDER BY cn.TenChiNhanh, cs.MaCuonSach";

        List<Object> args = new ArrayList<>();
        args.add(bookId);
        args.addAll(allowedBranches);
        List<BookCopyDetailResponse> copies = jdbcTemplate.query(sql, (rs, rowNum) -> new BookCopyDetailResponse(
                rs.getString("MaCuonSach"), rs.getString("MaChiNhanh"), rs.getString("TenChiNhanh"),
                rs.getString("MaViTri"), rs.getString("MaViTriHienThi"),
                rs.getString("MaTrangThai"), rs.getString("TenTrangThai"),
                rs.getString("MaVach"), rs.getString("MaQRCode"),
                rs.getDate("NgayNhapSach").toLocalDate()
        ), args.toArray());

        Map<String, List<BookCopyDetailResponse>> grouped = new LinkedHashMap<>();
        copies.forEach(copy -> grouped.computeIfAbsent(copy.branchId(), ignored -> new ArrayList<>()).add(copy));
        return grouped.values().stream().map(branchCopies -> {
            BookCopyDetailResponse first = branchCopies.get(0);
            long available = branchCopies.stream().filter(copy -> "Sẵn có".equals(copy.statusName())).count();
            return new BookBranchCopiesResponse(first.branchId(), first.branchName(), branchCopies.size(), available, branchCopies);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<NhatKyHoatDongResponse> getHistory(String bookId, int limit) {
        requireBook(bookId);
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return activityRepository
                .findByDoiTuongTacDongAndMaDoiTuongTacDongOrderByThoiGianDesc(
                        OBJECT_TYPE, bookId, PageRequest.of(0, safeLimit)
                )
                .stream().map(NhatKyHoatDongResponse::from).toList();
    }

    private List<String> allowedBranches(AuthUser user) {
        if (RoleConstants.ADMIN.equals(user.getTenVaiTro())) return List.of();
        return staffContextService.getContext(user).allowedBranches().stream().map(branch -> branch.id()).toList();
    }

    private void requireBook(String bookId) {
        if (!dauSachRepository.existsById(bookId)) throw new RuntimeException("Không tìm thấy đầu sách");
    }
}

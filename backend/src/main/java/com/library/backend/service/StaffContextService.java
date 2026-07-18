package com.library.backend.service;

import com.library.backend.dto.StaffContextResponse;
import com.library.backend.entity.NhanVien;
import com.library.backend.repository.NhanVienRepository;
import com.library.backend.repository.StaffBranchRepository;
import com.library.backend.repository.StaffBranchRepository.StaffBranch;
import com.library.backend.security.AuthUser;
import com.library.backend.security.AuthenticatedPrincipalService;
import com.library.backend.security.RoleConstants;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class StaffContextService {

    public static final String BLOCK_STAFF_PROFILE_REQUIRED = "STAFF_PROFILE_REQUIRED";
    public static final String BLOCK_NO_DEFAULT_BRANCH = "NO_DEFAULT_BRANCH";
    public static final String BLOCK_NO_ALLOWED_BRANCH = "NO_ALLOWED_BRANCH";

    private static final String PERMISSION_CONTEXT_READ = "STAFF_CONTEXT_READ";
    private static final String PERMISSION_ADMIN_ACCESS = "ADMIN_ACCESS";
    private static final ZoneId LIBRARY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final List<String> OPERATIONAL_PERMISSIONS = List.of(
            "LOAN_CREATE",
            "RETURN_CREATE",
            "PAYMENT_CREATE"
    );

    private final AuthenticatedPrincipalService authenticatedPrincipalService;
    private final NhanVienRepository nhanVienRepository;
    private final StaffBranchRepository staffBranchRepository;

    public StaffContextService(
            AuthenticatedPrincipalService authenticatedPrincipalService,
            NhanVienRepository nhanVienRepository,
            StaffBranchRepository staffBranchRepository
    ) {
        this.authenticatedPrincipalService = authenticatedPrincipalService;
        this.nhanVienRepository = nhanVienRepository;
        this.staffBranchRepository = staffBranchRepository;
    }

    @Transactional(readOnly = true)
    public StaffContextResponse getContext(AuthUser tokenUser) {
        AuthUser user = authenticatedPrincipalService.refresh(tokenUser);

        if (!isStaffRole(user)) {
            throw new AccessDeniedException("Tài khoản không có quyền truy cập staff context");
        }

        NhanVien staff = user.getMaNhanVien() == null
                ? null
                : nhanVienRepository.findById(user.getMaNhanVien()).orElse(null);

        List<StaffBranch> branches = staff == null
                ? List.of()
                : staffBranchRepository.findEffectiveBranches(staff, LocalDate.now(LIBRARY_ZONE));

        StaffBranch defaultBranch = branches.stream()
                .filter(StaffBranch::defaultBranch)
                .findFirst()
                .orElse(null);

        String blockReason = determineBlockReason(staff, branches, defaultBranch);
        boolean operational = blockReason == null;

        List<String> permissions = new ArrayList<>();
        permissions.add(PERMISSION_CONTEXT_READ);
        if (RoleConstants.ADMIN.equals(user.getTenVaiTro())) {
            permissions.add(PERMISSION_ADMIN_ACCESS);
        }
        if (operational) {
            permissions.addAll(OPERATIONAL_PERMISSIONS);
        }
        if (RoleConstants.ADMIN.equals(user.getTenVaiTro())
                || (RoleConstants.LIBRARIAN.equals(user.getTenVaiTro()) && staff != null)) {
            permissions.add(ReaderPasswordResetService.PERMISSION);
        }

        return new StaffContextResponse(
                user.getMaTaiKhoan(),
                staff == null ? null : staff.getMaNhanVien(),
                staff == null ? user.getHoTen() : staff.getHoTen(),
                new StaffContextResponse.RoleSummary(user.getMaVaiTro(), user.getTenVaiTro()),
                toBranchSummary(defaultBranch),
                branches.stream().map(this::toBranchSummary).toList(),
                permissions,
                operational,
                blockReason
        );
    }

    private boolean isStaffRole(AuthUser user) {
        return RoleConstants.LIBRARIAN.equals(user.getTenVaiTro())
                || RoleConstants.ADMIN.equals(user.getTenVaiTro());
    }

    private String determineBlockReason(
            NhanVien staff,
            List<StaffBranch> branches,
            StaffBranch defaultBranch
    ) {
        if (staff == null) {
            return BLOCK_STAFF_PROFILE_REQUIRED;
        }
        if (branches.isEmpty()) {
            return BLOCK_NO_ALLOWED_BRANCH;
        }
        if (defaultBranch == null) {
            return BLOCK_NO_DEFAULT_BRANCH;
        }
        return null;
    }

    private StaffContextResponse.BranchSummary toBranchSummary(StaffBranch branch) {
        if (branch == null) {
            return null;
        }
        return new StaffContextResponse.BranchSummary(branch.id(), branch.name());
    }
}

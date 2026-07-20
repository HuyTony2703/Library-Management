package com.library.backend.service;

import com.library.backend.dto.StaffContextResponse;
import com.library.backend.entity.NhanVien;
import com.library.backend.repository.NhanVienRepository;
import com.library.backend.repository.StaffBranchRepository;
import com.library.backend.repository.StaffBranchRepository.StaffBranch;
import com.library.backend.security.AuthUser;
import com.library.backend.security.AuthenticatedPrincipalService;
import com.library.backend.security.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffContextServiceTest {

    @Mock
    private AuthenticatedPrincipalService authenticatedPrincipalService;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private StaffBranchRepository staffBranchRepository;

    private StaffContextService service;

    @BeforeEach
    void setUp() {
        service = new StaffContextService(
                authenticatedPrincipalService,
                nhanVienRepository,
                staffBranchRepository
        );
    }

    @Test
    void returnsLibrarianDefaultAndAllowedBranches() {
        AuthUser librarian = user("TK_LIB", "NV001", RoleConstants.LIBRARIAN);
        NhanVien staff = staff("NV001", "Thủ thư A");
        when(authenticatedPrincipalService.refresh(librarian)).thenReturn(librarian);
        when(nhanVienRepository.findById("NV001")).thenReturn(Optional.of(staff));
        when(staffBranchRepository.findEffectiveBranches(eq(staff), any(LocalDate.class)))
                .thenReturn(List.of(
                        new StaffBranch("CN_A", "Chi nhánh A", true),
                        new StaffBranch("CN_B", "Chi nhánh B", false)
                ));

        StaffContextResponse context = service.getContext(librarian);

        assertThat(context.accountId()).isEqualTo("TK_LIB");
        assertThat(context.staffId()).isEqualTo("NV001");
        assertThat(context.defaultBranch().id()).isEqualTo("CN_A");
        assertThat(context.allowedBranches()).extracting(StaffContextResponse.BranchSummary::id)
                .containsExactly("CN_A", "CN_B");
        assertThat(context.operational()).isTrue();
        assertThat(context.operationalBlockReason()).isNull();
        assertThat(context.permissions()).contains("LOAN_CREATE", "RETURN_CREATE", "PAYMENT_CREATE");
    }

    @Test
    void returnsNonOperationalAdminContextWhenNoStaffProfileExists() {
        AuthUser admin = user("TK_ADMIN", null, RoleConstants.ADMIN);
        when(authenticatedPrincipalService.refresh(admin)).thenReturn(admin);

        StaffContextResponse context = service.getContext(admin);

        assertThat(context.staffId()).isNull();
        assertThat(context.allowedBranches()).isEmpty();
        assertThat(context.operational()).isFalse();
        assertThat(context.operationalBlockReason())
                .isEqualTo(StaffContextService.BLOCK_STAFF_PROFILE_REQUIRED);
        assertThat(context.permissions()).contains("ADMIN_ACCESS").doesNotContain("LOAN_CREATE");
    }

    @Test
    void returnsNonOperationalContextWhenStaffHasNoBranch() {
        AuthUser librarian = user("TK_NO_BRANCH", "NV_NO_BRANCH", RoleConstants.LIBRARIAN);
        NhanVien staff = staff("NV_NO_BRANCH", "Thủ thư chưa phân chi nhánh");
        when(authenticatedPrincipalService.refresh(librarian)).thenReturn(librarian);
        when(nhanVienRepository.findById("NV_NO_BRANCH")).thenReturn(Optional.of(staff));
        when(staffBranchRepository.findEffectiveBranches(eq(staff), any(LocalDate.class)))
                .thenReturn(List.of());

        StaffContextResponse context = service.getContext(librarian);

        assertThat(context.defaultBranch()).isNull();
        assertThat(context.operational()).isFalse();
        assertThat(context.operationalBlockReason())
                .isEqualTo(StaffContextService.BLOCK_NO_ALLOWED_BRANCH);
        assertThat(context.permissions()).doesNotContain("LOAN_CREATE");
    }

    @Test
    void rejectsReaderWithoutLoadingStaffData() {
        AuthUser reader = user("TK_READER", null, RoleConstants.READER);
        when(authenticatedPrincipalService.refresh(reader)).thenReturn(reader);

        assertThatThrownBy(() -> service.getContext(reader))
                .isInstanceOf(AccessDeniedException.class);
    }

    private AuthUser user(String accountId, String staffId, String roleName) {
        return new AuthUser(
                accountId,
                accountId.toLowerCase(),
                roleName.equals(RoleConstants.ADMIN) ? "VT_ADMIN" : "VT_TEST",
                roleName,
                null,
                staffId,
                staffId == null ? "Tài khoản quản trị" : "Nhân viên"
        );
    }

    private NhanVien staff(String id, String name) {
        NhanVien staff = new NhanVien();
        staff.setMaNhanVien(id);
        staff.setHoTen(name);
        staff.setTrangThai("Đang làm");
        return staff;
    }
}

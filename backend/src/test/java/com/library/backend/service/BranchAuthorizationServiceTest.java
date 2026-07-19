package com.library.backend.service;

import com.library.backend.dto.StaffContextResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchAuthorizationServiceTest {

    @Mock
    private StaffContextService staffContextService;

    @Test
    void allowsOnlyBranchesFromBackendContext() {
        AuthUser user = user();
        when(staffContextService.getContext(user)).thenReturn(context(true, "CN_A"));
        BranchAuthorizationService service = new BranchAuthorizationService(staffContextService);

        assertThat(service.canAccessBranch(user, "CN_A")).isTrue();
        assertThat(service.canAccessBranch(user, "CN_B")).isFalse();
    }

    @Test
    void rejectsAllBranchesForNonOperationalStaff() {
        AuthUser user = user();
        when(staffContextService.getContext(user)).thenReturn(context(false, "CN_A"));
        BranchAuthorizationService service = new BranchAuthorizationService(staffContextService);

        assertThatThrownBy(() -> service.requireAllowedBranch(user, "CN_A"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private StaffContextResponse context(boolean operational, String branchId) {
        StaffContextResponse.BranchSummary branch = new StaffContextResponse.BranchSummary(
                branchId,
                "Chi nhánh A"
        );
        return new StaffContextResponse(
                "TK_LIB",
                "NV001",
                "Thủ thư A",
                new StaffContextResponse.RoleSummary("VT_THU_THU", RoleConstants.LIBRARIAN),
                branch,
                List.of(branch),
                List.of("STAFF_CONTEXT_READ"),
                operational,
                operational ? null : StaffContextService.BLOCK_NO_ALLOWED_BRANCH
        );
    }

    private AuthUser user() {
        return new AuthUser(
                "TK_LIB",
                "thuthu",
                "VT_THU_THU",
                RoleConstants.LIBRARIAN,
                null,
                "NV001",
                "Thủ thư A"
        );
    }
}

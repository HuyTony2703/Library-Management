package com.library.backend.service;

import com.library.backend.dto.StaffContextResponse;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.repository.NhatKyHoatDongRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DauSachDetailServiceTest {

    @Test
    void librarianWithoutAllowedBranchesCannotReadAnyCopies() {
        DauSachRepository bookRepository = mock(DauSachRepository.class);
        NhatKyHoatDongRepository activityRepository = mock(NhatKyHoatDongRepository.class);
        StaffContextService staffContextService = mock(StaffContextService.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DauSachDetailService service = new DauSachDetailService(
                bookRepository, activityRepository, staffContextService, jdbcTemplate
        );
        AuthUser librarian = new AuthUser(
                "TK1", "staff", "VT", RoleConstants.LIBRARIAN, null, "NV1", "Thủ thư"
        );
        when(bookRepository.existsById("DS1")).thenReturn(true);
        when(staffContextService.getContext(librarian)).thenReturn(new StaffContextResponse(
                "TK1", "NV1", "Thủ thư",
                new StaffContextResponse.RoleSummary("VT", RoleConstants.LIBRARIAN),
                null, List.of(), List.of("STAFF_CONTEXT_READ"), false, "NO_ALLOWED_BRANCH"
        ));

        assertThat(service.getCopiesByBranch("DS1", librarian)).isEmpty();
        verifyNoInteractions(jdbcTemplate);
    }
}

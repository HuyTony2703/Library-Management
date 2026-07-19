package com.library.backend.service;

import com.library.backend.dto.CuonSachListItemResponse;
import com.library.backend.dto.CuonSachListQuery;
import com.library.backend.dto.PageResponse;
import com.library.backend.dto.StaffContextResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.repository.CuonSachPageRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuonSachListServiceTest {
    @Mock CuonSachPageRepository pageRepository;
    @Mock StaffContextService staffContextService;

    @Test
    void adminCanUseUnrestrictedBranchScope() {
        CuonSachListQuery query = query(List.of("CN1"), null, null);
        PageResponse<CuonSachListItemResponse> expected = new PageResponse<>(List.of(), 1, 20, 0, 0);
        when(pageRepository.findPage(query, List.of("CN1"), true)).thenReturn(expected);

        assertThat(service().getPage(query, user(RoleConstants.ADMIN))).isSameAs(expected);
        verify(pageRepository).findPage(query, List.of("CN1"), true);
    }

    @Test
    void librarianWithoutExplicitFilterIsScopedToAllowedBranches() {
        AuthUser librarian = user(RoleConstants.LIBRARIAN);
        CuonSachListQuery query = query(List.of(), null, null);
        PageResponse<CuonSachListItemResponse> expected = new PageResponse<>(List.of(), 1, 20, 0, 0);
        when(staffContextService.getContext(librarian)).thenReturn(context("CN1", "CN2"));
        when(pageRepository.findPage(query, List.of("CN1", "CN2"), false)).thenReturn(expected);

        assertThat(service().getPage(query, librarian)).isSameAs(expected);
        verify(pageRepository).findPage(query, List.of("CN1", "CN2"), false);
    }

    @Test
    void librarianCannotRequestBranchOutsideScope() {
        AuthUser librarian = user(RoleConstants.LIBRARIAN);
        CuonSachListQuery query = query(List.of("CN2"), null, null);
        when(staffContextService.getContext(librarian)).thenReturn(context("CN1"));

        assertThatThrownBy(() -> service().getPage(query, librarian))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("CN2");
        verify(pageRepository, never()).findPage(query, List.of("CN2"), false);
    }

    @Test
    void legacyUnpagedListIsDisabled() {
        AuthUser librarian = user(RoleConstants.LIBRARIAN);

        assertThatThrownBy(() -> service().getAllLegacy(librarian))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("phan trang");
        verify(pageRepository, never()).findAll(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void returnsEmptyPageWhenLibrarianHasNoAllowedBranches() {
        AuthUser librarian = user(RoleConstants.LIBRARIAN);
        CuonSachListQuery query = query(List.of(), null, null);
        when(staffContextService.getContext(librarian)).thenReturn(context());

        PageResponse<CuonSachListItemResponse> result = service().getPage(query, librarian);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalItems()).isZero();
        verify(pageRepository, never()).findPage(query, List.of(), false);
    }

    @Test
    void rejectsReversedImportDateRange() {
        CuonSachListQuery query = query(List.of(), LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 1));
        assertThatThrownBy(() -> service().getPage(query, user(RoleConstants.ADMIN)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ngày nhập");
    }

    private CuonSachListService service() {
        return new CuonSachListService(pageRepository, staffContextService);
    }

    private CuonSachListQuery query(List<String> branches, LocalDate from, LocalDate to) {
        return new CuonSachListQuery(
                1, 20, null, "importedAt,desc", List.of(), branches, List.of(),
                List.of(), List.of(), List.of(), from, to, null, null
        );
    }

    private AuthUser user(String role) {
        return new AuthUser("TK1", "user", "VT", role, null, "NV1", "Người dùng");
    }

    private StaffContextResponse context(String... branches) {
        List<StaffContextResponse.BranchSummary> allowed = java.util.Arrays.stream(branches)
                .map(id -> new StaffContextResponse.BranchSummary(id, id))
                .toList();
        return new StaffContextResponse(
                "TK1", "NV1", "Thủ thư",
                new StaffContextResponse.RoleSummary("VT", RoleConstants.LIBRARIAN),
                allowed.isEmpty() ? null : allowed.get(0), allowed, List.of(), !allowed.isEmpty(),
                allowed.isEmpty() ? "NO_ALLOWED_BRANCH" : null
        );
    }
}

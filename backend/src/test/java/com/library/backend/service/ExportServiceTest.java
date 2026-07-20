package com.library.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.backend.dto.DauSachListQuery;
import com.library.backend.dto.ExportRequest;
import com.library.backend.exception.BusinessException;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {
    @Mock DauSachService dauSachService;
    @Mock CuonSachListService cuonSachListService;
    @Mock ReaderQueryService readerQueryService;
    @Mock ActivityLogService activityLogService;

    @Test
    void legacyJobLookupWithoutOwnerIsRejected() {
        assertThatThrownBy(() -> service().getJob("job-1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allMatchingExportAboveHardLimitIsRejectedBeforeBuildingRows() {
        when(dauSachService.getMatchingIds(any(DauSachListQuery.class), anyList(), org.mockito.ArgumentMatchers.eq(5001)))
                .thenReturn(IntStream.range(0, 5001).mapToObj(i -> "DS" + i).toList());

        assertThatThrownBy(() -> service().exportBooks(
                new ExportRequest(ExportRequest.ScopeType.ALL_MATCHING, List.of(), Map.of(), List.of()),
                admin()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Export");

        verify(dauSachService, never()).getByIds(anyList());
        verify(dauSachService).getMatchingIds(any(DauSachListQuery.class), anyList(), org.mockito.ArgumentMatchers.eq(5001));
    }

    private ExportService service() {
        return new ExportService(dauSachService, cuonSachListService, readerQueryService,
                activityLogService, new ObjectMapper());
    }

    private AuthUser admin() {
        return new AuthUser("TK_ADMIN", "admin", "VT_ADMIN", RoleConstants.ADMIN,
                null, null, "Admin");
    }
}

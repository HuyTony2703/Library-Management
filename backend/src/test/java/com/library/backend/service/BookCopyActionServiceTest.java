package com.library.backend.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookCopyActionServiceTest {
    @Mock CuonSachRepository copyRepository;
    @Mock ChiNhanhRepository branchRepository;
    @Mock ViTriSachRepository locationRepository;
    @Mock BranchAuthorizationService branchAuthorizationService;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock ActivityLogService activityLogService;

    private BookCopyActionService service;

    @BeforeEach
    void setUp() {
        service = new BookCopyActionService(copyRepository, branchRepository, locationRepository,
                branchAuthorizationService, jdbcTemplate, activityLogService);
    }

    @Test
    void availableCopyCanBeMarkedDamagedWithBeforeAfterAudit() {
        CuonSach copy = allowCopy(BookCopyActionService.AVAILABLE);
        var response = service.applyCondition("CS1", condition(BookCopyConditionRequest.Action.MARK_DAMAGED), admin());

        assertThat(copy.getMaTrangThai()).isEqualTo(BookCopyActionService.DAMAGED);
        assertThat(response.previousStatus()).isEqualTo(BookCopyActionService.AVAILABLE);
        assertThat(response.currentStatus()).isEqualTo(BookCopyActionService.DAMAGED);
        verify(jdbcTemplate).update(contains("INSERT INTO CUONSACH_TRANGTHAI_EVENT"), any(Object[].class));
        verify(activityLogService).logAsAccount(anyString(), anyString(), anyString(), anyString(), contains("TT_SANCO->TT_HONG"));
    }

    @Test
    void borrowedAndReservedStatesCannotBeChangedManually() {
        allowCopy(BookCopyActionService.BORROWED);
        assertThatThrownBy(() -> service.applyCondition("CS1", condition(BookCopyConditionRequest.Action.MARK_LOST), admin()))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("nghiệp vụ");
        verify(copyRepository, never()).saveAndFlush(any());

        CuonSach reserved = copy(BookCopyActionService.RESERVED);
        when(copyRepository.findByIdForUpdate("CS2")).thenReturn(Optional.of(reserved));
        assertThatThrownBy(() -> service.applyCondition("CS2", condition(BookCopyConditionRequest.Action.WITHDRAW), admin()))
                .isInstanceOf(CatalogValidationException.class);
    }

    @Test
    void restoreActionsOnlyAcceptTheirMatchingSourceState() {
        allowCopy(BookCopyActionService.DAMAGED);
        assertThat(service.applyCondition("CS1", condition(BookCopyConditionRequest.Action.RESTORE_AFTER_REPAIR), admin()).currentStatus())
                .isEqualTo(BookCopyActionService.AVAILABLE);

        allowCopy(BookCopyActionService.AVAILABLE);
        assertThatThrownBy(() -> service.applyCondition("CS1", condition(BookCopyConditionRequest.Action.RESTORE_FOUND), admin()))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("Bị mất");
    }

    @Test
    void moveLocationStaysInsideCurrentBranchAndWritesHistory() {
        CuonSach copy = allowCopy(BookCopyActionService.AVAILABLE);
        when(locationRepository.existsActiveInBranch("VT2", "CN1")).thenReturn(true);

        var response = service.moveLocation("CS1", new BookCopyMoveRequest("VT2", "Sắp xếp lại kho"), admin());

        assertThat(copy.getMaViTri()).isEqualTo("VT2");
        assertThat(response.previousLocationId()).isEqualTo("VT1");
        assertThat(response.currentLocationId()).isEqualTo("VT2");
        verify(jdbcTemplate).update(contains("INSERT INTO CUONSACH_VITRI_EVENT"), any(Object[].class));
    }

    @Test
    void moveRejectsLocationOutsideBranchAndLostCopy() {
        allowCopy(BookCopyActionService.AVAILABLE);
        when(locationRepository.existsActiveInBranch("VT_OTHER", "CN1")).thenReturn(false);
        assertThatThrownBy(() -> service.moveLocation("CS1", new BookCopyMoveRequest("VT_OTHER", "Di chuyển"), admin()))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("không thuộc chi nhánh");

        allowCopy(BookCopyActionService.LOST);
        assertThatThrownBy(() -> service.moveLocation("CS1", new BookCopyMoveRequest("VT2", "Di chuyển"), admin()))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("bị đánh dấu mất");
    }

    @Test
    void librarianAuthorizationAlwaysUsesBackendBranchContext() {
        allowCopy(BookCopyActionService.AVAILABLE);
        AuthUser librarian = new AuthUser("TK2", "staff", "VT_STAFF", RoleConstants.LIBRARIAN,
                null, "NV2", "Thủ thư");

        service.applyCondition("CS1", condition(BookCopyConditionRequest.Action.WITHDRAW), librarian);

        verify(branchAuthorizationService).requireAllowedBranch(librarian, "CN1");
    }

    @Test
    void inconsistentOpenLoanBlocksManualAction() {
        allowCopy(BookCopyActionService.AVAILABLE);
        when(jdbcTemplate.queryForObject(contains("CHITIETPHIEUMUON"), any(Class.class), any(Object[].class)))
                .thenReturn(1);

        assertThatThrownBy(() -> service.applyCondition("CS1", condition(BookCopyConditionRequest.Action.MARK_DAMAGED), admin()))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("đang mở");
        verify(copyRepository, never()).saveAndFlush(any());
    }

    private CuonSach allowCopy(String status) {
        CuonSach copy = copy(status);
        when(copyRepository.findByIdForUpdate("CS1")).thenReturn(Optional.of(copy));
        ChiNhanh branch = new ChiNhanh();
        branch.setMaChiNhanh("CN1");
        branch.setTrangThai("Hoạt động");
        when(branchRepository.findById("CN1")).thenReturn(Optional.of(branch));
        return copy;
    }

    private CuonSach copy(String status) {
        CuonSach copy = new CuonSach();
        copy.setMaCuonSach(status.equals(BookCopyActionService.RESERVED) ? "CS2" : "CS1");
        copy.setMaChiNhanh("CN1");
        copy.setMaViTri("VT1");
        copy.setMaTrangThai(status);
        return copy;
    }

    private BookCopyConditionRequest condition(BookCopyConditionRequest.Action action) {
        return new BookCopyConditionRequest(action, BookCopyConditionRequest.Severity.MEDIUM,
                List.of("TORN_COVER"), "Mô tả", "Lý do kiểm kê");
    }

    private AuthUser admin() {
        return new AuthUser("TK1", "admin", "VT_ADMIN", RoleConstants.ADMIN, null, null, "Admin");
    }
}

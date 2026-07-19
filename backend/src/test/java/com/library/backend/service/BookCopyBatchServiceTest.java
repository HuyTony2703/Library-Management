package com.library.backend.service;

import com.library.backend.dto.BookCopyBatchRequest;
import com.library.backend.dto.BookCopyBatchResponse;
import com.library.backend.entity.ChiNhanh;
import com.library.backend.entity.DauSach;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.repository.ChiNhanhRepository;
import com.library.backend.repository.CuonSachRepository;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.repository.TrangThaiCuonSachRepository;
import com.library.backend.repository.ViTriSachRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookCopyBatchServiceTest {
    @Mock CuonSachRepository copyRepository;
    @Mock DauSachRepository titleRepository;
    @Mock ChiNhanhRepository branchRepository;
    @Mock ViTriSachRepository locationRepository;
    @Mock TrangThaiCuonSachRepository statusRepository;
    @Mock BranchAuthorizationService branchAuthorizationService;
    @Mock ActivityLogService activityLogService;

    private BookCopyBatchService service;

    @BeforeEach
    void setUp() {
        service = new BookCopyBatchService(copyRepository, titleRepository, branchRepository,
                locationRepository, statusRepository, branchAuthorizationService, activityLogService);
    }

    @Test
    void twoConcurrentAutoBatchesNeverShareCopyIdsOrBarcodes() throws Exception {
        allowValidContext();
        when(copyRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        BookCopyBatchRequest request = autoRequest(50);

        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<BookCopyBatchResponse> task = () -> service.create(request, admin());
            List<BookCopyBatchResponse> results = executor.invokeAll(List.of(task, task)).stream()
                    .map(future -> {
                        try { return future.get(); }
                        catch (Exception ex) { throw new RuntimeException(ex); }
                    }).toList();

            assertThat(results).extracting(BookCopyBatchResponse::created).containsExactly(50, 50);
            assertThat(results.stream().flatMap(result -> result.copies().stream()).map(BookCopyBatchResponse.CreatedCopy::id))
                    .doesNotHaveDuplicates().hasSize(100);
            assertThat(results.stream().flatMap(result -> result.copies().stream()).map(BookCopyBatchResponse.CreatedCopy::barcode))
                    .doesNotHaveDuplicates().hasSize(100);
            assertThat(results.stream().flatMap(result -> result.copies().stream()).map(BookCopyBatchResponse.CreatedCopy::status))
                    .containsOnly(BookCopyBatchService.AVAILABLE_STATUS);
        }
    }

    @Test
    void rejectsDuplicateManualBarcodeInsideBatchAndSavesNothing() {
        allowValidContext();
        BookCopyBatchRequest request = manualRequest("BC-1", "bc-1");

        assertThatThrownBy(() -> service.create(request, admin()))
                .isInstanceOf(CatalogValidationException.class)
                .extracting(ex -> ((CatalogValidationException) ex).getErrorCode())
                .isEqualTo("DUPLICATE_BARCODE");
        verify(copyRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void rejectsManualBarcodeAlreadyInDatabaseAndSavesNothing() {
        allowValidContext();
        when(copyRepository.existsByMaVach("BC-OLD")).thenReturn(true);

        assertThatThrownBy(() -> service.create(manualRequest("BC-OLD"), admin()))
                .isInstanceOf(CatalogValidationException.class)
                .extracting(ex -> ((CatalogValidationException) ex).getErrorCode())
                .isEqualTo("DUPLICATE_BARCODE");
        verify(copyRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void rejectsLocationOutsideSelectedBranch() {
        allowValidContext();
        when(locationRepository.existsActiveInBranch("VT1", "CN1")).thenReturn(false);

        assertThatThrownBy(() -> service.create(autoRequest(2), admin()))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("không thuộc chi nhánh");
        verify(copyRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void rejectsInactiveTitleEvenIfClientSubmitsItsId() {
        allowValidContext();
        DauSach inactive = activeTitle();
        inactive.setTrangThai("Ngừng hiển thị");
        when(titleRepository.findById("DS1")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.create(autoRequest(2), admin()))
                .isInstanceOf(CatalogValidationException.class)
                .extracting(ex -> ((CatalogValidationException) ex).getErrorCode())
                .isEqualTo("TITLE_INACTIVE");
        verify(copyRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void librarianBranchComesFromBackendAuthorizationContext() {
        allowValidContext();
        when(copyRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        AuthUser librarian = new AuthUser("TK2", "staff", "VT_STAFF", RoleConstants.LIBRARIAN,
                null, "NV2", "Thủ thư");

        service.create(autoRequest(1), librarian);

        verify(branchAuthorizationService).requireAllowedBranch(librarian, "CN1");
    }

    @Test
    void databaseConflictFailsTheWholeBatchBeforeAuditOrResult() {
        allowValidContext();
        when(copyRepository.saveAllAndFlush(anyList()))
                .thenThrow(new DataIntegrityViolationException("UX_CUONSACH_MAVACH"));

        assertThatThrownBy(() -> service.create(autoRequest(3), admin()))
                .isInstanceOf(CatalogValidationException.class)
                .extracting(ex -> ((CatalogValidationException) ex).getErrorCode())
                .isEqualTo("BATCH_CODE_CONFLICT");
        verify(activityLogService, never()).logAsAccount(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private void allowValidContext() {
        ChiNhanh branch = new ChiNhanh();
        branch.setMaChiNhanh("CN1");
        branch.setTrangThai("Hoạt động");
        when(branchRepository.findById("CN1")).thenReturn(Optional.of(branch));
        when(titleRepository.findById("DS1")).thenReturn(Optional.of(activeTitle()));
        lenient().when(locationRepository.existsActiveInBranch("VT1", "CN1")).thenReturn(true);
        lenient().when(statusRepository.existsById(BookCopyBatchService.AVAILABLE_STATUS)).thenReturn(true);
    }

    private DauSach activeTitle() {
        DauSach title = new DauSach();
        title.setMaDauSach("DS1");
        title.setTrangThai("Hoạt động");
        return title;
    }

    private BookCopyBatchRequest autoRequest(int quantity) {
        return new BookCopyBatchRequest("DS1", "CN1", "VT1", LocalDate.of(2026, 6, 22), quantity,
                BookCopyBatchRequest.BarcodeMode.AUTO, "Lô test", null);
    }

    private BookCopyBatchRequest manualRequest(String... barcodes) {
        List<BookCopyBatchRequest.ManualCopy> copies = java.util.Arrays.stream(barcodes)
                .map(barcode -> new BookCopyBatchRequest.ManualCopy(barcode, null)).toList();
        return new BookCopyBatchRequest("DS1", "CN1", "VT1", LocalDate.of(2026, 6, 22), null,
                BookCopyBatchRequest.BarcodeMode.MANUAL, null, copies);
    }

    private AuthUser admin() {
        return new AuthUser("TK1", "admin", "VT_ADMIN", RoleConstants.ADMIN, null, null, "Admin");
    }
}

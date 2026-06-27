package com.library.backend.controller;

import com.library.backend.dto.DocGiaRequest;
import com.library.backend.dto.DocGiaResponse;
import com.library.backend.dto.ReaderMembershipUpdateRequest;
import com.library.backend.dto.PageResponse;
import com.library.backend.dto.ReaderDebtItemResponse;
import com.library.backend.dto.ReaderListItemResponse;
import com.library.backend.dto.ReaderListQuery;
import com.library.backend.dto.ReaderLoanItemResponse;
import com.library.backend.dto.ReaderMembershipResponse;
import com.library.backend.dto.ReaderOverviewResponse;
import com.library.backend.dto.ReaderTransactionResponse;
import com.library.backend.dto.ReaderEligibilityResponse;
import com.library.backend.dto.ReaderLifecycleRequest;
import com.library.backend.dto.ReaderLockRequest;
import com.library.backend.dto.ReaderStateActionResponse;
import com.library.backend.dto.ReaderUnlockRequest;
import com.library.backend.dto.ReaderPasswordResetRequest;
import com.library.backend.dto.ReaderPasswordResetResponse;
import com.library.backend.dto.ReaderCreateRequest;
import com.library.backend.dto.ReaderProfileUpdateRequest;
import com.library.backend.dto.BulkActionRequest;
import com.library.backend.dto.BulkActionResponse;
import com.library.backend.dto.ExportRequest;
import com.library.backend.dto.ExportResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.security.AuthUser;
import com.library.backend.service.DocGiaService;
import com.library.backend.service.ReaderQueryService;
import com.library.backend.service.ReaderStateService;
import com.library.backend.service.ReaderPasswordResetService;
import com.library.backend.service.BulkActionService;
import com.library.backend.service.ExportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/readers")
@Validated
public class DocGiaController {

    private final DocGiaService docGiaService;
    private final ReaderQueryService readerQueryService;
    private final ReaderStateService readerStateService;
    private final ReaderPasswordResetService readerPasswordResetService;
    private final BulkActionService bulkActionService;
    private final ExportService exportService;

    public DocGiaController(DocGiaService docGiaService, ReaderQueryService readerQueryService,
                            ReaderStateService readerStateService,
                            ReaderPasswordResetService readerPasswordResetService,
                            BulkActionService bulkActionService,
                            ExportService exportService) {
        this.docGiaService = docGiaService;
        this.readerQueryService = readerQueryService;
        this.readerStateService = readerStateService;
        this.readerPasswordResetService = readerPasswordResetService;
        this.bulkActionService = bulkActionService;
        this.exportService = exportService;
    }

    @GetMapping(params = "!page")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public List<DocGiaResponse> getAll() {
        return docGiaService.getAll();
    }

    @GetMapping(params = "page")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public PageResponse<ReaderListItemResponse> getPage(
            @RequestParam @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "fullName,asc") String sort,
            @RequestParam(required = false) List<String> groupIds,
            @RequestParam(required = false) List<String> planIds,
            @RequestParam(required = false) List<String> profileStatuses,
            @RequestParam(required = false) List<String> accountStatuses,
            @RequestParam(required = false) String cardStatus,
            @RequestParam(required = false) String membershipStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cardExpiryFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cardExpiryTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate membershipExpiryFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate membershipExpiryTo,
            @RequestParam(required = false) Boolean locked
    ) {
        return readerQueryService.getPage(new ReaderListQuery(page, pageSize, search, sort, groupIds, planIds,
                profileStatuses, accountStatuses, cardStatus, membershipStatus, cardExpiryFrom, cardExpiryTo,
                membershipExpiryFrom, membershipExpiryTo, locked));
    }

    @GetMapping("/{maDocGia}/overview")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ReaderOverviewResponse getOverview(@PathVariable String maDocGia) {
        return readerQueryService.getOverview(maDocGia);
    }

    @GetMapping("/{maDocGia}/memberships")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public List<ReaderMembershipResponse> getMemberships(@PathVariable String maDocGia) {
        return readerQueryService.getMemberships(maDocGia);
    }

    @GetMapping("/{maDocGia}/current-loans-detail")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public List<ReaderLoanItemResponse> getCurrentLoansDetail(@PathVariable String maDocGia) {
        return readerQueryService.getCurrentLoans(maDocGia);
    }

    @GetMapping("/{maDocGia}/debts-detail")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public List<ReaderDebtItemResponse> getDebtsDetail(@PathVariable String maDocGia) {
        return readerQueryService.getDebts(maDocGia);
    }

    @GetMapping("/{maDocGia}/transactions")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public PageResponse<ReaderTransactionResponse> getTransactions(
            @PathVariable String maDocGia,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        return readerQueryService.getTransactions(maDocGia, page, pageSize);
    }

    @GetMapping("/{maDocGia}/borrowing-context")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ReaderEligibilityResponse getBorrowingContext(@PathVariable String maDocGia) {
        return readerStateService.eligibility(maDocGia);
    }

    @PostMapping("/{maDocGia}/locks")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ReaderStateActionResponse lock(@PathVariable String maDocGia,
                                          @Valid @RequestBody ReaderLockRequest request,
                                          @AuthenticationPrincipal AuthUser user) {
        return readerStateService.lock(maDocGia, request, user);
    }

    @PostMapping("/{maDocGia}/unlock")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ReaderStateActionResponse unlock(@PathVariable String maDocGia,
                                            @Valid @RequestBody ReaderUnlockRequest request,
                                            @AuthenticationPrincipal AuthUser user) {
        return readerStateService.unlock(maDocGia, request, user);
    }

    @PostMapping("/{maDocGia}/deactivate")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ReaderStateActionResponse deactivate(@PathVariable String maDocGia,
                                                @Valid @RequestBody ReaderLifecycleRequest request,
                                                @AuthenticationPrincipal AuthUser user) {
        return readerStateService.deactivate(maDocGia, request, user);
    }

    @PostMapping("/bulk-actions")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public BulkActionResponse bulkAction(
            @Valid @RequestBody BulkActionRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return bulkActionService.applyReaders(request, user);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ExportResponse export(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return exportService.exportReaders(request, user);
    }

    @PostMapping("/{maDocGia}/reactivate")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ReaderStateActionResponse reactivate(@PathVariable String maDocGia,
                                                @Valid @RequestBody ReaderLifecycleRequest request,
                                                @AuthenticationPrincipal AuthUser user) {
        return readerStateService.reactivate(maDocGia, request, user);
    }

    @PostMapping("/{maDocGia}/password-reset")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ResponseEntity<ReaderPasswordResetResponse> resetPassword(
            @PathVariable String maDocGia,
            @Valid @RequestBody ReaderPasswordResetRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .body(readerPasswordResetService.reset(maDocGia, request, user));
    }

    @GetMapping("/{maDocGia}")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public DocGiaResponse getById(@PathVariable String maDocGia) {
        return docGiaService.getById(maDocGia);
    }

    @PostMapping
    public DocGiaResponse create(@Valid @RequestBody ReaderCreateRequest request) {
        return docGiaService.create(request.toLegacyRequest());
    }

    @PatchMapping("/{maDocGia}/profile")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public DocGiaResponse updateProfile(@PathVariable String maDocGia,
                                        @Valid @RequestBody ReaderProfileUpdateRequest request) {
        return docGiaService.updateProfile(maDocGia, request);
    }

    @PutMapping("/{maDocGia}")
    @Deprecated
    public DocGiaResponse update(
            @PathVariable String maDocGia,
            @Valid @RequestBody DocGiaRequest request
    ) {
        return docGiaService.update(maDocGia, request);
    }

    @PatchMapping("/{maDocGia}/membership")
    public DocGiaResponse updateMembershipPlan(
            @PathVariable String maDocGia,
            @Valid @RequestBody ReaderMembershipUpdateRequest request
    ) {
        return docGiaService.updateMembershipPlan(maDocGia, request.getMaGoiThanhVien());
    }

    @DeleteMapping("/{maDocGia}")
    public String disable(
            @PathVariable String maDocGia,
            @RequestParam(defaultValue = "soft") String mode
    ) {
        if ("hard".equalsIgnoreCase(mode)) {
            docGiaService.hardDelete(maDocGia);
            return "Xóa độc giả thành công";
        }

        throw new BusinessException("Ngừng hoạt động phải dùng endpoint /deactivate và cung cấp lý do");
    }

    @PatchMapping("/{maDocGia}/restore")
    public String restore(@PathVariable String maDocGia) {
        throw new BusinessException("Khôi phục phải dùng endpoint /reactivate và cung cấp lý do");
    }
}

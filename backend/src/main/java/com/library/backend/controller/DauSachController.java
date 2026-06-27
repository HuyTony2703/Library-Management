package com.library.backend.controller;

import com.library.backend.dto.DauSachCreateRequest;
import com.library.backend.dto.BookBranchCopiesResponse;
import com.library.backend.dto.BookDeletePreflightResponse;
import com.library.backend.dto.BookLifecycleRequest;
import com.library.backend.dto.BulkActionRequest;
import com.library.backend.dto.BulkActionResponse;
import com.library.backend.dto.DauSachListQuery;
import com.library.backend.dto.DauSachResponse;
import com.library.backend.dto.DauSachUpdateRequest;
import com.library.backend.dto.ExportRequest;
import com.library.backend.dto.ExportResponse;
import com.library.backend.dto.IsbnCheckResponse;
import com.library.backend.dto.NhatKyHoatDongResponse;
import com.library.backend.dto.PageResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.DauSachDetailService;
import com.library.backend.service.DauSachLifecycleService;
import com.library.backend.service.DauSachService;
import com.library.backend.service.BulkActionService;
import com.library.backend.service.ExportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Validated
public class DauSachController {

    private final DauSachService dauSachService;
    private final DauSachDetailService dauSachDetailService;
    private final DauSachLifecycleService dauSachLifecycleService;
    private final BulkActionService bulkActionService;
    private final ExportService exportService;

    public DauSachController(
            DauSachService dauSachService,
            DauSachDetailService dauSachDetailService,
            DauSachLifecycleService dauSachLifecycleService,
            BulkActionService bulkActionService,
            ExportService exportService
    ) {
        this.dauSachService = dauSachService;
        this.dauSachDetailService = dauSachDetailService;
        this.dauSachLifecycleService = dauSachLifecycleService;
        this.bulkActionService = bulkActionService;
        this.exportService = exportService;
    }

    @GetMapping(params = "!page")
    public List<DauSachResponse> getAll() {
        return dauSachService.getAll();
    }

    @GetMapping(params = "page")
    public PageResponse<DauSachResponse> getPage(
            @RequestParam @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "title,asc") String sort,
            @RequestParam(required = false) List<String> statusIds,
            @RequestParam(required = false) List<String> categoryIds,
            @RequestParam(required = false) List<String> authorIds,
            @RequestParam(required = false) List<String> publisherIds,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean hasIsbn,
            @RequestParam(required = false) Boolean hasCover
    ) {
        return dauSachService.getPage(new DauSachListQuery(
                page, pageSize, search, sort, statusIds, categoryIds, authorIds,
                publisherIds, yearFrom, yearTo, language, hasIsbn, hasCover
        ));
    }

    @GetMapping("/{maDauSach}")
    public DauSachResponse getById(@PathVariable String maDauSach) {
        return dauSachService.getById(maDauSach);
    }

    @GetMapping("/{maDauSach}/copies-by-branch")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public List<BookBranchCopiesResponse> getCopiesByBranch(
            @PathVariable String maDauSach,
            @AuthenticationPrincipal AuthUser user
    ) {
        return dauSachDetailService.getCopiesByBranch(maDauSach, user);
    }

    @GetMapping("/{maDauSach}/history")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public List<NhatKyHoatDongResponse> getHistory(
            @PathVariable String maDauSach,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    ) {
        return dauSachDetailService.getHistory(maDauSach, limit);
    }

    @GetMapping("/isbn-check")
    public IsbnCheckResponse checkIsbn(
            @RequestParam String isbn,
            @RequestParam(required = false) String excludeId
    ) {
        return dauSachService.checkIsbn(isbn, excludeId);
    }

    @PostMapping
    public DauSachResponse create(@Valid @RequestBody DauSachCreateRequest request) {
        return dauSachService.create(request);
    }

    @RequestMapping(value = "/{maDauSach}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public DauSachResponse update(
            @PathVariable String maDauSach,
            @Valid @RequestBody DauSachUpdateRequest request
    ) {
        return dauSachService.update(maDauSach, request);
    }

    @PostMapping("/{maDauSach}/deactivate")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public DauSachResponse deactivate(
            @PathVariable String maDauSach,
            @Valid @RequestBody BookLifecycleRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return dauSachLifecycleService.deactivate(maDauSach, request.getReason(), user);
    }

    @PostMapping("/bulk-actions")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public BulkActionResponse bulkAction(
            @Valid @RequestBody BulkActionRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return bulkActionService.applyBooks(request, user);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ExportResponse export(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return exportService.exportBooks(request, user);
    }

    @PostMapping("/{maDauSach}/reactivate")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public DauSachResponse reactivate(
            @PathVariable String maDauSach,
            @Valid @RequestBody BookLifecycleRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return dauSachLifecycleService.reactivate(maDauSach, request.getReason(), user);
    }

    @GetMapping("/{maDauSach}/delete-preflight")
    @PreAuthorize("hasRole('QUAN_TRI_VIEN')")
    public BookDeletePreflightResponse deletePreflight(
            @PathVariable String maDauSach,
            @AuthenticationPrincipal AuthUser user
    ) {
        return dauSachLifecycleService.preflight(maDauSach, user);
    }

    @DeleteMapping("/{maDauSach}")
    @PreAuthorize("hasRole('QUAN_TRI_VIEN')")
    public void hardDelete(
            @PathVariable String maDauSach,
            @AuthenticationPrincipal AuthUser user
    ) {
        dauSachLifecycleService.hardDelete(maDauSach, user);
    }
}

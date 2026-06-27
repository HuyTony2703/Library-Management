package com.library.backend.controller;

import com.library.backend.dto.CuonSachRequest;
import com.library.backend.dto.CuonSachResponse;
import com.library.backend.dto.BookCopyLocationFiltersResponse;
import com.library.backend.dto.BookCopyBatchRequest;
import com.library.backend.dto.BookCopyBatchResponse;
import com.library.backend.dto.BookCopyActionResponse;
import com.library.backend.dto.BookCopyBarcodeLabelRequest;
import com.library.backend.dto.BookCopyBarcodeLabelResponse;
import com.library.backend.dto.BookCopyConditionRequest;
import com.library.backend.dto.BookCopyMoveRequest;
import com.library.backend.dto.BulkActionRequest;
import com.library.backend.dto.BulkActionResponse;
import com.library.backend.dto.CuonSachListItemResponse;
import com.library.backend.dto.CuonSachListQuery;
import com.library.backend.dto.ExportRequest;
import com.library.backend.dto.ExportResponse;
import com.library.backend.dto.PageResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.CuonSachListService;
import com.library.backend.service.BookCopyBatchService;
import com.library.backend.service.BookCopyActionService;
import com.library.backend.service.BookCopyBarcodeLabelService;
import com.library.backend.service.BulkActionService;
import com.library.backend.service.CuonSachService;
import com.library.backend.service.ExportService;
import com.library.backend.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/book-copies")
@Validated
public class CuonSachController {

    private final CuonSachService cuonSachService;
    private final CuonSachListService cuonSachListService;
    private final BookCopyBatchService bookCopyBatchService;
    private final BookCopyActionService bookCopyActionService;
    private final BookCopyBarcodeLabelService bookCopyBarcodeLabelService;
    private final BulkActionService bulkActionService;
    private final ExportService exportService;

    public CuonSachController(CuonSachService cuonSachService, CuonSachListService cuonSachListService,
                              BookCopyBatchService bookCopyBatchService, BookCopyActionService bookCopyActionService,
                              BookCopyBarcodeLabelService bookCopyBarcodeLabelService,
                              BulkActionService bulkActionService,
                              ExportService exportService) {
        this.cuonSachService = cuonSachService;
        this.cuonSachListService = cuonSachListService;
        this.bookCopyBatchService = bookCopyBatchService;
        this.bookCopyActionService = bookCopyActionService;
        this.bookCopyBarcodeLabelService = bookCopyBarcodeLabelService;
        this.bulkActionService = bulkActionService;
        this.exportService = exportService;
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public BookCopyBatchResponse createBatch(
            @Valid @RequestBody BookCopyBatchRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return bookCopyBatchService.create(request, user);
    }

    @PostMapping("/barcode-labels/preview")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public BookCopyBarcodeLabelResponse previewBarcodeLabels(
            @Valid @RequestBody BookCopyBarcodeLabelRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return bookCopyBarcodeLabelService.preview(request, user);
    }

    @PostMapping("/barcode-labels/print")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public BookCopyBarcodeLabelResponse printBarcodeLabels(
            @Valid @RequestBody BookCopyBarcodeLabelRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return bookCopyBarcodeLabelService.createPrintJob(request, user);
    }

    @PostMapping("/{maCuonSach}/condition-events")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public BookCopyActionResponse applyCondition(
            @PathVariable String maCuonSach,
            @Valid @RequestBody BookCopyConditionRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return bookCopyActionService.applyCondition(maCuonSach, request, user);
    }

    @PostMapping("/bulk-actions")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public BulkActionResponse bulkAction(
            @Valid @RequestBody BulkActionRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return bulkActionService.applyBookCopies(request, user);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ExportResponse export(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return exportService.exportBookCopies(request, user);
    }

    @PostMapping("/{maCuonSach}/move-location")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public BookCopyActionResponse moveLocation(
            @PathVariable String maCuonSach,
            @Valid @RequestBody BookCopyMoveRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return bookCopyActionService.moveLocation(maCuonSach, request, user);
    }

    @GetMapping(params = "!page")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public List<CuonSachListItemResponse> getAll(@AuthenticationPrincipal AuthUser user) {
        return cuonSachListService.getAllLegacy(user);
    }

    @GetMapping(params = "page")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public PageResponse<CuonSachListItemResponse> getPage(
            @RequestParam @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "importedAt,desc") String sort,
            @RequestParam(required = false) List<String> statusIds,
            @RequestParam(required = false) List<String> branchIds,
            @RequestParam(required = false) List<String> titleIds,
            @RequestParam(required = false) List<String> areaIds,
            @RequestParam(required = false) List<String> shelfIds,
            @RequestParam(required = false) List<String> locationIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate importedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate importedTo,
            @RequestParam(required = false) Boolean hasBarcode,
            @RequestParam(required = false) Boolean hasQr,
            @AuthenticationPrincipal AuthUser user
    ) {
        return cuonSachListService.getPage(new CuonSachListQuery(
                page, pageSize, search, sort, statusIds, branchIds, titleIds,
                areaIds, shelfIds, locationIds, importedFrom, importedTo, hasBarcode, hasQr
        ), user);
    }

    @GetMapping("/location-filter-options")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public BookCopyLocationFiltersResponse getLocationFilterOptions(
            @RequestParam(required = false) List<String> branchIds,
            @AuthenticationPrincipal AuthUser user
    ) {
        return cuonSachListService.getLocationFilters(branchIds, user);
    }

    @GetMapping("/{maCuonSach}")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public CuonSachListItemResponse getById(
            @PathVariable String maCuonSach,
            @AuthenticationPrincipal AuthUser user
    ) {
        return cuonSachListService.getById(maCuonSach, user);
    }

    @PostMapping
    public CuonSachResponse create(@Valid @RequestBody CuonSachRequest request) {
        return cuonSachService.create(request);
    }

    @PutMapping("/{maCuonSach}")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public CuonSachResponse update(
            @PathVariable String maCuonSach,
            @Valid @RequestBody CuonSachRequest request
    ) {
        return cuonSachService.update(maCuonSach, request);
    }

    @DeleteMapping("/{maCuonSach}")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public String disable(
            @PathVariable String maCuonSach,
            @RequestParam(defaultValue = "soft") String mode
    ) {
        if ("hard".equalsIgnoreCase(mode)) {
            cuonSachService.hardDelete(maCuonSach);
            return "Xóa cuốn sách thành công";
        }

        throw new BusinessException("Ngừng lưu thông phải dùng condition-events và cung cấp lý do");
    }

    @PatchMapping("/{maCuonSach}/restore")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public String restore(@PathVariable String maCuonSach) {
        throw new BusinessException("Khôi phục phải dùng condition-events và cung cấp lý do");
    }
}

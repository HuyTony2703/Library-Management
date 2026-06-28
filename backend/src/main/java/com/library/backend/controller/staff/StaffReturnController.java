package com.library.backend.controller.staff;

import com.library.backend.dto.OpenLoanLookupResponse;
import com.library.backend.dto.ReturnPreviewResponse;
import com.library.backend.dto.TraSachRequest;
import com.library.backend.dto.TraSachResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.TraSachService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
public class StaffReturnController {

    private final TraSachService traSachService;

    public StaffReturnController(TraSachService traSachService) {
        this.traSachService = traSachService;
    }

    @PostMapping("/returns")
    public TraSachResponse createReturn(
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody TraSachRequest request
    ) {
        return traSachService.createStaffReturn(request, (AuthUser) authentication.getPrincipal(), idempotencyKey);
    }

    @PostMapping("/returns/preview")
    public ReturnPreviewResponse previewReturn(
            Authentication authentication,
            @RequestBody TraSachRequest request
    ) {
        return traSachService.preview(request, (AuthUser) authentication.getPrincipal());
    }

    @GetMapping("/returns/{maPhieuTra}")
    public TraSachResponse getReturn(@PathVariable String maPhieuTra) {
        return traSachService.getById(maPhieuTra);
    }

    @GetMapping("/open-loans/by-barcode/{code}")
    public OpenLoanLookupResponse getOpenLoanByCode(
            Authentication authentication,
            @PathVariable String code
    ) {
        return traSachService.getOpenLoanByCode(code, (AuthUser) authentication.getPrincipal());
    }

    @GetMapping("/readers/{maDocGia}/open-loans")
    public List<OpenLoanLookupResponse> getOpenLoansByReader(
            Authentication authentication,
            @PathVariable String maDocGia
    ) {
        return traSachService.getOpenLoansByReader(maDocGia, (AuthUser) authentication.getPrincipal());
    }
}

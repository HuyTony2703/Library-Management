package com.library.backend.controller.staff;

import com.library.backend.dto.MuonSachRequest;
import com.library.backend.dto.MuonSachResponse;
import com.library.backend.dto.LoanCopyOptionResponse;
import com.library.backend.dto.LoanPreviewRequest;
import com.library.backend.dto.LoanPreviewResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.LoanPreviewService;
import com.library.backend.service.MuonSachService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
public class StaffLoanController {

    private final MuonSachService muonSachService;
    private final LoanPreviewService loanPreviewService;

    public StaffLoanController(MuonSachService muonSachService, LoanPreviewService loanPreviewService) {
        this.muonSachService = muonSachService;
        this.loanPreviewService = loanPreviewService;
    }

    @GetMapping("/book-copies/search")
    public List<LoanCopyOptionResponse> searchCopies(
            Authentication authentication,
            @RequestParam("q") String query,
            @RequestParam("readerId") String readerId,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int limit
    ) {
        return loanPreviewService.search(query, readerId, limit, principal(authentication));
    }

    @GetMapping("/book-copies/by-barcode/{code}")
    public LoanCopyOptionResponse exactCopy(
            Authentication authentication,
            @PathVariable String code,
            @RequestParam("readerId") String readerId
    ) {
        return loanPreviewService.exactLookup(code, readerId, principal(authentication));
    }

    @PostMapping("/loans/preview")
    public LoanPreviewResponse preview(
            Authentication authentication,
            @Valid @RequestBody LoanPreviewRequest request
    ) {
        return loanPreviewService.preview(request, principal(authentication));
    }

    @PostMapping("/loans")
    public MuonSachResponse createLoan(
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody MuonSachRequest request
    ) {
        return muonSachService.create(
                request,
                (AuthUser) authentication.getPrincipal(),
                idempotencyKey
        );
    }

    @GetMapping("/loans/{maPhieuMuon}")
    public MuonSachResponse getLoan(@PathVariable String maPhieuMuon) {
        return muonSachService.getById(maPhieuMuon);
    }

    private AuthUser principal(Authentication authentication) {
        return (AuthUser) authentication.getPrincipal();
    }
}

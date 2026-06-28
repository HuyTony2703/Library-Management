package com.library.backend.controller.staff;

import com.library.backend.dto.DebtorSearchResponse;
import com.library.backend.dto.KhoanNoResponse;
import com.library.backend.dto.PaymentPreviewResponse;
import com.library.backend.dto.PhieuThuRequest;
import com.library.backend.dto.PhieuThuResponse;
import com.library.backend.dto.ReaderDebtContextResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.ThanhToanService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
public class StaffPaymentController {

    private final ThanhToanService thanhToanService;

    public StaffPaymentController(ThanhToanService thanhToanService) {
        this.thanhToanService = thanhToanService;
    }

    @GetMapping("/debtors/search")
    public List<DebtorSearchResponse> searchDebtors(
            @RequestParam(value = "q", defaultValue = "") String query,
            @RequestParam(value = "outstandingOnly", defaultValue = "true") boolean outstandingOnly,
            @RequestParam(value = "limit", defaultValue = "15") int limit,
            Authentication authentication
    ) {
        return thanhToanService.searchDebtors(query, outstandingOnly, limit, (AuthUser) authentication.getPrincipal());
    }

    @GetMapping("/readers/{maDocGia}/debt-context")
    public ReaderDebtContextResponse getReaderDebtContext(Authentication authentication, @PathVariable String maDocGia) {
        return thanhToanService.getReaderDebtContext(maDocGia, (AuthUser) authentication.getPrincipal());
    }

    @GetMapping("/readers/{maDocGia}/debts")
    public List<KhoanNoResponse> getReaderDebts(Authentication authentication, @PathVariable String maDocGia) {
        return thanhToanService.getDebtsByReader(maDocGia, (AuthUser) authentication.getPrincipal());
    }

    @PostMapping("/payments/preview")
    public PaymentPreviewResponse previewPayment(
            Authentication authentication,
            @Valid @RequestBody PhieuThuRequest request
    ) {
        return thanhToanService.previewPayment(
                request,
                (AuthUser) authentication.getPrincipal()
        );
    }

    @PostMapping("/payments")
    public PhieuThuResponse createPayment(
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PhieuThuRequest request
    ) {
        return thanhToanService.createPayment(
                request,
                (AuthUser) authentication.getPrincipal(),
                idempotencyKey
        );
    }

    @GetMapping("/payments/{maPhieuThu}")
    public PhieuThuResponse getPayment(Authentication authentication, @PathVariable String maPhieuThu) {
        return thanhToanService.getPaymentById(maPhieuThu, (AuthUser) authentication.getPrincipal());
    }

    @GetMapping("/readers/{maDocGia}/payments")
    public List<PhieuThuResponse> getReaderPayments(Authentication authentication, @PathVariable String maDocGia) {
        return thanhToanService.getPaymentsByReader(maDocGia, (AuthUser) authentication.getPrincipal());
    }
}

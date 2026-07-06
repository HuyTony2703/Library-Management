package com.library.backend.controller.admin;

import com.library.backend.dto.PaymentReversalRequest;
import com.library.backend.dto.PaymentReversalResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.ThanhToanService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
@PreAuthorize("hasRole('QUAN_TRI_VIEN')")
public class AdminPaymentController {

    private final ThanhToanService thanhToanService;

    public AdminPaymentController(ThanhToanService thanhToanService) {
        this.thanhToanService = thanhToanService;
    }

    @PostMapping("/{paymentId}/reverse")
    public PaymentReversalResponse reversePayment(
            Authentication authentication,
            @PathVariable String paymentId,
            @Valid @RequestBody PaymentReversalRequest request
    ) {
        return thanhToanService.reversePayment(
                paymentId,
                request,
                (AuthUser) authentication.getPrincipal()
        );
    }
}

package com.library.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaymentPreviewResponse(
        String readerId,
        String paymentMethodId,
        String mode,
        BigDecimal balanceBefore,
        BigDecimal amount,
        BigDecimal balanceAfter,
        BigDecimal cashReceived,
        BigDecimal changeAmount,
        List<AllocationPreview> allocations
) {
    public record AllocationPreview(
            String debtId,
            String debtTypeId,
            String reason,
            BigDecimal incurredAmount,
            BigDecimal paidBefore,
            BigDecimal remainingBefore,
            BigDecimal appliedAmount,
            BigDecimal remainingAfter,
            String statusAfter
    ) {
    }
}

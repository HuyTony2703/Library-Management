package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentReversalResponse(
        String reversalId,
        String originalPaymentId,
        String readerId,
        String reversedByStaffId,
        String reason,
        String approvalReference,
        LocalDateTime reversedAt,
        BigDecimal restoredAmount,
        List<RestoredDebt> restoredDebts
) {
    public record RestoredDebt(
            String debtId,
            BigDecimal restoredAmount,
            BigDecimal paidBefore,
            BigDecimal paidAfter,
            BigDecimal remainingBefore,
            BigDecimal remainingAfter,
            String statusBefore,
            String statusAfter
    ) {
    }
}

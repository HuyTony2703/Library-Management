package com.library.backend.dto;

import java.math.BigDecimal;

public record DebtorSearchResponse(
        String readerId,
        String readerName,
        String email,
        String phone,
        BigDecimal outstandingAmount,
        long outstandingDebtCount
) {
}

package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReaderDebtContextResponse(
        String readerId,
        String readerName,
        BigDecimal totalIncurred,
        BigDecimal totalPaid,
        BigDecimal outstandingAmount,
        long totalDebtCount,
        long outstandingDebtCount,
        LocalDateTime oldestOutstandingDebtDate,
        boolean borrowingImpacted
) {
}

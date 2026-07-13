package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReaderDebtItemResponse(
        String debtId,
        String debtTypeId,
        String debtTypeName,
        BigDecimal originalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        LocalDateTime occurredAt,
        String reason,
        String status
) {}

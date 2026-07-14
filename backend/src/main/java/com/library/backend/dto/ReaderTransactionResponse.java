package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReaderTransactionResponse(
        String type,
        String id,
        LocalDateTime occurredAt,
        String status,
        BigDecimal amount,
        String description
) {}

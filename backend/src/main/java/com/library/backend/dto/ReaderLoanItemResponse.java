package com.library.backend.dto;

import java.time.LocalDateTime;

public record ReaderLoanItemResponse(
        String loanId,
        String loanDetailId,
        String copyId,
        String barcode,
        String titleId,
        String titleName,
        String branchId,
        String branchName,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        long overdueDays,
        String status
) {}

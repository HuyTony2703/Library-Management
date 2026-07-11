package com.library.backend.dto;

import java.time.LocalDateTime;

public record OpenLoanLookupResponse(
        String loanDetailId,
        String loanId,
        String readerId,
        String readerName,
        String copyId,
        String barcode,
        String titleId,
        String titleName,
        String branchId,
        String branchName,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        Integer overdueDays,
        String status
) {
}

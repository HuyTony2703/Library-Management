package com.library.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LoanCopyOptionResponse(
        String value,
        String label,
        String code,
        boolean exactMatch,
        String copyId,
        String barcode,
        String titleId,
        String titleName,
        String isbn,
        String branchId,
        String branchName,
        String locationId,
        String locationLabel,
        String statusId,
        String statusName,
        Reservation reservation,
        boolean borrowable,
        List<LoanReasonResponse> warnings,
        List<LoanReasonResponse> blockingReasons
) {
    public LoanCopyOptionResponse {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        blockingReasons = blockingReasons == null ? List.of() : List.copyOf(blockingReasons);
    }

    public record Reservation(
            String reservationId,
            String readerId,
            String status,
            LocalDateTime expiresAt,
            boolean ownedBySelectedReader
    ) {
    }
}

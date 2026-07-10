package com.library.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LoanPreviewResponse(
        String readerId,
        Branch branch,
        LocalDateTime previewedAt,
        boolean eligible,
        Quota quota,
        List<Item> items,
        List<LoanReasonResponse> warnings,
        List<LoanReasonResponse> blockingReasons,
        String disclaimer
) {
    public LoanPreviewResponse {
        items = items == null ? List.of() : List.copyOf(items);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        blockingReasons = blockingReasons == null ? List.of() : List.copyOf(blockingReasons);
    }

    public record Branch(String id, String name) {
    }

    public record Quota(long current, int cart, long after, Integer maximum, Integer remainingAfter) {
    }

    public record Item(
            String copyId,
            String barcode,
            String titleId,
            String titleName,
            String isbn,
            String locationId,
            String locationLabel,
            String statusId,
            String statusName,
            LoanCopyOptionResponse.Reservation reservation,
            String ruleId,
            Integer borrowDays,
            LocalDateTime expectedDueAt,
            boolean borrowable,
            List<LoanReasonResponse> warnings,
            List<LoanReasonResponse> blockingReasons
    ) {
        public Item {
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
            blockingReasons = blockingReasons == null ? List.of() : List.copyOf(blockingReasons);
        }
    }
}

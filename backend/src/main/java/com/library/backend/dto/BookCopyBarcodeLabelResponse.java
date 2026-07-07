package com.library.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookCopyBarcodeLabelResponse(
        String printJobId,
        String template,
        int total,
        int generatedBarcodes,
        LocalDateTime createdAt,
        List<Label> labels
) {
    public record Label(
            String copyId,
            String barcode,
            String title,
            String isbn,
            String branchName,
            String branchId,
            String locationLabel,
            String areaName,
            String shelfName
    ) {
    }
}

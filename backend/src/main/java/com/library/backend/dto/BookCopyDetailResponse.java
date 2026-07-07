package com.library.backend.dto;

import java.time.LocalDate;

public record BookCopyDetailResponse(
        String id,
        String branchId,
        String branchName,
        String locationId,
        String locationLabel,
        String statusId,
        String statusName,
        String barcode,
        String qrCode,
        LocalDate importedAt
) {
}

package com.library.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record BookCopyBatchRequest(
        @NotBlank @Size(max = 30) String titleId,
        @NotBlank @Size(max = 30) String branchId,
        @NotBlank @Size(max = 30) String locationId,
        @NotNull LocalDate importDate,
        @Min(1) @Max(100) Integer quantity,
        @NotNull BarcodeMode barcodeMode,
        @Size(max = 255) String note,
        @Valid @Size(max = 100) List<ManualCopy> copies
) {
    public enum BarcodeMode { AUTO, MANUAL, LATER }

    public record ManualCopy(
            @NotBlank @Size(max = 100) String barcode,
            @Size(max = 255) String note
    ) {}
}

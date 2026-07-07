package com.library.backend.dto;

import java.util.List;

public record BookCopyBatchResponse(
        String batchId,
        int created,
        List<CreatedCopy> copies,
        boolean labelReady
) {
    public record CreatedCopy(String id, String barcode, String status) {}
}

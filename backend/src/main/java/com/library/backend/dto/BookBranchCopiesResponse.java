package com.library.backend.dto;

import java.util.List;

public record BookBranchCopiesResponse(
        String branchId,
        String branchName,
        long totalCopies,
        long availableCopies,
        List<BookCopyDetailResponse> copies
) {
    public BookBranchCopiesResponse {
        copies = copies == null ? List.of() : List.copyOf(copies);
    }
}

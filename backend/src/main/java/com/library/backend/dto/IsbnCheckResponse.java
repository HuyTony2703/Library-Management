package com.library.backend.dto;

public record IsbnCheckResponse(
        boolean valid,
        String normalizedIsbn,
        String message,
        boolean duplicate,
        DauSachResponse existingBook
) {
}

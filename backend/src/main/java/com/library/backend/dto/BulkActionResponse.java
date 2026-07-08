package com.library.backend.dto;

import java.util.List;

public record BulkActionResponse(
        int requested,
        int succeeded,
        int failed,
        List<ItemError> errors
) {
    public record ItemError(String id, String code, String message) {}
}

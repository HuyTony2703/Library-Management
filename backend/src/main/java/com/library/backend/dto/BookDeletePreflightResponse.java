package com.library.backend.dto;

import java.util.Map;

public record BookDeletePreflightResponse(
        boolean canDelete,
        Map<String, Long> dependencies
) {
    public BookDeletePreflightResponse {
        dependencies = dependencies == null ? Map.of() : Map.copyOf(dependencies);
    }
}

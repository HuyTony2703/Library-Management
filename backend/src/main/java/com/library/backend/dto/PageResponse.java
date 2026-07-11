package com.library.backend.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {
    public PageResponse {
        items = List.copyOf(items);
    }
}

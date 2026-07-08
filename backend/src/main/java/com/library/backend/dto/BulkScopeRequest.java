package com.library.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record BulkScopeRequest(
        @NotNull ScopeType type,
        @Size(max = 5000) List<@Size(max = 50) String> ids,
        Map<String, Object> filters,
        @Size(max = 5000) List<@Size(max = 50) String> excludedIds
) {
    public enum ScopeType { SELECTED_IDS, FILTERED_QUERY }
}

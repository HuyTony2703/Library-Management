package com.library.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkActionRequest(
        @NotBlank @Size(max = 80) String action,
        @NotNull @Valid BulkScopeRequest scope,
        BookCopyConditionRequest.Severity severity,
        @Size(max = 10) List<@Size(max = 50) String> damageTypes,
        @Size(max = 1000) String description,
        @NotBlank @Size(max = 500) String reason
) {
}

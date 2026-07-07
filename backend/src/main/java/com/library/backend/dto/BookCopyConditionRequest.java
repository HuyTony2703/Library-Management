package com.library.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BookCopyConditionRequest(
        @NotNull Action action,
        Severity severity,
        @Size(max = 10) List<@Size(max = 50) String> damageTypes,
        @Size(max = 1000) String description,
        @NotBlank @Size(max = 500) String reason
) {
    public enum Action { MARK_DAMAGED, MARK_LOST, WITHDRAW, RESTORE_AFTER_REPAIR, RESTORE_FOUND }
    public enum Severity { LOW, MEDIUM, HIGH }
}

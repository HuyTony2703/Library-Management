package com.library.backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record ReaderLockRequest(
        @NotEmpty Set<Scope> scopes,
        @NotBlank @Size(max = 500) String reason,
        @FutureOrPresent LocalDate lockedUntil,
        @Size(max = 1000) String note
) {
    public enum Scope { BORROWING, LOGIN }
}

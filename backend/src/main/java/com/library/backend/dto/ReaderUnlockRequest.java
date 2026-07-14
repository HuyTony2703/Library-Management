package com.library.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ReaderUnlockRequest(
        @NotEmpty Set<ReaderLockRequest.Scope> scopes,
        @NotBlank @Size(max = 500) String reason
) {}

package com.library.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookCopyMoveRequest(
        @NotBlank @Size(max = 30) String locationId,
        @NotBlank @Size(max = 500) String reason
) {}

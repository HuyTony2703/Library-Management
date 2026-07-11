package com.library.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentReversalRequest(
        @NotBlank(message = "Reason is required")
        @Size(max = 255, message = "Reason must be at most 255 characters")
        String reason,

        @NotBlank(message = "Approval reference is required")
        @Size(max = 100, message = "Approval reference must be at most 100 characters")
        String approvalReference
) {
}

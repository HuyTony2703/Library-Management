package com.library.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LoanPreviewRequest(
        @NotBlank @Size(max = 30) String readerId,
        @Size(max = 30) String branchId,
        @NotEmpty @Size(max = 50) List<@NotBlank @Size(max = 40) String> copyIds
) {
}

package com.library.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ReaderPasswordResetRequest(
        @NotNull Mode mode,
        @NotNull Boolean forceChange,
        @NotNull Boolean revokeSessions,
        @NotBlank @Size(max = 500) String reason,
        @NotBlank @Email @Size(max = 255) String verificationEmail,
        @NotNull LocalDate verificationDateOfBirth
) {
    public enum Mode { GENERATE_TEMPORARY }
}

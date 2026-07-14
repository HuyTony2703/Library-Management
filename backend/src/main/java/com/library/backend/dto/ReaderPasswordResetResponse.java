package com.library.backend.dto;

import java.time.LocalDateTime;

public record ReaderPasswordResetResponse(
        String readerId,
        String temporaryPassword,
        boolean mustChangePassword,
        boolean sessionsRevoked,
        LocalDateTime issuedAt
) {}

package com.library.backend.dto;

import java.time.LocalDateTime;

public record ReaderStateActionResponse(
        String readerId,
        String action,
        String profileStatus,
        LocalDateTime occurredAt,
        ReaderEligibilityResponse eligibility
) {}

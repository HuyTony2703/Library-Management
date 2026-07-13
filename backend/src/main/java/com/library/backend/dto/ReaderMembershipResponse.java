package com.library.backend.dto;

import java.time.LocalDate;

public record ReaderMembershipResponse(
        String id,
        String planId,
        String planName,
        LocalDate startsAt,
        LocalDate expiresAt,
        String status,
        String note
) {}

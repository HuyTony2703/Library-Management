package com.library.backend.dto;

import java.time.LocalDateTime;

public record BookCopyActionResponse(
        String copyId,
        String action,
        String previousStatus,
        String currentStatus,
        String previousLocationId,
        String currentLocationId,
        String reason,
        LocalDateTime occurredAt
) {}

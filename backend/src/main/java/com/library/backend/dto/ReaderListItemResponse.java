package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReaderListItemResponse(
        String readerId,
        String fullName,
        String groupId,
        String groupName,
        String profileStatus,
        LocalDate cardIssuedAt,
        LocalDate cardExpiresAt,
        String cardStatus,
        String planId,
        String planName,
        LocalDate membershipExpiresAt,
        String membershipStatus,
        String accountStatus,
        long currentLoans,
        BigDecimal outstandingDebt
) {}

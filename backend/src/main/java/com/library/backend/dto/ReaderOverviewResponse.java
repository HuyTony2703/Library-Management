package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReaderOverviewResponse(
        String readerId,
        String accountId,
        String username,
        String fullName,
        LocalDate dateOfBirth,
        String address,
        String email,
        String phone,
        String groupId,
        String groupName,
        String profileStatus,
        LocalDate cardIssuedAt,
        LocalDate cardExpiresAt,
        String cardStatus,
        String accountStatus,
        String planId,
        String planName,
        LocalDate membershipStartsAt,
        LocalDate membershipExpiresAt,
        String membershipStatus,
        long currentLoans,
        BigDecimal outstandingDebt
) {}

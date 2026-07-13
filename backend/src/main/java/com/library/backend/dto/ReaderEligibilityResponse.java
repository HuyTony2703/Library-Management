package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReaderEligibilityResponse(
        Reader reader,
        Card card,
        Membership membership,
        Quota quota,
        Overdue overdue,
        Debt debt,
        Obligations obligations,
        List<ActiveLock> activeLocks,
        boolean eligible,
        List<Reason> warnings,
        List<Reason> blockingReasons
) {
    public record Reader(String id, String name, String profileStatus, String accountStatus) {}
    public record Card(LocalDate expiryDate, String status, Long remainingDays) {}
    public record Membership(String planId, String planName, String status, LocalDate expiryDate) {}
    public record Quota(long current, Integer maximum, Integer remaining, String policyVersion) {}
    public record Overdue(long count, long maxDays) {}
    public record Debt(BigDecimal outstanding, BigDecimal blockingThreshold) {}
    public record Obligations(long currentLoans, long overdueLoans, long activeReservations, BigDecimal outstandingDebt) {}
    public record ActiveLock(String scope, String reason, LocalDate lockedUntil) {}
    public record Reason(String code, String message) {}
}

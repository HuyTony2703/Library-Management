package com.library.backend.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoanPreviewServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 22, 10, 0);

    @Test
    void availableCopyAtCurrentBranchIsBorrowable() {
        LoanPreviewService.CopyEligibility result = LoanPreviewService.evaluateCopy(
                copy("CN_A", "TT_SANCO", null, null, null), "CN_A", "DG001", NOW
        );

        assertThat(result.blockingReasons()).isEmpty();
    }

    @Test
    void reservationAllowsOwnerAndBlocksAnotherReader() {
        LoanPreviewService.CopyRow held = copy(
                "CN_A", "TT_DANGDATTRUOC", "PDT001", "DG001", NOW.plusDays(2)
        );

        assertThat(LoanPreviewService.evaluateCopy(held, "CN_A", "DG001", NOW).warnings())
                .extracting(item -> item.code()).containsExactly("RESERVATION_OWNED_BY_READER");
        assertThat(LoanPreviewService.evaluateCopy(held, "CN_A", "DG999", NOW).blockingReasons())
                .extracting(item -> item.code()).contains("RESERVED_FOR_ANOTHER_READER");
    }

    @Test
    void wrongBranchAndExpiredReservationReturnStructuredBlocks() {
        LoanPreviewService.CopyEligibility result = LoanPreviewService.evaluateCopy(
                copy("CN_B", "TT_DANGDATTRUOC", "PDT001", "DG001", NOW.minusMinutes(1)),
                "CN_A", "DG001", NOW
        );

        assertThat(result.blockingReasons()).extracting(item -> item.code())
                .contains("COPY_WRONG_BRANCH", "RESERVATION_EXPIRED");
    }

    @Test
    void dueDateUsesRuleButNeverPassesCardBoundary() {
        assertThat(LoanPreviewService.expectedDueAt(NOW, 7, LocalDate.of(2026, 7, 30)))
                .isEqualTo(NOW.plusDays(7));
        assertThat(LoanPreviewService.expectedDueAt(NOW, 7, LocalDate.of(2026, 6, 25)))
                .isEqualTo(LocalDateTime.of(2026, 6, 24, 23, 59, 59));
        assertThat(LoanPreviewService.expectedDueAt(NOW, 7, LocalDate.of(2026, 6, 22)))
                .isNull();
    }

    private LoanPreviewService.CopyRow copy(
            String branchId,
            String statusId,
            String reservationId,
            String reservationReaderId,
            LocalDateTime reservationExpiry
    ) {
        return new LoanPreviewService.CopyRow(
                "CS001", "BAR-CS001", "DS001", "Clean Code", "9780132350884",
                branchId, "Chi nhánh", "VT001", "Kệ A1", statusId, "Trạng thái",
                reservationId, reservationReaderId, reservationId == null ? null : "Đã giữ chỗ",
                reservationExpiry, "QDM001", 7
        );
    }
}

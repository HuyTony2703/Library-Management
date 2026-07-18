package com.library.backend.repository;

import com.library.backend.dto.ReaderListItemResponse;
import com.library.backend.dto.ReaderListQuery;
import com.library.backend.dto.ReaderOverviewResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReaderPageRepositoryTest {
    @Mock NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void expiryBoundariesDoNotMixExpiredWithNextThirtyDays() {
        LocalDate today = LocalDate.of(2026, 6, 22);

        assertThat(ReaderPageRepository.cardStatus(today.minusDays(1), today)).isEqualTo("EXPIRED");
        assertThat(ReaderPageRepository.cardStatus(today, today)).isEqualTo("EXPIRING");
        assertThat(ReaderPageRepository.cardStatus(today.plusDays(30), today)).isEqualTo("EXPIRING");
        assertThat(ReaderPageRepository.cardStatus(today.plusDays(31), today)).isEqualTo("VALID");
        assertThat(ReaderPageRepository.membershipStatus(null, today)).isEqualTo("NONE");
    }

    @Test
    void sortIsAllowlistedAndAlwaysHasStableReaderTieBreak() {
        assertThat(ReaderPageRepository.parseSort("cardExpiry,desc"))
                .isEqualTo("dg.NgayHetHanThe DESC, dg.MaDocGia ASC");
        assertThatThrownBy(() -> ReaderPageRepository.parseSort("MatKhauHash,asc"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void currentLoanOverdueDaysHighlightsSameDayOverdue() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 22, 16, 0);

        assertThat(ReaderPageRepository.overdueDays(now.minusHours(2), now)).isEqualTo(1);
        assertThat(ReaderPageRepository.overdueDays(now.minusDays(3), now)).isEqualTo(3);
        assertThat(ReaderPageRepository.overdueDays(now.plusMinutes(1), now)).isZero();
    }

    @Test
    void pageAndOverviewDtosCannotSerializePasswordOrHashFields() {
        List<String> fields = java.util.stream.Stream.concat(
                        java.util.Arrays.stream(ReaderListItemResponse.class.getRecordComponents()),
                        java.util.Arrays.stream(ReaderOverviewResponse.class.getRecordComponents()))
                .map(component -> component.getName().toLowerCase(Locale.ROOT)).toList();

        assertThat(fields).noneMatch(name -> name.contains("password") || name.contains("matkhau") || name.contains("hash"));
    }

    @Test
    void countQuerySkipsLoanDebtSummariesAndPageQueryUsesOffset() {
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(Long.class))).thenReturn(0L);
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class))).thenReturn(List.of());
        ReaderPageRepository repository = new ReaderPageRepository(jdbcTemplate);

        repository.findPage(query(), LocalDate.of(2026, 6, 22));

        verify(jdbcTemplate).queryForObject(argThat(sql -> {
            String normalized = sql.toLowerCase(Locale.ROOT);
            return normalized.contains("count_big") && !normalized.contains("loan_summary")
                    && !normalized.contains("debt_summary") && !normalized.contains("matkhau");
        }), any(MapSqlParameterSource.class), eq(Long.class));
        verify(jdbcTemplate).query(argThat(sql -> {
            String normalized = sql.toLowerCase(Locale.ROOT);
            return normalized.contains("offset :offset rows") && !normalized.contains("matkhau");
        }), any(MapSqlParameterSource.class), any(RowMapper.class));
        verify(jdbcTemplate).query(argThat(sql -> !sql.toLowerCase(Locale.ROOT).contains("order bydg")),
                any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    private ReaderListQuery query() {
        return new ReaderListQuery(1, 20, "nguyen", "fullName,asc", List.of(), List.of(), List.of(),
                List.of(), null, null, null, null, null, null, null);
    }
}

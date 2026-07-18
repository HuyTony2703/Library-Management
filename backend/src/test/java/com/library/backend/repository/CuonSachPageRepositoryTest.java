package com.library.backend.repository;

import com.library.backend.dto.CuonSachListQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuonSachPageRepositoryTest {
    @Mock NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void sortByCopyIdDoesNotDuplicateOrderByColumn() {
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
        CuonSachPageRepository repository = new CuonSachPageRepository(jdbcTemplate);

        repository.findPage(query("id,asc"), List.of(), true);

        verify(jdbcTemplate).query(argThat(sql -> {
            String normalized = sql.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
            return normalized.contains("order by cs.macuonsach asc offset")
                    && !normalized.contains("cs.macuonsach asc, cs.macuonsach");
        }), any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    @Test
    void sortByOtherColumnKeepsStableCopyIdTieBreak() {
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
        CuonSachPageRepository repository = new CuonSachPageRepository(jdbcTemplate);

        repository.findPage(query("title,asc"), List.of(), true);

        verify(jdbcTemplate).query(argThat(sql -> {
            String normalized = sql.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
            return normalized.contains("order by ds.tendausach asc, cs.macuonsach asc offset");
        }), any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    private CuonSachListQuery query(String sort) {
        return new CuonSachListQuery(1, 20, null, sort, List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), null, null, null, null);
    }
}

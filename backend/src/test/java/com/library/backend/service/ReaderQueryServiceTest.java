package com.library.backend.service;

import com.library.backend.dto.PageResponse;
import com.library.backend.dto.EntityPickerOptionResponse;
import com.library.backend.dto.ReaderListItemResponse;
import com.library.backend.dto.ReaderListQuery;
import com.library.backend.repository.ReaderPageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReaderQueryServiceTest {
    @Mock ReaderPageRepository repository;

    @Test
    void delegatesValidServerSidePageQuery() {
        ReaderListQuery query = query(1, 20, null, null);
        PageResponse<ReaderListItemResponse> expected = new PageResponse<>(List.of(), 1, 20, 0, 0);
        when(repository.findPage(any(), any(LocalDate.class))).thenReturn(expected);

        assertThat(new ReaderQueryService(repository).getPage(query)).isSameAs(expected);
        verify(repository).findPage(any(), any(LocalDate.class));
    }

    @Test
    void rejectsInvalidPageAndReversedExpiryRangesBeforeRepository() {
        ReaderQueryService service = new ReaderQueryService(repository);
        assertThatThrownBy(() -> service.getPage(query(0, 20, null, null))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getPage(query(1, 101, null, null))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getPage(query(1, 20, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 6, 1))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("hạn thẻ");
    }

    @Test
    void validatesAndDelegatesAsyncReaderSearch() {
        ReaderQueryService service = new ReaderQueryService(repository);
        EntityPickerOptionResponse exact = new EntityPickerOptionResponse(
                "DG001", "Nguyễn An", "DG001", java.util.Map.of("phone", "0900000000"), true
        );
        when(repository.searchForPicker("DG001", 15)).thenReturn(List.of(exact));

        assertThat(service.searchForPicker("DG001", 15)).containsExactly(exact);
        verify(repository).searchForPicker("DG001", 15);
        assertThatThrownBy(() -> service.searchForPicker(" ", 15))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("không được để trống");
        assertThatThrownBy(() -> service.searchForPicker("DG", 51))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("1 đến 50");
    }

    private ReaderListQuery query(int page, int pageSize, LocalDate from, LocalDate to) {
        return new ReaderListQuery(page, pageSize, null, "fullName,asc", List.of(), List.of(), List.of(),
                List.of(), null, null, from, to, null, null, null);
    }
}

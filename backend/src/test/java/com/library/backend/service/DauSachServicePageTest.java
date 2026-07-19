package com.library.backend.service;

import com.library.backend.dto.DauSachListQuery;
import com.library.backend.dto.DauSachResponse;
import com.library.backend.dto.PageResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.repository.DauSachPageRepository;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.repository.NhaXuatBanRepository;
import com.library.backend.repository.TacGiaRepository;
import com.library.backend.repository.TheLoaiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DauSachServicePageTest {

    @Mock DauSachRepository dauSachRepository;
    @Mock NhaXuatBanRepository nhaXuatBanRepository;
    @Mock TacGiaRepository tacGiaRepository;
    @Mock TheLoaiRepository theLoaiRepository;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock DauSachPageRepository dauSachPageRepository;

    @Test
    void delegatesValidOneBasedPageRequest() {
        DauSachListQuery query = query(1, 20, 2000, 2020);
        PageResponse<DauSachResponse> expected = new PageResponse<>(List.of(), 1, 20, 0, 0);
        when(dauSachPageRepository.findPage(query)).thenReturn(expected);

        PageResponse<?> actual = service().getPage(query);

        assertSame(expected, actual);
        verify(dauSachPageRepository).findPage(query);
    }

    @Test
    void rejectsPageZero() {
        assertThrows(IllegalArgumentException.class,
                () -> service().getPage(query(0, 20, null, null)));
    }

    @Test
    void rejectsPageSizeAboveOneHundred() {
        assertThrows(IllegalArgumentException.class,
                () -> service().getPage(query(1, 101, null, null)));
    }

    @Test
    void rejectsReversedYearRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service().getPage(query(1, 20, 2021, 2020)));
    }

    @Test
    void legacyUnpagedListIsDisabled() {
        assertThrows(BusinessException.class, () -> service().getAll());
        verify(dauSachRepository, never()).findAll();
    }

    private DauSachService service() {
        return new DauSachService(dauSachRepository, nhaXuatBanRepository, tacGiaRepository,
                theLoaiRepository, jdbcTemplate, dauSachPageRepository);
    }

    private DauSachListQuery query(int page, int pageSize, Integer yearFrom, Integer yearTo) {
        return new DauSachListQuery(page, pageSize, null, null, List.of(), List.of(),
                List.of(), List.of(), yearFrom, yearTo, null, null, null);
    }
}

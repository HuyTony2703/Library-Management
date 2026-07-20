package com.library.backend.service;

import com.library.backend.dto.EntityPickerOptionResponse;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DauSachServicePickerTest {

    @Mock DauSachRepository dauSachRepository;
    @Mock NhaXuatBanRepository nhaXuatBanRepository;
    @Mock TacGiaRepository tacGiaRepository;
    @Mock TheLoaiRepository theLoaiRepository;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock DauSachPageRepository dauSachPageRepository;

    @Test
    void delegatesBoundedPickerSearch() {
        List<EntityPickerOptionResponse> expected = List.of(
                new EntityPickerOptionResponse("DS001", "Cơ sở dữ liệu", "DS001", Map.of(), true)
        );
        when(dauSachPageRepository.searchForPicker("DS001", 15, true)).thenReturn(expected);

        List<EntityPickerOptionResponse> actual = service().searchForPicker("DS001", 15, true);

        assertSame(expected, actual);
        verify(dauSachPageRepository).searchForPicker("DS001", 15, true);
    }

    @Test
    void rejectsQueryBelowMinimumLength() {
        assertThrows(IllegalArgumentException.class, () -> service().searchForPicker("D", 15, true));
    }

    @Test
    void rejectsLimitAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> service().searchForPicker("database", 51, true));
    }

    private DauSachService service() {
        return new DauSachService(dauSachRepository, nhaXuatBanRepository, tacGiaRepository,
                theLoaiRepository, jdbcTemplate, dauSachPageRepository);
    }
}

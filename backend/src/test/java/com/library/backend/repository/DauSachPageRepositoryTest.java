package com.library.backend.repository;

import com.library.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DauSachPageRepositoryTest {

    @Test
    void defaultsToStableTitleSort() {
        var sort = DauSachPageRepository.parseSort(null);

        assertEquals("ds.TenDauSach", sort.column());
        assertEquals("ASC", sort.direction());
    }

    @Test
    void acceptsAllowlistedSortField() {
        var sort = DauSachPageRepository.parseSort("year,desc");

        assertEquals("ds.NamXuatBan", sort.column());
        assertEquals("DESC", sort.direction());
    }

    @Test
    void rejectsUnknownSortField() {
        assertThrows(BusinessException.class,
                () -> DauSachPageRepository.parseSort("drop table,asc"));
    }

    @Test
    void rejectsUnknownSortDirection() {
        assertThrows(BusinessException.class,
                () -> DauSachPageRepository.parseSort("title,sideways"));
    }
}

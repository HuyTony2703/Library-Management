package com.library.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.backend.dto.DauSachResponse;
import com.library.backend.entity.DauSach;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DauSachLifecycleServiceTest {
    private DauSachRepository repository;
    private DauSachService dauSachService;
    private ActivityLogService activityLogService;
    private JdbcTemplate jdbcTemplate;
    private DauSachLifecycleService service;

    @BeforeEach
    void setUp() {
        repository = mock(DauSachRepository.class);
        dauSachService = mock(DauSachService.class);
        activityLogService = mock(ActivityLogService.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        service = new DauSachLifecycleService(
                repository, dauSachService, activityLogService, jdbcTemplate, new ObjectMapper()
        );
    }

    @Test
    void deactivateChangesOnlyTitleStatusAndWritesBeforeAfterAudit() {
        DauSach book = book("Hoạt động");
        AuthUser librarian = user(RoleConstants.LIBRARIAN);
        DauSachResponse response = mock(DauSachResponse.class);
        when(repository.findById("DS1")).thenReturn(Optional.of(book));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("DS1"))).thenReturn(0L);
        when(dauSachService.getById("DS1")).thenReturn(response);

        assertThat(service.deactivate("DS1", "Thanh lọc danh mục", librarian)).isSameAs(response);

        assertThat(book.getTrangThai()).isEqualTo("Ngừng hiển thị");
        verify(repository).saveAndFlush(book);
        ArgumentCaptor<String> detail = ArgumentCaptor.forClass(String.class);
        verify(activityLogService).logAsAccount(
                eq("TK1"), eq("Ngừng hiển thị đầu sách"), eq("DAUSACH"), eq("DS1"), detail.capture()
        );
        assertThat(detail.getValue()).contains("Thanh lọc danh mục", "Hoạt động", "Ngừng hiển thị");
    }

    @Test
    void deactivateIsBlockedWhenOpenReservationsExist() {
        DauSach book = book("Hoạt động");
        when(repository.findById("DS1")).thenReturn(Optional.of(book));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("DS1"))).thenReturn(2L);

        assertThatThrownBy(() -> service.deactivate("DS1", "Thanh lọc", user(RoleConstants.LIBRARIAN)))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("đặt trước");
        assertThat(book.getTrangThai()).isEqualTo("Hoạt động");
        verify(repository, never()).saveAndFlush(book);
    }

    @Test
    void lifecycleReasonIsRequiredAtServiceBoundary() {
        assertThatThrownBy(() -> service.deactivate("DS1", "  ", user(RoleConstants.LIBRARIAN)))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("Lý do");
        verify(repository, never()).findById(anyString());
    }

    @Test
    void librarianCannotRunHardDeleteOrPreflight() {
        AuthUser librarian = user(RoleConstants.LIBRARIAN);
        assertThatThrownBy(() -> service.hardDelete("DS1", librarian)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.preflight("DS1", librarian)).isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findById(anyString());
    }

    private DauSach book(String status) {
        DauSach book = new DauSach();
        book.setMaDauSach("DS1");
        book.setTenDauSach("Sách kiểm thử");
        book.setTrangThai(status);
        return book;
    }

    private AuthUser user(String role) {
        return new AuthUser("TK1", "user", "VT", role, null, "NV1", "Người dùng");
    }
}

package com.library.backend.service;

import com.library.backend.exception.BusinessException;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.GoiThanhVienRepository;
import com.library.backend.repository.LichSuGoiThanhVienRepository;
import com.library.backend.repository.NhomDocGiaRepository;
import com.library.backend.repository.TaiKhoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocGiaServiceSecurityTest {
    @Mock DocGiaRepository docGiaRepository;
    @Mock TaiKhoanRepository taiKhoanRepository;
    @Mock NhomDocGiaRepository nhomDocGiaRepository;
    @Mock GoiThanhVienRepository goiThanhVienRepository;
    @Mock LichSuGoiThanhVienRepository lichSuGoiThanhVienRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JdbcTemplate jdbcTemplate;

    @Test
    void legacyReaderHardDeleteIsDisabledAtServiceBoundary() {
        assertThatThrownBy(() -> service().hardDelete("DG001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("hard delete");
        verify(docGiaRepository, never()).findById("DG001");
        verify(docGiaRepository, never()).delete(org.mockito.ArgumentMatchers.any());
        verify(taiKhoanRepository, never()).deleteById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void legacyUnpagedReaderListIsDisabled() {
        assertThatThrownBy(() -> service().getAll())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("phan trang");
        verify(docGiaRepository, never()).findAll();
    }

    private DocGiaService service() {
        return new DocGiaService(docGiaRepository, taiKhoanRepository, nhomDocGiaRepository,
                goiThanhVienRepository, lichSuGoiThanhVienRepository, passwordEncoder, jdbcTemplate);
    }
}

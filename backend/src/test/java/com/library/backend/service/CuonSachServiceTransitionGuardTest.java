package com.library.backend.service;

import com.library.backend.dto.CuonSachRequest;
import com.library.backend.entity.CuonSach;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.repository.ChiNhanhRepository;
import com.library.backend.repository.CuonSachRepository;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.repository.TrangThaiCuonSachRepository;
import com.library.backend.repository.ViTriSachRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuonSachServiceTransitionGuardTest {
    @Mock CuonSachRepository copyRepository;
    @Mock DauSachRepository titleRepository;
    @Mock ChiNhanhRepository branchRepository;
    @Mock ViTriSachRepository locationRepository;
    @Mock TrangThaiCuonSachRepository statusRepository;

    private CuonSachService service;

    @BeforeEach
    void setUp() {
        service = new CuonSachService(copyRepository, titleRepository, branchRepository, locationRepository, statusRepository);
    }

    @Test
    void genericUpdateCannotTamperWithOperationalStatus() {
        CuonSach current = current();
        when(copyRepository.findById("CS1")).thenReturn(Optional.of(current));
        allowForeignKeys();
        CuonSachRequest request = matchingRequest();
        request.setMaTrangThai("TT_DANGMUON");

        assertThatThrownBy(() -> service.update("CS1", request))
                .isInstanceOf(CatalogValidationException.class)
                .extracting(ex -> ((CatalogValidationException) ex).getErrorCode())
                .isEqualTo("COPY_ACTION_REQUIRED");
        verify(copyRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void genericUpdateCannotMoveLocationOrBranch() {
        when(copyRepository.findById("CS1")).thenReturn(Optional.of(current()));
        allowForeignKeys();
        CuonSachRequest request = matchingRequest();
        request.setMaViTri("VT2");

        assertThatThrownBy(() -> service.update("CS1", request))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("move-location");
    }

    @Test
    void legacyHardDeleteIsDisabledAtServiceBoundary() {
        assertThatThrownBy(() -> service.hardDelete("CS1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("hard delete");
        verify(copyRepository, never()).findById("CS1");
        verify(copyRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    private void allowForeignKeys() {
        when(titleRepository.existsById("DS1")).thenReturn(true);
        when(branchRepository.existsById("CN1")).thenReturn(true);
        when(locationRepository.existsById(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
    }

    private CuonSach current() {
        CuonSach copy = new CuonSach();
        copy.setMaCuonSach("CS1");
        copy.setMaDauSach("DS1");
        copy.setMaChiNhanh("CN1");
        copy.setMaViTri("VT1");
        copy.setMaTrangThai("TT_SANCO");
        return copy;
    }

    private CuonSachRequest matchingRequest() {
        CuonSachRequest request = new CuonSachRequest();
        request.setMaCuonSach("CS1");
        request.setMaDauSach("DS1");
        request.setMaChiNhanh("CN1");
        request.setMaViTri("VT1");
        request.setMaTrangThai("TT_SANCO");
        return request;
    }
}

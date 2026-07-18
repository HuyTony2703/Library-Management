package com.library.backend.security;

import com.library.backend.entity.DocGia;
import com.library.backend.entity.TaiKhoan;
import com.library.backend.entity.VaiTro;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.NhanVienRepository;
import com.library.backend.repository.TaiKhoanRepository;
import com.library.backend.repository.VaiTroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedPrincipalServiceTest {

    @Mock
    private TaiKhoanRepository taiKhoanRepository;
    @Mock
    private VaiTroRepository vaiTroRepository;
    @Mock
    private DocGiaRepository docGiaRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;

    private AuthenticatedPrincipalService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticatedPrincipalService(
                taiKhoanRepository,
                vaiTroRepository,
                docGiaRepository,
                nhanVienRepository
        );
    }

    @Test
    void rejectsLockedAccountEvenWhenTokenWasPreviouslyValid() {
        TaiKhoan account = account("TK_LOCKED", "Khóa", "VT_THU_THU");
        when(taiKhoanRepository.findById("TK_LOCKED")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.refresh(tokenUser("TK_LOCKED", RoleConstants.LIBRARIAN)))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("không ở trạng thái hoạt động");
    }

    @Test
    void refreshesReaderWithoutRequiringStaffProfile() {
        TaiKhoan account = account("TK_READER", "Hoạt động", "VT_DOC_GIA");
        VaiTro role = role("VT_DOC_GIA", RoleConstants.READER);
        DocGia reader = new DocGia();
        reader.setMaDocGia("DG001");
        reader.setHoTen("Độc giả A");

        when(taiKhoanRepository.findById("TK_READER")).thenReturn(Optional.of(account));
        when(vaiTroRepository.findById("VT_DOC_GIA")).thenReturn(Optional.of(role));
        when(docGiaRepository.findByMaTaiKhoan("TK_READER")).thenReturn(Optional.of(reader));
        when(nhanVienRepository.findByMaTaiKhoan("TK_READER")).thenReturn(Optional.empty());

        AuthUser refreshed = service.refresh(tokenUser("TK_READER", RoleConstants.READER));

        assertThat(refreshed.getMaDocGia()).isEqualTo("DG001");
        assertThat(refreshed.getMaNhanVien()).isNull();
        assertThat(refreshed.getTenVaiTro()).isEqualTo(RoleConstants.READER);
    }

    @Test
    void rejectsActiveAccountWhenReaderHasLoginScopeLock() {
        TaiKhoan account = account("TK_READER", "Hoạt động", "VT_DOC_GIA");
        when(taiKhoanRepository.findById("TK_READER")).thenReturn(Optional.of(account));
        when(taiKhoanRepository.hasActiveReaderLoginLock("TK_READER")).thenReturn(true);

        assertThatThrownBy(() -> service.refresh(tokenUser("TK_READER", RoleConstants.READER)))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("khóa đăng nhập");
    }

    @Test
    void rejectsTokenIssuedBeforeTokenVersionWasIncremented() {
        TaiKhoan account = account("TK_READER", "Hoạt động", "VT_DOC_GIA");
        account.setTokenVersion(2);
        when(taiKhoanRepository.findById("TK_READER")).thenReturn(Optional.of(account));

        AuthUser oldToken = new AuthUser("TK_READER", "reader", "VT_DOC_GIA", RoleConstants.READER,
                "DG001", null, "Reader", 1, false);
        assertThatThrownBy(() -> service.refresh(oldToken))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class)
                .hasMessageContaining("thu hồi");
    }

    @Test
    void keepsAdminAuthenticatedWithoutInventingStaffProfile() {
        TaiKhoan account = account("TK_ADMIN", "Hoạt động", "VT_ADMIN");
        VaiTro role = role("VT_ADMIN", RoleConstants.ADMIN);

        when(taiKhoanRepository.findById("TK_ADMIN")).thenReturn(Optional.of(account));
        when(vaiTroRepository.findById("VT_ADMIN")).thenReturn(Optional.of(role));
        when(docGiaRepository.findByMaTaiKhoan("TK_ADMIN")).thenReturn(Optional.empty());
        when(nhanVienRepository.findByMaTaiKhoan("TK_ADMIN")).thenReturn(Optional.empty());

        AuthUser refreshed = service.refresh(tokenUser("TK_ADMIN", RoleConstants.ADMIN));

        assertThat(refreshed.getTenVaiTro()).isEqualTo(RoleConstants.ADMIN);
        assertThat(refreshed.getMaNhanVien()).isNull();
        assertThat(refreshed.getMaDocGia()).isNull();
    }

    private TaiKhoan account(String id, String status, String roleId) {
        TaiKhoan account = new TaiKhoan();
        account.setMaTaiKhoan(id);
        account.setTenDangNhap(id.toLowerCase());
        account.setTrangThai(status);
        account.setMaVaiTro(roleId);
        return account;
    }

    private VaiTro role(String id, String name) {
        VaiTro role = new VaiTro();
        role.setMaVaiTro(id);
        role.setTenVaiTro(name);
        return role;
    }

    private AuthUser tokenUser(String accountId, String roleName) {
        return new AuthUser(
                accountId,
                accountId.toLowerCase(),
                "TOKEN_ROLE",
                roleName,
                null,
                null,
                null
        );
    }
}

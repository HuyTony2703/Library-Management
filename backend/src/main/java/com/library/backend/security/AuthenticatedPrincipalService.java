package com.library.backend.security;

import com.library.backend.entity.DocGia;
import com.library.backend.entity.NhanVien;
import com.library.backend.entity.TaiKhoan;
import com.library.backend.entity.VaiTro;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.NhanVienRepository;
import com.library.backend.repository.TaiKhoanRepository;
import com.library.backend.repository.VaiTroRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedPrincipalService {

    private static final String ACCOUNT_ACTIVE = "Hoạt động";
    private static final String STAFF_ACTIVE = "Đang làm";

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final DocGiaRepository docGiaRepository;
    private final NhanVienRepository nhanVienRepository;

    public AuthenticatedPrincipalService(
            TaiKhoanRepository taiKhoanRepository,
            VaiTroRepository vaiTroRepository,
            DocGiaRepository docGiaRepository,
            NhanVienRepository nhanVienRepository
    ) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.docGiaRepository = docGiaRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    public AuthUser refresh(AuthUser tokenUser) {
        TaiKhoan account = taiKhoanRepository.findById(tokenUser.getMaTaiKhoan())
                .orElseThrow(() -> new BadCredentialsException("Tài khoản không tồn tại"));

        if (!ACCOUNT_ACTIVE.equals(account.getTrangThai())) {
            throw new DisabledException("Tài khoản không ở trạng thái hoạt động");
        }

        if (taiKhoanRepository.hasActiveReaderLoginLock(account.getMaTaiKhoan())) {
            throw new DisabledException("Tài khoản đang bị khóa đăng nhập");
        }

        if (tokenUser.getTokenVersion() != account.getTokenVersion()) {
            throw new BadCredentialsException("Token đã bị thu hồi");
        }

        VaiTro role = vaiTroRepository.findById(account.getMaVaiTro())
                .orElseThrow(() -> new BadCredentialsException("Vai trò tài khoản không tồn tại"));

        DocGia reader = docGiaRepository.findByMaTaiKhoan(account.getMaTaiKhoan()).orElse(null);
        NhanVien staff = nhanVienRepository.findByMaTaiKhoan(account.getMaTaiKhoan()).orElse(null);

        if (staff != null && !STAFF_ACTIVE.equals(staff.getTrangThai())) {
            throw new DisabledException("Nhân viên không ở trạng thái đang làm");
        }

        if (RoleConstants.LIBRARIAN.equals(role.getTenVaiTro()) && staff == null) {
            throw new DisabledException("Tài khoản thủ thư chưa liên kết hồ sơ nhân viên");
        }

        String displayName = staff != null
                ? staff.getHoTen()
                : reader == null ? tokenUser.getHoTen() : reader.getHoTen();

        return new AuthUser(
                account.getMaTaiKhoan(),
                account.getTenDangNhap(),
                account.getMaVaiTro(),
                role.getTenVaiTro(),
                reader == null ? null : reader.getMaDocGia(),
                staff == null ? null : staff.getMaNhanVien(),
                displayName,
                account.getTokenVersion(),
                account.isMustChangePassword()
        );
    }
}

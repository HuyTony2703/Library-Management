package com.library.backend.service;

import com.library.backend.dto.AuthResponse;
import com.library.backend.dto.ChangePasswordRequest;
import com.library.backend.dto.LoginRequest;
import com.library.backend.entity.DocGia;
import com.library.backend.entity.NhanVien;
import com.library.backend.entity.TaiKhoan;
import com.library.backend.entity.VaiTro;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.NhanVienRepository;
import com.library.backend.repository.TaiKhoanRepository;
import com.library.backend.repository.VaiTroRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final DocGiaRepository docGiaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ActivityLogService activityLogService;

    public AuthService(
            TaiKhoanRepository taiKhoanRepository,
            VaiTroRepository vaiTroRepository,
            DocGiaRepository docGiaRepository,
            NhanVienRepository nhanVienRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            ActivityLogService activityLogService
    ) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.docGiaRepository = docGiaRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.activityLogService = activityLogService;
    }

    public AuthResponse login(LoginRequest request) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(request.getUsernameOrEmail())
                .or(() -> taiKhoanRepository.findByEmailDangNhap(request.getUsernameOrEmail()))
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập/email hoặc mật khẩu không đúng"));

        if (!"Hoạt động".equals(taiKhoan.getTrangThai())) {
            throw new RuntimeException("Tài khoản không ở trạng thái hoạt động");
        }

        if (!passwordEncoder.matches(request.getPassword(), taiKhoan.getMatKhauHash())) {
            throw new RuntimeException("Tên đăng nhập/email hoặc mật khẩu không đúng");
        }

        VaiTro vaiTro = vaiTroRepository.findById(taiKhoan.getMaVaiTro())
                .orElseThrow(() -> new RuntimeException("Vai trò tài khoản không tồn tại"));

        AuthUser user = buildAuthUser(taiKhoan, vaiTro);
        String token = tokenService.generateToken(user);

        taiKhoan.setLanDangNhapCuoi(LocalDateTime.now());
        taiKhoanRepository.save(taiKhoan);

        activityLogService.logAsAccountSafe(
                taiKhoan.getMaTaiKhoan(),
                "Đăng nhập",
                "TAIKHOAN",
                taiKhoan.getMaTaiKhoan(),
                "Tài khoản " + taiKhoan.getTenDangNhap() + " đăng nhập thành công"
        );

        return toResponse(token, user);
    }

    public AuthResponse me(AuthUser user) {
        return toResponse(null, enrichDisplayName(user));
    }

    @Transactional
    public void changePassword(AuthUser user, ChangePasswordRequest request) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(user.getMaTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), taiKhoan.getMatKhauHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(request.getNewPassword(), taiKhoan.getMatKhauHash())) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.getNewPassword()));
        taiKhoanRepository.save(taiKhoan);

        activityLogService.logAsAccountSafe(
                taiKhoan.getMaTaiKhoan(),
                "Đổi mật khẩu",
                "TAIKHOAN",
                taiKhoan.getMaTaiKhoan(),
                "Tài khoản " + taiKhoan.getTenDangNhap() + " đổi mật khẩu"
        );
    }

    private AuthUser buildAuthUser(TaiKhoan taiKhoan, VaiTro vaiTro) {
        DocGia docGia = docGiaRepository.findByMaTaiKhoan(taiKhoan.getMaTaiKhoan())
                .orElse(null);

        NhanVien nhanVien = nhanVienRepository.findByMaTaiKhoan(taiKhoan.getMaTaiKhoan())
                .orElse(null);

        String maDocGia = docGia == null ? null : docGia.getMaDocGia();
        String maNhanVien = nhanVien == null ? null : nhanVien.getMaNhanVien();
        String hoTen = nhanVien != null
                ? nhanVien.getHoTen()
                : docGia == null ? null : docGia.getHoTen();

        return new AuthUser(
                taiKhoan.getMaTaiKhoan(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getMaVaiTro(),
                vaiTro.getTenVaiTro(),
                maDocGia,
                maNhanVien,
                hoTen
        );
    }

    private AuthResponse toResponse(String token, AuthUser user) {
        return new AuthResponse(
                token,
                "Bearer",
                user.getMaTaiKhoan(),
                user.getTenDangNhap(),
                user.getMaVaiTro(),
                user.getTenVaiTro(),
                user.getMaDocGia(),
                user.getMaNhanVien(),
                user.getHoTen()
        );
    }

    private AuthUser enrichDisplayName(AuthUser user) {
        if (hasText(user.getHoTen())) {
            return user;
        }

        String hoTen = null;

        if (hasText(user.getMaNhanVien())) {
            hoTen = nhanVienRepository.findById(user.getMaNhanVien())
                    .map(NhanVien::getHoTen)
                    .orElse(null);
        }

        if (!hasText(hoTen) && hasText(user.getMaDocGia())) {
            hoTen = docGiaRepository.findById(user.getMaDocGia())
                    .map(DocGia::getHoTen)
                    .orElse(null);
        }

        return new AuthUser(
                user.getMaTaiKhoan(),
                user.getTenDangNhap(),
                user.getMaVaiTro(),
                user.getTenVaiTro(),
                user.getMaDocGia(),
                user.getMaNhanVien(),
                hoTen
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

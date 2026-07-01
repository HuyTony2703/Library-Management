package com.library.backend.service;

import com.library.backend.dto.AuthResponse;
import com.library.backend.dto.ChangePasswordRequest;
import com.library.backend.dto.LoginRequest;
import com.library.backend.dto.ProfileUpdateRequest;
import com.library.backend.entity.DocGia;
import com.library.backend.entity.NhanVien;
import com.library.backend.entity.TaiKhoan;
import com.library.backend.entity.VaiTro;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.NhanVienRepository;
import com.library.backend.repository.TaiKhoanRepository;
import com.library.backend.repository.VaiTroRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.AuthenticatedPrincipalService;
import com.library.backend.security.TokenService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final AuthenticatedPrincipalService authenticatedPrincipalService;
    private final ActivityLogService activityLogService;
    private final JdbcTemplate jdbcTemplate;

    public AuthService(
            TaiKhoanRepository taiKhoanRepository,
            VaiTroRepository vaiTroRepository,
            DocGiaRepository docGiaRepository,
            NhanVienRepository nhanVienRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            AuthenticatedPrincipalService authenticatedPrincipalService,
            ActivityLogService activityLogService,
            JdbcTemplate jdbcTemplate
    ) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.docGiaRepository = docGiaRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.authenticatedPrincipalService = authenticatedPrincipalService;
        this.activityLogService = activityLogService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public AuthResponse login(LoginRequest request) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(request.getUsernameOrEmail())
                .or(() -> taiKhoanRepository.findByEmailDangNhap(request.getUsernameOrEmail()))
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập/email hoặc mật khẩu không đúng"));

        if (!"Hoạt động".equals(taiKhoan.getTrangThai())) {
            throw new RuntimeException("Tài khoản không ở trạng thái hoạt động");
        }

        if (taiKhoanRepository.hasActiveReaderLoginLock(taiKhoan.getMaTaiKhoan())) {
            throw new RuntimeException("Tài khoản đang bị khóa đăng nhập");
        }

        if (!passwordEncoder.matches(request.getPassword(), taiKhoan.getMatKhauHash())) {
            throw new RuntimeException("Tên đăng nhập/email hoặc mật khẩu không đúng");
        }

        VaiTro vaiTro = vaiTroRepository.findById(taiKhoan.getMaVaiTro())
                .orElseThrow(() -> new RuntimeException("Vai trò tài khoản không tồn tại"));

        AuthUser user = authenticatedPrincipalService.refresh(buildAuthUser(taiKhoan, vaiTro));
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
    public AuthResponse changePassword(AuthUser user, ChangePasswordRequest request) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(user.getMaTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), taiKhoan.getMatKhauHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(request.getNewPassword(), taiKhoan.getMatKhauHash())) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.getNewPassword()));
        taiKhoan.setMustChangePassword(false);
        taiKhoan.setPasswordChangedAt(LocalDateTime.now());
        taiKhoan.setTokenVersion(taiKhoan.getTokenVersion() + 1);
        taiKhoanRepository.save(taiKhoan);

        activityLogService.logAsAccountSafe(taiKhoan.getMaTaiKhoan(), "Đổi mật khẩu", "TAIKHOAN",
                taiKhoan.getMaTaiKhoan(), "Tài khoản đổi mật khẩu và thu hồi các phiên cũ");

        VaiTro role = vaiTroRepository.findById(taiKhoan.getMaVaiTro())
                .orElseThrow(() -> new RuntimeException("Vai trò tài khoản không tồn tại"));
        AuthUser refreshed = authenticatedPrincipalService.refresh(buildAuthUser(taiKhoan, role));
        return toResponse(tokenService.generateToken(refreshed), refreshed);
    }

    @Transactional
    public AuthResponse updateProfile(AuthUser user, ProfileUpdateRequest request) {
        String hoTen = cleanRequired(request.getHoTen(), "Họ tên không được để trống");
        String email = cleanRequired(request.getEmail(), "Email không được để trống");
        String soDienThoai = trimToNull(request.getSoDienThoai());
        String diaChi = trimToNull(request.getDiaChi());

        TaiKhoan taiKhoan = taiKhoanRepository.findById(user.getMaTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        taiKhoanRepository.findByEmailDangNhap(email)
                .filter(existing -> !existing.getMaTaiKhoan().equals(taiKhoan.getMaTaiKhoan()))
                .ifPresent(existing -> {
                    throw new RuntimeException("Email đăng nhập đã được sử dụng bởi tài khoản khác");
                });

        taiKhoan.setEmailDangNhap(email);
        taiKhoanRepository.save(taiKhoan);

        if (hasText(user.getMaNhanVien())) {
            NhanVien nhanVien = nhanVienRepository.findById(user.getMaNhanVien())
                    .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

            nhanVien.setHoTen(hoTen);
            nhanVien.setEmail(email);
            nhanVien.setSoDienThoai(soDienThoai);
            nhanVien.setDiaChi(diaChi);
            nhanVienRepository.save(nhanVien);
        } else if (hasText(user.getMaDocGia())) {
            DocGia docGia = docGiaRepository.findById(user.getMaDocGia())
                    .orElseThrow(() -> new RuntimeException("Độc giả không tồn tại"));

            if (docGiaRepository.existsByEmailAndMaDocGiaNot(email, docGia.getMaDocGia())) {
                throw new RuntimeException("Email đã được sử dụng bởi độc giả khác");
            }

            docGia.setHoTen(hoTen);
            docGia.setEmail(email);
            docGia.setSoDienThoai(soDienThoai);
            docGia.setDiaChi(diaChi);
            docGiaRepository.save(docGia);
        } else {
            throw new RuntimeException("Tài khoản không gắn với hồ sơ nhân viên hoặc độc giả");
        }

        activityLogService.logAsAccountSafe(
                taiKhoan.getMaTaiKhoan(),
                "Cập nhật hồ sơ",
                "TAIKHOAN",
                taiKhoan.getMaTaiKhoan(),
                "Tài khoản " + taiKhoan.getTenDangNhap() + " cập nhật thông tin cá nhân"
        );

        AuthUser refreshed = authenticatedPrincipalService.refresh(
                buildAuthUser(taiKhoan, vaiTroRepository.findById(taiKhoan.getMaVaiTro())
                        .orElseThrow(() -> new RuntimeException("Vai trò tài khoản không tồn tại")))
        );

        return toResponse(null, refreshed);
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
                hoTen,
                taiKhoan.getTokenVersion(),
                taiKhoan.isMustChangePassword()
        );
    }

    private AuthResponse toResponse(String token, AuthUser user) {
        ProfileSnapshot profile = getProfileSnapshot(user);

        return new AuthResponse(
                token,
                "Bearer",
                user.getMaTaiKhoan(),
                user.getTenDangNhap(),
                user.getMaVaiTro(),
                user.getTenVaiTro(),
                user.getMaDocGia(),
                user.getMaNhanVien(),
                profile.hoTen() != null ? profile.hoTen() : user.getHoTen(),
                profile.email(),
                profile.soDienThoai(),
                profile.diaChi(),
                user.isMustChangePassword()
        );
    }

    private ProfileSnapshot getProfileSnapshot(AuthUser user) {
        if (hasText(user.getMaNhanVien())) {
            return nhanVienRepository.findById(user.getMaNhanVien())
                    .map(nhanVien -> new ProfileSnapshot(
                            nhanVien.getHoTen(),
                            nhanVien.getEmail(),
                            nhanVien.getSoDienThoai(),
                            nhanVien.getDiaChi()
                    ))
                    .orElse(new ProfileSnapshot(user.getHoTen(), null, null, null));
        }

        if (hasText(user.getMaDocGia())) {
            return docGiaRepository.findById(user.getMaDocGia())
                    .map(docGia -> new ProfileSnapshot(
                            docGia.getHoTen(),
                            docGia.getEmail(),
                            docGia.getSoDienThoai(),
                            docGia.getDiaChi()
                    ))
                    .orElse(new ProfileSnapshot(user.getHoTen(), null, null, null));
        }

        return new ProfileSnapshot(user.getHoTen(), null, null, null);
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
                hoTen,
                user.getTokenVersion(),
                user.isMustChangePassword()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String cleanRequired(String value, String message) {
        String trimmed = trimToNull(value);

        if (trimmed == null) {
            throw new RuntimeException(message);
        }

        return trimmed;
    }

    private record ProfileSnapshot(String hoTen, String email, String soDienThoai, String diaChi) {}
}

package com.library.backend.service.admin;

import com.library.backend.dto.AdminLibrarianCreateRequest;
import com.library.backend.dto.AdminLibrarianResponse;
import com.library.backend.dto.AdminLibrarianStatusRequest;
import com.library.backend.dto.AdminLibrarianUpdateRequest;
import com.library.backend.dto.ResetPasswordRequest;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.service.ActivityLogService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Service
public class AdminLibrarianService {

    private static final String ROLE_THU_THU = "VT_THU_THU";
    private static final String NHAN_VIEN_DANG_LAM = "Đang làm";
    private static final String NHAN_VIEN_TAM_KHOA = "Tạm khóa";
    private static final String NHAN_VIEN_NGHI_VIEC = "Nghỉ việc";
    private static final String TAI_KHOAN_HOAT_DONG = "Hoạt động";
    private static final String TAI_KHOAN_KHOA = "Khóa";
    private static final String TAI_KHOAN_NGUNG_HOAT_DONG = "Ngừng hoạt động";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public AdminLibrarianService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            ActivityLogService activityLogService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    public List<AdminLibrarianResponse> getAll() {
        String sql = baseSelectSql() + """
                WHERE tk.MaVaiTro = ?
                ORDER BY nv.MaNhanVien
                """;

        return jdbcTemplate.query(sql, this::mapRow, ROLE_THU_THU);
    }

    public AdminLibrarianResponse getById(String maNhanVien) {
        String sql = baseSelectSql() + """
                WHERE nv.MaNhanVien = ?
                  AND tk.MaVaiTro = ?
                """;

        List<AdminLibrarianResponse> result = jdbcTemplate.query(
                sql,
                this::mapRow,
                maNhanVien,
                ROLE_THU_THU
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy thủ thư: " + maNhanVien);
        }

        return result.get(0);
    }

    @Transactional
    public AdminLibrarianResponse create(AdminLibrarianCreateRequest request) {
        validateCreateRequest(request);

        if (exists("TAIKHOAN", "MaTaiKhoan", request.getMaTaiKhoan())) {
            throw new BusinessException("Mã tài khoản đã tồn tại");
        }

        if (exists("TAIKHOAN", "TenDangNhap", request.getTenDangNhap())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }

        if (exists("TAIKHOAN", "EmailDangNhap", request.getEmailDangNhap())) {
            throw new BusinessException("Email đăng nhập đã tồn tại");
        }

        if (exists("NHANVIEN", "MaNhanVien", request.getMaNhanVien())) {
            throw new BusinessException("Mã nhân viên đã tồn tại");
        }

        validateBranchIfPresent(request.getMaChiNhanh());

        jdbcTemplate.update(
                """
                INSERT INTO TAIKHOAN
                (
                    MaTaiKhoan,
                    TenDangNhap,
                    MatKhauHash,
                    EmailDangNhap,
                    MaVaiTro,
                    TrangThai
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                request.getMaTaiKhoan(),
                request.getTenDangNhap(),
                passwordEncoder.encode(request.getMatKhau()),
                request.getEmailDangNhap(),
                ROLE_THU_THU,
                TAI_KHOAN_HOAT_DONG
        );

        jdbcTemplate.update(
                """
                INSERT INTO NHANVIEN
                (
                    MaNhanVien,
                    MaTaiKhoan,
                    MaChiNhanh,
                    HoTen,
                    NgaySinh,
                    Email,
                    SoDienThoai,
                    DiaChi,
                    TrangThai
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                request.getMaNhanVien(),
                request.getMaTaiKhoan(),
                emptyToNull(request.getMaChiNhanh()),
                request.getHoTen(),
                toSqlDate(request.getNgaySinh()),
                emptyToNull(request.getEmail()),
                emptyToNull(request.getSoDienThoai()),
                emptyToNull(request.getDiaChi()),
                NHAN_VIEN_DANG_LAM
        );

        syncDefaultBranchAssignment(request.getMaNhanVien(), request.getMaChiNhanh());

        activityLogService.logSafe(
                "Thêm thủ thư",
                "NHANVIEN",
                request.getMaNhanVien(),
                "Admin thêm thủ thư " + request.getHoTen()
        );

        return getById(request.getMaNhanVien());
    }

    @Transactional
    public AdminLibrarianResponse update(String maNhanVien, AdminLibrarianUpdateRequest request) {
        AdminLibrarianResponse current = getById(maNhanVien);

        validateBranchIfPresent(request.getMaChiNhanh());

        if (isBlank(request.getHoTen())) {
            throw new BusinessException("Họ tên không được để trống");
        }

        jdbcTemplate.update(
                """
                UPDATE NHANVIEN
                SET
                    MaChiNhanh = ?,
                    HoTen = ?,
                    NgaySinh = ?,
                    Email = ?,
                    SoDienThoai = ?,
                    DiaChi = ?
                WHERE MaNhanVien = ?
                """,
                emptyToNull(request.getMaChiNhanh()),
                request.getHoTen(),
                toSqlDate(request.getNgaySinh()),
                emptyToNull(request.getEmail()),
                emptyToNull(request.getSoDienThoai()),
                emptyToNull(request.getDiaChi()),
                maNhanVien
        );

        syncDefaultBranchAssignment(maNhanVien, request.getMaChiNhanh());

        activityLogService.logSafe(
                "Cập nhật thủ thư",
                "NHANVIEN",
                maNhanVien,
                "Admin cập nhật thông tin thủ thư " + current.getTenDangNhap()
        );

        return getById(maNhanVien);
    }

    @Transactional
    public AdminLibrarianResponse updateStatus(
            String maNhanVien,
            AdminLibrarianStatusRequest request
    ) {
        AdminLibrarianResponse current = getById(maNhanVien);
        String trangThaiNhanVien = request.getTrangThaiNhanVien();

        if (!NHAN_VIEN_DANG_LAM.equals(trangThaiNhanVien)
                && !NHAN_VIEN_TAM_KHOA.equals(trangThaiNhanVien)
                && !NHAN_VIEN_NGHI_VIEC.equals(trangThaiNhanVien)) {
            throw new BusinessException("Trạng thái nhân viên không hợp lệ");
        }

        String trangThaiTaiKhoan = switch (trangThaiNhanVien) {
            case NHAN_VIEN_DANG_LAM -> TAI_KHOAN_HOAT_DONG;
            case NHAN_VIEN_TAM_KHOA -> TAI_KHOAN_KHOA;
            default -> TAI_KHOAN_NGUNG_HOAT_DONG;
        };

        jdbcTemplate.update(
                "UPDATE NHANVIEN SET TrangThai = ? WHERE MaNhanVien = ?",
                trangThaiNhanVien,
                maNhanVien
        );

        jdbcTemplate.update(
                "UPDATE TAIKHOAN SET TrangThai = ? WHERE MaTaiKhoan = ?",
                trangThaiTaiKhoan,
                current.getMaTaiKhoan()
        );

        activityLogService.logSafe(
                "Đổi trạng thái thủ thư",
                "NHANVIEN",
                maNhanVien,
                "Admin đổi trạng thái thủ thư "
                        + current.getTenDangNhap()
                        + " thành "
                        + trangThaiNhanVien
        );

        return getById(maNhanVien);
    }

    @Transactional
    public void resetPassword(String maNhanVien, ResetPasswordRequest request) {
        AdminLibrarianResponse current = getById(maNhanVien);

        if (request.getMatKhauMoi() == null || request.getMatKhauMoi().length() < 6) {
            throw new BusinessException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        jdbcTemplate.update(
                "UPDATE TAIKHOAN SET MatKhauHash = ? WHERE MaTaiKhoan = ?",
                passwordEncoder.encode(request.getMatKhauMoi()),
                current.getMaTaiKhoan()
        );

        activityLogService.logSafe(
                "Reset mật khẩu thủ thư",
                "TAIKHOAN",
                current.getMaTaiKhoan(),
                "Admin reset mật khẩu cho thủ thư " + current.getTenDangNhap()
        );
    }

    @Transactional
    public void delete(String maNhanVien) {
        AdminLibrarianResponse current = getById(maNhanVien);

        jdbcTemplate.update(
                "UPDATE NHANVIEN SET TrangThai = ? WHERE MaNhanVien = ?",
                NHAN_VIEN_NGHI_VIEC,
                maNhanVien
        );

        jdbcTemplate.update(
                "UPDATE TAIKHOAN SET TrangThai = ? WHERE MaTaiKhoan = ?",
                TAI_KHOAN_NGUNG_HOAT_DONG,
                current.getMaTaiKhoan()
        );

        activityLogService.logSafe(
                "Xóa thủ thư",
                "NHANVIEN",
                maNhanVien,
                "Admin vô hiệu hóa thủ thư " + current.getTenDangNhap()
        );
    }

    @Transactional
    public void hardDelete(String maNhanVien) {
        AdminLibrarianResponse current = getById(maNhanVien);

        if (staffBranchTableExists()) {
            jdbcTemplate.update(
                    "DELETE FROM NHANVIEN_CHINHANH WHERE MaNhanVien = ?",
                    maNhanVien
            );
        }

        jdbcTemplate.update("DELETE FROM NHANVIEN WHERE MaNhanVien = ?", maNhanVien);
        jdbcTemplate.update("DELETE FROM TAIKHOAN WHERE MaTaiKhoan = ?", current.getMaTaiKhoan());

        activityLogService.logSafe(
                "Xóa vĩnh viễn thủ thư",
                "NHANVIEN",
                maNhanVien,
                "Admin xóa vĩnh viễn thủ thư " + current.getTenDangNhap()
        );
    }

    private String baseSelectSql() {
        return """
                SELECT
                    nv.MaNhanVien,
                    tk.MaTaiKhoan,
                    tk.TenDangNhap,
                    tk.EmailDangNhap,
                    tk.MaVaiTro,
                    nv.MaChiNhanh,
                    cn.TenChiNhanh,
                    nv.HoTen,
                    nv.NgaySinh,
                    nv.Email,
                    nv.SoDienThoai,
                    nv.DiaChi,
                    nv.NgayVaoLam,
                    tk.TrangThai AS TrangThaiTaiKhoan,
                    nv.TrangThai AS TrangThaiNhanVien,
                    tk.LanDangNhapCuoi
                FROM NHANVIEN nv
                INNER JOIN TAIKHOAN tk ON nv.MaTaiKhoan = tk.MaTaiKhoan
                LEFT JOIN CHINHANH cn ON nv.MaChiNhanh = cn.MaChiNhanh
                """;
    }

    private AdminLibrarianResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date ngaySinh = rs.getDate("NgaySinh");
        Date ngayVaoLam = rs.getDate("NgayVaoLam");
        Timestamp lanDangNhapCuoi = rs.getTimestamp("LanDangNhapCuoi");

        return new AdminLibrarianResponse(
                rs.getString("MaNhanVien"),
                rs.getString("MaTaiKhoan"),
                rs.getString("TenDangNhap"),
                rs.getString("EmailDangNhap"),
                rs.getString("MaVaiTro"),
                rs.getString("MaChiNhanh"),
                rs.getString("TenChiNhanh"),
                rs.getString("HoTen"),
                ngaySinh == null ? null : ngaySinh.toLocalDate(),
                rs.getString("Email"),
                rs.getString("SoDienThoai"),
                rs.getString("DiaChi"),
                ngayVaoLam == null ? null : ngayVaoLam.toLocalDate(),
                rs.getString("TrangThaiTaiKhoan"),
                rs.getString("TrangThaiNhanVien"),
                lanDangNhapCuoi == null ? null : lanDangNhapCuoi.toLocalDateTime()
        );
    }

    private void validateCreateRequest(AdminLibrarianCreateRequest request) {
        if (isBlank(request.getMaNhanVien())) {
            throw new BusinessException("Mã nhân viên không được để trống");
        }

        if (isBlank(request.getMaTaiKhoan())) {
            throw new BusinessException("Mã tài khoản không được để trống");
        }

        if (isBlank(request.getTenDangNhap())) {
            throw new BusinessException("Tên đăng nhập không được để trống");
        }

        if (isBlank(request.getEmailDangNhap())) {
            throw new BusinessException("Email đăng nhập không được để trống");
        }

        if (request.getMatKhau() == null || request.getMatKhau().length() < 6) {
            throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        if (isBlank(request.getHoTen())) {
            throw new BusinessException("Họ tên không được để trống");
        }
    }

    private void validateBranchIfPresent(String maChiNhanh) {
        if (!isBlank(maChiNhanh) && !exists("CHINHANH", "MaChiNhanh", maChiNhanh)) {
            throw new ResourceNotFoundException("Chi nhánh không tồn tại");
        }
    }

    private void syncDefaultBranchAssignment(String maNhanVien, String maChiNhanh) {
        if (!staffBranchTableExists()) {
            return;
        }

        jdbcTemplate.update(
                """
                UPDATE NHANVIEN_CHINHANH
                SET LaMacDinh = 0
                WHERE MaNhanVien = ?
                  AND LaMacDinh = 1
                """,
                maNhanVien
        );

        if (isBlank(maChiNhanh)) {
            return;
        }

        jdbcTemplate.update(
                """
                MERGE NHANVIEN_CHINHANH AS target
                USING (SELECT ? AS MaNhanVien, ? AS MaChiNhanh) AS source
                ON target.MaNhanVien = source.MaNhanVien
                   AND target.MaChiNhanh = source.MaChiNhanh
                WHEN MATCHED THEN
                    UPDATE SET
                        LaMacDinh = 1,
                        NgayBatDau = CASE
                            WHEN target.NgayBatDau > CAST(SYSDATETIME() AS DATE)
                            THEN CAST(SYSDATETIME() AS DATE)
                            ELSE target.NgayBatDau
                        END,
                        NgayKetThuc = NULL,
                        TrangThai = N'Hoạt động'
                WHEN NOT MATCHED THEN
                    INSERT
                    (
                        MaNhanVien,
                        MaChiNhanh,
                        LaMacDinh,
                        NgayBatDau,
                        NgayKetThuc,
                        TrangThai
                    )
                    VALUES
                    (
                        source.MaNhanVien,
                        source.MaChiNhanh,
                        1,
                        CAST(SYSDATETIME() AS DATE),
                        NULL,
                        N'Hoạt động'
                    );
                """,
                maNhanVien,
                maChiNhanh
        );
    }

    private boolean staffBranchTableExists() {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT CASE WHEN OBJECT_ID('dbo.NHANVIEN_CHINHANH', 'U') IS NULL THEN 0 ELSE 1 END",
                Integer.class
        );
        return result != null && result == 1;
    }

    private boolean exists(String tableName, String idColumn, String value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?",
                Integer.class,
                value
        );

        return count != null && count > 0;
    }

    private Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
    }
}

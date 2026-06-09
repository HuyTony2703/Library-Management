package com.library.backend.service.admin;

import com.library.backend.dto.RuleCreateRequest;
import com.library.backend.dto.RuleDetailResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.service.ActivityLogService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class AdminRuleService {

    private final JdbcTemplate jdbcTemplate;
    private final ActivityLogService activityLogService;

    public AdminRuleService(
            JdbcTemplate jdbcTemplate,
            ActivityLogService activityLogService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.activityLogService = activityLogService;
    }

    public RuleDetailResponse getCurrent() {
        String maPhienBan = jdbcTemplate.query(
                """
                SELECT MaPhienBan
                FROM PHIENBANQUYDINH
                WHERE TrangThai = N'Đang áp dụng'
                ORDER BY NgayApDung DESC
                """,
                rs -> rs.next() ? rs.getString("MaPhienBan") : null
        );

        if (maPhienBan == null) {
            throw new ResourceNotFoundException("Chưa có phiên bản quy định đang áp dụng");
        }

        return getById(maPhienBan);
    }

    public List<RuleDetailResponse> getHistory() {
        String sql = """
                SELECT MaPhienBan
                FROM PHIENBANQUYDINH
                ORDER BY NgayApDung DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> getById(rs.getString("MaPhienBan")));
    }

    public RuleDetailResponse getById(String maPhienBan) {
        String sql = """
                SELECT
                    MaPhienBan,
                    TenPhienBan,
                    NgayApDung,
                    MaNhanVienThayDoi,
                    GhiChu,
                    TrangThai
                FROM PHIENBANQUYDINH
                WHERE MaPhienBan = ?
                """;

        List<RuleDetailResponse> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Timestamp ngayApDung = rs.getTimestamp("NgayApDung");

                    return new RuleDetailResponse(
                            rs.getString("MaPhienBan"),
                            rs.getString("TenPhienBan"),
                            ngayApDung == null ? null : ngayApDung.toLocalDateTime(),
                            rs.getString("MaNhanVienThayDoi"),
                            rs.getString("GhiChu"),
                            rs.getString("TrangThai"),
                            getSystemParameter(maPhienBan),
                            getMembershipPriceRules(maPhienBan),
                            getPackageBorrowRules(maPhienBan),
                            getCategoryBorrowRules(maPhienBan)
                    );
                },
                maPhienBan
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy phiên bản quy định: " + maPhienBan);
        }

        return result.get(0);
    }

    @Transactional
    public RuleDetailResponse create(RuleCreateRequest request) {
        validateCreateRequest(request);

        if (exists("PHIENBANQUYDINH", "MaPhienBan", request.getMaPhienBan())) {
            throw new BusinessException("Mã phiên bản quy định đã tồn tại");
        }

        if (!exists("NHANVIEN", "MaNhanVien", request.getMaNhanVienThayDoi())) {
            throw new ResourceNotFoundException("Nhân viên thay đổi không tồn tại");
        }

        jdbcTemplate.update(
                """
                INSERT INTO PHIENBANQUYDINH
                (
                    MaPhienBan,
                    TenPhienBan,
                    NgayApDung,
                    MaNhanVienThayDoi,
                    GhiChu,
                    TrangThai
                )
                VALUES (?, ?, SYSDATETIME(), ?, ?, N'Dự thảo')
                """,
                request.getMaPhienBan(),
                request.getTenPhienBan(),
                request.getMaNhanVienThayDoi(),
                request.getGhiChu()
        );

        insertSystemParameter(request);
        insertMembershipPriceRules(request);
        insertPackageBorrowRules(request);
        insertCategoryBorrowRules(request);

        activityLogService.logSafe(
                "Tạo phiên bản quy định",
                "PHIENBANQUYDINH",
                request.getMaPhienBan(),
                "Admin tạo phiên bản quy định " + request.getMaPhienBan()
        );

        return getById(request.getMaPhienBan());
    }

    @Transactional
    public RuleDetailResponse activate(String maPhienBan) {
        RuleDetailResponse target = getById(maPhienBan);

        if (target.getThamSo() == null) {
            throw new BusinessException("Phiên bản quy định chưa có tham số hệ thống");
        }

        if (target.getQuyDinhGoi() == null || target.getQuyDinhGoi().isEmpty()) {
            throw new BusinessException("Phiên bản quy định chưa có quy định gói");
        }

        if (target.getQuyDinhMuonTheoTheLoai() == null || target.getQuyDinhMuonTheoTheLoai().isEmpty()) {
            throw new BusinessException("Phiên bản quy định chưa có quy định mượn theo thể loại");
        }

        jdbcTemplate.update(
                """
                UPDATE PHIENBANQUYDINH
                SET TrangThai = N'Ngừng áp dụng'
                WHERE TrangThai = N'Đang áp dụng'
                """
        );

        jdbcTemplate.update(
                """
                UPDATE PHIENBANQUYDINH
                SET TrangThai = N'Đang áp dụng',
                    NgayApDung = SYSDATETIME()
                WHERE MaPhienBan = ?
                """,
                maPhienBan
        );

        activityLogService.logSafe(
                "Áp dụng phiên bản quy định",
                "PHIENBANQUYDINH",
                maPhienBan,
                "Admin áp dụng phiên bản quy định " + maPhienBan
        );

        return getById(maPhienBan);
    }

    private RuleDetailResponse.SystemParameterResponse getSystemParameter(String maPhienBan) {
        String sql = """
                SELECT
                    MaThamSo,
                    TuoiToiThieu,
                    TuoiToiDa,
                    ThoiHanTheTheoThang,
                    KhoangCachNamXuatBan,
                    SoNgayNhacTruocHan,
                    SoNgayGiuDatTruoc,
                    MucPhatTreMoiNgay
                FROM THAMSOQUYDINH
                WHERE MaPhienBan = ?
                """;

        List<RuleDetailResponse.SystemParameterResponse> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new RuleDetailResponse.SystemParameterResponse(
                        rs.getString("MaThamSo"),
                        rs.getInt("TuoiToiThieu"),
                        rs.getInt("TuoiToiDa"),
                        rs.getInt("ThoiHanTheTheoThang"),
                        rs.getInt("KhoangCachNamXuatBan"),
                        rs.getInt("SoNgayNhacTruocHan"),
                        rs.getInt("SoNgayGiuDatTruoc"),
                        rs.getBigDecimal("MucPhatTreMoiNgay")
                ),
                maPhienBan
        );

        return result.isEmpty() ? null : result.get(0);
    }

    private List<RuleDetailResponse.MembershipPriceRuleResponse> getMembershipPriceRules(String maPhienBan) {
        String sql = """
                SELECT
                    MaGiaGoi,
                    MaGoiThanhVien,
                    MaNhomDocGia,
                    GiaTien,
                    ThoiHanGoiTheoNgay
                FROM GIAGOI_THEONHOM
                WHERE MaPhienBan = ?
                ORDER BY MaGoiThanhVien, MaNhomDocGia
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new RuleDetailResponse.MembershipPriceRuleResponse(
                        rs.getString("MaGiaGoi"),
                        rs.getString("MaGoiThanhVien"),
                        rs.getString("MaNhomDocGia"),
                        rs.getBigDecimal("GiaTien"),
                        rs.getInt("ThoiHanGoiTheoNgay")
                ),
                maPhienBan
        );
    }

    private List<RuleDetailResponse.PackageBorrowRuleResponse> getPackageBorrowRules(String maPhienBan) {
        String sql = """
                SELECT
                    MaQuyDinhGoi,
                    MaGoiThanhVien,
                    SoSachMuonToiDa,
                    SoLanGiaHanToiDa
                FROM QUYDINHGOI
                WHERE MaPhienBan = ?
                ORDER BY MaGoiThanhVien
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new RuleDetailResponse.PackageBorrowRuleResponse(
                        rs.getString("MaQuyDinhGoi"),
                        rs.getString("MaGoiThanhVien"),
                        rs.getInt("SoSachMuonToiDa"),
                        rs.getInt("SoLanGiaHanToiDa")
                ),
                maPhienBan
        );
    }

    private List<RuleDetailResponse.CategoryBorrowRuleResponse> getCategoryBorrowRules(String maPhienBan) {
        String sql = """
                SELECT
                    MaQuyDinhMuon,
                    MaGoiThanhVien,
                    MaTheLoai,
                    SoNgayMuon,
                    SoNgayGiaHanMoiLan
                FROM QUYDINHMUON_THELOAI
                WHERE MaPhienBan = ?
                ORDER BY MaGoiThanhVien, MaTheLoai
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new RuleDetailResponse.CategoryBorrowRuleResponse(
                        rs.getString("MaQuyDinhMuon"),
                        rs.getString("MaGoiThanhVien"),
                        rs.getString("MaTheLoai"),
                        rs.getInt("SoNgayMuon"),
                        rs.getInt("SoNgayGiaHanMoiLan")
                ),
                maPhienBan
        );
    }

    private void insertSystemParameter(RuleCreateRequest request) {
        RuleCreateRequest.SystemParameterRequest ts = request.getThamSo();

        jdbcTemplate.update(
                """
                INSERT INTO THAMSOQUYDINH
                (
                    MaThamSo,
                    MaPhienBan,
                    TuoiToiThieu,
                    TuoiToiDa,
                    ThoiHanTheTheoThang,
                    KhoangCachNamXuatBan,
                    SoNgayNhacTruocHan,
                    SoNgayGiuDatTruoc,
                    MucPhatTreMoiNgay
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                ts.getMaThamSo(),
                request.getMaPhienBan(),
                ts.getTuoiToiThieu(),
                ts.getTuoiToiDa(),
                ts.getThoiHanTheTheoThang(),
                ts.getKhoangCachNamXuatBan(),
                ts.getSoNgayNhacTruocHan(),
                ts.getSoNgayGiuDatTruoc(),
                ts.getMucPhatTreMoiNgay()
        );
    }

    private void insertMembershipPriceRules(RuleCreateRequest request) {
        if (request.getGiaGoiTheoNhom() == null) {
            return;
        }

        for (RuleCreateRequest.MembershipPriceRuleRequest item : request.getGiaGoiTheoNhom()) {
            validatePackageExists(item.getMaGoiThanhVien());
            validateReaderGroupExists(item.getMaNhomDocGia());

            jdbcTemplate.update(
                    """
                    INSERT INTO GIAGOI_THEONHOM
                    (
                        MaGiaGoi,
                        MaPhienBan,
                        MaGoiThanhVien,
                        MaNhomDocGia,
                        GiaTien,
                        ThoiHanGoiTheoNgay
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    item.getMaGiaGoi(),
                    request.getMaPhienBan(),
                    item.getMaGoiThanhVien(),
                    item.getMaNhomDocGia(),
                    item.getGiaTien(),
                    item.getThoiHanGoiTheoNgay()
            );
        }
    }

    private void insertPackageBorrowRules(RuleCreateRequest request) {
        if (request.getQuyDinhGoi() == null) {
            return;
        }

        for (RuleCreateRequest.PackageBorrowRuleRequest item : request.getQuyDinhGoi()) {
            validatePackageExists(item.getMaGoiThanhVien());

            jdbcTemplate.update(
                    """
                    INSERT INTO QUYDINHGOI
                    (
                        MaQuyDinhGoi,
                        MaPhienBan,
                        MaGoiThanhVien,
                        SoSachMuonToiDa,
                        SoLanGiaHanToiDa
                    )
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    item.getMaQuyDinhGoi(),
                    request.getMaPhienBan(),
                    item.getMaGoiThanhVien(),
                    item.getSoSachMuonToiDa(),
                    item.getSoLanGiaHanToiDa()
            );
        }
    }

    private void insertCategoryBorrowRules(RuleCreateRequest request) {
        if (request.getQuyDinhMuonTheoTheLoai() == null) {
            return;
        }

        for (RuleCreateRequest.CategoryBorrowRuleRequest item : request.getQuyDinhMuonTheoTheLoai()) {
            validatePackageExists(item.getMaGoiThanhVien());
            validateCategoryExists(item.getMaTheLoai());

            jdbcTemplate.update(
                    """
                    INSERT INTO QUYDINHMUON_THELOAI
                    (
                        MaQuyDinhMuon,
                        MaPhienBan,
                        MaGoiThanhVien,
                        MaTheLoai,
                        SoNgayMuon,
                        SoNgayGiaHanMoiLan
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    item.getMaQuyDinhMuon(),
                    request.getMaPhienBan(),
                    item.getMaGoiThanhVien(),
                    item.getMaTheLoai(),
                    item.getSoNgayMuon(),
                    item.getSoNgayGiaHanMoiLan()
            );
        }
    }

    private void validateCreateRequest(RuleCreateRequest request) {
        if (isBlank(request.getMaPhienBan())) {
            throw new BusinessException("Mã phiên bản không được để trống");
        }

        if (isBlank(request.getTenPhienBan())) {
            throw new BusinessException("Tên phiên bản không được để trống");
        }

        if (isBlank(request.getMaNhanVienThayDoi())) {
            throw new BusinessException("Mã nhân viên thay đổi không được để trống");
        }

        if (request.getThamSo() == null) {
            throw new BusinessException("Thiếu tham số hệ thống");
        }

        RuleCreateRequest.SystemParameterRequest ts = request.getThamSo();

        if (isBlank(ts.getMaThamSo())) {
            throw new BusinessException("Mã tham số không được để trống");
        }

        if (ts.getTuoiToiThieu() == null || ts.getTuoiToiDa() == null
                || ts.getTuoiToiThieu() <= 0
                || ts.getTuoiToiDa() < ts.getTuoiToiThieu()) {
            throw new BusinessException("Tuổi tối thiểu/tối đa không hợp lệ");
        }

        if (ts.getThoiHanTheTheoThang() == null || ts.getThoiHanTheTheoThang() <= 0) {
            throw new BusinessException("Thời hạn thẻ phải lớn hơn 0");
        }

        if (ts.getKhoangCachNamXuatBan() == null || ts.getKhoangCachNamXuatBan() < 0) {
            throw new BusinessException("Khoảng cách năm xuất bản không hợp lệ");
        }

        if (ts.getSoNgayNhacTruocHan() == null || ts.getSoNgayNhacTruocHan() < 0) {
            throw new BusinessException("Số ngày nhắc trước hạn không hợp lệ");
        }

        if (ts.getSoNgayGiuDatTruoc() == null || ts.getSoNgayGiuDatTruoc() < 0) {
            throw new BusinessException("Số ngày giữ đặt trước không hợp lệ");
        }

        if (ts.getMucPhatTreMoiNgay() == null
                || ts.getMucPhatTreMoiNgay().signum() < 0) {
            throw new BusinessException("Mức phạt trễ mỗi ngày không hợp lệ");
        }

        if (request.getQuyDinhGoi() == null || request.getQuyDinhGoi().isEmpty()) {
            throw new BusinessException("Phải có ít nhất một quy định gói");
        }

        if (request.getQuyDinhMuonTheoTheLoai() == null || request.getQuyDinhMuonTheoTheLoai().isEmpty()) {
            throw new BusinessException("Phải có ít nhất một quy định mượn theo thể loại");
        }
    }

    private void validatePackageExists(String maGoiThanhVien) {
        if (!exists("GOITHANHVIEN", "MaGoiThanhVien", maGoiThanhVien)) {
            throw new ResourceNotFoundException("Gói thành viên không tồn tại: " + maGoiThanhVien);
        }
    }

    private void validateReaderGroupExists(String maNhomDocGia) {
        if (!exists("NHOMDOCGIA", "MaNhomDocGia", maNhomDocGia)) {
            throw new ResourceNotFoundException("Nhóm độc giả không tồn tại: " + maNhomDocGia);
        }
    }

    private void validateCategoryExists(String maTheLoai) {
        if (!exists("THELOAI", "MaTheLoai", maTheLoai)) {
            throw new ResourceNotFoundException("Thể loại không tồn tại: " + maTheLoai);
        }
    }

    private boolean exists(String tableName, String columnName, String value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?",
                Integer.class,
                value
        );

        return count != null && count > 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

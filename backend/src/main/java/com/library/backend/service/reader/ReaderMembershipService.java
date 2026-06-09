package com.library.backend.service.reader;

import com.library.backend.dto.reader.MembershipPlanResponse;
import com.library.backend.dto.reader.MembershipPurchaseRequest;
import com.library.backend.dto.reader.MembershipPurchaseResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReaderMembershipService {

    private static final String ACTIVE_READER = "Hoạt động";
    private static final String ACTIVE_POLICY = "Đang áp dụng";
    private static final String ACTIVE_PLAN = "Hoạt động";
    private static final String MEMBERSHIP_IN_USE = "Đang sử dụng";
    private static final String MEMBERSHIP_EXPIRED = "Hết hạn";
    private static final String MEMBERSHIP_UPGRADED = "Đã nâng cấp";
    private static final String PAYMENT_SUCCESS = "Thành công";
    private static final String PAYMENT_TYPE_MEMBERSHIP = "Thu tiền mua gói";
    private static final String TB_MUA_GOI_TC = "TB_MUA_GOI_TC";

    private final JdbcTemplate jdbcTemplate;
    private final ReaderNotificationService readerNotificationService;

    public ReaderMembershipService(
            JdbcTemplate jdbcTemplate,
            ReaderNotificationService readerNotificationService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.readerNotificationService = readerNotificationService;
    }

    public MembershipPurchaseResponse getCurrentMembership(String maDocGia) {
        String sql = """
                SELECT TOP 1
                    lsg.MaLichSuGoi,
                    lsg.MaDocGia,
                    lsg.MaGoiThanhVien,
                    g.TenGoi,
                    lsg.MaPhieuThu,
                    pt.MaPhuongThuc,
                    ISNULL(pt.SoTienThu, 0) AS SoTienThu,
                    lsg.NgayBatDau,
                    lsg.NgayKetThuc,
                    lsg.TrangThai,
                    lsg.GhiChu
                FROM LICHSUGOITHANHVIEN lsg
                INNER JOIN GOITHANHVIEN g
                    ON lsg.MaGoiThanhVien = g.MaGoiThanhVien
                LEFT JOIN PHIEUTHU pt
                    ON lsg.MaPhieuThu = pt.MaPhieuThu
                WHERE lsg.MaDocGia = ?
                  AND lsg.TrangThai = ?
                  AND CAST(GETDATE() AS DATE) BETWEEN lsg.NgayBatDau AND lsg.NgayKetThuc
                ORDER BY lsg.NgayKetThuc DESC
                """;

        List<MembershipPurchaseResponse> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapMembership(rs),
                maDocGia,
                MEMBERSHIP_IN_USE
        );

        return result.isEmpty() ? null : result.get(0);
    }

    public List<MembershipPlanResponse> getAvailablePlans(String maDocGia) {
        ReaderInfo reader = getReaderInfo(maDocGia);
        String maPhienBan = getCurrentPolicyVersion();
        MembershipPurchaseResponse current = getCurrentMembership(maDocGia);
        String currentPlan = current == null ? null : current.getMaGoiThanhVien();

        String sql = """
                SELECT
                    g.MaGoiThanhVien,
                    g.TenGoi,
                    g.MoTa,
                    g.TrangThai,
                    gg.GiaTien,
                    gg.ThoiHanGoiTheoNgay
                FROM GOITHANHVIEN g
                INNER JOIN GIAGOI_THEONHOM gg
                    ON g.MaGoiThanhVien = gg.MaGoiThanhVien
                WHERE gg.MaPhienBan = ?
                  AND gg.MaNhomDocGia = ?
                  AND g.TrangThai = ?
                ORDER BY gg.GiaTien ASC, g.TenGoi ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new MembershipPlanResponse(
                        rs.getString("MaGoiThanhVien"),
                        rs.getString("TenGoi"),
                        rs.getString("MoTa"),
                        rs.getString("TrangThai"),
                        rs.getBigDecimal("GiaTien"),
                        rs.getInt("ThoiHanGoiTheoNgay"),
                        rs.getString("MaGoiThanhVien").equals(currentPlan)
                ),
                maPhienBan,
                reader.maNhomDocGia(),
                ACTIVE_PLAN
        );
    }

    public List<MembershipPurchaseResponse> getMembershipHistory(String maDocGia) {
        String sql = """
                SELECT
                    lsg.MaLichSuGoi,
                    lsg.MaDocGia,
                    lsg.MaGoiThanhVien,
                    g.TenGoi,
                    lsg.MaPhieuThu,
                    pt.MaPhuongThuc,
                    ISNULL(pt.SoTienThu, 0) AS SoTienThu,
                    lsg.NgayBatDau,
                    lsg.NgayKetThuc,
                    lsg.TrangThai,
                    lsg.GhiChu
                FROM LICHSUGOITHANHVIEN lsg
                INNER JOIN GOITHANHVIEN g
                    ON lsg.MaGoiThanhVien = g.MaGoiThanhVien
                LEFT JOIN PHIEUTHU pt
                    ON lsg.MaPhieuThu = pt.MaPhieuThu
                WHERE lsg.MaDocGia = ?
                ORDER BY lsg.NgayBatDau DESC, lsg.NgayKetThuc DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapMembership(rs), maDocGia);
    }

    @Transactional
    public MembershipPurchaseResponse purchaseMembership(
            String maDocGia,
            String maTaiKhoan,
            MembershipPurchaseRequest request
    ) {
        validatePurchaseRequest(request);

        ReaderInfo reader = getReaderInfo(maDocGia);

        if (!ACTIVE_READER.equals(reader.trangThai())) {
            throw new BusinessException("Tài khoản độc giả không ở trạng thái hoạt động");
        }

        if (!existsPaymentMethod(request.getMaPhuongThuc())) {
            throw new ResourceNotFoundException("Phương thức thanh toán không tồn tại");
        }

        String maPhienBan = getCurrentPolicyVersion();
        PlanPriceInfo plan = getPlanPrice(
                maPhienBan,
                reader.maNhomDocGia(),
                request.getMaGoiThanhVien()
        );

        String maPhieuThu = generateId("PTG");
        String maLichSuGoi = generateId("LSG");
        LocalDate ngayBatDau = LocalDate.now();
        LocalDate ngayKetThuc = ngayBatDau.plusDays(plan.thoiHanGoiTheoNgay());
        String ghiChu = cleanText(request.getGhiChu());

        updateOldMemberships(maDocGia, request.getMaGoiThanhVien());

        jdbcTemplate.update(
                """
                INSERT INTO PHIEUTHU
                (
                    MaPhieuThu,
                    MaDocGia,
                    MaNhanVienThu,
                    MaPhuongThuc,
                    LoaiThu,
                    SoTienThu,
                    NgayThu,
                    MaGiaoDichNgoai,
                    TrangThai,
                    GhiChu
                )
                VALUES (?, ?, NULL, ?, ?, ?, SYSDATETIME(), ?, ?, ?)
                """,
                maPhieuThu,
                maDocGia,
                request.getMaPhuongThuc(),
                PAYMENT_TYPE_MEMBERSHIP,
                plan.giaTien(),
                "MEM-" + maPhieuThu,
                PAYMENT_SUCCESS,
                trimToMaxLength(buildPaymentNote(ghiChu, plan.tenGoi()), 255)
        );

        jdbcTemplate.update(
                """
                INSERT INTO LICHSUGOITHANHVIEN
                (
                    MaLichSuGoi,
                    MaDocGia,
                    MaGoiThanhVien,
                    MaPhieuThu,
                    NgayBatDau,
                    NgayKetThuc,
                    TrangThai,
                    GhiChu
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                maLichSuGoi,
                maDocGia,
                request.getMaGoiThanhVien(),
                maPhieuThu,
                Date.valueOf(ngayBatDau),
                Date.valueOf(ngayKetThuc),
                MEMBERSHIP_IN_USE,
                ghiChu
        );

        readerNotificationService.createInAppNotification(
                maTaiKhoan,
                TB_MUA_GOI_TC,
                "Mua gói thành công",
                "Bạn đã mua gói " + plan.tenGoi()
                        + " thành công. Gói có hiệu lực đến ngày "
                        + ngayKetThuc + ".",
                false
        );

        return getMembershipById(maDocGia, maLichSuGoi);
    }

    private MembershipPurchaseResponse getMembershipById(String maDocGia, String maLichSuGoi) {
        String sql = """
                SELECT
                    lsg.MaLichSuGoi,
                    lsg.MaDocGia,
                    lsg.MaGoiThanhVien,
                    g.TenGoi,
                    lsg.MaPhieuThu,
                    pt.MaPhuongThuc,
                    ISNULL(pt.SoTienThu, 0) AS SoTienThu,
                    lsg.NgayBatDau,
                    lsg.NgayKetThuc,
                    lsg.TrangThai,
                    lsg.GhiChu
                FROM LICHSUGOITHANHVIEN lsg
                INNER JOIN GOITHANHVIEN g
                    ON lsg.MaGoiThanhVien = g.MaGoiThanhVien
                LEFT JOIN PHIEUTHU pt
                    ON lsg.MaPhieuThu = pt.MaPhieuThu
                WHERE lsg.MaDocGia = ?
                  AND lsg.MaLichSuGoi = ?
                """;

        List<MembershipPurchaseResponse> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapMembership(rs),
                maDocGia,
                maLichSuGoi
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy lịch sử gói vừa mua");
        }

        return result.get(0);
    }

    private ReaderInfo getReaderInfo(String maDocGia) {
        List<ReaderInfo> result = jdbcTemplate.query(
                """
                SELECT MaDocGia, MaNhomDocGia, TrangThai
                FROM DOCGIA
                WHERE MaDocGia = ?
                """,
                (rs, rowNum) -> new ReaderInfo(
                        rs.getString("MaDocGia"),
                        rs.getString("MaNhomDocGia"),
                        rs.getString("TrangThai")
                ),
                maDocGia
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy độc giả");
        }

        return result.get(0);
    }

    private String getCurrentPolicyVersion() {
        List<String> result = jdbcTemplate.query(
                """
                SELECT TOP 1 MaPhienBan
                FROM PHIENBANQUYDINH
                WHERE TrangThai = ?
                ORDER BY NgayApDung DESC
                """,
                (rs, rowNum) -> rs.getString("MaPhienBan"),
                ACTIVE_POLICY
        );

        if (result.isEmpty()) {
            throw new BusinessException("Chưa có phiên bản quy định đang áp dụng");
        }

        return result.get(0);
    }

    private PlanPriceInfo getPlanPrice(
            String maPhienBan,
            String maNhomDocGia,
            String maGoiThanhVien
    ) {
        List<PlanPriceInfo> result = jdbcTemplate.query(
                """
                SELECT TOP 1
                    g.MaGoiThanhVien,
                    g.TenGoi,
                    gg.GiaTien,
                    gg.ThoiHanGoiTheoNgay
                FROM GOITHANHVIEN g
                INNER JOIN GIAGOI_THEONHOM gg
                    ON g.MaGoiThanhVien = gg.MaGoiThanhVien
                WHERE gg.MaPhienBan = ?
                  AND gg.MaNhomDocGia = ?
                  AND gg.MaGoiThanhVien = ?
                  AND g.TrangThai = ?
                ORDER BY gg.GiaTien ASC
                """,
                (rs, rowNum) -> new PlanPriceInfo(
                        rs.getString("MaGoiThanhVien"),
                        rs.getString("TenGoi"),
                        rs.getBigDecimal("GiaTien"),
                        rs.getInt("ThoiHanGoiTheoNgay")
                ),
                maPhienBan,
                maNhomDocGia,
                maGoiThanhVien,
                ACTIVE_PLAN
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy giá gói phù hợp với nhóm độc giả hiện tại");
        }

        return result.get(0);
    }

    private boolean existsPaymentMethod(String maPhuongThuc) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM PHUONGTHUCTHANHTOAN
                WHERE MaPhuongThuc = ?
                """,
                Integer.class,
                maPhuongThuc
        );

        return count != null && count > 0;
    }

    private void updateOldMemberships(String maDocGia, String maGoiThanhVienMoi) {
        jdbcTemplate.update(
                """
                UPDATE LICHSUGOITHANHVIEN
                SET TrangThai = CASE
                        WHEN MaGoiThanhVien = ? THEN ?
                        ELSE ?
                    END,
                    GhiChu = LEFT(
                        COALESCE(GhiChu, N'') +
                        CASE WHEN GhiChu IS NULL OR GhiChu = N'' THEN N'' ELSE N' | ' END +
                        N'Cập nhật khi mua gói mới',
                        255
                    )
                WHERE MaDocGia = ?
                  AND TrangThai = ?
                  AND CAST(GETDATE() AS DATE) BETWEEN NgayBatDau AND NgayKetThuc
                """,
                maGoiThanhVienMoi,
                MEMBERSHIP_EXPIRED,
                MEMBERSHIP_UPGRADED,
                maDocGia,
                MEMBERSHIP_IN_USE
        );
    }

    private void validatePurchaseRequest(MembershipPurchaseRequest request) {
        if (request == null) {
            throw new BusinessException("Dữ liệu mua gói không được để trống");
        }

        if (isBlank(request.getMaGoiThanhVien())) {
            throw new BusinessException("Mã gói thành viên không được để trống");
        }

        if (isBlank(request.getMaPhuongThuc())) {
            throw new BusinessException("Phương thức thanh toán không được để trống");
        }
    }

    private MembershipPurchaseResponse mapMembership(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new MembershipPurchaseResponse(
                rs.getString("MaLichSuGoi"),
                rs.getString("MaDocGia"),
                rs.getString("MaGoiThanhVien"),
                rs.getString("TenGoi"),
                rs.getString("MaPhieuThu"),
                rs.getString("MaPhuongThuc"),
                rs.getBigDecimal("SoTienThu"),
                toLocalDate(rs.getDate("NgayBatDau")),
                toLocalDate(rs.getDate("NgayKetThuc")),
                rs.getString("TrangThai"),
                rs.getString("GhiChu")
        );
    }

    private String buildPaymentNote(String ghiChu, String tenGoi) {
        String prefix = "Mua gói " + tenGoi;

        if (isBlank(ghiChu)) {
            return prefix;
        }

        return prefix + " - " + ghiChu;
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimToMaxLength(trimmed, 255);
    }

    private String trimToMaxLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private String generateId(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return prefix + "_" + timestamp + "_" + random;
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ReaderInfo(String maDocGia, String maNhomDocGia, String trangThai) {
    }

    private record PlanPriceInfo(
            String maGoiThanhVien,
            String tenGoi,
            BigDecimal giaTien,
            int thoiHanGoiTheoNgay
    ) {
    }
}

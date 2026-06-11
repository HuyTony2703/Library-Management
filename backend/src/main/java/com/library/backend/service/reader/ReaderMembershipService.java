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
    private static final String PLAN_THUONG = "GOI_THUONG";
    private static final String PLAN_VIP = "GOI_VIP";
    private static final String PLAN_PREMIUM = "GOI_PREMIUM";
    private static final BigDecimal DEFAULT_PREMIUM_PRICE = BigDecimal.valueOf(100000);
    private static final int DEFAULT_PLAN_DURATION_DAYS = 180;

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
        ensurePremiumPlanExists();

        ReaderInfo reader = getReaderInfo(maDocGia);
        String maPhienBan = getCurrentPolicyVersion();
        ensurePremiumPlanPriceExists(maPhienBan, reader.maNhomDocGia());
        MembershipPurchaseResponse current = getCurrentMembership(maDocGia);
        String currentPlan = current == null ? null : current.getMaGoiThanhVien();

        String sql = """
                SELECT
                    g.MaGoiThanhVien,
                    g.TenGoi,
                    g.MoTa,
                    g.TrangThai,
                    COALESCE(groupPrice.GiaTien, fallbackPrice.GiaTien,
                        CASE WHEN g.MaGoiThanhVien = 'GOI_PREMIUM' THEN 100000 ELSE 0 END
                    ) AS GiaTien,
                    COALESCE(groupPrice.ThoiHanGoiTheoNgay, fallbackPrice.ThoiHanGoiTheoNgay, 180) AS ThoiHanGoiTheoNgay
                FROM GOITHANHVIEN g
                OUTER APPLY (
                    SELECT TOP 1 gg.GiaTien, gg.ThoiHanGoiTheoNgay
                    FROM GIAGOI_THEONHOM gg
                    WHERE gg.MaPhienBan = ?
                      AND gg.MaNhomDocGia = ?
                      AND gg.MaGoiThanhVien = g.MaGoiThanhVien
                    ORDER BY gg.GiaTien ASC
                ) groupPrice
                OUTER APPLY (
                    SELECT TOP 1 gg.GiaTien, gg.ThoiHanGoiTheoNgay
                    FROM GIAGOI_THEONHOM gg
                    WHERE gg.MaPhienBan = ?
                      AND gg.MaGoiThanhVien = g.MaGoiThanhVien
                    ORDER BY gg.GiaTien ASC
                ) fallbackPrice
                WHERE g.TrangThai = ?
                ORDER BY
                    CASE g.MaGoiThanhVien
                        WHEN 'GOI_THUONG' THEN 1
                        WHEN 'GOI_VIP' THEN 2
                        WHEN 'GOI_PREMIUM' THEN 3
                        ELSE 9
                    END,
                    g.TenGoi ASC
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
                maPhienBan,
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

        if (PLAN_PREMIUM.equals(request.getMaGoiThanhVien())) {
            ensurePremiumPlanExists();
        }

        ReaderInfo reader = getReaderInfo(maDocGia);

        if (!ACTIVE_READER.equals(reader.trangThai())) {
            throw new BusinessException("Tài khoản độc giả không ở trạng thái hoạt động");
        }

        MembershipPurchaseResponse current = getCurrentMembership(maDocGia);
        validatePackageUpgrade(current == null ? PLAN_THUONG : current.getMaGoiThanhVien(), request.getMaGoiThanhVien());

        if (!existsPaymentMethod(request.getMaPhuongThuc())) {
            throw new ResourceNotFoundException("Phương thức thanh toán không tồn tại");
        }

        String maPhienBan = getCurrentPolicyVersion();
        if (PLAN_PREMIUM.equals(request.getMaGoiThanhVien())) {
            ensurePremiumPlanPriceExists(maPhienBan, reader.maNhomDocGia());
        }
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
            PlanPriceInfo fallback = getFallbackPlanPrice(maPhienBan, maGoiThanhVien);

            if (fallback != null) {
                return fallback;
            }

            throw new ResourceNotFoundException("Không tìm thấy giá gói phù hợp với nhóm độc giả hiện tại");
        }

        return result.get(0);
    }

    private PlanPriceInfo getFallbackPlanPrice(String maPhienBan, String maGoiThanhVien) {
        if (PLAN_PREMIUM.equals(maGoiThanhVien)) {
            ensurePremiumPlanExists();
        }

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
                maGoiThanhVien,
                ACTIVE_PLAN
        );

        if (!result.isEmpty()) {
            return result.get(0);
        }

        if (!PLAN_PREMIUM.equals(maGoiThanhVien)) {
            return null;
        }

        return new PlanPriceInfo(PLAN_PREMIUM, "Premium", DEFAULT_PREMIUM_PRICE, DEFAULT_PLAN_DURATION_DAYS);
    }

    private void ensurePremiumPlanExists() {
        Integer exists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM GOITHANHVIEN
                WHERE MaGoiThanhVien = ?
                """,
                Integer.class,
                PLAN_PREMIUM
        );

        if (exists == null || exists == 0) {
            jdbcTemplate.update(
                    """
                    INSERT INTO GOITHANHVIEN(MaGoiThanhVien, TenGoi, MoTa, TrangThai)
                    VALUES (?, ?, ?, ?)
                    """,
                    PLAN_PREMIUM,
                    "Premium",
                    "Gói độc giả cao cấp",
                    ACTIVE_PLAN
            );
            return;
        }

        jdbcTemplate.update(
                """
                UPDATE GOITHANHVIEN
                SET TrangThai = ?
                WHERE MaGoiThanhVien = ?
                """,
                ACTIVE_PLAN,
                PLAN_PREMIUM
        );
    }

    private void ensurePremiumPlanPriceExists(String maPhienBan, String maNhomDocGia) {
        Integer exists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM GIAGOI_THEONHOM
                WHERE MaPhienBan = ?
                  AND MaNhomDocGia = ?
                  AND MaGoiThanhVien = ?
                """,
                Integer.class,
                maPhienBan,
                maNhomDocGia,
                PLAN_PREMIUM
        );

        if (exists != null && exists > 0) {
            return;
        }

        jdbcTemplate.update(
                """
                INSERT INTO GIAGOI_THEONHOM(
                    MaGiaGoi,
                    MaPhienBan,
                    MaGoiThanhVien,
                    MaNhomDocGia,
                    GiaTien,
                    ThoiHanGoiTheoNgay
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                buildPremiumPriceId(maPhienBan, maNhomDocGia),
                maPhienBan,
                PLAN_PREMIUM,
                maNhomDocGia,
                DEFAULT_PREMIUM_PRICE,
                DEFAULT_PLAN_DURATION_DAYS
        );
    }

    private String buildPremiumPriceId(String maPhienBan, String maNhomDocGia) {
        String key = maPhienBan + "|" + maNhomDocGia + "|" + PLAN_PREMIUM;
        return "GG_PR_" + Integer.toUnsignedString(key.hashCode(), 36).toUpperCase();
    }

    private void validatePackageUpgrade(String currentPlan, String targetPlan) {
        if (PLAN_THUONG.equals(targetPlan)) {
            throw new BusinessException("Gói Thường là gói mặc định và không thể mua từ giao diện độc giả");
        }

        int currentLevel = packageLevel(currentPlan);
        int targetLevel = packageLevel(targetPlan);

        if (targetLevel <= currentLevel) {
            throw new BusinessException("Chỉ có thể nâng cấp lên gói cao hơn gói hiện tại");
        }
    }

    private int packageLevel(String maGoiThanhVien) {
        if (PLAN_PREMIUM.equals(maGoiThanhVien)) {
            return 3;
        }

        if (PLAN_VIP.equals(maGoiThanhVien)) {
            return 2;
        }

        return 1;
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

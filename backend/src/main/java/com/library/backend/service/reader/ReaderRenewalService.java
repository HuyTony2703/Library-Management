package com.library.backend.service.reader;

import com.library.backend.dto.reader.ReaderCurrentLoanResponse;
import com.library.backend.dto.reader.ReaderRenewalResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.security.AuthUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReaderRenewalService {

    private static final String TRANG_THAI_DANG_MUON = "Đang mượn";
    private static final String TRANG_THAI_THANH_CONG = "Thành công";
    private static final String TRANG_THAI_DA_THANH_TOAN = "Đã thanh toán";
    private static final String LOAI_THONG_BAO_GIA_HAN = "TB_GIA_HAN_TC";

    private final JdbcTemplate jdbcTemplate;

    public ReaderRenewalService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReaderCurrentLoanResponse> getCurrentLoans(AuthUser user) {
        String maDocGia = getRequiredReaderId(user);
        boolean hasDebt = hasUnpaidDebt(maDocGia);

        String sql = """
                SELECT
                    ctm.MaChiTietMuon,
                    pm.MaPhieuMuon,
                    ctm.MaCuonSach,
                    ds.MaDauSach,
                    ds.TenDauSach,
                    ds.AnhBia,
                    ctm.NgayMuon,
                    ctm.HanTra,
                    ctm.TrangThai,
                    cs.MaChiNhanh,
                    cn.TenChiNhanh,
                    qdm.SoNgayGiaHanMoiLan,
                    COALESCE(qdg.SoLanGiaHanToiDa, 0) AS SoLanGiaHanToiDa,
                    COALESCE((
                        SELECT MAX(lsg.LanGiaHanThu)
                        FROM LICHSUGIAHAN lsg
                        WHERE lsg.MaChiTietMuon = ctm.MaChiTietMuon
                          AND lsg.TrangThai = N'Thành công'
                    ), 0) AS SoLanDaGiaHan
                FROM PHIEUMUON pm
                INNER JOIN CHITIETPHIEUMUON ctm
                    ON pm.MaPhieuMuon = ctm.MaPhieuMuon
                INNER JOIN CUONSACH cs
                    ON ctm.MaCuonSach = cs.MaCuonSach
                INNER JOIN DAUSACH ds
                    ON cs.MaDauSach = ds.MaDauSach
                INNER JOIN CHINHANH cn
                    ON cs.MaChiNhanh = cn.MaChiNhanh
                INNER JOIN QUYDINHMUON_THELOAI qdm
                    ON ctm.MaQuyDinhMuon = qdm.MaQuyDinhMuon
                LEFT JOIN QUYDINHGOI qdg
                    ON qdg.MaPhienBan = pm.MaPhienBanQuyDinh
                   AND qdg.MaGoiThanhVien = qdm.MaGoiThanhVien
                WHERE pm.MaDocGia = ?
                  AND ctm.TrangThai = N'Đang mượn'
                ORDER BY ctm.HanTra ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    int soLanDaGiaHan = rs.getInt("SoLanDaGiaHan");
                    int soLanGiaHanToiDa = rs.getInt("SoLanGiaHanToiDa");
                    int soNgayGiaHanMoiLan = rs.getInt("SoNgayGiaHanMoiLan");

                    RenewalEligibility eligibility = getRenewalEligibility(
                            hasDebt,
                            soLanDaGiaHan,
                            soLanGiaHanToiDa,
                            soNgayGiaHanMoiLan
                    );

                    return new ReaderCurrentLoanResponse(
                            rs.getString("MaChiTietMuon"),
                            rs.getString("MaPhieuMuon"),
                            rs.getString("MaCuonSach"),
                            rs.getString("MaDauSach"),
                            rs.getString("TenDauSach"),
                            rs.getString("AnhBia"),
                            toLocalDateTime(rs.getTimestamp("NgayMuon")),
                            toLocalDateTime(rs.getTimestamp("HanTra")),
                            rs.getString("TrangThai"),
                            rs.getString("MaChiNhanh"),
                            rs.getString("TenChiNhanh"),
                            soNgayGiaHanMoiLan,
                            soLanGiaHanToiDa,
                            soLanDaGiaHan,
                            eligibility.coTheGiaHan(),
                            eligibility.lyDoKhongTheGiaHan()
                    );
                },
                maDocGia
        );
    }

    @Transactional
    public ReaderRenewalResponse renew(AuthUser user, String maChiTietMuon) {
        String maDocGia = getRequiredReaderId(user);
        LoanRenewalData loan = findLoanForRenew(maChiTietMuon);

        if (!maDocGia.equals(loan.maDocGia())) {
            throw new BusinessException("Bạn không được gia hạn sách của độc giả khác");
        }

        if (!TRANG_THAI_DANG_MUON.equals(loan.trangThai())) {
            throw new BusinessException("Chỉ có thể gia hạn sách đang mượn");
        }

        if (hasUnpaidDebt(maDocGia)) {
            throw new BusinessException("Bạn đang có nợ chưa thanh toán nên không thể gia hạn");
        }

        if (loan.soLanGiaHanToiDa() <= 0) {
            throw new BusinessException("Gói hiện tại không cho phép gia hạn");
        }

        int soLanDaGiaHan = countSuccessfulRenewals(maChiTietMuon);

        if (soLanDaGiaHan >= loan.soLanGiaHanToiDa()) {
            throw new BusinessException("Bạn đã vượt số lần gia hạn tối đa");
        }

        if (loan.soNgayGiaHanMoiLan() <= 0) {
            throw new BusinessException("Quy định hiện tại không cho cộng thêm ngày gia hạn");
        }

        LocalDateTime hanTraCu = loan.hanTra();
        LocalDateTime hanTraMoi = hanTraCu.plusDays(loan.soNgayGiaHanMoiLan());
        LocalDateTime ngayGiaHan = LocalDateTime.now();
        int lanGiaHanThu = soLanDaGiaHan + 1;
        String maGiaHan = generateId("GH");

        jdbcTemplate.update(
                """
                UPDATE CHITIETPHIEUMUON
                SET HanTra = ?
                WHERE MaChiTietMuon = ?
                """,
                Timestamp.valueOf(hanTraMoi),
                maChiTietMuon
        );

        jdbcTemplate.update(
                """
                INSERT INTO LICHSUGIAHAN
                (
                    MaGiaHan,
                    MaChiTietMuon,
                    MaDocGia,
                    NgayGiaHan,
                    HanTraCu,
                    HanTraMoi,
                    SoNgayGiaHan,
                    LanGiaHanThu,
                    TrangThai,
                    LyDoTuChoi
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, N'Thành công', NULL)
                """,
                maGiaHan,
                maChiTietMuon,
                maDocGia,
                Timestamp.valueOf(ngayGiaHan),
                Timestamp.valueOf(hanTraCu),
                Timestamp.valueOf(hanTraMoi),
                loan.soNgayGiaHanMoiLan(),
                lanGiaHanThu
        );

        createRenewalSuccessNotification(
                loan.maTaiKhoan(),
                maGiaHan,
                loan.tenDauSach(),
                hanTraCu,
                hanTraMoi
        );

        return new ReaderRenewalResponse(
                maGiaHan,
                maChiTietMuon,
                maDocGia,
                loan.maCuonSach(),
                loan.tenDauSach(),
                ngayGiaHan,
                hanTraCu,
                hanTraMoi,
                loan.soNgayGiaHanMoiLan(),
                lanGiaHanThu,
                TRANG_THAI_THANH_CONG,
                null
        );
    }

    public List<ReaderRenewalResponse> getRenewalHistory(AuthUser user) {
        String maDocGia = getRequiredReaderId(user);

        String sql = """
                SELECT
                    lsg.MaGiaHan,
                    lsg.MaChiTietMuon,
                    lsg.MaDocGia,
                    ctm.MaCuonSach,
                    ds.TenDauSach,
                    lsg.NgayGiaHan,
                    lsg.HanTraCu,
                    lsg.HanTraMoi,
                    lsg.SoNgayGiaHan,
                    lsg.LanGiaHanThu,
                    lsg.TrangThai,
                    lsg.LyDoTuChoi
                FROM LICHSUGIAHAN lsg
                INNER JOIN CHITIETPHIEUMUON ctm
                    ON lsg.MaChiTietMuon = ctm.MaChiTietMuon
                INNER JOIN CUONSACH cs
                    ON ctm.MaCuonSach = cs.MaCuonSach
                INNER JOIN DAUSACH ds
                    ON cs.MaDauSach = ds.MaDauSach
                WHERE lsg.MaDocGia = ?
                ORDER BY lsg.NgayGiaHan DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReaderRenewalResponse(
                        rs.getString("MaGiaHan"),
                        rs.getString("MaChiTietMuon"),
                        rs.getString("MaDocGia"),
                        rs.getString("MaCuonSach"),
                        rs.getString("TenDauSach"),
                        toLocalDateTime(rs.getTimestamp("NgayGiaHan")),
                        toLocalDateTime(rs.getTimestamp("HanTraCu")),
                        toLocalDateTime(rs.getTimestamp("HanTraMoi")),
                        rs.getInt("SoNgayGiaHan"),
                        rs.getInt("LanGiaHanThu"),
                        rs.getString("TrangThai"),
                        rs.getString("LyDoTuChoi")
                ),
                maDocGia
        );
    }

    private LoanRenewalData findLoanForRenew(String maChiTietMuon) {
        String sql = """
                SELECT
                    ctm.MaChiTietMuon,
                    pm.MaDocGia,
                    dg.MaTaiKhoan,
                    ctm.MaCuonSach,
                    ds.TenDauSach,
                    ctm.HanTra,
                    ctm.TrangThai,
                    qdm.SoNgayGiaHanMoiLan,
                    COALESCE(qdg.SoLanGiaHanToiDa, 0) AS SoLanGiaHanToiDa
                FROM CHITIETPHIEUMUON ctm WITH (UPDLOCK, ROWLOCK)
                INNER JOIN PHIEUMUON pm
                    ON ctm.MaPhieuMuon = pm.MaPhieuMuon
                INNER JOIN DOCGIA dg
                    ON pm.MaDocGia = dg.MaDocGia
                INNER JOIN CUONSACH cs
                    ON ctm.MaCuonSach = cs.MaCuonSach
                INNER JOIN DAUSACH ds
                    ON cs.MaDauSach = ds.MaDauSach
                INNER JOIN QUYDINHMUON_THELOAI qdm
                    ON ctm.MaQuyDinhMuon = qdm.MaQuyDinhMuon
                LEFT JOIN QUYDINHGOI qdg
                    ON qdg.MaPhienBan = pm.MaPhienBanQuyDinh
                   AND qdg.MaGoiThanhVien = qdm.MaGoiThanhVien
                WHERE ctm.MaChiTietMuon = ?
                """;

        List<LoanRenewalData> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new LoanRenewalData(
                        rs.getString("MaChiTietMuon"),
                        rs.getString("MaDocGia"),
                        rs.getString("MaTaiKhoan"),
                        rs.getString("MaCuonSach"),
                        rs.getString("TenDauSach"),
                        toLocalDateTime(rs.getTimestamp("HanTra")),
                        rs.getString("TrangThai"),
                        rs.getInt("SoNgayGiaHanMoiLan"),
                        rs.getInt("SoLanGiaHanToiDa")
                ),
                maChiTietMuon
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy chi tiết mượn: " + maChiTietMuon);
        }

        return result.get(0);
    }

    private boolean hasUnpaidDebt(String maDocGia) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM KHOANNO
                WHERE MaDocGia = ?
                  AND TrangThai <> N'Đã thanh toán'
                  AND SoTienConLai > 0
                """,
                Integer.class,
                maDocGia
        );

        return count != null && count > 0;
    }

    private int countSuccessfulRenewals(String maChiTietMuon) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM LICHSUGIAHAN
                WHERE MaChiTietMuon = ?
                  AND TrangThai = N'Thành công'
                """,
                Integer.class,
                maChiTietMuon
        );

        return count == null ? 0 : count;
    }

    private void createRenewalSuccessNotification(
            String maTaiKhoan,
            String maGiaHan,
            String tenDauSach,
            LocalDateTime hanTraCu,
            LocalDateTime hanTraMoi
    ) {
        ensureNotificationTypeExists();

        jdbcTemplate.update(
                """
                INSERT INTO THONGBAO
                (
                    MaThongBao,
                    MaTaiKhoanNhan,
                    MaLoaiThongBao,
                    TieuDe,
                    NoiDung,
                    NgayTao,
                    GuiTrongApp,
                    GuiEmail,
                    TrangThaiEmail,
                    SoLanThuGuiEmail
                )
                VALUES (?, ?, ?, N'Gia hạn sách thành công', ?, SYSDATETIME(), 1, 0, N'Không gửi', 0)
                """,
                generateId("TB_GH"),
                maTaiKhoan,
                LOAI_THONG_BAO_GIA_HAN,
                "Bạn đã gia hạn thành công sách \"" + tenDauSach + "\" từ hạn "
                        + formatDateTime(hanTraCu) + " sang " + formatDateTime(hanTraMoi)
                        + ". Mã gia hạn: " + maGiaHan
        );
    }

    private void ensureNotificationTypeExists() {
        jdbcTemplate.update(
                """
                IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = ?)
                INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
                VALUES (?, N'Gia hạn thành công', N'Thông báo gia hạn sách thành công')
                """,
                LOAI_THONG_BAO_GIA_HAN,
                LOAI_THONG_BAO_GIA_HAN
        );
    }

    private RenewalEligibility getRenewalEligibility(
            boolean hasDebt,
            int soLanDaGiaHan,
            int soLanGiaHanToiDa,
            int soNgayGiaHanMoiLan
    ) {
        if (hasDebt) {
            return new RenewalEligibility(false, "Độc giả đang có nợ chưa thanh toán");
        }

        if (soLanGiaHanToiDa <= 0) {
            return new RenewalEligibility(false, "Gói hiện tại không cho phép gia hạn");
        }

        if (soLanDaGiaHan >= soLanGiaHanToiDa) {
            return new RenewalEligibility(false, "Đã vượt số lần gia hạn tối đa");
        }

        if (soNgayGiaHanMoiLan <= 0) {
            return new RenewalEligibility(false, "Quy định không cho cộng thêm ngày gia hạn");
        }

        return new RenewalEligibility(true, null);
    }

    private String getRequiredReaderId(AuthUser user) {
        if (user == null) {
            throw new BusinessException("Bạn chưa đăng nhập");
        }

        if (user.getMaDocGia() == null || user.getMaDocGia().isBlank()) {
            throw new BusinessException("Tài khoản hiện tại không phải tài khoản độc giả");
        }

        return user.getMaDocGia();
    }

    private String generateId(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return prefix + "_" + timestamp + "_" + random;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "chưa có" : value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private record LoanRenewalData(
            String maChiTietMuon,
            String maDocGia,
            String maTaiKhoan,
            String maCuonSach,
            String tenDauSach,
            LocalDateTime hanTra,
            String trangThai,
            int soNgayGiaHanMoiLan,
            int soLanGiaHanToiDa
    ) {
    }

    private record RenewalEligibility(
            boolean coTheGiaHan,
            String lyDoKhongTheGiaHan
    ) {
    }
}

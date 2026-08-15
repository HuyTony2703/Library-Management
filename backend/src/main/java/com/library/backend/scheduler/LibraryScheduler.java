package com.library.backend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class LibraryScheduler {

    private static final Logger log = LoggerFactory.getLogger(LibraryScheduler.class);

    private static final String TT_SANCO = "TT_SANCO";

    private static final String TB_SAP_DEN_HAN = "TB_SAP_DEN_HAN";
    private static final String TB_QUA_HAN_TRA = "TB_QUA_HAN_TRA";
    private static final String TB_GOI_SAP_HET_HAN = "TB_GOI_SAP_HET_HAN";
    private static final String TB_SAP_HET_HAN_THE = "TB_SAP_HET_HAN_THE";
    private static final String TB_DOI_TRANGTHAI = "TB_TAIKHOAN_THE_DOI_TRANGTHAI";

    private final JdbcTemplate jdbcTemplate;

    public LibraryScheduler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "${app.scheduler.reservation-expiry-cron:0 30 * * * *}")
    public void expireHeldReservations() {
        List<ExpiringReservation> rows = jdbcTemplate.query(
                """
                SELECT pdt.MaPhieuDatTruoc, pdt.MaCuonSachDuocGiu, pdt.MaDocGia
                FROM PHIEUDATTRUOC pdt
                WHERE pdt.TrangThai = N'Đã giữ chỗ'
                  AND pdt.NgayHetHanGiuCho IS NOT NULL
                  AND pdt.NgayHetHanGiuCho < SYSDATETIME()
                """,
                (rs, rowNum) -> new ExpiringReservation(
                        rs.getString("MaPhieuDatTruoc"),
                        rs.getString("MaCuonSachDuocGiu"),
                        rs.getString("MaDocGia")
                )
        );

        if (rows.isEmpty()) {
            return;
        }

        int expired = 0;
        for (ExpiringReservation row : rows) {
            jdbcTemplate.update(
                    """
                    UPDATE PHIEUDATTRUOC
                    SET TrangThai = N'Đã hết hạn'
                    WHERE MaPhieuDatTruoc = ?
                      AND TrangThai = N'Đã giữ chỗ'
                    """,
                    row.maPhieuDatTruoc()
            );

            if (row.maCuonSachDuocGiu() != null) {
                jdbcTemplate.update(
                        """
                        UPDATE CUONSACH
                        SET MaTrangThai = ?
                        WHERE MaCuonSach = ?
                        """,
                        TT_SANCO,
                        row.maCuonSachDuocGiu()
                );
            }

            notifyReservationExpired(row);
            expired++;
        }

        log.info("Scheduler expireHeldReservations: đã hết hạn {} phiếu đặt trước giữ chỗ", expired);
    }

    @Scheduled(cron = "${app.scheduler.reader-card-expiry-cron:0 0 1 * * *}")
    public void expireReaderCards() {
        List<ExpiringReaderCard> rows = jdbcTemplate.query(
                """
                SELECT MaDocGia, MaTaiKhoan
                FROM DOCGIA
                WHERE TrangThai = N'Hoạt động'
                  AND NgayHetHanThe < CAST(GETDATE() AS DATE)
                """,
                (rs, rowNum) -> new ExpiringReaderCard(
                        rs.getString("MaDocGia"),
                        rs.getString("MaTaiKhoan")
                )
        );

        if (rows.isEmpty()) {
            return;
        }

        int updated = 0;
        for (ExpiringReaderCard row : rows) {
            jdbcTemplate.update(
                    """
                    UPDATE DOCGIA
                    SET TrangThai = N'Hết hạn'
                    WHERE MaDocGia = ?
                      AND TrangThai = N'Hoạt động'
                    """,
                    row.maDocGia()
            );
            updated++;
        }

        log.info("Scheduler expireReaderCards: đã chuyển {} thẻ độc giả hết hạn", updated);
    }

    @Scheduled(cron = "${app.scheduler.membership-expiry-cron:0 0 1 * * *}")
    public void expireMemberships() {
        List<ExpiringMembership> rows = jdbcTemplate.query(
                """
                SELECT lsg.MaLichSuGoi, lsg.MaDocGia
                FROM LICHSUGOITHANHVIEN lsg
                WHERE lsg.TrangThai = N'Đang sử dụng'
                  AND lsg.NgayKetThuc < CAST(GETDATE() AS DATE)
                """,
                (rs, rowNum) -> new ExpiringMembership(
                        rs.getString("MaLichSuGoi"),
                        rs.getString("MaDocGia")
                )
        );

        if (rows.isEmpty()) {
            return;
        }

        int updated = 0;
        for (ExpiringMembership row : rows) {
            jdbcTemplate.update(
                    """
                    UPDATE LICHSUGOITHANHVIEN
                    SET TrangThai = N'Hết hạn'
                    WHERE MaLichSuGoi = ?
                      AND TrangThai = N'Đang sử dụng'
                    """,
                    row.maLichSuGoi()
            );
            updated++;
        }

        log.info("Scheduler expireMemberships: đã hết hạn {} gói thành viên", updated);
    }

    @Scheduled(cron = "${app.scheduler.due-reminder-cron:0 30 6 * * *}")
    public void sendDueReminders() {
        int soNgayNhac = getReminderDays();

        List<LoanReminder> rows = jdbcTemplate.query(
                """
                SELECT
                    ctm.MaChiTietMuon,
                    dg.MaTaiKhoan,
                    ds.TenDauSach,
                    ctm.HanTra
                FROM CHITIETPHIEUMUON ctm
                INNER JOIN PHIEUMUON pm ON pm.MaPhieuMuon = ctm.MaPhieuMuon
                INNER JOIN DOCGIA dg ON dg.MaDocGia = pm.MaDocGia
                INNER JOIN CUONSACH cs ON cs.MaCuonSach = ctm.MaCuonSach
                INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
                WHERE ctm.TrangThai = N'Đang mượn'
                  AND ctm.HanTra > SYSDATETIME()
                  AND ctm.HanTra <= DATEADD(DAY, ?, SYSDATETIME())
                """,
                (rs, rowNum) -> new LoanReminder(
                        rs.getString("MaChiTietMuon"),
                        rs.getString("MaTaiKhoan"),
                        rs.getString("TenDauSach"),
                        rs.getTimestamp("HanTra").toLocalDateTime()
                ),
                soNgayNhac
        );

        if (rows.isEmpty()) {
            return;
        }

        int created = 0;
        for (LoanReminder row : rows) {
            if (notificationAlreadySent(row.maTaiKhoan(), TB_SAP_DEN_HAN, row.maChiTietMuon())) {
                continue;
            }
            insertNotification(
                    row.maTaiKhoan(),
                    TB_SAP_DEN_HAN,
                    "Sắp đến hạn trả",
                    "Sách \"" + row.tenDauSach() + "\" (mã chi tiết mượn "
                            + row.maChiTietMuon() + ") sắp đến hạn trả vào "
                            + formatDateTime(row.hanTra()) + "."
            );
            created++;
        }

        log.info("Scheduler sendDueReminders: đã gửi {} nhắc nhở sắp đến hạn trả", created);
    }

    @Scheduled(cron = "${app.scheduler.overdue-notification-cron:0 30 6 * * *}")
    public void sendOverdueNotifications() {
        List<LoanReminder> rows = jdbcTemplate.query(
                """
                SELECT
                    ctm.MaChiTietMuon,
                    dg.MaTaiKhoan,
                    ds.TenDauSach,
                    ctm.HanTra
                FROM CHITIETPHIEUMUON ctm
                INNER JOIN PHIEUMUON pm ON pm.MaPhieuMuon = ctm.MaPhieuMuon
                INNER JOIN DOCGIA dg ON dg.MaDocGia = pm.MaDocGia
                INNER JOIN CUONSACH cs ON cs.MaCuonSach = ctm.MaCuonSach
                INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
                WHERE ctm.TrangThai = N'Đang mượn'
                  AND ctm.HanTra < SYSDATETIME()
                """,
                (rs, rowNum) -> new LoanReminder(
                        rs.getString("MaChiTietMuon"),
                        rs.getString("MaTaiKhoan"),
                        rs.getString("TenDauSach"),
                        rs.getTimestamp("HanTra").toLocalDateTime()
                )
        );

        if (rows.isEmpty()) {
            return;
        }

        int created = 0;
        for (LoanReminder row : rows) {
            if (notificationAlreadySent(row.maTaiKhoan(), TB_QUA_HAN_TRA, row.maChiTietMuon())) {
                continue;
            }
            insertNotification(
                    row.maTaiKhoan(),
                    TB_QUA_HAN_TRA,
                    "Đã quá hạn trả",
                    "Sách \"" + row.tenDauSach() + "\" (mã chi tiết mượn "
                            + row.maChiTietMuon() + ") đã quá hạn trả từ "
                            + formatDateTime(row.hanTra()) + ". Vui lòng trả sách sớm."
            );
            created++;
        }

        log.info("Scheduler sendOverdueNotifications: đã gửi {} nhắc nhở quá hạn trả", created);
    }

    @Scheduled(cron = "${app.scheduler.membership-reminder-cron:0 30 6 * * *}")
    public void sendMembershipExpiryReminders() {
        List<MembershipReminder> rows = jdbcTemplate.query(
                """
                SELECT
                    lsg.MaLichSuGoi,
                    lsg.MaDocGia,
                    dg.MaTaiKhoan,
                    g.TenGoi,
                    lsg.NgayKetThuc
                FROM LICHSUGOITHANHVIEN lsg
                INNER JOIN GOITHANHVIEN g ON g.MaGoiThanhVien = lsg.MaGoiThanhVien
                INNER JOIN DOCGIA dg ON dg.MaDocGia = lsg.MaDocGia
                WHERE lsg.TrangThai = N'Đang sử dụng'
                  AND lsg.NgayKetThuc >= CAST(GETDATE() AS DATE)
                  AND lsg.NgayKetThuc <= DATEADD(DAY, 7, CAST(GETDATE() AS DATE))
                """,
                (rs, rowNum) -> new MembershipReminder(
                        rs.getString("MaLichSuGoi"),
                        rs.getString("MaTaiKhoan"),
                        rs.getString("TenGoi"),
                        rs.getDate("NgayKetThuc").toLocalDate()
                )
        );

        if (rows.isEmpty()) {
            return;
        }

        int created = 0;
        for (MembershipReminder row : rows) {
            if (notificationAlreadySent(row.maTaiKhoan(), TB_GOI_SAP_HET_HAN, row.maLichSuGoi())) {
                continue;
            }
            insertNotification(
                    row.maTaiKhoan(),
                    TB_GOI_SAP_HET_HAN,
                    "Gói thành viên sắp hết hạn",
                    "Gói \"" + row.tenGoi() + "\" của bạn sẽ hết hạn vào ngày "
                            + row.ngayKetThuc() + ". Gia hạn để tiếp tục mượn sách."
            );
            created++;
        }

        log.info("Scheduler sendMembershipExpiryReminders: đã gửi {} nhắc nhở gói sắp hết hạn", created);
    }

    @Scheduled(cron = "${app.scheduler.card-expiry-reminder-cron:0 30 6 * * *}")
    public void sendCardExpiryReminders() {
        int soNgayNhac = getReminderDays();

        List<CardReminder> rows = jdbcTemplate.query(
                """
                SELECT MaDocGia, MaTaiKhoan, NgayHetHanThe
                FROM DOCGIA
                WHERE TrangThai = N'Hoạt động'
                  AND NgayHetHanThe > CAST(GETDATE() AS DATE)
                  AND NgayHetHanThe <= DATEADD(DAY, ?, CAST(GETDATE() AS DATE))
                """,
                (rs, rowNum) -> new CardReminder(
                        rs.getString("MaDocGia"),
                        rs.getString("MaTaiKhoan"),
                        rs.getDate("NgayHetHanThe").toLocalDate()
                ),
                soNgayNhac
        );

        if (rows.isEmpty()) {
            return;
        }

        int created = 0;
        for (CardReminder row : rows) {
            if (notificationAlreadySent(row.maTaiKhoan(), TB_SAP_HET_HAN_THE, row.maDocGia())) {
                continue;
            }
            insertNotification(
                    row.maTaiKhoan(),
                    TB_SAP_HET_HAN_THE,
                    "Thẻ sắp hết hạn",
                    "Thẻ độc giả của bạn sẽ hết hạn vào ngày " + row.ngayHetHanThe()
                            + ". Vui lòng liên hệ thủ thư để gia hạn thẻ."
            );
            created++;
        }

        log.info("Scheduler sendCardExpiryReminders: đã gửi {} nhắc nhở thẻ sắp hết hạn", created);
    }

    private void notifyReservationExpired(ExpiringReservation row) {
        String maTaiKhoan = lookupTaiKhoan(row.maDocGia());
        if (maTaiKhoan == null) {
            return;
        }
        if (notificationAlreadySent(maTaiKhoan, TB_DOI_TRANGTHAI, row.maPhieuDatTruoc())) {
            return;
        }
        insertNotification(
                maTaiKhoan,
                TB_DOI_TRANGTHAI,
                "Phiếu đặt trước đã hết hạn",
                "Phiếu đặt trước " + row.maPhieuDatTruoc() + " của bạn đã quá hạn giữ chỗ và bị hủy."
        );
    }

    private String lookupTaiKhoan(String maDocGia) {
        List<String> result = jdbcTemplate.query(
                "SELECT MaTaiKhoan FROM DOCGIA WHERE MaDocGia = ?",
                (rs, rowNum) -> rs.getString("MaTaiKhoan"),
                maDocGia
        );
        return result.isEmpty() ? null : result.get(0);
    }

    private int getReminderDays() {
        List<Integer> result = jdbcTemplate.query(
                """
                SELECT TOP 1 ts.SoNgayNhacTruocHan
                FROM PHIENBANQUYDINH pb
                INNER JOIN THAMSOQUYDINH ts ON pb.MaPhienBan = ts.MaPhienBan
                WHERE pb.TrangThai = N'Đang áp dụng'
                ORDER BY pb.NgayApDung DESC
                """,
                (rs, rowNum) -> rs.getInt("SoNgayNhacTruocHan")
        );
        return result.isEmpty() ? 3 : result.get(0);
    }

    private boolean notificationAlreadySent(String maTaiKhoan, String maLoaiThongBao, String reference) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM THONGBAO
                WHERE MaTaiKhoanNhan = ?
                  AND MaLoaiThongBao = ?
                  AND NoiDung LIKE N'%' + ? + N'%'
                """,
                Integer.class,
                maTaiKhoan,
                maLoaiThongBao,
                reference
        );
        return count != null && count > 0;
    }

    private void insertNotification(
            String maTaiKhoan,
            String maLoaiThongBao,
            String tieuDe,
            String noiDung
    ) {
        ensureNotificationTypeExists(maLoaiThongBao);
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
                    SoLanThuGuiEmail,
                    DaDoc,
                    ThoiGianDoc
                )
                VALUES (?, ?, ?, ?, ?, SYSDATETIME(), 1, 0, N'Không gửi', 0, 0, NULL)
                """,
                generateId("TB_SCH"),
                maTaiKhoan,
                maLoaiThongBao,
                tieuDe,
                noiDung
        );
    }

    private void ensureNotificationTypeExists(String maLoaiThongBao) {
        String tenLoai = switch (maLoaiThongBao) {
            case TB_SAP_DEN_HAN -> "Sách sắp đến hạn trả";
            case TB_QUA_HAN_TRA -> "Sách đã quá hạn trả";
            case TB_GOI_SAP_HET_HAN -> "Gói thành viên sắp hết hạn";
            case TB_SAP_HET_HAN_THE -> "Thẻ sắp hết hạn";
            case TB_DOI_TRANGTHAI -> "Tài khoản hoặc thẻ độc giả thay đổi trạng thái";
            default -> maLoaiThongBao;
        };

        jdbcTemplate.update(
                """
                IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = ?)
                INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
                VALUES (?, ?, N'Thông báo tự động từ hệ thống')
                """,
                maLoaiThongBao,
                maLoaiThongBao,
                tenLoai
        );
    }

    private String generateId(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return prefix + "_" + timestamp + "_" + random;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    record ExpiringReservation(String maPhieuDatTruoc, String maCuonSachDuocGiu, String maDocGia) {
    }

    record ExpiringReaderCard(String maDocGia, String maTaiKhoan) {
    }

    record ExpiringMembership(String maLichSuGoi, String maDocGia) {
    }

    record LoanReminder(String maChiTietMuon, String maTaiKhoan, String tenDauSach, LocalDateTime hanTra) {
    }

    record MembershipReminder(String maLichSuGoi, String maTaiKhoan, String tenGoi, java.time.LocalDate ngayKetThuc) {
    }

    record CardReminder(String maDocGia, String maTaiKhoan, java.time.LocalDate ngayHetHanThe) {
    }
}

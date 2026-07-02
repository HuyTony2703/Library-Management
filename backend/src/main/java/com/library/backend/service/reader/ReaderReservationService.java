package com.library.backend.service.reader;

import com.library.backend.dto.reader.ReaderReservationRequest;
import com.library.backend.dto.reader.ReaderReservationResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.security.AuthUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReaderReservationService {

    private static final String TT_SANCO = "TT_SANCO";
    private static final String TT_DANGDATTRUOC = "TT_DANGDATTRUOC";
    private static final String TB_DAT_TRUOC_TC = "TB_DAT_TRUOC_TC";

    private final JdbcTemplate jdbcTemplate;

    public ReaderReservationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReaderReservationResponse> getMyReservations(AuthUser user) {
        String maDocGia = getRequiredReaderId(user);

        String sql = """
                SELECT
                    pdt.MaPhieuDatTruoc,
                    pdt.MaDocGia,
                    pdt.MaDauSach,
                    ds.TenDauSach,
                    pdt.MaCuonSachDuocGiu,
                    pdt.MaChiNhanh,
                    pdt.NgayDat,
                    pdt.NgayHetHanGiuCho,
                    pdt.TrangThai,
                    pdt.GhiChu
                FROM PHIEUDATTRUOC pdt
                INNER JOIN DAUSACH ds
                    ON pdt.MaDauSach = ds.MaDauSach
                WHERE pdt.MaDocGia = ?
                ORDER BY pdt.NgayDat DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReaderReservationResponse(
                        rs.getString("MaPhieuDatTruoc"),
                        rs.getString("MaDocGia"),
                        rs.getString("MaDauSach"),
                        rs.getString("TenDauSach"),
                        rs.getString("MaCuonSachDuocGiu"),
                        rs.getString("MaChiNhanh"),
                        toLocalDateTime(rs.getTimestamp("NgayDat")),
                        toLocalDateTime(rs.getTimestamp("NgayHetHanGiuCho")),
                        rs.getString("TrangThai"),
                        rs.getString("GhiChu")
                ),
                maDocGia
        );
    }

    @Transactional
    public ReaderReservationResponse reserveByTitle(AuthUser user, ReaderReservationRequest request) {
        String maDocGia = getRequiredReaderId(user);

        validateRequestForTitle(request);
        validateReaderCanReserve(maDocGia);
        validateBasicReferences(request.getMaDauSach(), request.getMaChiNhanh());
        validateNoActiveReservation(maDocGia, request.getMaDauSach());

        String maPhieuDatTruoc = generateId("PDT");

        jdbcTemplate.update(
                """
                INSERT INTO PHIEUDATTRUOC
                (
                    MaPhieuDatTruoc,
                    MaDocGia,
                    MaDauSach,
                    MaCuonSachDuocGiu,
                    MaChiNhanh,
                    NgayDat,
                    NgayHetHanGiuCho,
                    TrangThai,
                    GhiChu
                )
                VALUES (?, ?, ?, NULL, ?, SYSDATETIME(), NULL, N'Đang chờ', ?)
                """,
                maPhieuDatTruoc,
                maDocGia,
                request.getMaDauSach(),
                request.getMaChiNhanh(),
                cleanText(request.getGhiChu())
        );

        createReservationNotification(
                user.getMaTaiKhoan(),
                maPhieuDatTruoc,
                request.getMaDauSach(),
                "Bạn đã đặt trước đầu sách " + request.getMaDauSach() + "."
        );

        return getReservationById(maDocGia, maPhieuDatTruoc);
    }

    @Transactional
    public ReaderReservationResponse reserveByCopy(AuthUser user, ReaderReservationRequest request) {
        String maDocGia = getRequiredReaderId(user);

        validateRequestForCopy(request);
        validateReaderCanReserve(maDocGia);
        validateBasicReferences(request.getMaDauSach(), request.getMaChiNhanh());
        validateNoActiveReservation(maDocGia, request.getMaDauSach());
        validateCopyCanBeReserved(
                request.getMaCuonSach(),
                request.getMaDauSach(),
                request.getMaChiNhanh()
        );

        String maPhieuDatTruoc = generateId("PDT");
        int soNgayGiu = getSoNgayGiuDatTruoc();

        jdbcTemplate.update(
                """
                INSERT INTO PHIEUDATTRUOC
                (
                    MaPhieuDatTruoc,
                    MaDocGia,
                    MaDauSach,
                    MaCuonSachDuocGiu,
                    MaChiNhanh,
                    NgayDat,
                    NgayHetHanGiuCho,
                    TrangThai,
                    GhiChu
                )
                VALUES (?, ?, ?, ?, ?, SYSDATETIME(), DATEADD(DAY, ?, SYSDATETIME()), N'Đã giữ chỗ', ?)
                """,
                maPhieuDatTruoc,
                maDocGia,
                request.getMaDauSach(),
                request.getMaCuonSach(),
                request.getMaChiNhanh(),
                soNgayGiu,
                cleanText(request.getGhiChu())
        );

        jdbcTemplate.update(
                """
                UPDATE CUONSACH
                SET MaTrangThai = ?
                WHERE MaCuonSach = ?
                """,
                TT_DANGDATTRUOC,
                request.getMaCuonSach()
        );

        createReservationNotification(
                user.getMaTaiKhoan(),
                maPhieuDatTruoc,
                request.getMaDauSach(),
                "Bạn đã giữ chỗ cuốn " + request.getMaCuonSach()
                        + " trong " + soNgayGiu + " ngày."
        );

        return getReservationById(maDocGia, maPhieuDatTruoc);
    }

    @Transactional
    public void cancelReservation(AuthUser user, String maPhieuDatTruoc) {
        String maDocGia = getRequiredReaderId(user);
        ReservationBasicInfo reservation = getBasicReservationForUpdate(maDocGia, maPhieuDatTruoc);

        if (!"Đang chờ".equals(reservation.trangThai())
                && !"Đã giữ chỗ".equals(reservation.trangThai())) {
            throw new BusinessException("Chỉ được hủy phiếu đặt trước đang chờ hoặc đã giữ chỗ");
        }

        jdbcTemplate.update(
                """
                UPDATE PHIEUDATTRUOC
                SET TrangThai = N'Đã hủy'
                WHERE MaPhieuDatTruoc = ?
                  AND MaDocGia = ?
                """,
                maPhieuDatTruoc,
                maDocGia
        );

        if (reservation.maCuonSachDuocGiu() != null && "Đã giữ chỗ".equals(reservation.trangThai())) {
            jdbcTemplate.update(
                    """
                    UPDATE CUONSACH
                    SET MaTrangThai = ?
                    WHERE MaCuonSach = ?
                    """,
                    TT_SANCO,
                    reservation.maCuonSachDuocGiu()
            );
        }
    }

    private ReaderReservationResponse getReservationById(String maDocGia, String maPhieuDatTruoc) {
        String sql = """
                SELECT
                    pdt.MaPhieuDatTruoc,
                    pdt.MaDocGia,
                    pdt.MaDauSach,
                    ds.TenDauSach,
                    pdt.MaCuonSachDuocGiu,
                    pdt.MaChiNhanh,
                    pdt.NgayDat,
                    pdt.NgayHetHanGiuCho,
                    pdt.TrangThai,
                    pdt.GhiChu
                FROM PHIEUDATTRUOC pdt
                INNER JOIN DAUSACH ds
                    ON pdt.MaDauSach = ds.MaDauSach
                WHERE pdt.MaPhieuDatTruoc = ?
                  AND pdt.MaDocGia = ?
                """;

        List<ReaderReservationResponse> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReaderReservationResponse(
                        rs.getString("MaPhieuDatTruoc"),
                        rs.getString("MaDocGia"),
                        rs.getString("MaDauSach"),
                        rs.getString("TenDauSach"),
                        rs.getString("MaCuonSachDuocGiu"),
                        rs.getString("MaChiNhanh"),
                        toLocalDateTime(rs.getTimestamp("NgayDat")),
                        toLocalDateTime(rs.getTimestamp("NgayHetHanGiuCho")),
                        rs.getString("TrangThai"),
                        rs.getString("GhiChu")
                ),
                maPhieuDatTruoc,
                maDocGia
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy phiếu đặt trước");
        }

        return result.get(0);
    }

    private ReservationBasicInfo getBasicReservationForUpdate(String maDocGia, String maPhieuDatTruoc) {
        String sql = """
                SELECT MaPhieuDatTruoc, MaCuonSachDuocGiu, TrangThai
                FROM PHIEUDATTRUOC WITH (UPDLOCK, ROWLOCK)
                WHERE MaPhieuDatTruoc = ?
                  AND MaDocGia = ?
                """;

        List<ReservationBasicInfo> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReservationBasicInfo(
                        rs.getString("MaPhieuDatTruoc"),
                        rs.getString("MaCuonSachDuocGiu"),
                        rs.getString("TrangThai")
                ),
                maPhieuDatTruoc,
                maDocGia
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy phiếu đặt trước");
        }

        return result.get(0);
    }

    private void validateRequestForTitle(ReaderReservationRequest request) {
        if (request == null) {
            throw new BusinessException("Thiếu thông tin đặt trước");
        }

        if (isBlank(request.getMaDauSach())) {
            throw new BusinessException("Mã đầu sách không được để trống");
        }

        if (isBlank(request.getMaChiNhanh())) {
            throw new BusinessException("Mã chi nhánh không được để trống");
        }
    }

    private void validateRequestForCopy(ReaderReservationRequest request) {
        validateRequestForTitle(request);

        if (isBlank(request.getMaCuonSach())) {
            throw new BusinessException("Mã cuốn sách không được để trống khi đặt đúng cuốn");
        }
    }

    private void validateReaderCanReserve(String maDocGia) {
        String sql = """
                SELECT TrangThai, NgayHetHanThe
                FROM DOCGIA
                WHERE MaDocGia = ?
                """;

        List<ReaderStatusInfo> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReaderStatusInfo(
                        rs.getString("TrangThai"),
                        rs.getDate("NgayHetHanThe").toLocalDate()
                ),
                maDocGia
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy độc giả");
        }

        ReaderStatusInfo reader = result.get(0);

        if (!"Hoạt động".equals(reader.trangThai())) {
            throw new BusinessException("Độc giả không ở trạng thái hoạt động");
        }

        if (!reader.ngayHetHanThe().isAfter(LocalDate.now())) {
            throw new BusinessException("Thẻ độc giả đã hết hạn, không thể đặt trước sách");
        }
    }

    private void validateBasicReferences(String maDauSach, String maChiNhanh) {
        List<String> bookStatuses = jdbcTemplate.query(
                "SELECT TrangThai FROM DAUSACH WHERE MaDauSach = ?",
                (rs, rowNum) -> rs.getString("TrangThai"),
                maDauSach
        );
        if (bookStatuses.isEmpty()) {
            throw new ResourceNotFoundException("Đầu sách không tồn tại");
        }
        if (!"Hoạt động".equals(bookStatuses.get(0))) {
            throw new BusinessException("Đầu sách đang ngừng hiển thị, không thể tạo đặt trước mới");
        }

        if (!exists("CHINHANH", "MaChiNhanh", maChiNhanh)) {
            throw new ResourceNotFoundException("Chi nhánh không tồn tại");
        }
    }

    private void validateNoActiveReservation(String maDocGia, String maDauSach) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM PHIEUDATTRUOC
                WHERE MaDocGia = ?
                  AND MaDauSach = ?
                  AND TrangThai IN (N'Đang chờ', N'Đã giữ chỗ')
                """,
                Integer.class,
                maDocGia,
                maDauSach
        );

        if (count != null && count > 0) {
            throw new BusinessException("Bạn đã có phiếu đặt trước đang hoạt động cho đầu sách này");
        }
    }

    private void validateCopyCanBeReserved(String maCuonSach, String maDauSach, String maChiNhanh) {
        String sql = """
                SELECT MaDauSach, MaChiNhanh, MaTrangThai
                FROM CUONSACH WITH (UPDLOCK, ROWLOCK)
                WHERE MaCuonSach = ?
                """;

        List<BookCopyStatusInfo> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new BookCopyStatusInfo(
                        rs.getString("MaDauSach"),
                        rs.getString("MaChiNhanh"),
                        rs.getString("MaTrangThai")
                ),
                maCuonSach
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Cuốn sách không tồn tại");
        }

        BookCopyStatusInfo copy = result.get(0);

        if (!maDauSach.equals(copy.maDauSach())) {
            throw new BusinessException("Cuốn sách không thuộc đầu sách đã chọn");
        }

        if (!maChiNhanh.equals(copy.maChiNhanh())) {
            throw new BusinessException("Cuốn sách không thuộc chi nhánh đã chọn");
        }

        if (!TT_SANCO.equals(copy.maTrangThai())) {
            throw new BusinessException("Chỉ được đặt trước cuốn sách đang sẵn có");
        }
    }

    private int getSoNgayGiuDatTruoc() {
        String sql = """
                SELECT TOP 1 ts.SoNgayGiuDatTruoc
                FROM PHIENBANQUYDINH pb
                INNER JOIN THAMSOQUYDINH ts
                    ON pb.MaPhienBan = ts.MaPhienBan
                WHERE pb.TrangThai = N'Đang áp dụng'
                ORDER BY pb.NgayApDung DESC
                """;

        List<Integer> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("SoNgayGiuDatTruoc")
        );

        return result.isEmpty() ? 2 : result.get(0);
    }

    private void createReservationNotification(
            String maTaiKhoan,
            String maPhieuDatTruoc,
            String maDauSach,
            String noiDung
    ) {
        ensureReservationNotificationTypeExists();

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
                VALUES (?, ?, ?, N'Đặt trước thành công', ?, SYSDATETIME(), 1, 0, N'Không gửi', 0)
                """,
                generateId("TB_PDT"),
                maTaiKhoan,
                TB_DAT_TRUOC_TC,
                noiDung + " Mã phiếu: " + maPhieuDatTruoc + ", đầu sách: " + maDauSach
        );
    }

    private void ensureReservationNotificationTypeExists() {
        jdbcTemplate.update(
                """
                IF NOT EXISTS (SELECT 1 FROM LOAITHONGBAO WHERE MaLoaiThongBao = ?)
                INSERT INTO LOAITHONGBAO(MaLoaiThongBao, TenLoaiThongBao, MoTa)
                VALUES (?, N'Đặt trước thành công', N'Thông báo đặt trước sách thành công')
                """,
                TB_DAT_TRUOC_TC,
                TB_DAT_TRUOC_TC
        );
    }

    private boolean exists(String tableName, String idColumn, String value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?",
                Integer.class,
                value
        );

        return count != null && count > 0;
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String cleanText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record ReaderStatusInfo(String trangThai, LocalDate ngayHetHanThe) {
    }

    private record BookCopyStatusInfo(String maDauSach, String maChiNhanh, String maTrangThai) {
    }

    private record ReservationBasicInfo(
            String maPhieuDatTruoc,
            String maCuonSachDuocGiu,
            String trangThai
    ) {
    }
}

package com.library.backend.service;

import com.library.backend.dto.MuonSachRequest;
import com.library.backend.dto.MuonSachResponse;
import com.library.backend.dto.StaffContextResponse;
import com.library.backend.entity.ChiTietPhieuMuon;
import com.library.backend.entity.CuonSach;
import com.library.backend.entity.DocGia;
import com.library.backend.entity.PhieuMuon;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.repository.ChiTietPhieuMuonRepository;
import com.library.backend.repository.CuonSachRepository;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.PhieuMuonRepository;
import com.library.backend.security.AuthUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MuonSachService {

    private static final String TT_SANCO = "TT_SANCO";
    private static final String TT_DANGDATTRUOC = "TT_DANGDATTRUOC";
    private static final String TT_DANGMUON = "TT_DANGMUON";
    private static final String LOAN_STATUS_BORROWING = "Äang mÆ°á»£n";
    private static final String READER_STATUS_ACTIVE = "Hoáº¡t Ä‘á»™ng";
    private static final DateTimeFormatter LOAN_ID_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PhieuMuonRepository phieuMuonRepository;
    private final ChiTietPhieuMuonRepository chiTietPhieuMuonRepository;
    private final DocGiaRepository docGiaRepository;
    private final CuonSachRepository cuonSachRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ActivityLogService activityLogService;
    private final BranchAuthorizationService branchAuthorizationService;

    public MuonSachService(
            PhieuMuonRepository phieuMuonRepository,
            ChiTietPhieuMuonRepository chiTietPhieuMuonRepository,
            DocGiaRepository docGiaRepository,
            CuonSachRepository cuonSachRepository,
            JdbcTemplate jdbcTemplate,
            ActivityLogService activityLogService,
            BranchAuthorizationService branchAuthorizationService
    ) {
        this.phieuMuonRepository = phieuMuonRepository;
        this.chiTietPhieuMuonRepository = chiTietPhieuMuonRepository;
        this.docGiaRepository = docGiaRepository;
        this.cuonSachRepository = cuonSachRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.activityLogService = activityLogService;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    @Transactional
    public MuonSachResponse create(
            MuonSachRequest request,
            AuthUser authenticatedUser,
            String requestedIdempotencyKey
    ) {
        LoanCommand command = normalizeRequest(request, requestedIdempotencyKey);

        StaffContextResponse staffContext = branchAuthorizationService.requireAllowedBranch(
                authenticatedUser,
                command.maChiNhanh()
        );
        String authenticatedStaffId = staffContext.staffId();

        if (hasText(request.getMaNhanVienLap())
                && !request.getMaNhanVienLap().equals(authenticatedStaffId)) {
            throw new AccessDeniedException("Ma nhan vien lap khong khop tai khoan dang nhap");
        }

        String requestFingerprint = buildRequestFingerprint(command, authenticatedUser, authenticatedStaffId);
        MuonSachResponse idempotentResponse = findIdempotentResponse(command.idempotencyKey(), requestFingerprint);
        if (idempotentResponse != null) {
            return idempotentResponse;
        }

        String maPhieuMuon = hasText(request.getMaPhieuMuon())
                ? request.getMaPhieuMuon().trim()
                : generateLoanId();
        while (phieuMuonRepository.existsById(maPhieuMuon)) {
            if (hasText(request.getMaPhieuMuon())) {
                throw new BusinessException("Ma phieu muon da ton tai");
            }
            maPhieuMuon = generateLoanId();
        }

        if (!existsById("CHINHANH", "MaChiNhanh", command.maChiNhanh())) {
            throw new ResourceNotFoundException("Chi nhanh khong ton tai");
        }

        DocGia docGia = docGiaRepository.findById(command.maDocGia())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay doc gia voi ma: " + command.maDocGia()
                ));

        LocalDateTime ngayMuon = LocalDateTime.now();
        validateReaderCanBorrow(docGia, ngayMuon);

        String maPhienBan = getCurrentPolicyVersion();
        String maGoiThanhVien = getCurrentMembershipPlan(command.maDocGia(), ngayMuon.toLocalDate());
        int soSachDangMuon = countCurrentLoans(command.maDocGia());
        int soSachMuonToiDa = getBorrowLimit(maPhienBan, maGoiThanhVien);
        int soSachMuonMoi = command.maCuonSachs().size();

        if (soSachDangMuon + soSachMuonMoi > soSachMuonToiDa) {
            throw new BusinessException(
                    "Vuot qua so sach duoc muon. Dang muon "
                            + soSachDangMuon
                            + ", muon them "
                            + soSachMuonMoi
                            + ", toi da "
                            + soSachMuonToiDa
            );
        }

        PhieuMuon phieuMuon = new PhieuMuon();
        phieuMuon.setMaPhieuMuon(maPhieuMuon);
        phieuMuon.setMaDocGia(command.maDocGia());
        phieuMuon.setMaNhanVienLap(authenticatedStaffId);
        phieuMuon.setMaChiNhanh(command.maChiNhanh());
        phieuMuon.setMaPhienBanQuyDinh(maPhienBan);
        phieuMuon.setNgayMuon(ngayMuon);
        phieuMuon.setTrangThai(LOAN_STATUS_BORROWING);
        phieuMuon.setGhiChu(command.ghiChu());
        phieuMuon.setIdempotencyKey(command.idempotencyKey());
        phieuMuon.setRequestFingerprint(requestFingerprint);
        phieuMuonRepository.save(phieuMuon);

        for (int i = 0; i < command.maCuonSachs().size(); i++) {
            String maCuonSach = command.maCuonSachs().get(i);
            CuonSach cuonSach = cuonSachRepository.findByIdForUpdate(maCuonSach)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Khong tim thay cuon sach: " + maCuonSach
                    ));

            validateBookCopyCanBorrow(cuonSach, command.maChiNhanh(), command.maDocGia(), ngayMuon);

            BorrowRule borrowRule = getBorrowRule(maPhienBan, maGoiThanhVien, maCuonSach);
            LocalDateTime hanTra = calculateDueDate(ngayMuon, borrowRule.soNgayMuon(), docGia);

            ChiTietPhieuMuon chiTiet = new ChiTietPhieuMuon();
            chiTiet.setMaChiTietMuon(buildMaChiTietMuon(maPhieuMuon, i + 1));
            chiTiet.setMaPhieuMuon(maPhieuMuon);
            chiTiet.setMaCuonSach(maCuonSach);
            chiTiet.setMaQuyDinhMuon(borrowRule.maQuyDinhMuon());
            chiTiet.setNgayMuon(ngayMuon);
            chiTiet.setHanTra(hanTra);
            chiTiet.setNgayTraThucTe(null);
            chiTiet.setTrangThai(LOAN_STATUS_BORROWING);
            chiTietPhieuMuonRepository.save(chiTiet);

            cuonSach.setMaTrangThai(TT_DANGMUON);
            cuonSachRepository.save(cuonSach);
            closeReservationIfOwned(maCuonSach, command.maDocGia(), maPhieuMuon);
        }

        MuonSachResponse response = getById(maPhieuMuon);

        activityLogService.logAsAccountSafe(
                authenticatedUser.getMaTaiKhoan(),
                "Tao phieu muon",
                "PHIEUMUON",
                maPhieuMuon,
                "Nhan vien " + authenticatedStaffId
                        + " lap phieu cho doc gia " + command.maDocGia()
                        + " muon cac cuon: " + String.join(", ", command.maCuonSachs())
                        + " tai chi nhanh " + command.maChiNhanh()
                        + ", idempotency key: " + command.idempotencyKey()
        );

        return response;
    }

    public MuonSachResponse create(MuonSachRequest request, AuthUser authenticatedUser) {
        return create(request, authenticatedUser, null);
    }

    public MuonSachResponse getById(String maPhieuMuon) {
        PhieuMuon phieuMuon = phieuMuonRepository.findById(maPhieuMuon)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phieu muon"));

        List<MuonSachResponse.ChiTietMuonResponse> chiTiet = chiTietPhieuMuonRepository
                .findByMaPhieuMuon(maPhieuMuon)
                .stream()
                .map(ct -> new MuonSachResponse.ChiTietMuonResponse(
                        ct.getMaChiTietMuon(),
                        ct.getMaCuonSach(),
                        ct.getMaQuyDinhMuon(),
                        ct.getNgayMuon(),
                        ct.getHanTra(),
                        ct.getTrangThai()
                ))
                .toList();

        return new MuonSachResponse(
                phieuMuon.getMaPhieuMuon(),
                phieuMuon.getMaDocGia(),
                phieuMuon.getMaNhanVienLap(),
                phieuMuon.getMaChiNhanh(),
                phieuMuon.getMaPhienBanQuyDinh(),
                phieuMuon.getNgayMuon(),
                phieuMuon.getTrangThai(),
                phieuMuon.getGhiChu(),
                phieuMuon.getIdempotencyKey(),
                chiTiet,
                buildPrintData(phieuMuon, chiTiet)
        );
    }

    public List<MuonSachResponse.ChiTietMuonResponse> getCurrentLoansByReader(String maDocGia) {
        String sql = """
                SELECT
                    ctm.MaChiTietMuon,
                    ctm.MaCuonSach,
                    ctm.MaQuyDinhMuon,
                    ctm.NgayMuon,
                    ctm.HanTra,
                    ctm.TrangThai
                FROM PHIEUMUON pm
                INNER JOIN CHITIETPHIEUMUON ctm
                    ON pm.MaPhieuMuon = ctm.MaPhieuMuon
                WHERE pm.MaDocGia = ?
                  AND ctm.TrangThai IN (N'Dang muon', N'Äang mÆ°á»£n')
                ORDER BY ctm.HanTra ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new MuonSachResponse.ChiTietMuonResponse(
                        rs.getString("MaChiTietMuon"),
                        rs.getString("MaCuonSach"),
                        rs.getString("MaQuyDinhMuon"),
                        rs.getTimestamp("NgayMuon").toLocalDateTime(),
                        rs.getTimestamp("HanTra").toLocalDateTime(),
                        rs.getString("TrangThai")
                ),
                maDocGia
        );
    }

    private LoanCommand normalizeRequest(MuonSachRequest request, String requestedIdempotencyKey) {
        String maDocGia = firstText(request.getReaderId(), request.getMaDocGia());
        String maChiNhanh = firstText(request.getBranchId(), request.getMaChiNhanh());
        List<String> rawCopies = request.getCopyIds() != null ? request.getCopyIds() : request.getMaCuonSachs();
        List<String> maCuonSachs = rawCopies == null ? List.of() : rawCopies.stream()
                .filter(this::hasText)
                .map(String::trim)
                .toList();
        String ghiChu = firstText(request.getNote(), request.getGhiChu());
        String idempotencyKey = normalizeIdempotencyKey(requestedIdempotencyKey);

        if (!hasText(maDocGia)) {
            throw new BusinessException("Ma doc gia khong duoc de trong");
        }
        if (!hasText(maChiNhanh)) {
            throw new BusinessException("Ma chi nhanh khong duoc de trong");
        }
        if (maCuonSachs.isEmpty()) {
            throw new BusinessException("Phieu muon phai co it nhat mot cuon sach");
        }
        if (hasText(request.getMaPhieuMuon()) && request.getMaPhieuMuon().trim().length() > 30) {
            throw new BusinessException("Ma phieu muon toi da 30 ky tu");
        }

        Set<String> uniqueBookCopies = new HashSet<>(maCuonSachs);
        if (uniqueBookCopies.size() != maCuonSachs.size()) {
            throw new BusinessException("Danh sach cuon sach muon bi trung");
        }

        return new LoanCommand(maDocGia.trim(), maChiNhanh.trim(), maCuonSachs, ghiChu, idempotencyKey);
    }

    private String normalizeIdempotencyKey(String requestedKey) {
        if (!hasText(requestedKey)) {
            throw new BusinessException("Idempotency-Key khong duoc de trong khi tao phieu muon");
        }
        String key = requestedKey.trim();
        if (key.length() > 100) {
            throw new BusinessException("Idempotency-Key toi da 100 ky tu");
        }
        return key;
    }

    private MuonSachResponse findIdempotentResponse(String idempotencyKey, String requestFingerprint) {
        return phieuMuonRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    if (!requestFingerprint.equals(existing.getRequestFingerprint())) {
                        throw new BusinessException("Idempotency-Key da duoc dung cho mot yeu cau muon sach khac");
                    }
                    return getById(existing.getMaPhieuMuon());
                })
                .orElse(null);
    }

    private void validateReaderCanBorrow(DocGia docGia, LocalDateTime ngayMuon) {
        if (!READER_STATUS_ACTIVE.equals(docGia.getTrangThai())
                && !"Hoạt động".equals(docGia.getTrangThai())
                && !"Hoat dong".equals(docGia.getTrangThai())) {
            throw new BusinessException("Doc gia khong o trang thai hoat dong");
        }

        if (docGia.getNgayHetHanThe().isBefore(ngayMuon.toLocalDate())) {
            throw new BusinessException("The doc gia da het han");
        }

        Integer borrowingLocks = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM DOCGIA_KHOA
                WHERE MaDocGia = ? AND PhamVi = 'BORROWING' AND MoKhoaLuc IS NULL
                  AND (KhoaDen IS NULL OR KhoaDen >= CAST(? AS DATE))
                """, Integer.class, docGia.getMaDocGia(), ngayMuon);
        if (borrowingLocks != null && borrowingLocks > 0) {
            throw new BusinessException("Doc gia dang bi khoa quyen muon");
        }

        BigDecimal tongNo = jdbcTemplate.queryForObject(
                """
                SELECT CAST(COALESCE(SUM(SoTienConLai), 0) AS DECIMAL(18,2))
                FROM KHOANNO
                WHERE MaDocGia = ?
                  AND TrangThai <> N'ÄĂ£ thanh toĂ¡n'
                """,
                BigDecimal.class,
                docGia.getMaDocGia()
        );

        if (tongNo != null && tongNo.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Doc gia dang co no, khong duoc muon sach");
        }

        Integer soSachQuaHan = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM PHIEUMUON pm
                INNER JOIN CHITIETPHIEUMUON ctm
                    ON pm.MaPhieuMuon = ctm.MaPhieuMuon
                WHERE pm.MaDocGia = ?
                  AND ctm.TrangThai IN (N'Dang muon', N'Äang mÆ°á»£n')
                  AND ctm.HanTra < ?
                """,
                Integer.class,
                docGia.getMaDocGia(),
                ngayMuon
        );

        if (soSachQuaHan != null && soSachQuaHan > 0) {
            throw new BusinessException("Doc gia co sach qua han chua tra");
        }
    }

    private void validateBookCopyCanBorrow(
            CuonSach cuonSach,
            String maChiNhanh,
            String maDocGia,
            LocalDateTime now
    ) {
        if (!maChiNhanh.equals(cuonSach.getMaChiNhanh())) {
            throw new BusinessException("Cuon sach " + cuonSach.getMaCuonSach() + " khong thuoc chi nhanh da chon");
        }

        if (TT_SANCO.equals(cuonSach.getMaTrangThai())) {
            return;
        }

        if (TT_DANGDATTRUOC.equals(cuonSach.getMaTrangThai()) && hasValidOwnedReservation(cuonSach.getMaCuonSach(), maDocGia, now)) {
            return;
        }

        throw new BusinessException("Cuon sach " + cuonSach.getMaCuonSach() + " khong o trang thai co the muon");
    }

    private boolean hasValidOwnedReservation(String maCuonSach, String maDocGia, LocalDateTime now) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM PHIEUDATTRUOC WITH (UPDLOCK, ROWLOCK)
                WHERE MaCuonSachDuocGiu = ?
                  AND MaDocGia = ?
                  AND TrangThai = N'ÄĂ£ giá»¯ chá»—'
                  AND (NgayHetHanGiuCho IS NULL OR NgayHetHanGiuCho >= ?)
                """,
                Integer.class,
                maCuonSach,
                maDocGia,
                now
        );
        return count != null && count > 0;
    }

    private void closeReservationIfOwned(String maCuonSach, String maDocGia, String maPhieuMuon) {
        jdbcTemplate.update(
                """
                UPDATE PHIEUDATTRUOC
                SET TrangThai = N'ÄĂ£ mÆ°á»£n',
                    GhiChu = CONCAT(COALESCE(GhiChu, N''), N' | Chuyen thanh phieu muon ', ?)
                WHERE MaPhieuDatTruoc IN (
                    SELECT MaPhieuDatTruoc
                    FROM PHIEUDATTRUOC WITH (UPDLOCK, ROWLOCK)
                    WHERE MaCuonSachDuocGiu = ?
                      AND MaDocGia = ?
                      AND TrangThai = N'ÄĂ£ giá»¯ chá»—'
                      AND (NgayHetHanGiuCho IS NULL OR NgayHetHanGiuCho >= SYSDATETIME())
                )
                """,
                maPhieuMuon,
                maCuonSach,
                maDocGia
        );
    }

    private String getCurrentPolicyVersion() {
        List<String> result = jdbcTemplate.query(
                """
                SELECT TOP 1 MaPhienBan
                FROM PHIENBANQUYDINH
                WHERE TrangThai = N'Äang Ă¡p dá»¥ng'
                ORDER BY NgayApDung DESC
                """,
                (rs, rowNum) -> rs.getString("MaPhienBan")
        );

        if (result.isEmpty()) {
            throw new BusinessException("Chua co phien ban quy dinh dang ap dung");
        }
        return result.get(0);
    }

    private String getCurrentMembershipPlan(String maDocGia, LocalDate ngayMuon) {
        List<String> result = jdbcTemplate.query(
                """
                SELECT TOP 1 MaGoiThanhVien
                FROM LICHSUGOITHANHVIEN
                WHERE MaDocGia = ?
                  AND TrangThai = N'Äang sá»­ dá»¥ng'
                  AND NgayBatDau <= ?
                  AND NgayKetThuc >= ?
                ORDER BY NgayBatDau DESC
                """,
                (rs, rowNum) -> rs.getString("MaGoiThanhVien"),
                maDocGia,
                ngayMuon,
                ngayMuon
        );

        if (result.isEmpty()) {
            throw new BusinessException("Doc gia chua co goi thanh vien dang su dung");
        }
        return result.get(0);
    }

    private int countCurrentLoans(String maDocGia) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM PHIEUMUON pm
                INNER JOIN CHITIETPHIEUMUON ctm
                    ON pm.MaPhieuMuon = ctm.MaPhieuMuon
                WHERE pm.MaDocGia = ?
                  AND ctm.TrangThai IN (N'Dang muon', N'Äang mÆ°á»£n')
                """,
                Integer.class,
                maDocGia
        );
        return count == null ? 0 : count;
    }

    private int getBorrowLimit(String maPhienBan, String maGoiThanhVien) {
        List<Integer> result = jdbcTemplate.query(
                """
                SELECT SoSachMuonToiDa
                FROM QUYDINHGOI
                WHERE MaPhienBan = ?
                  AND MaGoiThanhVien = ?
                """,
                (rs, rowNum) -> rs.getInt("SoSachMuonToiDa"),
                maPhienBan,
                maGoiThanhVien
        );

        if (result.isEmpty()) {
            throw new BusinessException("Chua co quy dinh so sach muon toi da cho goi " + maGoiThanhVien);
        }
        return result.get(0);
    }

    private BorrowRule getBorrowRule(String maPhienBan, String maGoiThanhVien, String maCuonSach) {
        String sql = """
                SELECT TOP 1
                    qdm.MaQuyDinhMuon,
                    qdm.SoNgayMuon
                FROM CUONSACH cs
                INNER JOIN DAUSACH_THELOAI dstl
                    ON cs.MaDauSach = dstl.MaDauSach
                INNER JOIN QUYDINHMUON_THELOAI qdm
                    ON dstl.MaTheLoai = qdm.MaTheLoai
                WHERE cs.MaCuonSach = ?
                  AND qdm.MaPhienBan = ?
                  AND qdm.MaGoiThanhVien = ?
                ORDER BY qdm.SoNgayMuon ASC, qdm.MaQuyDinhMuon ASC
                """;

        List<BorrowRule> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new BorrowRule(rs.getString("MaQuyDinhMuon"), rs.getInt("SoNgayMuon")),
                maCuonSach,
                maPhienBan,
                maGoiThanhVien
        );

        if (result.isEmpty()) {
            throw new BusinessException("Chua co quy dinh muon phu hop cho cuon sach " + maCuonSach);
        }
        return result.get(0);
    }

    private LocalDateTime calculateDueDate(LocalDateTime ngayMuon, int soNgayMuon, DocGia docGia) {
        LocalDateTime hanTraTheoQuyDinh = ngayMuon.plusDays(soNgayMuon);
        LocalDateTime hanToiDaTheoThe = docGia.getNgayHetHanThe().minusDays(1).atTime(23, 59, 59);
        LocalDateTime hanTraThucTe = hanTraTheoQuyDinh.isBefore(hanToiDaTheoThe)
                ? hanTraTheoQuyDinh
                : hanToiDaTheoThe;

        if (!hanTraThucTe.isAfter(ngayMuon)) {
            throw new BusinessException("The doc gia sap het han, khong the muon sach");
        }
        return hanTraThucTe;
    }

    private String buildMaChiTietMuon(String maPhieuMuon, int index) {
        String maChiTietMuon = "CTM_" + maPhieuMuon + "_" + String.format("%02d", index);
        if (maChiTietMuon.length() > 30) {
            throw new BusinessException("Ma chi tiet muon vuot qua 30 ky tu");
        }
        return maChiTietMuon;
    }

    private String generateLoanId() {
        return "PM" + LOAN_ID_TIME_FORMAT.format(LocalDateTime.now())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
    }

    private String buildRequestFingerprint(LoanCommand command, AuthUser authenticatedUser, String staffId) {
        StringBuilder canonical = new StringBuilder();
        appendFingerprintPart(canonical, authenticatedUser.getMaTaiKhoan());
        appendFingerprintPart(canonical, staffId);
        appendFingerprintPart(canonical, command.maDocGia());
        appendFingerprintPart(canonical, command.maChiNhanh());
        appendFingerprintPart(canonical, emptyToNull(command.ghiChu()));
        command.maCuonSachs().stream()
                .sorted(Comparator.naturalOrder())
                .forEach(copyId -> appendFingerprintPart(canonical, copyId));

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Runtime does not support SHA-256", ex);
        }
    }

    private void appendFingerprintPart(StringBuilder target, String value) {
        String normalized = value == null ? "" : value;
        target.append(normalized.length()).append(':').append(normalized).append('|');
    }

    private MuonSachResponse.PrintData buildPrintData(
            PhieuMuon phieuMuon,
            List<MuonSachResponse.ChiTietMuonResponse> chiTiet
    ) {
        return new MuonSachResponse.PrintData(
                phieuMuon.getMaPhieuMuon(),
                phieuMuon.getMaDocGia(),
                phieuMuon.getMaNhanVienLap(),
                phieuMuon.getMaChiNhanh(),
                phieuMuon.getNgayMuon(),
                phieuMuon.getGhiChu(),
                chiTiet
        );
    }

    private boolean existsById(String tableName, String idColumn, String value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?",
                Integer.class,
                value
        );
        return count != null && count > 0;
    }

    private String firstText(String first, String second) {
        if (hasText(first)) return first;
        if (hasText(second)) return second;
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String emptyToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private record LoanCommand(
            String maDocGia,
            String maChiNhanh,
            List<String> maCuonSachs,
            String ghiChu,
            String idempotencyKey
    ) {
    }

    private record BorrowRule(String maQuyDinhMuon, int soNgayMuon) {
    }
}

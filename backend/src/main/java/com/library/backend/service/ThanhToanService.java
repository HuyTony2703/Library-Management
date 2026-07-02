package com.library.backend.service;

import com.library.backend.dto.DebtorSearchResponse;
import com.library.backend.dto.KhoanNoResponse;
import com.library.backend.dto.PaymentPreviewResponse;
import com.library.backend.dto.PaymentReversalRequest;
import com.library.backend.dto.PaymentReversalResponse;
import com.library.backend.dto.PhieuThuRequest;
import com.library.backend.dto.PhieuThuResponse;
import com.library.backend.dto.ReaderDebtContextResponse;
import com.library.backend.dto.StaffContextResponse;
import com.library.backend.entity.ChiTietPhieuThuNo;
import com.library.backend.entity.KhoanNo;
import com.library.backend.entity.PhieuThu;
import com.library.backend.entity.PhieuThuReversal;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.repository.ChiTietPhieuThuNoRepository;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.KhoanNoRepository;
import com.library.backend.repository.PhieuThuRepository;
import com.library.backend.repository.PhieuThuReversalRepository;
import com.library.backend.repository.PhuongThucThanhToanRepository;
import com.library.backend.security.AuthUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ThanhToanService {

    private static final String STATUS_PAID = "\u0110\u00e3 thanh to\u00e1n";
    private static final String STATUS_PARTIAL = "Thanh to\u00e1n m\u1ed9t ph\u1ea7n";
    private static final String STATUS_UNPAID = "Ch\u01b0a thanh to\u00e1n";
    private static final String STATUS_SUCCESS = "Th\u00e0nh c\u00f4ng";
    private static final String RECEIPT_TYPE_FINE = "Thu ti\u1ec1n ph\u1ea1t";
    private static final DateTimeFormatter RECEIPT_ID_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RECEIPT_RANDOM = new SecureRandom();

    private final KhoanNoRepository khoanNoRepository;
    private final PhieuThuRepository phieuThuRepository;
    private final PhieuThuReversalRepository phieuThuReversalRepository;
    private final ChiTietPhieuThuNoRepository chiTietPhieuThuNoRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final DocGiaRepository docGiaRepository;
    private final ActivityLogService activityLogService;
    private final StaffContextService staffContextService;
    private final JdbcTemplate jdbcTemplate;

    public ThanhToanService(
            KhoanNoRepository khoanNoRepository,
            PhieuThuRepository phieuThuRepository,
            PhieuThuReversalRepository phieuThuReversalRepository,
            ChiTietPhieuThuNoRepository chiTietPhieuThuNoRepository,
            PhuongThucThanhToanRepository phuongThucThanhToanRepository,
            DocGiaRepository docGiaRepository,
            ActivityLogService activityLogService,
            StaffContextService staffContextService,
            JdbcTemplate jdbcTemplate
    ) {
        this.khoanNoRepository = khoanNoRepository;
        this.phieuThuRepository = phieuThuRepository;
        this.phieuThuReversalRepository = phieuThuReversalRepository;
        this.chiTietPhieuThuNoRepository = chiTietPhieuThuNoRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.docGiaRepository = docGiaRepository;
        this.activityLogService = activityLogService;
        this.staffContextService = staffContextService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DebtorSearchResponse> searchDebtors(String query, boolean outstandingOnly, int limit) {
        throw new AccessDeniedException("Debt search requires authenticated staff context");
    }

    public List<DebtorSearchResponse> searchDebtors(
            String query,
            boolean outstandingOnly,
            int limit,
            AuthUser authenticatedUser
    ) {
        validateStaffContext(authenticatedUser);
        int safeLimit = Math.max(1, Math.min(limit, 30));
        String trimmedQuery = query == null ? "" : query.trim();
        String likeQuery = "%" + trimmedQuery + "%";
        String havingClause = outstandingOnly
                ? "HAVING COALESCE(SUM(kn.SoTienPhatSinh - kn.SoTienDaThanhToan), 0) > 0"
                : "";
        String sql = """
                SELECT TOP (?) dg.MaDocGia,
                       dg.HoTen,
                       dg.Email,
                       dg.SoDienThoai,
                       COALESCE(SUM(kn.SoTienPhatSinh - kn.SoTienDaThanhToan), 0) AS TongNoConLai,
                       COUNT(kn.MaKhoanNo) AS SoKhoanNoConLai
                FROM DOCGIA dg
                LEFT JOIN KHOANNO kn
                  ON kn.MaDocGia = dg.MaDocGia
                 AND kn.TrangThai <> N'\u0110\u00e3 thanh to\u00e1n'
                WHERE (? = ''
                       OR dg.MaDocGia LIKE ?
                       OR dg.HoTen LIKE ?
                       OR dg.Email LIKE ?
                       OR dg.SoDienThoai LIKE ?)
                GROUP BY dg.MaDocGia, dg.HoTen, dg.Email, dg.SoDienThoai
                %s
                ORDER BY TongNoConLai DESC, dg.MaDocGia ASC
                """.formatted(havingClause);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new DebtorSearchResponse(
                        rs.getString("MaDocGia"),
                        rs.getString("HoTen"),
                        rs.getString("Email"),
                        rs.getString("SoDienThoai"),
                        rs.getBigDecimal("TongNoConLai"),
                        rs.getLong("SoKhoanNoConLai")
                ),
                safeLimit,
                trimmedQuery,
                likeQuery,
                likeQuery,
                likeQuery,
                likeQuery
        );
    }

    public ReaderDebtContextResponse getReaderDebtContext(String maDocGia) {
        throw new AccessDeniedException("Reader debt context requires authenticated staff context");
    }

    public ReaderDebtContextResponse getReaderDebtContext(String maDocGia, AuthUser authenticatedUser) {
        validateStaffContext(authenticatedUser);
        if (!docGiaRepository.existsById(maDocGia)) {
            throw new ResourceNotFoundException("Doc gia khong ton tai");
        }

        String sql = """
                SELECT dg.MaDocGia,
                       dg.HoTen,
                       COALESCE(SUM(kn.SoTienPhatSinh), 0) AS TongPhatSinh,
                       COALESCE(SUM(kn.SoTienDaThanhToan), 0) AS TongDaThanhToan,
                       COALESCE(SUM(kn.SoTienPhatSinh - kn.SoTienDaThanhToan), 0) AS TongNoConLai,
                       COUNT(kn.MaKhoanNo) AS SoKhoanNo,
                       SUM(CASE WHEN kn.TrangThai <> N'\u0110\u00e3 thanh to\u00e1n' THEN 1 ELSE 0 END) AS SoKhoanNoConLai,
                       MIN(CASE WHEN kn.TrangThai <> N'\u0110\u00e3 thanh to\u00e1n' THEN kn.NgayPhatSinh ELSE NULL END) AS KhoanNoCuNhat
                FROM DOCGIA dg
                LEFT JOIN KHOANNO kn ON kn.MaDocGia = dg.MaDocGia
                WHERE dg.MaDocGia = ?
                GROUP BY dg.MaDocGia, dg.HoTen
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    BigDecimal outstanding = defaultMoney(rs.getBigDecimal("TongNoConLai"));
                    java.sql.Timestamp oldest = rs.getTimestamp("KhoanNoCuNhat");
                    return new ReaderDebtContextResponse(
                            rs.getString("MaDocGia"),
                            rs.getString("HoTen"),
                            defaultMoney(rs.getBigDecimal("TongPhatSinh")),
                            defaultMoney(rs.getBigDecimal("TongDaThanhToan")),
                            outstanding,
                            rs.getLong("SoKhoanNo"),
                            rs.getLong("SoKhoanNoConLai"),
                            oldest == null ? null : oldest.toLocalDateTime(),
                            outstanding.compareTo(BigDecimal.ZERO) > 0
                    );
                },
                maDocGia
        );
    }

    public List<KhoanNoResponse> getDebtsByReader(String maDocGia) {
        throw new AccessDeniedException("Reader debt list requires authenticated staff context");
    }

    public List<KhoanNoResponse> getDebtsByReader(String maDocGia, AuthUser authenticatedUser) {
        validateStaffContext(authenticatedUser);
        if (!docGiaRepository.existsById(maDocGia)) {
            throw new ResourceNotFoundException("Doc gia khong ton tai");
        }

        return khoanNoRepository.findByMaDocGiaOrderByNgayPhatSinhDesc(maDocGia)
                .stream()
                .map(this::toKhoanNoResponse)
                .toList();
    }

    public PaymentPreviewResponse previewPayment(
            PhieuThuRequest request,
            AuthUser authenticatedUser
    ) {
        validateStaffContext(authenticatedUser);
        validatePaymentRequestBasics(request);
        validateReaderAndPaymentMethod(request);
        List<KhoanNo> debts = loadPreviewDebts(request);
        List<PaymentApplyItem> applyItems = buildApplyItems(request, debts);
        return toPaymentPreviewResponse(request, applyItems);
    }

    @Transactional
    public PhieuThuResponse createPayment(
            PhieuThuRequest request,
            AuthUser authenticatedUser,
            String requestedIdempotencyKey
    ) {
        validatePaymentRequestBasics(request);

        StaffContextResponse staffContext = validateStaffContext(authenticatedUser);
        String authenticatedStaffId = staffContext.staffId();
        String branchId = staffContext.defaultBranch().id();

        if (hasText(request.getMaNhanVienThu())
                && !request.getMaNhanVienThu().equals(authenticatedStaffId)) {
            throw new AccessDeniedException("Ma nhan vien thu khong khop tai khoan dang nhap");
        }

        String idempotencyKey = resolveIdempotencyKey(requestedIdempotencyKey, request.getMaPhieuThu());
        String externalTransactionId = resolveExternalTransactionId(request);
        String requestFingerprint = buildRequestFingerprint(request, authenticatedUser, authenticatedStaffId, branchId);

        PhieuThuResponse idempotentResponse = findIdempotentResponse(idempotencyKey, requestFingerprint);
        if (idempotentResponse != null) {
            return idempotentResponse;
        }

        validateReaderAndPaymentMethod(request);
        rejectExternalTransactionCollision(externalTransactionId);

        List<KhoanNo> lockedDebts = lockRequestedDebts(request);

        idempotentResponse = findIdempotentResponse(idempotencyKey, requestFingerprint);
        if (idempotentResponse != null) {
            return idempotentResponse;
        }
        rejectExternalTransactionCollision(externalTransactionId);

        List<PaymentApplyItem> applyItems = buildApplyItems(request, lockedDebts);
        BigDecimal tongApDung = totalApplied(applyItems);
        if (tongApDung.compareTo(request.getSoTienThu()) != 0) {
            throw new BusinessException("Tong tien ap dung vao khoan no phai bang so tien thu");
        }

        String receiptId = generateReceiptId();
        PhieuThu phieuThu = new PhieuThu();
        phieuThu.setMaPhieuThu(receiptId);
        phieuThu.setMaDocGia(request.getMaDocGia());
        phieuThu.setMaNhanVienThu(authenticatedStaffId);
        phieuThu.setMaPhuongThuc(request.getMaPhuongThuc());
        phieuThu.setLoaiThu(RECEIPT_TYPE_FINE);
        phieuThu.setSoTienThu(request.getSoTienThu());
        phieuThu.setNgayThu(LocalDateTime.now());
        phieuThu.setMaGiaoDichNgoai(externalTransactionId);
        phieuThu.setTienKhachDua(request.getCashReceived());
        phieuThu.setTienThua(calculateChangeAmount(request));
        phieuThu.setTrangThai(STATUS_SUCCESS);
        phieuThu.setGhiChu(request.getGhiChu());
        phieuThu.setIdempotencyKey(idempotencyKey);
        phieuThu.setRequestFingerprint(requestFingerprint);

        phieuThuRepository.save(phieuThu);

        for (int i = 0; i < applyItems.size(); i++) {
            PaymentApplyItem item = applyItems.get(i);
            KhoanNo khoanNo = item.khoanNo();
            BigDecimal daThanhToanTruoc = khoanNo.getSoTienDaThanhToan();
            BigDecimal conLaiTruoc = getConLai(khoanNo);
            String trangThaiTruoc = khoanNo.getTrangThai();
            BigDecimal daThanhToanMoi = daThanhToanTruoc.add(item.soTienApDung());

            if (daThanhToanMoi.compareTo(khoanNo.getSoTienPhatSinh()) > 0) {
                throw new BusinessException("So tien thanh toan vuot qua khoan no: " + khoanNo.getMaKhoanNo());
            }

            khoanNo.setSoTienDaThanhToan(daThanhToanMoi);
            khoanNo.setTrangThai(statusAfter(getConLai(khoanNo)));
            khoanNoRepository.save(khoanNo);

            ChiTietPhieuThuNo chiTiet = new ChiTietPhieuThuNo();
            chiTiet.setMaChiTietPhieuThu(buildMaChiTietPhieuThu(receiptId, i + 1));
            chiTiet.setMaPhieuThu(receiptId);
            chiTiet.setMaKhoanNo(khoanNo.getMaKhoanNo());
            chiTiet.setSoTienApDung(item.soTienApDung());
            chiTietPhieuThuNoRepository.save(chiTiet);

            activityLogService.logAsAccount(
                    authenticatedUser.getMaTaiKhoan(),
                    "Phan bo phieu thu vao khoan no",
                    "KHOANNO",
                    khoanNo.getMaKhoanNo(),
                    "Phieu thu " + receiptId
                            + ", nhan vien " + authenticatedStaffId
                            + ", chi nhanh " + branchId
                            + ", ap dung " + item.soTienApDung()
                            + ", truoc {da thanh toan=" + daThanhToanTruoc
                            + ", con lai=" + conLaiTruoc
                            + ", trang thai=" + trangThaiTruoc
                            + "}, sau {da thanh toan=" + daThanhToanMoi
                            + ", con lai=" + getConLai(khoanNo)
                            + ", trang thai=" + khoanNo.getTrangThai() + "}"
            );
        }

        PhieuThuResponse response = getPaymentByIdInternal(receiptId);

        activityLogService.logAsAccount(
                authenticatedUser.getMaTaiKhoan(),
                "Thu tien",
                "PHIEUTHU",
                receiptId,
                "Nhan vien " + authenticatedStaffId
                        + " thu tien doc gia " + request.getMaDocGia()
                        + " tai chi nhanh " + branchId
                        + ", so tien: " + request.getSoTienThu()
                        + ", phuong thuc: " + request.getMaPhuongThuc()
                        + ", idempotency key: " + idempotencyKey
        );

        return response;
    }

    public PhieuThuResponse getPaymentById(String maPhieuThu) {
        throw new AccessDeniedException("Payment lookup requires authenticated staff context");
    }

    public PhieuThuResponse getPaymentById(String maPhieuThu, AuthUser authenticatedUser) {
        validateStaffContext(authenticatedUser);
        return getPaymentByIdInternal(maPhieuThu);
    }

    private PhieuThuResponse getPaymentByIdInternal(String maPhieuThu) {
        PhieuThu phieuThu = phieuThuRepository.findById(maPhieuThu)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phieu thu"));

        return toPhieuThuResponse(phieuThu);
    }

    public List<PhieuThuResponse> getPaymentsByReader(String maDocGia) {
        throw new AccessDeniedException("Reader payment history requires authenticated staff context");
    }

    public List<PhieuThuResponse> getPaymentsByReader(String maDocGia, AuthUser authenticatedUser) {
        validateStaffContext(authenticatedUser);
        if (!docGiaRepository.existsById(maDocGia)) {
            throw new ResourceNotFoundException("Doc gia khong ton tai");
        }

        return phieuThuRepository.findByMaDocGiaOrderByNgayThuDesc(maDocGia)
                .stream()
                .map(this::toPhieuThuResponse)
                .toList();
    }

    @Transactional
    public PaymentReversalResponse reversePayment(
            String paymentId,
            PaymentReversalRequest request,
            AuthUser authenticatedUser
    ) {
        if (!hasText(request.reason())) {
            throw new BusinessException("Ly do dao phieu thu khong duoc de trong");
        }
        if (!hasText(request.approvalReference())) {
            throw new BusinessException("Approval reference khong duoc de trong");
        }

        StaffContextResponse staffContext = validateStaffContext(authenticatedUser);
        String staffId = staffContext.staffId();
        String branchId = staffContext.defaultBranch().id();

        PhieuThu original = phieuThuRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phieu thu"));

        if (!STATUS_SUCCESS.equals(original.getTrangThai())) {
            throw new BusinessException("Chi phieu thu thanh cong moi duoc dao");
        }
        if (phieuThuReversalRepository.existsByMaPhieuThuGoc(paymentId)) {
            throw new BusinessException("Phieu thu da duoc dao truoc do");
        }

        List<ChiTietPhieuThuNo> details = chiTietPhieuThuNoRepository.findByMaPhieuThu(paymentId);
        if (details.isEmpty()) {
            throw new BusinessException("Phieu thu khong co chi tiet khoan no de dao");
        }

        List<PaymentReversalResponse.RestoredDebt> restoredDebts = new ArrayList<>();
        BigDecimal restoredAmount = BigDecimal.ZERO;

        for (ChiTietPhieuThuNo detail : details) {
            KhoanNo debt = khoanNoRepository.findByIdForUpdate(detail.getMaKhoanNo())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay khoan no: " + detail.getMaKhoanNo()));

            BigDecimal paidBefore = debt.getSoTienDaThanhToan();
            BigDecimal remainingBefore = getConLai(debt);
            String statusBefore = debt.getTrangThai();
            BigDecimal restored = detail.getSoTienApDung();
            BigDecimal paidAfter = paidBefore.subtract(restored);

            if (paidAfter.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Khoan no khong du so tien da thanh toan de dao: " + debt.getMaKhoanNo());
            }

            debt.setSoTienDaThanhToan(paidAfter);
            debt.setTrangThai(statusFromPaidAmount(debt));
            khoanNoRepository.save(debt);

            BigDecimal remainingAfter = getConLai(debt);
            restoredAmount = restoredAmount.add(restored);
            restoredDebts.add(new PaymentReversalResponse.RestoredDebt(
                    debt.getMaKhoanNo(),
                    restored,
                    paidBefore,
                    paidAfter,
                    remainingBefore,
                    remainingAfter,
                    statusBefore,
                    debt.getTrangThai()
            ));

            activityLogService.logAsAccount(
                    authenticatedUser.getMaTaiKhoan(),
                    "Dao phieu thu - hoan khoan no",
                    "KHOANNO",
                    debt.getMaKhoanNo(),
                    "Phieu thu goc " + paymentId
                            + ", nhan vien dao " + staffId
                            + ", chi nhanh " + branchId
                            + ", approval " + request.approvalReference()
                            + ", ly do " + request.reason()
                            + ", truoc {da thanh toan=" + paidBefore
                            + ", con lai=" + remainingBefore
                            + ", trang thai=" + statusBefore
                            + "}, sau {da thanh toan=" + paidAfter
                            + ", con lai=" + remainingAfter
                            + ", trang thai=" + debt.getTrangThai() + "}"
            );
        }

        String reversalId = generateReversalId();
        PhieuThuReversal reversal = new PhieuThuReversal();
        reversal.setMaDaoPhieuThu(reversalId);
        reversal.setMaPhieuThuGoc(original.getMaPhieuThu());
        reversal.setMaDocGia(original.getMaDocGia());
        reversal.setMaNhanVienDao(staffId);
        reversal.setSoTienHoan(restoredAmount);
        reversal.setLyDo(request.reason().trim());
        reversal.setApprovalReference(request.approvalReference().trim());
        reversal.setNgayDao(LocalDateTime.now());
        phieuThuReversalRepository.save(reversal);

        activityLogService.logAsAccount(
                authenticatedUser.getMaTaiKhoan(),
                "Dao phieu thu",
                "PHIEUTHU",
                original.getMaPhieuThu(),
                "Tao reversal " + reversalId
                        + ", giu nguyen phieu goc"
                        + ", doc gia " + original.getMaDocGia()
                        + ", so tien hoan " + restoredAmount
                        + ", approval " + request.approvalReference()
                        + ", ly do " + request.reason()
        );

        return new PaymentReversalResponse(
                reversalId,
                original.getMaPhieuThu(),
                original.getMaDocGia(),
                staffId,
                reversal.getLyDo(),
                reversal.getApprovalReference(),
                reversal.getNgayDao(),
                restoredAmount,
                restoredDebts
        );
    }

    private StaffContextResponse validateStaffContext(AuthUser authenticatedUser) {
        StaffContextResponse staffContext = staffContextService.getContext(authenticatedUser);
        if (!staffContext.operational() || staffContext.defaultBranch() == null) {
            throw new AccessDeniedException("Tai khoan chua co staff context de thu tien");
        }
        return staffContext;
    }

    private void validatePaymentRequestBasics(PhieuThuRequest request) {
        if (request.getSoTienThu() == null || request.getSoTienThu().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("So tien thu phai lon hon 0");
        }
        BigDecimal changeAmount = calculateChangeAmount(request);
        if (request.getCashReceived() != null && changeAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Tien khach dua khong duoc nho hon so tien thu");
        }
    }

    private void validateReaderAndPaymentMethod(PhieuThuRequest request) {
        if (!docGiaRepository.existsById(request.getMaDocGia())) {
            throw new ResourceNotFoundException("Doc gia khong ton tai");
        }
        if (!phuongThucThanhToanRepository.existsById(request.getMaPhuongThuc())) {
            throw new ResourceNotFoundException("Phuong thuc thanh toan khong ton tai");
        }
    }

    private List<KhoanNo> loadPreviewDebts(PhieuThuRequest request) {
        if (!hasManualAllocations(request)) {
            return khoanNoRepository.findOutstandingByReaderForPreview(request.getMaDocGia(), STATUS_PAID);
        }

        Set<String> usedDebts = new HashSet<>();
        for (PhieuThuRequest.ChiTietThuNoRequest item : request.getChiTietNo()) {
            if (!usedDebts.add(item.getMaKhoanNo())) {
                throw new BusinessException("Khoan no bi lap trong phieu thu: " + item.getMaKhoanNo());
            }
        }

        Map<String, KhoanNo> debtsById = new HashMap<>();
        khoanNoRepository.findAllById(usedDebts).forEach(debt -> debtsById.put(debt.getMaKhoanNo(), debt));
        return usedDebts.stream()
                .sorted()
                .map(debtId -> {
                    KhoanNo debt = debtsById.get(debtId);
                    if (debt == null) {
                        throw new ResourceNotFoundException("Khong tim thay khoan no: " + debtId);
                    }
                    return debt;
                })
                .toList();
    }

    private List<KhoanNo> lockRequestedDebts(PhieuThuRequest request) {
        if (!hasManualAllocations(request)) {
            return khoanNoRepository.findOutstandingByReaderForUpdate(request.getMaDocGia(), STATUS_PAID);
        }

        Set<String> usedDebts = new HashSet<>();
        for (PhieuThuRequest.ChiTietThuNoRequest item : request.getChiTietNo()) {
            if (!usedDebts.add(item.getMaKhoanNo())) {
                throw new BusinessException("Khoan no bi lap trong phieu thu: " + item.getMaKhoanNo());
            }
        }

        return usedDebts.stream()
                .sorted()
                .map(debtId -> khoanNoRepository.findByIdForUpdate(debtId)
                        .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay khoan no: " + debtId)))
                .toList();
    }

    private List<PaymentApplyItem> buildApplyItems(PhieuThuRequest request, List<KhoanNo> debts) {
        return hasManualAllocations(request)
                ? buildManualApplyItems(request, debts)
                : buildAutoApplyItems(request.getSoTienThu(), debts);
    }

    private List<PaymentApplyItem> buildManualApplyItems(PhieuThuRequest request, List<KhoanNo> debts) {
        List<PaymentApplyItem> result = new ArrayList<>();
        Map<String, KhoanNo> debtsById = new HashMap<>();
        debts.forEach(debt -> debtsById.put(debt.getMaKhoanNo(), debt));

        for (PhieuThuRequest.ChiTietThuNoRequest item : request.getChiTietNo()) {
            if (item.getSoTienApDung() == null || item.getSoTienApDung().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("So tien ap dung phai lon hon 0");
            }

            KhoanNo khoanNo = debtsById.get(item.getMaKhoanNo());
            if (khoanNo == null) {
                throw new ResourceNotFoundException("Khong tim thay khoan no: " + item.getMaKhoanNo());
            }
            if (!request.getMaDocGia().equals(khoanNo.getMaDocGia())) {
                throw new BusinessException("Khoan no khong thuoc doc gia nay: " + item.getMaKhoanNo());
            }

            BigDecimal conLai = getConLai(khoanNo);
            if (conLai.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Khoan no da thanh toan het: " + item.getMaKhoanNo());
            }
            if (item.getSoTienApDung().compareTo(conLai) > 0) {
                throw new BusinessException("So tien thu vuot qua khoan no con lai: " + item.getMaKhoanNo());
            }
            result.add(new PaymentApplyItem(khoanNo, item.getSoTienApDung()));
        }

        return result;
    }

    private List<PaymentApplyItem> buildAutoApplyItems(BigDecimal soTienThu, List<KhoanNo> debts) {
        BigDecimal tongNoConLai = debts.stream()
                .map(this::getConLai)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (tongNoConLai.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Doc gia khong co khoan no can thanh toan");
        }
        if (tongNoConLai.compareTo(soTienThu) < 0) {
            throw new BusinessException("So tien thu vuot qua tong no con lai");
        }

        List<PaymentApplyItem> result = new ArrayList<>();
        BigDecimal remaining = soTienThu;
        List<KhoanNo> orderedDebts = debts.stream()
                .sorted(Comparator.comparing(KhoanNo::getNgayPhatSinh).thenComparing(KhoanNo::getMaKhoanNo))
                .toList();

        for (KhoanNo debt : orderedDebts) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal conLai = getConLai(debt);
            if (conLai.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal applyAmount = remaining.min(conLai);
            result.add(new PaymentApplyItem(debt, applyAmount));
            remaining = remaining.subtract(applyAmount);
        }

        return result;
    }

    private PaymentPreviewResponse toPaymentPreviewResponse(
            PhieuThuRequest request,
            List<PaymentApplyItem> applyItems
    ) {
        BigDecimal balanceBefore = applyItems.stream()
                .map(item -> getConLai(item.khoanNo()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal amount = totalApplied(applyItems);
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        List<PaymentPreviewResponse.AllocationPreview> allocations = applyItems.stream()
                .map(item -> {
                    KhoanNo debt = item.khoanNo();
                    BigDecimal remainingBefore = getConLai(debt);
                    BigDecimal remainingAfter = remainingBefore.subtract(item.soTienApDung());
                    return new PaymentPreviewResponse.AllocationPreview(
                            debt.getMaKhoanNo(),
                            debt.getMaLoaiKhoanNo(),
                            debt.getLyDo(),
                            debt.getSoTienPhatSinh(),
                            debt.getSoTienDaThanhToan(),
                            remainingBefore,
                            item.soTienApDung(),
                            remainingAfter,
                            statusAfter(remainingAfter)
                    );
                })
                .toList();

        return new PaymentPreviewResponse(
                request.getMaDocGia(),
                request.getMaPhuongThuc(),
                hasManualAllocations(request) ? "MANUAL" : "AUTO",
                balanceBefore,
                amount,
                balanceAfter,
                request.getCashReceived(),
                calculateChangeAmount(request),
                allocations
        );
    }

    private KhoanNoResponse toKhoanNoResponse(KhoanNo khoanNo) {
        return new KhoanNoResponse(
                khoanNo.getMaKhoanNo(),
                khoanNo.getMaDocGia(),
                khoanNo.getMaLoaiKhoanNo(),
                khoanNo.getMaChiTietTra(),
                khoanNo.getSoTienPhatSinh(),
                khoanNo.getSoTienDaThanhToan(),
                getConLai(khoanNo),
                khoanNo.getNgayPhatSinh(),
                khoanNo.getLyDo(),
                khoanNo.getTrangThai()
        );
    }

    private PhieuThuResponse toPhieuThuResponse(PhieuThu phieuThu) {
        List<PhieuThuResponse.ChiTietPhieuThuNoResponse> chiTiet = chiTietPhieuThuNoRepository
                .findByMaPhieuThu(phieuThu.getMaPhieuThu())
                .stream()
                .map(ct -> new PhieuThuResponse.ChiTietPhieuThuNoResponse(
                        ct.getMaChiTietPhieuThu(),
                        ct.getMaKhoanNo(),
                        ct.getSoTienApDung()
                ))
                .toList();
        PhieuThuResponse.PrintReceiptData printData = new PhieuThuResponse.PrintReceiptData(
                phieuThu.getMaPhieuThu(),
                phieuThu.getMaDocGia(),
                phieuThu.getMaNhanVienThu(),
                phieuThu.getMaPhuongThuc(),
                phieuThu.getSoTienThu(),
                phieuThu.getTienKhachDua(),
                phieuThu.getTienThua(),
                phieuThu.getNgayThu(),
                chiTiet.stream()
                        .map(item -> new PhieuThuResponse.PrintReceiptLine(
                                item.getMaKhoanNo(),
                                item.getSoTienApDung()
                        ))
                        .toList()
        );

        return new PhieuThuResponse(
                phieuThu.getMaPhieuThu(),
                phieuThu.getMaDocGia(),
                phieuThu.getMaNhanVienThu(),
                phieuThu.getMaPhuongThuc(),
                phieuThu.getLoaiThu(),
                phieuThu.getSoTienThu(),
                phieuThu.getTienKhachDua(),
                phieuThu.getTienThua(),
                phieuThu.getMaGiaoDichNgoai(),
                phieuThu.getGhiChu(),
                phieuThu.getNgayThu(),
                phieuThu.getTrangThai(),
                chiTiet,
                printData
        );
    }

    private BigDecimal getConLai(KhoanNo khoanNo) {
        return khoanNo.getSoTienPhatSinh().subtract(khoanNo.getSoTienDaThanhToan());
    }

    private BigDecimal totalApplied(List<PaymentApplyItem> applyItems) {
        return applyItems.stream()
                .map(PaymentApplyItem::soTienApDung)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String statusAfter(BigDecimal remainingAfter) {
        return remainingAfter.compareTo(BigDecimal.ZERO) == 0 ? STATUS_PAID : STATUS_PARTIAL;
    }

    private String statusFromPaidAmount(KhoanNo debt) {
        if (debt.getSoTienDaThanhToan().compareTo(BigDecimal.ZERO) == 0) {
            return STATUS_UNPAID;
        }
        if (debt.getSoTienDaThanhToan().compareTo(debt.getSoTienPhatSinh()) == 0) {
            return STATUS_PAID;
        }
        return STATUS_PARTIAL;
    }

    private BigDecimal calculateChangeAmount(PhieuThuRequest request) {
        if (request.getCashReceived() == null) {
            return null;
        }
        return request.getCashReceived().subtract(request.getSoTienThu());
    }

    private String resolveExternalTransactionId(PhieuThuRequest request) {
        String externalId = hasText(request.getExternalTransactionId())
                ? request.getExternalTransactionId()
                : request.getMaGiaoDichNgoai();
        return emptyToNull(externalId);
    }

    private void rejectExternalTransactionCollision(String externalTransactionId) {
        if (hasText(externalTransactionId) && phieuThuRepository.existsByMaGiaoDichNgoai(externalTransactionId)) {
            throw new BusinessException("Ma giao dich ngoai da ton tai");
        }
    }

    private String buildMaChiTietPhieuThu(String maPhieuThu, int index) {
        String maChiTietPhieuThu = "CTPT_" + maPhieuThu + "_" + String.format("%02d", index);
        if (maChiTietPhieuThu.length() > 30) {
            throw new BusinessException("Ma chi tiet phieu thu vuot qua 30 ky tu");
        }
        return maChiTietPhieuThu;
    }

    private String generateReceiptId() {
        for (int i = 0; i < 10; i++) {
            String randomPart = String.format("%04d", RECEIPT_RANDOM.nextInt(10_000));
            String receiptId = "PT" + LocalDateTime.now().format(RECEIPT_ID_TIME_FORMAT) + randomPart;
            if (!phieuThuRepository.existsById(receiptId)) {
                return receiptId;
            }
        }
        throw new BusinessException("Khong the sinh ma phieu thu duy nhat");
    }

    private String generateReversalId() {
        for (int i = 0; i < 10; i++) {
            String randomPart = String.format("%04d", RECEIPT_RANDOM.nextInt(10_000));
            String reversalId = "DPT" + LocalDateTime.now().format(RECEIPT_ID_TIME_FORMAT) + randomPart;
            if (!phieuThuReversalRepository.existsById(reversalId)) {
                return reversalId;
            }
        }
        throw new BusinessException("Khong the sinh ma dao phieu thu duy nhat");
    }

    private PhieuThuResponse findIdempotentResponse(String idempotencyKey, String requestFingerprint) {
        return phieuThuRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    if (!requestFingerprint.equals(existing.getRequestFingerprint())) {
                        throw new BusinessException("Idempotency-Key da duoc dung cho mot yeu cau thu tien khac");
                    }
                    return toPhieuThuResponse(existing);
                })
                .orElse(null);
    }

    private String resolveIdempotencyKey(String requestedKey, String legacyReceiptId) {
        String key = hasText(requestedKey) ? requestedKey.trim() : legacyReceiptId;
        if (!hasText(key)) {
            throw new BusinessException("Idempotency-Key khong duoc de trong");
        }
        if (key.length() > 100) {
            throw new BusinessException("Idempotency-Key toi da 100 ky tu");
        }
        return key;
    }

    private String buildRequestFingerprint(
            PhieuThuRequest request,
            AuthUser authenticatedUser,
            String staffId,
            String branchId
    ) {
        StringBuilder canonical = new StringBuilder();
        appendFingerprintPart(canonical, authenticatedUser.getMaTaiKhoan());
        appendFingerprintPart(canonical, staffId);
        appendFingerprintPart(canonical, branchId);
        appendFingerprintPart(canonical, request.getMaDocGia());
        appendFingerprintPart(canonical, request.getMaPhuongThuc());
        appendFingerprintPart(canonical, normalizedMoney(request.getSoTienThu()));
        appendFingerprintPart(canonical, resolveExternalTransactionId(request));
        appendFingerprintPart(canonical, normalizedMoney(request.getCashReceived()));
        appendFingerprintPart(canonical, emptyToNull(request.getGhiChu()));
        appendFingerprintPart(canonical, hasManualAllocations(request) ? "MANUAL" : "AUTO");

        if (hasManualAllocations(request)) {
            request.getChiTietNo().stream()
                    .sorted(Comparator.comparing(PhieuThuRequest.ChiTietThuNoRequest::getMaKhoanNo))
                    .forEach(item -> {
                        appendFingerprintPart(canonical, item.getMaKhoanNo());
                        appendFingerprintPart(canonical, normalizedMoney(item.getSoTienApDung()));
                    });
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(canonical.toString().getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Runtime khong ho tro SHA-256", ex);
        }
    }

    private void appendFingerprintPart(StringBuilder target, String value) {
        String normalized = value == null ? "" : value;
        target.append(normalized.length()).append(':').append(normalized).append('|');
    }

    private String normalizedMoney(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private boolean hasManualAllocations(PhieuThuRequest request) {
        return request.getChiTietNo() != null && !request.getChiTietNo().isEmpty();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record PaymentApplyItem(KhoanNo khoanNo, BigDecimal soTienApDung) {
    }
}

package com.library.backend.service;

import com.library.backend.dto.OpenLoanLookupResponse;
import com.library.backend.dto.ReturnPreviewResponse;
import com.library.backend.dto.StaffContextResponse;
import com.library.backend.dto.TraSachRequest;
import com.library.backend.dto.TraSachResponse;
import com.library.backend.entity.ChiTietPhieuMuon;
import com.library.backend.entity.ChiTietPhieuTra;
import com.library.backend.entity.CuonSach;
import com.library.backend.entity.KhoanNo;
import com.library.backend.entity.PhieuMuon;
import com.library.backend.entity.PhieuTra;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.repository.ChiTietPhieuMuonRepository;
import com.library.backend.repository.ChiTietPhieuTraRepository;
import com.library.backend.repository.CuonSachRepository;
import com.library.backend.repository.KhoanNoRepository;
import com.library.backend.repository.PhieuMuonRepository;
import com.library.backend.repository.PhieuTraRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TraSachService {

    private static final String TT_SANCO = "TT_SANCO";
    private static final String TT_HONG = "TT_HONG";
    private static final String TT_MAT = "TT_MAT";
    private static final String TT_DANGDATTRUOC = "TT_DANGDATTRUOC";
    private static final String CONDITION_NORMAL = "BĂ¬nh thÆ°á»ng";
    private static final String CONDITION_DAMAGED = "Há»ng";
    private static final String CONDITION_LOST = "Máº¥t";
    private static final String STATUS_BORROWING = "Äang mÆ°á»£n";
    private static final String DAMAGE_SEVERITY_FULL = "FULL";
    private static final DateTimeFormatter RETURN_ID_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String OPEN_LOAN_SELECT = """
            SELECT
                ctm.MaChiTietMuon,
                pm.MaPhieuMuon,
                pm.MaDocGia,
                dg.HoTen,
                ctm.MaCuonSach,
                cs.MaVach,
                cs.MaDauSach,
                ds.TenDauSach,
                pm.MaChiNhanh,
                cn.TenChiNhanh,
                ctm.NgayMuon,
                ctm.HanTra,
                CASE
                    WHEN ctm.HanTra < SYSDATETIME()
                    THEN DATEDIFF(DAY, CAST(ctm.HanTra AS DATE), CAST(SYSDATETIME() AS DATE))
                    ELSE 0
                END AS SoNgayTre,
                ctm.TrangThai
            FROM CHITIETPHIEUMUON ctm
            INNER JOIN PHIEUMUON pm ON pm.MaPhieuMuon = ctm.MaPhieuMuon
            INNER JOIN DOCGIA dg ON dg.MaDocGia = pm.MaDocGia
            INNER JOIN CUONSACH cs ON cs.MaCuonSach = ctm.MaCuonSach
            INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
            INNER JOIN CHINHANH cn ON cn.MaChiNhanh = pm.MaChiNhanh
            WHERE ctm.TrangThai IN (N'Ă„Âang mĂ†Â°Ă¡Â»Â£n', N'Äang mÆ°á»£n', N'Dang muon')
              AND NOT EXISTS (
                  SELECT 1
                  FROM CHITIETPHIEUTRA ctpt
                  WHERE ctpt.MaChiTietMuon = ctm.MaChiTietMuon
              )
            """;

    private final PhieuTraRepository phieuTraRepository;
    private final ChiTietPhieuTraRepository chiTietPhieuTraRepository;
    private final ChiTietPhieuMuonRepository chiTietPhieuMuonRepository;
    private final PhieuMuonRepository phieuMuonRepository;
    private final CuonSachRepository cuonSachRepository;
    private final KhoanNoRepository khoanNoRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ActivityLogService activityLogService;
    private final BranchAuthorizationService branchAuthorizationService;

    public TraSachService(
            PhieuTraRepository phieuTraRepository,
            ChiTietPhieuTraRepository chiTietPhieuTraRepository,
            ChiTietPhieuMuonRepository chiTietPhieuMuonRepository,
            PhieuMuonRepository phieuMuonRepository,
            CuonSachRepository cuonSachRepository,
            KhoanNoRepository khoanNoRepository,
            JdbcTemplate jdbcTemplate,
            ActivityLogService activityLogService,
            BranchAuthorizationService branchAuthorizationService
    ) {
        this.phieuTraRepository = phieuTraRepository;
        this.chiTietPhieuTraRepository = chiTietPhieuTraRepository;
        this.chiTietPhieuMuonRepository = chiTietPhieuMuonRepository;
        this.phieuMuonRepository = phieuMuonRepository;
        this.cuonSachRepository = cuonSachRepository;
        this.khoanNoRepository = khoanNoRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.activityLogService = activityLogService;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    @Transactional
    public TraSachResponse createStaffReturn(
            TraSachRequest request,
            AuthUser authenticatedUser,
            String requestedIdempotencyKey
    ) {
        String idempotencyKey = normalizeIdempotencyKey(requestedIdempotencyKey);
        validateDistinctReturnItems(request);
        TraSachResponse idempotentResponse = findIdempotentResponseBeforeLock(
                idempotencyKey,
                request,
                authenticatedUser
        );
        if (idempotentResponse != null) {
            return idempotentResponse;
        }

        ReturnContext context = inferReturnContextFromLockedDetails(request);
        StaffContextResponse staffContext = branchAuthorizationService.requireAllowedBranch(
                authenticatedUser,
                context.branchId()
        );
        String fingerprint = buildRequestFingerprint(
                request,
                authenticatedUser,
                staffContext.staffId(),
                context.readerId(),
                context.branchId()
        );

        if (hasText(request.getMaDocGia()) && !request.getMaDocGia().trim().equals(context.readerId())) {
            throw new BusinessException("Ma doc gia request khong khop chi tiet muon");
        }
        if (hasText(request.getMaChiNhanh()) && !request.getMaChiNhanh().trim().equals(context.branchId())) {
            throw new BusinessException("Ma chi nhanh request khong khop chi tiet muon");
        }
        if (hasText(request.getMaNhanVienNhan()) && !request.getMaNhanVienNhan().trim().equals(staffContext.staffId())) {
            throw new AccessDeniedException("Ma nhan vien nhan khong khop tai khoan dang nhap");
        }

        request.setMaPhieuTra(generateReturnId());
        request.setMaDocGia(context.readerId());
        request.setMaChiNhanh(context.branchId());
        request.setMaNhanVienNhan(null);
        request.setIdempotencyKey(idempotencyKey);
        request.setRequestFingerprint(fingerprint);
        return create(request, authenticatedUser);
    }

    @Transactional
    public TraSachResponse create(TraSachRequest request, AuthUser authenticatedUser) {
        StaffContextResponse staffContext = branchAuthorizationService.requireAllowedBranch(
                authenticatedUser,
                request.getMaChiNhanh()
        );
        String authenticatedStaffId = staffContext.staffId();

        if (hasText(request.getMaNhanVienNhan())
                && !request.getMaNhanVienNhan().equals(authenticatedStaffId)) {
            throw new AccessDeniedException("MĂ£ nhĂ¢n viĂªn nháº­n khĂ´ng khá»›p tĂ i khoáº£n Ä‘Äƒng nháº­p");
        }

        if (phieuTraRepository.existsById(request.getMaPhieuTra())) {
            throw new BusinessException("MĂ£ phiáº¿u tráº£ Ä‘Ă£ tá»“n táº¡i");
        }

        if (!existsById("DOCGIA", "MaDocGia", request.getMaDocGia())) {
            throw new ResourceNotFoundException("Äá»™c giáº£ khĂ´ng tá»“n táº¡i");
        }

        if (!existsById("CHINHANH", "MaChiNhanh", request.getMaChiNhanh())) {
            throw new ResourceNotFoundException("Chi nhĂ¡nh khĂ´ng tá»“n táº¡i");
        }

        LocalDateTime ngayTra = LocalDateTime.now();
        Map<String, ChiTietPhieuMuon> lockedDetails = lockAndValidateDetails(request);
        validateNonNegativeDamageFines(request);

        PhieuTra phieuTra = new PhieuTra();
        phieuTra.setMaPhieuTra(request.getMaPhieuTra());
        phieuTra.setMaDocGia(request.getMaDocGia());
        phieuTra.setMaNhanVienNhan(authenticatedStaffId);
        phieuTra.setMaChiNhanh(request.getMaChiNhanh());
        phieuTra.setNgayTra(ngayTra);
        phieuTra.setGhiChu(request.getGhiChu());
        phieuTra.setIdempotencyKey(request.getIdempotencyKey());
        phieuTra.setRequestFingerprint(request.getRequestFingerprint());

        phieuTraRepository.save(phieuTra);

        for (int i = 0; i < request.getChiTiet().size(); i++) {
            TraSachRequest.ChiTietTraRequest item = request.getChiTiet().get(i);
            ChiTietPhieuMuon chiTietMuon = lockedDetails.get(item.getMaChiTietMuon());
            PhieuMuon phieuMuon = phieuMuonRepository.findById(chiTietMuon.getMaPhieuMuon())
                    .orElseThrow(() -> new ResourceNotFoundException("KhĂ´ng tĂ¬m tháº¥y phiáº¿u mÆ°á»£n"));
            CuonSach cuonSach = cuonSachRepository.findById(chiTietMuon.getMaCuonSach())
                    .orElseThrow(() -> new ResourceNotFoundException("KhĂ´ng tĂ¬m tháº¥y cuá»‘n sĂ¡ch"));
            ReturnAssessment assessment = assessReturnItem(item, chiTietMuon, phieuMuon, ngayTra, authenticatedUser);

            String maChiTietTra = buildMaChiTietTra(request.getMaPhieuTra(), i + 1);
            ChiTietPhieuTra chiTietTra = new ChiTietPhieuTra();
            chiTietTra.setMaChiTietTra(maChiTietTra);
            chiTietTra.setMaPhieuTra(request.getMaPhieuTra());
            chiTietTra.setMaChiTietMuon(item.getMaChiTietMuon());
            chiTietTra.setTinhTrangKhiTra(item.getTinhTrangKhiTra());
            chiTietTra.setSoNgayTre(assessment.soNgayTre());
            chiTietTra.setTienPhatTre(assessment.tienPhatTre());
            chiTietTra.setTienPhatHongMat(assessment.finalDamageFine());
            chiTietTra.setGhiChu(buildReturnDetailNote(item));
            chiTietPhieuTraRepository.save(chiTietTra);

            chiTietMuon.setNgayTraThucTe(ngayTra);
            ReturnCondition condition = normalizeCondition(item.getTinhTrangKhiTra());
            if (condition == ReturnCondition.NORMAL) {
                chiTietMuon.setTrangThai("ÄĂ£ tráº£");
                cuonSach.setMaTrangThai(resolveAvailableOrHeldStatus(cuonSach, request.getMaChiNhanh(), ngayTra));
            } else if (condition == ReturnCondition.DAMAGED) {
                chiTietMuon.setTrangThai("Há»ng");
                cuonSach.setMaTrangThai(TT_HONG);
            } else {
                chiTietMuon.setTrangThai("Máº¥t");
                cuonSach.setMaTrangThai(TT_MAT);
            }

            chiTietPhieuMuonRepository.save(chiTietMuon);
            cuonSachRepository.save(cuonSach);

            if (assessment.tienPhatTre().compareTo(BigDecimal.ZERO) > 0) {
                createKhoanNo(
                        "NO_" + request.getMaPhieuTra() + "_" + String.format("%02d", i + 1) + "T",
                        request.getMaDocGia(),
                        "NO_TRA_TRE",
                        maChiTietTra,
                        assessment.tienPhatTre(),
                        "Tráº£ trá»… " + assessment.soNgayTre() + " ngĂ y cho cuá»‘n " + chiTietMuon.getMaCuonSach(),
                        ngayTra
                );
            }

            if (assessment.finalDamageFine().compareTo(BigDecimal.ZERO) > 0) {
                String loaiNo = condition == ReturnCondition.DAMAGED ? "NO_HONG_SACH" : "NO_MAT_SACH";
                createKhoanNo(
                        "NO_" + request.getMaPhieuTra() + "_" + String.format("%02d", i + 1) + "H",
                        request.getMaDocGia(),
                        loaiNo,
                        maChiTietTra,
                        assessment.finalDamageFine(),
                        "Pháº¡t do sĂ¡ch " + item.getTinhTrangKhiTra().toLowerCase() + ": " + chiTietMuon.getMaCuonSach(),
                        ngayTra
                );
            }

            if (assessment.adjusted()) {
                auditFineAdjustment(
                        request.getMaPhieuTra(),
                        maChiTietTra,
                        item.getMaChiTietMuon(),
                        assessment.suggestedDamageFine(),
                        assessment.finalDamageFine(),
                        assessment.adjustmentReason(),
                        authenticatedUser,
                        ngayTra
                );
            }

            updatePhieuMuonStatusIfDone(phieuMuon);
        }

        TraSachResponse response = getById(request.getMaPhieuTra());

        String chiTietTraText = request.getChiTiet()
                .stream()
                .map(item -> item.getMaChiTietMuon() + " - " + item.getTinhTrangKhiTra())
                .collect(Collectors.joining(", "));

        activityLogService.logAsAccountSafe(
                authenticatedUser.getMaTaiKhoan(),
                "Táº¡o phiáº¿u tráº£",
                "PHIEUTRA",
                request.getMaPhieuTra(),
                "NhĂ¢n viĂªn " + authenticatedStaffId
                        + " nháº­n sĂ¡ch tá»« Ä‘á»™c giáº£ " + request.getMaDocGia()
                        + " táº¡i chi nhĂ¡nh " + request.getMaChiNhanh()
                        + " tráº£ sĂ¡ch. Chi tiáº¿t: "
                        + chiTietTraText
        );

        return response;
    }

    @Transactional(readOnly = true)
    public ReturnPreviewResponse preview(TraSachRequest request, AuthUser authenticatedUser) {
        branchAuthorizationService.requireAllowedBranch(authenticatedUser, request.getMaChiNhanh());
        validateDistinctReturnItems(request);

        if (!existsById("DOCGIA", "MaDocGia", request.getMaDocGia())) {
            throw new ResourceNotFoundException("Äá»™c giáº£ khĂ´ng tá»“n táº¡i");
        }

        if (!existsById("CHINHANH", "MaChiNhanh", request.getMaChiNhanh())) {
            throw new ResourceNotFoundException("Chi nhĂ¡nh khĂ´ng tá»“n táº¡i");
        }

        LocalDateTime previewedAt = LocalDateTime.now();
        List<ReturnPreviewResponse.ReturnPreviewItemResponse> items = new ArrayList<>();
        BigDecimal tongPhatTre = BigDecimal.ZERO;
        BigDecimal tongPhatHongMat = BigDecimal.ZERO;

        for (TraSachRequest.ChiTietTraRequest item : request.getChiTiet()) {
            ChiTietPhieuMuon chiTietMuon = chiTietPhieuMuonRepository.findById(item.getMaChiTietMuon())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "KhĂ´ng tĂ¬m tháº¥y chi tiáº¿t mÆ°á»£n: " + item.getMaChiTietMuon()
                    ));

            if (chiTietPhieuTraRepository.existsByMaChiTietMuon(item.getMaChiTietMuon())) {
                throw new BusinessException("Chi tiáº¿t mÆ°á»£n Ä‘Ă£ Ä‘Æ°á»£c tráº£: " + item.getMaChiTietMuon());
            }

            if (!isBorrowingStatus(chiTietMuon.getTrangThai())) {
                throw new BusinessException("Chi tiáº¿t mÆ°á»£n khĂ´ng á»Ÿ tráº¡ng thĂ¡i Äang mÆ°á»£n");
            }

            PhieuMuon phieuMuon = phieuMuonRepository.findById(chiTietMuon.getMaPhieuMuon())
                    .orElseThrow(() -> new ResourceNotFoundException("KhĂ´ng tĂ¬m tháº¥y phiáº¿u mÆ°á»£n"));

            if (!request.getMaDocGia().equals(phieuMuon.getMaDocGia())) {
                throw new BusinessException("Chi tiáº¿t mÆ°á»£n khĂ´ng thuá»™c Ä‘á»™c giáº£ nĂ y");
            }

            if (!request.getMaChiNhanh().equals(phieuMuon.getMaChiNhanh())) {
                throw new BusinessException("KhĂ´ng Ä‘Æ°á»£c tráº£ sĂ¡ch khĂ¡c chi nhĂ¡nh mÆ°á»£n");
            }

            ReturnAssessment assessment = assessReturnItem(item, chiTietMuon, phieuMuon, previewedAt, authenticatedUser);
            CopyValue copyValue = getCopyValue(chiTietMuon.getMaCuonSach());
            BigDecimal itemTotal = assessment.tienPhatTre().add(assessment.finalDamageFine());
            tongPhatTre = tongPhatTre.add(assessment.tienPhatTre());
            tongPhatHongMat = tongPhatHongMat.add(assessment.finalDamageFine());

            items.add(new ReturnPreviewResponse.ReturnPreviewItemResponse(
                    item.getMaChiTietMuon(),
                    chiTietMuon.getMaCuonSach(),
                    copyValue.titleId(),
                    copyValue.titleName(),
                    item.getTinhTrangKhiTra(),
                    item.getLoaiHuHong() == null ? Collections.emptyList() : item.getLoaiHuHong(),
                    item.getMucDoHuHong(),
                    item.getMoTaHuHong(),
                    assessment.soNgayTre(),
                    assessment.tienPhatTre(),
                    assessment.suggestedDamageFine(),
                    assessment.finalDamageFine(),
                    itemTotal,
                    assessment.adjusted(),
                    assessment.adjustmentReason()
            ));
        }

        return new ReturnPreviewResponse(
                true,
                request.getMaDocGia(),
                request.getMaChiNhanh(),
                previewedAt,
                tongPhatTre,
                tongPhatHongMat,
                tongPhatTre.add(tongPhatHongMat),
                Collections.emptyList(),
                items
        );
    }

    private Map<String, ChiTietPhieuMuon> lockAndValidateDetails(TraSachRequest request) {
        validateDistinctReturnItems(request);
        Set<String> detailIds = request.getChiTiet()
                .stream()
                .map(TraSachRequest.ChiTietTraRequest::getMaChiTietMuon)
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, ChiTietPhieuMuon> lockedDetails = new HashMap<>();
        detailIds.stream().sorted().forEach(detailId -> {
            ChiTietPhieuMuon detail = chiTietPhieuMuonRepository.findByIdForUpdate(detailId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "KhĂ´ng tĂ¬m tháº¥y chi tiáº¿t mÆ°á»£n: " + detailId
                    ));

            if (chiTietPhieuTraRepository.existsByMaChiTietMuon(detailId)) {
                throw new BusinessException("Chi tiáº¿t mÆ°á»£n Ä‘Ă£ Ä‘Æ°á»£c tráº£: " + detailId);
            }

            if (!isBorrowingStatus(detail.getTrangThai())) {
                throw new BusinessException("Chi tiáº¿t mÆ°á»£n khĂ´ng á»Ÿ tráº¡ng thĂ¡i Äang mÆ°á»£n");
            }

            PhieuMuon loan = phieuMuonRepository.findById(detail.getMaPhieuMuon())
                    .orElseThrow(() -> new ResourceNotFoundException("KhĂ´ng tĂ¬m tháº¥y phiáº¿u mÆ°á»£n"));

            if (!request.getMaDocGia().equals(loan.getMaDocGia())) {
                throw new BusinessException("Chi tiáº¿t mÆ°á»£n khĂ´ng thuá»™c Ä‘á»™c giáº£ nĂ y");
            }

            if (!request.getMaChiNhanh().equals(loan.getMaChiNhanh())) {
                throw new BusinessException("KhĂ´ng Ä‘Æ°á»£c tráº£ sĂ¡ch khĂ¡c chi nhĂ¡nh mÆ°á»£n");
            }

            lockedDetails.put(detailId, detail);
        });

        return lockedDetails;
    }

    private ReturnContext inferReturnContextFromLockedDetails(TraSachRequest request) {
        Set<String> readerIds = new HashSet<>();
        Set<String> branchIds = new HashSet<>();

        request.getChiTiet()
                .stream()
                .map(TraSachRequest.ChiTietTraRequest::getMaChiTietMuon)
                .sorted()
                .forEach(detailId -> {
                    ChiTietPhieuMuon detail = chiTietPhieuMuonRepository.findByIdForUpdate(detailId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Khong tim thay chi tiet muon: " + detailId
                            ));

                    if (chiTietPhieuTraRepository.existsByMaChiTietMuon(detailId)) {
                        throw new BusinessException("Chi tiet muon da duoc tra: " + detailId);
                    }

                    if (!isBorrowingStatus(detail.getTrangThai())) {
                        throw new BusinessException("Chi tiet muon khong o trang thai Dang muon");
                    }

                    PhieuMuon loan = phieuMuonRepository.findById(detail.getMaPhieuMuon())
                            .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phieu muon"));
                    readerIds.add(loan.getMaDocGia());
                    branchIds.add(loan.getMaChiNhanh());
                });

        if (readerIds.size() != 1) {
            throw new BusinessException("Khong the tron nhieu doc gia trong mot phieu tra");
        }
        if (branchIds.size() != 1) {
            throw new BusinessException("Khong the tron nhieu chi nhanh trong mot phieu tra");
        }

        return new ReturnContext(readerIds.iterator().next(), branchIds.iterator().next());
    }

    private void validateDistinctReturnItems(TraSachRequest request) {
        if (request == null || request.getChiTiet() == null || request.getChiTiet().isEmpty()) {
            throw new BusinessException("Phieu tra phai co it nhat mot chi tiet muon");
        }
        Set<String> detailIds = new HashSet<>();
        for (TraSachRequest.ChiTietTraRequest item : request.getChiTiet()) {
            if (!detailIds.add(item.getMaChiTietMuon())) {
                throw new BusinessException(
                        "Chi tiáº¿t mÆ°á»£n bá»‹ trĂ¹ng trong phiáº¿u tráº£: " + item.getMaChiTietMuon()
                );
            }
        }
    }

    private void validateNonNegativeDamageFines(TraSachRequest request) {
        for (TraSachRequest.ChiTietTraRequest item : request.getChiTiet()) {
            BigDecimal requestedFine = item.getTienPhatHongMatDieuChinh();
            if (requestedFine == null) {
                requestedFine = item.getTienPhatHongMat();
            }
            if (requestedFine != null && requestedFine.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Tien phat hong/mat khong duoc am");
            }
        }
    }

    private String normalizeIdempotencyKey(String requestedKey) {
        if (!hasText(requestedKey)) {
            throw new BusinessException("Idempotency-Key khong duoc de trong khi tao phieu tra");
        }
        String key = requestedKey.trim();
        if (key.length() > 100) {
            throw new BusinessException("Idempotency-Key toi da 100 ky tu");
        }
        return key;
    }

    private TraSachResponse findIdempotentResponseBeforeLock(
            String idempotencyKey,
            TraSachRequest request,
            AuthUser authenticatedUser
    ) {
        return phieuTraRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    String retryFingerprint = buildRequestFingerprint(
                            request,
                            authenticatedUser,
                            existing.getMaNhanVienNhan(),
                            existing.getMaDocGia(),
                            existing.getMaChiNhanh()
                    );
                    if (!retryFingerprint.equals(existing.getRequestFingerprint())) {
                        throw new BusinessException("Idempotency-Key da duoc dung cho mot yeu cau tra sach khac");
                    }
                    return getById(existing.getMaPhieuTra());
                })
                .orElse(null);
    }

    private String generateReturnId() {
        String base = "PTR" + LocalDateTime.now().format(RETURN_ID_TIME_FORMAT);
        String returnId = base;
        int counter = 0;
        while (phieuTraRepository.existsById(returnId)) {
            counter++;
            returnId = base + String.format("%02d", counter);
            if (returnId.length() > 30) {
                throw new BusinessException("Khong the sinh ma phieu tra hop le");
            }
        }
        return returnId;
    }

    private String buildRequestFingerprint(
            TraSachRequest request,
            AuthUser authenticatedUser,
            String staffId,
            String readerId,
            String branchId
    ) {
        StringBuilder canonical = new StringBuilder();
        appendFingerprintPart(canonical, authenticatedUser.getMaTaiKhoan());
        appendFingerprintPart(canonical, staffId);
        appendFingerprintPart(canonical, readerId);
        appendFingerprintPart(canonical, branchId);
        appendFingerprintPart(canonical, emptyToNull(request.getGhiChu()));

        request.getChiTiet()
                .stream()
                .sorted(Comparator.comparing(TraSachRequest.ChiTietTraRequest::getMaChiTietMuon))
                .forEach(item -> {
                    appendFingerprintPart(canonical, item.getMaChiTietMuon());
                    appendFingerprintPart(canonical, normalizeCondition(item.getTinhTrangKhiTra()).name());
                    appendFingerprintPart(canonical, item.getLoaiHuHong() == null
                            ? null
                            : item.getLoaiHuHong().stream().sorted().collect(Collectors.joining(",")));
                    appendFingerprintPart(canonical, emptyToNull(item.getMucDoHuHong()));
                    appendFingerprintPart(canonical, emptyToNull(item.getMoTaHuHong()));
                    appendFingerprintPart(canonical, normalizedMoney(item.getTienPhatHongMat()));
                    appendFingerprintPart(canonical, normalizedMoney(item.getTienPhatHongMatDieuChinh()));
                    appendFingerprintPart(canonical, emptyToNull(item.getLyDoDieuChinhTienPhat()));
                    appendFingerprintPart(canonical, emptyToNull(item.getGhiChu()));
                });

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 khong kha dung", e);
        }
    }

    private void appendFingerprintPart(StringBuilder canonical, String value) {
        canonical.append(value == null ? "<null>" : value).append('\u001F');
    }

    private String normalizedMoney(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }

    private String emptyToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    public TraSachResponse getById(String maPhieuTra) {
        PhieuTra phieuTra = phieuTraRepository.findById(maPhieuTra)
                .orElseThrow(() -> new ResourceNotFoundException("KhĂ´ng tĂ¬m tháº¥y phiáº¿u tráº£"));

        List<TraSachResponse.ChiTietTraResponse> chiTiet = chiTietPhieuTraRepository
                .findByMaPhieuTra(maPhieuTra)
                .stream()
                .map(ct -> new TraSachResponse.ChiTietTraResponse(
                        ct.getMaChiTietTra(),
                        ct.getMaChiTietMuon(),
                        ct.getTinhTrangKhiTra(),
                        ct.getSoNgayTre(),
                        ct.getTienPhatTre(),
                        ct.getTienPhatHongMat()
                ))
                .toList();
        List<TraSachResponse.KhoanNoTraResponse> khoanNo = findDebtsByReturnId(maPhieuTra);
        BigDecimal totalFine = chiTiet.stream()
                .map(item -> item.getTienPhatTre().add(item.getTienPhatHongMat()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TraSachResponse(
                phieuTra.getMaPhieuTra(),
                phieuTra.getMaDocGia(),
                phieuTra.getMaNhanVienNhan(),
                phieuTra.getMaChiNhanh(),
                phieuTra.getNgayTra(),
                phieuTra.getIdempotencyKey(),
                khoanNo,
                new TraSachResponse.PrintPayload(
                        "Phieu tra sach",
                        phieuTra.getMaPhieuTra(),
                        phieuTra.getMaDocGia(),
                        phieuTra.getMaNhanVienNhan(),
                        phieuTra.getMaChiNhanh(),
                        phieuTra.getNgayTra(),
                        totalFine
                ),
                new TraSachResponse.PaymentSuggestion(
                        !khoanNo.isEmpty(),
                        phieuTra.getMaDocGia(),
                        khoanNo.stream()
                                .map(TraSachResponse.KhoanNoTraResponse::getSoTienPhatSinh)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        khoanNo.stream()
                                .map(TraSachResponse.KhoanNoTraResponse::getMaKhoanNo)
                                .toList()
                ),
                chiTiet
        );
    }

    private List<TraSachResponse.KhoanNoTraResponse> findDebtsByReturnId(String maPhieuTra) {
        return jdbcTemplate.query(
                """
                SELECT kn.MaKhoanNo, kn.MaChiTietTra, kn.MaLoaiKhoanNo, kn.SoTienPhatSinh, kn.LyDo
                FROM KHOANNO kn
                INNER JOIN CHITIETPHIEUTRA ctpt ON ctpt.MaChiTietTra = kn.MaChiTietTra
                WHERE ctpt.MaPhieuTra = ?
                ORDER BY kn.NgayPhatSinh ASC, kn.MaKhoanNo ASC
                """,
                (rs, rowNum) -> new TraSachResponse.KhoanNoTraResponse(
                        rs.getString("MaKhoanNo"),
                        rs.getString("MaChiTietTra"),
                        rs.getString("MaLoaiKhoanNo"),
                        rs.getBigDecimal("SoTienPhatSinh"),
                        rs.getString("LyDo")
                ),
                maPhieuTra
        );
    }

    @Transactional(readOnly = true)
    public OpenLoanLookupResponse getOpenLoanByCode(String code, AuthUser authenticatedUser) {
        if (!hasText(code)) {
            throw new BusinessException("Ma quet khong duoc de trong");
        }

        List<OpenLoanLookupResponse> rows = jdbcTemplate.query(
                OPEN_LOAN_SELECT + """
                  AND (
                      UPPER(ctm.MaChiTietMuon) = UPPER(?)
                      OR UPPER(ctm.MaCuonSach) = UPPER(?)
                      OR UPPER(COALESCE(cs.MaVach, '')) = UPPER(?)
                  )
                ORDER BY ctm.HanTra ASC, ctm.MaChiTietMuon ASC
                """,
                (rs, rowNum) -> mapOpenLoan(rs),
                code.trim(),
                code.trim(),
                code.trim()
        );

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Khong tim thay sach dang muon theo ma quet");
        }

        OpenLoanLookupResponse row = rows.get(0);
        branchAuthorizationService.requireAllowedBranch(authenticatedUser, row.branchId());
        return row;
    }

    @Transactional(readOnly = true)
    public List<OpenLoanLookupResponse> getOpenLoansByReader(String maDocGia, AuthUser authenticatedUser) {
        if (!existsById("DOCGIA", "MaDocGia", maDocGia)) {
            throw new ResourceNotFoundException("Doc gia khong ton tai");
        }

        return jdbcTemplate.query(
                        OPEN_LOAN_SELECT + """
                          AND pm.MaDocGia = ?
                        ORDER BY ctm.HanTra ASC, ctm.MaChiTietMuon ASC
                        """,
                        (rs, rowNum) -> mapOpenLoan(rs),
                        maDocGia
                )
                .stream()
                .filter(row -> branchAuthorizationService.canAccessBranch(authenticatedUser, row.branchId()))
                .toList();
    }

    private OpenLoanLookupResponse mapOpenLoan(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp borrowedAt = rs.getTimestamp("NgayMuon");
        Timestamp dueAt = rs.getTimestamp("HanTra");
        return new OpenLoanLookupResponse(
                rs.getString("MaChiTietMuon"),
                rs.getString("MaPhieuMuon"),
                rs.getString("MaDocGia"),
                rs.getString("HoTen"),
                rs.getString("MaCuonSach"),
                rs.getString("MaVach"),
                rs.getString("MaDauSach"),
                rs.getString("TenDauSach"),
                rs.getString("MaChiNhanh"),
                rs.getString("TenChiNhanh"),
                borrowedAt == null ? null : borrowedAt.toLocalDateTime(),
                dueAt == null ? null : dueAt.toLocalDateTime(),
                rs.getInt("SoNgayTre"),
                rs.getString("TrangThai")
        );
    }

    private ReturnAssessment assessReturnItem(
            TraSachRequest.ChiTietTraRequest item,
            ChiTietPhieuMuon chiTietMuon,
            PhieuMuon phieuMuon,
            LocalDateTime returnAt,
            AuthUser authenticatedUser
    ) {
        ReturnCondition condition = normalizeCondition(item.getTinhTrangKhiTra());

        int lateDays = calculateLateDays(chiTietMuon, returnAt);
        BigDecimal lateFine = getMucPhatTreMoiNgay(phieuMuon.getMaPhienBanQuyDinh())
                .multiply(BigDecimal.valueOf(lateDays));
        BigDecimal requestedAdjustment = item.getTienPhatHongMatDieuChinh();
        if (requestedAdjustment == null && item.getTienPhatHongMat() != null) {
            requestedAdjustment = item.getTienPhatHongMat();
        }
        if (requestedAdjustment != null && requestedAdjustment.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Tien phat hong/mat khong duoc am");
        }
        BigDecimal suggestedDamageFine = calculateSuggestedDamageFine(condition, item, chiTietMuon.getMaCuonSach());
        BigDecimal finalDamageFine = suggestedDamageFine;

        boolean adjusted = requestedAdjustment != null && requestedAdjustment.compareTo(suggestedDamageFine) != 0;
        String adjustmentReason = item.getLyDoDieuChinhTienPhat();
        if (adjusted) {
            if (!RoleConstants.ADMIN.equals(authenticatedUser.getTenVaiTro())) {
                throw new AccessDeniedException("Chá»‰ quáº£n trá»‹ viĂªn Ä‘Æ°á»£c Ä‘iá»u chá»‰nh tiá»n pháº¡t há»ng/máº¥t");
            }
            if (!hasText(adjustmentReason)) {
                throw new BusinessException("Äiá»u chá»‰nh tiá»n pháº¡t pháº£i cĂ³ lĂ½ do");
            }
            finalDamageFine = requestedAdjustment;
        }

        return new ReturnAssessment(lateDays, lateFine, suggestedDamageFine, finalDamageFine, adjusted, adjustmentReason);
    }

    private int calculateLateDays(ChiTietPhieuMuon chiTietMuon, LocalDateTime returnAt) {
        long soNgayTreLong = ChronoUnit.DAYS.between(
                chiTietMuon.getHanTra().toLocalDate(),
                returnAt.toLocalDate()
        );
        return Math.max(0, (int) soNgayTreLong);
    }

    private BigDecimal calculateSuggestedDamageFine(
            ReturnCondition condition,
            TraSachRequest.ChiTietTraRequest item,
            String copyId
    ) {
        if (condition == ReturnCondition.NORMAL) {
            return BigDecimal.ZERO;
        }

        if (condition == ReturnCondition.DAMAGED && (item.getLoaiHuHong() == null || item.getLoaiHuHong().isEmpty())) {
            throw new BusinessException("SĂ¡ch há»ng pháº£i cĂ³ loáº¡i hÆ° há»ng");
        }

        String severity = condition == ReturnCondition.LOST ? DAMAGE_SEVERITY_FULL : item.getMucDoHuHong();
        if (!hasText(severity)) {
            throw new BusinessException("SĂ¡ch há»ng pháº£i cĂ³ má»©c Ä‘á»™ hÆ° há»ng");
        }

        DamageFineRule rule = getDamageFineRule(condition.databaseValue(), severity.trim().toUpperCase());
        BigDecimal copyValue = getCopyValue(copyId).value();
        BigDecimal fine = copyValue.multiply(rule.rate()).setScale(0, RoundingMode.HALF_UP);
        if (fine.compareTo(rule.minimum()) < 0) {
            fine = rule.minimum();
        }
        if (rule.maximum() != null && fine.compareTo(rule.maximum()) > 0) {
            fine = rule.maximum();
        }

        return fine;
    }

    private DamageFineRule getDamageFineRule(String condition, String severity) {
        List<DamageFineRule> result = jdbcTemplate.query(
                """
                SELECT TOP 1 TyLePhat, SoTienToiThieu, SoTienToiDa
                FROM QUYDINH_PHAT_HONGMAT
                WHERE TinhTrang = ?
                  AND MucDo = ?
                  AND TrangThai = N'Äang Ă¡p dá»¥ng'
                ORDER BY MaQuyDinhPhat ASC
                """,
                (rs, rowNum) -> new DamageFineRule(
                        rs.getBigDecimal("TyLePhat"),
                        rs.getBigDecimal("SoTienToiThieu"),
                        rs.getBigDecimal("SoTienToiDa")
                ),
                condition,
                severity
        );

        if (result.isEmpty()) {
            throw new BusinessException("KhĂ´ng tĂ¬m tháº¥y quy Ä‘á»‹nh pháº¡t há»ng/máº¥t cho tĂ¬nh tráº¡ng " + condition);
        }

        return result.get(0);
    }

    private CopyValue getCopyValue(String copyId) {
        List<CopyValue> result = jdbcTemplate.query(
                """
                SELECT ds.MaDauSach, ds.TenDauSach, ds.TriGia
                FROM CUONSACH cs
                INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
                WHERE cs.MaCuonSach = ?
                """,
                (rs, rowNum) -> new CopyValue(
                        rs.getString("MaDauSach"),
                        rs.getString("TenDauSach"),
                        rs.getBigDecimal("TriGia")
                ),
                copyId
        );

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("KhĂ´ng tĂ¬m tháº¥y trá»‹ giĂ¡ cuá»‘n sĂ¡ch");
        }

        return result.get(0);
    }

    private String resolveAvailableOrHeldStatus(CuonSach cuonSach, String branchId, LocalDateTime returnedAt) {
        List<String> reservationIds = jdbcTemplate.query(
                """
                SELECT TOP 1 MaPhieuDatTruoc
                FROM PHIEUDATTRUOC WITH (UPDLOCK, ROWLOCK)
                WHERE MaDauSach = ?
                  AND MaChiNhanh = ?
                  AND MaCuonSachDuocGiu IS NULL
                  AND TrangThai IN (?, ?)
                ORDER BY NgayDat ASC, MaPhieuDatTruoc ASC
                """,
                (rs, rowNum) -> rs.getString("MaPhieuDatTruoc"),
                cuonSach.getMaDauSach(),
                branchId,
                "\u0110ang ch\u1edd",
                "Dang cho"
        );

        if (reservationIds.isEmpty()) {
            return TT_SANCO;
        }

        String reservationId = reservationIds.get(0);
        jdbcTemplate.update(
                """
                UPDATE PHIEUDATTRUOC
                SET MaCuonSachDuocGiu = ?,
                    TrangThai = ?,
                    NgayHetHanGiuCho = ?
                WHERE MaPhieuDatTruoc = ?
                """,
                cuonSach.getMaCuonSach(),
                "\u0110\u00e3 gi\u1eef ch\u1ed7",
                returnedAt.plusDays(getReservationHoldDays()),
                reservationId
        );

        return TT_DANGDATTRUOC;
    }

    private int getReservationHoldDays() {
        List<Integer> result = jdbcTemplate.query(
                """
                SELECT TOP 1 ts.SoNgayGiuDatTruoc
                FROM PHIENBANQUYDINH pb
                INNER JOIN THAMSOQUYDINH ts ON pb.MaPhienBan = ts.MaPhienBan
                WHERE pb.TrangThai IN (?, ?)
                ORDER BY pb.NgayApDung DESC
                """,
                (rs, rowNum) -> rs.getInt("SoNgayGiuDatTruoc"),
                "\u0110ang \u00e1p d\u1ee5ng",
                "Dang ap dung"
        );

        if (result.isEmpty()) {
            throw new BusinessException("Khong tim thay quy dinh so ngay giu dat truoc");
        }

        return result.get(0);
    }

    private String buildReturnDetailNote(TraSachRequest.ChiTietTraRequest item) {
        List<String> parts = new ArrayList<>();
        if (hasText(item.getGhiChu())) {
            parts.add(item.getGhiChu());
        }
        if (item.getLoaiHuHong() != null && !item.getLoaiHuHong().isEmpty()) {
            parts.add("Loai hu hong: " + String.join(", ", item.getLoaiHuHong()));
        }
        if (hasText(item.getMucDoHuHong())) {
            parts.add("Muc do: " + item.getMucDoHuHong());
        }
        if (hasText(item.getMoTaHuHong())) {
            parts.add("Mo ta: " + item.getMoTaHuHong());
        }
        return parts.isEmpty() ? null : String.join(" | ", parts);
    }

    private void auditFineAdjustment(
            String returnId,
            String returnDetailId,
            String loanDetailId,
            BigDecimal suggestedFine,
            BigDecimal finalFine,
            String reason,
            AuthUser authenticatedUser,
            LocalDateTime adjustedAt
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO PHIEUTRA_DIEUCHINH_PHAT (
                    MaDieuChinh,
                    MaPhieuTra,
                    MaChiTietTra,
                    MaChiTietMuon,
                    TienPhatDeXuat,
                    TienPhatCuoiCung,
                    LyDoDieuChinh,
                    MaNhanVienDieuChinh,
                    ThoiGianDieuChinh
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "DCP_" + returnDetailId,
                returnId,
                returnDetailId,
                loanDetailId,
                suggestedFine,
                finalFine,
                reason,
                authenticatedUser.getMaNhanVien(),
                adjustedAt
        );

        activityLogService.logAsAccountSafe(
                authenticatedUser.getMaTaiKhoan(),
                "Äiá»u chá»‰nh tiá»n pháº¡t tráº£ sĂ¡ch",
                "PHIEUTRA_DIEUCHINH_PHAT",
                "DCP_" + returnDetailId,
                "Äiá»u chá»‰nh tiá»n pháº¡t cho " + loanDetailId
                        + " tá»« " + suggestedFine
                        + " thĂ nh " + finalFine
                        + ". LĂ½ do: " + reason
        );
    }

    private void createKhoanNo(
            String maKhoanNo,
            String maDocGia,
            String maLoaiKhoanNo,
            String maChiTietTra,
            BigDecimal soTien,
            String lyDo,
            LocalDateTime ngayPhatSinh
    ) {
        if (khoanNoRepository.existsById(maKhoanNo)) {
            throw new BusinessException("MĂ£ khoáº£n ná»£ Ä‘Ă£ tá»“n táº¡i: " + maKhoanNo);
        }

        KhoanNo khoanNo = new KhoanNo();
        khoanNo.setMaKhoanNo(maKhoanNo);
        khoanNo.setMaDocGia(maDocGia);
        khoanNo.setMaLoaiKhoanNo(maLoaiKhoanNo);
        khoanNo.setMaChiTietTra(maChiTietTra);
        khoanNo.setSoTienPhatSinh(soTien);
        khoanNo.setSoTienDaThanhToan(BigDecimal.ZERO);
        khoanNo.setNgayPhatSinh(ngayPhatSinh);
        khoanNo.setLyDo(lyDo);
        khoanNo.setTrangThai("ChÆ°a thanh toĂ¡n");

        khoanNoRepository.save(khoanNo);
    }

    private BigDecimal getMucPhatTreMoiNgay(String maPhienBan) {
        List<BigDecimal> result = jdbcTemplate.query(
                """
                SELECT MucPhatTreMoiNgay
                FROM THAMSOQUYDINH
                WHERE MaPhienBan = ?
                """,
                (rs, rowNum) -> rs.getBigDecimal("MucPhatTreMoiNgay"),
                maPhienBan
        );

        if (result.isEmpty()) {
            throw new BusinessException("KhĂ´ng tĂ¬m tháº¥y má»©c pháº¡t trá»… cho phiĂªn báº£n quy Ä‘á»‹nh " + maPhienBan);
        }

        return result.get(0);
    }

    private void updatePhieuMuonStatusIfDone(PhieuMuon phieuMuon) {
        Integer soSachConDangMuon = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM CHITIETPHIEUMUON
                WHERE MaPhieuMuon = ?
                  AND TrangThai IN (?, ?, ?)
                """,
                Integer.class,
                phieuMuon.getMaPhieuMuon(),
                "Äang mÆ°á»£n",
                "\u0110ang m\u01b0\u1ee3n",
                "Dang muon"
        );

        if (soSachConDangMuon != null && soSachConDangMuon == 0) {
            phieuMuon.setTrangThai("ÄĂ£ tráº£ háº¿t");
            phieuMuonRepository.save(phieuMuon);
        }
    }

    private String buildMaChiTietTra(String maPhieuTra, int index) {
        String maChiTietTra = "CTT_" + maPhieuTra + "_" + String.format("%02d", index);

        if (maChiTietTra.length() > 30) {
            throw new BusinessException("MĂ£ chi tiáº¿t tráº£ vÆ°á»£t quĂ¡ 30 kĂ½ tá»±");
        }

        return maChiTietTra;
    }

    private boolean existsById(String tableName, String idColumn, String value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?",
                Integer.class,
                value
        );

        return count != null && count > 0;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isBorrowingStatus(String status) {
        if (!hasText(status)) {
            return false;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("đang mượn")
                || normalized.equals("dang muon")
                || normalized.equals(STATUS_BORROWING.toLowerCase(Locale.ROOT))
                || (normalized.contains("ang") && normalized.contains("m"));
    }

    private ReturnCondition normalizeCondition(String condition) {
        if (!hasText(condition)) {
            throw new BusinessException("TĂ¬nh tráº¡ng khi tráº£ chá»‰ Ä‘Æ°á»£c lĂ  BĂ¬nh thÆ°á»ng, Há»ng hoáº·c Máº¥t");
        }
        String normalized = condition.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals(CONDITION_NORMAL.toLowerCase(Locale.ROOT))
                || normalized.equals("bình thường")
                || normalized.equals("binh thuong")
                || normalized.contains("b") && normalized.contains("nh") && normalized.contains("th")) {
            return ReturnCondition.NORMAL;
        }
        if (normalized.equals(CONDITION_DAMAGED.toLowerCase(Locale.ROOT))
                || normalized.equals("hỏng")
                || normalized.equals("hong")
                || normalized.contains("h") && normalized.contains("ng")) {
            return ReturnCondition.DAMAGED;
        }
        if (normalized.equals(CONDITION_LOST.toLowerCase(Locale.ROOT))
                || normalized.equals("mất")
                || normalized.equals("mat")
                || normalized.contains("m") && normalized.contains("t")) {
            return ReturnCondition.LOST;
        }
        throw new BusinessException("TĂ¬nh tráº¡ng khi tráº£ chá»‰ Ä‘Æ°á»£c lĂ  BĂ¬nh thÆ°á»ng, Há»ng hoáº·c Máº¥t");
    }

    private record ReturnAssessment(
            int soNgayTre,
            BigDecimal tienPhatTre,
            BigDecimal suggestedDamageFine,
            BigDecimal finalDamageFine,
            boolean adjusted,
            String adjustmentReason
    ) {}

    private record DamageFineRule(BigDecimal rate, BigDecimal minimum, BigDecimal maximum) {}

    private record CopyValue(String titleId, String titleName, BigDecimal value) {}

    private record ReturnContext(String readerId, String branchId) {}

    private enum ReturnCondition {
        NORMAL("B\u00ecnh th\u01b0\u1eddng"),
        DAMAGED("H\u1ecfng"),
        LOST("M\u1ea5t");

        private final String databaseValue;

        ReturnCondition(String databaseValue) {
            this.databaseValue = databaseValue;
        }

        private String databaseValue() {
            return databaseValue;
        }
    }
}

package com.library.backend.service;

import com.library.backend.dto.BookCopyBarcodeLabelRequest;
import com.library.backend.dto.BookCopyBarcodeLabelResponse;
import com.library.backend.dto.CuonSachListItemResponse;
import com.library.backend.entity.CuonSach;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.repository.CuonSachRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class BookCopyBarcodeLabelService {
    private final CuonSachRepository copyRepository;
    private final CuonSachListService copyListService;
    private final BranchAuthorizationService branchAuthorizationService;
    private final ActivityLogService activityLogService;

    public BookCopyBarcodeLabelService(
            CuonSachRepository copyRepository,
            CuonSachListService copyListService,
            BranchAuthorizationService branchAuthorizationService,
            ActivityLogService activityLogService
    ) {
        this.copyRepository = copyRepository;
        this.copyListService = copyListService;
        this.branchAuthorizationService = branchAuthorizationService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public BookCopyBarcodeLabelResponse preview(BookCopyBarcodeLabelRequest request, AuthUser user) {
        return buildLabels(request, user, false);
    }

    @Transactional
    public BookCopyBarcodeLabelResponse createPrintJob(BookCopyBarcodeLabelRequest request, AuthUser user) {
        BookCopyBarcodeLabelResponse response = buildLabels(request, user, true);
        activityLogService.logAsAccount(user.getMaTaiKhoan(), "PRINT_BOOK_COPY_LABELS", "BOOK_COPY_LABEL",
                response.printJobId(), "template=" + response.template() + ", total=" + response.total());
        return response;
    }

    private BookCopyBarcodeLabelResponse buildLabels(BookCopyBarcodeLabelRequest request, AuthUser user, boolean printJob) {
        if (user == null) throw new AccessDeniedException("Thieu tai khoan xac thuc");
        List<String> copyIds = sanitizeCopyIds(request.copyIds());
        int generated = 0;
        List<BookCopyBarcodeLabelResponse.Label> labels = new ArrayList<>();

        for (String copyId : copyIds) {
            CuonSach copy = copyRepository.findByIdForUpdate(copyId)
                    .orElseThrow(() -> validation("copyIds", "Khong tim thay cuon sach: " + copyId));
            requireCopyAccess(copy, user);
            boolean generatedThisCopy = false;
            if (isBlank(copy.getMaVach())) {
                if (!request.shouldGenerateMissing()) {
                    throw validation("copyIds", "Cuon sach " + copyId + " chua co barcode");
                }
                copy.setMaVach(newBarcode());
                generated++;
                generatedThisCopy = true;
            }
            CuonSach saved = generatedThisCopy ? copyRepository.saveAndFlush(copy) : copy;
            CuonSachListItemResponse detail = copyListService.getById(saved.getMaCuonSach(), user);
            labels.add(toLabel(detail));
        }

        String jobId = (printJob ? "PRINT-" : "PREVIEW-") + compactUuid();
        return new BookCopyBarcodeLabelResponse(jobId, request.effectiveTemplate().name(),
                labels.size(), generated, LocalDateTime.now(), labels);
    }

    private void requireCopyAccess(CuonSach copy, AuthUser user) {
        if (RoleConstants.ADMIN.equals(user.getTenVaiTro())) return;
        if (!RoleConstants.LIBRARIAN.equals(user.getTenVaiTro())) {
            throw new AccessDeniedException("Chi nhan vien moi duoc in nhan cuon sach");
        }
        branchAuthorizationService.requireAllowedBranch(user, copy.getMaChiNhanh());
    }

    private BookCopyBarcodeLabelResponse.Label toLabel(CuonSachListItemResponse copy) {
        return new BookCopyBarcodeLabelResponse.Label(
                copy.maCuonSach(),
                copy.maVach(),
                copy.tenDauSach(),
                copy.isbn(),
                copy.tenChiNhanh(),
                copy.maChiNhanh(),
                copy.viTriLabel(),
                copy.tenKhu(),
                copy.tenKeSach()
        );
    }

    private List<String> sanitizeCopyIds(List<String> copyIds) {
        if (copyIds == null) throw validation("copyIds", "Can chon it nhat mot cuon sach");
        List<String> sanitized = copyIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));
        if (sanitized.isEmpty()) throw validation("copyIds", "Can chon it nhat mot cuon sach");
        if (sanitized.size() > 100) throw validation("copyIds", "Moi lan in toi da 100 nhan");
        return sanitized;
    }

    private String newBarcode() {
        String barcode;
        do {
            barcode = "BC" + compactUuid();
        } while (copyRepository.existsByMaVach(barcode));
        return barcode;
    }

    private String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private CatalogValidationException validation(String field, String message) {
        return new CatalogValidationException(HttpStatus.BAD_REQUEST, "BARCODE_LABEL_VALIDATION_ERROR",
                message, Map.of(field, message), null);
    }
}

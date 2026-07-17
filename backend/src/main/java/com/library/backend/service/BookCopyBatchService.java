package com.library.backend.service;

import com.library.backend.dto.BookCopyBatchRequest;
import com.library.backend.dto.BookCopyBatchResponse;
import com.library.backend.entity.ChiNhanh;
import com.library.backend.entity.CuonSach;
import com.library.backend.entity.DauSach;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.repository.ChiNhanhRepository;
import com.library.backend.repository.CuonSachRepository;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.repository.TrangThaiCuonSachRepository;
import com.library.backend.repository.ViTriSachRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class BookCopyBatchService {
    static final String AVAILABLE_STATUS = "TT_SANCO";
    private static final int MAX_BATCH_SIZE = 100;

    private final CuonSachRepository copyRepository;
    private final DauSachRepository titleRepository;
    private final ChiNhanhRepository branchRepository;
    private final ViTriSachRepository locationRepository;
    private final TrangThaiCuonSachRepository statusRepository;
    private final BranchAuthorizationService branchAuthorizationService;
    private final ActivityLogService activityLogService;

    public BookCopyBatchService(
            CuonSachRepository copyRepository,
            DauSachRepository titleRepository,
            ChiNhanhRepository branchRepository,
            ViTriSachRepository locationRepository,
            TrangThaiCuonSachRepository statusRepository,
            BranchAuthorizationService branchAuthorizationService,
            ActivityLogService activityLogService
    ) {
        this.copyRepository = copyRepository;
        this.titleRepository = titleRepository;
        this.branchRepository = branchRepository;
        this.locationRepository = locationRepository;
        this.statusRepository = statusRepository;
        this.branchAuthorizationService = branchAuthorizationService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public BookCopyBatchResponse create(BookCopyBatchRequest request, AuthUser user) {
        validateActorAndBranch(user, request.branchId());
        DauSach title = titleRepository.findById(request.titleId())
                .orElseThrow(() -> validation("titleId", "Đầu sách không tồn tại"));
        if (!isActive(title.getTrangThai())) {
            throw new CatalogValidationException(HttpStatus.CONFLICT, "TITLE_INACTIVE",
                    "Đầu sách đã ngừng hiển thị, không thể nhập thêm cuốn",
                    Map.of("titleId", "Đầu sách phải đang hoạt động"), null);
        }
        if (!locationRepository.existsActiveInBranch(request.locationId(), request.branchId())) {
            throw validation("locationId", "Vị trí không hoạt động hoặc không thuộc chi nhánh đã chọn");
        }
        if (!statusRepository.existsById(AVAILABLE_STATUS)) {
            throw new IllegalStateException("Thiếu trạng thái TT_SANCO trong database");
        }

        List<CopyInput> inputs = resolveInputs(request);
        validateManualBarcodes(inputs, request.barcodeMode());
        String batchId = newBatchId();
        List<CuonSach> entities = IntStream.range(0, inputs.size())
                .mapToObj(index -> toEntity(request, inputs.get(index)))
                .toList();
        try {
            copyRepository.saveAllAndFlush(entities);
        } catch (DataIntegrityViolationException ex) {
            throw new CatalogValidationException(HttpStatus.CONFLICT, "BATCH_CODE_CONFLICT",
                    "Mã cuốn hoặc barcode đã tồn tại; toàn bộ lô không được tạo",
                    Map.of("copies", "Kiểm tra lại barcode trong lô"), null);
        }

        activityLogService.logAsAccount(user.getMaTaiKhoan(), "CREATE_BOOK_COPY_BATCH", "BOOK_COPY_BATCH",
                batchId, "title=" + request.titleId() + ", branch=" + request.branchId() + ", created=" + entities.size());
        List<BookCopyBatchResponse.CreatedCopy> created = entities.stream()
                .map(copy -> new BookCopyBatchResponse.CreatedCopy(copy.getMaCuonSach(), copy.getMaVach(), copy.getMaTrangThai()))
                .toList();
        return new BookCopyBatchResponse(batchId, created.size(), created,
                created.stream().allMatch(copy -> copy.barcode() != null));
    }

    private void validateActorAndBranch(AuthUser user, String branchId) {
        if (user == null) throw new org.springframework.security.access.AccessDeniedException("Thiếu tài khoản xác thực");
        ChiNhanh branch = branchRepository.findById(branchId)
                .orElseThrow(() -> validation("branchId", "Chi nhánh không tồn tại"));
        if (!isActive(branch.getTrangThai())) throw validation("branchId", "Chi nhánh đã ngừng hoạt động");
        if (!RoleConstants.ADMIN.equals(user.getTenVaiTro())) {
            branchAuthorizationService.requireAllowedBranch(user, branchId);
        }
    }

    private List<CopyInput> resolveInputs(BookCopyBatchRequest request) {
        if (request.barcodeMode() == BookCopyBatchRequest.BarcodeMode.MANUAL) {
            if (request.copies() == null || request.copies().isEmpty()) {
                throw validation("copies", "Chế độ nhập barcode thủ công cần ít nhất một dòng");
            }
            if (request.copies().size() > MAX_BATCH_SIZE) throw validation("copies", "Mỗi lô tối đa 100 cuốn");
            return request.copies().stream()
                    .map(copy -> new CopyInput(trim(copy.barcode()), trimToNull(copy.note())))
                    .toList();
        }
        if (request.quantity() == null || request.quantity() < 1 || request.quantity() > MAX_BATCH_SIZE) {
            throw validation("quantity", "Số lượng phải từ 1 đến 100");
        }
        return IntStream.range(0, request.quantity())
                .mapToObj(index -> new CopyInput(
                        request.barcodeMode() == BookCopyBatchRequest.BarcodeMode.AUTO ? newBarcode() : null,
                        trimToNull(request.note())))
                .toList();
    }

    private void validateManualBarcodes(List<CopyInput> inputs, BookCopyBatchRequest.BarcodeMode mode) {
        if (mode != BookCopyBatchRequest.BarcodeMode.MANUAL) return;
        Set<String> seen = new HashSet<>();
        for (int index = 0; index < inputs.size(); index++) {
            String barcode = inputs.get(index).barcode();
            if (!seen.add(barcode.toUpperCase(Locale.ROOT))) {
                throw duplicateBarcode(index, "Barcode bị trùng trong lô");
            }
            if (copyRepository.existsByMaVach(barcode)) {
                throw duplicateBarcode(index, "Barcode đã tồn tại trong hệ thống");
            }
        }
    }

    private CatalogValidationException duplicateBarcode(int index, String message) {
        return new CatalogValidationException(HttpStatus.CONFLICT, "DUPLICATE_BARCODE", message,
                Map.of("copies[" + index + "].barcode", message), Map.of("row", index));
    }

    private CuonSach toEntity(BookCopyBatchRequest request, CopyInput input) {
        CuonSach copy = new CuonSach();
        copy.setMaCuonSach(newCopyId());
        copy.setMaDauSach(request.titleId());
        copy.setMaChiNhanh(request.branchId());
        copy.setMaViTri(request.locationId());
        copy.setMaTrangThai(AVAILABLE_STATUS);
        copy.setMaVach(input.barcode());
        copy.setMaQrCode(null);
        copy.setNgayNhapSach(request.importDate());
        copy.setGhiChu(input.note());
        return copy;
    }

    private boolean isActive(String value) { return value != null && value.trim().equalsIgnoreCase("Hoạt động"); }
    private String newCopyId() { return "CS-" + compactUuid(); }
    private String newBarcode() { return "BC" + compactUuid(); }
    private String newBatchId() { return "BATCH-" + compactUuid(); }
    private String compactUuid() { return UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT); }
    private String trim(String value) { return value == null ? null : value.trim(); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private CatalogValidationException validation(String field, String message) {
        return new CatalogValidationException(HttpStatus.BAD_REQUEST, "BATCH_VALIDATION_ERROR", message, Map.of(field, message), null);
    }
    private record CopyInput(String barcode, String note) {}
}

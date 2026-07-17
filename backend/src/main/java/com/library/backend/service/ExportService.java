package com.library.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.backend.dto.CuonSachListItemResponse;
import com.library.backend.dto.CuonSachListQuery;
import com.library.backend.dto.DauSachListQuery;
import com.library.backend.dto.DauSachResponse;
import com.library.backend.dto.ExportJobResponse;
import com.library.backend.dto.ExportRequest;
import com.library.backend.dto.ExportResponse;
import com.library.backend.dto.PageResponse;
import com.library.backend.dto.ReaderListItemResponse;
import com.library.backend.dto.ReaderListQuery;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.security.AuthUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class ExportService {
    private static final int MAX_SYNC_ROWS = 1000;
    private static final int MAX_EXPORT_ROWS = 5000;
    private static final String CSV_MEDIA_TYPE = "text/csv;charset=UTF-8";
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final DauSachService dauSachService;
    private final CuonSachListService cuonSachListService;
    private final ReaderQueryService readerQueryService;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;
    private final Map<String, ExportJob> jobs = new ConcurrentHashMap<>();

    public ExportService(
            DauSachService dauSachService,
            CuonSachListService cuonSachListService,
            ReaderQueryService readerQueryService,
            ActivityLogService activityLogService,
            ObjectMapper objectMapper
    ) {
        this.dauSachService = dauSachService;
        this.cuonSachListService = cuonSachListService;
        this.readerQueryService = readerQueryService;
        this.activityLogService = activityLogService;
        this.objectMapper = objectMapper;
    }

    public ExportResponse exportBooks(ExportRequest request, AuthUser user) {
        List<String> ids = resolveBookIds(request);
        return createOrQueue("books", "BOOK", request.scope(), ids.size(), user,
                () -> bookCsv(dauSachService.getByIds(ids)));
    }

    public ExportResponse exportBookCopies(ExportRequest request, AuthUser user) {
        List<String> ids = resolveCopyIds(request, user);
        return createOrQueue("book-copies", "BOOK_COPY", request.scope(), ids.size(), user,
                () -> copyCsv(cuonSachListService.getByIds(ids, user)));
    }

    public ExportResponse exportReaders(ExportRequest request, AuthUser user) {
        List<String> ids = resolveReaderIds(request);
        return createOrQueue("readers", "READER", request.scope(), ids.size(), user,
                () -> readerCsv(readerQueryService.getByIds(ids)));
    }

    public ExportJobResponse getJob(String jobId) {
        throw new AccessDeniedException("Export job lookup requires an authenticated owner");
    }

    public ExportJobResponse getJob(String jobId, AuthUser user) {
        ExportJob job = jobs.get(jobId);
        if (job == null) throw new ResourceNotFoundException("Khong tim thay export job");
        String accountId = user == null ? null : user.getMaTaiKhoan();
        if (accountId == null || !accountId.equals(job.ownerAccountId())) {
            throw new AccessDeniedException("Khong co quyen xem export job nay");
        }
        return new ExportJobResponse(job.id(), job.status(), job.filename(), job.mediaType(),
                job.totalRows(), job.content(), job.message());
    }

    private ExportResponse createOrQueue(
            String filePrefix,
            String entity,
            ExportRequest.ScopeType scope,
            long totalRows,
            AuthUser user,
            Supplier<String> csvSupplier
    ) {
        enforceExportSize(totalRows);
        String filename = filePrefix + "-" + LocalDateTime.now().format(FILE_TIME) + ".csv";
        if (totalRows > MAX_SYNC_ROWS) {
            String jobId = UUID.randomUUID().toString();
            String ownerAccountId = user == null ? null : user.getMaTaiKhoan();
            ExportJob queued = new ExportJob(jobId, "QUEUED", filename, CSV_MEDIA_TYPE, totalRows, null,
                    "Export dang duoc tao o nen", ownerAccountId);
            jobs.put(jobId, queued);
            CompletableFuture.runAsync(() -> runJob(jobId, csvSupplier));
            audit(entity, scope, totalRows, user, "QUEUED:" + jobId);
            return ExportResponse.queued(jobId, totalRows, "Tap du lieu lon, da tao export job");
        }
        String csv = csvSupplier.get();
        audit(entity, scope, totalRows, user, "READY");
        return ExportResponse.ready(filename, CSV_MEDIA_TYPE, totalRows, csv);
    }

    private void runJob(String jobId, Supplier<String> csvSupplier) {
        ExportJob current = jobs.get(jobId);
        if (current == null) return;
        jobs.put(jobId, current.withStatus("RUNNING", null, "Export dang chay"));
        try {
            String content = csvSupplier.get();
            jobs.put(jobId, current.withStatus("READY", content, null));
        } catch (Exception ex) {
            jobs.put(jobId, current.withStatus("FAILED", null,
                    ex.getMessage() == null ? "Export that bai" : ex.getMessage()));
        }
    }

    private List<String> resolveBookIds(ExportRequest request) {
        return switch (request.scope()) {
            case SELECTED -> selectedIds(request.ids());
            case PAGE -> dauSachService.getPage(bookQuery(request.filters(), true)).items()
                    .stream().map(DauSachResponse::getMaDauSach).toList();
            case ALL_MATCHING -> dauSachService.getMatchingIds(
                    bookQuery(request.filters(), false),
                    cleanIds(request.excludedIds()),
                    MAX_EXPORT_ROWS + 1
            );
        };
    }

    private List<String> resolveCopyIds(ExportRequest request, AuthUser user) {
        return switch (request.scope()) {
            case SELECTED -> selectedIds(request.ids());
            case PAGE -> cuonSachListService.getPage(copyQuery(request.filters(), true), user).items()
                    .stream().map(CuonSachListItemResponse::maCuonSach).toList();
            case ALL_MATCHING -> cuonSachListService.getMatchingIds(
                    copyQuery(request.filters(), false),
                    cleanIds(request.excludedIds()),
                    user,
                    MAX_EXPORT_ROWS + 1
            );
        };
    }

    private List<String> resolveReaderIds(ExportRequest request) {
        return switch (request.scope()) {
            case SELECTED -> selectedIds(request.ids());
            case PAGE -> readerQueryService.getPage(readerQuery(request.filters(), true)).items()
                    .stream().map(ReaderListItemResponse::readerId).toList();
            case ALL_MATCHING -> readerQueryService.getMatchingIds(
                    readerQuery(request.filters(), false),
                    cleanIds(request.excludedIds()),
                    MAX_EXPORT_ROWS + 1
            );
        };
    }

    private DauSachListQuery bookQuery(Map<String, Object> filters, boolean keepPage) {
        DauSachListQuery query = objectMapper.convertValue(nonNullFilters(filters), DauSachListQuery.class);
        return new DauSachListQuery(keepPage ? query.page() : 1, keepPage ? query.pageSize() : 100,
                query.search(), defaultSort(query.sort(), "title,asc"), query.statusIds(), query.categoryIds(),
                query.authorIds(), query.publisherIds(), query.yearFrom(), query.yearTo(), query.language(),
                query.hasIsbn(), query.hasCover());
    }

    private CuonSachListQuery copyQuery(Map<String, Object> filters, boolean keepPage) {
        CuonSachListQuery query = objectMapper.convertValue(nonNullFilters(filters), CuonSachListQuery.class);
        return new CuonSachListQuery(keepPage ? query.page() : 1, keepPage ? query.pageSize() : 100,
                query.search(), defaultSort(query.sort(), "importedAt,desc"), cleanIds(query.statusIds()),
                cleanIds(query.branchIds()), cleanIds(query.titleIds()), cleanIds(query.areaIds()),
                cleanIds(query.shelfIds()), cleanIds(query.locationIds()), query.importedFrom(), query.importedTo(),
                query.hasBarcode(), query.hasQr());
    }

    private ReaderListQuery readerQuery(Map<String, Object> filters, boolean keepPage) {
        ReaderListQuery query = objectMapper.convertValue(nonNullFilters(filters), ReaderListQuery.class);
        return new ReaderListQuery(keepPage ? query.page() : 1, keepPage ? query.pageSize() : 100,
                query.search(), defaultSort(query.sort(), "fullName,asc"), cleanIds(query.groupIds()),
                cleanIds(query.planIds()), cleanIds(query.profileStatuses()), cleanIds(query.accountStatuses()),
                trimToNull(query.cardStatus()), trimToNull(query.membershipStatus()), query.cardExpiryFrom(),
                query.cardExpiryTo(), query.membershipExpiryFrom(), query.membershipExpiryTo(), query.locked());
    }

    private String bookCsv(List<DauSachResponse> rows) {
        CsvBuilder csv = new CsvBuilder("maDauSach", "tenDauSach", "isbn", "nhaXuatBan", "namXuatBan",
                "ngonNgu", "trangThai", "tacGia", "theLoai", "tongSoBan", "soBanSanCo");
        rows.forEach(row -> csv.row(row.getMaDauSach(), row.getTenDauSach(), row.getIsbn(),
                row.getTenNhaXuatBan(), row.getNamXuatBan(), row.getNgonNgu(), row.getTrangThai(),
                join(row.getTenTacGias()), join(row.getTenTheLoais()), row.getTongSoBan(), row.getSoBanSanCo()));
        return csv.toString();
    }

    private String copyCsv(List<CuonSachListItemResponse> rows) {
        CsvBuilder csv = new CsvBuilder("maCuonSach", "maDauSach", "tenDauSach", "isbn", "maChiNhanh",
                "tenChiNhanh", "viTri", "trangThai", "maVach", "maQrCode", "ngayNhapSach");
        rows.forEach(row -> csv.row(row.maCuonSach(), row.maDauSach(), row.tenDauSach(), row.isbn(),
                row.maChiNhanh(), row.tenChiNhanh(), row.viTriLabel(), row.tenTrangThai(), row.maVach(),
                row.maQrCode(), row.ngayNhapSach()));
        return csv.toString();
    }

    private String readerCsv(List<ReaderListItemResponse> rows) {
        CsvBuilder csv = new CsvBuilder("readerId", "fullName", "groupName", "profileStatus",
                "cardIssuedAt", "cardExpiresAt", "cardStatus", "planName", "membershipExpiresAt",
                "membershipStatus", "accountStatus", "currentLoans", "outstandingDebt");
        rows.forEach(row -> csv.row(row.readerId(), row.fullName(), row.groupName(), row.profileStatus(),
                row.cardIssuedAt(), row.cardExpiresAt(), row.cardStatus(), row.planName(),
                row.membershipExpiresAt(), row.membershipStatus(), row.accountStatus(), row.currentLoans(),
                money(row.outstandingDebt())));
        return csv.toString();
    }

    private void audit(String entity, ExportRequest.ScopeType scope, long totalRows, AuthUser user, String status) {
        String accountId = user == null ? null : user.getMaTaiKhoan();
        activityLogService.logAsAccountSafe(accountId, "EXPORT_" + entity, entity, "EXPORT",
                "scope=" + scope + ", rows=" + totalRows + ", status=" + status);
    }

    private List<String> selectedIds(List<String> ids) {
        List<String> clean = cleanIds(ids);
        if (clean.isEmpty()) throw new IllegalArgumentException("Danh sach ID export khong duoc de trong");
        return clean;
    }

    private void enforceExportSize(long totalRows) {
        if (totalRows > MAX_EXPORT_ROWS) {
            throw new BusinessException("Export toi da " + MAX_EXPORT_ROWS + " dong; hay thu hep filter");
        }
    }

    private List<String> cleanIds(List<String> values) {
        if (values == null) return List.of();
        return values.stream().map(this::trimToNull).filter(value -> value != null).distinct().toList();
    }

    private Map<String, Object> nonNullFilters(Map<String, Object> filters) {
        return filters == null ? Map.of() : filters;
    }

    private String defaultSort(String sort, String fallback) {
        String value = trimToNull(sort);
        return value == null ? fallback : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String join(List<String> values) {
        return values == null ? "" : String.join("; ", values);
    }

    private String money(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private record ExportJob(
            String id,
            String status,
            String filename,
            String mediaType,
            Long totalRows,
            String content,
            String message,
            String ownerAccountId
    ) {
        ExportJob withStatus(String nextStatus, String nextContent, String nextMessage) {
            return new ExportJob(id, nextStatus, filename, mediaType, totalRows, nextContent, nextMessage, ownerAccountId);
        }
    }

    private static final class CsvBuilder {
        private final StringBuilder builder = new StringBuilder("\uFEFF");

        CsvBuilder(String... headers) {
            row((Object[]) headers);
        }

        void row(Object... values) {
            for (int i = 0; i < values.length; i++) {
                if (i > 0) builder.append(',');
                builder.append(escape(values[i]));
            }
            builder.append('\n');
        }

        private String escape(Object value) {
            String text = value == null ? "" : String.valueOf(value);
            if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
                text = "'" + text;
            }
            boolean quote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
            String escaped = text.replace("\"", "\"\"");
            return quote ? "\"" + escaped + "\"" : escaped;
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
}

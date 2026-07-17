package com.library.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.backend.dto.BookCopyConditionRequest;
import com.library.backend.dto.BulkActionRequest;
import com.library.backend.dto.BulkActionResponse;
import com.library.backend.dto.BulkScopeRequest;
import com.library.backend.dto.CuonSachListQuery;
import com.library.backend.dto.DauSachListQuery;
import com.library.backend.dto.ReaderLifecycleRequest;
import com.library.backend.dto.ReaderListQuery;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.security.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BulkActionService {
    private static final int MAX_SYNC_ITEMS = 5000;

    private final DauSachService dauSachService;
    private final DauSachLifecycleService dauSachLifecycleService;
    private final CuonSachListService cuonSachListService;
    private final BookCopyActionService bookCopyActionService;
    private final ReaderQueryService readerQueryService;
    private final ReaderStateService readerStateService;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    public BulkActionService(
            DauSachService dauSachService,
            DauSachLifecycleService dauSachLifecycleService,
            CuonSachListService cuonSachListService,
            BookCopyActionService bookCopyActionService,
            ReaderQueryService readerQueryService,
            ReaderStateService readerStateService,
            ActivityLogService activityLogService,
            ObjectMapper objectMapper
    ) {
        this.dauSachService = dauSachService;
        this.dauSachLifecycleService = dauSachLifecycleService;
        this.cuonSachListService = cuonSachListService;
        this.bookCopyActionService = bookCopyActionService;
        this.readerQueryService = readerQueryService;
        this.readerStateService = readerStateService;
        this.activityLogService = activityLogService;
        this.objectMapper = objectMapper;
    }

    public BulkActionResponse applyBooks(BulkActionRequest request, AuthUser user) {
        BookAction action = enumValue(BookAction.class, request.action(), "action");
        List<String> ids = resolveBookIds(request);
        return execute("BOOK", action.name(), ids, request.reason(), user, id -> {
            switch (action) {
                case DEACTIVATE -> dauSachLifecycleService.deactivate(id, request.reason(), user);
                case REACTIVATE -> dauSachLifecycleService.reactivate(id, request.reason(), user);
                case HARD_DELETE -> dauSachLifecycleService.hardDelete(id, user);
            }
        });
    }

    public BulkActionResponse applyBookCopies(BulkActionRequest request, AuthUser user) {
        BookCopyConditionRequest.Action action = enumValue(BookCopyConditionRequest.Action.class, request.action(), "action");
        List<String> ids = resolveCopyIds(request, user);
        BookCopyConditionRequest itemRequest = new BookCopyConditionRequest(
                action,
                request.severity(),
                request.damageTypes(),
                request.description(),
                request.reason()
        );
        return execute("BOOK_COPY", action.name(), ids, request.reason(), user,
                id -> bookCopyActionService.applyCondition(id, itemRequest, user));
    }

    public BulkActionResponse applyReaders(BulkActionRequest request, AuthUser user) {
        ReaderAction action = enumValue(ReaderAction.class, request.action(), "action");
        List<String> ids = resolveReaderIds(request);
        ReaderLifecycleRequest itemRequest = new ReaderLifecycleRequest(request.reason());
        return execute("READER", action.name(), ids, request.reason(), user, id -> {
            switch (action) {
                case DEACTIVATE -> readerStateService.deactivate(id, itemRequest, user);
                case REACTIVATE -> readerStateService.reactivate(id, itemRequest, user);
            }
        });
    }

    private List<String> resolveBookIds(BulkActionRequest request) {
        BulkScopeRequest scope = request.scope();
        return switch (scope.type()) {
            case SELECTED_IDS -> selectedIds(scope.ids());
            case FILTERED_QUERY -> dauSachService.getMatchingIds(
                    bookQuery(scope.filters()),
                    cleanIds(scope.excludedIds()),
                    MAX_SYNC_ITEMS + 1
            );
        };
    }

    private List<String> resolveCopyIds(BulkActionRequest request, AuthUser user) {
        BulkScopeRequest scope = request.scope();
        return switch (scope.type()) {
            case SELECTED_IDS -> selectedIds(scope.ids());
            case FILTERED_QUERY -> cuonSachListService.getMatchingIds(
                    copyQuery(scope.filters()),
                    cleanIds(scope.excludedIds()),
                    user,
                    MAX_SYNC_ITEMS + 1
            );
        };
    }

    private List<String> resolveReaderIds(BulkActionRequest request) {
        BulkScopeRequest scope = request.scope();
        return switch (scope.type()) {
            case SELECTED_IDS -> selectedIds(scope.ids());
            case FILTERED_QUERY -> readerQueryService.getMatchingIds(
                    readerQuery(scope.filters()),
                    cleanIds(scope.excludedIds()),
                    MAX_SYNC_ITEMS + 1
            );
        };
    }

    private BulkActionResponse execute(
            String entity,
            String action,
            List<String> ids,
            String reason,
            AuthUser user,
            ItemOperation operation
    ) {
        enforceSize(ids);
        List<BulkActionResponse.ItemError> errors = new ArrayList<>();
        int succeeded = 0;
        for (String id : ids) {
            try {
                operation.apply(id);
                succeeded++;
            } catch (Exception ex) {
                errors.add(new BulkActionResponse.ItemError(id, errorCode(ex), safeMessage(ex)));
            }
        }
        BulkActionResponse response = new BulkActionResponse(ids.size(), succeeded, errors.size(), errors);
        auditSummary(entity, action, reason, user, response);
        return response;
    }

    private DauSachListQuery bookQuery(Map<String, Object> filters) {
        DauSachListQuery query = objectMapper.convertValue(nonNullFilters(filters), DauSachListQuery.class);
        return new DauSachListQuery(1, 100, query.search(), defaultSort(query.sort(), "title,asc"),
                query.statusIds(), query.categoryIds(), query.authorIds(), query.publisherIds(),
                query.yearFrom(), query.yearTo(), query.language(), query.hasIsbn(), query.hasCover());
    }

    private CuonSachListQuery copyQuery(Map<String, Object> filters) {
        CuonSachListQuery query = objectMapper.convertValue(nonNullFilters(filters), CuonSachListQuery.class);
        return new CuonSachListQuery(1, 100, query.search(), defaultSort(query.sort(), "importedAt,desc"),
                cleanIds(query.statusIds()), cleanIds(query.branchIds()), cleanIds(query.titleIds()),
                cleanIds(query.areaIds()), cleanIds(query.shelfIds()), cleanIds(query.locationIds()),
                query.importedFrom(), query.importedTo(), query.hasBarcode(), query.hasQr());
    }

    private ReaderListQuery readerQuery(Map<String, Object> filters) {
        ReaderListQuery query = objectMapper.convertValue(nonNullFilters(filters), ReaderListQuery.class);
        return new ReaderListQuery(1, 100, query.search(), defaultSort(query.sort(), "fullName,asc"),
                cleanIds(query.groupIds()), cleanIds(query.planIds()), cleanIds(query.profileStatuses()),
                cleanIds(query.accountStatuses()), trimToNull(query.cardStatus()), trimToNull(query.membershipStatus()),
                query.cardExpiryFrom(), query.cardExpiryTo(), query.membershipExpiryFrom(), query.membershipExpiryTo(),
                query.locked());
    }

    private void auditSummary(String entity, String action, String reason, AuthUser user, BulkActionResponse response) {
        String accountId = user == null ? null : user.getMaTaiKhoan();
        activityLogService.logAsAccountSafe(accountId, "BULK_" + entity + "_" + action, entity, "BULK",
                "requested=" + response.requested()
                        + ", succeeded=" + response.succeeded()
                        + ", failed=" + response.failed()
                        + ", reason=" + reason.trim());
    }

    private void enforceSize(List<String> ids) {
        if (ids.size() > MAX_SYNC_ITEMS) {
            throw validation("scope", "Bulk đồng bộ tối đa " + MAX_SYNC_ITEMS + " bản ghi; hãy thu hẹp filter");
        }
    }

    private List<String> selectedIds(List<String> ids) {
        List<String> clean = cleanIds(ids);
        if (clean.isEmpty()) {
            throw validation("scope.ids", "Danh sách ID được chọn không được để trống");
        }
        return clean;
    }

    private List<String> cleanIds(List<String> values) {
        if (values == null) return List.of();
        return values.stream()
                .map(this::trimToNull)
                .filter(value -> value != null)
                .distinct()
                .toList();
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

    private <E extends Enum<E>> E enumValue(Class<E> type, String value, String field) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw validation(field, "Action không được hỗ trợ: " + value);
        }
    }

    private String errorCode(Exception ex) {
        if (ex instanceof CatalogValidationException catalog) return catalog.getErrorCode();
        if (ex instanceof ResourceNotFoundException) return "NOT_FOUND";
        if (ex instanceof AccessDeniedException) return "ACCESS_DENIED";
        return "BUSINESS_ERROR";
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? "Thao tác không thành công" : message;
    }

    private CatalogValidationException validation(String field, String message) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put(field, message);
        return new CatalogValidationException(HttpStatus.BAD_REQUEST, "BULK_VALIDATION", message, fields, null);
    }

    private enum BookAction { DEACTIVATE, REACTIVATE, HARD_DELETE }
    private enum ReaderAction { DEACTIVATE, REACTIVATE }

    @FunctionalInterface
    private interface ItemOperation {
        void apply(String id);
    }
}

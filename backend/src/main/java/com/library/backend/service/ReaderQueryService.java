package com.library.backend.service;

import com.library.backend.dto.PageResponse;
import com.library.backend.dto.ReaderDebtItemResponse;
import com.library.backend.dto.ReaderListItemResponse;
import com.library.backend.dto.ReaderListQuery;
import com.library.backend.dto.ReaderLoanItemResponse;
import com.library.backend.dto.ReaderMembershipResponse;
import com.library.backend.dto.ReaderOverviewResponse;
import com.library.backend.dto.ReaderTransactionResponse;
import com.library.backend.dto.EntityPickerOptionResponse;
import com.library.backend.repository.ReaderPageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ReaderQueryService {
    private static final ZoneId LIBRARY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final ReaderPageRepository repository;

    public ReaderQueryService(ReaderPageRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ReaderListItemResponse> getPage(ReaderListQuery query) {
        validatePage(query.page(), query.pageSize());
        validateRange(query.cardExpiryFrom(), query.cardExpiryTo(), "hạn thẻ");
        validateRange(query.membershipExpiryFrom(), query.membershipExpiryTo(), "hạn gói");
        return repository.findPage(query, LocalDate.now(LIBRARY_ZONE));
    }

    @Transactional(readOnly = true)
    public List<String> getMatchingIds(ReaderListQuery query, List<String> excludedIds) {
        return getMatchingIds(query, excludedIds, null);
    }

    @Transactional(readOnly = true)
    public List<String> getMatchingIds(ReaderListQuery query, List<String> excludedIds, Integer maxRows) {
        validateRange(query.cardExpiryFrom(), query.cardExpiryTo(), "háº¡n tháº»");
        validateRange(query.membershipExpiryFrom(), query.membershipExpiryTo(), "háº¡n gĂ³i");
        return repository.findMatchingIds(query, LocalDate.now(LIBRARY_ZONE), excludedIds, maxRows);
    }

    @Transactional(readOnly = true)
    public List<ReaderListItemResponse> getByIds(List<String> readerIds) {
        if (readerIds == null || readerIds.isEmpty()) return List.of();
        List<String> cleanIds = readerIds.stream().distinct().toList();
        List<ReaderListItemResponse> rows = repository.findListItemsByIds(cleanIds, LocalDate.now(LIBRARY_ZONE));
        if (rows.size() != cleanIds.size()) {
            throw new IllegalArgumentException("Mot hoac nhieu doc gia khong ton tai hoac khong duoc phep export");
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public List<EntityPickerOptionResponse> searchForPicker(String query, int limit) {
        if (query == null || query.isBlank()) throw new IllegalArgumentException("Từ khóa tìm độc giả không được để trống");
        if (limit < 1 || limit > 50) throw new IllegalArgumentException("Giới hạn kết quả phải từ 1 đến 50");
        return repository.searchForPicker(query, limit);
    }

    @Transactional(readOnly = true)
    public ReaderOverviewResponse getOverview(String readerId) {
        return repository.findOverview(readerId, LocalDate.now(LIBRARY_ZONE));
    }

    @Transactional(readOnly = true)
    public List<ReaderMembershipResponse> getMemberships(String readerId) {
        getOverview(readerId);
        return repository.findMemberships(readerId, LocalDate.now(LIBRARY_ZONE));
    }

    @Transactional(readOnly = true)
    public List<ReaderLoanItemResponse> getCurrentLoans(String readerId) {
        getOverview(readerId);
        return repository.findCurrentLoans(readerId, LocalDateTime.now(LIBRARY_ZONE));
    }

    @Transactional(readOnly = true)
    public List<ReaderDebtItemResponse> getDebts(String readerId) {
        getOverview(readerId);
        return repository.findDebts(readerId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReaderTransactionResponse> getTransactions(String readerId, int page, int pageSize) {
        validatePage(page, pageSize);
        getOverview(readerId);
        return repository.findTransactions(readerId, page, pageSize);
    }

    private void validatePage(int page, int pageSize) {
        if (page < 1) throw new IllegalArgumentException("Page phải bắt đầu từ 1");
        if (pageSize < 1 || pageSize > 100) throw new IllegalArgumentException("Page size phải từ 1 đến 100");
    }

    private void validateRange(LocalDate from, LocalDate to, String label) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Ngày bắt đầu " + label + " không được lớn hơn ngày kết thúc");
        }
    }
}

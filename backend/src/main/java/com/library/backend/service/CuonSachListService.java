package com.library.backend.service;

import com.library.backend.dto.BookCopyLocationFiltersResponse;
import com.library.backend.dto.CuonSachListItemResponse;
import com.library.backend.dto.CuonSachListQuery;
import com.library.backend.dto.PageResponse;
import com.library.backend.dto.StaffContextResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.repository.CuonSachPageRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class CuonSachListService {
    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "title", "branch", "location", "status", "importedAt", "barcode"
    );

    private final CuonSachPageRepository pageRepository;
    private final StaffContextService staffContextService;

    public CuonSachListService(CuonSachPageRepository pageRepository, StaffContextService staffContextService) {
        this.pageRepository = pageRepository;
        this.staffContextService = staffContextService;
    }

    @Transactional(readOnly = true)
    public PageResponse<CuonSachListItemResponse> getPage(CuonSachListQuery query, AuthUser user) {
        validateQuery(query);
        BranchScope scope = resolveScope(user, query.branchIds());
        if (!scope.unrestricted() && scope.branches().isEmpty()) {
            return new PageResponse<>(List.of(), query.page(), query.pageSize(), 0, 0);
        }
        return pageRepository.findPage(query, scope.branches(), scope.unrestricted());
    }

    @Transactional(readOnly = true)
    public List<CuonSachListItemResponse> getAllLegacy(AuthUser user) {
        throw new BusinessException("Danh sach cuon sach khong phan trang da bi vo hieu hoa; hay dung endpoint phan trang");
    }

    @Transactional(readOnly = true)
    public CuonSachListItemResponse getById(String copyId, AuthUser user) {
        BranchScope scope = resolveScope(user, List.of());
        return pageRepository.findById(copyId, scope.branches(), scope.unrestricted())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn sách hoặc không có quyền truy cập"));
    }

    @Transactional(readOnly = true)
    public List<CuonSachListItemResponse> getByIds(List<String> copyIds, AuthUser user) {
        if (copyIds == null || copyIds.isEmpty()) return List.of();
        return copyIds.stream()
                .distinct()
                .map(copyId -> getById(copyId, user))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getMatchingIds(CuonSachListQuery query, List<String> excludedIds, AuthUser user) {
        return getMatchingIds(query, excludedIds, user, null);
    }

    @Transactional(readOnly = true)
    public List<String> getMatchingIds(CuonSachListQuery query, List<String> excludedIds, AuthUser user, Integer maxRows) {
        validateQuery(query);
        BranchScope scope = resolveScope(user, query.branchIds());
        return pageRepository.findMatchingIds(query, scope.branches(), scope.unrestricted(), excludedIds, maxRows);
    }

    @Transactional(readOnly = true)
    public BookCopyLocationFiltersResponse getLocationFilters(List<String> requestedBranches, AuthUser user) {
        BranchScope scope = resolveScope(user, requestedBranches);
        return pageRepository.findLocationOptions(scope.branches(), scope.unrestricted() && isEmpty(requestedBranches));
    }

    private BranchScope resolveScope(AuthUser user, List<String> requestedBranches) {
        if (user == null) throw new AccessDeniedException("Thiếu thông tin tài khoản");
        if (RoleConstants.ADMIN.equals(user.getTenVaiTro())) {
            return new BranchScope(requestedBranches == null ? List.of() : requestedBranches, true);
        }
        if (!RoleConstants.LIBRARIAN.equals(user.getTenVaiTro())) {
            throw new AccessDeniedException("Chỉ nhân viên được xem danh sách cuốn sách");
        }
        StaffContextResponse context = staffContextService.getContext(user);
        List<String> allowed = context.allowedBranches().stream().map(StaffContextResponse.BranchSummary::id).toList();
        if (isEmpty(requestedBranches)) return new BranchScope(allowed, false);
        List<String> invalid = requestedBranches.stream().filter(branch -> !allowed.contains(branch)).distinct().toList();
        if (!invalid.isEmpty()) throw new AccessDeniedException("Không có quyền xem chi nhánh: " + String.join(", ", invalid));
        return new BranchScope(requestedBranches.stream().distinct().toList(), false);
    }

    private void validateQuery(CuonSachListQuery query) {
        if (query.page() < 1) throw new IllegalArgumentException("Page phải bắt đầu từ 1");
        if (query.pageSize() < 1 || query.pageSize() > 100) throw new IllegalArgumentException("Page size phải từ 1 đến 100");
        if (query.importedFrom() != null && query.importedTo() != null && query.importedFrom().isAfter(query.importedTo())) {
            throw new IllegalArgumentException("Ngày nhập từ không được lớn hơn ngày nhập đến");
        }
        String[] sort = query.sort() == null ? new String[0] : query.sort().split(",", 2);
        if (sort.length != 2 || !SORT_FIELDS.contains(sort[0]) || !("asc".equals(sort[1]) || "desc".equals(sort[1]))) {
            throw new IllegalArgumentException("Sort cuốn sách không hợp lệ");
        }
    }

    private boolean isEmpty(List<String> values) { return values == null || values.isEmpty(); }

    private record BranchScope(List<String> branches, boolean unrestricted) {}
}

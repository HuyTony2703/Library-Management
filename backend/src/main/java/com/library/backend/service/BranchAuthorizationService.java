package com.library.backend.service;

import com.library.backend.dto.StaffContextResponse;
import com.library.backend.security.AuthUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class BranchAuthorizationService {

    private final StaffContextService staffContextService;

    public BranchAuthorizationService(StaffContextService staffContextService) {
        this.staffContextService = staffContextService;
    }

    public StaffContextResponse requireAllowedBranch(AuthUser user, String branchId) {
        if (branchId == null || branchId.isBlank()) {
            throw new AccessDeniedException("Chi nhánh không hợp lệ");
        }

        StaffContextResponse context = staffContextService.getContext(user);
        if (!context.operational()) {
            throw new AccessDeniedException("Tài khoản chưa có staff context để thao tác nghiệp vụ");
        }

        boolean allowed = context.allowedBranches().stream()
                .anyMatch(branch -> branch.id().equals(branchId));

        if (!allowed) {
            throw new AccessDeniedException("Không có quyền thao tác tại chi nhánh: " + branchId);
        }

        return context;
    }

    public boolean canAccessBranch(AuthUser user, String branchId) {
        try {
            requireAllowedBranch(user, branchId);
            return true;
        } catch (AccessDeniedException ex) {
            return false;
        }
    }
}

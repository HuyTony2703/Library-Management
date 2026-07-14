package com.library.backend.dto;

import java.util.List;

public record StaffContextResponse(
        String accountId,
        String staffId,
        String staffName,
        RoleSummary role,
        BranchSummary defaultBranch,
        List<BranchSummary> allowedBranches,
        List<String> permissions,
        boolean operational,
        String operationalBlockReason
) {
    public StaffContextResponse {
        allowedBranches = allowedBranches == null ? List.of() : List.copyOf(allowedBranches);
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }

    public record RoleSummary(String id, String name) {}

    public record BranchSummary(String id, String name) {}
}

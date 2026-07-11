package com.library.backend.dto;

public record LocationFilterOptionResponse(
        String value,
        String label,
        String branchId,
        String parentId
) {
}

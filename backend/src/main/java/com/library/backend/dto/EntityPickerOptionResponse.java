package com.library.backend.dto;

import java.util.Map;

public record EntityPickerOptionResponse(
        String value,
        String label,
        String code,
        Map<String, Object> metadata,
        boolean exactMatch
) {
}

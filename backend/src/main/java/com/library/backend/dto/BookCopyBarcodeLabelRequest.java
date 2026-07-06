package com.library.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BookCopyBarcodeLabelRequest(
        @NotEmpty(message = "Can chon it nhat mot cuon sach")
        @Size(max = 100, message = "Moi lan in toi da 100 nhan")
        List<String> copyIds,
        LabelTemplate template,
        Boolean generateMissing
) {
    public enum LabelTemplate {
        STANDARD,
        COMPACT
    }

    public LabelTemplate effectiveTemplate() {
        return template == null ? LabelTemplate.STANDARD : template;
    }

    public boolean shouldGenerateMissing() {
        return generateMissing == null || generateMissing;
    }
}

package com.library.backend.dto;

public record ExportJobResponse(
        String jobId,
        String status,
        String filename,
        String mediaType,
        Long totalRows,
        String content,
        String message
) {
}

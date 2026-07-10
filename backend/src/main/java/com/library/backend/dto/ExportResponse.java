package com.library.backend.dto;

public record ExportResponse(
        String status,
        String jobId,
        String filename,
        String mediaType,
        Long totalRows,
        String content,
        String message
) {
    public static ExportResponse ready(String filename, String mediaType, long totalRows, String content) {
        return new ExportResponse("READY", null, filename, mediaType, totalRows, content, null);
    }

    public static ExportResponse queued(String jobId, long totalRows, String message) {
        return new ExportResponse("QUEUED", jobId, null, null, totalRows, null, message);
    }
}

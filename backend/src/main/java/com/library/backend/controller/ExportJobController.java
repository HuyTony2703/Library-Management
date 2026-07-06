package com.library.backend.controller;

import com.library.backend.dto.ExportJobResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.ExportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export-jobs")
public class ExportJobController {
    private final ExportService exportService;

    public ExportJobController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
    public ExportJobResponse getJob(@PathVariable String jobId, @AuthenticationPrincipal AuthUser user) {
        return exportService.getJob(jobId, user);
    }
}

package com.library.backend.controller.admin;

import com.library.backend.dto.AdminLibrarianCreateRequest;
import com.library.backend.dto.AdminLibrarianResponse;
import com.library.backend.dto.AdminLibrarianStatusRequest;
import com.library.backend.dto.AdminLibrarianUpdateRequest;
import com.library.backend.dto.ResetPasswordRequest;
import com.library.backend.service.admin.AdminLibrarianService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/librarians")
@PreAuthorize("hasRole('QUAN_TRI_VIEN')")
public class AdminLibrarianController {

    private final AdminLibrarianService adminLibrarianService;

    public AdminLibrarianController(AdminLibrarianService adminLibrarianService) {
        this.adminLibrarianService = adminLibrarianService;
    }

    @GetMapping
    public List<AdminLibrarianResponse> getAll() {
        return adminLibrarianService.getAll();
    }

    @GetMapping("/{maNhanVien}")
    public AdminLibrarianResponse getById(@PathVariable String maNhanVien) {
        return adminLibrarianService.getById(maNhanVien);
    }

    @PostMapping
    public AdminLibrarianResponse create(@RequestBody AdminLibrarianCreateRequest request) {
        return adminLibrarianService.create(request);
    }

    @PutMapping("/{maNhanVien}")
    public AdminLibrarianResponse update(
            @PathVariable String maNhanVien,
            @RequestBody AdminLibrarianUpdateRequest request
    ) {
        return adminLibrarianService.update(maNhanVien, request);
    }

    @PatchMapping("/{maNhanVien}/status")
    public AdminLibrarianResponse updateStatus(
            @PathVariable String maNhanVien,
            @RequestBody AdminLibrarianStatusRequest request
    ) {
        return adminLibrarianService.updateStatus(maNhanVien, request);
    }

    @PostMapping("/{maNhanVien}/reset-password")
    public void resetPassword(
            @PathVariable String maNhanVien,
            @RequestBody ResetPasswordRequest request
    ) {
        adminLibrarianService.resetPassword(maNhanVien, request);
    }

    @DeleteMapping("/{maNhanVien}")
    public void delete(
            @PathVariable String maNhanVien,
            @RequestParam(defaultValue = "soft") String mode
    ) {
        if ("hard".equalsIgnoreCase(mode)) {
            adminLibrarianService.hardDelete(maNhanVien);
            return;
        }

        adminLibrarianService.delete(maNhanVien);
    }
}

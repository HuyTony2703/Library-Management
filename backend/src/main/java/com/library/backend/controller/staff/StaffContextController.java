package com.library.backend.controller.staff;

import com.library.backend.dto.StaffContextResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.StaffContextService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/me")
public class StaffContextController {

    private final StaffContextService staffContextService;

    public StaffContextController(StaffContextService staffContextService) {
        this.staffContextService = staffContextService;
    }

    @GetMapping("/context")
    public StaffContextResponse getContext(Authentication authentication) {
        return staffContextService.getContext((AuthUser) authentication.getPrincipal());
    }
}

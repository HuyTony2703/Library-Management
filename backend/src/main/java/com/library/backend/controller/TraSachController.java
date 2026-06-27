package com.library.backend.controller;

import com.library.backend.dto.TraSachRequest;
import com.library.backend.dto.TraSachResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.TraSachService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
public class TraSachController {

    private final TraSachService traSachService;

    public TraSachController(TraSachService traSachService) {
        this.traSachService = traSachService;
    }

    @PostMapping
    public TraSachResponse create(
            Authentication authentication,
            @Valid @RequestBody TraSachRequest request
    ) {
        return traSachService.create(request, (AuthUser) authentication.getPrincipal());
    }

    @GetMapping("/{maPhieuTra}")
    public TraSachResponse getById(@PathVariable String maPhieuTra) {
        return traSachService.getById(maPhieuTra);
    }
}

package com.library.backend.controller;

import com.library.backend.dto.MuonSachRequest;
import com.library.backend.dto.MuonSachResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.MuonSachService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MuonSachController {

    private final MuonSachService muonSachService;

    public MuonSachController(MuonSachService muonSachService) {
        this.muonSachService = muonSachService;
    }

    @PostMapping("/loans")
    public MuonSachResponse create(
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody MuonSachRequest request
    ) {
        return muonSachService.create(
                request,
                (AuthUser) authentication.getPrincipal(),
                idempotencyKey
        );
    }

    @GetMapping("/loans/{maPhieuMuon}")
    public MuonSachResponse getById(@PathVariable String maPhieuMuon) {
        return muonSachService.getById(maPhieuMuon);
    }

    @GetMapping("/readers/{maDocGia}/current-loans")
    public List<MuonSachResponse.ChiTietMuonResponse> getCurrentLoans(
            @PathVariable String maDocGia
    ) {
        return muonSachService.getCurrentLoansByReader(maDocGia);
    }
}

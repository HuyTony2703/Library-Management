package com.library.backend.controller.reader;

import com.library.backend.dto.reader.ReaderCurrentLoanResponse;
import com.library.backend.dto.reader.ReaderRenewalResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.reader.ReaderRenewalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reader/loans")
public class ReaderLoanController {

    private final ReaderRenewalService readerRenewalService;

    public ReaderLoanController(ReaderRenewalService readerRenewalService) {
        this.readerRenewalService = readerRenewalService;
    }

    @GetMapping("/current")
    public List<ReaderCurrentLoanResponse> getCurrentLoans(
            @AuthenticationPrincipal AuthUser user
    ) {
        return readerRenewalService.getCurrentLoans(user);
    }

    @PostMapping("/{maChiTietMuon}/renew")
    public ReaderRenewalResponse renew(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maChiTietMuon
    ) {
        return readerRenewalService.renew(user, maChiTietMuon);
    }

    @GetMapping("/renewal-history")
    public List<ReaderRenewalResponse> getRenewalHistory(
            @AuthenticationPrincipal AuthUser user
    ) {
        return readerRenewalService.getRenewalHistory(user);
    }
}

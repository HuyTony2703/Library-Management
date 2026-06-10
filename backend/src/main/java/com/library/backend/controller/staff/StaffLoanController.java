package com.library.backend.controller.staff;

import com.library.backend.dto.MuonSachRequest;
import com.library.backend.dto.MuonSachResponse;
import com.library.backend.service.MuonSachService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
public class StaffLoanController {

    private final MuonSachService muonSachService;

    public StaffLoanController(MuonSachService muonSachService) {
        this.muonSachService = muonSachService;
    }

    @PostMapping("/loans")
    public MuonSachResponse createLoan(@Valid @RequestBody MuonSachRequest request) {
        return muonSachService.create(request);
    }

    @GetMapping("/loans/{maPhieuMuon}")
    public MuonSachResponse getLoan(@PathVariable String maPhieuMuon) {
        return muonSachService.getById(maPhieuMuon);
    }

    @GetMapping("/readers/{maDocGia}/current-loans")
    public List<MuonSachResponse.ChiTietMuonResponse> getCurrentLoansByReader(
            @PathVariable String maDocGia
    ) {
        return muonSachService.getCurrentLoansByReader(maDocGia);
    }
}

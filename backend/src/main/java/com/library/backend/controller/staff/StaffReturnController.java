package com.library.backend.controller.staff;

import com.library.backend.dto.TraSachRequest;
import com.library.backend.dto.TraSachResponse;
import com.library.backend.service.TraSachService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
public class StaffReturnController {

    private final TraSachService traSachService;

    public StaffReturnController(TraSachService traSachService) {
        this.traSachService = traSachService;
    }

    @PostMapping("/returns")
    public TraSachResponse createReturn(@Valid @RequestBody TraSachRequest request) {
        return traSachService.create(request);
    }

    @GetMapping("/returns/{maPhieuTra}")
    public TraSachResponse getReturn(@PathVariable String maPhieuTra) {
        return traSachService.getById(maPhieuTra);
    }
}

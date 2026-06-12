package com.library.backend.controller;

import com.library.backend.dto.CuonSachRequest;
import com.library.backend.dto.CuonSachResponse;
import com.library.backend.service.CuonSachService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-copies")
public class CuonSachController {

    private final CuonSachService cuonSachService;

    public CuonSachController(CuonSachService cuonSachService) {
        this.cuonSachService = cuonSachService;
    }

    @GetMapping
    public List<CuonSachResponse> getAll() {
        return cuonSachService.getAll();
    }

    @GetMapping("/{maCuonSach}")
    public CuonSachResponse getById(@PathVariable String maCuonSach) {
        return cuonSachService.getById(maCuonSach);
    }

    @PostMapping
    public CuonSachResponse create(@Valid @RequestBody CuonSachRequest request) {
        return cuonSachService.create(request);
    }

    @PutMapping("/{maCuonSach}")
    public CuonSachResponse update(
            @PathVariable String maCuonSach,
            @Valid @RequestBody CuonSachRequest request
    ) {
        return cuonSachService.update(maCuonSach, request);
    }

    @DeleteMapping("/{maCuonSach}")
    public String disable(
            @PathVariable String maCuonSach,
            @RequestParam(defaultValue = "soft") String mode
    ) {
        if ("hard".equalsIgnoreCase(mode)) {
            cuonSachService.hardDelete(maCuonSach);
            return "Xóa cuốn sách thành công";
        }

        cuonSachService.disable(maCuonSach);
        return "Ngừng lưu thông cuốn sách thành công";
    }

    @PatchMapping("/{maCuonSach}/restore")
    public String restore(@PathVariable String maCuonSach) {
        cuonSachService.restore(maCuonSach);
        return "Khôi phục cuốn sách thành công";
    }
}

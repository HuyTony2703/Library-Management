package com.library.backend.controller;

import com.library.backend.dto.DauSachRequest;
import com.library.backend.dto.DauSachResponse;
import com.library.backend.service.DauSachService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class DauSachController {

    private final DauSachService dauSachService;

    public DauSachController(DauSachService dauSachService) {
        this.dauSachService = dauSachService;
    }

    @GetMapping
    public List<DauSachResponse> getAll() {
        return dauSachService.getAll();
    }

    @GetMapping("/{maDauSach}")
    public DauSachResponse getById(@PathVariable String maDauSach) {
        return dauSachService.getById(maDauSach);
    }

    @PostMapping
    public DauSachResponse create(@Valid @RequestBody DauSachRequest request) {
        return dauSachService.create(request);
    }

    @PutMapping("/{maDauSach}")
    public DauSachResponse update(
            @PathVariable String maDauSach,
            @Valid @RequestBody DauSachRequest request
    ) {
        return dauSachService.update(maDauSach, request);
    }

    @DeleteMapping("/{maDauSach}")
    public String disable(
            @PathVariable String maDauSach,
            @RequestParam(defaultValue = "soft") String mode
    ) {
        if ("hard".equalsIgnoreCase(mode)) {
            dauSachService.hardDelete(maDauSach);
            return "Xóa đầu sách thành công";
        }

        dauSachService.disable(maDauSach);
        return "Ngừng hiển thị đầu sách thành công";
    }
}

package com.library.backend.controller;

import com.library.backend.dto.DocGiaRequest;
import com.library.backend.dto.DocGiaResponse;
import com.library.backend.dto.ReaderMembershipUpdateRequest;
import com.library.backend.service.DocGiaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readers")
public class DocGiaController {

    private final DocGiaService docGiaService;

    public DocGiaController(DocGiaService docGiaService) {
        this.docGiaService = docGiaService;
    }

    @GetMapping
    public List<DocGiaResponse> getAll() {
        return docGiaService.getAll();
    }

    @GetMapping("/{maDocGia}")
    public DocGiaResponse getById(@PathVariable String maDocGia) {
        return docGiaService.getById(maDocGia);
    }

    @PostMapping
    public DocGiaResponse create(@Valid @RequestBody DocGiaRequest request) {
        return docGiaService.create(request);
    }

    @PutMapping("/{maDocGia}")
    public DocGiaResponse update(
            @PathVariable String maDocGia,
            @Valid @RequestBody DocGiaRequest request
    ) {
        return docGiaService.update(maDocGia, request);
    }

    @PatchMapping("/{maDocGia}/membership")
    public DocGiaResponse updateMembershipPlan(
            @PathVariable String maDocGia,
            @Valid @RequestBody ReaderMembershipUpdateRequest request
    ) {
        return docGiaService.updateMembershipPlan(maDocGia, request.getMaGoiThanhVien());
    }

    @DeleteMapping("/{maDocGia}")
    public String disable(
            @PathVariable String maDocGia,
            @RequestParam(defaultValue = "soft") String mode
    ) {
        if ("hard".equalsIgnoreCase(mode)) {
            docGiaService.hardDelete(maDocGia);
            return "Xóa độc giả thành công";
        }

        docGiaService.disable(maDocGia);
        return "Ngừng hoạt động độc giả thành công";
    }

    @PatchMapping("/{maDocGia}/restore")
    public String restore(@PathVariable String maDocGia) {
        docGiaService.restore(maDocGia);
        return "Khôi phục độc giả thành công";
    }
}

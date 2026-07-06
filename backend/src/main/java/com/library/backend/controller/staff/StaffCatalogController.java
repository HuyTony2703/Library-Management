package com.library.backend.controller.staff;

import com.library.backend.dto.EntityPickerOptionResponse;
import com.library.backend.service.DauSachService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff/catalog")
@Validated
public class StaffCatalogController {

    private final DauSachService dauSachService;

    public StaffCatalogController(DauSachService dauSachService) {
        this.dauSachService = dauSachService;
    }

    @GetMapping("/titles/search")
    public List<EntityPickerOptionResponse> search(
            @RequestParam @NotBlank @Size(min = 2, max = 100) String q,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int limit,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return dauSachService.searchForPicker(q, limit, activeOnly);
    }

    @GetMapping("/authors/search")
    public List<EntityPickerOptionResponse> searchAuthors(
            @RequestParam @NotBlank @Size(min = 2, max = 100) String q,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int limit
    ) {
        return dauSachService.searchAuthors(q, limit);
    }

    @GetMapping("/categories/search")
    public List<EntityPickerOptionResponse> searchCategories(
            @RequestParam @NotBlank @Size(min = 2, max = 100) String q,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int limit
    ) {
        return dauSachService.searchCategories(q, limit);
    }

    @GetMapping("/publishers/search")
    public List<EntityPickerOptionResponse> searchPublishers(
            @RequestParam @NotBlank @Size(min = 2, max = 100) String q,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int limit
    ) {
        return dauSachService.searchPublishers(q, limit);
    }
}

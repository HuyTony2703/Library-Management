package com.library.backend.controller.staff;

import com.library.backend.dto.ReaderEligibilityResponse;
import com.library.backend.dto.EntityPickerOptionResponse;
import com.library.backend.dto.ReaderLoanItemResponse;
import com.library.backend.service.ReaderQueryService;
import com.library.backend.service.ReaderStateService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/staff/readers")
@PreAuthorize("hasAnyRole('THU_THU', 'QUAN_TRI_VIEN')")
public class StaffReaderContextController {
    private final ReaderStateService readerStateService;
    private final ReaderQueryService readerQueryService;

    public StaffReaderContextController(ReaderStateService readerStateService, ReaderQueryService readerQueryService) {
        this.readerStateService = readerStateService;
        this.readerQueryService = readerQueryService;
    }

    @GetMapping("/search")
    public List<EntityPickerOptionResponse> search(@RequestParam("q") String query,
                                                    @RequestParam(defaultValue = "15") @Min(1) @Max(50) int limit) {
        return readerQueryService.searchForPicker(query, limit);
    }

    @GetMapping("/{readerId}/borrowing-context")
    public ReaderEligibilityResponse borrowingContext(@PathVariable String readerId) {
        return readerStateService.eligibility(readerId);
    }

    @GetMapping("/{readerId}/current-loans")
    public List<ReaderLoanItemResponse> currentLoans(@PathVariable String readerId) {
        return readerQueryService.getCurrentLoans(readerId);
    }
}

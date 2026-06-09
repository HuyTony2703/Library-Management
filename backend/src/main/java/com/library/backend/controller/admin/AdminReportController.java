package com.library.backend.controller.admin;

import com.library.backend.dto.AdminReportResponses.BorrowByCategoryReportResponse;
import com.library.backend.dto.AdminReportResponses.CurrentLoanReportResponse;
import com.library.backend.dto.AdminReportResponses.DebtReportResponse;
import com.library.backend.dto.AdminReportResponses.LateReturnReportResponse;
import com.library.backend.dto.AdminReportResponses.OverviewResponse;
import com.library.backend.dto.AdminReportResponses.PaymentReportResponse;
import com.library.backend.service.admin.AdminReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping("/overview")
    public OverviewResponse getOverview(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return adminReportService.getOverview(month, year);
    }

    @GetMapping("/debts")
    public List<DebtReportResponse> getDebts() {
        return adminReportService.getDebts();
    }

    @GetMapping("/current-loans")
    public List<CurrentLoanReportResponse> getCurrentLoans() {
        return adminReportService.getCurrentLoans();
    }

    @GetMapping("/borrow-by-category")
    public List<BorrowByCategoryReportResponse> getBorrowByCategory(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return adminReportService.getBorrowByCategory(month, year);
    }

    @GetMapping("/late-returns")
    public List<LateReturnReportResponse> getLateReturns(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return adminReportService.getLateReturns(month, year);
    }

    @GetMapping("/payments")
    public List<PaymentReportResponse> getPayments(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return adminReportService.getPayments(month, year);
    }
}

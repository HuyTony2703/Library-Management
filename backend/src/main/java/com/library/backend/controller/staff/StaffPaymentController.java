package com.library.backend.controller.staff;

import com.library.backend.dto.KhoanNoResponse;
import com.library.backend.dto.PhieuThuRequest;
import com.library.backend.dto.PhieuThuResponse;
import com.library.backend.service.ThanhToanService;
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
public class StaffPaymentController {

    private final ThanhToanService thanhToanService;

    public StaffPaymentController(ThanhToanService thanhToanService) {
        this.thanhToanService = thanhToanService;
    }

    @GetMapping("/readers/{maDocGia}/debts")
    public List<KhoanNoResponse> getReaderDebts(@PathVariable String maDocGia) {
        return thanhToanService.getDebtsByReader(maDocGia);
    }

    @PostMapping("/payments")
    public PhieuThuResponse createPayment(@Valid @RequestBody PhieuThuRequest request) {
        return thanhToanService.createPayment(request);
    }

    @GetMapping("/payments/{maPhieuThu}")
    public PhieuThuResponse getPayment(@PathVariable String maPhieuThu) {
        return thanhToanService.getPaymentById(maPhieuThu);
    }

    @GetMapping("/readers/{maDocGia}/payments")
    public List<PhieuThuResponse> getReaderPayments(@PathVariable String maDocGia) {
        return thanhToanService.getPaymentsByReader(maDocGia);
    }
}

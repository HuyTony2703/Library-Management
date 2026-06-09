package com.library.backend.controller.reader;

import com.library.backend.dto.reader.MembershipPlanResponse;
import com.library.backend.dto.reader.MembershipPurchaseRequest;
import com.library.backend.dto.reader.MembershipPurchaseResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.security.AuthUser;
import com.library.backend.service.reader.ReaderMembershipService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reader/membership")
public class ReaderMembershipController {

    private final ReaderMembershipService readerMembershipService;

    public ReaderMembershipController(ReaderMembershipService readerMembershipService) {
        this.readerMembershipService = readerMembershipService;
    }

    @GetMapping("/current")
    public MembershipPurchaseResponse getCurrentMembership(@AuthenticationPrincipal AuthUser user) {
        return readerMembershipService.getCurrentMembership(requireReader(user));
    }

    @GetMapping("/plans")
    public List<MembershipPlanResponse> getAvailablePlans(@AuthenticationPrincipal AuthUser user) {
        return readerMembershipService.getAvailablePlans(requireReader(user));
    }

    @GetMapping("/history")
    public List<MembershipPurchaseResponse> getMembershipHistory(@AuthenticationPrincipal AuthUser user) {
        return readerMembershipService.getMembershipHistory(requireReader(user));
    }

    @PostMapping("/purchase")
    public MembershipPurchaseResponse purchaseMembership(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody MembershipPurchaseRequest request
    ) {
        return readerMembershipService.purchaseMembership(
                requireReader(user),
                requireAccount(user),
                request
        );
    }

    private String requireReader(AuthUser user) {
        if (user == null || user.getMaDocGia() == null || user.getMaDocGia().isBlank()) {
            throw new BusinessException("Chỉ độc giả mới được dùng chức năng mua gói");
        }

        return user.getMaDocGia();
    }

    private String requireAccount(AuthUser user) {
        if (user == null || user.getMaTaiKhoan() == null || user.getMaTaiKhoan().isBlank()) {
            throw new BusinessException("Bạn cần đăng nhập");
        }

        return user.getMaTaiKhoan();
    }
}

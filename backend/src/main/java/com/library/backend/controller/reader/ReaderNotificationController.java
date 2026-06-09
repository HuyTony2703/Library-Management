package com.library.backend.controller.reader;

import com.library.backend.dto.reader.ReaderNotificationResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.security.AuthUser;
import com.library.backend.service.reader.ReaderNotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reader/notifications")
public class ReaderNotificationController {

    private final ReaderNotificationService readerNotificationService;

    public ReaderNotificationController(ReaderNotificationService readerNotificationService) {
        this.readerNotificationService = readerNotificationService;
    }

    @GetMapping
    public List<ReaderNotificationResponse> getMyNotifications(@AuthenticationPrincipal AuthUser user) {
        return readerNotificationService.getMyNotifications(requireAccount(user));
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@AuthenticationPrincipal AuthUser user) {
        int count = readerNotificationService.countUnread(requireAccount(user));
        return Map.of("unreadCount", count);
    }

    @PatchMapping("/{maThongBao}/read")
    public Map<String, Object> markAsRead(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maThongBao
    ) {
        readerNotificationService.markAsRead(requireAccount(user), maThongBao);

        return Map.of(
                "message", "Đã đánh dấu thông báo là đã đọc",
                "maThongBao", maThongBao
        );
    }

    @PatchMapping("/read-all")
    public Map<String, Object> markAllAsRead(@AuthenticationPrincipal AuthUser user) {
        int updated = readerNotificationService.markAllAsRead(requireAccount(user));

        return Map.of(
                "message", "Đã đánh dấu tất cả thông báo là đã đọc",
                "updated", updated
        );
    }

    private String requireAccount(AuthUser user) {
        if (user == null || user.getMaTaiKhoan() == null || user.getMaTaiKhoan().isBlank()) {
            throw new BusinessException("Bạn cần đăng nhập để xem thông báo");
        }

        if (user.getMaDocGia() == null || user.getMaDocGia().isBlank()) {
            throw new BusinessException("Chỉ độc giả mới được xem thông báo độc giả");
        }

        return user.getMaTaiKhoan();
    }
}

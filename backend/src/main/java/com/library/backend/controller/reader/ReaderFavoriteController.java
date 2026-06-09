package com.library.backend.controller.reader;

import com.library.backend.dto.reader.FavoriteBookResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.security.AuthUser;
import com.library.backend.service.reader.ReaderFavoriteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reader/favorites")
public class ReaderFavoriteController {

    private final ReaderFavoriteService readerFavoriteService;

    public ReaderFavoriteController(ReaderFavoriteService readerFavoriteService) {
        this.readerFavoriteService = readerFavoriteService;
    }

    @GetMapping
    public List<FavoriteBookResponse> getMyFavorites(@AuthenticationPrincipal AuthUser user) {
        return readerFavoriteService.getMyFavorites(requireReader(user));
    }

    @PostMapping("/{maDauSach}")
    public FavoriteBookResponse addFavorite(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maDauSach
    ) {
        return readerFavoriteService.addFavorite(requireReader(user), maDauSach);
    }

    @DeleteMapping("/{maDauSach}")
    public Map<String, Object> removeFavorite(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maDauSach
    ) {
        readerFavoriteService.removeFavorite(requireReader(user), maDauSach);

        return Map.of(
                "message", "Đã xóa khỏi sách yêu thích",
                "maDauSach", maDauSach
        );
    }

    @GetMapping("/{maDauSach}/exists")
    public Map<String, Object> existsFavorite(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maDauSach
    ) {
        boolean exists = readerFavoriteService.existsFavorite(requireReader(user), maDauSach);

        return Map.of(
                "maDauSach", maDauSach,
                "exists", exists
        );
    }

    private String requireReader(AuthUser user) {
        if (user == null || user.getMaDocGia() == null || user.getMaDocGia().isBlank()) {
            throw new BusinessException("Chỉ độc giả mới được dùng chức năng sách yêu thích");
        }

        return user.getMaDocGia();
    }
}

package com.library.backend.controller.reader;

import com.library.backend.dto.reader.ReaderRecommendationBookResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.security.AuthUser;
import com.library.backend.service.reader.ReaderRecommendationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reader/recommendations")
public class ReaderRecommendationController {

    private final ReaderRecommendationService readerRecommendationService;

    public ReaderRecommendationController(ReaderRecommendationService readerRecommendationService) {
        this.readerRecommendationService = readerRecommendationService;
    }

    @GetMapping
    public List<ReaderRecommendationBookResponse> getRecommendations(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "new") String type,
            @RequestParam(defaultValue = "12") Integer limit
    ) {
        return readerRecommendationService.getRecommendations(requireReader(user), type, limit);
    }

    @GetMapping("/random")
    public List<ReaderRecommendationBookResponse> getRandomRecommendations(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "6") Integer limit
    ) {
        return readerRecommendationService.getRandomRecommendations(requireReader(user), limit);
    }

    private String requireReader(AuthUser user) {
        if (user == null || user.getMaDocGia() == null || user.getMaDocGia().isBlank()) {
            throw new BusinessException("Chỉ độc giả mới được dùng chức năng gợi ý sách");
        }

        return user.getMaDocGia();
    }
}

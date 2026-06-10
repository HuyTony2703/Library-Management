package com.library.backend.controller.reader;

import com.library.backend.dto.reader.RatingRequest;
import com.library.backend.dto.reader.RatingSummaryResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.security.AuthUser;
import com.library.backend.service.reader.ReaderRatingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reader/books/{maDauSach}/ratings")
public class ReaderRatingController {

    private final ReaderRatingService readerRatingService;

    public ReaderRatingController(ReaderRatingService readerRatingService) {
        this.readerRatingService = readerRatingService;
    }

    @GetMapping("/summary")
    public RatingSummaryResponse getRatingSummary(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maDauSach
    ) {
        return readerRatingService.getRatingSummary(maDauSach, requireReader(user));
    }

    @PostMapping
    public RatingSummaryResponse createRating(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maDauSach,
            @RequestBody RatingRequest request
    ) {
        return readerRatingService.createRating(requireReader(user), maDauSach, request);
    }

    @PutMapping("/me")
    public RatingSummaryResponse updateMyRating(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maDauSach,
            @RequestBody RatingRequest request
    ) {
        return readerRatingService.updateMyRating(requireReader(user), maDauSach, request);
    }

    private String requireReader(AuthUser user) {
        if (user == null || user.getMaDocGia() == null || user.getMaDocGia().isBlank()) {
            throw new BusinessException("Chỉ độc giả mới được dùng chức năng đánh giá");
        }

        return user.getMaDocGia();
    }
}

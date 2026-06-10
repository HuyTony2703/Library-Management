package com.library.backend.controller.reader;

import com.library.backend.dto.reader.CommentRequest;
import com.library.backend.dto.reader.CommentResponse;
import com.library.backend.exception.BusinessException;
import com.library.backend.security.AuthUser;
import com.library.backend.service.reader.ReaderCommentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reader")
public class ReaderCommentController {

    private final ReaderCommentService readerCommentService;

    public ReaderCommentController(ReaderCommentService readerCommentService) {
        this.readerCommentService = readerCommentService;
    }

    @GetMapping("/books/{maDauSach}/comments")
    public List<CommentResponse> getComments(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maDauSach
    ) {
        return readerCommentService.getComments(maDauSach, requireReader(user));
    }

    @PostMapping("/books/{maDauSach}/comments")
    public CommentResponse createComment(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maDauSach,
            @RequestBody CommentRequest request
    ) {
        return readerCommentService.createComment(requireReader(user), maDauSach, request);
    }

    @PutMapping("/comments/{maBinhLuan}")
    public CommentResponse updateComment(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maBinhLuan,
            @RequestBody CommentRequest request
    ) {
        return readerCommentService.updateComment(requireReader(user), maBinhLuan, request);
    }

    @DeleteMapping("/comments/{maBinhLuan}")
    public Map<String, Object> deleteComment(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maBinhLuan
    ) {
        readerCommentService.deleteComment(requireReader(user), maBinhLuan);

        return Map.of(
                "message", "Đã xóa bình luận",
                "maBinhLuan", maBinhLuan
        );
    }

    private String requireReader(AuthUser user) {
        if (user == null || user.getMaDocGia() == null || user.getMaDocGia().isBlank()) {
            throw new BusinessException("Chỉ độc giả mới được dùng chức năng bình luận");
        }

        return user.getMaDocGia();
    }
}

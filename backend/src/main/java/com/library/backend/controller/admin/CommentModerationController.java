package com.library.backend.controller.admin;

import com.library.backend.dto.CommentModerationResponse;
import com.library.backend.dto.ModerateCommentRequest;
import com.library.backend.service.admin.CommentModerationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comments")
@PreAuthorize("hasAnyRole('QUAN_TRI_VIEN', 'THU_THU')")
public class CommentModerationController {

    private final CommentModerationService commentModerationService;

    public CommentModerationController(CommentModerationService commentModerationService) {
        this.commentModerationService = commentModerationService;
    }

    @GetMapping
    public List<CommentModerationResponse> getComments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String maDauSach,
            @RequestParam(required = false) String keyword
    ) {
        return commentModerationService.getComments(status, maDauSach, keyword);
    }

    @GetMapping("/{maBinhLuan}")
    public CommentModerationResponse getById(@PathVariable String maBinhLuan) {
        return commentModerationService.getById(maBinhLuan);
    }

    @PatchMapping("/{maBinhLuan}/hide")
    public CommentModerationResponse hide(
            @PathVariable String maBinhLuan,
            @RequestBody ModerateCommentRequest request
    ) {
        return commentModerationService.hide(maBinhLuan, request);
    }

    @PatchMapping("/{maBinhLuan}/delete")
    public CommentModerationResponse delete(
            @PathVariable String maBinhLuan,
            @RequestBody ModerateCommentRequest request
    ) {
        return commentModerationService.delete(maBinhLuan, request);
    }

    @PatchMapping("/{maBinhLuan}/restore")
    public CommentModerationResponse restore(
            @PathVariable String maBinhLuan,
            @RequestBody ModerateCommentRequest request
    ) {
        return commentModerationService.restore(maBinhLuan, request);
    }
}

package com.library.backend.controller.reader;

import com.library.backend.dto.reader.ReaderProfileResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.reader.ReaderProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reader")
public class ReaderProfileController {

    private final ReaderProfileService readerProfileService;

    public ReaderProfileController(ReaderProfileService readerProfileService) {
        this.readerProfileService = readerProfileService;
    }

    @GetMapping("/me")
    public ReaderProfileResponse me(@AuthenticationPrincipal AuthUser user) {
        return readerProfileService.getCurrentReaderProfile(user);
    }
}

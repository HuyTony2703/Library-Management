package com.library.backend.controller.reader;

import com.library.backend.dto.reader.ReaderCurrentRuleResponse;
import com.library.backend.service.reader.ReaderRuleGuideService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reader/rules")
public class ReaderRuleGuideController {

    private final ReaderRuleGuideService readerRuleGuideService;

    public ReaderRuleGuideController(ReaderRuleGuideService readerRuleGuideService) {
        this.readerRuleGuideService = readerRuleGuideService;
    }

    @GetMapping("/current")
    public ReaderCurrentRuleResponse getCurrentRules() {
        return readerRuleGuideService.getCurrentRules();
    }
}

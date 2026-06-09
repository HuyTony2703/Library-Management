package com.library.backend.controller.admin;

import com.library.backend.dto.RuleCreateRequest;
import com.library.backend.dto.RuleDetailResponse;
import com.library.backend.service.admin.AdminRuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rules")
public class AdminRuleController {

    private final AdminRuleService adminRuleService;

    public AdminRuleController(AdminRuleService adminRuleService) {
        this.adminRuleService = adminRuleService;
    }

    @GetMapping("/current")
    public RuleDetailResponse getCurrent() {
        return adminRuleService.getCurrent();
    }

    @GetMapping("/history")
    public List<RuleDetailResponse> getHistory() {
        return adminRuleService.getHistory();
    }

    @GetMapping("/{maPhienBan}")
    public RuleDetailResponse getById(@PathVariable String maPhienBan) {
        return adminRuleService.getById(maPhienBan);
    }

    @PostMapping
    public RuleDetailResponse create(@RequestBody RuleCreateRequest request) {
        return adminRuleService.create(request);
    }

    @PostMapping("/{maPhienBan}/activate")
    public RuleDetailResponse activate(@PathVariable String maPhienBan) {
        return adminRuleService.activate(maPhienBan);
    }
}

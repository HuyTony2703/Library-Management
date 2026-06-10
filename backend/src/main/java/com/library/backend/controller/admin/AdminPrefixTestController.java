package com.library.backend.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminPrefixTestController {

    @GetMapping("/api/admin/prefix-test")
    public String testAdminPrefix() {
        return "ADMIN_PREFIX_OK";
    }
}

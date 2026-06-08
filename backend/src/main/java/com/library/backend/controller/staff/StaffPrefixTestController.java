package com.library.backend.controller.staff;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaffPrefixTestController {

    @GetMapping("/api/staff/prefix-test")
    public String testStaffPrefix() {
        return "STAFF_PREFIX_OK";
    }
}

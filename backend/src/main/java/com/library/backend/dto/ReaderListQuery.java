package com.library.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record ReaderListQuery(
        int page,
        int pageSize,
        String search,
        String sort,
        List<String> groupIds,
        List<String> planIds,
        List<String> profileStatuses,
        List<String> accountStatuses,
        String cardStatus,
        String membershipStatus,
        LocalDate cardExpiryFrom,
        LocalDate cardExpiryTo,
        LocalDate membershipExpiryFrom,
        LocalDate membershipExpiryTo,
        Boolean locked
) {}

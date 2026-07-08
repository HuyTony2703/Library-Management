package com.library.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record CuonSachListQuery(
        int page,
        int pageSize,
        String search,
        String sort,
        List<String> statusIds,
        List<String> branchIds,
        List<String> titleIds,
        List<String> areaIds,
        List<String> shelfIds,
        List<String> locationIds,
        LocalDate importedFrom,
        LocalDate importedTo,
        Boolean hasBarcode,
        Boolean hasQr
) {
}

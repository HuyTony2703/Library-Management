package com.library.backend.dto;

import java.util.List;

public record DauSachListQuery(
        int page,
        int pageSize,
        String search,
        String sort,
        List<String> statusIds,
        List<String> categoryIds,
        List<String> authorIds,
        List<String> publisherIds,
        Integer yearFrom,
        Integer yearTo,
        String language,
        Boolean hasIsbn,
        Boolean hasCover
) {
    public DauSachListQuery {
        search = trimToNull(search);
        sort = trimToNull(sort);
        statusIds = clean(statusIds);
        categoryIds = clean(categoryIds);
        authorIds = clean(authorIds);
        publisherIds = clean(publisherIds);
        language = trimToNull(language);
    }

    private static List<String> clean(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(DauSachListQuery::trimToNull)
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

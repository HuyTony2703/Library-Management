package com.library.backend.dto;

import java.util.List;

public record BookCopyLocationFiltersResponse(
        List<LocationFilterOptionResponse> areas,
        List<LocationFilterOptionResponse> shelves,
        List<LocationFilterOptionResponse> locations
) {
    public BookCopyLocationFiltersResponse {
        areas = areas == null ? List.of() : List.copyOf(areas);
        shelves = shelves == null ? List.of() : List.copyOf(shelves);
        locations = locations == null ? List.of() : List.copyOf(locations);
    }
}

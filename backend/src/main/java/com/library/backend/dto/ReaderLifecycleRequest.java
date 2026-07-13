package com.library.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReaderLifecycleRequest(@NotBlank @Size(max = 500) String reason) {}

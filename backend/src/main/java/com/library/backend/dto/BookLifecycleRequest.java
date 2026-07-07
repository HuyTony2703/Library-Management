package com.library.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookLifecycleRequest {
    @NotBlank(message = "Lý do không được để trống")
    @Size(max = 500, message = "Lý do tối đa 500 ký tự")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

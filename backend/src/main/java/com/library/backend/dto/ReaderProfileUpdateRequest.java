package com.library.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ReaderProfileUpdateRequest(
        @NotBlank String maNhomDocGia,
        @NotBlank @Size(max = 150) String hoTen,
        @NotNull LocalDate ngaySinh,
        @Size(max = 255) String diaChi,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 20) String soDienThoai
) {}

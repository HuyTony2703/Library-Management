package com.library.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ReaderMembershipUpdateRequest {

    @NotBlank(message = "Mã gói thành viên không được để trống")
    private String maGoiThanhVien;

    public String getMaGoiThanhVien() {
        return maGoiThanhVien;
    }

    public void setMaGoiThanhVien(String maGoiThanhVien) {
        this.maGoiThanhVien = maGoiThanhVien;
    }
}

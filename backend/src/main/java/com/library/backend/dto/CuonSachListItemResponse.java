package com.library.backend.dto;

import java.time.LocalDate;

public record CuonSachListItemResponse(
        String maCuonSach,
        String maDauSach,
        String tenDauSach,
        String isbn,
        String maChiNhanh,
        String tenChiNhanh,
        String maKhu,
        String tenKhu,
        String maKeSach,
        String tenKeSach,
        String maViTri,
        String viTriLabel,
        String maTrangThai,
        String tenTrangThai,
        String maVach,
        String maQrCode,
        LocalDate ngayNhapSach,
        String ghiChu
) {
}

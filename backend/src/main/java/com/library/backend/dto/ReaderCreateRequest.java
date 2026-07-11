package com.library.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ReaderCreateRequest {
    @NotBlank private String maDocGia;
    @NotBlank private String maTaiKhoan;
    @NotBlank private String tenDangNhap;
    @NotBlank @Size(min = 8, max = 100) private String matKhau;
    @NotBlank private String maNhomDocGia;
    private String maGoiThanhVien;
    @NotBlank @Size(max = 150) private String hoTen;
    @NotNull private LocalDate ngaySinh;
    @Size(max = 255) private String diaChi;
    @NotBlank @Email @Size(max = 255) private String email;
    @Size(max = 20) private String soDienThoai;
    private LocalDate ngayLapThe;

    public DocGiaRequest toLegacyRequest() {
        DocGiaRequest value = new DocGiaRequest();
        value.setMaDocGia(maDocGia); value.setMaTaiKhoan(maTaiKhoan); value.setTenDangNhap(tenDangNhap);
        value.setMatKhau(matKhau); value.setMaNhomDocGia(maNhomDocGia); value.setMaGoiThanhVien(maGoiThanhVien);
        value.setHoTen(hoTen); value.setNgaySinh(ngaySinh); value.setDiaChi(diaChi);
        value.setEmail(email); value.setSoDienThoai(soDienThoai); value.setNgayLapThe(ngayLapThe);
        return value;
    }

    public String getMaDocGia() { return maDocGia; } public void setMaDocGia(String v) { maDocGia = v; }
    public String getMaTaiKhoan() { return maTaiKhoan; } public void setMaTaiKhoan(String v) { maTaiKhoan = v; }
    public String getTenDangNhap() { return tenDangNhap; } public void setTenDangNhap(String v) { tenDangNhap = v; }
    public String getMatKhau() { return matKhau; } public void setMatKhau(String v) { matKhau = v; }
    public String getMaNhomDocGia() { return maNhomDocGia; } public void setMaNhomDocGia(String v) { maNhomDocGia = v; }
    public String getMaGoiThanhVien() { return maGoiThanhVien; } public void setMaGoiThanhVien(String v) { maGoiThanhVien = v; }
    public String getHoTen() { return hoTen; } public void setHoTen(String v) { hoTen = v; }
    public LocalDate getNgaySinh() { return ngaySinh; } public void setNgaySinh(LocalDate v) { ngaySinh = v; }
    public String getDiaChi() { return diaChi; } public void setDiaChi(String v) { diaChi = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getSoDienThoai() { return soDienThoai; } public void setSoDienThoai(String v) { soDienThoai = v; }
    public LocalDate getNgayLapThe() { return ngayLapThe; } public void setNgayLapThe(LocalDate v) { ngayLapThe = v; }
}

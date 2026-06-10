package com.library.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminLibrarianResponse {

    private String maNhanVien;
    private String maTaiKhoan;
    private String tenDangNhap;
    private String emailDangNhap;
    private String maVaiTro;
    private String maChiNhanh;
    private String tenChiNhanh;
    private String hoTen;
    private LocalDate ngaySinh;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private LocalDate ngayVaoLam;
    private String trangThaiTaiKhoan;
    private String trangThaiNhanVien;
    private LocalDateTime lanDangNhapCuoi;

    public AdminLibrarianResponse(
            String maNhanVien,
            String maTaiKhoan,
            String tenDangNhap,
            String emailDangNhap,
            String maVaiTro,
            String maChiNhanh,
            String tenChiNhanh,
            String hoTen,
            LocalDate ngaySinh,
            String email,
            String soDienThoai,
            String diaChi,
            LocalDate ngayVaoLam,
            String trangThaiTaiKhoan,
            String trangThaiNhanVien,
            LocalDateTime lanDangNhapCuoi
    ) {
        this.maNhanVien = maNhanVien;
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.emailDangNhap = emailDangNhap;
        this.maVaiTro = maVaiTro;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.ngayVaoLam = ngayVaoLam;
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
        this.trangThaiNhanVien = trangThaiNhanVien;
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }

    public String getMaNhanVien() { return maNhanVien; }
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getEmailDangNhap() { return emailDangNhap; }
    public String getMaVaiTro() { return maVaiTro; }
    public String getMaChiNhanh() { return maChiNhanh; }
    public String getTenChiNhanh() { return tenChiNhanh; }
    public String getHoTen() { return hoTen; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public String getEmail() { return email; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public LocalDate getNgayVaoLam() { return ngayVaoLam; }
    public String getTrangThaiTaiKhoan() { return trangThaiTaiKhoan; }
    public String getTrangThaiNhanVien() { return trangThaiNhanVien; }
    public LocalDateTime getLanDangNhapCuoi() { return lanDangNhapCuoi; }
}

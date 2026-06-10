package com.library.backend.dto;

public class AuthResponse {

    private String token;
    private String tokenType;
    private String maTaiKhoan;
    private String tenDangNhap;
    private String maVaiTro;
    private String tenVaiTro;
    private String maDocGia;
    private String maNhanVien;
    private String hoTen;

    public AuthResponse(
            String token,
            String tokenType,
            String maTaiKhoan,
            String tenDangNhap,
            String maVaiTro,
            String tenVaiTro,
            String maDocGia,
            String maNhanVien,
            String hoTen
    ) {
        this.token = token;
        this.tokenType = tokenType;
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.maDocGia = maDocGia;
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMaVaiTro() { return maVaiTro; }
    public String getTenVaiTro() { return tenVaiTro; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaNhanVien() { return maNhanVien; }
    public String getHoTen() { return hoTen; }
}

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
    private String email;
    private String soDienThoai;
    private String diaChi;
    private boolean mustChangePassword;

    public AuthResponse(
            String token,
            String tokenType,
            String maTaiKhoan,
            String tenDangNhap,
            String maVaiTro,
            String tenVaiTro,
            String maDocGia,
            String maNhanVien,
            String hoTen,
            String email,
            String soDienThoai,
            String diaChi,
            boolean mustChangePassword
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
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.mustChangePassword = mustChangePassword;
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
    public String getEmail() { return email; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}

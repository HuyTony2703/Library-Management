package com.library.backend.security;

public class AuthUser {

    private final String maTaiKhoan;
    private final String tenDangNhap;
    private final String maVaiTro;
    private final String tenVaiTro;
    private final String maDocGia;
    private final String maNhanVien;

    public AuthUser(
            String maTaiKhoan,
            String tenDangNhap,
            String maVaiTro,
            String tenVaiTro,
            String maDocGia,
            String maNhanVien
    ) {
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.maDocGia = maDocGia;
        this.maNhanVien = maNhanVien;
    }

    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMaVaiTro() { return maVaiTro; }
    public String getTenVaiTro() { return tenVaiTro; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaNhanVien() { return maNhanVien; }

    public String getRoleAuthority() {
        return RoleConstants.toAuthority(tenVaiTro);
    }
}

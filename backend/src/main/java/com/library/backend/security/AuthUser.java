package com.library.backend.security;

public class AuthUser {

    private final String maTaiKhoan;
    private final String tenDangNhap;
    private final String maVaiTro;
    private final String tenVaiTro;
    private final String maDocGia;
    private final String maNhanVien;
    private final String hoTen;
    private final long tokenVersion;
    private final boolean mustChangePassword;

    public AuthUser(
            String maTaiKhoan,
            String tenDangNhap,
            String maVaiTro,
            String tenVaiTro,
            String maDocGia,
            String maNhanVien,
            String hoTen
    ) {
        this(maTaiKhoan, tenDangNhap, maVaiTro, tenVaiTro, maDocGia, maNhanVien, hoTen, 0, false);
    }

    public AuthUser(
            String maTaiKhoan, String tenDangNhap, String maVaiTro, String tenVaiTro,
            String maDocGia, String maNhanVien, String hoTen,
            long tokenVersion, boolean mustChangePassword
    ) {
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.maDocGia = maDocGia;
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.tokenVersion = tokenVersion;
        this.mustChangePassword = mustChangePassword;
    }

    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMaVaiTro() { return maVaiTro; }
    public String getTenVaiTro() { return tenVaiTro; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaNhanVien() { return maNhanVien; }
    public String getHoTen() { return hoTen; }
    public long getTokenVersion() { return tokenVersion; }
    public boolean isMustChangePassword() { return mustChangePassword; }

    public String getRoleAuthority() {
        return RoleConstants.toAuthority(tenVaiTro);
    }
}

package com.library.backend.dto;

import java.time.LocalDateTime;

public class CommentModerationResponse {

    private final String maBinhLuan;
    private final String maDocGia;
    private final String hoTenDocGia;
    private final String maDauSach;
    private final String tenDauSach;
    private final String noiDung;
    private final LocalDateTime ngayBinhLuan;
    private final String trangThai;
    private final String maNhanVienXuLy;
    private final String hoTenNhanVienXuLy;
    private final String lyDoAnXoa;

    public CommentModerationResponse(
            String maBinhLuan,
            String maDocGia,
            String hoTenDocGia,
            String maDauSach,
            String tenDauSach,
            String noiDung,
            LocalDateTime ngayBinhLuan,
            String trangThai,
            String maNhanVienXuLy,
            String hoTenNhanVienXuLy,
            String lyDoAnXoa
    ) {
        this.maBinhLuan = maBinhLuan;
        this.maDocGia = maDocGia;
        this.hoTenDocGia = hoTenDocGia;
        this.maDauSach = maDauSach;
        this.tenDauSach = tenDauSach;
        this.noiDung = noiDung;
        this.ngayBinhLuan = ngayBinhLuan;
        this.trangThai = trangThai;
        this.maNhanVienXuLy = maNhanVienXuLy;
        this.hoTenNhanVienXuLy = hoTenNhanVienXuLy;
        this.lyDoAnXoa = lyDoAnXoa;
    }

    public String getMaBinhLuan() { return maBinhLuan; }
    public String getMaDocGia() { return maDocGia; }
    public String getHoTenDocGia() { return hoTenDocGia; }
    public String getMaDauSach() { return maDauSach; }
    public String getTenDauSach() { return tenDauSach; }
    public String getNoiDung() { return noiDung; }
    public LocalDateTime getNgayBinhLuan() { return ngayBinhLuan; }
    public String getTrangThai() { return trangThai; }
    public String getMaNhanVienXuLy() { return maNhanVienXuLy; }
    public String getHoTenNhanVienXuLy() { return hoTenNhanVienXuLy; }
    public String getLyDoAnXoa() { return lyDoAnXoa; }
}
